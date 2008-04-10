##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import bisect
import weakref

from Britefury.Util.SignalSlot import ClassSignal




class DocTextMarker (object):
	__slots__ = [ '__weakref__', '_index' ]

	def __init__(self, index):
		super( DocTextMarker, self ).__init__()
		self._index = index
		
		
	def getIndex(self):
		return self._index
	
		
		
	def _f_onInsert(self, delta):
		self._index += delta
		
	def _f_onRemove(self, start, delta):
		self._index = max( start, self._index - delta )
		

	def __lt__(self, x):
		return self._index < x._index
	
	
	index = property( getIndex )

	

	
class DocTextActiveMarker (DocTextMarker):
	moveSignal = ClassSignal()


	def _f_onInsert(self, delta):
		super( DocTextActiveMarker, self )._f_onInsert( delta )
		self.moveSignal.emit( self )

	def _f_onRemove(self, start, delta):
		super( DocTextActiveMarker, self )._f_onRemove( start, delta )
		self.moveSignal.emit( self )


		
	
class DocTextRange (object):
	__slots__ = [ '__weakref__', '_begin', '_end' ]

	def __init__(self, begin, end):
		super( DocTextRange, self ).__init__()
		self._begin = begin
		self._end = end
		
		
	def getBegin(self):
		return self._begin
	
	def getEnd(self):
		return self._end
		
		
	def _f_onInsertBefore(self, startPosition, delta):
		assert self._end >= startPosition
		if self._begin > startPosition:
			self._begin += delta
		self._end += delta
		
	def _f_onInsertAfter(self, startPosition, delta):
		assert self._end >= startPosition
		if self._begin >= startPosition:
			self._begin += delta
		self._end += delta
		
	def _f_onRemove(self, start, delta):
		assert self._end >= start
		if self._begin > start:
			self._begin = max( start, self._begin - delta )
		self._end = max( start, self._end - delta )

		
	def __lt__(self, x):
		return self._end < x._end
	
	
	begin = property( getBegin )
	end = property( getEnd )

		

class DocTextActiveRange (DocTextRange):
	moveSignal = ClassSignal()
	contentsSignal = ClassSignal()


	def _f_onInsertBefore(self, startPosition, delta):
		bContentsChanged = startPosition >= self._begin  and  startPosition < self._end
		super( DocTextActiveRange, self )._f_onInsertBefore( startPosition, delta )
		self.moveSignal.emit( self )
		if bContentsChanged:
			self.contentsSignal.emit( self )

	def _f_onInsertAfter(self, startPosition, delta):
		bContentsChanged = startPosition > self._begin  and  startPosition <= self._end
		super( DocTextActiveRange, self )._f_onInsertAfter( startPosition, delta )
		self.moveSignal.emit( self )
		if bContentsChanged:
			self.contentsSignal.emit( self )

	def _f_onRemove(self, start, delta):
		stop = start + delta
		bContentsChanged = stop > self._begin  and  start < self._end
		super( DocTextActiveRange, self )._f_onRemove( start, delta )
		self.moveSignal.emit( self )
		if bContentsChanged:
			self.contentsSignal.emit( self )

		

			
			
