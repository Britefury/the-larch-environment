##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import compiler
from compiler import ast


# Would need to generate an 'oldLambdaExpr' node when necessary



class _Importer (object):
	def __call__(self, node, method=None):
		if method is None:
			name = node.__class__.__name__
			try:
				method = getattr( self, name )
			except AttributeError:
				return [ 'UNPARSED', name ]
		return method( node )


	
_cmpOps = { '<' : 'lt',  '<=' : 'lte',  '==' : 'eq',  '!=' : 'neq',  '>' : 'gt',  '>=' : 'gte',  'is' : 'isTest',  'is not' : 'isNotTest',  'in' : 'inTest',  'not in' : 'notInTest' }
	
	

def _extractParameters(node):
	bParamList = ( node.flags & 4 )  !=  0
	bKWParamList = ( node.flags & 8 )  !=  0
	numListParams = 0
	if bParamList:
		numListParams += 1
	if bKWParamList:
		numListParams += 1
	numDefaultParams = len( node.defaults )
	numSimpleParams = len( node.argnames )  -  ( numDefaultParams + numListParams )
	params = [ [ 'simpleParam', name ]   for name in node.argnames[:numSimpleParams] ]
	params.extend( [ [ 'defaultValueParam', name, _expr( value ) ]   for name, value in zip( node.argnames[numSimpleParams:numSimpleParams+numDefaultParams], node.defaults ) ] )
	if bParamList:
		params.append( [ 'paramList', node.argnames[-2 if bKWParamList  else  -1] ] )
	if bKWParamList:
		params.append( [ 'kwParamList', node.argnames[-1] ] )
	return params





class _TargetImporter (_Importer):
	def __call__(self, node, method=None):
		if method is None:
			name = node.__class__.__name__
			try:
				method = getattr( self, name )
			except AttributeError:
				return _expr( node )
		return method( node )

	
	# Targets
	def AssName(self, node):
		return [ 'singleTarget', node.name ]
	
	def AssAttr(self, node):
		return [ 'attributeRef', _expr( node.expr ), node.attrname ]
	
	def AssTuple(self, node):
		return [ 'tupleTarget' ]  +  [ _expr( x )   for x in node.nodes ]
	
	def AssList(self, node):
		return [ 'listTarget' ]  +  [ _expr( x )   for x in node.nodes ]

	
	def Name(self, node):
		return [ 'singleTarget', node.name ]
	
	def Getattr(self, node):
		return [ 'attributeRef', _expr( node.expr ), node.attrname ]
	
	def Tuple(self, node):
		return [ 'tupleTarget' ]  +  [ _expr( x )   for x in node.nodes ]
	
	def List(self, node):
		return [ 'listTarget' ]  +  [ _expr( x )   for x in node.nodes ]




	
