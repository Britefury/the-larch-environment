##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.Cell import Cell

from Britefury.DocModel.DMListInterface import DMListInterface
from Britefury.DocTree.DocTreeNode import DocTreeNode

from Britefury.DocView.DocViewNodeTable import DocViewNodeTable
from Britefury.DocView.DVNode import DVNode




class DocView (object):
	def __init__(self, tree, root, commandHistory, rootNodeInitialiser):
		super( DocView, self ).__init__()
		
		assert isinstance( root, DocTreeNode )

		self._tree = tree
		self._root = root

		self._document = None
		self._commandHistory = commandHistory

		self._rootNodeInitialiser = rootNodeInitialiser

		self.refreshCell = Cell()
		self.refreshCell.setFunction( self._p_refresh )

		self._nodeTable = DocViewNodeTable()
		
		self._rootView = None
		
		self._caretNode = None

		
	def getRootView(self):
		if self._rootView is None:
			self._rootView = self.buildNodeView( self._root )
			self._rootNodeInitialiser( self._rootView, self._root )
		return self._rootView




	def getDocument(self):
		return self._document
	
	def setDocument(self, document):
		self._document = document


	def documentUngrab(self):
		self._document.removeFocusGrab()



	def buildNodeView(self, treeNode):
		assert isinstance( treeNode, DocTreeNode )
		
		if treeNode is None:
			return None
		else:
			try:
				viewNode = self._nodeTable[treeNode]
			except KeyError:
				try:
					viewNode = self._nodeTable.takeUnusedViewNodeFor( treeNode )
				except KeyError:
					viewNode = DVNode( self, treeNode )
					self._nodeTable[treeNode] = viewNode
					
			return viewNode


	def getViewNodeForDocTreeNode(self, treeNode):
		return self._nodeTable[treeNode]



	def refreshAndGetViewNodeForDocTreeNode(self, treeNode):
		self.refresh()
		return self._nodeTable[treeNode]





	def _p_refresh(self):
		self.getRootView().refresh()


	def refresh(self):
		self.refreshCell.getValue()




	def _f_commandHistoryFreeze(self):
		if self._commandHistory is not None:
			self._commandHistory.freeze()


	def _f_commandHistoryThaw(self):
		if self._commandHistory is not None:
			self._commandHistory.thaw()
			
			
	document = property( getDocument, setDocument )

