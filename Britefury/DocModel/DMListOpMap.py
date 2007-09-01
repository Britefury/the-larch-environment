##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

from Britefury.DocModel.DocModelLayer import DocModelLayer
from Britefury.DocModel.DMListOperator import DMListOperator, TestCase_DMListOperator_base
from Britefury.DocModel.DMLiteralList import DMLiteralList
from Britefury.DocModel.DMList import DMList




class DMListOpMap (DMListOperator):
	def __init__(self, layer, src, f, invF):
		super( DMListOpMap, self ).__init__( layer )
		self._src = src
		self._f = f
		self._invF = invF


	def _p_dest(self, x):
		try:
			getDestList = x.getDestList
		except AttributeError:
			return self._f( x )
		else:
			return getDestList( self._layer )


	def _p_src(self, x):
		try:
			getSrcList = x.getSrcList
		except AttributeError:
			return self._invF( x )
		else:
			return getSrcList( self._layer )


	def evaluate(self):
		return [ self._p_dest( x )   for x in self._src ]


	def append(self, x):
		self._src.append( self._p_src( x ) )

	def extend(self, xs):
		self._src.extend( [ self._p_src( p )   for p in xs ] )

	def insert(self, i, x):
		self._src.insert( i, self._p_src( x ) )

	def insertBefore(self, before, x):
		self._src.insertBefore( self._p_src( before ),  self._p_src( x ) )

	def insertAfter(self, after, x):
		self._src.insertAfter( self._p_src( after ),  self._p_src( x ) )

	def remove(self, x):
		self._src.remove( self._p_src( x ) )

	def replace(self, a, x):
		self._src.replace( self._p_src( a ), self._p_src( x ) )

	def replaceRange(self, a, b, xs):
		self._src.replaceRange( self._p_src( a ), self._p_src( b ), [ self._p_src( x )  for x in xs ] )


	def __setitem__(self, i, x):
		if isinstance( i, slice ):
			self._src[i] = [ self._p_src( p )   for p in x ]
		else:
			self._src[i] = self._p_src( x )


	def __delitem__(self, i):
		del self._src[i]

	def __len__(self):
		return len( self._src )







class TestCase_DMListOpMap (TestCase_DMListOperator_base):
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


	def _p_makeLayerList(self, layer, literalList):
		return DMList( DMListOpMap( layer, literalList, lambda x: x * 10, lambda x: x / 10 ) )

	def _p_expectedValue(self, xs):
		return xs

	def _p_expectedLiteralValue(self, xs):
		return [ x / 10   for x in xs ]


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
		for i in xrange( -12, 13 ):
			self._p_testCase( lambda xs: xs.insert( i, 340 ), 'insert %d' % (i, ) )

	def testRemove(self):
		for i in xrange( -120, 130, 10 ):
			self._p_testCase( lambda xs: xs.remove( i ), 'remove %d' % (i, ) )

	def testSetSingle(self):
		for i in xrange( -12, 13 ):
			def _set(xs):
				xs[i] = 50
			self._p_testCase( _set, 'set %d' % (i, ) )

	def testSetSlice(self):
		for i in xrange( -12, 13 ):
			for j in xrange( -12, 13 ):
				def _set(xs):
					xs[i:j] = range( 500, 550, 10 )
				def _set2(xs):
					xs[i:j] = range( 500, 750, 10 )
				self._p_testCase( _set, 'set %d:%d' % (i,j, ) )
				self._p_testCase( _set2, 'set2 %d:%d' % (i,j, ) )

	def testSetSliceStep(self):
		for i in xrange( -12, 13 ):
			for j in xrange( -12, 13 ):
				for k in xrange( -3, 3 ):
					def _set(xs):
						xs[i:j:k] = range( 500, 550, 10 )
					def _set2(xs):
						xs[i:j:k] = range( 500, 750, 10 )
					self._p_testCase( _set, 'set %d:%d:%d' % (i,j,k, ) )
					self._p_testCase( _set2, 'set2 %d:%d:%d' % (i,j,k, ) )

	def testDelSingle(self):
		for i in xrange( -12, 13 ):
			def _del(xs):
				del xs[i]
			self._p_testCase( _del, 'del %d' % (i, ) )

	def testDelSlice(self):
		for i in xrange( -12, 13 ):
			for j in xrange( -2, 13 ):
				def _del(xs):
					del xs[i:j]
				self._p_testCase( _del, 'del %d:%d' % (i,j, ) )

	def testDelSliceStep(self):
		for i in xrange( -12, 13 ):
			for j in xrange( -12, 13 ):
				for k in xrange( -3, 3 ):
					def _del(xs):
						del xs[i:j:k]
					self._p_testCase( _del, 'del %d:%d:%d' % (i,j,k, ) )



if __name__ == '__main__':
	import unittest
	unittest.main()