class _ExprImporter (_Importer):
	def __call__(self, node, method=None):
		if node is None:
			return '<nil>'
		else:
			if method is None:
				name = node.__class__.__name__
				try:
					method = getattr( self, name )
				except AttributeError:
					return [ 'UNPARSED', name ]
			return method( node )

	
	# Constant / literal
	def Const(self, node):
		if isinstance( node.value, str ):
			return [ 'stringLiteral', 'ascii', 'single', repr( node.value )[1:-1] ]
		elif isinstance( node.value, unicode ):
			return [ 'stringLiteral', 'unicode', 'single', repr( node.value )[2:-1] ]
		elif isinstance( node.value, int ):
			return [ 'intLiteral', 'decimal', 'int', repr( node.value ) ]
		elif isinstance( node.value, long ):
			return [ 'intLiteral', 'decimal', 'long', repr( node.value )[:-1] ]
		elif isinstance( node.value, float ):
			return [ 'floatLiteral', repr( node.value ) ]
		elif isinstance( node.value, complex ):
			if node.value.real == 0.0:
				return [ 'imaginaryLiteral', repr( node.value.imag ) + 'j' ]
			else:
				return [ 'add', [ 'floatLiteral', repr( node.value.real ) ], [ 'imaginaryLiteral', repr( node.value.imag ) + 'j' ] ]
		elif node.value is None:
			return None
		else:
			print 'Const: could not handle value', node.value
			raise ValueError
		
		
	# Variable ref
	def Name(self, node):
		return [ 'var', node.name ]


	# Targets
	def AssName(self, node):
		return [ 'singleTarget', node.name ]
	
	def AssAttr(self, node):
		return [ 'attributeRef', _expr( node.expr ), node.attrname ]
	
	def AssTuple(self, node):
		return [ 'tupleTarget' ]  +  [ _expr( x )   for x in node.nodes ]
	
	def AssList(self, node):
		return [ 'listTarget' ]  +  [ _expr( x )   for x in node.nodes ]

	
	
	# Tuple literal
	def Tuple(self, node):
		return [ 'tupleLiteral' ]  +  [ _expr( x )   for x in node.nodes ]
	
	
	
	# List literal
	def List(self, node):
		return [ 'listLiteral' ]  +  [ _expr( x )   for x in node.nodes ]
	
	
	
	# List comprehension
	def ListCompIf(self, node):
		return [ 'listIf', _expr( node.test ) ]
	
	def ListCompFor(self, node):
		return [ [ 'listFor', _expr( node.assign ), _expr( node.list ) ] ]  +  [ _expr( x )   for x in node.ifs ]
	
	def ListComp(self, node):
		quals = []
		for x in node.quals:
			quals.extend( _expr( x ) )
		return [ 'listComprehension', _expr( node.expr ) ]   +   quals
	
	
	
	# Generator expression
	def GenExprIf(self, node):
		return [ 'genIf', _expr( node.test ) ]
	
	def GenExprFor(self, node):
		return [ [ 'genFor', _expr( node.assign ), _expr( node.iter ) ] ]  +  [ _expr( x )   for x in node.ifs ]
	
	def GenExprInner(self, node):
		quals = []
		for x in node.quals:
			quals.extend( _expr( x ) )
		return [ 'generatorExpression', _expr( node.expr ) ]   +   quals
	
	def GenExpr(self, node):
		return _expr( node.code )
	
	
	
	# Dictionary literal
	def Dict(self, node):
		return [ 'dictLiteral' ]  +  [ [ 'keyValuePair', _expr( x[0] ), _expr( x[1] ) ]   for x in node.items ]
	
	
	
	# Yield expresion
	def Yield(self, node):
		return [ 'yieldAtom', _expr( node.value ) ]
	
	
	
	# Attribute ref
	def Getattr(self, node):
		return [ 'attributeRef', _expr( node.expr ), node.attrname ]
		
	
	
	# Subscript	
	def Subscript(self, node):
		if len( node.subs ) == 1:
			s = _expr( node.subs[0] )
		else:
			s = [ 'tupleLiteral' ]  +  [ _expr( x )   for x in node.subs ]
		return [ 'subscript', _expr( node.expr ), s ]
	
	def Slice(self, node):
		return [ 'subscript', _expr( node.expr ), [ 'subscriptSlice', _expr( node.lower ), _expr( node.upper ) ] ]
	
	def Sliceobj(self, node):
		def _s(x):
			s = _expr( x )
			return s   if s is not None   else   '<nil>'
		def _slice(xs):
			if len( xs ) == 3:
				return [ 'subscriptLongSlice', _s( xs[0] ), _s( xs[1] ), _s( xs[2] ) ]
			elif len( xs ) == 2:
				return [ 'subscriptSlice', _s( xs[0] ), _s( xs[1] ) ]
			else:
				raise ValueError
		return _slice( node.nodes )
	
	def Ellipsis(self, node):
		return [ 'ellipsis' ]
	
	
	
	# CallFunc
	def CallFunc(self, node):
		_starArg = lambda name, x:   [ [ name, _expr( x ) ] ]   if x is not None   else   []
		return [ 'call', _expr( node.node ) ]  +  [ _expr( x )   for x in node.args ]  +   _starArg( 'argList', node.star_args )  +  _starArg( 'kwArgList', node.dstar_args )

	def Keyword(self, node):
		return [ 'kwArg', node.name, _expr( node.expr ) ]
			
				
	
	
	# Operators
	def Power(self, node):
		return [ 'pow', _expr( node.left ), _expr( node.right ) ]
	
	def Invert(self, node):
		return [ 'invert', _expr( node.expr ) ]
	
	def UnaryAdd(self, node):
		return [ 'pos', _expr( node.expr ) ]
	
	def UnarySub(self, node):
		return [ 'negate', _expr( node.expr ) ]
	
	def Mul(self, node):
		return [ 'mul', _expr( node.left ), _expr( node.right ) ]

	def Div(self, node):
		return [ 'div', _expr( node.left ), _expr( node.right ) ]

	def Mod(self, node):
		return [ 'mod', _expr( node.left ), _expr( node.right ) ]
	
	def Add(self, node):
		return [ 'add', _expr( node.left ), _expr( node.right ) ]
	
	def Sub(self, node):
		return [ 'sub', _expr( node.left ), _expr( node.right ) ]
	
	def LeftShift(self, node):
		return [ 'lshift', _expr( node.left ), _expr( node.right ) ]
	
	def RightShift(self, node):
		return [ 'rshift', _expr( node.left ), _expr( node.right ) ]
	
	def Bitand(self, node):
		return reduce( lambda left, right: [ 'bitAnd', left, _expr( right ) ],   node.nodes[1:],  _expr( node.nodes[0] ) )
	
	def Bitxor(self, node):
		return reduce( lambda left, right: [ 'bitXor', left, _expr( right ) ],   node.nodes[1:],  _expr( node.nodes[0] ) )
	
	def Bitor(self, node):
		return reduce( lambda left, right: [ 'bitOr', left, _expr( right ) ],   node.nodes[1:],  _expr( node.nodes[0] ) )
	
	def Compare(self, node):
		result = [ _cmpOps[node.ops[0][0]], _expr( node.expr ), _expr( node.ops[0][1] ) ]
		prev = node.ops[0][1]
		if len( node.ops ) > 1:
			for op in node.ops[1:]:
				result = [ 'andTest', result, [ _cmpOps[op[0]], _expr( prev ), _expr( op[1] ) ] ]
				prev = op
		return result
	
	def Not(self, node):
		return [ 'notTest', _expr( node.expr ) ]
	
	def And(self, node):
		return reduce( lambda left, right: [ 'andTest', left, _expr( right ) ],   node.nodes[1:],  _expr( node.nodes[0] ) )
	
	def Or(self, node):
		return reduce( lambda left, right: [ 'orTest', left, _expr( right ) ],   node.nodes[1:],  _expr( node.nodes[0] ) )
	

	
	# Lambda expression
	def Lambda(self, node):
		params = _extractParameters( node )
		return [ 'lambdaExpr', params, _expr( node.code ) ]
	
	
	
	# Conditional expression
	def IfExp(self, node):
		return [ 'conditionalExpr', _expr( node.test ), _expr( node.then ), _expr( node.else_ ) ]
	
	
	
	
