##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy

from Britefury.Cell.Cell import RefCell

from Britefury.DocModel.DMListInterface import DMListInterface






class DMVirtualList (DMListInterface):
	__slots__ = [ '_op', '_cell' ]


	def __init__(self, op):
		self._op = op
		self._cell = RefCell()

		self._cell.function = self._op.evaluate


	def append(self, x):
		self._op.append( x )

	def extend(self, xs):
		self._op.extend( xs )

	def insert(self, i, x):
		self._op.insert( i, x)

	def remove(self, x):
		self._op.remove( x )

	def __setitem__(self, i, x):
		self._op[i] = x

	def __delitem__(self, i):
		del self._op[i]


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
		return DMVirtualList( self._op )

	def __deepcopy__(self, memo):
		return DMVirtualList( self._op )



	def setOp(self, op):
		self._op = op
		self._cell.function = self._op.evaluate


	op = property( None, setOp )

































#class TestCase_List (unittest.TestCase):
	#def testLayers(self):
		#def opPlus2(ls, layer):
			#return DMListOpWrap( layer, DMListOpMap( layer, DMListOpSlice( layer, ls, 1, None ), lambda x: x + 2, lambda x: x - 2 ), [ 'plus2b' ], [] )

		#def opTimes2(ls, layer):
			#return DMListOpWrap( layer, DMListOpMap( layer, DMListOpSlice( layer, ls, 1, None ), lambda x: x * 2, lambda x: x / 2 ), [ 'times2b' ], [] )

		#def opNop(ls, layer):
			#return DMListOpNop( layer, ls )

		#def layerOpFunctionGenerator(ls, layer):
			#if ls[0] == 'plus2':
				#return opPlus2
			#elif ls[0] == 'times2':
				#return opTimes2
			#else:
				#return opNop

		#layer1 = DocModelLayer()
		#layer2 = DocModelLayer( layerOpFunctionGenerator )

		#x = DMList()
		#x.extend( [ 1, 2, 3 ] )
		#xx1 = DMList()
		#xx1.extend( [ 'plus2', 5, 6, 7 ] )
		#x.append( xx1 )
		#xx2 = DMList()
		#xx2.extend( [ 'times2', 11, 12, 13 ] )
		#x.append( xx2 )


		#y = DMVirtualList( DMListOpNop( layer2, x ) )


		#self.assert_( y[0:3] == [ 1, 2, 3 ] )
		#self.assert_( y[3][:] == [ 'plus2b', 7, 8, 9 ] )
		#self.assert_( y[4][:] == [ 'times2b', 22, 24, 26 ] )

		#xx1[2] = 8
		#self.assert_( x[3][:] == [ 'plus2', 5, 8, 7 ] )
		#self.assert_( y[3][:] == [ 'plus2b', 7, 10, 9 ] )
		#xx1[0] = 'times2'
		#self.assert_( x[3][:] == [ 'times2', 5, 8, 7 ] )
		#self.assert_( y[3][:] == [ 'times2b', 10, 16, 14 ] )
		#xx1[0] = 'plus2'
		#self.assert_( x[3][:] == [ 'plus2', 5, 8, 7 ] )
		#self.assert_( y[3][:] == [ 'plus2b', 7, 10, 9 ] )
		#y[3][2] = 8
		#self.assert_( x[3][:] == [ 'plus2', 5, 6, 7 ] )
		#self.assert_( y[3][:] == [ 'plus2b', 7, 8, 9 ] )
		#y[3][2] = 8
		#self.assert_( x[3][:] == [ 'plus2', 5, 6, 7 ] )
		#self.assert_( y[3][:] == [ 'plus2b', 7, 8, 9 ] )
		#self.assert_( x[4][:] == [ 'times2', 11, 12, 13 ] )
		#self.assert_( y[4][:] == [ 'times2b', 22, 24, 26 ] )
		#y[4][2] = 8
		#self.assert_( x[3][:] == [ 'plus2', 5, 6, 7 ] )
		#self.assert_( y[3][:] == [ 'plus2b', 7, 8, 9 ] )
		#self.assert_( x[4][:] == [ 'times2', 11, 4, 13 ] )
		#self.assert_( y[4][:] == [ 'times2b', 22, 8, 26 ] )





if __name__ == '__main__':
	unittest.main()
