##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocModel.DMListInterface import DMListInterface


def compileGLispExpressionToPy(x, compileGLispExpr):
	if not isinstance( x, DMListInterface ):
		raise TypeError, 'Error compiling gLisp expression: input should be a list'
	
	return compileGLispExpr( x )


def compileSimpleGLispExprToPySrc(x):
	def compileSubExpression(sub):
		if isinstance( sub, DMListInterface ):
			return '(' + compileSimpleGLispExprToPySrc( sub ) + ')'
		else:
			if sub[0] == '@':
				return sub[1:]
			elif sub[0] == '#':
				return sub[1:]
			else:
				return '\'' + sub + '\''
	
	if len(x) == 0:
		return 'None'
	elif len(x) == 1:
		return compileSubExpression( x[0] )
	else:
		method = x[1]
		if method == '[]'  and  len(x) == 3:
			return '%s[%s]'  %  ( compileSubExpression( x[0] ), compileSubExpression( x[2] ) )
		elif method == '.'  and  len(x) == 3:
			return '%s.%s'  %  ( compileSubExpression( x[0] ), compileSubExpression( x[2] ) )
		elif method in [ '+', '-', '*', '/', '%', '**', '<<', '>>', '&', '|', '^', '<', '<=', '==', '!=', '>=', '>' ]   and   len(x) == 3:
			return '%s %s %s'  %  ( compileSubExpression( x[0] ), method, compileSubExpression( x[2] ) )
		else:
			return '%s.%s(%s)'  %  ( compileSubExpression( x[0] ), method, ', '.join( [ compileSubExpression( ex )   for ex in x[2:] ] ) )
		

def compileGLispFunctionToPySrc(xs, compileGLispExpr, functionName):
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
	
	return pySrcHdr + ''.join( pyLines )
	



def compileGLispFunctionToPy(xs, compileGLispExpr, functionName):
	pySrc = compileGLispFunctionToPySrc( xs, compileGLispExpr, functionName )
	
	lcl = {}
	exec pySrc in lcl
	return lcl[functionName]




import unittest
from Britefury.DocModel.DMIO import readSX


class TestCase_gLisp (unittest.TestCase):
	def gLispExec(self, programText):
		return gSymGLispEnvironment().execute( readSX( programText ) )
	
	def gLispEval(self, programText):
		return gSymGLispEnvironment().evaluate( readSX( programText ) )
	
	
	def testCompileSimpleGLispExprToPy_empty(self):
		self.assert_( compileSimpleGLispExprToPySrc( readSX( '()' ) )  ==  'None' )

	def testCompileSimpleGLispExprToPy_variable(self):
		self.assert_( compileSimpleGLispExprToPySrc( readSX( '(@test)' ) )  ==  'test' )

	def testCompileSimpleGLispExprToPy_var_add(self):
		self.assert_( compileSimpleGLispExprToPySrc( readSX( '(@a + @b)' ) )  ==  'a + b' )

	def testCompileSimpleGLispExprToPy_var_index(self):
		self.assert_( compileSimpleGLispExprToPySrc( readSX( '(@a [] @b)' ) )  ==  'a[b]' )

	def testCompileSimpleGLispExprToPy_var_subexp(self):
		self.assert_( compileSimpleGLispExprToPySrc( readSX( '(@a + (@b * @c))' ) )  ==  'a + (b * c)' )

	def testCompileSimpleGLispExprToPy_var_func_subexp(self):
		self.assert_( compileSimpleGLispExprToPySrc( readSX( '(@a + (@b func @c))' ) )  ==  'a + (b.func(c))' )
	
	def testCompileSimpleGLispExprToPy_var_func_subexp_2(self):
		self.assert_( compileSimpleGLispExprToPySrc( readSX( '(@a + (@b func @c @d (@e * @f)))' ) )  ==  'a + (b.func(c, d, (e * f)))' )

	def testCompileSimpleGLispExprToPy_strings(self):
		self.assert_( compileSimpleGLispExprToPySrc( readSX( '(a + b)' ) )  ==  '\'a\' + \'b\'' )
		
	def testCompileSimpleGLispExprToPy_numbers(self):
		self.assert_( compileSimpleGLispExprToPySrc( readSX( '(#1 + #2)' ) )  ==  '1 + 2' )
		
		
	def testCompileGLispFunctionToPy(self):
		def makeFunc(src):
			return compileGLispFunctionToPy( readSX( src ), compileSimpleGLispExprToPySrc, 'test' )

		def makeFuncSrc(src):
			return compileGLispFunctionToPySrc( readSX( src ), compileSimpleGLispExprToPySrc, 'test' )

		glispSrc = '( (a b c) (@a lower) ((@a upper) + (@b * @c)) )'
		pySrc = \
		      'def test(a, b, c):\n'  +  \
		      '   a.lower()\n'  +  \
		      '   return (a.upper()) + (b * c)\n'
		
		self.assert_( makeFuncSrc( glispSrc ) == pySrc )
		self.assert_( makeFunc( glispSrc )('x', 'y', 3)  ==  'Xyyy' )



if __name__ == '__main__':
	unittest.main()
