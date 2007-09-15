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




class DMListOpJoin (DMListOperator):
	__slots__ = [ '_srcA', '_srcB' ]


	def __init__(self, layer, srcA, srcB):
		super( DMListOpJoin, self ).__init__( layer )
		self._srcA = srcA
		self._srcB = srcB


	def evaluate(self):
		return self._srcA[:] + self._srcB[:]



	def append(self, x):
		self._srcB.append( x )


	def extend(self, xs):
		self._srcB.extend( xs )


	def insert(self, i, x):
		if i < 0:
			lenB = len( self._srcB )
			if i >= -lenB:
				self._srcB.insert( i, self._p_src( x ) )
			else:
				self._srcA.insert( i + lenB, self._p_src( x ) )
		else:
			lenA = len( self._srcA )
			if i < lenA:
				self._srcA.insert( i, self._p_src( x ) )
			else:
				self._srcB.insert( i - lenA, self._p_src( x ) )


	def remove(self, x):
		try:
			self._srcA.remove( self._p_src( x ) )
		except ValueError:
			self._srcB.remove( self._p_src( x ) )


	def __setitem__(self, i, x):
		if isinstance( i, slice ):
			lenA = len( self._srcA )
			lenB = len( self._srcB )
			lenSelf = lenA + lenB
			start, stop, step = i.indices( lenSelf )

			if i.step is None:
				if stop == -1  or  stop < start:
					if start <= lenA:
						self._srcA[start:start] = [ self._p_src( p )   for p in x ]
					else:
						start -= lenA
						self._srcB[start:start] = [ self._p_src( p )   for p in x ]
				else:
					if start <= lenA:
						if stop <= lenA:
							self._srcA[start:stop] = [ self._p_src( p )   for p in x ]
						else:
							a = lenA - start
							self._srcA[start:lenA] = [ self._p_src( p )   for p in x[:a] ]
							self._srcB[0:stop-lenA] = [ self._p_src( p )   for p in x[a:] ]
					else:
						self._srcB[start-lenA:stop-lenA] = [ self._p_src( p )   for p in x ]
			else:
				indices = xrange( start, stop, step )
				if len( indices )  !=  len( x ):
					raise ValueError, 'attempt to assign sequence of size %d to to extended slice of size %d'  %  ( len( x ), len( indices ) )
				for i, p in zip( indices, x ):
					self[i] = self._p_src( p )
		else:
			if i < 0:
				lenB = len( self._srcB )
				if i >= -lenB:
					self._srcB[i] = self._p_src( x )
				else:
					self._srcA[i+lenB] = self._p_src( x )
			else:
				lenA = len( self._srcA )
				if i < lenA:
					self._srcA[i] = self._p_src( x )
				else:
					self._srcB[i-lenA] = self._p_src( x )


	def __delitem__(self, i):
		if isinstance( i, slice ):
			lenA = len( self._srcA )
			lenB = len( self._srcB )
			lenSelf = lenA + lenB
			start, stop, step = i.indices( lenSelf )

			if i.step is None:
				if stop == -1  or  stop < start:
					if start <= lenA:
						del self._srcA[start:start]
					else:
						start -= lenA
						del self._srcB[start:start]
				else:
					if start <= lenA:
						if stop <= lenA:
							del self._srcA[start:stop]
						else:
							del self._srcA[start:lenA]
							del self._srcB[0:stop-lenA]
					else:
						del self._srcB[start-lenA:stop-lenA]
			else:
				indices = xrange( start, stop, step )
				if step > 0:
					indices = reversed( indices )
				for i in indices:
					del self[i]
		else:
			if i < 0:
				lenB = len( self._srcB )
				if i >= -lenB:
					del self._srcB[i]
				else:
					del self._srcA[i+lenB]
			else:
				lenA = len( self._srcA )
				if i < lenA:
					del self._srcA[i]
				else:
					del self._srcB[i-lenA]



	def __len__(self):
		return len( self._srcA ) + len( self._srcB )








