##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMObject

from Britefury.gSym.gSymCodeGenerator import GSymCodeGeneratorObjectNodeDispatch
from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod

from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25 import ExternalExpression


def _indent(x):
	lines = x.split( '\n' )
	lines = [ '\t' + l   for l in lines ]
	if lines[-1] == '\t':
		lines[-1] = ''
	return '\n'.join( lines )


_runtime_resourceMap_Name = '__gsym_resourceMap__'


class Python25CodeGeneratorError (Exception):
	pass


class Python25CodeGeneratorUnparsedError (Python25CodeGeneratorError):
	pass


class Python25CodeGeneratorIndentationError (Python25CodeGeneratorError):
	pass


class Python25CodeGeneratorInvalidFormatError (Python25CodeGeneratorError):
	pass

class Python25CodeGeneratorSyntaxError (Python25CodeGeneratorError):
	pass



class Python25CodeGenerator (GSymCodeGeneratorObjectNodeDispatch):
	__dispatch_num_args__ = 0
	
	
	def __init__(self, bErrorChecking=True):
		super( Python25CodeGenerator, self ).__init__()
		self._bErrorChecking = bErrorChecking
			
		
	
	
	# Misc
	@DMObjectNodeDispatchMethod( Schema.BlankLine )
	def BlankLine(self, node):
		return ''
	

	@DMObjectNodeDispatchMethod( Schema.UNPARSED )
	def UNPARSED(self, node, value):
		raise Python25CodeGeneratorUnparsedError
	
	
	# String literal
	@DMObjectNodeDispatchMethod( Schema.StringLiteral )
	def StringLiteral(self, node, format, quotation, value):
		prefix = ''
		if format == 'ascii':
			prefix = ''
		elif format == 'unicode':
			prefix = 'u'
		elif format == 'ascii-regex':
			prefix = 'r'
		elif format == 'unicode-regex':
			prefix = 'ur'
		else:
			raise ValueError, 'Unknown string literal format'
		quote = '"'   if quotation == 'double'   else   "'"
		return prefix + quote + value + quote
	
	
	
	# Integer literal
	@DMObjectNodeDispatchMethod( Schema.IntLiteral )
	def IntLiteral(self, node, format, numType, value):
		format = format
		numType = numType
		value = value

		if numType == 'int':
			if format == 'decimal':
				valueString = '%d'  %  int( value )
			elif format == 'hex':
				valueString = '0x%x'  %  int( value, 16 )
			else:
				raise Python25CodeGeneratorInvalidFormatError, 'invalid integer literal format'
		elif numType == 'long':
			if format == 'decimal':
				valueString = '%dL'  %  long( value )
			elif format == 'hex':
				valueString = '0x%xL'  %  long( value, 16 )
			else:
				raise Python25CodeGeneratorInvalidFormatError, 'invalid integer literal format'
		else:
			raise Python25CodeGeneratorInvalidFormatError, 'invalid integer literal type'
				
		return valueString

	
	

	# Float literal
	@DMObjectNodeDispatchMethod( Schema.FloatLiteral )
	def FloatLiteral(self, node, value):
		return value

	
	
	# Imaginary literal
	@DMObjectNodeDispatchMethod( Schema.ImaginaryLiteral )
	def ImaginaryLiteral(self, node, value):
		return value
	
	
	
	# Target
	@DMObjectNodeDispatchMethod( Schema.SingleTarget )
	def SingleTarget(self, node, name):
		return name
	
	@DMObjectNodeDispatchMethod( Schema.TupleTarget )
	def TupleTarget(self, node, targets):
		return '( '  +  ', '.join( [ self( i )   for i in targets ] )  +  ', )'
	
	@DMObjectNodeDispatchMethod( Schema.ListTarget )
	def ListTarget(self, node, targets):
		return '[ '  +  ', '.join( [ self( i )   for i in targets ] )  +  ' ]'
	
	

	# Variable reference
	@DMObjectNodeDispatchMethod( Schema.Load )
	def Load(self, node, name):
		return str( name )

	
	
	# Tuple literal	
	@DMObjectNodeDispatchMethod( Schema.TupleLiteral )
	def TupleLiteral(self, node, values):
		return '( '  +  ', '.join( [ self( i )   for i in values ] )  +  ', )'

	
	
	# List literal
	@DMObjectNodeDispatchMethod( Schema.ListLiteral )
	def ListLiteral(self, node, values):
		return '[ '  +  ', '.join( [ self( i )   for i in values ] )  +  ' ]'
	
	
	
	# List comprehension / generator expression
	@DMObjectNodeDispatchMethod( Schema.ComprehensionFor )
	def ComprehensionFor(self, node, target, source):
		return 'for ' + self( target ) + ' in ' + self( source )
	
	@DMObjectNodeDispatchMethod( Schema.ComprehensionIf )
	def ComprehensionIf(self, node, condition):
		return 'if ' + self( condition )
	
	@DMObjectNodeDispatchMethod( Schema.ListComp )
	def ListComp(self, node, resultExpr, comprehensionItems):
		return '[ ' + self( resultExpr ) + '   ' + '   '.join( [ self( x )   for x in comprehensionItems ] )  +  ' ]'
	
	@DMObjectNodeDispatchMethod( Schema.GeneratorExpr )
	def GeneratorExpr(self, node, resultExpr, comprehensionItems):
		return '( ' + self( resultExpr ) + '   ' + '   '.join( [ self( c )   for c in comprehensionItems ] )  +  ' )'
	
	
	
	# Dictionary literal
	@DMObjectNodeDispatchMethod( Schema.DictKeyValuePair )
	def DictKeyValuePair(self, node, key, value):
		return self( key ) + ':' + self( value )
	
	@DMObjectNodeDispatchMethod( Schema.DictLiteral )
	def DictLiteral(self, node, values):
		return '{ '  +  ', '.join( [ self( i )   for i in values ] )  +  ' }'
	
	
	
	# Yield expression and yield atom
	@DMObjectNodeDispatchMethod( Schema.YieldExpr )
	def YieldExpr(self, node, value):
		return '(yield ' + self( value ) + ')'
		
	
	
	# Attribute ref
	@DMObjectNodeDispatchMethod( Schema.AttributeRef )
	def AttributeRef(self, node, target, name):
		return self( target ) + '.' + name

	

	# Subscript
	@DMObjectNodeDispatchMethod( Schema.SubscriptSlice )
	def SubscriptSlice(self, node, lower, upper):
		txt = lambda x:  self( x )   if x is not None   else ''
		return txt( lower ) + ':' + txt( upper )

	@DMObjectNodeDispatchMethod( Schema.SubscriptLongSlice )
	def SubscriptLongSlice(self, node, lower, upper, stride):
		txt = lambda x:  self( x )   if x is not None   else ''
		return txt( lower ) + ':' + txt( upper ) + ':' + txt( stride )
	
	@DMObjectNodeDispatchMethod( Schema.SubscriptEllipsis )
	def SubscriptEllipsis(self, node):
		return '...'

	@DMObjectNodeDispatchMethod( Schema.SubscriptTuple )
	def SubscriptTuple(self, node, values):
		return '('  +  ','.join( [ self( i )   for i in values ] )  +  ',)'

	@DMObjectNodeDispatchMethod( Schema.Subscript )
	def Subscript(self, node, target, index):
		return self( target ) + '[' + self( index ) + ']'
	

	
	# Call	
	@DMObjectNodeDispatchMethod( Schema.CallKWArg )
	def CallKWArg(self, node, name, value):
		return name + '=' + self( value )
	
	@DMObjectNodeDispatchMethod( Schema.CallArgList )
	def CallArgList(self, node, value):
		return '*' + self( value )
	
	@DMObjectNodeDispatchMethod( Schema.CallKWArgList )
	def CallKWArgList(self, node, value):
		return '**' + self( value )
	
	@DMObjectNodeDispatchMethod( Schema.Call )
	def Call(self, node, target, args):
		return self( target ) + '( ' + ', '.join( [ self( a )   for a in args ] ) + ' )'
	
	
	
	# Operators
	@DMObjectNodeDispatchMethod( Schema.Pow )
	def Pow(self, node, x, y):
		return '( '  +  self( x )  +  ' ** '  +  self( y )  +  ' )'
	
	
	@DMObjectNodeDispatchMethod( Schema.Invert )
	def Invert(self, node, x):
		return '( ~'  +  self( x )  +  ' )'
	
	@DMObjectNodeDispatchMethod( Schema.Negate )
	def Negate(self, node, x):
		return '( -'  +  self( x )  +  ' )'
	
	@DMObjectNodeDispatchMethod( Schema.Pos )
	def Pos(self, node, x):
		return '( +'  +  self( x )  +  ' )'
	
	
	@DMObjectNodeDispatchMethod( Schema.Mul )
	def Mul(self, node, x, y):
		return '( '  +  self( x )  +  ' * '  +  self( y )  +  ' )'
	
	@DMObjectNodeDispatchMethod( Schema.Div )
	def Div(self, node, x, y):
		return '( '  +  self( x )  +  ' / '  +  self( y )  +  ' )'
	
	@DMObjectNodeDispatchMethod( Schema.Mod )
	def Mod(self, node, x, y):
		return '( '  +  self( x )  +  ' % '  +  self( y )  +  ' )'
	
	@DMObjectNodeDispatchMethod( Schema.Add )
	def Add(self, node, x, y):
		return '( '  +  self( x )  +  ' + '  +  self( y )  +  ' )'
	
	@DMObjectNodeDispatchMethod( Schema.Sub )
	def Sub(self, node, x, y):
		return '( '  +  self( x )  +  ' - '  +  self( y )  +  ' )'
	
	
	@DMObjectNodeDispatchMethod( Schema.LShift )
	def LShift(self, node, x, y):
		return '( '  +  self( x )  +  ' << '  +  self( y )  +  ' )'
	
	@DMObjectNodeDispatchMethod( Schema.RShift )
	def RShift(self, node, x, y):
		return '( '  +  self( x )  +  ' >> '  +  self( y )  +  ' )'
	
	
	@DMObjectNodeDispatchMethod( Schema.BitAnd )
	def BitAnd(self, node, x, y):
		return '( '  +  self( x )  +  ' & '  +  self( y )  +  ' )'
	
	@DMObjectNodeDispatchMethod( Schema.BitXor )
	def BitXor(self, node, x, y):
		return '( '  +  self( x )  +  ' ^ '  +  self( y )  +  ' )'
	
	@DMObjectNodeDispatchMethod( Schema.BitOr )
	def BitOr(self, node, x, y):
		return '( '  +  self( x )  +  ' | '  +  self( y )  +  ' )'
	

	@DMObjectNodeDispatchMethod( Schema.Cmp )
	def Cmp(self, node, x, ops):
		return '( ' + self( x )  +  ''.join( [ self( op )   for op in ops ] ) + ' )'
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpLte )
	def CmpOpLte(self, node, y):
		return ' <= ' + self( y )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpLt )
	def CmpOpLt(self, node, y):
		return ' < ' + self( y )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpGte )
	def CmpOpGte(self, node, y):
		return ' >= ' + self( y )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpGt )
	def CmpOpGt(self, node, y):
		return ' > ' + self( y )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpEq )
	def CmpOpEq(self, node, y):
		return ' == ' + self( y )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpNeq )
	def CmpOpNeq(self, node, y):
		return ' != ' + self( y )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpIsNot )
	def CmpOpIsNot(self, node, y):
		return ' is not ' + self( y )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpIs )
	def CmpOpIs(self, node, y):
		return ' is ' + self( y )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpNotIn )
	def CmpOpNotIn(self, node, y):
		return ' not in ' + self( y )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpIn )
	def CmpOpIn(self, node, y):
		return ' in ' + self( y )
	
	

	
	@DMObjectNodeDispatchMethod( Schema.NotTest )
	def NotTest(self, node, x):
		return '(not '  +  self( x )  +  ')'
	
	@DMObjectNodeDispatchMethod( Schema.AndTest )
	def AndTest(self, node, x, y):
		return '( '  +  self( x )  +  ' and '  +  self( y )  +  ' )'
	
	@DMObjectNodeDispatchMethod( Schema.OrTest )
	def OrTest(self, node, x, y):
		return '( '  +  self( x )  +  ' or '  +  self( y )  +  ' )'
	
	
	
	
	# Parameters	
	@DMObjectNodeDispatchMethod( Schema.SimpleParam )
	def SimpleParam(self, node, name):
		return name
	
	@DMObjectNodeDispatchMethod( Schema.DefaultValueParam )
	def DefaultValueParam(self, node, name, defaultValue):
		return name  +  '='  +  self( defaultValue )
	
	@DMObjectNodeDispatchMethod( Schema.ParamList )
	def ParamList(self, node, name):
		return '*'  +  name
	
	@DMObjectNodeDispatchMethod( Schema.KWParamList )
	def KWParamList(self, node, name):
		return '**'  +  name
	
	
	
	# Lambda expression
	@DMObjectNodeDispatchMethod( Schema.LambdaExpr )
	def LambdaExpr(self, node, params, expr):
		return '( lambda '  +  ', '.join( [ self( p )   for p in params ] )  +  ': '  +  self( expr ) + ' )'
	
	
	
	# Conditional expression
	@DMObjectNodeDispatchMethod( Schema.ConditionalExpr )
	def ConditionalExpr(self, node, condition, expr, elseExpr):
		return self( expr )  +  '   if '  +  self( condition )  +  '   else '  +  self( elseExpr )

	
	
	
	# External expression
	@DMObjectNodeDispatchMethod( Schema.ExternalExpr )
	def ExternalExpr(self, node, expr):
		if isinstance( expr, DMObject ):
			schema = expr.getDMObjectClass().getSchema()
			codeGenFac = ExternalExpression.getExternalExpressionCodeGeneratorFactory( schema )
			codeGen = codeGenFac( self )
			return codeGen( expr )
		else:
			return 'None'
		
	
	
	# Inline object
	@DMObjectNodeDispatchMethod( Schema.InlineObjectExpr )
	def InlineObjectExpr(self, node, resource):
		raise ValueError, 'Python25CodeGenerator does not support inline object expressions; a Python25ModuleCodeGenerator must be used'
	
	
	@DMObjectNodeDispatchMethod( Schema.InlineObjectStmt )
	def InlineObjectStmt (self, node, resource):
		raise ValueError, 'Python25CodeGenerator does not support inline object statements; a Python25ModuleCodeGenerator must be used'
		
	
	
	# Expression statement
	@DMObjectNodeDispatchMethod( Schema.ExprStmt )
	def ExprStmt(self, node, expr):
		return self( expr )
	
	
	# Assert statement
	@DMObjectNodeDispatchMethod( Schema.AssertStmt )
	def AssertStmt(self, node, condition, fail):
		return 'assert '  +  self( condition )  +  ( ', ' + self( fail )   if fail is not None   else  '' )
	
	
	# Assignment statement
	@DMObjectNodeDispatchMethod( Schema.AssignStmt )
	def AssignStmt(self, node, targets, value):
		return ''.join( [ self( t ) + ' = '   for t in targets ] )  +  self( value )
	
	
	# Augmented assignment statement
	@DMObjectNodeDispatchMethod( Schema.AugAssignStmt )
	def AugAssignStmt(self, node, op, target, value):
		return self( target )  +  ' '  +  op  +  ' '  +  self( value )
	
	
	# Pass statement
	@DMObjectNodeDispatchMethod( Schema.PassStmt )
	def PassStmt(self, node):
		return 'pass'
	
	
	# Del statement
	@DMObjectNodeDispatchMethod( Schema.DelStmt )
	def DelStmt(self, node, target):
		return 'del '  +  self( target )
	
	
	# Return statement
	@DMObjectNodeDispatchMethod( Schema.ReturnStmt )
	def ReturnStmt(self, node, value):
		return 'return '  +  self( value )
	
	
	# Yield statement
	@DMObjectNodeDispatchMethod( Schema.YieldStmt )
	def YieldStmt(self, node, value):
		return 'yield '  +  self( value )
	
	
	# Raise statement
	@DMObjectNodeDispatchMethod( Schema.RaiseStmt )
	def RaiseStmt(self, node, excType, excValue, traceback):
		params = ', '.join( [ self( x )   for x in excType, excValue, traceback   if x is not None ] )
		if params != '':
			return 'raise ' + params
		else:
			return 'raise'
	
	
	# Break statement
	@DMObjectNodeDispatchMethod( Schema.BreakStmt )
	def BreakStmt(self, node):
		return 'break'
	
	
	# Continue statement
	@DMObjectNodeDispatchMethod( Schema.ContinueStmt )
	def ContinueStmt(self, node):
		return 'continue'
	
	
	# Import statement
	@DMObjectNodeDispatchMethod( Schema.RelativeModule )
	def RelativeModule(self, node, name):
		return name
	
	@DMObjectNodeDispatchMethod( Schema.ModuleImport )
	def ModuleImport(self, node, name):
		return name
	
	@DMObjectNodeDispatchMethod( Schema.ModuleImportAs )
	def ModuleImportAs(self, node, name, asName):
		return name + ' as ' + asName
	
	@DMObjectNodeDispatchMethod( Schema.ModuleContentImport )
	def ModuleContentImport(self, node, name):
		return name
	
	@DMObjectNodeDispatchMethod( Schema.ModuleContentImportAs )
	def ModuleContentImportAs(self, node, name, asName):
		return name + ' as ' + asName
	
	@DMObjectNodeDispatchMethod( Schema.ImportStmt )
	def ImportStmt(self, node, modules):
		return 'import '  +  ', '.join( [ self( x )   for x in modules ] )
	
	@DMObjectNodeDispatchMethod( Schema.FromImportStmt )
	def FromImportStmt(self, node, module, imports):
		return 'from ' + self( module ) + ' import ' + ', '.join( [ self( x )   for x in imports ] )
	
	@DMObjectNodeDispatchMethod( Schema.FromImportAllStmt )
	def FromImportAllStmt(self, node, module):
		return 'from ' + self( module ) + ' import *'
	
	
	# Global statement
	@DMObjectNodeDispatchMethod( Schema.GlobalVar )
	def GlobalVar(self, node, name):
		return name
	
	@DMObjectNodeDispatchMethod( Schema.GlobalStmt )
	def GlobalStmt(self, node, vars):
		return 'global '  +  ', '.join( [ self( x )   for x in vars ] )
	
	
	# Exec statement
	@DMObjectNodeDispatchMethod( Schema.ExecStmt )
	def ExecStmt(self, node, source, globals, locals):
		txt = 'exec '  +  self( source )
		if globals is not None:
			txt += ' in '  +  self( globals )
		if locals is not None:
			txt += ', '  +  self( locals )
		return txt
	
	
	# Print statement
	@DMObjectNodeDispatchMethod( Schema.PrintStmt )
	def PrintStmt(self, node, destination, values):
		txt = 'print'
		if destination is not None:
			txt += ' >> %s'  %  self( destination )
		if len( values ) > 0:
			if destination is not None:
				txt += ','
			txt += ' '
			txt += ', '.join( [ self( v )   for v in values ] )
		return txt
	
	

	def _elseSuiteToText(self, suite):
		if suite is not None:
			suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
			return 'else:\n'  +  _indent( suiteText )
		else:
			return ''
			
	
	
	def _finallySuiteToText(self, suite):
		if suite is not None:
			suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
			return 'finally:\n'  +  _indent( suiteText )
		else:
			return ''
			
	
	
	# If statement
	@DMObjectNodeDispatchMethod( Schema.IfStmt )
	def IfStmt(self, node, condition, suite, elifBlocks, elseSuite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		elifText = ''.join( [ self( b )   for b in elifBlocks ] )
		return 'if '  +  self( condition ) + ':\n'  +  _indent( suiteText )  +  elifText  +  self._elseSuiteToText( elseSuite )
	

	# Elif block
	@DMObjectNodeDispatchMethod( Schema.ElifBlock )
	def ElifBlock(self, node, condition, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'elif '  +  self( condition ) + ':\n'  +  _indent( suiteText )
	

	# While statement
	@DMObjectNodeDispatchMethod( Schema.WhileStmt )
	def WhileStmt(self, node, condition, suite, elseSuite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'while '  +  self( condition ) + ':\n'  +  _indent( suiteText )  +  self._elseSuiteToText( elseSuite )
	

	# For statement
	@DMObjectNodeDispatchMethod( Schema.ForStmt )
	def ForStmt(self, node, target, source, suite, elseSuite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'for '  +  self( target )  +  ' in '  +  self( source )  +  ':\n'  +  _indent( suiteText )  +  self._elseSuiteToText( elseSuite )
	

	# Try statement
	@DMObjectNodeDispatchMethod( Schema.TryStmt )
	def TryStmt(self, node, suite, exceptBlocks, elseSuite, finallySuite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		exceptText = ''.join( [ self( b )   for b in exceptBlocks ] )
		return 'try:\n'  +  _indent( suiteText )  +  exceptText  +  self._elseSuiteToText( elseSuite )  +  self._finallySuiteToText( finallySuite )
	

	# Except statement
	@DMObjectNodeDispatchMethod( Schema.ExceptBlock )
	def ExceptBlock(self, node, exception, target, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		txt = 'except'
		if exception is not None:
			txt += ' ' + self( exception )
		if target is not None:
			txt += ', ' + self( target )
		return txt + ':\n'  +  _indent( suiteText )
	

	# With statement
	@DMObjectNodeDispatchMethod( Schema.WithStmt )
	def WithStmt(self, node, expr, target, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return 'with '  +  self( expr )  +  ( ' as ' + self( target )   if target is not None   else   '' )  +  ':\n'  +  _indent( suiteText )
	
	
	# Decorator
	@DMObjectNodeDispatchMethod( Schema.Decorator )
	def Decorator(self, node, name, args):
		text = '@' + name
		if args is not None:
			text += '( ' + ', '.join( [ self( a )   for a in args ] ) + ' )'
		return text
	
	
	def _decoratorsToText(self, decorators):
		if len( decorators ) == 0:
			return ''
		else:
			return '\n'.join( [ self( deco )   for deco in decorators ] ) + '\n'
	

	# Def statement
	@DMObjectNodeDispatchMethod( Schema.DefStmt )
	def DefStmt(self, node, decorators, name, params, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return self._decoratorsToText( decorators )  +  'def '  +  name  +  '('  +  ', '.join( [ self( p )   for p in params ] )  +  '):\n'  +  _indent( suiteText )
	

	# Class statement
	@DMObjectNodeDispatchMethod( Schema.ClassStmt )
	def ClassStmt(self, node, name, bases, suite):
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		text = 'class '  +  name
		if bases is not None:
			text += ' ('  +  ', '.join( [ self( h )   for h in bases ] )  +  ')'
		return text  +  ':\n'  +  _indent( suiteText )
	
	
	
	# Indented block
	@DMObjectNodeDispatchMethod( Schema.IndentedBlock )
	def IndentedBlock(self, node, suite):
		if self._bErrorChecking:
			raise Python25CodeGeneratorIndentationError, 'Indentation error'
		suiteText = '\n'.join( [ self( line )   for line in suite ] ) + '\n'
		return _indent( suiteText )
	
	
	
	# Comment statement
	@DMObjectNodeDispatchMethod( Schema.CommentStmt )
	def CommentStmt(self, node, comment):
		return '#' + comment

	
	# Module
	@DMObjectNodeDispatchMethod( Schema.PythonModule )
	def PythonModule(self, node, suite):
		return '\n'.join( [ self( line )   for line in suite ] )

	
	# Expression
	@DMObjectNodeDispatchMethod( Schema.PythonExpression )
	def PythonExpression(self, node, expr):
		if expr is None:
			return 'None'
		else:
			return self( expr )

		

class Python25ModuleCodeGenerator (Python25CodeGenerator):
	def __init__(self, module, bErrorChecking=True):
		super( Python25ModuleCodeGenerator, self ).__init__( bErrorChecking )
		
		try:
			self._resourceMap = getattr( module, _runtime_resourceMap_Name )
		except AttributeError:
			self._resourceMap = []
			setattr( module, _runtime_resourceMap_Name, self._resourceMap )


	# Inline object expression
	@DMObjectNodeDispatchMethod( Schema.InlineObjectExpr )
	def InlineObjectExpr(self, node, resource):
		value = resource.getValue()
		
		modelType = Schema.getInlineObjectModelType( value )
		
		if modelType is Schema.Expr:
			try:
				modelFn = value.__py_model__
			except AttributeError:
				pass
			else:
				model = modelFn()
				return self( model )
		
		
		index = len( self._resourceMap )
		self._resourceMap.append( resource.getValue() )
		return _runtime_resourceMap_Name + '[%d]'  %  ( index, )
		
	
	
	# Inline object
	@DMObjectNodeDispatchMethod( Schema.InlineObjectStmt )
	def InlineObjectStmt(self, node, resource):
		value = resource.getValue()
		
		modelType = Schema.getInlineObjectModelType( value )
		
		if modelType is Schema.Stmt:
			try:
				modelFn = value.__py_model__
			except AttributeError:
				pass
			else:
				model = modelFn()
				return self( model )
		
		
		index = len( self._resourceMap )
		self._resourceMap.append( resource.getValue() )
		return _runtime_resourceMap_Name + '[%d]'  %  ( index, )
			

	
	
	
def _compileForExecution(codeGen, pythonModule, filename):
	source = codeGen( pythonModule )
	return compile( source, filename, 'exec' )


def _compileForExecutionAndEvaluation(codeGen, pythonModule, filename):
	execModule = None
	evalExpr = None
	for i, stmt in reversed( list( enumerate( pythonModule['suite'] ) ) ):
		if stmt.isInstanceOf( Schema.ExprStmt ):
			execModule = Schema.PythonModule( suite=pythonModule['suite'][:i] )
			evalExpr = stmt['expr']
			break
		elif stmt.isInstanceOf( Schema.BlankLine )  or  stmt.isInstanceOf( Schema.CommentStmt ):
			pass
		else:
			break
	
	if execModule is not None  and  evalExpr is not None:
		execSource = codeGen( execModule )
		evalSource = codeGen( evalExpr )
		
		execCode = compile( execSource, filename, 'exec' )
		evalCode = compile( evalSource, filename, 'eval' )
		
		return execCode, evalCode
	else:
		return _compileForExecution( codeGen, pythonModule, filename ),  None

				
				
				

	
	
def compileForExecution(pythonModule, filename):
	return _compileForExecution( Python25CodeGenerator(), pythonModule, filename )


def compileForExecutionAndEvaluation(pythonModule, filename):
	return _compileForExecutionAndEvaluation( Python25CodeGenerator(), pythonModule, filename )

				
				
				
def compileForModuleExecution(module, pythonModule, filename):
	return _compileForExecution( Python25ModuleCodeGenerator( module ), pythonModule, filename )


def compileForModuleExecutionAndEvaluation(module, pythonModule, filename):
	return _compileForExecutionAndEvaluation( Python25ModuleCodeGenerator( module ), pythonModule, filename )

				
				
				
				
import unittest
from BritefuryJ.DocModel import DMIOReader, DMSchemaResolver

class TestCase_Python25CodeGenerator (unittest.TestCase):
	class _Resolver (DMSchemaResolver):
		def getSchema(self, location):
			return Schema.schema
		
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
		
		
	def _testGenSX(self, gen, sx, expected):
		sx = '{ py=org.Britefury.gSym.Languages.Python25 : ' + sx + ' }'
		data = DMIOReader.readFromString( sx, self._resolver )
		
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
		self.assertRaises( Python25CodeGeneratorUnparsedError, lambda: self._testSX( '(py UNPARSED value=Test)', '' ) )
		
		
	def test_StringLiteral(self):
		self._testSX( '(py StringLiteral format=ascii quotation=single value="Hi there")', '\'Hi there\'' )
		
		
	def test_IntLiteral(self):
		self._testSX( '(py IntLiteral format=decimal numType=int value=123)', '123' )
		self._testSX( '(py IntLiteral format=hex numType=int value=1a4)', '0x1a4' )
		self._testSX( '(py IntLiteral format=decimal numType=long value=123)', '123L' )
		self._testSX( '(py IntLiteral format=hex numType=long value=1a4)', '0x1a4L' )
		self.assertRaises( Python25CodeGeneratorInvalidFormatError, lambda: self._testSX( '(py IntLiteral format=foo numType=long value=1a4)', '' ) )
		self.assertRaises( Python25CodeGeneratorInvalidFormatError, lambda: self._testSX( '(py IntLiteral format=hex numType=foo value=1a4)', '' ) )
		
		
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
		
	
	def test_YieldExpr(self):
		self._testSX( '(py YieldExpr value=(py Load name=a))', '(yield a)' )
		
		
	def test_AttributeRef(self):
		self._testSX( '(py AttributeRef target=(py Load name=a) name=b)', 'a.b' )
		
		
	def test_Subscript(self):
		self._testSX( '(py Subscript target=(py Load name=a) index=(py Load name=b))', 'a[b]' )
		
		
	def test_Subscript_Ellipsis(self):
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptEllipsis))', 'a[...]' )
		
		
	def test_subscript_slice(self):
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptSlice lower=(py Load name=a) upper=(py Load name=b)))', 'a[a:b]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptSlice lower=(py Load name=a) upper=`null`))', 'a[a:]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptSlice lower=`null` upper=(py Load name=b)))', 'a[:b]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptSlice lower=`null` upper=`null`))', 'a[:]' )
		

	def test_subscript_longSlice(self):
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=(py Load name=a) upper=(py Load name=b) stride=(py Load name=c)))', 'a[a:b:c]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=(py Load name=a) upper=(py Load name=b) stride=`null`))', 'a[a:b:]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=(py Load name=a) upper=`null` stride=(py Load name=c)))', 'a[a::c]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=(py Load name=a) upper=`null` stride=`null`))', 'a[a::]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=`null` upper=(py Load name=b) stride=(py Load name=c)))', 'a[:b:c]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=`null` upper=(py Load name=b) stride=`null`))', 'a[:b:]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=`null` upper=`null` stride=(py Load name=c)))', 'a[::c]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=`null` upper=`null` stride=`null`))', 'a[::]' )

		
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
		
		
		
	
	#
	# Simple statements
	#
	
	
	def test_exprStmt(self):
		self._testSX( '(py ExprStmt expr=(py Load name=x))', 'x' )
		
		
	def test_assertStmt(self):
		self._testSX( '(py AssertStmt condition=(py Load name=x) fail=`null`)', 'assert x' )
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
		self._testSX( '(py RaiseStmt excType=`null` excValue=`null` traceback=`null`)', 'raise' )
		self._testSX( '(py RaiseStmt excType=(py Load name=a) excValue=`null` traceback=`null`)', 'raise a' )
		self._testSX( '(py RaiseStmt excType=(py Load name=a) excValue=(py Load name=b) traceback=`null`)', 'raise a, b' )
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
		self._testSX( '(py ExecStmt source=(py Load name=a) globals=`null` locals=`null`)', 'exec a' )
		self._testSX( '(py ExecStmt source=(py Load name=a) globals=(py Load name=b) locals=`null`)', 'exec a in b' )
		self._testSX( '(py ExecStmt source=(py Load name=a) globals=(py Load name=b) locals=(py Load name=c))', 'exec a in b, c' )
		
		
	def test_PrintStmt(self):
		self._testSX( '(py PrintStmt values=[])', 'print' )
		self._testSX( '(py PrintStmt values=[(py Load name=a)])', 'print a' )
		self._testSX( '(py PrintStmt values=[(py Load name=a) (py Load name=b)])', 'print a, b' )
		self._testSX( '(py PrintStmt destination=(py Load name=x) values=[])', 'print >> x' )
		self._testSX( '(py PrintStmt destination=(py Load name=x) values=[(py Load name=a)])', 'print >> x, a' )
		self._testSX( '(py PrintStmt destination=(py Load name=x) values=[(py Load name=a) (py Load name=b)])', 'print >> x, a, b' )
		
				
	
	#
	# Compound statements
	#
	
	def test_IfStmt(self):
		self._testSX( '(py IfStmt condition=(py Load name=bA) suite=[(py Load name=b)] elifBlocks=[])', 'if bA:\n\tb\n' )
		self._testSX( '(py IfStmt condition=(py Load name=bA) suite=[(py Load name=b)] elifBlocks=[] elseSuite=[(py Load name=c)])', 'if bA:\n\tb\nelse:\n\tc\n' )
		self._testSX( '(py IfStmt condition=(py Load name=bA) suite=[(py Load name=b)] elifBlocks=[(py ElifBlock condition=(py Load name=bA) suite=[(py Load name=b)])] elseSuite=[(py Load name=c)])',
			      'if bA:\n\tb\nelif bA:\n\tb\nelse:\n\tc\n' )


	def test_ElifBlock(self):
		self._testSX( '(py ElifBlock condition=(py Load name=bA) suite=[(py Load name=b)])', 'elif bA:\n\tb\n' )


	def test_WhileStmt(self):
		self._testSX( '(py WhileStmt condition=(py Load name=bA) suite=[(py Load name=b)])', 'while bA:\n\tb\n' )
		self._testSX( '(py WhileStmt condition=(py Load name=bA) suite=[(py Load name=b)] elseSuite=[(py Load name=c)])', 'while bA:\n\tb\nelse:\n\tc\n' )


	def test_ForStmt(self):
		self._testSX( '(py ForStmt target=(py Load name=a) source=(py Load name=b) suite=[(py Load name=c)])', 'for a in b:\n\tc\n' )
		self._testSX( '(py ForStmt target=(py Load name=a) source=(py Load name=b) suite=[(py Load name=c)] elseSuite=[(py Load name=d)])', 'for a in b:\n\tc\nelse:\n\td\n' )


	def test_TryStmt(self):
		self._testSX( '(py TryStmt suite=[(py Load name=b)] exceptBlocks=[])', 'try:\n\tb\n' )
		self._testSX( '(py TryStmt suite=[(py Load name=b)] exceptBlocks=[] elseSuite=[(py Load name=d)])', 'try:\n\tb\nelse:\n\td\n' )
		self._testSX( '(py TryStmt suite=[(py Load name=b)] exceptBlocks=[] elseSuite=[(py Load name=d)] finallySuite=[(py Load name=e)])', 'try:\n\tb\nelse:\n\td\nfinally:\n\te\n' )
		self._testSX( '(py TryStmt suite=[(py Load name=b)] exceptBlocks=[(py ExceptBlock exception=`null` target=`null` suite=[(py Load name=b)])] elseSuite=[(py Load name=d)] finallySuite=[(py Load name=e)])',
			      'try:\n\tb\nexcept:\n\tb\nelse:\n\td\nfinally:\n\te\n' )


	def test_exceptBlock(self):
		self._testSX( '(py ExceptBlock exception=`null` target=`null` suite=[(py Load name=b)])', 'except:\n\tb\n' )
		self._testSX( '(py ExceptBlock exception=(py Load name=a) target=`null` suite=[(py Load name=b)])', 'except a:\n\tb\n' )
		self._testSX( '(py ExceptBlock exception=(py Load name=a) target=(py Load name=x) suite=[(py Load name=b)])', 'except a, x:\n\tb\n' )


	def test_withStmt(self):
		self._testSX( '(py WithStmt expr=(py Load name=a) target=`null` suite=[(py Load name=b)])', 'with a:\n\tb\n' )
		self._testSX( '(py WithStmt expr=(py Load name=a) target=(py Load name=x) suite=[(py Load name=b)])', 'with a as x:\n\tb\n' )


	def test_decorator(self):
		self._testSX( '(py Decorator name=myDeco args=`null`)', '@myDeco' )
		self._testSX( '(py Decorator name=myDeco args=[(py Load name=a) (py Load name=b)])', '@myDeco( a, b )' )

		
	def test_defStmt(self):
		self._testSX( '(py DefStmt decorators=[] name=myFunc params=[(py SimpleParam name=a) (py DefaultValueParam name=b defaultValue=(py Load name=c)) (py ParamList name=d) (py KWParamList name=e)] suite=[(py Load name=b)])', 'def myFunc(a, b=c, *d, **e):\n\tb\n' )
		self._testSX( '(py DefStmt decorators=[(py Decorator name=myDeco args=`null`)] name=myFunc params=[(py SimpleParam name=a) (py DefaultValueParam name=b defaultValue=(py Load name=c)) (py ParamList name=d) (py KWParamList name=e)] suite=[(py Load name=b)])', '@myDeco\ndef myFunc(a, b=c, *d, **e):\n\tb\n' )
		self._testSX( '(py DefStmt decorators=[(py Decorator name=myDeco args=`null`) (py Decorator name=myDeco args=[(py Load name=a) (py Load name=b)])] name=myFunc params=[(py SimpleParam name=a) (py DefaultValueParam name=b defaultValue=(py Load name=c)) (py ParamList name=d) (py KWParamList name=e)] suite=[(py Load name=b)])', '@myDeco\n@myDeco( a, b )\ndef myFunc(a, b=c, *d, **e):\n\tb\n' )


	def test_classStmt(self):
		self._testSX( '(py ClassStmt name=A bases=`null` suite=[(py Load name=b)])', 'class A:\n\tb\n' )
		self._testSX( '(py ClassStmt name=A bases=[(py Load name=object)] suite=[(py Load name=b)])', 'class A (object):\n\tb\n' )
		self._testSX( '(py ClassStmt name=A bases=[(py Load name=object) (py Load name=Q)] suite=[(py Load name=b)])', 'class A (object, Q):\n\tb\n' )


	def test_IndentedBlock(self):
		self._testGenSX( Python25CodeGenerator( False ), '(py IndentedBlock suite=[(py Load name=b)])', '\tb\n' )
		self.assertRaises( Python25CodeGeneratorIndentationError, lambda: self._testSX( '(py IndentedBlock suite=[(py Load name=b)])', '' ) )
		

	def test_CommentStmt(self):
		self._testSX( '(py CommentStmt comment=HelloWorld)', '#HelloWorld' )
		
		
		
