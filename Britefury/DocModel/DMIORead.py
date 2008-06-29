##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pyparsing
import string

from Britefury.DocModel import DMList


"""
gSym document model IO


Uses basic S-expressions

lists are (...) as normal
tokens inside are:
	atom:   A-Z a-z 0-9 _+-*/%^&|!$@.,<>=[]~
	quoted string
	another list
"""



## PARSE ACTIONS

def _unquotedStringParseAction(tokens):
	return str( tokens[0] )


def _quotedStringParseAction(tokens):
	return eval( tokens[0] )


def _listParseAction(tokens):
	return DMList.DMList( tokens[0] )





_unquotedStringChars = ( string.digits + string.letters + string.punctuation ).replace( '(', '' ).replace( ')', '' ).replace( '\'', '' ).replace( '`', '' ).replace( '{', '' ).replace( '}', '' )


_unquotedString = pyparsing.Word( _unquotedStringChars ).setParseAction( _unquotedStringParseAction )
_quotedString = ( pyparsing.unicodeString  |  pyparsing.quotedString  |  pyparsing.dblQuotedString ).setParseAction( _quotedStringParseAction )

_item = _quotedString | _unquotedString

_sxp = pyparsing.Forward()
_sxList = pyparsing.Group( pyparsing.Suppress( '(' )  +  pyparsing.ZeroOrMore( _sxp )  +  pyparsing.Suppress( ')' ) ).setParseAction( _listParseAction )
_sxp << ( _item | _sxList )


def readSX(source):
	if isinstance( source, str )  or  isinstance( source, unicode ):
		parseResult = _sxp.parseString( source )
	else:
		parseResult = _sxp.parseFile( source )

	result = parseResult[0]

	return result







import unittest



class TestCase_DMIO (unittest.TestCase):
	def _testRead(self, source, result):
		x = readSX( source )
		self.assert_( x == result )




	def testReadUnquotedString(self):
		self._testRead( 'abc', 'abc' )

	def testReadQuotedString(self):
		self._testRead( "'abc 123'", 'abc 123' )

	def testReadUnicodeString(self):
		self._testRead( "u'\\u0107'", u'\u0107' )

	def testReadEmptyString(self):
		self._testRead( '""', '' )

	def testReadList(self):
		source = '(f (g (h 1 2L 3.0) \'Hi \') \' There\' u\'\\u0107\')'
		self._testRead( source,  [ 'f', [ 'g', [ 'h', '1', '2L', '3.0' ], 'Hi ' ], ' There', u'\u0107' ] )




if __name__ == '__main__':
	unittest.main()
