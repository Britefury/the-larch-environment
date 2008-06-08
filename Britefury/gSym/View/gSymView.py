##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import sys

from copy import copy

from Britefury.Math.Math import Colour3f

from Britefury.Cell.Cell import Cell

from Britefury.DocPresent.Toolkit.DTWidget import DTWidget
from Britefury.DocPresent.Toolkit.DTActiveBorder import DTActiveBorder
from Britefury.DocPresent.Toolkit.DTBin import DTBin 
from Britefury.DocPresent.Toolkit.DTBorder import DTBorder
from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTHighlight import DTHighlight
from Britefury.DocPresent.Toolkit.DTHLine import DTHLine
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTScript import DTScript
from Britefury.DocPresent.Toolkit.DTEntryLabel import DTEntryLabel
from Britefury.DocPresent.Toolkit.DTCustomEntry import DTCustomEntry
from Britefury.DocPresent.Toolkit.DTTokenisedEntryLabel import DTTokenisedEntryLabel
from Britefury.DocPresent.Toolkit.DTTokenisedCustomEntry import DTTokenisedCustomEntry
from Britefury.DocPresent.Toolkit.DTWrappedHBox import DTWrappedHBox
from Britefury.DocPresent.Toolkit.DTWrappedHBoxWithSeparators import DTWrappedHBoxWithSeparators


from Britefury.DocView.DVNode import DVNode
from Britefury.DocView.DocView import DocView
from Britefury.DocView.DocViewNodeTable import DocNodeKey


from Britefury.GLisp.GLispUtil import isGLispList, gLispSrcToString

from Britefury.GLisp.GLispDispatch import dispatch

from Britefury.gSym.View.gSymStyleSheet import GSymStyleSheet
from Britefury.gSym.View.Interactor import Interactor, NoEventMatch
from Britefury.gSym.View.InteractorEvent import InteractorEventKey, InteractorEventText, InteractorEventTokenList
from Britefury.gSym.View import ListView
from Britefury.gSym.View.UnparsedText import UnparsedText

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



def raiseRuntimeError(exceptionClass, src, reason):
	raise exceptionClass, reason  +  '   ::   '  +  gLispSrcToString( src, 3 )



def _relativeNodeToDocNodeKey(node):
	return DocNodeKey( node.node, node.parent, node.indexInParent )



_globalNodeViewInstanceStack = []



class _ViewQueue (object):
	def __init__(self, view):
		super( _ViewQueue, self ).__init__()
		self._events = []
		self._view = view
		self.final = None
		
		
	def queue(self, f):
		if f not in self._events:
			if len( self._events ) == 0:
				self._view.document.queueImmediateEvent( self._p_fire )
			self._events.append( f )
		
	def dequeue(self, f):
		if f in self._events:
			self._events.remove( f )
			if len( self._events ) == 0:
				self._view.document.dequeueImmediateEvent( self._p_fire )
		
		
	def _p_fire(self):
		while len( self._events ) > 0:
			events = copy( self._events )
			self._events = []
			for event in events:
				event()
		if self.final is not None:
			self.final()
			self.final = None
				



def _buildRefreshCellAndRegister(viewNodeInstance, refreshFunction):
	"""
	Runtime - called by compiled code at run-time
	Builds a refresh cell, and registers it by appending it to @refreshCells
	"""
	cell = Cell()
	cell.function = refreshFunction
	viewNodeInstance.refreshCells.append( cell )
	
