##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import string

from Britefury.Kernel.Abstract import abstractmethod




class PyCodeGenError (Exception):
	def __init__(self, dbgSrc):
		super( PyCodeGenError, self ).__init__()
		self.dbgSrc = dbgSrc
		
		
class PyInvalidNameError (PyCodeGenError):
	def __init__(self, dbgSrc, name):
		super( PyInvalidNameError, self ).__init__( dbgSrc )
		self.name = name

class PyInvalidVarNameError (PyInvalidNameError):
	pass


class PyInvalidAttrNameError (PyInvalidNameError):
	pass


class PyInvalidMethodNameError (PyInvalidNameError):
	pass


class PyInvalidFunctionNameError (PyInvalidNameError):
	pass


class PyInvalidArgNameError (PyInvalidNameError):
	pass


class PyInvalidUnaryOperatorError (PyCodeGenError):
	def __init__(self, dbgSrc, op):
		super( PyInvalidUnaryOperatorError, self ).__init__( dbgSrc )
		self.op = op


class PyInvalidBinaryOperatorError (PyCodeGenError):
	def __init__(self, dbgSrc, op):
		super( PyInvalidBinaryOperatorError, self ).__init__( dbgSrc )
		self.op = op



_binaryOperatorPrecedenceTable = {
	'**' : 1,
	'*' : 4,
	'/' : 4,
	'%' : 4,
	'+' : 5,
	'-' : 5,
	'<<' : 6,
	'>>' : 6,
	'&' : 7,
	'^' : 8,
	'|' : 9,
	'<' : 10,
	'<=' : 10,
	'==' : 10,
	'!=' : 10,
	'>=' : 10,
	'>' : 10,
	'is' : 11,
	'in' : 12,
	'and' : 14,
	'or' : 15,
}

_unaryOperatorPrecedenceTable = {
	'~' : ( 2, False ),
	'-' : ( 3, False ),
	'not' : ( 13, True ),
}



_pyIdentifierChars = string.ascii_letters + string.digits + '_'
_pyIdentifierStartChars = string.ascii_letters + '_'

def _isPyIdentifier(i):
	if len( i ) < 1:
		return False
	if i[0] not in _pyIdentifierStartChars:
		return False
	for c in i[1:]:
		if c not in _pyIdentifierChars:
			return False
	return True

def _isPyDottedIdentifier(i):
	parts = i.split( '.' )
	for p in parts:
		if not _isPyIdentifier( p ):
			return False
	return True




def _indent(pysrc):
	return [ '  ' + line   for line in pysrc ]

def _passBlock(pysrc):
	if len( pysrc ) == 0:
		return [ 'pass' ]
	else:
		return pysrc



class PyNode (object):
	def __init__(self, dbgSrc):
		self._dbgSrc = dbgSrc
		
		
	def error(self, exceptionClass, *args):
		raise exceptionClass( self._dbgSrc, *args )
		

	def compileAsExpr(self, outerPredecence=None):
		thisPrecedence = self._o_getPrecedence()
		src = self._o_compileAsExpr()
		if outerPredecence is not None  and  thisPrecedence is not None  and  outerPredecence <=thisPrecedence:
			return '(' + src + ')'
		else:
			return src
		
	@abstractmethod
	def _o_compileAsExpr(self):
		pass
	
	def _o_getPrecedence(self):
		return None
	
	def compileAsStmt(self):
		return [ self.compileAsExpr() ]
	
	def __cmp__(self, x):
		if type( self )  is  type( x ):
			return self._o_compareWith( x )
		else:
			return False
		
	def _o_compareWith(self, x):
		return 0
	
	
	
	
class PySrc (PyNode):
	def __init__(self, src, dbgSrc=None):
		super( PySrc, self ).__init__( dbgSrc )
		self.src = src
	
	def _o_compileAsExpr(self):
		return self.src
		
	def _o_compareWith(self, x):
		return cmp( self.src, x.src )
	
	
	
	
