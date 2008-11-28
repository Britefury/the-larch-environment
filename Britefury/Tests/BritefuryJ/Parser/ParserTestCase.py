##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import java.util.List

import string
import unittest
import cStringIO

import re

_whitespaceRegex = '[' + re.escape( string.whitespace ) + ']*'

def _deepList(xs):
	if isinstance( xs, java.util.List )  or  isinstance( xs, list ):
		return [ _deepList( x )   for x in xs ]
	else:
		return xs

class ParserTestCase (unittest.TestCase):
	def _cmpValue(self, x, y):
		x = _deepList( x )
		y = _deepList( y )
		#xstr = DMIOWrite.writeSX( x )
		#ystr = DMIOWrite.writeSX( y )
		#return xstr == ystr
		return x == y
			
	
	
	def _matchTest(self, parser, input, expected, ignoreChars=_whitespaceRegex):
		result = parser.parseString( input, ignoreChars )
		
		if not result.isValid():
			print 'PARSE FAILURE while parsing %s, stopped at %d: %s'  %  ( input, result.end, input[:result.end] )
			print 'EXPECTED:'
			print expected
		self.assert_( result.isValid() )
		
		value = result.value;

		if result.end != len( input ):
			print 'INCOMPLETE PARSE while parsing', input
			print 'Parsed %d/%d characters'  %  ( result.end, len( input ) )
			print input[:result.end]
			print 'EXPECTED:'
			print expected
			print 'RESULT:'
			print value

		bSame = self._cmpValue( value, expected )
		if not bSame:
			print 'EXPECTED:'
			print expected
			print ''
			print 'RESULT:'
			print str( value )
		self.assert_( bSame )



	def _matchTestSX(self, parser, input, expectedSX, ignoreChars=_whitespaceRegex):
		result = parser.parseString( input, ignoreChars )

		expected = DMIORead.readSX( expectedSX )
		
		if not result.isValid():
			print 'PARSE FAILURE while parsing', input
			print 'EXPECTED:'
			print expectedSX
		self.assert_( result.isValid() )

		value = result.value

		if result.end != len( input ):
			print 'INCOMPLETE PARSE while parsing', input
			print 'Parsed %d/%d characters'  %  ( result.end, len( input ) )
			print input[:result.end]
			print 'EXPECTED:'
			print expectedSX
			print 'RESULT:'
			print DMIOWrite.writeSX( value )

		bSame = self._cmpValue( value, expected )
		if not bSame:
			print 'EXPECTED:'
			print expectedSX
			print ''
			print 'RESULT:'
			print DMIOWrite.writeSX( value )
		self.assert_( bSame )

		
	def _matchFailTest(self, parser, input, ignoreChars=_whitespaceRegex):
		result = parser.parseString( input, ignoreChars )
		
		if result.isValid()   and   result.end == len( input ):
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print result.value
			print 'consumed %d/%d chars'  %  ( result.end, len( input ) )
		self.assert_( not result.isValid()  or  result.end != len( input ) )
