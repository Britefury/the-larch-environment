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
		ast = dmObjectNodeMethodDispatch( self, x, lineno, ctx )
		if ast is not None:
			ast.lineno = lineno
		return ast



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
	def _testSX(self, sx, expected, ctx=_ast.Load()):
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


	def _testGenSX(self, gen, sx, expected, ctx=_ast.Load()):
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


	def _binOpTest(self, sxOp, expectedOp):
		self._testSX( '(py %s x=(py Load name=a) y=(py Load name=b))'  %  sxOp,  'a %s b'  %  expectedOp )




	def test_BlankLine(self):
		self._testSX( '(py BlankLine)', None )


	def test_UNPARSED(self):
		self.assertRaises( Python25ASTGeneratorUnparsedError, lambda: self._testSX( '(py UNPARSED value=Test)', '' ) )


	def test_StringLiteral(self):
		self._testSX( '(py StringLiteral format=ascii quotation=single value="Hi there")', _ast.Str( 'Hi there' ) )


	def test_IntLiteral(self):
		self._testSX( '(py IntLiteral format=decimal numType=int value=123)', _ast.Num( 123 ) )
		self._testSX( '(py IntLiteral format=hex numType=int value=1a4)', _ast.Num( 0x1a4 ) )
		self._testSX( '(py IntLiteral format=decimal numType=long value=123)', _ast.Num( 123L ) )
		self._testSX( '(py IntLiteral format=hex numType=long value=1a4)', _ast.Num( 0x1a4L ) )
		self.assertRaises( Python25ASTGeneratorInvalidFormatError, lambda: self._testSX( '(py IntLiteral format=foo numType=long value=1a4)', '' ) )
		self.assertRaises( Python25ASTGeneratorInvalidFormatError, lambda: self._testSX( '(py IntLiteral format=hex numType=foo value=1a4)', '' ) )


	def test_FloatLiteral(self):
		self._testSX( '(py FloatLiteral value=123.0)', _ast.Num( 123.0 ) )


	def test_ImaginaryLiteral(self):
		self._testSX( '(py ImaginaryLiteral value=123j)', _ast.Num( 123j ) )


	def test_SingleTarget(self):
		self._testSX( '(py SingleTarget name=a)', _ast.Name( 'a', _store ), _store )


	def test_TupleTarget(self):
		self._testSX( '(py TupleTarget targets=[])', _ast.Tuple( [], _store ), _store )
		self._testSX( '(py TupleTarget targets=[(py SingleTarget name=a)])', _ast.Tuple( [ _ast.Name( 'a', _store ) ], _store ), _store )
		self._testSX( '(py TupleTarget targets=[(py SingleTarget name=a) (py SingleTarget name=b) (py SingleTarget name=c)])',
		              _ast.Tuple( [ _ast.Name( 'a', _store ), _ast.Name( 'b', _store ), _ast.Name( 'c', _store ) ], _store ), _store )
		self._testSX( '(py ListTarget targets=[(py SingleTarget name=a) (py TupleTarget targets=[(py SingleTarget name=a) (py SingleTarget name=b) (py SingleTarget name=c)])])',
		              _ast.List( [ _ast.Name( 'a', _store ), _ast.Tuple( [ _ast.Name( 'a', _store ), _ast.Name( 'b', _store ), _ast.Name( 'c', _store ) ], _store ) ], _store ), _store )


	def test_ListTarget(self):
		self._testSX( '(py ListTarget targets=[(py SingleTarget name=a) (py SingleTarget name=b) (py SingleTarget name=c)])',
		               _ast.List( [ _ast.Name( 'a', _store ), _ast.Name( 'b', _store ), _ast.Name( 'c', _store ) ], _store ), _store )


	def test_Load(self):
		self._testSX( '(py Load name=a)', _ast.Name( 'a', _load ) )


	def test_TupleLiteral(self):
		self._testSX( '(py TupleLiteral values=[])', _ast.Tuple( [], _load ) )
		self._testSX( '(py TupleLiteral values=[(py Load name=a)])', _ast.Tuple( [ _ast.Name( 'a', _load ) ], _load ) )
		self._testSX( '(py TupleLiteral values=[(py Load name=a) (py Load name=b) (py Load name=c)])', _ast.Tuple( [ _ast.Name( 'a', _load ), _ast.Name( 'b', _load ), _ast.Name( 'c', _load ) ], _load ) )
		self._testSX( '(py ListLiteral values=[(py Load name=a) (py TupleLiteral values=[(py Load name=a) (py Load name=b) (py Load name=c)])])',
				_ast.List( [ _ast.Name( 'a', _load ), _ast.Tuple( [ _ast.Name( 'a', _load ), _ast.Name( 'b', _load ), _ast.Name( 'c', _load ) ], _load ) ], _load ) )


	def test_ListLiteral(self):
		self._testSX( '(py ListLiteral values=[(py Load name=a) (py Load name=b) (py Load name=c)])',
				_ast.List( [ _ast.Name( 'a', _load ), _ast.Name( 'b', _load ), _ast.Name( 'c', _load ) ], _load ))


	def test_ListComp(self):
		self._testSX( '(py ListComp resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs))])',
			      _ast.ListComp( _ast.Name( 'a', _load ), [ _ast.comprehension( _ast.Name( 'a', _store ), _ast.Name( 'xs', _load ), [] ) ] ) )
		self._testSX( '(py ListComp resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs)) (py ComprehensionIf condition=(py Load name=a))])',
			      _ast.ListComp( _ast.Name( 'a', _load ), [ _ast.comprehension( _ast.Name( 'a', _store ), _ast.Name( 'xs', _load ), [ _ast.Name( 'a', _load ) ] ) ] ) )
		self._testSX( '(py ListComp resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs)) (py ComprehensionIf condition=(py Load name=a)) (py ComprehensionIf condition=(py Load name=b))])',
			      _ast.ListComp( _ast.Name( 'a', _load ), [ _ast.comprehension( _ast.Name( 'a', _store ), _ast.Name( 'xs', _load ), [ _ast.Name( 'a', _load ), _ast.Name( 'b', _load ) ] ) ] ) )


	def test_GeneratorExpr(self):
		self._testSX( '(py GeneratorExpr resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs))])',
			      _ast.GeneratorExp( _ast.Name( 'a', _load ), [ _ast.comprehension( _ast.Name( 'a', _store ), _ast.Name( 'xs', _load ), [] ) ] ) )
		self._testSX( '(py GeneratorExpr resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs)) (py ComprehensionIf condition=(py Load name=a))])',
			      _ast.GeneratorExp( _ast.Name( 'a', _load ), [ _ast.comprehension( _ast.Name( 'a', _store ), _ast.Name( 'xs', _load ), [ _ast.Name( 'a', _load ) ] ) ] ) )
		self._testSX( '(py GeneratorExpr resultExpr=(py Load name=a) comprehensionItems=[(py ComprehensionFor target=(py SingleTarget name=a) source=(py Load name=xs)) (py ComprehensionIf condition=(py Load name=a)) (py ComprehensionIf condition=(py Load name=b))])',
			      _ast.GeneratorExp( _ast.Name( 'a', _load ), [ _ast.comprehension( _ast.Name( 'a', _store ), _ast.Name( 'xs', _load ), [ _ast.Name( 'a', _load ), _ast.Name( 'b', _load ) ] ) ] ) )



	def test_DictLiteral(self):
		self._testSX( '(py DictLiteral values=[(py DictKeyValuePair key=(py Load name=a) value=(py Load name=b)) (py DictKeyValuePair key=(py Load name=c) value=(py Load name=d))])',
			      _ast.Dict( [ _ast.Name( 'a', _load ), _ast.Name( 'c', _load ) ], [ _ast.Name( 'b', _load ), _ast.Name( 'd', _load ) ] ) )


