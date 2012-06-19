##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from copy import deepcopy

from BritefuryJ.DocModel import DMObject, DMList, DMEmbeddedObject, DMEmbeddedIsolatedObject, DMSchema, DMObjectClass, DMNode

from Britefury.Dispatch.MethodDispatch import ObjectDispatchMethod, DMObjectNodeDispatchMethod, methodDispatch

from LarchCore.Languages.Python2 import Schema
from LarchCore.Languages.Python2.PythonEditor.Precedence import *


class Line (object):
	def __init__(self, text, node):
		self.text = text
		self.node = node
	
	def nodes(self, nodesList):
		nodesList.append( self.node )
		
	def indent(self):
		return Line( '\t' + self.text, self.node )
		
	def __str__(self):
		return self.text



class Block (object):
	def __init__(self, lines):
		self.lines = []
		for l in lines:
			if isinstance( l, Line ):
				self.lines.append( l )
			elif isinstance( l, Block ):
				self.lines.extend( l.lines )
			else:
				raise TypeError, 'line should be a line or a Block, not a %s (with a value of %s)' % ( type( l ), l )
			
	def __add__(self, x):
		if not isinstance( x, Block ):
			raise TypeError, 'can only add blocks to one another'
		return Block( self.lines + x.lines )
		
	def indent(self):
		return Block( [ l.indent()   for l in self.lines ] )
	
	def nodeMap(self):
		return [ l.node   for l in self.lines ]
	
	def __str__(self):
		return '\n'.join( [ l.text   for l in self.lines ] ) + '\n'


_emptyBlock = Block( [] )




class Python2CodeGeneratorError (Exception):
	pass


class Python2CodeGeneratorUnparsedError (Python2CodeGeneratorError):
	pass


class Python2CodeGeneratorIndentationError (Python2CodeGeneratorError):
	pass


class Python2CodeGeneratorInvalidFormatError (Python2CodeGeneratorError):
	pass



