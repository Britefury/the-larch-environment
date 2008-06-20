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

from GSymCore.Languages.Python25.Keywords import keywordsSet


pythonIdentifier = identifier  &  ( lambda input, pos, result: result not in keywordsSet )
	
asciiStringSLiteral = Production( singleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'ascii', 'single', xs[1:-1] ] )
asciiStringDLiteral = Production( doubleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'ascii', 'double', xs[1:-1] ] )
unicodeStringSLiteral = Production( Suppress( Literal( 'u' )  |  Literal( 'U' ) ) + singleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'unicode', 'single', xs[0][1:-1] ] )
unicodeStringDLiteral = Production( Suppress( Literal( 'u' )  |  Literal( 'U' ) ) + doubleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'unicode', 'double', xs[0][1:-1] ] )
regexAsciiStringSLiteral = Production( Suppress( Literal( 'r' )  |  Literal( 'R' ) ) + singleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'ascii-regex', 'single', xs[0][1:-1] ] )
regexAsciiStringDLiteral = Production( Suppress( Literal( 'r' )  |  Literal( 'R' ) ) + doubleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'ascii-regex', 'double', xs[0][1:-1] ] )
regexUnicodeStringSLiteral = Production( Suppress( Literal( 'ur' )  |  Literal( 'uR' )  |  Literal( 'Ur' )  |  Literal( 'UR' ) ) + singleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'unicode-regex', 'single', xs[0][1:-1] ] )
regexUnicodeStringDLiteral = Production( Suppress( Literal( 'ur' )  |  Literal( 'uR' )  |  Literal( 'Ur' )  |  Literal( 'UR' ) ) + singleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'unicode-regex', 'double', xs[0][1:-1] ] )
shortStringLiteral = asciiStringSLiteral | asciiStringDLiteral | unicodeStringSLiteral | unicodeStringDLiteral |  \
				regexAsciiStringSLiteral | regexAsciiStringDLiteral | regexUnicodeStringSLiteral | regexUnicodeStringDLiteral



decimalIntLiteral = Production( decimalInteger ).action( lambda input, pos, xs: [ 'intLiteral', 'decimal', 'int', xs ] )
decimalLongLiteral = Production( decimalInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, pos, xs: [ 'intLiteral', 'decimal', 'long', xs[0] ] )
hexIntLiteral = Production( hexInteger ).action( lambda input, pos, xs: [ 'intLiteral', 'hex', 'int', xs ] )
hexLongLiteral = Production( hexInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, pos, xs: [ 'intLiteral', 'hex', 'long', xs[0] ] )

integerLiteral = decimalLongLiteral | decimalIntLiteral | hexLongLiteral | hexIntLiteral

floatLiteral = Production( floatingPoint ).action( lambda input, pos, xs: [ 'floatLiteral', xs ] )

imaginaryLiteral = Production( Combine( [ ( floatingPoint | decimalInteger ), Literal( 'j' ) ] ) ).action( lambda input, pos, xs: [ 'imaginaryLiteral', xs ] )


literal = shortStringLiteral | imaginaryLiteral | floatLiteral | integerLiteral


argName = Production( pythonIdentifier )
attrName = Production( pythonIdentifier )



expression = Forward()

loadLocal = Production( pythonIdentifier ).action( lambda input, begin, xs: [ 'var', xs ] )





listDisplay = Production( Literal( '[' )  +  separatedList( expression )  +  Literal( ']' ) ).action( lambda input, begin, xs: [ 'listDisplay' ]  +  xs[1] )

parenForm = Production( Literal( '(' ) + expression + ')' ).action( lambda input, begin, xs: xs[1] )

enclosure = Production( parenForm | listDisplay )

atom = Production( enclosure | literal | loadLocal )

primary = Forward()


kwArg = Production( argName + '=' + expression ).action( lambda input, begin, xs: [ 'kwArg', xs[0], xs[2] ] )
argList = Production( Literal( '*' )  +  expression ).action( lambda input, begin, xs: [ 'argList', xs[1] ] )
kwArgList = Production( Literal( '**' )  +  expression ).action( lambda input, begin, xs: [ 'kwArgList', xs[1] ] )
arg = Production( kwArgList | argList | kwArg | expression )
listOfArgs = Production( Suppress( '(' )  -  separatedList( arg )  -  Suppress( ')' ) )
call = Production( ( primary + listOfArgs ).action( lambda input, begin, tokens: [ 'call', tokens[0] ] + tokens[1] ) )


subscript = Production( ( primary + '[' + expression + ']' ).action( lambda input, begin, tokens: [ 'subscript', tokens[0], tokens[2] ] ) )
slice = Production( ( primary + '[' + expression + ':' + expression + ']' ).action( lambda input, begin, tokens: [ 'slice', tokens[0], tokens[2], tokens[4] ] ) )
attr = Production( primary + '.' + attrName ).action( lambda input, begin, tokens: [ 'attr', tokens[0], tokens[2] ] )
primary  <<  Production( call | subscript | slice | attr | atom )

power = Forward()
power  <<  Production( ( primary  +  '**'  +  power ).action( lambda input, begin, xs: [ 'pow', xs[0], xs[2] ] )   |   primary )

_symToOp = { '+' : 'add',  '-' : 'sub',  '*' : 'mul',  '/' : 'div',  '%' : 'mod' }
mulDivMod = Forward()
mulDivMod  <<  Production( ( mulDivMod + ( Literal( '*' ) | '/' | '%' ) + power ).action( lambda input, begin, xs:  [ _symToOp[xs[1]], xs[0], xs[2] ] )  |  power )
addSub = Forward()
addSub  <<  Production( ( addSub + ( Literal( '+' ) | '-' ) + mulDivMod ).action( lambda input, begin, xs: [ _symToOp[xs[1]], xs[0], xs[2] ] )  |  mulDivMod )
			 
expression  <<  Production( addSub )