class DocText (object):
	def __init__(self, text=''):
		self._text = text
		self._markers = []
		self._ranges = []
		
		
	def getText(self):
		return self._text
		
		
	def textMarker(self, index):
		m = DocTextMarker( index )
		self._p_registerMarker( m )
		return m
		
	def textActiveMarker(self, index):
		m = DocTextActiveMarker( index )
		self._p_registerMarker( m )
		return m
		
		
	def textRange(self, begin, end):
		r = DocTextRange( begin, end )
		self._p_registerRange(r  )
		return r
		
	def textActiveRange(self, begin, end):
		r = DocTextActiveRange( begin, end )
		self._p_registerRange( r )
		return r
		
		
		
		

	def insertBefore(self, position, text):
		if text != '':
			if isinstance( position, DocTextMarker ):
				position = position._index
			elif isinstance( position, DocTextRange ):
				position = position._begin
		
			self._text = self._text[:position] + text + self._text[position:]
			
			delta = len( text )
			
			markerIndex = bisect.bisect_right( [ mRef()   for mRef in self._markers ], DocTextMarker( position ) )
			for mRef in self._markers[markerIndex:]:
				m = mRef()
				m._f_onInsert( delta )
			
			# Need to consider all ranges that end *after* @position
			# bisect_right() will return an index, before which reside ranges who end *before* the insertion, hence cannot be affected by it
			rangeIndex = bisect.bisect_right( [ rRef()   for rRef in self._ranges ], DocTextRange( 0, position ) )
			for rRef in self._ranges[rangeIndex:]:
				r = rRef()
				r._f_onInsertBefore( position, delta )
		
		
	
	
	def insertAfter(self, position, text):
		if text != '':
			if isinstance( position, DocTextMarker ):
				position = position._index + 1
			elif isinstance( position, DocTextRange ):
				position = position._end + 1
			else:
				position += 1
				
			self._text = self._text[:position] + text + self._text[position:]
			
			delta = len( text )
			
			markerIndex = bisect.bisect_left( [ mRef()   for mRef in self._markers ], DocTextMarker( position ) )
			for mRef in self._markers[markerIndex:]:
				m = mRef()
				m._f_onInsert( delta )
			
			# Need to consider all ranges that end *at or after* @position
			# bisect_left() will return an index, before which reside ranges who end *before or at* the insertion, hence cannot be affected by it
			rangeIndex = bisect.bisect_left( [ rRef()   for rRef in self._ranges ], DocTextRange( 0, position ) )
			for rRef in self._ranges[rangeIndex:]:
				r = rRef()
				r._f_onInsertAfter( position, delta )

	
	def remove(self, begin, end):
		if end > begin:
			if isinstance( begin, DocTextMarker ):
				begin = begin._index
			if isinstance( end, DocTextMarker ):
				end = end._index
			
			self._text = self._text[:begin]  +  self._text[end:]
		
			delta = end - begin
			
			markerIndex = bisect.bisect_right( [ mRef()   for mRef in self._markers ], DocTextMarker( begin ) )
			for mRef in self._markers[markerIndex:]:
				m = mRef()
				m._f_onRemove( begin, delta )
			
			# Need to consider all ranges that end *after* @begin
			# bisect_right() will return an index, before which reside ranges who end *before* the removal, hence cannot be affected by it
			rangeIndex = bisect.bisect_right( [ rRef()   for rRef in self._ranges ], DocTextRange( 0, begin ) )
			for rRef in self._ranges[rangeIndex:]:
				r = rRef()
				r._f_onRemove( begin, delta )

				
				
	def removeRange(self, range):
		self.remove( range._begin, range._end )

		
		
		
	def _p_markerCleaner(self, ref):
		self._markers.remove( ref )
		
	def _p_rangeCleaner(self, ref):
		self._ranges.remove( ref )
		
		
	def _p_registerMarker(self, m):
		mRef = weakref.ref( m, self._p_markerCleaner )
		index = bisect.bisect_right( [ mRef()   for mRef in self._markers ], m )
		self._markers.insert( index, mRef )
		
		
	def _p_registerRange(self, r):
		rRef = weakref.ref( r, self._p_rangeCleaner )
		index = bisect.bisect_right( [ rRef()   for rRef in self._ranges ], r )
		self._ranges.insert( index, rRef )
		
		
	text = property( getText )
		
		

		
import unittest

class Test_DocText_text (unittest.TestCase):
	def _textTest(self, initialText, operation, expectedResult):
		t = DocText( initialText )
		operation( t )
		if t.text != expectedResult:
			print 'EXPECTED:'
			print expectedResult
			print ''
			print 'RESULT:'
			print t.text
		self.assert_( t.text == expectedResult )
		
		
	def testText(self):
		self._textTest( 'hello world', lambda x: None, 'hello world' )

	def testInsertBefore(self):
		self._textTest( 'hello world', lambda x: x.insertBefore( 6, 'xyz ' ), 'hello xyz world' )

	def testInsertAfter(self):
		self._textTest( 'hello world', lambda x: x.insertAfter( 5, 'xyz ' ), 'hello xyz world' )

	def testRemove(self):
		self._textTest( 'hello xyz world', lambda x: x.remove( 6, 10 ), 'hello world' )

	def testInsertRemove(self):
		def op(x):
			x.insertBefore( 6, 'xyz ' )
			x.remove( 7, 9 )
		self._textTest( 'hello world', op, 'hello x world' )

		
