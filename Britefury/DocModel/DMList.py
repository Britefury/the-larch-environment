##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from weakref import WeakKeyDictionary
from copy import copy

from Britefury.Cell.Cell import RefCell

from Britefury.DocModel.DMListOperator import DMListOpMap, DMListOpSlice, DMListOpWrap, DMListOpNop






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







class DMList (object):
	def __init__(self, layer, op=None):
		self._op = op
		self._cell = RefCell()
		self._layer = layer

		if op is None:
			self._cell.literalValue = []
		else:
			self._cell.function = op.evaluate


	def append(self, x):
		if self._op is None:
			v = self._cell.literalValue
			v.append( x )
			self._cell.literalValue = v
		else:
			self._op.append( x )

	def extend(self, xs):
		if self._op is None:
			v = self._cell.literalValue
			v.extend( xs )
			self._cell.literalValue = v
		else:
			self._op.extend( xs )

	def insertBefore(self, before, x):
		if self._op is None:
			v = self._cell.literalValue
			i = v.index( before )
			v.insert( i, x )
			self._cell.literalValue = v
		else:
			self._op.insertBefore( before, x )

	def insertAfter(self, after, x):
		if self._op is None:
			v = self._cell.literalValue
			i = v.index( after )
			v.insert( i + 1, x )
			self._cell.literalValue = v
		else:
			self._op.insertAfter( after, x )

	def remove(self, x):
		if self._op is None:
			v = self._cell.literalValue
			v.remove( x )
			self._cell.literalValue = v
		else:
			self._op.remove( x )

	def __setitem__(self, i, x):
		if self._op is None:
			v = self._cell.literalValue
			v[i] = x
			self._cell.literalValue = v
		else:
			self._op[i] = x


	def __getitem__(self, i):
		return self._cell.value[i]

	def __contains__(self, x):
		return x in self._cell.value

	def __iter__(self):
		return iter( self._cell.value )

	def __add__(self, xs):
		return self._cell.value + xs

	def __len__(self):
		return len( self._cell.value )

	def index(self, x):
		return self._cell.value.index( x )


	def getLayer(self):
		return self._layer


	def getDestList(self, layer):
		return layer.getDestList( self )

	def getSrcList(self, layer):
		return layer.getSrcList( self )



	def __copy__(self):
		c = DMList( self._layer, self._op )
		if self._op is None:
			c[:] = self[:]
		return c





import unittest



