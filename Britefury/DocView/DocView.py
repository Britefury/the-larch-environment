##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Cell.Cell import Cell

from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.DocView.SelectPath import SelectPath




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

		self.rootView = self._f_buildView( self._root, None, 0 )



	def setDocument(self, document):
		self._document = document


	def documentUngrab(self):
		self._document.removeFocusGrab()



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

			viewNode = nodeClass( docNode, self, parentDocNode, indexInParent )
			viewNode.refresh()

			return viewNode






	def _p_refresh(self):
		self.rootView.refresh()


	def refresh(self):
		self.refreshCell.immutableValue
		#self.rootView.refresh()



	def _f_getStyleSheet(self, docNode, parentDocNode, indexInParent):
		return self._styleSheetDispatcher.getStyleSheetForNode( docNode, parentDocNode, indexInParent )




	def _p_handleSelectPath(self, nodeView, selectPath, parentDocNode, indexInParent):
		if selectPath is None:
			if indexInParent < len( parentDocNode ):
				selectPath = SelectPath( parentDocNode, [ parentDocNode[indexInParent] ] )
			else:
				selectPath = SelectPath( parentDocNode, [] )

		nodeView.getDocView()

		# Travel to the ancestor
		while nodeView.docNode is not selectPath.ancestor:
			nodeView = nodeView.getParentNodeView()
			if nodeView is None:
				raise ValueError, 'reached root while following select path'

		# Trigger a rebuild
		self.refresh()

		for child in selectPath.children:
			try:
				nodeView.refresh()
				nodeView = nodeView.getChildViewNodeForChildDocNode( child )
			except KeyError:
				raise ValueError, 'could not follow select path children, nodeView=%s, docNode=%s, docNode[:]=%s, child=%s'  %  ( nodeView, nodeView.docNode, nodeView.docNode[:], child )

		nodeView.makeCurrent()

		return nodeView


	def _f_handleTokenList(self, nodeView, tokens, parentDocNode, indexInParent, parentStyleSheet, bDirectEvent):
		if len( tokens ) == 0:
			selectPath = nodeView._styleSheet._f_handleEmpty( nodeView, parentDocNode, indexInParent, parentStyleSheet, bDirectEvent )
			self._p_handleSelectPath( nodeView, selectPath, parentDocNode, indexInParent )
		elif len( tokens ) == 1  and  bDirectEvent:
			selectPath = nodeView._styleSheet._f_handleToken( nodeView, tokens[0], parentDocNode, indexInParent, parentStyleSheet, True )
			self._p_handleSelectPath( nodeView, selectPath, parentDocNode, indexInParent )
		else:
			for token in tokens:
				selectPath = nodeView._styleSheet._f_handleToken( nodeView, token, parentDocNode, indexInParent, parentStyleSheet, False )
				nodeView = self._p_handleSelectPath( nodeView, selectPath, parentDocNode, indexInParent )




	def _f_commandHistoryFreeze(self):
		if self._commandHistory is not None:
			self._commandHistory.freeze()


	def _f_commandHistoryThaw(self):
		if self._commandHistory is not None:
			self._commandHistory.thaw()

