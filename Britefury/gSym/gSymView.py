##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Cell.Cell import Cell

from Britefury.DocPresent.Toolkit.DTWidget import DTWidget
from Britefury.DocPresent.Toolkit.DTActiveBorder import DTActiveBorder
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTEntryLabel import DTEntryLabel
from Britefury.DocPresent.Toolkit.DTBox import DTBox

from Britefury.DocView.DVNode import DVNode
from Britefury.DocView.DVCustomNode import DVCustomNode
from Britefury.DocView.DocView import DocView


from Britefury.GLisp.GLispInterpreter import GLispParameterListError, GLispItemTypeError, isGLispList
from Britefury.GLisp.GLispCompiler import compileGLispFunctionToPy, compileGLispExprToPySrc, GLispCompilerError, filterIdentifierForPy
from Britefury.GLisp.PatternMatch import compileMatchExpression, NoMatchError




"""
A bried explanation as to how this module works.


The view is specified as a pattern match expression.
Each match is matched with a view expression that is compiled into Python source which is executed to create the contents of a view node.


The code that is executed to create a view is split into two parts:
  - compiled code
    - match expressions
    - view expressions
  - non-compiled code
    - made available to compiled code
    - prefixed by _runtime_
    
There are 3 levels of operation:
1. View node instance level
  - Handles a view of a specific subtree of the document
2. View instance level
  - Handles the whole document
3. View factory level
  - Creates a view instance
  
  
Refresh policy

DVNode instances have a content refresh system which will allow some level of automation for keeping the view of the document in sync with the document contents.
A DVNode displays its content in a widget hierarchy.
A DVNode has a refresh cell that will result in refreshing its contents
The node can be passed a list of cells that are refreshed in its refresh-cell function.
This means that these cells will be refreshed when the overall node cell is refreshed.
Hierarchy of view-expression is ignored here;
the node refresh cell invoked all cells from the view-expression directly (from a list).
The hierarchy of document view nodes is respected however.
"""




class GSymViewDefinition (object):
	"""The language view definition
	An instance of GSymViewDefinition is created and returned by defineView()"""
	def __init__(self, name, docFormat, viewFactory):
		self.name = name
		self.docFormat = docFormat
		self._viewFactory = viewFactory
		

	def createDocumentView(self, xs, commandHistory, styleSheetDispatcher):
		viewInstance = self._viewFactory.createInstance( xs, commandHistory, styleSheetDispatcher )
		return viewInstance.view



class _ViewNodeAndIndrection (object):
	"""A helper class for storing the results of view computations"""
	def __init__(self, viewNode, indirection):
		self.viewNode = viewNode
		self.indirection = indirection


def _runtime_buildRefreshCellAndRegister(viewNodeInstance, refreshFunction):
	"""
	Runtime - called by compiled code at run-time
	Builds a refresh cell, and registers it by appending it to @refreshCells
	"""
	cell = Cell()
	cell.function = refreshFunction
	viewNodeInstance.refreshCells.append( cell )

