##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import sys

from copy import copy

from Britefury.DocPresent.Web.WDNode import WDNode
from Britefury.DocPresent.Web.Context.WebViewContext import WebViewContext
from Britefury.DocPresent.Web.Context.WebViewNodeContext import WebViewNodeContext

from Britefury.DocView.DVNode import DVNode
from Britefury.DocView.DocView import DocView


from Britefury.GLisp.GLispUtil import isGLispList, gLispSrcToString

from Britefury.GLisp.GLispDispatch import dispatch

from Britefury.gSym.View import ListView
from Britefury.gSym.View.UnparsedText import UnparsedText

from Britefury.DocTree.DocTree import DocTree
from Britefury.DocTree.DocTreeNode import DocTreeNode


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
				



def _registerViewNodeRelationship(viewNode, childNode):
	viewNode._registerChild( childNode )



def _setKeyHandler(viewNodeInstance, widget):
	widget.keyHandler = viewNodeInstance.viewInstance._handleKeyPress


	
	
	
				
def listView(nodeContext, layout, beginDelim, endDelim, separatorFactory, children, style=None):
	"""
	Runtime - called by compiled code at run-time
	Builds a list view.
	@layout controls the layout
	"""
	widget, refreshCell = ListView.listView( viewNodeInstance.xs, layout, beginDelim, endDelim, separatorFactory, children )
	viewNodeInstance.refreshCells.append( refreshCell )
	_applyStyleSheetStack( viewNodeInstance, widget )
	_applyStyle( style, widget )
	def _registerRelationships():
		for ch in children:
			if isinstance( ch, DVNode ):
				_registerViewNodeRelationship( viewNodeInstance, ch )
	_buildRefreshCellAndRegister( viewNodeInstance, _registerRelationships )
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
				if nodeToSelect is not None:
					self.viewInstance._queueRefreshAndSelect( nodeToSelect )
				if ev is not event:
					return ev
		return event
		


def viewEval(nodeContext, content, nodeViewFunction=None, state=None):
	"""Build a view for a document subtree (@content)"""

	if not isinstance( content, DocTreeNode ):
		raise TypeError, 'buildView: content is not a DocTreeNode'
	
	viewContext = nodeContext.viewContext
	viewInstance = viewContext.owner
	
	# Get a node factory
	nodeFactory = viewInstance._makeNodeFactory( nodeViewFunction, state )
	# A call to DocNode.buildNodeView builds the view, and puts it in the DocView's table
	viewNode = viewInstance.view.buildNodeView( content, nodeFactory )
	viewNode.setContentsFactory( viewInstance._makeNodeContentsFactory( nodeViewFunction, state ) )
	viewNode.refresh()
	
	_registerViewNodeRelationship( nodeContext.owner, viewNode )
		
	return viewNode


