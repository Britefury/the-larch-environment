##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

import string
import operator
from copy import copy

from Britefury.Parser.Parser import Literal, Word, RegEx, Sequence, Combine, FirstOf, BestOf, Optional, ZeroOrMore, OneOrMore, Production, Group, Forward, Suppress, identifier, quotedString, unicodeString, integer, floatingPoint, delimitedList

from Britefury.GLisp.GLispUtil import isGLispList



class GMetaParserError (Exception):
	pass


def _p(x, *args):
	print x, args
	return x


def _flatten(x):
	if len( x ) > 0:
		return reduce( operator.__add__, x )
	else:
		return []
	
	
def _unaryOpAction(input, begin, end, tokens):
	return [ tokens[1], tokens[0] ]
	

_none = Production( Literal( 'None' ) ).setAction( lambda input, begin, end, token: '#None' )
_false = Production( Literal( 'False' ) ).setAction( lambda input, begin, end, token: '#False' )
_true = Production( Literal( 'True' ) ).setAction( lambda input, begin, end, token: '#True' )
_strLit = Production( unicodeString | quotedString ).setAction( lambda input, begin, end, token: "#'" + eval( token ) )
_intLit = Production( integer ).setAction( lambda input, begin, end, token: '#' + token )
_floatLit = Production( floatingPoint ).setAction( lambda input, begin, end, token: '#' + token )
_loadLocal = Production( identifier ).setAction( lambda input, begin, end, token: '@' + token )
_var = Production( identifier ).setAction( lambda input, begin, end, token: '@' + token )
_terminalLiteral = Production( _floatLit | _intLit | _strLit | _none | _false | _true )
_methodName = copy( identifier )
_paramName = Production( identifier ).setAction( lambda input, begin, end, token: ':' + token )
_attrName = copy( identifier )



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


def _checkParams(input, begin, end, tokens):
	bKW = False
	for p in tokens:
		if isGLispList( p )  and  len( p ) == 2  and  p[0][0] == ':':
			bKW = True
		else:
			if bKW:
				raise GMetaParserError, 'normal parameters must not come after keyword parameters'
	return tokens

_kwParam = Production( _paramName + Suppress( '=' ) + _expression )
_parameterList = Production( Suppress( '(' )  -  delimitedList( _kwParam | _expression )  -  Suppress( ')' ) ).setAction( _checkParams )


_parenExp = Production( Literal( '(' ) + _expression + ')' ).setAction( lambda input, begin, end, tokens: tokens[1] )


_enclosure = Production( _parenExp | _listLiteral | _setLiteral )
_special = Production( _lambdaExpression | _mapExpression | _filterExpression | _reduceExpression | _raiseExpression | _tryExpression | _ifExpression | _whereExpression | _moduleExpression | _matchExpression )
_atom = Production( _enclosure | _special | _terminalLiteral | _loadLocal )
 
_primary = Forward()
_call = Production( ( _primary + _parameterList ).setAction( lambda input, begin, end, tokens: [ tokens[0], '<-' ] + tokens[1] ) )
_subscript = Production( ( _primary + '[' + _expression + ']' ).setAction( lambda input, begin, end, tokens: [ tokens[0], '[]', tokens[2] ] ) )
_slice = Production( ( _primary + '[' + _expression + ':' + _expression + ']' ).setAction( lambda input, begin, end, tokens: [ tokens[0], '[:]', tokens[2], tokens[4] ] ) )
_getAttr = Production( _primary + '.' + _attrName )
_primary  <<  Production( _call | _subscript | _slice | _getAttr | _atom )

_power = Forward()
_unary = Forward()

_power  <<  Production( ( _primary  +  '**'  +  _unary )  |  _primary )
_unary  <<  Production( ( ( Literal( '~' ) | '-' | 'not' )  +  _unary ).setAction( _unaryOpAction )  |  _power )