class PyVar (PyNode):
	def __init__(self, varName, dbgSrc=None):
		super( PyVar, self ).__init__( dbgSrc )
		if not _isPyDottedIdentifier( varName ):
			self.error( PyInvalidVarNameError, varName )
		self.varName = varName

	def _o_compileAsExpr(self):
		return self.varName
		
	def _o_compareWith(self, x):
		return cmp( self.varName, x.varName )
	
	
	
	
class PyLiteral (PyNode):
	def __init__(self, value, dbgSrc=None):
		super( PyLiteral, self ).__init__( dbgSrc )
		self.value = value
		
	def _o_compileAsExpr(self):
		return self.value
		
	def _o_compareWith(self, x):
		return cmp( self.value, x.value )
	
	
	
	
class PyListLiteral (PyNode):
	def __init__(self, subexps, dbgSrc=None):
		super( PyListLiteral, self ).__init__( dbgSrc )
		self.subexps = subexps
		
	def _o_compileAsExpr(self):
		return '[ ' + ', '.join( [ x.compileAsExpr()   for x in self.subexps ] )  +  ' ]'
		
	def _o_compareWith(self, x):
		return cmp( self.subexps, x.subexps )
	
	
	
	
class PyGetAttr (PyNode):
	def __init__(self, a, attrName, dbgSrc=None):
		super( PyGetAttr, self ).__init__( dbgSrc )
		if not _isPyIdentifier( attrName ):
			self.error( PyInvalidAttrNameError, attrName )
		self.a = a
		self.attrName = attrName

	def _o_compileAsExpr(self):
		return '%s.%s'  %  ( self.a.compileAsExpr(), self.attrName )
	
	def _o_compareWith(self, x):
		return cmp( ( self.a, self.attrName ),  ( x.a, x.attrName ) )
		
	
	

class PyGetItem (PyNode):
	def __init__(self, a, key, dbgSrc=None):
		super( PyGetItem, self ).__init__( dbgSrc )
		self.a = a
		self.key = key
		
	def _o_compileAsExpr(self):
		return '%s[%s]'  %  ( self.a.compileAsExpr(), self.key.compileAsExpr() )
	
	def _o_compareWith(self, x):
		return cmp( ( self.a, self.key ),  ( x.a, x.key ) )

	
	
	
class PyAssign_SideEffects (PyNode):
	def __init__(self, target, value, dbgSrc=None):
		super( PyAssign_SideEffects, self ).__init__( dbgSrc )
		self.target = target
		self.value =value
		
	def compileAsStmt(self):
		return [ '%s = %s'  %  ( self.target.compileAsExpr(), self.value.compileAsExpr() ) ]
	
	def _o_compareWith(self, x):
		return cmp( ( self.target, self.value ),  ( x.target, x.value ) )

	
	
	
class PyUnOp (PyNode):
	def __init__(self, a, op, dbgSrc=None):
		super( PyUnOp, self ).__init__( dbgSrc )
		if op not in _unaryOperatorPrecedenceTable:
			self.error( PyInvalidUnaryOperatorError, op )
		self.a = a
		self.op = op
		
	def _o_getPrecedence(self):
		return _unaryOperatorPrecedenceTable[self.op][0]

	def _o_compileAsExpr(self):
		thisPrecedence, bUseSpace = _unaryOperatorPrecedenceTable[self.op]
		space = ''
		if bUseSpace:
			space = ' '
		return '%s%s%s'  %  ( self.op, space, self.a.compileAsExpr( thisPrecedence ) )
	
	def _o_compareWith(self, x):
		return cmp( ( self.a, self.op ),  ( x.a, x.op ) )

	
	
		
