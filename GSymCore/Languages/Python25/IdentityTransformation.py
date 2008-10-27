##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

from Britefury.Dispatch.MethodDispatch import methodDispatch
from Britefury.Util.NodeUtil import isNullNode

from Britefury.Transformation.TransformationInterface import TransformationInterface


def _checkedEval(xform, x):
	if isNullNode( x ):
		return x
	else:
		return xform( x )

def _unOp(xform, node, x):
	return [ node[0], xform( x ) ]

def _binOp(xform, node, x, y):
	return [ node[0], xform( x ), xform( y ) ]


class Python25IdentityTransformation (TransformationInterface):
	# MISC
	def blankLine(self, xform, node):
		return node


	def UNPARSED(self, xform, node, value):
		return node


	# String literal
	def stringLiteral(self, xform, node, format, quotation, value):
		return node


	# Integer literal
	def intLiteral(self, xform, node, format, numType, value):
		return node



	# Float literal
	def floatLiteral(self, xform, node, value):
		return node



	# Imaginary literal
	def imaginaryLiteral(self, xform, node, value):
		return node



	# Targets
	def singleTarget(self, xform, node, name):
		return node


	def tupleTarget(self, xform, node, *xs):
		return [ 'tupleTarget' ]  +  [ xform( x )   for x in xs ]

	
	def listTarget(self, xform, node, *xs):
		return [ 'listTarget' ]  +  [ xform( x )   for x in xs ]




	# Variable reference
	def var(self, xform, node, name):
		return node



	# Tuple literal
	def tupleLiteral(self, xform, node, *xs):
		return [ 'tupleLiteral' ]  +  [ xform( x )   for x in xs ]



	# List literal
	def listLiteral(self, xform, node, *xs):
		return [ 'listLiteral' ]  +  [ xform( x )   for x in xs ]



	# List comprehension
	def listFor(self, xform, node, target, source):
		return [ 'listFor', xform( target ), xform( source ) ]

	def listIf(self, xform, node, condition):
		return [ 'listIf', xform( condition ) ]

	def listComprehension(self, xform, node, expr, *xs):
		return [ 'listComprehension', xform( expr ) ]  +  [ xform( x )   for x in xs ]




	# Generator expression
	def genFor(self, xform, node, target, source):
		return [ 'genFor', xform( target ), xform( source ) ]

	def genIf(self, xform, node, condition):
		return [ 'genIf', xform( condition ) ]

	def generatorExpression(self, xform, node, expr, *xs):
		return [ 'generatorExpression', xform( expr ) ]  +  [ xform( x )   for x in xs ]




	# Dictionary literal
	def keyValuePair(self, xform, node, key, value):
		return [ 'keyValuePair', xform( key ), xform( value ) ]

	def dictLiteral(self, xform, node, *xs):
		return [ 'dictLiteral' ]  +  [ xform( x )   for x in xs ]


	# Yield expression
	def yieldExpr(self, xform, node, value):
		return [ 'yieldExpr', xform( value ) ]

	def yieldAtom(self, xform, node, value):
		return [ 'yieldAtom', xform( value ) ]



	# Attribute ref
	def attributeRef(self, xform, node, target, name):
		return [ 'attributeRef', xform( target ), name.toString() ]



	# Subscript
	def subscriptSlice(self, xform, node, x, y):
		return [ 'subscriptSlice', _checkedEval( xform, x ), _checkedEval( xform, y ) ]

	def subscriptLongSlice(self, xform, node, x, y, z):
		return [ 'subscriptLongSlice', _checkedEval( xform, x ), _checkedEval( xform, y ), _checkedEval( xform, z ) ]

	def ellipsis(self, xform, node):
		return node

	def subscriptTuple(self, xform, node, *xs):
		return [ 'subscriptTuple' ]  +  [ xform( x )   for x in xs ]

	def subscript(self, xform, node, target, index):
		return [ 'subscript', xform( target ), xform( index ) ]




	# Call
	def kwArg(self, xform, node, name, value):
		return [ 'kwArg', name.toString(), xform( value ) ]

	def argList(self, xform, node, value):
		return [ 'argList', xform( value ) ]

	def kwArgList(self, xform, node, value):
		return [ 'kwArgList', xform( value ) ]

	def call(self, xform, node, target, *args):
		return [ 'call', xform( target ) ]  +  [ xform( arg )   for arg in args ]




	# Operators
	def pow(self, xform, node, x, y):
		return _binOp( xform, node, x, y )


	def invert(self, xform, node, x):
		return _unOp( xform, node, x )

	def negate(self, xform, node, x):
		return _unOp( xform, node, x )

	def pos(self, xform, node, x):
		return _unOp( xform, node, x )


	def mul(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def div(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def mod(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	
	def add(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def sub(self, xform, node, x, y):
		return _binOp( xform, node, x, y )


	def lshift(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def rshift(self, xform, node, x, y):
		return _binOp( xform, node, x, y )


	def bitAnd(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def bitXor(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def bitOr(self, xform, node, x, y):
		return _binOp( xform, node, x, y )


	def lte(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def lt(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def gte(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def gt(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def eq(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def neq(self, xform, node, x, y):
		return _binOp( xform, node, x, y )


	def isNotTest(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def isTest(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def notInTest(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def inTest(self, xform, node, x, y):
		return _binOp( xform, node, x, y )


	def notTest(self, xform, node, x):
		return _unOp( xform, node, x )

	def andTest(self, xform, node, x, y):
		return _binOp( xform, node, x, y )

	def orTest(self, xform, node, x, y):
		return _binOp( xform, node, x, y )





	# Parameters
	def simpleParam(self, xform, node, name):
		return node

	def defaultValueParam(self, xform, node, name, value):
		return [ 'defaultValueParam', name.toString(), xform( value ) ]

	def paramList(self, xform, node, name):
		return node

	def kwParamList(self, xform, node, name):
		return node



	# Lambda expression
	def lambdaExpr(self, xform, node, params, expr):
		return [ 'lambdaExpr', [ xform( p )   for p in params ], xform( expr ) ]



	# Conditional expression
	def conditionalExpr(self, xform, node, condition, expr, elseExpr):
		return [ 'conditionalExpr', xform( condition ), xform( expr ), xform( elseExpr ) ]





	# Assert statement
	def assertStmt(self, xform, node, condition, fail):
		return [ 'assertStmt', xform( condition ), _checkedEval( xform, fail ) ]


	# Assignment statement
	def assignmentStmt(self, xform, node, targets, value):
		return [ 'assignmentStmt', [ xform( t )   for t in targets ], xform( value ) ]


	# Augmented assignment statement
	def augAssignStmt(self, xform, node, op, target, value):
		return [ 'augAssignStmt', op.toString(), xform( target ), xform( value ) ]


	# Pass statement
	def passStmt(self, xform, node):
		return node


	# Del statement
	def delStmt(self, xform, node, target):
		return [ 'delStmt', xform( target ) ]


	# Return statement
	def returnStmt(self, xform, node, value):
		return [ 'returnStmt', xform( value ) ]


	# Yield statement
	def yieldStmt(self, xform, node, value):
		return [ 'yieldStmt', xform( value ) ]


	# Raise statement
	def raiseStmt(self, xform, node, *xs):
		return [ 'raiseStmt' ]  +  [ _checkedEval( xform, x )   for x in xs ]


	# Break statement
	def breakStmt(self, xform, node):
		return node


	# Continue statement
	def continueStmt(self, xform, node):
		return node


	# Import statement
	def relativeModule(self, xform, node, name):
		return node
	
	def moduleImport(self, xform, node, name):
		return node
	
	def moduleImportAs(self, xform, node, name, asName):
		return node
	
	def moduleContentImport(self, xform, node, name):
		return node
	
	def moduleContentImportAs(self, xform, node, name, asName):
		return node
	
	def importStmt(self, xform, node, *xs):
		return [ 'importStmt' ]  +  [ xform( x )   for x in xs ]
	
	def fromImportStmt(self, xform, node, mod, *xs):
		return [ 'fromImportStmt', mod ]  +  [ xform( x )   for x in xs ]
	
	def fromImportAllStmt(self, xform, node, mod):
		return node


	# Global statement
	def globalVar(self, xform, node, name):
		return node
	
	def globalStmt(self, xform, node, *xs):
		return [ 'globalStmt' ]  +  [ xform( x )   for x in xs ]
	

	
	# Exec statement
	def execStmt(self, xform, node, src, loc, glob):
		return [ 'execStmt', xform( src ), _checkedEval( xform, loc ), _checkedEval( xform, glob ) ]


	
	
	
	# If statement
	def ifStmt(self, xform, node, condition, suite):
		return [ 'ifStmt', xform( condition ), [ xform( x )   for x in suite ] ]
	
	
	
	# Elif statement
	def elifStmt(self, xform, node, condition, suite):
		return [ 'elifStmt', xform( condition ), [ xform( x )   for x in suite ] ]
	
	
	
	# Else statement
	def elseStmt(self, xform, node, suite):
		return [ 'elseStmt', [ xform( x )   for x in suite ] ]
	
	
	# While statement
	def whileStmt(self, xform, node, condition, suite):
		return [ 'whileStmt', xform( condition ), [ xform( x )   for x in suite ] ]


	# For statement
	def forStmt(self, xform, node, target, source, suite):
		return [ 'forStmt', xform( target ), xform( source ), [ xform( x )   for x in suite ] ]
	
	

	# Try statement
	def tryStmt(self, xform, node, suite):
		return [ 'tryStmt', [ xform( x )   for x in suite ] ]
	
	
	
	# Except statement
	def exceptStmt(self, xform, node, exc, target, suite):
		return [ 'exceptStmt', _checkedEval( xform, exc ), _checkedEval( xform, target ), [ xform( x )   for x in suite ] ]

	
	
	# Finally statement
	def finallyStmt(self, xform, node, suite):
		return [ 'finallyStmt', [ xform( x )   for x in suite ] ]
	
	
	
	# With statement
	def withStmt(self, xform, node, expr, target, suite):
		return [ 'withStmt', xform( expr ), _checkedEval( xform, target ), [ xform( x )   for x in suite ] ]

	
	
	# Def statement
	def defStmt(self, xform, node, name, params, suite):
		return [ 'defStmt', name.toString(), [ xform( p )   for p in params ], [ xform( x )   for x in suite ] ]

	
	
	# Decorator statement
	def decoStmt(self, xform, node, name, args):
		if isNullNode( args ):
			a = args
		else:
			a = [ xform( a )   for a in args ]
		return [ 'decoStmt', name.toString(), a ]
	
	
	
	# Def statement
	def classStmt(self, xform, node, name, inheritance, suite):
		if isNullNode( inheritance ):
			hs = inheritance
		else:
			hs = [ xform( h )   for h in inheritance ]
		return [ 'classStmt', name.toString(), hs, [ xform( x )   for x in suite ] ]
	
	
	
	# Comment statement
	def commentStmt(self, xform, node, comment):
		return node


	
	# Module
	def python25Module(self, xform, node, *content):
		return [ 'python25Module' ]  +  [ xform( x )   for x in content ]


	
	


	
import unittest
from BritefuryJ.DocModel import DMIORead, DMIOWrite

class TestCase_Python25IdentityTransformation (unittest.TestCase):
	def _testSX(self, sx):
		try:
			data = DMIORead.readSX( sx )
		except DMIORead.ParseSXErrorException:
			print 'SX Parse error'
			self.fail()
		
		xform = Python25IdentityTransformation()
		result = xform.__apply__( data )
		
		resultSX = DMIOWrite.writeSX( result )
		
		if resultSX != sx:
			print 'TRANSFORMED DIFFERS FROM ORIGINAL'
			print 'ORIGINAL:'
			print sx
			print 'TRANSFORMED:'
			print resultSX
			
		self.assert_( resultSX == sx )
		
		
	def _binOpTest(self, sxOp):
		self._testSX( '(%s (var a) (var b))'  %  sxOp )
		
		
	def test_blankLine(self):
		self._testSX( '(blankLine)' )
		
		
	def test_UNPARSED(self):
		self._testSX( '(UNPARSED xyz)' )
		
		
	def test_stringLiteral(self):
		self._testSX( '(stringLiteral ascii single "Hi there")')
		
		
	def test_intLiteral(self):
		self._testSX( '(intLiteral decimal int 123)' )
		self._testSX( '(intLiteral hex int 1a4)' )
		self._testSX( '(intLiteral decimal long 123)' )
		self._testSX( '(intLiteral hex long 1a4)' )
		
		
	def test_floatLiteral(self):
		self._testSX( '(floatLiteral 123.0)' )
		
		
	def test_imaginaryLiteral(self):
		self._testSX( '(imaginaryLiteral 123j)' )
		
		
	def test_singleTarget(self):
		self._testSX( '(singleTarget a)' )
		
		
	def test_tupleTarget(self):
		self._testSX( '(tupleTarget (singleTarget a) (singleTarget b) (singleTarget c))' )
		
		
	def test_listTarget(self):
		self._testSX( '(listTarget (singleTarget a) (singleTarget b) (singleTarget c))' )
		
		
	def test_var(self):
		self._testSX( '(var a)' )
		
		
	def test_tupleLiteral(self):
		self._testSX( '(tupleLiteral (var a) (var b) (var c))' )
		
		
	def test_listLiteral(self):
		self._testSX( '(listLiteral (var a) (var b) (var c))' )
		
		
	def test_listFor(self):
		self._testSX( '(listFor (singleTarget x) (var xs))' )
		
		
	def test_listIf(self):
		self._testSX( '(listIf (var a))' )
		
		
	def test_listComprehension(self):
		self._testSX( '(listComprehension (var a) (listFor (singleTarget a) (var xs)) (listIf (var a)))' )
		
		
	def test_genFor(self):
		self._testSX( '(genFor (singleTarget x) (var xs))' )
		
		
	def test_genIf(self):
		self._testSX( '(genIf (var a))' )
		
		
	def test_generatorExpression(self):
		self._testSX( '(generatorExpression (var a) (genFor (singleTarget a) (var xs)) (genIf (var a)))' )
		
		
	def test_keyValuePair(self):
		self._testSX( '(keyValuePair (var a) (var b))' )
	
		
	def test_dictLiteral(self):
		self._testSX( '(dictLiteral (keyValuePair (var a) (var b)) (keyValuePair (var c) (var d)))' )
		
	
	def test_yieldExpr(self):
		self._testSX( '(yieldExpr (var a))' )
		
		
	def test_yieldAtom(self):
		self._testSX( '(yieldAtom (var a))' )
		
		
	def test_attributeRef(self):
		self._testSX( '(attributeRef (var a) b)' )
		
		
	def test_subscript(self):
		self._testSX( '(subscript (var a) (var b))' )
		
		
	def test_subscript_ellipsis(self):
		self._testSX( '(subscript (var a) (ellipsis))' )
		
		
	def test_subscript_slice(self):
		self._testSX( '(subscript (var a) (subscriptSlice (var a) (var b)))' )
		self._testSX( '(subscript (var a) (subscriptSlice (var a) <nil>))' )
		self._testSX( '(subscript (var a) (subscriptSlice <nil> (var b)))' )
		self._testSX( '(subscript (var a) (subscriptSlice <nil> <nil>))' )
		

	def test_subscript_longSlice(self):
		self._testSX( '(subscript (var a) (subscriptLongSlice (var a) (var b) (var c)))' )
		self._testSX( '(subscript (var a) (subscriptLongSlice (var a) (var b) <nil>))' )
		self._testSX( '(subscript (var a) (subscriptLongSlice (var a) <nil> (var c)))' )
		self._testSX( '(subscript (var a) (subscriptLongSlice (var a) <nil> <nil>))' )
		self._testSX( '(subscript (var a) (subscriptLongSlice <nil> (var b) (var c)))' )
		self._testSX( '(subscript (var a) (subscriptLongSlice <nil> (var b) <nil>))' )
		self._testSX( '(subscript (var a) (subscriptLongSlice <nil> <nil> (var c)))' )
		self._testSX( '(subscript (var a) (subscriptLongSlice <nil> <nil> <nil>))' )

		
	def test_subscript_tuple(self):
		self._testSX( '(subscript (var a) (subscriptTuple (var a) (var b)))' )
		
		
	def test_call(self):
		self._testSX( '(call (var x) (var a) (var b) (kwArg c (var d)) (kwArg e (var f)) (argList (var g)) (kwArgList (var h)))' )
		
		
	def test_operators(self):
		self._binOpTest( 'pow' )
		self._testSX( '(invert (var a))' )
		self._testSX( '(negate (var a))' )
		self._testSX( '(pos (var a))' )
		self._binOpTest( 'mul' )
		self._binOpTest( 'div' )
		self._binOpTest( 'mod' )
		self._binOpTest( 'add' )
		self._binOpTest( 'sub' )
		self._binOpTest( 'lshift' )
		self._binOpTest( 'rshift' )
		self._binOpTest( 'bitAnd' )
		self._binOpTest( 'bitXor' )
		self._binOpTest( 'bitOr' )
		self._binOpTest( 'lte' )
		self._binOpTest( 'lt' )
		self._binOpTest( 'gte' )
		self._binOpTest( 'gt' )
		self._binOpTest( 'eq' )
		self._binOpTest( 'neq' )
		self._binOpTest( 'isTest' )
		self._binOpTest( 'isNotTest' )
		self._binOpTest( 'inTest' )
		self._binOpTest( 'notInTest' )
		self._testSX( '(notTest (var a))' )
		self._binOpTest( 'andTest' )
		self._binOpTest( 'orTest' )
		
		
	def test_lambdaExpr(self):
		self._testSX( '(lambdaExpr ((simpleParam a) (simpleParam b) (defaultValueParam c (var d)) (defaultValueParam e (var f)) (paramList g) (kwParamList h)) (var a))' )
	
		
	def test_conditionalExpr(self):
		self._testSX( '(conditionalExpr (var b) (var a) (var c))' )
		
		
		
	def test_assertStmt(self):
		self._testSX( '(assertStmt (var x) <nil>)' )
		self._testSX( '(assertStmt (var x) (var y))' )
		
		
	def test_assignmentStmt(self):
		self._testSX( '(assignmentStmt ((singleTarget x)) (var a))' )
		self._testSX( '(assignmentStmt ((singleTarget x) (singleTarget y)) (var a))' )
		
		
	def test_augAssignStmt(self):
		self._testSX( '(augAssignStmt += (singleTarget x) (var a))' )
		
		
	def test_passStmt(self):
		self._testSX( '(passStmt)' )
		
		
	def test_delStmt(self):
		self._testSX( '(delStmt (singleTarget a))' )
		
		
	def test_returnStmt(self):
		self._testSX( '(returnStmt (var a))' )
		
		
	def test_yieldStmt(self):
		self._testSX( '(yieldStmt (var a))' )
		
		
	def test_raiseStmt(self):
		self._testSX( '(raiseStmt <nil> <nil> <nil>)' )
		self._testSX( '(raiseStmt (var a) <nil> <nil>)' )
		self._testSX( '(raiseStmt (var a) (var b) <nil>)' )
		self._testSX( '(raiseStmt (var a) (var b) (var c))' )
		
		
	def test_breakStmt(self):
		self._testSX( '(breakStmt)' )
		
		
	def test_continueStmt(self):
		self._testSX( '(continueStmt)' )
		
		
	def test_importStmt(self):
		self._testSX( '(importStmt (moduleImport a))' )
		self._testSX( '(importStmt (moduleImport a.b))' )
		self._testSX( '(importStmt (moduleImportAs a x))' )
		self._testSX( '(importStmt (moduleImportAs a.b x))' )
		
		
	def test_fromImportStmt(self):
		self._testSX( '(fromImportStmt (relativeModule x) (moduleContentImport a))' )
		self._testSX( '(fromImportStmt (relativeModule x) (moduleContentImportAs a p))' )
		self._testSX( '(fromImportStmt (relativeModule x) (moduleContentImportAs a p) (moduleContentImportAs b q))' )
		
		
	def test_fromImportAllStmt(self):
		self._testSX( '(fromImportAllStmt (relativeModule x))' )
		
		
	def test_globalStmt(self):
		self._testSX( '(globalStmt (globalVar a))' )
		self._testSX( '(globalStmt (globalVar a) (globalVar b))' )
		
		
	def test_execStmt(self):
		self._testSX( '(execStmt (var a) <nil> <nil>)' )
		self._testSX( '(execStmt (var a) (var b) <nil>)' )
		self._testSX( '(execStmt (var a) (var b) (var c))' )
		
		
		
	def test_ifStmt(self):
		self._testSX( '(ifStmt (var bA) ((var b)))' )


	def test_elifStmt(self):
		self._testSX( '(elifStmt (var bA) ((var b)))' )


	def test_elseStmt(self):
		self._testSX( '(elseStmt ((var b)))' )


	def test_whileStmt(self):
		self._testSX( '(whileStmt (var bA) ((var b)))' )


	def test_forStmt(self):
		self._testSX( '(forStmt (var a) (var b) ((var c)))' )


	def test_tryStmt(self):
		self._testSX( '(tryStmt ((var b)))' )


	def test_exceptStmt(self):
		self._testSX( '(exceptStmt <nil> <nil> ((var b)))' )
		self._testSX( '(exceptStmt (var a) <nil> ((var b)))' )
		self._testSX( '(exceptStmt (var a) (var x) ((var b)))' )


	def test_finallyStmt(self):
		self._testSX( '(finallyStmt ((var b)))' )


	def test_withStmt(self):
		self._testSX( '(withStmt (var a) <nil> ((var b)))' )
		self._testSX( '(withStmt (var a) (var x) ((var b)))' )


	def test_defStmt(self):
		self._testSX( '(defStmt myFunc ((simpleParam a) (defaultValueParam b (var c)) (paramList d) (kwParamList e)) ((var b)))' )


	def test_decoStmt(self):
		self._testSX( '(decoStmt myDeco <nil>)' )
		self._testSX( '(decoStmt myDeco ((var a) (var b)))' )

		
	def test_classStmt(self):
		self._testSX( '(classStmt A <nil> ((var b)))' )
		self._testSX( '(classStmt A ((var object)) ((var b)))' )
		self._testSX( '(classStmt A ((var object) (var Q)) ((var b)))' )



	def test_commentStmt(self):
		self._testSX( '(commentStmt HelloWorld)' )
