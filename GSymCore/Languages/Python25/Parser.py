##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

from Britefury.Parser.Parser import getErrorLine, parserCoerce, Bind, Action, Condition, Forward, Group, Production, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot, ParserTestCase
from Britefury.Parser.GrammarUtils.Tokens import identifier, decimalInteger, hexInteger, integer, singleQuotedString, doubleQuotedString, quotedString, floatingPoint
from Britefury.Parser.GrammarUtils.SeparatedList import separatedList, delimitedSeparatedList
from Britefury.Parser.GrammarUtils.Operators import Prefix, Suffix, InfixLeft, InfixRight, buildOperatorParser

from GSymCore.Languages.Python25.Keywords import *


pythonIdentifier = identifier  &  ( lambda input, pos, result: result not in keywordsSet )
	
asciiStringSLiteral = Production( singleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'ascii', 'single', xs[1:-1] ] )
asciiStringDLiteral = Production( doubleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'ascii', 'double', xs[1:-1] ] )
unicodeStringSLiteral = Production( Suppress( Literal( 'u' )  |  Literal( 'U' ) ) + singleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'unicode', 'single', xs[0][1:-1] ] )
unicodeStringDLiteral = Production( Suppress( Literal( 'u' )  |  Literal( 'U' ) ) + doubleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'unicode', 'double', xs[0][1:-1] ] )
regexAsciiStringSLiteral = Production( Suppress( Literal( 'r' )  |  Literal( 'R' ) ) + singleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'ascii-regex', 'single', xs[0][1:-1] ] )
regexAsciiStringDLiteral = Production( Suppress( Literal( 'r' )  |  Literal( 'R' ) ) + doubleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'ascii-regex', 'double', xs[0][1:-1] ] )
regexUnicodeStringSLiteral = Production( Suppress( Literal( 'ur' )  |  Literal( 'uR' )  |  Literal( 'Ur' )  |  Literal( 'UR' ) ) + singleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'unicode-regex', 'single', xs[0][1:-1] ] )
regexUnicodeStringDLiteral = Production( Suppress( Literal( 'ur' )  |  Literal( 'uR' )  |  Literal( 'Ur' )  |  Literal( 'UR' ) ) + doubleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'unicode-regex', 'double', xs[0][1:-1] ] )
shortStringLiteral = asciiStringSLiteral | asciiStringDLiteral | unicodeStringSLiteral | unicodeStringDLiteral |  \
				regexAsciiStringSLiteral | regexAsciiStringDLiteral | regexUnicodeStringSLiteral | regexUnicodeStringDLiteral



decimalIntLiteral = Production( decimalInteger ).action( lambda input, pos, xs: [ 'intLiteral', 'decimal', 'int', xs ] )
decimalLongLiteral = Production( decimalInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, pos, xs: [ 'intLiteral', 'decimal', 'long', xs[0] ] )
hexIntLiteral = Production( hexInteger ).action( lambda input, pos, xs: [ 'intLiteral', 'hex', 'int', xs ] )
hexLongLiteral = Production( hexInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, pos, xs: [ 'intLiteral', 'hex', 'long', xs[0] ] )

integerLiteral = hexLongLiteral | hexIntLiteral | decimalLongLiteral | decimalIntLiteral

floatLiteral = Production( floatingPoint ).action( lambda input, pos, xs: [ 'floatLiteral', xs ] )

imaginaryLiteral = Production( Combine( [ ( floatingPoint | decimalInteger ), Literal( 'j' ) ] ) ).action( lambda input, pos, xs: [ 'imaginaryLiteral', xs ] )


literal = shortStringLiteral | imaginaryLiteral | floatLiteral | integerLiteral


attrName = Production( pythonIdentifier )



expression = Forward()
oldExpression = Forward()
subscript = Forward()
attr = Forward()
tupleOrExpression = Forward()
oldTupleOrExpression = Forward()


# Target (assignment, for-loop, ...)
_target = Forward()

singleTarget = Production( pythonIdentifier ).action( lambda input, pos, xs: [ 'singleTarget', xs ] )
tupleTarget = Production( separatedList( _target, bNeedAtLeastOne=True, bAllowTrailingSeparator=True, bRequireTrailingSeparatorForLengthOne=True ) ).action( lambda input, pos, xs: [ 'tupleTarget' ] + xs )
targetList = ( tupleTarget  |  _target ).debug( 'targetList' )