class TestCase_List (unittest.TestCase):
	def testListCtor(self):
		layer = DocModelLayer()
		x = DMList( layer )



	def testLiteralIter(self):
		layer = DocModelLayer()
		x = DMList( layer )
		x.extend( [ 1, 2, 3 ] )
		q = [ p   for p in x ]
		self.assert_( q == [ 1, 2, 3 ] )


	def testLiteralAppend(self):
		layer = DocModelLayer()
		x = DMList( layer )
		x.append( 1 )
		self.assert_( x[0] == 1 )


	def testLiteralExtend(self):
		layer = DocModelLayer()
		x = DMList( layer )
		x.extend( [ 1, 2, 3 ] )
		self.assert_( x[0] == 1 )
		self.assert_( x[1] == 2 )
		self.assert_( x[2] == 3 )
		self.assert_( x[:] == [ 1, 2, 3 ] )


	def testLiteralInsertBefore(self):
		layer = DocModelLayer()
		x = DMList( layer )
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.insertBefore( 3, 12 )
		self.assert_( x[:] == [ 1, 2, 12, 3, 4, 5 ] )


	def testLiteralInsertAfter(self):
		layer = DocModelLayer()
		x = DMList( layer )
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.insertAfter( 3, 12 )
		self.assert_( x[:] == [ 1, 2, 3, 12, 4, 5 ] )


	def testLiteralRemove(self):
		layer = DocModelLayer()
		x = DMList( layer )
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.remove( 3 )
		self.assert_( x[:] == [ 1, 2, 4, 5 ] )


	def testLiteralSet(self):
		layer = DocModelLayer()
		x = DMList( layer )
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x[4] = 12
		self.assert_( x[:] == [ 1, 2, 3, 4, 12 ] )
		x[1:3] = [ 20, 21, 22 ]
		self.assert_( x[:] == [ 1, 20, 21, 22, 4, 12 ] )


	def testOpMap(self):
		layer1 = DocModelLayer()
		layer2 = DocModelLayer()
		x = DMList( layer1 )
		x.extend( [ 1, 2, 3 ] )

		y = DMList( layer2, DMListOpMap( layer2, x, lambda x: x * 10, lambda x: x / 10 ) )
		self.assert_( y[0] == 10 )
		self.assert_( y[:] == [ 10, 20, 30 ] )
		y.append( 40 )
		self.assert_( y[:] == [ 10, 20, 30, 40 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4 ] )
		y[1:3] = [ 100, 200, 300, 400 ]
		self.assert_( y[:] == [ 10, 100, 200, 300, 400, 40 ] )
		self.assert_( x[:] == [ 1, 10, 20, 30, 40, 4 ] )
		y.extend( [ 600, 700 ] )
		self.assert_( y[:] == [ 10, 100, 200, 300, 400, 40, 600, 700 ] )
		self.assert_( x[:] == [ 1, 10, 20, 30, 40, 4, 60, 70 ] )
		y.insertBefore( 200, 220 )
		self.assert_( y[:] == [ 10, 100, 220, 200, 300, 400, 40, 600, 700 ] )
		self.assert_( x[:] == [ 1, 10, 22, 20, 30, 40, 4, 60, 70 ] )
		y.insertAfter( 200, 310 )
		self.assert_( y[:] == [ 10, 100, 220, 200, 310, 300, 400, 40, 600, 700 ] )
		self.assert_( x[:] == [ 1, 10, 22, 20, 31, 30, 40, 4, 60, 70 ] )
		y.remove( 600 )
		self.assert_( y[:] == [ 10, 100, 220, 200, 310, 300, 400, 40, 700 ] )
		self.assert_( x[:] == [ 1, 10, 22, 20, 31, 30, 40, 4, 70 ] )



	def testOpSlice(self):
		layer1 = DocModelLayer()
		layer2 = DocModelLayer()
		x = DMList( layer1 )
		x.extend( [ 1, 2, 3 ] )

		y = DMList( layer2, DMListOpSlice( layer2, x, 1, -1 ) )
		self.assert_( y[:] == [ 2 ] )
		y.append( 4 )
		self.assert_( y[:] == [ 2, 4 ] )
		self.assert_( x[:] == [ 1, 2, 4, 3 ] )
		y[1:2] = [ 10, 20, 30, 40 ]
		self.assert_( y[:] == [ 2, 10, 20, 30, 40 ] )
		self.assert_( x[:] == [ 1, 2, 10, 20, 30, 40, 3 ] )
		y.extend( [ 60, 70 ] )
		self.assert_( y[:] == [ 2, 10, 20, 30, 40, 60, 70 ] )
		self.assert_( x[:] == [ 1, 2, 10, 20, 30, 40, 60, 70, 3 ] )
		y.insertBefore( 20, 22 )
		self.assert_( y[:] == [ 2, 10, 22, 20, 30, 40, 60, 70 ] )
		self.assert_( x[:] == [ 1, 2, 10, 22, 20, 30, 40, 60, 70, 3 ] )
		y.insertAfter( 20, 31 )
		self.assert_( y[:] == [ 2, 10, 22, 20, 31, 30, 40, 60, 70 ] )
		self.assert_( x[:] == [ 1, 2, 10, 22, 20, 31, 30, 40, 60, 70, 3 ] )
		y.remove( 60 )
		self.assert_( y[:] == [ 2, 10, 22, 20, 31, 30, 40, 70 ] )
		self.assert_( x[:] == [ 1, 2, 10, 22, 20, 31, 30, 40, 70, 3 ] )



	def testOpWrap(self):
		layer1 = DocModelLayer()
		layer2 = DocModelLayer()
		x = DMList( layer1 )

		y = DMList( layer2, DMListOpWrap( layer2, x, [ -1 ], [ -2 ] ) )
		self.assert_( y[:] == [ -1, -2 ] )
		x.append( 4 )
		self.assert_( y[:] == [ -1, 4, -2 ] )
		self.assert_( x[:] == [ 4 ] )
		x.extend( [ 1, 2 ] )
		self.assert_( y[:] == [ -1, 4, 1, 2, -2 ] )
		self.assert_( x[:] == [ 4, 1, 2 ] )
		y[2:3] = [ 6, 7, 8 ]
		self.assert_( y[:] == [ -1, 4, 6, 7, 8, 2, -2 ] )
		self.assert_( x[:] == [ 4, 6, 7, 8, 2 ] )
		y.insertBefore( 7, 13 )
		self.assert_( y[:] == [ -1, 4, 6, 13, 7, 8, 2, -2 ] )
		self.assert_( x[:] == [ 4, 6, 13, 7, 8, 2 ] )
		y.insertAfter( 7, 15 )
		self.assert_( y[:] == [ -1, 4, 6, 13, 7, 15, 8, 2, -2 ] )
		self.assert_( x[:] == [ 4, 6, 13, 7, 15, 8, 2 ] )
		y.remove( 7 )
		self.assert_( y[:] == [ -1, 4, 6, 13, 15, 8, 2, -2 ] )
		self.assert_( x[:] == [ 4, 6, 13, 15, 8, 2 ] )




	def testLayers(self):
		def layerFunction(ls, layer):
			if ls[0] == 'plus2':
				return DMList( layer, DMListOpWrap( layer, DMListOpMap( layer, DMListOpSlice( layer, ls, 1, None ), lambda x: x + 2, lambda x: x - 2 ), [ 'plus2b' ], [] ) )
			elif ls[0] == 'times2':
				return DMList( layer, DMListOpWrap( layer, DMListOpMap( layer, DMListOpSlice( layer, ls, 1, None ), lambda x: x * 2, lambda x: x / 2 ), [ 'times2b' ], [] ) )
			else:
				return copy( ls )

		layer1 = DocModelLayer()
		layer2 = DocModelLayer( layerFunction )

		x = DMList( layer1 )
		x.extend( [ 1, 2, 3 ] )
		xx1 = DMList( layer1 )
		xx1.extend( [ 'plus2', 5, 6, 7 ] )
		x.append( xx1 )
		xx2 = DMList( layer1 )
		xx2.extend( [ 'times2', 11, 12, 13 ] )
		x.append( xx2 )


		y = DMList( layer2, DMListOpNop( layer2, x ) )


		self.assert_( y[0:3] == [ 1, 2, 3 ] )
		self.assert_( y[3][:] == [ 'plus2b', 7, 8, 9 ] )
		self.assert_( y[4][:] == [ 'times2b', 22, 24, 26 ] )

		xx1[2] = 8
		self.assert_( x[3][:] == [ 'plus2', 5, 8, 7 ] )
		self.assert_( y[3][:] == [ 'plus2b', 7, 10, 9 ] )
		xx1[0] = 'times2'
		self.assert_( x[3][:] == [ 'times2', 5, 8, 7 ] )
		self.assert_( y[3][:] == [ 'times2b', 10, 16, 14 ] )
		xx1[0] = 'plus2'
		self.assert_( x[3][:] == [ 'plus2', 5, 8, 7 ] )
		self.assert_( y[3][:] == [ 'plus2b', 7, 10, 9 ] )





1
if __name__ == '__main__':
	unittest.main()
