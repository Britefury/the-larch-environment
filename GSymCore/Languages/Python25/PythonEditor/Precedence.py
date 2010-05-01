##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.util import List

from BritefuryJ.Transformation import DefaultIdentityTransformationFunction, Transformation, TransformationFunction

from BritefuryJ.DocModel import DMObjectInterface, DMListInterface, DMClassAttribute

from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeMethodDispatchMetaClass, ObjectNodeDispatchMethod, objectNodeMethodDispatch
from Britefury.Dispatch.Dispatch import DispatchError
from Britefury.Util.NodeUtil import isStringNode

from GSymCore.Languages.Python25 import Schema


PRECEDENCE_NONE = None
PRECEDENCE_UNPARSED = None
PRECEDENCE_COMMENT = None
PRECEDENCE_STMT = 1000
PRECEDENCE_EXPR = 0
PRECEDENCE_TARGET = None


PRECEDENCE_CONDITIONAL = 100

PRECEDENCE_LAMBDAEXPR = 50

PRECEDENCE_OR = 14
PRECEDENCE_AND = 13
PRECEDENCE_NOT = 12
PRECEDENCE_CMP = 9
PRECEDENCE_BITOR = 8
PRECEDENCE_BITXOR = 7
PRECEDENCE_BITAND = 6
PRECEDENCE_SHIFT = 5
PRECEDENCE_ADDSUB = 4
PRECEDENCE_MULDIVMOD = 3
PRECEDENCE_INVERT_NEGATE_POS = 2
PRECEDENCE_POW = 1
PRECEDENCE_CALL = 0
PRECEDENCE_SUBSCRIPT = 0
PRECEDENCE_ATTR = 0

PRECEDENCE_LITERALVALUE = 0
PRECEDENCE_LOAD = 0
PRECEDENCE_TUPLE = 0
PRECEDENCE_LISTDISPLAY = 0
PRECEDENCE_GENERATOREXPRESSION = 0
PRECEDENCE_DICTDISPLAY = 0
PRECEDENCE_YIELDEXPR = 200

PRECEDENCE_CONTAINER_ELEMENT = 500
PRECEDENCE_CONTAINER_ATTRIBUTEREFTARGET = 0
PRECEDENCE_CONTAINER_SUBSCRIPTTARGET = 0
PRECEDENCE_CONTAINER_SUBSCRIPTINDEX = 500
PRECEDENCE_CONTAINER_CALLTARGET = 0
PRECEDENCE_CONTAINER_CALLARG = 500

# The lambda expression should only expect orTest or lambda expression
PRECEDENCE_CONTAINER_YIELDEXPR = 199

# The lambda expression should only expect orTest or lambda expression
PRECEDENCE_CONTAINER_LAMBDAEXPR = 50

# The condition and true-expression subtrees of a conditional expression should only expect orTest
PRECEDENCE_CONTAINER_CONDITIONALEXPR = 25

# The comprehension for statements should only expect orTest
PRECEDENCE_CONTAINER_COMPREHENSIONFOR = 25

# The comprehension if statements should only expect orTest or lambda expression
PRECEDENCE_CONTAINER_COMPREHENSIONIF = 50


PRECEDENCE_CONTAINER_UNPARSED = None


PRECEDENCE_IMPORTCONTENT = 0




precedence = DMClassAttribute( 'precedence', Schema.Node )
precedence[Schema.CommentStmt] = PRECEDENCE_COMMENT
precedence[Schema.BlankLine] = PRECEDENCE_COMMENT
precedence[Schema.UNPARSED] = PRECEDENCE_UNPARSED
precedence[Schema.Target] = PRECEDENCE_TARGET

precedence[Schema.Stmt] = PRECEDENCE_STMT
precedence[Schema.Expr] = PRECEDENCE_EXPR