class Test_DocText_marker (unittest.TestCase):
	def _markerTest(self, text, markerPosition, operation, expectedMarkerPosition, bMoveExpected, expectedText=None):
		bMoved = [ False ]
		def _onMoved(m):
			bMoved[0] = True
		t = DocText( text )
		marker = t.textActiveMarker( markerPosition )
		marker.moveSignal.connect( _onMoved )
		operation( t )
		if marker.index != expectedMarkerPosition:
			print 'EXPECTED:'
			print expectedMarkerPosition
			print ''
			print 'RESULT:'
			print marker.index
		self.assert_( marker.index == expectedMarkerPosition )
		self.assert_( bMoved[0]  ==  bMoveExpected )
		if expectedText is not None:
			if t.text != expectedText:
				print 'EXPECTED:'
				print expectedText
				print ''
				print 'RESULT:'
				print t.text
			self.assert_( t.text == expectedText )
		
		
	def testMarker(self):
		self._markerTest( '0123456789', 5, lambda x: None, 5, False )
		
	def testInsertBefore(self):
		self._markerTest( '0123456789', 5, lambda x: x.insertBefore( 2, 'abc' ), 8, True )
		self._markerTest( '0123456789', 5, lambda x: x.insertBefore( 4, 'abc' ), 8, True )
		self._markerTest( '0123456789', 5, lambda x: x.insertBefore( 5, 'abc' ), 5, False )
		self._markerTest( '0123456789', 5, lambda x: x.insertBefore( 7, 'abc' ), 5, False )
		
	def testInsertAfter(self):
		self._markerTest( '0123456789', 5, lambda x: x.insertAfter( 1, 'abc' ), 8, True )
		self._markerTest( '0123456789', 5, lambda x: x.insertAfter( 3, 'abc' ), 8, True )
		self._markerTest( '0123456789', 5, lambda x: x.insertAfter( 4, 'abc' ), 8, True )
		self._markerTest( '0123456789', 5, lambda x: x.insertAfter( 6, 'abc' ), 5, False )

	def testRemove(self):
		self._markerTest( '0123456789', 5, lambda x: x.remove( 1, 4 ), 2, True )
		self._markerTest( '0123456789', 5, lambda x: x.remove( 2, 5 ), 2, True )
		self._markerTest( '0123456789', 5, lambda x: x.remove( 3, 6 ), 3, True )
		self._markerTest( '0123456789', 5, lambda x: x.remove( 4, 7 ), 4, True )
		self._markerTest( '0123456789', 5, lambda x: x.remove( 5, 8 ), 5, False )
		self._markerTest( '0123456789', 5, lambda x: x.remove( 6, 9 ), 5, False )

	def testInsertRemove(self):
		def op(x):
			x.remove( 5, 9 )
			x.insertBefore( 5, 'abc' )
		self._markerTest( '01234xyz 56789', 6, op, 5, True )


		
		
