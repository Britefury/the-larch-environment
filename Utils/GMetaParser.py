##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

import string
import operator

from Britefury.Parser.Parser import Literal, Word, RegEx, Sequence, Combine, FirstOf, BestOf, Optional, ZeroOrMore, OneOrMore, Production, Group, Forward, Suppress, identifier, quotedString, unicodeString, integer, floatingPoint, delimitedList

def _p(x, *args):
	print x, args
	return x


def _flatten(x):
	if len( x ) > 0:
		return reduce( operator.__add__, x )
	else:
		return []



_none = Production( Literal( 'None' ) ).setAction( lambda input, begin, end, token: '#None' )
_false = Production( Literal( 'False' ) ).setAction( lambda input, begin, end, token: '#False' )
_true = Production( Literal( 'True' ) ).setAction( lambda input, begin, end, token: '#True' )
_strLit = Production( unicodeString | quotedString ).setAction( lambda input, begin, end, token: "#'" + eval( token ) )
_intLit = Production( integer ).setAction( lambda input, begin, end, token: '#' + token )
_floatLit = Production( floatingPoint ).setAction( lambda input, begin, end, token: '#' + token )
_loadLocal = Production( identifier ).setAction( lambda input, begin, end, token: '@' + token )
_var = Production( identifier ).setAction( lambda input, begin, end, token: '@' + token )
_terminalExpr = Production( _floatLit | _intLit | _strLit | _none | _false | _true | _loadLocal )



_expression = Forward()

_compoundExpression = Production( Literal( '{' )  +  ZeroOrMore( ( _expression + Literal( ';' ) ).setAction( lambda input, begin, end, tokens: tokens[0] ) )  +  Literal( '}' ) ).setAction( lambda input, begin, end, tokens: tokens[1] )
_singleOrCompoundExpression = Production( _compoundExpression  |  ( _expression + ';' ).setAction( lambda input, begin, end, tokens: [ tokens[0] ] ) )

_listLiteral = Production( Literal( '[' )  +  delimitedList( _expression )  +  Literal( ']' ) ).setAction( lambda input, begin, end, tokens: [ '$list' ]  +  tokens[1] )

_setLiteral = Production( Literal( '[:' )  +  delimitedList( _expression )  +  Literal( ':]' ) ).setAction( lambda input, begin, end, tokens: [ '$set' ]  +  tokens[1] )

_lambdaExpression = Production( Literal( 'lambda' )  +  '('  +  delimitedList( _var )  +  ')'  +  '='  +  _compoundExpression ).setAction( lambda input, begin, end, tokens: [ '$lambda' ]  +  [ tokens[2] ] + tokens[5] )

_mapExpression = Production( Literal( 'map' )  +  '('  +  _expression + ',' + _expression + ')' ).setAction( lambda input, begin, end, tokens: [ '$map' ]  +  [ tokens[2], tokens[4] ] )

_filterExpression = Production( Literal( 'filter' )  +  '('  +  _expression + ',' + _expression + ')' ).setAction( lambda input, begin, end, tokens: [ '$filter' ]  +  [ tokens[2], tokens[4] ] )

_reduce1Expression = Production( Literal( 'reduce' )  +  '('  +  _expression + ',' + _expression + ')' ).setAction( lambda input, begin, end, tokens: [ '$reduce' ]  +  [ tokens[2], tokens[4] ] )
_reduce2Expression = Production( Literal( 'reduce' )  +  '('  +  _expression + ',' + _expression + ',' + _expression + ')' ).setAction( lambda input, begin, end, tokens: [ '$reduce' ]  +  [ tokens[2], tokens[4], tokens[6] ] )
_reduceExpression = _reduce2Expression | _reduce1Expression

_raiseExpression = Production( Literal( 'raise' )  +  '('  +  _expression + ')' ).setAction( lambda input, begin, end, tokens: [ '$raise' ]  +  [ tokens[2] ] )

_exceptBlock = Production( Literal( 'except' )  +  '('  +  _expression  +  ')'  +  _compoundExpression ).setAction( lambda input, begin, end, tokens: [ '$except' ]  +  [ tokens[2] ]  +  tokens[4] )
_elseBlock = Production( Literal( 'else' )  +  _compoundExpression ).setAction( lambda input, begin, end, tokens: [ '$else' ]  +  tokens[1] )
_finallyBlock = Production( Literal( 'finally' )  +  _compoundExpression ).setAction( lambda input, begin, end, tokens: [ '$finally' ]  +  tokens[1] )
_tryExpression = Production( Literal( 'try' )  +  _compoundExpression  +  OneOrMore( _exceptBlock )  +  Optional( _elseBlock )  +  Optional( _finallyBlock ) ).setAction( lambda input, begin, end, tokens: [ '$try', tokens[1] ] + tokens[2] + _flatten( tokens[3:] ) )

