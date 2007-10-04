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


"""gSym document model IO


Uses basic S-expressions

lists are (...) as normal
tokens inside are:
	atom:   A-Z a-z 0.9 _+-*/%^&|!$@.,<>=[]~
	null:		`null`
	quoted string
	double quoted string
	reference: see below
	another list


references:
	{identifier}

	identifier can be an index:
	 	an index is used to identify the entity being referenced. Indices are assigned to items in the order that they appear, with a list taking an index after its contents.
		e.g.
				(a (b c d) e)
				(0 (1 2 3)<-4 5)<-6

"""



## UNESCAPE HELPER

def _unescape(token):
	return token.replace( '\\"', '"' ).replace( "\\'", "'" ).replace( '\\\\', '\\' )



## PARSER STATE

_nodesByIndex = None
_nodesByName = None


def _initParser():
	global _nodesByIndex, _nodesByName
	assert _nodesByIndex is None  and  _nodesByName is None
	_nodesByIndex = []
	_nodesByName = {}

def _shutdownParser():
	global _nodesByIndex, _nodesByName
	_nodesByIndex = None
	_nodesByName = None


def _registerNode(node):
	_nodesByIndex.append( node )
	return node

def _registerNodeByName(name, node):
	_nodesByName[name] = node
	return node


## PARSE ACTIONS

def _atomParseAction(tokens):
	token = tokens[0]

	try:
		v = float( token )
		return _registerNode( DMString( token, DMString.formatFloat ) )
	except ValueError:
		pass

	try:
		v = int( token )
		return _registerNode( DMString( token, DMString.formatInt ) )
	except ValueError:
		pass

	try:
		v = long( token )
		return _registerNode( DMString( token, DMString.formatLong ) )
	except ValueError:
		pass

	return _registerNode( DMSymbol( token ) )


def _nullParseAction():
	return _registerNode( DMNull() )


def _quotedStringParseAction(tokens):
	token = tokens[0]
	return _registerNode( DMString( _unescape( token[1:-1] ), DMString.formatSingle ) )


def _dblQuotedStringParseAction(tokens):
	token = tokens[0]
	return _registerNode( DMString( _unescape( token[1:-1] ), DMString.formatDouble ) )


def _listParseAction(tokens):
	return _registerNode( DMList( tokens[0] ) )


def _referenceParseAction(tokens):
	token = tokens[0]
	try:
		index = int( token )
		return _nodesByIndex[index]
	except ValueError:
		return _nodesByName[token]

def _tagParseAction(tokens):
	name = tokens[0]
	node = tokens[1]
	_registerNodeByName( name, node )
	return node




_atom = pyparsing.Word( pyparsing.alphanums + '_+-*/%^&|!$@.,<>=[]~' ).setParseAction( _atomParseAction )
_null = pyparsing.Literal( '`null`' ).setParseAction( _nullParseAction )
_quotedString = pyparsing.quotedString.setParseAction( _quotedStringParseAction )
_dblQuotedString = pyparsing.dblQuotedString.setParseAction( _dblQuotedStringParseAction )

_item = _atom | _null | _quotedString | _dblQuotedString

_reference = ( pyparsing.Suppress( '{' )  +  pyparsing.Word( pyparsing.alphanums )  +  pyparsing.Suppress( '}' ) ).setParseAction( _referenceParseAction )

_sxp = pyparsing.Forward()
_sxList = pyparsing.Group( pyparsing.Suppress( '(' )  +  pyparsing.ZeroOrMore( _sxp )  +  pyparsing.Suppress( ')' ) ).setParseAction( _listParseAction )
_dataItem = _item | _sxList | _reference
_tag = ( ( pyparsing.Suppress( '{:' )  +  pyparsing.Word( pyparsing.alphas, pyparsing.alphanums )  +  pyparsing.Suppress( '}' ) )  +  _dataItem ).setParseAction( _tagParseAction )
_sxp << ( _dataItem | _tag )


def readSX(source):
	_initParser()
	if isinstance( source, str ):
		parseResult = _sxp.parseString( source )
	else:
		parseResult = _sxp.parseFile( source )

	result = parseResult[0]

	_shutdownParser()
	return result







## WRITING FUNCTIONS

def writeSX(stream, content):
	content.__writesx__( stream, {} )






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
		source = '(f (g (h 1 2L 3.0) \'Hi\') "There")'
		self._testRead( source,  [ 'f', [ 'g', [ 'h', '1', '2L', '3.0' ], DMString( 'Hi', DMString.formatSingle ) ], DMString( 'There', DMString.formatDouble ) ] )

	def testReadListWithRef(self):
		source = '(f (g (h 1 2L 3.0) \'Hi\') "There" {6})'
		x = readSX( source )
		self.assert_( x[3] is x[1][1] )



	def testReadListWithTags(self):
		source = '(f (g {:x}(h 1 2L 3.0) \'Hi\') "There" {x})'
		x = readSX( source )
		self.assert_( x[3] is x[1][1] )




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



	def testWriteDiamond(self):
		import cStringIO

		sourceA = [ DMSymbol( '1' ), DMSymbol( '2' ), DMSymbol( '3' ) ]
		sourceB = [ DMSymbol( '5' ), DMSymbol( '6' ), sourceA, DMSymbol( '7' ), [ DMSymbol( '8' ), [ sourceA, DMSymbol( '9' ) ], DMSymbol( '10' ) ] ]
		b = DMList( sourceB )

		stream = cStringIO.StringIO()
		writeSX( stream, b )
		sourceText = stream.getvalue()
		self.assert_( sourceText =='(5 6 (1 2 3) 7 (8 ({5} 9) 10))' )

		x = readSX( sourceText )
		self.assert_( x[2] is x[4][1][0] )




if __name__ == '__main__':
	unittest.main()
