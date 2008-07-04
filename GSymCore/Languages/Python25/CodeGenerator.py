##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.gSym.gSymCodeGenerator import GSymCodeGenerator


def _indent(x):
	lines = x.split( '\n' )
	return '\n'.join( [ '\t' + l   for l in lines ] )


class Python25CodeGenerator (GSymCodeGenerator):
	# Misc
	def python25Module(self, node, *content):
		return '\n'.join( [ self( line )   for line in content ] )

	
	def nilExpr(self, node):
		return '<NIL>'
	
	def blankLine(self, node):
		return ''
	

	
	
	# String literal
	def stringLiteral(self, node, format, quotation, value):
		return repr( value )
	
	
	
	# Integer literal
	def intLiteral(self, node, format, numType, value):
		return repr( value )
	
	

	# Float literal
	def floatLiteral(self, node, value):
		return repr( value )

	
	
	# Imaginary literal
	def imaginaryLiteral(self, node, value):
		return repr( value )
	
	
	
	# Target
	def singleTarget(self, node, name):
		return name
	
	def tupleTarget(self, node, *x):
		return '( '  +  ', '.join( [ self( i )   for i in x ]  +  ', ' )  +  ' )'
	
	def listTarget(self, node, *x):
		return '[ '  +  ', '.join( [ self( i )   for i in x ]  +  ', ' )  +  ' ]'
	
	

	# Variable reference
	def var(self, node, name):
		return name

	
	
	# Tuple literal	
	def tupleLiteral(self, node, *x):
		return '( '  +  ', '.join( [ self( i )   for i in x ]  +  ', ' )  +  ' )'

	
	
	# List literal
	def listLiteral(self, node, *x):
		return '[ '  +  ', '.join( [ self( i )   for i in x ] )  +  ' ]'
	
	
	
	# List comprehension
	def listFor(self, node, target, source):
		return 'for ' + self( target ) + ' in ' + self( source )
	
	def listIf(self, node, condition):
		return 'if ' + self( condition )
	
	def listComprehension(self, node, expr, *xs):
		return '[ ' + self( expr ) + '   ' + '   '.join( [ self( x )   for x in xs ] )  +  ' ]'
	
	

	# Generator expression
	def genFor(self, node, target, source):
		return 'for ' + self( target ) + ' in ' + self( source )
	
	def genIf(self, node, condition):
		return 'if ' + self( condition )
	
	def generatorExpression(self, node, expr, *comps):
		return '( ' + self( expr ) + '   ' + '   '.join( [ self( c )   for c in comps ] )  +  ' )'
	
	
	
	# Dictionary literal
	def keyValuePair(self, node, key, value):
		return self( key ) + ':' + self( value )
	
	def dictLiteral(self, node, *x):
		return '{ '  +  ', '.join( [ self( i )   for i in x ] )  +  ' }'
	
	
	
	# Yield expression
	def yieldExpression(self, node, expr):
		return '(yield ' + self( expr ) + ')'
		
	
	
	# Attribute ref
	def attributeRef(self, node, target, name):
		return self( target ) + '.' + name

	

	# Subscript
	def subscriptSlice(self, node, lower, upper):
		return self( lower ) + ':' + self( upper )

	def subscriptLongSlice(self, node, lower, upper, stride):
		return self( lower ) + ':' + self( upper ) + ':' + self( stride )
	
	def ellipsis(self, node):
		return '...'

	def subscriptTuple(self, node, *x):
		return '( '  +  ', '.join( [ self( i )   for i in x ]  +  ', ' )  +  ' )'

	def subscript(self, node, target, index):
		return self( target ) + '[' + self( index ) + ']'
	

	
	# Call	
	def kwArg(self, node, name, value):
		return name + '=' + self( value )
	
	def argList(self, node, value):
		return '*' + self( value )
	
	def kwArgList(self, node, value):
		return '*' + self( value )
	
	def call(self, node, target, *params):
		return self( target ) + '( ' + ', '.join( [ self( p )   for p in params ] ) + ' )'
	
	
	
	# Operators
	def pow(self, node, x, y):
		return '( '  +  self( x )  +  ' ** '  +  self( y )  +  ' )'
	
	
	def invert(self, node, x):
		return '~( '  +  self( x )  +  ' )'
	
	def negate(self, node, x):
		return '-( '  +  self( x )  +  ' )'
	
	def pos(self, node, x):
		return '+( '  +  self( x )  +  ' )'
	
	
	def mul(self, node, x, y):
		return '( '  +  self( x )  +  ' * '  +  self( y )  +  ' )'
	
	def div(self, node, x, y):
		return '( '  +  self( x )  +  ' / '  +  self( y )  +  ' )'
	
	def mod(self, node, x, y):
		return '( '  +  self( x )  +  ' % '  +  self( y )  +  ' )'
	
	def add(self, node, x, y):
		return '( '  +  self( x )  +  ' + '  +  self( y )  +  ' )'
	
	def sub(self, node, x, y):
		return '( '  +  self( x )  +  ' - '  +  self( y )  +  ' )'
	
	
	def lshift(self, node, x, y):
		return '( '  +  self( x )  +  ' << '  +  self( y )  +  ' )'
	
	def rshift(self, node, x, y):
		return '( '  +  self( x )  +  ' >> '  +  self( y )  +  ' )'
	
	
	def bitAnd(self, node, x, y):
		return '( '  +  self( x )  +  ' & '  +  self( y )  +  ' )'
	
	def bitXor(self, node, x, y):
		return '( '  +  self( x )  +  ' ^ '  +  self( y )  +  ' )'
	
	def bitOr(self, node, x, y):
		return '( '  +  self( x )  +  ' | '  +  self( y )  +  ' )'
	

	def lte(self, node, x, y):
		return '( '  +  self( x )  +  ' <= '  +  self( y )  +  ' )'
	
	def lt(self, node, x, y):
		return '( '  +  self( x )  +  ' < '  +  self( y )  +  ' )'
	
	def gte(self, node, x, y):
		return '( '  +  self( x )  +  ' >= '  +  self( y )  +  ' )'
	
	def gt(self, node, x, y):
		return '( '  +  self( x )  +  ' > '  +  self( y )  +  ' )'
	
	def eq(self, node, x, y):
		return '( '  +  self( x )  +  ' == '  +  self( y )  +  ' )'
	
	def neq(self, node, x, y):
		return '( '  +  self( x )  +  ' != '  +  self( y )  +  ' )'
	

	def cmpIsNot(self, node, x, y):
		return '( '  +  self( x )  +  ' is not '  +  self( y )  +  ' )'
	
	def cmpIs(self, node, x, y):
		return '( '  +  self( x )  +  ' is '  +  self( y )  +  ' )'
	
	def cmpNotIn(self, node, x, y):
		return '( '  +  self( x )  +  ' not in '  +  self( y )  +  ' )'
	
	def cmpIn(self, node, x, y):
		return '( '  +  self( x )  +  ' in '  +  self( y )  +  ' )'

	
	def boolNot(self, node, x):
		return '(not '  +  self( x )  +  ')'
	
	def boolAnd(self, node, x, y):
		return '( '  +  self( x )  +  ' and '  +  self( y )  +  ' )'
	
	def boolOr(self, node, x, y):
		return '( '  +  self( x )  +  ' or '  +  self( y )  +  ' )'
	
	
	
	
	# Parameters	
	def simpleParam(self, node, name):
		return name
	
	def defaultValueParam(self, node, name, value):
		return name  +  ' = '  +  self( value )
	
	def paramList(self, node, name):
		return '*'  +  name
	
	def kwParamList(self, node, name):
		return '**'  +  name
	
	
	
	# Lambda expression
	def lambdaExpr(self, node, params, expr):
		return 'lambda '  +  ', '.join( [ self( p )   for p in params ] )  +  ': '  +  self( expr )
	
	
	
	# Conditional expression
	def conditionalExpr(self, node, condition, expr, elseExpr):
		return self( expr )  +  '   '  +  'if '  +  self( condition )  +  ' else '  +  self( elseExpr )

	
	
	# Assignment statement
	def assignmentStmt(self, node, targets, value):
		return ''.join( [ self( t ) + ' = '   for t in targets ] )  +  self( value )
	
	
	
	# Return statement
	def returnStmt(self, node, value):
		return 'return '  +  self( value )
	
	
	
	# If statement
	def ifStmt(self, node, value, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] )
		return 'if '  +  self( value ) + ':\n'  +  _indent( suiteText )
	

	
