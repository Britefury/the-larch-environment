##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Cell.Cell import RefCell

from Britefury.DocModel.DMListOperator import DMListOpMap, DMListOpSlice, DMListOpWrap






class DMListEvaluationContext (object):
	pass


class DMList (object):
	def __init__(self, evaluationContext, op=None):
		self._op = op
		self._cell = RefCell()
		self._evaluationContext = evaluationContext

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


	def _f_getEvaluationContext(self):
		return self._evaluationContext





import unittest



class TestCase_List (unittest.TestCase):
	def testListCtor(self):
		x = DMList( DMListEvaluationContext() )



	def testLiteralIter(self):
		x = DMList( DMListEvaluationContext() )
		x.extend( [ 1, 2, 3 ] )
		q = [ p   for p in x ]
		self.assert_( q == [ 1, 2, 3 ] )


	def testLiteralAppend(self):
		x = DMList( DMListEvaluationContext() )
		x.append( 1 )
		self.assert_( x[0] == 1 )


	def testLiteralExtend(self):
		x = DMList( DMListEvaluationContext() )
		x.extend( [ 1, 2, 3 ] )
		self.assert_( x[0] == 1 )
		self.assert_( x[1] == 2 )
		self.assert_( x[2] == 3 )
		self.assert_( x[:] == [ 1, 2, 3 ] )


	def testLiteralInsertBefore(self):
		x = DMList( DMListEvaluationContext() )
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.insertBefore( 3, 12 )
		self.assert_( x[:] == [ 1, 2, 12, 3, 4, 5 ] )


	def testLiteralInsertAfter(self):
		x = DMList( DMListEvaluationContext() )
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.insertAfter( 3, 12 )
		self.assert_( x[:] == [ 1, 2, 3, 12, 4, 5 ] )


	def testLiteralRemove(self):
		x = DMList( DMListEvaluationContext() )
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x.remove( 3 )
		self.assert_( x[:] == [ 1, 2, 4, 5 ] )


	def testLiteralSet(self):
		x = DMList( DMListEvaluationContext() )
		x.extend( [ 1, 2, 3, 4, 5 ] )
		self.assert_( x[:] == [ 1, 2, 3, 4, 5 ] )
		x[4] = 12
		self.assert_( x[:] == [ 1, 2, 3, 4, 12 ] )
		x[1:3] = [ 20, 21, 22 ]
		self.assert_( x[:] == [ 1, 20, 21, 22, 4, 12 ] )


	def testOpMap(self):
		x = DMList( DMListEvaluationContext() )
		x.extend( [ 1, 2, 3 ] )

		y = DMList( DMListEvaluationContext(), DMListOpMap( x, lambda x: x * 10, lambda x: x / 10 ) )
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
		x = DMList( DMListEvaluationContext() )
		x.extend( [ 1, 2, 3 ] )

		y = DMList( DMListEvaluationContext(), DMListOpSlice( x, 1, -1 ) )
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
		x = DMList( DMListEvaluationContext() )

		y = DMList( DMListEvaluationContext(), DMListOpWrap( x, [ -1 ], [ -2 ] ) )
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






if __name__ == '__main__':
	unittest.main()
