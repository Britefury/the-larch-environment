##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

import string

from Britefury.Parser.Parser import getErrorLine, parserCoerce, Bind, Action, Condition, Forward, Group, Production, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot, ParserTestCase
from Britefury.Parser.GrammarUtils.Tokens import identifier, decimalInteger, hexInteger, integer, singleQuotedString, doubleQuotedString, quotedString, floatingPoint
from Britefury.Parser.GrammarUtils.SeparatedList import separatedList, delimitedSeparatedList
from Britefury.Parser.GrammarUtils.Operators import Prefix, Suffix, InfixLeft, InfixRight, buildOperatorParserWithAllLevels

from GSymCore.Languages.Python25.Keywords import *



#
#
#
# !!!!!! NOTES !!!!!!
# Comparison operators are NOT parsed correctly;
# 'a < b < c' is valid in Python, but is not handled here.
# The parser needs to be changed, in addition to changing the python document structure to reflect this.
#
# yieldExpr and yieldAtom are basically the same thing; find a way of unifying this.
#
#
#





# Python identifier
pythonIdentifier = identifier  &  ( lambda input, pos, result: result not in keywordsSet )
dottedPythonIdentifer = Production( separatedList( pythonIdentifier, '.', bNeedAtLeastOne=True ) ).action( lambda input, pos, xs: '.'.join( xs ) )
	


# String literal
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



# Integer literal
decimalIntLiteral = Production( decimalInteger ).action( lambda input, pos, xs: [ 'intLiteral', 'decimal', 'int', xs ] )
decimalLongLiteral = Production( decimalInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, pos, xs: [ 'intLiteral', 'decimal', 'long', xs[0] ] )
hexIntLiteral = Production( hexInteger ).action( lambda input, pos, xs: [ 'intLiteral', 'hex', 'int', xs ] )
hexLongLiteral = Production( hexInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, pos, xs: [ 'intLiteral', 'hex', 'long', xs[0] ] )
integerLiteral = hexLongLiteral | hexIntLiteral | decimalLongLiteral | decimalIntLiteral



# Float literal
floatLiteral = Production( floatingPoint ).action( lambda input, pos, xs: [ 'floatLiteral', xs ] )



# Imaginary literal
imaginaryLiteral = Production( Combine( [ ( floatingPoint | decimalInteger ), Literal( 'j' ) ] ) ).action( lambda input, pos, xs: [ 'imaginaryLiteral', xs ] )



# Literal
literal = shortStringLiteral | imaginaryLiteral | floatLiteral | integerLiteral



# Attribute name
attrName = Production( pythonIdentifier )



# Forward definitions for various components
expression = Forward()
oldExpression = Forward()
attributeRef = Forward()
subscript = Forward()
orTest = Forward()
tupleOrExpression = Forward()
oldTupleOrExpression = Forward()


# Target (assignment, for-loop, ...)
targetItem = Forward()

singleTarget = Production( pythonIdentifier ).action( lambda input, pos, xs: [ 'singleTarget', xs ] ).debug( 'singleTarget' )
tupleTarget = Production( separatedList( targetItem, bNeedAtLeastOne=True, bAllowTrailingSeparator=True, bRequireTrailingSeparatorForLengthOne=True ) ).action( lambda input, pos, xs: [ 'tupleTarget' ] + xs ).debug( 'tupleTarget' )
targetList = ( tupleTarget  |  targetItem ).debug( 'targetList' )

parenTarget = Production( Literal( '(' )  +  targetList  +  Literal( ')' ) ).action( lambda input, pos, xs: xs[1] ).debug( 'parenTarget' )
listTarget = Production( delimitedSeparatedList( targetItem, '[', ']', bAllowTrailingSeparator=True ) ).action( lambda input, pos, xs: [ 'listTarget' ]  +  xs ).debug( 'listTarget' )
#targetItem  <<  ( ( attributeRef  ^  subscript )  |  parenTarget  |  listTarget  |  singleTarget )
targetItem  <<  ( parenTarget  |  listTarget  |  singleTarget )



# Load local variable
loadLocal = Production( pythonIdentifier ).action( lambda input, pos, xs: [ 'var', xs ] ).debug( 'loadLocal' )



# Tuples
tupleLiteral = Production( separatedList( expression, bNeedAtLeastOne=True, bAllowTrailingSeparator=True, bRequireTrailingSeparatorForLengthOne=True ) ).action( lambda input, pos, xs: [ 'tupleLiteral' ]  +  xs ).debug( 'tupleLiteral' )
oldTupleLiteral = Production( separatedList( expression, bNeedAtLeastOne=True, bAllowTrailingSeparator=True, bRequireTrailingSeparatorForLengthOne=True ) ).action( lambda input, pos, xs: [ 'tupleLiteral' ]  +  xs ).debug( 'oldTupleLiteral' )



# Parentheses
parenForm = Production( Literal( '(' ) + tupleOrExpression + ')' ).action( lambda input, pos, xs: xs[1] ).debug( 'parenForm' )



# List literal
listLiteral = Production( delimitedSeparatedList( expression, '[', ']', bAllowTrailingSeparator=True ) ).action( lambda input, pos, xs: [ 'listLiteral' ] + xs ).debug( 'listLiteral' )



# List comprehension
listFor = Production( Keyword( forKeyword )  +  targetList  +  Keyword( inKeyword )  +  oldTupleOrExpression ).action( lambda input, pos, xs: [ 'listFor', xs[1], xs[3] ] ).debug( 'listFor' )
listIf = Production( Keyword( ifKeyword )  +  oldExpression ).action( lambda input, pos, xs: [ 'listIf', xs[1] ] ).debug( 'listIf' )
listComprehensionItem = listFor | listIf
listComprehension = Production( Literal( '[' )  +  expression  +  listFor  +  ZeroOrMore( listComprehensionItem )  +  Literal( ']' ) ).action( lambda input, pos, xs: [ 'listComprehension', xs[1], xs[2] ]  +  xs[3] ).debug( 'listComprehension' )



# Generator expression
genFor = Production( Keyword( forKeyword )  +  targetList  +  Keyword( inKeyword )  +  orTest ).action( lambda input, pos, xs: [ 'genFor', xs[1], xs[3] ] ).debug( 'genFor' )
genIf = Production( Keyword( ifKeyword )  +  oldExpression ).action( lambda input, pos, xs: [ 'genIf', xs[1] ] ).debug( 'genIf' )
generatorExpressionItem = genFor | genIf
generatorExpression = Production( Literal( '(' )  +  expression  +  genFor  +  ZeroOrMore( generatorExpressionItem )  +  Literal( ')' ) ).action( lambda input, pos, xs: [ 'generatorExpression', xs[1], xs[2] ]  +  xs[3] ).debug( 'generatorExpression' )



# Dictionary literal
keyValuePair = Production( expression  +  Literal( ':' )  +  expression ).action( lambda input, pos, xs: [ 'keyValuePair', xs[0], xs[2] ] ).debug( 'keyValuePair' )
dictLiteral = Production( delimitedSeparatedList( keyValuePair, '{', '}', bAllowTrailingSeparator=True ) ).action( lambda input, pos, xs: [ 'dictLiteral' ] + xs ).debug( 'dictLiteral' )