_mulDivMod = Forward()
_mulDivMod  <<  Production( ( _mulDivMod + ( Literal( '*' ) | '/' | '%' ) + _unary )  |  _unary )
_addSub = Forward()
_addSub  <<  Production( ( _addSub + ( Literal( '+' ) | '-' ) + _mulDivMod )  |  _mulDivMod )
_shift = Forward()
_shift  <<  Production( ( _shift + ( Literal( '<<' ) | '>>' ) + _addSub )  |  _addSub )
_bitAnd = Forward()
_bitAnd  <<  Production( ( _bitAnd + '&' + _shift )  |  _shift )
_bitXor = Forward()
_bitXor  <<  Production( ( _bitXor + '^' + _bitAnd )  |  _bitAnd )
_bitOr = Forward()
_bitOr  <<  Production( ( _bitOr + '|' + _bitXor)  |  _bitXor )
_cmp = Forward()
_cmp  <<  Production( ( _cmp + ( Literal( '<' ) | '<=' | '==' | '!=' | '>=' | '>' ) + _bitOr )  |  _bitOr )
_isIn = Forward()
_isIn  <<  Production( ( _isIn + 'is' + 'not' + _cmp ).setAction( lambda input, begin, end, tokens: [ [ tokens[0], tokens[1], tokens[3] ], 'not' ]  )  |  \
		       ( _isIn + 'not' + 'in' + _cmp ).setAction( lambda input, begin, end, tokens: [ [ tokens[0], tokens[2], tokens[3] ], 'not' ]  )  |  \
		     ( _isIn + 'is' + _cmp)  |  \
		     ( _isIn + 'in' + _cmp)  |  \
		     _cmp )
_and = Forward()
_and  <<  Production( ( _and + 'and' + _isIn )  |  _isIn )
_or = Forward()
_or  <<  Production( ( _or + 'or' + _and )  |  _and )

