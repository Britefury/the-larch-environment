##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Cell.Cell import Cell

from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.DocView.DocViewNodeTable import DocViewNodeTable, DocNodeKey




class DocView (object):
	_nodeClassTable = {}

	def __init__(self, root, commandHistory, styleSheetDispatcher):
		super( DocView, self ).__init__()

		self._root = root

		self._document = None
		self._commandHistory = commandHistory

		self._styleSheetDispatcher = styleSheetDispatcher

		self.refreshCell = Cell()
		self.refreshCell.function = self._p_refresh

		self._nodeTable = DocViewNodeTable()

		self.rootView = self._f_buildView( self._root, None, 0 )




	def setDocument(self, document):
		self._document = document


	def documentUngrab(self):
		self._document.removeFocusGrab()



	def _f_nodeChangeKey(self, viewNode, oldKey, newKey):
		try:
			del self._nodeTable[oldKey]
		except KeyError:
			pass
		self._nodeTable[newKey] = viewNode


	def _f_buildView(self, docNode, parentViewNode, indexInParent):
		if docNode is None:
			return None
		else:
			docNodeClass = docNode.__class__

			try:
				nodeClass = self._nodeClassTable[docNodeClass]
			except KeyError:
				raise TypeError, 'could not get view node class for doc node class %s'  %  ( docNodeClass.__name__, )

			parentDocNode = None
			if parentViewNode is not None:
				parentDocNode = parentViewNode.docNode

			key = DocNodeKey( docNode, parentDocNode, indexInParent )

			try:
				viewNode = self._nodeTable[key]
			except KeyError:
				viewNode = nodeClass( docNode, self, key )
				self._nodeTable[key] = viewNode

			viewNode.refresh()

			return viewNode


	def getViewNodeForDocNodeKey(self, key):
		return self._nodeTable[key]







	def _p_refresh(self):
		self.rootView.refresh()


	def refresh(self):
		self.refreshCell.immutableValue
		#self.rootView.refresh()



	def _f_getStyleSheet(self, key):
		return self._styleSheetDispatcher.getStyleSheetForNode( key )




	def _f_handleSelectNode(self, nodeView, selectNodeKey):
		# Trigger a rebuild
		self.refresh()

		# Get the view node
		nodeView = self.getViewNodeForDocNodeKey( selectNodeKey )

		nodeView.makeCurrent()

		return nodeView



	def _f_handleTokenList(self, nodeView, tokens, key, parentStyleSheet, bDirectEvent):
		if len( tokens ) == 0:
			selectNodeKey = nodeView._styleSheet._f_handleEmpty( nodeView, key, parentStyleSheet, bDirectEvent )
			self._f_handleSelectNode( nodeView, selectNodeKey )
		elif len( tokens ) == 1  and  bDirectEvent:
			selectNodeKey = nodeView._styleSheet._f_handleToken( nodeView, tokens[0], key, parentStyleSheet, True )
			self._f_handleSelectNode( nodeView, selectNodeKey )
		else:
			for token in tokens:
				selectNodeKey = nodeView._styleSheet._f_handleToken( nodeView, token, key, parentStyleSheet, False )
				nodeView = self._f_handleSelectNode( nodeView, selectNodeKey )




	def _f_commandHistoryFreeze(self):
		if self._commandHistory is not None:
			self._commandHistory.freeze()


	def _f_commandHistoryThaw(self):
		if self._commandHistory is not None:
			self._commandHistory.thaw()