# Yield expression
yieldExpression = Production( Keyword( yieldKeyword )  +  expression ).action( lambda input, pos, xs: [ 'yieldExpr', xs[1] ] ).debug( 'yieldExpression' )
yieldAtom = Production( Literal( '(' )  +  Keyword( yieldKeyword )  +  expression  +  Literal( ')' ) ).action( lambda input, pos, xs: [ 'yieldAtom', xs[2] ] ).debug( 'yieldAtom' )



# Enclosure
enclosure = Production( parenForm | listLiteral | listComprehension | generatorExpression | dictLiteral | yieldExpression ).debug( 'enclosure' )



# Atom
atom = Production( enclosure | literal | loadLocal ).debug( 'atom' )



# forward def - primary
primary = Forward()



# Attribute ref
attributeRef  <<  Production( primary + '.' + attrName ).action( lambda input, pos, xs: [ 'attributeRef', xs[0], xs[2] ] ).debug( 'attributeRef' )



# Subscript and slice
_sliceItem = lambda x: x   if x is not None   else   '<nil>'
subscriptSlice = Production( ( Optional( expression ) + ':' + Optional( expression )  ).action( lambda input, pos, xs: [ 'subscriptSlice', _sliceItem( xs[0] ), _sliceItem( xs[2] ) ] ) ).debug( 'subscriptSlice' )
subscriptLongSlice = Production( ( Optional( expression )  + ':' + Optional( expression )  + ':' + Optional( expression )  ).action( lambda input, pos, xs: [ 'subscriptLongSlice', _sliceItem( xs[0] ), _sliceItem( xs[2] ), _sliceItem( xs[4] ) ] ) ).debug( 'subscriptLongSlice' )
subscriptEllipsis = Production( '...' ).action( lambda input, pos, xs: [ 'ellipsis' ] ).debug( 'subscriptEllipsis' )
subscriptItem = subscriptLongSlice | subscriptSlice | subscriptEllipsis | expression
subscriptTuple = Production( separatedList( subscriptItem, bNeedAtLeastOne=True, bAllowTrailingSeparator=True, bRequireTrailingSeparatorForLengthOne=True ) ).action( lambda input, pos, xs: [ 'subscriptTuple' ]  +  xs ).debug( 'subscriptTuple' )
subscriptIndex = subscriptTuple  |  subscriptItem
subscript  <<  Production( ( primary + '[' + subscriptIndex + ']' ).action( lambda input, pos, xs: [ 'subscript', xs[0], xs[2] ] ) ).debug( 'subscript' )



# Call
def _checkCallArgs(input, pos, xs):
	bKW = False
	bArgList = False
	bKWArgList = False
	for x in xs:
		if isinstance( x, list )  and  len( x ) >= 2:
			if x[0] == 'kwArgList':
				if bKWArgList:
					# Not after KW arg list (only 1 allowed)
					return False
				bKWArgList = True
				continue
			elif x[0] == 'argList':
				if bKWArgList | bArgList:
					# Not after KW arg list
					# Not after arg list (only 1 allowed)
					return False
				bArgList = True
				continue
			elif x[0] == 'kwArg':
				if bKWArgList | bArgList:
					# Not after arg list or KW arg list
					return False
				bKW = True
				continue
		if bKWArgList | bArgList | bKW:
			# Not after KW arg list, or arg list, or KW arg
			return False
	return True
				
argName = Production( pythonIdentifier )
kwArg = Production( argName + '=' + expression ).action( lambda input, pos, xs: [ 'kwArg', xs[0], xs[2] ] ).debug( 'kwArg' )
argList = Production( Literal( '*' )  +  expression ).action( lambda input, pos, xs: [ 'argList', xs[1] ] ).debug( 'argList' )
kwArgList = Production( Literal( '**' )  +  expression ).action( lambda input, pos, xs: [ 'kwArgList', xs[1] ] ).debug( 'kwArgList' )
callArg = Production( kwArgList | argList | kwArg | expression ).debug( 'callArg' )
callArgs = Production( separatedList( callArg, bAllowTrailingSeparator=True ).condition( _checkCallArgs ) )
call = Production( ( primary + Literal( '(' ) + callArgs + Literal( ')' ) ).action( lambda input, pos, xs: [ 'call', xs[0] ] + xs[2] ) ).debug( 'call' )



# Primary
primary  <<  Production( call | attributeRef | subscript | atom ).debug( 'primary' )



# Python operators
ops, ( powOp, invNegPosOp, mulDivModOp, addSubOp, lrShiftOp, andOP, xorOp, orOp, cmpOp, isOp, inOp, notTestOp, andTestOp, orTestOp ) =  buildOperatorParserWithAllLevels( \
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
		[ InfixLeft( Keyword( isKeyword ) + Keyword( notKeyword ),  lambda op, x, y: [ 'isNotTest', x, y ] ),   InfixLeft( Keyword( isKeyword ),  lambda op, x, y: [ 'isTest', x, y ] ) ],
		[ InfixLeft( Keyword( notKeyword ) + Keyword( inKeyword ),  lambda op, x, y: [ 'notInTest', x, y ] ),   InfixLeft( Keyword( inKeyword ),  lambda op, x, y: [ 'inTest', x, y ] ) ],
		[ Prefix( Keyword( notKeyword ),  lambda op, x: [ 'notTest', x ] ) ],
		[ InfixLeft( Keyword( andKeyword ),  lambda op, x, y: [ 'andTest', x, y ] ) ],
		[ InfixLeft( Keyword( orKeyword ),  lambda op, x, y: [ 'orTest', x, y ] ) ],
	],  primary )

orTest  <<  orTestOp



# Parameters (lambda, def statement, etc)
def _checkParams(input, pos, xs):
	bDefaultValParam = False
	bParamList = False
	bKWParamList = False
	for x in xs:
		if isinstance( x, list )  and  len( x ) >= 2:
			if x[0] == 'kwParamList':
				if bKWParamList:
					# Not after KW param list (only 1 allowed)
					return False
				bKWParamList = True
				continue
			elif x[0] == 'paramList':
				if bKWParamList | bParamList:
					# Not after KW param list
					# Not after param list (only 1 allowed)
					return False
				bParamList = True
				continue
			elif x[0] == 'defaultValueParam':
				if bKWParamList | bParamList:
					# Not after param list or KW param list
					return False
				bDefaultValParam = True
				continue
		if bKWParamList | bParamList | bDefaultValParam:
			# Not after KW param list, or param list, or default value param
			return False
	return True
paramName = pythonIdentifier
simpleParam = Production( pythonIdentifier.action( lambda input, pos, xs: [ 'simpleParam', xs[0] ] ) ).debug( 'simpleParam' )
defaultValueParam = Production( paramName + '=' + expression ).action( lambda input, pos, xs: [ 'defaultValueParam', xs[0], xs[2] ] ).debug( 'defaultValueParam' )
paramList = Production( Literal( '*' )  +  paramName ).action( lambda input, pos, xs: [ 'paramList', xs[1] ] ).debug( 'paramList' )
kwParamList = Production( Literal( '**' )  +  paramName ).action( lambda input, pos, xs: [ 'kwParamList', xs[1] ] ).debug( 'kwParamList' )
param = Production( kwParamList | paramList | defaultValueParam | simpleParam ).debug( 'param' )
params = Production( separatedList( param, bAllowTrailingSeparator=True ).condition( _checkParams ) ).debug( 'params' )