#_expression  <<  Production( _terminalLiteral | _listLiteral | _setLiteral | _lambdaExpression | _mapExpression | _filterExpression | _reduceExpression | _raiseExpression | _tryExpression | _ifExpression | _whereExpression | _moduleExpression | _matchExpression | _loadLocal )
_expression  <<  Production( _or )

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
		
		
	def testCall(self):
		self._matchTest( expression, 'a(b)',  '(@a <- @b)' )
		self._matchTest( expression, 'a(b,c)',  '(@a <- @b @c)' )
		self._matchTest( expression, 'a(b)(c)',  '((@a <- @b) <- @c)' )
		self._matchTest( expression, 'a(b)(c)(d)',  '(((@a <- @b) <- @c) <- @d)' )
		self._matchTest( expression, 'a(x=b)',  '(@a <- (:x @b))' )
		self._matchTest( expression, 'a(x=b,y=c)',  '(@a <- (:x @b) (:y @c))' )
		self._matchTest( expression, 'a(b,y=c)',  '(@a <- @b (:y @c))' )
		
	def testSubscript(self):
		self._matchTest( expression, 'a[b]',  '(@a [] @b)' )
		self._matchTest( expression, 'a[b][c]',  '((@a [] @b) [] @c)' )
		self._matchTest( expression, 'a[b][c][d]',  '(((@a [] @b) [] @c) [] @d)' )
		self._matchTest( expression, 'a[b](x)',  '((@a [] @b) <- @x)' )
		self._matchTest( expression, 'a(x)[b]',  '((@a <- @x) [] @b)' )
		self._matchTest( expression, 'a[b](x)[c]',  '(((@a [] @b) <- @x) [] @c)' )
		self._matchTest( expression, 'a(x)[b](y)',  '(((@a <- @x) [] @b) <- @y)' )
		self._matchTest( expression, 'a[b:c]',  '(@a [:] @b @c)' )
		

	def testGetAttr(self):
		self._matchTest( expression, 'a.i',  '(@a . i)' )
		self._matchTest( expression, 'a.i.j',  '((@a . i) . j)' )
		self._matchTest( expression, 'a(b).i',  '((@a <- @b) . i)' )
		self._matchTest( expression, 'a(b).i(c)',  '(((@a <- @b) . i) <- @c)' )
		self._matchTest( expression, 'a.i(b)',  '((@a . i) <- @b)' )
		self._matchTest( expression, 'a.i(b).j',  '(((@a . i) <- @b) . j)' )
		self._matchTest( expression, 'a[x].i',  '((@a [] @x) . i)' )
		self._matchTest( expression, 'a.i[x]',  '((@a . i) [] @x)' )
		
		
	def testExponent(self):
		self._matchTest( expression, 'a ** p',  '(@a ** @p)' )
		self._matchTest( expression, 'a ** p ** q',  '(@a ** (@p ** @q))' )
		self._matchTest( expression, 'a.i ** p',  '((@a . i) ** @p)' )
		self._matchTest( expression, 'a.i ** p ** q',  '((@a . i) ** (@p ** @q))' )
		self._matchTest( expression, 'a ** p.i',  '(@a ** (@p . i))' )
		self._matchTest( expression, 'a ** p.i ** q',  '(@a ** ((@p . i) ** @q))' )
		self._matchTest( expression, 'a ** p ** q.i',  '(@a ** (@p ** (@q . i)))' )
		self._matchTest( expression, 'a.i ** p.j',  '((@a . i) ** (@p . j))' )
		self._matchTest( expression, 'a ** p.j ** q',  '(@a ** ((@p . j) ** @q))' )
		
		
	def testInvertNegateNot(self):
		self._matchTest( expression, '~a',  '(@a ~)' )
		self._matchTest( expression, '~~a',  '((@a ~) ~)' )
		self._matchTest( expression, '~a**p',  '((@a ** @p) ~)' )
		self._matchTest( expression, '(~a)**p',  '((@a ~) ** @p)' )
		self._matchTest( expression, 'a**~p',  '(@a ** (@p ~))' )
		self._matchTest( expression, '-a',  '(@a -)' )
		self._matchTest( expression, '--a',  '((@a -) -)' )
		self._matchTest( expression, '-~a',  '((@a ~) -)' )
		self._matchTest( expression, '~-a',  '((@a -) ~)' )
		self._matchTest( expression, 'a**-p',  '(@a ** (@p -))' )
		self._matchTest( expression, 'not a',  '(@a not)' )
		self._matchTest( expression, 'not not a',  '((@a not) not)' )
		self._matchTest( expression, 'not ~a',  '((@a ~) not)' )
		self._matchTest( expression, '~not a',  '((@a not) ~)' )
		self._matchTest( expression, 'a**not p',  '(@a ** (@p not))' )
		
	
	def testMulDivMod(self):
		self._matchTest( expression, 'a*b',  '(@a * @b)' )
		self._matchTest( expression, 'a*b*c',  '((@a * @b) * @c)' )
		self._matchTest( expression, 'a**b*c',  '((@a ** @b) * @c)' )
		self._matchTest( expression, 'a*b**c',  '(@a * (@b ** @c))' )
		self._matchTest( expression, 'a**b*c**d',  '((@a ** @b) * (@c ** @d))' )
		self._matchTest( expression, 'a*b**c*d',  '((@a * (@b ** @c)) * @d)' )
		self._matchTest( expression, '-a*b**c*d',  '(((@a -) * (@b ** @c)) * @d)' )
		self._matchTest( expression, 'a*-b**c*d',  '((@a * ((@b ** @c) -)) * @d)' )
		self._matchTest( expression, 'a*b**-c*d',  '((@a * (@b ** (@c -))) * @d)' )
		self._matchTest( expression, 'a*b**c*-d',  '((@a * (@b ** @c)) * (@d -))' )
		self._matchTest( expression, 'a/b',  '(@a / @b)' )
		self._matchTest( expression, 'a%b',  '(@a % @b)' )
		
		
	def testAddSub(self):
		self._matchTest( expression, 'a+b',  '(@a + @b)' )
		self._matchTest( expression, 'a+b+c',  '((@a + @b) + @c)' )
		self._matchTest( expression, 'a*b+c',  '((@a * @b) + @c)' )
		self._matchTest( expression, 'a+b*c',  '(@a + (@b * @c))' )
		self._matchTest( expression, 'a*b+c*d',  '((@a * @b) + (@c * @d))' )
		self._matchTest( expression, 'a+b*c+d',  '((@a + (@b * @c)) + @d)' )

		
	def testIsIn(self):
		self._matchTest( expression, 'a is b',  '(@a is @b)' )
		self._matchTest( expression, 'a is not b',  '((@a is @b) not)' )
		self._matchTest( expression, 'a is b+x',  '(@a is (@b + @x))' )
		self._matchTest( expression, 'a+x is b',  '((@a + @x) is @b)' )
		self._matchTest( expression, 'a is not b+x',  '((@a is (@b + @x)) not)' )
		self._matchTest( expression, 'a+x is not b',  '(((@a + @x) is @b) not)' )
		self._matchTest( expression, 'a is (not b)',  '(@a is (@b not))' )
		self._matchTest( expression, 'not a is b',  '((@a not) is @b)' )

		self._matchTest( expression, 'a in b',  '(@a in @b)' )
		self._matchTest( expression, 'a not in b',  '((@a in @b) not)' )
		self._matchTest( expression, 'a in b+x',  '(@a in (@b + @x))' )
		self._matchTest( expression, 'a+x in b',  '((@a + @x) in @b)' )
		self._matchTest( expression, 'a not in b+x',  '((@a in (@b + @x)) not)' )
		self._matchTest( expression, 'a+x not in b',  '(((@a + @x) in @b) not)' )
		self._matchTest( expression, 'a in (not b)',  '(@a in (@b not))' )
		self._matchTest( expression, 'not a in b',  '((@a not) in @b)' )

		self._matchTest( expression, 'a in b is x',  '((@a in @b) is @x)' )
		self._matchTest( expression, 'a is b in x',  '((@a is @b) in @x)' )
		self._matchTest( expression, 'a not in b is x',  '(((@a in @b) not) is @x)' )
		self._matchTest( expression, 'a is not b in x',  '(((@a is @b) not) in @x)' )
		self._matchTest( expression, 'a in b is not x',  '(((@a in @b) is @x) not)' )
		self._matchTest( expression, 'a is b not in x',  '(((@a is @b) in @x) not)' )
		self._matchTest( expression, 'a not in b is not x',  '((((@a in @b) not) is @x) not)' )
		self._matchTest( expression, 'a is not b not in x',  '((((@a is @b) not) in @x) not)' )
		
		
	def testAndOr(self):
		self._matchTest( expression, 'a and b',  '(@a and @b)' )
		self._matchTest( expression, 'a and b and c',  '((@a and @b)  and @c)' )
		self._matchTest( expression, 'not a and b',  '((@a not) and @b)' )
		self._matchTest( expression, 'a and not b',  '(@a and (@b not))' )
		self._matchTest( expression, 'a and b in m',  '(@a and (@b in @m))' )
		self._matchTest( expression, 'a and b not in m',  '(@a and ((@b in @m) not))' )
		self._matchTest( expression, 'a is m and b',  '((@a is @m) and @b)' )
		self._matchTest( expression, 'a is not m and b',  '(((@a is @m) not) and @b)' )
		self._matchTest( expression, 'a or b',  '(@a or @b)' )

		
	def testCompoundExpression(self):
		self._matchTest( _compoundExpression, '{ a; b; c; }', '(@a @b @c)' )

	def testSingleOrCompoundExpression(self):
		self._matchTest( _singleOrCompoundExpression, '{ a; b; c; }', '(@a @b @c)' )
		self._matchTest( _singleOrCompoundExpression, 'a;', '(@a)' )

		
		
