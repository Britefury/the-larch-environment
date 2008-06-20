##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

from Britefury.Parser import Parser
from Britefury.Parser.GrammarUtils import Tokens
from Britefury.Parser.GrammarUtils import SeparatedList

from GSymCore.Languages.Python25.Keywords import keywordsSet


_pythonIdentifier = Tokens.identifier  &  ( lambda input, pos, result: result not in keywordsSet )
	
_asciiStringSLiteral = Parser.Production( Tokens.singleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'ascii', 'single', xs[1:-1] ] )
_asciiStringDLiteral = Parser.Production( Tokens.doubleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'ascii', 'double', xs[1:-1] ] )
_unicodeStringSLiteral = Parser.Production( Parser.Suppress( Parser.Literal( 'u' )  |  Parser.Literal( 'U' ) ) + Tokens.singleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'unicode', 'single', xs[0][1:-1] ] )
_unicodeStringDLiteral = Parser.Production( Parser.Suppress( Parser.Literal( 'u' )  |  Parser.Literal( 'U' ) ) + Tokens.doubleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'unicode', 'double', xs[0][1:-1] ] )
_regexAsciiStringSLiteral = Parser.Production( Parser.Suppress( Parser.Literal( 'r' )  |  Parser.Literal( 'R' ) ) + Tokens.singleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'ascii-regex', 'single', xs[0][1:-1] ] )
_regexAsciiStringDLiteral = Parser.Production( Parser.Suppress( Parser.Literal( 'r' )  |  Parser.Literal( 'R' ) ) + Tokens.doubleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'ascii-regex', 'double', xs[0][1:-1] ] )
_regexUnicodeStringSLiteral = Parser.Production( Parser.Suppress( Parser.Literal( 'ur' )  |  Parser.Literal( 'uR' )  |  Parser.Literal( 'Ur' )  |  Parser.Literal( 'UR' ) ) + Tokens.singleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'unicode-regex', 'single', xs[0][1:-1] ] )
_regexUnicodeStringDLiteral = Parser.Production( Parser.Suppress( Parser.Literal( 'ur' )  |  Parser.Literal( 'uR' )  |  Parser.Literal( 'Ur' )  |  Parser.Literal( 'UR' ) ) + Tokens.singleQuotedString ).action( lambda input, pos, xs: [ 'stringLiteral', 'unicode-regex', 'double', xs[0][1:-1] ] )
_shortStringLiteral = _asciiStringSLiteral | _asciiStringDLiteral | _unicodeStringSLiteral | _unicodeStringDLiteral |  \
				_regexAsciiStringSLiteral | _regexAsciiStringDLiteral | _regexUnicodeStringSLiteral | _regexUnicodeStringDLiteral



_decimalIntLiteral = Parser.Production( Tokens.decimalInteger ).action( lambda input, pos, xs: [ 'intLiteral', 'decimal', 'int', xs ] )
_decimalLongLiteral = Parser.Production( Tokens.decimalInteger + Parser.Suppress( Parser.Literal( 'l' )  |  Parser.Literal( 'L' ) ) ).action( lambda input, pos, xs: [ 'intLiteral', 'decimal', 'long', xs[0] ] )
_hexIntLiteral = Parser.Production( Tokens.hexInteger ).action( lambda input, pos, xs: [ 'intLiteral', 'hex', 'int', xs ] )
_hexLongLiteral = Parser.Production( Tokens.hexInteger + Parser.Suppress( Parser.Literal( 'l' )  |  Parser.Literal( 'L' ) ) ).action( lambda input, pos, xs: [ 'intLiteral', 'hex', 'long', xs[0] ] )

_integerLiteral = _decimalLongLiteral | _decimalIntLiteral | _hexLongLiteral | _hexIntLiteral

_floatLiteral = Parser.Production( Tokens.floatingPoint ).action( lambda input, pos, xs: [ 'floatLiteral', xs ] )

_imaginaryLiteral = Parser.Production( Parser.Combine( [ ( Tokens.floatingPoint | Tokens.decimalInteger ), Parser.Literal( 'j' ) ] ) ).action( lambda input, pos, xs: [ 'imaginaryLiteral', xs ] )


_literal = _shortStringLiteral | _imaginaryLiteral | _floatLiteral | _integerLiteral


_paramName = Parser.Production( _pythonIdentifier )
_attrName = Parser.Production( _pythonIdentifier )



_expression = Parser.Forward()

_loadLocal = Parser.Production( _pythonIdentifier ).action( lambda input, begin, xs: [ 'var', xs ] )

_kwParam = Parser.Production( _paramName + '=' + _expression ).action( lambda input, begin, xs: [ 'kwParam', xs[0], xs[2] ] )
_param = Parser.Production( _kwParam | _expression )
_parameterList = Parser.Production( Parser.Suppress( '(' )  -  SeparatedList.separatedList( _param )  -  Parser.Suppress( ')' ) )




_listDisplay = Parser.Production( Parser.Literal( '[' )  +  SeparatedList.separatedList( _expression )  +  Parser.Literal( ']' ) ).action( lambda input, begin, xs: [ 'listDisplay' ]  +  xs[1] )

_parenForm = Parser.Production( Parser.Literal( '(' ) + _expression + ')' ).action( lambda input, begin, xs: xs[1] )

_enclosure = Parser.Production( _parenForm | _listDisplay )

_atom = Parser.Production( _enclosure | _literal | _loadLocal )

_primary = Parser.Forward()

_call = Parser.Production( ( _primary + _parameterList ).action( lambda input, begin, tokens: [ 'call', tokens[0] ] + tokens[1] ) )
_subscript = Parser.Production( ( _primary + '[' + _expression + ']' ).action( lambda input, begin, tokens: [ 'subscript', tokens[0], tokens[2] ] ) )
_slice = Parser.Production( ( _primary + '[' + _expression + ':' + _expression + ']' ).action( lambda input, begin, tokens: [ 'slice', tokens[0], tokens[2], tokens[4] ] ) )
_attr = Parser.Production( _primary + '.' + _attrName ).action( lambda input, begin, tokens: [ 'attr', tokens[0], tokens[2] ] )
_primary  <<  Parser.Production( _call | _subscript | _slice | _attr | _atom )

_power = Parser.Forward()
_power  <<  Parser.Production( ( _primary  +  '**'  +  _power ).action( lambda input, begin, xs: [ 'pow', xs[0], xs[2] ] )   |   _primary )

_symToOp = { '+' : 'add',  '-' : 'sub',  '*' : 'mul',  '/' : 'div',  '%' : 'mod' }
_mulDivMod = Parser.Forward()
_mulDivMod  <<  Parser.Production( ( _mulDivMod + ( Parser.Literal( '*' ) | '/' | '%' ) + _power ).action( lambda input, begin, xs:  [ _symToOp[xs[1]], xs[0], xs[2] ] )  |  _power )
_addSub = Parser.Forward()
_addSub  <<  Parser.Production( ( _addSub + ( Parser.Literal( '+' ) | '-' ) + _mulDivMod ).action( lambda input, begin, xs: [ _symToOp[xs[1]], xs[0], xs[2] ] )  |  _mulDivMod )
			 
_expression  <<  Parser.Production( _addSub )
