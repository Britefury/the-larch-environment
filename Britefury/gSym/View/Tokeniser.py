##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pyparsing
import string

from Britefury.GLisp.PyCodeGen import pyt_compare, pyt_coerce, PyCodeGenError, PyVar, PyLiteral, PyLiteralValue, PyListLiteral, PyListComprehension, PyGetAttr, PyGetItem, PyGetSlice, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyIsInstance, PyReturn, PyRaise, PyTry, PyIf, PySimpleIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects
from Britefury.GLisp.GLispUtil import isGLispList, isGLispString
from Britefury.GLisp.GLispCompiler import raiseCompilerError, raiseRuntimeError, compileGLispExprToPyFunction, compileGLispCallParamToPyTree, GLispCompilerCouldNotCompileSpecial, GLispCompilerInvalidFormType, GLispCompilerInvalidFormLength, GLispCompilerInvalidItem

from Britefury.gSym.gMeta.GMetaComponent import GMetaComponent
from Britefury.gSym.View.InteractorEvent import InteractorEventTokenList






class TokenDefinition (object):
	def __init__(self, tokenClass, parser):
		self._tokenClass = tokenClass
		self._parser = parser.setParseAction( self._p_parseAction )


	def _p_parseAction(self, tokens):
		return InteractorEventTokenList.Token( self._tokenClass, tokens[0] )




class Tokeniser (object):
	def __init__(self, tokenDefinitions=None):
		if tokenDefinitions is not None  and  len( tokenDefinitions ) > 0:
			toks = tokenDefinitions[0]._parser
			for tok in tokenDefinitions[1:]:
				toks = toks | tok._parser
			self._parser = pyparsing.ZeroOrMore( toks )
			self._parser.leaveWhitespace()
		else:
			self._parser = None


	def tokenise(self, text):
		if self._parser is not None:
			return self._parser.parseString( text ).asList()
		else:
			return [ DocViewToken( '', text ) ]

		

def _compileTokenDefinition(srcXs, context, compileSpecial, compileGLispExprToPyTree):
	if len( srcXs )  !=  2:
		raiseCompilerError( GLispCompilerInvalidFormLength, definitionXs, 'token definition must consist of two entries; the token class and the token expression' )
		
	tokenClass = srcXs[0]
	tokenExprXs = srcXs[1]
	
	if not isGLispString( tokenClass ):
		raiseCompilerError( GLispCompilerInvalidFormType, tokenClass, 'token class must be a string' )
		
	return PyVar( '__gsym__TokenDefinition' )( PyLiteralValue( tokenClass ), compileGLispExprToPyTree( tokenExprXs, context, True, compileSpecial ) ).debug( srcXs )


def _compileTokeniser(srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
	"""
	($tokeniser <token_definitions...>)
	
	token definition:
	(token_class <token_expression>)
	"""
	definitionsXs = srcXs[1:]
	
	if bNeedResult:
		return PyVar( '__gsym__Tokeniser' )( PyListLiteral( [ _compileTokenDefinition( x, context, compileSpecial, compileGLispExprToPyTree )   for x in definitionsXs ] ) ).debug( srcXs )
	else:
		return None
		
			
		
	
		
		
class GMetaComponentTokeniser (GMetaComponent):
	def compileSpecial(self, srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
		name = srcXs[0]
		
		compileSubExp = lambda xs: compileGLispExprToPyTree( xs, context, True, compileSpecial )

		if name == '$tokeniser':
			"""
			($tokeniser <token_definitions...>)
			"""
			return _compileTokeniser( srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree )
		elif name == '$tokWord':
			"""
			($tokWord <initChars> [<bodyChars>])
			"""
			if len( srcXs ) < 2  or  len( srcXs ) > 3:
				raiseCompilerError( GLispCompilerInvalidFormLength, srcXs, 'token word definition requires one or two parameters; the initial characters, and optionally, the body characters' )
				
			if len( srcXs ) == 3:
				return PyVar( '__gsym__pyparsing' ).attr( 'Word' )( compileSubExp( srcXs[1] ), compileSubExp( srcXs[2] ) ).debug( srcXs )
			else:
				return PyVar( '__gsym__pyparsing' ).attr( 'Word' )( compileSubExp( srcXs[1] ) ).debug( srcXs )
		elif name == '$tokLiteral':
			"""
			($tokLiteral <match_string>)
			"""
			if len( srcXs ) != 2:
				raiseCompilerError( GLispCompilerInvalidFormLength, srcXs, 'token literal definition requires one parameter; the match string' )
				
			return PyVar( '__gsym__pyparsing' ).attr( 'Literal' )( compileSubExp( srcXs[1] ) ).debug( srcXs )
		elif name == '$tokKeyword':
			"""
			($tokKeyword <match_string>)
			"""
			if len( srcXs ) != 2:
				raiseCompilerError( GLispCompilerInvalidFormLength, srcXs, 'token keyword definition requires one parameter; the match string' )
				
			return PyVar( '__gsym__pyparsing' ).attr( 'Keyword' )( compileSubExp( srcXs[1] ) ).debug( srcXs )
		elif name == '$tokQuotedString':
			"""
			($tokQuotedString)
			"""
			if len( srcXs ) != 1:
				raiseCompilerError( GLispCompilerInvalidFormLength, srcXs, 'token quoted string requires no parameters' )
				
			return PyVar( '__gsym__pyparsing' ).attr( 'quotedString' ).debug( srcXs )
		elif name == '$tokDblQuotedString':
			"""
			($tokDblQuotedString)
			"""
			if len( srcXs ) != 1:
				raiseCompilerError( GLispCompilerInvalidFormLength, srcXs, 'token quoted string requires no parameters' )
				
			return PyVar( '__gsym__pyparsing' ).attr( 'dblQuotedString' ).debug( srcXs )
		
		raise GLispCompilerCouldNotCompileSpecial( srcXs )


	def getConstants(self):
		return {
			'__gsym__TokenDefinition' : TokenDefinition,
			'__gsym__Tokeniser' : Tokeniser,
			'__gsym__pyparsing' : pyparsing,
			'string' : string,
			}
	
	
	def getGlobalNames(self):
		return []

