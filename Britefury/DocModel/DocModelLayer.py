##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from weakref import WeakKeyDictionary

from Britefury.Cell.Cell import RefCell

import Britefury.DocModel.DMList



class DocModelLayer (object):
	def __init__(self, layerOpFunctionGenerator=lambda x, y: lambda x, y: copy( x )):
		"""layerFunctionGenerator signature:    layerOpFunctionGenerator(sourceList, destLayer)  ->  layerOpFunction(sourceList, destLayer)"""
		self._layerOpFunctionGenerator = layerOpFunctionGenerator

		self._srcListToDestCell = WeakKeyDictionary()
		self._srcListToLayerOpFunctionToDestList = WeakKeyDictionary()
		self._destListToSrcList = WeakKeyDictionary()



	def getDestList(self, srcList):
		try:
			destCell = self._srcListToDestCell[srcList]
		except KeyError:
			def _cellFunc():
				layerOpFunction = self._layerOpFunctionGenerator( srcList, self )

				try:
					layerOpFunctionToDestList = self._srcListToLayerOpFunctionToDestList[srcList]
				except KeyError:
					layerOpFunctionToDestList = WeakKeyDictionary()
					self._srcListToLayerOpFunctionToDestList[srcList] = layerOpFunctionToDestList

				try:
					destList = layerOpFunctionToDestList[layerOpFunction]
				except KeyError:
					destList = Britefury.DocModel.DMList.DMList( self, layerOpFunction( srcList, self ) )
					layerOpFunctionToDestList[layerOpFunction] = destList

				return destList


			destCell = RefCell()
			destCell.function = _cellFunc

			self._srcListToDestCell[srcList] = destCell


		destList = destCell.getValue()
		self._destListToSrcList[destList] = srcList

		return destList



	def getSrcList(self, destList):
		return self._destListToSrcList[destList]



