##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMNode
from BritefuryJ.DocModel import DMIOReader, DMIOWriter, DMSchemaResolver

import string
import unittest
import cStringIO

import re

_whitespaceRegex = '[ ]*'

def _deepList(xs):
	if isinstance( xs, java.util.List )  or  isinstance( xs, list ):
		return [ _deepList( x )   for x in xs ]
	else:
		return xs

class ParserTestCase (unittest.TestCase):
	class Resolver (DMSchemaResolver):
		def __init__(self):
			self.schemas = {}
			
		def getSchema(self, location):
			return self.schemas[location]
		
	resolver = Resolver()
	
	
	
	def __init__(self, *args, **kwargs):
		super( ParserTestCase, self ).__init__( *args, **kwargs )
				
	
	
	
	def _parseStringTest(self, parser, input, expected, ignoreChars=_whitespaceRegex):
		result = parser.traceParseStringChars( input, ignoreChars )
		
		if not result.isValid():
			print 'PARSE FAILURE while parsing %s, stopped at %d: %s'  %  ( input, result.end, input[:result.end] )
			print 'EXPECTED:'
			print expected
			self._onError( result )
		self.assert_( result.isValid() )
		
		value = DMNode.coerce( result.value )
		expected = DMNode.coerce( expected )

		if result.end != len( input ):
			print 'INCOMPLETE PARSE while parsing', input
			print 'Parsed %d/%d characters'  %  ( result.end, len( input ) )
			print input[:result.end]
			print 'EXPECTED:'
			print expected
			print 'RESULT:'
			print value
			self._onError( result )

		bSame = value == expected
		if not bSame:
			print 'While parsing', input
			print 'EXPECTED:'
			print expected
			print ''
			print 'RESULT:'
			print value
			self._onError( result )
		self.assert_( bSame )



	def _parseStringTestSX(self, parser, input, expectedSX, ignoreChars=_whitespaceRegex):
		result = parser.traceParseStringChars( input, ignoreChars )

		expected = DMIOReader.readFromString( expectedSX, self.resolver )
		
		if not result.isValid():
			print 'PARSE FAILURE while parsing', input
			print 'EXPECTED:'
			print expectedSX
			self._onError( result )
		self.assert_( result.isValid() )

		value = DMNode.coerce( result.value )
		expected = DMNode.coerce( expected )

		if result.end != len( input ):
			print 'INCOMPLETE PARSE while parsing', input
			print 'Parsed %d/%d characters'  %  ( result.end, len( input ) )
			print input[:result.end]
			print 'EXPECTED:'
			print expectedSX
			print 'RESULT:'
			print DMIOWriter.writeAsString( value )
			self._onError( result )

		bSame = value == expected
		if not bSame:
			print 'While parsing', input
			print 'EXPECTED:'
			print expectedSX
			print ''
			print 'RESULT:'
			print DMIOWriter.writeAsString( value )
			self._onError( result )
		self.assert_( bSame )

		
	def _parseStringFailTest(self, parser, input, ignoreChars=_whitespaceRegex):
		result = parser.traceParseStringChars( input, ignoreChars )
		
		if result.isValid()   and   result.end == len( input ):
			print 'While parsing', input
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print result.value
			print 'consumed %d/%d chars'  %  ( result.end, len( input ) )
			self._onError( result )
		self.assert_( not result.isValid()  or  result.end != len( input ) )

		
		
		
	def _parseNodeTest(self, parser, input, expected, ignoreChars=_whitespaceRegex):
		result = parser.traceParseNode( input, ignoreChars )
		
		if not result.isValid():
			print 'PARSE FAILURE while parsing %s'  %  ( input, )
			print 'EXPECTED:'
			print expected
			self._onError( result )
		self.assert_( result.isValid() )
		
		value = DMNode.coerce( result.value )
		expected = DMNode.coerce( expected )

		bSame = value == expected
		if not bSame:
			print 'While parsing', input
			print 'EXPECTED:'
			print expected
			print ''
			print 'RESULT:'
			print value
			self._onError( result )
		self.assert_( bSame )



	def _parseNodeTestSX(self, parser, inputXS, expectedSX, ignoreChars=_whitespaceRegex):
		input = DMIOReader.readFromString( inputXS, self.resolver )
		expected = DMIOReader.readFromString( expectedSX, self.resolver )
		
		result = parser.traceParseNode( input, ignoreChars )

		if not result.isValid():
			print 'PARSE FAILURE while parsing', input
			print 'EXPECTED:'
			print expectedSX
			self._onError( result )
		self.assert_( result.isValid() )

		value = DMNode.coerce( result.value )
		expected = DMNode.coerce( expected )

		bSame = value == expected
		if not bSame:
			print 'While parsing', input
			print 'EXPECTED:'
			print expectedSX
			print ''
			print 'RESULT:'
			print DMIOWriter.writeAsString( value )
			self._onError( result )
		self.assert_( bSame )

		
	def _parseNodeFailTest(self, parser, input, ignoreChars=_whitespaceRegex):
		result = parser.traceParseNode( input, ignoreChars )
		
		if result.isValid()   and   result.end == len( input ):
			print 'While parsing', input
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print result.value
			print 'consumed %d/%d chars'  %  ( result.end, len( input ) )
			self._onError( result )
		self.assert_( not result.isValid()  or  result.end != len( input ) )

		
	def _parseNodeFailTestSX(self, parser, inputSX, ignoreChars=_whitespaceRegex):
		input = DMIOReader.readFromString( inputXS, self.resolver )
	
		result = parser.traceParseNode( input, ignoreChars )
		
		if result.isValid()   and   result.end == len( input ):
			print 'While parsing', input
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print result.value
			print 'consumed %d/%d chars'  %  ( result.end, len( input ) )
			self._onError( result )
		self.assert_( not result.isValid()  or  result.end != len( input ) )

		
		
		
		
	def _parseListTest(self, parser, input, expected, ignoreChars=_whitespaceRegex):
		result = parser.traceParseListItems( input, ignoreChars )
		
		if not result.isValid():
			print 'PARSE FAILURE while parsing %s, stopped at %d: %s'  %  ( input, result.end, input[:result.end] )
			print 'EXPECTED:'
			print expected
			self._onError( result )
		self.assert_( result.isValid() )
		
		value = DMNode.coerce( result.value )
		expected = DMNode.coerce( expected )

		if result.end != len( input ):
			print 'INCOMPLETE PARSE while parsing', input
			print 'Parsed %d/%d items'  %  ( result.end, len( input ) )
			print input[:result.end]
			print 'EXPECTED:'
			print expected
			print 'RESULT:'
			print value
			self._onError( result )

		bSame = value == expected
		if not bSame:
			print 'While parsing', input
			print 'EXPECTED:'
			print expected
			print ''
			print 'RESULT:'
			print value
			self._onError( result )
		self.assert_( bSame )



	def _parseListTestSX(self, parser, inputSX, expectedSX, ignoreChars=_whitespaceRegex):
		input = DMIOReader.readFromString( inputSX, self.resolver )
		expected = DMIOReader.readFromString( expectedSX, self.resolver )
		
		result = parser.traceParseListItems( input, ignoreChars )

		if not result.isValid():
			print 'PARSE FAILURE while parsing', input
			print 'EXPECTED:'
			print expectedSX
			self._onError( result )
		self.assert_( result.isValid() )

		value = DMNode.coerce( result.value )
		expected = DMNode.coerce( expected )

		if result.end != len( input ):
			print 'INCOMPLETE PARSE while parsing', input
			print 'Parsed %d/%d items'  %  ( result.end, len( input ) )
			print input[:result.end]
			print 'EXPECTED:'
			print expectedSX
			print 'RESULT:'
			print DMIOWriter.writeAsString( value )
			self._onError( result )

		bSame = value == expected
		if not bSame:
			print 'While parsing', input
			print 'EXPECTED:'
			print expectedSX
			print ''
			print 'RESULT:'
			print DMIOWriter.writeAsString( value )
			self._onError( result )
		self.assert_( bSame )

		
	def _parseListFailTest(self, parser, input, ignoreChars=_whitespaceRegex):
		result = parser.traceParseListItems( input, ignoreChars )
		
		if result.isValid()   and   result.end == len( input ):
			print 'While parsing', input
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print result.value
			print 'consumed %d/%d items'  %  ( result.end, len( input ) )
			self._onError( result )
		self.assert_( not result.isValid()  or  result.end != len( input ) )

		
		
	def _parseListFailTestSX(self, parser, inputSX, ignoreChars=_whitespaceRegex):
		input = DMIOReader.readFromString( inputSX, self.resolver )

		result = parser.traceParseListItems( input, ignoreChars )
		
		if result.isValid()   and   result.end == len( input ):
			print 'While parsing', input
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print result.value
			print 'consumed %d/%d items'  %  ( result.end, len( input ) )
			self._onError( result )
		self.assert_( not result.isValid()  or  result.end != len( input ) )

		
		

		
		
		
	def _parseStreamTest(self, parser, input, expected, ignoreChars=_whitespaceRegex):
		result = parser.traceParseStreamItems( input, ignoreChars )
		
		if not result.isValid():
			print 'PARSE FAILURE while parsing %s, stopped at %d: %s'  %  ( input, result.end, input[:result.end] )
			print 'EXPECTED:'
			print expected
			self._onError( result )
		self.assert_( result.isValid() )
		
		value = DMNode.coerce( result.value )
		expected = DMNode.coerce( expected )

		if result.end != len( input ):
			print 'INCOMPLETE PARSE while parsing', input
			print 'Parsed %d/%d items'  %  ( result.end, len( input ) )
			print input[:result.end]
			print 'EXPECTED:'
			print expected
			print 'RESULT:'
			print value
			self._onError( result )

		bSame = value == expected
		if not bSame:
			print 'While parsing', input
			print 'EXPECTED:'
			print expected
			print ''
			print 'RESULT:'
			print value
			self._onError( result )
		self.assert_( bSame )



	def _parseStreamTestSX(self, parser, input, expectedSX, ignoreChars=_whitespaceRegex):
		expected = DMIOReader.readFromString( expectedSX, self.resolver )
		
		result = parser.traceParseListItems( input, ignoreChars )

		if not result.isValid():
			print 'PARSE FAILURE while parsing', input
			print 'EXPECTED:'
			print expectedSX
			self._onError( result )
		self.assert_( result.isValid() )

		value = DMNode.coerce( result.value )
		expected = DMNode.coerce( expected )

		if result.end != len( input ):
			print 'INCOMPLETE PARSE while parsing', input
			print 'Parsed %d/%d items'  %  ( result.end, len( input ) )
			print input[:result.end]
			print 'EXPECTED:'
			print expectedSX
			print 'RESULT:'
			print DMIOWriter.writeAsString( value )
			self._onError( result )

		bSame = value == expected
		if not bSame:
			print 'While parsing', input
			print 'EXPECTED:'
			print expectedSX
			print ''
			print 'RESULT:'
			print DMIOWriter.writeAsString( value )
			self._onError( result )
		self.assert_( bSame )

		
	def _parseStreamFailTest(self, parser, input, ignoreChars=_whitespaceRegex):
		result = parser.traceParseListItems( input, ignoreChars )
		
		if result.isValid()   and   result.end == len( input ):
			print 'While parsing', input
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print result.value
			print 'consumed %d/%d items'  %  ( result.end, len( input ) )
			self._onError( result )
		self.assert_( not result.isValid()  or  result.end != len( input ) )
		
		
		
	def _onError(self, parseResult):
		pass
	