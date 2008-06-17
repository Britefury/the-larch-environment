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

from Britefury.Parser.Parser import getErrorLine, parserCoerce, Bind, Action, Condition, Forward, Group, Production, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot
from Britefury.Parser.GrammarUtils.Tokens import identifier, quotedString, unicodeString, integer, floatingPoint
from Britefury.Parser.GrammarUtils.SeparatedList import separatedList
from Britefury.Parser.GrammarUtils.Operators import Prefix, Suffix, InfixLeft, InfixRight, buildOperatorParser
from Britefury.GLisp.GLispUtil import isGLispList, gLispSrcToStringPretty



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
	
	
def _unaryOpAction(input, begin, tokens):
	return [ tokens[1], tokens[0] ]


_gmetaKeywords = set( [ 'False', 'True', 'None', 'lambda', 'map', 'filter', 'reduce', 'try', 'except', 'finally', 'raise', 'if', 'elif', 'else', 'where', 'module', 'match', 'not', 'is', 'in', 'or', 'and', 'compilerCollection', 'defineCompiler', 'defineTokeniser',
			'key', 'accel', 'tokens', 'defineInteractor' ] )
	

_gmetaIdentifier = identifier  &  ( lambda input, pos, result: result not in _gmetaKeywords )
#_gmetaIdentifier = identifier

_none = Production( Literal( 'None' ).action( lambda input, begin, token: '#None' ) )
_false = Production( Literal( 'False' ).action( lambda input, begin, token: '#False' ) )
_true = Production( Literal( 'True' ).action( lambda input, begin, token: '#True' ) )
_strLit = Production( ( unicodeString | quotedString ).action( lambda input, begin, token: "#'" + eval( token ) ) )
_intLit = Production( integer.action( lambda input, begin, token: '#' + token ) )
_floatLit = Production( floatingPoint.action( lambda input, begin, token: '#' + token ) )
_loadLocal = Production( _gmetaIdentifier.action( lambda input, begin, token: '@' + token ) )
_var = Production( _gmetaIdentifier.action( lambda input, begin, token: '@' + token ) )
_terminalLiteral = Production( _floatLit | _intLit | _strLit | _none | _false | _true )
_methodName = copy( _gmetaIdentifier )
_paramName = Production( _gmetaIdentifier.action( lambda input, begin, token: ':' + token ) )
_attrName = copy( _gmetaIdentifier )



_expression = Forward()

_compoundExpression = Production( Literal( '{' )  +  ZeroOrMore( ( _expression + Literal( ';' ) ).action( lambda input, begin, tokens: tokens[0] ) )  +  Literal( '}' ) ).action( lambda input, begin, tokens: tokens[1] )
_singleOrCompoundExpression = Production( _compoundExpression  |  ( _expression + ';' ).action( lambda input, begin, tokens: [ tokens[0] ] ) )

_listLiteral = Production( Literal( '[' )  +  separatedList( _expression )  +  Literal( ']' ) ).action( lambda input, begin, tokens: [ '$list' ]  +  tokens[1] )

_setLiteral = Production( Literal( '[:' )  +  separatedList( _expression )  +  Literal( ':]' ) ).action( lambda input, begin, tokens: [ '$set' ]  +  tokens[1] )

_lambdaExpression = Production( Literal( 'lambda' )  +  '('  +  separatedList( _var )  +  ')'  +  '->'  +  _compoundExpression ).action( lambda input, begin, tokens: [ '$lambda' ]  +  [ tokens[2] ] + tokens[5] )

_mapExpression = Production( Literal( 'map' )  +  '('  +  _expression + ',' + _expression + ')' ).action( lambda input, begin, tokens: [ '$map' ]  +  [ tokens[2], tokens[4] ] )

_filterExpression = Production( Literal( 'filter' )  +  '('  +  _expression + ',' + _expression + ')' ).action( lambda input, begin, tokens: [ '$filter' ]  +  [ tokens[2], tokens[4] ] )