_elifBlock = Production( Literal( 'elif' )  +  '('  +  _expression  +  ')'  +  _compoundExpression ).setAction( lambda input, begin, end, tokens: [ tokens[2] ]  +  tokens[4] )
_ifExpression = Production( Literal( 'if' )  +  '('  +  _expression  +  ')'  +  _compoundExpression  +  ZeroOrMore( _elifBlock )  +  Optional( _elseBlock ) ).setAction( lambda input, begin, end, tokens: [ '$if', [ tokens[2] ] + tokens[4] ]  +  tokens[5]  +  _flatten( tokens[6:] ) )

_binding = Production( _var + Suppress( '=' ) + _expression + Suppress( ';' ) )

_whereExpression = Production( Literal( 'where' ) + '{' + ZeroOrMore( _binding ) + '}' + '->' + _compoundExpression ).setAction( lambda input, begin, end, tokens: [ '$where', tokens[2] ] + tokens[5] )

_moduleExpression = Production( Literal( 'module' ) + '{' + ZeroOrMore( _binding ) + '}' ).setAction( lambda input, begin, end, tokens: [ '$module' ]  +  tokens[2] )

# Pattern matching
_matchPattern = Forward()
_matchConstant = Production( quotedString ).setAction( lambda input, begin, end, token: eval( token ) )
_matchAnyString = Production( '!' )
_matchAnyList = Production( '/' )
_matchAnything = Production( '^' )
_matchList = Production( Literal( '(' )  +  ZeroOrMore( _matchPattern )  +  ')' ).setAction( lambda input, begin, end, tokens: tokens[1] )
_matchItemData = Production( _matchConstant | _matchAnyString | _matchAnyList | _matchAnything | _matchList )
_matchItem = Production( ( _matchItemData + '*' ).setAction( lambda input, begin, end, tokens: [ '*', tokens[0] ] )   |   \
			 ( _matchItemData + '+' ).setAction( lambda input, begin, end, tokens: [ '+', tokens[0] ] )   |   \
			 ( _matchItemData + '?' ).setAction( lambda input, begin, end, tokens: [ '?', tokens[0] ] )   |   \
			 _matchItemData )
_matchBind = Production( ( _matchItem + ':' + _var ).setAction( lambda input, begin, end, tokens: [ ':', tokens[2], tokens[0] ] )   |   _matchItem )
_matchPredicate = Production( ( _matchBind + '&' + _var + '=' + _expression + ';' ).setAction( lambda input, begin, end, tokens: [ '&', tokens[2], tokens[4], tokens[0] ] )   |   _matchBind )
_matchPattern  <<  _matchPredicate
_matchPair = Production( _matchPattern + '=>' + _compoundExpression ).setAction( lambda input, begin, end, tokens: [ tokens[0] ]  +  tokens[2] )
_matchExpression = Production( Literal( 'match' ) + '(' + _expression + ')' + '{' + ZeroOrMore( _matchPair ) + '}' ).setAction( lambda input, begin, end, tokens: [ '$match', tokens[2] ]  +  tokens[5] )
# End pattern matching

_expression  <<  Production( _listLiteral | _setLiteral | _lambdaExpression | _mapExpression | _filterExpression | _reduceExpression | _raiseExpression | _tryExpression | _ifExpression | _whereExpression | _moduleExpression | _matchExpression | _terminalExpr )

expression = _expression


import unittest
from Britefury.DocModel.DMIO import readSX
from Britefury.GLisp.GLispUtil import gLispSrcToString


