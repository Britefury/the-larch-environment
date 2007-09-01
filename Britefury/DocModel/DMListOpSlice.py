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




class DMListOpSlice (DMListOperator):
	def __init__(self, layer, src, start=None, stop=None):
		super( DMListOpSlice, self ).__init__( layer )
		self._src = src
		if start is None:
			start = 0
		self._start = start
		self._stop = stop


	def evaluate(self):
		return [ self._p_dest( x )   for x in self._src[self._start:self._stop] ]


	def append(self, x):
		if self._stop is None:
			self._src.append( self._p_src( x ) )
			if self._start < 0:
				self._start -= 1
		else:
			self._src.insert( self._stop, self._p_src( x ) )
			# Extend ranges if necessary
			if self._start < 0:
				self._start -= 1
			if self._stop >= 0  and  self._stop is not None:
				self._stop += 1


	def extend(self, xs):
		if self._stop is None:
			self._src.extend( [ self._p_src( x )   for x in xs ] )
			if self._start < 0:
				self._start -= len( xs )
		else:
			i = self._stop
			for x in xs:
				self._src.insert( i, self._p_src( x ) )
				if i > 0:
					i += 1
			if self._start < 0:
				self._start -= len( xs )
			if self._stop >= 0  and  self._stop is not None:
				self._stop += len( xs )

	def insert(self, i, x):
		if i < 0:
			i = max( i, -len( self ) )
			if self._stop is None:
				self._src.insert( len( self._src ) + i, x )
			else:
				self._src.insert( i + self._stop, x )
		else:
			start = self._start
			if start < 0:
				start += len( self._src )
			ii = min( i, len( self ) )
			self._src.insert( ii + start, x )
		if self._start < 0:
			self._start -= 1
		if self._stop >= 0  and  self._stop is not None:
			self._stop += 1

	def insertBefore(self, before, x):
		self._src.insertBefore( self._p_src( before ), self._p_src( x ) )

	def insertAfter(self, after, x):
		self._src.insertAfter( self._p_src( after ), self._p_src( x ) )

	def remove(self, x):
		self._src.remove( self._p_src( x ) )
		if self._start < 0:
			self._start += 1
			if self._start == 0:
				self._start = len( self._src )
		if self._stop >= 0  and  self._stop is not None:
			self._stop -= 1

	def replace(self, a, x):
		self._src.replace( self._p_src( a ), self._p_src( x ) )

	def replaceRange(self, a, b, xs):
		self._src.replaceRange( self._p_src( a ), self._p_src( b ), [ self._p_src( x )  for x in xs ] )

	def __setitem__(self, i, x):
		if isinstance( i, slice ):
			srcLen = len( self._src )
			srcStart, srcStop, srcStep = slice( self._start, self._stop ).indices( srcLen )
			myLen = srcStop - srcStart
			start, stop, step = i.indices( myLen )

			start += srcStart
			stop += srcStart

			if step == 1:
				self._src[start:stop] = [ self._p_src( p )   for p in x ]
			else:
				self._src[start:stop:step] = [ self._p_src( p )   for p in x ]
			newLen = len( self._src )
			changeInLength = newLen - srcLen
			if self._start < 0:
				self._start -= changeInLength
				if self._start == 0:
					self._start = len( self._src )
			if self._stop >= 0:
				self._stop += changeInLength
		else:
			selfLen = len( self )
			if i < -selfLen  or  i >= selfLen:
				raise IndexError, 'index out of range'
			if i < 0:
				stop = self._stop
				if stop is None:
					stop = len( self._src )
				self._src[stop+i] = self._p_src( x )
			else:
				self._src[self._start+i] = self._p_src( x )


	def __delitem__(self, i):
		if isinstance( i, slice ):
			srcLen = len( self._src )
			srcStart, srcStop, srcStep = slice( self._start, self._stop ).indices( srcLen )
			myLen = srcStop - srcStart
			start, stop, step = i.indices( myLen )

			start += srcStart
			stop += srcStart

			del self._src[start:stop:step]
			newLen = len( self._src )
			changeInLength = newLen - srcLen
			if self._start < 0:
				self._start -= changeInLength
				if self._start == 0:
					self._start = len( self._src )
			if self._stop >= 0:
				self._stop += changeInLength
		else:
			if i < 0:
				stop = self._stop
				if stop is None:
					stop = len( self._src )
				del self._src[stop+i]
			else:
				del self._src[self._start+i]
			if self._start < 0:
				self._start += 1
				if self._start == 0:
					self._start = len( self._src )
			if self._stop >= 0  and  self._stop is not None:
				self._stop -= 1



	def __len__(self):
		srcLen = len( self._src )
		start, stop, step = slice( self._start, self._stop ).indices( srcLen )
		return stop - start