if __name__ == '__main__':
	source = \
"""
where
{
  compileNode =
    lambda (node) =
      {
        match (node)
	{
	  ( "add" ^:x ^:y )   =>   { '(' + compileNode(x) + ' + ' + compileNode( y ) + ')'; }
	  ( "sub" ^:x ^:y )   =>   { '(' + compileNode(x) + ' - ' + compileNode( y ) + ')'; }
	  ( "mul" ^:x ^:y )   =>   { '(' + compileNode(x) + ' * ' + compileNode( y ) + ')'; }
	  ( "div" ^:x ^:y )   =>   { '(' + compileNode(x) + ' / ' + compileNode( y ) + ')'; }
	  ( "pow" ^:x ^:y )   =>   { '(' + compileNode(x) + ' ** ' + compileNode( y ) + ')'; }
	  ( "loadLocal" !:x )   =>   { x; }
	  ( "undefinedExpr" )   =>   { '<UNDEFINED>'; }
	};
      };
}
->
{
  compileNode;
}
"""
	import time
	t1 = time.time()
	res = expression.parseString( source )
	t2 = time.time()
	if res is not None:
		if res.end != len( source ):
			print '<INCOMPLETE>'
		else:
			print gLispSrcToString( res.result, 100 )
	else:
		print '<FAIL>'
	print 'Parsed %d bytes in %s; %s chars per second'  %  ( len( source ), t2 - t1, len(source)/(t2-t1) )
	
	
	
	lr = Forward()
	lr  <<  Production( ( lr + '1' )  |  '1' )
	
	assert lr.parseString( '111111' ).end == 6
	
	
	print 'SIMPLE GRAMMAR'
	ones = '1' * 8000
	t1 = time.time()
	res = lr.parseString( ones )
	t2 = time.time()
	print 'ONES: parsed %d bytes in %s; %s chars per second'  %  ( len( ones ), t2 - t1, len(ones)/(t2-t1) )
	
	