class TestCase_GMetaParser (unittest.TestCase):
	def _matchTest(self, parser, input, expected, begin=None, end=None, ignoreChars=string.whitespace):
		expectedXs = readSX( expected )
		result = parser.parseString( input, ignoreChars=ignoreChars )
		self.assert_( result is not None )
		res = result.result
		if res != expectedXs:
			print 'EXPECTED:'
			print expectedXs
			print ''
			print 'RESULT:'
			print gLispSrcToString( res, 10 )
		self.assert_( expectedXs  ==  res )
		
		if result is not None:
			if begin is not None:
				self.assert_( begin == result.begin )
			if end is not None:
				self.assert_( end == result.end )
		
	
	def _matchFailTest(self, parser, input, ignoreChars=string.whitespace):
		result = parser.parseString( input, ignoreChars=ignoreChars )
		if result is not None   and   result.end == len( input ):
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print result
		self.assert_( result is None  or  result.end != len( input ) )

		
	def testTerminal(self):
		self._matchTest( expression, 'None', '#None' )
		self._matchTest( expression, 'False', '#False' )
		self._matchTest( expression, 'True', '#True' )
		self._matchTest( expression, "'abc'", "\"#'abc\"" )
		self._matchTest( expression, '123', '#123' )
		self._matchTest( expression, '3.14', '#3.14' )
		self._matchTest( expression, 'abc', '@abc' )
		
	def testListLiteral(self):
		self._matchTest( expression, '[ a, b, c ]', '($list @a @b @c)' )

	def testSetLiteral(self):
		self._matchTest( expression, '[: a, b, c :]', '($set @a @b @c)' )
		
	def testLambda(self):
		self._matchTest( expression, 'lambda(a,b,c)={[a,b,c];}', '($lambda (@a @b @c) ($list @a @b @c))' )
		
	def testMap(self):
		self._matchTest( expression, 'map( a, b )', '($map @a @b)' )
		
	def testFilter(self):
		self._matchTest( expression, 'filter( a, b )', '($filter @a @b)' )
		
	def testReduce(self):
		self._matchTest( expression, 'reduce( a, b )', '($reduce @a @b)' )
		self._matchTest( expression, 'reduce( a, b, c )', '($reduce @a @b @c)' )
		
	def testRaise(self):
		self._matchTest( expression, 'raise( a )', '($raise @a)' )
		
	def testTry(self):
		self._matchTest( expression, 'try { a; } except (b) {c;}', '($try (@a) ($except @b @c))' )
		self._matchTest( expression, 'try { a; } except (b) {c;} except (d) {e;}', '($try (@a) ($except @b @c) ($except @d @e))' )
		self._matchTest( expression, 'try { a; } except (b) {c;} except (d) {e;}  else {f;}', '($try (@a) ($except @b @c) ($except @d @e) ($else @f))' )
		self._matchTest( expression, 'try { a; } except (b) {c;} except (d) {e;}  finally {f;}', '($try (@a) ($except @b @c) ($except @d @e) ($finally @f))' )
		
	def testIf(self):
		self._matchTest( expression, 'if (a) {b;}', '($if (@a @b))' )
		self._matchTest( expression, 'if (a) {b;} elif (c) {d;}', '($if (@a @b) (@c @d))' )
		self._matchTest( expression, 'if (a) {b;} elif (c) {d;} else {e;}', '($if (@a @b) (@c @d) ($else @e))' )
		self._matchTest( expression, 'if (a) {b;} else {e;}', '($if (@a @b) ($else @e))' )
		
	def testWhere(self):
		self._matchTest( expression, 'where { a=1; b=2; } -> { a; b; }',  '($where ((@a #1) (@b #2)) @a @b)' )

	def testModule(self):
		self._matchTest( expression, 'module { a=1; b=2; }',  '($module (@a #1) (@b #2))' )
		
	def testMatch(self):
		self._matchTest( expression, 'match (a) {}',  '($match @a)' )
		self._matchTest( expression, 'match (a) { ! => {a;} }',  '($match @a (! @a))' )
		self._matchTest( expression, 'match (a) { ! => {a;}  ^ => {b;} }',  '($match @a (! @a) (^ @b))' )
		self._matchTest( expression, 'match (a) { ^ => {a;} }',  '($match @a (^ @a))' )
		self._matchTest( expression, 'match (a) { / => {a;} }',  '($match @a (/ @a))' )
		self._matchTest( expression, 'match (a) { "abc" => {a;} }',  '($match @a (abc @a))' )
		self._matchTest( expression, 'match (a) { (! /) => {a;} }',  '($match @a ((! /) @a))' )
		self._matchTest( expression, 'match (a) { (!* /) => {a;} }',  '($match @a (((* !) /) @a))' )
		self._matchTest( expression, 'match (a) { (!+ /) => {a;} }',  '($match @a (((+ !) /) @a))' )
		self._matchTest( expression, 'match (a) { (!? /) => {a;} }',  '($match @a (((? !) /) @a))' )
		self._matchTest( expression, 'match (a) { (("a" !)* /) => {a;} }',  '($match @a (((* (a !)) /) @a))' )
		self._matchTest( expression, 'match (a) { (("a" !)+ /) => {a;} }',  '($match @a (((+ (a !)) /) @a))' )
		self._matchTest( expression, 'match (a) { (("a" !)? /) => {a;} }',  '($match @a (((? (a !)) /) @a))' )
		self._matchTest( expression, 'match (a) { (!:x /) => {a;} }',  '($match @a (((: @x !) /) @a))' )
		self._matchTest( expression, 'match (a) { (!*:x /) => {a;} }',  '($match @a (((: @x (* !)) /) @a))' )
		self._matchTest( expression, 'match (a) { (!&x=y; /) => {a;} }',  '($match @a (((& @x @y !) /) @a))' )
		self._matchTest( expression, 'match (a) { (!:q&x=y; /) => {a;} }',  '($match @a (((& @x @y (: @q !)) /) @a))' )
		self._matchTest( expression, 'match (a) { (("a" "b" "c"):q&x=y; /) => {a;} }',  '($match @a (((& @x @y (: @q (a b c))) /) @a))' )
		

	def testCompoundExpression(self):
		self._matchTest( _compoundExpression, '{ a; b; c; }', '(@a @b @c)' )

	def testSingleOrCompoundExpression(self):
		self._matchTest( _singleOrCompoundExpression, '{ a; b; c; }', '(@a @b @c)' )
		self._matchTest( _singleOrCompoundExpression, 'a;', '(@a)' )
		