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
		if i < 0:
			relativeI = i + self._suffixLen
			if relativeI < 0  and  relativeI >= -len( self._src ):
				self._src.insert( relativeI, self._p_src( x ) )
		else:
			relativeI = i - self._prefixLen
			if relativeI >= 0  and  relativeI <= len( self._src ):
				self._src.insert( relativeI, self._p_src( x ) )

	def remove(self, x):
		if x not in self._pre  and  x not in self._suf:
			self._src.remove( self._p_src( x ) )

	def __setitem__(self, i, x):
		if isinstance( i, slice ):
			start, stop, step = i.indices( len( self ) )

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
			if i < 0:
				ii = i + self._suffixLen
				if ii < 0:
					print ii, self._suffixLen, self._pre, self._suf
					self._src[ii] = self._p_src( x )
			else:
				if i >= self._prefixLen:
					self._src[i-self._prefixLen] = self._p_src( x )



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