def _binRefreshCell(viewNodeInstance, bin, child):
	"""
	Runtime - called by compiled code at run-time
	Builds and registers a refresh cell (if necessary) for a widget that is an instance of DTBin
	"""
	if isinstance( child, DVNode ):
		chNode = child
		def _binRefresh():
			chNode.refresh()
			bin.child = chNode.widget
		_buildRefreshCellAndRegister( viewNodeInstance, _binRefresh )
	elif isinstance( child, DTWidget ):
		bin.child = child
	else:
		raiseRuntimeError( TypeError, viewNodeInstance.xs, '_GSymNodeViewInstance._binRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )

def _customEntryRefreshCell(viewNodeInstance, customEntry, child):
	"""
	Runtime - called by compiled code at run-time
	Builds and registers a refresh cell (if necessary) for a widget that is an instance of DTCusomEntry
	"""
	if isinstance( child, DVNode ):
		chNode = child
		def _customEntryRefresh():
			chNode.refresh()
			customEntry.customChild = chNode.widget
		_buildRefreshCellAndRegister( viewNodeInstance, _customEntryRefresh )
	elif isinstance( child, DTWidget ):
		customEntry.customChild = child
	else:
		raiseRuntimeError( TypeError, viewNodeInstance.xs, '_GSymNodeViewInstance._customEntryRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )

def _containerSeqRefreshCell(viewNodeInstance, widget, children):
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
	_buildRefreshCellAndRegister( viewNodeInstance, _containerSeqRefresh )

def _scriptRefreshCell(viewNodeInstance, script, child, childSlotAttrName):
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
		_buildRefreshCellAndRegister( viewNodeInstance, _scriptRefresh )
	elif isinstance( child, DTWidget ):
		#script.mainChild = child
		setattr( script, childSlotAttrName, child)
	else:
		raiseRuntimeError( TypeError, viewNodeInstance.xs, '_GSymNodeViewInstance._scriptRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )


	
	
def _applyStyleSheetStack(viewNodeInstance, widget):
	for styleSheet in viewNodeInstance.styleSheetStack:
		styleSheet.applyToWidget( widget )




	


			
	

def _setKeyHandler(viewNodeInstance, widget):
	widget.keyHandler = viewNodeInstance.viewInstance._p_handleKeyPress


	
	
	
	
	
def _applyStyle(style, widget):
	if style is not None:
		if isinstance( style, GSymStyleSheet ):
			style.applyToWidget( widget )
		else:
			for s in style:
				s.applyToWidget( widget )
				
				

def activeBorder(child, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTActiveBorder widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	widget = DTActiveBorder()
	_setKeyHandler( viewNodeInstance, widget )
	_binRefreshCell( viewNodeInstance, widget, child )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def border(child, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTBorder widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	widget = DTBorder()
	_binRefreshCell( viewNodeInstance, widget, child )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def indent(child, indentation, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTBorder widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	widget = DTBorder()
	widget.allMargins = 0.0
	widget.leftMargin = indentation
	_binRefreshCell( viewNodeInstance, widget, child )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def highlight(child, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTHighlight widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	widget = DTHighlight()
	_binRefreshCell( viewNodeInstance, widget, child )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def hline(style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTLabel widget
	"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	widget = DTHLine()
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def label(text, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTLabel widget
	"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	if isinstance( text, RelativeNode ):
		text = text.node
	widget = DTLabel( text )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def markupLabel(text, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a markup DTLabel widget
	"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	if isinstance( text, RelativeNode ):
		text = text.node
	widget = DTLabel( text )
	widget.bUseMarkup = True
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget



def entry(labelText, entryText, style=None):
	"""Builds a DTEntryLabel widget"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	if isinstance( labelText, RelativeNode ):
		labelText = labelText.node
	if isinstance( entryText, RelativeNode ):
		entryText = entryText.node
	widget = DTEntryLabel( labelText, entryText )
	widget.textModifiedSignal.connect( viewNodeInstance.viewInstance._p_onEntryModifed )
	widget.finishEditingSignal.connect( viewNodeInstance.viewInstance._p_onEntryFinished )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def markupEntry(labelText, entryText, style=None):
	"""Builds a DTEntryLabel widget"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	if isinstance( labelText, RelativeNode ):
		labelText = labelText.node
	if isinstance( entryText, RelativeNode ):
		entryText = entryText.node
	widget = DTEntryLabel( labelText, entryText )
	widget.textModifiedSignal.connect( viewNodeInstance.viewInstance._p_onEntryModifed )
	widget.finishEditingSignal.connect( viewNodeInstance.viewInstance._p_onEntryFinished )
	widget.bLabelUseMarkup = True
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def customEntry(customChild, entryText, style=None):
	"""Builds a DTEntryLabel widget"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	if isinstance( entryText, RelativeNode ):
		entryText = entryText.node
	elif isinstance( entryText, UnparsedText ):
		entryText = entryText.getText()
	widget = DTCustomEntry( entryText )
	widget.textModifiedSignal.connect( viewNodeInstance.viewInstance._p_onEntryModifed )
	widget.finishEditingSignal.connect( viewNodeInstance.viewInstance._p_onEntryFinished )
	_customEntryRefreshCell( viewNodeInstance, widget, customChild )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget



def tokEntry(labelText, entryText, tokeniser, style=None):
	"""Builds a DTEntryLabel widget"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	if isinstance( labelText, RelativeNode ):
		labelText = labelText.node
	if isinstance( entryText, RelativeNode ):
		entryText = entryText.node
	widget = DTTokenisedEntryLabel( tokeniser, labelText, entryText )
	widget.textModifiedSignal.connect( viewNodeInstance.viewInstance._p_onTokenisedEntryModifed )
	widget.finishEditingSignal.connect( viewNodeInstance.viewInstance._p_onTokenisedEntryFinished )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def tokMarkupEntry(labelText, entryText, tokeniser, style=None):
	"""Builds a DTEntryLabel widget"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	if isinstance( labelText, RelativeNode ):
		labelText = labelText.node
	if isinstance( entryText, RelativeNode ):
		entryText = entryText.node
	widget = DTTokenisedEntryLabel( tokeniser, labelText, entryText )
	widget.textModifiedSignal.connect( viewNodeInstance.viewInstance._p_onTokenisedEntryModifed )
	widget.finishEditingSignal.connect( viewNodeInstance.viewInstance._p_onTokenisedEntryFinished )
	widget.bLabelUseMarkup = True
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def tokCustomEntry(customChild, entryText, tokeniser, style=None):
	"""Builds a DTEntryLabel widget"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	if isinstance( entryText, RelativeNode ):
		entryText = entryText.node
	elif isinstance( entryText, UnparsedText ):
		entryText = entryText.getText()
	widget = DTTokenisedCustomEntry( tokeniser, entryText )
	widget.textModifiedSignal.connect( viewNodeInstance.viewInstance._p_onTokenisedEntryModifed )
	widget.finishEditingSignal.connect( viewNodeInstance.viewInstance._p_onTokenisedEntryFinished )
	_customEntryRefreshCell( viewNodeInstance, widget, customChild )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget



def hbox(children, style=None, alignment=DTBox.ALIGN_CENTRE, spacing=0.0):
	"""
	Runtime - called by compiled code at run-time
	Builds a horizontal DTBox widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	widget = DTBox( spacing=spacing, alignment=alignment )
	_containerSeqRefreshCell( viewNodeInstance, widget, children )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def ahbox(children, style=None, spacing=0.0):
	"""
	Runtime - called by compiled code at run-time
	Builds a horizontal DTBox widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	widget = DTBox( spacing=spacing, alignment=DTBox.ALIGN_BASELINES )
	_containerSeqRefreshCell( viewNodeInstance, widget, children )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def vbox(children, style=None, alignment=DTBox.ALIGN_LEFT, spacing=0.0):
	"""
	Runtime - called by compiled code at run-time
	Builds a vertical DTBox widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	widget = DTBox( direction=DTBox.TOP_TO_BOTTOM, spacing=spacing, alignment=alignment )
	_containerSeqRefreshCell( viewNodeInstance, widget, children )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def wrappedHBox(children, style=None, spacing=0.0, indentation=0.0):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTWrappedHBox widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	widget = DTWrappedHBox( spacing=spacing, indentation=indentation )
	_containerSeqRefreshCell( viewNodeInstance, widget, children )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def wrappedHBoxSep(children, separatorFactory=',', style=None, spacing=0.0, indentation=0.0):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTWrappedHBoxWithSeparators widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	widget = DTWrappedHBoxWithSeparators( separatorFactory, spacing=spacing, indentation=indentation )
	_containerSeqRefreshCell( viewNodeInstance, widget, children )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def script(mainChild, leftSuperChild, leftSubChild, rightSuperChild, rightSubChild, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTActiveBorder widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	widget = DTScript()
	
	_scriptRefreshCell( viewNodeInstance, widget, mainChild, 'mainChild' )
	if leftSuperChild is not None:
		_scriptRefreshCell( viewNodeInstance, widget, leftSuperChild, 'leftSuperscriptChild' )
	if leftSubChild is not None:
		_scriptRefreshCell( viewNodeInstance, widget, leftSubChild, 'leftSubscriptChild' )
	if rightSuperChild is not None:
		_scriptRefreshCell( viewNodeInstance, widget, rightSuperChild, 'rightSuperscriptChild' )
	if rightSubChild is not None:
		_scriptRefreshCell( viewNodeInstance, widget, rightSubChild, 'rightSubscriptChild' )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget

def scriptLSuper(mainChild, scriptChild, style=None):
	return script( mainChild, scriptChild, None, None, None, style )

def scriptLSub(mainChild, scriptChild, style=None):
	return script( mainChild, None, scriptChild, None, None, style )

def scriptRSuper(mainChild, scriptChild, style=None):
	return script( mainChild, None, None, scriptChild, None, style )

def scriptRSub(mainChild, scriptChild, style=None):
	return script( mainChild, None, None, None, scriptChild, style )




def listView(layout, beginDelim, endDelim, separatorFactory, children, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a list view.
	@layout controls the layout
	"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	widget, refreshCell = ListView.listView( viewNodeInstance.xs, children, layout, beginDelim, endDelim, separatorFactory )
	viewNodeInstance.refreshCells.append( refreshCell )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	return widget





class _DocEventHandler (object):
	def __init__(self, viewInstance, interactors):
		super( _DocEventHandler, self ).__init__()
		self.viewInstance = viewInstance
		self.interactors = interactors
		
	def __call__(self, event):
		for interactor in self.interactors:
			try:
				nodeToSelect, ev = interactor.handleEvent( event )
			except NoEventMatch:
				pass
			else:
				if ev is not event:
					self.viewInstance._p_queueRefreshAndSelect( nodeToSelect )
					return ev
		return event
		

def interact(child, *interactors):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTBorder widget, with child, builds and registers a refresh cell
	"""
	
	viewNodeInstance = _globalNodeViewInstanceStack[-1]

	def _processChild(c):
		if isinstance( c, DVNode ):
			widget = DTBin()
			widget.addDocEventHandler( _DocEventHandler( viewNodeInstance.viewInstance, interactors ) )
			_binRefreshCell( viewNodeInstance, widget, child )
		elif isinstance( c, DTWidget ):
			c.addDocEventHandler( _DocEventHandler( viewNodeInstance.viewInstance, interactors ) )
		else:
			raiseRuntimeError( TypeError, viewNodeInstance.xs, 'interact: could not process child of type %s'  %  ( type( c ).__name__, ) )
			
	if isinstance( child, list )  or  isinstance( child, tuple ):
		for c in child:
			_processChild( c )
	else:
		_processChild( child )
		
	return child
		

def focus(child):
	viewNodeInstance = _globalNodeViewInstanceStack[-1]
	viewNodeInstance.viewNode.focus = child
	return child



def viewEval(content, nodeViewFunction=None, state=None):
	"""Build a view for a document subtree (@content)"""
	viewNodeInstance = _globalNodeViewInstanceStack[-1]

	if not isinstance( content, RelativeNode ):
		raise TypeError, 'buildView: content is not a RelativeNode'
		
	# A call to DocNode._f_buildView builds the view, and puts it in the DocView's table
	viewInstance = viewNodeInstance.viewInstance
	nodeFactory = viewInstance._f_makeNodeFactory( nodeViewFunction, state )
	viewNode = viewNodeInstance.view._f_buildView( content.node, content.parent, content.indexInParent, nodeFactory )
	viewNode._f_setContentsFactory( viewNodeInstance.viewInstance._f_makeNodeContentsFactory( nodeViewFunction, state ) )
	viewNode.refresh()
	
	return viewNode


def mapViewEval(content, nodeViewFunction=None, state=None):
	return [ viewEval( x, nodeViewFunction, state )   for x in content ]





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
	def __init__(self, xs, viewFactory, commandHistory):
		self.xs = xs
		self.viewNodeInstanceStack = []
		self.generalNodeViewFunction = viewFactory._f_createViewFunction( self.viewNodeInstanceStack )
		# self._p_buildDVNode is a factory that builds DVNode instances for document subtrees
		self.view = DocView( self.xs, commandHistory, self._p_rootNodeFactory )
		self.focusWidget = None
		self._queue = _ViewQueue( self.view )
		
		self._nodeContentsFactories = {}
		
	
	def _f_makeNodeFactory(self, nodeViewFunction, state):
		def _nodeFactory(docNode, view, docNodeKey):
			# Build a DVNode for the document subtree at @docNode
			# self._p_buildNodeContents is a factory that builds the contents withing the DVNode
			node = DVNode( docNode, view, docNodeKey )
			node._f_setContentsFactory( self._f_makeNodeContentsFactory( nodeViewFunction, state ) )
			return node
		return _nodeFactory
	

	def _p_rootNodeFactory(self, docNode, view, docNodeKey):
		# Build a DVNode for the document subtree at @docNode
		# self._p_buildNodeContents is a factory that builds the contents withing the DVNode
		node = DVNode( docNode, view, docNodeKey )
		node._f_setContentsFactory( self._f_makeNodeContentsFactory( None, None ) )
		return node
	


	def _f_makeNodeContentsFactory(self, nodeViewFunction, state):
		def _buildNodeContents(viewNode, docNodeKey):
			# Create the node view instance
			nodeViewInstance = _GSymNodeViewInstance( docNodeKey.docNode, self.view, self, viewNode )
			relativeNode = relative( docNodeKey.docNode, docNodeKey.parentDocNode, docNodeKey.index )
			# Build the contents
			viewContents = self._p_buildNodeViewContents( nodeViewInstance, relativeNode, nodeViewFunction, state )
			# Get the refresh cells that need to be monitored, and hand them to the DVNode
			viewNode._f_setRefreshCells( nodeViewInstance.refreshCells )
			# Return the contents
			return viewContents
		
		#Memoise the contents factory; keyed by @nodeViewFunction and @state
		key = nodeViewFunction, state
		try:
			return self._nodeContentsFactories[key]
		except KeyError:
			factory = _buildNodeContents
			self._nodeContentsFactories[key] = factory
			return factory
		
	
	def _p_buildNodeViewContents(self, nodeViewInstance, content, nodeViewFunction, state):
		"""Runtime - build the contents of a view node"""
		if nodeViewFunction is None:
			nodeViewFunction = self.generalNodeViewFunction

		#1. Push @nodeViewInstance onto the view instance's view node instance stack
		# This is done so that the functions that have been compiled can get a reference to @this
		self.viewNodeInstanceStack.append( nodeViewInstance )
		_globalNodeViewInstanceStack.append( nodeViewInstance )
		
		#2. Create the view contents
		viewContents = nodeViewFunction( content, state )
		
		#3. Pop @self from the view instance's view node instance stack
		_globalNodeViewInstanceStack.pop()
		self.viewNodeInstanceStack.pop()
		
		#4. Return the view contents
		return viewContents
	
	
	
	
	def _p_queueRefresh(self):
		def _refresh():
			self.view.refresh()
		self._queue.queue( _refresh )
		
		
	def _p_queueSelect(self, node):
		def _focus():
			assert isinstance( node, RelativeNode ), '%s'  %  ( type( node ), )
			docNodeKey = _relativeNodeToDocNodeKey( node )
			try:
				viewNode = self.view.getViewNodeForDocNodeKey( docNodeKey )
			except KeyError:
				self.focusWidget = None
			else:
				self.focusWidget = viewNode.focus
		self._queue.queue( _focus )
		
		
		def _select():
			if self.focusWidget is not None:
				#self.focusWidget.makeCurrent()
				self.focusWidget.startEditing()
		self._queue.final = _select
		
		
			
	def _p_queueRefreshAndSelect(self, node):
		self._p_queueRefresh()
		self._p_queueSelect( node )
		
	
	def _p_queueSendEventToFocus(self, event):
		def _send():
			if self.focusWidget is not None:
				self._p_sendDocEventToWidget( self.focusWidget, event )
		self._queue.queue( _send )
		
		
	def _p_sendDocEventToWidget(self, widget, event):
		self.focusWidget = widget
		processedEvent = widget.sendDocEvent( event )
		if processedEvent is None:
			return True
		elif processedEvent is event:
			return False
		else:
			self._p_queueSendEventToFocus( processedEvent )
			return True
	
	
	def _p_handleKeyPress(self, widget, keyPressEvent):
		event = InteractorEventKey.fromDTKeyEvent( widget, True, keyPressEvent )
		return self._p_sendDocEventToWidget( widget, event )

	

	
	def _p_sendTextDocEvent(self, widget, text):
		event = InteractorEventText( True, text )
		bHandled = self._p_sendDocEventToWidget( widget, event )
		if not bHandled:
			print 'gSymView._sendTokenListDocEvent: ***unhandled event*** %s'  %  ( event, )
		return bHandled
	
	def _p_onEntryModifed(self, widget, text):
		pass
	
	def _p_onEntryFinished(self, widget, text, bChanged, bUserEvent):
		if bUserEvent  and  bChanged:
			self._p_sendTextDocEvent( widget, text )

			
			
	def _p_sendTokenListDocEvent(self, widget, tokens):
		event = InteractorEventTokenList( True, tokens )
		bHandled = self._p_sendDocEventToWidget( widget, event )
		if not bHandled:
			print 'gSymView._sendTokenListDocEvent: ***unhandled event*** %s'  %  ( event, )
		return bHandled
	
	def _p_onTokenisedEntryModifed(self, widget, text, tokens):
		if len( tokens ) > 1:
			self._p_sendTokenListDocEvent( widget, tokens )
	
	def _p_onTokenisedEntryFinished(self, widget, text, tokens, bChanged, bUserEvent):
		if bUserEvent  and  bChanged:
			self._p_sendTokenListDocEvent( widget, tokens )



			
class GSymView (object):
	def __call__(self, xs, state):
		return dispatch( self, xs, state )
	
		
		
		
class GSymViewFactory (object):
	"""
	Used to manufacture document views
	Manages state concerning a view that has been compiled.
	"""
	def __init__(self, world, name, viewClass):
		super( GSymViewFactory, self ).__init__()
		self.world = world
		self.name = name
		self.viewClass = viewClass
		
		
	def _f_createViewFunction(self, viewNodeInstanceStack):
		return self.viewClass()
		
		
	def createDocumentView(self, xs, commandHistory):
		viewInstance = _GSymViewInstance( xs, self, commandHistory )
		return viewInstance.view


	