class Python2CodeGenerator (object):
	__dispatch_num_args__ = 0
	
	
	def __init__(self, filename, bErrorChecking=True):
		super( Python2CodeGenerator, self ).__init__()
		self._filename = filename
		self._bErrorChecking = bErrorChecking


	@property
	def filename(self):
		return self._filename


	@property
	def module(self):
		return None
			
		
	def compileForEvaluation(self, pythonExpression):
		source = str( self( pythonExpression ) )
		return compile( source, self._filename, 'eval' )
	
	
	def compileForExecution(self, pythonModule):
		source = str( self( pythonModule ) )
		return compile( source, self._filename, 'exec' )
	
	
	def compileForExecutionAndEvaluation(self, pythonModule):
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
			execSource = str( self( execModule ) )
			evalSource = str( self( evalExpr ) )
			
			execCode = compile( execSource, self._filename, 'exec' )
			evalCode = compile( evalSource, self._filename, 'eval' )
			
			return execCode, evalCode
		else:
			return self.compileForExecution( pythonModule ),  None

	
	
	# Callable - use document model node method dispatch mechanism
	def __call__(self, x, outerPrec=PRECEDENCE_NONE):
		s = methodDispatch( self, x )
		if parensRequired[x]:
			prec = nodePrecedence[x]
			if prec != -1  and  outerPrec != -1  and  prec > outerPrec:
				s = '(' + s + ')'
		return s
		

	
	def _tupleElements(self, xs):
		if len( xs ) == 1:
			return self( xs[0], PRECEDENCE_CONTAINER_ELEMENT ) + ','
		else:
			return ', '.join( [ self( x, PRECEDENCE_CONTAINER_ELEMENT )   for x in xs ] )
	
	
	# Misc
	@DMObjectNodeDispatchMethod( Schema.BlankLine )
	def BlankLine(self, node):
		return Line( '', node )
	

	@DMObjectNodeDispatchMethod( Schema.UNPARSED )
	def UNPARSED(self, node, value):
		raise Python2CodeGeneratorUnparsedError
	


	# String literal
	@DMObjectNodeDispatchMethod( Schema.StringLiteral )
	def StringLiteral(self, node, format, quotation, value):
		prefix = Schema.stringFormatToPrefix( format )
		quote = '"'   if quotation == 'double'   else   "'"
		return prefix + quote + value + quote


	@DMObjectNodeDispatchMethod( Schema.MultilineStringLiteral )
	def MultilineStringLiteral(self, node, format, value):
		s = repr( value )
		if not format.endswith( 'regex' ):
			s = s.replace( '\\\\', '\\' )
		return s



	# Integer literal
	@DMObjectNodeDispatchMethod( Schema.IntLiteral )
	def IntLiteral(self, node, format, numType, value):
		if numType == 'int':
			if format == 'decimal':
				valueString = '{0}'.format( int( value, 10 ) )
			elif format == 'hex':
				valueString = '0x{0:x}'.format( int( value, 16 ) )
			elif format == 'bin':
				valueString = '0b{0:b}'.format( int( value, 2 ) )
			elif format == 'oct':
				valueString = '0o{0:o}'.format( int( value, 8 ) )
			else:
				raise Python2CodeGeneratorInvalidFormatError, 'invalid integer literal format'
		elif numType == 'long':
			if format == 'decimal':
				valueString = '{0}L'.format( long( value, 10 ) )
			elif format == 'hex':
				valueString = '0x{0:x}L'.format( long( value, 16 ) )
			elif format == 'bin':
				valueString = '0b{0:b}L'.format( long( value, 2 ) )
			elif format == 'oct':
				valueString = '0o{0:o}L'.format( long( value, 8 ) )
			else:
				raise Python2CodeGeneratorInvalidFormatError, 'invalid integer literal format'
		else:
			raise Python2CodeGeneratorInvalidFormatError, 'invalid integer literal type'
				
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
		return '(' + self._tupleElements( targets ) + ')'
	
	@DMObjectNodeDispatchMethod( Schema.ListTarget )
	def ListTarget(self, node, targets):
		return '[ '  +  ', '.join( [ self( i, PRECEDENCE_CONTAINER_ELEMENT )   for i in targets ] )  +  ' ]'
	
	

	# Variable reference
	@DMObjectNodeDispatchMethod( Schema.Load )
	def Load(self, node, name):
		return str( name )

	
	
	# Tuple literal	
	@DMObjectNodeDispatchMethod( Schema.TupleLiteral )
	def TupleLiteral(self, node, values):
		return '(' + self._tupleElements( values ) + ')'

	
	
	# List literal
	@DMObjectNodeDispatchMethod( Schema.ListLiteral )
	def ListLiteral(self, node, values):
		return '[ '  +  ', '.join( [ self( i, PRECEDENCE_CONTAINER_ELEMENT )   for i in values ] )  +  ' ]'
	
	
	
	# List comprehension / generator expression
	@DMObjectNodeDispatchMethod( Schema.ComprehensionFor )
	def ComprehensionFor(self, node, target, source):
		return 'for ' + self( target, PRECEDENCE_CONTAINER_COMPREHENSIONFOR ) + ' in ' + self( source, PRECEDENCE_CONTAINER_COMPREHENSIONFOR )
	
	@DMObjectNodeDispatchMethod( Schema.ComprehensionIf )
	def ComprehensionIf(self, node, condition):
		return 'if ' + self( condition, PRECEDENCE_CONTAINER_COMPREHENSIONIF )
	
	@DMObjectNodeDispatchMethod( Schema.ListComp )
	def ListComp(self, node, resultExpr, comprehensionItems):
		return '[ ' + self( resultExpr, PRECEDENCE_CONTAINER_ELEMENT ) + '   ' + '   '.join( [ self( x, PRECEDENCE_CONTAINER_ELEMENT )   for x in comprehensionItems ] )  +  ' ]'
	
	@DMObjectNodeDispatchMethod( Schema.GeneratorExpr )
	def GeneratorExpr(self, node, resultExpr, comprehensionItems):
		return '( ' + self( resultExpr, PRECEDENCE_CONTAINER_ELEMENT ) + '   ' + '   '.join( [ self( c, PRECEDENCE_CONTAINER_ELEMENT )   for c in comprehensionItems ] )  +  ' )'
	
	
	
	# Dictionary literal
	@DMObjectNodeDispatchMethod( Schema.DictKeyValuePair )
	def DictKeyValuePair(self, node, key, value):
		return self( key, PRECEDENCE_CONTAINER_ELEMENT ) + ':' + self( value, PRECEDENCE_CONTAINER_ELEMENT )
	
	@DMObjectNodeDispatchMethod( Schema.DictLiteral )
	def DictLiteral(self, node, values):
		return '{ '  +  ', '.join( [ self( i, PRECEDENCE_CONTAINER_ELEMENT )   for i in values ] )  +  ' }'

	@DMObjectNodeDispatchMethod( Schema.DictComp )
	def DictComp(self, node, resultExpr, comprehensionItems):
		return '{ ' + self( resultExpr, PRECEDENCE_CONTAINER_ELEMENT ) + '   ' + '   '.join( [ self( x, PRECEDENCE_CONTAINER_ELEMENT )   for x in comprehensionItems ] )  +  ' }'



	# Set literal
	@DMObjectNodeDispatchMethod( Schema.SetLiteral )
	def SetLiteral(self, node, values):
		return '{ '  +  ', '.join( [ self( i, PRECEDENCE_CONTAINER_ELEMENT )   for i in values ] )  +  ' }'

	@DMObjectNodeDispatchMethod( Schema.SetComp )
	def SetComp(self, node, resultExpr, comprehensionItems):
		return '{ ' + self( resultExpr, PRECEDENCE_CONTAINER_ELEMENT ) + '   ' + '   '.join( [ self( x, PRECEDENCE_CONTAINER_ELEMENT )   for x in comprehensionItems ] )  +  ' }'



	# Yield expression and yield atom
	@DMObjectNodeDispatchMethod( Schema.YieldExpr )
	def YieldExpr(self, node, value):
		if value is not None:
			return '(yield ' + self( value, PRECEDENCE_CONTAINER_YIELDEXPR ) + ')'
		else:
			return '(yield)'
		
	
	
	# Attribute ref
	@DMObjectNodeDispatchMethod( Schema.AttributeRef )
	def AttributeRef(self, node, target, name):
		if target.isInstanceOf( Schema.IntLiteral ):
			return '(' + self( target, PRECEDENCE_CONTAINER_ATTRIBUTEREFTARGET ) + ').' + name
		else:
			return self( target, PRECEDENCE_CONTAINER_ATTRIBUTEREFTARGET ) + '.' + name

	

	# Subscript
	@DMObjectNodeDispatchMethod( Schema.SubscriptSlice )
	def SubscriptSlice(self, node, lower, upper):
		txt = lambda x:  self( x, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )   if x is not None   else ''
		return txt( lower ) + ':' + txt( upper )

	@DMObjectNodeDispatchMethod( Schema.SubscriptLongSlice )
	def SubscriptLongSlice(self, node, lower, upper, stride):
		txt = lambda x:  self( x, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )   if x is not None   else ''
		return txt( lower ) + ':' + txt( upper ) + ':' + txt( stride )
	
	@DMObjectNodeDispatchMethod( Schema.SubscriptEllipsis )
	def SubscriptEllipsis(self, node):
		return '...'

	@DMObjectNodeDispatchMethod( Schema.SubscriptTuple )
	def SubscriptTuple(self, node, values):
		return '(' + self._tupleElements( values ) + ')'

	@DMObjectNodeDispatchMethod( Schema.Subscript )
	def Subscript(self, node, target, index):
		if index.isInstanceOf( Schema.SubscriptTuple ):
			indexSrc = self._tupleElements( index['values'] )
		else:
			indexSrc = self( index, PRECEDENCE_CONTAINER_SUBSCRIPTINDEX )
		return self( target, PRECEDENCE_CONTAINER_SUBSCRIPTTARGET ) + '[' + indexSrc + ']'
	

	
	# Call	
	@DMObjectNodeDispatchMethod( Schema.CallKWArg )
	def CallKWArg(self, node, name, value):
		return name + '=' + self( value, PRECEDENCE_CONTAINER_CALLARG )
	
	@DMObjectNodeDispatchMethod( Schema.CallArgList )
	def CallArgList(self, node, value):
		return '*' + self( value, PRECEDENCE_CONTAINER_CALLARG )
	
	@DMObjectNodeDispatchMethod( Schema.CallKWArgList )
	def CallKWArgList(self, node, value):
		return '**' + self( value, PRECEDENCE_CONTAINER_CALLARG )
	
	@DMObjectNodeDispatchMethod( Schema.Call )
	def Call(self, node, target, args):
		return self( target, PRECEDENCE_CONTAINER_CALLTARGET ) + '( ' + ', '.join( [ self( a, PRECEDENCE_CONTAINER_CALLARG )   for a in args ] ) + ' )'
	
	
	
	# Operators
	def _prefixOp(self, node, x, op):
		p = nodePrecedence[node]
		return op + self( x, p )

	def _binOp(self, node, x, y, op):
		p = nodePrecedence[node]
		if rightAssociative[node]:
			return self( x, p - 1 )  +  op  +  self( y, p )
		else:
			return self( x, p  )  +  op  +  self( y, p - 1 )
	
	def _cmpOp(self, node, y, op):
		p = nodePrecedence[node]
		return op + self( y, p )
	
	@DMObjectNodeDispatchMethod( Schema.Pow )
	def Pow(self, node, x, y):
		return self._binOp( node, x, y, ' ** ' )
	
	
	@DMObjectNodeDispatchMethod( Schema.Invert )
	def Invert(self, node, x):
		return self._prefixOp( node, x, '~' )
	
	@DMObjectNodeDispatchMethod( Schema.Negate )
	def Negate(self, node, x):
		return self._prefixOp( node, x, '-' )
	
	@DMObjectNodeDispatchMethod( Schema.Pos )
	def Pos(self, node, x):
		return self._prefixOp( node, x, '+' )
	
	
	@DMObjectNodeDispatchMethod( Schema.Mul )
	def Mul(self, node, x, y):
		return self._binOp( node, x, y, ' * ' )
	
	@DMObjectNodeDispatchMethod( Schema.Div )
	def Div(self, node, x, y):
		return self._binOp( node, x, y, ' / ' )
	
	@DMObjectNodeDispatchMethod( Schema.Mod )
	def Mod(self, node, x, y):
		return self._binOp( node, x, y, ' % ' )
	
	@DMObjectNodeDispatchMethod( Schema.Add )
	def Add(self, node, x, y):
		return self._binOp( node, x, y, ' + ' )
	
	@DMObjectNodeDispatchMethod( Schema.Sub )
	def Sub(self, node, x, y):
		return self._binOp( node, x, y, ' - ' )
	
	
	@DMObjectNodeDispatchMethod( Schema.LShift )
	def LShift(self, node, x, y):
		return self._binOp( node, x, y, ' << ' )
	
	@DMObjectNodeDispatchMethod( Schema.RShift )
	def RShift(self, node, x, y):
		return self._binOp( node, x, y, ' >> ' )
	
	
	@DMObjectNodeDispatchMethod( Schema.BitAnd )
	def BitAnd(self, node, x, y):
		return self._binOp( node, x, y, ' & ' )
	
	@DMObjectNodeDispatchMethod( Schema.BitXor )
	def BitXor(self, node, x, y):
		return self._binOp( node, x, y, ' ^ ' )
	
	@DMObjectNodeDispatchMethod( Schema.BitOr )
	def BitOr(self, node, x, y):
		return self._binOp( node, x, y, ' | ' )
	

	@DMObjectNodeDispatchMethod( Schema.Cmp )
	def Cmp(self, node, x, ops):
		return self( x, PRECEDENCE_CMP )  +  ''.join( [ self( op, PRECEDENCE_CMP )   for op in ops ] )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpLte )
	def CmpOpLte(self, node, y):
		return self._cmpOp( node, y, ' <= ' )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpLt )
	def CmpOpLt(self, node, y):
		return self._cmpOp( node, y, ' < ' )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpGte )
	def CmpOpGte(self, node, y):
		return self._cmpOp( node, y, ' >= ' )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpGt )
	def CmpOpGt(self, node, y):
		return self._cmpOp( node, y, ' > ' )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpEq )
	def CmpOpEq(self, node, y):
		return self._cmpOp( node, y, ' == ' )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpNeq )
	def CmpOpNeq(self, node, y):
		return self._cmpOp( node, y, ' != ' )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpIsNot )
	def CmpOpIsNot(self, node, y):
		return self._cmpOp( node, y, ' is not ' )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpIs )
	def CmpOpIs(self, node, y):
		return self._cmpOp( node, y, ' is ' )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpNotIn )
	def CmpOpNotIn(self, node, y):
		return self._cmpOp( node, y, ' not in ' )
	
	@DMObjectNodeDispatchMethod( Schema.CmpOpIn )
	def CmpOpIn(self, node, y):
		return self._cmpOp( node, y, ' in ' )
	
	

	
	@DMObjectNodeDispatchMethod( Schema.NotTest )
	def NotTest(self, node, x):
		return self._prefixOp( node, x, 'not ' )
	
	@DMObjectNodeDispatchMethod( Schema.AndTest )
	def AndTest(self, node, x, y):
		return self._binOp( node, x, y, ' and ' )
	
	@DMObjectNodeDispatchMethod( Schema.OrTest )
	def OrTest(self, node, x, y):
		return self._binOp( node, x, y, ' or ' )
	
	
	
	
	# Parameters	
	@DMObjectNodeDispatchMethod( Schema.SimpleParam )
	def SimpleParam(self, node, name):
		return name
	
	@DMObjectNodeDispatchMethod( Schema.TupleParam )
	def TupleParam(self, node, params, paramsTrailingSeparator):
		return '(' + ', '.join( [ self( p, PRECEDENCE_NONE )   for p in params ] ) + ( ','   if paramsTrailingSeparator is not None   else '' ) + ')'

	@DMObjectNodeDispatchMethod( Schema.DefaultValueParam )
	def DefaultValueParam(self, node, param, defaultValue):
		return self( param, PRECEDENCE_NONE )  +  '='  +  self( defaultValue, PRECEDENCE_NONE )
	
	@DMObjectNodeDispatchMethod( Schema.ParamList )
	def ParamList(self, node, name):
		return '*'  +  name
	
	@DMObjectNodeDispatchMethod( Schema.KWParamList )
	def KWParamList(self, node, name):
		return '**'  +  name
	
	
	
	# Lambda expression
	@DMObjectNodeDispatchMethod( Schema.LambdaExpr )
	def LambdaExpr(self, node, params, expr):
		return 'lambda '  +  ', '.join( [ self( p, PRECEDENCE_NONE )   for p in params ] )  +  ': '  +  self( expr, PRECEDENCE_CONTAINER_LAMBDAEXPR )
	
	
	
	# Conditional expression
	@DMObjectNodeDispatchMethod( Schema.ConditionalExpr )
	def ConditionalExpr(self, node, condition, expr, elseExpr):
		return self( expr, PRECEDENCE_CONTAINER_CONDITIONALEXPR )  +  '   if '  +  self( condition, PRECEDENCE_CONTAINER_CONDITIONALEXPR )  +  '   else '  +  self( elseExpr, PRECEDENCE_CONTAINER_CONDITIONALEXPR )

	
	
	
	# Quote and Unquote
	@DMObjectNodeDispatchMethod( Schema.Quote )
	def Quote(self, node, value):
		raise ValueError, 'Python2CodeGenerator does not support quote expressions; a Python2ModuleCodeGenerator must be used'
	
	@DMObjectNodeDispatchMethod( Schema.Unquote )
	def Unquote(self, node, value):
		raise ValueError, 'Python2CodeGenerator does not support unquote expressions; a Python2ModuleCodeGenerator must be used'
	
	
	
	
	# Embedded object
	@DMObjectNodeDispatchMethod( Schema.EmbeddedObjectLiteral )
	def EmbeddedObjectLiteral(self, node, embeddedValue):
		raise ValueError, 'Python2CodeGenerator does not support embedded object literals; a Python2ModuleCodeGenerator must be used'


	@DMObjectNodeDispatchMethod( Schema.EmbeddedObjectExpr )
	def EmbeddedObjectExpr(self, node, embeddedValue):
		raise ValueError, 'Python2CodeGenerator does not support embedded object expressions; a Python2ModuleCodeGenerator must be used'
	
	
	@DMObjectNodeDispatchMethod( Schema.EmbeddedObjectStmt )
	def EmbeddedObjectStmt (self, node, embeddedValue):
		raise ValueError, 'Python2CodeGenerator does not support embedded object statements; a Python2ModuleCodeGenerator must be used'
		
	
	
	# Expression statement
	@DMObjectNodeDispatchMethod( Schema.ExprStmt )
	def ExprStmt(self, node, expr):
		return Line( self( expr, PRECEDENCE_STMT ),   node )
	
	
	# Expression statement
	@DMObjectNodeDispatchMethod( Schema.UnparsedStmt )
	def UnparsedStmt(self, node, value):
		raise Python2CodeGeneratorUnparsedError
	
	
	# Assert statement
	@DMObjectNodeDispatchMethod( Schema.AssertStmt )
	def AssertStmt(self, node, condition, fail):
		return Line( 'assert '  +  self( condition, PRECEDENCE_STMT )  +  ( ', ' + self( fail, PRECEDENCE_STMT )   if fail is not None   else  '' ),   node )
	
	
	# Assignment statement
	@DMObjectNodeDispatchMethod( Schema.AssignStmt )
	def AssignStmt(self, node, targets, value):
		return Line( ''.join( [ self( t, PRECEDENCE_STMT ) + ' = '   for t in targets ] )  +  self( value, PRECEDENCE_STMT ),   node )
	
	
	# Augmented assignment statement
	@DMObjectNodeDispatchMethod( Schema.AugAssignStmt )
	def AugAssignStmt(self, node, op, target, value):
		return Line( self( target, PRECEDENCE_STMT )  +  ' '  +  op  +  ' '  +  self( value, PRECEDENCE_STMT ),   node )
	
	
	# Pass statement
	@DMObjectNodeDispatchMethod( Schema.PassStmt )
	def PassStmt(self, node):
		return Line( 'pass',   node )
	
	
	# Del statement
	@DMObjectNodeDispatchMethod( Schema.DelStmt )
	def DelStmt(self, node, target):
		return Line( 'del '  +  self( target, PRECEDENCE_STMT ),   node )
	
	
	# Return statement
	@DMObjectNodeDispatchMethod( Schema.ReturnStmt )
	def ReturnStmt(self, node, value):
		if value is not None:
			return Line( 'return '  +  self( value, PRECEDENCE_STMT ),   node )
		else:
			return Line( 'return',  node )
	
	
	# Yield statement
	@DMObjectNodeDispatchMethod( Schema.YieldStmt )
	def YieldStmt(self, node, value):
		if value is not None:
			return Line( 'yield '  +  self( value, PRECEDENCE_STMT ),   node )
		else:
			return Line( 'yield',  node )
	
	
	# Raise statement
	@DMObjectNodeDispatchMethod( Schema.RaiseStmt )
	def RaiseStmt(self, node, excType, excValue, traceback):
		params = ', '.join( [ self( x, PRECEDENCE_STMT )   for x in excType, excValue, traceback   if x is not None ] )
		if params != '':
			return Line( 'raise ' + params,   node )
		else:
			return Line( 'raise',   node )
	
	
	# Break statement
	@DMObjectNodeDispatchMethod( Schema.BreakStmt )
	def BreakStmt(self, node):
		return Line( 'break',   node )
	
	
	# Continue statement
	@DMObjectNodeDispatchMethod( Schema.ContinueStmt )
	def ContinueStmt(self, node):
		return Line( 'continue',   node )
	
	
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
		return Line( 'import '  +  ', '.join( [ self( x, PRECEDENCE_STMT )   for x in modules ] ),   node )
	
	@DMObjectNodeDispatchMethod( Schema.FromImportStmt )
	def FromImportStmt(self, node, module, imports):
		return Line( 'from ' + self( module, PRECEDENCE_STMT ) + ' import ' + ', '.join( [ self( x, PRECEDENCE_STMT )   for x in imports ] ),   node )
	
	@DMObjectNodeDispatchMethod( Schema.FromImportAllStmt )
	def FromImportAllStmt(self, node, module):
		return Line( 'from ' + self( module, PRECEDENCE_STMT ) + ' import *',   node )
	
	
	# Global statement
	@DMObjectNodeDispatchMethod( Schema.GlobalVar )
	def GlobalVar(self, node, name):
		return name
	
	@DMObjectNodeDispatchMethod( Schema.GlobalStmt )
	def GlobalStmt(self, node, vars):
		return Line( 'global '  +  ', '.join( [ self( x, PRECEDENCE_STMT )   for x in vars ] ),   node )
	
	
	# Exec statement
	@DMObjectNodeDispatchMethod( Schema.ExecStmt )
	def ExecStmt(self, node, source, globals, locals):
		txt = 'exec '  +  self( source, PRECEDENCE_STMT )
		if globals is not None:
			txt += ' in '  +  self( globals, PRECEDENCE_STMT )
		if locals is not None:
			txt += ', '  +  self( locals, PRECEDENCE_STMT )
		return Line( txt,   node )
	
	
	# Print statement
	@DMObjectNodeDispatchMethod( Schema.PrintStmt )
	def PrintStmt(self, node, destination, values):
		txt = 'print'
		if destination is not None:
			txt += ' >> %s'  %  self( destination, PRECEDENCE_STMT )
		if len( values ) > 0:
			if destination is not None:
				txt += ','
			txt += ' '
			txt += ', '.join( [ self( v, PRECEDENCE_STMT )   for v in values ] )
		return Line( txt,   node )
	
	

	def _indentedSuite(self, headerLine, suite):
		return Block( [ headerLine ]  +  [ self( stmt ).indent()   for stmt in suite ] )

	def _elseSuite(self, node, suite):
		if suite is not None:
			return self._indentedSuite( Line( 'else:', node ),  suite )
		else:
			return _emptyBlock
			
	def _finallySuite(self, node, suite):
		if suite is not None:
			return self._indentedSuite( Line( 'finally:', node ),  suite )
		else:
			return _emptyBlock
			
	
	
	# If statement
	@DMObjectNodeDispatchMethod( Schema.IfStmt )
	def IfStmt(self, node, condition, suite, elifBlocks, elseSuite):
		lines = self._indentedSuite( Line( 'if '  +  self( condition, PRECEDENCE_STMT ) + ':', node ),  suite )
		for b in elifBlocks:
			lines += self( b, PRECEDENCE_STMT )
		lines += self._elseSuite( node, elseSuite )
		return lines
	

	# Elif block
	@DMObjectNodeDispatchMethod( Schema.ElifBlock )
	def ElifBlock(self, node, condition, suite):
		return self._indentedSuite( Line( 'elif '  +  self( condition, PRECEDENCE_STMT ) + ':', node ),  suite )
	

	# While statement
	@DMObjectNodeDispatchMethod( Schema.WhileStmt )
	def WhileStmt(self, node, condition, suite, elseSuite):
		return self._indentedSuite( Line( 'while '  +  self( condition, PRECEDENCE_STMT ) + ':', node ),  suite )  +  self._elseSuite( node, elseSuite )
	

	# For statement
	@DMObjectNodeDispatchMethod( Schema.ForStmt )
	def ForStmt(self, node, target, source, suite, elseSuite):
		return self._indentedSuite( Line( 'for '  +  self( target, PRECEDENCE_STMT )  +  ' in '  +  self( source, PRECEDENCE_STMT ) + ':', node ),  suite )  +  self._elseSuite( node, elseSuite )
	

	# Try statement
	@DMObjectNodeDispatchMethod( Schema.TryStmt )
	def TryStmt(self, node, suite, exceptBlocks, elseSuite, finallySuite):
		lines = self._indentedSuite( Line( 'try:', node ),  suite )
		for b in exceptBlocks:
			lines += self( b, PRECEDENCE_STMT )
		lines += self._elseSuite( node, elseSuite )
		lines += self._finallySuite( node, finallySuite )
		return lines
	

	# Except statement
	@DMObjectNodeDispatchMethod( Schema.ExceptBlock )
	def ExceptBlock(self, node, exception, target, suite):
		txt = 'except'
		if exception is not None:
			txt += ' ' + self( exception, PRECEDENCE_STMT )
		if target is not None:
			txt += ', ' + self( target, PRECEDENCE_STMT )
		return self._indentedSuite( Line( txt + ':', node ),  suite )
	

	# With statement
	@DMObjectNodeDispatchMethod( Schema.WithContext )
	def WithContext(self, node, expr, target):
		return self( expr, PRECEDENCE_STMT )  +  ( ( ' as ' + self( target, PRECEDENCE_STMT ) )   if target is not None   else   '' )

	@DMObjectNodeDispatchMethod( Schema.WithStmt )
	def WithStmt(self, node, contexts, suite):
		return self._indentedSuite( Line( 'with '  +  ', '.join( [ self( ctx )   for ctx in contexts ] )  +  ':',   node ),   suite )
	
	
	# Decorator
	@DMObjectNodeDispatchMethod( Schema.Decorator )
	def Decorator(self, node, name, args):
		text = '@' + name
		if args is not None:
			text += '( ' + ', '.join( [ self( a, PRECEDENCE_STMT )   for a in args ] ) + ' )'
		return Line( text, node )
	
	
	# Def statement
	@DMObjectNodeDispatchMethod( Schema.DefStmt )
	def DefStmt(self, node, decorators, name, params, suite):
		decos = Block( [ self( d )   for d in decorators ] )
		return decos + self._indentedSuite( Line( 'def '  +  name  +  '('  +  ', '.join( [ self( p, PRECEDENCE_STMT )   for p in params ] )  +  '):' ,   node ),   suite )
	

	# Class statement
	@DMObjectNodeDispatchMethod( Schema.ClassStmt )
	def ClassStmt(self, node, decorators, name, bases, suite):
		decos = Block( [ self( d )   for d in decorators ] )

		text = 'class '  +  name
		if bases is not None:
			text += ' ('  +  ', '.join( [ self( h, PRECEDENCE_STMT )   for h in bases ] )  +  ')'
		clsStmt = text  +  ':'
		
		return decos + self._indentedSuite( Line( clsStmt,  node ),   suite )
	
	
	
	# Indented block
	@DMObjectNodeDispatchMethod( Schema.IndentedBlock )
	def IndentedBlock(self, node, suite):
		if self._bErrorChecking:
			raise Python2CodeGeneratorIndentationError, 'Indentation error'
		return Block( [ self( stmt )   for stmt in suite ] ).indent()
	
	
	
	# Comment statement
	@DMObjectNodeDispatchMethod( Schema.CommentStmt )
	def CommentStmt(self, node, comment):
		return Line( '#' + comment,  node )

	
	# Module
	@DMObjectNodeDispatchMethod( Schema.PythonModule )
	def PythonModule(self, node, suite):
		return Block( [ self( stmt )   for stmt in suite ] )

	
	# Suite
	@DMObjectNodeDispatchMethod( Schema.PythonSuite )
	def PythonSuite(self, node, suite):
		return Block( [ self( stmt )   for stmt in suite ] )

	
	# Expression
	@DMObjectNodeDispatchMethod( Schema.PythonExpression )
	def PythonExpression(self, node, expr):
		if expr is None:
			return 'None'
		else:
			return self( expr )

	
	# Target
	@DMObjectNodeDispatchMethod( Schema.PythonTarget )
	def PythonTarget(self, node, target):
		if expr is None:
			return 'None'
		else:
			return self( target )

		