precedence[Schema.Pow] = PRECEDENCE_POW
precedence[Schema.Invert] = PRECEDENCE_INVERT_NEGATE_POS
precedence[Schema.Negate] = PRECEDENCE_INVERT_NEGATE_POS
precedence[Schema.Pos] = PRECEDENCE_INVERT_NEGATE_POS
precedence[Schema.Mul] = PRECEDENCE_MULDIVMOD
precedence[Schema.Div] = PRECEDENCE_MULDIVMOD
precedence[Schema.Mod] = PRECEDENCE_MULDIVMOD
precedence[Schema.Add] = PRECEDENCE_ADDSUB
precedence[Schema.Sub] = PRECEDENCE_ADDSUB
precedence[Schema.LShift] = PRECEDENCE_SHIFT
precedence[Schema.RShift] = PRECEDENCE_SHIFT
precedence[Schema.BitAnd] = PRECEDENCE_BITAND
precedence[Schema.BitXor] = PRECEDENCE_BITXOR
precedence[Schema.BitOr] = PRECEDENCE_BITOR
precedence[Schema.Cmp] = PRECEDENCE_CMP
precedence[Schema.NotTest] = PRECEDENCE_NOT
precedence[Schema.AndTest] = PRECEDENCE_AND
precedence[Schema.OrTest] = PRECEDENCE_OR
precedence[Schema.LambdaExpr] = PRECEDENCE_LAMBDAEXPR
precedence[Schema.ConditionalExpr] = PRECEDENCE_CONDITIONAL
precedence[Schema.YieldExpr] = PRECEDENCE_YIELDEXPR
precedence.commit()

	
	
rightAssociative = DMClassAttribute( 'rightAssociative', Schema.BinOp )
rightAssociative[Schema.BinOp] = False
rightAssociative[Schema.Pow] = True
rightAssociative.commit()



_identity = DefaultIdentityTransformationFunction()
def _updatedNodeCopy(node, xform, **fieldValues):
	newNode = _identity( node, xform )
	newNode.update( fieldValues )
	return newNode
		

	
def _areParensRequired(childNode, outerPrecedence):
	childPrec = precedence[childNode]
	return childPrec is not None   and   outerPrecedence is not None   and   childPrec > outerPrecedence
	
def getNumParens(node):
	try:
		p = node['parens']
	except KeyError:
		print 'Attempted to get number of parens for %s'  %  node
		raise
	
	numParens = 0
	if p is not None   and   isStringNode( p ):
		p = str( p )
		try:
			numParens = int( p )
		except ValueError:
			pass
	return numParens

def _decrementParens(node, xform):
	numParens = getNumParens( node )
	numParens -= 1
	numParens = max( numParens, 0 )
	p = str( numParens )   if numParens > 0   else   None
	return _updatedNodeCopy( node, xform, parens=p )


def _computeBinOpContainmentPrecedenceValues(precedence, bRightAssociative):
	if bRightAssociative:
		return precedence - 1, precedence
	else:
		return precedence, precedence - 1
	
def _transformBinOp(node, xform):
	outerPrec = precedence[node]
	bRightAssociative = rightAssociative[node]
	
	xPrec, yPrec = _computeBinOpContainmentPrecedenceValues( outerPrec, bRightAssociative )
	x = node['x']
	y = node['y']
	bXParens = _areParensRequired( x, xPrec )
	bYParens = _areParensRequired( y, yPrec )
	if bXParens  or  bYParens:
		if bXParens:
			x = _decrementParens( x, xform )
		else:
			x = xform( x, xform )
		if bYParens:
			y = _decrementParens( y, xform )
		else:
			y = xform( y, xform )
		result = _updatedNodeCopy( node, xform, x=x, y=y )
		return result
	else:
		raise DispatchError
	

def _transformOp(node, xform, outerPrec, fieldName):
	value = node[fieldName]
	bParens = _areParensRequired( value, outerPrec )
	if bParens:
		value = _decrementParens( value, xform )
		result = _updatedNodeCopy( node, xform, **{ fieldName : value } )
		return result
	else:
		raise DispatchError


