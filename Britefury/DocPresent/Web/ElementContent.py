##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import weakref
import copy
import collections



_openTag = u'\ue000'
_startTag = u'\ue001'



def _tag(x):
	if isinstance( x, ElementContent ):
		return _openTag + str( id( x ) ) + _startTag + x._text
	else:
		return x
	
	
def _text(x):
	if isinstance( x, ElementContent ):
		return x._text
	else:
		return x
	
	


class ElementContent (object):
	class _Entry (object):
		def __init__(self, text, position):
			super( ElementContent._Entry, self ).__init__()
			self._text = text
			self._position = position
			
	def __init__(self, text):
		super( ElementContent, self ).__init__()
		if isinstance( text, ElementContent ):
			self._text = text._text
			self._childTable = copy.copy( text._childTable )
			self._bOnClient = text._bOnClient
		else:
			self._text = text
			self._childTable = []
			self._bOnClient = False
			
			
	def getText(self):
		return self._text
	
	def join(self, items):
		result = ElementContent()
		
		position = 0
		for x in items:
			if isinstance( x, ElementContent ):
				self._childTable.append( ( position, x ) )
			else:
				self._text += x
				position += len( x )
		itemTexts = [ _text( x )   for x in items ]
		resultText = self._text.join( itemTexts )
		
		result = ElementContent( resultText )
		offset = 0
		for i, t in zip( items, itemTexts ):
			if isinstance( i, ElementContent ):
				result._table.update( i._p_offset( offset )._table )
			offset += len( t )  +  len( self._text )
		return result
			

	
	def __add__(self, text):
		if isinstance( text, ElementContent ):
			src2 = text._p_offset( len( self._text ) )
			text = src2._text
		else:
			src2 = None
		result = ElementContent( self._text + text, self.state )
		result._table.update( self._table )
		if src2 is not None:
			result._table.update( src2._table )
		return result
		
		
	def __radd__(self, text):
		if isinstance( text, ElementContent ):
			src1 = text
			text = src1._text
		else:
			src1 = None
		src2 = self._p_offset( len( text ) )
		
		result = ElementContent( text + self._text, self.state )
		if src1 is not None:
			result._table.update( src1._table )
		result._table.update( src2._table )
		return result
	
	
	
	def __mod__(self, args):
		# Produce a copy of args, with all the ElementContent objects replaced with tagged strings.
		if isinstance( args, tuple ):
			taggedArgs = tuple( [ _tag( a )   for a in args ] )
			elementArgs = [ a   for a in args   if isinstance( a, ElementContent ) ]
		elif isinstance( args, dict ):
			taggedArgs = {}
			for k, v in args.items():
				taggedArgs[k] = _tag( v )
			elementArgs = [ a   for a in args.values()   if isinstance( a, ElementContent ) ]
		else:
			taggedArgs = _tag( args )
			if isinstance( args, ElementContent ):
				elementArgs = [ args ]
			else:
				elementArgs = []
		
		# Produce the formatted string
		taggedString = self._text  %  taggedArgs
		
		return self.__unTag( taggedString, elementArgs )
	
	
	
	def _p_offset(self, offset):
		p = ElementContent( self._text )
		for elem, pos in self.childTable:
			p._childTable.append( ( elem, pos + offset ) )
		return p
	

	
	def __unTag(self, taggedString, elementArgs):
		# Build an ID to element table
		idToElement = {}
		for a in elementArgs:
			idToElement[id(a)] = a

		# Remove all tags from the tagged string, building a tag-id to offset table as we go
		childTable = []
		index = 0
		try:
			while True:
				# Find the open and start tag
				openIndex = taggedString.index( _openTag, index )
				startIndex = taggedString.index( _startTag, index )
				# Get the ID
				idString = taggedString[openIndex+1:startIndex]
				a = idToElement[int(idString)]
				# Enter into the child table
				childTable.append( ( a, openIndex ) )
				# Remove the range openIndex:startIndex+1
				taggedString = taggedString[:openIndex]  +  taggedString[startIndex+1:]
				index = openIndex
		except ValueError:
			pass
		
		# Construct the result ElementContent object
		result = ElementContent( taggedString )
		result._childTable = list( reversed( childTable ) )
		
		return result
	
	

	
import unittest

class TestCase_UnParseAs (unittest.TestCase):
	class TestObject (object):
		pass
	
	
	def testStr(self):
		w1 = self.TestObject()
		u1 = ElementContent( 'hi there' )
		u1.associateWith( w1 )
		self.assert_( u1.getText() == 'hi there' )
		
		
	def testAdd_u_s(self):
		w1 = self.TestObject()
		u1 = ElementContent( 'hi' )
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
		u1 = ElementContent( 'hi' )
		u1.associateWith( w1 )
		u2 = ElementContent( ' there' )
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
		u2 = ElementContent( ' there' )
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
		u1 = ElementContent( 'hi' )
		u1.associateWith( w1 )
		u2 = ElementContent( ' there' )
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
		u1 = ElementContent( 'x' )
		u1.associateWith( w1 )
		u2 = ElementContent( 'y' )
		u2.associateWith( w2 )
		u3 = ElementContent( 'z' )
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
		u1 = ElementContent( 'x' )
		u1.associateWith( w1 )
		u2 = ElementContent( 'y')
		u2.associateWith( w2 )
		u3 = ElementContent( 'z' )
		u3.associateWith( w3 )
		r = ElementContent( '( %s * %s / %d * %s )' )  %  ( u1, u2, 5, u3 )
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
		u1 = ElementContent( 'x' )
		u1.associateWith( w1 )
		u2 = ElementContent( 'y')
		u2.associateWith( w2 )
		u3 = ElementContent( 'z' )
		u3.associateWith( w3 )
		r = ElementContent( ', ' ).join( [ u1, u2, u3 ] )
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
		