def mapViewEval(nodeContext, content, nodeViewFunction=None, state=None):
	return [ viewEval( nodeContext, x, nodeViewFunction, state )   for x in content ]





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
		self.generalNodeViewFunction = viewFactory._createViewFunction()
		# self._p_buildDVNode is a factory that builds DVNode instances for document subtrees
		self.view = DocView( self, self.tree, self.xs, commandHistory, self._rootNodeFactory )
		self.focusWidget = None
		self._queue = _ViewQueue( self.view )
		
		self._nodeContentsFactories = {}
		
	
	def _makeNodeFactory(self, nodeViewFunction, state):
		def _nodeFactory(view, treeNode):
			# Build a DVNode for the document subtree at @docNode
			# self._p_buildNodeContents is a factory that builds the contents withing the DVNode
			node = DVNode( view, treeNode )
			node.setContentsFactory( self._makeNodeContentsFactory( nodeViewFunction, state ) )
			return node
		return _nodeFactory
	

	def _rootNodeFactory(self, view, treeNode):
		# Build a DVNode for the document subtree at @docNode
		# self._p_buildNodeContents is a factory that builds the contents withing the DVNode
		node = DVNode( view, treeNode )
		node.setContentsFactory( self._makeNodeContentsFactory( None, None ) )
		return node
	


	def _makeNodeContentsFactory(self, nodeViewFunction, state):
		def _buildNodeContents(viewNode, treeNode, nodeContext):
			# Create the node view instance
			nodeViewInstance = _GSymNodeViewInstance( treeNode.node, self.view, self, viewNode )
			## HACK ##
			# Build the contents
			viewContents = self._buildNodeViewContents( nodeContext, nodeViewInstance, treeNode, nodeViewFunction, state )
			# Get the refresh cells that need to be monitored, and hand them to the DVNode
			# HACK viewNode._f_setRefreshCells( nodeViewInstance.refreshCells )
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
		
	
	def _buildNodeViewContents(self, nodeContext, nodeViewInstance, content, nodeViewFunction, state):
		"""Runtime - build the contents of a view node"""
		if nodeViewFunction is None:
			nodeViewFunction = self.generalNodeViewFunction

		viewContents = nodeViewFunction( content, nodeContext, state )
		
		return viewContents
	
	
	
	
	def _queueRefresh(self):
		def _refresh():
			self.view.refresh()
		self._queue.queue( _refresh )
		
		
	def _queueSelect(self, node):
		def _focus():
			assert isinstance( node, DocTreeNode ), 'Could not select a node of type %s'  %  ( type( node ), )
			try:
				viewNode = self.view.getViewNodeForDocTreeNode( node )
			except KeyError:
				print 'gSymView:_GSymViewInstance._queueSelect(): Could not get widget to focus on', node.node
				self.focusWidget = None
			else:
				self.focusWidget = viewNode.focus
		self._queue.queue( _focus )
		
		
		def _select():
			if self.focusWidget is not None:
				#self.focusWidget.makeCurrent()
				self.focusWidget.startEditing()
		self._queue.final = _select
		
		
			
	def _queueRefreshAndSelect(self, node):
		self._queueRefresh()
		self._queueSelect( node )
		
	
	def _queueSendEventToFocus(self, event):
		def _send():
			if self.focusWidget is not None:
				self._sendDocEventToWidget( self.focusWidget, event )
		self._queue.queue( _send )
		
		
	def _sendDocEventToWidget(self, widget, event):
		self.focusWidget = widget
		processedEvent = widget.sendDocEvent( event )
		if processedEvent is None:
			return True
		elif processedEvent is event:
			return False
		else:
			self._queueSendEventToFocus( processedEvent )
			return True
	
	
	def _handleKeyPress(self, widget, keyPressEvent):
		event = InteractorEventKey.fromDTKeyEvent( widget, True, keyPressEvent )
		return self._sendDocEventToWidget( widget, event )

	

	def _sendDocEvent(self, widget, event):
		bHandled = self._sendDocEventToWidget( widget, event )
		if not bHandled:
			print 'gSymView._sendDocEvent: ***unhandled event*** %s'  %  ( event, )
		return bHandled
	
	def _p_onEntryModifed(self, widget, text):
		pass
	
	def _p_onEntryFinished(self, widget, text, bChanged, bUserEvent):
		self._sendDocEvent( widget, InteractorEventText( bUserEvent, bChanged, text ) )

	def _p_onEntryBackspaceStart(self, widget):
		self._sendDocEvent( widget, InteractorEventBackspaceStart() )
	
	def _p_onEntryDeleteEnd(self, widget):
		self._sendDocEvent( widget, InteractorEventDeleteEnd() )
	


			
class GSymView (object):
	def __call__(self, xs, nodeContext, state):
		return dispatch( self, xs, nodeContext, state )
	
		
		
		
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
		
		
	def _createViewFunction(self):
		return self.viewClass()
		
		
	def createDocumentView(self, xs, commandHistory):
		tree = DocTree()
		txs = tree.treeNode( xs )
		viewInstance = _GSymViewInstance( tree, txs, self, commandHistory )
		return viewInstance.view


	


