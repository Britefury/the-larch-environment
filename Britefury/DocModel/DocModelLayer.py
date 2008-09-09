##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from weakref import WeakKeyDictionary

from BritefuryJ.Cell import Cell

import Britefury.DocModel.DMVirtualList



class DocModelLayer (object):
	def __init__(self, layerOpFunctionGenerator=lambda x, y: lambda x, y: copy( x ), layerOpInvFunctionGenerator=lambda x, y: lambda x, y: copy( x )):
		"""
		layerFunctionGenerator signature:    layerOpFunctionGenerator(sourceList, destLayer)  ->  layerOpFunction(sourceList, destLayer)  ->  destList
		layerOpInvFunctionGenerator signature:    layerOpInvFunctionGenerator(destList, destLayer)  ->  layerOpInvFunction(destList, destLayer)  ->  sourceList
		"""
		self._layerOpFunctionGenerator = layerOpFunctionGenerator
		self._layerOpInvFunctionGenerator = layerOpInvFunctionGenerator


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
					destList = Britefury.DocModel.DMVirtualList.DMVirtualList( layerOpFunction( srcList, self ) )
					layerOpFunctionToDestList[layerOpFunction] = destList

				return destList


			destCell = Cell()
			destCell.setFunction( _cellFunc )

			self._srcListToDestCell[srcList] = destCell


		destList = destCell.getValue()
		self._destListToSrcList[destList] = srcList

		return destList



	def getSrcList(self, destList):
		try:
			return self._destListToSrcList[destList]
		except KeyError:
			layerOpInvFunction = self._layerOpInvFunctionGenerator( destList, self )
			return layerOpFunction( destList, self )



