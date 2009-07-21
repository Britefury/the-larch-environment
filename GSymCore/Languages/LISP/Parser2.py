##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

import string

from BritefuryJ.DocModel import DMList

from BritefuryJ.Parser import Action, Condition, Production, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot
from BritefuryJ.Parser.Utils.Tokens import identifier, decimalInteger, hexInteger, integer, singleQuotedString, doubleQuotedString, quotedString, floatingPoint

from Britefury.Tests.BritefuryJ.Parser.ParserTestCase import ParserTestCase


from Britefury.Grammar.Grammar import Grammar, Rule, RuleList



def _unquotedStringParseAction(tokens):
	return str( tokens[0] )


def _quotedStringParseAction(tokens):
	return eval( tokens[0] )


def _listParseAction(tokens):
	return DMList( tokens[0] )


def _p(xs):
	print xs
	return xs


_unquotedStringChars = ( string.digits + string.letters + string.punctuation ).replace( '(', '' ).replace( ')', '' ).replace( '\'', '' ).replace( '`', '' ).replace( '{', '' ).replace( '}', '' )

class LispGrammar (Grammar):
	@Rule
	def unicodeStringS(self):
		return ( ( Literal( 'u' )  |  Literal( 'U' ) ) + singleQuotedString ).action( lambda input, begin, end, xs, bindings: 'u' + xs[1] )
	
	@Rule
	def unicodeStringD(self):
		return ( ( Literal( 'u' )  |  Literal( 'U' ) ) + doubleQuotedString ).action( lambda input, begin, end, xs, bindings: 'u' + xs[1] )
	
	

	@Rule
	def unquotedString(self):
		return Word( _unquotedStringChars ).action( lambda input, begin, end, xs, bindings: xs )
	
	@Rule
	def _quotedString(self):
		return ( self.unicodeStringS()  |  self.unicodeStringD()  |  singleQuotedString  |  doubleQuotedString ).action( lambda input, begin, end, xs, bindings: eval( xs ) )
	
	@Rule
	def _list(self):
		return ( Literal( '(' )  +  ZeroOrMore( self.expression() )  +  Literal( ')' ) ).action( lambda input, begin, end, xs, bindings: xs[1] )
	
	@Rule
	def expression(self):
		return self._quotedString()  |  self.unquotedString()  |  self._list()



import unittest


class TestCase_LISPParser (ParserTestCase):
	def testString(self):
		g = LispGrammar()
		self._parseStringTest( g.expression(), 'abc', 'abc' )
		self._parseStringTest( g.expression(), "'abc'", 'abc' )
		self._parseStringTest( g.expression(), '"abc"', 'abc' )
		self._parseStringTest( g.expression(), "u'abc'", u'abc' )
		self._parseStringTest( g.expression(), 'u"abc"', u'abc' )

	def testList(self):
		g = LispGrammar()
		self._parseStringTest( g.expression(), '()', [] )
		self._parseStringTest( g.expression(), '(x)', [ 'x' ] )
		self._parseStringTest( g.expression(), '(())', [ [] ] )
		self._parseStringTest( g.expression(), '(x y z)', [ 'x', 'y', 'z' ] )
		self._parseStringTest( g.expression(), '(x (y (z())))', [ 'x', [ 'y', [ 'z', []]]] )


if __name__ == '__main__':
	res, pos, dot = parser.debugParseString( '(x y z)' )
	print dot



