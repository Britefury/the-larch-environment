##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import _ast


# Would need to generate an 'oldLambdaExpr' node when necessary



def _getNodeTypeName(node):
	name = node.__class__.__name__
	if '.' in name:
		return name[name.rfind( '.' )+1:]
	else:
		return name


class _Importer (object):
	def __call__(self, node, method=None):
		if method is None:
			name = _getNodeTypeName( node )
			try:
				method = getattr( self, name )
			except AttributeError:
				return [ 'UNPARSED', name ]
		return method( node )


	
def _extractParameters(node):
	numDefaultParams = len( node.defaults )
	numSimpleParams = len( node.args )  -  ( numDefaultParams )
	
	params = [ [ 'simpleParam', name.id ]   for name in node.args[:numSimpleParams] ]
	params.extend( [ [ 'defaultValueParam', name.id, _expr( value ) ]   for name, value in zip( node.args[numSimpleParams:], node.defaults ) ] )
	
	if node.vararg is not None:
		params.append( [ 'paramList', node.vararg ] )
	if node.kwarg is not None:
		params.append( [ 'kwParamList', node.kwarg ] )
	
	return params





class _TargetImporter (_Importer):
	def __call__(self, node, method=None):
		if method is None:
			name = _getNodeTypeName( node )
			try:
				method = getattr( self, name )
			except AttributeError:
				return _expr( node )
		return method( node )

	
	# Targets
	#def AssName(self, node):
		#return [ 'singleTarget', node.id ]
	
	#def AssAttr(self, node):
		#return [ 'attributeRef', _target( node.expr ), node.attr ]
	
	#def AssTuple(self, node):
		#return [ 'tupleTarget' ]  +  [ _target( x )   for x in node.elts ]
	
	#def AssList(self, node):
		#return [ 'listTarget' ]  +  [ _target( x )   for x in node.elts ]

	
	def Name(self, node):
		return [ 'singleTarget', node.id ]
	
	def Attribute(self, node):
		return [ 'attributeRef', _expr( node.value ), node.attr ]
	
	def Tuple(self, node):
		return [ 'tupleTarget' ]  +  [ _target( x )   for x in node.elts ]
	
	def List(self, node):
		return [ 'listTarget' ]  +  [ _target( x )   for x in node.elts ]




	
class _ListCompImporter (_Importer):
	# List comprehension
	def comprehension(self, node):
		return [ [ 'listFor', _target( node.target ), _expr( node.iter ) ] ]  +  [ [ 'listIf', _expr( x ) ]   for x in node.ifs ]
	
	def comprehensionType(self, node):
		return self.comprehension( node )


class _GenExprImporter (_Importer):
	# Generator expression comprehension
	def comprehension(self, node):
		return [ [ 'genFor', _target( node.target ), _expr( node.iter ) ] ]  +  [ [ 'genIf', _expr( x ) ]   for x in node.ifs ]

	def comprehensionType(self, node):
		return self.comprehension( node )


class _LambdaArgsImporter (_Importer):
	# Lambda arguments
	def arguments(self, node):
		return _extractParameters( node )
	
	def argumentsType(self, node):
		return self.arguments( node )

	
class _ImportImporter (_Importer):
	# Import statement
	def alias(self, node):
		if node.asname is None:
			return [ 'moduleImport', node.name ]
		else:
			return [ 'moduleImportAs', node.name, node.asname ]
		

class _ImportFromImporter (_Importer):
	# Import statement
	def alias(self, node):
		if node.asname is None:
			return [ 'moduleContentImport', node.name ]
		else:
			return [ 'moduleContentImportAs', node.name, node.asname ]
		
	
class _ExceptHandlerImporter (_Importer):
	# Import statement
	def excepthandler(self, node):
		return [ 'exceptStmt',     _expr( node.type )   if node.type is not None   else '<nil>',     _target( node.name )   if node.name is not None   else '<nil>',    _flattenedCompound( node.body ) ]
		
	
class _DecoratorImporter (_Importer):
	def Name(self, node):
		return [ 'decoStmt', node.id, '<nil>' ]

	def Call(self, node):
		_starArg = lambda name, x:   [ [ name, _expr( x ) ] ]   if x is not None   else   []
		return [ 'decoStmt', node.func.id, [ _expr( x )   for x in node.args ]  +  [ _expr( x )   for x in node.keywords ]  +  _starArg( 'argList', node.starargs )  +  _starArg( 'kwArgList', node.kwargs ) ]

	def keyword(self, node):
		return [ 'kwArg', node.arg, _expr( node.value ) ]


	
	
_binOpNameTable = { 'Pow' : 'pow',  'Add' : 'add',  'Mult' : 'mul',  'Div' : 'div',  'Mod' : 'mod',  'Add' : 'add',  'Sub' : 'sub',  'LShift' : 'lshift',  'RShift' : 'rshift',  'BitAnd' : 'bitAnd',  'BitXor' : 'bitXor',  'BitOr' : 'bitOr' }	
_unaryOpNameTable = { 'Invert' : 'invert',  'USub' : 'negate',  'UAdd' : 'pos',  'Not' : 'notTest' }	
_boolOpNameTable = { 'And' : 'andTest',  'Or' : 'orTest' }	
_cmpOpTable = { 'Lt' : 'lt',  'LtE' : 'lte',  'Eq' : 'eq',  'NotEq' : 'neq',  'Gt' : 'gt',  'GtE' : 'gte',  'Is' : 'isTest',  'IsNot' : 'isNotTest',  'In' : 'inTest',  'NotIn' : 'notInTest' }
	
	
def _getOpNodeName(op):
	if hasattr( op, 'name' ):
		return op.name()
	else:
		return _getNodeTypeName( op )

	