_reduce1Expression = Production( Literal( 'reduce' )  +  '('  +  _expression + ',' + _expression + ')' ).action( lambda input, begin, tokens: [ '$reduce' ]  +  [ tokens[2], tokens[4] ] )
_reduce2Expression = Production( Literal( 'reduce' )  +  '('  +  _expression + ',' + _expression + ',' + _expression + ')' ).action( lambda input, begin, tokens: [ '$reduce' ]  +  [ tokens[2], tokens[4], tokens[6] ] )
_reduceExpression = _reduce2Expression | _reduce1Expression

_raiseExpression = Production( Literal( 'raise' )  +  '('  +  _expression + ')' ).action( lambda input, begin, tokens: [ '$raise' ]  +  [ tokens[2] ] )

_exceptBlock = Production( Literal( 'except' )  +  '('  +  _expression  +  ')'  +  _compoundExpression ).action( lambda input, begin, tokens: [ '$except' ]  +  [ tokens[2] ]  +  tokens[4] )
_elseBlock = Production( Literal( 'else' )  +  _compoundExpression ).action( lambda input, begin, tokens: [ '$else' ]  +  tokens[1] )
_finallyBlock = Production( Literal( 'finally' )  +  _compoundExpression ).action( lambda input, begin, tokens: [ '$finally' ]  +  tokens[1] )
_tryExpression = Production( Literal( 'try' )  +  _compoundExpression  +  OneOrMore( _exceptBlock )  +  Optional( _elseBlock )  +  Optional( _finallyBlock ) ).action( lambda input, begin, tokens: [ '$try', tokens[1] ] + tokens[2] + (  [tokens[3] ]   if tokens[3] is not None   else  [] ) + (  [tokens[4] ]   if tokens[4] is not None   else  [] ) )

_elifBlock = Production( Literal( 'elif' )  +  '('  +  _expression  +  ')'  +  _compoundExpression ).action( lambda input, begin, tokens: [ tokens[2] ]  +  tokens[4] )
_ifExpression = Production( Literal( 'if' )  +  '('  +  _expression  +  ')'  +  _compoundExpression  +  ZeroOrMore( _elifBlock )  +  Optional( _elseBlock ) ).action( lambda input, begin, tokens: [ '$if', [ tokens[2] ] + tokens[4] ]  +  tokens[5]  +  (  [tokens[6] ]   if tokens[6] is not None   else  [] ) )

_binding = Production( _var + Suppress( '=' ) + _expression + Suppress( ';' ) )

_whereExpression = Production( Literal( 'where' ) + '{' + ZeroOrMore( _binding ) + '}' + '->' + _compoundExpression ).action( lambda input, begin, tokens: [ '$where', tokens[2] ] + tokens[5] )

_moduleExpression = Production( Literal( 'module' ) + '{' + ZeroOrMore( _binding ) + '}' ).action( lambda input, begin, tokens: [ '$module' ]  +  tokens[2] )

# Pattern matching
def _matchBindPred(item):
	# Wraps an item in bind and predicate rules
	bind = Production( ( item + ':' + _var ).action( lambda input, begin, tokens: [ ':', tokens[2], tokens[0] ] )   |   item )
	pred = Production( ( bind + '&' + _var + '=' + _expression + ';' ).action( lambda input, begin, tokens: [ '&', tokens[2], tokens[4], tokens[0] ] )   |   bind )
	return pred

_matchList = Forward()
_matchConstant = Production( quotedString ).action( lambda input, begin, token: eval( token ) )
_matchAnyString = Production( '!' )
_matchAnyList = Production( '/' )
_matchAnything = Production( '^' )

_matchSublist = Production( Literal( '?' ) | Literal( '*' ) | Literal( '+' ) )

_matchListItem = Production( _matchConstant | _matchAnyString | _matchAnyList | _matchAnything | _matchList | _matchSublist )
_matchListItemBindPred = _matchBindPred( _matchListItem )

