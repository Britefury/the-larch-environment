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
	params.extend( [ [ 'kwParam', name, self( value ) ]   for name, value in zip( node.argnames[numSimpleParams:-numListParams], node.defaults ) ] )
	if bParamList:
		params.append( [ 'paramList', node.argnames[-(numListParams-1)] ] )
	if bKWParamList:
		params.append( [ 'kwParamList', node.argnames[-1] ] )
	return params

	
class _ExprImporter (_Importer):
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
		return [ 'attributeRef', self( node.expr ), node.attrname ]
	
	def AssTuple(self, node):
		return [ 'tupleTarget' ]  +  [ self( x )   for x in node.nodes ]
	
	def AssList(self, node):
		return [ 'listTarget' ]  +  [ self( x )   for x in node.nodes ]
	
	
	
	# Tuple literal
	def Tuple(self, node):
		return [ 'tupleLiteral' ]  +  [ self( x )   for x in node.nodes ]
	
	
	
	# List literal
	def List(self, node):
		return [ 'listLiteral' ]  +  [ self( x )   for x in node.nodes ]
	
	
	
	# List comprehension
	def ListCompIf(self, node):
		return [ 'listIf', self( node.test ) ]
	
	def ListCompFor(self, node):
		return [ [ 'listFor', self( node.assign ), self( node.list ) ] ]  +  [ self( x )   for x in node.ifs ]
	
	def ListComp(self, node):
		quals = []
		for x in node.quals:
			quals.extend( self( x ) )
		return [ 'listComprehension', self( node.expr ) ]   +   quals
	
	
	
	# Generator expression
	def GenExprIf(self, node):
		return [ 'genIf', self( node.test ) ]
	
	def GenExprFor(self, node):
		return [ [ 'genFor', self( node.assign ), self( node.iter ) ] ]  +  [ self( x )   for x in node.ifs ]
	
	def GenExprInner(self, node):
		quals = []
		for x in node.quals:
			quals.extend( self( x ) )
		return [ 'generatorExpression', self( node.expr ) ]   +   quals
	
	def GenExpr(self, node):
		return self( node.code )
	
	
	
	# Dictionary literal
	def Dict(self, node):
		return [ 'dictLiteral' ]  +  [ [ 'keyValuePair', self( x[0] ), self( x[1] ) ]   for x in node.items ]
	
	
	
	# Yield expresion
	def Yield(self, node):
		return [ 'yieldAtom', self( node.value ) ]
	
	
	
	# Attribute ref
	def Getattr(self, node):
		return [ 'attributeRef', self( node.expr ), node.attrname ]
		
	
	
	# Subscript	
	def Subscript(self, node):
		if len( node.subs ) == 1:
			s = self( node.subs[0] )
		else:
			s = [ 'tupleLiteral' ]  +  [ self( x )   for x in node.subs ]
		return [ 'subscript', self( node.expr ), s ]
	
	def Slice(self, node):
		_s = lambda x: self( x )   if x  is not None   else   '<nil>'
		return [ 'subscript', self( node.expr ), [ 'subscriptSlice', _s( node.lower ), _s( node.upper ) ] ]
	
	def Sliceobj(self, node):
		def _s(x):
			if x is not None:
				s = self( x )
				return s   if s is not None   else   '<nil>'
			else:
				return '<nil>'
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
		_starArg = lambda name, x:   [ [ name, self( x ) ] ]   if x is not None   else   []
		return [ 'call', self( node.node ) ]  +  [ self( x )   for x in node.args ]  +   _starArg( 'argList', node.star_args )  +  _starArg( 'kwArgList', node.dstar_args )

	def Keyword(self, node):
		return [ 'kwArg', node.name, self( node.expr ) ]
			
				
	
	
	# Operators
	def Power(self, node):
		return [ 'pow', self( node.left ), self( node.right ) ]
	
	def Invert(self, node):
		return [ 'invert', self( node.expr ) ]
	
	def UnaryAdd(self, node):
		return [ 'pos', self( node.expr ) ]
	
	def UnarySub(self, node):
		return [ 'negate', self( node.expr ) ]
	
	def Mul(self, node):
		return [ 'mul', self( node.left ), self( node.right ) ]

	def Div(self, node):
		return [ 'div', self( node.left ), self( node.right ) ]

	def Mod(self, node):
		return [ 'mod', self( node.left ), self( node.right ) ]
	
	def Add(self, node):
		return [ 'add', self( node.left ), self( node.right ) ]
	
	def Sub(self, node):
		return [ 'sub', self( node.left ), self( node.right ) ]
	
	def LeftShift(self, node):
		return [ 'lshift', self( node.left ), self( node.right ) ]
	
	def RightShift(self, node):
		return [ 'rshift', self( node.left ), self( node.right ) ]
	
	def Bitand(self, node):
		return reduce( lambda left, right: [ 'bitAnd', left, self( right ) ],   node.nodes[1:],  self( node.nodes[0] ) )
	
	def Bitxor(self, node):
		return reduce( lambda left, right: [ 'bitXor', left, self( right ) ],   node.nodes[1:],  self( node.nodes[0] ) )
	
	def Bitor(self, node):
		return reduce( lambda left, right: [ 'bitOr', left, self( right ) ],   node.nodes[1:],  self( node.nodes[0] ) )
	
	def Compare(self, node):
		result = [ _cmpOps[node.ops[0][0]], self( node.expr ), self( node.ops[0][1] ) ]
		prev = node.ops[0][1]
		if len( node.ops ) > 1:
			for op in node.ops[1:]:
				result = [ 'andTest', result, [ _cmpOps[op[0]], self( prev ), self( op[1] ) ] ]
				prev = op
		return result
	
	def Not(self, node):
		return [ 'notTest', self( node.expr ) ]
	
	def And(self, node):
		return reduce( lambda left, right: [ 'andTest', left, self( right ) ],   node.nodes[1:],  self( node.nodes[0] ) )
	
	def Or(self, node):
		return reduce( lambda left, right: [ 'orTest', left, self( right ) ],   node.nodes[1:],  self( node.nodes[0] ) )
	

	
	# Lambda expression
	def Lambda(self, node):
		params = _extractParameters( node )
		return [ 'lambdaExpr', params, self( node.code ) ]
	
	
	
	
class _StmtImporter (_Importer):
	def __init__(self):
		self._exprImporter = _ExprImporter()
	
		
	# Discard (expression statement)
	def Discard(self, node):
		return self._exprImporter( node.expr )
	
	


class _ModuleImporter (_Importer):
	def __init__(self):
		self._stmtImporter = _StmtImporter()

	
	# Module
	def Module(self, node):
		return [ 'python25Module' ]  +  self( node.node )
	
	
	# Statement list
	def Stmt(self, node):
		return [ self._stmtImporter( x )   for x in node.nodes ]

	
	
	
	
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
		
		
	def _stmtTest(self, source, expectedResult):
		result = importPy25Source( source + '\n' )
		result = result[1]
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
		self._exprTest( 'lambda a: x', [ 'lambdaExpr', [ [ 'simpleParam', 'a' ] ], [ 'var', 'x' ] ] )
		
	