class _ExprImporter (_Importer):
	def __call__(self, node, method=None):
		if node is None:
			return '<nil>'
		else:
			if method is None:
				name = _getNodeTypeName( node )
				try:
					method = getattr( self, name )
				except AttributeError:
					return [ 'UNPARSED', name ]
			return method( node )

	
	# Expression
	def Expression(self, node):
		return _expr( node.body )
		
		
		
	# Number literal
	def Num(self, node):
		value = node.n
		if isinstance( value, int ):
			return [ 'intLiteral', 'decimal', 'int', repr( value ) ]
		elif isinstance( value, long ):
			return [ 'intLiteral', 'decimal', 'long', repr( value )[:-1] ]
		elif isinstance( value, float ):
			return [ 'floatLiteral', repr( value ) ]
		elif isinstance( value, complex ):
			if value.real == 0.0:
				return [ 'imaginaryLiteral', repr( value.imag ) + 'j' ]
			else:
				return [ 'add', [ 'floatLiteral', repr( value.real ) ], [ 'imaginaryLiteral', repr( value.imag ) + 'j' ] ]
		else:
			print 'Const: could not handle value', value
			raise ValueError
		
	
	# String literal
	def Str(self, node):
		value = node.s
		if isinstance( value, str ):
			return [ 'stringLiteral', 'ascii', 'single', repr( value )[1:-1] ]
		elif isinstance( value, unicode ):
			return [ 'stringLiteral', 'unicode', 'single', repr( value )[2:-1] ]
		else:
			print 'Const: could not handle value', value
			raise ValueError

		
		
	# Variable ref
	def Name(self, node):
		return [ 'var', node.id ]


	# Targets
	#def AssName(self, node):
		#return [ 'singleTarget', node.name ]
	
	#def AssAttr(self, node):
		#return [ 'attributeRef', _expr( node.expr ), node.attrname ]
	
	#def AssTuple(self, node):
		#return [ 'tupleTarget' ]  +  [ _expr( x )   for x in node.nodes ]
	
	#def AssList(self, node):
		#return [ 'listTarget' ]  +  [ _expr( x )   for x in node.nodes ]

	
	
	# Tuple literal
	def Tuple(self, node):
		return [ 'tupleLiteral' ]  +  [ _expr( x )   for x in node.elts ]
	
	
	
	# List literal
	def List(self, node):
		return [ 'listLiteral' ]  +  [ _expr( x )   for x in node.elts ]
	
	
	
	# List comprehension
	def ListComp(self, node):
		gens = []
		for x in node.generators:
			gens.extend( _listComp( x ) )
		return [ 'listComprehension', _expr( node.elt ) ]   +   gens
	
	
	
	# Generator expression
	def GeneratorExp(self, node):
		gens = []
		for x in node.generators:
			gens.extend( _genExp( x ) )
		return [ 'generatorExpression', _expr( node.elt ) ]   +   gens
	
	
	
	# Dictionary literal
	def Dict(self, node):
		return [ 'dictLiteral' ]  +  [ [ 'keyValuePair', _expr( k ), _expr( v ) ]   for k, v in zip( node.keys, node.values ) ]
	
	
	
	# Yield expresion
	def Yield(self, node):
		return [ 'yieldAtom', _expr( node.value ) ]
	
	
	
	# Attribute ref
	def Attribute(self, node):
		return [ 'attributeRef', _expr( node.value ), node.attr ]
		
	
	
	# Subscript	
	def Subscript(self, node):
		return [ 'subscript', _expr( node.value ), _expr( node.slice ) ]
	
	def Index(self, node):
		return _expr( node.value )
	
	def Slice(self, node):
		def _s(x):
			s = _expr( x )
			return s   if s is not None   else   '<nil>'
		if node.step is None:
			return [ 'subscriptSlice', _s( node.lower ), _s( node.upper ) ]
		else:
			return [ 'subscriptLongSlice', _s( node.lower ), _s( node.upper ), _s( node.step ) ]
	
	def ExtSlice(self, node):
		return [ 'tupleLiteral' ]  +  [ _expr( x )   for x in node.dims ]
	
	def Ellipsis(self, node):
		return [ 'ellipsis' ]
	
	
	
	# Call
	def Call(self, node):
		_starArg = lambda name, x:   [ [ name, _expr( x ) ] ]   if x is not None   else   []
		return [ 'call', _expr( node.func ) ]  +  [ _expr( x )   for x in node.args ]  +  [ _expr( x )   for x in node.keywords ]  +  _starArg( 'argList', node.starargs )  +  _starArg( 'kwArgList', node.kwargs )

	def keyword(self, node):
		return [ 'kwArg', node.arg, _expr( node.value ) ]
			
				
	
	
	# Binary operator
	def BinOp(self, node):
		opName = _getOpNodeName( node.op )
		opNodeHdr = _binOpNameTable[opName]
		return [ opNodeHdr, _expr( node.left ), _expr( node.right ) ]
		
	
	
	# Unary operator
	def UnaryOp(self, node):
		opName = _getOpNodeName( node.op )
		opNodeHdr = _unaryOpNameTable[opName]
		return [ opNodeHdr, _expr( node.operand ) ]
		

	
	# Compare
	def Compare(self, node):
		def _cmpName(x):
			return _cmpOpTable[ _getOpNodeName( x ) ]
		result = [ _cmpName( node.ops[0] ), _expr( node.left ), _expr( node.comparators[0] ) ]
		prev = node.comparators[0]
		if len( node.ops ) > 1:
			for op, comparator in zip( node.ops[1:], node.comparators[1:] ):
				result = [ 'andTest', result, [ _cmpName( op ), _expr( prev ), _expr( comparator ) ] ]
				prev = comparator
		return result
	
	
	
	# Boolean operations
	def BoolOp(self, node):
		opName = _getOpNodeName( node.op )
		opNodeHdr = _boolOpNameTable[opName]
		return reduce( lambda left, right: [ opNodeHdr, left, _expr( right ) ],   node.values[1:],  _expr( node.values[0] ) )
		
	
	# Lambda expression
	def Lambda(self, node):
		return [ 'lambdaExpr', _lambdaArgs( node.args ), _expr( node.body ) ]
	
	
	
	# Conditional expression
	def IfExp(self, node):
		return [ 'conditionalExpr', _expr( node.test ), _expr( node.body ), _expr( node.orelse ) ]
	
	
	
	
	