_runtime_resourceMap_Name = '__larch_resourceMap__'
_runtime_DMList_Name = '__larch_DMList__'






#
#
# HACK - to get around the fact that we cannot just put normal objects into the document model
#
#

_cgHelperSchema = DMSchema( 'Python2CGHelper', 'pych', 'LarchCore.Languages.Python2.CodeGeneratorHelper', 0 )

_FactoryWrapper = _cgHelperSchema.newClass( '_FactoryWrapper', Schema.Node, [ 'embeddedFactory' ] )



class _Factory (object):
	def generateCode(self, codeGen):
		raise NotImplementedError, 'abstract'


class _Deferred (_Factory):
	def __init__(self, fn):
		self.__fn = fn


	def generateCode(self, codeGen):
		return codeGen( self.__fn( codeGen ) )


class _Guard (_Factory):
	def __init__(self, beginFn, content, endFn):
		self.__beginFn = beginFn
		self.__content = content
		self.__endFn = endFn


	def generateCode(self, codeGen):
		self.__beginFn( codeGen )
		result = codeGen( self.__content )
		self.__endFn( codeGen )
		return result



class Python2ModuleCodeGenerator (Python2CodeGenerator):
	def __init__(self, module, filename, bErrorChecking=True):
		super( Python2ModuleCodeGenerator, self ).__init__( filename, bErrorChecking )
		
		try:
			self._resourceMap = getattr( module, _runtime_resourceMap_Name )
		except AttributeError:
			self._resourceMap = []
			setattr( module, _runtime_resourceMap_Name, self._resourceMap )
			
		setattr( module, _runtime_DMList_Name, DMList )
		
		self._resourceValueIdToIndex = {}
		for i, x in enumerate( self._resourceMap ):
			self._resourceValueIdToIndex[id(x)] = i

		self._module = module



	@property
	def module(self):
		return self._module


	def _quotedNode(self, node):
		if node is None:
			return 'None'
		elif isinstance( node, str )  or  isinstance( node, unicode ):
			return repr( node )
		elif isinstance( node, DMObject ):
			if node.isInstanceOf( Schema.Unquote ):
				return '(' + self( node ) + ')'
			else:
				cls = node.getDMObjectClass()

				astMapExpr = self._embeddedValueSrc( cls )

				args = []
				for i, field in enumerate( cls.getFields() ):
					value = node.get( i )
					if value is not None:
						args.append( ( field.getName(), self._quotedNode( value ) ) )
				return astMapExpr + '(' + ', '.join( [ '%s=%s' % ( k,v )   for k,v in args ] ) + ')'
		elif isinstance( node, DMList ):
			return _runtime_DMList_Name + '( [' + ', '.join( [ self._quotedNode( v )   for v in node ] ) + '] )'
		elif isinstance( node, DMEmbeddedObject )  or  isinstance( node, DMEmbeddedIsolatedObject ):
			return self._embeddedValueSrc( deepcopy( node ) )
		else:
			raise TypeError, 'Cannot quote a %s'  %  type( node )
		
		
		


	# Quote and Unquote
	@DMObjectNodeDispatchMethod( Schema.Quote )
	def Quote(self, node, value):
		return self._quotedNode( value )
	
	
	
	@DMObjectNodeDispatchMethod( Schema.Unquote )
	def Unquote(self, node, value):
		return self( value )



	# Embedded object literal
	@DMObjectNodeDispatchMethod( Schema.EmbeddedObjectLiteral )
	def EmbeddedObjectLiteral(self, node, embeddedValue):
		value = embeddedValue.getValue()

		# Use the object as a value
		return self._embeddedValueSrc( value )



	# Embedded object expression
	@DMObjectNodeDispatchMethod( Schema.EmbeddedObjectExpr )
	def EmbeddedObjectExpr(self, node, embeddedValue):
		value = embeddedValue.getValue()
	
		
		# Try to use the __py_compile_visit__ method
		try:
			visitFn = value.__py_compile_visit__
		except AttributeError:
			pass
		else:
			# Got a 'visit' function - invoke to allow object to initialise resources, etc
			visitFn( self )
			
		
		# Try to use the __py_evalmodel__ method
		try:
			modelFn = value.__py_evalmodel__
		except AttributeError:
			pass
		else:
			# Got a 'model' function - invoke to create AST nodes, then convert them to code
			model = modelFn( self )
			return self( model )

		
		# Try to use the __py_eval__ method
		try:
			evalFn = value.__py_eval__
		except AttributeError:
			pass
		else:
			evalFnSource = self._embeddedValueSrc( evalFn )
			selfSource = self._embeddedValueSrc( self )
			return evalFnSource + '( globals(), locals(), ' + selfSource + ' )'
		
		
		# Use the object as a value
		return self._embeddedValueSrc( value )
		
	
	
	# Embedded object statement
	@DMObjectNodeDispatchMethod( Schema.EmbeddedObjectStmt )
	def EmbeddedObjectStmt(self, node, embeddedValue):
		value = embeddedValue.getValue()
	
		
		# Try to use the __py_compile_visit__ method
		try:
			visitFn = value.__py_compile_visit__
		except AttributeError:
			pass
		else:
			# Got a 'visit' function - invoke to allow object to initialise resources, etc
			visitFn( self )
		
		
		# Try to use the __py_execmodel__ method
		try:
			modelFn = value.__py_execmodel__
		except AttributeError:
			pass
		else:
			# Got a 'model' function - invoke to create AST nodes, then convert them to code
			model = modelFn( self )
			return self( model )



	# Try to use the __py_exec__ method
		try:
			execFn = value.__py_exec__
		except AttributeError:
			pass
		else:
			execFnSource = self._embeddedValueSrc( execFn )
			selfSource = self._embeddedValueSrc( self )
			return Line( execFnSource + '( globals(), locals(), ' + selfSource + ' )',  node )

			
		# Try to use the __py_localnames__ and __py_localvalues__ method pair
		try:
			attrNamesFn = value.__py_localnames__
			attrValuesFn = value.__py_localvalues__
		except AttributeError:
			pass
		else:
			# Get the attribute name list
			names = attrNamesFn()
			selfSource = self._embeddedValueSrc( self )
			valuesCallSource = self._embeddedValueSrc( attrValuesFn ) + '( globals(), locals(), ' + selfSource + ' )'
			if isinstance( names, str )  or  isinstance( names, unicode ):
				return Line( names + ' = ' + valuesCallSource,   node )
			else:
				return Line( ', '.join( names ) + ',' + ' = ' + valuesCallSource,   node )

		
		# Get the object as a value
		return Line( self._embeddedValueSrc( value ),   node )



	# AST factory
	@DMObjectNodeDispatchMethod( _FactoryWrapper )
	def _FactoryWrapper(self, node, embeddedFactory):
		factory = embeddedFactory.getValue()
		return factory.generateCode( self )



	def _embeddedValueIndex(self, resourceValue):
		rscId = id( resourceValue )
		try:
			index = self._resourceValueIdToIndex[rscId]
		except KeyError:
			index = len( self._resourceMap )
			self._resourceMap.append( resourceValue )
			self._resourceValueIdToIndex[rscId] = index
		return index
	
	def _embeddedValueSrc(self, resourceValue):
		index = self._embeddedValueIndex( resourceValue )
		return _runtime_resourceMap_Name + '[%d]'  %  ( index, )

	def embeddedValue(self, resourceValue):
		index = self._embeddedValueIndex( resourceValue )
		targetAST = Schema.Load( name=_runtime_resourceMap_Name )
		indexAST= Schema.IntLiteral( format='decimal', numType='int', value=str( index ) )
		return Schema.Subscript( target=targetAST, index=indexAST )







	def deferred(self, fn):
		return self._factory( _Deferred( fn ) )

	def guard(self, beginFn, content, endFn):
		return self._factory( _Guard( beginFn, content, endFn ) )


	def _factory(self, fac):
		return _FactoryWrapper( embeddedFactory=DMNode.embed( fac, False ) )







