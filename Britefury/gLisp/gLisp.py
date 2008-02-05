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
from Britefury.DocModel.DMInterpreter import DMInterpreterEnv, specialform

from Britefury.DocView.DocViewTokeniser import DocViewTokenDefinition, DocViewTokeniser

from Britefury.DocView.StyleSheet.DVStyleSheet import *




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
	

	
def compileGLispExpressionToPy(x, compileGLispExpr):
	if not isinstance( x, DMListInterface ):
		raise TypeError, 'Error compiling gLisp expression: input should be a list'
	
	return compileGLispExpr( x )


def compileSimpleGLispExprToPy(x):
	def compileSubExpression(sub):
		if isinstance( sub, DMListInterface ):
			return '(' + compileSimpleGLispExprToPy( sub ) + ')'
		else:
			if sub[0] != '@':
				raise ValueError, 'variable access must be proceeded by @'
			return sub[1:]
	
	if len(x) == 0:
		return 'None'
	elif len(x) == 1:
		return compileSubExpression( x[0] )
	else:
		method = x[1]
		if method == '[]'  and  len(x) == 3:
			return '%s[%s]'  %  ( compileSubExpression( x[0] ), compileSubExpression( x[2] ) )
		if method == '[]='  and  len(x) == 4:
			return '%s[%s] = %s'  %  ( compileSubExpression( x[0] ), compileSubExpression( x[2] ), compileSubExpression( x[3] ) )
		if method in [ '+', '-', '*', '/', '%', '**', '<<', '>>', '&', '|', '^', '<', '<=', '==', '!=', '>=', '>' ]   and   len(x) == 3:
			return '%s %s %s'  %  ( compileSubExpression( x[0] ), method, compileSubExpression( x[2] ) )
		else:
			return '%s.%s(%s)'  %  ( compileSubExpression( x[0] ), method, ', '.join( [ compileSubExpression( ex )   for ex in x[2:] ] ) )
		

def compileGLispFunctionToPy(xs, compileGLispExpr, functionName):
	if len(xs) < 2:
		raise TypeError, 'Error compiling gLisp function: needs at least a parameter list'
	
	params = xs[0]
	source = xs[1:]
	
	if not isinstance( params, DMListInterface ):
		raise TypeError, 'Error compiling gLisp function: first parameter must be a list of variable names'
	
	pySrcHdr = 'def %s(%s):\n'  %  ( functionName, ', '.join( params[:] ) )
	pyLines = [ compileGLispExpressionToPy( srcLine, compileGLispExpr ) + '\n'   for srcLine in source ]
	if len( pyLines ) == 0:
		pyLines = [ 'pass\n' ]
	else:
		pyLines[-1] = 'return ' + pyLines[-1]
	pyLines = [ '   ' + l   for l in pyLines ]
	
	pySrc = pySrcHdr + ''.join( pyLines )
	
	lcl = {}
	exec pySrc in lcl
	return lcl[functionName]
	
	
	
	
	
	
		



class GLispStyleSheet (object):
	def __init__(self):
		pass
	
	@specialform
	def defineSetValueAction(self, xs):
		if len(xs) < 4:
			raise TypeError, 'GLispStyleSheet.defineSetValueAction: needs a function name and a parameter list'
		
		name = xs[2]
		pyFunc = compileGLispFunctionToPy( xs[3:], compileSimpleGLispExprToPy, name )
		
		return DVStyleSheetSetValueAction( pyFunc )
	




def gSymGLispEnvironment():
	tokeniser = GLispTokeniser()
	styleSheet = GLispStyleSheet()
	
	return DMInterpreterEnv( tokeniser=tokeniser, styleSheet=styleSheet )



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
		self.assert_( definition._parser.parseString( 'aabbccbbaa' ).asList() == [ DocViewToken( 'testToken', 'aabbccbbaa' ) ] )
		
		
	def testCompileSimpleGLispExprToPy(self):
		self.assert_( compileSimpleGLispExprToPy( readSX( '()' ) )  ==  'None' )
		self.assert_( compileSimpleGLispExprToPy( readSX( '(@test)' ) )  ==  'test' )
		self.assert_( compileSimpleGLispExprToPy( readSX( '(@a + @b)' ) )  ==  'a + b' )
		self.assert_( compileSimpleGLispExprToPy( readSX( '(@a [] @b)' ) )  ==  'a[b]' )
		self.assert_( compileSimpleGLispExprToPy( readSX( '(@a []= @b @c)' ) )  ==  'a[b] = c' )
		self.assert_( compileSimpleGLispExprToPy( readSX( '(@a + (@b * @c))' ) )  ==  'a + (b * c)' )
		self.assert_( compileSimpleGLispExprToPy( readSX( '(@a + (@b func @c))' ) )  ==  'a + (b.func(c))' )
		self.assert_( compileSimpleGLispExprToPy( readSX( '(@a + (@b func @c @d (@e * @f)))' ) )  ==  'a + (b.func(c, d, (e * f)))' )
		
		
	def testCompileGLispFunctionToPy(self):
		def makeFunc(src):
			return compileGLispFunctionToPy( readSX( src ), compileSimpleGLispExprToPy, 'test' )
		
		self.assert_( makeFunc( '( (a b c) (@a lower) ((@a upper) + (@b * @c)) )' )('x', 'y', 3)  ==  'Xyyy' )



if __name__ == '__main__':
	unittest.main()