class PyBinOp (PyNode):
	def __init__(self, a, b, op, dbgSrc=None):
		super( PyBinOp, self ).__init__( dbgSrc )
		if op not in _binaryOperatorPrecedenceTable:
			self.error( PyInvalidBinaryOperatorError, op )
		self.a = a
		self.b = b
		self.op = op
		
	def _o_getPrecedence(self):
		return _binaryOperatorPrecedenceTable[self.op]

	def _o_compileAsExpr(self):
		thisPrecedence = _binaryOperatorPrecedenceTable[self.op]
		return '%s %s %s'  %  ( self.a.compileAsExpr( thisPrecedence ), self.op, self.b.compileAsExpr( thisPrecedence ) )
	
	def _o_compareWith(self, x):
		return cmp( ( self.a, self.b, self.op ),  ( x.a, x.b, x.op ) )

	
	
	
class PyCall (PyNode):
	def __init__(self, a, params, dbgSrc=None):
		super( PyCall, self ).__init__( dbgSrc )
		self.a = a
		self.params = params
		
	def _o_compileAsExpr(self):
		return '%s( %s )'  %  ( self.a.compileAsExpr(), ', '.join( [ p.compileAsExpr()   for p in self.params ] ) )
		
	def _o_compareWith(self, x):
		return cmp( ( self.a, self.params ),  ( x.a, x.params ) )

	
	

class PyMethodCall (PyNode):
	def __init__(self, a, methodName, params, dbgSrc=None):
		super( PyMethodCall, self ).__init__( dbgSrc )
		if not _isPyIdentifier( methodName ):
			self.error( PyInvalidMethodNameError, methodName )
		self.a = a
		self.methodName = methodName
		self.params = params
		
	def _o_compileAsExpr(self):
		return '%s.%s( %s )'  %  ( self.a.compileAsExpr(), self.methodName, ', '.join( [ p.compileAsExpr()   for p in self.params ] ) )
	
	def _o_compareWith(self, x):
		return cmp( ( self.a, self.methodName, self.params ),  ( x.a, x.methodName, x.params ) )


	
	
class PyReturn (PyNode):
	def __init__(self, value, dbgSrc=None):
		super( PyReturn, self ).__init__( dbgSrc )
		self.value =value
		
	def compileAsStmt(self):
		return [ 'return %s'  %  ( self.value.compileAsExpr(), ) ]
	
	def _o_compareWith(self, x):
		return cmp( self.value, x.value )



class PyIf (PyNode):
	def __init__(self, ifElifSpecs, elseStatements=None, dbgSrc=None):
		"""
		ifElifSpecs is a list of tuples
		each tuple is a (condition, statement-list) pair
		
		elseStatements is a list of statements for the else clause, or None if one is not desired
		"""
		super( PyIf, self ).__init__( dbgSrc )
		
		assert isinstance( ifElifSpecs, list ), 'PyIf: if-elif specs must be a list'
		assert len( ifElifSpecs ) >= 1, 'PyIf: if-elif specs must contain at least 1 element'
		if elseStatements is not None:
			assert isinstance( elseStatements, list ), 'PyIf: else statements must be a list'
		for i in ifElifSpecs:
			assert isinstance( i, tuple ), 'PyIf: if-specification must be a tuple (condition, [statement*]); not a tuple'
			assert len( i ) == 2, 'PyIf: if-specification must be a tuple (condition, [statement*]); length != 2'
			assert isinstance( i[1], list ), 'PyIf: if-specification must be a tuple (condition, [statement*]); second element not a lit'
			
		self.ifElifSpecs = ifElifSpecs
		self.elseStatements = elseStatements
		
		
	def compileAsStmt(self):
		ifSrc = []
		bElif = False
		
		for i in self.ifElifSpecs:
			ifSrc.extend( self._p_compileIfBlock( i, bElif ) )
			bElif = True
		
		if self.elseStatements is not None:
			ifSrc.extend( self._p_compileElseBlock( self.elseStatements ) )
			
		return ifSrc
				

	def _o_compareWith(self, x):
		return -1


	def _p_compileIfBlock(self, t, bElif):
		condition, statements = t
		stmtSrc = []
		for s in statements:
			stmtSrc.extend( s.compileAsStmt() )
		stmtSrc = _passBlock( stmtSrc )
		
		if bElif:
			ifKeyword = 'elif'
		else:
			ifKeyword = 'if'
			
		return [ '%s %s:'  %  ( ifKeyword, condition.compileAsExpr() ) ]  +  _indent( stmtSrc )
	

	def _p_compileElseBlock(self, statements):
		stmtSrc = []
		for s in statements:
			stmtSrc.extend( s.compileAsStmt() )
		stmtSrc = _passBlock( stmtSrc )
		return [ 'else:' ]  +  _indent( stmtSrc )

	
	