_augAssignOpTable = { 'Add' : '+=',  'Sub' : '-=',  'Mult' : '*=',  'Div' : '/=',  'Mod' : '%=',  'Pow' : '**=',  'LShift' : '<<=',  'RShift' : '>>=',  'BitAnd' : '&=',  'BitOr' : '|=', 'BitXor' : '^=' }
	
class _StmtImporter (_Importer):
	# Assert statement
	def Assert(self, node):
		return [ 'assertStmt', _expr( node.test ),  _expr( node.msg )   if node.msg is not None   else   '<nil>' ]
	
	
	# Assignment statement
	def Assign(self, node):
		return [ 'assignmentStmt', [ _target( x )   for x in node.targets ],  _expr( node.value ) ]
	
	
	# Augmented assignment statement
	def AugAssign(self, node):
		return [ 'augAssignStmt', _augAssignOpTable[ _getNodeTypeName( node.op ) ], _target( node.target ), _expr( node.value ) ]
	
	
	# Pass
	def Pass(self, node):
		return [ 'passStmt' ]
	
	
	# Del
	def Delete(self, node):
		return [ 'delStmt' ]  +  [ _target( x )   for x in node.targets ]
		
	
	# Return
	def Return(self, node):
		return [ 'returnStmt', _expr( node.value ) ]

	
	# Yield
	def Yield(self, node):
		return [ 'yieldStmt', _expr( node.value ) ]

	
	# Raise
	def Raise(self, node):
		return [ 'raiseStmt', _expr( node.type ), _expr( node.inst ), _expr( node.tback ) ]
	

	# Break
	def Break(self, node):
		return [ 'breakStmt' ]
	
	
	# Continue
	def Continue(self, node):
		return [ 'continueStmt' ]
	
	
	# Import
	def Import(self, node):
		return [ 'importStmt' ]  +  [ _import( x )   for x in node.names ]
	
	def ImportFrom(self, node):
		if len( node.names ) == 1   and   node.names[0].name == '*':
			return [ 'fromImportAllStmt', [ 'relativeModule', node.module ] ]
		else:
			return [ 'fromImportStmt', [ 'relativeModule', node.module ] ]  +  [ _importFrom( x )   for x in node.names ]
		
		
	# Global
	def Global(self, node):
		return [ 'globalStmt' ]  +  [ [ 'globalVar', name ]   for name in node.names ]
	
	
	# Exec
	def Exec(self, node):
		return [ 'execStmt', _expr( node.body ), _expr( node.locals ), _expr( node.globals ) ]
	
	
	# Print
	def Print(self, node):
		return [ 'call', [ 'var', 'print' ] ]  +  [ _expr( x )   for x in node.values ]
	
	
	
	
	# Expression
	def Expr(self, node):
		return _expr( node.value )
	

	
	
class _CompoundStmtImporter (_Importer):
	def __call__(self, node, method=None):
		if method is None:
			name = _getNodeTypeName( node )
			try:
				method = getattr( self, name )
			except AttributeError:
				return [ _stmt( node ) ]
		return method( node )

	
	# If
	def If(self, node):
		def _handleNode(n, bFirst):
			name = 'ifStmt'   if bFirst   else 'elifStmt'
			result = [ [ name, _expr( n.test ), _flattenedCompound( n.body ) ] ]
			if len( n.orelse ) == 1   and   isinstance( n.orelse[0], _ast.If ):
				result.extend( _handleNode( n.orelse[0], False ) )
			elif len( n.orelse ) == 0:
				pass
			else:
				result.extend( [ [ 'elseStmt', _flattenedCompound( n.orelse) ] ] )
			return result;
			
		return _handleNode( node, True )

	
	# While
	def While(self, node):
		result = [ [ 'whileStmt', _expr( node.test ), _flattenedCompound( node.body ) ] ]
		if len( node.orelse ) > 0:
			result.append( [ 'elseStmt', _flattenedCompound( node.orelse ) ] )
		return result

	
	# For
	def For(self, node):
		result = [ [ 'forStmt', _target( node.target ), _expr( node.iter ), _flattenedCompound( node.body ) ] ]
		if len( node.orelse ) > 0:
			result.append( [ 'elseStmt', _flattenedCompound( node.orelse ) ] )
		return result
	
	
	# Try
	def TryExcept(self, node):
		result = [ [ 'tryStmt', _flattenedCompound( node.body ) ] ]
		for h in node.handlers:
			result.append( _except( h ) )
		if len( node.orelse ) > 0:
			result.append( [ 'elseStmt', _flattenedCompound( node.orelse ) ] )
		return result
	
	def TryFinally(self, node):
		body = _flattenedCompound( node.body )
		body.append( [ 'finallyStmt', _flattenedCompound( node.finalbody ) ] )
		return body
	
	
	# With
	def With(self, node):
		return [ [ 'withStmt',    _expr( node.expr ),   _target( node.vars )   if node.vars is not None   else   '<nil>',   _flattenedCompound( node.body ) ] ]
	
	
	# Function
	def FunctionDef(self, node):
		result = [ _decorator( dec )   for dec in node.decorators ]
		params = _extractParameters( node.args )
		result.append( [ 'defStmt', node.name, params, _flattenedCompound( node.body ) ] )
		return result
		
	

	# Class
	def ClassDef(self, node):
		if len( node.bases ) == 0:
			bases = '<nil>'
		elif len( node.bases ) == 1:
			bases = _expr( node.bases[0] )
		else:
			bases = [ 'tupleLiteral' ]  +  [ _expr( b )   for b in node.bases ]
		return [ [ 'classStmt', node.name, bases, _flattenedCompound( node.body ) ] ]
	



class _ModuleImporter (_Importer):
	# Module
	def Module(self, node):
		return [ 'python25Module' ]  +  _flattenedCompound( node.body )
	
	
	
	
	
_target = _TargetImporter()
_listComp = _ListCompImporter()
_genExp = _GenExprImporter()
_lambdaArgs = _LambdaArgsImporter()
_import = _ImportImporter()
_importFrom = _ImportFromImporter()
_except = _ExceptHandlerImporter()
_expr = _ExprImporter()
_decorator = _DecoratorImporter()
_stmt = _StmtImporter()
_compound = _CompoundStmtImporter()
_module = _ModuleImporter()


