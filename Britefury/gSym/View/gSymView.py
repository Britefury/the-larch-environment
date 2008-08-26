##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import sys

from copy import copy

from Britefury.Cell.Cell import Cell

from Britefury.DocView.DVNode import DVNode
from Britefury.DocView.DocView import DocView


from Britefury.GLisp.GLispUtil import isGLispList, gLispSrcToString

from Britefury.GLisp.GLispDispatch import dispatch

from Britefury.gSym.View.Interactor import Interactor, NoEventMatch
from Britefury.gSym.View.InteractorEvent import InteractorEventKey, InteractorEventText, InteractorEventBackspaceStart, InteractorEventDeleteEnd
from Britefury.gSym.View import ListView
from Britefury.gSym.View.UnparsedText import UnparsedText

from Britefury.DocTree.DocTree import DocTree
from Britefury.DocTree.DocTreeNode import DocTreeNode


from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.ElementTree import *
from BritefuryJ.DocPresent.StyleSheets import *



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
	Builds a refresh cell, and registers it by appending it to @refreshCells
	"""
	cell = Cell()
	cell.function = refreshFunction
	viewNodeInstance.refreshCells.append( cell )

def _registerViewNodeRelationship(viewNodeInstance, childNode):
	viewNodeInstance.viewNode._registerChild( childNode )


def _binRefreshCell(viewNodeInstance, bin, child):
	"""
	Runtime - called by compiled code at run-time
	Builds and registers a refresh cell (if necessary) for a widget that is an instance of DTBin
	"""
	if isinstance( child, DVNode ):
		chNode = child
		def _binRefresh():
			chNode.refresh()
			bin.setChild( chNode.element )
			_registerViewNodeRelationship( viewNodeInstance, chNode )
		_buildRefreshCellAndRegister( viewNodeInstance, _binRefresh )
	elif isinstance( child, Element ):
		bin.setChild( child )
	else:
		raiseRuntimeError( TypeError, viewNodeInstance.xs, '_GSymNodeViewInstance._binRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )

def _containerSeqRefreshCell(viewNodeInstance, container, children):
	"""
	Runtime - called by compiled code at run-time
	Builds and registers a refresh cell (if necessary) for a widget that is an instance of DTBox
	"""
	def _containerSeqRefresh():
		elements = []
		for child in children:
			if isinstance( child, DVNode ):
				child.refresh()
				elements.append( child.element )
				_registerViewNodeRelationship( viewNodeInstance, child )
			elif isinstance( child, Element ):
				elements.append( child )
			else:
				raiseRuntimeError( TypeError, viewNodeInstance.xs, 'defineView: _containerSeqRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )
		container.setChildren( elements )
	_buildRefreshCellAndRegister( viewNodeInstance, _containerSeqRefresh )

def _scriptRefreshCell(viewNodeInstance, script, child, slotIndex):
	"""
	Runtime - called by compiled code at run-time
	Builds and registers a refresh cell (if necessary) for a widget that is an instance of DTBin
	"""
	if isinstance( child, DVNode ):
		chNode = child
		def _scriptRefresh():
			chNode.refresh()
			script.setChild( slotIndex, chNode.element )
			_registerViewNodeRelationship( viewNodeInstance, chNode )
		_buildRefreshCellAndRegister( viewNodeInstance, _scriptRefresh )
	elif isinstance( child, Element ):
		script.setChild( slotIndex, child )
	else:
		raiseRuntimeError( TypeError, viewNodeInstance.xs, '_GSymNodeViewInstance._scriptRefreshCell: could not process child of type %s'  %  ( type( child ).__name__, ) )


	
	
			
	

def _setKeyHandler(viewNodeInstance, widget):
	widget.keyHandler = viewNodeInstance.viewInstance._p_handleKeyPress




#
#
# 'ctx' parameters are _GSymNodeViewInstance instances
#
#



def border(ctx, styleSheet, child):
	"""
	Runtime - called by compiled code at run-time
	Builds a DPBorder widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = ctx
	element = BorderElement( styleSheet )
	_binRefreshCell( viewNodeInstance, element, child )
	return element

def indent(ctx, indentation, child):
	"""
	Runtime - called by compiled code at run-time
	Builds a DPBorder widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = ctx
	styleSheet = viewNodeInstance.viewInstance._indentationStyleSheet( indentation )
	element = BorderElement( styleSheet )
	_binRefreshCell( viewNodeInstance, element, child )
	return element

def text(ctx, styleSheet, txt):
	"""
	Runtime - called by compiled code at run-time
	Builds a DPText widget
	"""
	viewNodeInstance = ctx
	element = TextElement( styleSheet, txt )
	return element



def hbox(ctx, styleSheet, children):
	"""
	Runtime - called by compiled code at run-time
	Builds a DPHBox widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = ctx
	element = HBoxElement( styleSheet )
	_containerSeqRefreshCell( viewNodeInstance, element, children )
	return element

