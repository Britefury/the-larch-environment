##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from copy import deepcopy

import _ast

from BritefuryJ.DocModel import DMObject, DMList, DMEmbeddedObject, DMEmbeddedIsolatedObject

from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod, dmObjectNodeMethodDispatch

from LarchCore.Languages.Python25 import Schema
from LarchCore.Languages.Python25.PythonEditor.Precedence import *



# Jython 2.5 bugfix
if _ast.Attribute is _ast.AugAssign:
	print 'Applying Jython 2.5.2 _ast module bugfix (getting correct value for _ast.Attribute)'
	expr = compile( 'a.x', '<bugfix>', 'eval', _ast.PyCF_ONLY_AST )
	attr = expr.body
	_ast.Attribute = type( attr )



class Python25ASTGeneratorError (Exception):
	pass


class Python25ASTGeneratorUnparsedError (Python25ASTGeneratorError):
	pass


class Python25ASTGeneratorIndentationError (Python25ASTGeneratorError):
	pass


class Python25ASTGeneratorInvalidFormatError (Python25ASTGeneratorError):
	pass


class Python25ASTGeneratorInvalidStructureError (Python25ASTGeneratorError):
	pass



_cmpOpTable = { Schema.CmpOpLt : _ast.Lt,
		Schema.CmpOpLte : _ast.LtE,
		Schema.CmpOpEq : _ast.Eq,
		Schema.CmpOpNeq : _ast.NotEq,
		Schema.CmpOpGt : _ast.Gt,
		Schema.CmpOpGte : _ast.GtE,
		Schema.CmpOpIs : _ast.Is,
		Schema.CmpOpIsNot : _ast.IsNot,
		Schema.CmpOpIn : _ast.In,
		Schema.CmpOpNotIn : _ast.NotIn
		}



