##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import weakref
import copy



_openTag = u'\ue000'
_startTag = u'\ue001'



def _tag(x):
	if isinstance( x, UnparsedText ):
		return _openTag + str( id( x ) ) + _startTag + x._text
	else:
		return x
	
	
def _text(x):
	if isinstance( x, UnparsedText ):
		return x._text
	else:
		return x
	
	


class UnparsedText (object):
	class _Entry (object):
		def __init__(self, text, position):
			super( UnparsedText._Entry, self ).__init__()
			self._text = text
			self._position = position
			
	def __init__(self, text, state=None):
		super( UnparsedText, self ).__init__()
		if isinstance( text, UnparsedText ):
			text = text._text
		self._text = text
		self._table = weakref.WeakKeyDictionary()
		self.state = state
			
			
	def associateWith(self, widget):
		self._table[widget] = self._Entry( self._text, 0 )
			
			
	def getText(self):
		return self._text
	
	def getTextForWidget(self, widget):
		return self._table[widget]._text
		
	def getPositionForWidget(self, widget):
		return self._table[widget]._position
	
	
	def join(self, items):
		itemTexts = [ _text( x )   for x in items ]
		resultText = self._text.join( itemTexts )
		
		result = UnparsedText( resultText, self.state )
		offset = 0
		for i, t in zip( items, itemTexts ):
			if isinstance( i, UnparsedText ):
				result._table.update( i._p_offset( offset )._table )
			offset += len( t )  +  len( self._text )
		return result
			

	
	def __add__(self, text):
		if isinstance( text, UnparsedText ):
			src2 = text._p_offset( len( self._text ) )
			text = src2._text
		else:
			src2 = None
		result = UnparsedText( self._text + text, self.state )
		result._table.update( self._table )
		if src2 is not None:
			result._table.update( src2._table )
		return result
		
		
	def __radd__(self, text):
		if isinstance( text, UnparsedText ):
			src1 = text
			text = src1._text
		else:
			src1 = None
		src2 = self._p_offset( len( text ) )
		
		result = UnparsedText( text + self._text, self.state )
		if src1 is not None:
			result._table.update( src1._table )
		result._table.update( src2._table )
		return result
	
	
	
	def __mod__(self, args):
		# Produce a copy of args, with all the UnparsedText objects replaced with tagged strings.
		if isinstance( args, tuple ):
			taggedArgs = tuple( [ _tag( a )   for a in args ] )
		elif isinstance( args, dict ):
			taggedArgs = {}
			for k, v in args.items():
				taggedArgs[k] = _tag( v )
		else:
			taggedArgs = _tag( args )
		
		# Produce the formatted string
		formatted = self._text  %  taggedArgs
		
		# Remove all tags from the formatted string, building a tag-id to index table as we go
		argIndices = {}
		index = 0
		try:
			while True:
				# Find the open and start tag
				openIndex = formatted.index( _openTag, index )
				startIndex = formatted.index( _startTag, index )
				# Get the ID
				idString = formatted[openIndex+1:startIndex]
				# Enter into the table
				argIndices.setdefault( int( idString ),  openIndex )
				# Remove the range openIndex:startIndex+1
				formatted = formatted[:openIndex]  +  formatted[startIndex+1:]
				index = openIndex
		except ValueError:
			pass
		
		# Extract the UnparsedText arguments
		unparsedSourceArgs = [ a   for a in args   if isinstance( a, UnparsedText ) ]
		
		# Construct the result UnparsedText object
		result = UnparsedText( formatted, self.state )
		
		# Join with arguments
		for a in unparsedSourceArgs:
			offset = argIndices[id(a)]
			result._table.update( a._p_offset( offset )._table )
		
		return result
	
	
	
	def _p_offset(self, offset):
		p = UnparsedText( self._text, self.state )
		for k, v in self._table.items():
			p._table[k] = self._Entry( v._text, v._position + offset )
		return p
	

	
	

	
import unittest