_ahboxStyleSheet = HBoxStyleSheet( DPHBox.Alignment.BASELINES, 0.0, False, 0.0 )
def ahbox(ctx, children):
	"""
	Runtime - called by compiled code at run-time
	Builds a DPHBox widget, with child, builds and registers a refresh cell
	"""
	return hbox( ctx, _ahboxStyleSheet, children )

def vbox(ctx, styleSheet, children):
	"""
	Runtime - called by compiled code at run-time
	Builds a vertical DTBox widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = ctx
	element = VBoxElement( styleSheet )
	_containerSeqRefreshCell( viewNodeInstance, element, children )
	return element

def paragraph(ctx, styleSheet, children):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTFlow widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = ctx
	element = ParagraphElement( styleSheet )
	_containerSeqRefreshCell( viewNodeInstance, element, children )
	return element


def script(ctx, styleSheet, mainChild, leftSuperChild, leftSubChild, rightSuperChild, rightSubChild):
	"""
	Runtime - called by compiled code at run-time
	Builds a DTActiveBorder widget, with child, builds and registers a refresh cell
	"""
	viewNodeInstance = ctx
	element = ScriptElement( styleSheet )
	
	_scriptRefreshCell( viewNodeInstance, widget, mainChild, DPScript.MAIN )
	if leftSuperChild is not None:
		_scriptRefreshCell( viewNodeInstance, widget, leftSuperChild, DPScript.LEFTSUPER )
	if leftSubChild is not None:
		_scriptRefreshCell( viewNodeInstance, widget, leftSubChild, DPScript.LEFTSUB )
	if rightSuperChild is not None:
		_scriptRefreshCell( viewNodeInstance, widget, rightSuperChild, DPScript.RIGHTSUPER )
	if rightSubChild is not None:
		_scriptRefreshCell( viewNodeInstance, widget, rightSubChild, DPScript.RIGHTSUB )
	return element

def scriptLSuper(ctx, styleSheet, mainChild, scriptChild):
	return script( ctx, styleSheet, mainChild, scriptChild, None, None, None )

def scriptLSub(ctx, styleSheet, mainChild, scriptChild):
	return script( ctx, styleSheet, mainChild, None, scriptChild, None, None )

def scriptRSuper(ctx, styleSheet, mainChild, scriptChild):
	return script( ctx, styleSheet, mainChild, None, None, scriptChild, None )

def scriptRSub(ctx, styleSheet, mainChild, scriptChild):
	return script( ctx, styleSheet, mainChild, None, None, None, scriptChild )




def listView(ctx, layout, beginDelim, endDelim, separatorFactory, children):
	"""
	Runtime - called by compiled code at run-time
	Builds a list view.
	@layout controls the layout
	"""
	viewNodeInstance = ctx
	element, refreshCell = ListView.listView( viewNodeInstance.xs, layout, beginDelim, endDelim, separatorFactory, children )
	viewNodeInstance.refreshCells.append( refreshCell )
	def _registerRelationships():
		for ch in children:
			if isinstance( ch, DVNode ):
				_registerViewNodeRelationship( viewNodeInstance, ch )
	_buildRefreshCellAndRegister( viewNodeInstance, _registerRelationships )
	return element


listViewStrToElementFactory = ListView.listViewStrToElementFactory




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
				if nodeToSelect is not None:
					self.viewInstance._p_queueRefreshAndSelect( nodeToSelect )
				if ev is not event:
					return ev
		return event
		

def interact(ctx, child, *interactors):
	"""
	Runtime - called by compiled code at run-time
	Connects interactors
	"""
	
	viewNodeInstance = ctx

	def _processChild(c):
		if isinstance( c, DVNode ):
			element = BinElement()
			element.addDocEventHandler( _DocEventHandler( viewNodeInstance.viewInstance, interactors ) )
			_binRefreshCell( viewNodeInstance, element, c )
			return widget
		elif isinstance( c, Element ):
			c.addDocEventHandler( _DocEventHandler( viewNodeInstance.viewInstance, interactors ) )
			return c
		else:
			raiseRuntimeError( TypeError, viewNodeInstance.xs, 'interact: could not process child of type %s'  %  ( type( c ).__name__, ) )
			
	if isinstance( child, list )  or  isinstance( child, tuple ):
		return [ _processChild( c )   for c in child ]
	else:
		return _processChild( child )
		



def viewEval(ctx, content, nodeViewFunction=None, state=None):
	"""Build a view for a document subtree (@content)"""
	viewNodeInstance = ctx

	if not isinstance( content, DocTreeNode ):
		raise TypeError, 'buildView: content is not a DocTreeNode'
		
	# A call to DocNode.buildNodeView builds the view, and puts it in the DocView's table
	viewInstance = viewNodeInstance.viewInstance
	nodeFactory = viewInstance._f_makeNodeFactory( nodeViewFunction, state )
	viewNode = viewNodeInstance.view.buildNodeView( content, nodeFactory )
	viewNode._f_setContentsFactory( viewNodeInstance.viewInstance._f_makeNodeContentsFactory( nodeViewFunction, state ) )
	viewNode.refresh()
	
	return viewNode


def mapViewEval(ctx, content, nodeViewFunction=None, state=None):
	return [ viewEval( ctx, x, nodeViewFunction, state )   for x in content ]







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
	def __init__(self, tree, xs, viewFactory, commandHistory):
		self.tree = tree
		self.xs = xs
		self.viewNodeInstanceStack = []
		self.generalNodeViewFunction = viewFactory.createViewFunction( self.viewNodeInstanceStack )
		# self._p_buildDVNode is a factory that builds DVNode instances for document subtrees
		self.view = DocView( self.tree, self.xs, commandHistory, self._p_rootNodeFactory )
		self.focusWidget = None
		self._queue = _ViewQueue( self.view )
		
		self._indentationStyleSheets = {}
		
		self._nodeContentsFactories = {}
		
		
		
	def _indentationStyleSheet(self, indentation):
		try:
			return self._indentationStyleSheets[indentation]
		except KeyError:
			styleSheet = BorderStyleSheet( indentation, 0.0, 0.0, 0.0 )
			self._indentationStyleSheets[indentation] = styleSheet
			return styleSheet
	
	
		
	def _f_makeNodeFactory(self, nodeViewFunction, state):
		def _nodeFactory(view, treeNode):
			# Build a DVNode for the document subtree at @docNode
			# self._p_buildNodeContents is a factory that builds the contents withing the DVNode
			node = DVNode( view, treeNode )
			node._f_setContentsFactory( self._f_makeNodeContentsFactory( nodeViewFunction, state ) )
			return node
		return _nodeFactory
	

	def _p_rootNodeFactory(self, view, treeNode):
		# Build a DVNode for the document subtree at @docNode
		# self._p_buildNodeContents is a factory that builds the contents withing the DVNode
		node = DVNode( view, treeNode )
		node._f_setContentsFactory( self._f_makeNodeContentsFactory( None, None ) )
		return node
	


	def _f_makeNodeContentsFactory(self, nodeViewFunction, state):
		def _buildNodeContents(viewNode, treeNode):
			# Create the node view instance
			nodeViewInstance = _GSymNodeViewInstance( treeNode.node, self.view, self, viewNode )
			## HACK ##
			# Build the contents
			viewContents = self._p_buildNodeViewContents( nodeViewInstance, treeNode, nodeViewFunction, state )
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
		
		#2. Create the view contents
		viewContents = nodeViewFunction( content, nodeViewInstance, state )
		
		#3. Pop @self from the view instance's view node instance stack
		self.viewNodeInstanceStack.pop()
		
		#4. Return the view contents
		return viewContents
	
	
	
	
	def _p_queueRefresh(self):
		def _refresh():
			self.view.refresh()
		self._queue.queue( _refresh )
		
		
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

	

	def _sendDocEvent(self, widget, event):
		bHandled = self._p_sendDocEventToWidget( widget, event )
		if not bHandled:
			print 'gSymView._sendDocEvent: ***unhandled event*** %s'  %  ( event, )
		return bHandled
	


			
class GSymView (object):
	def __call__(self, xs, ctx, state):
		return dispatch( self, xs, ctx, state )
	
		
		
		
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
		
		
	def createViewFunction(self, viewNodeInstanceStack):
		return self.viewClass()
		
		
	def createDocumentView(self, xs, commandHistory):
		tree = DocTree()
		txs = tree.treeNode( xs )
		viewInstance = _GSymViewInstance( tree, txs, self, commandHistory )
		return viewInstance.view


	