def compileForEvaluation(pythonExpression, filename):
	return Python2CodeGenerator( filename ).compileForEvaluation( pythonExpression )


def compileForExecution(pythonCode, filename):
	return Python2CodeGenerator( filename ).compileForExecution( pythonCode )


def compileForExecutionAndEvaluation(pythonCode, filename):
	return Python2CodeGenerator( filename ).compileForExecutionAndEvaluation( pythonCode )

				
				
				
def compileForModuleExecution(module, pythonCode, filename):
	return Python2ModuleCodeGenerator( module, filename ).compileForExecution( pythonCode )


def compileForModuleExecutionAndEvaluation(module, pythonCode, filename):
	return Python2ModuleCodeGenerator( module, filename ).compileForExecutionAndEvaluation( pythonCode )

				
				
				
from BritefuryJ.DocModel import DMIOReader
import unittest

class TestCase_Python2CodeGenerator (unittest.TestCase):
	def _testSX(self, sx, expected):
		sx = '{ py=LarchCore.Languages.Python2<5> : ' + sx + ' }'
		data = DMIOReader.readFromString( sx )
		
		gen = Python2CodeGenerator( '<test>' )
		result = str( gen( data ) )
		
		if result != expected:
			print 'UNEXPECTED RESULT'
			print 'INPUT:'
			print data
			print 'EXPECTED:'
			print expected.replace( '\n', '\\n' ) + '<<'
			print 'RESULT:'
			print result.replace( '\n', '\\n' ) + '<<'
			
		self.assert_( result == expected )
		
		
	def _testGenSX(self, gen, sx, expected):
		sx = '{ py=LarchCore.Languages.Python2<5> : ' + sx + ' }'
		data = DMIOReader.readFromString( sx )
		
		result = str( gen( data ) )
		
		if result != expected:
			print 'UNEXPECTED RESULT'
			print 'EXPECTED:'
			print expected.replace( '\n', '\\n' ) + '<<'
			print 'RESULT:'
			print result.replace( '\n', '\\n' ) + '<<'
			
		self.assert_( result == expected )
		
		
	def _binOpTest(self, sxOp, expectedOp):
		self._testSX( '(py %s x=(py Load name=a) y=(py Load name=b))'  %  sxOp,  'a %s b'  %  expectedOp )
		
		
	def test_BlankLine(self):
		self._testSX( '(py BlankLine)', '' )
		
		
	def test_UNPARSED(self):
		self.assertRaises( Python2CodeGeneratorUnparsedError, lambda: self._testSX( '(py UNPARSED value=Test)', '' ) )
		
		
	def test_StringLiteral(self):
		self._testSX( '(py StringLiteral format=ascii quotation=single value="Hi there")', '\'Hi there\'' )
		self._testSX( '(py StringLiteral format=unicode quotation=single value="Hi there")', 'u\'Hi there\'' )
		self._testSX( '(py StringLiteral format=bytes quotation=single value="Hi there")', 'b\'Hi there\'' )


	def test_IntLiteral(self):
		self._testSX( '(py IntLiteral format=decimal numType=int value=123)', '123' )
		self._testSX( '(py IntLiteral format=decimal numType=long value=123)', '123L' )
		self._testSX( '(py IntLiteral format=hex numType=int value=1a4)', '0x1a4' )
		self._testSX( '(py IntLiteral format=hex numType=long value=1a4)', '0x1a4L' )
		self._testSX( '(py IntLiteral format=bin numType=int value=101)', '0b101' )
		self._testSX( '(py IntLiteral format=bin numType=long value=101)', '0b101L' )
		self._testSX( '(py IntLiteral format=oct numType=int value=123)', '0o123' )
		self._testSX( '(py IntLiteral format=oct numType=long value=123)', '0o123L' )
		self.assertRaises( Python2CodeGeneratorInvalidFormatError, lambda: self._testSX( '(py IntLiteral format=foo numType=long value=1a4)', '' ) )
		self.assertRaises( Python2CodeGeneratorInvalidFormatError, lambda: self._testSX( '(py IntLiteral format=hex numType=foo value=1a4)', '' ) )
		# Ensure that 0 prefix results in decimal
		self._testSX( '(py IntLiteral format=decimal numType=int value=00123)', '123' )
		self._testSX( '(py IntLiteral format=decimal numType=long value=00123)', '123L' )



	def test_FloatLiteral(self):
		self._testSX( '(py FloatLiteral value=123.0)', '123.0' )
		
		
	def test_ImaginaryLiteral(self):
		self._testSX( '(py ImaginaryLiteral value=123j)', '123j' )
		
		
	def test_SingleTarget(self):
		self._testSX( '(py SingleTarget name=a)', 'a' )
		
		
	def test_TupleTarget(self):
		self._testSX( '(py TupleTarget targets=[])', '()' )
		self._testSX( '(py TupleTarget targets=[(py SingleTarget name=a)])', '(a,)' )
		self._testSX( '(py TupleTarget targets=[(py SingleTarget name=a) (py SingleTarget name=b) (py SingleTarget name=c)])', '(a, b, c)' )
		self._testSX( '(py ListTarget targets=[(py SingleTarget name=a) (py TupleTarget targets=[(py SingleTarget name=a) (py SingleTarget name=b) (py SingleTarget name=c)])])', '[ a, (a, b, c) ]' )
		
		
	def test_ListTarget(self):
		self._testSX( '(py ListTarget targets=[(py SingleTarget name=a) (py SingleTarget name=b) (py SingleTarget name=c)])', '[ a, b, c ]' )
		
		
	def test_Load(self):
		self._testSX( '(py Load name=a)', 'a' )
		
		
	def test_TupleLiteral(self):
		self._testSX( '(py TupleLiteral values=[])', '()' )
		self._testSX( '(py TupleLiteral values=[(py Load name=a)])', '(a,)' )
		self._testSX( '(py TupleLiteral values=[(py Load name=a) (py Load name=b) (py Load name=c)])', '(a, b, c)' )
		self._testSX( '(py ListLiteral values=[(py Load name=a) (py TupleLiteral values=[(py Load name=a) (py Load name=b) (py Load name=c)])])', '[ a, (a, b, c) ]' )
		
		
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


	def test_DictComp(self):
		self._testSX( '(py DictComp resultExpr=(py DictKeyValuePair key=(py Load name=a) value=(py Load name=b)) '+\
		              'comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs)) (py ComprehensionIf condition=(py Load name=a))])', '{ a:b   for a in xs   if a }' )


	def test_SetLiteral(self):
		self._testSX( '(py SetLiteral values=[(py Load name=a) (py Load name=c)])', '{ a, c }' )


	def test_SetComp(self):
		self._testSX( '(py SetComp resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs)) (py ComprehensionIf condition=(py Load name=a))])', '{ a   for a in xs   if a }' )


	def test_YieldExpr(self):
		self._testSX( '(py YieldExpr value=(py Load name=a))', '(yield a)' )
		self._testSX( '(py YieldExpr value=`null`)', '(yield)' )

		
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
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptTuple values=[(py Load name=a) (py SubscriptSlice lower=(py Load name=b) upper=(py Load name=c))]))', 'a[a, b:c]' )
		self._testSX( '(py Subscript target=(py Load name=a) index=(py SubscriptTuple values=[(py Load name=b) (py SubscriptTuple values=[(py Load name=c) (py Load name=d)])]))', 'a[b, (c, d)]' )
		
		
	def test_call(self):
		self._testSX( '(py Call target=(py Load name=x) args=[(py Load name=a) (py Load name=b) (py CallKWArg name=c value=(py Load name=d)) (py CallKWArg name=e value=(py Load name=f)) (py CallArgList value=(py Load name=g)) (py CallKWArgList value=(py Load name=h))])', 'x( a, b, c=d, e=f, *g, **h )' )
		
		
	def test_operators(self):
		self._binOpTest( 'Pow', '**' )
		self._testSX( '(py Invert x=(py Load name=a))', '~a' )
		self._testSX( '(py Negate x=(py Load name=a))', '-a' )
		self._testSX( '(py Pos x=(py Load name=a))', '+a' )
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
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpLte y=(py Load name=b))])',  'a <= b' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpLt y=(py Load name=b))])',  'a < b' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpGte y=(py Load name=b))])',  'a >= b' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpGt y=(py Load name=b))])',  'a > b' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpEq y=(py Load name=b))])',  'a == b' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpNeq y=(py Load name=b))])',  'a != b' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpIsNot y=(py Load name=b))])',  'a is not b' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpIs y=(py Load name=b))])',  'a is b' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpNotIn y=(py Load name=b))])',  'a not in b' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpIn y=(py Load name=b))])',  'a in b' )
		self._testSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpLt y=(py Load name=b)) (py CmpOpGt y=(py Load name=c))])',  'a < b > c' )
		self._testSX( '(py NotTest x=(py Load name=a))', 'not a' )
		self._binOpTest( 'AndTest', 'and' )
		self._binOpTest( 'OrTest', 'or' )
		self._testSX( '(py Mul x=(py Load name=a) y=(py Add x=(py Load name=b) y=(py Load name=c)))', 'a * (b + c)' )
		self._testSX( '(py Add x=(py Load name=a) y=(py Mul x=(py Load name=b) y=(py Load name=c)))', 'a + b * c' )
		
		
	def test_LambdaExpr(self):
		self._testSX( '(py LambdaExpr params=[(py SimpleParam name=a) (py SimpleParam name=b) '+\
				'(py TupleParam params=[(py SimpleParam name=i) (py SimpleParam name=j)]) '
			      '(py DefaultValueParam param=(py SimpleParam name=c) defaultValue=(py Load name=d))'+\
			      '(py DefaultValueParam param=(py SimpleParam name=e) defaultValue=(py Load name=f)) '+\
			      '(py DefaultValueParam param=(py TupleParam params=[(py SimpleParam name=k) (py SimpleParam name=l)]) defaultValue=(py Load name=x)) '+\
			      '(py ParamList name=g) (py KWParamList name=h)] expr=(py Load name=a))',
		              'lambda a, b, (i, j), c=d, e=f, (k, l)=x, *g, **h: a' )
	
		
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
		self._testSX( '(py ReturnStmt value=`null`)', 'return' )

		
	def test_YieldStmt(self):
		self._testSX( '(py YieldStmt value=(py Load name=a))', 'yield a' )
		self._testSX( '(py YieldStmt value=`null`)', 'yield' )

		
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
		self._testSX( '(py IfStmt condition=(py Load name=bA) suite=[(py ExprStmt expr=(py Load name=b))] elifBlocks=[])', 'if bA:\n\tb\n' )
		self._testSX( '(py IfStmt condition=(py Load name=bA) suite=[(py ExprStmt expr=(py Load name=b))] elifBlocks=[] elseSuite=[(py ExprStmt expr=(py Load name=c))])', 'if bA:\n\tb\nelse:\n\tc\n' )
		self._testSX( '(py IfStmt condition=(py Load name=bA) suite=[(py ExprStmt expr=(py Load name=b))] elifBlocks=[(py ElifBlock condition=(py Load name=bA) suite=[(py ExprStmt expr=(py Load name=b))])] elseSuite=[(py ExprStmt expr=(py Load name=c))])',
			      'if bA:\n\tb\nelif bA:\n\tb\nelse:\n\tc\n' )


	def test_ElifBlock(self):
		self._testSX( '(py ElifBlock condition=(py Load name=bA) suite=[(py ExprStmt expr=(py Load name=b))])', 'elif bA:\n\tb\n' )


	def test_WhileStmt(self):
		self._testSX( '(py WhileStmt condition=(py Load name=bA) suite=[(py ExprStmt expr=(py Load name=b))])', 'while bA:\n\tb\n' )
		self._testSX( '(py WhileStmt condition=(py Load name=bA) suite=[(py ExprStmt expr=(py Load name=b))] elseSuite=[(py ExprStmt expr=(py Load name=c))])', 'while bA:\n\tb\nelse:\n\tc\n' )


	def test_ForStmt(self):
		self._testSX( '(py ForStmt target=(py Load name=a) source=(py Load name=b) suite=[(py ExprStmt expr=(py Load name=c))])', 'for a in b:\n\tc\n' )
		self._testSX( '(py ForStmt target=(py Load name=a) source=(py Load name=b) suite=[(py ExprStmt expr=(py Load name=c))] elseSuite=[(py ExprStmt expr=(py Load name=d))])', 'for a in b:\n\tc\nelse:\n\td\n' )


	def test_TryStmt(self):
		self._testSX( '(py TryStmt suite=[(py ExprStmt expr=(py Load name=b))] exceptBlocks=[])', 'try:\n\tb\n' )
		self._testSX( '(py TryStmt suite=[(py ExprStmt expr=(py Load name=b))] exceptBlocks=[] elseSuite=[(py ExprStmt expr=(py Load name=d))])', 'try:\n\tb\nelse:\n\td\n' )
		self._testSX( '(py TryStmt suite=[(py ExprStmt expr=(py Load name=b))] exceptBlocks=[] elseSuite=[(py ExprStmt expr=(py Load name=d))] finallySuite=[(py ExprStmt expr=(py Load name=e))])', 'try:\n\tb\nelse:\n\td\nfinally:\n\te\n' )
		self._testSX( '(py TryStmt suite=[(py ExprStmt expr=(py Load name=b))] exceptBlocks=[(py ExceptBlock exception=`null` target=`null` suite=[(py ExprStmt expr=(py Load name=b))])] elseSuite=[(py ExprStmt expr=(py Load name=d))] finallySuite=[(py ExprStmt expr=(py Load name=e))])',
			      'try:\n\tb\nexcept:\n\tb\nelse:\n\td\nfinally:\n\te\n' )


	def test_exceptBlock(self):
		self._testSX( '(py ExceptBlock exception=`null` target=`null` suite=[(py ExprStmt expr=(py Load name=b))])', 'except:\n\tb\n' )
		self._testSX( '(py ExceptBlock exception=(py Load name=a) target=`null` suite=[(py ExprStmt expr=(py Load name=b))])', 'except a:\n\tb\n' )
		self._testSX( '(py ExceptBlock exception=(py Load name=a) target=(py Load name=x) suite=[(py ExprStmt expr=(py Load name=b))])', 'except a, x:\n\tb\n' )


	def test_withStmt(self):
		self._testSX( '(py WithStmt contexts=[(py WithContext expr=(py Load name=a) target=`null`)] suite=[(py ExprStmt expr=(py Load name=b))])', 'with a:\n\tb\n' )
		self._testSX( '(py WithStmt contexts=[(py WithContext expr=(py Load name=a) target=(py SingleTarget name=x))] suite=[(py ExprStmt expr=(py Load name=b))])', 'with a as x:\n\tb\n' )
		self._testSX( '(py WithStmt contexts=[(py WithContext expr=(py Load name=a) target=(py SingleTarget name=x)) (py WithContext expr=(py Load name=b) target=(py SingleTarget name=y)) '+\
		              '(py WithContext expr=(py Load name=c) target=`null`)] suite=[(py ExprStmt expr=(py Load name=b))])', 'with a as x, b as y, c:\n\tb\n' )


	def test_decorator(self):
		self._testSX( '(py Decorator name=myDeco args=`null`)', '@myDeco' )
		self._testSX( '(py Decorator name=myDeco args=[(py Load name=a) (py Load name=b)])', '@myDeco( a, b )' )

		
	def test_defStmt(self):
		self._testSX( '(py DefStmt decorators=[] name=myFunc params=[(py SimpleParam name=a) (py DefaultValueParam param=(py SimpleParam name=b) defaultValue=(py Load name=c)) '+\
			      '(py ParamList name=d) (py KWParamList name=e)] suite=[(py ExprStmt expr=(py Load name=b))])', 'def myFunc(a, b=c, *d, **e):\n\tb\n' )
		self._testSX( '(py DefStmt decorators=[(py Decorator name=myDeco args=`null`)] name=myFunc params=[(py SimpleParam name=a) ' +\
			      '(py DefaultValueParam param=(py SimpleParam name=b) defaultValue=(py Load name=c)) (py ParamList name=d) (py KWParamList name=e)] suite=[(py ExprStmt expr=(py Load name=b))])',
			      '@myDeco\ndef myFunc(a, b=c, *d, **e):\n\tb\n' )
		self._testSX( '(py DefStmt decorators=[(py Decorator name=myDeco args=`null`) (py Decorator name=myDeco args=[(py Load name=a) (py Load name=b)])] '+\
			      'name=myFunc params=[(py SimpleParam name=a) (py DefaultValueParam param=(py SimpleParam name=b) defaultValue=(py Load name=c)) (py ParamList name=d) '+\
			      '(py KWParamList name=e)] suite=[(py ExprStmt expr=(py Load name=b))])', '@myDeco\n@myDeco( a, b )\ndef myFunc(a, b=c, *d, **e):\n\tb\n' )


	def test_classStmt(self):
		self._testSX( '(py ClassStmt decorators=[] name=A bases=`null` suite=[(py ExprStmt expr=(py Load name=b))])', 'class A:\n\tb\n' )
		self._testSX( '(py ClassStmt decorators=[] name=A bases=[(py Load name=object)] suite=[(py ExprStmt expr=(py Load name=b))])', 'class A (object):\n\tb\n' )
		self._testSX( '(py ClassStmt decorators=[] name=A bases=[(py Load name=object) (py Load name=Q)] suite=[(py ExprStmt expr=(py Load name=b))])', 'class A (object, Q):\n\tb\n' )
		self._testSX( '(py ClassStmt decorators=[(py Decorator name=f)] name=A bases=[(py Load name=object)] suite=[(py ExprStmt expr=(py Load name=b))])', '@f\nclass A (object):\n\tb\n' )


	def test_IndentedBlock(self):
		self._testGenSX( Python2CodeGenerator( '<test>', False ), '(py IndentedBlock suite=[(py ExprStmt expr=(py Load name=b))])', '\tb\n' )
		self.assertRaises( Python2CodeGeneratorIndentationError, lambda: self._testSX( '(py IndentedBlock suite=[(py ExprStmt expr=(py Load name=b))])', '' ) )
		

	def test_CommentStmt(self):
		self._testSX( '(py CommentStmt comment=HelloWorld)', '#HelloWorld' )
		
		
		
