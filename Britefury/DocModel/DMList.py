##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy

from Britefury.Cell.Cell import RefCell

from Britefury.DocModel.DocModelLayer import DocModelLayer
from Britefury.DocModel.DMListOperator import DMListOpMap, DMListOpSlice, DMListOpWrap, DMListOpNop
from Britefury.DocModel.DMListInterface import DMListInterface
from Britefury.DocModel.DMLiteralList import DMLiteralList






class DMList (DMListInterface):
	def __init__(self, op):
		self._op = op
		self._cell = RefCell()

		self._cell.function = self._op.evaluate


	def append(self, x):
		self._op.append( x )

	def extend(self, xs):
		self._op.extend( xs )

	def insertBefore(self, before, x):
		self._op.insertBefore( before, x )

	def insertAfter(self, after, x):
		self._op.insertAfter( after, x )

	def remove(self, x):
		self._op.remove( x )

	def replace(self, a, x):
		self._op.replace( a, x )

	def replaceRange(self, a, b, xs):
		self._op.replaceRange( a, b, xs )

	def __setitem__(self, i, x):
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


	def getDestList(self, layer):
		return layer.getDestList( self )

	def getSrcList(self, layer):
		return layer.getSrcList( self )



	def __copy__(self):
		return DMList( self._op )

	def __deepcopy__(self, memo):
		return DMList( self._op )



	def setOp(self, op):
		self._op = op
		self._cell.function = self._op.evaluate


	op = property( None, setOp )






import unittest



