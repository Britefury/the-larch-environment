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

	def insert(self, i, x):
		self._op.insert( i, x)

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
		return DMList( self._op )

	def __deepcopy__(self, memo):
		return DMList( self._op )



	def setOp(self, op):
		self._op = op
		self._cell.function = self._op.evaluate


	op = property( None, setOp )






import unittest




class TestCase_List_Nop (unittest.TestCase):
	def setUp(self):
		self.layer1 = DocModelLayer()
		self.layer2 = DocModelLayer()
		self.x = DMLiteralList()
		self.x.extend( range( 0, 10 ) )

		self.y = DMList( DMListOpNop( self.layer2, self.x ) )

	def tearDown(self):
		del self.layer1
		del self.layer2
		del self.x
		del self.y


	def testFunction(self):
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.y[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )

	def testAppend(self):
		self.y.append( 11 )
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11 ] )
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11 ] )

	def testExtend(self):
		self.y.extend( range( 20, 23 ) )
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 20, 21, 22 ] )
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 20, 21, 22 ] )

	def testInsert(self):
		self.y.insert( 5, 34 )
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 34, 5, 6, 7, 8, 9 ] )
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 34, 5, 6, 7, 8, 9 ] )

	def testRemove(self):
		self.y.remove( 5 )
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 6, 7, 8, 9 ] )
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 6, 7, 8, 9 ] )

	def testSet(self):
		self.y[2] = 22
		self.assert_( self.x[:] == [ 0, 1, 22, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.x[:] == [ 0, 1, 22, 3, 4, 5, 6, 7, 8, 9 ] )
		self.y[2:4] = [ 22, 23, 24 ]
		self.assert_( self.x[:] == [ 0, 1, 22, 23, 24, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.x[:] == [ 0, 1, 22, 23, 24, 4, 5, 6, 7, 8, 9 ] )

	def testDel(self):
		del self.y[2]
		self.assert_( self.x[:] == [ 0, 1, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.x[:] == [ 0, 1, 3, 4, 5, 6, 7, 8, 9 ] )
		del self.y[2:4]
		self.assert_( self.x[:] == [ 0, 1, 5, 6, 7, 8, 9 ] )
		self.assert_( self.x[:] == [ 0, 1, 5, 6, 7, 8, 9 ] )




class TestCase_List_Map (unittest.TestCase):
	def setUp(self):
		self.layer1 = DocModelLayer()
		self.layer2 = DocModelLayer()
		self.x = DMLiteralList()
		self.x.extend( range( 0, 10 ) )

		self.y = DMList( DMListOpMap( self.layer2, self.x, lambda x: x * 10, lambda x: x / 10 ) )

	def tearDown(self):
		del self.layer1
		del self.layer2
		del self.x
		del self.y


	def testFunction(self):
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.y[:] == [ 0, 10, 20, 30, 40, 50, 60, 70, 80, 90 ] )

	def testAppend(self):
		self.y.append( 110 )
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11 ] )
		self.assert_( self.y[:] == [ 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 110 ] )

	def testExtend(self):
		self.y.extend( range( 200, 230, 10 ) )
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 20, 21, 22 ] )
		self.assert_( self.y[:] == [ 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 200, 210, 220 ] )

	def testInsert(self):
		self.y.insert( 5, 340 )
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 34, 5, 6, 7, 8, 9 ] )
		self.assert_( self.y[:] == [ 0, 10, 20, 30, 40, 340, 50, 60, 70, 80, 90 ] )

	def testRemove(self):
		self.y.remove( 50 )
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 6, 7, 8, 9 ] )
		self.assert_( self.y[:] == [ 0, 10, 20, 30, 40, 60, 70, 80, 90 ] )

	def testSet(self):
		self.y[2] = 220
		self.assert_( self.x[:] == [ 0, 1, 22, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.y[:] == [ 0, 10, 220, 30, 40, 50, 60, 70, 80, 90 ] )
		self.y[2:4] = [ 220, 230, 240 ]
		self.assert_( self.x[:] == [ 0, 1, 22, 23, 24, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.y[:] == [ 0, 10, 220, 230, 240, 40, 50, 60, 70, 80, 90 ] )

	def testDel(self):
		del self.y[2]
		self.assert_( self.x[:] == [ 0, 1, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.y[:] == [ 0, 10, 30, 40, 50, 60, 70, 80, 90 ] )
		del self.y[2:4]
		self.assert_( self.x[:] == [ 0, 1, 5, 6, 7, 8, 9 ] )
		self.assert_( self.y[:] == [ 0, 10, 50, 60, 70, 80, 90 ] )




class TestCase_List_Slice (unittest.TestCase):
	def setUp(self):
		self.layer1 = DocModelLayer()
		self.layer2 = DocModelLayer()
		self.xpp = DMLiteralList()
		self.xpp.extend( range( 0, 10 ) )
		self.xpn = DMLiteralList()
		self.xpn.extend( range( 0, 10 ) )
		self.xnp = DMLiteralList()
		self.xnp.extend( range( 0, 10 ) )
		self.xnn = DMLiteralList()
		self.xnn.extend( range( 0, 10 ) )
		self.xp0 = DMLiteralList()
		self.xp0.extend( range( 0, 10 ) )
		self.xn0 = DMLiteralList()
		self.xn0.extend( range( 0, 10 ) )

		self.ypp = DMList( DMListOpSlice( self.layer2, self.xpp, 1, 9 ) )
		self.ypn = DMList( DMListOpSlice( self.layer2, self.xpn, 1, -1 ) )
		self.ynp = DMList( DMListOpSlice( self.layer2, self.xnp, -9, 9 ) )
		self.ynn = DMList( DMListOpSlice( self.layer2, self.xnn, -9, -1 ) )
		self.yp0 = DMList( DMListOpSlice( self.layer2, self.xp0, 1 ) )
		self.yn0 = DMList( DMListOpSlice( self.layer2, self.xn0, -9 ) )

	def tearDown(self):
		del self.layer1
		del self.layer2
		del self.xpp
		del self.xpn
		del self.xnp
		del self.xnn
		del self.xp0
		del self.xn0
		del self.ypp
		del self.ypn
		del self.ynp
		del self.ynn
		del self.yp0
		del self.yn0


	def testFunction(self):
		self.assert_( self.xpp[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.ypp[:] == [ 1, 2, 3, 4, 5, 6, 7, 8 ] )
		self.assert_( self.xpn[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.ypn[:] == [ 1, 2, 3, 4, 5, 6, 7, 8 ] )
		self.assert_( self.xnp[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.ynp[:] == [ 1, 2, 3, 4, 5, 6, 7, 8 ] )
		self.assert_( self.xnn[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.ynn[:] == [ 1, 2, 3, 4, 5, 6, 7, 8 ] )
		self.assert_( self.xp0[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.yp0[:] == [ 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.xn0[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.yn0[:] == [ 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )

	def testAppend(self):
		self.ypp.append( 11 )
		self.ypn.append( 11 )
		self.ynp.append( 11 )
		self.ynn.append( 11 )
		self.yp0.append( 11 )
		self.yn0.append( 11 )
		self.assert_( self.xpp[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 9 ] )
		self.assert_( self.ypp[:] == [ 1, 2, 3, 4, 5, 6, 7, 8, 11 ] )
		self.assert_( self.xpn[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 9 ] )
		self.assert_( self.ypn[:] == [ 1, 2, 3, 4, 5, 6, 7, 8, 11 ] )
		self.assert_( self.xnp[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 9 ] )
		self.assert_( self.ynp[:] == [ 1, 2, 3, 4, 5, 6, 7, 8, 11 ] )
		self.assert_( self.xnn[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 9 ] )
		self.assert_( self.ynn[:] == [ 1, 2, 3, 4, 5, 6, 7, 8, 11 ] )
		self.assert_( self.xp0[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11 ] )
		self.assert_( self.yp0[:] == [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 11 ] )
		self.assert_( self.xn0[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11 ] )
		self.assert_( self.yn0[:] == [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 11 ] )

	def testExtend(self):
		self.ypp.extend( [ 20, 21, 22 ] )
		self.ypn.extend( [ 20, 21, 22 ] )
		self.ynp.extend( [ 20, 21, 22 ] )
		self.ynn.extend( [ 20, 21, 22 ] )
		self.yp0.extend( [ 20, 21, 22 ] )
		self.yn0.extend( [ 20, 21, 22 ] )
		self.assert_( self.xpp[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 20, 21, 22, 9 ] )
		self.assert_( self.ypp[:] == [ 1, 2, 3, 4, 5, 6, 7, 8, 20, 21, 22 ] )
		self.assert_( self.xpn[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 20, 21, 22, 9 ] )
		self.assert_( self.ypn[:] == [ 1, 2, 3, 4, 5, 6, 7, 8, 20, 21, 22 ] )
		self.assert_( self.xnp[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 20, 21, 22, 9 ] )
		self.assert_( self.ynp[:] == [ 1, 2, 3, 4, 5, 6, 7, 8, 20, 21, 22 ] )
		self.assert_( self.xnn[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 20, 21, 22, 9 ] )
		self.assert_( self.ynn[:] == [ 1, 2, 3, 4, 5, 6, 7, 8, 20, 21, 22 ] )
		self.assert_( self.xp0[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 20, 21, 22 ] )
		self.assert_( self.yp0[:] == [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 20, 21, 22 ] )
		self.assert_( self.xn0[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 20, 21, 22 ] )
		self.assert_( self.yn0[:] == [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 20, 21, 22 ] )

	def testInsert(self):
		self.ypp.insert( 4, 11 )
		self.ypn.insert( 4, 11 )
		self.ynp.insert( 4, 11 )
		self.ynn.insert( 4, 11 )
		self.yp0.insert( 4, 11 )
		self.yn0.insert( 4, 11 )
		self.assert_( self.xpp[:] == [ 0, 1, 2, 3, 4, 11, 5, 6, 7, 8, 9 ] )
		self.assert_( self.ypp[:] == [ 1, 2, 3, 4, 11, 5, 6, 7, 8 ] )
		self.assert_( self.xpn[:] == [ 0, 1, 2, 3, 4, 11, 5, 6, 7, 8, 9 ] )
		self.assert_( self.ypn[:] == [ 1, 2, 3, 4, 11, 5, 6, 7, 8 ] )
		self.assert_( self.xnp[:] == [ 0, 1, 2, 3, 4, 11, 5, 6, 7, 8, 9 ] )
		self.assert_( self.ynp[:] == [ 1, 2, 3, 4, 11, 5, 6, 7, 8 ] )
		self.assert_( self.xnn[:] == [ 0, 1, 2, 3, 4, 11, 5, 6, 7, 8, 9 ] )
		self.assert_( self.ynn[:] == [ 1, 2, 3, 4, 11, 5, 6, 7, 8 ] )
		self.assert_( self.xp0[:] == [ 0, 1, 2, 3, 4, 11, 5, 6, 7, 8, 9 ] )
		self.assert_( self.yp0[:] == [ 1, 2, 3, 4, 11, 5, 6, 7, 8, 9 ] )
		self.assert_( self.xn0[:] == [ 0, 1, 2, 3, 4, 11, 5, 6, 7, 8, 9 ] )
		self.assert_( self.yn0[:] == [ 1, 2, 3, 4, 11, 5, 6, 7, 8, 9 ] )
		self.ypp.insert( -3, 13 )
		self.ypn.insert( -3, 13 )
		self.ynp.insert( -3, 13 )
		self.ynn.insert( -3, 13 )
		self.yp0.insert( -3, 13 )
		self.yn0.insert( -3, 13 )
		self.assert_( self.xpp[:] == [ 0, 1, 2, 3, 4, 11, 5, 13, 6, 7, 8, 9 ] )
		self.assert_( self.ypp[:] == [ 1, 2, 3, 4, 11, 5, 13, 6, 7, 8 ] )
		self.assert_( self.xpn[:] == [ 0, 1, 2, 3, 4, 11, 5, 13, 6, 7, 8, 9 ] )
		self.assert_( self.ypn[:] == [ 1, 2, 3, 4, 11, 5, 13, 6, 7, 8 ] )
		self.assert_( self.xnp[:] == [ 0, 1, 2, 3, 4, 11, 5, 13, 6, 7, 8, 9 ] )
		self.assert_( self.ynp[:] == [ 1, 2, 3, 4, 11, 5, 13, 6, 7, 8 ] )
		self.assert_( self.xnn[:] == [ 0, 1, 2, 3, 4, 11, 5, 13, 6, 7, 8, 9 ] )
		self.assert_( self.ynn[:] == [ 1, 2, 3, 4, 11, 5, 13, 6, 7, 8 ] )
		self.assert_( self.xp0[:] == [ 0, 1, 2, 3, 4, 11, 5, 6, 13, 7, 8, 9 ] )
		self.assert_( self.yp0[:] == [ 1, 2, 3, 4, 11, 5, 6, 13, 7, 8, 9 ] )
		self.assert_( self.xn0[:] == [ 0, 1, 2, 3, 4, 11, 5, 6, 13, 7, 8, 9 ] )
		self.assert_( self.yn0[:] == [ 1, 2, 3, 4, 11, 5, 6, 13, 7, 8, 9 ] )

	def testRemove(self):
		self.ypp.remove( 5 )
		self.ypn.remove( 5 )
		self.ynp.remove( 5 )
		self.ynn.remove( 5 )
		self.yp0.remove( 5 )
		self.yn0.remove( 5 )
		self.assert_( self.xpp[:] == [ 0, 1, 2, 3, 4, 6, 7, 8, 9 ] )
		self.assert_( self.ypp[:] == [ 1, 2, 3, 4, 6, 7, 8 ] )
		self.assert_( self.xpn[:] == [ 0, 1, 2, 3, 4, 6, 7, 8, 9 ] )
		self.assert_( self.ypn[:] == [ 1, 2, 3, 4, 6, 7, 8 ] )
		self.assert_( self.xnp[:] == [ 0, 1, 2, 3, 4, 6, 7, 8, 9 ] )
		self.assert_( self.ynp[:] == [ 1, 2, 3, 4, 6, 7, 8 ] )
		self.assert_( self.xnn[:] == [ 0, 1, 2, 3, 4, 6, 7, 8, 9 ] )
		self.assert_( self.ynn[:] == [ 1, 2, 3, 4, 6, 7, 8 ] )
		self.assert_( self.xp0[:] == [ 0, 1, 2, 3, 4, 6, 7, 8, 9 ] )
		self.assert_( self.yp0[:] == [ 1, 2, 3, 4, 6, 7, 8, 9 ] )
		self.assert_( self.xn0[:] == [ 0, 1, 2, 3, 4, 6, 7, 8, 9 ] )
		self.assert_( self.yn0[:] == [ 1, 2, 3, 4, 6, 7, 8, 9 ] )


	def _testSet(self, index, value, xRes, yRes, xRes0, yRes0):
		if isinstance( index, tuple ):
			if len( index ) == 2:
				start, stop = index
				self.ypp[start:stop] = value
				self.ypn[start:stop] = value
				self.ynp[start:stop] = value
				self.ynn[start:stop] = value
				self.yp0[start:stop] = value
				self.yn0[start:stop] = value
			elif len( index ) == 3:
				start, stop, step = index
				self.ypp[start:stop:step] = value
				self.ypn[start:stop:step] = value
				self.ynp[start:stop:step] = value
				self.ynn[start:stop:step] = value
				self.yp0[start:stop:step] = value
				self.yn0[start:stop:step] = value
		else:
			self.ypp[index] = value
			self.ypn[index] = value
			self.ynp[index] = value
			self.ynn[index] = value
			self.yp0[index] = value
			self.yn0[index] = value
		self.assert_( self.xpp[:] == xRes, ( self.xpp[:], xRes ) )
		self.assert_( self.ypp[:] == yRes, ( self.ypp[:], yRes ) )
		self.assert_( self.xpn[:] == xRes, ( self.xpn[:], xRes ) )
		self.assert_( self.ypn[:] == yRes, ( self.ypn[:], yRes ) )
		self.assert_( self.xnp[:] == xRes, ( self.xnp[:], xRes ) )
		self.assert_( self.ynp[:] == yRes, ( self.ynp[:], yRes ) )
		self.assert_( self.xnn[:] == xRes, ( self.xnn[:], xRes ) )
		self.assert_( self.ynn[:] == yRes, ( self.ynn[:], yRes ) )
		self.assert_( self.xp0[:] == xRes0, ( self.xp0[:], xRes0 ) )
		self.assert_( self.yp0[:] == yRes0, ( self.yp0[:], yRes0 ) )
		self.assert_( self.xn0[:] == xRes0, ( self.xn0[:], xRes0 ) )
		self.assert_( self.yn0[:] == yRes0, ( self.yn0[:], yRes0 ) )

	def testSetP(self):
		self._testSet( 4, 11,   [ 0, 1, 2, 3, 4, 11, 6, 7, 8, 9 ],   [ 1, 2, 3, 4, 11, 6, 7, 8 ],    [ 0, 1, 2, 3, 4, 11, 6, 7, 8, 9 ],   [ 1, 2, 3, 4, 11, 6, 7, 8, 9 ] )

	def testSetN(self):
		self._testSet( -4, 11,   [ 0, 1, 2, 3, 4, 11, 6, 7, 8, 9 ],   [ 1, 2, 3, 4, 11, 6, 7, 8 ],    [ 0, 1, 2, 3, 4, 5, 11, 7, 8, 9 ],   [ 1, 2, 3, 4, 5, 11, 7, 8, 9 ] )

	def testSetPP(self):
		self._testSet( (2,6), [11,13],   [ 0, 1, 2, 11, 13, 7, 8, 9 ],   [ 1, 2, 11, 13, 7, 8 ],    [ 0, 1, 2, 11, 13, 7, 8, 9 ],   [ 1, 2, 11, 13, 7, 8, 9 ] )

	def testSetPN(self):
		self._testSet( (2,-2), [11,13],   [ 0, 1, 2, 11, 13, 7, 8, 9 ],   [ 1, 2, 11, 13, 7, 8 ],    [ 0, 1, 2, 11, 13, 8, 9 ],   [ 1, 2, 11, 13, 8, 9 ] )

	def testSetP0(self):
		self._testSet( (2,None), [11,13],   [ 0, 1, 2, 11, 13, 9 ],   [ 1, 2, 11, 13 ],    [ 0, 1, 2, 11, 13 ],   [ 1, 2, 11, 13 ] )

	def testSetNP(self):
		self._testSet( (-6,6), [11,13],   [ 0, 1, 2, 11, 13, 7, 8, 9 ],   [ 1, 2, 11, 13, 7, 8 ],    [ 0, 1, 2, 3, 11, 13, 7, 8, 9 ],   [ 1, 2, 3, 11, 13, 7, 8, 9 ] )

	def testSetNN(self):
		self._testSet( (-6,-2), [11,13],   [ 0, 1, 2, 11, 13, 7, 8, 9 ],   [ 1, 2, 11, 13, 7, 8 ],    [ 0, 1, 2, 3, 11, 13, 8, 9 ],   [ 1, 2, 3, 11, 13, 8, 9 ] )

	def testSetN0(self):
		self._testSet( (-6,None), [11,13],   [ 0, 1, 2, 11, 13, 9 ],   [ 1, 2, 11, 13 ],    [ 0, 1, 2, 3, 11, 13 ],   [ 1, 2, 3, 11, 13 ] )

	def testSet0P(self):
		self._testSet( (None,6), [11,13],   [ 0, 11, 13, 7, 8, 9 ],   [ 11, 13, 7, 8 ],    [ 0, 11, 13, 7, 8, 9 ],   [ 11, 13, 7, 8, 9 ] )

	def testSet0N(self):
		self._testSet( (None,-2), [11,13],   [ 0, 11, 13, 7, 8, 9 ],   [ 11, 13, 7, 8 ],    [ 0, 11, 13, 8, 9 ],   [ 11, 13, 8, 9 ] )

	def testSet00(self):
		self._testSet( (None,None), [11,13],   [ 0, 11, 13, 9 ],   [ 11, 13 ],    [ 0, 11, 13 ],   [ 11, 13 ] )

	def testSetStep(self):
		self._testSet( (2,6,2), [11,13],   [ 0, 1, 2, 11, 4, 13, 6, 7, 8, 9 ],   [ 1, 2, 11, 4, 13, 6, 7, 8 ],    [ 0, 1, 2, 11, 4, 13, 6, 7, 8, 9 ],   [ 1, 2, 11, 4, 13, 6, 7, 8, 9 ] )






	def _testDel(self, index, xRes, yRes, xRes0, yRes0):
		if isinstance( index, tuple ):
			if len( index ) == 2:
				start, stop = index
				del self.ypp[start:stop]
				del self.ypn[start:stop]
				del self.ynp[start:stop]
				del self.ynn[start:stop]
				del self.yp0[start:stop]
				del self.yn0[start:stop]
			elif len( index ) == 3:
				start, stop, step = index
				del self.ypp[start:stop:step]
				del self.ypn[start:stop:step]
				del self.ynp[start:stop:step]
				del self.ynn[start:stop:step]
				del self.yp0[start:stop:step]
				del self.yn0[start:stop:step]
		else:
			del self.ypp[index]
			del self.ypn[index]
			del self.ynp[index]
			del self.ynn[index]
			del self.yp0[index]
			del self.yn0[index]
		self.assert_( self.xpp[:] == xRes, ( self.xpp[:], xRes, self.ypp[:], yRes ) )
		self.assert_( self.ypp[:] == yRes, ( self.xpp[:], xRes, self.ypp[:], yRes ) )
		self.assert_( self.xpn[:] == xRes, ( self.xpn[:], xRes, self.ypn[:], yRes ) )
		self.assert_( self.ypn[:] == yRes, ( self.xpn[:], xRes, self.ypn[:], yRes ) )
		self.assert_( self.xnp[:] == xRes, ( self.xnp[:], xRes, self.ynp[:], yRes ) )
		self.assert_( self.ynp[:] == yRes, ( self.xnp[:], xRes, self.ynp[:], yRes ) )
		self.assert_( self.xnn[:] == xRes, ( self.xnn[:], xRes, self.ynn[:], yRes ) )
		self.assert_( self.ynn[:] == yRes, ( self.xnn[:], xRes, self.ynn[:], yRes ) )
		self.assert_( self.xp0[:] == xRes0, ( self.xp0[:], xRes0, self.yp0[:], yRes0 ) )
		self.assert_( self.yp0[:] == yRes0, ( self.xp0[:], xRes0, self.yp0[:], yRes0 ) )
		self.assert_( self.xn0[:] == xRes0, ( self.xn0[:], xRes0, self.yn0[:], yRes0 ) )
		self.assert_( self.yn0[:] == yRes0, ( self.xn0[:], xRes0, self.yn0[:], yRes0 ) )


	def testDelP(self):
		self._testDel( 4,   [ 0, 1, 2, 3, 4, 6, 7, 8, 9 ],   [ 1, 2, 3, 4, 6, 7, 8 ],    [ 0, 1, 2, 3, 4, 6, 7, 8, 9 ],   [ 1, 2, 3, 4, 6, 7, 8, 9 ] )

	def testDelN(self):
		self._testDel( -4,   [ 0, 1, 2, 3, 4, 6, 7, 8, 9 ],   [ 1, 2, 3, 4, 6, 7, 8 ],    [ 0, 1, 2, 3, 4, 5, 7, 8, 9 ],   [ 1, 2, 3, 4, 5, 7, 8, 9 ] )

	def testDelPP(self):
		self._testDel( (2,6),   [ 0, 1, 2, 7, 8, 9 ],   [ 1, 2, 7, 8 ],    [ 0, 1, 2, 7, 8, 9 ],   [ 1, 2, 7, 8, 9 ] )

	def testDelPN(self):
		self._testDel( (2,-2),   [ 0, 1, 2, 7, 8, 9 ],   [ 1, 2, 7, 8 ],    [ 0, 1, 2, 8, 9 ],   [ 1, 2, 8, 9 ] )

	def testDelP0(self):
		self._testDel( (2,None),   [ 0, 1, 2, 9 ],   [ 1, 2,  ],    [ 0, 1, 2,  ],   [ 1, 2,  ] )

	def testDelNP(self):
		self._testDel( (-6,6),   [ 0, 1, 2, 7, 8, 9 ],   [ 1, 2, 7, 8 ],    [ 0, 1, 2, 3, 7, 8, 9 ],   [ 1, 2, 3, 7, 8, 9 ] )

	def testDelNN(self):
		self._testDel( (-6,-2),   [ 0, 1, 2, 7, 8, 9 ],   [ 1, 2, 7, 8 ],    [ 0, 1, 2, 3, 8, 9 ],   [ 1, 2, 3, 8, 9 ] )

	def testDelN0(self):
		self._testDel( (-6,None),   [ 0, 1, 2, 9 ],   [ 1, 2,  ],    [ 0, 1, 2, 3,  ],   [ 1, 2, 3,  ] )

	def testDel0P(self):
		self._testDel( (None,6),   [ 0, 7, 8, 9 ],   [ 7, 8 ],    [ 0, 7, 8, 9 ],   [ 7, 8, 9 ] )

	def testDel0N(self):
		self._testDel( (None,-2),   [ 0, 7, 8, 9 ],   [ 7, 8 ],    [ 0, 8, 9 ],   [ 8, 9 ] )

	def testDel00(self):
		self._testDel( (None,None),   [ 0, 9 ],   [  ],    [ 0,  ],   [  ] )

	def testDelStep(self):
		self._testDel( (2,6,2),   [ 0, 1, 2, 4, 6, 7, 8, 9 ],   [ 1, 2, 4, 6, 7, 8 ],    [ 0, 1, 2, 4, 6, 7, 8, 9 ],   [ 1, 2, 4, 6, 7, 8, 9 ] )



	def testDelAppend(self):
		del self.ypp[:]
		del self.ypn[:]
		del self.ynp[:]
		del self.ynn[:]
		del self.yp0[:]
		del self.yn0[:]
		self.assert_( self.xpp[:] == [ 0, 9 ] )
		self.assert_( self.ypp[:] == [] )
		self.assert_( self.xpn[:] == [ 0, 9 ] )
		self.assert_( self.ypn[:] == [] )
		self.assert_( self.xnp[:] == [ 0, 9 ] )
		self.assert_( self.ynp[:] == [] )
		self.assert_( self.xnn[:] == [ 0, 9 ] )
		self.assert_( self.ynn[:] == [] )
		self.assert_( self.xp0[:] == [ 0 ] )
		self.assert_( self.yp0[:] == [] )
		self.assert_( self.xn0[:] == [ 0 ] )
		self.assert_( self.yn0[:] == [] )
		self.ypp.append( 11 )
		self.ypn.append( 11 )
		self.ynp.append( 11 )
		self.ynn.append( 11 )
		self.yp0.append( 11 )
		self.yn0.append( 11 )
		self.assert_( self.xpp[:] == [ 0, 11, 9 ] )
		self.assert_( self.ypp[:] == [ 11 ] )
		self.assert_( self.xpn[:] == [ 0, 11, 9 ] )
		self.assert_( self.ypn[:] == [ 11 ] )
		self.assert_( self.xnp[:] == [ 0, 11, 9 ] )
		self.assert_( self.ynp[:] == [ 11 ] )
		self.assert_( self.xnn[:] == [ 0, 11, 9 ] )
		self.assert_( self.ynn[:] == [ 11 ] )
		self.assert_( self.xp0[:] == [ 0, 11 ] )
		self.assert_( self.yp0[:] == [ 11 ] )
		self.assert_( self.xn0[:] == [ 0, 11 ] )
		self.assert_( self.yn0[:] == [ 11 ] )




	def testDelInsert(self):
		del self.ypp[:]
		del self.ypn[:]
		del self.ynp[:]
		del self.ynn[:]
		del self.yp0[:]
		del self.yn0[:]
		self.assert_( self.xpp[:] == [ 0, 9 ] )
		self.assert_( self.ypp[:] == [] )
		self.assert_( self.xpn[:] == [ 0, 9 ] )
		self.assert_( self.ypn[:] == [] )
		self.assert_( self.xnp[:] == [ 0, 9 ] )
		self.assert_( self.ynp[:] == [] )
		self.assert_( self.xnn[:] == [ 0, 9 ] )
		self.assert_( self.ynn[:] == [] )
		self.assert_( self.xp0[:] == [ 0 ] )
		self.assert_( self.yp0[:] == [] )
		self.assert_( self.xn0[:] == [ 0 ] )
		self.assert_( self.yn0[:] == [] )
		self.ypp.insert( 0, 11 )
		self.ypn.insert( 0, 11 )
		self.ynp.insert( 0, 11 )
		self.ynn.insert( 0, 11 )
		self.yp0.insert( 0, 11 )
		self.yn0.insert( 0, 11 )
		self.assert_( self.xpp[:] == [ 0, 11, 9 ] )
		self.assert_( self.ypp[:] == [ 11 ] )
		self.assert_( self.xpn[:] == [ 0, 11, 9 ] )
		self.assert_( self.ypn[:] == [ 11 ] )
		self.assert_( self.xnp[:] == [ 0, 11, 9 ] )
		self.assert_( self.ynp[:] == [ 11 ] )
		self.assert_( self.xnn[:] == [ 0, 11, 9 ] )
		self.assert_( self.ynn[:] == [ 11 ] )
		self.assert_( self.xp0[:] == [ 0, 11 ] )
		self.assert_( self.yp0[:] == [ 11 ] )
		self.assert_( self.xn0[:] == [ 0, 11 ] )
		self.assert_( self.yn0[:] == [ 11 ] )








class TestCase_List_Wrap (unittest.TestCase):
	def setUp(self):
		self.layer1 = DocModelLayer()
		self.layer2 = DocModelLayer()
		self.x00 = DMLiteralList()
		self.x00.extend( range( 0, 10 ) )
		self.x01 = DMLiteralList()
		self.x01.extend( range( 0, 10 ) )
		self.x10 = DMLiteralList()
		self.x10.extend( range( 0, 10 ) )
		self.x11 = DMLiteralList()
		self.x11.extend( range( 0, 10 ) )

		self.y00 = DMList( DMListOpWrap( self.layer2, self.x00, [], [] ) )
		self.y01 = DMList( DMListOpWrap( self.layer2, self.x01, [], [ -2, -2 ] ) )
		self.y10 = DMList( DMListOpWrap( self.layer2, self.x10, [ -1, -1 ], [] ) )
		self.y11 = DMList( DMListOpWrap( self.layer2, self.x11, [ -1, -1 ], [ -2, -2 ] ) )

	def tearDown(self):
		del self.layer1
		del self.layer2
		del self.x00
		del self.x01
		del self.x10
		del self.x11
		del self.y00
		del self.y01
		del self.y10
		del self.y11


	def _doTestOp(self, function, args, x00Res, x01Res, x10Res, x11Res):
		function( self.y00, *args )
		function( self.y01, *args )
		function( self.y10, *args )
		function( self.y11, *args )
		self.assert_( self.x00[:] == x00Res, ( self.x00[:], x00Res, self.y00[:], x00Res ) )
		self.assert_( self.y00[:] == x00Res, ( self.x00[:], x00Res, self.y00[:], x00Res ) )
		self.assert_( self.x01[:] == x01Res, ( self.x01[:], x01Res, self.y01[:], x01Res + [ -2,-2 ] ) )
		self.assert_( self.y01[:] == x01Res + [ -2,-2 ], ( self.x01[:], x01Res, self.y01[:], x01Res + [ -2,-2 ] ) )
		self.assert_( self.x10[:] == x10Res, ( self.x10[:], x10Res, self.y10[:], [-1,-1] + x10Res ) )
		self.assert_( self.y10[:] == [ -1,-1 ] + x10Res, ( self.x10[:], x10Res, self.y10[:], [-1,-1] + x10Res ) )
		self.assert_( self.x11[:] == x11Res, ( self.x11[:], x11Res, self.y11[:], [-1,-1] + x11Res + [-2,-2] ) )
		self.assert_( self.y11[:] == [ -1,-1 ] + x11Res + [-2,-2], ( self.x11[:], x11Res, self.y11[:], [-1,-1] + x11Res + [-2,-2] ) )


	def _doTestOp2(self, function, args00, args01, args10, args11, x00Res, x01Res, x10Res, x11Res):
		function( self.y00, *args00 )
		function( self.y01, *args01 )
		function( self.y10, *args10 )
		function( self.y11, *args11 )
		self.assert_( self.x00[:] == x00Res, ( self.x00[:], x00Res, self.y00[:], x00Res ) )
		self.assert_( self.y00[:] == x00Res, ( self.x00[:], x00Res, self.y00[:], x00Res ) )
		self.assert_( self.x01[:] == x01Res, ( self.x01[:], x01Res, self.y01[:], x01Res + [ -2,-2 ] ) )
		self.assert_( self.y01[:] == x01Res + [ -2,-2 ], ( self.x01[:], x01Res, self.y01[:], x01Res + [ -2,-2 ] ) )
		self.assert_( self.x10[:] == x10Res, ( self.x10[:], x10Res, self.y10[:], [-1,-1] + x10Res ) )
		self.assert_( self.y10[:] == [ -1,-1 ] + x10Res, ( self.x10[:], x10Res, self.y10[:], [-1,-1] + x10Res ) )
		self.assert_( self.x11[:] == x11Res, ( self.x11[:], x11Res, self.y11[:], [-1,-1] + x11Res + [-2,-2] ) )
		self.assert_( self.y11[:] == [ -1,-1 ] + x11Res + [-2,-2], ( self.x11[:], x11Res, self.y11[:], [-1,-1] + x11Res + [-2,-2] ) )


	def testFunction(self):
		self.assert_( self.x00[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.x01[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.x10[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.x11[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.y00[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.y01[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -2, -2 ] )
		self.assert_( self.y10[:] == [ -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.y11[:] == [ -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -2, -2 ] )

	def testAppend(self):
		self._doTestOp( DMList.append, ( 11, ),   range(0,10)+[11],   range(0,10),   range(0,10)+[11],   range(0,10) )

	def testExtend(self):
		self._doTestOp( DMList.extend, ( [ 11, 12, 13 ], ),   range(0,10)+[11,12,13],   range(0,10),   range(0,10)+[11,12,13],   range(0,10) )


	def testInsertPB(self):
		self._doTestOp( DMList.insert, ( 1, 21, ),   [ 0,21,1,2,3,4,5,6,7,8,9 ], [ 0,21,1,2,3,4,5,6,7,8,9 ], [ 0,1,2,3,4,5,6,7,8,9 ], [ 0,1,2,3,4,5,6,7,8,9 ]  )

	def testInsertPM(self):
		self._doTestOp( DMList.insert, ( 4, 21, ),   [ 0,1,2,3,21,4,5,6,7,8,9 ], [ 0,1,2,3,21,4,5,6,7,8,9 ], [ 0,1,21,2,3,4,5,6,7,8,9 ], [ 0,1,21,2,3,4,5,6,7,8,9 ]  )

	def testInsertPE(self):
		self._doTestOp2( DMList.insert, (9,21), (11,21), (11,21), (13,21),   [ 0,1,2,3,4,5,6,7,8,21,9 ], [ 0,1,2,3,4,5,6,7,8,9 ], [ 0,1,2,3,4,5,6,7,8,21,9 ], [ 0,1,2,3,4,5,6,7,8,9 ]  )


	def testInsertNB(self):
		self._doTestOp2( DMList.insert, (-9,21), (-11,21), (-11,21), (-13,21),   [ 0,21,1,2,3,4,5,6,7,8,9 ], [ 0,21,1,2,3,4,5,6,7,8,9 ], [ 0,1,2,3,4,5,6,7,8,9 ], [ 0,1,2,3,4,5,6,7,8,9 ]  )

	def testInsertNM(self):
		self._doTestOp( DMList.insert, ( -4, 21, ),   [ 0,1,2,3,4,5,21,6,7,8,9 ], [ 0,1,2,3,4,5,6,7,21,8,9 ], [ 0,1,2,3,4,5,21,6,7,8,9 ], [ 0,1,2,3,4,5,6,7,21,8,9 ]  )

	def testInsertNE(self):
		self._doTestOp( DMList.insert, ( -1, 21, ),   [ 0,1,2,3,4,5,6,7,8,21,9 ], [ 0,1,2,3,4,5,6,7,8,9 ], [ 0,1,2,3,4,5,6,7,8,21,9 ], [ 0,1,2,3,4,5,6,7,8,9 ]  )


	def testRemoveB(self):
		self.y11.remove( -1 )
		self.assert_( self.x11[:] == range( 0, 10 ) )
		self.assert_( self.y11[:] == [-1,-1] + range( 0, 10 ) + [ -2,-2 ] )

	def testRemoveM(self):
		self.y11.remove( 5 )
		self.assert_( self.x11[:] == range( 0, 5 ) + range(6,10)  )
		self.assert_( self.y11[:] == [-1,-1] + range( 0, 5 ) + range(6,10) + [ -2,-2 ] )

	def testRemoveE(self):
		self.y11.remove( -2 )
		self.assert_( self.x11[:] == range( 0, 10 ) )
		self.assert_( self.y11[:] == [-1,-1] + range( 0, 10 ) + [ -2,-2 ] )


	#def testSet(self):
		#self.y[2] = 220
		#self.assert_( self.x[:] == [ 0, 1, 22, 3, 4, 5, 6, 7, 8, 9 ] )
		#self.assert_( self.y[:] == [ 0, 10, 220, 30, 40, 50, 60, 70, 80, 90 ] )
		#self.y[2:4] = [ 220, 230, 240 ]
		#self.assert_( self.x[:] == [ 0, 1, 22, 23, 24, 4, 5, 6, 7, 8, 9 ] )
		#self.assert_( self.y[:] == [ 0, 10, 220, 230, 240, 40, 50, 60, 70, 80, 90 ] )

	#def testDel(self):
		#del self.y[2]
		#self.assert_( self.x[:] == [ 0, 1, 3, 4, 5, 6, 7, 8, 9 ] )
		#self.assert_( self.y[:] == [ 0, 10, 30, 40, 50, 60, 70, 80, 90 ] )
		#del self.y[2:4]
		#self.assert_( self.x[:] == [ 0, 1, 5, 6, 7, 8, 9 ] )
		#self.assert_( self.y[:] == [ 0, 10, 50, 60, 70, 80, 90 ] )





class TestCase_List (unittest.TestCase):
	#def testOpWrap(self):
		#layer1 = DocModelLayer()
		#layer2 = DocModelLayer()
		#x = DMLiteralList()

		#y = DMList( DMListOpWrap( layer2, x, [ -1 ], [ -2 ] ) )
		#self.assert_( y[:] == [ -1, -2 ] )
		#x.append( 4 )
		#self.assert_( x[:] == [ 4 ] )
		#self.assert_( y[:] == [ -1, 4, -2 ] )
		#x.extend( [ 1, 2 ] )
		#self.assert_( x[:] == [ 4, 1, 2 ] )
		#self.assert_( y[:] == [ -1, 4, 1, 2, -2 ] )
		#y[2:3] = [ 6, 7, 8 ]
		#self.assert_( x[:] == [ 4, 6, 7, 8, 2 ] )
		#self.assert_( y[:] == [ -1, 4, 6, 7, 8, 2, -2 ] )
		#y.insertBefore( 7, 13 )
		#self.assert_( x[:] == [ 4, 6, 13, 7, 8, 2 ] )
		#self.assert_( y[:] == [ -1, 4, 6, 13, 7, 8, 2, -2 ] )
		#y.insertAfter( 7, 15 )
		#self.assert_( x[:] == [ 4, 6, 13, 7, 15, 8, 2 ] )
		#self.assert_( y[:] == [ -1, 4, 6, 13, 7, 15, 8, 2, -2 ] )
		#y.remove( 7 )
		#self.assert_( x[:] == [ 4, 6, 13, 15, 8, 2 ] )
		#self.assert_( y[:] == [ -1, 4, 6, 13, 15, 8, 2, -2 ] )
		#y.replace( 13, 17 )
		#self.assert_( x[:] == [ 4, 6, 17, 15, 8, 2 ] )
		#self.assert_( y[:] == [ -1, 4, 6, 17, 15, 8, 2, -2 ] )
		#y.replaceRange( 15, 2, [ 98, 99 ] )
		#self.assert_( x[:] == [ 4, 6, 17, 98, 99 ] )
		#self.assert_( y[:] == [ -1, 4, 6, 17, 98, 99, -2 ] )




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