class _StmtImporter (_Importer):
	# Discard (expression statement)
	def Discard(self, node):
		return _expr( node.expr )
	
	
	# Assert statement
	def Assert(self, node):
		return [ 'assertStmt', _expr( node.test ),  _expr( node.fail )   if node.fail is not None   else   '<nil>' ]
	
	
	# Assignment statement
	def Assign(self, node):
		return [ 'assignmentStmt', [ _expr( x )   for x in node.nodes ],  _expr( node.expr ) ]
	
	
	# Augmented assignment statement
	def AugAssign(self, node):
		return [ 'augAssignStmt', node.op, _target( node.node ), _expr( node.expr ) ]
	
	
	# Pass
	def Pass(self, node):
		return [ 'passStmt' ]
	
	
	# Del
	def AssName(self, node):
		xs = [ 'singleTarget', node.name ]
		if node.flags == 'OP_DELETE':
			xs = [ 'delStmt', xs ]
		return xs
	
	def AssAttr(self, node):
		xs = [ 'attributeRef', _expr( node.expr ), node.attrname ]
		if node.flags == 'OP_DELETE':
			xs = [ 'delStmt', xs ]
		return xs
	
	def AssTuple(self, node):
		xs = [ 'tupleTarget' ]  +  [ _expr( x )   for x in node.nodes ]
		if node.flags == 'OP_DELETE':
			xs = [ 'delStmt', xs ]
		return xs
	
	def AssList(self, node):
		xs = [ 'listTarget' ]  +  [ _expr( x )   for x in node.nodes ]
		if node.flags == 'OP_DELETE':
			xs = [ 'delStmt', xs ]
		return xs
	
	
	
	# Return
	def Return(self, node):
		return [ 'returnStmt', _expr( node.value ) ]

	
	# Yield
	def Yield(self, node):
		return [ 'yieldStmt', _expr( node.value ) ]

	
	# Raise
	def Raise(self, node):
		return [ 'raiseStmt', _expr( node.expr1 ), _expr( node.expr2 ), _expr( node.expr3 ) ]
	

	# Break
	def Break(self, node):
		return [ 'breakStmt' ]
	
	
	# Continue
	def Continue(self, node):
		return [ 'continueStmt' ]
	
	
	# Import
	def Import(self, node):
		def nameImport(x):
			if x[1] is None:
				return [ 'moduleImport', x[0] ]
			else:
				return [ 'moduleImportAs', x[0], x[1] ]
		return [ 'importStmt' ]  +  [ nameImport( x )   for x in node.names ]
	
	def From(self, node):
		def nameImport(x):
			if x[1] is None:
				return [ 'moduleContentImport', x[0] ]
			else:
				return [ 'moduleContentImportAs', x[0], x[1] ]
		if node.names == [ ('*',None) ]:
			return [ 'fromImportAllStmt', [ 'relativeModule', node.modname ] ]
		else:
			return [ 'fromImportStmt', [ 'relativeModule', node.modname ] ]  +  [ nameImport( x )   for x in node.names ]
		
		
	# Global
	def Global(self, node):
		return [ 'globalStmt' ]  +  [ [ 'globalVar', name ]   for name in node.names ]
	
	
	# Exec
	def Exec(self, node):
		return [ 'execStmt', _expr( node.expr ), _expr( node.locals ), _expr( node.globals ) ]
	
	
	# Printnl
	def Printnl(self, node):
		return [ 'call', [ 'var', 'print' ] ]  +  [ _expr( x )   for x in node.nodes ]
	
	

	
	