class PyDef (PyNode):
	def __init__(self, name, argNames, statements, dbgSrc=None):
		super( PyDef, self ).__init__( dbgSrc )
		if not _isPyIdentifier( name ):
			self.error( PyInvalidFunctionNameError, name )
		for argName in argNames:
			if not _isPyIdentifier(argName ):
				self.error( PyInvalidArgNameError, name )
		self.name = name
		self.argNames = argNames
		self.statements = statements
		
	def compileAsStmt(self):
		stmtSrc = []
		
		for statement in self.statements:
			stmtSrc.extend( statement.compileAsStmt() )
		stmtSrc = _passBlock( stmtSrc )
	
		return [ 'def %s(%s):'  %  ( self.name, ', '.join( self.argNames ) ) ]  +  _indent( stmtSrc )
	
	def _o_compareWith(self, x):
		return -1




	
	


	
	
import unittest




class TestCase_PyCodeGen_IdentifierCheck (unittest.TestCase):
	def test_isPyIdentifier(self):
		self.assert_( _isPyIdentifier( 'abcXYZ123_' ) )
		self.assert_( _isPyIdentifier( '_abcXYZ123' ) )
		self.assert_( not _isPyIdentifier( '123abcXYZ123_' ) )
		self.assert_( not _isPyIdentifier( 'abcXYZ123_$' ) )
	
	def test_isPyDottedIdentifier(self):
		self.assert_( _isPyDottedIdentifier( 'abcXYZ123_' ) )
		self.assert_( _isPyDottedIdentifier( '_abcXYZ123' ) )
		self.assert_( _isPyDottedIdentifier( 'abcXY.Z123_' ) )
		self.assert_( not _isPyDottedIdentifier( 'abcXYZ123_$' ) )
		self.assert_( not _isPyDottedIdentifier( 'abcXYZ.123_' ) )
		self.assert_( not _isPyDottedIdentifier( 'abcX..YZ123_' ) )
		self.assert_( not _isPyDottedIdentifier( '.abcXYZ123_' ) )
		

class TestCase_PyCodeGen_Block (unittest.TestCase):
	def test_indent(self):
		self.assert_( _indent( [ 'a' ] )  ==  [ '  a' ] )
		self.assert_( _indent( [ 'a', 'b' ] )  ==  [ '  a' , '  b' ] )
		
	def test_passBlock(self):
		self.assert_( _passBlock( [ 'a' ] )  ==  [ 'a' ] )
		self.assert_( _passBlock( [] )  ==  [ 'pass' ] )
		

