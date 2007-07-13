##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from weakref import WeakKeyDictionary

from Britefury.Cell.Cell import Cell

from Britefury.Sheet.Sheet import *



class CodeView (object):
	_nodeClassTable = {}

	def __init__(self, tree, commandHistory):
		super( CodeView, self ).__init__()

		self._nodeTable = WeakKeyDictionary()
		self._tree = tree
		self._document = None
		self._commandHistory = commandHistory

		self.refreshCell = Cell()
		self.refreshCell.function = self._p_refresh



	def setDocument(self, document):
		self._document = document


	def documentUngrab(self):
		self._document.removeFocusGrab()



	def buildView(self, treeNode, parentNode, nodeClass=None):
		if treeNode is None:
			return None
		else:
			treeNodeClass = treeNode.__class__
			bNodeTableChanged = False

			if nodeClass is None:
				try:
					nodeClass = self._nodeClassTable[treeNodeClass]
				except KeyError:
					raise TypeError, 'could not get view node class for tree node class %s'  %  ( treeNodeClass.__name__, )

			try:
				subTable = self._nodeTable[treeNode]
			except KeyError:
				subTable = {}
				self._nodeTable[treeNode] = subTable
				bNodeTableChanged = True

			try:
				viewNode = subTable[nodeClass]
			except KeyError:
				viewNode = nodeClass( treeNode, self )
				subTable[nodeClass] = viewNode
				bNodeTableChanged = True


			viewNode._parent = parentNode

			viewNode.refresh()

			if bNodeTableChanged:
				self._p_onNodeTableChanged()

			return viewNode


	def getViewNodeForTreeNode(self, treeNode, nodeClass=None):
		treeNodeClass = treeNode.__class__
		if nodeClass is None:
			try:
				nodeClass = self._nodeClassTable[treeNodeClass]
			except KeyError:
				raise TypeError, 'could not get view node class for tree node class %s'  %  ( treeNodeClass.__name__, )

		try:
			subTable = self._nodeTable[treeNode]
		except KeyError:
			return None

		try:
			return subTable[nodeClass]
		except KeyError:
			return None



	def _p_onNodeTableChanged(self):
		self.refreshCell.function = self._p_refresh



	def _p_refresh(self):
		self.buildView( self._tree.getRootNode(), None )


	def refresh(self):
		self.refreshCell.immutableValue



	def _f_commandHistoryFreeze(self):
		if self._commandHistory is not None:
			self._commandHistory.freeze()


	def _f_commandHistoryThaw(self):
		if self._commandHistory is not None:
			self._commandHistory.thaw()





class CodeViewSettings (Sheet):
	bRenderTuplesUsingParens = Field( bool, False )
	bShowDoc = Field( bool, True )
	bShowCode = Field( bool, True )



codeViewSettings = CodeViewSettings()