_matchList  <<  Production( Literal( '(' )  +  ZeroOrMore( _matchListItemBindPred )  +  ')' ).action( lambda input, begin, tokens: tokens[1] )

_matchItemData = Production( _matchConstant | _matchAnyString | _matchAnyList | _matchAnything | _matchList )
_matchPattern = _matchBindPred( _matchItemData )

_matchPair = Production( _matchPattern + '=>' + _compoundExpression ).action( lambda input, begin, tokens: [ tokens[0] ]  +  tokens[2] )
_matchExpression = Production( Literal( 'match' ) + '(' + _expression + ')' + '{' + ZeroOrMore( _matchPair ) + '}' ).action( lambda input, begin, tokens: [ '$match', tokens[2] ]  +  tokens[5] )
# End pattern matching


def _checkParams(input, begin, tokens):
	bKW = False
	for p in tokens:
		if isGLispList( p )  and  len( p ) == 2  and  p[0][0] == ':':
			bKW = True
		else:
			if bKW:
				raise GMetaParserError, 'normal parameters must not come after keyword parameters'
	return tokens

_kwParam = Production( _paramName + Suppress( '=' ) + _expression )
_parameterList = Production( Suppress( '(' )  -  separatedList( _kwParam | _expression )  -  Suppress( ')' ) ).action( _checkParams )


_parenExp = Production( Literal( '(' ) + _expression + ')' ).action( lambda input, begin, tokens: tokens[1] )



# SPECIALS

_compilerCollection = Production( Literal( 'compilerCollection' )  +  _compoundExpression ).action( lambda input, begin, tokens: [ '$compilerCollection' ]  +  tokens[1] )
_defineCompiler = Production( Literal( 'defineCompiler' )  +  _gmetaIdentifier  +  _gmetaIdentifier  +  Suppress( '->' )  +  _expression ).action( lambda input, begin, tokens: [ '$defineCompiler' ] + tokens[1:] )
_tokenDefinition = Production( _gmetaIdentifier  +  Suppress( ':=' )  +  _expression  +  Suppress( ';' ) )
_tokeniser = Production( Literal( 'defineTokeniser' )  +  '{' +  ZeroOrMore( _tokenDefinition )  +  '}' ).action( lambda input, begin, tokens: [ '$tokeniser' ] + tokens[2] )

_interactorEventKey = Production( Literal( 'key' )  +  '('  +  _expression  +  ')' ).action( lambda input, begin, tokens: [ '$key', tokens[2] ] )
_interactorEventAccel = Production( Literal( 'accel' )  +  '('  +  _expression  +  ')' ).action( lambda input, begin, tokens: [ '$accel', tokens[2] ] )
_interactorTokenWithBind = Production( _gmetaIdentifier + ':' + _var ).action( lambda input, begin, tokens: [ ':', tokens[2], tokens[0] ] )
_interactorTokenEntry = Production( _interactorTokenWithBind  |  _gmetaIdentifier )
_interactorEventTokens = Production( Literal( 'tokens' )  +  '('  +  separatedList( _interactorTokenEntry )  +  ')' ).action( lambda input, begin, tokens: [ '$tokens' ]  +  tokens[2] )
_interactorEvent = Production( _interactorEventKey  |  _interactorEventAccel  |  _interactorEventTokens )
_interactorMatch = Production( _interactorEvent  +  Suppress( '=>' )  +  _expression  +  Suppress( ';' ) )
_interactor = Production( Literal( 'defineInteractor' )  +  '{' +  ZeroOrMore( _interactorMatch )  +  '}' ).action( lambda input, begin, tokens: [ '$interactor' ] + tokens[2] )

_special = Production( _compilerCollection | _defineCompiler | _tokeniser | _interactor )


