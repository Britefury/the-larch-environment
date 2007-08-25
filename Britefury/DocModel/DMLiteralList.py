##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from weakref import WeakKeyDictionary
from copy import copy, deepcopy

from Britefury.FileIO.IOXml import ioObjectFactoryRegister, ioReadObjectFromString, ioWriteObjectAsString

from Britefury.Cell.LiteralCell import LiteralRefCell

from Britefury.DocModel.DocModelLayer import DocModelLayer
from Britefury.DocModel.DMListInterface import DMListInterface






class DMLiteralList (DMListInterface):
	def __init__(self, value=None):
		self._cell = LiteralRefCell()
		if value is None:
			value = []
		self._cell.literalValue = value


	def __readxml__(self, xmlNode):
		xs = []
		if xmlNode.isValid():
			for child in xmlNode.childrenNamed( 'item' ):
				item = child.readObject()
				if item is not None:
					xs.append( item )
		self._cell.literalValue = xs


	def __writexml__(self, xmlNode):
		if xmlNode.isValid():
			xs = self._cell.literalValue
			for x in xs:
				child = xmlNode.addChild( 'item' )
				child.writeObject( x )


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

	def replace(self, a, x):
		v = self._cell.literalValue
		index = v.index( a )
		v[index] =x
		self._cell.literalValue = v

	def replaceRange(self, a, b, xs):
		v = self._cell.literalValue
		start = v.index( a )
		stop = v.index( b )
		v[start:stop+1] = xs
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


	def getDestList(self, layer):
		return layer.getDestList( self )

	def getSrcList(self, layer):
		return layer.getSrcList( self )



	def __copy__(self):
		c = DMLiteralList()
		c._cell.literalValue = self._cell.literalValue
		return c

	def __deepcopy__(self, memo):
		c = DMLiteralList()
		c._cell.literalValue = deepcopy( self._cell.literalValue, memo )
		return c



ioObjectFactoryRegister( 'DMLiteralList', DMLiteralList )




import unittest



class TestCase_LiteralList (unittest.TestCase):
	def testLiteralListCtor(self):
		x = DMLiteralList()



	def testLiteralIter(self):
		x = DMLiteralList()
		x.extend( [ 1, 2, 3 ] )
		q = [ p   for p in x ]
		self.assert_( q == [ 1, 2, 3 ] )


	def testLiteralAppend(self):
		x = DMLiteralList()
		x.append( 1 )
		self.assert_( x[0] == 1 )


	def testLiteralExtend(self):
		x = DMLiteralList()
		x.extend( [ 1, 2, 3 ] )
		self.assert_( x[0] == 1 )
		self.assert_( x[1] == 2 )
		self.assert_( x[2] == 3 )
		self.assert_( x[:] == [ 1, 2, 3 ] )


	def testLiteralInsertBefore(self):
		x = DMLiteralList()
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.insertBefore( 3, 12 )
		self.assert_( x[:] == [ 1, 2, 12, 3, 4, 5 ] )


	def testLiteralInsertAfter(self):
		x = DMLiteralList()
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.insertAfter( 3, 12 )
		self.assert_( x[:] == [ 1, 2, 3, 12, 4, 5 ] )


	def testLiteralRemove(self):
		x = DMLiteralList()
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.remove( 3 )
		self.assert_( x[:] == [ 1, 2, 4, 5 ] )


	def testLiteralReplace(self):
		x = DMLiteralList()
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.replace( 3, 8 )
		self.assert_( x[:] == [ 1, 2, 8, 4, 5 ] )


	def testLiteralReplaceRange(self):
		x = DMLiteralList()
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.replaceRange( 2, 4, [ 7, 8, 9 ] )
		self.assert_( x[:] == [ 1, 7, 8, 9, 5 ] )


	def testLiteralSet(self):
		x = DMLiteralList()
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x[4] = 12
		self.assert_( x[:] == [ 1, 2, 3, 4, 12 ] )
		x[1:3] = [ 20, 21, 22 ]
		self.assert_( x[:] == [ 1, 20, 21, 22, 4, 12 ] )





	def testIOXml(self):
		xxa = DMLiteralList( [ 'a', 'b', 'c' ] )

		x = DMLiteralList( [ 1, 2, 3 ] )
		xx1 = DMLiteralList( [ 'plus2', 5, 6, 7 ] )
		xx1.append( xxa )
		x.append( xx1 )
		xx2 = DMLiteralList( [ 'times2', 11, 12, 13 ] )
		xx2.append( xxa )
		x.append( xx2 )


		s = ioWriteObjectAsString( x )
		y = ioReadObjectFromString( s )

		self.assert_( y[0:3] == [ 1, 2, 3 ] )
		self.assert_( y[3][:-1] == [ 'plus2', 5, 6, 7 ] )
		self.assert_( y[4][:-1] == [ 'times2', 11, 12, 13 ] )
		self.assert_( y[3][-1] is y[4][-1] )
		self.assert_( y[3][-1][:] == [ 'a', 'b', 'c' ] )
		self.assert_( y[4][-1][:] == [ 'a', 'b', 'c' ] )






1
if __name__ == '__main__':
	unittest.main()