class _CompoundStmtImporter (_Importer):
	def __call__(self, node, method=None):
		if method is None:
			name = node.__class__.__name__
			try:
				method = getattr( self, name )
			except AttributeError:
				return [ _stmt( node ) ]
		return method( node )

	
	# Statement list
	def Stmt(self, node):
		suite = []
		for x in node.nodes:
			suite.extend( _compound( x ) )
		return suite

	
	# If
	def If(self, node):
		def _test(x, bIf):
			if bIf:
				return [ 'ifStmt', _expr( x[0] ), _compound( x[1] ) ]
			else:
				return [ 'elifStmt', _expr( x[0] ), _compound( x[1] ) ]
		result = [ _test( node.tests[0], True ) ]
		for t in node.tests[1:]:
			result.append( _test( t, False ) )
		if node.else_ is not None:
			result.append( [ 'elseStmt', _compound( node.else_ ) ] )
		return result

	
	# While
	def While(self, node):
		result = [ [ 'whileStmt', _expr( node.test ), _compound( node.body ) ] ]
		if node.else_ is not None:
			result.append( [ 'elseStmt', _compound( node.else_ ) ] )
		return result
	
	
	
	
	



class _ModuleImporter (_Importer):
	# Module
	def Module(self, node):
		return [ 'python25Module' ]  +  _module( node.node )
	
	
	# Statement list
	def Stmt(self, node):
		suite = []
		for x in node.nodes:
			suite.extend( _compound( x ) )
		return suite

	
	
	
_target = _TargetImporter()
_expr = _ExprImporter()
_stmt = _StmtImporter()
_compound = _CompoundStmtImporter()
_module = _ModuleImporter()

	
	
def importPy25Source(source):
	importer = _ModuleImporter()
	tree = compiler.parse( source )
	return importer( tree )


def importPy25File(filename):
	importer = _ModuleImporter()
	tree = compiler.parseFile( filename )
	return importer( tree )



import unittest