class Python25ASTGenerator (object):
	__dispatch_num_args__ = 2


	def __init__(self, filename, bErrorChecking=True):
		super( Python25ASTGenerator, self ).__init__()
		self._filename = filename
		self._bErrorChecking = bErrorChecking


	def compileForEvaluation(self, pythonExpression):
		ast = self( pythonExpression )
		return compile( source, self._filename, 'eval' )


	def compileForExecution(self, pythonModule):
		ast = self( pythonModule )
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
			execAST = self( execModule )
			evalAST = self( evalExpr )

			execCode = compile( execAST, self._filename, 'exec' )
			evalCode = compile( evalAST, self._filename, 'eval' )

			return execCode, evalCode
		else:
			return self.compileForExecution( pythonModule ),  None



	# Callable - use document model node method dispatch mechanism
	def __call__(self, x, lineno, ctx=_ast.Load()):
		if x is not None:
			ast = dmObjectNodeMethodDispatch( self, x, lineno, ctx )
			if ast is not None  and  hasattr( ast, 'lineno' ):
				ast.lineno = lineno
			return ast
		else:
			return None



	# Misc
	@DMObjectNodeDispatchMethod( Schema.BlankLine )
	def BlankLine(self, lineno, ctx, node):
		return None


	@DMObjectNodeDispatchMethod( Schema.UNPARSED )
	def UNPARSED(self, lineno, ctx, node, value):
		raise Python25ASTGeneratorUnparsedError


	# String literal
	@DMObjectNodeDispatchMethod( Schema.StringLiteral )
	def StringLiteral(self, lineno, ctx, node, format, quotation, value):
		return _ast.Str( value )


	# Integer literal
	@DMObjectNodeDispatchMethod( Schema.IntLiteral )
	def IntLiteral(self, lineno, ctx, node, format, numType, value):
		if numType == 'int':
			if format == 'decimal':
				return  _ast.Num( int( value ) )
			elif format == 'hex':
				return  _ast.Num( int( value, 16 ) )
			else:
				raise Python25ASTGeneratorInvalidFormatError, 'invalid integer literal format'
		elif numType == 'long':
			if format == 'decimal':
				return  _ast.Num( long( value ) )
			elif format == 'hex':
				return  _ast.Num( long( value, 16 ) )
			else:
				raise Python25ASTGeneratorInvalidFormatError, 'invalid integer literal format'
		else:
			raise Python25ASTGeneratorInvalidFormatError, 'invalid integer literal type'


	# Float literal
	@DMObjectNodeDispatchMethod( Schema.FloatLiteral )
	def FloatLiteral(self, lineno, ctx, node, value):
		return _ast.Num( float( value ) )

	
	# Imaginary literal
	@DMObjectNodeDispatchMethod( Schema.ImaginaryLiteral )
	def ImaginaryLiteral(self, lineno, ctx, node, value):
		return _ast.Num( complex( value ) )


	# Target
	@DMObjectNodeDispatchMethod( Schema.SingleTarget )
	def SingleTarget(self, lineno, ctx, node, name):
		return _ast.Name( name, ctx )


	@DMObjectNodeDispatchMethod( Schema.TupleTarget )
	def TupleTarget(self, lineno, ctx, node, targets):
		return _ast.Tuple( [ self( t, lineno, ctx )   for t in targets ],  ctx )


	@DMObjectNodeDispatchMethod( Schema.ListTarget )
	def ListTarget(self, lineno, ctx, node, targets):
		return _ast.List( [ self( t, lineno, ctx )   for t in targets ],  ctx )



	# Variable reference
	@DMObjectNodeDispatchMethod( Schema.Load )
	def Load(self, lineno, ctx, node, name):
		return _ast.Name( name, ctx )


	# Tuple literal
	@DMObjectNodeDispatchMethod( Schema.TupleLiteral )
	def TupleLiteral(self, lineno, ctx, node, values):
		return _ast.Tuple( [ self( v, lineno, ctx )   for v in values ],  ctx )

	
	# List literal
	@DMObjectNodeDispatchMethod( Schema.ListLiteral )
	def ListLiteral(self, lineno, ctx, node, values):
		return _ast.List( [ self( v, lineno, ctx )   for v in values ],  ctx )



	# List comprehension / generator expression
	def _comprehensionGenerators(self, lineno, ctx, comprehensionItems):
		generators = []
		gen = None
		for x in comprehensionItems:
			if x.isInstanceOf( Schema.ComprehensionFor ):
				target = self( x['target'], lineno, _ast.Store() )
				iter = self( x['source'], lineno, ctx )
				gen = _ast.comprehension( target, iter, [] )
				generators.append( gen )
			elif x.isInstanceOf( Schema.ComprehensionIf ):
				if gen is None:
					raise Python25ASTGeneratorInvalidStructureError, 'ComprehensionIf must come after ComprehensionFor'
				expr = self( x['condition'], lineno, ctx )
				gen.ifs.append( expr )
			else:
				raise Python25ASTGeneratorInvalidStructureError, 'List comprehensions and generator expressions can only contain comprehension items'
		return generators

			
	@DMObjectNodeDispatchMethod( Schema.ComprehensionFor )
	def ComprehensionFor(self, lineno, ctx, node, target, source):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned ComprehensionFor'


	@DMObjectNodeDispatchMethod( Schema.ComprehensionIf )
	def ComprehensionIf(self, lineno, ctx, node, condition):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned ComprehensionIf'


	@DMObjectNodeDispatchMethod( Schema.ListComp )
	def ListComp(self, lineno, ctx, node, resultExpr, comprehensionItems):
		expr = self( resultExpr, lineno, ctx )
		generators = self._comprehensionGenerators( lineno, ctx, comprehensionItems )
		return _ast.ListComp( expr, generators )

	
	@DMObjectNodeDispatchMethod( Schema.GeneratorExpr )
	def GeneratorExpr(self, lineno, ctx, node, resultExpr, comprehensionItems):
		expr = self( resultExpr, lineno, ctx )
		generators = self._comprehensionGenerators( lineno, ctx, comprehensionItems )
		return _ast.GeneratorExp( expr, generators )



	# Dictionary literal
	@DMObjectNodeDispatchMethod( Schema.DictKeyValuePair )
	def DictKeyValuePair(self, lineno, ctx, node, key, value):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned DictKeyValuePair'

	@DMObjectNodeDispatchMethod( Schema.DictLiteral )
	def DictLiteral(self, lineno, ctx, node, values):
		ks = []
		vs = []
		for p in values:
			if p.isInstanceOf( Schema.DictKeyValuePair ):
				ks.append( self( p['key'], lineno, ctx ) )
				vs.append( self( p['value'], lineno, ctx ) )
		return _ast.Dict( ks, vs )



	# Yield expression and yield atom
	@DMObjectNodeDispatchMethod( Schema.YieldExpr )
	def YieldExpr(self, lineno, ctx, node, value):
		if value is not None:
			return _ast.Yield( self( value, lineno, ctx ) )
		else:
			return _ast.Yield( None )



	# Attribute ref
	@DMObjectNodeDispatchMethod( Schema.AttributeRef )
	def AttributeRef(self, lineno, ctx, node, target, name):
		return _ast.Attribute( self( target, lineno, _load ), name, ctx )



	# Subscript
	def _processIndex(self, index, lineno, ctx):
		if index.isInstanceOf( Schema.SubscriptSlice )  or  index.isInstanceOf( Schema.SubscriptLongSlice )  or  \
		   index.isInstanceOf( Schema.SubscriptEllipsis )  or  index.isInstanceOf( Schema.SubscriptTuple ):
			return self( index, lineno, _load )
		else:
			return _ast.Index( self( index, lineno, _load ) )

	@DMObjectNodeDispatchMethod( Schema.SubscriptSlice )
	def SubscriptSlice(self, lineno, ctx, node, lower, upper):
		return _ast.Slice( self( lower, lineno, _load ), self( upper, lineno, _load ), None )

	@DMObjectNodeDispatchMethod( Schema.SubscriptLongSlice )
	def SubscriptLongSlice(self, lineno, ctx, node, lower, upper, stride):
		return _ast.Slice( self( lower, lineno, _load ), self( upper, lineno, _load ), self( stride, lineno, _load ) )

	@DMObjectNodeDispatchMethod( Schema.SubscriptEllipsis )
	def SubscriptEllipsis(self, lineno, ctx, node):
		return _ast.Ellipsis()

	@DMObjectNodeDispatchMethod( Schema.SubscriptTuple )
	def SubscriptTuple(self, lineno, ctx, node, values):
		return _ast.ExtSlice( [ self._processIndex( v, lineno, _load )   for v in values ] )

	@DMObjectNodeDispatchMethod( Schema.Subscript )
	def Subscript(self, lineno, ctx, node, target, index):
		value = self( target, lineno, _load )
		slice = self._processIndex( index, lineno, _load )
		return _ast.Subscript( value, slice, ctx )



	# Call
	@DMObjectNodeDispatchMethod( Schema.CallKWArg )
	def CallKWArg(self, lineno, ctx, node, name, value):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned CallKWArg'

	@DMObjectNodeDispatchMethod( Schema.CallArgList )
	def CallArgList(self, lineno, ctx, node, value):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned CallArgList'

	@DMObjectNodeDispatchMethod( Schema.CallKWArgList )
	def CallKWArgList(self, lineno, ctx, node, value):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned CallKWArgList'

	@DMObjectNodeDispatchMethod( Schema.Call )
	def Call(self, lineno, ctx, node, target, args):
		func = self( target, lineno, _load )
		argvs = []
		kwargvs = []
		arglist = None
		kwarglist = None
		for a in args:
			if a.isInstanceOf( Schema.CallKWArg ):
				if arglist is not None  or  kwarglist is not None:
					raise Python25ASTGeneratorInvalidStructureError, 'keyword argument after argument list or keyword argument list'
				kwargvs.append( _ast.keyword( a['name'], self( a['value'], lineno, _load ) ) )
			elif a.isInstanceOf( Schema.CallArgList ):
				if kwarglist is not None:
					raise Python25ASTGeneratorInvalidStructureError, 'argument list after keyword argument list'
				arglist = self( a['value'], lineno, _load )
			elif a.isInstanceOf( Schema.CallKWArgList ):
				kwarglist = self( a['value'], lineno, _load )
			else:
				if len( kwargvs ) > 0  or  arglist is not None  or  kwarglist is not None:
					raise Python25ASTGeneratorInvalidStructureError, 'argument after keyword arguments, argument list or keyword argument list'
				argvs.append( self( a, lineno, _load ) )
		return _ast.Call( func, argvs, kwargvs, arglist, kwarglist )



	# Operators
	def _prefixOp(self, x, opType, lineno):
		right = self( x, lineno, _load )
		op = opType()
		return _ast.UnaryOp( op, right )

	def _binOp(self, x, y, opType, lineno):
		left = self( x, lineno, _load )
		right = self( y, lineno, _load )
		op = opType()
		return _ast.BinOp( left, op, right )

	@DMObjectNodeDispatchMethod( Schema.Pow )
	def Pow(self, lineno, ctx, node, x, y):
		return self._binOp( x, y, _ast.Pow, lineno )


	@DMObjectNodeDispatchMethod( Schema.Invert )
	def Invert(self, lineno, ctx, node, x):
		return self._prefixOp( x, _ast.Invert, lineno )

	@DMObjectNodeDispatchMethod( Schema.Negate )
	def Negate(self, lineno, ctx, node, x):
		return self._prefixOp( x, _ast.USub, lineno )

	@DMObjectNodeDispatchMethod( Schema.Pos )
	def Pos(self, lineno, ctx, node, x):
		return self._prefixOp( x, _ast.UAdd, lineno )


	@DMObjectNodeDispatchMethod( Schema.Mul )
	def Mul(self, lineno, ctx, node, x, y):
		return self._binOp( x, y, _ast.Mult, lineno )

	@DMObjectNodeDispatchMethod( Schema.Div )
	def Div(self, lineno, ctx, node, x, y):
		return self._binOp( x, y, _ast.Div, lineno )

	@DMObjectNodeDispatchMethod( Schema.Mod )
	def Mod(self, lineno, ctx, node, x, y):
		return self._binOp( x, y, _ast.Mod, lineno )

	@DMObjectNodeDispatchMethod( Schema.Add )
	def Add(self, lineno, ctx, node, x, y):
		return self._binOp( x, y, _ast.Add, lineno )

	@DMObjectNodeDispatchMethod( Schema.Sub )
	def Sub(self, lineno, ctx, node, x, y):
		return self._binOp( x, y, _ast.Sub, lineno )


	@DMObjectNodeDispatchMethod( Schema.LShift )
	def LShift(self, lineno, ctx, node, x, y):
		return self._binOp( x, y, _ast.LShift, lineno )

	@DMObjectNodeDispatchMethod( Schema.RShift )
	def RShift(self, lineno, ctx, node, x, y):
		return self._binOp( x, y, _ast.RShift, lineno )


	@DMObjectNodeDispatchMethod( Schema.BitAnd )
	def BitAnd(self, lineno, ctx, node, x, y):
		return self._binOp( x, y, _ast.BitAnd, lineno )

	@DMObjectNodeDispatchMethod( Schema.BitXor )
	def BitXor(self, lineno, ctx, node, x, y):
		return self._binOp( x, y, _ast.BitXor, lineno )

	@DMObjectNodeDispatchMethod( Schema.BitOr )
	def BitOr(self, lineno, ctx, node, x, y):
		return self._binOp( x, y, _ast.BitOr, lineno )


	@DMObjectNodeDispatchMethod( Schema.Cmp )
	def Cmp(self, lineno, ctx, node, x, ops):
		left = self( x, lineno, _load )
		opTypes = []
		comparators = []
		for op in ops:
			opClass = op.getDMObjectClass()
			opTypes.append( _cmpOpTable[opClass]() )
			comparators.append( self( op['y'], lineno, _load ) )
		return _ast.Compare( left, opTypes, comparators )

	@DMObjectNodeDispatchMethod( Schema.CmpOpLte )
	def CmpOpLte(self, lineno, ctx, node, y):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned CmpOpLte'

	@DMObjectNodeDispatchMethod( Schema.CmpOpLt )
	def CmpOpLt(self, lineno, ctx, node, y):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned CmpOpLt'

	@DMObjectNodeDispatchMethod( Schema.CmpOpGte )
	def CmpOpGte(self, lineno, ctx, node, y):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned CmpOpGte'

	@DMObjectNodeDispatchMethod( Schema.CmpOpGt )
	def CmpOpGt(self, lineno, ctx, node, y):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned CmpOpGt'

	@DMObjectNodeDispatchMethod( Schema.CmpOpEq )
	def CmpOpEq(self, lineno, ctx, node, y):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned CmpOpEq'

	@DMObjectNodeDispatchMethod( Schema.CmpOpNeq )
	def CmpOpNeq(self, lineno, ctx, node, y):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned CmpOpNeq'

	@DMObjectNodeDispatchMethod( Schema.CmpOpIsNot )
	def CmpOpIsNot(self, lineno, ctx, node, y):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned CmpOpIsNot'

	@DMObjectNodeDispatchMethod( Schema.CmpOpIs )
	def CmpOpIs(self, lineno, ctx, node, y):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned CmpOpIs'

	@DMObjectNodeDispatchMethod( Schema.CmpOpNotIn )
	def CmpOpNotIn(self, lineno, ctx, node, y):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned CmpOpNotIn'

	@DMObjectNodeDispatchMethod( Schema.CmpOpIn )
	def CmpOpIn(self, lineno, ctx, node, y):
		raise Python25ASTGeneratorInvalidStructureError, 'Cannot process orphaned CmpOpIn'




	@DMObjectNodeDispatchMethod( Schema.NotTest )
	def NotTest(self, lineno, ctx, node, x):
		return self._prefixOp( x, _ast.Not, lineno )

	def _joinBoolOp(self, lineno, ctx, x, nodeCls):
		if x.isInstanceOf( nodeCls ):
			return self._joinBoolOp( lineno, ctx, x['x'], nodeCls )  +  self._joinBoolOp( lineno, ctx, x['y'], nodeCls )
		else:
			return [ x ]

	@DMObjectNodeDispatchMethod( Schema.AndTest )
	def AndTest(self, lineno, ctx, node, x, y):
		xs = [ self( a, lineno, _load )   for a in self._joinBoolOp( lineno, ctx, node, Schema.AndTest ) ]
		return _ast.BoolOp( _ast.And(), xs )

	@DMObjectNodeDispatchMethod( Schema.OrTest )
	def OrTest(self, lineno, ctx, node, x, y):
		xs = [ self( a, lineno, _load )   for a in self._joinBoolOp( lineno, ctx, node, Schema.OrTest ) ]
		return _ast.BoolOp( _ast.Or(), xs )