class TestCase_DMListOpJoin (TestCase_DMListOperator_base):
	def setUp(self):
		self.layer1 = DocModelLayer()
		self.layer2 = DocModelLayer()
		self.xa = DMList()
		self.xa.extend( range( 0, 5 ) )
		self.xb = DMList()
		self.xb.extend( range( 5, 10 ) )

		self.y = DMVirtualList( DMListOpJoin( self.layer2, self.xa, self.xb ) )

	def tearDown(self):
		del self.layer1
		del self.layer2
		del self.xa
		del self.xb
		del self.y


	def _p_joinTestCase(self, operationFunc, opDescription, checkFunction=None):
		"""
		operationFunc :				f( xs )
		"""
		self.xa[:] = range(0,5)
		self.xb[:] = range(5,10)

		expectedError = None
		expectedErrorClass = None
		error = None
		errorClass = None

		testList = self.y[:]
		try:
			operationFunc( testList )
		except Exception, e:
			expectedError = e
			expectedErrorClass = e.__class__

		try:
			operationFunc( self.y )
		except Exception, e:
			error = e
			errorClass = e.__class__

		self.assert_( expectedErrorClass == errorClass, ( opDescription, expectedError, error ) )

		if error is None:
			self.assert_( self.y[:] == testList, ( opDescription, self.xa[:], self.xb[:], self.y[:], testList ) )

			self.assert_( ( len(self.xa) + len(self.xb) )  ==  len( self.y ),  ( opDescription, self.xa[:], self.xb[:], self.y[:], testList ) )
			self.assert_( self.xa[:] == testList[:len(self.xa)],  ( opDescription, self.xa[:], self.xb[:], self.y[:], testList ) )
			if len(self.xb) > 0:
				self.assert_( self.xa[:] == testList[:-len(self.xb)],  ( opDescription, self.xa[:], self.xb[:], self.y[:], testList ) )
			self.assert_( self.xb[:] == testList[len(self.xa):],  ( opDescription, self.xa[:], self.xb[:], self.y[:], testList ) )
			if len(self.xb) > 0:
				self.assert_( self.xb[:] == testList[-len(self.xb):],  ( opDescription, self.xa[:], self.xb[:], self.y[:], testList ) )

			if checkFunction is not None:
				self.assert_( checkFunction( self.xa, self.xb, self.y, testList ),  ( opDescription, self.xa[:], self.xb[:], self.y[:], testList ) )


	def testFunction(self):
		self._p_joinTestCase( lambda x: x, 'function' )

	def testAppend(self):
		def _append(xs):
			xs.append( 11 )
		def _check(xa, xb, y, testList):
			return xa[:] == range(0,5)  and  xb[:] == range(5,10)+[11]
		self._p_joinTestCase( _append, 'append', _check )

	def testExtend(self):
		def _extend(xs):
			xs.extend( [11,12,13] )
		def _check(xa, xb, y, testList):
			return xa[:] == range(0,5)  and  xb[:] == range(5,10)+[11,12,13]
		self._p_joinTestCase( _extend, 'extend', _check )

	def testInsert(self):
		for i in xrange( -11, 11 ):
			def _insert(xs):
				xs.insert( i, 50 )
			def _check(xa, xb, y, testList):
				return 50 in y  and  ( 50 in xa  or  50 in xb )  and  not ( 50 in xa and 50 in xb )
			self._p_joinTestCase( _insert, 'insert %d' % (i, ), _check )

	def testRemove(self):
		for i in xrange( -11, 11 ):
			def _remove(xs):
				xs.remove( i, 50 )
			def _check(xa, xb, y, testList):
				return 50 not in y  and  50 not in xa  and  50 not in xb
			self._p_joinTestCase( _remove, 'remove %d' % (i, ), _check )

	def testSet(self):
		for i in xrange( -11, 11 ):
			def _set(xs):
				xs[i] = 50
			self._p_joinTestCase( _set, 'set %d' % (i, ) )

	def testSetSlice(self):
		for i in xrange( -11, 11 ):
			for j in xrange( -11, 11 ):
				def _set(xs):
					xs[i:j] = range( 50, 55 )
				def _set2(xs):
					xs[i:j] = range( 50, 75 )
				self._p_joinTestCase( _set, 'set %d:%d' % (i,j, ) )
				self._p_joinTestCase( _set2, 'set2 %d:%d' % (i,j, ) )

	def testSetSliceStep(self):
		for i in xrange( -11, 11 ):
			for j in xrange( -11, 11 ):
				for k in xrange( -3, 3 ):
					def _set(xs):
						xs[i:j:k] = range( 50, 55 )
					def _set2(xs):
						xs[i:j:k] = range( 50, 75 )
					self._p_joinTestCase( _set, 'set %d:%d:%d' % (i,j,k, ) )
					self._p_joinTestCase( _set2, 'set2 %d:%d:%d' % (i,j,k, ) )

	def testDel(self):
		for i in xrange( -11, 11 ):
			def _del(xs):
				del xs[i]
			self._p_joinTestCase( _del, 'del %d' % (i, ) )

	def testDelSlice(self):
		for i in xrange( -11, 11 ):
			for j in xrange( -11, 11 ):
				def _del(xs):
					del xs[i:j]
				self._p_joinTestCase( _del, 'del %d:%d' % (i,j, ) )

	def testDelSliceStep(self):
		for i in xrange( -11, 11 ):
			for j in xrange( -11, 11 ):
				for k in xrange( -3, 3 ):
					def _del(xs):
						del xs[i:j:k]
					self._p_joinTestCase( _del, 'del %d:%d:%d' % (i,j,k, ) )



	def testDelAppend(self):
		def _delappend(xs):
			del sx[:]
			xs.append( 11 )
		self._p_joinTestCase( _delappend, 'delappend' )

	def testDelInsert(self):
		for i in xrange( -11, 11 ):
			def _delinsert(xs):
				del sx[:]
				xs.insert( i, 50 )
			self._p_joinTestCase( _delinsert, 'delinsert %d' % (i, ) )





if __name__ == '__main__':
	import unittest
	unittest.main()