class TestCase_List (unittest.TestCase):
	def testOpMap(self):
		layer1 = DocModelLayer()
		layer2 = DocModelLayer()
		x = DMLiteralList()
		x.extend( [ 1, 2, 3 ] )

		y = DMList( DMListOpMap( layer2, x, lambda x: x * 10, lambda x: x / 10 ) )
		self.assert_( y[0] == 10 )
		self.assert_( y[:] == [ 10, 20, 30 ] )
		y.append( 40 )
		self.assert_( x[:] == [ 1, 2, 3, 4 ] )
		self.assert_( y[:] == [ 10, 20, 30, 40 ] )
		y[1:3] = [ 100, 200, 300, 400 ]
		self.assert_( x[:] == [ 1, 10, 20, 30, 40, 4 ] )
		self.assert_( y[:] == [ 10, 100, 200, 300, 400, 40 ] )
		y.extend( [ 600, 700 ] )
		self.assert_( x[:] == [ 1, 10, 20, 30, 40, 4, 60, 70 ] )
		self.assert_( y[:] == [ 10, 100, 200, 300, 400, 40, 600, 700 ] )
		y.insertBefore( 200, 220 )
		self.assert_( x[:] == [ 1, 10, 22, 20, 30, 40, 4, 60, 70 ] )
		self.assert_( y[:] == [ 10, 100, 220, 200, 300, 400, 40, 600, 700 ] )
		y.insertAfter( 200, 310 )
		self.assert_( x[:] == [ 1, 10, 22, 20, 31, 30, 40, 4, 60, 70 ] )
		self.assert_( y[:] == [ 10, 100, 220, 200, 310, 300, 400, 40, 600, 700 ] )
		y.remove( 600 )
		self.assert_( x[:] == [ 1, 10, 22, 20, 31, 30, 40, 4, 70 ] )
		self.assert_( y[:] == [ 10, 100, 220, 200, 310, 300, 400, 40, 700 ] )
		y.replace( 200, 210 )
		self.assert_( x[:] == [ 1, 10, 22, 21, 31, 30, 40, 4, 70 ] )
		self.assert_( y[:] == [ 10, 100, 220, 210, 310, 300, 400, 40, 700 ] )
		y.replaceRange( 310, 40, [ 980, 990 ] )
		self.assert_( x[:] == [ 1, 10, 22, 21, 98, 99, 70 ] )
		self.assert_( y[:] == [ 10, 100, 220, 210, 980, 990, 700 ] )



	def testOpSlice(self):
		layer1 = DocModelLayer()
		layer2 = DocModelLayer()
		x = DMLiteralList()
		x.extend( [ 1, 2, 3 ] )

		y = DMList( DMListOpSlice( layer2, x, 1, -1 ) )
		self.assert_( y[:] == [ 2 ] )
		y.append( 4 )
		self.assert_( y[:] == [ 2, 4 ] )
		self.assert_( x[:] == [ 1, 2, 4, 3 ] )
		y[1:2] = [ 10, 20, 30, 40 ]
		self.assert_( x[:] == [ 1, 2, 10, 20, 30, 40, 3 ] )
		self.assert_( y[:] == [ 2, 10, 20, 30, 40 ] )
		y.extend( [ 60, 70 ] )
		self.assert_( x[:] == [ 1, 2, 10, 20, 30, 40, 60, 70, 3 ] )
		self.assert_( y[:] == [ 2, 10, 20, 30, 40, 60, 70 ] )
		y.insertBefore( 20, 22 )
		self.assert_( x[:] == [ 1, 2, 10, 22, 20, 30, 40, 60, 70, 3 ] )
		self.assert_( y[:] == [ 2, 10, 22, 20, 30, 40, 60, 70 ] )
		y.insertAfter( 20, 31 )
		self.assert_( x[:] == [ 1, 2, 10, 22, 20, 31, 30, 40, 60, 70, 3 ] )
		self.assert_( y[:] == [ 2, 10, 22, 20, 31, 30, 40, 60, 70 ] )
		y.remove( 60 )
		self.assert_( x[:] == [ 1, 2, 10, 22, 20, 31, 30, 40, 70, 3 ] )
		self.assert_( y[:] == [ 2, 10, 22, 20, 31, 30, 40, 70 ] )
		y.replace( 20, 21 )
		self.assert_( x[:] == [ 1, 2, 10, 22, 21, 31, 30, 40, 70, 3 ] )
		self.assert_( y[:] == [ 2, 10, 22, 21, 31, 30, 40, 70 ] )
		y.replaceRange( 31, 40, [ 98, 99 ] )
		self.assert_( x[:] == [ 1, 2, 10, 22, 21, 98, 99, 70, 3 ] )
		self.assert_( y[:] == [ 2, 10, 22, 21, 98, 99, 70 ] )



	def testOpWrap(self):
		layer1 = DocModelLayer()
		layer2 = DocModelLayer()
		x = DMLiteralList()

		y = DMList( DMListOpWrap( layer2, x, [ -1 ], [ -2 ] ) )
		self.assert_( y[:] == [ -1, -2 ] )
		x.append( 4 )
		self.assert_( x[:] == [ 4 ] )
		self.assert_( y[:] == [ -1, 4, -2 ] )
		x.extend( [ 1, 2 ] )
		self.assert_( x[:] == [ 4, 1, 2 ] )
		self.assert_( y[:] == [ -1, 4, 1, 2, -2 ] )
		y[2:3] = [ 6, 7, 8 ]
		self.assert_( x[:] == [ 4, 6, 7, 8, 2 ] )
		self.assert_( y[:] == [ -1, 4, 6, 7, 8, 2, -2 ] )
		y.insertBefore( 7, 13 )
		self.assert_( x[:] == [ 4, 6, 13, 7, 8, 2 ] )
		self.assert_( y[:] == [ -1, 4, 6, 13, 7, 8, 2, -2 ] )
		y.insertAfter( 7, 15 )
		self.assert_( x[:] == [ 4, 6, 13, 7, 15, 8, 2 ] )
		self.assert_( y[:] == [ -1, 4, 6, 13, 7, 15, 8, 2, -2 ] )
		y.remove( 7 )
		self.assert_( x[:] == [ 4, 6, 13, 15, 8, 2 ] )
		self.assert_( y[:] == [ -1, 4, 6, 13, 15, 8, 2, -2 ] )
		y.replace( 13, 17 )
		self.assert_( x[:] == [ 4, 6, 17, 15, 8, 2 ] )
		self.assert_( y[:] == [ -1, 4, 6, 17, 15, 8, 2, -2 ] )
		y.replaceRange( 15, 2, [ 98, 99 ] )
		self.assert_( x[:] == [ 4, 6, 17, 98, 99 ] )
		self.assert_( y[:] == [ -1, 4, 6, 17, 98, 99, -2 ] )




	def testLayers(self):
		def opPlus2(ls, layer):
			return DMListOpWrap( layer, DMListOpMap( layer, DMListOpSlice( layer, ls, 1, None ), lambda x: x + 2, lambda x: x - 2 ), [ 'plus2b' ], [] )

		def opTimes2(ls, layer):
			return DMListOpWrap( layer, DMListOpMap( layer, DMListOpSlice( layer, ls, 1, None ), lambda x: x * 2, lambda x: x / 2 ), [ 'times2b' ], [] )

		def opNop(ls, layer):
			return DMListOpNop( layer, ls )

		def layerOpFunctionGenerator(ls, layer):
			if ls[0] == 'plus2':
				return opPlus2
			elif ls[0] == 'times2':
				return opTimes2
			else:
				return opNop

		layer1 = DocModelLayer()
		layer2 = DocModelLayer( layerOpFunctionGenerator )

		x = DMLiteralList()
		x.extend( [ 1, 2, 3 ] )
		xx1 = DMLiteralList()
		xx1.extend( [ 'plus2', 5, 6, 7 ] )
		x.append( xx1 )
		xx2 = DMLiteralList()
		xx2.extend( [ 'times2', 11, 12, 13 ] )
		x.append( xx2 )


		y = DMList( DMListOpNop( layer2, x ) )


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
		y[3][2] = 8
		self.assert_( x[3][:] == [ 'plus2', 5, 6, 7 ] )
		self.assert_( y[3][:] == [ 'plus2b', 7, 8, 9 ] )
		y[3][2] = 8
		self.assert_( x[3][:] == [ 'plus2', 5, 6, 7 ] )
		self.assert_( y[3][:] == [ 'plus2b', 7, 8, 9 ] )
		self.assert_( x[4][:] == [ 'times2', 11, 12, 13 ] )
		self.assert_( y[4][:] == [ 'times2b', 22, 24, 26 ] )
		y[4][2] = 8
		self.assert_( x[3][:] == [ 'plus2', 5, 6, 7 ] )
		self.assert_( y[3][:] == [ 'plus2b', 7, 8, 9 ] )
		self.assert_( x[4][:] == [ 'times2', 11, 4, 13 ] )
		self.assert_( y[4][:] == [ 'times2b', 22, 8, 26 ] )





1
if __name__ == '__main__':
	unittest.main()
