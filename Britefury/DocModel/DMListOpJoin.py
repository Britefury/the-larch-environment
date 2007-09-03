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




class DMListOpJoin (DMListOperator):
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
				for i, p in zip( xrange( start, stop, step ), x ):
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
				if start <= lenA:
					if stop <= lenA:
						del self._srcA[start:stop]
					else:
						a = lenA - start
						del self._srcA[start:lenA]
						del self._srcB[0:stop-lenA]
				else:
					del self._srcB[start-lenA:stop-lenA]
			else:
				if step > 0:
					start, stop = stop, start
					step = -step
				for i, p in zip( xrange( start, stop, step ), x ):
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
	def _p_joinTestCase(self, operationFunc, opDescription):
		"""
		operationFunc :				f( xs )
		"""
		layer1 = DocModelLayer()
		layer2 = DocModelLayer()
		xa = DMLiteralList()
		xa.extend( range( 0, 5 ) )
		xb = DMLiteralList()
		xb.extend( range( 5, 10 ) )

		y = DMListOpJoin( layer2, xa, xb )

		expectedError = None
		expectedErrorClass = None
		error = None
		errorClass = None

		testList = y[:]
		try:
			operationFunc( testList )
		except Exception, e:
			expectedError = e
			expectedErrorClass = e.__class__

		try:
			operationFunc( y )
		except Exception, e:
			error = e
			errorClass = e.__class__

		self.assert_( expectedErrorClass == errorClass, ( opDescription, expectedError, error ) )

		if error is None:
			self.assert_( y[:] == testList, ( opDescription, xa[:], xb[:], y[:], testList ) )

			self.assert_( ( len(xa) + len(xb) )  ==  len( y ),  ( opDescription, xa[:], xb[:], y[:], testList ) )
			self.assert_( xa[:] == testList[:len(xa)],  ( opDescription, xa[:], xb[:], y[:], testList ) )
			self.assert_( xa[:] == testList[:-len(xb)],  ( opDescription, xa[:], xb[:], y[:], testList ) )
			self.assert_( xb[:] == testList[len(xa):],  ( opDescription, xa[:], xb[:], y[:], testList ) )
			self.assert_( xb[:] == testList[-len(xb):],  ( opDescription, xa[:], xb[:], y[:], testList ) )


	def testFunction(self):
		self._p_joinTestCase( lambda x: x, 'function' )

	#def testAppend(self):
		#def _append(xs):
			#xs.append( 11 )
		#self._sliceTestCase( _append, 'append' )

	#def testExtend(self):
		#def _extend(xs):
			#xs.extend( [11,12,13] )
		#self._sliceTestCase( _extend, 'extend' )

	#def testInsert(self):
		#for i in xrange( -11, 11 ):
			#def _insert(xs):
				#xs.insert( i, 50 )
			#self._sliceTestCase( _insert, 'insert %d' % (i, ) )

	#def testRemove(self):
		#for i in xrange( -11, 11 ):
			#def _remove(xs):
				#xs.remove( i, 50 )
			#self._sliceTestCase( _remove, 'remove %d' % (i, ) )

	#def testSet(self):
		#for i in xrange( -11, 11 ):
			#def _set(xs):
				#xs[i] = 50
			#self._sliceTestCase( _set, 'set %d' % (i, ) )

	#def testSetSlice(self):
		#for i in xrange( -11, 11 ):
			#for j in xrange( -11, 11 ):
				#def _set(xs):
					#xs[i:j] = range( 50, 55 )
				#def _set2(xs):
					#xs[i:j] = range( 50, 75 )
				#self._sliceTestCase( _set, 'set %d:%d' % (i,j, ) )
				#self._sliceTestCase( _set2, 'set2 %d:%d' % (i,j, ) )

	#def testSetSliceStep(self):
		#for i in xrange( -11, 11 ):
			#for j in xrange( -11, 11 ):
				#for k in xrange( -3, 3 ):
					#def _set(xs):
						#xs[i:j:k] = range( 50, 55 )
					#def _set2(xs):
						#xs[i:j:k] = range( 50, 75 )
					#self._sliceTestCase( _set, 'set %d:%d:%d' % (i,j,k, ) )
					#self._sliceTestCase( _set2, 'set2 %d:%d:%d' % (i,j,k, ) )

	#def testDel(self):
		#for i in xrange( -11, 11 ):
			#def _del(xs):
				#del xs[i]
			#self._sliceTestCase( _del, 'del %d' % (i, ) )

	#def testDelSlice(self):
		#for i in xrange( -11, 11 ):
			#for j in xrange( -11, 11 ):
				#def _del(xs):
					#del xs[i:j]
				#self._sliceTestCase( _del, 'del %d:%d' % (i,j, ) )

	#def testDelSliceStep(self):
		#for i in xrange( -11, 11 ):
			#for j in xrange( -11, 11 ):
				#for k in xrange( -3, 3 ):
					#def _del(xs):
						#del xs[i:j:k]
					#self._sliceTestCase( _del, 'del %d:%d:%d' % (i,j,k, ) )



	#def testDelAppend(self):
		#def _delappend(xs):
			#del sx[:]
			#xs.append( 11 )
		#self._sliceTestCase( _delappend, 'delappend' )

	#def testDelInsert(self):
		#for i in xrange( -11, 11 ):
			#def _delinsert(xs):
				#del sx[:]
				#xs.insert( i, 50 )
			#self._sliceTestCase( _delinsert, 'delinsert %d' % (i, ) )





if __name__ == '__main__':
	import unittest
	unittest.main()