_enclosure = Production( _parenExp | _listLiteral | _setLiteral )
_keywords = Production( _lambdaExpression | _mapExpression | _filterExpression | _reduceExpression | _raiseExpression | _tryExpression | _ifExpression | _whereExpression | _moduleExpression | _matchExpression )
_atom = Production( _enclosure | _keywords | _special | _terminalLiteral | _loadLocal )
 
_primary = Forward()
_specialCall = Production( ( Suppress( '$' ) + _gmetaIdentifier + _parameterList ).action( lambda input, begin, tokens: [ '$' + tokens[0] ] + tokens[1] ) )
_call = Production( ( _primary + _parameterList ).action( lambda input, begin, tokens: [ tokens[0], '<-' ] + tokens[1] ) )
_subscript = Production( ( _primary + '[' + _expression + ']' ).action( lambda input, begin, tokens: [ tokens[0], '[]', tokens[2] ] ) )
_slice = Production( ( _primary + '[' + _expression + ':' + _expression + ']' ).action( lambda input, begin, tokens: [ tokens[0], '[:]', tokens[2], tokens[4] ] ) )
_getAttr = Production( _primary + '.' + _attrName )
_primary  <<  Production( _specialCall | _call | _subscript | _slice | _getAttr | _atom )


def _unary(op, x):
	return [ x, op ]

def _binary(op, a, b):
	return [ a, op, b ]


_or = buildOperatorParser( \
	[
		[ InfixRight( '**', _binary ) ],
		[ Prefix( '~', _unary ),   Prefix( '-', _unary ),   Prefix( 'not', _unary ) ],
		[ InfixLeft( '*', _binary ),   InfixLeft( '/', _binary ),   InfixLeft( '%', _binary ) ],
		[ InfixLeft( '+', _binary ),   InfixLeft( '-', _binary ) ],
		[ InfixLeft( '<<', _binary ),   InfixLeft( '>>', _binary ) ],
		[ InfixLeft( '&', _binary ) ],
		[ InfixLeft( '^', _binary ) ],
		[ InfixLeft( '|', _binary ) ],
		[ InfixLeft( '<', _binary ),   InfixLeft( '<=', _binary ),   InfixLeft( '==', _binary ),   InfixLeft( '!=', _binary ),   InfixLeft( '>=', _binary ),   InfixLeft( '>', _binary ) ],
		[ InfixLeft( Keyword( 'is' ) + Keyword( 'not' ), lambda op, a, b: [ [ a, 'is', b ], 'not' ] ),
		  	InfixLeft( Keyword( 'not' ) + Keyword( 'in' ), lambda op, a, b: [ [ a, 'in', b ], 'not' ] ),
			InfixLeft( 'is', _binary ),   InfixLeft( 'in', _binary ) ],
		[ InfixLeft( 'and', _binary ) ],
		[ InfixLeft( 'or', _binary ) ],
	], _primary )

#_power = Forward()
#_unary = Forward()

#_power  <<  Production( ( _primary  +  '**'  +  _unary )  |  _primary )
#_unary  <<  Production( ( ( Literal( '~' ) | '-' | 'not' )  +  _unary ).action( _unaryOpAction )  |  _power )

#_mulDivMod = Forward()
#_mulDivMod  <<  Production( ( _mulDivMod + ( Literal( '*' ) | '/' | '%' ) + _unary )  |  _unary )
#_addSub = Forward()
#_addSub  <<  Production( ( _addSub + ( Literal( '+' ) | '-' ) + _mulDivMod )  |  _mulDivMod )
#_shift = Forward()
#_shift  <<  Production( ( _shift + ( Literal( '<<' ) | '>>' ) + _addSub )  |  _addSub )
#_bitAnd = Forward()
#_bitAnd  <<  Production( ( _bitAnd + '&' + _shift )  |  _shift )
#_bitXor = Forward()
#_bitXor  <<  Production( ( _bitXor + '^' + _bitAnd )  |  _bitAnd )
#_bitOr = Forward()
#_bitOr  <<  Production( ( _bitOr + '|' + _bitXor)  |  _bitXor )
#_cmp = Forward()
#_cmp  <<  Production( ( _cmp + ( Literal( '<' ) | '<=' | '==' | '!=' | '>=' | '>' ) + _bitOr )  |  _bitOr )
#_isIn = Forward()
#_isIn  <<  Production( ( _isIn + 'is' + 'not' + _cmp ).action( lambda input, begin, tokens: [ [ tokens[0], tokens[1], tokens[3] ], 'not' ]  )  |  \
		       #( _isIn + 'not' + 'in' + _cmp ).action( lambda input, begin, tokens: [ [ tokens[0], tokens[2], tokens[3] ], 'not' ]  )  |  \
		     #( _isIn + 'is' + _cmp)  |  \
		     #( _isIn + 'in' + _cmp)  |  \
		     #_cmp )