def _transformOpMulti(node, xform, outerPrec, fieldNames):
	values = [ node[fieldName]   for fieldName in fieldNames ]
	bParens = [ _areParensRequired( value, outerPrec )   for value in values ]
	bNoParens = True
	for bParen in bParens:
		if bParen:
			bNoParens = False
			break
	if bNoParens:
		raise DispatchError
	else:
		values = [ ( _decrementParens( value, xform )   if bParen   else   value )   for value, bParen in zip( values, bParens ) ]
		m = {}
		for fieldName, value in zip( fieldNames, values ):
			m[fieldName] = value
		return _updatedNodeCopy( node, xform, **m )

	
def _transformUnaryOp(node, xform):
	outerPrec = precedence[node]
	return _transformOp( node, xform, outerPrec, 'x' )


def _transformCmpOp(node, xform):
	outerPrec = precedence[node]
	return _transformOp( node, xform, outerPrec, 'y' )

	
def _transformCmp(node, xform):
	outerPrec = precedence[node]
	return _transformOp( node, xform, outerPrec, 'x' )

	
	
class RemoveUnNeededParensXform (object):
	__metaclass__ = ObjectNodeMethodDispatchMetaClass
	__dispatch_num_args__ = 1
	
	
	def __call__(self, node, xform):
		try:
			return objectNodeMethodDispatch( self, node, xform )
		except DispatchError:
			return TransformationFunction.cannotApplyTransformationValue
	
	@ObjectNodeDispatchMethod( Schema.ComprehensionFor )
	def ComprehensionFor(self, xform, node):
		return _transformOp( node, xform, PRECEDENCE_CONTAINER_COMPREHENSIONFOR, 'source' )
		
	@ObjectNodeDispatchMethod( Schema.ComprehensionIf )
	def ComprehensionIf(self, xform, node):
		return _transformOp( node, xform, PRECEDENCE_CONTAINER_COMPREHENSIONIF, 'condition' )
	
	@ObjectNodeDispatchMethod( Schema.AttributeRef )
	def AttributeRef(self, xform, node):
		return _transformOp( node, xform, PRECEDENCE_CONTAINER_ATTRIBUTEREFTARGET, 'target' )
	
	@ObjectNodeDispatchMethod( Schema.Subscript )
	def Subscript(self, xform, node):
		return _transformOp( node, xform, PRECEDENCE_CONTAINER_SUBSCRIPTTARGET, 'target' )
	
	@ObjectNodeDispatchMethod( Schema.Call )
	def Call(self, xform, node):
		return _transformOp( node, xform, PRECEDENCE_CONTAINER_CALLTARGET, 'target' )
	
	@ObjectNodeDispatchMethod( Schema.BinOp )
	def BinOp(self, xform, node):
		return _transformBinOp( node, xform )
	
	@ObjectNodeDispatchMethod( Schema.UnaryOp )
	def UnaryOp(self, xform, node):
		return _transformUnaryOp( node, xform )
	
	@ObjectNodeDispatchMethod( Schema.Cmp )
	def Cmp(self, xform, node):
		return _transformCmp( node, xform )
	
	@ObjectNodeDispatchMethod( Schema.CmpOp )
	def CmpOp(self, xform, node):
		return _transformCmpOp( node, xform )
	
	@ObjectNodeDispatchMethod( Schema.LambdaExpr )
	def LambdaExpr(self, xform, node):
		return _transformOp( node, xform, PRECEDENCE_CONTAINER_LAMBDAEXPR, 'expr' )
	
	@ObjectNodeDispatchMethod( Schema.ConditionalExpr )
	def ConditionalExpr(self, xform, node):
		return _transformOpMulti( node, xform, PRECEDENCE_CONTAINER_CONDITIONALEXPR, [ 'expr', 'condition' ] )

	
_removeParensXform = Transformation( _identity, [ RemoveUnNeededParensXform() ] )

