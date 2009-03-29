##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.gSymCodeGenerator import GSymCodeGeneratorListNodeDispatch

from Britefury.Util.NodeUtil import isNullNode


def _indent(x):
	lines = x.split( '\n' )
	lines = [ '\t' + l   for l in lines ]
	if lines[-1] == '\t':
		lines[-1] = ''
	return '\n'.join( lines )


class CodeGeneratorUnparsedException (Exception):
	pass



class Python25CodeGenerator (GSymCodeGeneratorListNodeDispatch):
	# Misc
	def blankLine(self, node):
		return ''
	

	def UNPARSED(self, node, value):
		raise CodeGeneratorUnparsedException
	
	
	# String literal
	def stringLiteral(self, node, format, quotation, value):
		return repr( value.toString() )
	
	
	
	# Integer literal
	def intLiteral(self, node, format, numType, value):
		format = format.toString()
		numType = numType.toString()
		value = value.toString()

		if numType == 'int':
			if format == 'decimal':
				valueString = '%d'  %  int( value )
			elif format == 'hex':
				valueString = '0x%x'  %  int( value, 16 )
			else:
				raise ValueError, 'invalid integer literal format'
		elif numType == 'long':
			if format == 'decimal':
				valueString = '%dL'  %  long( value )
			elif format == 'hex':
				valueString = '0x%xL'  %  long( value, 16 )
			else:
				raise ValueError, 'invalid integer literal format'
		else:
			raise ValueError, 'invalid integer literal type'
				
		return valueString

	
	

	# Float literal
	def floatLiteral(self, node, value):
		return value.toString()

	
	
	# Imaginary literal
	def imaginaryLiteral(self, node, value):
		return value.toString()
	
	
	
	# Target
	def singleTarget(self, node, name):
		return name.toString()
	
	def tupleTarget(self, node, *x):
		return '( '  +  ', '.join( [ self( i )   for i in x ] )  +  ', )'
	
	def listTarget(self, node, *x):
		return '[ '  +  ', '.join( [ self( i )   for i in x ] )  +  ' ]'
	
	

	# Variable reference
	def var(self, node, name):
		return name.toString()

	
	
	# Tuple literal	
	def tupleLiteral(self, node, *x):
		return '( '  +  ', '.join( [ self( i )   for i in x ] )  +  ', )'

	
	
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
	def yieldExpr(self, node, expr):
		return 'yield ' + self( expr )
		
	def yieldAtom(self, node, expr):
		return '(yield ' + self( expr ) + ')'
		
	
	
	# Attribute ref
	def attributeRef(self, node, target, name):
		return self( target ) + '.' + name

	

	# Subscript
	def subscriptSlice(self, node, lower, upper):
		txt = lambda x:  self( x )   if not isNullNode( x )   else ''
		return txt( lower ) + ':' + txt( upper )

	def subscriptLongSlice(self, node, lower, upper, stride):
		txt = lambda x:  self( x )   if not isNullNode( x )   else ''
		return txt( lower ) + ':' + txt( upper ) + ':' + txt( stride )
	
	def ellipsis(self, node):
		return '...'

	def subscriptTuple(self, node, *x):
		return '('  +  ','.join( [ self( i )   for i in x ] )  +  ',)'

	def subscript(self, node, target, index):
		return self( target ) + '[' + self( index ) + ']'
	

	
	# Call	
	def kwArg(self, node, name, value):
		return name.toString() + '=' + self( value )
	
	def argList(self, node, value):
		return '*' + self( value )
	
	def kwArgList(self, node, value):
		return '**' + self( value )
	
	def call(self, node, target, *args):
		return self( target ) + '( ' + ', '.join( [ self( a )   for a in args ] ) + ' )'
	
	
	
	# Operators
	def pow(self, node, x, y):
		return '( '  +  self( x )  +  ' ** '  +  self( y )  +  ' )'
	
	
	def invert(self, node, x):
		return '( ~'  +  self( x )  +  ' )'
	
	def negate(self, node, x):
		return '( -'  +  self( x )  +  ' )'
	
	def pos(self, node, x):
		return '( +'  +  self( x )  +  ' )'
	
	
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
		return name.toString()
	
	def defaultValueParam(self, node, name, value):
		return name.toString()  +  '='  +  self( value )
	
	def paramList(self, node, name):
		return '*'  +  name.toString()
	
	def kwParamList(self, node, name):
		return '**'  +  name.toString()
	
	
	
	# Lambda expression
	def lambdaExpr(self, node, params, expr):
		return '( lambda '  +  ', '.join( [ self( p )   for p in params ] )  +  ': '  +  self( expr ) + ' )'
	
	
	
	# Conditional expression
	def conditionalExpr(self, node, condition, expr, elseExpr):
		return self( expr )  +  '   if '  +  self( condition )  +  '   else '  +  self( elseExpr )

	
	
	# Assert statement
	def assertStmt(self, node, condition, fail):
		return 'assert '  +  self( condition )  +  ( ', ' + self( fail )   if fail != '<nil>'   else  '' )
	
	
	# Assignment statement
	def assignmentStmt(self, node, targets, value):
		return ''.join( [ self( t ) + ' = '   for t in targets ] )  +  self( value )
	
	
	# Augmented assignment statement
	def augAssignStmt(self, node, op, target, value):
		return self( target )  +  ' '  +  op.toString()  +  ' '  +  self( value )
	
	
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
		params = ', '.join( [ self( x )   for x in xs   if not isNullNode( x ) ] )
		if params != '':
			return 'raise ' + params
		else:
			return 'raise'
	
	
	# Break statement
	def breakStmt(self, node):
		return 'break'
	
	
	# Continue statement
	def continueStmt(self, node):
		return 'continue'
	
	
	# Import statement
	def relativeModule(self, node, name):
		return name.toString()
	
	def moduleImport(self, node, name):
		return name.toString()
	
	def moduleImportAs(self, node, name, asName):
		return name.toString() + ' as ' + asName.toString()
	
	def moduleContentImport(self, node, name):
		return name.toString()
	
	def moduleContentImportAs(self, node, name, asName):
		return name.toString() + ' as ' + asName.toString()
	
	def importStmt(self, node, *xs):
		return 'import '  +  ', '.join( [ self( x )   for x in xs ] )
	
	def fromImportStmt(self, node, mod, *xs):
		return 'from ' + self( mod ) + ' import ' + ', '.join( [ self( x )   for x in xs ] )
	
	def fromImportAllStmt(self, node, mod):
		return 'from ' + self( mod ) + ' import *'
	
	
	# Global statement
	def globalVar(self, node, name):
		return name
	
	def globalStmt(self, node, *xs):
		return 'global '  +  ', '.join( [ self( x )   for x in xs ] )
	
	
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
		return 'def '  +  name.toString()  +  '('  +  ', '.join( [ self( p )   for p in params ] )  +  '):\n'  +  _indent( suiteText )
	

	# Deco statement
	def decoStmt(self, node, name, args):
		text = '@' + name.toString()
		if not isNullNode( args ):
			text += '( ' + ', '.join( [ self( a )   for a in args ] ) + ' )'
		return text
	

	# Class statement
	def classStmt(self, node, name, inheritance, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		text = 'class '  +  name
		if not isNullNode( inheritance ):
			text += ' ('  +  ', '.join( [ self( h )   for h in inheritance ] )  +  ')'
		return text  +  ':\n'  +  _indent( suiteText )
	
	
	
	# Comment statement
	def commentStmt(self, node, comment):
		return '#' + comment

	
	# Module
	def python25Module(self, node, *content):
		return '\n'.join( [ self( line )   for line in content ] )

	

	
	
import unittest
from BritefuryJ.DocModel import DMIORead

class TestCase_Python25CodeGenerator (unittest.TestCase):
	def _testSX(self, sx, expected):
		try:
			data = DMIORead.readSX( sx )
		except DMIORead.ParseSXErrorException:
			print 'SX Parse error'
			self.fail()
		
		gen = Python25CodeGenerator()
		result = gen( data )
		
		if result != expected:
			print 'UNEXPECTED RESULT'
			print 'EXPECTED:'
			print expected.replace( '\n', '\\n' ) + '<<'
			print 'RESULT:'
			print result.replace( '\n', '\\n' ) + '<<'
			
		self.assert_( result == expected )
		
		
	def _binOpTest(self, sxOp, expectedOp):
		self._testSX( '(%s (var a) (var b))'  %  sxOp,  '( a %s b )'  %  expectedOp )
		
		
	def test_blankLine(self):
		self._testSX( '(blankLine)', '' )
		
		
	def test_UNPARSED(self):
		self.assertRaises( CodeGeneratorUnparsedException, lambda: self._testSX( '(UNPARSED Test)', '' ) )
		
		
	def test_stringLiteral(self):
		self._testSX( '(stringLiteral ascii single "Hi there")', '\'Hi there\'' )
		
		
	def test_intLiteral(self):
		self._testSX( '(intLiteral decimal int 123)', '123' )
		self._testSX( '(intLiteral hex int 1a4)', '0x1a4' )
		self._testSX( '(intLiteral decimal long 123)', '123L' )
		self._testSX( '(intLiteral hex long 1a4)', '0x1a4L' )
		
		
	def test_floatLiteral(self):
		self._testSX( '(floatLiteral 123.0)', '123.0' )
		
		
	def test_imaginaryLiteral(self):
		self._testSX( '(imaginaryLiteral 123j)', '123j' )
		
		
	def test_singleTarget(self):
		self._testSX( '(singleTarget a)', 'a' )
		
		
	def test_tupleTarget(self):
		self._testSX( '(tupleTarget (singleTarget a) (singleTarget b) (singleTarget c))', '( a, b, c, )' )
		
		
	def test_listTarget(self):
		self._testSX( '(listTarget (singleTarget a) (singleTarget b) (singleTarget c))', '[ a, b, c ]' )
		
		
	def test_var(self):
		self._testSX( '(var a)', 'a' )
		
		
	def test_tupleLiteral(self):
		self._testSX( '(tupleLiteral (var a) (var b) (var c))', '( a, b, c, )' )
		
		
	def test_listLiteral(self):
		self._testSX( '(listLiteral (var a) (var b) (var c))', '[ a, b, c ]' )
		
		
	def test_listFor(self):
		self._testSX( '(listFor (singleTarget x) (var xs))', 'for x in xs' )
		
		
	def test_listIf(self):
		self._testSX( '(listIf (var a))', 'if a' )
		
		
	def test_listComprehension(self):
		self._testSX( '(listComprehension (var a) (listFor (singleTarget a) (var xs)) (listIf (var a)))', '[ a   for a in xs   if a ]' )
		
		
	def test_genFor(self):
		self._testSX( '(genFor (singleTarget x) (var xs))', 'for x in xs' )
		
		
	def test_genIf(self):
		self._testSX( '(genIf (var a))', 'if a' )
		
		
	def test_generatorExpression(self):
		self._testSX( '(generatorExpression (var a) (genFor (singleTarget a) (var xs)) (genIf (var a)))', '( a   for a in xs   if a )' )
		
		
	def test_keyValuePair(self):
		self._testSX( '(keyValuePair (var a) (var b))', 'a:b' )
		
		
	def test_dictLiteral(self):
		self._testSX( '(dictLiteral (keyValuePair (var a) (var b)) (keyValuePair (var c) (var d)))', '{ a:b, c:d }' )
		
	
	def test_yieldExpr(self):
		self._testSX( '(yieldExpr (var a))', 'yield a' )
		
		
	def test_yieldAtom(self):
		self._testSX( '(yieldAtom (var a))', '(yield a)' )
		
		
	def test_attributeRef(self):
		self._testSX( '(attributeRef (var a) b)', 'a.b' )
		
		
	def test_subscript(self):
		self._testSX( '(subscript (var a) (var b))', 'a[b]' )
		
		
	def test_subscript_ellipsis(self):
		self._testSX( '(subscript (var a) (ellipsis))', 'a[...]' )
		
		
	def test_subscript_slice(self):
		self._testSX( '(subscript (var a) (subscriptSlice (var a) (var b)))', 'a[a:b]' )
		self._testSX( '(subscript (var a) (subscriptSlice (var a) <nil>))', 'a[a:]' )
		self._testSX( '(subscript (var a) (subscriptSlice <nil> (var b)))', 'a[:b]' )
		self._testSX( '(subscript (var a) (subscriptSlice <nil> <nil>))', 'a[:]' )
		

	def test_subscript_longSlice(self):
		self._testSX( '(subscript (var a) (subscriptLongSlice (var a) (var b) (var c)))', 'a[a:b:c]' )
		self._testSX( '(subscript (var a) (subscriptLongSlice (var a) (var b) <nil>))', 'a[a:b:]' )
		self._testSX( '(subscript (var a) (subscriptLongSlice (var a) <nil> (var c)))', 'a[a::c]' )
		self._testSX( '(subscript (var a) (subscriptLongSlice (var a) <nil> <nil>))', 'a[a::]' )
		self._testSX( '(subscript (var a) (subscriptLongSlice <nil> (var b) (var c)))', 'a[:b:c]' )
		self._testSX( '(subscript (var a) (subscriptLongSlice <nil> (var b) <nil>))', 'a[:b:]' )
		self._testSX( '(subscript (var a) (subscriptLongSlice <nil> <nil> (var c)))', 'a[::c]' )
		self._testSX( '(subscript (var a) (subscriptLongSlice <nil> <nil> <nil>))', 'a[::]' )

		
	def test_subscript_tuple(self):
		self._testSX( '(subscript (var a) (subscriptTuple (var a) (var b)))', 'a[(a,b,)]' )
		
		
	def test_call(self):
		self._testSX( '(call (var x) (var a) (var b) (kwArg c (var d)) (kwArg e (var f)) (argList (var g)) (kwArgList (var h)))', 'x( a, b, c=d, e=f, *g, **h )' )
		
		
	def test_operators(self):
		self._binOpTest( 'pow', '**' )
		self._testSX( '(invert (var a))', '( ~a )' )
		self._testSX( '(negate (var a))', '( -a )' )
		self._testSX( '(pos (var a))', '( +a )' )
		self._binOpTest( 'mul', '*' )
		self._binOpTest( 'div', '/' )
		self._binOpTest( 'mod', '%' )
		self._binOpTest( 'add', '+' )
		self._binOpTest( 'sub', '-' )
		self._binOpTest( 'lshift', '<<' )
		self._binOpTest( 'rshift', '>>' )
		self._binOpTest( 'bitAnd', '&' )
		self._binOpTest( 'bitXor', '^' )
		self._binOpTest( 'bitOr', '|' )
		self._binOpTest( 'lte', '<=' )
		self._binOpTest( 'lt', '<' )
		self._binOpTest( 'gte', '>=' )
		self._binOpTest( 'gt', '>' )
		self._binOpTest( 'eq', '==' )
		self._binOpTest( 'neq', '!=' )
		self._binOpTest( 'isTest', 'is' )
		self._binOpTest( 'isNotTest', 'is not' )
		self._binOpTest( 'inTest', 'in' )
		self._binOpTest( 'notInTest', 'not in' )
		self._testSX( '(notTest (var a))', '(not a)' )
		self._binOpTest( 'andTest', 'and' )
		self._binOpTest( 'orTest', 'or' )
		
		
	def test_lambdaExpr(self):
		self._testSX( '(lambdaExpr ((simpleParam a) (simpleParam b) (defaultValueParam c (var d)) (defaultValueParam e (var f)) (paramList g) (kwParamList h)) (var a))', 'lambda a, b, c=d, e=f, *g, **h: a' )
	
		
	def test_conditionalExpr(self):
		self._testSX( '(conditionalExpr (var b) (var a) (var c))', 'a   if b   else c' )
		
		
		
	def test_assertStmt(self):
		self._testSX( '(assertStmt (var x) <nil>)', 'assert x' )
		self._testSX( '(assertStmt (var x) (var y))', 'assert x, y' )
		
		
	def test_assignmentStmt(self):
		self._testSX( '(assignmentStmt ((singleTarget x)) (var a))', 'x = a' )
		self._testSX( '(assignmentStmt ((singleTarget x) (singleTarget y)) (var a))', 'x = y = a' )
		
		
	def test_augAssignStmt(self):
		self._testSX( '(augAssignStmt += (singleTarget x) (var a))', 'x += a' )
		
		
	def test_passStmt(self):
		self._testSX( '(passStmt)', 'pass' )
		
		
	def test_delStmt(self):
		self._testSX( '(delStmt (singleTarget a))', 'del a' )
		
		
	def test_returnStmt(self):
		self._testSX( '(returnStmt (var a))', 'return a' )
		
		
	def test_yieldStmt(self):
		self._testSX( '(yieldStmt (var a))', 'yield a' )
		
		
	def test_raiseStmt(self):
		self._testSX( '(raiseStmt <nil> <nil> <nil>)', 'raise' )
		self._testSX( '(raiseStmt (var a) <nil> <nil>)', 'raise a' )
		self._testSX( '(raiseStmt (var a) (var b) <nil>)', 'raise a, b' )
		self._testSX( '(raiseStmt (var a) (var b) (var c))', 'raise a, b, c' )
		
		
	def test_breakStmt(self):
		self._testSX( '(breakStmt)', 'break' )
		
		
	def test_continueStmt(self):
		self._testSX( '(continueStmt)', 'continue' )
		
		
	def test_importStmt(self):
		self._testSX( '(importStmt (moduleImport a))', 'import a' )
		self._testSX( '(importStmt (moduleImport a.b))', 'import a.b' )
		self._testSX( '(importStmt (moduleImportAs a x))', 'import a as x' )
		self._testSX( '(importStmt (moduleImportAs a.b x))', 'import a.b as x' )
		
		
	def test_fromImportStmt(self):
		self._testSX( '(fromImportStmt (relativeModule x) (moduleContentImport a))', 'from x import a' )
		self._testSX( '(fromImportStmt (relativeModule x) (moduleContentImportAs a p))', 'from x import a as p' )
		self._testSX( '(fromImportStmt (relativeModule x) (moduleContentImportAs a p) (moduleContentImportAs b q))', 'from x import a as p, b as q' )
		
		
	def test_fromImportAllStmt(self):
		self._testSX( '(fromImportAllStmt (relativeModule x))', 'from x import *' )
		
		
	def test_globalStmt(self):
		self._testSX( '(globalStmt (globalVar a))', 'global a' )
		self._testSX( '(globalStmt (globalVar a) (globalVar b))', 'global a, b' )
		
		
	def test_execStmt(self):
		self._testSX( '(execStmt (var a) <nil> <nil>)', 'exec a' )
		self._testSX( '(execStmt (var a) (var b) <nil>)', 'exec a in b' )
		self._testSX( '(execStmt (var a) (var b) (var c))', 'exec a in b, c' )
		
		
		
	def test_ifStmt(self):
		self._testSX( '(ifStmt (var bA) ((var b)))', 'if bA:\n\tb\n' )


	def test_elifStmt(self):
		self._testSX( '(elifStmt (var bA) ((var b)))', 'elif bA:\n\tb\n' )


	def test_elseStmt(self):
		self._testSX( '(elseStmt ((var b)))', 'else:\n\tb\n' )


	def test_whileStmt(self):
		self._testSX( '(whileStmt (var bA) ((var b)))', 'while bA:\n\tb\n' )


	def test_forStmt(self):
		self._testSX( '(forStmt (var a) (var b) ((var c)))', 'for a in b:\n\tc\n' )


	def test_tryStmt(self):
		self._testSX( '(tryStmt ((var b)))', 'try:\n\tb\n' )


	def test_exceptStmt(self):
		self._testSX( '(exceptStmt <nil> <nil> ((var b)))', 'except:\n\tb\n' )
		self._testSX( '(exceptStmt (var a) <nil> ((var b)))', 'except a:\n\tb\n' )
		self._testSX( '(exceptStmt (var a) (var x) ((var b)))', 'except a, x:\n\tb\n' )


	def test_finallyStmt(self):
		self._testSX( '(finallyStmt ((var b)))', 'finally:\n\tb\n' )


	def test_withStmt(self):
		self._testSX( '(withStmt (var a) <nil> ((var b)))', 'with a:\n\tb\n' )
		self._testSX( '(withStmt (var a) (var x) ((var b)))', 'with a as x:\n\tb\n' )


	def test_defStmt(self):
		self._testSX( '(defStmt myFunc ((simpleParam a) (defaultValueParam b (var c)) (paramList d) (kwParamList e)) ((var b)))', 'def myFunc(a, b=c, *d, **e):\n\tb\n' )


	def test_decoStmt(self):
		self._testSX( '(decoStmt myDeco <nil>)', '@myDeco' )
		self._testSX( '(decoStmt myDeco ((var a) (var b)))', '@myDeco( a, b )' )

		
	def test_classStmt(self):
		self._testSX( '(classStmt A <nil> ((var b)))', 'class A:\n\tb\n' )
		self._testSX( '(classStmt A ((var object)) ((var b)))', 'class A (object):\n\tb\n' )
		self._testSX( '(classStmt A ((var object) (var Q)) ((var b)))', 'class A (object, Q):\n\tb\n' )



	def test_commentStmt(self):
		self._testSX( '(commentStmt HelloWorld)', '#HelloWorld' )
		
		
		
		