parenTarget = Production( Literal( '(' )  +  targetList  +  Literal( ')' ) ).action( lambda input, pos, xs: xs[1] )
listTarget = Production( delimitedSeparatedList( _target, '[', ']', bAllowTrailingSeparator=True ) ).action( lambda input, pos, xs: [ 'listTarget' ]  +  xs )
_target  <<  ( subscript  |  attr  |  parenTarget  |  listTarget  |  singleTarget )



# Load local variable
loadLocal = Production( pythonIdentifier ).action( lambda input, begin, xs: [ 'var', xs ] )


# Tuples
tupleLiteral = Production( separatedList( expression, bNeedAtLeastOne=True, bAllowTrailingSeparator=True, bRequireTrailingSeparatorForLengthOne=True ) ).action( lambda input, pos, xs: [ 'tupleLiteral' ]  +  xs )
oldTupleLiteral = Production( separatedList( expression, bNeedAtLeastOne=True, bAllowTrailingSeparator=True, bRequireTrailingSeparatorForLengthOne=True ) ).action( lambda input, pos, xs: [ 'tupleLiteral' ]  +  xs )


# List literal
listLiteral = Production( delimitedSeparatedList( expression, '[', ']', bAllowTrailingSeparator=True ) ).action( lambda input, begin, xs: [ 'listLiteral' ] + xs )


# List comprehension
def _checkListComprehension(input, pos, xs):
	xs = xs[1]
	tag = None
	while isinstance( xs, list )  and  len( xs ) > 1  and  ( xs[0] == 'listFor'  or  xs[0] == 'listIf' ):
		tag = xs[0]
		xs = xs[1]
	return tag == 'listFor'
listIter = Forward()
listFor = Production( listIter  +  Keyword( forKeyword )  +  targetList  +  Keyword( inKeyword )  +  oldTupleOrExpression ).action( lambda input, pos, xs: [ 'listFor', xs[0], xs[2], xs[4] ] )
listIf = Production( listIter  +  Keyword( ifKeyword )  +  oldExpression ).action( lambda input, pos, xs: [ 'listIf', xs[0], xs[2] ] )
listIter  <<  Production( listFor | listIf | expression )
listComprehension = Production( Literal( '[' )  +  listIter  +  Literal( ']' ) ).condition( _checkListComprehension ).action( lambda input, pos, xs: [ 'listComprehension', xs[1] ] )

# List display
listDisplay = listLiteral  |  listComprehension




parenForm = Production( Literal( '(' ) + tupleOrExpression + ')' ).action( lambda input, begin, xs: xs[1] )

enclosure = Production( parenForm | listDisplay )

atom = Production( enclosure | literal | loadLocal )

primary = Forward()


argName = Production( pythonIdentifier )
kwArg = Production( argName + '=' + expression ).action( lambda input, begin, xs: [ 'kwArg', xs[0], xs[2] ] )
argList = Production( Literal( '*' )  +  expression ).action( lambda input, begin, xs: [ 'argList', xs[1] ] )
kwArgList = Production( Literal( '**' )  +  expression ).action( lambda input, begin, xs: [ 'kwArgList', xs[1] ] )
arg = Production( kwArgList | argList | kwArg | expression )
listOfArgs = Production( separatedList( arg ) )
call = Production( ( primary + Literal( '(' ) + listOfArgs + Literal( ')' ) ).action( lambda input, begin, tokens: [ 'call', tokens[0] ] + tokens[2] ) )


subscriptSlice = Production( ( expression + ':' + expression ).action( lambda input, begin, tokens: [ 'subscriptSlice', tokens[0], tokens[2] ] ) )
subscriptIndex = Production( subscriptSlice  |  expression )
subscript  <<  Production( ( primary + '[' + subscriptIndex + ']' ).action( lambda input, begin, tokens: [ 'subscript', tokens[0], tokens[2] ] ) )
attr  <<  Production( primary + '.' + attrName ).action( lambda input, begin, tokens: [ 'attr', tokens[0], tokens[2] ] )
primary  <<  Production( call | subscript | attr | atom )



