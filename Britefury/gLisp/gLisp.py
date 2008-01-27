##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import string
import pyparsing

from Britefury.DocModel.DMInterpreter import DMInterpreterEnv

from Britefury.DocView.DocViewTokeniser import DocViewTokenDefinition, DocViewTokeniser




class GLispTokeniser (object):
	def __init__(self):
		pass
	
	
	# Define a token
	def defineToken(self, name, parser):
		return DocViewTokenDefinition( name, parser )
	
	
	# Literal and word
	def literalSubtoken(self, matchString):
		return pyparsing.Literal( matchString )
	
	def wordSubtoken(self, initChars):
		return pyparsing.Word( initChars )
	
	
	# Combine
	def combineOr(self, x, y):
		return x | y
	
	
	# Constants
	def quotedString(self):
		return pyparsing.quotedString
	
	def whitespace(self):
		return string.whitespace
	




def gSymGLispEnvironment():
	tokeniser = GLispTokeniser()
	
	return DMInterpreterEnv( tokeniser=tokeniser )



glisp = gSymGLispEnvironment()




import unittest
from Britefury.DocModel.DMIO import readSX
from Britefury.DocView.DocViewTokeniser import DocViewToken



class TestCase_gLisp (unittest.TestCase):
	def gLispExec(self, programText):
		return gSymGLispEnvironment().dmExec( readSX( programText ) )
	
	def gLispEval(self, programText):
		return gSymGLispEnvironment().dmEval( readSX( programText ) )
	
	
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
		self.assert_( definition._tokenClassName == 'testToken' )
		self.assert_( definition._parser.parseString( 'aabbccbbaa' ).asList() == [ DocViewToken( 'testToken', 'aabbccbbaa' ) ] )



if __name__ == '__main__':
	unittest.main()
