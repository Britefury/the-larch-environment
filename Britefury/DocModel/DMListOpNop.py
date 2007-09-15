##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

from Britefury.DocModel.DocModelLayer import DocModelLayer
from Britefury.DocModel.DMListOperator import DMListOperator, TestCase_DMListOperator_base
from Britefury.DocModel.DMList import DMList
from Britefury.DocModel.DMVirtualList import DMVirtualList




class DMListOpNop (DMListOperator):
	__slots__ = [ '_src' ]


	def __init__(self, layer, src):
		super( DMListOpNop, self ).__init__( layer )
		self._src = src


	def evaluate(self):
		return [ self._p_dest( x )   for x in self._src ]



	def append(self, x):
		self._src.append( self._p_src( x ) )


	def extend(self, xs):
		self._src.extend( [ self._p_src( x )   for x in xs ] )


	def insert(self, i, x):
		self._src.insert( i, self._p_src( x ) )


	def remove(self, x):
		self._src.remove( self._p_src( x ) )


	def __setitem__(self, i, x):
		if isinstance( i, slice ):
			self._src[i] = [ self._p_src( p )   for p in x ]
		else:
			self._src[i] = self._p_src( x )


	def __delitem__(self, i):
		del self._src[i]



	def __len__(self):
		return len( self._src )







class TestCase_DMListOpNop (TestCase_DMListOperator_base):
	def setUp(self):
		self.layer1 = DocModelLayer()
		self.layer2 = DocModelLayer()
		self.x = DMList()
		self.x.extend( range( 0, 10 ) )

		self.y = DMVirtualList( DMListOpNop( self.layer2, self.x ) )


	def tearDown(self):
		del self.layer1
		del self.layer2
		del self.x
		del self.y



	def _p_expectedValue(self, xs):
		return xs

	def _p_expectedLiteralValue(self, xs):
		return xs


	def _p_testCase(self, operationFunc, opDescription):
		self.x[:] = range( 0, 10 )
		return self._p_testCaseCheckInPlace( self.x, self.y, operationFunc, opDescription )



	def testFunction(self):
		self.assert_( self.x[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )
		self.assert_( self.y[:] == [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] )

	def testAppend(self):
		def _append(xs):
			xs.append( 11 )
		self._p_testCase( _append, 'append' )

	def testExtend(self):
		def _extend(xs):
			xs.extend( [ 11, 12, 13 ] )
		self._p_testCase( _extend, 'extend' )

	def testInsert(self):
		for i in xrange( -12, 13 ):
			def _insert(xs):
				xs.insert( i, 34 )
			self._p_testCase( _insert, 'insert %d' % (i, ) )

	def testRemove(self):
		for i in xrange( -12, 13 ):
			def _remove(xs):
				xs.remove( i )
			self._p_testCase( _remove, 'remove %d' % (i, ) )

	def testSetSingle(self):
		for i in xrange( -12, 13 ):
			def _set(xs):
				xs[i] = 55
			self._p_testCase( _set, 'set %d' % (i, ) )

	def testSetRange(self):
		for i in xrange( -12, 13 ):
			for j in xrange( -12, 13 ):
				def _set(xs):
					xs[i:j] = range( 50, 55 )
				def _set2(xs):
					xs[i:j] = range( 50, 75 )
				self._p_testCase( _set, 'set %d:%d' % (i,j, ) )
				self._p_testCase( _set2, 'set2 %d:%d' % (i,j, ) )

	def testSetRangeStep(self):
		for i in xrange( -12, 13 ):
			for j in xrange( -12, 13 ):
				for k in xrange( -3, 3 ):
					def _set(xs):
						xs[i:j:k] = range( 50, 55 )
					def _set2(xs):
						xs[i:j:k] = range( 50, 75 )
					self._p_testCase( _set, 'set %d:%d:%d' % (i,j,k ) )
					self._p_testCase( _set2, 'set2 %d:%d:%d' % (i,j,k ) )

	def testDelSingle(self):
		for i in xrange( -12, 13 ):
			def _del(xs):
				del xs[i]
			self._p_testCase( _del, 'del %d' % (i, ) )

	def testDelRange(self):
		for i in xrange( -12, 13 ):
			for j in xrange( -2, 13 ):
				def _del(xs):
					del xs[i:j]
				self._p_testCase( _del, 'del %d:%d' % (i,j, ) )

	def testDelRangeStep(self):
		for i in xrange( -12, 13 ):
			for j in xrange( -12, 13 ):
				for k in xrange( -3, 3 ):
					def _del(xs):
						del xs[i:j:k]
					self._p_testCase( _del, 'del %d:%d:%d' % (i,j,k, ) )



if __name__ == '__main__':
	import unittest
	unittest.main()