from BritefuryJ.DocModel import DMIOReader
import unittest

def _astToString(x):
	if isinstance(x, _ast.AST):
		name = x.__class__.__name__
		fieldNames = x._fields
		data = ', '.join( [ '%s=%s' % ( fieldName, _astToString( getattr( x, fieldName ) ) )    for fieldName in fieldNames ] )
		return '%s(%s)' % ( name, data )
	elif isinstance(x, list):
		return '[' + ', '.join( [ _astToString( a )   for a in x ] ) + ']'
	else:
		return str( x )



_load = _ast.Load()
_store = _ast.Store()



class TestCase_Python25ASTGenerator (unittest.TestCase):
	def _testSXAST(self, sx, expected, ctx=_load):
		sx = '{ py=LarchCore.Languages.Python25<5> : ' + sx + ' }'
		data = DMIOReader.readFromString( sx )

		gen = Python25ASTGenerator( '<test>' )
		result = gen( data, 0, ctx )

		resultStr = _astToString( result )
		expectedStr = _astToString( expected )

		if resultStr != expectedStr:
			print 'UNEXPECTED RESULT'
			print 'INPUT:'
			print data
			print 'EXPECTED:'
			print expectedStr
			print 'RESULT:'
			print resultStr

		self.assert_( resultStr == expectedStr )


	def _testGenSXAST(self, gen, sx, expected, mode):
		sx = '{ py=LarchCore.Languages.Python25<5> : ' + sx + ' }'
		data = DMIOReader.readFromString( sx )

		result = gen( data, 0, ctx )

		resultStr = _astToString( result )
		expectedStr = _astToString( expected )

		if resultStr != expectedStr:
			print 'UNEXPECTED RESULT'
			print 'EXPECTED:'
			print expectedStr
			print 'RESULT:'
			print resultStr

		self.assert_( resultStr == expectedStr )


	def _testSX(self, sx, expected, mode):
		if isinstance( expected, str )  or  isinstance( expected, unicode ):
			expected = compile( expected, '<test>', mode, _ast.PyCF_ONLY_AST )

		self._testSXAST( sx, expected )


	def _testGenSX(self, gen, sx, expected, mode):
		if isinstance( expected, str )  or  isinstance( expected, unicode ):
			expected = compile( expected, '<test>', mode, _ast.PyCF_ONLY_AST )

		self._testGenSXAST( gen, sx, expected )


	def _testExprSX(self, sx, expected):
		expectedAST = compile( expected, '<test>', 'eval', _ast.PyCF_ONLY_AST ).body

		self._testSXAST( sx, expectedAST )


	def _testExprGenSX(self, gen, sx, expected):
		expectedAST = compile( expected, '<test>', 'eval', _ast.PyCF_ONLY_AST ).body

		self._testGenSX( gen, sx, expectedAST, 'eval' )


	def _testExecSX(self, sx, expected):
		self._testSX( sx, expected, 'exec' )


	def _testExecGenSX(self, gen, sx, expected):
		self._testGenSX( gen, sx, expected, 'exec' )


	def _binOpTest(self, sxOp, expectedOp):
		self._testExprSX( '(py %s x=(py Load name=a) y=(py Load name=b))'  %  sxOp,  'a %s b'  %  expectedOp )




	def test_BlankLine(self):
		self._testSXAST( '(py BlankLine)', None )


	def test_UNPARSED(self):
		self.assertRaises( Python25ASTGeneratorUnparsedError, lambda: self._testSXAST( '(py UNPARSED value=Test)', '' ) )


	def test_StringLiteral(self):
		self._testExprSX( '(py StringLiteral format=ascii quotation=single value="Hi there")', "'Hi there'" )


	def test_IntLiteral(self):
		self._testExprSX( '(py IntLiteral format=decimal numType=int value=123)', '123' )
		self._testExprSX( '(py IntLiteral format=hex numType=int value=1a4)', '0x1a4' )
		self._testExprSX( '(py IntLiteral format=decimal numType=long value=123)', '123L' )
		self._testExprSX( '(py IntLiteral format=hex numType=long value=1a4)', '0x1a4L' )
		self.assertRaises( Python25ASTGeneratorInvalidFormatError, lambda: self._testSXAST( '(py IntLiteral format=foo numType=long value=1a4)', None ) )
		self.assertRaises( Python25ASTGeneratorInvalidFormatError, lambda: self._testSXAST( '(py IntLiteral format=hex numType=foo value=1a4)', None ) )


	def test_FloatLiteral(self):
		self._testExprSX( '(py FloatLiteral value=123.0)', '123.0' )


	def test_ImaginaryLiteral(self):
		self._testExprSX( '(py ImaginaryLiteral value=123j)', '123j' )


	def test_SingleTarget(self):
		self._testSXAST( '(py SingleTarget name=a)', _ast.Name( 'a', _store ), _store )


	def test_TupleTarget(self):
		self._testSXAST( '(py TupleTarget targets=[])', _ast.Tuple( [], _store ), _store )
		self._testSXAST( '(py TupleTarget targets=[(py SingleTarget name=a)])', _ast.Tuple( [ _ast.Name( 'a', _store ) ], _store ), _store )
		self._testSXAST( '(py TupleTarget targets=[(py SingleTarget name=a) (py SingleTarget name=b) (py SingleTarget name=c)])',
		              _ast.Tuple( [ _ast.Name( 'a', _store ), _ast.Name( 'b', _store ), _ast.Name( 'c', _store ) ], _store ), _store )
		self._testSXAST( '(py ListTarget targets=[(py SingleTarget name=a) (py TupleTarget targets=[(py SingleTarget name=a) (py SingleTarget name=b) (py SingleTarget name=c)])])',
		              _ast.List( [ _ast.Name( 'a', _store ), _ast.Tuple( [ _ast.Name( 'a', _store ), _ast.Name( 'b', _store ), _ast.Name( 'c', _store ) ], _store ) ], _store ), _store )


	def test_ListTarget(self):
		self._testSXAST( '(py ListTarget targets=[(py SingleTarget name=a) (py SingleTarget name=b) (py SingleTarget name=c)])',
		               _ast.List( [ _ast.Name( 'a', _store ), _ast.Name( 'b', _store ), _ast.Name( 'c', _store ) ], _store ), _store )


	def test_Load(self):
		self._testExprSX( '(py Load name=a)', 'a' )


	def test_TupleLiteral(self):
		self._testExprSX( '(py TupleLiteral values=[])', '()' )
		self._testExprSX( '(py TupleLiteral values=[(py Load name=a)])', '( a, )' )
		self._testExprSX( '(py TupleLiteral values=[(py Load name=a) (py Load name=b) (py Load name=c)])', '( a, b, c )' )
		self._testExprSX( '(py ListLiteral values=[(py Load name=a) (py TupleLiteral values=[(py Load name=a) (py Load name=b) (py Load name=c)])])', '[ a, ( a, b, c ) ]' )


	def test_ListLiteral(self):
		self._testExprSX( '(py ListLiteral values=[(py Load name=a) (py Load name=b) (py Load name=c)])', '[ a, b, c ]' )


	def test_ListComp(self):
		self._testExprSX( '(py ListComp resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs))])', '[ a  for a in xs ]' )
		self._testExprSX( '(py ListComp resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs)) (py ComprehensionIf condition=(py Load name=a))])',
				 '[ a  for a in xs   if a ]' )
		self._testExprSX( '(py ListComp resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs)) (py ComprehensionIf condition=(py Load name=a)) (py ComprehensionIf condition=(py Load name=b))])',
				 '[ a  for a in xs  if a  if b ]' )


	def test_GeneratorExpr(self):
		self._testExprSX( '(py GeneratorExpr resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs))])', '( a  for a in xs )' )
		self._testExprSX( '(py GeneratorExpr resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs)) (py ComprehensionIf condition=(py Load name=a))])',
				  '( a  for a in xs   if a )' )
		self._testExprSX( '(py GeneratorExpr resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs)) (py ComprehensionIf condition=(py Load name=a)) (py ComprehensionIf condition=(py Load name=b))])',
				  '( a  for a in xs  if a  if b )' )



	def test_DictLiteral(self):
		self._testExprSX( '(py DictLiteral values=[(py DictKeyValuePair key=(py Load name=a) value=(py Load name=b)) (py DictKeyValuePair key=(py Load name=c) value=(py Load name=d))])', '{ a:b, c:d }' )


	def test_YieldExpr(self):
		self._testExprSX( '(py YieldExpr value=(py Load name=a))', '(yield a)' )
		self._testExprSX( '(py YieldExpr value=`null`)', '(yield)' )


	def test_AttributeRef(self):
		self._testExprSX( '(py AttributeRef target=(py Load name=a) name=b)', 'a.b' )


	def test_Subscript(self):
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py Load name=b))', 'a[b]' )


	def test_Subscript_Ellipsis(self):
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py SubscriptEllipsis))', 'a[...]' )


	def test_subscript_slice(self):
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py SubscriptSlice lower=(py Load name=a) upper=(py Load name=b)))', 'a[a:b]' )
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py SubscriptSlice lower=(py Load name=a) upper=`null`))', 'a[a:]' )
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py SubscriptSlice lower=`null` upper=(py Load name=b)))', 'a[:b]' )
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py SubscriptSlice lower=`null` upper=`null`))', 'a[:]' )


	def test_subscript_longSlice(self):
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=(py Load name=a) upper=(py Load name=b) stride=(py Load name=c)))', 'a[a:b:c]' )
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=(py Load name=a) upper=(py Load name=b) stride=`null`))', 'a[a:b]' )
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=(py Load name=a) upper=`null` stride=(py Load name=c)))', 'a[a::c]' )
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=(py Load name=a) upper=`null` stride=`null`))', 'a[a:]' )
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=`null` upper=(py Load name=b) stride=(py Load name=c)))', 'a[:b:c]' )
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=`null` upper=(py Load name=b) stride=`null`))', 'a[:b]' )
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=`null` upper=`null` stride=(py Load name=c)))', 'a[::c]' )
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py SubscriptLongSlice lower=`null` upper=`null` stride=`null`))', 'a[:]' )


	def test_subscript_tuple(self):
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py SubscriptTuple values=[(py Load name=a) (py SubscriptSlice lower=(py Load name=b) upper=(py Load name=c))]))', 'a[a, b:c]' )
		self._testExprSX( '(py Subscript target=(py Load name=a) index=(py TupleLiteral values=[(py Load name=b) (py TupleLiteral values=[(py Load name=c) (py Load name=d)])]))', 'a[b, (c,d)]' )
		self._testSXAST( '(py Subscript target=(py Load name=a) index=(py SubscriptTuple values=[(py Load name=b) (py SubscriptTuple values=[(py Load name=c) (py Load name=d)])]))',
			_ast.Subscript( _ast.Name( 'a', _load ), _ast.ExtSlice( [ _ast.Index( _ast.Name( 'b', _load ) ), _ast.ExtSlice( [ _ast.Index( _ast.Name( 'c', _load ) ), _ast.Index( _ast.Name( 'd', _load ) ) ] ) ] ), _load ) )


	def test_call(self):
		self._testExprSX( '(py Call target=(py Load name=x) args=[(py Load name=a) (py Load name=b) (py CallKWArg name=c value=(py Load name=d)) (py CallKWArg name=e value=(py Load name=f)) (py CallArgList value=(py Load name=g)) (py CallKWArgList value=(py Load name=h))])',
				  'x( a, b, c=d, e=f, *g, **h )' )


	def test_operators(self):
		self._binOpTest( 'Pow', '**' )
		self._testExprSX( '(py Invert x=(py Load name=a))', '~a' )
		self._testExprSX( '(py Negate x=(py Load name=a))', '-a' )
		self._testExprSX( '(py Pos x=(py Load name=a))', '+a' )
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
		self._testExprSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpLte y=(py Load name=b))])',  'a <= b' )
		self._testExprSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpLt y=(py Load name=b))])',  'a < b' )
		self._testExprSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpGte y=(py Load name=b))])',  'a >= b' )
		self._testExprSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpGt y=(py Load name=b))])',  'a > b' )
		self._testExprSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpEq y=(py Load name=b))])',  'a == b' )
		self._testExprSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpNeq y=(py Load name=b))])',  'a != b' )
		self._testExprSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpIsNot y=(py Load name=b))])',  'a is not b' )
		self._testExprSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpIs y=(py Load name=b))])',  'a is b' )
		self._testExprSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpNotIn y=(py Load name=b))])',  'a not in b' )
		self._testExprSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpIn y=(py Load name=b))])',  'a in b' )
		self._testExprSX( '(py Cmp x=(py Load name=a) ops=[(py CmpOpLt y=(py Load name=b)) (py CmpOpGt y=(py Load name=c))])',  'a < b > c' )
		self._testExprSX( '(py NotTest x=(py Load name=a))', 'not a' )
		self._binOpTest( 'AndTest', 'and' )
		self._binOpTest( 'OrTest', 'or' )
		self._testExprSX( '(py AndTest x=(py AndTest x=(py AndTest x=(py Load name=a) y=(py Load name=b)) y=(py Load name=c)) y=(py Load name=d))', 'a and b and c and d' )
		self._testExprSX( '(py OrTest x=(py OrTest x=(py OrTest x=(py Load name=a) y=(py Load name=b)) y=(py Load name=c)) y=(py Load name=d))', 'a or b or c or d' )
		self._testExprSX( '(py OrTest x=(py OrTest x=(py AndTest x=(py Load name=a) y=(py Load name=b)) y=(py Load name=c)) y=(py Load name=d))', 'a and b or c or d' )
		self._testExprSX( '(py Mul x=(py Load name=a) y=(py Add x=(py Load name=b) y=(py Load name=c)))', 'a * (b + c)' )
		self._testExprSX( '(py Add x=(py Load name=a) y=(py Mul x=(py Load name=b) y=(py Load name=c)))', 'a + b * c' )


