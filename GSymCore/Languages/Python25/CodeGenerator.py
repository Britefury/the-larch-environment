##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
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
	
	
	
	# Yield expression and yield atom
	def yieldExpression(self, node, expr):
		return 'yield ' + self( expr )
		
	def yieldAtom(self, node, expr):
		return '(yield ' + self( expr ) + ')'
		
	
	
	# Attribute ref
	def attributeRef(self, node, target, name):
		return self( target ) + '.' + name

	

	# Subscript
	def subscriptSlice(self, node, lower, upper):
		txt = lambda x:  self( x )   if x != '<nil>'   else ''
		return txt( lower ) + ':' + txt( upper )

	def subscriptLongSlice(self, node, lower, upper, stride):
		txt = lambda x:  self( x )   if x != '<nil>'   else ''
		return txt( lower ) + ':' + txt( upper ) + ':' + txt( stride )
	
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
	
	def call(self, node, target, *args):
		return self( target ) + '( ' + ', '.join( [ self( a )   for a in args ] ) + ' )'
	
	
	
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
	

	def isNotTest(self, node, x, y):
		return '( '  +  self( x )  +  ' is not '  +  self( y )  +  ' )'
	
	def isTest(self, node, x, y):
		return '( '  +  self( x )  +  ' is '  +  self( y )  +  ' )'
	
	def notInTest(self, node, x, y):
		return '( '  +  self( x )  +  ' not in '  +  self( y )  +  ' )'
	
	def inTest(self, node, x, y):
		return '( '  +  self( x )  +  ' in '  +  self( y )  +  ' )'

	
	def notTest(self, node, x):
		return '(not '  +  self( x )  +  ')'
	
	def andTest(self, node, x, y):
		return '( '  +  self( x )  +  ' and '  +  self( y )  +  ' )'
	
	def orTest(self, node, x, y):
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

	
	
	# Assert statement
	def assertStmt(self, node, condition, fail):
		return 'assert '  +  self( condition )  +  ( ', ' + self( fail )   if fail != '<nil>'   else  '' )
	
	
	# Assignment statement
	def assignmentStmt(self, node, targets, value):
		return ''.join( [ self( t ) + ' = '   for t in targets ] )  +  self( value )
	
	
	# Augmented assignment statement
	def augAssignStmt(self, node, op, target, value):
		return self( target )  +  ' '  +  op  +  ' '  +  self( value )
	
	
	# Pass statement
	def passStmt(self, node):
		return 'pass'
	
	
	# Del statement
	def delStmt(self, node, target):
		return 'del '  +  self( target )
	
	
	# Return statement
	def returnStmt(self, node, value):
		return 'return '  +  self( value )
	
	
	# Yield statement
	def yieldStmt(self, node, value):
		return 'yield '  +  self( value )
	
	
	# Raise statement
	def raiseStmt(self, node, *xs):
		return 'raise '  +  ', '.join( [ self( x )   for x in xs   if x != '<nil>' ] )
	
	
	# Break statement
	def breakStmt(self, node):
		return 'break'
	
	
	# Continue statement
	def continueStmt(self, node):
		return 'continue'
	
	
	# Import statement
	def relativeModule(self, node, name):
		return name
	
	def moduleImport(self, node, name):
		return name
	
	def moduleImportAs(self, node, name, asName):
		return name + ' as ' + asName
	
	def moduleContentImport(self, node, name):
		return name
	
	def moduleContentImportAs(self, node, name, asName):
		return name + ' as ' + asName
	
	def importStmt(self, node, *xs):
		return 'import '  +  ', '.join( [ self( x )   for x in xs ] )
	
	def fromImportStmt(self, node, moduleName, *xs):
		return 'from ' + moduleName + ' import ' + ', '.join( [ self( x )   for x in xs ] )
	
	def fromImportAllStmt(self, node, moduleName):
		return 'from ' + moduleName + ' import *'
	
	
	# Global statement
	def globalVar(self, node, name):
		return name
	
	def globalStmt(self, node, *xs):
		return 'global '  +  ', '.join( xs )
	
	
	# Exec statement
	def execStmt(self, node, src, loc, glob):
		txt = 'exec '  +  self( src )
		if loc != '<nil>':
			txt += ' in '  +  self( loc )
		if glob != '<nil>':
			txt += ', '  +  self( glob )
		return txt
	
	
	# If statement
	def ifStmt(self, node, condition, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'if '  +  self( condition ) + ':\n'  +  _indent( suiteText )
	

	# Elif statement
	def elifStmt(self, node, condition, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'elif '  +  self( condition ) + ':\n'  +  _indent( suiteText )
	

	# Else statement
	def elseStmt(self, node, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'else:\n'  +  _indent( suiteText )
	

	# While statement
	def whileStmt(self, node, condition, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'while '  +  self( condition ) + ':\n'  +  _indent( suiteText )
	

	# For statement
	def forStmt(self, node, target, source, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'for '  +  self( target )  +  ' in '  +  self( source )  +  ':\n'  +  _indent( suiteText )
	

	# Try statement
	def tryStmt(self, node, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'try:\n'  +  _indent( suiteText )
	

	# Except statement
	def exceptStmt(self, node, exc, target, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		txt = 'except'
		if exc != '<nil>':
			txt += ' ' + self( exc )
		if target != '<nil>':
			txt += ', ' + self( target )
		return txt + ':\n'  +  _indent( suiteText )
	

	# Finally statement
	def finallyStmt(self, node, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'finally:\n'  +  _indent( suiteText )
	

	# With statement
	def withStmt(self, node, expr, target, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'with '  +  self( expr )  +  ( ' as ' + self( target )   if target != '<nil>'   else   '' )  +  ':\n'  +  _indent( suiteText )
	
	
	# Def statement
	def defStmt(self, node, name, params, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'def '  +  name  +  '('  +  ', '.join( [ self( p )   for p in params ] )  +  '):\n'  +    +  _indent( suiteText )
	

	# Deco statement
	def decoStmt(self, node, name, args):
		text = '@' + name
		if args != '<nil>':
			text += '( ' + ', '.join( [ self( a )   for a in args ] ) + ' )'
		return text
	

	# Class statement
	def classStmt(self, node, name, inheritance, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		text = 'class '  +  name
		if inheritance != '<nil>':
			text += '('  +  self( inheritance )  +  ')'
		return text  +  '):\n'  +    +  _indent( suiteText )
	
	
	
	# Comment statement
	def commentStmt(self, node, comment):
		return '#' + comment
	

