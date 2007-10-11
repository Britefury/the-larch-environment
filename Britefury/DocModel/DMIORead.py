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
	atom:   A-Z a-z 0.9 _+-*/%^&|!$@.,<>=[]~
	null:		`null`
	quoted string
	another list
"""



## PARSE ACTIONS

def _unquotedStringParseAction(tokens):
	return str( tokens[0] )


def _nullParseAction(tokens):
	return [ None ]


def _quotedStringParseAction(tokens):
	return eval( tokens[0] )


def _listParseAction(tokens):
	return DMList.DMList( tokens[0] )





_unquotedStringChars = ( string.digits + string.letters + string.punctuation ).replace( '(', '' ).replace( ')', '' ).replace( '\'', '' ).replace( '`', '' ).replace( '{', '' ).replace( '}', '' )


_unquotedString = pyparsing.Word( _unquotedStringChars ).setParseAction( _unquotedStringParseAction )
_quotedString = pyparsing.quotedString.setParseAction( _quotedStringParseAction )
_null = pyparsing.Literal( '`null`' ).setParseAction( _nullParseAction )

_item = _unquotedString | _quotedString | _null

_sxp = pyparsing.Forward()
_sxList = pyparsing.Group( pyparsing.Suppress( '(' )  +  pyparsing.ZeroOrMore( _sxp )  +  pyparsing.Suppress( ')' ) ).setParseAction( _listParseAction )
_sxp << ( _item | _sxList )


def readSX(source):
	if isinstance( source, str ):
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

	def testReadNull(self):
		self._testRead( '`null`', None )

	def testReadQuotedString(self):
		self._testRead( "'abc 123'", 'abc 123' )

	def testReadList(self):
		source = '(f (g (h 1 2L 3.0) \'Hi \') \' There\')'
		self._testRead( source,  [ 'f', [ 'g', [ 'h', '1', '2L', '3.0' ], 'Hi ' ], ' There' ] )




if __name__ == '__main__':
	unittest.main()