class Test_DocText_range (unittest.TestCase):
	def _rangeTest(self, text, rangeIndices, operation, expectedRangeIndices, bMoveExpected, bContextsExptected, expectedText=None):
		bMoved = [ False ]
		def _onMoved(m):
			bMoved[0] = True

		bContents = [ False ]
		def _onContents(m):
			bContents[0] = True

		t = DocText( text )
		r = t.textActiveRange( *rangeIndices )
		r.moveSignal.connect( _onMoved )
		r.contentsSignal.connect( _onContents )
		operation( t )
		if ( r.begin, r.end ) != expectedRangeIndices:
			print 'EXPECTED:'
			print expectedRangeIndices
			print ''
			print 'RESULT:'
			print rangeIndices
		self.assert_( ( r.begin, r.end ) == expectedRangeIndices )
		self.assert_( bMoved[0]  ==  bMoveExpected )
		self.assert_( bContents[0]  ==  bContextsExptected )
		if expectedText is not None:
			if t.text != expectedText:
				print 'EXPECTED:'
				print expectedText
				print ''
				print 'RESULT:'
				print t.text
			self.assert_( t.text == expectedText )
		
		
	def testMarker(self):
		self._rangeTest( '0123456789', ( 4, 6 ), lambda x: None, ( 4, 6 ), False, False )
		
		
	def testInsertBefore(self):
		self._rangeTest( '0123456789', ( 1, 1 ), lambda x: x.insertBefore( 5, 'abc' ), ( 1, 1 ), False, False )
		self._rangeTest( '0123456789', ( 1, 3 ), lambda x: x.insertBefore( 5, 'abc' ), ( 1, 3 ), False, False )
		self._rangeTest( '0123456789', ( 1, 4 ), lambda x: x.insertBefore( 5, 'abc' ), ( 1, 4 ), False, False )
		self._rangeTest( '0123456789', ( 1, 5 ), lambda x: x.insertBefore( 5, 'abc' ), ( 1, 5 ), False, False )
		self._rangeTest( '0123456789', ( 1, 6 ), lambda x: x.insertBefore( 5, 'abc' ), ( 1, 9 ), True, True )
		self._rangeTest( '0123456789', ( 1, 8 ), lambda x: x.insertBefore( 5, 'abc' ), ( 1, 11 ), True, True )
		
		self._rangeTest( '0123456789', ( 3, 3 ), lambda x: x.insertBefore( 5, 'abc' ), ( 3, 3 ), False, False )
		self._rangeTest( '0123456789', ( 3, 4 ), lambda x: x.insertBefore( 5, 'abc' ), ( 3, 4 ), False, False )
		self._rangeTest( '0123456789', ( 3, 5 ), lambda x: x.insertBefore( 5, 'abc' ), ( 3, 5 ), False, False )
		self._rangeTest( '0123456789', ( 3, 6 ), lambda x: x.insertBefore( 5, 'abc' ), ( 3, 9 ), True, True )
		self._rangeTest( '0123456789', ( 3, 8 ), lambda x: x.insertBefore( 5, 'abc' ), ( 3, 11 ), True, True )
		
		self._rangeTest( '0123456789', ( 4, 4 ), lambda x: x.insertBefore( 5, 'abc' ), ( 4, 4 ), False, False )
		self._rangeTest( '0123456789', ( 4, 5 ), lambda x: x.insertBefore( 5, 'abc' ), ( 4, 5 ), False, False )
		self._rangeTest( '0123456789', ( 4, 6 ), lambda x: x.insertBefore( 5, 'abc' ), ( 4, 9 ), True, True )
		self._rangeTest( '0123456789', ( 4, 8 ), lambda x: x.insertBefore( 5, 'abc' ), ( 4, 11 ), True, True )
		
		self._rangeTest( '0123456789', ( 5, 5 ), lambda x: x.insertBefore( 5, 'abc' ), ( 5, 5 ), False, False )
		self._rangeTest( '0123456789', ( 5, 6 ), lambda x: x.insertBefore( 5, 'abc' ), ( 5, 9 ), True, True )
		self._rangeTest( '0123456789', ( 5, 8 ), lambda x: x.insertBefore( 5, 'abc' ), ( 5, 11 ), True, True )
		
		self._rangeTest( '0123456789', ( 6, 6 ), lambda x: x.insertBefore( 5, 'abc' ), ( 9, 9 ), True, False )
		self._rangeTest( '0123456789', ( 6, 8 ), lambda x: x.insertBefore( 5, 'abc' ), ( 9, 11 ), True, False )
		
		self._rangeTest( '0123456789', ( 8, 8 ), lambda x: x.insertBefore( 5, 'abc' ), ( 11, 11 ), True, False )
		
		
	def testInsertAfter(self):
		self._rangeTest( '0123456789', ( 1, 1 ), lambda x: x.insertAfter( 4, 'abc' ), ( 1, 1 ), False, False )
		self._rangeTest( '0123456789', ( 1, 3 ), lambda x: x.insertAfter( 4, 'abc' ), ( 1, 3 ), False, False )
		self._rangeTest( '0123456789', ( 1, 4 ), lambda x: x.insertAfter( 4, 'abc' ), ( 1, 4 ), False, False )
		self._rangeTest( '0123456789', ( 1, 5 ), lambda x: x.insertAfter( 4, 'abc' ), ( 1, 8 ), True, True )
		self._rangeTest( '0123456789', ( 1, 6 ), lambda x: x.insertAfter( 4, 'abc' ), ( 1, 9 ), True, True )
		self._rangeTest( '0123456789', ( 1, 8 ), lambda x: x.insertAfter( 4, 'abc' ), ( 1, 11 ), True, True )
		
		self._rangeTest( '0123456789', ( 3, 3 ), lambda x: x.insertAfter( 4, 'abc' ), ( 3, 3 ), False, False )
		self._rangeTest( '0123456789', ( 3, 4 ), lambda x: x.insertAfter( 4, 'abc' ), ( 3, 4 ), False, False )
		self._rangeTest( '0123456789', ( 3, 5 ), lambda x: x.insertAfter( 4, 'abc' ), ( 3, 8 ), True, True )
		self._rangeTest( '0123456789', ( 3, 6 ), lambda x: x.insertAfter( 4, 'abc' ), ( 3, 9 ), True, True )
		self._rangeTest( '0123456789', ( 3, 8 ), lambda x: x.insertAfter( 4, 'abc' ), ( 3, 11 ), True, True )
		
		self._rangeTest( '0123456789', ( 4, 4 ), lambda x: x.insertAfter( 4, 'abc' ), ( 4, 4 ), False, False )
		self._rangeTest( '0123456789', ( 4, 5 ), lambda x: x.insertAfter( 4, 'abc' ), ( 4, 8 ), True, True )
		self._rangeTest( '0123456789', ( 4, 6 ), lambda x: x.insertAfter( 4, 'abc' ), ( 4, 9 ), True, True )
		self._rangeTest( '0123456789', ( 4, 8 ), lambda x: x.insertAfter( 4, 'abc' ), ( 4, 11 ), True, True )

		self._rangeTest( '0123456789', ( 5, 5 ), lambda x: x.insertAfter( 4, 'abc' ), ( 8, 8 ), True, False )
		self._rangeTest( '0123456789', ( 5, 6 ), lambda x: x.insertAfter( 4, 'abc' ), ( 8, 9 ), True, False )
		self._rangeTest( '0123456789', ( 5, 8 ), lambda x: x.insertAfter( 4, 'abc' ), ( 8, 11 ), True, False )

		self._rangeTest( '0123456789', ( 6, 6 ), lambda x: x.insertAfter( 4, 'abc' ), ( 9, 9 ), True, False )
		self._rangeTest( '0123456789', ( 6, 8 ), lambda x: x.insertAfter( 4, 'abc' ), ( 9, 11 ), True, False )

		self._rangeTest( '0123456789', ( 8, 8 ), lambda x: x.insertAfter( 4, 'abc' ), ( 11, 11 ), True, False )

	def testRemove(self):
		self._rangeTest( '01234abc56789', ( 1, 1 ), lambda x: x.remove( 5, 8 ), ( 1, 1 ), False, False, '0123456789' )
		self._rangeTest( '01234abc56789', ( 1, 4 ), lambda x: x.remove( 5, 8 ), ( 1, 4 ), False, False, '0123456789' )
		self._rangeTest( '01234abc56789', ( 1, 5 ), lambda x: x.remove( 5, 8 ), ( 1, 5 ), False, False, '0123456789' )
		self._rangeTest( '01234abc56789', ( 1, 6 ), lambda x: x.remove( 5, 8 ), ( 1, 5 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 1, 7 ), lambda x: x.remove( 5, 8 ), ( 1, 5 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 1, 8 ), lambda x: x.remove( 5, 8 ), ( 1, 5 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 1, 9 ), lambda x: x.remove( 5, 8 ), ( 1, 6 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 1, 11 ), lambda x: x.remove( 5, 8 ), ( 1, 8 ), True, True, '0123456789' )

		self._rangeTest( '01234abc56789', ( 4, 4 ), lambda x: x.remove( 5, 8 ), ( 4, 4 ), False, False, '0123456789' )
		self._rangeTest( '01234abc56789', ( 4, 5 ), lambda x: x.remove( 5, 8 ), ( 4, 5 ), False, False, '0123456789' )
		self._rangeTest( '01234abc56789', ( 4, 6 ), lambda x: x.remove( 5, 8 ), ( 4, 5 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 4, 7 ), lambda x: x.remove( 5, 8 ), ( 4, 5 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 4, 8 ), lambda x: x.remove( 5, 8 ), ( 4, 5 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 4, 9 ), lambda x: x.remove( 5, 8 ), ( 4, 6 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 4, 11 ), lambda x: x.remove( 5, 8 ), ( 4, 8 ), True, True, '0123456789' )

		self._rangeTest( '01234abc56789', ( 5, 5 ), lambda x: x.remove( 5, 8 ), ( 5, 5 ), False, False, '0123456789' )
		self._rangeTest( '01234abc56789', ( 5, 6 ), lambda x: x.remove( 5, 8 ), ( 5, 5 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 5, 7 ), lambda x: x.remove( 5, 8 ), ( 5, 5 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 5, 8 ), lambda x: x.remove( 5, 8 ), ( 5, 5 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 5, 9 ), lambda x: x.remove( 5, 8 ), ( 5, 6 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 5, 11 ), lambda x: x.remove( 5, 8 ), ( 5, 8 ), True, True, '0123456789' )

		self._rangeTest( '01234abc56789', ( 6, 6 ), lambda x: x.remove( 5, 8 ), ( 5, 5 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 6, 7 ), lambda x: x.remove( 5, 8 ), ( 5, 5 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 6, 8 ), lambda x: x.remove( 5, 8 ), ( 5, 5 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 6, 9 ), lambda x: x.remove( 5, 8 ), ( 5, 6 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 6, 11 ), lambda x: x.remove( 5, 8 ), ( 5, 8 ), True, True, '0123456789' )

		self._rangeTest( '01234abc56789', ( 7, 7 ), lambda x: x.remove( 5, 8 ), ( 5, 5 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 7, 8 ), lambda x: x.remove( 5, 8 ), ( 5, 5 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 7, 9 ), lambda x: x.remove( 5, 8 ), ( 5, 6 ), True, True, '0123456789' )
		self._rangeTest( '01234abc56789', ( 7, 11 ), lambda x: x.remove( 5, 8 ), ( 5, 8 ), True, True, '0123456789' )

		self._rangeTest( '01234abc56789', ( 8, 8 ), lambda x: x.remove( 5, 8 ), ( 5, 5 ), True, False, '0123456789' )
		self._rangeTest( '01234abc56789', ( 8, 9 ), lambda x: x.remove( 5, 8 ), ( 5, 6 ), True, False, '0123456789' )
		self._rangeTest( '01234abc56789', ( 8, 11 ), lambda x: x.remove( 5, 8 ), ( 5, 8 ), True, False, '0123456789' )

		self._rangeTest( '01234abc56789', ( 9, 9 ), lambda x: x.remove( 5, 8 ), ( 6, 6 ), True, False, '0123456789' )
		self._rangeTest( '01234abc56789', ( 9, 11 ), lambda x: x.remove( 5, 8 ), ( 6, 8 ), True, False, '0123456789' )

		self._rangeTest( '01234abc56789', ( 11, 11 ), lambda x: x.remove( 5, 8 ), ( 8, 8 ), True, False, '0123456789' )

