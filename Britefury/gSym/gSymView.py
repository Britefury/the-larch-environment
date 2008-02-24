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


from Britefury.GLisp.GLispInterpreter import GLispParameterListError, GLispItemTypeError, isGLispList
from Britefury.GLisp.GLispCompiler import compileGLispCustomFunctionToPy, compileGLispExprToPySrc, GLispCompilerError, filterIdentifierForPy
from Britefury.GLisp.GuardExpression import compileGuardExpression, GuardError



class _ViewNodeAndIndrection (object):
	"""A helper class for storing the results of view computations"""
	def __init__(self, viewNode, indirection):
		self.viewNode = viewNode
		self.indirection = indirection


class GSymViewDefinition (object):
	def __init__(self, name, docFormat, viewFactory):
		self.name = name
		self.docFormat = docFormat
		self._viewFactory = viewFactory
		

	def createDocumentView(self, xs, commandHistory, styleSheetDispatcher):
		viewInstance = self._viewFactory.createInstance( xs, commandHistory, styleSheetDispatcher )
		return viewInstance.view



def _runtime_buildRefreshCellAndRegister(viewNodeInstance, refreshFunction):
	"""Builds a refresh cell, and registers it by appending it to @refreshCells"""
	cell = Cell()
	cell.function = refreshFunction
	viewNodeInstance.refreshCells.append( cell )

