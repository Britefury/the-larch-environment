##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Kernel.Abstract import abstractmethod





class DMListOperator (object):
	def __init__(self, layer):
		self._layer = layer


	def _p_dest(self, x):
		try:
			getDestList = x.getDestList
		except AttributeError:
			return x
		else:
			return getDestList( self._layer )


	def _p_src(self, x):
		try:
			getSrcList = x.getSrcList
		except AttributeError:
			return x
		else:
			return getSrcList( self._layer )



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








class DMListOpNop (DMListOperator):
	def __init__(self, layer, src):
		super( DMListOpNop, self ).__init__( layer )
		self._src = src


	def evaluate(self):
		return [ self._p_dest( x )   for x in self._src ]



	def append(self, x):
		self._src.append( self._p_src( x ) )

	def extend(self, xs):
		self._src.extend( [ self._p_src( x )   for x in xs ] )

	def insertBefore(self, before, x):
		self._src.insertBefore( self._p_src( before ), self._p_src( x ) )

	def insertAfter(self, after, x):
		after = self._p_src( after )
		self._src.insertAfter( self._p_src( after ), self._p_src( x ) )

	def remove(self, x):
		self._src.remove( self._p_src( x ) )


	def __setitem__(self, i, x):
		self._src[i] = [ self._p_src( y )   for y in x ]






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

	def insertBefore(self, before, x):
		self._src.insertBefore( self._p_src( before ),  self._p_src( x ) )

	def insertAfter(self, after, x):
		self._src.insertAfter( self._p_src( after ),  self._p_src( x ) )

	def remove(self, x):
		self._src.remove( self._p_src( x ) )

	def __setitem__(self, i, x):
		self._src[i] = [ self._p_src( p )   for p in x ]




class DMListOpSlice (DMListOperator):
	def __init__(self, layer, src, start=None, stop=None):
		super( DMListOpSlice, self ).__init__( layer )
		self._src = src
		self._start = start
		self._stop = stop


	def evaluate(self):
		return [ self._p_dest( x )   for x in self._src[self._start:self._stop] ]


	def append(self, x):
		if self._stop is None:
			self._src.append( self._p_src( x ) )
		else:
			self._src.insertBefore( self._src[self._stop], self._p_src( x ) )

	def extend(self, xs):
		if self._stop is None:
			self._src.extend( [ self._p_src( x )   for x in xs ] )
		else:
			after = self._src[self._stop-1]
			for x in xs:
				self._src.insertAfter( after, self._p_src( x ) )
				after = x

	def insertBefore(self, before, x):
		self._src.insertBefore( self._p_src( before ), self._p_src( x ) )

	def insertAfter(self, after, x):
		self._src.insertAfter( self._p_src( after ), self._p_src( x ) )

	def remove(self, x):
		self._src.remove( self._p_src( x ) )

	def __setitem__(self, i, x):
		if isinstance( i, slice ):
			assert i.step is None, 'step must be 1'
			start = i.start
			stop = i.stop

			if start is None:
				pass
			elif start < 0:
				start += self._stop
			else:
				start += self._start

			if start is None:
				pass
			elif stop < 0:
				stop += self._stop
			else:
				stop += self._start

			self._src[start:stop] = [ self._p_src( p )   for p in x ]
		else:
			if i < 0:
				stop = self._stop
				if stop is None:
					stop = len( self._src ) - 1
				self._src[stop+i] = self._p_src( x )
			else:
				start = self._start
				if start is None:
					start = 0
				self._src[start+i] = self._p_src( x )





class DMListOpWrap (DMListOperator):
	def __init__(self, layer, src, prefix, suffix):
		super( DMListOpWrap, self ).__init__( layer )
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

		return self._pre + [ self._p_dest( x )   for x in self._src ] + self._suf


	def append(self, x):
		if self._suffix is None:
			self._src.append( self._p_src( x ) )

	def extend(self, xs):
		if self._suffix is None:
			self._src.extend( [ self._p_src( x )   for x in xs ] )

	def insertBefore(self, before, x):
		if before not in self._pre:
			if before not in self._suf   or   ( len( self._suf ) > 0  and  before is self._suf[0] ):
				self._src.insertBefore( self._p_src( before ), self._p_src( x ) )

	def insertAfter(self, after, x):
		if after not in self._suf:
			if after not in self._pre   or   ( len( self._pre ) > 0  and  after is self._pre[-1] ):
				self._src.insertAfter( self._p_src( after ), self._p_src( x ) )

	def remove(self, x):
		if x not in self._pre  and  x not in self._suf:
			self._src.remove( self._p_src( x ) )

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
			self._src[start:stop] = [ self._p_src( p )   for p in x ]
		else:
			if i < 0:
				self._src[i+self._suffixLen] = self._p_src( x )
			else:
				self._src[i-self._prefixLen] = self._p_src( x )





import unittest



class TestCase_String (unittest.TestCase):
	def testStringCtor(self):
		x = DMString( 'x' )

		self.assert_( x.getValue() == 'x' )
		self.assert_( x.value == 'x' )



if __name__ == '__main__':
	unittest.main()