#_and = Forward()
#_and  <<  Production( ( _and + 'and' + _isIn )  |  _isIn )
#_or = Forward()
#_or  <<  Production( ( _or + 'or' + _and )  |  _and )

#_expression  <<  Production( _terminalLiteral | _listLiteral | _setLiteral | _lambdaExpression | _mapExpression | _filterExpression | _reduceExpression | _raiseExpression | _tryExpression | _ifExpression | _whereExpression | _moduleExpression | _matchExpression | _loadLocal )
_expression  <<  Production( _or )

expression = _expression


import unittest
from Britefury.DocModel.DMIO import readSX
from Britefury.GLisp.GLispUtil import gLispSrcToString


class TestCase_GMetaParser (unittest.TestCase):
	def _matchTest(self, parser, input, expected, begin=None, end=None, ignoreChars=string.whitespace):
		expectedXs = readSX( expected )
		result, pos = parser.parseString( input, ignoreChars=ignoreChars )
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
		result, pos = parser.parseString( input, ignoreChars=ignoreChars )
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
		self._matchTest( expression, 'lambda(a,b,c) -> {[a,b,c];}', '($lambda (@a @b @c) ($list @a @b @c))' )
		
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
		self._matchTest( expression, 'match (a) { (* /) => {a;} }',  '($match @a ((* /) @a))' )
		self._matchTest( expression, 'match (a) { (+ /) => {a;} }',  '($match @a ((+ /) @a))' )
		self._matchTest( expression, 'match (a) { (? /) => {a;} }',  '($match @a ((? /) @a))' )
		self._matchTest( expression, 'match (a) { (!:x /) => {a;} }',  '($match @a (((: @x !) /) @a))' )
		self._matchTest( expression, 'match (a) { (*:x /) => {a;} }',  '($match @a (((: @x *) /) @a))' )
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
		
	def testSpecialCall(self):
		self._matchTest( expression, '$a(b)',  '($a @b)' )
		self._matchTest( expression, '$a(b,c)',  '($a @b @c)' )
		self._matchTest( expression, '$a(x=b)',  '($a (:x @b))' )
		self._matchTest( expression, '$a(x=b,y=c)',  '($a (:x @b) (:y @c))' )
		self._matchTest( expression, '$a(b,y=c)',  '($a @b (:y @c))' )

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

		
		
	def testCompilerCollection(self):
		self._matchTest( _compilerCollection, 'compilerCollection { a; b; }',  '($compilerCollection @a @b)' )
		
	def testDefineCompiler(self):
		self._matchTest( _defineCompiler, 'defineCompiler python25 ascii -> a',  '($defineCompiler python25 ascii @a)' )
		
	def testTokeniser(self):
		self._matchTest( _tokeniser, 'defineTokeniser { a := b; c := d; }',  '($tokeniser (a @b) (c @d))' )
		
	def testInteractor(self):
		self._matchTest( _interactor, """defineInteractor { key('a') => a; }""",  """($interactor (($key "#'a") @a))""" )
		self._matchTest( _interactor, """defineInteractor { accel( '<ctrl>8' )  =>  e; }""",  """($interactor (($accel "#'<ctrl>8") @e))""" )
		self._matchTest( _interactor, """defineInteractor { tokens( a, b, c ) => d; }""",  """($interactor (($tokens a b c) @d))""" )
		self._matchTest( _interactor, """defineInteractor { tokens( a:x ) => d; }""",  """($interactor (($tokens (: @x a)) @d))""" )
		
		