class TestCase_PyCodeGen_Node_check (unittest.TestCase):
	def test_PyVar(self):
		self.assertRaises( PyInvalidVarNameError, lambda: PyVar( '$' ) )
		self.assertRaises( PyInvalidVarNameError, lambda: PyVar( 'a..b' ) )
		PyVar( 'a.b' )

	def test_PyGetAttr(self):
		self.assertRaises( PyInvalidAttrNameError, lambda: PyGetAttr( PySrc( 'a' ), '$test' ) )
		
	def test_PyUnOp(self):
		self.assertRaises( PyInvalidUnaryOperatorError, lambda: PyUnOp( PySrc( 'a' ), '$' ) )
		PyUnOp( PySrc( 'a' ), '-' )
		PyUnOp( PySrc( 'a' ), '~' )
		PyUnOp( PySrc( 'a' ), 'not' )
		
	def test_PyBinOp(self):
		self.assertRaises( PyInvalidBinaryOperatorError, lambda: PyBinOp( PySrc( 'a' ), PySrc( 'b' ), '$' ) )
		for op in _binaryOperatorPrecedenceTable.keys():
			PyBinOp( PySrc( 'a' ), PySrc( 'b' ), op )

	def test_PyMethodCall(self):
		self.assertRaises( PyInvalidMethodNameError, lambda: PyMethodCall( PySrc( 'a' ), '$', [ PySrc( 'b' ), PySrc( 'c' ) ] ) )

	def test_PyIf(self):
		self.assertRaises( AssertionError, lambda: PyIf( 1 ) )
		self.assertRaises( AssertionError, lambda: PyIf( [],  ) )
		self.assertRaises( AssertionError, lambda: PyIf( [ ( PySrc( 'True' ), PySrc( 'pass' ) ) ], 1 ) )

		self.assertRaises( AssertionError, lambda: PyIf( [ 1 ] ) )
		self.assertRaises( AssertionError, lambda: PyIf( [ ( PySrc( 'True' ), [ PySrc( 'pass' ) ], PySrc( 'test' ) ) ] ) )
		
		PyIf( [ ( PySrc( 'True' ), [ PySrc( 'pass' ) ] ) ] )

	def test_PyDef(self):
		self.assertRaises( PyInvalidFunctionNameError, lambda: PyDef( 'a.b', [], [] ) )
		self.assertRaises( PyInvalidArgNameError, lambda: PyDef( 'a', [ 'b.c' ], [] ) )


		
