##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from weakref import WeakKeyDictionary

from Britefury.Cell.Cell import Cell

from Britefury.DocModel.DMListInterface import DMListInterface
from Britefury.DocModel.DMSymbol import DMSymbol

from Britefury.DocView.StyleSheet.DVBorderStyleSheet import DVBorderStyleSheet
from Britefury.DocView.StyleSheet.DVSymbolStyleSheet import DVSymbolStyleSheet
from Britefury.DocView.StyleSheet.DVListSExpressionStyleSheet import DVListSExpressionStyleSheet



class DocView (object):
	_nodeClassTable = {}

	def __init__(self, root, commandHistory):
		super( DocView, self ).__init__()

		self._root = root

		self._document = None
		self._commandHistory = commandHistory

		self.refreshCell = Cell()
		self.refreshCell.function = self._p_refresh

		self.rootView = self.buildView( self._root, None, 0 )



	def setDocument(self, document):
		self._document = document


	def documentUngrab(self):
		self._document.removeFocusGrab()



	def buildView(self, docNode, parentNode, indexInParent):
		if docNode is None:
			return None
		else:
			docNodeClass = docNode.__class__
			bNodeTableChanged = False

			try:
				nodeClass = self._nodeClassTable[docNodeClass]
			except KeyError:
				raise TypeError, 'could not get view node class for doc node class %s'  %  ( docNodeClass.__name__, )

			parentDocNode = None
			if parentNode is not None:
				parentDocNode = parentNode.docNode

			viewNode = nodeClass( docNode, self, parentDocNode, indexInParent )
			viewNode.refresh()

			return viewNode





	def _p_refresh(self):
		self.rootView.refresh()


	def refresh(self):
		self.refreshCell.immutableValue
		#self.rootView.refresh()



	def _f_getStyleSheet(self, docNode, parentStyleSheet, indexInParent):
		if isinstance( docNode, DMListInterface ):
			return DVListSExpressionStyleSheet.singleton
		elif isinstance( docNode, str ):
			return DVStringStyleSheet.singleton
		else:
			return DVBorderStyleSheet.singleton




	def _f_commandHistoryFreeze(self):
		if self._commandHistory is not None:
			self._commandHistory.freeze()


	def _f_commandHistoryThaw(self):
		if self._commandHistory is not None:
			self._commandHistory.thaw()