def _flattenedCompound(nodeList):
	xs = []
	for node in nodeList:
		xs.extend( _compound( node ) )
	return xs
	

	
	
def importPy25Source(source, moduleName, mode):
	tree = compile( source, moduleName, mode, _ast.PyCF_ONLY_AST )
	return _module( tree )


def importPy25File(filename):
	source = open( filename, 'r' ).read()
	tree = compile( source, filename, 'exec', _ast.PyCF_ONLY_AST )
	return _module( tree )



import unittest


class ImporterTestCase (unittest.TestCase):
	def _moduleTest(self, source, expectedResult):
		result = importPy25Source( source, '<test_module>', 'exec' )
		if result != expectedResult:
			print 'EXPECTED:'
			print expectedResult
			print 'RESULT:'
			print result
		self.assert_( result == expectedResult )
		
		
	def _exprTest(self, source, expectedResult):
		result = importPy25Source( source, '<text_expr>', 'exec' )
		result = result[1]
		if result != expectedResult:
			print 'EXPECTED:'
			print expectedResult
			print 'RESULT:'
			print result
		self.assert_( result == expectedResult )
		
	def _stmtTest(self, source, expectedResult):
		result = importPy25Source( source, '<test_stmt>', 'exec' )
		result = result[1]
		if result != expectedResult:
			print 'EXPECTED:'
			print expectedResult
			print 'RESULT:'
			print result
		self.assert_( result == expectedResult )
		
	def _compStmtTest(self, source, expectedResult):
		result = importPy25Source( source, '<test_stmt>', 'exec' )
		result = result[1:]
		if result != expectedResult:
			print 'EXPECTED:'
			print expectedResult
			print 'RESULT:'
			print result
		self.assert_( result == expectedResult )
		

		
		
	def testModule(self):
		self._moduleTest( 'a ** b', [ 'python25Module', [ 'pow', [ 'var', 'a' ], [ 'var', 'b' ] ] ] )

	def testStmt(self):
		self._stmtTest( 'a ** b', [ 'pow', [ 'var', 'a' ], [ 'var', 'b' ] ] )

	def testExpr(self):
		self._exprTest( 'a ** b', [ 'pow', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		
	

	def testName(self):
		self._exprTest( 'a', [ 'var', 'a' ] )
	
	def testStr(self):
		self._exprTest( "'a'", [ 'stringLiteral', 'ascii', 'single', 'a' ] )
		self._exprTest( "u'a'", [ 'stringLiteral', 'unicode', 'single', u'a' ] )
		
		
	def testNum(self):
		self._exprTest( '1', [ 'intLiteral', 'decimal', 'int', '1' ] )
		self._exprTest( '1L', [ 'intLiteral', 'decimal', 'long', '1' ] )
		self._exprTest( '1.0', [ 'floatLiteral', '1.0' ] )
		self._exprTest( '1j', [ 'imaginaryLiteral', '1.0j' ] )
	
	
	
	def testTuple(self):
		self._exprTest( 'a,b',  [ 'tupleLiteral', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		
		

	def testList(self):
		self._exprTest( '[a,b]',  [ 'listLiteral', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		
		
	def testListComp(self):
		self._exprTest( '[a   for a in x]',   [ 'listComprehension', [ 'var', 'a' ],  [ 'listFor', [ 'singleTarget', 'a', ], [ 'var', 'x' ] ] ] )
		self._exprTest( '[a   for a,b in x]',   [ 'listComprehension', [ 'var', 'a' ],  [ 'listFor', [ 'tupleTarget', [ 'singleTarget', 'a', ], [ 'singleTarget', 'b' ] ], [ 'var', 'x' ] ] ] )
		self._exprTest( '[a   for [a,b] in x]',   [ 'listComprehension', [ 'var', 'a' ],  [ 'listFor', [ 'listTarget', [ 'singleTarget', 'a', ], [ 'singleTarget', 'b' ] ], [ 'var', 'x' ] ] ] )
		self._exprTest( '[a   for a.b in x]',   [ 'listComprehension', [ 'var', 'a' ],  [ 'listFor', [ 'attributeRef', [ 'var', 'a', ], 'b'], [ 'var', 'x' ] ] ] )
		self._exprTest( '[a   for a[b] in x]',   [ 'listComprehension', [ 'var', 'a' ],  [ 'listFor', [ 'subscript', [ 'var', 'a', ], [ 'var', 'b' ] ], [ 'var', 'x' ] ] ] )
		self._exprTest( '[a   for a in x   if q]',   [ 'listComprehension', [ 'var', 'a' ],  [ 'listFor', [ 'singleTarget', 'a', ], [ 'var', 'x' ] ],   [ 'listIf', [ 'var', 'q' ] ] ] )
		self._exprTest( '[a   for a in x   if q  if w]',   [ 'listComprehension', [ 'var', 'a' ],  [ 'listFor', [ 'singleTarget', 'a', ], [ 'var', 'x' ] ],   [ 'listIf', [ 'var', 'q' ] ],   [ 'listIf', [ 'var', 'w' ] ] ] )
		self._exprTest( '[a   for a in x   if q  if w   for b in f]',   [ 'listComprehension', [ 'var', 'a' ],  [ 'listFor', [ 'singleTarget', 'a', ], [ 'var', 'x' ] ],   [ 'listIf', [ 'var', 'q' ] ],   [ 'listIf', [ 'var', 'w' ] ],  [ 'listFor', [ 'singleTarget', 'b', ], [ 'var', 'f' ] ] ] )
		
		
		
	def testGeneratorExp(self):
		self._exprTest( '(a   for a in x)',   [ 'generatorExpression', [ 'var', 'a' ],  [ 'genFor', [ 'singleTarget', 'a', ], [ 'var', 'x' ] ] ] )
		self._exprTest( '(a   for a,b in x)',   [ 'generatorExpression', [ 'var', 'a' ],  [ 'genFor', [ 'tupleTarget', [ 'singleTarget', 'a', ], [ 'singleTarget', 'b' ] ], [ 'var', 'x' ] ] ] )
		self._exprTest( '(a   for [a,b] in x)',   [ 'generatorExpression', [ 'var', 'a' ],  [ 'genFor', [ 'listTarget', [ 'singleTarget', 'a', ], [ 'singleTarget', 'b' ] ], [ 'var', 'x' ] ] ] )
		self._exprTest( '(a   for a.b in x)',   [ 'generatorExpression', [ 'var', 'a' ],  [ 'genFor', [ 'attributeRef', [ 'var', 'a', ], 'b'], [ 'var', 'x' ] ] ] )
		self._exprTest( '(a   for a[b] in x)',   [ 'generatorExpression', [ 'var', 'a' ],  [ 'genFor', [ 'subscript', [ 'var', 'a', ], [ 'var', 'b' ] ], [ 'var', 'x' ] ] ] )
		self._exprTest( '(a   for a in x   if q)',   [ 'generatorExpression', [ 'var', 'a' ],  [ 'genFor', [ 'singleTarget', 'a', ], [ 'var', 'x' ] ],   [ 'genIf', [ 'var', 'q' ] ] ] )
		self._exprTest( '(a   for a in x   if q  if w)',   [ 'generatorExpression', [ 'var', 'a' ],  [ 'genFor', [ 'singleTarget', 'a', ], [ 'var', 'x' ] ],   [ 'genIf', [ 'var', 'q' ] ],   [ 'genIf', [ 'var', 'w' ] ] ] )
		self._exprTest( '(a   for a in x   if q  if w   for b in f)',   [ 'generatorExpression', [ 'var', 'a' ],  [ 'genFor', [ 'singleTarget', 'a', ], [ 'var', 'x' ] ],   [ 'genIf', [ 'var', 'q' ] ],   [ 'genIf', [ 'var', 'w' ] ],  [ 'genFor', [ 'singleTarget', 'b', ], [ 'var', 'f' ] ] ] )

		
	
	def testDict(self):
		self._exprTest( '{a:b, c:d}',  [ 'dictLiteral', [ 'keyValuePair', [ 'var', 'a' ], [ 'var', 'b' ] ], [ 'keyValuePair', [ 'var', 'c' ], [ 'var', 'd' ] ] ] )
		
		
	def testYieldExpr(self):
		self._exprTest( '(yield a)', [ 'yieldAtom', [ 'var', 'a' ] ] )
		
		
	def testAttribute(self):
		self._exprTest( 'a.b', [ 'attributeRef', [ 'var', 'a' ], 'b' ] )
		
		

	def testSubscript(self):
		self._exprTest( 'a[b]',  [ 'subscript', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a[b,c]',  [ 'subscript', [ 'var', 'a' ], [ 'tupleLiteral', [ 'var', 'b' ], [ 'var', 'c' ] ] ] )
		
	def testSlice(self):	
		self._exprTest( 'a[b:c]',  [ 'subscript', [ 'var', 'a' ], [ 'subscriptSlice', [ 'var', 'b' ], [ 'var', 'c' ] ] ] )
		self._exprTest( 'a[b:]',  [ 'subscript', [ 'var', 'a' ], [ 'subscriptSlice', [ 'var', 'b' ], '<nil>' ] ] )
		self._exprTest( 'a[:c]',  [ 'subscript', [ 'var', 'a' ], [ 'subscriptSlice', '<nil>', [ 'var', 'c' ] ] ] )
		self._exprTest( 'a[b:c:d]',  [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', [ 'var', 'b' ], [ 'var', 'c' ], [ 'var', 'd' ] ] ] )
		self._exprTest( 'a[b:c:]',  [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', [ 'var', 'b' ], [ 'var', 'c' ], [ 'var', 'None' ] ] ] )
		self._exprTest( 'a[b::d]',  [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', [ 'var', 'b' ], '<nil>', [ 'var', 'd' ] ] ] )
		self._exprTest( 'a[:c:d]',  [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', '<nil>', [ 'var', 'c' ], [ 'var', 'd' ] ] ] )
		self._exprTest( 'a[b:c,d:e]',  [ 'subscript', [ 'var', 'a' ], [ 'tupleLiteral', [ 'subscriptSlice', [ 'var', 'b' ], [ 'var', 'c' ] ], [ 'subscriptSlice', [ 'var', 'd' ], [ 'var', 'e' ] ] ] ] )
		self._exprTest( 'a[b:c,d:e:f]',  [ 'subscript', [ 'var', 'a' ], [ 'tupleLiteral', [ 'subscriptSlice', [ 'var', 'b' ], [ 'var', 'c' ] ], [ 'subscriptLongSlice', [ 'var', 'd' ], [ 'var', 'e' ], [ 'var', 'f' ] ] ] ] )
		
	def testEllipsis(self):
		self._exprTest( 'a[...]',  [ 'subscript', [ 'var', 'a' ], [ 'ellipsis' ] ] )
		
	

	def testCall(self):
		self._exprTest( 'a()',   [ 'call', [ 'var', 'a' ] ] )
		self._exprTest( 'a(f)',   [ 'call', [ 'var', 'a' ], [ 'var', 'f' ] ] )
		self._exprTest( 'a(f,g=x)',   [ 'call', [ 'var', 'a' ], [ 'var', 'f' ], [ 'kwArg', 'g', [ 'var', 'x' ] ] ] )
		self._exprTest( 'a(f,g=x,*h)',   [ 'call', [ 'var', 'a' ], [ 'var', 'f' ], [ 'kwArg', 'g', [ 'var', 'x' ] ], [ 'argList', [ 'var', 'h' ] ] ] )
		self._exprTest( 'a(f,g=x,*h,**i)',   [ 'call', [ 'var', 'a' ], [ 'var', 'f' ], [ 'kwArg', 'g', [ 'var', 'x' ] ], [ 'argList', [ 'var', 'h' ] ], [ 'kwArgList', [ 'var', 'i' ] ] ] )
	
	
	def testBinOp(self):
		self._exprTest( 'a ** b', [ 'pow', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a * b', [ 'mul', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a / b', [ 'div', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a % b', [ 'mod', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a + b', [ 'add', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a - b', [ 'sub', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a << b', [ 'lshift', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a >> b', [ 'rshift', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a & b', [ 'bitAnd', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a & b & c', [ 'bitAnd', [ 'bitAnd', [ 'var', 'a' ], [ 'var', 'b' ] ], [ 'var', 'c' ] ] )
		self._exprTest( 'a ^ b', [ 'bitXor', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a ^ b ^ c', [ 'bitXor', [ 'bitXor', [ 'var', 'a' ], [ 'var', 'b' ] ], [ 'var', 'c' ] ] )
		self._exprTest( 'a | b', [ 'bitOr', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a | b | c', [ 'bitOr', [ 'bitOr', [ 'var', 'a' ], [ 'var', 'b' ] ], [ 'var', 'c' ] ] )
		
		
	def testUnaryOp(self):
		self._exprTest( '~a', [ 'invert', [ 'var', 'a' ], ] )
		self._exprTest( '+a', [ 'pos', [ 'var', 'a' ], ] )
		self._exprTest( '-a', [ 'negate', [ 'var', 'a' ], ] )
		self._exprTest( 'not a', [ 'notTest', [ 'var', 'a' ] ] )
		
		
	def testCompare(self):
		self._exprTest( 'a < b', [ 'lt', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a < b < c', [ 'andTest', [ 'lt', [ 'var', 'a' ], [ 'var', 'b' ] ], [ 'lt', [ 'var', 'b' ], [ 'var', 'c' ] ] ] )
		self._exprTest( 'a <= b', [ 'lte', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a == b', [ 'eq', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a != b', [ 'neq', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a > b', [ 'gt', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a >= b', [ 'gte', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a is b', [ 'isTest', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a is not b', [ 'isNotTest', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a in b', [ 'inTest', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a not in b', [ 'notInTest', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		
		
	def testBoolOp(self):
		self._exprTest( 'a and b', [ 'andTest', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a or b', [ 'orTest', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		
		
	def testLambda(self):
		self._exprTest( 'lambda: x', [ 'lambdaExpr', [], [ 'var', 'x' ] ] )
		self._exprTest( 'lambda f: x', [ 'lambdaExpr', [ [ 'simpleParam', 'f' ] ], [ 'var', 'x' ] ] )
		self._exprTest( 'lambda f,: x', [ 'lambdaExpr', [ [ 'simpleParam', 'f' ] ], [ 'var', 'x' ] ] )
		self._exprTest( 'lambda f,g: x', [ 'lambdaExpr', [ [ 'simpleParam', 'f' ], [ 'simpleParam', 'g' ] ], [ 'var', 'x' ] ] )
		self._exprTest( 'lambda f,g,m=a: x', [ 'lambdaExpr', [ [ 'simpleParam', 'f' ], [ 'simpleParam', 'g' ], [ 'defaultValueParam', 'm', [ 'var', 'a' ] ] ], [ 'var', 'x' ] ] )
		self._exprTest( 'lambda f,g,m=a,n=b: x', [ 'lambdaExpr', [ [ 'simpleParam', 'f' ], [ 'simpleParam', 'g' ], [ 'defaultValueParam', 'm', [ 'var', 'a' ] ], [ 'defaultValueParam', 'n', [ 'var', 'b' ] ] ], [ 'var', 'x' ] ] )
		self._exprTest( 'lambda f,g,m=a,n=b,*p: x', [ 'lambdaExpr', [ [ 'simpleParam', 'f' ], [ 'simpleParam', 'g' ], [ 'defaultValueParam', 'm', [ 'var', 'a' ] ], [ 'defaultValueParam', 'n', [ 'var', 'b' ] ], [ 'paramList', 'p' ] ], [ 'var', 'x' ] ] )
		self._exprTest( 'lambda f,m=a,*p,**w: x', [ 'lambdaExpr', [ [ 'simpleParam', 'f' ], [ 'defaultValueParam', 'm', [ 'var', 'a' ] ], [ 'paramList', 'p' ], [ 'kwParamList', 'w' ] ], [ 'var', 'x' ] ] )
		self._exprTest( 'lambda f,m=a,*p: x', [ 'lambdaExpr', [ [ 'simpleParam', 'f' ], [ 'defaultValueParam', 'm', [ 'var', 'a' ] ], [ 'paramList', 'p' ] ], [ 'var', 'x' ] ] )
		self._exprTest( 'lambda f,m=a,**w: x', [ 'lambdaExpr', [ [ 'simpleParam', 'f' ], [ 'defaultValueParam', 'm', [ 'var', 'a' ] ], [ 'kwParamList', 'w' ] ], [ 'var', 'x' ] ] )
		self._exprTest( 'lambda f,*p,**w: x', [ 'lambdaExpr', [ [ 'simpleParam', 'f' ], [ 'paramList', 'p' ], [ 'kwParamList', 'w' ] ], [ 'var', 'x' ] ] )
		self._exprTest( 'lambda m=a,*p,**w: x', [ 'lambdaExpr', [ [ 'defaultValueParam', 'm', [ 'var', 'a' ] ], [ 'paramList', 'p' ], [ 'kwParamList', 'w' ] ], [ 'var', 'x' ] ] )
		self._exprTest( 'lambda *p,**w: x', [ 'lambdaExpr', [ [ 'paramList', 'p' ], [ 'kwParamList', 'w' ] ], [ 'var', 'x' ] ] )
		self._exprTest( 'lambda **w: x', [ 'lambdaExpr', [ [ 'kwParamList', 'w' ] ], [ 'var', 'x' ] ] )
		
		
	def testConditionalExpr(self):
		self._exprTest( 'a   if b   else   c', [ 'conditionalExpr', [ 'var', 'b' ], [ 'var', 'a' ], [ 'var', 'c' ] ] )
		
		

	
	def testAssert(self):
		self._stmtTest( 'assert a', [ 'assertStmt', [ 'var', 'a' ], '<nil>' ] )
		self._stmtTest( 'assert a,b', [ 'assertStmt', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		

	def testAssign(self):
		self._stmtTest( 'a=x', [ 'assignmentStmt', [ [ 'singleTarget', 'a' ] ], [ 'var', 'x' ] ] )
		self._stmtTest( 'a,b=c,d=x', [ 'assignmentStmt', [ [ 'tupleTarget', [ 'singleTarget', 'a' ],  [ 'singleTarget', 'b' ] ],  [ 'tupleTarget', [ 'singleTarget', 'c' ],  [ 'singleTarget', 'd' ] ] ], [ 'var', 'x' ] ] )
		self._stmtTest( 'a=yield x', [ 'assignmentStmt', [ [ 'singleTarget', 'a' ] ], [ 'yieldAtom', [ 'var', 'x' ] ] ] )
	
		
	def testAugAssignStmt(self):
		self._stmtTest( 'a += b', [ 'augAssignStmt', '+=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._stmtTest( 'a -= b', [ 'augAssignStmt', '-=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._stmtTest( 'a *= b', [ 'augAssignStmt', '*=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._stmtTest( 'a /= b', [ 'augAssignStmt', '/=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._stmtTest( 'a %= b', [ 'augAssignStmt', '%=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._stmtTest( 'a **= b', [ 'augAssignStmt', '**=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._stmtTest( 'a >>= b', [ 'augAssignStmt', '>>=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._stmtTest( 'a <<= b', [ 'augAssignStmt', '<<=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._stmtTest( 'a &= b', [ 'augAssignStmt', '&=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._stmtTest( 'a ^= b', [ 'augAssignStmt', '^=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._stmtTest( 'a |= b', [ 'augAssignStmt', '|=', [ 'singleTarget', 'a' ], [ 'var', 'b' ] ] )
		self._stmtTest( 'a.b += b', [ 'augAssignStmt', '+=', [ 'attributeRef', [ 'var', 'a' ], 'b' ], [ 'var', 'b' ] ] )
		self._stmtTest( 'a[x] += b', [ 'augAssignStmt', '+=', [ 'subscript', [ 'var', 'a' ], [ 'var', 'x' ] ], [ 'var', 'b' ] ] )

	def testPassStmt(self):
		self._stmtTest( 'pass', [ 'passStmt' ] )
		
		
	def testDelStmt(self):
		self._stmtTest( 'del x', [ 'delStmt', [ 'singleTarget', 'x' ] ] )
		
		
	def testReturnStmt(self):
		self._stmtTest( 'return x', [ 'returnStmt', [ 'var', 'x' ] ] )
		
	
	def testYieldStmt(self):
		self._stmtTest( 'yield x', [ 'yieldAtom', [ 'var', 'x' ] ] )
		
		
	def testRaiseStmt(self):
		self._stmtTest( 'raise', [ 'raiseStmt', '<nil>', '<nil>', '<nil>' ] )
		self._stmtTest( 'raise x', [ 'raiseStmt', [ 'var', 'x' ], '<nil>', '<nil>' ] )
		self._stmtTest( 'raise x,y', [ 'raiseStmt', [ 'var', 'x' ], [ 'var', 'y' ], '<nil>' ] )
		self._stmtTest( 'raise x,y,z', [ 'raiseStmt', [ 'var', 'x' ], [ 'var', 'y' ], [ 'var', 'z' ] ] )
		
		
	def testBreakStmt(self):
		self._stmtTest( 'break', [ 'breakStmt' ] )
		
		
	def testContinueStmt(self):
		self._stmtTest( 'continue', [ 'continueStmt' ] )
		
		
	def testImportStmt(self):
		self._stmtTest( 'import a', [ 'importStmt', [ 'moduleImport', 'a' ] ] )
		self._stmtTest( 'import a.b', [ 'importStmt', [ 'moduleImport', 'a.b' ] ] )
		self._stmtTest( 'import a.b as x', [ 'importStmt', [ 'moduleImportAs', 'a.b', 'x' ] ] )
		self._stmtTest( 'import a.b as x, c.d as y', [ 'importStmt', [ 'moduleImportAs', 'a.b', 'x' ], [ 'moduleImportAs', 'c.d', 'y' ] ] )
		self._stmtTest( 'from x import a', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._stmtTest( 'from x import a as p', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._stmtTest( 'from x import a as p, b as q', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._stmtTest( 'from x import (a)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._stmtTest( 'from x import (a,)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImport', 'a' ] ] )
		self._stmtTest( 'from x import (a as p)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._stmtTest( 'from x import (a as p,)', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ] ] )
		self._stmtTest( 'from x import ( a as p, b as q )', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._stmtTest( 'from x import ( a as p, b as q, )', [ 'fromImportStmt', [ 'relativeModule', 'x' ], [ 'moduleContentImportAs', 'a', 'p' ], [ 'moduleContentImportAs', 'b', 'q' ] ] )
		self._stmtTest( 'from x import *', [ 'fromImportAllStmt', [ 'relativeModule', 'x' ] ] )
		
		
	def testGlobalStmt(self):
		self._stmtTest( 'global x', [ 'globalStmt', [ 'globalVar', 'x' ] ] )
		self._stmtTest( 'global x, y', [ 'globalStmt', [ 'globalVar', 'x' ], [ 'globalVar', 'y' ] ] )
	
		
	def testExecStmt(self):
		self._stmtTest( 'exec a', [ 'execStmt', [ 'var', 'a' ], '<nil>', '<nil>' ] )
		self._stmtTest( 'exec a in b', [ 'execStmt', [ 'var', 'a' ], '<nil>', [ 'var', 'b' ] ] )
		self._stmtTest( 'exec a in b,c', [ 'execStmt', [ 'var', 'a' ], [ 'var', 'c' ], [ 'var', 'b' ] ] )
		
		
	def testPrintnl(self):
		self._stmtTest( 'print x', [ 'call', [ 'var', 'print' ], [ 'var', 'x' ] ] )
		self._stmtTest( 'print x,y', [ 'call', [ 'var', 'print' ], [ 'var', 'x' ], [ 'var', 'y' ] ] )
		
		
	def testIf(self):
		src1 = \
"""
if a:
	x
"""
		src2 = \
"""
if a:
	x
elif b:
	y
"""
		src3 = \
"""
if a:
	x
else:
	z
"""
		src4 = \
"""
if a:
	x
elif b:
	y
else:
	z
"""
		self._compStmtTest( src1, [ [ 'ifStmt', [ 'var', 'a' ], [ [ 'var', 'x' ] ] ] ] )
		self._compStmtTest( src2, [ [ 'ifStmt', [ 'var', 'a' ], [ [ 'var', 'x' ] ] ], [ 'elifStmt', [ 'var', 'b' ], [ [ 'var', 'y' ] ] ] ] )
		self._compStmtTest( src3, [ [ 'ifStmt', [ 'var', 'a' ], [ [ 'var', 'x' ] ] ], [ 'elseStmt', [ [ 'var', 'z' ] ] ] ] )
		self._compStmtTest( src4, [ [ 'ifStmt', [ 'var', 'a' ], [ [ 'var', 'x' ] ] ], [ 'elifStmt', [ 'var', 'b' ], [ [ 'var', 'y' ] ] ], [ 'elseStmt', [ [ 'var', 'z' ] ] ] ] )

		
	def testWhile(self):
		src1 = \
"""
while a:
	x
"""
		src2 = \
"""
while a:
	x
else:
	z
"""
		self._compStmtTest( src1, [ [ 'whileStmt', [ 'var', 'a' ], [ [ 'var', 'x' ] ] ] ] )
		self._compStmtTest( src2, [ [ 'whileStmt', [ 'var', 'a' ], [ [ 'var', 'x' ] ] ], [ 'elseStmt', [ [ 'var', 'z' ] ] ] ] )

		
		
	def testFor(self):
		src1 = \
"""
for a in q:
	x
"""
		src2 = \
"""
for a in q:
	x
else:
	z
"""
		self._compStmtTest( src1, [ [ 'forStmt', [ 'singleTarget', 'a' ], [ 'var', 'q' ], [ [ 'var', 'x' ] ] ] ] )
		self._compStmtTest( src2, [ [ 'forStmt', [ 'singleTarget', 'a' ], [ 'var', 'q' ], [ [ 'var', 'x' ] ] ], [ 'elseStmt', [ [ 'var', 'z' ] ] ] ] )

		
		
	def testTry(self):
		src1 = \
"""
try:
	x
except:
	p
except a:
	q
except a,b:
	r
"""
		src2 = \
"""
try:
	x
except:
	p
else:
	z
"""
		src3 = \
"""
try:
	x
except:
	p
finally:
	z
"""
		self._compStmtTest( src1, [ [ 'tryStmt', [ [ 'var', 'x' ] ] ],  [ 'exceptStmt', '<nil>', '<nil>', [ [ 'var', 'p' ] ] ],   [ 'exceptStmt', [ 'var', 'a' ], '<nil>', [ [ 'var', 'q' ] ] ],    [ 'exceptStmt', [ 'var', 'a' ], [ 'singleTarget', 'b' ], [ [ 'var', 'r' ] ] ] ] )
		self._compStmtTest( src2, [ [ 'tryStmt', [ [ 'var', 'x' ] ] ],  [ 'exceptStmt', '<nil>', '<nil>', [ [ 'var', 'p' ] ] ],   [ 'elseStmt', [ [ 'var', 'z' ] ] ] ] )
		self._compStmtTest( src3, [ [ 'tryStmt', [ [ 'var', 'x' ] ] ],  [ 'exceptStmt', '<nil>', '<nil>', [ [ 'var', 'p' ] ] ],   [ 'finallyStmt', [ [ 'var', 'z' ] ] ] ] )

		
		
	#def testWith(self):
		#src1 = \
#"""
#with a:
	#x
#"""
		#src2 = \
#"""
#with a as b:
	#x
#"""
		#self._compStmtTest( src1, [ [ 'withStmt', [ 'var', 'a' ], '<nil>', [ [ 'var', 'x' ] ] ] ] )
		#self._compStmtTest( src2, [ [ 'withStmt', [ 'var', 'a' ], [ 'singleTarget', 'b' ], [ [ 'var', 'x' ] ] ] ] )

	
	def testFunction(self):
		src1 = \
"""
def f():
	x
"""
		src2 = \
"""
def f(a,b=q,*c,**d):
	x
"""
		src3 = \
"""
@p
def f():
	x
"""
		src4 = \
"""
@p(h)
def f():
	x
"""
		src5 = \
"""
@p(h)
@q(j)
def f():
	x
"""
		self._compStmtTest( src1, [ [ 'defStmt', 'f', [], [ [ 'var', 'x' ] ] ] ] )
		self._compStmtTest( src2, [ [ 'defStmt', 'f', [ [ 'simpleParam', 'a' ], [ 'defaultValueParam', 'b', [ 'var', 'q' ] ], [ 'paramList', 'c' ], [ 'kwParamList', 'd' ] ], [ [ 'var', 'x' ] ] ] ] )
		self._compStmtTest( src3, [ [ 'decoStmt', 'p', '<nil>' ], [ 'defStmt', 'f', [], [ [ 'var', 'x' ] ] ] ] )
		self._compStmtTest( src4, [ [ 'decoStmt', 'p', [ [ 'var', 'h' ] ] ], [ 'defStmt', 'f', [], [ [ 'var', 'x' ] ] ] ] )
		self._compStmtTest( src5, [ [ 'decoStmt', 'p', [ [ 'var', 'h' ] ] ], [ 'decoStmt', 'q', [ [ 'var', 'j' ] ] ], [ 'defStmt', 'f', [], [ [ 'var', 'x' ] ] ] ] )

		
	def testClass(self):
		src1 = \
"""
class Q:
	x
"""
		src2 = \
"""
class Q (object):
	x
"""
		src3 = \
"""
class Q (a,b):
	x
"""
		self._compStmtTest( src1, [ [ 'classStmt', 'Q', '<nil>', [ [ 'var', 'x' ] ] ] ] )
		self._compStmtTest( src2, [ [ 'classStmt', 'Q', [ 'var', 'object' ], [ [ 'var', 'x' ] ] ] ] )
		self._compStmtTest( src3, [ [ 'classStmt', 'Q', [ 'tupleLiteral', [ 'var', 'a' ], [ 'var', 'b' ] ], [ [ 'var', 'x' ] ] ] ] )

		
if __name__ == '__main__':
	unittest.main()
		
