##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.gSymCodeGenerator import GSymCodeGeneratorObjectNodeDispatch
from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeDispatchMethod

from Britefury.Util.NodeUtil import isNullNode

import GSymCore.Languages.Python25.NodeClasses as Nodes


def _indent(x):
	lines = x.split( '\n' )
	lines = [ '\t' + l   for l in lines ]
	if lines[-1] == '\t':
		lines[-1] = ''
	return '\n'.join( lines )


class CodeGeneratorUnparsedException (Exception):
	pass



class Python25CodeGenerator (GSymCodeGeneratorObjectNodeDispatch):
	__dispatch_module__ = Nodes.module
	__dispatch_num_args__ = 0
	
	
	
	# Misc
	@ObjectNodeDispatchMethod
	def BlankLine(self, node):
		return ''
	

	@ObjectNodeDispatchMethod
	def UNPARSED(self, node, value):
		raise CodeGeneratorUnparsedException
	
	
	# String literal
	@ObjectNodeDispatchMethod
	def StringLiteral(self, node, format, quotation, value):
		return repr( value.toString() )
	
	
	
	# Integer literal
	@ObjectNodeDispatchMethod
	def IntLiteral(self, node, format, numType, value):
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
	@ObjectNodeDispatchMethod
	def FloatLiteral(self, node, value):
		return value.toString()

	
	
	# Imaginary literal
	@ObjectNodeDispatchMethod
	def ImaginaryLiteral(self, node, value):
		return value.toString()
	
	
	
	# Target
	@ObjectNodeDispatchMethod
	def SingleTarget(self, node, name):
		return name.toString()
	
	@ObjectNodeDispatchMethod
	def TupleTarget(self, node, targets):
		return '( '  +  ', '.join( [ self( i )   for i in targets ] )  +  ', )'
	
	@ObjectNodeDispatchMethod
	def ListTarget(self, node, targets):
		return '[ '  +  ', '.join( [ self( i )   for i in targets ] )  +  ' ]'
	
	

	# Variable reference
	@ObjectNodeDispatchMethod
	def Load(self, node, name):
		return str( name )

	
	
	# Tuple literal	
	@ObjectNodeDispatchMethod
	def TupleLiteral(self, node, values):
		return '( '  +  ', '.join( [ self( i )   for i in values ] )  +  ', )'

	
	
	# List literal
	@ObjectNodeDispatchMethod
	def ListLiteral(self, node, values):
		return '[ '  +  ', '.join( [ self( i )   for i in values ] )  +  ' ]'
	
	
	
	# List comprehension / generator expression
	@ObjectNodeDispatchMethod
	def ComprehensionFor(self, node, target, source):
		return 'for ' + self( target ) + ' in ' + self( source )
	
	@ObjectNodeDispatchMethod
	def ComprehensionIf(self, node, condition):
		return 'if ' + self( condition )
	
	@ObjectNodeDispatchMethod
	def ListComp(self, node, resultExpr, comprehensionItems):
		return '[ ' + self( resultExpr ) + '   ' + '   '.join( [ self( x )   for x in comprehensionItems ] )  +  ' ]'
	
	@ObjectNodeDispatchMethod
	def GeneratorExpr(self, node, resultExpr, comprehensionItems):
		return '( ' + self( resultExpr ) + '   ' + '   '.join( [ self( c )   for c in comprehensionItems ] )  +  ' )'
	
	
	
	# Dictionary literal
	@ObjectNodeDispatchMethod
	def DictKeyValuePair(self, node, key, value):
		return self( key ) + ':' + self( value )
	
	@ObjectNodeDispatchMethod
	def DictLiteral(self, node, values):
		return '{ '  +  ', '.join( [ self( i )   for i in values ] )  +  ' }'
	
	
	
	# Yield expression and yield atom
	@ObjectNodeDispatchMethod
	def YieldAtom(self, node, value):
		return '(yield ' + self( value ) + ')'
		
	
	
	# Attribute ref
	@ObjectNodeDispatchMethod
	def AttributeRef(self, node, target, name):
		return self( target ) + '.' + name

	

	# Subscript
	@ObjectNodeDispatchMethod
	def SubscriptSlice(self, node, lower, upper):
		txt = lambda x:  self( x )   if not isNullNode( x )   else ''
		return txt( lower ) + ':' + txt( upper )

	@ObjectNodeDispatchMethod
	def SubscriptLongSlice(self, node, lower, upper, stride):
		txt = lambda x:  self( x )   if not isNullNode( x )   else ''
		return txt( lower ) + ':' + txt( upper ) + ':' + txt( stride )
	
	@ObjectNodeDispatchMethod
	def SubscriptEllipsis(self, node):
		return '...'

	@ObjectNodeDispatchMethod
	def SubscriptTuple(self, node, values):
		return '('  +  ','.join( [ self( i )   for i in values ] )  +  ',)'

	@ObjectNodeDispatchMethod
	def Subscript(self, node, target, index):
		return self( target ) + '[' + self( index ) + ']'
	

	
	# Call	
	@ObjectNodeDispatchMethod
	def CallKWArg(self, node, name, value):
		return name.toString() + '=' + self( value )
	
	@ObjectNodeDispatchMethod
	def CallArgList(self, node, value):
		return '*' + self( value )
	
	@ObjectNodeDispatchMethod
	def CallKWArgList(self, node, value):
		return '**' + self( value )
	
	@ObjectNodeDispatchMethod
	def Call(self, node, target, args):
		return self( target ) + '( ' + ', '.join( [ self( a )   for a in args ] ) + ' )'
	
	
	
	# Operators
	@ObjectNodeDispatchMethod
	def Pow(self, node, x, y):
		return '( '  +  self( x )  +  ' ** '  +  self( y )  +  ' )'
	
	
	@ObjectNodeDispatchMethod
	def Invert(self, node, x):
		return '( ~'  +  self( x )  +  ' )'
	
	@ObjectNodeDispatchMethod
	def Negate(self, node, x):
		return '( -'  +  self( x )  +  ' )'
	
	@ObjectNodeDispatchMethod
	def Pos(self, node, x):
		return '( +'  +  self( x )  +  ' )'
	
	
	@ObjectNodeDispatchMethod
	def Mul(self, node, x, y):
		return '( '  +  self( x )  +  ' * '  +  self( y )  +  ' )'
	
	@ObjectNodeDispatchMethod
	def Div(self, node, x, y):
		return '( '  +  self( x )  +  ' / '  +  self( y )  +  ' )'
	
	@ObjectNodeDispatchMethod
	def Mod(self, node, x, y):
		return '( '  +  self( x )  +  ' % '  +  self( y )  +  ' )'
	
	@ObjectNodeDispatchMethod
	def Add(self, node, x, y):
		return '( '  +  self( x )  +  ' + '  +  self( y )  +  ' )'
	
	@ObjectNodeDispatchMethod
	def Sub(self, node, x, y):
		return '( '  +  self( x )  +  ' - '  +  self( y )  +  ' )'
	
	
	@ObjectNodeDispatchMethod
	def LShift(self, node, x, y):
		return '( '  +  self( x )  +  ' << '  +  self( y )  +  ' )'
	
	@ObjectNodeDispatchMethod
	def RShift(self, node, x, y):
		return '( '  +  self( x )  +  ' >> '  +  self( y )  +  ' )'
	
	
	@ObjectNodeDispatchMethod
	def BitAnd(self, node, x, y):
		return '( '  +  self( x )  +  ' & '  +  self( y )  +  ' )'
	
	@ObjectNodeDispatchMethod
	def BitXor(self, node, x, y):
		return '( '  +  self( x )  +  ' ^ '  +  self( y )  +  ' )'
	
	@ObjectNodeDispatchMethod
	def BitOr(self, node, x, y):
		return '( '  +  self( x )  +  ' | '  +  self( y )  +  ' )'
	

	@ObjectNodeDispatchMethod
	def Cmp(self, node, x, ops):
		return '( ' + self( x )  +  ''.join( [ self( op )   for op in ops ] ) + ' )'
	
	@ObjectNodeDispatchMethod
	def CmpOpLte(self, node, y):
		return ' <= ' + self( y )
	
	@ObjectNodeDispatchMethod
	def CmpOpLt(self, node, y):
		return ' < ' + self( y )
	
	@ObjectNodeDispatchMethod
	def CmpOpGte(self, node, y):
		return ' >= ' + self( y )
	
	@ObjectNodeDispatchMethod
	def CmpOpGt(self, node, y):
		return ' > ' + self( y )
	
	@ObjectNodeDispatchMethod
	def CmpOpEq(self, node, y):
		return ' == ' + self( y )
	
	@ObjectNodeDispatchMethod
	def CmpOpNeq(self, node, y):
		return ' != ' + self( y )
	
	@ObjectNodeDispatchMethod
	def CmpOpIsNot(self, node, y):
		return ' is not ' + self( y )
	
	@ObjectNodeDispatchMethod
	def CmpOpIs(self, node, y):
		return ' is ' + self( y )
	
	@ObjectNodeDispatchMethod
	def CmpOpNotIn(self, node, y):
		return ' not in ' + self( y )
	
	@ObjectNodeDispatchMethod
	def CmpOpIn(self, node, y):
		return ' in ' + self( y )
	
	

	
	@ObjectNodeDispatchMethod
	def NotTest(self, node, x):
		return '(not '  +  self( x )  +  ')'
	
	@ObjectNodeDispatchMethod
	def AndTest(self, node, x, y):
		return '( '  +  self( x )  +  ' and '  +  self( y )  +  ' )'
	
	@ObjectNodeDispatchMethod
	def OrTest(self, node, x, y):
		return '( '  +  self( x )  +  ' or '  +  self( y )  +  ' )'
	
	
	
	
	# Parameters	
	@ObjectNodeDispatchMethod
	def SimpleParam(self, node, name):
		return name.toString()
	
	@ObjectNodeDispatchMethod
	def DefaultValueParam(self, node, name, defaultValue):
		return name.toString()  +  '='  +  self( defaultValue )
	
	@ObjectNodeDispatchMethod
	def ParamList(self, node, name):
		return '*'  +  name.toString()
	
	@ObjectNodeDispatchMethod
	def KWParamList(self, node, name):
		return '**'  +  name.toString()
	
	
	
	# Lambda expression
	@ObjectNodeDispatchMethod
	def LambdaExpr(self, node, params, expr):
		return '( lambda '  +  ', '.join( [ self( p )   for p in params ] )  +  ': '  +  self( expr ) + ' )'
	
	
	
	# Conditional expression
	@ObjectNodeDispatchMethod
	def ConditionalExpr(self, node, condition, expr, elseExpr):
		return self( expr )  +  '   if '  +  self( condition )  +  '   else '  +  self( elseExpr )

	
	
	# Assert statement
	@ObjectNodeDispatchMethod
	def AssertStmt(self, node, condition, fail):
		return 'assert '  +  self( condition )  +  ( ', ' + self( fail )   if fail != '<nil>'   else  '' )
	
	
	# Assignment statement
	@ObjectNodeDispatchMethod
	def AssignStmt(self, node, targets, value):
		return ''.join( [ self( t ) + ' = '   for t in targets ] )  +  self( value )
	
	
	# Augmented assignment statement
	@ObjectNodeDispatchMethod
	def AugAssignStmt(self, node, op, target, value):
		return self( target )  +  ' '  +  op.toString()  +  ' '  +  self( value )
	
	
	# Pass statement
	@ObjectNodeDispatchMethod
	def PassStmt(self, node):
		return 'pass'
	
	
	# Del statement
	@ObjectNodeDispatchMethod
	def DelStmt(self, node, target):
		return 'del '  +  self( target )
	
	
	# Return statement
	@ObjectNodeDispatchMethod
	def ReturnStmt(self, node, value):
		return 'return '  +  self( value )
	
	
	# Yield statement
	@ObjectNodeDispatchMethod
	def YieldStmt(self, node, value):
		return 'yield '  +  self( value )
	
	
	# Raise statement
	@ObjectNodeDispatchMethod
	def RaiseStmt(self, node, excType, excValue, traceback):
		params = ', '.join( [ self( x )   for x in excType, excValue, traceback   if not isNullNode( x ) ] )
		if params != '':
			return 'raise ' + params
		else:
			return 'raise'
	
	
	# Break statement
	@ObjectNodeDispatchMethod
	def BreakStmt(self, node):
		return 'break'
	
	
	# Continue statement
	@ObjectNodeDispatchMethod
	def ContinueStmt(self, node):
		return 'continue'
	
	
	# Import statement
	@ObjectNodeDispatchMethod
	def RelativeModule(self, node, name):
		return name.toString()
	
	@ObjectNodeDispatchMethod
	def ModuleImport(self, node, name):
		return name.toString()
	
	@ObjectNodeDispatchMethod
	def ModuleImportAs(self, node, name, asName):
		return name.toString() + ' as ' + asName.toString()
	
	@ObjectNodeDispatchMethod
	def ModuleContentImport(self, node, name):
		return name.toString()
	
	@ObjectNodeDispatchMethod
	def ModuleContentImportAs(self, node, name, asName):
		return name.toString() + ' as ' + asName.toString()
	
	@ObjectNodeDispatchMethod
	def ImportStmt(self, node, modules):
		return 'import '  +  ', '.join( [ self( x )   for x in modules ] )
	
	@ObjectNodeDispatchMethod
	def FromImportStmt(self, node, module, imports):
		return 'from ' + self( module ) + ' import ' + ', '.join( [ self( x )   for x in imports ] )
	
	@ObjectNodeDispatchMethod
	def FromImportAllStmt(self, node, module):
		return 'from ' + self( module ) + ' import *'
	
	
	# Global statement
	@ObjectNodeDispatchMethod
	def GlobalVar(self, node, name):
		return name
	
	@ObjectNodeDispatchMethod
	def GlobalStmt(self, node, vars):
		return 'global '  +  ', '.join( [ self( x )   for x in vars ] )
	
	
	# Exec statement
	@ObjectNodeDispatchMethod
	def ExecStmt(self, node, source, locals, globals):
		txt = 'exec '  +  self( source )
		if locals != '<nil>':
			txt += ' in '  +  self( locals )
		if globals != '<nil>':
			txt += ', '  +  self( globals )
		return txt
	
	
	# If statement
	@ObjectNodeDispatchMethod
	def IfStmt(self, node, condition, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'if '  +  self( condition ) + ':\n'  +  _indent( suiteText )
	

	# Elif statement
	@ObjectNodeDispatchMethod
	def ElifStmt(self, node, condition, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'elif '  +  self( condition ) + ':\n'  +  _indent( suiteText )
	

	# Else statement
	@ObjectNodeDispatchMethod
	def ElseStmt(self, node, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'else:\n'  +  _indent( suiteText )
	

	# While statement
	@ObjectNodeDispatchMethod
	def WhileStmt(self, node, condition, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'while '  +  self( condition ) + ':\n'  +  _indent( suiteText )
	

	# For statement
	@ObjectNodeDispatchMethod
	def ForStmt(self, node, target, source, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'for '  +  self( target )  +  ' in '  +  self( source )  +  ':\n'  +  _indent( suiteText )
	

	# Try statement
	@ObjectNodeDispatchMethod
	def TryStmt(self, node, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'try:\n'  +  _indent( suiteText )
	

	# Except statement
	@ObjectNodeDispatchMethod
	def ExceptStmt(self, node, exception, target, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		txt = 'except'
		if exception != '<nil>':
			txt += ' ' + self( exception )
		if target != '<nil>':
			txt += ', ' + self( target )
		return txt + ':\n'  +  _indent( suiteText )
	

	# Finally statement
	@ObjectNodeDispatchMethod
	def FinallyStmt(self, node, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'finally:\n'  +  _indent( suiteText )
	

	# With statement
	@ObjectNodeDispatchMethod
	def WithStmt(self, node, expr, target, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'with '  +  self( expr )  +  ( ' as ' + self( target )   if target != '<nil>'   else   '' )  +  ':\n'  +  _indent( suiteText )
	
	
	# Def statement
	@ObjectNodeDispatchMethod
	def DefStmt(self, node, name, params, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'def '  +  name.toString()  +  '('  +  ', '.join( [ self( p )   for p in params ] )  +  '):\n'  +  _indent( suiteText )
	

	# Deco statement
	@ObjectNodeDispatchMethod
	def DecoStmt(self, node, name, args):
		text = '@' + name.toString()
		if not isNullNode( args ):
			text += '( ' + ', '.join( [ self( a )   for a in args ] ) + ' )'
		return text
	

	# Class statement
	@ObjectNodeDispatchMethod
	def ClassStmt(self, node, name, bases, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		text = 'class '  +  name
		if not isNullNode( bases ):
			text += ' ('  +  ', '.join( [ self( h )   for h in bases ] )  +  ')'
		return text  +  ':\n'  +  _indent( suiteText )
	
	
	
	# Comment statement
	@ObjectNodeDispatchMethod
	def CommentStmt(self, node, comment):
		return '#' + comment

	
	# Module
	@ObjectNodeDispatchMethod
	def PythonModule(self, node, suite):
		return '\n'.join( [ self( line )   for line in suite ] )

	

	
	
import unittest
from BritefuryJ.DocModel import DMIOReader, DMModuleResolver

class TestCase_Python25CodeGenerator (unittest.TestCase):
	class _Resolver (DMModuleResolver):
		def getModule(self, location):
			return Nodes.module
		
	_resolver = _Resolver()
	
		
	def _testSX(self, sx, expected):
		sx = '{ py=org.Britefury.gSym.Languages.Python25 : ' + sx + ' }'
		data = DMIOReader.readFromString( sx, self._resolver )
		
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
		self._testSX( '(py %s x=(py Load name=a) y=(py Load name=b))'  %  sxOp,  '( a %s b )'  %  expectedOp )
		
		
	def test_BlankLine(self):
		self._testSX( '(py BlankLine)', '' )
		
		
	def test_UNPARSED(self):
		self.assertRaises( CodeGeneratorUnparsedException, lambda: self._testSX( '(py UNPARSED value=Test)', '' ) )
		
		
	def test_StringLiteral(self):
		self._testSX( '(py StringLiteral format=ascii quotation=single value="Hi there")', '\'Hi there\'' )
		
		
	def test_IntLiteral(self):
		self._testSX( '(py IntLiteral format=decimal numType=int value=123)', '123' )
		self._testSX( '(py IntLiteral format=hex numType=int value=1a4)', '0x1a4' )
		self._testSX( '(py IntLiteral format=decimal numType=long value=123)', '123L' )
		self._testSX( '(py IntLiteral format=hex numType=long value=1a4)', '0x1a4L' )
		
		
	def test_FloatLiteral(self):
		self._testSX( '(py FloatLiteral value=123.0)', '123.0' )
		
		
	def test_ImaginaryLiteral(self):
		self._testSX( '(py ImaginaryLiteral value=123j)', '123j' )
		
		
	def test_SingleTarget(self):
		self._testSX( '(py SingleTarget name=a)', 'a' )
		
		
	def test_TupleTarget(self):
		self._testSX( '(py TupleTarget targets=[(py SingleTarget name=a) (py SingleTarget name=b) (py SingleTarget name=c)])', '( a, b, c, )' )
		
		
	def test_ListTarget(self):
		self._testSX( '(py ListTarget targets=[(py SingleTarget name=a) (py SingleTarget name=b) (py SingleTarget name=c)])', '[ a, b, c ]' )
		
		
	def test_Load(self):
		self._testSX( '(py Load name=a)', 'a' )
		
		
	def test_TupleLiteral(self):
		self._testSX( '(py TupleLiteral values=[(py Load name=a) (py Load name=b) (py Load name=c)])', '( a, b, c, )' )
		
		
	def test_ListLiteral(self):
		self._testSX( '(py ListLiteral values=[(py Load name=a) (py Load name=b) (py Load name=c)])', '[ a, b, c ]' )
		
		
	def test_ComprehensionFor(self):
		self._testSX( '(py ComprehensionFor target=(py SingleTarget name=x) source=(py Load name=xs))', 'for x in xs' )
		
		
	def test_ComprehensionIf(self):
		self._testSX( '(py ComprehensionIf condition=(py Load name=a))', 'if a' )
		
		
	def test_ListComp(self):
		self._testSX( '(py ListComp resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs)) (py ComprehensionIf condition=(py Load name=a))])', '[ a   for a in xs   if a ]' )
		
		
	def test_GeneratorExpr(self):
		self._testSX( '(py GeneratorExpr resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs)) (py ComprehensionIf condition=(py Load name=a))])', '( a   for a in xs   if a )' )
		
		
	def test_DictKeyValuePair(self):
		self._testSX( '(py DictKeyValuePair key=(py Load name=a) value=(py Load name=b))', 'a:b' )
		
		
	def test_DictLiteral(self):
		self._testSX( '(py DictLiteral values=[(py DictKeyValuePair key=(py Load name=a) value=(py Load name=b)) (py DictKeyValuePair key=(py Load name=c) value=(py Load name=d))])', '{ a:b, c:d }' )
		
	
	def test_YieldAtom(self):
		self._testSX( '(py YieldAtom value=(py Load name=a))', '(yield a)' )
		
		
	def test_AttributeRef(self):
		self._testSX( '(py AttributeRef target=(py Load name=a) name=b)', 'a.b' )
		
		
	def test_Subscript(self):
		self._testSX( '(py Subscript target=(py Load name=a) index=(py Load name=b))', 'a[b]' )
		
		
	def test_Subscript_Ellipsis(self):
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptEllipsis))', 'a[...]' )
		
		
	def test_subscript_slice(self):
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptSlice lower=(py Load name=a) upper=(py Load name=b)))', 'a[a:b]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptSlice lower=(py Load name=a) upper=<nil>))', 'a[a:]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptSlice lower=<nil> upper=(py Load name=b)))', 'a[:b]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptSlice lower=<nil> upper=<nil>))', 'a[:]' )
		

	def test_subscript_longSlice(self):
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=(py Load name=a) upper=(py Load name=b) stride=(py Load name=c)))', 'a[a:b:c]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=(py Load name=a) upper=(py Load name=b) stride=<nil>))', 'a[a:b:]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=(py Load name=a) upper=<nil> stride=(py Load name=c)))', 'a[a::c]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=(py Load name=a) upper=<nil> stride=<nil>))', 'a[a::]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=<nil> upper=(py Load name=b) stride=(py Load name=c)))', 'a[:b:c]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=<nil> upper=(py Load name=b) stride=<nil>))', 'a[:b:]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=<nil> upper=<nil> stride=(py Load name=c)))', 'a[::c]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=<nil> upper=<nil> stride=<nil>))', 'a[::]' )

		
	def test_subscript_tuple(self):
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptTuple values=[(py Load name=a) (py Load name=b)]))', 'a[(a,b,)]' )
		
		
	def test_call(self):
		self._testSX( '(py Call target=(py Load name=x) args=[(py Load name=a) (py Load name=b) (py CallKWArg name=c value=(py Load name=d)) (py CallKWArg name=e value=(py Load name=f)) (py CallArgList value=(py Load name=g)) (py CallKWArgList value=(py Load name=h))])', 'x( a, b, c=d, e=f, *g, **h )' )
		
		
	def test_operators(self):
		self._binOpTest( 'Pow', '**' )
		self._testSX( '(py Invert x=(py Load name=a))', '( ~a )' )
		self._testSX( '(py Negate x=(py Load name=a))', '( -a )' )
		self._testSX( '(py Pos x=(py Load name=a))', '( +a )' )
		self._binOpTest( 'Mul', '*' )
		self._binOpTest( 'Div', '/' )
		self._binOpTest( 'Mod', '%' )
		self._binOpTest( 'Add', '+' )
		self._binOpTest( 'Sub', '-' )
		self._binOpTest( 'LShift', '<<' )
		self._binOpTest( 'RShift', '>>' )
		self._binOpTest( 'BitAnd', '&' )
		self._binOpTest( 'BitXor', '^' )
		self._binOpTest( 'BitOr', '|' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpLte y=(py Load name=b))])',  '( a <= b )' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpLt y=(py Load name=b))])',  '( a < b )' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpGte y=(py Load name=b))])',  '( a >= b )' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpGt y=(py Load name=b))])',  '( a > b )' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpEq y=(py Load name=b))])',  '( a == b )' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpNeq y=(py Load name=b))])',  '( a != b )' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpIsNot y=(py Load name=b))])',  '( a is not b )' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpIs y=(py Load name=b))])',  '( a is b )' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpNotIn y=(py Load name=b))])',  '( a not in b )' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpIn y=(py Load name=b))])',  '( a in b )' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpLt y=(py Load name=b)) (py CmpOpGt y=(py Load name=c))])',  '( a < b > c )' )
		self._testSX( '(py NotTest x=(py Load name=a))', '(not a)' )
		self._binOpTest( 'AndTest', 'and' )
		self._binOpTest( 'OrTest', 'or' )
		
		
	def test_LambdaExpr(self):
		self._testSX( '(py LambdaExpr params=[(py SimpleParam name=a) (py SimpleParam name=b) (py DefaultValueParam name=c defaultValue=(py Load name=d)) (py DefaultValueParam name=e defaultValue=(py Load name=f)) (py ParamList name=g) (py KWParamList name=h)] expr=(py Load name=a))', '( lambda a, b, c=d, e=f, *g, **h: a )' )
	
		
	def test_ConditionalExpr(self):
		self._testSX( '(py ConditionalExpr condition=(py Load name=b) expr=(py Load name=a) elseExpr=(py Load name=c))', 'a   if b   else c' )
		
		
		
	def test_assertStmt(self):
		self._testSX( '(py AssertStmt condition=(py Load name=x) fail=<nil>)', 'assert x' )
		self._testSX( '(py AssertStmt condition=(py Load name=x) fail=(py Load name=y))', 'assert x, y' )
		
		
	def test_AssignStmt(self):
		self._testSX( '(py AssignStmt targets=[(py SingleTarget name=x)] value=(py Load name=a))', 'x = a' )
		self._testSX( '(py AssignStmt targets=[(py SingleTarget name=x) (py SingleTarget name=y)] value=(py Load name=a))', 'x = y = a' )
		
		
	def test_AugAssignStmt(self):
		self._testSX( '(py AugAssignStmt op="+=" target=(py SingleTarget name=x) value=(py Load name=a))', 'x += a' )
		
		
	def test_PassStmt(self):
		self._testSX( '(py PassStmt)', 'pass' )
		
		
	def test_DelStmt(self):
		self._testSX( '(py DelStmt target=(py SingleTarget name=a))', 'del a' )
		
		
	def test_ReturnStmt(self):
		self._testSX( '(py ReturnStmt value=(py Load name=a))', 'return a' )
		
		
	def test_YieldStmt(self):
		self._testSX( '(py YieldStmt value=(py Load name=a))', 'yield a' )
		
		
	def test_raiseStmt(self):
		self._testSX( '(py RaiseStmt excType=<nil> excValue=<nil> traceback=<nil>)', 'raise' )
		self._testSX( '(py RaiseStmt excType=(py Load name=a) excValue=<nil> traceback=<nil>)', 'raise a' )
		self._testSX( '(py RaiseStmt excType=(py Load name=a) excValue=(py Load name=b) traceback=<nil>)', 'raise a, b' )
		self._testSX( '(py RaiseStmt excType=(py Load name=a) excValue=(py Load name=b) traceback=(py Load name=c))', 'raise a, b, c' )
		
		
	def test_BreakStmt(self):
		self._testSX( '(py BreakStmt)', 'break' )
		
		
	def test_ContinueStmt(self):
		self._testSX( '(py ContinueStmt)', 'continue' )
		
		
	def test_ImportStmt(self):
		self._testSX( '(py ImportStmt modules=[(py ModuleImport name=a)])', 'import a' )
		self._testSX( '(py ImportStmt modules=[(py ModuleImport name=a.b)])', 'import a.b' )
		self._testSX( '(py ImportStmt modules=[(py ModuleImportAs name=a asName=x)])', 'import a as x' )
		self._testSX( '(py ImportStmt modules=[(py ModuleImportAs name=a.b asName=x)])', 'import a.b as x' )
		
		
	def test_FromImportStmt(self):
		self._testSX( '(py FromImportStmt module=(py RelativeModule name=x) imports=[(py ModuleContentImport name=a)])', 'from x import a' )
		self._testSX( '(py FromImportStmt module=(py RelativeModule name=x) imports=[(py ModuleContentImportAs name=a asName=p)])', 'from x import a as p' )
		self._testSX( '(py FromImportStmt module=(py RelativeModule name=x) imports=[(py ModuleContentImportAs name=a asName=p) (py ModuleContentImportAs name=b asName=q)])', 'from x import a as p, b as q' )
		
		
	def test_FromImportAllStmt(self):
		self._testSX( '(py FromImportAllStmt module=(py RelativeModule name=x))', 'from x import *' )
		
		
	def test_GlobalStmt(self):
		self._testSX( '(py GlobalStmt vars=[(py GlobalVar name=a)])', 'global a' )
		self._testSX( '(py GlobalStmt vars=[(py GlobalVar name=a) (py GlobalVar name=b)])', 'global a, b' )
		
		
	def test_ExecStmt(self):
		self._testSX( '(py ExecStmt source=(py Load name=a) locals=<nil> globals=<nil>)', 'exec a' )
		self._testSX( '(py ExecStmt source=(py Load name=a) locals=(py Load name=b) globals=<nil>)', 'exec a in b' )
		self._testSX( '(py ExecStmt source=(py Load name=a) locals=(py Load name=b) globals=(py Load name=c))', 'exec a in b, c' )
		
		
		
	def test_IfStmt(self):
		self._testSX( '(py IfStmt condition=(py Load name=bA) suite=[(py Load name=b)])', 'if bA:\n\tb\n' )


	def test_ElifStmt(self):
		self._testSX( '(py ElifStmt condition=(py Load name=bA) suite=[(py Load name=b)])', 'elif bA:\n\tb\n' )


	def test_ElseStmt(self):
		self._testSX( '(py ElseStmt suite=[(py Load name=b)])', 'else:\n\tb\n' )


	def test_WhileStmt(self):
		self._testSX( '(py WhileStmt condition=(py Load name=bA) suite=[(py Load name=b)])', 'while bA:\n\tb\n' )


	def test_ForStmt(self):
		self._testSX( '(py ForStmt target=(py Load name=a) source=(py Load name=b) suite=[(py Load name=c)])', 'for a in b:\n\tc\n' )


	def test_TryStmt(self):
		self._testSX( '(py TryStmt suite=[(py Load name=b)])', 'try:\n\tb\n' )


	def test_exceptStmt(self):
		self._testSX( '(py ExceptStmt exception=<nil> target=<nil> suite=[(py Load name=b)])', 'except:\n\tb\n' )
		self._testSX( '(py ExceptStmt exception=(py Load name=a) target=<nil> suite=[(py Load name=b)])', 'except a:\n\tb\n' )
		self._testSX( '(py ExceptStmt exception=(py Load name=a) target=(py Load name=x) suite=[(py Load name=b)])', 'except a, x:\n\tb\n' )


	def test_finallyStmt(self):
		self._testSX( '(py FinallyStmt suite=[(py Load name=b)])', 'finally:\n\tb\n' )


	def test_withStmt(self):
		self._testSX( '(py WithStmt expr=(py Load name=a) target=<nil> suite=[(py Load name=b)])', 'with a:\n\tb\n' )
		self._testSX( '(py WithStmt expr=(py Load name=a) target=(py Load name=x) suite=[(py Load name=b)])', 'with a as x:\n\tb\n' )


	def test_defStmt(self):
		self._testSX( '(py DefStmt name=myFunc params=[(py SimpleParam name=a) (py DefaultValueParam name=b defaultValue=(py Load name=c)) (py ParamList name=d) (py KWParamList name=e)] suite=[(py Load name=b)])', 'def myFunc(a, b=c, *d, **e):\n\tb\n' )


	def test_decoStmt(self):
		self._testSX( '(py DecoStmt name=myDeco args=<nil>)', '@myDeco' )
		self._testSX( '(py DecoStmt name=myDeco args=[(py Load name=a) (py Load name=b)])', '@myDeco( a, b )' )

		
	def test_classStmt(self):
		self._testSX( '(py ClassStmt name=A bases=<nil> suite=[(py Load name=b)])', 'class A:\n\tb\n' )
		self._testSX( '(py ClassStmt name=A bases=[(py Load name=object)] suite=[(py Load name=b)])', 'class A (object):\n\tb\n' )
		self._testSX( '(py ClassStmt name=A bases=[(py Load name=object) (py Load name=Q)] suite=[(py Load name=b)])', 'class A (object, Q):\n\tb\n' )



	def test_CommentStmt(self):
		self._testSX( '(py CommentStmt comment=HelloWorld)', '#HelloWorld' )
		
		
		
		
