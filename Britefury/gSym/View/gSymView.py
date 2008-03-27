##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import sys

from Britefury.Math.Math import Colour3f

from Britefury.Cell.Cell import Cell

from Britefury.DocPresent.Toolkit.DTWidget import DTWidget
from Britefury.DocPresent.Toolkit.DTActiveBorder import DTActiveBorder
from Britefury.DocPresent.Toolkit.DTBorder import DTBorder
from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTEntryLabel import DTEntryLabel
from Britefury.DocPresent.Toolkit.DTHLine import DTHLine
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTScript import DTScript

from Britefury.DocPresent.Toolkit.DTDirection import DTDirection

from Britefury.DocView.DVNode import DVNode
from Britefury.DocView.DVCustomNode import DVCustomNode
from Britefury.DocView.DocView import DocView


from Britefury.GLisp.GLispUtil import isGLispList, gLispSrcToString
from Britefury.GLisp.GLispInterpreter import GLispParameterListError, GLispItemTypeError, GLispItemError
from Britefury.GLisp.GLispCompiler import compileGLispExprToPyFunction, GLispCompilerCouldNotCompileSpecial
from Britefury.GLisp.PyCodeGen import filterIdentifierForPy, pyt_coerce, PyCodeGenError, PySrc, PyVar, PyLiteral, PyListLiteral, PyListComprehension, PyGetAttr, PyGetItem, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyReturn, PyIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects

from Britefury.gSym.gSymStyleSheet import GSymStyleSheet

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