def _runtime_binRefreshCell(viewNodeInstance, bin, child):
	"""Builds and registers a refresh cell (if necessary) for a widget that is an instance of DTBin"""
	if isinstance( child, _ViewNodeAndIndrection ):
		def _binRefresh():
			child.viewNode.refresh()
			bin.child = child.viewNode.widget
		_runtime_buildRefreshCellAndRegister( viewNodeInstance, _binRefresh )
	elif isinstance( child, DTWidget ):
		bin.child = child
	else:
		viewNodeInstance.env.glispError( TypeError, xs, '_GSymNodeViewInstance._binRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )

def _runtime_boxRefreshCell(viewNodeInstance, widget, children):
	"""Builds and registers a refresh cell (if necessary) for a widget that is an instance of DTBox"""
	def _boxRefresh():
		widgets = []
		for child in children:
			if isinstance( child, _ViewNodeAndIndrection ):
				child.viewNode.refresh()
				widgets.append( child.viewNode.widget )
			elif isinstance( child, DTWidget ):
				widgets.append( child )
			else:
				viewNodeInstance.env.glispError( TypeError, xs, 'defineView: _boxRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )
		widget[:] = widgets
	_runtime_buildRefreshCellAndRegister( viewNodeInstance, _boxRefresh )

def _runtime_activeBorder(viewNodeInstance, child, styleSheets):
	"""Builds a DTActiveBorder widget, with child, builds and registers a refresh cell"""
	widget = DTActiveBorder()
	_runtime_binRefreshCell( viewNodeInstance, widget, child )
	for sheet in styleSheets:
		sheet.apply()
	return widget

def _runtime_label(viewNodeInstance, text, styleSheets):
	"""Builds a DTLabel widget"""
	widget = DTLabel(text)
	for sheet in styleSheets:
		sheet.apply()
	return widget

def _runtime_entry(viewNodeInstance, text, styleSheets):
	"""Builds a DTEntryLabel widget"""
	widget = DTEntryLabel(text)
	for sheet in styleSheets:
		sheet.apply()
	return widget

def _runtime_hbox(viewNodeInstance, children, styleSheets):
	"""Builds a horizontal DTBox widget, with child, builds and registers a refresh cell"""
	widget = DTBox()
	_runtime_boxRefreshCell( viewNodeInstance, widget, children )
	for sheet in styleSheets:
		sheet.apply()
	return widget








	

class _GSymNodeViewInstance (object):
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
		#1. Ese the guard function in the factory to determine which view expression to apply it
		try:
			varValues, index = self.viewFactory.guardFunction( content )
		except GuardError:
			raise
		#2. Get the view expression function from the table, along with the varName->valueIndirection table for that function
		f, varNameToValueIndirection = self.viewFactory.viewExprFunctionAndVarNameToIndirectionPairs[index]
		
		#3. Mix the variable values with value indirection
		varValuesWithIndireciton = {}
		for varName, viewNode in varValues:
			varValuesWithIndireciton[varName] = _ViewNodeAndIndrection( viewNode, varNameToValueIndirection[varName] )
		
		#4. Call the view expression function
		viewContents = f( self, self.refreshCells, **varValuesWithIndireciton )
		
		#5. Return the view contents
		return viewContents
		
		

	
	
	
	
	
	
	
class _GSymViewInstance (object):
	__slots__ = [ 'env', 'xs', 'viewFactory', 'view' ]

	def __init__(self, env, xs, viewFactory, commandHistory, styleSheetDispatcher):
		self.env = env
		self.xs = xs
		self.viewFactory = viewFactory
		self.view = DocView( self.xs, commandHistory, styleSheetDispatcher, self._p_buildDVNode )
		
	
	def _p_buildDVNode(self, docNode, view, docNodeKey):
		return DVCustomNode( docNode, view, docNodeKey, self._p_buildNodeContents )
	
	
	def _p_buildNodeContents(self, viewNode, docNodeKey):
		nodeViewInstance = _GSymNodeViewInstance( self.env, docNodeKey.docNode, self.view, self, self.viewFactory, viewNode )
		viewContents = nodeViewInstance._runtime_buildViewContents( docNodeKey.docNode )
		viewNode.setRefreshCells( nodeViewInstance.refreshCells )
		return viewContents
	
	
	
class _GSymViewFactory (object):
	def __init__(self, env, name, spec):
		self.env = env
		self.name = name
		
		# Build the guard expression
		self.guardFunction, varNameToValueIndirectionByGuard = compileGuardExpression( spec, [0], filterIdentifierForPy( 'view_guard_%s'  %  ( self.name, ) ) )
		
		self.viewExprFunctionAndVarNameToIndirectionPairs = [ self._p_generateExprFunctionAndValueIndirection( guardAndViewExpr, i, varNameToValueIndir )
					   for i, ( guardAndViewExpr, varNameToValueIndir ) in enumerate( zip( spec, varNameToValueIndirectionByGuard ) ) ]
		
		
	def createInstance(self, xs, commandHistory, styleSheetDispatcher):
		return _GSymViewInstance( self.env, xs, self, commandHistory, styleSheetDispatcher )


	
	def _p_generateExprFunctionAndValueIndirection(self, guardAndViewExpr, i, varNameToValueIndirection):
		functionName = filterIdentifierForPy( 'view_expr_%s_%d'  %  ( self.name, i ) )
		paramNames = [ '__view_node_instance__', '__refreshCells__' ]  +  list( varNameToValueIndirection.keys() )
		lcls = { '_buildView': self._runtime_buildView,
			 '_buildViewForMap' : self._runtime_buildViewForMap,
			 '_activeBorder' : _runtime_activeBorder,
			 '_label' : _runtime_label,
			 '_entry' : _runtime_entry,
			 '_hbox' : _runtime_hbox, }
		return compileGLispCustomFunctionToPy( guardAndViewExpr[1], functionName, paramNames, self._p_compileSpecial, lcls  ), varNameToValueIndirection
	
	
	def _runtime_buildView(self, viewNodeInstance, content):
		if not isinstance( content, _ViewNodeAndIndrection ):
			self.env.glispError( TypeError, None, '_GSymViewFactory._runtime_buildView: content is not a _ViewNodeAndIndrection' )
		xs, indirection = content
		parentViewNode = viewNodeInstance.viewNode
		indexInParentDocNode = indirection[-1]
		return view._f_buildView( xs, parentViewNode, indexInParentDocNode )
		
	def _runtime_buildViewForMap(self, viewNodeInstance, content, index):
		if not isinstance( content, _ViewNodeAndIndrection ):
			self.env.glispError( TypeError, None, '_GSymViewFactory._runtime_buildViewForMap: content is not a _ViewNodeAndIndrection' )
		xs, indirection = content
		parentViewNode = viewNodeInstance.viewNode
		lastIndirection = indirection[-1]
		if isinstance( lastIndirection, tuple ):
			indexInParentDocNode = lastIndirection[0] + index
		else:
			indexInParentDocNode = index
		return view._f_buildView( xs, parentViewNode, indexInParentDocNode )

		
	
	def _p_compileStylesheetAccess(self, src):
		return '[]'
	
	def _p_compileSubExp(self, src):
		return compileGLispExprToPySrc( src, self._p_compileSpecial )
	
	def _p_compileSpecial(self, src):
		name = src[0]
		if name == '/viewEval':
			return '_buildView( __view_node_instance__, %s )'  %  ( self._p_compileSubExp( src[1] ), )
		elif name == '/mapViewEval':
			return '[ _buildViewForMap( __view_node_instance__, x, i )   for i, x in enumerate( %s ) ]'  %  ( self._p_compileSubExp( src[1] ), )
		elif name == '/activeBorder':
			#(/activeBorder child styleSheet*)
			if len( src ) < 2:
				self.env.glispError( GLispParameterListError, src, 'defineView: /activeBorder needs at least 1 parameter; the child content' )
			return '_activeBorder( __view_node_instance__, %s, %s )'  %  ( self._p_compileSubExp( src[1] ), self._p_compileStylesheetAccess( src[2:] ) )
		elif name == '/label':
			#(/label text styleSheet*)
			if len( src ) < 2:
				self.env.glispError( GLispParameterListError, src, 'defineView: /label needs at least 1 parameter; the text' )
			return '_label( __view_node_instance__, %s, %s )'  %  ( self._p_compileSubExp( src[1] ), self._p_compileStylesheetAccess( src[2:] ) )
		elif name == '/entry':
			#(/label text styleSheet*)
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