# Lambda expression_checkParams
lambdaExpr = Production( ( Keyword( lambdaKeyword )  +  params  +  Literal( ':' )  +  expression ).action( lambda input, pos, xs: [ 'lambdaExpr', xs[1], xs[3] ] ) ).debug( 'lambdaExpr' )
oldLambdaExpr = Production( ( Keyword( lambdaKeyword )  +  params  +  Literal( ':' )  +  oldExpression ).action( lambda input, pos, xs: [ 'lambdaExpr', xs[1], xs[3] ] ) ).debug( 'oldLambdaExpr' )



# Conditional expression
conditionalExpression = Production( orTest  +  Keyword( ifKeyword )  +  orTest  +  Keyword( elseKeyword )  +  expression ).action( lambda input, pos, xs: [ 'conditionalExpr', xs[2], xs[0], xs[4] ] )



# Expression and old expression (old expression is expression without conditional expression)
oldExpression  <<  Production( lambdaExpr  |  orTest ).debug( 'oldExpression' )
expression  <<  Production( lambdaExpr  |  conditionalExpression  |  orTest ).debug( 'expression' )



# Tuple or (old) expression
tupleOrExpression  <<  ( tupleLiteral | expression ).debug( 'tupleOrExpression' )
oldTupleOrExpression  <<  ( oldTupleLiteral | oldExpression ).debug( 'oldTupleOrExpression' )



# Tuple or expression or yield expression
tupleOrExpressionOrYieldExpression = tupleOrExpression | yieldExpression



# Assert statement
assertStmt = Production( Keyword( assertKeyword ) + expression  +  Optional( Literal( ',' ) + expression ) ).action( lambda input, pos, xs: [ 'assertStmt', xs[1], xs[2][1]   if xs[2] is not None  else  '<nil>' ] ).debug( 'assertStmt' )



# Assignment statement
assignmentStmt = Production( OneOrMore( ( targetList  +  '=' ).action( lambda input, pos, xs: xs[0] ) )  +  tupleOrExpressionOrYieldExpression ).action( lambda input, pos, xs: [ 'assignmentStmt', xs[0], xs[1] ] ).debug( 'assignmentStmt' )



# Augmented assignment statement
augOp = Choice( [ Literal( op )   for op in augAssignOps ] )
augAssignStmt = Production( targetItem  +  augOp  +  tupleOrExpressionOrYieldExpression ).action( lambda input, pos, xs: [ 'augAssignStmt', xs[1], xs[0], xs[2] ] ).debug( 'augAssignStmt' )



# Pass statement
passStmt = Production( Keyword( passKeyword ) ).action( lambda input, pos, xs: [ 'passStmt' ] )



# Del statement
delStmt = Production( Keyword( delKeyword )  +  targetList ).action( lambda input, pos, xs: [ 'delStmt', xs[1] ] )



# Return statement
returnStmt = Production( Keyword( 'return' )  +  tupleOrExpression ).action( lambda input, pos, xs: [ 'returnStmt', xs[1] ] ).debug( 'returnStmt' )



# Yield statement
yieldStmt = Production( Keyword( yieldKeyword )  +  expression ).action( lambda input, pos, xs: [ 'yieldStmt', xs[1] ] ).debug( 'yieldStmt' )



# Raise statement
def _raiseFlatten(xs, level):
	if xs is None:
		return [ '<nil>' ] * level
	else:
		if xs[0] == ',':
			xs = xs[1:]
		if len( xs ) == 2:
			return [ xs[0] ]  +  _raiseFlatten( xs[1], level - 1 )
		else:
			return [ xs[0] ]
raiseStmt = Production( Keyword( raiseKeyword ) + Optional( expression + Optional( Literal( ',' ) + expression + Optional( Literal( ',' ) + expression ) ) ) ).action( \
	lambda input, pos, xs: [ 'raiseStmt', ]  +  _raiseFlatten( xs[1], 3 ) ).debug( 'assertStmt' )



# Break statement
breakStmt = Production( Keyword( breakKeyword ) ).action( lambda input, pos, xs: [ 'breakStmt' ] )



# Continue statement
continueStmt = Production( Keyword( continueKeyword ) ).action( lambda input, pos, xs: [ 'continueStmt' ] )



# Import statement
_moduleIdentifier = Production( pythonIdentifier )
# dotted name
moduleName = Production( separatedList( _moduleIdentifier, '.', bNeedAtLeastOne=True ) ).action( lambda input, pos, xs: '.'.join( xs ) )
# relative module name
_relModDotsModule = ( ZeroOrMore( '.' ) + moduleName ).action( lambda input, pos, xs: ''.join( xs[0] )  +  xs[1] )
_relModDots = OneOrMore( '.' ).action( lambda input, pos, xs: ''.join( xs ) )
relativeModule = Production( _relModDotsModule | _relModDots ).action( lambda input, pos, xs: [ 'relativeModule', xs ] )
# ( <moduleName> 'as' <pythonIdentifier> )  |  <moduleName>
moduleImport = Production( ( moduleName + Keyword( asKeyword ) + pythonIdentifier ).action( lambda input, pos, xs: [ 'moduleImportAs', xs[0], xs[2] ] )   |
			    			moduleName.action( lambda input, pos, xs: [ 'moduleImport', xs ] ) )
# 'import' <separatedList( moduleImport )>
simpleImport = Production( Keyword( importKeyword )  +  separatedList( moduleImport, bNeedAtLeastOne=True ) ).action( lambda input, pos, xs: [ 'importStmt' ] + xs[1] )
# ( <pythonIdentifier> 'as' <pythonIdentifier> )  |  <pythonIdentifier>
moduleContentImport = Production( ( pythonIdentifier + Keyword( asKeyword ) + pythonIdentifier ).action( lambda input, pos, xs: [ 'moduleContentImportAs', xs[0], xs[2] ] )   |
			    			pythonIdentifier.action( lambda input, pos, xs: [ 'moduleContentImport', xs ] ) )
# 'from' <relativeModule> 'import' ( <separatedList( moduleContentImport )>  |  ( '(' <separatedList( moduleContentImport )> ',' ')' )
fromImport = Production( Keyword( fromKeyword ) + relativeModule + Keyword( importKeyword ) + \
				(  \
					separatedList( moduleContentImport, bNeedAtLeastOne=True )  |  \
					( Literal( '(' )  +  separatedList( moduleContentImport, bNeedAtLeastOne=True, bAllowTrailingSeparator=True )  +  Literal( ')' ) ).action( lambda input, pos, xs: xs[1] )  \
				)  \
			).action( lambda input, pos, xs: [ 'fromImportStmt', xs[1] ] + xs[3] )
# 'from' <relativeModule> 'import' '*'
fromImportAll = Production( Keyword( fromKeyword ) + relativeModule + Keyword( importKeyword ) + '*' ).action( lambda input, pos, xs: [ 'fromImportAllStmt', xs[1] ] )
# Final :::
importStmt = Production( simpleImport | fromImport | fromImportAll )