class _InteractWrapper (object):
	def __init__(self, child, interact):
		self.child = child
		self.interact = interact




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
		viewNodeInstance.env.raiseError( TypeError, viewNodeInstance.xs, '_GSymNodeViewInstance._binRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )

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
				viewNodeInstance.env.raiseError( TypeError, viewNodeInstance.xs, 'defineView: _boxRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )
		widget[:] = widgets
	_runtime_buildRefreshCellAndRegister( viewNodeInstance, _boxRefresh )

def _runtime_scriptRefreshCell(viewNodeInstance, script, child, childSlotAttrName):
	"""
	Runtime - called by compiled code at run-time
	Builds and registers a refresh cell (if necessary) for a widget that is an instance of DTBin
	"""
	if isinstance( child, DVNode ):
		chNode = child
		def _scriptRefresh():
			chNode.refresh()
			#script.mainChild = chNode.widget
			setattr( script, childSlotAttrName, chNode.widget )
		_runtime_buildRefreshCellAndRegister( viewNodeInstance, _scriptRefresh )
	elif isinstance( child, DTWidget ):
		#script.mainChild = child
		setattr( script, childSlotAttrName, child)
	else:
		viewNodeInstance.env.raiseError( TypeError, viewNodeInstance.xs, '_GSymNodeViewInstance._runtime_scriptRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )


	
	
def _runtime_applyStyleSheetStack(viewNodeInstance, widget):
	for styleSheet in viewNodeInstance.styleSheetStack:
		styleSheet.applyToWidget( widget )

def _runtime_applyStyleSheetList(styleSheets, widget):
	for styleSheet in styleSheets:
		styleSheet.applyToWidget( widget )



def _runtime_activeBorder(viewNodeInstance, child, styleSheets):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTActiveBorder widget, with child, builds and registers a refresh cell
	"""
	widget = DTActiveBorder()
	widget.keyHandler = viewNodeInstance.viewNode
	_runtime_binRefreshCell( viewNodeInstance, widget, child )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyleSheetList( styleSheets, widget )
	return widget

def _runtime_border(viewNodeInstance, child, styleSheets):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTBorder widget, with child, builds and registers a refresh cell
	"""
	widget = DTBorder()
	widget.keyHandler = viewNodeInstance.viewNode
	_runtime_binRefreshCell( viewNodeInstance, widget, child )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyleSheetList( styleSheets, widget )
	return widget

def _runtime_indent(viewNodeInstance, child, indentation, styleSheets):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTBorder widget, with child, builds and registers a refresh cell
	"""
	widget = DTBorder()
	widget.leftMargin = indentation
	widget.keyHandler = viewNodeInstance.viewNode
	_runtime_binRefreshCell( viewNodeInstance, widget, child )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyleSheetList( styleSheets, widget )
	return widget

def _runtime_hline(viewNodeInstance, styleSheets):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTLabel widget
	"""
	widget = DTHLine()
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyleSheetList( styleSheets, widget )
	return widget

def _runtime_label(viewNodeInstance, text, styleSheets):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTLabel widget
	"""
	if isinstance( text, RelativeNode ):
		text = text.node
	widget = DTLabel( text )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyleSheetList( styleSheets, widget )
	return widget

def _runtime_entry(viewNodeInstance, text, styleSheets):
	"""Builds a DTEntryLabel widget"""
	if isinstance( text, RelativeNode ):
		text = text.node
	widget = DTEntryLabel(text)
	widget.keyHandler = viewNodeInstance.viewNode
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyleSheetList( styleSheets, widget )
	return widget

def _runtime_hbox(viewNodeInstance, children, styleSheets):
	"""
	Runtime - called by compiled code at run-time
	Builds a horizontal DTBox widget, with child, builds and registers a refresh cell
	"""
	widget = DTBox()
	_runtime_boxRefreshCell( viewNodeInstance, widget, children )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyleSheetList( styleSheets, widget )
	return widget

def _runtime_ahbox(viewNodeInstance, children, styleSheets):
	"""
	Runtime - called by compiled code at run-time
	Builds a horizontal DTBox widget, with child, builds and registers a refresh cell
	"""
	widget = DTBox( alignment=DTBox.ALIGN_BASELINES )
	_runtime_boxRefreshCell( viewNodeInstance, widget, children )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyleSheetList( styleSheets, widget )
	return widget

def _runtime_vbox(viewNodeInstance, children, styleSheets):
	"""
	Runtime - called by compiled code at run-time
	Builds a vertical DTBox widget, with child, builds and registers a refresh cell
	"""
	widget = DTBox( direction=DTDirection.TOP_TO_BOTTOM, alignment=DTBox.ALIGN_LEFT )
	_runtime_boxRefreshCell( viewNodeInstance, widget, children )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyleSheetList( styleSheets, widget )
	return widget

def _runtime_script(viewNodeInstance, mainChild, leftSuperChild, leftSubChild, rightSuperChild, rightSubChild, styleSheets):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTActiveBorder widget, with child, builds and registers a refresh cell
	"""
	widget = DTScript()
	
	_runtime_scriptRefreshCell( viewNodeInstance, widget, mainChild, 'mainChild' )
	if leftSuperChild is not None:
		_runtime_scriptRefreshCell( viewNodeInstance, widget, leftSuperChild, 'leftSuperscriptChild' )
	if leftSubChild is not None:
		_runtime_scriptRefreshCell( viewNodeInstance, widget, leftSubChild, 'leftSubscriptChild' )
	if rightSuperChild is not None:
		_runtime_scriptRefreshCell( viewNodeInstance, widget, rightSuperChild, 'rightSuperscriptChild' )
	if rightSubChild is not None:
		_runtime_scriptRefreshCell( viewNodeInstance, widget, rightSubChild, 'rightSubscriptChild' )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyleSheetList( styleSheets, widget )
	return widget









_buildViewContentsRecursionLockSet = set()
	

class _GSymNodeViewInstance (object):
	"""
	Manages state that concerns a view of a specific sub-tree of a document
	"""
	
	__slots__ = [ 'env', 'xs', 'view', 'viewInstance', 'viewNode', 'refreshCells', 'styleSheetStack' ]

	def __init__(self, env, xs, view, viewInstance, viewNode):
		self.env = env
		self.xs = xs
		self.view = view
		self.viewInstance = viewInstance
		self.viewNode = viewNode
		self.refreshCells = []
		self.styleSheetStack = []

		


	
	
	
	
	
	
class _GSymViewInstance (object):
	"""
	Manages state concerning a view of a specific document
	"""
	def __init__(self, env, xs, viewFactory, commandHistory, styleSheetDispatcher):
		self.env = env
		self.xs = xs
		self.viewNodeInstanceStack = []
		self.generalNodeViewFunction = viewFactory.makeViewFunction( self.viewNodeInstanceStack )
		# self._p_buildDVNode is a factory that builds DVNode instances for document subtrees
		self.view = DocView( self.xs, commandHistory, styleSheetDispatcher, self._p_rootNodeFactory )
		
		self._nodeContentsFactories = {}
		
	
	def _f_makeNodeFactory(self, nodeViewFunction):
		def _nodeFactory(docNode, view, docNodeKey):
			# Build a DVNode for the document subtree at @docNode
			# self._p_buildNodeContents is a factory that builds the contents withing the DVNode
			node = DVCustomNode( docNode, view, docNodeKey )
			node._f_setContentsFactory( self._f_makeNodeContentsFactory( nodeViewFunction ) )
			return node
		return _nodeFactory
	

	def _p_rootNodeFactory(self, docNode, view, docNodeKey):
		# Build a DVNode for the document subtree at @docNode
		# self._p_buildNodeContents is a factory that builds the contents withing the DVNode
		node = DVCustomNode( docNode, view, docNodeKey )
		node._f_setContentsFactory( self._f_makeNodeContentsFactory() )
		return node
	


	def _f_makeNodeContentsFactory(self, nodeViewFunction=None):
		def _buildNodeContents(viewNode, docNodeKey):
			# Create the node view instance
			nodeViewInstance = _GSymNodeViewInstance( self.env, docNodeKey.docNode, self.view, self, viewNode )
			relativeNode = relative( docNodeKey.docNode, docNodeKey.parentDocNode, docNodeKey.index )
			# Build the contents
			viewContents = self._runtime_buildNodeViewContents( nodeViewInstance, relativeNode, nodeViewFunction )
			# Get the refresh cells that need to be monitored, and hand them to the DVNode
			viewNode._f_setRefreshCells( nodeViewInstance.refreshCells )
			# Return the contents
			return viewContents
		
		#Memoise the contents factory; keyed by @viewFunction
		try:
			return self._nodeContentsFactories[nodeViewFunction]
		except KeyError:
			factory = _buildNodeContents
			self._nodeContentsFactories[nodeViewFunction] = factory
			return factory
		
	
	def _runtime_buildNodeViewContents(self, nodeViewInstance, content, nodeViewFunction=None):
		"""Runtime - build the contents of a view node"""
		if nodeViewFunction is None:
			nodeViewFunction = self.generalNodeViewFunction

		#1. Push @nodeViewInstance onto the view instance's view node instance stack
		# This is done so that the functions that have been compiled can get a reference to @this
		self.viewNodeInstanceStack.append( nodeViewInstance )
		
		#2. Create the view contents
		viewContents = nodeViewFunction( content )
		
		#3. Push @self from the view instance's view node instance stack
		self.viewNodeInstanceStack.pop()
		
		#4. Return the view contents
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
		viewModuleName = filterIdentifierForPy( 'viewFactoryModule_%s'  %  ( name, ) )
		
		lcls = { '_buildView': self._runtime_buildView,
			 '_activeBorder' : _runtime_activeBorder,
			 '_border' : _runtime_border,
			 '_indent' : _runtime_indent,
			 '_hline' : _runtime_hline,
			 '_label' : _runtime_label,
			 '_entry' : _runtime_entry,
			 '_hbox' : _runtime_hbox,
			 '_ahbox' : _runtime_ahbox,
			 '_vbox' : _runtime_vbox,
			 '_script' : _runtime_script,
			 '_GSymStyleSheet' : GSymStyleSheet,
			 '_Colour3f' : Colour3f,
			 'DTDirection' : DTDirection,
			 'DTBox' : DTBox,
			 }
		try:
			self.makeViewFunction = compileGLispExprToPyFunction( viewModuleName, viewFunctionName, [ '__view_node_instance_stack__' ], spec, self._p_compileSpecial, lcls )
		except PyCodeGenError, e:
			print e.args
			raise
		
		
		
	def _runtime_buildView(self, viewNodeInstance, content, nodeViewFunction=None):
		"""Build a view for a document subtree (@content)"""
		if not isinstance( content, RelativeNode ):
			self.env.raiseError( TypeError, None, '_GSymViewFactory._runtime_buildView: content is not a RelativeNode' )
			
		# A call to DocNode._f_buildView builds the view, and puts it in the DocView's table
		viewInstance = viewNodeInstance.viewInstance
		nodeFactory = viewInstance._f_makeNodeFactory( nodeViewFunction )
		viewNode = viewNodeInstance.view._f_buildView( content.node, content.parent, content.indexInParent, nodeFactory )
		viewNode._f_setContentsFactory( viewNodeInstance.viewInstance._f_makeNodeContentsFactory( nodeViewFunction ) )
		viewNode.refresh()
		
		return viewNode

		

	
	
	
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
		compileStyleSheetAccess = lambda xs: PyListLiteral( [ compileSubExp( x )  for x in xs ] )
	
		if name == '$viewEval':
			# ($viewEval <document-subtree> ?<node-view-function>)
			if len( srcXs ) < 2:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $viewEval needs at least 1 parameter; the document subtree' )
			params = [ PySrc( '__view_node_instance_stack__[-1]', dbgSrc=srcXs ), compileSubExp( srcXs[1] ) ]
			# View function
			if len( srcXs ) == 3:
				params.append( compileSubExp( srcXs[2] ) )
			return PyCall( PyVar( '_buildView', dbgSrc=srcXs ), params, dbgSrc=srcXs )
		elif name == '$mapViewEval':
			# ($mapViewEval <document-subtree> ?<node-view-function>)
			if len( srcXs ) < 2:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $mapViewEval needs at least 1 parameter; the document subtree' )
			params = [ PySrc( '__view_node_instance_stack__[-1]', dbgSrc=srcXs ), PyVar( 'x', dbgSrc=srcXs ) ]
			# View function
			if len( srcXs ) == 3:
				params.append( compileSubExp( srcXs[2] ) )
			itemExpr = PyCall( PyVar( '_buildView', dbgSrc=srcXs ), params, dbgSrc=srcXs )
			return PyListComprehension( itemExpr, 'x', compileSubExp( srcXs[1] ), None, dbgSrc=srcXs )
		elif name == '$colour':
			#($colour <red> <green> <blue>)
			if len( srcXs ) != 4:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $colour needs 3 parameters; red, green, and blue' )
			return PyVar( '_Colour3f' )( compileSubExp( srcXs[1] ), compileSubExp( srcXs[2] ), compileSubExp( srcXs[3] ) ).debug( srcXs )
		elif name == '$style':
			#($style <settings_pairs>)
			#settings pair: (:key <value>)
			def _settingsPair(pairXs):
				if len( pairXs ) != 2:
					self.env.raiseError( GLispParameterListError, src, 'defineView: $style settings pair needs 2 parameters; the key and the value' )
				if not isinstance( pairXs[0], str )  and  not isinstance( pairXs[0], unicode ):
					self.env.raiseError( GLispItemTypeError, src, 'defineView: $style settings pair key must be a string' )
				if pairXs[0][0] != ':':
					self.env.raiseError( GLispItemError, src, 'defineView: $style settings pair key must start with a :' )
				return pyt_coerce( [ pairXs[0][1:], compileSubExp( pairXs[1] ) ] )
			return PyVar( '_GSymStyleSheet' )( pyt_coerce( [ _settingsPair( pairXs )   for pairXs in srcXs[1:] ] ) ).debug( srcXs )
		elif name == '$applyStyle':
			#($applyStyle <stylesheet> <child>)
			if len( srcXs ) != 3:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $applyStyle needs 2 parameters; the style and the child content' )
			childResVarName = None
			py_stylePush = PyVar( '__view_node_instance_stack__' )[-1].attr( 'styleSheetStack' ).methodCall( 'append', compileSubExp( srcXs[1] ) ).debug( srcXs )
			py_childResult = compileSubExp( srcXs[2] )
			if bNeedResult:
				childResVarName = context.temps.allocateTempName( 'view_special_child_result' )
				py_childResult = PyVar( childResVarName ).assign_sideEffects( py_childResult ).debug( srcXs )
			py_stylePop = PyVar( '__view_node_instance_stack__' )[-1].attr( 'styleSheetStack' ).methodCall( 'pop' )
			
			context.body.append( py_stylePush )
			context.body.append( py_childResult )
			context.body.append( py_stylePop )
			
			if bNeedResult:
				return PyVar( childResVarName ).debug( srcXs )
			else:
				return None
		elif name == '$activeBorder':
			#($activeBorder <child> styleSheet*)
			if len( srcXs ) < 2:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $activeBorder needs at least 1 parameters; the child content' )
			return PyVar( '_activeBorder' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), compileStyleSheetAccess( srcXs[2:]) ).debug( srcXs )
		elif name == '$border':
			#($border <child> styleSheet*)
			if len( srcXs ) < 2:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $border needs at least 1 parameters; the child content' )
			return PyVar( '_border' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), compileStyleSheetAccess( srcXs[1:]) ).debug( srcXs )
		elif name == '$indent':
			#($indent <indentation> <child> styleSheet*)
			if len( srcXs ) < 3:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $border needs at least 2 parameters; the indentation and the child content' )
			return PyVar( '_indent' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[2] ), compileSubExp( srcXs[1] ), compileStyleSheetAccess( srcXs[3:]) ).debug( srcXs )
		elif name == '$hline':
			#($hline styleSheet*)
			return PyCall( PyVar( '_hline', dbgSrc=srcXs ), [ PySrc( '__view_node_instance_stack__[-1]', dbgSrc=srcXs ), compileStyleSheetAccess( srcXs[2:]) ], dbgSrc=srcXs )
		elif name == '$label':
			#($label text styleSheet*)
			if len( srcXs ) < 2:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $label needs at least 1 parameter; the text' )
			return PyCall( PyVar( '_label', dbgSrc=srcXs ), [ PySrc( '__view_node_instance_stack__[-1]', dbgSrc=srcXs ), compileSubExp( srcXs[1] ), compileStyleSheetAccess( srcXs[2:]) ], dbgSrc=srcXs )
		elif name == '$entry':
			#($entry text styleSheet*)
			if len( srcXs ) < 2:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $entry needs at least 1 parameter; the text' )
			return PyCall( PyVar( '_entry', dbgSrc=srcXs ), [ PySrc( '__view_node_instance_stack__[-1]', dbgSrc=srcXs ), compileSubExp( srcXs[1] ), compileStyleSheetAccess( srcXs[2:]) ], dbgSrc=srcXs )
		elif name == '$hbox':
			#($hbox (child*) styleSheet*)
			if len( srcXs ) < 2:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $hbox needs at least 1 parameter; the children' )
			return PyCall( PyVar( '_hbox', dbgSrc=srcXs ), [ PySrc( '__view_node_instance_stack__[-1]', dbgSrc=srcXs ), compileSubExp( srcXs[1] ), compileStyleSheetAccess( srcXs[2:]) ], dbgSrc=srcXs )
		elif name == '$ahbox':
			#($ahbox (child*) styleSheet*)
			if len( srcXs ) < 2:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $ahbox needs at least 1 parameter; the children' )
			return PyCall( PyVar( '_ahbox', dbgSrc=srcXs ), [ PySrc( '__view_node_instance_stack__[-1]', dbgSrc=srcXs ), compileSubExp( srcXs[1] ), compileStyleSheetAccess( srcXs[2:]) ], dbgSrc=srcXs )
		elif name == '$vbox':
			#($vbox (child*) styleSheet*)
			if len( srcXs ) < 2:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $vbox needs at least 1 parameter; the children' )
			return PyCall( PyVar( '_vbox', dbgSrc=srcXs ), [ PySrc( '__view_node_instance_stack__[-1]', dbgSrc=srcXs ), compileSubExp( srcXs[1] ), compileStyleSheetAccess( srcXs[2:]) ], dbgSrc=srcXs )
		elif name == '$script':
			#($script <mainChild> <leftSuperChild> <leftSubChild> <rightSuperChild> <rightSubChild> styleSheet*)
			if len( srcXs ) < 6:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $script needs at least 5 parameters; the main, left-super, left-sub, right-super, and right-sub children' )
			return PyVar( '_script' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), compileSubExp( srcXs[2] ), compileSubExp( srcXs[3] ), compileSubExp( srcXs[4] ), compileSubExp( srcXs[5] ),
						   compileStyleSheetAccess( srcXs[6:]) ).debug( srcXs )
		elif name == '$scriptLSuper':
			#($scriptLSuper <mainChild> <scriptChild> styleSheet*)
			if len( srcXs ) < 3:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $scriptLSuper needs at least 2 parameters; the main child, and the script child' )
			return PyVar( '_script' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), compileSubExp( srcXs[2] ), None, None, None,
						   compileStyleSheetAccess( srcXs[6:]) ).debug( srcXs )
		elif name == '$scriptLSub':
			#($scriptLSub <mainChild> <scriptChild> styleSheet*)
			if len( srcXs ) < 3:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $scriptLSub needs at least 2 parameters; the main child, and the script child' )
			return PyVar( '_script' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), None, compileSubExp( srcXs[2] ), None, None,
						   compileStyleSheetAccess( srcXs[6:]) ).debug( srcXs )
		elif name == '$scriptRSuper':
			#($scriptRSuper <mainChild> <scriptChild> styleSheet*)
			if len( srcXs ) < 3:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $scriptRSuper needs at least 2 parameters; the main child, and the script child' )
			return PyVar( '_script' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), None, None, compileSubExp( srcXs[2] ), None,
						   compileStyleSheetAccess( srcXs[6:]) ).debug( srcXs )
		elif name == '$scriptRSub':
			#($scriptRSub <mainChild> <scriptChild> styleSheet*)
			if len( srcXs ) < 3:
				self.env.raiseError( GLispParameterListError, src, 'defineView: $scriptRSub needs at least 2 parameters; the main child, and the script child' )
			return PyVar( '_script' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), None, None, None, compileSubExp( srcXs[2] ),
						   compileStyleSheetAccess( srcXs[6:]) ).debug( srcXs )
		elif name == '$interactor':
			#($interactor ....)
			return self._p_compileInteractor( srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree )
		else:
			raise GLispCompilerCouldNotCompileSpecial( srcXs )
		

		
	def _p_compileInteractor(self, srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
		#
		#( $interactor <interactor_specs...> )
		#
		# interactor_pair:
		# ( <event_spec>  <actions...> )
		#
		# event_spec:
		# key:
		# ( $key <key_value> <mods...> )    - key event
		# tokens:
		# ( $tokens <token_specs...> )     - token list; consumes the tokens specified in the list
		#
		# token_spec:
		# token_type_id        - the token type id
		# or:
		# (: @var token_type_id)     - look for a token with the specified type, and bind the value to a variable called 'var'
		return Interactor
			

		

class GSymViewDefinition (object):
	"""The language view definition
	An instance of GSymViewDefinition is created and returned by defineView()"""
	def __init__(self, env, name, spec):
		self._env = env
		self._viewFactory = _GSymViewFactory( env, name, spec )
		

	def createDocumentView(self, xs, commandHistory, styleSheetDispatcher):
		viewInstance = _GSymViewInstance( self._env, xs, self._viewFactory, commandHistory, styleSheetDispatcher )
		return viewInstance.view


	

		
		
		
			
def defineView(env, xs, name, docFormat, spec):
	return GSymViewDefinition( env, name, spec )

