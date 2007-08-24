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


	@abstractmethod
	def _f_getEvaluationContext(self):
		pass





class DMListOpNop (object):
	def __init__(self, src):
		self._src = src


	def evaluate(self):
		return self._src[:]



	def append(self, x):
		self._src.append( x )

	def extend(self, xs):
		self._src.extend( xs )

	def insertBefore(self, before, x):
		self._src.insertBefore( before, x )

	def insertAfter(self, after, x):
		self._src.insertAfter( after, x )

	def remove(self, x):
		self._src.remove( x )


	def __setitem__(self, i, x):
		self._src[i] = x


	def _f_getEvaluationContext(self):
		return self._src





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



	def _f_getEvaluationContext(self):
		return self._src



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
			assert i.step is None, 'step must be 1'
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

	def _f_getEvaluationContext(self):
		return self._src





class DMListOpWrap (object):
	def __init__(self, src, prefix, suffix):
		self._src = src
		self._prefix = prefix
		self._suffix = suffix
		self._pre = []
		self._suf = []
		self._prefixLen = 0
		self._suffixLen = 0


	def evaluate(self):
		if isinstance( self._prefix, list ):
			self._pre = self._prefix
		else:
			self._pre = self._prefix()
		self._prefixLen = len( self._pre )

		if isinstance( self._suffix, list ):
			self._suf = self._suffix
		else:
			self._suf = self._suffix()
		self._suffixLen = len( self._suf )

		return self._pre + self._src[:] + self._suf


	def append(self, x):
		if self._suffix is None:
			self._src.append( x )

	def extend(self, xs):
		if self._suffix is None:
			self._src.extend( x )

	def insertBefore(self, before, x):
		if before not in self._pre:
			if before not in self._suf   or   ( len( self._suf ) > 0  and  before is self._suf[0] ):
				self._src.insertBefore( before, x )

	def insertAfter(self, after, x):
		if after not in self._suf:
			if after not in self._pre   or   ( len( self._pre ) > 0  and  after is self._pre[-1] ):
				self._src.insertAfter( after, x )

	def remove(self, x):
		if x not in self._pre  and  x not in self._suf:
			self._src.remove( x )

	def __setitem__(self, i, x):
		if isinstance( i, slice ):
			assert i.step is None, 'step must be 1'
			start = i.start
			stop = i.stop
			if start < 0:
				start += self._suffixLen
			else:
				start -= self._prefixLen
			if stop < 0:
				stop += self._suffixLen
			else:
				stop -= self._prefixLen
			self._src[start:stop] = x
		else:
			if i < 0:
				self._src[i+self._suffixLen] = x
			else:
				self._src[i-self._prefixLen] = x

	def _f_getEvaluationContext(self):
		return self._src





import unittest



class TestCase_String (unittest.TestCase):
	def testStringCtor(self):
		x = DMString( 'x' )

		self.assert_( x.getValue() == 'x' )
		self.assert_( x.value == 'x' )



if __name__ == '__main__':
	unittest.main()
