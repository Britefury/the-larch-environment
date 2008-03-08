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
from Britefury.GLisp.GLispCompiler import compileGLispExprToPyFunction
from Britefury.GLisp.PyCodeGen import filterIdentifierForPy, PyCodeGenError, PySrc, PyVar, PyLiteral, PyListLiteral, PyListComprehension, PyGetAttr, PyGetItem, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyReturn, PyIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects

from Britefury.gSym.RelativeNode import RelativeNode, relative



"""
A brief explanation as to how this module works.


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
	if isinstance( child, DVNode ):
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
			if isinstance( child, DVNode ):
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
	if isinstance( text, RelativeNode ):
		text = text.node
	widget = DTLabel(text)
	for sheet in styleSheets:
		sheet.apply()
	return widget

def _runtime_entry(viewNodeInstance, text, styleSheets):
	"""Builds a DTEntryLabel widget"""
	if isinstance( text, RelativeNode ):
		text = text.node
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
		#1. Get the view function factory from the view factory
		viewFunctionFactory = self.viewFactory.viewFunctionFactory
		
		#2. Create the view function. (the first parameter is the view instance; self)
		viewFunction = viewFunctionFactory( self )
		
		#3. Call the view function to create the view contents
		viewContents = viewFunction( content )
		
		#4. Return the view contents
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
		relativeNode = relative( docNodeKey.docNode, docNodeKey.parentDocNode, docNodeKey.index )
		# Build the contents
		viewContents = nodeViewInstance._runtime_buildViewContents( relativeNode )
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
		
		viewFunctionName = filterIdentifierForPy( 'viewFactory_%s'  %  ( name, ) )
		
		lcls = { '_buildView': self._runtime_buildView,
			 '_activeBorder' : _runtime_activeBorder,
			 '_label' : _runtime_label,
			 '_entry' : _runtime_entry,
			 '_hbox' : _runtime_hbox, }
		self.viewFunctionFactory = compileGLispExprToPyFunction( viewFunctionName, [ '__view_node_instance__' ], spec, self._p_compileSpecial, lcls=lcls )
		
		
		
	def createInstance(self, xs, commandHistory, styleSheetDispatcher):
		"""Create a view instance"""
		return _GSymViewInstance( self.env, xs, self, commandHistory, styleSheetDispatcher )


	
	def _runtime_buildView(self, viewNodeInstance, content):
		"""Build a view for a document subtree (@content)"""
		if not isinstance( content, RelativeNode ):
			self.env.glispError( TypeError, None, '_GSymViewFactory._runtime_buildView: content is not a RelativeNode' )
		# A call to DocNode._f_buildView builds the view, and puts it in the DocView's table
		return viewNodeInstance.view._f_buildView( content.node, content.parent, content.indexInParent )
		
	
	
	def _p_compileStylesheetAccess(self, srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
		"""Compile style-sheet access
		TODO"""
		return PyListLiteral( [], dbgSrc=srcXs )
	
	def _p_compileSubExp(self, src):
		"""Helper for compiling a sub-expression"""
		return compileGLispExprToPySrc( src, self._p_compileSpecial )
	
	def _p_compileSpecial(self, srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
		"""Compile special statements specific to view expressions"""
		name = srcXs[0]

		compileSubExp = lambda xs: compileGLispExprToPyTree( xs, context, True, compileSpecial )
		compileStyleSheetAccess = lambda xs: self._p_compileStylesheetAccess( xs, context, True, compileSpecial, compileGLispExprToPyTree )
	
		if name == '$viewEval':
			# ($viewEval <document-subtree>)
			return PyCall( PyVar( '_buildView', dbgSrc=srcXs ), [ PyVar( '__view_node_instance__', dbgSrc=srcXs ), compileSubExp( srcXs[1] ) ], dbgSrc=srcXs )
		elif name == '$mapViewEval':
			# ($mapViewEval <document-subtree>)
			itemExpr = PyCall( PyVar( '_buildView', dbgSrc=srcXs ), [ PyVar( '__view_node_instance__', dbgSrc=srcXs ), PyVar( 'x', dbgSrc=srcXs ) ], dbgSrc=srcXs )
			return PyListComprehension( itemExpr, 'x', compileSubExp( srcXs[1] ), None, dbgSrc=srcXs )
		elif name == '$activeBorder':
			#($activeBorder <child> styleSheet*)
			if len( srcXs ) < 2:
				self.env.glispError( GLispParameterListError, src, 'defineView: /activeBorder needs at least 1 parameter; the child content' )
			return PyCall( PyVar( '_activeBorder', dbgSrc=srcXs ), [ PyVar( '__view_node_instance__', dbgSrc=srcXs ), compileSubExp( srcXs[1] ), compileStyleSheetAccess( srcXs[2:]) ], dbgSrc=srcXs )
		elif name == '$label':
			#($label text styleSheet*)
			if len( srcXs ) < 2:
				self.env.glispError( GLispParameterListError, src, 'defineView: /label needs at least 1 parameter; the text' )
			return PyCall( PyVar( '_label', dbgSrc=srcXs ), [ PyVar( '__view_node_instance__', dbgSrc=srcXs ), compileSubExp( srcXs[1] ), compileStyleSheetAccess( srcXs[2:]) ], dbgSrc=srcXs )
		elif name == '$entry':
			#($entry text styleSheet*)
			if len( srcXs ) < 2:
				self.env.glispError( GLispParameterListError, src, 'defineView: /entry needs at least 1 parameter; the text' )
			return PyCall( PyVar( '_entry', dbgSrc=srcXs ), [ PyVar( '__view_node_instance__', dbgSrc=srcXs ), compileSubExp( srcXs[1] ), compileStyleSheetAccess( srcXs[2:]) ], dbgSrc=srcXs )
		elif name == '$hbox':
			#($hbox (child*) styleSheet*)
			if len( srcXs ) < 2:
				self.env.glispError( GLispParameterListError, src, 'defineView: /hbox needs at least 1 parameter; the children' )
			return PyCall( PyVar( '_hbox', dbgSrc=srcXs ), [ PyVar( '__view_node_instance__', dbgSrc=srcXs ), compileSubExp( srcXs[1] ), compileStyleSheetAccess( srcXs[2:]) ], dbgSrc=srcXs )
		else:
			return None
		

			
			
			
def defineView(env, xs, name, docFormat, spec):
	viewFactory = _GSymViewFactory( env, name, spec )
	#viewFactory = None
	
	return GSymViewDefinition( name, docFormat, viewFactory )