class TestCase_List_Slice (TestCase_DMListOperator_base):
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


	def _p_makeLayerListpp(self, layer, literalList):
		return DMList( DMListOpSlice( layer, literalList, 1, 9 ) )

	def _p_makeLayerListpn(self, layer, literalList):
		return DMList( DMListOpSlice( layer, literalList, 1, -1 ) )

	def _p_makeLayerListnp(self, layer, literalList):
		return DMList( DMListOpSlice( layer, literalList, -9, 9 ) )

	def _p_makeLayerListnn(self, layer, literalList):
		return DMList( DMListOpSlice( layer, literalList, -9, -1 ) )

	def _p_makeLayerListp0(self, layer, literalList):
		return DMList( DMListOpSlice( layer, literalList, 1 ) )

	def _p_makeLayerListn0(self, layer, literalList):
		return DMList( DMListOpSlice( layer, literalList, -9 ) )


	def _p_expectedValue(self, xs):
		return xs

	def _p_expectedLiteralValue(self, xs):
		return [ 0 ] + xs + [ 9 ]

	def _p_expectedLiteralValue0(self, xs):
		return [ 0 ] + xs


	def _sliceTestCase(self, operationFunc, opDescription):
		self._p_testCaseParam( operationFunc, opDescription, self._p_makeLayerListpp, self._p_expectedValue, self._p_expectedLiteralValue )
		self._p_testCaseParam( operationFunc, opDescription, self._p_makeLayerListpn, self._p_expectedValue, self._p_expectedLiteralValue )
		self._p_testCaseParam( operationFunc, opDescription, self._p_makeLayerListnp, self._p_expectedValue, self._p_expectedLiteralValue )
		self._p_testCaseParam( operationFunc, opDescription, self._p_makeLayerListnn, self._p_expectedValue, self._p_expectedLiteralValue )
		self._p_testCaseParam( operationFunc, opDescription, self._p_makeLayerListp0, self._p_expectedValue, self._p_expectedLiteralValue0 )
		self._p_testCaseParam( operationFunc, opDescription, self._p_makeLayerListn0, self._p_expectedValue, self._p_expectedLiteralValue0 )


	def testFunction(self):
		self._sliceTestCase( lambda x: x, 'function' )

	def testAppend(self):
		def _append(xs):
			xs.append( 11 )
		self._sliceTestCase( _append, 'append' )

	def testExtend(self):
		def _extend(xs):
			xs.extend( [11,12,13] )
		self._sliceTestCase( _extend, 'extend' )

	def testInsert(self):
		for i in xrange( -11, 11 ):
			def _insert(xs):
				xs.insert( i, 50 )
			self._sliceTestCase( _insert, 'insert %d' % (i, ) )

	def testRemove(self):
		for i in xrange( -11, 11 ):
			def _remove(xs):
				xs.remove( i, 50 )
			self._sliceTestCase( _remove, 'remove %d' % (i, ) )

	def testSet(self):
		for i in xrange( -11, 11 ):
			def _set(xs):
				xs[i] = 50
			self._sliceTestCase( _set, 'set %d' % (i, ) )

	def testSetSlice(self):
		for i in xrange( -11, 11 ):
			for j in xrange( -11, 11 ):
				def _set(xs):
					xs[i:j] = range( 50, 54 )
				self._sliceTestCase( _set, 'set %d:%d' % (i,j, ) )

	def testSetSliceStep(self):
		for i in xrange( -11, 11 ):
			for j in xrange( -11, 11 ):
				for k in xrange( -3, 3 ):
					def _set(xs):
						xs[i:j:k] = range( 50, 54 )
					self._sliceTestCase( _set, 'set %d:%d:%d' % (i,j,k, ) )




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

	def testSetSpecial1(self):
		self._testSet( (2,6), range(10,20),   [ 0,1,2,10,11,12,13,14,15,16,17,18,19,7,8,9 ], [ 1,2,10,11,12,13,14,15,16,17,18,19,7,8 ],   [ 0,1,2,10,11,12,13,14,15,16,17,18,19,7,8,9 ], [ 1,2,10,11,12,13,14,15,16,17,18,19,7,8,9 ] )

	def testSetSpecial2(self):
		self._testSet( (2,16), range(10,20),   [ 0,1,2,10,11,12,13,14,15,16,17,18,19,9 ], [ 1,2,10,11,12,13,14,15,16,17,18,19 ],   [ 0,1,2,10,11,12,13,14,15,16,17,18,19 ], [ 1,2,10,11,12,13,14,15,16,17,18,19 ] )

	def testSetSpecial3(self):
		self._testSet( (2,8), range(10,20),   [ 0,1,2,10,11,12,13,14,15,16,17,18,19,9 ], [ 1,2,10,11,12,13,14,15,16,17,18,19 ],   [ 0,1,2,10,11,12,13,14,15,16,17,18,19,9 ], [ 1,2,10,11,12,13,14,15,16,17,18,19,9 ] )

	def testSetSpecial4(self):
		self._testSet( (2,9), range(10,20),   [ 0,1,2,10,11,12,13,14,15,16,17,18,19,9 ], [ 1,2,10,11,12,13,14,15,16,17,18,19 ],   [ 0,1,2,10,11,12,13,14,15,16,17,18,19 ], [ 1,2,10,11,12,13,14,15,16,17,18,19 ] )





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



if __name__ == '__main__':
	import unittest
	unittest.main()