class TestCase_PyCodeGen_Node_cmp (unittest.TestCase):
	def test_PySrc(self):
		self.assert_( PySrc( 'a' )  ==  PySrc( 'a' ) )
		self.assert_( PySrc( 'a' )  !=  PySrc( 'b' ) )
	
	def test_PyVar(self):
		self.assert_( PyVar( 'a' )  ==  PyVar( 'a' ) )
		self.assert_( PyVar( 'a' )  !=  PyVar( 'b' ) )

	def test_PyLiteral(self):
		self.assert_( PyLiteral( '1' )  ==  PyLiteral( '1' ) )
		self.assert_( PyLiteral( '1' )  !=  PyLiteral( '2' ) )
		
	def test_PyListLiteral(self):
		self.assert_( PyListLiteral( [ PySrc( 'a' ), PySrc( 'b' ) ] )  ==  PyListLiteral( [ PySrc( 'a' ), PySrc( 'b' ) ] ) )
		self.assert_( PyListLiteral( [ PySrc( 'a' ), PySrc( 'b' ) ] )  !=  PyListLiteral( [ PySrc( 'a' ), PySrc( 'c' ) ] ) )

	def test_PyGetAttr(self):
		self.assert_( PyGetAttr( PySrc( 'a' ), 'test' ) ==  PyGetAttr( PySrc( 'a' ), 'test' ) )
		self.assert_( PyGetAttr( PySrc( 'a' ), 'test' ) !=  PyGetAttr( PySrc( 'b' ), 'test' ) )
		self.assert_( PyGetAttr( PySrc( 'a' ), 'test' ) !=  PyGetAttr( PySrc( 'a' ), 'foo' ) )

	def test_PyGetItem(self):
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ) ) ==  PyGetItem( PySrc( 'a' ), PySrc( '1' ) ) )
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ) ) !=  PyGetItem( PySrc( 'b' ), PySrc( '1' ) ) )
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ) ) !=  PyGetItem( PySrc( 'a' ), PySrc( '2' ) ) )

	def test_PyAssign_SideEffects(self):
		self.assert_( PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ) ==  PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ) )
		self.assert_( PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ) !=  PyAssign_SideEffects( PySrc( 'b' ), PySrc( '1' ) ) )
		self.assert_( PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ) !=  PyAssign_SideEffects( PySrc( 'a' ), PySrc( '2' ) ) )

	def test_PyUnOp(self):
		self.assert_( PyUnOp( PySrc( 'a' ), '-' ) ==  PyUnOp( PySrc( 'a' ), '-' ) )
		self.assert_( PyUnOp( PySrc( 'a' ), '-' ) !=  PyUnOp( PySrc( 'b' ), '-' ) )
		self.assert_( PyUnOp( PySrc( 'a' ), '-' ) !=  PyUnOp( PySrc( 'a' ), '~' ) )

	def test_PyBinOp(self):
		for op in _binaryOperatorPrecedenceTable.keys():
			self.assert_( PyBinOp( PySrc( 'a' ), PySrc( 'b' ), op )  ==  PyBinOp( PySrc( 'a' ), PySrc( 'b' ), op ) )
		self.assert_( PyBinOp( PySrc( 'a' ), PySrc( 'b' ), '+' )  ==  PyBinOp( PySrc( 'a' ), PySrc( 'b' ), '+' ) )
		self.assert_( PyBinOp( PySrc( 'a' ), PySrc( 'b' ), '+' )  !=  PyBinOp( PySrc( 'x' ), PySrc( 'b' ), '+' ) )
		self.assert_( PyBinOp( PySrc( 'a' ), PySrc( 'b' ), '+' )  !=  PyBinOp( PySrc( 'a' ), PySrc( 'y' ), '+' ) )
		self.assert_( PyBinOp( PySrc( 'a' ), PySrc( 'b' ), '+' )  !=  PyBinOp( PySrc( 'a' ), PySrc( 'b' ), '-' ) )

	def test_PyCall(self):
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) ==  PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) )
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) !=  PyCall( PySrc( 'x' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) )
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) !=  PyCall( PySrc( 'a' ), [ PySrc( 'y' ), PySrc( 'c' ) ] ) )
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) !=  PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'z' ) ] ) )
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) !=  PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ), PySrc( 'w' ) ] ) )

	def test_PyMethodCall(self):
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) ==  PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) !=  PyMethodCall( PySrc( 'x' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) !=  PyMethodCall( PySrc( 'a' ), 'x', [ PySrc( 'c' ), PySrc( 'd' ) ] ) )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) !=  PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'x' ), PySrc( 'd' ) ] ) )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) !=  PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'x' ) ] ) )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) !=  PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ), PySrc( 'x' ) ] ) )
		
	def test_PyReturn(self):
		self.assert_( PyReturn( PySrc( '1' ) )  ==  PyReturn( PySrc( '1' ) ) )
		self.assert_( PyReturn( PySrc( '1' ) )  !=  PyReturn( PySrc( '2' ) ) )

	def test_PyIf(self):
		self.assert_( PyIf( [ ( PySrc( 'True' ), [ PySrc( '1' ) ] ) ] )  !=  PyIf( [ ( PySrc( 'True' ), [ PySrc( 'pass' ) ] ) ] ) )
		
	def test_PyDef(self):
		self.assert_( PyDef( 'foo', [ 'a', 'b' ], [] )  !=  PyDef( 'foo', [ 'a', 'b' ], [] ) )


		
