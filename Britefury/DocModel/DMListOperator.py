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
	def insert(self, i, x):
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
	def replace(self, a, x):
		pass

	@abstractmethod
	def replaceRange(self, a, b, xs):
		"""Replaces the range (a,b) inclusive with the contents of xs"""
		pass

	@abstractmethod
	def __setitem__(self, i, x):
		pass

	@abstractmethod
	def __delitem__(self, i):
		pass


	def __getitem__(self, i):
		return self.evaluate()[i]


	@abstractmethod
	def __len__(self):
		pass








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

	def insert(self, i, x):
		self._src.insert( i, self._p_src( x ) )

	def insertBefore(self, before, x):
		self._src.insertBefore( self._p_src( before ), self._p_src( x ) )

	def insertAfter(self, after, x):
		after = self._p_src( after )
		self._src.insertAfter( self._p_src( after ), self._p_src( x ) )

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
			if self._stop is None:
				self._src.insert( len( self._src ) + i, x )
			else:
				self._src.insert( i + self._stop, x )
		else:
			self._src.insert( i + self._start, x )
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
			start = i.start
			stop = i.stop
			step = i.step

			if start is None:
				start = self._start
			elif start < 0:
				if self._stop is None:
					start += len( self._src )
				else:
					start += self._stop
			else:
				start += self._start

			if stop is None:
				stop = self._stop
			elif stop < 0:
				if self._stop is None:
					stop += len( self._src )
				else:
					stop += self._stop
			else:
				stop += self._start

			oldLen = len( self._src )
			self._src[start:stop:step] = [ self._p_src( p )   for p in x ]
			newLen = len( self._src )
			changeInLength = newLen - oldLen
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
				self._src[stop+i] = self._p_src( x )
			else:
				self._src[self._start+i] = self._p_src( x )


	def __delitem__(self, i):
		if isinstance( i, slice ):
			start = i.start
			stop = i.stop
			step = i.step

			if start is None:
				start = self._start
			elif start < 0:
				if self._stop is None:
					start += len( self._src )
				else:
					start += self._stop
			else:
				start += self._start

			if stop is None:
				stop = self._stop
			elif stop < 0:
				if self._stop is None:
					stop += len( self._src )
				else:
					stop += self._stop
			else:
				stop += self._start

			oldLen = len( self._src )
			del self._src[start:stop:step]
			newLen = len( self._src )
			changeInLength = newLen - oldLen
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
		if self._stop is None:
			if self._start < 0:
				return -self._start
			else:
				return len( self._src ) - self._start
		elif self._stop < 0:
			if self._start < 0:
				return self._stop - self._start
			else:
				return len( self._src ) + self._stop - self._start
		else:
			if self._start < 0:
				return self._stop  -  ( len( self._src ) + self._start )
			else:
				return self._stop - self._start




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

	def replace(self, a, x):
		if a not in self._pre  and  a not in self._suf:
			self._src.replace( self._p_src( a ), self._p_src( x ) )

	def replaceRange(self, a, b, xs):
		if a in self._pre:
			a = self[0]
		if b in self._suf:
			b = self[-1]
		self._src.replaceRange( self._p_src( a ), self._p_src( b ), [ self._p_src( x )  for x in xs ] )


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

