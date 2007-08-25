##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from weakref import WeakKeyDictionary
from copy import copy, deepcopy

from Britefury.Cell.LiteralCell import LiteralRefCell

from Britefury.DocModel.DocModelLayer import DocModelLayer
from Britefury.DocModel.DMListInterface import DMListInterface






class DMLiteralList (DMListInterface):
	def __init__(self, layer):
		self._cell = LiteralRefCell()
		self._layer = layer
		self._cell.literalValue = []


	def append(self, x):
		v = self._cell.literalValue
		v.append( x )
		self._cell.literalValue = v

	def extend(self, xs):
		v = self._cell.literalValue
		v.extend( xs )
		self._cell.literalValue = v

	def insertBefore(self, before, x):
		v = self._cell.literalValue
		i = v.index( before )
		v.insert( i, x )
		self._cell.literalValue = v

	def insertAfter(self, after, x):
		v = self._cell.literalValue
		i = v.index( after )
		v.insert( i + 1, x )
		self._cell.literalValue = v

	def remove(self, x):
		v = self._cell.literalValue
		v.remove( x )
		self._cell.literalValue = v

	def __setitem__(self, i, x):
		v = self._cell.literalValue
		v[i] = x
		self._cell.literalValue = v


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
		c = DMLiteralList( self._layer )
		c._cell.literalValue = self._cell.literalValue
		return c

	def __deepcopy__(self, memo):
		c = DMLiteralList( self._layer )
		c._cell.literalValue = deepcopy( self._cell.literalValue, memo )
		return c




import unittest



class TestCase_LiteralList (unittest.TestCase):
	def testLiteralListCtor(self):
		layer = DocModelLayer()
		x = DMLiteralList( layer )



	def testLiteralIter(self):
		layer = DocModelLayer()
		x = DMLiteralList( layer )
		x.extend( [ 1, 2, 3 ] )
		q = [ p   for p in x ]
		self.assert_( q == [ 1, 2, 3 ] )


	def testLiteralAppend(self):
		layer = DocModelLayer()
		x = DMLiteralList( layer )
		x.append( 1 )
		self.assert_( x[0] == 1 )


	def testLiteralExtend(self):
		layer = DocModelLayer()
		x = DMLiteralList( layer )
		x.extend( [ 1, 2, 3 ] )
		self.assert_( x[0] == 1 )
		self.assert_( x[1] == 2 )
		self.assert_( x[2] == 3 )
		self.assert_( x[:] == [ 1, 2, 3 ] )


	def testLiteralInsertBefore(self):
		layer = DocModelLayer()
		x = DMLiteralList( layer )
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.insertBefore( 3, 12 )
		self.assert_( x[:] == [ 1, 2, 12, 3, 4, 5 ] )


	def testLiteralInsertAfter(self):
		layer = DocModelLayer()
		x = DMLiteralList( layer )
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.insertAfter( 3, 12 )
		self.assert_( x[:] == [ 1, 2, 3, 12, 4, 5 ] )


	def testLiteralRemove(self):
		layer = DocModelLayer()
		x = DMLiteralList( layer )
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.remove( 3 )
		self.assert_( x[:] == [ 1, 2, 4, 5 ] )


	def testLiteralSet(self):
		layer = DocModelLayer()
		x = DMLiteralList( layer )
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x[4] = 12
		self.assert_( x[:] == [ 1, 2, 3, 4, 12 ] )
		x[1:3] = [ 20, 21, 22 ]
		self.assert_( x[:] == [ 1, 20, 21, 22, 4, 12 ] )







1
if __name__ == '__main__':
	unittest.main()