orTest = buildOperatorParser( \
	[
		[ InfixRight( Literal( '**' ),  lambda op, x, y: [ 'pow', x, y ] ) ],
		[ Prefix( Literal( '~' ),  lambda op, x: [ 'invert', x ] ),   Prefix( Literal( '-' ),  lambda op, x: [ 'negate', x ] ),   Prefix( Literal( '+' ),  lambda op, x: [ 'pos', x ] ) ],
		[ InfixLeft( Literal( '*' ),  lambda op, x, y: [ 'mul', x, y ] ),   InfixLeft( Literal( '/' ),  lambda op, x, y: [ 'div', x, y ] ),   InfixLeft( Literal( '%' ),  lambda op, x, y: [ 'mod', x, y ] ) ],
		[ InfixLeft( Literal( '+' ),  lambda op, x, y: [ 'add', x, y ] ),   InfixLeft( Literal( '-' ),  lambda op, x, y: [ 'sub', x, y ] ) ],
		[ InfixLeft( Literal( '<<' ),  lambda op, x, y: [ 'lshift', x, y ] ),   InfixLeft( Literal( '>>' ),  lambda op, x, y: [ 'rshift', x, y ] ) ],
		[ InfixLeft( Literal( '&' ),  lambda op, x, y: [ 'bitAnd', x, y ] ) ],
		[ InfixLeft( Literal( '^' ),  lambda op, x, y: [ 'bitXor', x, y ] ) ],
		[ InfixLeft( Literal( '|' ),  lambda op, x, y: [ 'bitOr', x, y ] ) ],
		[
			InfixLeft( Literal( '<=' ),  lambda op, x, y: [ 'lte', x, y ] ),
			InfixLeft( Literal( '<' ),  lambda op, x, y: [ 'lt', x, y ] ),
			InfixLeft( Literal( '>=' ),  lambda op, x, y: [ 'gte', x, y ] ),
			InfixLeft( Literal( '>' ),  lambda op, x, y: [ 'gt', x, y ] ),
			InfixLeft( Literal( '==' ),  lambda op, x, y: [ 'eq', x, y ] ),
			InfixLeft( Literal( '!=' ),  lambda op, x, y: [ 'neq', x, y ] ),
		],
		[ InfixLeft( Keyword( isKeyword ) + Keyword( notKeyword ),  lambda op, x, y: [ 'cmpIsNot', x, y ] ),   InfixLeft( Keyword( isKeyword ),  lambda op, x, y: [ 'cmpIs', x, y ] ) ],
		[ InfixLeft( Keyword( notKeyword ) + Keyword( inKeyword ),  lambda op, x, y: [ 'cmpNotIn', x, y ] ),   InfixLeft( Keyword( inKeyword ),  lambda op, x, y: [ 'cmpIn', x, y ] ) ],
		[ Prefix( Keyword( notKeyword ),  lambda op, x: [ 'boolNot', x ] ) ],
		[ InfixLeft( Keyword( andKeyword ),  lambda op, x, y: [ 'boolAnd', x, y ] ) ],
		[ InfixLeft( Keyword( orKeyword ),  lambda op, x, y: [ 'boolOr', x, y ] ) ],
	],  primary )


paramName = pythonIdentifier
simpleParam = Production( pythonIdentifier.action( lambda input, begin, xs: [ 'simpleParam', xs[0] ] ) )
defaultValueParam = Production( paramName + '=' + expression ).action( lambda input, begin, xs: [ 'defaultValueParam', xs[0], xs[2] ] )
paramList = Production( Literal( '*' )  +  paramName ).action( lambda input, begin, xs: [ 'paramList', xs[1] ] )
kwParamList = Production( Literal( '**' )  +  paramName ).action( lambda input, begin, xs: [ 'kwParamList', xs[1] ] )
param = Production( kwParamList | paramList | defaultValueParam | simpleParam )
listOfParams = Production( separatedList( param ) )
lambdaExpr = Production( ( Keyword( lambdaKeyword )  +  listOfParams  +  Literal( ':' )  +  expression ).action( lambda input, pos, xs: [ 'lambdaExpr', xs[1], xs[3] ] ) )

			 
oldExpression  <<  Production( lambdaExpr  |  orTest )
expression  <<  Production( lambdaExpr  |  orTest )