# Global statement
globalVar = Production( pythonIdentifier ).action( lambda input, pos, xs: [ 'globalVar', xs ] )
globalStmt = Production( Keyword( globalKeyword )  +  separatedList( globalVar, bNeedAtLeastOne=True ) ).action( lambda input, pos, xs: [ 'globalStmt' ]  +  xs[1] )



# Exec statement
execCodeStmt = Production( Keyword( execKeyword )  +  orOp ).action( lambda input, pos, xs: [ 'execStmt', xs[1], '<nil>', '<nil>' ] )
execCodeInLocalsStmt = Production( Keyword( execKeyword )  +  orOp  +  Keyword( inKeyword )  +  expression ).action( lambda input, pos, xs: [ 'execStmt', xs[1], xs[3], '<nil>' ] )
execCodeInLocalsAndGlobalsStmt = Production( Keyword( execKeyword )  +  orOp  +  Keyword( inKeyword )  +  expression  +  ','  +  expression ).action( lambda input, pos, xs: [ 'execStmt', xs[1], xs[3], xs[5] ] )
execStmt = Production( execCodeInLocalsAndGlobalsStmt | execCodeInLocalsStmt | execCodeStmt )



# If statement
ifStmt = Production( Keyword( ifKeyword )  +  expression  +  ':' ).action( lambda input, pos, xs: [ 'ifStmt', xs[1], [] ] ).debug( 'ifStmt' )



# Elif statement
elifStmt = Production( Keyword( elifKeyword )  +  expression  +  ':' ).action( lambda input, pos, xs: [ 'elifStmt', xs[1], [] ] ).debug( 'elifStmt' )



# Else statement
elseStmt = Production( Keyword( elseKeyword )  +  ':' ).action( lambda input, pos, xs: [ 'elseStmt', [] ] ).debug( 'elseStmt' )



# While statement
whileStmt = Production( Keyword( whileKeyword )  +  expression  +  ':' ).action( lambda input, pos, xs: [ 'whileStmt', xs[1], [] ] ).debug( 'whileStmt' )



# For statement
forStmt = Production( Keyword( forKeyword )  +  targetList  +  Keyword( inKeyword )  +  tupleOrExpression  +  ':' ).action( lambda input, pos, xs: [ 'forStmt', xs[1], xs[3], [] ] ).debug( 'forStmt' )



# Try statement
tryStmt = Production( Keyword( tryKeyword )  +  ':' ).action( lambda input, pos, xs: [ 'tryStmt', [] ] ).debug( 'tryStmt' )



# Except statement
exceptAllStmt = Production( Keyword( exceptKeyword ) + ':' ).action( lambda input, pos, xs: [ 'exceptStmt', '<nil>', '<nil>', [] ] )
exceptExcStmt = Production( Keyword( exceptKeyword )  +  expression + ':' ).action( lambda input, pos, xs: [ 'exceptStmt', xs[1], '<nil>', [] ] )
exceptExcIntoTargetStmt = Production( Keyword( exceptKeyword )  +  expression  +  ','  +  targetItem + ':' ).action( lambda input, pos, xs: [ 'exceptStmt', xs[1], xs[3], [] ] )
exceptStmt = Production( exceptExcIntoTargetStmt | exceptExcStmt | exceptAllStmt )



# Finally statement
finallyStmt = Production( Keyword( finallyKeyword )  +  ':' ).action( lambda input, pos, xs: [ 'finallyStmt', [] ] ).debug( 'finallyStmt' )



# With statement
withStmt = Production( Keyword( withKeyword )  +  expression  +  Optional( Keyword( asKeyword )  +  targetItem )  +  ':' ).action( lambda input, pos, xs: [ 'withStmt', xs[1], xs[2][1]   if xs[2] is not None   else   '<nil>', [] ] )



# Def statement
defStmt = Production( Keyword( defKeyword )  +  pythonIdentifier  +  '('  +  params  +  ')'  +  ':' ).action( lambda input, pos, xs: [ 'defStmt', xs[1], xs[3], [] ] )



# Decorator statement
decoStmt = Production( Literal( '@' )  +  dottedPythonIdentifer  +  Optional( Literal( '(' )  +  callArgs  +  ')' ) ).action( lambda input, pos, xs: [ 'decoStmt', xs[1], xs[2][1]   if xs[2] is not None   else   '<nil>' ] )



# Class statement
classStmt = Production( Keyword( classKeyword )  +  pythonIdentifier  +  Optional( Literal( '(' )  +  tupleOrExpression  +  ')' )  +  ':' ).action( lambda input, pos, xs: [ 'classStmt', xs[1], xs[2][1]   if xs[2] is not None   else   '<nil>', [] ] )



# Comment statement
commentStmt = Production( Literal( '#' )  +  Word( string.printable ) ).action( lambda input, pos, xs: [ 'commentStmt', xs[1] ] )