def _removeUnNeededParensFromObject(node, outerPrecedence):
	if node.isInstanceOf( Schema.SimpleStmt )  or  node.isInstanceOf( Schema.CompountStmtHeader )  or  node.isInstanceOf( Schema.Expr ):
		x = _removeParensXform( DMNode.coerce( node ) )
		bParens = _areParensRequired( x, outerPrecedence )
		if bParens:
			x = _decrementParens( x, None )
		return DMNode.coerce( x )
	else:
		return node
	
def removeUnNeededParens(node, outerPrecedence):
	if isinstance( node, DMObjectInterface ):
		return _removeUnNeededParensFromObject( node, outerPrecedence )
	elif isinstance( node, List ):
		return [ removeUnNeededParens( x, outerPrecedence )   for x in node ]
	else:
		raise TypeError, 'Cannot apply removeUnNeededParens to %s'  %  ( type( node ), )
	

	

	
import unittest
from BritefuryJ.DocModel import DMNode
from GSymCore.Languages.Python25.PythonEditor import Parser


class Test_Precedence (unittest.TestCase):
	def _parseStringTest(self, parser, input, expected):
		result = parser.parseStringChars( input )
		
		if not result.isValid():
			print 'PARSE FAILURE while parsing %s, stopped at %d: %s'  %  ( input, result.end, input[:result.end] )
			print 'EXPECTED:'
			print expected
		self.assert_( result.isValid() )
		
		value = removeUnNeededParens( result.value, None )
		
		expected = DMNode.coerce( expected )

		if result.end != len( input ):
			print 'INCOMPLETE PARSE while parsing', input
			print 'Parsed %d/%d characters'  %  ( result.end, len( input ) )
			print input[:result.end]
			print 'EXPECTED:'
			print expected
			print 'RESULT:'
			print value

		bSame = value == expected
		if not bSame:
			print 'EXPECTED:'
			print expected
			print ''
			print 'RESULT:'
			print value
		self.assert_( bSame )

		
		
	def setUp(self):
		self._parser = Parser.Python25Grammar()
	
	def tearDown(self):
		self._parser = None
		
		
	def test_Load(self):
		self._parseStringTest( self._parser.expression(), 'a', Schema.Load( name='a' ) )
		self._parseStringTest( self._parser.expression(), '(a)', Schema.Load( parens='1', name='a' ) )	
		
	def test_BinOp(self):
		self._parseStringTest( self._parser.expression(), '(a+b)', Schema.Add( parens='1', x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )	
		self._parseStringTest( self._parser.expression(), '(((a+b)))', Schema.Add( parens='3', x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )	
		self._parseStringTest( self._parser.expression(), '(a*b)+c', Schema.Add( x=Schema.Mul( parens='1', x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Load( name='c' ) ) )
		self._parseStringTest( self._parser.expression(), '(a+b)*c', Schema.Mul( x=Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Load( name='c' ) ) )
		self._parseStringTest( self._parser.expression(), '(a+b)/(a+b)', Schema.Div( x=Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) ) )
		self._parseStringTest( self._parser.expression(), '(a+b)/(c+d)+e', Schema.Add( x=Schema.Div( x=Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Add( x=Schema.Load( name='c' ), y=Schema.Load( name='d' ) ) ), y=Schema.Load( name='e' ) ) )
		self._parseStringTest( self._parser.expression(), '((a+b)/(a+b)+c)*x', Schema.Mul( x=Schema.Add( x=Schema.Div( x=Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) ), y=Schema.Load( name='c' ) ), y=Schema.Load( name='x' ) ) )
		self._parseStringTest( self._parser.expression(), '(a/b)*(c+d)', Schema.Mul( x=Schema.Div( parens='1', x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Add( x=Schema.Load( name='c' ), y=Schema.Load( name='d' ) ) ) )
		self._parseStringTest( self._parser.expression(), '-(a+b)', Schema.Negate( x=Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) ) )	
		self._parseStringTest( self._parser.expression(), '-(a/b)', Schema.Negate( x=Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) ) )	
		self._parseStringTest( self._parser.expression(), '-a*b', Schema.Mul( x=Schema.Negate( x=Schema.Load( name='a' ) ), y=Schema.Load( name='b' ) ) )
		self._parseStringTest( self._parser.expression(), '-(a/b)*c', Schema.Mul( x=Schema.Negate( x=Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) ), y=Schema.Load( name='c' ) ) )
		self._parseStringTest( self._parser.expression(), '-(a/b)*(c+d)', Schema.Mul( x=Schema.Negate( x=Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) ), y=Schema.Add( x=Schema.Load( name='c' ), y=Schema.Load( name='d' ) ) ) )
		
		
	def test_LambdaConditional(self):
		self._parseStringTest( self._parser.expression(), 'lambda: (x if y else z)', Schema.LambdaExpr( params=[], expr=Schema.ConditionalExpr( condition=Schema.Load( name='y' ), expr=Schema.Load( name='x' ), elseExpr=Schema.Load( name='z' ) ) ) )
		self._parseStringTest( self._parser.expression(), 'lambda: x if y else z', Schema.LambdaExpr( params=[], expr=Schema.ConditionalExpr( condition=Schema.Load( name='y' ), expr=Schema.Load( name='x' ), elseExpr=Schema.Load( name='z' ) ) ) )
		self._parseStringTest( self._parser.expression(), '(lambda: x) if y else z', Schema.ConditionalExpr( condition=Schema.Load( name='y' ), expr=Schema.LambdaExpr( params=[], expr=Schema.Load( name='x' ) ), elseExpr=Schema.Load( name='z' ) ) )
		self._parseStringTest( self._parser.expression(), 'x if (lambda: y) else z', Schema.ConditionalExpr( condition=Schema.LambdaExpr( params=[], expr=Schema.Load( name='y' ) ), expr=Schema.Load( name='x' ), elseExpr=Schema.Load( name='z' ) ) )
		
	def test_Statement(self):
		self._parseStringTest( self._parser.singleLineStatement(), '((a+b)/(a+b)+c)*x\n', Schema.ExprStmt( expr=Schema.Mul( x=Schema.Add( x=Schema.Div( x=Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) ), y=Schema.Load( name='c' ) ), y=Schema.Load( name='x' ) ) ) )
		self._parseStringTest( self._parser.singleLineStatement(), 'y=((a+b)/(a+b)+c)*x\n', Schema.AssignStmt( targets=[ Schema.SingleTarget( name='y' ) ], value=Schema.Mul( x=Schema.Add( x=Schema.Div( x=Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) ), y=Schema.Load( name='c' ) ), y=Schema.Load( name='x' ) ) ) )
		self._parseStringTest( self._parser.singleLineStatement(), 'y=(((a+b))/(a+b)+c)*x\n', Schema.AssignStmt( targets=[ Schema.SingleTarget( name='y' ) ], value=Schema.Mul( x=Schema.Add( x=Schema.Div( x=Schema.Add( parens='1', x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) ), y=Schema.Load( name='c' ) ), y=Schema.Load( name='x' ) ) ) )
		self._parseStringTest( self._parser.singleLineStatement(), 'x=(a+b)/(c+d)+e\n', Schema.AssignStmt( targets=[ Schema.SingleTarget( name='x' ) ], value=Schema.Add( x=Schema.Div( x=Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Add( x=Schema.Load( name='c' ), y=Schema.Load( name='d' ) ) ), y=Schema.Load( name='e' ) ) ) )
		self._parseStringTest( self._parser.singleLineStatement(), 'x=a/(b+c+d)\n', Schema.AssignStmt( targets=[ Schema.SingleTarget( name='x' ) ], value=Schema.Div( x=Schema.Load( name='a' ), y=Schema.Add( x=Schema.Add( x=Schema.Load( name='b' ), y=Schema.Load( name='c' ) ), y=Schema.Load( name='d' ) ) ) ) )