tupleOrExpression  <<  ( tupleLiteral | expression )
oldTupleOrExpression  <<  ( oldTupleLiteral | oldExpression )


assignmentStatement = Production( pythonIdentifier  +  '='  +  tupleOrExpression ).action( lambda input, pos, xs: [ 'assignmentStmt', xs[0], xs[2] ] )
returnStatement = Production( Keyword( 'return' )  +  tupleOrExpression ).action( lambda input, pos, xs: [ 'returnStmt', xs[1] ] )
ifStatement = Production( Keyword( 'if' )  +  expression  +  ':' ).action( lambda input, pos, xs: [ 'ifStmt', xs[1], [] ] )


statement = Production( assignmentStatement  |  returnStatement  |  ifStatement  |  expression )




import unittest


class TestCase_Python25Parser (ParserTestCase):
	def test_shortStringLiteral(self):
		self._matchTest( expression, '\'abc\'', [ 'stringLiteral', 'ascii', 'single', 'abc' ] )
		self._matchTest( expression, '\"abc\"', [ 'stringLiteral', 'ascii', 'double', 'abc' ] )
		self._matchTest( expression, 'u\'abc\'', [ 'stringLiteral', 'unicode', 'single', 'abc' ] )
		self._matchTest( expression, 'u\"abc\"', [ 'stringLiteral', 'unicode', 'double', 'abc' ] )
		self._matchTest( expression, 'r\'abc\'', [ 'stringLiteral', 'ascii-regex', 'single', 'abc' ] )
		self._matchTest( expression, 'r\"abc\"', [ 'stringLiteral', 'ascii-regex', 'double', 'abc' ] )
		self._matchTest( expression, 'ur\'abc\'', [ 'stringLiteral', 'unicode-regex', 'single', 'abc' ] )
		self._matchTest( expression, 'ur\"abc\"', [ 'stringLiteral', 'unicode-regex', 'double', 'abc' ] )
		
		
	def test_integerLiteral(self):
		self._matchTest( expression, '123', [ 'intLiteral', 'decimal', 'int', '123' ] )
		self._matchTest( expression, '123L', [ 'intLiteral', 'decimal', 'long', '123' ] )
		self._matchTest( expression, '0x123', [ 'intLiteral', 'hex', 'int', '0x123' ] )
		self._matchTest( expression, '0x123L', [ 'intLiteral', 'hex', 'long', '0x123' ] )
	
		
	def test_floatLiteral(self):
		self._matchTest( expression, '123.0', [ 'floatLiteral', '123.0' ] )
	
		
	def test_imaginaryLiteral(self):
		self._matchTest( expression, '123.0j', [ 'imaginaryLiteral', '123.0j' ] )
	
		
	def testTargets(self):
		self._matchTest( targetList, 'a', [ 'singleTarget', 'a' ] )
		self._matchTest( targetList, '(a)', [ 'singleTarget', 'a' ] )
		
		self._matchTest( targetList, '(a,)', [ 'tupleTarget', [ 'singleTarget', 'a' ] ] )
		self._matchTest( targetList, 'a,b', [ 'tupleTarget', [ 'singleTarget', 'a' ],  [ 'singleTarget', 'b' ] ] )
		self._matchTest( targetList, '(a,b)', [ 'tupleTarget', [ 'singleTarget', 'a' ],  [ 'singleTarget', 'b' ] ] )
		self._matchTest( targetList, '(a,b,)', [ 'tupleTarget', [ 'singleTarget', 'a' ],  [ 'singleTarget', 'b' ] ] )
		self._matchTest( targetList, '(a,b),(c,d)', [ 'tupleTarget', [ 'tupleTarget', [ 'singleTarget', 'a' ], [ 'singleTarget', 'b' ] ], [ 'tupleTarget', [ 'singleTarget', 'c' ], [ 'singleTarget', 'd' ] ] ] )
		
		self._matchFailTest( targetList, '(a,) (b,)' )

		self._matchTest( targetList, '[a]', [ 'listTarget', [ 'singleTarget', 'a' ] ] )
		self._matchTest( targetList, '[a,]', [ 'listTarget', [ 'singleTarget', 'a' ] ] )
		self._matchTest( targetList, '[a,b]', [ 'listTarget', [ 'singleTarget', 'a' ],  [ 'singleTarget', 'b' ] ] )
		self._matchTest( targetList, '[a,b,]', [ 'listTarget', [ 'singleTarget', 'a' ],  [ 'singleTarget', 'b' ] ] )
		self._matchTest( targetList, '[a],[b,]', [ 'tupleTarget', [ 'listTarget', [ 'singleTarget', 'a' ] ], [ 'listTarget', [ 'singleTarget', 'b' ] ] ] )
		self._matchTest( targetList, '[(a,)],[(b,)]', [ 'tupleTarget', [ 'listTarget', [ 'tupleTarget', [ 'singleTarget', 'a' ] ] ], [ 'listTarget', [ 'tupleTarget', [ 'singleTarget', 'b' ] ] ] ] )
		
		
	def testListLiteral(self):
		self._matchTest( expression, '[a,b]', [ 'listLiteral', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._matchTest( expression, '[a,b,]', [ 'listLiteral', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		
		
	def testListComprehension(self):
		self._matchTest( expression, '[i  for i in a]', [ 'listComprehension',
									[ 'listFor', [ 'var', 'i' ], [ 'singleTarget', 'i' ], [ 'var', 'a' ] ] ] )
		self._matchFailTest( expression, '[i  if x]', )
		self._matchTest( expression, '[i  for i in a  if x]', [ 'listComprehension',
									[ 'listIf',
										[ 'listFor', [ 'var', 'i' ], [ 'singleTarget', 'i' ], [ 'var', 'a' ] ],
										[ 'var', 'x' ] ] ] )
		self._matchTest( expression, '[i  for i in a  for j in b]', [ 'listComprehension',
									[ 'listFor',
										[ 'listFor', [ 'var', 'i' ], [ 'singleTarget', 'i' ], [ 'var', 'a' ] ],
										[ 'singleTarget', 'j' ], [ 'var', 'b' ] ] ] )
		self._matchTest( expression, '[i  for i in a  if x  for j in b]', [ 'listComprehension',
									[ 'listFor',
										[ 'listIf',
											[ 'listFor', [ 'var', 'i' ], [ 'singleTarget', 'i' ], [ 'var', 'a' ] ],
										[ 'var', 'x' ] ],	
									[ 'singleTarget', 'j' ], [ 'var', 'b' ] ] ] )
		self._matchTest( expression, '[i  for i in a  if x  for j in b  if y]', [ 'listComprehension',
									[ 'listIf',
										[ 'listFor',
											[ 'listIf',
												[ 'listFor', [ 'var', 'i' ], [ 'singleTarget', 'i' ], [ 'var', 'a' ] ],
											[ 'var', 'x' ] ],	
										[ 'singleTarget', 'j' ], [ 'var', 'b' ] ],
									[ 'var', 'y' ] ] ] )
		

		
	def testTupleOrExpression(self):
		self._matchTest( tupleOrExpression, 'a', [ 'var', 'a' ] )
		self._matchTest( tupleOrExpression, 'a,b', [ 'tupleLiteral', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._matchTest( tupleOrExpression, 'a,2', [ 'tupleLiteral', [ 'var', 'a' ], [ 'intLiteral', 'decimal', 'int', '2' ] ] )
		self._matchTest( tupleOrExpression, 'lambda x, y: x+y,2', [ 'tupleLiteral', [ 'lambdaExpr', [ [ 'simpleParam', 'x' ], [ 'simpleParam', 'y' ] ], [ 'add', [ 'var', 'x' ], [ 'var', 'y' ] ] ], [ 'intLiteral', 'decimal', 'int', '2' ] ] )

		

if __name__ == '__main__':
	result, pos, dot = listComprehension.debugParseString( '[i  for i in a  if x]' )
	print dot
