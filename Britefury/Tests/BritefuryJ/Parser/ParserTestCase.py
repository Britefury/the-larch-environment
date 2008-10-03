##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

import string
import unittest
from Britefury.DocModel.DMIO import readSX, writeSX
import cStringIO


class ParserTestCase (unittest.TestCase):
	def _matchTest(self, parser, input, expected, ignoreChars=string.whitespace):
		result = parser.parseString( input, ignoreChars )
		if result is None:
			print 'PARSE FAILURE while parsing %s, stopped at %d: %s'  %  ( input, pos, input[:pos] )
			print 'EXPECTED:'
			print expected
		self.assert_( result is not None )

		res = result.result

		if result.end != len( input ):
			print 'INCOMPLETE PARSE while parsing', input
			print 'Parsed %d/%d characters'  %  ( result.end, len( input ) )
			print input[:result.end]
			print 'EXPECTED:'
			print expected
			print 'RESULT:'
			print res

		if res != expected:
			print 'EXPECTED:'
			print expected
			print ''
			print 'RESULT:'
			print res
		self.assert_( res == expected )



	def _matchTestSX(self, parser, input, expectedSX, ignoreChars=string.whitespace):
		result = parser.parseString( input, ignoreChars )

		expected = readSX( expectedSX )

		if result is None:
			print 'PARSE FAILURE while parsing', input
			print 'EXPECTED:'
			print expectedSX
		self.assert_( result is not None )

		res = result.result

		if result.end != len( input ):
			print 'INCOMPLETE PARSE while parsing', input
			print 'Parsed %d/%d characters'  %  ( result.end, len( input ) )
			print input[:result.end]
			print 'EXPECTED:'
			print expectedSX
			print 'RESULT:'
			stream = cStringIO.StringIO()
			writeSX( stream, res )
			print stream.getvalue()

		if res != expected:
			print 'EXPECTED:'
			print expectedSX
			print ''
			print 'RESULT:'
			stream = cStringIO.StringIO()
			writeSX( stream, res )
			print stream.getvalue()
		self.assert_( res == expected )


	def _matchSubTest(self, parser, input, expected, begin=None, end=None, ignoreChars=string.whitespace):
		result = parser.parseString( input, ignoreChars )
		if result is None:
			print 'PARSE FAILURE while parsing', input
			print 'EXPECTED:'
			print expected
		self.assert_( result is not None )
		res = result.result
		if res != expected:
			print 'EXPECTED:'
			print expected
			print ''
			print 'RESULT:'
			print res
		self.assert_( res == expected )

		if result is not None:
			if begin is not None:
				self.assert_( begin == result.begin )
			if end is not None:
				self.assert_( end == result.end )


	def _matchFailTest(self, parser, input, ignoreChars=string.whitespace):
		result = parser.parseString( input, ignoreChars )
		if result is not None   and   result.end == len( input ):
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print result.result
			print 'consumed %d/%d chars'  %  ( result.end, len( input ) )
		self.assert_( result is None  or  result.end != len( input ) )
