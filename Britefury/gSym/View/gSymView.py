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
from Britefury.DocPresent.Toolkit.DTBin import DTBin 
from Britefury.DocPresent.Toolkit.DTBorder import DTBorder
from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTHLine import DTHLine
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTScript import DTScript
from Britefury.DocPresent.Toolkit.DTTokenisedEntryLabel import DTTokenisedEntryLabel
from Britefury.DocPresent.Toolkit.DTTokenisedCustomEntry import DTTokenisedCustomEntry
from Britefury.DocPresent.Toolkit.DTWrappedHBox import DTWrappedHBox
from Britefury.DocPresent.Toolkit.DTWrappedHBoxWithSeparators import DTWrappedHBoxWithSeparators


from Britefury.DocView.DVNode import DVNode
from Britefury.DocView.DVCustomNode import DVCustomNode
from Britefury.DocView.DocView import DocView
from Britefury.DocView.DocViewNodeTable import DocNodeKey


from Britefury.GLisp.GLispUtil import isGLispList, gLispSrcToString
from Britefury.GLisp.GLispCompiler import raiseCompilerError, raiseRuntimeError, compileGLispExprToPyFunction, compileGLispCallParamToPyTree, GLispCompilerCouldNotCompileSpecial, GLispCompilerInvalidFormType, GLispCompilerInvalidFormLength, GLispCompilerInvalidItem
from Britefury.GLisp.PyCodeGen import filterIdentifierForPy, pyt_coerce, PyCodeGenError, PyVar, PyLiteral, PyListLiteral, PyListComprehension, PyGetAttr, PyGetItem, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyMultilineSrc, PyReturn, PyIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects

from Britefury.gSym.gMeta.GMetaComponent import GMetaComponent
from Britefury.gSym.gSymStyleSheet import GSymStyleSheet
from Britefury.gSym.View.Interactor import Interactor, NoEventMatch
from Britefury.gSym.View.InteractorEvent import InteractorEventKey, InteractorEventTokenList

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