class TestCase_PyCodeGen_Node_compile (unittest.TestCase):
	def test_PySrc(self):
		self.assert_( PySrc( 'a' ).compileAsExpr()  ==  'a' )
	
	def test_PyVar(self):
		self.assert_( PyVar( 'a' ).compileAsExpr()  ==  'a' )

	def test_PyLiteral(self):
		self.assert_( PyLiteral( '1' ).compileAsExpr()  ==  '1' )

	def test_PyListLiteral(self):
		self.assert_( PyListLiteral( [ PySrc( 'a' ), PySrc( 'b' ) ] ).compileAsExpr()  ==  '[ a, b ]' )
		
	def test_PyGetAttr(self):
		self.assert_( PyGetAttr( PySrc( 'a' ), 'test' ).compileAsExpr()  ==  'a.test' )
		
	def test_PyGetItem(self):
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ) ).compileAsExpr()  ==  'a[1]' )
		
	def test_PyAssign_SideEffects(self):
		self.assert_( PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ).compileAsStmt()  ==  [ 'a = 1' ] )

	def test_PyUnOp(self):
		self.assert_( PyUnOp( PySrc( 'a' ), '-' ).compileAsExpr()  ==  '-a' )
		self.assert_( PyUnOp( PySrc( 'a' ), '~' ).compileAsExpr()  ==  '~a' )
		self.assert_( PyUnOp( PySrc( 'a' ), 'not' ).compileAsExpr()  ==  'not a' )

	def test_PyBinOp(self):
		for op in _binaryOperatorPrecedenceTable.keys():
			self.assert_( PyBinOp( PySrc( 'a' ), PySrc( 'b' ), op ).compileAsExpr()  ==  'a %s b'  %  ( op, ) )
		
	def test_PyCall(self):
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ).compileAsExpr()  ==  'a( b, c )' )
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ), PySrc( 'd' ) ] ).compileAsExpr()  ==  'a( b, c, d )' )

	def test_PyMethodCall(self):
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ).compileAsExpr()  ==  'a.b( c, d )' )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ), PySrc( 'e' ) ] ).compileAsExpr()  ==  'a.b( c, d, e )' )
		
	def test_PyReturn(self):
		self.assert_( PyReturn( PySrc( '1' ) ).compileAsStmt()  ==  [ 'return 1' ] )

	def test_PyIf(self):
		pysrc1 = [
			'if True:',
			'  pass'
		]

		pysrc2 = [
			'if True:',
			'  pass',
			'elif False:',
			'  pass'		
		]

		pysrc3 = [
			'if True:',
			'  pass',
			'elif False:',
			'  pass',	
			'else:',
			'  pass'		
		]

		self.assert_( PyIf( [ ( PySrc( 'True' ),  [ PySrc( 'pass' ) ] ) ] ).compileAsStmt()  ==  pysrc1 )
		self.assert_( PyIf( [ ( PySrc( 'True' ),  [] ) ] ).compileAsStmt()  ==  pysrc1 )
		self.assert_( PyIf( [ ( PySrc( 'True' ),  [ PySrc( 'pass' ) ] ),   ( PySrc( 'False' ),  [ PySrc( 'pass' ) ] ) ] ).compileAsStmt()  ==  pysrc2 )
		self.assert_( PyIf( [ ( PySrc( 'True' ),  [ PySrc( 'pass' ) ] ),   ( PySrc( 'False' ),  [] ) ] ).compileAsStmt()  ==  pysrc2 )
		self.assert_( PyIf( [ ( PySrc( 'True' ),  [ PySrc( 'pass' ) ] ),   ( PySrc( 'False' ),  [ PySrc( 'pass' ) ] ) ],   [ PySrc( 'pass' ) ]  ).compileAsStmt()  ==  pysrc3 )
		self.assert_( PyIf( [ ( PySrc( 'True' ),  [ PySrc( 'pass' ) ] ),   ( PySrc( 'False' ),  [ PySrc( 'pass' ) ] ) ],   []  ).compileAsStmt()  ==  pysrc3 )

	def test_PyDef(self):
		pysrc1 = [
			'def foo():',
			'  pass'
		]

		pysrc2 = [
			'def foo(a, b):',
			'  pass'
		]
		
		self.assert_( PyDef( 'foo', [], [ PySrc( 'pass' ) ] ).compileAsStmt()  ==  pysrc1 )
		self.assert_( PyDef( 'foo', [], [] ).compileAsStmt()  ==  pysrc1 )
		self.assert_( PyDef( 'foo', [ 'a', 'b' ], [ PySrc( 'pass' ) ] ).compileAsStmt()  ==  pysrc2 )
		self.assert_( PyDef( 'foo', [ 'a', 'b' ], [] ).compileAsStmt()  ==  pysrc2 )