def _runtime_binRefreshCell(viewNodeInstance, bin, child):
	"""
	Runtime - called by compiled code at run-time
	Builds and registers a refresh cell (if necessary) for a widget that is an instance of DTBin
	"""
	if isinstance( child, _ViewNodeAndIndrection )  or  isinstance( child, DVNode ):
		if isinstance( child, _ViewNodeAndIndrection ):
			chNode = child.viewNode
		else:
			chNode = child
		def _binRefresh():
			chNode.refresh()
			bin.child = chNode.widget
		_runtime_buildRefreshCellAndRegister( viewNodeInstance, _binRefresh )
	elif isinstance( child, DTWidget ):
		bin.child = child
	else:
		viewNodeInstance.env.glispError( TypeError, viewNodeInstance.xs, '_GSymNodeViewInstance._binRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )

def _runtime_boxRefreshCell(viewNodeInstance, widget, children):
	"""
	Runtime - called by compiled code at run-time
	Builds and registers a refresh cell (if necessary) for a widget that is an instance of DTBox
	"""
	def _boxRefresh():
		widgets = []
		for child in children:
			if isinstance( child, _ViewNodeAndIndrection ):
				child.viewNode.refresh()
				widgets.append( child.viewNode.widget )
			elif isinstance( child, DVNode ):
				child.refresh()
				widgets.append( child.widget )
			elif isinstance( child, DTWidget ):
				widgets.append( child )
			else:
				viewNodeInstance.env.glispError( TypeError, viewNodeInstance.xs, 'defineView: _boxRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )
		widget[:] = widgets
	_runtime_buildRefreshCellAndRegister( viewNodeInstance, _boxRefresh )

	
	
	
def _runtime_activeBorder(viewNodeInstance, child, styleSheets):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTActiveBorder widget, with child, builds and registers a refresh cell
	"""
	widget = DTActiveBorder()
	widget.keyHandler = viewNodeInstance.viewNode
	_runtime_binRefreshCell( viewNodeInstance, widget, child )
	for sheet in styleSheets:
		sheet.apply()
	return widget

def _runtime_label(viewNodeInstance, text, styleSheets):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTLabel widget
	"""
	if isinstance( text, _ViewNodeAndIndrection ):
		text = text.viewNode
	widget = DTLabel(text)
	for sheet in styleSheets:
		sheet.apply()
	return widget

def _runtime_entry(viewNodeInstance, text, styleSheets):
	"""Builds a DTEntryLabel widget"""
	if isinstance( text, _ViewNodeAndIndrection ):
		text = text.viewNode
	widget = DTEntryLabel(text)
	widget.keyHandler = viewNodeInstance.viewNode
	for sheet in styleSheets:
		sheet.apply()
	return widget

def _runtime_hbox(viewNodeInstance, children, styleSheets):
	"""
	Runtime - called by compiled code at run-time
	Builds a horizontal DTBox widget, with child, builds and registers a refresh cell
	"""
	widget = DTBox()
	_runtime_boxRefreshCell( viewNodeInstance, widget, children )
	for sheet in styleSheets:
		sheet.apply()
	return widget








	

class _GSymNodeViewInstance (object):
	"""
	Manages state that concerns a view of a specific sub-tree of a document
	"""
	
	__slots__ = [ 'env', 'xs', 'view', 'viewInstance', 'viewFactory', 'refreshCells', 'viewNode' ]

	def __init__(self, env, xs, view, viewInstance, viewFactory, viewNode):
		self.env = env
		self.xs = xs
		self.view = view
		self.viewInstance = viewInstance
		self.viewFactory = viewFactory
		self.viewNode = viewNode
		self.refreshCells = []

		

	
	def _runtime_buildViewContents(self, content):
		"""Runtime - build the contents of a view node"""
		#1. Ese the match function in the factory to determine which view expression to apply it
		try:
			varValues, index = self.viewFactory.matchFunction( content )
		except NoMatchError:
			self.env.glispError( NoMatchError, content, 'buildViewContents: cannot process; no suitable match expression found' )
		#2. Get the view expression function from the table, along with the varName->valueIndirection table for that function
		f, varNameToValueIndirection = self.viewFactory.viewExprFunctionAndVarNameToIndirectionPairs[index]
		
		#3. Mix the variable values with value indirection
		varValuesWithIndireciton = {}
		for varName, viewNode in varValues.items():
			varValuesWithIndireciton[varName] = _ViewNodeAndIndrection( viewNode, varNameToValueIndirection[varName] )
		
		#4. Call the view expression function
		viewContents = f( self, self.refreshCells, **varValuesWithIndireciton )
		
		#5. Return the view contents
		return viewContents
		
		

	
	
	
	
	
	
	
class _GSymViewInstance (object):
	"""
	Manages state concerning a view of a specific document
	"""
	__slots__ = [ 'env', 'xs', 'viewFactory', 'view' ]

	def __init__(self, env, xs, viewFactory, commandHistory, styleSheetDispatcher):
		self.env = env
		self.xs = xs
		self.viewFactory = viewFactory
		# self._p_buildDVNode is a factory that builds DVNode instances for document subtrees
		self.view = DocView( self.xs, commandHistory, styleSheetDispatcher, self._p_buildDVNode )
		
	
	def _p_buildDVNode(self, docNode, view, docNodeKey):
		# Build a DVNode for the document subtree at @docNode
		# self._p_buildNodeContents is a factory that builds the contents withing the DVNode
		return DVCustomNode( docNode, view, docNodeKey, self._p_buildNodeContents )
	
	
	def _p_buildNodeContents(self, viewNode, docNodeKey):
		# Create the node view instance
		nodeViewInstance = _GSymNodeViewInstance( self.env, docNodeKey.docNode, self.view, self, self.viewFactory, viewNode )
		# Build the contents
		viewContents = nodeViewInstance._runtime_buildViewContents( docNodeKey.docNode )
		# Get the refresh cells that need to be monitored, and hand them to the DVNode
		viewNode._f_setRefreshCells( nodeViewInstance.refreshCells )
		# Return the contents
		return viewContents
	
	
	
class _GSymViewFactory (object):
	"""
	Used to manufacture document views
	Manages state concerning a view that has been compiled.
	"""
	def __init__(self, env, name, spec):
		self.env = env
		self.name = name
		
		# Build the match expression
		self.matchFunction, varNameToValueIndirectionByMatch = compileMatchExpression( spec, [0], filterIdentifierForPy( 'view_match_%s'  %  ( self.name, ) ) )
		
		# Build the view-expressions that create the view contents (1 for each match expression)
		self.viewExprFunctionAndVarNameToIndirectionPairs = [ self._p_generateExprFunctionAndValueIndirection( matchAndViewExpr, i, varNameToValueIndir )
					   for i, ( matchAndViewExpr, varNameToValueIndir ) in enumerate( zip( spec, varNameToValueIndirectionByMatch ) ) ]
		
		
	def createInstance(self, xs, commandHistory, styleSheetDispatcher):
		"""Create a view instance"""
		return _GSymViewInstance( self.env, xs, self, commandHistory, styleSheetDispatcher )


	
	def _p_generateExprFunctionAndValueIndirection(self, matchAndViewExpr, i, varNameToValueIndirection):
		"""Helper function for the constructor
		Compiles a GLisp view-expression to a python function"""
		functionName = filterIdentifierForPy( 'view_expr_%s_%d'  %  ( self.name, i ) )
		paramNames = [ '__view_node_instance__', '__refreshCells__' ]  +  list( varNameToValueIndirection.keys() )
		lcls = { '_buildView': self._runtime_buildView,
			 '_buildViewForMap' : self._runtime_buildViewForMap,
			 '_activeBorder' : _runtime_activeBorder,
			 '_label' : _runtime_label,
			 '_entry' : _runtime_entry,
			 '_hbox' : _runtime_hbox, }
		return compileGLispFunctionToPy( matchAndViewExpr[1], functionName, paramNames, self._p_compileSpecial, lcls  ), varNameToValueIndirection
	
	
	def _runtime_buildView(self, viewNodeInstance, content):
		"""Build a view for a document subtree (@content)"""
		if not isinstance( content, _ViewNodeAndIndrection ):
			self.env.glispError( TypeError, None, '_GSymViewFactory._runtime_buildView: content is not a _ViewNodeAndIndrection' )
		# Need the indirection to get parentage; used by DocNode._f_buildView
		xs, indirection = content.viewNode, content.indirection
		parentViewNode = viewNodeInstance.viewNode
		indexInParentDocNode = indirection[-1]
		# A call to DocNode._f_buildView builds the view, and puts it in the DocView's table
		return viewNodeInstance.view._f_buildView( xs, parentViewNode, indexInParentDocNode )
		
	def _runtime_buildViewForMap(self, viewNodeInstance, content, index):
		"""Build a view for a document subtree (@content)
		Like the above, but when applied to a list"""
		if not isinstance( content, _ViewNodeAndIndrection ):
			self.env.glispError( TypeError, None, '_GSymViewFactory._runtime_buildViewForMap: content is not a _ViewNodeAndIndrection' )
		# Need the indirection to get parentage; used by DocNode._f_buildView
		xs, indirection = content.viewNode, content.indirection
		parentViewNode = viewNodeInstance.viewNode
		lastIndirection = indirection[-1]
		if isinstance( lastIndirection, tuple ):
			indexInParentDocNode = lastIndirection[0] + index
		else:
			indexInParentDocNode = index
		# A call to DocNode._f_buildView builds the view, and puts it in the DocView's table
		return viewNodeInstance.view._f_buildView( xs, parentViewNode, indexInParentDocNode )

		
	
	def _p_compileStylesheetAccess(self, src):
		"""Compile style-sheet access
		TODO"""
		return '[]'
	
	def _p_compileSubExp(self, src):
		"""Helper for compiling a sub-expression"""
		return compileGLispExprToPySrc( src, self._p_compileSpecial )
	
	def _p_compileSpecial(self, src):
		"""Compile special statements specific to view expressions"""
		name = src[0]
		if name == '/viewEval':
			# (/viewEval <document-subtree>)
			return '_buildView( __view_node_instance__, %s )'  %  ( self._p_compileSubExp( src[1] ), )
		elif name == '/mapViewEval':
			# (/mapViewEval <document-subtree>)
			return '[ _buildViewForMap( __view_node_instance__, x, i )   for i, x in enumerate( %s ) ]'  %  ( self._p_compileSubExp( src[1] ), )
		elif name == '/activeBorder':
			#(/activeBorder <child> styleSheet*)
			if len( src ) < 2:
				self.env.glispError( GLispParameterListError, src, 'defineView: /activeBorder needs at least 1 parameter; the child content' )
			return '_activeBorder( __view_node_instance__, %s, %s )'  %  ( self._p_compileSubExp( src[1] ), self._p_compileStylesheetAccess( src[2:] ) )
		elif name == '/label':
			#(/label text styleSheet*)
			if len( src ) < 2:
				self.env.glispError( GLispParameterListError, src, 'defineView: /label needs at least 1 parameter; the text' )
			return '_label( __view_node_instance__, %s, %s )'  %  ( self._p_compileSubExp( src[1] ), self._p_compileStylesheetAccess( src[2:] ) )
		elif name == '/entry':
			#(/entry text styleSheet*)
			if len( src ) < 2:
				self.env.glispError( GLispParameterListError, src, 'defineView: /entry needs at least 1 parameter; the text' )
			return '_entry( __view_node_instance__, %s, %s )'  %  ( self._p_compileSubExp( src[1] ), self._p_compileStylesheetAccess( src[2:] ) )
		elif name == '/hbox':
			#(/hbox (child*) styleSheet*)
			if len( src ) < 2:
				self.env.glispError( GLispParameterListError, src, 'defineView: /hbox needs at least 1 parameter; the children' )
			return '_hbox( __view_node_instance__, %s, %s )'  %  ( self._p_compileSubExp( src[1] ), self._p_compileStylesheetAccess( src[2:] ) )
		else:
			self.env.glispError( GLispCompilerError, src, 'cannot compile special \'%s\''  %  ( src[0], ) )
		

			
			
			
def defineView(env, xs, name, docFormat, spec):
	viewFactory = _GSymViewFactory( env, name, spec )
	
	return GSymViewDefinition( name, docFormat, viewFactory )

# WE NEED TO COMPILE:
# Node contents factory factory
#        nodeContentsFactoryFactory( xs )  ->  nodeContentsFactory()  ->  the contents for a DVCustomNode