def _relativeNodeToDocNodeKey(node):
	return DocNodeKey( node.node, node.parent, node.indexInParent )





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
		raiseRuntimeError( TypeError, viewNodeInstance.xs, '_GSymNodeViewInstance._binRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )

def _runtime_customEntryRefreshCell(viewNodeInstance, customEntry, child):
	"""
	Runtime - called by compiled code at run-time
	Builds and registers a refresh cell (if necessary) for a widget that is an instance of DTCusomEntry
	"""
	if isinstance( child, DVNode ):
		chNode = child
		def _customEntryRefresh():
			chNode.refresh()
			customEntry.customChild = chNode.widget
		_runtime_buildRefreshCellAndRegister( viewNodeInstance, _customEntryRefresh )
	elif isinstance( child, DTWidget ):
		customEntry.customChild = child
	else:
		raiseRuntimeError( TypeError, viewNodeInstance.xs, '_GSymNodeViewInstance._customEntryRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )

def _runtime_containerSeqRefreshCell(viewNodeInstance, widget, children):
	"""
	Runtime - called by compiled code at run-time
	Builds and registers a refresh cell (if necessary) for a widget that is an instance of DTBox
	"""
	def _containerSeqRefresh():
		widgets = []
		for child in children:
			if isinstance( child, DVNode ):
				child.refresh()
				widgets.append( child.widget )
			elif isinstance( child, DTWidget ):
				widgets.append( child )
			else:
				raiseRuntimeError( TypeError, viewNodeInstance.xs, 'defineView: _containerSeqRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )
		widget[:] = widgets
	_runtime_buildRefreshCellAndRegister( viewNodeInstance, _containerSeqRefresh )

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
		raiseRuntimeError( TypeError, viewNodeInstance.xs, '_GSymNodeViewInstance._runtime_scriptRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )


	
	
def _runtime_applyStyleSheetStack(viewNodeInstance, widget):
	for styleSheet in viewNodeInstance.styleSheetStack:
		styleSheet.applyToWidget( widget )

def _runtime_applyStyle(style, widget):
	if style is not None:
		if isinstance( style, GSymStyleSheet ):
			style.applyToWidget( widget )
		else:
			for s in style:
				s.applyToWidget( widget )
				
				

			
	

def _runtime_setKeyHandler(viewNodeInstance, widget):
	widget.keyHandler = viewNodeInstance.viewInstance._runtime_handleKeyPress


def _runtime_activeBorder(viewNodeInstance, child, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTActiveBorder widget, with child, builds and registers a refresh cell
	"""
	widget = DTActiveBorder()
	_runtime_setKeyHandler( viewNodeInstance, widget )
	_runtime_binRefreshCell( viewNodeInstance, widget, child )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyle( style, widget )
	return widget

def _runtime_border(viewNodeInstance, child, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTBorder widget, with child, builds and registers a refresh cell
	"""
	widget = DTBorder()
	_runtime_binRefreshCell( viewNodeInstance, widget, child )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyle( style, widget )
	return widget

def _runtime_indent(viewNodeInstance, child, indentation, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTBorder widget, with child, builds and registers a refresh cell
	"""
	widget = DTBorder()
	widget.allMargins = 0.0
	widget.leftMargin = indentation
	_runtime_binRefreshCell( viewNodeInstance, widget, child )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyle( style, widget )
	return widget

def _runtime_hline(viewNodeInstance, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTLabel widget
	"""
	widget = DTHLine()
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyle( style, widget )
	return widget

def _runtime_label(viewNodeInstance, text, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTLabel widget
	"""
	if isinstance( text, RelativeNode ):
		text = text.node
	widget = DTLabel( text )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyle( style, widget )
	return widget

def _runtime_markupLabel(viewNodeInstance, text, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a markup DTLabel widget
	"""
	if isinstance( text, RelativeNode ):
		text = text.node
	widget = DTLabel( text )
	widget.bUseMarkup = True
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyle( style, widget )
	return widget



def _runtime_entry(viewNodeInstance, labelText, entryText, tokeniser, style=None):
	"""Builds a DTEntryLabel widget"""
	if isinstance( labelText, RelativeNode ):
		labelText = labelText.node
	if isinstance( entryText, RelativeNode ):
		entryText = entryText.node
	widget = DTTokenisedEntryLabel( tokeniser, labelText, entryText )
	widget.textModifiedSignal.connect( viewNodeInstance.viewInstance._runtime_onEntryModifed )
	widget.finishEditingSignal.connect( viewNodeInstance.viewInstance._runtime_onEntryFinished )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyle( style, widget )
	return widget

def _runtime_markupEntry(viewNodeInstance, labelText, entryText, tokeniser, style=None):
	"""Builds a DTEntryLabel widget"""
	if isinstance( labelText, RelativeNode ):
		labelText = labelText.node
	if isinstance( entryText, RelativeNode ):
		entryText = entryText.node
	widget = DTTokenisedEntryLabel( tokeniser, labelText, entryText )
	widget.textModifiedSignal.connect( viewNodeInstance.viewInstance._runtime_onEntryModifed )
	widget.finishEditingSignal.connect( viewNodeInstance.viewInstance._runtime_onEntryFinished )
	widget.bLabelUseMarkup = True
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyle( style, widget )
	return widget

def _runtime_customEntry(viewNodeInstance, customChild, entryText, tokeniser, style=None):
	"""Builds a DTEntryLabel widget"""
	if isinstance( entryText, RelativeNode ):
		entryText = entryText.node
	widget = DTTokenisedCustomEntry( tokeniser, entryText )
	widget.textModifiedSignal.connect( viewNodeInstance.viewInstance._runtime_onEntryModifed )
	widget.finishEditingSignal.connect( viewNodeInstance.viewInstance._runtime_onEntryFinished )
	_runtime_customEntryRefreshCell( viewNodeInstance, widget, customChild )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyle( style, widget )
	return widget

def _runtime_hbox(viewNodeInstance, children, style=None, alignment=DTBox.ALIGN_CENTRE, spacing=0.0):
	"""
	Runtime - called by compiled code at run-time
	Builds a horizontal DTBox widget, with child, builds and registers a refresh cell
	"""
	widget = DTBox( spacing=spacing, alignment=alignment )
	_runtime_containerSeqRefreshCell( viewNodeInstance, widget, children )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyle( style, widget )
	return widget

def _runtime_ahbox(viewNodeInstance, children, style=None, spacing=0.0):
	"""
	Runtime - called by compiled code at run-time
	Builds a horizontal DTBox widget, with child, builds and registers a refresh cell
	"""
	widget = DTBox( spacing=spacing, alignment=DTBox.ALIGN_BASELINES )
	_runtime_containerSeqRefreshCell( viewNodeInstance, widget, children )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyle( style, widget )
	return widget

def _runtime_vbox(viewNodeInstance, children, style=None, alignment=DTBox.ALIGN_LEFT, spacing=0.0):
	"""
	Runtime - called by compiled code at run-time
	Builds a vertical DTBox widget, with child, builds and registers a refresh cell
	"""
	widget = DTBox( direction=DTBox.TOP_TO_BOTTOM, spacing=spacing, alignment=alignment )
	_runtime_containerSeqRefreshCell( viewNodeInstance, widget, children )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyle( style, widget )
	return widget

def _runtime_wrappedHBox(viewNodeInstance, children, style=None, spacing=0.0, indentation=0.0):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTWrappedHBox widget, with child, builds and registers a refresh cell
	"""
	widget = DTWrappedHBox( spacing=spacing, indentation=indentation )
	_runtime_containerSeqRefreshCell( viewNodeInstance, widget, children )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyle( style, widget )
	return widget

def _runtime_wrappedHBoxSep(viewNodeInstance, children, separatorFactory=',', style=None, spacing=0.0, indentation=0.0):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTWrappedHBoxWithSeparators widget, with child, builds and registers a refresh cell
	"""
	widget = DTWrappedHBoxWithSeparators( separatorFactory, spacing=spacing, indentation=indentation )
	_runtime_containerSeqRefreshCell( viewNodeInstance, widget, children )
	_runtime_applyStyleSheetStack( viewNodeInstance, widget )
	_runtime_applyStyle( style, widget )
	return widget

def _runtime_script(viewNodeInstance, mainChild, leftSuperChild, leftSubChild, rightSuperChild, rightSubChild, style=None):
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
	_runtime_applyStyle( style, widget )
	return widget




class _DocEventHandler (object):
	def __init__(self, viewInstance, interactors):
		super( _DocEventHandler, self ).__init__()
		self.viewInstance = viewInstance
		if isinstance( interactors, Interactor ):
			interactors = [ interactors ]
		self.interactors = interactors
		
	def __call__(self, event):
		for interactor in self.interactors:
			try:
				nodeToSelect, ev = interactor.handleEvent( event )
			except NoEventMatch:
				pass
			else:
				if ev is not event:
					self.viewInstance._runtime_queueRefreshAndSelect( nodeToSelect )
					return ev
		return event
		

def _runtime_interact(viewNodeInstance, child, interactors=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTBorder widget, with child, builds and registers a refresh cell
	"""
	
	def _processChild(c):
		if isinstance( c, DVNode ):
			widget = DTBin()
			widget.addDocEventHandler( _DocEventHandler( viewNodeInstance.viewInstance, interactors ) )
			_runtime_binRefreshCell( viewNodeInstance, widget, child )
		elif isinstance( c, DTWidget ):
			c.addDocEventHandler( _DocEventHandler( viewNodeInstance.viewInstance, interactors ) )
		else:
			raiseRuntimeError( TypeError, viewNodeInstance.xs, '_runtime_interact: could not process child of type %s'  %  ( type( c ).__name__, ) )
			
	if isinstance( child, list )  or  isinstance( child, tuple ):
		for c in child:
			_processChild( c )
	else:
		_processChild( child )
		
	return child
		




def _runtime_buildView(viewNodeInstance, content, nodeViewFunction=None):
	"""Build a view for a document subtree (@content)"""
	if not isinstance( content, RelativeNode ):
		raise TypeError, '_runtime_buildView: content is not a RelativeNode'
		
	# A call to DocNode._f_buildView builds the view, and puts it in the DocView's table
	viewInstance = viewNodeInstance.viewInstance
	nodeFactory = viewInstance._f_makeNodeFactory( nodeViewFunction )
	viewNode = viewNodeInstance.view._f_buildView( content.node, content.parent, content.indexInParent, nodeFactory )
	viewNode._f_setContentsFactory( viewNodeInstance.viewInstance._f_makeNodeContentsFactory( nodeViewFunction ) )
	viewNode.refresh()
	
	return viewNode





_buildViewContentsRecursionLockSet = set()
	

class _GSymNodeViewInstance (object):
	"""
	Manages state that concerns a view of a specific sub-tree of a document
	"""
	
	__slots__ = [ 'xs', 'view', 'viewInstance', 'viewNode', 'refreshCells', 'styleSheetStack' ]

	def __init__(self, xs, view, viewInstance, viewNode):
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
	def __init__(self, xs, viewFactory, commandHistory, styleSheetDispatcher):
		self.xs = xs
		self.viewNodeInstanceStack = []
		self.generalNodeViewFunction = viewFactory._f_createViewFunction( self.viewNodeInstanceStack )
		# self._p_buildDVNode is a factory that builds DVNode instances for document subtrees
		self.view = DocView( self.xs, commandHistory, styleSheetDispatcher, self._p_rootNodeFactory )
		self.focusWidget = None
		
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
			nodeViewInstance = _GSymNodeViewInstance( docNodeKey.docNode, self.view, self, viewNode )
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
	
	
	
	
	def _runtime_queueRefresh(self):
		def _refresh():
			self.view.refresh()
		self.view.document.queueUserEvent( _refresh )
		
		
	def _runtime_queueSelect(self, node):
		def _select():
			assert isinstance( node, RelativeNode ), '%s'  %  ( type( node ), )
			docNodeKey = _relativeNodeToDocNodeKey( node )
			viewNode = self.view.getViewNodeForDocNodeKey( docNodeKey )
			if viewNode.focus is not None:
				viewNode.focus.makeCurrent()
			self.focusWidget = viewNode.focus
		self.view.document.queueUserEvent( _select )
		
		
		
		
			
	def _runtime_queueRefreshAndSelect(self, node):
		self._runtime_queueRefresh()
		self._runtime_queueSelect( node )
		
	
	def _runtime_queueSendEventToFocus(self, event):
		def _send():
			if self.focusWidget is not None:
				self._runtime_sendDocEventToWidget( self.focusWidget, event )
		self.view.document.queueUserEvent( _send )
		
		
	def _runtime_sendDocEventToWidget(self, widget, event):
		self.focusWidget = widget
		processedEvent = widget.sendDocEvent( event )
		if processedEvent is None:
			return True
		elif processedEvent is event:
			return False
		else:
			self._runtime_queueSendEventToFocus( processedEvent )
			return True
	
	
	def _runtime_handleKeyPress(self, widget, keyPressEvent):
		event = InteractorEventKey.fromDTKeyEvent( widget, True, keyPressEvent )
		return self._runtime_sendDocEventToWidget( widget, event )

	

	
	def _runtime_sendTokenListDocEvent(self, widget, tokens):
		event = InteractorEventTokenList( True, tokens )
		bHandled = self._runtime_sendDocEventToWidget( widget, event )
		if not bHandled:
			print 'gSymView._sendTokenListDocEvent: ***unhandled event*** %s'  %  ( event, )
		return bHandled
	
	def _runtime_onEntryModifed(self, widget, text, tokens):
		print "gSymView._onEntryModifed: text='%s', tokens=%s"  %  ( text, tokens )
		if len( tokens ) > 1:
			self._runtime_sendTokenListDocEvent( widget, tokens )
	
	def _runtime_onEntryFinished(self, widget, text, tokens, bUserEvent):
		if bUserEvent:
			self._runtime_sendTokenListDocEvent( widget, tokens )



		
		
		
class GSymViewNoNodeViewFunction (Exception):
	pass


class GSymViewFactory (object):
	"""
	Used to manufacture document views
	Manages state concerning a view that has been compiled.
	"""
	def __init__(self, world, name, moduleFactory):
		super( GSymViewFactory, self ).__init__()
		self.world = world
		self.name = name
		self.moduleFactory = moduleFactory
		
		
	def _f_createViewFunction(self, viewNodeInstanceStack):
		moduleGlobals = { '__view_node_instance_stack__' : viewNodeInstanceStack }
		moduleInstance = self.moduleFactory.instantiate( self.world, moduleGlobals )
		try:
			nodeViewFunction = moduleInstance.nodeViewFunction
		except AttributeError:
			raise GSymViewNoNodeViewFunction
		else:
			return nodeViewFunction
		
		
	def createDocumentView(self, xs, commandHistory, styleSheetDispatcher):
		viewInstance = _GSymViewInstance( xs, self, commandHistory, styleSheetDispatcher )
		return viewInstance.view


	


class GMetaComponentView (GMetaComponent):
	def compileSpecial(self, srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
		"""Compile special statements specific to view expressions"""
		name = srcXs[0]
	
		compileSubExp = lambda xs: compileGLispExprToPyTree( xs, context, True, compileSpecial )
		compileStyleSheetAccess = lambda xs: PyListLiteral( [ compileSubExp( x )  for x in xs ] )
		compileWidgetParams = lambda xs: [ compileGLispCallParamToPyTree( x, context, compileSpecial )   for x in xs ]
	
		if name == '$viewEval':
			# ($viewEval <document-subtree> ?<node-view-function>)
			if len( srcXs ) < 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $viewEval needs at least 1 parameter; the document subtree' )
			params = [ PyVar( '__view_node_instance_stack__' )[-1].debug( srcXs ), compileSubExp( srcXs[1] ) ]
			# View function
			if len( srcXs ) == 3:
				params.append( compileSubExp( srcXs[2] ) )
			return PyCall( PyVar( '__gsym__buildView__' ).debug( srcXs ), params, dbgSrc=srcXs )
		elif name == '$mapViewEval':
			# ($mapViewEval <document-subtree> ?<node-view-function>)
			if len( srcXs ) < 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $mapViewEval needs at least 1 parameter; the document subtree' )
			params = [ PyVar( '__view_node_instance_stack__' )[-1].debug( srcXs ), PyVar( 'x' ).debug( srcXs ) ]
			# View function
			if len( srcXs ) == 3:
				params.append( compileSubExp( srcXs[2] ) )
			itemExpr = PyCall( PyVar( '__gsym__buildView__' ), params ).debug( srcXs )
			return PyListComprehension( itemExpr, 'x', compileSubExp( srcXs[1] ), None ).debug( srcXs )
		elif name == '$style':
			#($style <settings_pairs>)
			#settings pair: (:key <value>)
			def _settingsPair(pairXs):
				if len( pairXs ) != 2:
					raiseCompilerError( GLispParameterListError, src, 'defineView: $style settings pair needs 2 parameters; the key and the value' )
				if not isinstance( pairXs[0], str )  and  not isinstance( pairXs[0], unicode ):
					raiseCompilerError( GLispItemTypeError, src, 'defineView: $style settings pair key must be a string' )
				if pairXs[0][0] != ':':
					raiseCompilerError( GLispItemError, src, 'defineView: $style settings pair key must start with a :' )
				return pyt_coerce( [ pairXs[0][1:], compileSubExp( pairXs[1] ) ] )
			return PyVar( '__gsym__GSymStyleSheet__' )( pyt_coerce( [ _settingsPair( pairXs )   for pairXs in srcXs[1:] ] ) ).debug( srcXs )
		elif name == '$applyStyle':
			#($applyStyle <stylesheet> <child>)
			if len( srcXs ) != 3:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $applyStyle needs 2 parameters; the style and the child content' )
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
		elif name == '$focus':
			#($focus <child>)
			if len( srcXs ) != 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $focus needs 1 parameters; the child content' )
			childResVarName = None
			_py_childResult = compileSubExp( srcXs[1] )

			if bNeedResult:
				childResVarName = context.temps.allocateTempName( 'view_special_child_result' )
				py_childResultTemp = PyVar( childResVarName ).assign_sideEffects( _py_childResult ).debug( srcXs )
				py_focusSet = PyVar( '__view_node_instance_stack__' )[-1].attr( 'viewNode' ).attr( 'focus' ).assign_sideEffects( PyVar( childResVarName ) ).debug( srcXs )
				context.body.append( py_childResultTemp )
				context.body.append( py_focusSet )
				return PyVar( childResVarName ).debug( srcXs )
			else:
				py_focusSet = PyVar( '__view_node_instance_stack__' )[-1].attr( 'viewNode' ).attr( 'focus' ).assign_sideEffects( _py_childResult ).debug( srcXs )
				context.body.append( py_focusSet )
				return None
		elif name == '$activeBorder':
			#($activeBorder <child> [<styleSheet>])
			if len( srcXs ) < 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $activeBorder needs at least 1 parameters; the child content' )
			return PyVar( '__gsym__activeBorder__' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), *compileWidgetParams( srcXs[2:]) ).debug( srcXs )
		elif name == '$border':
			#($border <child> [<styleSheet>])
			if len( srcXs ) < 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $border needs at least 1 parameters; the child content' )
			return PyVar( '__gsym__border__' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), *compileWidgetParams( srcXs[2:]) ).debug( srcXs )
		elif name == '$indent':
			#($indent <indentation> <child> [<styleSheet>])
			if len( srcXs ) < 3:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $border needs at least 2 parameters; the indentation and the child content' )
			return PyVar( '__gsym__indent__' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[2] ), compileSubExp( srcXs[1] ), *compileWidgetParams( srcXs[3:]) ).debug( srcXs )
		elif name == '$hline':
			#($hline [<styleSheet>])
			return PyVar( '__gsym__hline__', dbgSrc=srcXs )( PyVar( '__view_node_instance_stack__' )[-1], *compileWidgetParams( srcXs[1:]) ).debug( srcXs )
		elif name == '$label':
			#($label text [<styleSheet>])
			if len( srcXs ) < 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $label needs at least 1 parameter; the text' )
			return PyVar( '__gsym__label__', dbgSrc=srcXs )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), *compileWidgetParams( srcXs[2:]) ).debug( srcXs )
		elif name == '$markupLabel':
			#($markupLabel text [<styleSheet>])
			if len( srcXs ) < 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $label needs at least 1 parameter; the text' )
			return PyVar( '__gsym__markupLabel__', dbgSrc=srcXs )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), *compileWidgetParams( srcXs[2:]) ).debug( srcXs )
		elif name == '$entry':
			#($entry <label_text> <entry_text> <tokeniser> [<styleSheet>])
			if len( srcXs ) < 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $entry needs at least 3 parameters; the label text, the entry text, and the tokeniser' )
			return PyVar( '__gsym__entry__', dbgSrc=srcXs )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), compileSubExp( srcXs[2] ), compileSubExp( srcXs[3] ), *compileWidgetParams( srcXs[4:]) ).debug( srcXs )
		elif name == '$markupEntry':
			#($markupEntry <label_text> <entry_text> <tokeniser> [<styleSheet>])
			if len( srcXs ) < 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $markupEntry needs at least 3 parameters; the label text, the entry text, and the tokeniser' )
			return PyVar( '__gsym__markupEntry__', dbgSrc=srcXs )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), compileSubExp( srcXs[2] ), compileSubExp( srcXs[3] ), *compileWidgetParams( srcXs[4:]) ).debug( srcXs )
		elif name == '$customEntry':
			#($customEntry <child> <entry_text> <tokeniser> [<styleSheet>])
			if len( srcXs ) < 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $customEntry needs at least 3 parameters; the custom child, the entry text, and the tokeniser' )
			return PyVar( '__gsym__customEntry__', dbgSrc=srcXs )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), compileSubExp( srcXs[2] ), compileSubExp( srcXs[3] ), *compileWidgetParams( srcXs[4:]) ).debug( srcXs )
		elif name == '$hbox':
			#($hbox (child*) [<styleSheet>])
			if len( srcXs ) < 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $hbox needs at least 1 parameter; the children' )
			return PyVar( '__gsym__hbox__', dbgSrc=srcXs )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), *compileWidgetParams( srcXs[2:]) ).debug( srcXs )
		elif name == '$ahbox':
			#($ahbox (child*) [<styleSheet>])
			if len( srcXs ) < 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $ahbox needs at least 1 parameter; the children' )
			return PyVar( '__gsym__ahbox__', dbgSrc=srcXs )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), *compileWidgetParams( srcXs[2:]) ).debug( srcXs )
		elif name == '$vbox':
			#($vbox (child*) [<styleSheet>])
			if len( srcXs ) < 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $vbox needs at least 1 parameter; the children' )
			return PyVar( '__gsym__vbox__', dbgSrc=srcXs )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), *compileWidgetParams( srcXs[2:]) ).debug( srcXs )
		elif name == '$wrap':
			#($wrappedHBox (child*) [<styleSheet>])
			if len( srcXs ) < 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $wrappedHBox needs at least 1 parameter; the children' )
			return PyVar( '__gsym__wrappedHBox__', dbgSrc=srcXs )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), *compileWidgetParams( srcXs[2:]) ).debug( srcXs )
		elif name == '$wrapSep':
			#($wrappedHBox (child*) [<separatorFactory>] [<styleSheet>])
			if len( srcXs ) < 2:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $wrappedHBoxSep needs at least 1 parameter; the children' )
			return PyVar( '__gsym__wrappedHBoxSep__', dbgSrc=srcXs )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), *compileWidgetParams( srcXs[2:]) ).debug( srcXs )
		elif name == '$script':
			#($script <mainChild> <leftSuperChild> <leftSubChild> <rightSuperChild> <rightSubChild> [<styleSheet>])
			if len( srcXs ) < 6:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $script needs at least 5 parameters; the main, left-super, left-sub, right-super, and right-sub children' )
			return PyVar( '__gsym__script__' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), compileSubExp( srcXs[2] ), compileSubExp( srcXs[3] ), compileSubExp( srcXs[4] ), compileSubExp( srcXs[5] ),
						   *compileWidgetParams( srcXs[6:]) ).debug( srcXs )
		elif name == '$scriptLSuper':
			#($scriptLSuper <mainChild> <scriptChild> [<styleSheet>])
			if len( srcXs ) < 3:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $scriptLSuper needs at least 2 parameters; the main child, and the script child' )
			return PyVar( '__gsym__script__' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), compileSubExp( srcXs[2] ), None, None, None,
						   *compileWidgetParams( srcXs[3:]) ).debug( srcXs )
		elif name == '$scriptLSub':
			#($scriptLSub <mainChild> <scriptChild> [<styleSheet>])
			if len( srcXs ) < 3:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $scriptLSub needs at least 2 parameters; the main child, and the script child' )
			return PyVar( '__gsym__script__' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), None, compileSubExp( srcXs[2] ), None, None,
						   *compileWidgetParams( srcXs[3:]) ).debug( srcXs )
		elif name == '$scriptRSuper':
			#($scriptRSuper <mainChild> <scriptChild> [<styleSheet>])
			if len( srcXs ) < 3:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $scriptRSuper needs at least 2 parameters; the main child, and the script child' )
			return PyVar( '__gsym__script__' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), None, None, compileSubExp( srcXs[2] ), None,
						   *compileWidgetParams( srcXs[3:]) ).debug( srcXs )
		elif name == '$scriptRSub':
			#($scriptRSub <mainChild> <scriptChild> [<styleSheet>])
			if len( srcXs ) < 3:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $scriptRSub needs at least 2 parameters; the main child, and the script child' )
			return PyVar( '__gsym__script__' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), None, None, None, compileSubExp( srcXs[2] ),
						   *compileWidgetParams( srcXs[3:]) ).debug( srcXs )
		elif name == '$interact':
			#($interact <child> <interactors*>)
			if len( srcXs ) < 3:
				raiseCompilerError( GLispParameterListError, src, 'defineView: $interact needs at least 2 parameters; the child, and the interactor(s)' )
			return PyVar( '__gsym__interact__' )( PyVar( '__view_node_instance_stack__' )[-1], compileSubExp( srcXs[1] ), PyListLiteral( [ compileSubExp( x )   for x in srcXs[2:] ] ) ).debug( srcXs )
		else:
			raise GLispCompilerCouldNotCompileSpecial( srcXs )



	def getConstants(self):
		return {
			'ColourRGB' : Colour3f,
			'__gsym__buildView__' : _runtime_buildView,
			'__gsym__activeBorder__' : _runtime_activeBorder,
			'__gsym__border__' : _runtime_border,
			'__gsym__indent__' : _runtime_indent,
			'__gsym__hline__' : _runtime_hline,
			'__gsym__label__' : _runtime_label,
			'__gsym__markupLabel__' : _runtime_markupLabel,
			'__gsym__entry__' : _runtime_entry,
			'__gsym__markupEntry__' : _runtime_markupEntry,
			'__gsym__customEntry__' : _runtime_customEntry,
			'__gsym__hbox__' : _runtime_hbox,
			'__gsym__ahbox__' : _runtime_ahbox,
			'__gsym__vbox__' : _runtime_vbox,
			'__gsym__wrappedHBox__' : _runtime_wrappedHBox,
			'__gsym__wrappedHBoxSep__' : _runtime_wrappedHBoxSep,
			'__gsym__script__' : _runtime_script,
			'__gsym__interact__' : _runtime_interact,
			'__gsym__GSymStyleSheet__' : GSymStyleSheet,
			'__gsym__RelativeNode__' : RelativeNode,
			'__gsym__DTActiveBorder__' : DTActiveBorder,
			'__gsym__DTBorder__' : DTBorder,
			'__gsym__DTHLine__' : DTHLine,
			'__gsym__DTLabel__' : DTLabel,
			'__gsym__DTTokenisedEntryLabel__' : DTTokenisedEntryLabel,
			'__gsym__DTTokenisedCustomEntry__' : DTTokenisedCustomEntry,
			'__gsym__DTBox__' : DTBox,
			'__gsym__DTScript__' : DTScript,
			'__gsym__runtime_setKeyHandler__' : _runtime_setKeyHandler,
			'__gsym__runtime_binRefreshCell__' : _runtime_binRefreshCell,
			'__gsym__runtime_containerSeqRefreshCell__' : _runtime_containerSeqRefreshCell,
			'__gsym__runtime_scriptRefreshCell__' : _runtime_scriptRefreshCell,
			'__gsym__runtime_applyStyleSheetStack__' : _runtime_applyStyleSheetStack,
			'__gsym__runtime_applyStyle__' : _runtime_applyStyle,
			}
	
	
	def getGlobalNames(self):
		return [ '__view_node_instance_stack__' ]
	
	
	def getPrefixTrees(self):
		#return [ PyMultilineSrc( _gsymViewPrefixSrc ) ]
		return []


_gsymViewPrefixSrc = '''
def activeBorder(child, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTActiveBorder widget, with child, builds and registers a refresh cell
	"""
	widget = __gsym__DTActiveBorder__()
	__gsym__runtime_setKeyHandler__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_binRefreshCell__( __view_node_instance_stack__[-1], widget, child )
	__gsym__runtime_applyStyleSheetStack__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheets__( styleSheets, widget )
	return widget

def border(child, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTBorder widget, with child, builds and registers a refresh cell
	"""
	widget = __gsym__DTBorder__()
	__gsym__runtime_binRefreshCell__( __view_node_instance_stack__[-1], widget, child )
	__gsym__runtime_applyStyleSheetStack__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheets__( styleSheets, widget )
	return widget

def indent(child, indentation, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTBorder widget, with child, builds and registers a refresh cell
	"""
	widget = __gsym__DTBorder__()
	widget.leftMargin = indentation
	__gsym__runtime_binRefreshCell__( __view_node_instance_stack__[-1], widget, child )
	__gsym__runtime_applyStyleSheetStack__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheets__( styleSheets, widget )
	return widget

def hline(style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTLabel widget
	"""
	widget = __gsym__DTHLine__()
	__gsym__runtime_applyStyleSheetStack__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheets__( styleSheets, widget )
	return widget

def label(text, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTLabel widget
	"""
	if isinstance( text, __gsym__RelativeNode__ ):
		text = text.node
	widget = __gsym__DTLabel__( text )
	__gsym__runtime_applyStyleSheetStack__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheets__( styleSheets, widget )
	return widget

def entry(text, style=None):
	"""Builds a DTEntryLabel widget"""
	if isinstance( text, __gsym__RelativeNode__ ):
		text = text.node
	widget = __gsym__DTEntryLabel__(text)
	__gsym__runtime_setKeyHandler__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheetStack__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheets__( styleSheets, widget )
	return widget

def hbox(children, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a horizontal DTBox widget, with child, builds and registers a refresh cell
	"""
	widget = __gsym__DTBox__()
	__gsym__runtime_containerSeqRefreshCell__( __view_node_instance_stack__[-1], widget, children )
	__gsym__runtime_applyStyleSheetStack__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheets__( styleSheets, widget )
	return widget

def ahbox(children, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a horizontal DTBox widget, with child, builds and registers a refresh cell
	"""
	widget = __gsym__DTBox__( alignment=DTBox.ALIGN_BASELINES )
	__gsym__runtime_containerSeqRefreshCell__( __view_node_instance_stack__[-1], widget, children )
	__gsym__runtime_applyStyleSheetStack__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheets__( styleSheets, widget )
	return widget

def vbox(children, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a vertical DTBox widget, with child, builds and registers a refresh cell
	"""
	widget = __gsym__DTBox__( direction=DTBox.TOP_TO_BOTTOM, alignment=DTBox.ALIGN_LEFT )
	__gsym__runtime_containerSeqRefreshCell__( __view_node_instance_stack__[-1], widget, children )
	__gsym__runtime_applyStyleSheetStack__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheets__( styleSheets, widget )
	return widget

def scriptLSuper(mainChild, scriptChild, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTScript widget, with children, builds and registers refresh cells
	"""
	widget = __gsym__DTScript__()
	
	__gsym__runtime_scriptRefreshCell__( __view_node_instance_stack__[-1], widget, mainChild, 'mainChild' )
	__gsym__runtime_scriptRefreshCell__( __view_node_instance_stack__[-1], widget, scriptChild, 'leftSuperscriptChild' )
	__gsym__runtime_applyStyleSheetStack__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheets__( styleSheets, widget )
	return widget

def scriptLSub(mainChild, scriptChild, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTScript widget, with children, builds and registers refresh cells
	"""
	widget = __gsym__DTScript__()
	
	__gsym__runtime_scriptRefreshCell__( __view_node_instance_stack__[-1], widget, mainChild, 'mainChild' )
	__gsym__runtime_scriptRefreshCell__( __view_node_instance_stack__[-1], widget, scriptChild, 'leftSubscriptChild' )
	__gsym__runtime_applyStyleSheetStack__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheets__( styleSheets, widget )
	return widget

def scriptRSuper(mainChild, scriptChild, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTScript widget, with children, builds and registers refresh cells
	"""
	widget = __gsym__DTScript__()
	
	__gsym__runtime_scriptRefreshCell__( __view_node_instance_stack__[-1], widget, mainChild, 'mainChild' )
	__gsym__runtime_scriptRefreshCell__( __view_node_instance_stack__[-1], widget, scriptChild, 'rightSuperscriptChild' )
	__gsym__runtime_applyStyleSheetStack__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheets__( styleSheets, widget )
	return widget

def scriptRSub(mainChild, scriptChild, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTScript widget, with children, builds and registers refresh cells
	"""
	widget = __gsym__DTScript__()
	
	__gsym__runtime_scriptRefreshCell__( __view_node_instance_stack__[-1], widget, mainChild, 'mainChild' )
	__gsym__runtime_scriptRefreshCell__( __view_node_instance_stack__[-1], widget, scriptChild, 'rightSubscriptChild' )
	__gsym__runtime_applyStyleSheetStack__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheets__( styleSheets, widget )
	return widget

def script(mainChild, leftSuperChild, leftSubChild, rightSuperChild, rightSubChild, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTScript widget, with children, builds and registers refresh cells
	"""
	widget = __gsym__DTScript__()
	
	__gsym__runtime_scriptRefreshCell__( __view_node_instance_stack__[-1], widget, mainChild, 'mainChild' )
	if leftSuperChild is not None:
		__gsym__runtime_scriptRefreshCell__( __view_node_instance_stack__[-1], widget, leftSuperChild, 'leftSuperscriptChild' )
	if leftSubChild is not None:
		__gsym__runtime_scriptRefreshCell__( __view_node_instance_stack__[-1], widget, leftSubChild, 'leftSubscriptChild' )
	if rightSuperChild is not None:
		__gsym__runtime_scriptRefreshCell__( __view_node_instance_stack__[-1], widget, rightSuperChild, 'rightSuperscriptChild' )
	if rightSubChild is not None:
		__gsym__runtime_scriptRefreshCell__( __view_node_instance_stack__[-1], widget, rightSubChild, 'rightSubscriptChild' )
	__gsym__runtime_applyStyleSheetStack__( __view_node_instance_stack__[-1], widget )
	__gsym__runtime_applyStyleSheets__( styleSheets, widget )
	return widget
'''
