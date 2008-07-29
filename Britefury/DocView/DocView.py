##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Cell.Cell import Cell

from Britefury.DocModel.DMListInterface import DMListInterface
from Britefury.DocTree.DocTreeNode import DocTreeNode

from Britefury.DocView.DocViewNodeTable import DocViewNodeTable

from Britefury.DocPresent.Web.Context.WebViewContext import WebViewContext
from Britefury.DocPresent.Web.Page import Page



class DocViewPage (Page):
	def __init__(self, title, docView):
		super( DocViewPage, self ).__init__( title )
		self._docView = docView
		
	
	def _htmlBody(self):
		html, resolvedRefNodes, placeHolderIDs = self._docView.rootView.getHtmlNode().resolvedSubtreeHtmlForClient()
		return html



class DocView (object):
	def __init__(self, owner, tree, root, commandHistory, nodeFactory):
		super( DocView, self ).__init__()
		
		assert isinstance( root, DocTreeNode )
		
		self.owner = owner

		# Tree and tree root node
		self._tree = tree
		self._root = root

		# Command history
		self._commandHistory = commandHistory

		# Factory to make nodes
		self._defaultNodeFactory = nodeFactory

		# Refresh cell
		self.refreshCell = Cell()
		self.refreshCell.function = self._refresh

		# Node to DocViewNode table
		self._nodeTable = DocViewNodeTable()
		
		# View of root node
		self._rootView = None
		
		# Create the page, and the view context
		self.page = DocViewPage( 'gSym test', self )
		self.viewContext = WebViewContext( owner, self.page )

		
	
	def _getRootView(self):
		"""
		Get the view of the root node
		(creates it if it does not exist yet)
		"""
		if self._rootView is None:
			self._rootView = self.buildNodeView( self._root )
		return self._rootView




	def buildNodeView(self, treeNode, nodeFactory=None):
		"""
		Build a view node for a document node (obtained through a tree node)
		"""
		assert isinstance( treeNode, DocTreeNode )
		
		if treeNode is None:
			# No document node; no view node
			return None
		else:
			# If no node factory has been specified, use the default one
			if nodeFactory is None:
				nodeFactory = self._defaultNodeFactory
			
			# See if we already have a view node for @treeNode
			try:
				viewNode = self._nodeTable[treeNode]
			except KeyError:
				# We don't have a view node
				try:
					# Try to re-use a now unused view node
					viewNode = self._nodeTable.takeUnusedViewNodeFor( treeNode )
				except KeyError:
					# Could not find anything.....
					# Create one
					viewNode = nodeFactory( self, treeNode )
					# Add to the node table
					self._nodeTable[treeNode] = viewNode

			# Refresh the node
			viewNode.refresh()

			return viewNode

		
		

	def getViewNodeForDocTreeNode(self, treeNode):
		"""
		Get the view node for a document/tree node (if there is one)
		"""
		return self._nodeTable[treeNode]



	def refreshAndGetViewNodeForDocTreeNode(self, treeNode):
		"""
		Refresh the whole documentm then get the view node for a document/tree node (if there is one)
		"""
		self.refresh()
		return self._nodeTable[treeNode]





	def _refresh(self):
		"""
		Refresh the whole document
		"""
		# Refresh the root view
		self.rootView.refresh()


	def refresh(self):
		"""
		Refresh the whole document
		"""
		# Go through the refresh cell
		self.refreshCell.immutableValue




	def _commandHistoryFreeze(self):
		"""
		Freeze the command history
		"""
		if self._commandHistory is not None:
			self._commandHistory.freeze()


	def _commandHistoryThaw(self):
		"""
		Thaw the command history
		"""
		if self._commandHistory is not None:
			self._commandHistory.thaw()
			
			
			
	def _queueNodeRefresh(self, viewNode):
		assert False, 'not implemented'
			
			
	rootView = property( _getRootView )

