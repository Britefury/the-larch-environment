##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pyparsing
import string

from Britefury.DocModel.DMList import DMList
from Britefury.DocModel.DMSymbol import DMSymbol
from Britefury.DocModel.DMString import DMString
from Britefury.DocModel.DMNull import DMNull
from Britefury.DocModel.DMNode import DMNode


"""gSym document model IO"""



## UNESCAPE HELPER

def _unescape(token):
	return token.replace( '\\"', '"' ).replace( "\\'", "'" ).replace( '\\\\', '\\' )



## PARSE ACTIONS

def _atomParseAction(tokens):
	token = tokens[0]

	try:
		v = float( token )
		return DMString( token, DMString.formatFloat )
	except ValueError:
		pass

	try:
		v = int( token )
		return DMString( token, DMString.formatInt )
	except ValueError:
		pass

	try:
		v = long( token )
		return DMString( token, DMString.formatLong )
	except ValueError:
		pass

	return DMSymbol( token )


def _nullParseAction():
	return DMNull()


def _quotedStringParseAction(tokens):
	token = tokens[0]
	return DMString( _unescape( token[1:-1] ), DMString.formatSingle )


def _dblQuotedStringParseAction(tokens):
	token = tokens[0]
	return DMString( _unescape( token[1:-1] ), DMString.formatDouble )


def _listParseAction(tokens):
	return tokens.asList()



_atom = pyparsing.Word( pyparsing.alphanums + '_+-*/%^&|!$@.,<>=[]' ).setParseAction( _atomParseAction )
_null = pyparsing.Literal( '`null`' ).setParseAction( _nullParseAction )
_quotedString = pyparsing.quotedString.setParseAction( _quotedStringParseAction )
_dblQuotedString = pyparsing.dblQuotedString.setParseAction( _dblQuotedStringParseAction )

_item = _atom | _null | _quotedString | _dblQuotedString

_sxp = pyparsing.Forward()
_sxList = pyparsing.Group( pyparsing.Suppress( '(' )  +  pyparsing.ZeroOrMore( _sxp )  +  pyparsing.Suppress( ')' ) ).setParseAction( _listParseAction )
_sxp << ( _item | _sxList )




def readSX(source):
	if isinstance( source, str ):
		parseResult = _sxp.parseString( source )
	else:
		parseResult = _sxp.parseFile( source )

	result = parseResult[0]

	if isinstance( result, list ):
		return DMList( result )
	else:
		return result


def writeSX(stream, content):
	content.__writesx__( stream )






import unittest



class TestCase_DMIO (unittest.TestCase):
	def _testRead(self, source, result):
		x = readSX( source )
		self.assert_( x == result )




	def testReadAtomFloat(self):
		self._testRead( '123.0', DMString( '123.0', DMString.formatFloat ) )

	def testReadAtomInt(self):
		self._testRead( '123', DMString( '123', DMString.formatInt ) )

	def testReadAtomLong(self):
		self._testRead( '123L', DMString( '123L', DMString.formatLong ) )

	def testReadAtomSym(self):
		self._testRead( 'abc123', DMSymbol( 'abc123' ) )

	def testReadNull(self):
		self._testRead( '`null`', DMNull() )

	def testReadQuotedString(self):
		self._testRead( "'abc 123'", DMString( 'abc 123', DMString.formatSingle ) )

	def testReadDblQuotedString(self):
		self._testRead( '"abc 123"', DMString( 'abc 123', DMString.formatDouble ) )

	def testReadList(self):
		self._testRead( '(f (g (h 1 2L 3.0) \'Hi\') "There")',  [ 'f', [ 'g', [ 'h', '1', '2L', '3.0' ], DMString( 'Hi', DMString.formatSingle ) ], DMString( 'There', DMString.formatDouble ) ] )




	def testWrite(self):
		import cStringIO

		h = DMList()
		h.extend( [ DMSymbol( 'h' ), DMString( '1', DMString.formatInt ), DMString( '2L', DMString.formatLong ), DMString( '3.0', DMString.formatFloat ) ] )
		g = DMList()
		g.extend( [ DMSymbol( 'g' ), h, DMString( 'Hi', DMString.formatSingle ) ] )
		f = DMList()
		f.extend( [ DMSymbol( 'f' ), g, DMString( 'There', DMString.formatDouble ) ] )

		stream = cStringIO.StringIO()
		writeSX( stream, f )
		self.assert_( stream.getvalue() == '(f (g (h 1 2L 3.0) \'Hi\') "There")' )




if __name__ == '__main__':
	unittest.main()