class ImporterTestCase (unittest.TestCase):
	def _moduleTest(self, source, expectedResult):
		result = importPy25Source( source )
		if result != expectedResult:
			print 'EXPECTED:'
			print expectedResult
			print 'RESULT:'
			print result
		self.assert_( result == expectedResult )
		
		
	def _exprTest(self, source, expectedResult):
		result = importPy25Source( 'a+(' + source + ')\n' )
		result = result[1][2]
		if result != expectedResult:
			print 'EXPECTED:'
			print expectedResult
			print 'RESULT:'
			print result
		self.assert_( result == expectedResult )
		
	def _stmtTest(self, source, expectedResult):
		result = importPy25Source( source + '\n' )
		result = result[1]
		if result != expectedResult:
			print 'EXPECTED:'
			print expectedResult
			print 'RESULT:'
			print result
		self.assert_( result == expectedResult )
		
	def _compStmtTest(self, source, expectedResult):
		result = importPy25Source( source + '\n' )
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
	
	def testConst(self):
		self._exprTest( "'a'", [ 'stringLiteral', 'ascii', 'single', 'a' ] )
		self._exprTest( "u'a'", [ 'stringLiteral', 'unicode', 'single', u'a' ] )
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
		
		
		
	def testGenExpr(self):
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
		
		
	def testGetAttr(self):
		self._exprTest( 'a.b', [ 'attributeRef', [ 'var', 'a' ], 'b' ] )
		
		

	def testSubscript(self):
		self._exprTest( 'a[b]',  [ 'subscript', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( 'a[b,c]',  [ 'subscript', [ 'var', 'a' ], [ 'tupleLiteral', [ 'var', 'b' ], [ 'var', 'c' ] ] ] )
		
	def testSlice(self):	
		self._exprTest( 'a[b:c]',  [ 'subscript', [ 'var', 'a' ], [ 'subscriptSlice', [ 'var', 'b' ], [ 'var', 'c' ] ] ] )
		self._exprTest( 'a[b:]',  [ 'subscript', [ 'var', 'a' ], [ 'subscriptSlice', [ 'var', 'b' ], '<nil>' ] ] )
		self._exprTest( 'a[:c]',  [ 'subscript', [ 'var', 'a' ], [ 'subscriptSlice', '<nil>', [ 'var', 'c' ] ] ] )
		self._exprTest( 'a[b:c:d]',  [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', [ 'var', 'b' ], [ 'var', 'c' ], [ 'var', 'd' ] ] ] )
		self._exprTest( 'a[b:c:]',  [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', [ 'var', 'b' ], [ 'var', 'c' ], '<nil>' ] ] )
		self._exprTest( 'a[b::d]',  [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', [ 'var', 'b' ], '<nil>', [ 'var', 'd' ] ] ] )
		self._exprTest( 'a[:c:d]',  [ 'subscript', [ 'var', 'a' ], [ 'subscriptLongSlice', '<nil>', [ 'var', 'c' ], [ 'var', 'd' ] ] ] )
		self._exprTest( 'a[b:c,d:e]',  [ 'subscript', [ 'var', 'a' ], [ 'tupleLiteral', [ 'subscriptSlice', [ 'var', 'b' ], [ 'var', 'c' ] ], [ 'subscriptSlice', [ 'var', 'd' ], [ 'var', 'e' ] ] ] ] )
		self._exprTest( 'a[b:c,d:e:f]',  [ 'subscript', [ 'var', 'a' ], [ 'tupleLiteral', [ 'subscriptSlice', [ 'var', 'b' ], [ 'var', 'c' ] ], [ 'subscriptLongSlice', [ 'var', 'd' ], [ 'var', 'e' ], [ 'var', 'f' ] ] ] ] )
		
	def testEllipsis(self):
		self._exprTest( 'a[...]',  [ 'subscript', [ 'var', 'a' ], [ 'ellipsis' ] ] )
		
	

	def testCallFunc(self):
		self._exprTest( 'a()',   [ 'call', [ 'var', 'a' ] ] )
		self._exprTest( 'a(f)',   [ 'call', [ 'var', 'a' ], [ 'var', 'f' ] ] )
		self._exprTest( 'a(f,g=x)',   [ 'call', [ 'var', 'a' ], [ 'var', 'f' ], [ 'kwArg', 'g', [ 'var', 'x' ] ] ] )
		self._exprTest( 'a(f,g=x,*h)',   [ 'call', [ 'var', 'a' ], [ 'var', 'f' ], [ 'kwArg', 'g', [ 'var', 'x' ] ], [ 'argList', [ 'var', 'h' ] ] ] )
		self._exprTest( 'a(f,g=x,*h,**i)',   [ 'call', [ 'var', 'a' ], [ 'var', 'f' ], [ 'kwArg', 'g', [ 'var', 'x' ] ], [ 'argList', [ 'var', 'h' ] ], [ 'kwArgList', [ 'var', 'i' ] ] ] )
	
	
	def testOperators(self):
		self._exprTest( 'a ** b', [ 'pow', [ 'var', 'a' ], [ 'var', 'b' ] ] )
		self._exprTest( '~a', [ 'invert', [ 'var', 'a' ], ] )
		self._exprTest( '+a', [ 'pos', [ 'var', 'a' ], ] )
		self._exprTest( '-a', [ 'negate', [ 'var', 'a' ], ] )
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
		self._stmtTest( 'exec a in b', [ 'execStmt', [ 'var', 'a' ], [ 'var', 'b' ], '<nil>' ] )
		self._stmtTest( 'exec a in b,c', [ 'execStmt', [ 'var', 'a' ], [ 'var', 'b' ], [ 'var', 'c' ] ] )
		
		
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
