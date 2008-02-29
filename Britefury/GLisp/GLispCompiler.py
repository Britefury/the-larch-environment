##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy
import string

from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.GLisp.GLispInterpreter import isGLispList




class GLispCompilerError (Exception):
	pass




def _compileGLispExprToPySrcAndPrecedence(x, compileSpecialExpr):
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
		src, innerPrecedence = _compileGLispExprToPySrcAndPrecedence( sub, compileSpecialExpr )
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
		elif x[0] == '/list':
			return '[ %s ]'  %  ( ', '.join( [ compileSubExpression( e, None )   for e in x[1:] ] ), ), None
		elif isinstance( x[0], str )  and  x[0][0] == '/'  and  compileSpecialExpr is not None:
			return compileSpecialExpr( x ), None
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


def compileGLispExprToPySrc(x, compileSpecialExpr=None):
	return _compileGLispExprToPySrcAndPrecedence(x, compileSpecialExpr)[0]



def _compileWhere(xs, compileSpecialExpr):
	"""
	($where ((name0 value0) (name1 value1) ... (nameN valueN)) (statements_to_execute))
	"""
	if len( xs ) < 2:
		raise GLispCompilerError, '$where must have have at least 1 parameter; the binding list'

	bindings = xs[1]
	statements = xs[2:]
	
	if not isGLispList( bindings ):
		raise GLispCompilerError, '$where bindings must be a list of pairs'
	
	pyLines = []
	
	boundNames = set()
	for binding in bindings:
		if not isGLispList( binding )  or  len( binding ) != 2:
			raise GLispCompilerError, '$where binding must be a name value pair'
		
		if binding[0][0] != '@':
			raise GLispCompilerError, '$where binding name must start with @'
		
		name = binding[0][1:]
		valueExpr = binding[1]
		if name in boundNames:
			raise GLispCompilerError, '$where cannot bind variable \'%d\' more than once'  %  ( binding[0], )
		
		valueExprPySrc = compileGLispExprToPySrc( valueExpr, compileSpecialExpr )
		
		pyLines.append( '%s = %s'  %  ( name, valueExprPySrc ) )
		
		boundNames.add( name )
		
	for srcLine in statements:
		pyLines.extend( compileGLispStatementToPySrc( srcLine, compileSpecialExpr ) )

	return pyLines



def compileGLispStatementToPySrc(x, compileSpecialExpr=None):
	if isGLispList( x ):
		if len( x ) >= 1:
			if x[0] == '$where':
				return _compileWhere( x, compileSpecialExpr )
				
	
	return [ compileGLispExprToPySrc( x, compileSpecialExpr ) ]


def compileGLispFunctionToPySrc(xs, functionName, params, compileSpecialExpr=None):
	"""
	Compile a GLisp function to Python source code
	compileGLispFunctionToPySrc( xs, functionName, params, compileSpecialExpr=None )  ->  <source_text>
	
	Compiles the GLisp content in @xs into Python source code.
	A function is defined. It is given the name passed in @functionName. Its parameter list is taken from @params.
	@compileSpecialExpr can be used to customise the compilation of special expressions (start with a /).
	"""
	pySrcHdr = 'def %s(%s):\n'  %  ( functionName, ', '.join( params ) )
	pyLines = []
	for srcLine in xs:
		pyLines.extend( compileGLispStatementToPySrc( srcLine, compileSpecialExpr ) )
	if len( pyLines ) == 0:
		pyLines = [ 'pass' ]
	else:
		pyLines[-1] = 'return ' + pyLines[-1]
	pyLines = [ '  ' + l + '\n'   for l in pyLines ]
	
	return pySrcHdr + ''.join( pyLines )
	

def compileGLispFunctionToPy(xs, functionName, params, compileSpecialExpr=None, locals={}):
	pySrc = compileGLispFunctionToPySrc( xs, functionName, params, compileSpecialExpr )
	
	lcl = copy( locals )
	exec pySrc in lcl
	return lcl[functionName]




_pyIdentifierChars = string.ascii_letters + string.digits + '_'
def filterIdentifierForPy(identifier):
	return ''.join( [ c  for c in identifier   if c in _pyIdentifierChars ] )



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
		
		
	def testCompileGLispExprToPy_list(self):
		self.assert_( compileGLispExprToPySrc( readSX( '(/list (@a + (@b * @c)) #1 #2 @d)' ) )  ==  '[ a + b * c, 1, 2, d ]' )
		

	def testCompileGLispFunctionToPy(self):
		def makeFunc(src, params):
			return compileGLispFunctionToPy( readSX( src ), 'test', params )

		def makeFuncSrc(src, params):
			return compileGLispFunctionToPySrc( readSX( src ), 'test', params )

		glispSrc = '( (@a lower) ((@a upper) + (@b * @c)) )'
		pySrc = \
		      'def test(a, b, c):\n'  +  \
		      '  a.lower()\n'  +  \
		      '  return a.upper() + b * c\n'
		self.assert_( makeFuncSrc( glispSrc, [ 'a', 'b', 'c' ] ) == pySrc )
		self.assert_( makeFunc( glispSrc, [ 'a', 'b', 'c' ] )('x', 'y', 3)  ==  'Xyyy' )
		
		
		

	def testCompileGLispFunctionToPy_where(self):
		def makeFunc(src, params):
			return compileGLispFunctionToPy( readSX( src ), 'test', params )

		def makeFuncSrc(src, params):
			return compileGLispFunctionToPySrc( readSX( src ), 'test', params )

		glispSrc = """
		(
		  ($where
		    ( (@p (@a + @b))  (@q  (@b + @c)) )
		  
		    (@p * @q)
		  )
		)
		"""
		pySrc = \
		      'def test(a, b, c):\n'  +  \
		      '  p = a + b\n'  +  \
		      '  q = b + c\n'  +  \
		      '  return p * q\n'
		
		self.assert_( makeFuncSrc( glispSrc, [ 'a', 'b', 'c' ] ) == pySrc )
		self.assert_( makeFunc( glispSrc, [ 'a', 'b', 'c' ] )(3, 5, 7)  ==  96 )
		
		
if __name__ == '__main__':
	unittest.main()
