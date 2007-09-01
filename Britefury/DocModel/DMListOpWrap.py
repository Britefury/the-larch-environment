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




class DMListOpWrap (DMListOperator):
	def __init__(self, layer, src, prefix=[], suffix=[]):
		super( DMListOpWrap, self ).__init__( layer )
		assert isinstance( prefix, list )
		assert isinstance( suffix, list )
		self._src = src
		self._pre = prefix
		self._suf = suffix
		self._prefixLen = len( prefix )
		self._suffixLen = len( suffix )


	def evaluate(self):
		return self._pre + [ self._p_dest( x )   for x in self._src ] + self._suf


	def append(self, x):
		if self._suffixLen == 0:
			self._src.append( self._p_src( x ) )

	def extend(self, xs):
		if self._suffixLen == 0:
			self._src.extend( [ self._p_src( x )   for x in xs ] )

	def insert(self, i, x):
		srcLen = len( self._src )
		if i < 0:
			relativeI = i + self._suffixLen
			if relativeI == 0:
				self._src.insert( srcLen, self._p_src( x ) )
			elif relativeI < 0:
				if relativeI < -srcLen:
					if self._prefixLen == 0:
						relativeI = -srcLen
					else:
						relativeI = None
				if relativeI is not None:
					self._src.insert( relativeI, self._p_src( x ) )
		else:
			relativeI = i - self._prefixLen
			if relativeI >= 0:
				if relativeI > srcLen:
					if self._suffixLen == 0:
						relativeI = srcLen
					else:
						relativeI = None
				if relativeI is not None:
					self._src.insert( relativeI, self._p_src( x ) )

	def remove(self, x):
		if x not in self._pre  and  x not in self._suf:
			self._src.remove( self._p_src( x ) )

	def __setitem__(self, i, x):
		if isinstance( i, slice ):
			srcLen = len( self._src )
			selfLen = self._prefixLen + srcLen + self._suffixLen
			start, stop, step = i.indices( selfLen )

			numItems = ( stop - start )  /  step

			start -= self._prefixLen
			stop -= self._prefixLen

			newItems = ( stop - start )  /  step
			diff = newItems - numItems

			if diff != 0:
				if step == 1:
					self._src[start:stop] = [ self._p_src( p )   for p in x[:diff] ]
				else:
					self._src[start:stop:step] = [ self._p_src( p )   for p in x[:diff] ]
			else:
				if step == 1:
					self._src[start:stop] = [ self._p_src( p )   for p in x ]
				else:
					self._src[start:stop:step] = [ self._p_src( p )   for p in x ]
		else:
			selfLen = len( self )
			if i < -selfLen  or  i >= selfLen:
				raise IndexError, 'index out of bounds'
			if i < 0:
				ii = i + self._suffixLen
				if ii < 0  and  ii >= -len( self._src ):
					self._src[ii] = self._p_src( x )
			else:
				ii = i - self._prefixLen
				if ii >= 0  and  ii < len( self._src ):
					self._src[ii] = self._p_src( x )



	def __delitem__(self, i):
		if isinstance( i, slice ):
			start, stop, step = i.indices( len( self ) )

			numItems = ( stop - start )  /  step

			start -= self._prefixLen
			stop -= self._prefixLen

			del self._src[start:stop:step]
		else:
			del self._src[i]



	def __len__(self):
		return self._prefixLen + len( self._src ) + self._suffixLen







