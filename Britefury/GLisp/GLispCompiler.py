##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy

from Britefury.DocModel.DMListInterface import DMListInterface






def _compileGLispExprToPySrcAndPrecedence(x):
	_binaryOperatorPrecedenceTable = {
		'**' : 1,
		'*' : 4,
		'/' : 4,
		'%' : 4,
		'+' : 5,
		'-' : 5,
		'<<' : 6,
		'>>' : 6,
		'&' : 7,
		'^' : 8,
		'|' : 9,
		'<' : 10,
		'<=' : 10,
		'==' : 10,
		'!=' : 10,
		'>=' : 10,
		'>' : 10,
		'is' : 11,
		'in' : 12,
		'and' : 14,
		'or' : 15,
	}
	
	_unaryOperatorPrecedenceTable = {
		'~' : ( 2, False ),
		'-' : ( 3, False ),
		'not' : ( 13, True ),
	}
	
	def compileSubExpression(sub, outerPrecedence):
		src, innerPrecedence = _compileGLispExprToPySrcAndPrecedence( sub )
		if outerPrecedence is not None  and  innerPrecedence is not None  and  outerPrecedence <=innerPrecedence:
			return '(' + src + ')'
		else:
			return src

	if x is None:
		return 'None', None
	elif isinstance( x, str ):
		if x[0] == '@':
			return x[1:], None
		elif x[0] == '#':
			return x[1:], None
		else:
			return '\'' + x + '\'', None
	else:
		if len(x) == 0:
			return 'None', None
		elif len(x) == 1:
			return compileSubExpression( x[0], None ), None
		else:
			method = x[1]
			if method == '[]'  and  len(x) == 3:
				return '%s[%s]'  %  ( compileSubExpression( x[0], None ), compileSubExpression( x[2], None ) ), None
			elif method == '.'  and  len(x) == 3:
				return '%s.%s'  %  ( compileSubExpression( x[0], None ), x[2], None ), None
			elif method in _binaryOperatorPrecedenceTable   and   len(x) == 3:
				precedence = _binaryOperatorPrecedenceTable[method]
				return '%s %s %s'  %  ( compileSubExpression( x[0], precedence ), method, compileSubExpression( x[2], precedence ) ), precedence
			elif method in _unaryOperatorPrecedenceTable   and   len(x) == 2:
				precedence, bUseSpace = _unaryOperatorPrecedenceTable[method]
				space = ''
				if bUseSpace:
					space = ' '
				return '%s%s%s'  %  ( method, space, compileSubExpression( x[0], precedence ) ), precedence
			else:
				return '%s.%s(%s)'  %  ( compileSubExpression( x[0], None ), method, ', '.join( [ compileSubExpression( ex, None )   for ex in x[2:] ] ) ), None


def compileGLispExprToPySrc(x):
	return _compileGLispExprToPySrcAndPrecedence(x)[0]


def compileGLispFunctionToPySrc(xs, functionName):
	if len(xs) < 2:
		raise TypeError, 'Error compiling gLisp function: needs at least a parameter list'
	
	params = xs[0]
	source = xs[1:]
	
	if not isinstance( params, DMListInterface ):
		raise TypeError, 'Error compiling gLisp function: first parameter must be a list of variable names'
	
	pySrcHdr = 'def %s(%s):\n'  %  ( functionName, ', '.join( params[:] ) )
	pyLines = [ compileGLispExprToPySrc( srcLine ) + '\n'   for srcLine in source ]
	if len( pyLines ) == 0:
		pyLines = [ 'pass\n' ]
	else:
		pyLines[-1] = 'return ' + pyLines[-1]
	pyLines = [ '   ' + l   for l in pyLines ]
	
	return pySrcHdr + ''.join( pyLines )
	

def compileGLispFunctionToPy(xs, functionName, locals={}):
	pySrc = compileGLispFunctionToPySrc( xs, functionName )
	
	lcl = copy( locals )
	exec pySrc in lcl
	return lcl[functionName]




import unittest
from Britefury.DocModel.DMIO import readSX


class TestCase_gLisp (unittest.TestCase):
	def testCompileGLispExprToPy_empty(self):
		self.assert_( compileGLispExprToPySrc( readSX( '()' ) )  ==  'None' )

	def testCompileGLispExprToPy_variable(self):
		self.assert_( compileGLispExprToPySrc( readSX( '(@test)' ) )  ==  'test' )

	def testCompileGLispExprToPy_var_add(self):
		self.assert_( compileGLispExprToPySrc( readSX( '(@a + @b)' ) )  ==  'a + b' )

	def testCompileGLispExprToPy_var_index(self):
		self.assert_( compileGLispExprToPySrc( readSX( '(@a [] @b)' ) )  ==  'a[b]' )

	def testCompileGLispExprToPy_var_subexp(self):
		self.assert_( compileGLispExprToPySrc( readSX( '(@a + (@b * @c))' ) )  ==  'a + b * c' )

	def testCompileGLispExprToPy_var_func_subexp(self):
		self.assert_( compileGLispExprToPySrc( readSX( '(@a + (@b func @c))' ) )  ==  'a + b.func(c)' )
	
	def testCompileGLispExprToPy_var_func_subexp_2(self):
		self.assert_( compileGLispExprToPySrc( readSX( '(@a + (@b func @c @d (@e * @f)))' ) )  ==  'a + b.func(c, d, e * f)' )

	def testCompileGLispExprToPy_strings(self):
		self.assert_( compileGLispExprToPySrc( readSX( '(a + b)' ) )  ==  '\'a\' + \'b\'' )
		
	def testCompileGLispExprToPy_numbers(self):
		self.assert_( compileGLispExprToPySrc( readSX( '(#1 + #2)' ) )  ==  '1 + 2' )
		
	def testCompileGLispExprToPy_precedence(self):
		self.assert_( compileGLispExprToPySrc( readSX( '(@a + (@b * @c))' ) )  ==  'a + b * c' )
		self.assert_( compileGLispExprToPySrc( readSX( '((@a + @b) * @c)' ) )  ==  '(a + b) * c' )
		self.assert_( compileGLispExprToPySrc( readSX( '(@a + (@b + @c))' ) )  ==  'a + (b + c)' )
		self.assert_( compileGLispExprToPySrc( readSX( '((@a + @b) + @c)' ) )  ==  '(a + b) + c' )
		
		
	def testCompileGLispFunctionToPy(self):
		def makeFunc(src):
			return compileGLispFunctionToPy( readSX( src ), 'test' )

		def makeFuncSrc(src):
			return compileGLispFunctionToPySrc( readSX( src ), 'test' )

		glispSrc = '( (a b c) (@a lower) ((@a upper) + (@b * @c)) )'
		pySrc = \
		      'def test(a, b, c):\n'  +  \
		      '   a.lower()\n'  +  \
		      '   return a.upper() + b * c\n'
		
		self.assert_( makeFuncSrc( glispSrc ) == pySrc )
		self.assert_( makeFunc( glispSrc )('x', 'y', 3)  ==  'Xyyy' )
		
		

if __name__ == '__main__':
	unittest.main()
