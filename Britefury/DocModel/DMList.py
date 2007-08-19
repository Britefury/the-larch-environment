##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Cell.Cell import RefCell

from Britefury.DocModel.DMListOperator import DMListOpMap






class DMList (object):
	def __init__(self, op=None):
		self._op = op
		self._cell = RefCell()

		if op is None:
			self._cell.literalValue = []
		else:
			self._cell.function = op


	def append(self, x):
		if self._op is None:
			v = self._cell.literalValue
			v.append( x )
			self._cell.literalValue = v
		else:
			self._op.append( x )

	def extend(self, x):
		if self._op is None:
			v = self._cell.literalValue
			v.extend( x )
			self._cell.literalValue = v
		else:
			self._op.extend( x )


	def __setitem__(self, i, x):
		if self._op is None:
			v = self._cell.literalValue
			v[i] = x
			self._cell.literalValue = v
		else:
			self._op[i] = x


	def __getitem__(self, i):
		return self._cell.value[i]

	def __iter__(self):
		return iter( self._cell.value )





import unittest



class TestCase_List (unittest.TestCase):
	def testListCtor(self):
		x = DMList()



	def testLiteralAppend(self):
		x = DMList()
		x.append( 1 )
		self.assert_( x[0] == 1 )


	def testLiteralAppend(self):
		x = DMList()
		x.extend( [ 1, 2, 3 ] )
		self.assert_( x[0] == 1 )
		self.assert_( x[1] == 2 )
		self.assert_( x[2] == 3 )
		self.assert_( x[:] == [ 1, 2, 3 ] )


	def testLiteralIter(self):
		x = DMList()
		x.extend( [ 1, 2, 3 ] )
		q = [ p   for p in x ]
		self.assert_( q == [ 1, 2, 3 ] )


	def testLiteralSet(self):
		x = DMList()
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x[4] = 12
		self.assert_( x[:] == [ 1, 2, 3, 4, 12 ] )
		x[1:3] = [ 20, 21, 22 ]
		self.assert_( x[:] == [ 1, 20, 21, 22, 4, 12 ] )


	def testMap(self):
		x = DMList()
		x.extend( [ 1, 2, 3 ] )

		y = DMList( DMListOpMap( x, lambda x: x * 10, lambda x: x / 10 ) )
		self.assert_( y[0] == 10 )
		self.assert_( y[:] == [ 10, 20, 30 ] )
		y.append( 40 )
		self.assert_( y[:] == [ 10, 20, 30, 40 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4 ] )
		y[1:3] = [ 100, 200, 300, 400 ]
		self.assert_( y[:] == [ 10, 100, 200, 300, 400, 40 ] )
		self.assert_( x[:] == [ 1, 10, 20, 30, 40, 4 ] )








if __name__ == '__main__':
	unittest.main()