class TestCase_DMListOpWrap (TestCase_DMListOperator_base):
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


	def _p_makeLayerListp00(self, layer, literalList):
		return DMList( DMListOpWrap( layer, literalList, [], [] ) )

	def _p_makeLayerListp01(self, layer, literalList):
		return DMList( DMListOpWrap( layer, literalList, [], [ -2, -2 ] ) )

	def _p_makeLayerListp10(self, layer, literalList):
		return DMList( DMListOpWrap( layer, literalList, [ -1, -1 ], [] ) )

	def _p_makeLayerListp11(self, layer, literalList):
		return DMList( DMListOpWrap( layer, literalList, [ -1, -1 ], [ -2, -2 ] ) )


	def _p_expectedValueStrip(self, xs):
		try:
			end = xs.index( -2 )
		except ValueError:
			end = None

		try:
			start = len( xs )  -  list( reversed( xs ) ).index( -1 )
		except ValueError:
			start = None

		return xs[start:end]

	def _p_expectedValue00(self, xs):
		return self._p_expectedValueStrip( xs )

	def _p_expectedValue01(self, xs):
		return self._p_expectedValueStrip( xs ) + [ -2, -2 ]

	def _p_expectedValue10(self, xs):
		return [ -1, -1 ] + self._p_expectedValueStrip( xs )

	def _p_expectedValue11(self, xs):
		return [ -1, -1 ] + self._p_expectedValueStrip( xs ) + [ -2, -2 ]

	def _p_expectedLiteralValue(self, xs):
		return self._p_expectedValueStrip( xs )


	def _wrapTestCase(self, operationFunc, opDescription):
		self._p_testCaseParam( operationFunc, opDescription, self._p_makeLayerListp00, self._p_expectedValue00, self._p_expectedLiteralValue )
		self._p_testCaseParam( operationFunc, opDescription, self._p_makeLayerListp01, self._p_expectedValue01, self._p_expectedLiteralValue )
		self._p_testCaseParam( operationFunc, opDescription, self._p_makeLayerListp10, self._p_expectedValue10, self._p_expectedLiteralValue )
		self._p_testCaseParam( operationFunc, opDescription, self._p_makeLayerListp11, self._p_expectedValue11, self._p_expectedLiteralValue )


	def testFunction(self):
		def _f(xs):
			pass
		self._wrapTestCase( _f, 'function' )

	def testAppend(self):
		def _f(xs):
			xs.append( 11 )
		self._wrapTestCase( _f, 'append' )

	def testExtend(self):
		def _f(xs):
			xs.extend( [ 11, 12, 13 ] )
		self._wrapTestCase( _f, 'extend' )


	def testInsert(self):
		for i in xrange( -14, 14 ):
			def _f(xs):
				xs.insert( i, 50 )
			self._wrapTestCase( _f, 'insert %d' % (i, ) )

	def testRemove(self):
		for i in xrange( -3, 11 ):
			def _f(xs):
				xs.remove( i )
			self._wrapTestCase( _f, 'remove %d' % (i, ) )


	def testSet(self):
		for i in xrange( -14, 14 ):
			def _f(xs):
				xs[i] = 50
			self._wrapTestCase( _f, 'set %d' % (i, ) )

	def testSetB(self):
		self._doTestOp( DMList.__setitem__,  ( 1, 21 ),   [0,21]+range(2,10),   [0,21]+range(2,10),   range(0,10),   range(0,10) )

	def testSetb(self):
		self._doTestOp2( DMList.__setitem__,  ( -9, 21 ), ( -11, 21 ), ( -11, 21 ), ( -13, 21 ),   [0,21]+range(2,10),   [0,21]+range(2,10),   range(0,10),   range(0,10) )

	def testSetM(self):
		res0x = range(0,5) + [21] + range(6,10)
		res1x = range(0,3) + [21] + range(4,10)
		self._doTestOp( DMList.__setitem__,  ( 5, 21 ),   res0x, res0x, res1x, res1x )

	def testSetm(self):
		res = range(0,5) + [21] + range(6,10)
		self._doTestOp2( DMList.__setitem__,  ( -5, 21 ), ( -7, 21 ), ( -5, 21 ), ( -7, 21 ),   res, res, res, res )

	def testSetE(self):
		self._doTestOp2( DMList.__setitem__,  ( 8, 21 ), ( 10, 21 ), ( 10, 21 ), ( 12, 21 ),   range(0,8) + [21,9],   range(0,10),  range(0,8) + [21,9],  range(0,10) )

	def testSete(self):
		self._doTestOp( DMList.__setitem__,  ( -2, 21 ),   range(0,8) + [21,9],   range(0,10),  range(0,8) + [21,9],  range(0,10) )

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



if __name__ == '__main__':
	import unittest
	unittest.main()
