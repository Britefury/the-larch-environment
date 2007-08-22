##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Kernel.Abstract import abstractmethod





class DMListOperator (object):
	def __init__(self):
		pass


	@abstractmethod
	def evaluate(self):
		pass



	@abstractmethod
	def append(self, x):
		pass

	@abstractmethod
	def extend(self, xs):
		pass

	@abstractmethod
	def insertBefore(self, before, x):
		pass

	@abstractmethod
	def insertAfter(self, after, x):
		pass

	@abstractmethod
	def remove(self, x):
		pass


	@abstractmethod
	def __setitem__(self, i, x):
		pass

	def __getitem__(self, i):
		return self.evaluate()[i]





class DMListOpMap (object):
	def __init__(self, src, f, invF):
		self._src = src
		self._f = f
		self._invF = invF


	def evaluate(self):
		return [ self._f( x )   for x in self._src ]


	def append(self, x):
		self._src.append( self._invF( x ) )

	def extend(self, xs):
		self._src.extend( [ self._invF( p )   for p in xs ] )

	def insertBefore(self, before, x):
		self._src.insertBefore( self._invF( before ),  self._invF( x ) )

	def insertAfter(self, after, x):
		self._src.insertAfter( self._invF( after ),  self._invF( x ) )

	def remove(self, x):
		self._src.remove( self._invF( x ) )

	def __setitem__(self, i, x):
		self._src[i] = [ self._invF( p )   for p in x ]



class DMListOpSlice (object):
	def __init__(self, src, start, stop):
		self._src = src
		self._start = start
		self._stop = stop


	def evaluate(self):
		return self._src[self._start:self._stop]


	def append(self, x):
		self._src.insertAfter( self._src[self._stop-1], x )

	def extend(self, xs):
		after = self._src[self._stop-1]
		for x in xs:
			self._src.insertAfter( after, x )
			after = x

	def insertBefore(self, before, x):
		self._src.insertBefore( before, x )

	def insertAfter(self, after, x):
		self._src.insertAfter( after, x )

	def remove(self, x):
		self._src.remove( x )

	def __setitem__(self, i, x):
		if isinstance( i, slice ):
			assert i.step == 1, 'step must be 1'
			start = i.start
			stop = i.stop
			if start < 0:
				start += self._stop
			else:
				start += self._start
			if stop < 0:
				stop += self._stop
			else:
				stop += self._start
			self._src[start:stop] = x
		else:
			if i < 0:
				self._src[self._stop+i] = x
			else:
				self._src[self._start+i] = x





class DMListOpJoin (object):
	def __init__(self, srcs):
		self._srcs = srcs
		self._endIndices = [ 0 ]  *  len( srcs )


	def evaluate(self):
		res = []
		self._endIndices = []
		l = 0
		for src in self._srcs:
			res += src
			l += len( src )
			self._endIndices.append( l )
		return res


	def append(self, x):
		if len( self._srcs ) > 0:
			self._srcs[-1].append( x )

	def extend(self, xs):
		if len( self._srcs ) > 0:
			self._srcs[-1].extend( x )

	#def insertBefore(self, before, x):
		#self._src.insertBefore( before, x )

	#def insertAfter(self, after, x):
		#self._src.insertAfter( after, x )

	#def remove(self, x):
		#self._src.remove( x )

	#def __setitem__(self, i, x):
		#if isinstance( i, slice ):
			#assert i.step == 1, 'step must be 1'
			#start = i.start
			#stop = i.stop
			#if start < 0:
				#start += self._stop
			#else:
				#start += self._start
			#if stop < 0:
				#stop += self._stop
			#else:
				#stop += self._start
			#self._src[start:stop] = x
		#else:
			#if i < 0:
				#self._src[self._stop+i] = x
			#else:
				#self._src[self._start+i] = x





import unittest



class TestCase_String (unittest.TestCase):
	def testStringCtor(self):
		x = DMString( 'x' )

		self.assert_( x.getValue() == 'x' )
		self.assert_( x.value == 'x' )



if __name__ == '__main__':
	unittest.main()