# Statements
simpleStmt = assertStmt | assignmentStmt | augAssignStmt | passStmt | delStmt | returnStmt | yieldStmt | raiseStmt | breakStmt | continueStmt | importStmt | globalStmt | execStmt
compoundStmtHeader = ifStmt | elifStmt | elseStmt | whileStmt | forStmt | tryStmt | exceptStmt | finallyStmt | withStmt | defStmt | decoStmt | classStmt
statement = Production( simpleStmt | compoundStmtHeader | commentStmt | expression ).debug( 'statement' )




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

		self._matchTest( subscript, 'a[x]', [ 'subscript', [ 'var', 'a' ], [ 'var', 'x' ] ] )
		self._matchTest( attributeRef | subscript, 'a[x]', [ 'subscript', [ 'var', 'a' ], [ 'var', 'x' ] ] )
		self._matchTest( targetItem, 'a[x]', [ 'subscript', [ 'var', 'a' ], [ 'var', 'x' ] ] )
		self._matchTest( targetList, 'a[x]', [ 'subscript', [ 'var', 'a' ], [ 'var', 'x' ] ] )
		self._matchTest( targetList, 'a[x][y]', [ 'subscript', [ 'subscript', [ 'var', 'a' ], [ 'var', 'x' ] ], [ 'var', 'y' ] ] )
		self._matchTest( targetList, 'a.b', [ 'attributeRef', [ 'var', 'a' ], 'b' ] )
		self._matchTest( targetList, 'a.b.c', [ 'attributeRef', [ 'attributeRef', [ 'var', 'a' ], 'b' ], 'c' ] )

		self._matchTest( targetList, 'a.b[x]', [ 'subscript', [ 'attributeRef', [ 'var', 'a' ], 'b' ], [ 'var', 'x' ] ] )
		self._matchTest( targetList, 'a[x].b', [ 'attributeRef', [ 'subscript', [ 'var', 'a' ], [ 'var', 'x' ] ], 'b' ] )

		
	def testListLiteral(self):
		self._matchTest( expression, '[a,b]', [ 'listLiteral', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._matchTest( expression, '[a,b,]', [ 'listLiteral', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		
		
	def testListComprehension(self):
		self._matchTest( expression, '[i  for i in a]', [ 'listComprehension', [ 'var', 'i' ],
												[ 'listFor', [ 'singleTarget', 'i' ], [ 'var', 'a' ] ]
										] )
		self._matchFailTest( expression, '[i  if x]', )
		self._matchTest( expression, '[i  for i in a  if x]', [ 'listComprehension', [ 'var', 'i' ],
												[ 'listFor', [ 'singleTarget', 'i' ], [ 'var', 'a' ] ],
												[ 'listIf', [ 'var', 'x' ] ]
										] )
		self._matchTest( expression, '[i  for i in a  for j in b]', [ 'listComprehension', [ 'var', 'i' ],
												[ 'listFor', [ 'singleTarget', 'i' ], [ 'var', 'a' ] ],
												[ 'listFor', [ 'singleTarget', 'j' ], [ 'var', 'b' ] ]
										] )
		self._matchTest( expression, '[i  for i in a  if x  for j in b]', [ 'listComprehension', [ 'var', 'i' ],
												[ 'listFor', [ 'singleTarget', 'i' ], [ 'var', 'a' ] ],
												[ 'listIf', [ 'var', 'x' ] ],
												[ 'listFor', [ 'singleTarget', 'j' ], [ 'var', 'b' ] ]
										] )
		self._matchTest( expression, '[i  for i in a  if x  for j in b  if y]', [ 'listComprehension', [ 'var', 'i' ],
												[ 'listFor', [ 'singleTarget', 'i' ], [ 'var', 'a' ] ],
												[ 'listIf', [ 'var', 'x' ] ],
												[ 'listFor', [ 'singleTarget', 'j' ], [ 'var', 'b' ] ],
												[ 'listIf', [ 'var', 'y' ] ]
										] )
		

		
	def testGeneratorExpression(self):
		self._matchTest( expression, '(i  for i in a)', [ 'generatorExpression', [ 'var', 'i' ],
												[ 'genFor', [ 'singleTarget', 'i' ], [ 'var', 'a' ] ]
										] )
		self._matchFailTest( expression, '(i  if x)', )
		self._matchTest( expression, '(i  for i in a  if x)', [ 'generatorExpression', [ 'var', 'i' ],
												[ 'genFor', [ 'singleTarget', 'i' ], [ 'var', 'a' ] ],
												[ 'genIf', [ 'var', 'x' ] ]
										] )
		self._matchTest( expression, '(i  for i in a  for j in b)', [ 'generatorExpression', [ 'var', 'i' ],
												[ 'genFor', [ 'singleTarget', 'i' ], [ 'var', 'a' ] ],
												[ 'genFor', [ 'singleTarget', 'j' ], [ 'var', 'b' ] ]
										] )
		self._matchTest( expression, '(i  for i in a  if x  for j in b)', [ 'generatorExpression', [ 'var', 'i' ],
												[ 'genFor', [ 'singleTarget', 'i' ], [ 'var', 'a' ] ],
												[ 'genIf', [ 'var', 'x' ] ],
												[ 'genFor', [ 'singleTarget', 'j' ], [ 'var', 'b' ] ]
										] )
		self._matchTest( expression, '(i  for i in a  if x  for j in b  if y)', [ 'generatorExpression', [ 'var', 'i' ],
												[ 'genFor', [ 'singleTarget', 'i' ], [ 'var', 'a' ] ],
												[ 'genIf', [ 'var', 'x' ] ],
												[ 'genFor', [ 'singleTarget', 'j' ], [ 'var', 'b' ] ],
												[ 'genIf', [ 'var', 'y' ] ]
										] )

		
	def testDictLiteral(self):
		self._matchTest( expression, '{a:x,b:y}', [ 'dictLiteral', [ 'keyValuePair', [ 'var', 'a' ], [ 'var', 'x' ] ],   [ 'keyValuePair', [ 'var', 'b' ], [ 'var', 'y' ] ] ] )
		self._matchTest( expression, '{a:x,b:y,}', [ 'dictLiteral', [ 'keyValuePair', [ 'var', 'a' ], [ 'var', 'x' ] ],   [ 'keyValuePair', [ 'var', 'b' ], [ 'var', 'y' ] ] ] )
		
		
	def testYieldExpression(self):
		self._matchTest( expression, '(yield 2+3)', [ 'yieldExpr', [ 'add', [ 'intLiteral', 'decimal', 'int', '2' ], [ 'intLiteral', 'decimal', 'int', '3' ] ] ] )
		
		

	def testAttributeRef(self):
		self._matchTest( expression, 'a.b', [ 'attributeRef', [ 'var', 'a' ], 'b' ] )
		
		
	def testSubscript(self):
		self._matchTest( expression, 'a[x]', [ 'subscript', [ 'var', 'a' ], [ 'var', 'x' ] ] )
		self._matchTest( expression, 'a[x:p]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptSlice', [ 'var', 'x' ], [ 'var', 'p' ] ] ] )
		self._matchTest( expression, 'a[x:]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptSlice', [ 'var', 'x' ], '<nil>' ] ] )
		self._matchTest( expression, 'a[:p]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptSlice', '<nil>', [ 'var', 'p' ] ] ] )
		self._matchTest( expression, 'a[:]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptSlice', '<nil>', '<nil>' ] ] )
		self._matchTest( expression, 'a[x:p:f]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', [ 'var', 'x' ], [ 'var', 'p' ], [ 'var', 'f' ] ] ] )
		self._matchTest( expression, 'a[x:p:]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', [ 'var', 'x' ], [ 'var', 'p' ], '<nil>' ] ] )
		self._matchTest( expression, 'a[x::f]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', [ 'var', 'x' ], '<nil>', [ 'var', 'f' ] ] ] )
		self._matchTest( expression, 'a[:p:f]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', '<nil>', [ 'var', 'p' ], [ 'var', 'f' ] ] ] )
		self._matchTest( expression, 'a[::]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', '<nil>', '<nil>', '<nil>' ] ] )
		self._matchTest( expression, 'a[::f]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', '<nil>', '<nil>', [ 'var', 'f' ] ] ] )
		self._matchTest( expression, 'a[x::]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', [ 'var', 'x' ], '<nil>', '<nil>' ] ] )
		self._matchTest( expression, 'a[:p:]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', '<nil>', [ 'var', 'p' ], '<nil>' ] ] )
		self._matchTest( expression, 'a[x,y]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptTuple', [ 'var', 'x' ], [ 'var', 'y' ] ] ] )
		self._matchTest( expression, 'a[x:p,y:q]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptTuple', [ 'subscriptSlice', [ 'var', 'x' ], [ 'var', 'p' ] ], [ 'subscriptSlice', [ 'var', 'y' ], [ 'var', 'q' ] ] ] ] )
		self._matchTest( expression, 'a[x:p:f,y:q:g]', [ 'subscript', [ 'var', 'a' ], [ 'subscriptTuple', [ 'subscriptLongSlice', [ 'var', 'x' ], [ 'var', 'p' ], [ 'var', 'f' ] ], [ 'subscriptLongSlice', [ 'var', 'y' ], [ 'var', 'q' ], [ 'var', 'g' ] ] ] ] )
		self._matchTest( expression, 'a[x:p:f,y:q:g,...]', [ 'subscript', [ 'var', 'a' ],
								     [ 'subscriptTuple', [ 'subscriptLongSlice', [ 'var', 'x' ], [ 'var', 'p' ], [ 'var', 'f' ] ], [ 'subscriptLongSlice', [ 'var', 'y' ], [ 'var', 'q' ], [ 'var', 'g' ] ], [ 'ellipsis' ] ] ] )
		
		

	def testCall(self):
		self._matchTest( expression, 'a()', [ 'call', [ 'var', 'a' ] ] )
		self._matchTest( expression, 'a(f)', [ 'call', [ 'var', 'a' ], [ 'var', 'f' ] ] )
		self._matchTest( expression, 'a(f,)', [ 'call', [ 'var', 'a' ], [ 'var', 'f' ] ] )
		self._matchTest( expression, 'a(f,g)', [ 'call', [ 'var', 'a' ], [ 'var', 'f' ], [ 'var', 'g' ] ] )
		self._matchTest( expression, 'a(f,g,m=a)', [ 'call', [ 'var', 'a' ], [ 'var', 'f' ], [ 'var', 'g' ], [ 'kwArg', 'm', [ 'var', 'a' ] ] ] )
		self._matchTest( expression, 'a(f,g,m=a,n=b)', [ 'call', [ 'var', 'a' ], [ 'var', 'f' ], [ 'var', 'g' ], [ 'kwArg', 'm', [ 'var', 'a' ] ], [ 'kwArg', 'n', [ 'var', 'b' ] ] ] )
		self._matchTest( expression, 'a(f,g,m=a,n=b,*p)', [ 'call', [ 'var', 'a' ], [ 'var', 'f' ], [ 'var', 'g' ], [ 'kwArg', 'm', [ 'var', 'a' ] ], [ 'kwArg', 'n', [ 'var', 'b' ] ], [ 'argList', [ 'var', 'p' ] ] ] )
		self._matchTest( expression, 'a(f,m=a,*p,**w)', [ 'call', [ 'var', 'a' ], [ 'var', 'f' ], [ 'kwArg', 'm', [ 'var', 'a' ] ], [ 'argList', [ 'var', 'p' ] ], [ 'kwArgList', [ 'var', 'w' ] ] ] )
		self._matchTest( expression, 'a(f,m=a,*p)', [ 'call', [ 'var', 'a' ], [ 'var', 'f' ], [ 'kwArg', 'm', [ 'var', 'a' ] ], [ 'argList', [ 'var', 'p' ] ] ] )
		self._matchTest( expression, 'a(f,m=a,**w)', [ 'call', [ 'var', 'a' ], [ 'var', 'f' ], [ 'kwArg', 'm', [ 'var', 'a' ] ], [ 'kwArgList', [ 'var', 'w' ] ] ] )
		self._matchTest( expression, 'a(f,*p,**w)', [ 'call', [ 'var', 'a' ], [ 'var', 'f' ], [ 'argList', [ 'var', 'p' ] ], [ 'kwArgList', [ 'var', 'w' ] ] ] )
		self._matchTest( expression, 'a(m=a,*p,**w)', [ 'call', [ 'var', 'a' ], [ 'kwArg', 'm', [ 'var', 'a' ] ], [ 'argList', [ 'var', 'p' ] ], [ 'kwArgList', [ 'var', 'w' ] ] ] )
		self._matchTest( expression, 'a(*p,**w)', [ 'call', [ 'var', 'a' ], [ 'argList', [ 'var', 'p' ] ], [ 'kwArgList', [ 'var', 'w' ] ] ] )
		self._matchTest( expression, 'a(**w)', [ 'call', [ 'var', 'a' ], [ 'kwArgList', [ 'var', 'w' ] ] ] )
		self._matchFailTest( expression, 'a(m=a,f)' )
		self._matchFailTest( expression, 'a(*p,f)' )
		self._matchFailTest( expression, 'a(**w,f)' )
		self._matchFailTest( expression, 'a(*p,m=a)' )
		self._matchFailTest( expression, 'a(**w,m=a)' )
		self._matchFailTest( expression, 'a(**w,*p)' )


		
	def testParams(self):
		self._matchTest( params, '', [] )
		self._matchTest( params, 'f', [ [ 'simpleParam', 'f' ] ] )
		self._matchTest( params, 'f,', [ [ 'simpleParam', 'f' ] ] )
		self._matchTest( params, 'f,g', [ [ 'simpleParam', 'f' ], [ 'simpleParam', 'g' ] ] )
		self._matchTest( params, 'f,g,m=a', [ [ 'simpleParam', 'f' ], [ 'simpleParam', 'g' ], [ 'defaultValueParam', 'm', [ 'var', 'a' ] ] ] )
		self._matchTest( params, 'f,g,m=a,n=b', [ [ 'simpleParam', 'f' ], [ 'simpleParam', 'g' ], [ 'defaultValueParam', 'm', [ 'var', 'a' ] ], [ 'defaultValueParam', 'n', [ 'var', 'b' ] ] ] )
		self._matchTest( params, 'f,g,m=a,n=b,*p', [ [ 'simpleParam', 'f' ], [ 'simpleParam', 'g' ], [ 'defaultValueParam', 'm', [ 'var', 'a' ] ], [ 'defaultValueParam', 'n', [ 'var', 'b' ] ], [ 'paramList', 'p' ] ] )
		self._matchTest( params, 'f,m=a,*p,**w', [ [ 'simpleParam', 'f' ], [ 'defaultValueParam', 'm', [ 'var', 'a' ] ], [ 'paramList', 'p' ], [ 'kwParamList', 'w' ] ] )
		self._matchTest( params, 'f,m=a,*p', [ [ 'simpleParam', 'f' ], [ 'defaultValueParam', 'm', [ 'var', 'a' ] ], [ 'paramList', 'p' ] ] )
		self._matchTest( params, 'f,m=a,**w', [ [ 'simpleParam', 'f' ], [ 'defaultValueParam', 'm', [ 'var', 'a' ] ], [ 'kwParamList', 'w' ] ] )
		self._matchTest( params, 'f,*p,**w', [ [ 'simpleParam', 'f' ], [ 'paramList', 'p' ], [ 'kwParamList', 'w' ] ] )
		self._matchTest( params, 'm=a,*p,**w', [ [ 'defaultValueParam', 'm', [ 'var', 'a' ] ], [ 'paramList', 'p' ], [ 'kwParamList', 'w' ] ] )
		self._matchTest( params, '*p,**w', [ [ 'paramList', 'p' ], [ 'kwParamList', 'w' ] ] )
		self._matchTest( params, '**w', [ [ 'kwParamList', 'w' ] ] )
		self._matchFailTest( params, 'm=a,f' )
		self._matchFailTest( params, '*p,f' )
		self._matchFailTest( params, '**w,f' )
		self._matchFailTest( params, '*p,m=a' )
		self._matchFailTest( params, '**w,m=a' )
		self._matchFailTest( params, '**w,*p' )


		
	def testLambda(self):
		self._matchTest( expression, 'lambda f,m=a,*p,**w: f+m+p+w', [ 'lambdaExpr', [ [ 'simpleParam', 'f' ], [ 'defaultValueParam', 'm', [ 'var', 'a' ] ], [ 'paramList', 'p' ], [ 'kwParamList', 'w' ] ],
									   [ 'add', [ 'add', [ 'add', [ 'var', 'f' ], [ 'var', 'm' ] ], [ 'var', 'p' ] ], [ 'var', 'w' ] ] ] )

		
		
	def testConditionalExpr(self):
		self._matchTest( expression, 'x   if y else   z', [ 'conditionalExpr', [ 'var', 'y' ], [ 'var', 'x' ], [ 'var', 'z' ] ] )
		self._matchTest( expression, '(x   if y else   z)   if w else   q', [ 'conditionalExpr', [ 'var', 'w' ], [ 'conditionalExpr', [ 'var', 'y' ], [ 'var', 'x' ], [ 'var', 'z' ] ], [ 'var', 'q' ] ] )
		self._matchTest( expression, 'w   if (x   if y else   z) else   q', [ 'conditionalExpr', [ 'conditionalExpr', [ 'var', 'y' ], [ 'var', 'x' ], [ 'var', 'z' ] ], [ 'var', 'w' ], [ 'var', 'q' ] ] )
		self._matchTest( expression, 'w   if q else   x   if y else   z', [ 'conditionalExpr', [ 'var', 'q' ], [ 'var', 'w' ], [ 'conditionalExpr', [ 'var', 'y' ], [ 'var', 'x' ], [ 'var', 'z' ] ] ] )
		self._matchFailTest( expression, 'w   if x   if y else   z else   q' )
		
		
	
	def testTupleOrExpression(self):
		self._matchTest( tupleOrExpression, 'a', [ 'var', 'a' ] )
		self._matchTest( tupleOrExpression, 'a,b', [ 'tupleLiteral', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._matchTest( tupleOrExpression, 'a,2', [ 'tupleLiteral', [ 'var', 'a' ], [ 'intLiteral', 'decimal', 'int', '2' ] ] )
		self._matchTest( tupleOrExpression, 'lambda x, y: x+y,2', [ 'tupleLiteral', [ 'lambdaExpr', [ [ 'simpleParam', 'x' ], [ 'simpleParam', 'y' ] ], [ 'add', [ 'var', 'x' ], [ 'var', 'y' ] ] ], [ 'intLiteral', 'decimal', 'int', '2' ] ] )
		
		
		
	def testAssertStmt(self):
		self._matchTest( statement, 'assert x', [ 'assertStmt', [ 'var', 'x' ], '<nil>' ] )
		self._matchTest( statement, 'assert x,y', [ 'assertStmt', [ 'var', 'x' ], [ 'var', 'y' ] ] )
	
	
	def testAssignmentStmt(self):
		self._matchTest( statement, 'a=x', [ 'assignmentStmt', [ [ 'singleTarget', 'a' ] ], [ 'var', 'x' ] ] )
		self._matchTest( statement, 'a,b=c,d=x', [ 'assignmentStmt', [ [ 'tupleTarget', [ 'singleTarget', 'a' ],  [ 'singleTarget', 'b' ] ],  [ 'tupleTarget', [ 'singleTarget', 'c' ],  [ 'singleTarget', 'd' ] ] ], [ 'var', 'x' ] ] )
		self._matchTest( statement, 'a=yield x', [ 'assignmentStmt', [ [ 'singleTarget', 'a' ] ], [ 'yieldExpr', [ 'var', 'x' ] ] ] )
		self._matchFailTest( statement, '=x' )
		
		
	def testAugAssignStmt(self):
		self._matchTest( statement, 'a += b', [ 'augAssignStmt', '+=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._matchTest( statement, 'a -= b', [ 'augAssignStmt', '-=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._matchTest( statement, 'a *= b', [ 'augAssignStmt', '*=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._matchTest( statement, 'a /= b', [ 'augAssignStmt', '/=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._matchTest( statement, 'a %= b', [ 'augAssignStmt', '%=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._matchTest( statement, 'a **= b', [ 'augAssignStmt', '**=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._matchTest( statement, 'a >>= b', [ 'augAssignStmt', '>>=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._matchTest( statement, 'a <<= b', [ 'augAssignStmt', '<<=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._matchTest( statement, 'a &= b', [ 'augAssignStmt', '&=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._matchTest( statement, 'a ^= b', [ 'augAssignStmt', '^=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._matchTest( statement, 'a |= b', [ 'augAssignStmt', '|=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )

		
	def testPassStmt(self):
		self._matchTest( statement, 'pass', [ 'passStmt' ] )
		
		
	def testDelStmt(self):
		self._matchTest( statement, 'del x', [ 'delStmt', [ 'singleTarget', 'x' ] ] )
		
		
	def testReturnStmt(self):
		self._matchTest( statement, 'return x', [ 'returnStmt', [ 'var', 'x' ] ] )
		
	
	def testYieldStmt(self):
		self._matchTest( statement, 'yield x', [ 'yieldStmt', [ 'var', 'x' ] ] )
		
		
	def testRaiseStmt(self):
		self._matchTest( statement, 'raise', [ 'raiseStmt', '<nil>', '<nil>', '<nil>' ] )
		self._matchTest( statement, 'raise x', [ 'raiseStmt', [ 'var', 'x' ], '<nil>', '<nil>' ] )
		self._matchTest( statement, 'raise x,y', [ 'raiseStmt', [ 'var', 'x' ], [ 'var', 'y' ], '<nil>' ] )
		self._matchTest( statement, 'raise x,y,z', [ 'raiseStmt', [ 'var', 'x' ], [ 'var', 'y' ], [ 'var', 'z' ] ] )
		
		
	def testBreakStmt(self):
		self._matchTest( statement, 'break', [ 'breakStmt' ] )
		
		
	def testContinueStmt(self):
		self._matchTest( statement, 'continue', [ 'continueStmt' ] )
		
		
	def testImportStmt(self):
		self._matchTest( _moduleIdentifier, 'abc', 'abc' )
		self._matchTest( moduleName, 'abc', 'abc' )
		self._matchTest( moduleName, 'abc.xyz', 'abc.xyz' )
		self._matchTest( _relModDotsModule, 'abc.xyz', 'abc.xyz' )
		self._matchTest( _relModDotsModule, '...abc.xyz', '...abc.xyz' )
		self._matchTest( _relModDots, '...', '...' )
		self._matchTest( relativeModule, 'abc.xyz', [ 'relativeModule', 'abc.xyz' ] )
		self._matchTest( relativeModule, '...abc.xyz', [ 'relativeModule', '...abc.xyz' ] )
		self._matchTest( relativeModule, '...', [ 'relativeModule', '...' ] )
		self._matchTest( moduleImport, 'abc.xyz', [ 'moduleImport', 'abc.xyz' ] )
		self._matchTest( moduleImport, 'abc.xyz as q', [ 'moduleImportAs', 'abc.xyz', 'q' ] )
		self._matchTest( simpleImport, 'import a', [ 'importStmt', [ 'moduleImport', 'a' ] ] )
		self._matchTest( simpleImport, 'import a.b', [ 'importStmt', [ 'moduleImport', 'a.b' ] ] )
		self._matchTest( simpleImport, 'import a.b as x', [ 'importStmt', [ 'moduleImportAs', 'a.b', 'x' ] ] )
		self._matchTest( simpleImport, 'import a.b as x, c.d as y', [ 'importStmt', [ 'moduleImportAs', 'a.b', 'x' ], [ 'moduleImportAs', 'c.d', 'y' ] ] )
		self._matchTest( moduleContentImport, 'xyz', [ 'moduleContentImport', 'xyz' ] )
		self._matchTest( moduleContentImport, 'xyz as q', [ 'moduleContentImportAs', 'xyz', 'q' ] )
		self._matchTest( fromImport, 'from x import a', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._matchTest( fromImport, 'from x import a as p', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._matchTest( fromImport, 'from x import a as p, b as q', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._matchTest( fromImport, 'from x import (a)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._matchTest( fromImport, 'from x import (a,)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._matchTest( fromImport, 'from x import (a as p)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._matchTest( fromImport, 'from x import (a as p,)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._matchTest( fromImport, 'from x import ( a as p, b as q )', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._matchTest( fromImport, 'from x import ( a as p, b as q, )', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._matchTest( fromImportAll, 'from x import *', [ 'fromImportAllStmt', [ 'relativeModule', 'x' ] ] )
		self._matchTest( importStmt, 'import a', [ 'importStmt', [ 'moduleImport', 'a' ] ] )
		self._matchTest( importStmt, 'import a.b', [ 'importStmt', [ 'moduleImport', 'a.b' ] ] )
		self._matchTest( importStmt, 'import a.b as x', [ 'importStmt', [ 'moduleImportAs', 'a.b', 'x' ] ] )
		self._matchTest( importStmt, 'import a.b as x, c.d as y', [ 'importStmt', [ 'moduleImportAs', 'a.b', 'x' ], [ 'moduleImportAs', 'c.d', 'y' ] ] )
		self._matchTest( importStmt, 'from x import a', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._matchTest( importStmt, 'from x import a as p', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._matchTest( importStmt, 'from x import a as p, b as q', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._matchTest( importStmt, 'from x import (a)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._matchTest( importStmt, 'from x import (a,)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._matchTest( importStmt, 'from x import (a as p)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._matchTest( importStmt, 'from x import (a as p,)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._matchTest( importStmt, 'from x import ( a as p, b as q )', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._matchTest( importStmt, 'from x import ( a as p, b as q, )', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._matchTest( importStmt, 'from x import *', [ 'fromImportAllStmt', [ 'relativeModule', 'x' ] ] )
		
		
	def testGlobalStmt(self):
		self._matchTest( statement, 'global x', [ 'globalStmt', [ 'globalVar', 'x' ] ] )
		self._matchTest( statement, 'global x, y', [ 'globalStmt', [ 'globalVar', 'x' ], [ 'globalVar', 'y' ] ] )
	
		
	def testExecStmt(self):
		self._matchTest( statement, 'exec a', [ 'execStmt', [ 'var', 'a' ], '<nil>', '<nil>' ] )
		self._matchTest( statement, 'exec a in b', [ 'execStmt', [ 'var', 'a' ], [ 'var', 'b' ], '<nil>' ] )
		self._matchTest( statement, 'exec a in b,c', [ 'execStmt', [ 'var', 'a' ], [ 'var', 'b' ], [ 'var', 'c' ] ] )
		
		
	def testIfStmt(self):
		self._matchTest( ifStmt, 'if a:', [ 'ifStmt', [ 'var', 'a' ], [] ] )
		
		
	def testElIfStmt(self):
		self._matchTest( elifStmt, 'elif a:', [ 'elifStmt', [ 'var', 'a' ], [] ] )
		
		
	def testElseStmt(self):
		self._matchTest( elseStmt, 'else:', [ 'elseStmt', [] ] )
		
		
	def testWhileStmt(self):
		self._matchTest( whileStmt, 'while a:', [ 'whileStmt', [ 'var', 'a' ], [] ] )
		
		
	def testForStmt(self):
		self._matchTest( forStmt, 'for x in y:', [ 'forStmt', [ 'singleTarget', 'x' ], [ 'var', 'y' ], [] ] )
		
		
	def testTryStmt(self):
		self._matchTest( tryStmt, 'try:', [ 'tryStmt', [] ] )
		
		
	def testExceptStmt(self):
		self._matchTest( exceptStmt, 'except:', [ 'exceptStmt', '<nil>', '<nil>', [] ] )
		self._matchTest( exceptStmt, 'except x:', [ 'exceptStmt', [ 'var', 'x' ], '<nil>', [] ] )
		self._matchTest( exceptStmt, 'except x, y:', [ 'exceptStmt', [ 'var', 'x' ], [ 'singleTarget', 'y' ], [] ] )
		
		
	def testFinallyStmt(self):
		self._matchTest( finallyStmt, 'finally:', [ 'finallyStmt', [] ] )
		
		
	def testWithStmt(self):
		self._matchTest( withStmt, 'with a:', [ 'withStmt', [ 'var', 'a' ], '<nil>', [] ] )
		self._matchTest( withStmt, 'with a as b:', [ 'withStmt', [ 'var', 'a' ], [ 'singleTarget', 'b' ], [] ] )
		
		
	def testDefStmt(self):
		self._matchTest( defStmt, 'def f():', [ 'defStmt', 'f', [], [] ] )
		self._matchTest( defStmt, 'def f(x):', [ 'defStmt', 'f', [ [ 'simpleParam', 'x' ] ], [] ] )
		
		
	def testDecoStmt(self):
		self._matchTest( decoStmt, '@f', [ 'decoStmt', 'f', '<nil>' ] )
		self._matchTest( decoStmt, '@f(x)', [ 'decoStmt', 'f', [ [ 'var', 'x' ] ] ] )
		
		
	def testClassStmt(self):
		self._matchTest( classStmt, 'class Q:', [ 'classStmt', 'Q', '<nil>', [] ] )
		self._matchTest( classStmt, 'class Q (x):', [ 'classStmt', 'Q', [ 'var', 'x' ], [] ] )
		
		
	def testCommentStmt(self):
		self._matchTest( commentStmt, '#x', [ 'commentStmt', 'x' ] )
		self._matchTest( commentStmt, '#' + string.printable, [ 'commentStmt', string.printable ] )
		
		

		
		
	def testFnCallStStmt(self):
		self._matchTest( expression, 'x.y()', [ 'call', [ 'attributeRef', [ 'var', 'x' ], 'y' ] ] )
		self._matchTest( statement, 'x.y()', [ 'call', [ 'attributeRef', [ 'var', 'x' ], 'y' ] ] )
		
		
		
		
	def testDictInList(self):
		self._matchTest( statement, 'y = [ x, { a : b } ]', [ 'assignmentStmt', [ [ 'singleTarget', 'y' ] ], [ 'listLiteral', [ 'var', 'x' ], [ 'dictLiteral', [ 'keyValuePair', [ 'var', 'a' ], [ 'var', 'b' ] ] ] ] ] )
		
		
		
		

if __name__ == '__main__':
	#result, pos, dot = targetList.debugParseString( 'a.b' )
	result, pos, dot = subscript.debugParseString( 'a.b' )
	print dot
