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
	double quoted string
	reference: see below
	another list

tokens can be preceeded by a tag, see below


references:
	{identifier}

	identifier can be an index:
	 	an index is used to identify the entity being referenced. Indices are assigned to lists in the order that they appear, with a list taking an index after its contents.
		e.g.
				(a (b c d) e)
				(. (. . .)0 .)1

	identifier can be a name:  [a-zA-Z_] [a-zA-Z0-9]*
	 	the name must match a previously encountered tag



tags:
	{:identifier}

	identifier is a name:  [a-zA-Z_] [a-zA-Z0-9]*
		the token/item following the tag can be referenced using this name
"""



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

def _unquotedStringParseAction(tokens):
	return str( tokens[0] )


def _nullParseAction(tokens):
	return [ None ]


def _quotedStringParseAction(tokens):
	return eval( tokens[0] )


def _listParseAction(tokens):
	return _registerNode( DMList.DMList( tokens[0] ) )


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




_unquotedStringChars = ( string.digits + string.letters + string.punctuation ).replace( '(', '' ).replace( ')', '' ).replace( '\'', '' ).replace( '`', '' ).replace( '{', '' ).replace( '}', '' )


_unquotedString = pyparsing.Word( _unquotedStringChars ).setParseAction( _unquotedStringParseAction )
_quotedString = pyparsing.quotedString.setParseAction( _quotedStringParseAction )
_null = pyparsing.Literal( '`null`' ).setParseAction( _nullParseAction )

_item = _unquotedString | _quotedString | _null

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

	def testReadListWithRef(self):
		source = '(f (g (h 1 2L 3.0) \'Hi \') \' There\' {0})'
		x = readSX( source )
		self.assert_( x[3] is x[1][1] )

	def testReadListWithTags(self):
		source = '(f (g {:x}(h 1 2L 3.0) \'Hi \') \' There\' {x})'
		x = readSX( source )
		self.assert_( x[3] is x[1][1] )




if __name__ == '__main__':
	unittest.main()
