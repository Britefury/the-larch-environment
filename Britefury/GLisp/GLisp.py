##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import string
import pyparsing

from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.GLisp.GLispFrame import GLispFrame
from Britefury.GLisp.GLispInterpreter import GLispInterpreterEnv, specialform
from Britefury.GLisp.GLispCompiler import compileGLispFunctionToPy

from Britefury.DocView.DocViewTokeniser import DocViewTokenDefinition, DocViewTokeniser

from Britefury.DocView.StyleSheet.DVStyleSheet import *





class GLispTokeniser (object):
	# Define a token
	def defineToken(self, name, parser):
		return DocViewTokenDefinition( name, parser )
	
	
	# Literal and word
	def literalSubtoken(self, matchString):
		return pyparsing.Literal( matchString )
	
	def wordSubtoken(self, initChars, bodyChars=None):
		return pyparsing.Word( initChars, bodyChars )
	
	
	# Combine
	def combineOr(self, x, y):
		return x | y
	
	
	# Constants
	def quotedString(self):
		return pyparsing.quotedString
	
	def whitespace(self):
		return string.whitespace
	

	



class GLispStyleSheet (object):
	def __init__(self):
		pass
	
	@specialform
	def defineSetValueAction(self, env, xs):
		if len(xs) < 4:
			raise TypeError, 'GLispStyleSheet.defineSetValueAction: needs a function name and a parameter list'
		
		name = xs[2]
		pyFunc = compileGLispFunctionToPy( xs[3:], compileSimpleGLispExprToPy, name )
		
		return DVStyleSheetSetValueAction( pyFunc )
	




def gSymGLispEnvironment():
	tokeniser = GLispTokeniser()
	styleSheet = GLispStyleSheet()
	
	return GLispInterpreterEnv( GLispFrame( tokeniser=tokeniser, styleSheet=styleSheet ) )



glisp = gSymGLispEnvironment()




import unittest
from Britefury.DocModel.DMIO import readSX
from Britefury.DocView.DocViewTokeniser import DocViewToken



class TestCase_gLisp (unittest.TestCase):
	def gLispExec(self, programText):
		return gSymGLispEnvironment().execute( readSX( programText ) )
	
	def gLispEval(self, programText):
		return gSymGLispEnvironment().evaluate( readSX( programText ) )
	
	
	def testTokeniser(self):
		self.assert_( isinstance( self.gLispEval( '@tokeniser' ), GLispTokeniser ) )
		
	def testTokWhitespace(self):
		self.assert_( self.gLispEval( '(@tokeniser whitespace)' )  ==  string.whitespace )

	def testTokQuotedString(self):
		self.assert_( self.gLispEval( '(@tokeniser quotedString)' )  ==  pyparsing.quotedString )
		
	def testTokLiteralSub(self):
		self.assert_( self.gLispEval( '(@tokeniser literalSubtoken test)' ).parseString( 'test' ).asList() == [ 'test' ] )

	def testTokWordSub(self):
		self.assert_( self.gLispEval( '(@tokeniser wordSubtoken abc)' ).parseString( 'aabbccbbaa' ).asList() == [ 'aabbccbbaa' ] )

	def testTokCombineOr(self):
		parser = self.gLispEval( '(@tokeniser combineOr (@tokeniser literalSubtoken test) (@tokeniser wordSubtoken abc))' )
		self.assert_( parser.parseString( 'test' ).asList() == [ 'test' ] )
		self.assert_( parser.parseString( 'aabbccbbaa' ).asList() == [ 'aabbccbbaa' ] )
		
	def testDefineToken(self):
		definition = self.gLispEval( '(@tokeniser defineToken testToken (@tokeniser wordSubtoken abc))' )
		self.assert_( definition._parser.parseString( 'aabbccbbaa' ).asList() == [ DocViewToken( 'testToken', 'aabbccbbaa' ) ] )
		
		

if __name__ == '__main__':
	unittest.main()
