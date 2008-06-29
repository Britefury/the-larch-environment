##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Cell.Cell import Cell

from Britefury.DocModel.DMListInterface import DMListInterface
from Britefury.DocModel.RelativeNode import RelativeNode, relative

from Britefury.DocView.DocViewNodeTable import DocViewNodeTable, DocNodeKey




class DocView (object):
	def __init__(self, root, commandHistory, nodeFactory):
		super( DocView, self ).__init__()
		
		assert isinstance( root, RelativeNode )

		self._root = root

		self._document = None
		self._commandHistory = commandHistory

		self._nodeFactory = nodeFactory

		self.refreshCell = Cell()
		self.refreshCell.function = self._p_refresh

		self._nodeTable = DocViewNodeTable()
		
		self._rootView = None

		
	def _p_getRootView(self):
		if self._rootView is None:
			self._rootView = self.buildNodeView( self._root )
		return self._rootView




	def getDocument(self):
		return self._document
	
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


	def buildNodeView(self, relativeNode, nodeFactory=None):
		assert isinstance( relativeNode, RelativeNode )
		
		if relativeNode is None:
			return None
		else:
			docNodeKey = DocNodeKey.fromRelativeNode( relativeNode )

			if nodeFactory is None:
				nodeFactory = self._nodeFactory
			
			try:
				viewNode = self._nodeTable[docNodeKey]
			except KeyError:
				viewNode = nodeFactory( relativeNode.node, self, docNodeKey )
				self._nodeTable[docNodeKey] = viewNode

			viewNode.refresh()

			return viewNode


	def getViewNodeForDocNodeKey(self, docNodeKey):
		return self._nodeTable[docNodeKey]




	def refreshAndGetViewNodeForDocNodeKey(self, docNodeKey):
		self.refresh()
		return self._nodeTable[docNodeKey]





	def _p_refresh(self):
		self.rootView.refresh()


	def refresh(self):
		self.refreshCell.immutableValue
		#self.rootView.refresh()




	def _f_handleSelectNode(self, nodeView, selectNodeKey):
		# Trigger a rebuild
		self.refresh()

		# Get the view node
		nodeView = self.getViewNodeForDocNodeKey( selectNodeKey )

		nodeView.makeCurrent()

		return nodeView



	def _f_handleTokenList(self, nodeView, docNodeKey, tokens, parentStyleSheet, bDirectEvent):
		if len( tokens ) == 0:
			nodeView._f_handleEmpty( [], bDirectEvent )
		elif len( tokens ) == 1  and  bDirectEvent:
			nodeView._f_handleToken( [], tokens[0], 0, 1, True )
		else:
			numTokens = len( tokens )
			for i, token in enumerate( tokens ):
				nodeView = nodeView._f_handleToken( [], token, i, numTokens, False )
				if nodeView is None:
					break




	def _f_commandHistoryFreeze(self):
		if self._commandHistory is not None:
			self._commandHistory.freeze()


	def _f_commandHistoryThaw(self):
		if self._commandHistory is not None:
			self._commandHistory.thaw()
			
			
	rootView = property( _p_getRootView )
	document = property( getDocument, setDocument )