class TestCase_UnParseAs (unittest.TestCase):
	class TestObject (object):
		pass
	
	
	def testStr(self):
		w1 = self.TestObject()
		u1 = UnparsedText( 'hi there' )
		u1.associateWith( w1 )
		self.assert_( u1.getText() == 'hi there' )
		
		
	def testAdd_u_s(self):
		w1 = self.TestObject()
		u1 = UnparsedText( 'hi' )
		u1.associateWith( w1 )
		s2 = ' there'
		r = u1 + s2
		self.assert_( u1.getTextForWidget( w1 ) == 'hi' )
		self.assert_( u1.getPositionForWidget( w1 ) == 0 )
		self.assert_( r.getText() == 'hi there' )
		self.assert_( r.getTextForWidget( w1 ) == 'hi' )
		self.assert_( r.getPositionForWidget( w1 ) == 0 )
		
		
	def testAdd_u_u(self):
		w1 = self.TestObject()
		w2 = self.TestObject()
		u1 = UnparsedText( 'hi' )
		u1.associateWith( w1 )
		u2 = UnparsedText( ' there' )
		u2.associateWith( w2 )
		r = u1 + u2
		self.assert_( u1.getTextForWidget( w1 ) == 'hi' )
		self.assert_( u1.getPositionForWidget( w1 ) == 0 )
		self.assert_( u2.getTextForWidget( w2 ) == ' there' )
		self.assert_( u2.getPositionForWidget( w2 ) == 0 )
		self.assert_( r.getText() == 'hi there' )
		self.assert_( r.getTextForWidget( w1 ) == 'hi' )
		self.assert_( r.getPositionForWidget( w1 ) == 0 )
		self.assert_( r.getTextForWidget( w2 ) == ' there' )
		self.assert_( r.getPositionForWidget( w2 ) == 2 )
		
		
	def testRAdd_s_u(self):
		s1 = 'hi'
		w2 = self.TestObject()
		u2 = UnparsedText( ' there' )
		u2.associateWith( w2 )
		r = s1 + u2
		self.assert_( u2.getTextForWidget( w2 ) == ' there' )
		self.assert_( u2.getPositionForWidget( w2 ) == 0 )
		self.assert_( r.getText() == 'hi there' )
		self.assert_( r.getTextForWidget( w2 ) == ' there' )
		self.assert_( r.getPositionForWidget( w2 ) == 2 )
		
		
	def testRAdd_u_u(self):
		w1 = self.TestObject()
		w2 = self.TestObject()
		u1 = UnparsedText( 'hi' )
		u1.associateWith( w1 )
		u2 = UnparsedText( ' there' )
		u2.associateWith( w2 )
		r = u2.__radd__( u1 )
		self.assert_( u1.getTextForWidget( w1 ) == 'hi' )
		self.assert_( u1.getPositionForWidget( w1 ) == 0 )
		self.assert_( u2.getTextForWidget( w2 ) == ' there' )
		self.assert_( u2.getPositionForWidget( w2 ) == 0 )
		self.assert_( r.getText() == 'hi there' )
		self.assert_( r.getTextForWidget( w1 ) == 'hi' )
		self.assert_( r.getPositionForWidget( w1 ) == 0 )
		self.assert_( r.getTextForWidget( w2 ) == ' there' )
		self.assert_( r.getPositionForWidget( w2 ) == 2 )
		
	def testComplexAdd(self):
		w1 = self.TestObject()
		w2 = self.TestObject()
		w3 = self.TestObject()
		u1 = UnparsedText( 'x' )
		u1.associateWith( w1 )
		u2 = UnparsedText( 'y' )
		u2.associateWith( w2 )
		u3 = UnparsedText( 'z' )
		u3.associateWith( w3 )
		r = '( ' + u1 + ' * ' + u2 + ' * ' + u3 + ' )'
		self.assert_( u1.getTextForWidget( w1 ) == 'x' )
		self.assert_( u1.getPositionForWidget( w1 ) == 0 )
		self.assert_( u2.getTextForWidget( w2 ) == 'y' )
		self.assert_( u2.getPositionForWidget( w2 ) == 0 )
		self.assert_( u3.getTextForWidget( w3 ) == 'z' )
		self.assert_( u3.getPositionForWidget( w3 ) == 0 )
		self.assert_( r.getText() == '( x * y * z )' )
		self.assert_( r.getTextForWidget( w1 ) == 'x' )
		self.assert_( r.getPositionForWidget( w1 ) == 2 )
		self.assert_( r.getTextForWidget( w2 ) == 'y' )
		self.assert_( r.getPositionForWidget( w2 ) == 6 )
		self.assert_( r.getTextForWidget( w3 ) == 'z' )
		self.assert_( r.getPositionForWidget( w3 ) == 10 )
		
		
	def testFormat(self):
		w1 = self.TestObject()
		w2 = self.TestObject()
		w3 = self.TestObject()
		u1 = UnparsedText( 'x' )
		u1.associateWith( w1 )
		u2 = UnparsedText( 'y')
		u2.associateWith( w2 )
		u3 = UnparsedText( 'z' )
		u3.associateWith( w3 )
		r = UnparsedText( '( %s * %s / %d * %s )' )  %  ( u1, u2, 5, u3 )
		self.assert_( u1.getTextForWidget( w1 ) == 'x' )
		self.assert_( u1.getPositionForWidget( w1 ) == 0 )
		self.assert_( u2.getTextForWidget( w2 ) == 'y' )
		self.assert_( u2.getPositionForWidget( w2 ) == 0 )
		self.assert_( u3.getTextForWidget( w3 ) == 'z' )
		self.assert_( u3.getPositionForWidget( w3 ) == 0 )
		self.assert_( r.getText() == '( x * y / 5 * z )' )
		self.assert_( r.getTextForWidget( w1 ) == 'x' )
		self.assert_( r.getPositionForWidget( w1 ) == 2 )
		self.assert_( r.getTextForWidget( w2 ) == 'y' )
		self.assert_( r.getPositionForWidget( w2 ) == 6 )
		self.assert_( r.getTextForWidget( w3 ) == 'z' )
		self.assert_( r.getPositionForWidget( w3 ) == 14 )
		
		
	def testJoin(self):
		w1 = self.TestObject()
		w2 = self.TestObject()
		w3 = self.TestObject()
		u1 = UnparsedText( 'x' )
		u1.associateWith( w1 )
		u2 = UnparsedText( 'y')
		u2.associateWith( w2 )
		u3 = UnparsedText( 'z' )
		u3.associateWith( w3 )
		r = UnparsedText( ', ' ).join( [ u1, u2, u3 ] )
		self.assert_( u1.getTextForWidget( w1 ) == 'x' )
		self.assert_( u1.getPositionForWidget( w1 ) == 0 )
		self.assert_( u2.getTextForWidget( w2 ) == 'y' )
		self.assert_( u2.getPositionForWidget( w2 ) == 0 )
		self.assert_( u3.getTextForWidget( w3 ) == 'z' )
		self.assert_( u3.getPositionForWidget( w3 ) == 0 )
		self.assert_( r.getText() == 'x, y, z' )
		self.assert_( r.getTextForWidget( w1 ) == 'x' )
		self.assert_( r.getPositionForWidget( w1 ) == 0 )
		self.assert_( r.getTextForWidget( w2 ) == 'y' )
		self.assert_( r.getPositionForWidget( w2 ) == 3 )
		self.assert_( r.getTextForWidget( w3 ) == 'z' )
		self.assert_( r.getPositionForWidget( w3 ) == 6 )
		