if __name__ == '__main__':
	source = \
"""
module
{
  compilers =
    compilerCollection
    {
      defineCompiler simple ascii ->
        where
        {
          compileNode =
            lambda (node) ->
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
        };
    };
    
  nodeViewFunction =
    where
    {
      divBoxStyle = $style( alignment='expand' );
      undefinedStyle = $style( colour=ColourRGB( 0.75, 0.0, 0.0 ), font='Sans 11 Italic' );
      identifierInitChars = string.letters + '_';
      identifierBodyChars = identifierInitChars + string.digits;
      tokeniser = defineTokeniser
        {
	  identifier := $tokWord( identifierInitChars, identifierBodyChars );
	  op_pow := $tokLiteral( '**' );
	  op_add := $tokLiteral( '+' );
	  op_sub := $tokLiteral( '-' );
	  op_mul := $tokLiteral( '*' );
	  op_div := $tokLiteral( '/' );
	  op_mod := $tokLiteral( '%' );
        };
      exprInteractor = lambda (node) ->
        {
	  defineInteractor
	  {
	    key( '+' )  =>  $replace( node, [ 'add', node, [ 'nilExpr' ] ] );
	    tokens( op_add )  =>  $replace( node, [ 'add', node, [ 'nilExpr' ] ] );

	    key( '-' )  =>  $replace( node, [ 'sub', node, [ 'nilExpr' ] ] );
	    tokens( op_sub )  =>  $replace( node, [ 'sub', node, [ 'nilExpr' ] ] );

	    key( '*' )  =>  $replace( node, [ 'mul', node, [ 'nilExpr' ] ] );
	    tokens( op_mul )  =>  $replace( node, [ 'mul', node, [ 'nilExpr' ] ] );

	    key( '/' )  =>  $replace( node, [ 'div', node, [ 'nilExpr' ] ] );
	    tokens( op_div )  =>  $replace( node, [ 'div', node, [ 'nilExpr' ] ] );

	    key( '%' )  =>  $replace( node, [ 'mod', node, [ 'nilExpr' ] ] );
	    tokens( op_mod )  =>  $replace( node, [ 'mod', node, [ 'nilExpr' ] ] );
	    
	    accel( '<ctrl>8' )  =>  $replace( node, [ 'pow', node, [ 'nilExpr' ] ] );
	    tokens( op_pow )  =>  $replace( node, [ 'pow', node, [ 'nilExpr' ] ] );
	  };
	};
      loadLocalInteractor = lambda (node) ->
        {
	  defineInteractor
	  {
	    tokens( identifier:name )  =>  $replace( node, [ 'loadLocal', name ] );
	  };
	};
      nilExprInteractor = lambda (node) ->
        {
	  defineInteractor
	  {
	    tokens( identifier:name )  =>  $replace( node, [ 'loadLocal', name ] );
	  };
	};
      operatorEditor = lambda (node, x, y, op) ->
        {
	  $interact( $activeBorder( $focus( $entry( op, op, tokeniser ) ) ),  operatorInteractor( node, x, y ) );
	};
      divideEditor = lambda (node, x, y) ->
        {
	  $interact( $focus( $customEntry( $hline(), '/', tokeniser ) ),  operatorInteractor( node, x, y ) );
	};
    }
    ->
    {
      lambda (node) ->
        {
	  match (node)
	  {
      	    ( "add" ^:x ^:y )   =>   { $interact( $activeBorder( $hbox( [ $viewEval( x ), operatorEditor( node, x, y, '+' ), $viewEval( y ) ] ) ),  exprInteractor( node ) ); }
      	    ( "sub" ^:x ^:y )   =>   { $interact( $activeBorder( $hbox( [ $viewEval( x ), operatorEditor( node, x, y, '-' ), $viewEval( y ) ] ) ),  exprInteractor( node ) ); }
      	    ( "mul" ^:x ^:y )   =>   { $interact( $activeBorder( $hbox( [ $viewEval( x ), operatorEditor( node, x, y, '*' ), $viewEval( y ) ] ) ),  exprInteractor( node ) ); }
      	    ( "div" ^:x ^:y )   =>   { $interact( $activeBorder( $vbox( [ $viewEval( x ), divideEditor( node, x, y ), $viewEval( y ) ], divBoxStyle ) ),  exprInteractor( node ) ); }
      	    ( "mod" ^:x ^:y )   =>   { $interact( $activeBorder( $hbox( [ $viewEval( x ), operatorEditor( node, x, y, '%' ), $viewEval( y ) ] ) ),  exprInteractor( node ) ); }
      	    ( "pow" ^:x ^:y )   =>   { $interact( $activeBorder( $scriptRSuper( $viewEval( x ), $hbox( [ operatorEditor( node, x, y, '**' ), $viewEval( y ) ] ) ) ),  exprInteractor( node ) ); }
      	    ( "loadLocal" !:x )   =>   { $interact( $activeBorder( $focus( $entry( x, x, tokeniser ))),  loadLocalInteractor( node ),  exprInteractor( node ) ); }
      	    ( "nilExpr" )   =>   { $interact( $activeBorder( $focus( $entry( '<expr>', '', tokeniser, undefinedStyle ))),  nilExprInteractor( node ) ); }
      	    ( "list" *:x )   =>   { $listView( listViewLayoutVertical( 0.0, 0.0, 45.0 ), '[', ']', ',', $mapViewEval( x ) ); }
	  };
	};
    };
}
"""

	
	
	def printError(errorName, pos, src):
		lineIndex, lineSrc = getErrorLine( src, pos )
		print '%s: line %d'  %  ( errorName, lineIndex + 1 )
		print lineSrc
		
	
	
	REPITITIONS = 1
	
	import time
	t1 = time.time()
	for i in xrange( 0, REPITITIONS ):
		res, pos = expression.parseString( source )
	t2 = time.time()
	if res is not None:
		if res.end != len( source ):
			printError( 'Parse error', pos, source )
		else:
			#print gLispSrcToStringPretty( res.result )
			pass
	else:
		printError( 'Syntax error', pos, source )
	print 'gMeta: Parsed %d chars in %s; %s chars per second'  %  ( len( source ) * REPITITIONS, t2 - t1, len(source)*REPITITIONS/(t2-t1) )
	
	
	
	SIMPLE_REPITIONS = 80
	
	rr = Forward()
	rr  <<  Production( ( Literal( '1' )  +  rr )  |  '1' )
	
	lr = Forward()
	lr  <<  Production( ( lr + '1' )  |  '1' )
	
	assert rr.parseString( '111111' )[0].end == 6
	assert lr.parseString( '111111' )[0].end == 6
	
	
	ones = '1' * 100
	t1 = time.time()
	for i in xrange( 0, SIMPLE_REPITIONS ):
		res, pos = rr.parseString( ones )
	t2 = time.time()
	print 'RR: parsed %d chars in %s; %s chars per second'  %  ( len( ones )*SIMPLE_REPITIONS, t2 - t1, len(ones)*SIMPLE_REPITIONS/(t2-t1) )
	
	t1 = time.time()
	for i in xrange( 0, SIMPLE_REPITIONS ):
		res, pos = lr.parseString( ones )
	t2 = time.time()
	print 'LR: parsed %d chars in %s; %s chars per second'  %  ( len( ones )*SIMPLE_REPITIONS, t2 - t1, len(ones)*SIMPLE_REPITIONS/(t2-t1) )
	