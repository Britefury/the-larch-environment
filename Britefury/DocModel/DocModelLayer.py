##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from weakref import WeakKeyDictionary

from Britefury.Cell.Cell import RefCell



class DocModelLayer (object):
	def __init__(self, layerFunction=lambda x, y: copy( x )):
		"""layerFunction signature:    layerFunction(sourceList, destLayer)"""
		self._layerFunction = layerFunction

		self._srcListToDestCell = WeakKeyDictionary()
		self._destListToSrcList = WeakKeyDictionary()



	def getDestList(self, srcList):
		try:
			destCell = self._srcListToDestCell[srcList]
		except KeyError:
			def _cellFunc():
				return self._layerFunction( srcList, self )

			destCell = RefCell()
			destCell.function = _cellFunc

			self._srcListToDestCell[srcList] = destCell

		destList = destCell.getValue()
		self._destListToSrcList[destList] = srcList

		return destList



	def getSrcList(self, destList):
		return self._destListToSrcList[destList]







