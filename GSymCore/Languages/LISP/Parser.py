##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

import string

from Britefury.Parser.Parser import getErrorLine, parserCoerce, Bind, Action, Condition, Forward, Group, Production, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot, ParserTestCase
from Britefury.Parser.GrammarUtils.Tokens import identifier, decimalInteger, hexInteger, integer, singleQuotedString, doubleQuotedString, quotedString, floatingPoint




def _unquotedStringParseAction(tokens):
	return str( tokens[0] )


def _quotedStringParseAction(tokens):
	return eval( tokens[0] )


def _listParseAction(tokens):
	return DMList.DMList( tokens[0] )


def _p(xs):
	print xs
	return xs
unicodeStringS = Production( ( Literal( 'u' )  |  Literal( 'U' ) ) + singleQuotedString ).action( lambda input, pos, xs: 'u' + xs[1] )
unicodeStringD = Production( ( Literal( 'u' )  |  Literal( 'U' ) ) + doubleQuotedString ).action( lambda input, pos, xs: 'u' + xs[1] )


_unquotedStringChars = ( string.digits + string.letters + string.punctuation ).replace( '(', '' ).replace( ')', '' ).replace( '\'', '' ).replace( '`', '' ).replace( '{', '' ).replace( '}', '' )


unquotedString = Production( Word( _unquotedStringChars ) ).action( lambda input, pos, xs: xs )
_quotedString = Production( unicodeStringS  |  unicodeStringD  |  singleQuotedString  |  doubleQuotedString ).action( lambda input, pos, xs: eval( xs ) )

parser = Forward()
_list = Production( Literal( '(' )  +  ZeroOrMore( parser )  +  Literal( ')' ) ).action( lambda input, pos, xs: xs[1] )
parser  <<  Production( _quotedString | unquotedString | _list )



import unittest


class TestCase_LISPParser (ParserTestCase):
	def testString(self):
		self._matchTest( parser, 'abc', 'abc' )
		self._matchTest( parser, "'abc'", 'abc' )
		self._matchTest( parser, '"abc"', 'abc' )
		self._matchTest( parser, "u'abc'", u'abc' )
		self._matchTest( parser, 'u"abc"', u'abc' )

	def testList(self):
		self._matchTest( parser, '()', [] )
		self._matchTest( parser, '(x)', [ 'x' ] )
		self._matchTest( parser, '(())', [ [] ] )
		self._matchTest( parser, '(x y z)', [ 'x', 'y', 'z' ] )
		self._matchTest( parser, '(x (y (z())))', [ 'x', [ 'y', [ 'z', []]]] )


if __name__ == '__main__':
	res, pos, dot = parser.debugParseString( '(x y z)' )
	print dot



