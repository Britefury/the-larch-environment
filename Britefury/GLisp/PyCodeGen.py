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

class PyXArgMustFollowNormalArgsError (PyInvalidNameError):
	pass

class PyKWArgMustBeLastError (PyInvalidNameError):
	pass


class PyInvalidUnaryOperatorError (PyCodeGenError):
	def __init__(self, dbgSrc, op):
		super( PyInvalidUnaryOperatorError, self ).__init__( dbgSrc )
		self.op = op


class PyInvalidBinaryOperatorError (PyCodeGenError):
	def __init__(self, dbgSrc, op):
		super( PyInvalidBinaryOperatorError, self ).__init__( dbgSrc )
		self.op = op


_LEFT = 1
_RIGHT = 2
		
		


_callPrecedence = 1
_methodCallPrecedence = 1
_subscriptPrecedence = 1
_attributePrecedence = 1

_binaryOperatorPrecedenceTable = {
	'**' : 4,
	'*' : 7,
	'/' : 7,
	'%' : 7,
	'+' : 8,
	'-' : 8,
	'<<' : 9,
	'>>' : 9,
	'&' : 10,
	'^' : 11,
	'|' : 12,
	'<' : 13,
	'<=' : 13,
	'==' : 13,
	'!=' : 13,
	'>=' : 13,
	'>' : 13,
	'is' : 14,
	'in' : 15,
	'and' : 17,
	'or' : 18,
}

_unaryOperatorPrecedenceTable = {
	'~' : ( 5, False ),
	'-' : ( 6, False ),
	'not' : ( 16, True ),
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


def filterIdentifierForPy(identifier):
	result = ''
	for c in identifier:
		if _isPyIdentifier( result + c ):
			result += c
	return result




def _indent(pysrc):
	return [ '  ' + line   for line in pysrc ]

def _passBlock(pysrc):
	if len( pysrc ) == 0:
		return [ 'pass' ]
	else:
		return pysrc

	

	
	
	
	
#
#
#
# Coerce and Compare
#
#
#
	
def pyt_coerce(x):
	if isinstance( x, PyNode ):
		return x
	elif x is None  or  isinstance( x, bool )  or  isinstance( x, int )  or  isinstance( x, long )  or  isinstance( x, float )  or  isinstance( x, str )  or  isinstance( x, unicode ):
		return PyLiteralValue( x )
	elif isinstance( x, list ):
		return PyListLiteral( [ pyt_coerce( item )   for item in x ] )
	else:
		raise TypeError, 'could not coerce into PyNode'
	
	
def pyt_compare(x, y):
	if isinstance( x, PyNode )  and  isinstance( y, PyNode ):
		return x.compare( y )
	elif isinstance( x, list )  and  isinstance( y, list ):
		if len( x ) != len( y ):
			return False
		for a, b in zip( x, y ):
			if not pyt_compare( a, b ):
				return False
		return True
	elif not isinstance( x, PyNode )  and  not isinstance( y, PyNode ):
		return x == y
	else:
		return False

	
	




#
#
#
# Node classes
#
#
#



class PyNode (object):
	def __init__(self, dbgSrc):
		self._dbgSrc = dbgSrc
		
		
	def error(self, exceptionClass, *args):
		raise exceptionClass( self._dbgSrc, *args )
		

	def compare(self, x):
		if type( self )  is  type( x ):
			return self._o_compareWith( x )
		else:
			return False
		
	def _o_compareWith(self, x):
		return True
	
	def getChildren(self):
		return []
	
	
	def debug(self, dbgSrc):
		if self._dbgSrc is None:
			self._dbgSrc = dbgSrc
			for child in self.getChildren():
				child.debug( dbgSrc )
		return self
	
	
	def getVariablesAccessed(self):
		vars = set()
		for child in self.getChildren():
			vars = vars.union( child.getVariablesAccessed() )
		return vars
	
	
	

class PyKWParam (PyNode):
	def __init__(self, name, value, dbgSrc=None):
		super( PyKWParam, self ).__init__( dbgSrc )
		assert isinstance( name, str )
		assert _isPyIdentifier( name )
		self.name = name
		self.value = value

	def _o_compareWith(self, x):
		return self.name == x.name  and  pyt_compare( self.value, x.value )



class PyStatement (PyNode):
	@abstractmethod
	def compileAsStmt(self):
		pass
	


class PyExpression (PyStatement):
	def compileAsExpr(self, outerPredecence=None, position=_RIGHT):
		thisPrecedence = self._o_getPrecedence()
		src = self._o_compileAsExpr()
		if outerPredecence is not None  and  thisPrecedence is not None:
			if position == _LEFT:
				if outerPredecence < thisPrecedence:
					return '(' + src + ')'
			else:
				if outerPredecence <= thisPrecedence:
					return '(' + src + ')'
		return src
		
	@abstractmethod
	def _o_compileAsExpr(self):
		pass
	
	def _o_getPrecedence(self):
		return None
	
	
	def compileAsStmt(self):
		return [ self.compileAsExpr() ]

	
	# Get attribute
	def attr(self, name):
		return PyGetAttr( self, name )
	
	
	# Identity test
	def is_(self, x):
		return PyBinOp( self, 'is', pyt_coerce( x ) )
		
	
	# Boolean operators
	def and_(self, x):
		return PyBinOp( self, 'and', pyt_coerce( x ) )

	def or_(self, x):
		return PyBinOp( self, 'or', pyt_coerce( x ) )
		
	def not_(self):
		return PyUnOp( 'not', self )

	
	# Unary operators
	def __neg__(self):
		return PyUnOp( '-', self )
	def __invert__(self):
		return PyUnOp( '~', self )
	
	# Binary operators
	def __add__(self, x):
		return PyBinOp( self, '+', pyt_coerce( x ) )
	def __sub__(self, x):
		return PyBinOp( self, '-', pyt_coerce( x ) )
	def __mul__(self, x):
		return PyBinOp( self, '*', pyt_coerce( x ) )
	def __div__(self, x):
		return PyBinOp( self, '/', pyt_coerce( x ) )
	def __mod__(self, x):
		return PyBinOp( self, '%', pyt_coerce( x ) )
	def __pow__(self, x):
		return PyBinOp( self, '**', pyt_coerce( x ) )
	def __lshift__(self, x):
		return PyBinOp( self, '<<', pyt_coerce( x ) )
	def __rshift__(self, x):
		return PyBinOp( self, '>>', pyt_coerce( x ) )
	def __and__(self, x):
		return PyBinOp( self, '&', pyt_coerce( x ) )
	def __or__(self, x):
		return PyBinOp( self, '|', pyt_coerce( x ) )
	def __xor__(self, x):
		return PyBinOp( self, '^', pyt_coerce( x ) )
	
	
	# Comparison operators
	def __lt__(self, x):
		return PyBinOp( self, '<', pyt_coerce( x ) )
	def __le__(self, x):
		return PyBinOp( self, '<=', pyt_coerce( x ) )
	def __eq__(self, x):
		return PyBinOp( self, '==', pyt_coerce( x ) )
	def __ne__(self, x):
		return PyBinOp( self, '!=', pyt_coerce( x ) )
	def __gt__(self, x):
		return PyBinOp( self, '>', pyt_coerce( x ) )
	def __ge__(self, x):
		return PyBinOp( self, '>=', pyt_coerce( x ) )

	
	# Container operators
	def len_(self):
		return PyCall( PyVar( 'len' ), [ self ] )
	
	def in_(self, x):
		return PyBinOp( self, 'in', pyt_coerce( x ) )
	
	def __getitem__(self, i):
		if isinstance( i, slice ):
			start = stop = step = None
			if i.start is not None:
				start = pyt_coerce( i.start )
			if i.stop is not None:
				stop = pyt_coerce( i.stop )
			if i.step is not None:
				step = pyt_coerce( i.step )
			return PyGetSlice( self, start, stop, step )
		else:
			return PyGetItem( self, pyt_coerce( i ) )
		
		
	# Call
	def __call__(self, *params):
		return PyCall( self, [ self._p_coerceCallParam( p )   for p in params ] )
	
	
	# Method call
	def methodCall(self, methodName, *params):
		return PyMethodCall( self, methodName, [ self._p_coerceCallParam( p )   for p in params ] )
	
	
	def isinstance_(self, typ):
		return PyIsInstance( self, typ )
	
	
	# Return
	def return_(self):
		return PyReturn( self )
	
	
	# Raise
	def raise_(self):
		return PyRaise( self )
	
	
	# If
	def ifTrue(self, statements):
		return PySimpleIf( self, statements )
	
	
	# Assignment
	def assignTo_sideEffects(self, target):
		return PyAssign_SideEffects( target, self )
	
	def assignToVar_sideEffects(self, varName):
		return PyAssign_SideEffects( PyVar( varName ), self )
	
	
	def _p_coerceCallParam(self, p):
		if isinstance( p, PyKWParam ):
			return PyKWParam( p.name, pyt_coerce( p.value ) )
		else:
			return pyt_coerce( p )

	


	
	
	
	
class PySrc (PyExpression):
	def __init__(self, src, dbgSrc=None):
		super( PySrc, self ).__init__( dbgSrc )
		self.src = src
	
	def _o_compileAsExpr(self):
		return self.src
		
	def _o_compareWith(self, x):
		return self.src == x.src
	
	
	
	
class PyVar (PyExpression):
	def __init__(self, varName, dbgSrc=None):
		super( PyVar, self ).__init__( dbgSrc )
		if not _isPyDottedIdentifier( varName ):
			self.error( PyInvalidVarNameError, varName )
		self.varName = varName

	def _o_compileAsExpr(self):
		return self.varName
		
	def _o_compareWith(self, x):
		return self.varName == x.varName
	
	
	def assign_sideEffects(self, value):
		return PyAssign_SideEffects( self, pyt_coerce( value ) )
	
	def del_sideEffects(self):
		return PyDel_SideEffects( self )
	

	def getVariablesAccessed(self):
		return set( [ self.varName ] )

	
	
class PyLiteral (PyExpression):
	def __init__(self, value, dbgSrc=None):
		super( PyLiteral, self ).__init__( dbgSrc )
		self.value = value
		
	def _o_compileAsExpr(self):
		return self.value
		
	def _o_compareWith(self, x):
		return self.value == x.value	
	

	
	
class PyLiteralValue (PyExpression):
	def __init__(self, value, dbgSrc=None):
		super( PyLiteralValue, self ).__init__( dbgSrc )
		self.value = value
		
	def _o_compileAsExpr(self):
		return repr( self.value )
		
	def _o_compareWith(self, x):
		return self.value == x.value



class PyListLiteral (PyExpression):
	def __init__(self, subexps, dbgSrc=None):
		super( PyListLiteral, self ).__init__( dbgSrc )
		for e in subexps:
			assert isinstance( e, PyExpression )
		self.subexps = subexps
		
	def _o_compileAsExpr(self):
		if len( self.subexps ) == 0:
			return '[]'
		else:
			return '[ ' + ', '.join( [ x.compileAsExpr()   for x in self.subexps ] )  +  ' ]'
		
	def _o_compareWith(self, x):
		return pyt_compare( self.subexps, x.subexps )
	
	def getChildren(self):
		return self.subexps
	
	
	
class PyDictLiteral (PyExpression):
	def __init__(self, keyValuePairs, dbgSrc=None):
		super( PyDictLiteral, self ).__init__( dbgSrc )
		for key, value in keyValuePairs:
			assert isinstance( key, PyExpression )
			assert isinstance( value, PyExpression )
		self.keyValuePairs = keyValuePairs
		
	def _o_compileAsExpr(self):
		return '{ ' + ', '.join( [ key.compileAsExpr() + ' : ' + value.compileAsExpr()   for key, value in self.keyValuePairs ] )  +  ' }'
		
	def _o_compareWith(self, x):
		return pyt_compare( self.keyValuePairs, x.keyValuePairs )
	
	def getChildren(self):
		children = []
		for kv in self.keyValuePairs:
			children.extend( kv )
		return children
	
	
	
class PyListComprehension (PyExpression):
	def __init__(self, itemExpr, itemName, srcIterableExpr, filterExpr=None, dbgSrc=None):
		super( PyListComprehension, self ).__init__( dbgSrc )
		assert isinstance( itemExpr, PyExpression )
		assert isinstance( srcIterableExpr, PyExpression )
		assert isinstance( itemName, str )
		assert filterExpr is None  or  isinstance( filterExpr, PyExpression )

		self.itemExpr = itemExpr
		self.itemName = itemName
		self.srcIterableExpr = srcIterableExpr
		self.filterExpr = filterExpr
		
	def _o_compileAsExpr(self):
		if self.filterExpr is None:
			return '[ %s   for %s in %s ]'  %  ( self.itemExpr.compileAsExpr(), self.itemName, self.srcIterableExpr.compileAsExpr() )
		else:
			return '[ %s   for %s in %s   if %s ]'  %  ( self.itemExpr.compileAsExpr(), self.itemName, self.srcIterableExpr.compileAsExpr(), self.filterExpr.compileAsExpr() )
		
	def _o_compareWith(self, x):
		return pyt_compare( self.itemExpr, x.itemExpr )  and  pyt_compare( self.srcIterableExpr, x.srcIterableExpr )  and  pyt_compare( self.filterExpr, x.filterExpr )  and  self.itemName == x.itemName
	
	def getChildren(self):
		if self.filterExpr is None:
			return [ self.itemExpr, self.srcIterableExpr ]
		else:
			return [ self.itemExpr, self.srcIterableExpr, self.filterExpr ]

	
	
class PyGetAttr (PyExpression):
	def __init__(self, a, attrName, dbgSrc=None):
		super( PyGetAttr, self ).__init__( dbgSrc )
		assert isinstance( a, PyExpression )
		if not _isPyIdentifier( attrName ):
			self.error( PyInvalidAttrNameError, attrName )
		self.a = a
		self.attrName = attrName

	def _o_compileAsExpr(self):
		return '%s.%s'  %  ( self.a.compileAsExpr( _attributePrecedence, _LEFT ), self.attrName )
	
	def _o_getPrecedence(self):
		return _attributePrecedence
	
	def _o_compareWith(self, x):
		return pyt_compare( self.a, x.a )  and  self.attrName == x.attrName
		
	def getChildren(self):
		return [ self.a ]
	
	

class PyGetItem (PyExpression):
	def __init__(self, a, key, dbgSrc=None):
		super( PyGetItem, self ).__init__( dbgSrc )
		assert isinstance( a, PyExpression )
		assert isinstance( key, PyExpression )
		self.a = a
		self.key = key
		
	def _o_compileAsExpr(self):
		return '%s[%s]'  %  ( self.a.compileAsExpr( _subscriptPrecedence, _LEFT ), self.key.compileAsExpr() )
	
	def _o_getPrecedence(self):
		return _subscriptPrecedence
	
	def _o_compareWith(self, x):
		return pyt_compare( self.a, x.a )  and  pyt_compare( self.key, x.key )

	def getChildren(self):
		return [ self.a, self.key ]
	
	
	
class PyGetSlice (PyExpression):
	def __init__(self, a, start, stop=None, step=None, dbgSrc=None):
		super( PyGetSlice, self ).__init__( dbgSrc )
		assert isinstance( a, PyExpression )
		assert start is None  or  isinstance( start, PyExpression )
		assert stop is None  or  isinstance( stop, PyExpression )
		assert step is None  or  isinstance( step, PyExpression )
		self.a = a
		self.start = start
		self.stop = stop
		self.step = step
		
	def _o_compileAsExpr(self):
		start = stop = step = ''
		a = self.a.compileAsExpr( _subscriptPrecedence, _LEFT )
		if self.start is not None:
			start = self.start.compileAsExpr()
		if self.stop is not None:
			stop = self.stop.compileAsExpr()
		if self.step is not None:
			step = self.step.compileAsExpr()
			
		if self.step is None:
			if self.stop is None:
				return '%s[%s:]'  %  ( a, start )
			else:
				return '%s[%s:%s]'  %  ( a, start, stop )
		else:
			return '%s[%s:%s:%s]'  %  ( a, start, stop, step )
	
	def _o_getPrecedence(self):
		return _subscriptPrecedence
	
	def _o_compareWith(self, x):
		return pyt_compare( self.a, x.a )  and  pyt_compare( self.start, x.start)  and  pyt_compare( self.stop, x.stop)  and  pyt_compare( self.step, x.step)

	def getChildren(self):
		children = [ self.a ]
		if self.start is not None:
			children.append( self.start )
		if self.stop is not None:
			children.append( self.stop )
		if self.step is not None:
			children.append( self.step )
		return children
	
	
	
class PyUnOp (PyExpression):
	operators = _unaryOperatorPrecedenceTable.keys()
	
	def __init__(self, op, a, dbgSrc=None):
		super( PyUnOp, self ).__init__( dbgSrc )
		assert isinstance( a, PyExpression )
		if op not in _unaryOperatorPrecedenceTable:
			self.error( PyInvalidUnaryOperatorError, op )
		self.op = op
		self.a = a
		
	def _o_compileAsExpr(self):
		thisPrecedence, bUseSpace = _unaryOperatorPrecedenceTable[self.op]
		space = ''
		if bUseSpace:
			space = ' '
		return '%s%s%s'  %  ( self.op, space, self.a.compileAsExpr( thisPrecedence, _RIGHT ) )
	
	def _o_getPrecedence(self):
		return _unaryOperatorPrecedenceTable[self.op][0]

	def _o_compareWith(self, x):
		return pyt_compare( self.a, x.a )  and  self.op == x.op

	def getChildren(self):
		return [ self.a ]
	
	
		
class PyBinOp (PyExpression):
	operators = _binaryOperatorPrecedenceTable.keys()
	
	def __init__(self, a, op, b, dbgSrc=None):
		super( PyBinOp, self ).__init__( dbgSrc )
		assert isinstance( a, PyExpression )
		assert isinstance( b, PyExpression )
		if op not in _binaryOperatorPrecedenceTable:
			self.error( PyInvalidBinaryOperatorError, op )
		self.a = a
		self.op = op
		self.b = b
		
	def _o_compileAsExpr(self):
		thisPrecedence = _binaryOperatorPrecedenceTable[self.op]
		return '%s %s %s'  %  ( self.a.compileAsExpr( thisPrecedence, _LEFT ), self.op, self.b.compileAsExpr( thisPrecedence, _RIGHT ) )
	
	def _o_getPrecedence(self):
		return _binaryOperatorPrecedenceTable[self.op]

	def _o_compareWith(self, x):
		return pyt_compare( self.a, x.a )  and  pyt_compare( self.b, x.b )  and  self.op == x.op

	def getChildren(self):
		return [ self.a, self.b ]
	
	
	

def _checkCallParamList(params):
	bKeywordParam = False
	for p in params:
		if isinstance( p, PyKWParam ):
			assert isinstance( p.value, PyExpression )
			bKeywordParam = True
		else:
			assert isinstance( p, PyExpression )
			assert not bKeywordParam
	

class PyCall (PyExpression):
	def __init__(self, a, params, dbgSrc=None):
		super( PyCall, self ).__init__( dbgSrc )
		assert isinstance( a, PyExpression )
		_checkCallParamList( params )
		self.a = a
		self.params = params
		
	def _o_compileAsExpr(self):
		if len( self.params ) == 0:
			return '%s()'  %  ( self.a.compileAsExpr( _callPrecedence, _LEFT ), )
		else:
			return '%s( %s )'  %  ( self.a.compileAsExpr( _callPrecedence, _LEFT ), ', '.join( [ self._p_compileParam( p )   for p in self.params ] ) )
		
	def _o_compareWith(self, x):
		return pyt_compare( self.a, x.a )  and  pyt_compare( self.params, x.params )
	
	def getChildren(self):
		return [ self.a ]  +  self.params
	
	def _p_compileParam(self, p):
		if isinstance( p, PyKWParam ):
			return '%s=%s'  %  ( p.name, p.value.compileAsExpr() )
		else:
			return p.compileAsExpr()

	

class PyMethodCall (PyExpression):
	def __init__(self, a, methodName, params, dbgSrc=None):
		super( PyMethodCall, self ).__init__( dbgSrc )
		assert isinstance( a, PyExpression )
		_checkCallParamList( params )
		if not _isPyIdentifier( methodName ):
			print methodName
			self.error( PyInvalidMethodNameError, methodName )
		self.a = a
		self.methodName = methodName
		self.params = params
		
	def _o_compileAsExpr(self):
		if len( self.params ) == 0:
			return '%s.%s()'  %  ( self.a.compileAsExpr( _methodCallPrecedence, _LEFT ), self.methodName )
		else:
			return '%s.%s( %s )'  %  ( self.a.compileAsExpr( _methodCallPrecedence, _LEFT ), self.methodName, ', '.join( [ self._p_compileParam( p )   for p in self.params ] ) )
	
	def _o_getPrecedence(self):
		return _methodCallPrecedence
	
	def _o_compareWith(self, x):
		return pyt_compare( self.a, x.a )  and  pyt_compare( self.params, x.params )  and  self.methodName == x.methodName

	def getChildren(self):
		return [ self.a ]  +  self.params

	def _p_compileParam(self, p):
		if isinstance( p, PyKWParam ):
			return '%s=%s'  %  ( p.name, p.value.compileAsExpr() )
		else:
			return p.compileAsExpr()

	
	
class PyIsInstance (PyExpression):
	def __init__(self, value, typ, dbgSrc=None):
		super( PyIsInstance, self ).__init__( dbgSrc )
		assert isinstance( value, PyExpression )
		assert isinstance( typ, PyExpression )
		self.value = value
		self.typ = typ
		
	def _o_compileAsExpr(self):
		return 'isinstance( %s, %s )'  %  ( self.value.compileAsExpr(), self.typ.compileAsExpr() )
		
	def _o_compareWith(self, x):
		return pyt_compare( self.value, x.value )  and  pyt_compare( self.typ, x.typ )

	def getChildren(self):
		return [ self.value, self.typ ]
	
	
	
class PyMultilineSrc (PyStatement):
	def __init__(self, srcText, dbgSrc=None):
		self.srcLines = srcText.split( '\n' )
		if len( srcText ) > 0:
			if srcText[-1] == '\n'  and  self.srcLines[-1] == '':
				del self.srcLines[-1]
		
	def compileAsStmt(self):
		return self.srcLines
	
	def _o_compareWith(self, x):
		return self.srcLines == x.srcLines
	
	
	
	

class PyReturn (PyStatement):
	def __init__(self, value, dbgSrc=None):
		super( PyReturn, self ).__init__( dbgSrc )
		assert isinstance( value, PyExpression )
		self.value =value
		
	def compileAsStmt(self):
		return [ 'return %s'  %  ( self.value.compileAsExpr(), ) ]
	
	def _o_compareWith(self, x):
		return pyt_compare( self.value, x.value )

	def getChildren(self):
		return [ self.value ]


class PyRaise (PyStatement):
	def __init__(self, exception, dbgSrc=None):
		super( PyRaise, self ).__init__( dbgSrc )
		assert isinstance( exception, PyExpression )
		self.exception = exception
		
	def compileAsStmt(self):
		return [ 'raise %s'  %  ( self.exception.compileAsExpr(), ) ]
	
	def _o_compareWith(self, x):
		return pyt_compare( self.exception, x.exception )

	def getChildren(self):
		return [ self.exception ]


class PyTry (PyStatement):
	def __init__(self, tryStatements, exceptSpecs=[], elseStatements=None, finallyStatements=None, dbgSrc=None):
		"""
		tryStatements is a list of statements
		exceptSpecsis a list of tuples
		each tuple is a (exception, statement-list) pair
		
		elseStatements is a list of statements for the else clause, or None if one is not desired
		finallyStatements is a list of statements for the finally clause, or None if one is not desired
		"""
		super( PyTry, self ).__init__( dbgSrc )
		
		assert isinstance( tryStatements, list ), 'PyTry: try statements must be a list'
		assert isinstance( exceptSpecs, list ), 'PyTry: except specs must be a list'
		if elseStatements is not None:
			assert isinstance( elseStatements, list ), 'PyTry: else statements must be a list'
		if finallyStatements is not None:
			assert isinstance( finallyStatements, list ), 'PyTry: finally statements must be a list'
		for i in exceptSpecs:
			assert isinstance( i, tuple ), 'PyTry: except-specification must be a tuple (exception, [statement*]); not a tuple'
			assert len( i ) == 2, 'PyTry: except-specification must be a tuple (exception, [statement*]); length != 2'
			assert isinstance( i[0], PyExpression ), 'PyTry: except-specification must be a tuple (exception, [statement*]); first element not an expression'
			assert isinstance( i[1], list ), 'PyTry: except-specification must be a tuple (exception, [statement*]); second element not a list'
			
		self.tryStatements = tryStatements
		self.exceptSpecs = exceptSpecs
		self.elseStatements = elseStatements
		self.finallyStatements = finallyStatements
		
		
	def compileAsStmt(self):
		assert len( self.exceptSpecs ) >= 1, 'PyTry: except specs must have at least 1 entry'
		
		trySrc = []
		
		trySrc.extend( self._p_compileKeywordBlock( 'try', self.tryStatements ) )
		
		for i in self.exceptSpecs:
			trySrc.extend( self._p_compileExceptBlock( i ) )
			
		if self.elseStatements is not None:
			trySrc.extend( self._p_compileKeywordBlock( 'else', self.elseStatements ) )
			
		if self.finallyStatements is not None:
			trySrc.extend( self._p_compileKeywordBlock( 'finally', self.finallyStatements ) )
			
		return trySrc
				

	def _o_compareWith(self, x):
		return False

	def getChildren(self):
		children = []
		children.extend( self.tryStatements )
		for exceptBlock in self.exceptSpecs:
			children.append( exceptBlock[0] )
			children.extend( exceptBlock[1] )
		if self.elseStatements is not None:
			children.extend( self.elseStatements )
		if self.finallyStatements is not None:
			children.extend( self.finallyStatements )
		return children
	
	
	
	def except_(self, exception, statements):
		return PyTry( self.tryStatements, self.exceptSpecs + [ ( exception, statements ) ], self.elseStatements, self.finallyStatements )

	def else_(self, statements):
		if self.elseStatements is None:
			return PyTry( self.tryStatements, self.exceptSpecs, statements, self.finallyStatements )
		else:
			return PyTry( self.tryStatements, self.exceptSpecs, self.elseStatements + statements, self.finallyStatements )

	def finally_(self, statements):
		if self.finallyStatements is None:
			return PyTry( self.tryStatements, self.exceptSpecs, self.elseStatements, statements )
		else:
			return PyTry( self.tryStatements, self.exceptSpecs, self.elseStatements, self.finallyStatements + statements )
	
		
	def _p_compileKeywordBlock(self, keyword, statements):
		stmtSrc = []
		for s in statements:
			stmtSrc.extend( s.compileAsStmt() )
		stmtSrc = _passBlock( stmtSrc )
		return [ keyword + ':' ]  +  _indent( stmtSrc )
	

	def _p_compileExceptBlock(self, t):
		exception, statements = t
		stmtSrc = []
		for s in statements:
			stmtSrc.extend( s.compileAsStmt() )
		stmtSrc = _passBlock( stmtSrc )
		
		return [ 'except %s:'  %  ( exception.compileAsExpr() ) ]  +  _indent( stmtSrc )
	

	

	

class PyIf (PyStatement):
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
			assert isinstance( i[0], PyExpression ), 'PyIf: if-specification must be a tuple (condition, [statement*]); first element not an expression'
			assert isinstance( i[1], list ), 'PyIf: if-specification must be a tuple (condition, [statement*]); second element not a list'
			
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
		return False

	def getChildren(self):
		children = []
		for ifBlock in self.ifElifSpecs:
			children.append( ifBlock[0] )
			children.extend( ifBlock[1] )
		if self.elseStatements is not None:
			children.extend( self.elseStatements )
		return children
	
	
	
	def elif_(self, condition, statements):
		return PyIf( self.ifElifSpecs + [ ( condition, statements ) ], self.elseStatements )

	def else_(self, statements):
		if self.elseStatements is None:
			return PyIf( self.ifElifSpecs, statements )
		else:
			return PyIf( self.ifElifSpecs, self.elseStatements + statements )

	
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

	
	
class PySimpleIf (PyIf):
	def __init__(self, condition, statements, dbgSrc=None):
		assert isinstance( condition, PyNode ), 'PySimpleIf condition must be a PyNode'
		assert isinstance( statements,list ), 'PySimpleIf statement list must be a list'
		super( PySimpleIf, self ).__init__( [ ( condition, statements ) ], None, dbgSrc=dbgSrc )



class PyDef (PyStatement):
	def __init__(self, name, argNames, statements, dbgSrc=None):
		super( PyDef, self ).__init__( dbgSrc )
		if not _isPyIdentifier( name ):
			self.error( PyInvalidFunctionNameError, name )
		bXArg = False
		bKWArg = False
		for argName in argNames:
			if argName.startswith( '**' ):
				n = argName[2:]
				bKWArg = True
			elif argName.startswith( '*' ):
				if bKWArg:
					raise PyKWArgMustBeLastError( argName )
				n = argName[1:]
				bXArg = True
			else:
				if bXArg:
					raise PyXArgMustFollowNormalArgsError( argName )
				if bKWArg:
					raise PyKWArgMustBeLastError( argName )
				n = argName
			if not _isPyIdentifier( n ):
				self.error( PyInvalidArgNameError, n )
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
		return False

	def getChildren(self):
		return self.statements

	
	
	
class PyAssign_SideEffects (PyStatement):
	def __init__(self, target, value, dbgSrc=None):
		super( PyAssign_SideEffects, self ).__init__( dbgSrc )
		assert isinstance( target, PyExpression )
		assert isinstance( value, PyExpression )
		self.target = target
		self.value =value
		
	def compileAsStmt(self):
		return [ '%s = %s'  %  ( self.target.compileAsExpr(), self.value.compileAsExpr() ) ]
	
	def _o_compareWith(self, x):
		return pyt_compare( self.target, x.target )  and  pyt_compare( self.value, x.value )

	def getChildren(self):
		return [ self.target, self.value ]

	
	
	
class PyDel_SideEffects (PyStatement):
	def __init__(self, target, dbgSrc=None):
		super( PyDel_SideEffects, self ).__init__( dbgSrc )
		assert isinstance( target, PyExpression )
		self.target = target
		
	def compileAsStmt(self):
		return [ 'del %s'  %  ( self.target.compileAsExpr(), ) ]
	
	def _o_compareWith(self, x):
		return pyt_compare( self.target, x.target )

	def getChildren(self):
		return [ self.target ]


	


#
#
#
# Unit tests
#
#
#


	
	
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
		self.assertRaises( PyInvalidUnaryOperatorError, lambda: PyUnOp( '$', PySrc( 'a' ) ) )
		PyUnOp( '-', PySrc( 'a' ) )
		PyUnOp( '~', PySrc( 'a' ) )
		PyUnOp( 'not', PySrc( 'a' ) )
		
	def test_PyBinOp(self):
		self.assertRaises( PyInvalidBinaryOperatorError, lambda: PyBinOp( PySrc( 'a' ), '$', PySrc( 'b' ) ) )
		for op in _binaryOperatorPrecedenceTable.keys():
			PyBinOp( PySrc( 'a' ), op, PySrc( 'b' ) )

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
		
	def test_PyKWParam(self):
		self.assertRaises( AssertionError, lambda: PyKWParam( None, PyVar( 'a' ) ) )
		self.assertRaises( AssertionError, lambda: PyKWParam( ':', PyVar( 'a' ) ) )
		
	def test_PyCall(self):
		self.assertRaises( AssertionError, lambda: PyCall( PyVar( 'a' ), [ None ] ) )
		self.assertRaises( AssertionError, lambda: PyCall( PyVar( 'a' ), [ PyKWParam( 'b', None ) ] ) )
		self.assertRaises( AssertionError, lambda: PyCall( PyVar( 'a' ), [ PyKWParam( 'b', PyLiteralValue( 1 ) ), PyLiteralValue( 2 ) ] ) )

	def test_PyMethodCall(self):
		self.assertRaises( PyInvalidMethodNameError, lambda: PyMethodCall( PySrc( 'a' ), '$', [ PySrc( 'b' ), PySrc( 'c' ) ] ) )
		self.assertRaises( AssertionError, lambda: PyMethodCall( PyVar( 'a' ), 'm', [ None ] ) )
		self.assertRaises( AssertionError, lambda: PyMethodCall( PyVar( 'a' ), 'm', [ PyKWParam( 'b', None ) ] ) )
		self.assertRaises( AssertionError, lambda: PyMethodCall( PyVar( 'a' ), 'm', [ PyKWParam( 'b', PyLiteralValue( 1 ) ), PyLiteralValue( 2 ) ] ) )


		
class TestCase_PyCodeGen_Node_cmp (unittest.TestCase):
	def test_PyKWParam(self):
		self.assert_( pyt_compare( PyKWParam( 'a', PyVar( 'x' ) ),  PyKWParam( 'a', PyVar( 'x' ) ) ) )
		self.assert_( not pyt_compare( PyKWParam( 'a', PyVar( 'x' ) ),  PyKWParam( 'b', PyVar( 'x' ) ) ) )
		self.assert_( not pyt_compare( PyKWParam( 'a', PyVar( 'x' ) ),  PyKWParam( 'a', PyVar( 'y' ) ) ) )
	
	def test_PySrc(self):
		self.assert_( pyt_compare( PySrc( 'a' ),  PySrc( 'a' ) ) )
		self.assert_( not pyt_compare( PySrc( 'a' ),  PySrc( 'b' ) ) )
	
	def test_PyVar(self):
		self.assert_( pyt_compare( PyVar( 'a' ),  PyVar( 'a' ) ) )
		self.assert_( not pyt_compare( PyVar( 'a' ),  PyVar( 'b' ) ) )

	def test_PyLiteral(self):
		self.assert_( pyt_compare( PyLiteral( '1' ),  PyLiteral( '1' ) ) )
		self.assert_( not pyt_compare( PyLiteral( '1' ),  PyLiteral( '2' ) ) )
		
	def test_PyLiteralValue(self):
		self.assert_( pyt_compare( PyLiteralValue( '1' ),  PyLiteralValue( '1' ) ) )
		self.assert_( not pyt_compare( PyLiteralValue( '1' ),  PyLiteralValue( '2' ) ) )
		
	def test_PyListLiteral(self):
		self.assert_( pyt_compare( PyListLiteral( [ PySrc( 'a' ), PySrc( 'b' ) ] ),  PyListLiteral( [ PySrc( 'a' ), PySrc( 'b' ) ] ) ) )
		self.assert_( not pyt_compare( PyListLiteral( [ PySrc( 'a' ), PySrc( 'b' ) ] ),  PyListLiteral( [ PySrc( 'a' ), PySrc( 'c' ) ] ) ) )

	def test_PyDictLiteral(self):
		self.assert_( pyt_compare( PyDictLiteral( [ ( PyLiteralValue( 'a' ), PyLiteralValue( 1 ) ),  ( PyLiteralValue( 'b' ), PyLiteralValue( 2 ) ) ] ),
					   PyDictLiteral( [ ( PyLiteralValue( 'a' ), PyLiteralValue( 1 ) ),  ( PyLiteralValue( 'b' ), PyLiteralValue( 2 ) ) ] ) ) )
		self.assert_( pyt_compare( PyDictLiteral( [ ( PyLiteralValue( 'a' ), PyLiteralValue( 1 ) ),  ( PyLiteralValue( 'b' ), PyLiteralValue( 2 ) ) ] ),
					   PyDictLiteral( [ ( PyLiteralValue( 'a' ), PyLiteralValue( 1 ) ),  ( PyLiteralValue( 'b' ), PyLiteralValue( 3 ) ) ] ) ) )

	def test_PyListComprehension(self):
		self.assert_( pyt_compare( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), None ),  PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), None ) ) )
		self.assert_( not pyt_compare( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), None ),  PyListComprehension( PySrc( 'b' ), 'a', PySrc( 'x' ), None ) ) )
		self.assert_( not pyt_compare( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), None ),  PyListComprehension( PySrc( 'a' ), 'b', PySrc( 'x' ), None ) ) )
		self.assert_( not pyt_compare( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), None ),  PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'y' ), None ) ) )
		self.assert_( pyt_compare( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'True' ) ),  PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ),PySrc( 'True' ) ) ) )
		self.assert_( not pyt_compare( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'True' )),  PyListComprehension( PySrc( 'b' ), 'a', PySrc( 'x' ), PySrc( 'True' ) ) ) )
		self.assert_( not pyt_compare( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'True' ) ),  PyListComprehension( PySrc( 'a' ), 'b', PySrc( 'x' ), PySrc( 'True' ) ) ) )
		self.assert_( not pyt_compare( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'True' ) ),  PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'y' ), PySrc( 'True' ) ) ) )
		self.assert_( not pyt_compare( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'True' ) ),  PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'False' ) ) ) )
		self.assert_( not pyt_compare( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'True' ) ),  PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), None ) ) )
		
	def test_PyGetAttr(self):
		self.assert_( pyt_compare( PyGetAttr( PySrc( 'a' ), 'test' ),   PyGetAttr( PySrc( 'a' ), 'test' ) ) )
		self.assert_( not pyt_compare( PyGetAttr( PySrc( 'a' ), 'test' ),   PyGetAttr( PySrc( 'b' ), 'test' ) ) )
		self.assert_( not pyt_compare( PyGetAttr( PySrc( 'a' ), 'test' ),   PyGetAttr( PySrc( 'a' ), 'foo' ) ) )

	def test_PyGetItem(self):
		self.assert_( pyt_compare( PyGetItem( PySrc( 'a' ), PySrc( '1' ) ),   PyGetItem( PySrc( 'a' ), PySrc( '1' ) ) ) )
		self.assert_( not pyt_compare( PyGetItem( PySrc( 'a' ), PySrc( '1' ) ),   PyGetItem( PySrc( 'b' ), PySrc( '1' ) ) ) )
		self.assert_( not pyt_compare( PyGetItem( PySrc( 'a' ), PySrc( '1' ) ),   PyGetItem( PySrc( 'a' ), PySrc( '2' ) ) ) )
		
	def test_PyGetSlice(self):
		self.assert_( pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ) ),   PyGetSlice( PySrc( 'a' ), PySrc( '1' ) ) ) )
		self.assert_( not pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ) ),   PyGetSlice( PySrc( 'b' ), PySrc( '1' ) ) ) )
		self.assert_( not pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ) ),   PyGetSlice( PySrc( 'a' ), PySrc( '2' ) ) ) )

		self.assert_( pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ),   PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ) ) )
		self.assert_( not pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ),   PyGetSlice( PySrc( 'a' ), PySrc( '1' ) ) ) )
		self.assert_( not pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ),   PyGetSlice( PySrc( 'b' ), PySrc( '1' ), PySrc( '2' ) ) ) )
		self.assert_( not pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ),   PyGetSlice( PySrc( 'a' ), PySrc( '2' ), PySrc( '2' ) ) ) )
		self.assert_( not pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ),   PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '3' ) ) ) )

		self.assert_( pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ), PySrc( '3' ) ),   PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ), PySrc( '3' ) ) ) )
		self.assert_( not pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ), PySrc( '3' ) ),   PyGetSlice( PySrc( 'b' ), PySrc( '1' ), PySrc( '2' ), PySrc( '3' ) ) ) )
		self.assert_( not pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ), PySrc( '3' ) ),   PyGetSlice( PySrc( 'a' ), PySrc( '2' ), PySrc( '2' ), PySrc( '3' ) ) ) )
		self.assert_( not pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ), PySrc( '3' ) ),   PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '3' ), PySrc( '3' ) ) ) )
		self.assert_( not pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ), PySrc( '3' ) ),   PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ), PySrc( '4' ) ) ) )
		self.assert_( not pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ), PySrc( '3' ) ),   PyGetSlice( PySrc( 'a' ), PySrc( '1' ) ) ) )
		self.assert_( not pyt_compare( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ), PySrc( '3' ) ),   PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ) ) )
		
	def test_PyUnOp(self):
		self.assert_( pyt_compare( PyUnOp( '-', PySrc( 'a' ) ),   PyUnOp( '-', PySrc( 'a' ) ) ) )
		self.assert_( not pyt_compare( PyUnOp( '-', PySrc( 'a' ) ),   PyUnOp( '-', PySrc( 'b' ) ) ) )
		self.assert_( not pyt_compare( PyUnOp( '-', PySrc( 'a' ) ),   PyUnOp( '~', PySrc( 'a' ) ) ) )

	def test_PyBinOp(self):
		for op in _binaryOperatorPrecedenceTable.keys():
			self.assert_( pyt_compare( PyBinOp( PySrc( 'a' ), op, PySrc( 'b' ) ),  PyBinOp( PySrc( 'a' ), op, PySrc( 'b' ) ) ) )
		self.assert_( pyt_compare( PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) ),  PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) ) ) )
		self.assert_( not pyt_compare( PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) ),  PyBinOp( PySrc( 'x' ), '+', PySrc( 'b' ) ) ) )
		self.assert_( not pyt_compare( PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) ),  PyBinOp( PySrc( 'a' ), '+', PySrc( 'y' ) ) ) )
		self.assert_( not pyt_compare( PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) ),  PyBinOp( PySrc( 'a' ), '-', PySrc( 'b' ) ) ) )

	def test_PyCall(self):
		self.assert_( pyt_compare( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ),   PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) ) )
		self.assert_( not pyt_compare( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ),   PyCall( PySrc( 'x' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ) ) )
		self.assert_( not pyt_compare( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ),   PyCall( PySrc( 'a' ), [ PySrc( 'y' ), PySrc( 'c' ) ] ) ) )
		self.assert_( not pyt_compare( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ),   PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'z' ) ] ) ) )
		self.assert_( not pyt_compare( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ),   PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ), PySrc( 'w' ) ] ) ) )

	def test_PyMethodCall(self):
		self.assert_( pyt_compare( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ),   PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) ) )
		self.assert_( not pyt_compare( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ),   PyMethodCall( PySrc( 'x' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ) ) )
		self.assert_( not pyt_compare( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ),   PyMethodCall( PySrc( 'a' ), 'x', [ PySrc( 'c' ), PySrc( 'd' ) ] ) ) )
		self.assert_( not pyt_compare( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ),   PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'x' ), PySrc( 'd' ) ] ) ) )
		self.assert_( not pyt_compare( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ),   PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'x' ) ] ) ) )
		self.assert_( not pyt_compare( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ),   PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ), PySrc( 'x' ) ] ) ) )
		
	def test_PyIsInstance(self):
		self.assert_( pyt_compare( PyIsInstance( PySrc( 'a' ), PySrc( 'b' ) ),   PyIsInstance( PySrc( 'a' ), PySrc( 'b' ) ) ) )
		self.assert_( not pyt_compare( PyIsInstance( PySrc( 'a' ), PySrc( 'b' ) ),   PyIsInstance( PySrc( 'c' ), PySrc( 'b' ) ) ) )
		self.assert_( not pyt_compare( PyIsInstance( PySrc( 'a' ), PySrc( 'b' ) ),   PyIsInstance( PySrc( 'a' ), PySrc( 'c' ) ) ) )

	def test_PyMultilineSrc(self):
		src1 = """
		a
		b
		c
		"""
		src2 = """
		d
		e
		f
		"""
		self.assert_( pyt_compare( PyMultilineSrc( src1 ),  PyMultilineSrc( src1 ) ) )
		self.assert_( not pyt_compare( PyMultilineSrc( src1 ),  PyMultilineSrc( src2 ) ) )
		
	def test_PyReturn(self):
		self.assert_( pyt_compare( PyReturn( PySrc( '1' ) ),  PyReturn( PySrc( '1' ) ) ) )
		self.assert_( not pyt_compare( PyReturn( PySrc( '1' ) ),  PyReturn( PySrc( '2' ) ) ) )

	def test_PyRaise(self):
		self.assert_( pyt_compare( PyRaise( PySrc( 'ValueError' ) ),  PyRaise( PySrc( 'ValueError' ) ) ) )
		self.assert_( not pyt_compare( PyRaise( PySrc( 'ValueError' ) ),  PyRaise( PySrc( 'TypeError' ) ) ) )

	def test_PyTry(self):
		self.assert_( not pyt_compare( PyTry( [ PySrc( 'x' ) ] ),  PyTry( [ PySrc( 'x' ) ] ) ) )
		
	def test_PyIf(self):
		self.assert_( not pyt_compare( PyIf( [ ( PySrc( 'True' ), [ PySrc( '1' ) ] ) ] ),  PyIf( [ ( PySrc( 'True' ), [ PySrc( 'pass' ) ] ) ] ) ) )
		
	def test_PyDef(self):
		self.assert_( not pyt_compare( PyDef( 'foo', [ 'a', 'b' ], [] ),  PyDef( 'foo', [ 'a', 'b' ], [] ) ) )

	def test_PyAssign_SideEffects(self):
		self.assert_( pyt_compare( PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ),   PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ) ) )
		self.assert_( not pyt_compare( PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ),   PyAssign_SideEffects( PySrc( 'b' ), PySrc( '1' ) ) ) )
		self.assert_( not pyt_compare( PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ),   PyAssign_SideEffects( PySrc( 'a' ), PySrc( '2' ) ) ) )

	def test_PyDel_SideEffects(self):
		self.assert_( pyt_compare( PyDel_SideEffects( PySrc( 'a' ) ),   PyDel_SideEffects( PySrc( 'a' ) ) ) )
		self.assert_( not pyt_compare( PyDel_SideEffects( PySrc( 'a' ) ),   PyDel_SideEffects( PySrc( 'b' ) ) ) )

	

		
class TestCase_PyCodeGen_Node_compile (unittest.TestCase):
	def test_PySrc(self):
		self.assert_( PySrc( 'a' ).compileAsExpr()  ==  'a' )
	
	def test_PyVar(self):
		self.assert_( PyVar( 'a' ).compileAsExpr()  ==  'a' )

	def test_PyLiteral(self):
		self.assert_( PyLiteral( '1' ).compileAsExpr()  ==  '1' )

	def test_PyLiteralValue(self):
		self.assert_( PyLiteralValue( 1 ).compileAsExpr()  ==  '1' )

	def test_PyListLiteral(self):
		self.assert_( PyListLiteral( [] ).compileAsExpr()  ==  '[]' )
		self.assert_( PyListLiteral( [ PySrc( 'a' ), PySrc( 'b' ) ] ).compileAsExpr()  ==  '[ a, b ]' )
		
	def test_PyDictLiteral(self):
		self.assert_( PyDictLiteral( [ ( PyLiteralValue( 'a' ), PyLiteralValue( 1 ) ),  ( PyLiteralValue( 'b' ), PyLiteralValue( 2 ) ) ] ).compileAsExpr()  ==  "{ 'a' : 1, 'b' : 2 }" )

	def test_PyListComprehension(self):
		self.assert_( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), None ).compileAsExpr()  ==  '[ a   for a in x ]' )
		self.assert_( PyListComprehension( PySrc( 'a' ), 'a', PySrc( 'x' ), PySrc( 'True' ) ).compileAsExpr()  ==  '[ a   for a in x   if True ]' )
		
	def test_PyGetAttr(self):
		self.assert_( PyGetAttr( PySrc( 'a' ), 'test' ).compileAsExpr()  ==  'a.test' )
		
	def test_PyGetItem(self):
		self.assert_( PyGetItem( PySrc( 'a' ), PySrc( '1' ) ).compileAsExpr()  ==  'a[1]' )
		
	def test_PyGetSlice(self):
		self.assert_( PyGetSlice( PySrc( 'a' ), PySrc( '1' ) ).compileAsExpr()  ==  'a[1:]' )
		self.assert_( PyGetSlice( PySrc( 'a' ), None, PySrc( '1' ) ).compileAsExpr()  ==  'a[:1]' )
		self.assert_( PyGetSlice( PySrc( 'a' ), None, None, PySrc( '1' ) ).compileAsExpr()  ==  'a[::1]' )
		self.assert_( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ) ).compileAsExpr()  ==  'a[1:2]' )
		self.assert_( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), None, PySrc( '2' ) ).compileAsExpr()  ==  'a[1::2]' )
		self.assert_( PyGetSlice( PySrc( 'a' ), None, PySrc( '1' ), PySrc( '2' ) ).compileAsExpr()  ==  'a[:1:2]' )
		self.assert_( PyGetSlice( PySrc( 'a' ), PySrc( '1' ), PySrc( '2' ), PySrc( '3' ) ).compileAsExpr()  ==  'a[1:2:3]' )
		
	def test_PyUnOp(self):
		self.assert_( PyUnOp( '-', PySrc( 'a' ) ).compileAsExpr()  ==  '-a' )
		self.assert_( PyUnOp( '~', PySrc( 'a' ) ).compileAsExpr()  ==  '~a' )
		self.assert_( PyUnOp( 'not', PySrc( 'a' ) ).compileAsExpr()  ==  'not a' )

	def test_PyBinOp(self):
		for op in _binaryOperatorPrecedenceTable.keys():
			self.assert_( PyBinOp( PySrc( 'a' ), op, PySrc( 'b' ) ).compileAsExpr()  ==  'a %s b'  %  ( op, ) )
		
	def test_PyCall(self):
		self.assert_( PyCall( PySrc( 'a' ), [] ).compileAsExpr()  ==  'a()' )
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ) ] ).compileAsExpr()  ==  'a( b, c )' )
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PySrc( 'c' ), PySrc( 'd' ) ] ).compileAsExpr()  ==  'a( b, c, d )' )
		self.assert_( PyCall( PySrc( 'a' ), [ PySrc( 'b' ), PyKWParam( 'x', PySrc( 'c' ) ) ] ).compileAsExpr()  ==  'a( b, x=c )' )

	def test_PyMethodCall(self):
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [] ).compileAsExpr()  ==  'a.b()' )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ) ] ).compileAsExpr()  ==  'a.b( c, d )' )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'c' ), PySrc( 'd' ), PySrc( 'e' ) ] ).compileAsExpr()  ==  'a.b( c, d, e )' )
		self.assert_( PyMethodCall( PySrc( 'a' ), 'b', [ PySrc( 'b' ), PyKWParam( 'x', PySrc( 'c' ) ) ] ).compileAsExpr()  ==  'a.b( b, x=c )' )
		
	def test_PyIsInstance(self):
		self.assert_( PyIsInstance( PySrc( 'a' ), PySrc( 'b' ) ).compileAsExpr()  ==  'isinstance( a, b )' )
		
	def test_PyMultilineSrc(self):
		src1 = "a()\nb()\nreturn 1\n"
		self.assert_( PyMultilineSrc( src1 ).compileAsStmt()  ==  [ 'a()', 'b()', 'return 1' ] )
		
	def test_PyReturn(self):
		self.assert_( PyReturn( PySrc( '1' ) ).compileAsStmt()  ==  [ 'return 1' ] )

	def test_PyRaise(self):
		self.assert_( PyRaise( PySrc( 'ValueError' ) ).compileAsStmt()  ==  [ 'raise ValueError' ] )

	def test_PyTry(self):
		pysrc2 = [
			"try:",
			"  a",
			"except ValueError:",
			"  b",
		]

		pysrc3 = [
			"try:",
			"  a",
			"except ValueError:",
			"  b",
			"except TypeError:",
			"  c",
		]

		pysrc4 = [
			"try:",
			"  a",
			"except ValueError:",
			"  b",
			"except TypeError:",
			"  c",
			"else:",
			"  d",
		]

		pysrc5 = [
			"try:",
			"  a",
			"except ValueError:",
			"  b",
			"except TypeError:",
			"  c",
			"else:",
			"  d",
			"finally:",
			"  e",
		]

		pysrc6 = [
			"try:",
			"  a",
			"except ValueError:",
			"  b",
			"except TypeError:",
			"  c",
			"finally:",
			"  e",
		]

		
		self.assert_( PyTry( [ PyVar( 'a' ) ],  [ ( PyVar( 'ValueError' ), [ PyVar( 'b' ) ] ) ] ).compileAsStmt()  ==  pysrc2 )
		self.assert_( PyTry( [ PyVar( 'a' ) ],  [ ( PyVar( 'ValueError' ), [ PyVar( 'b' ) ] ), ( PyVar( 'TypeError' ), [ PyVar( 'c' ) ] ) ] ).compileAsStmt()  ==  pysrc3 )
		self.assert_( PyTry( [ PyVar( 'a' ) ],  [ ( PyVar( 'ValueError' ), [ PyVar( 'b' ) ] ), ( PyVar( 'TypeError' ), [ PyVar( 'c' ) ] ) ], [ PyVar( 'd' ) ] ).compileAsStmt()  ==  pysrc4 )
		self.assert_( PyTry( [ PyVar( 'a' ) ],  [ ( PyVar( 'ValueError' ), [ PyVar( 'b' ) ] ), ( PyVar( 'TypeError' ), [ PyVar( 'c' ) ] ) ], [ PyVar( 'd' ) ], [ PyVar( 'e' ) ] ).compileAsStmt()  ==  pysrc5 )
		self.assert_( PyTry( [ PyVar( 'a' ) ],  [ ( PyVar( 'ValueError' ), [ PyVar( 'b' ) ] ), ( PyVar( 'TypeError' ), [ PyVar( 'c' ) ] ) ], finallyStatements=[ PyVar( 'e' ) ] ).compileAsStmt()  ==  pysrc6 )


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

	def test_PySimpleIf(self):
		pysrc1 = [
			'if True:',
			'  pass'
		]

		self.assert_( PySimpleIf( PySrc( 'True' ),  [ PySrc( 'pass' ) ] ).compileAsStmt()  ==  pysrc1 )
		self.assert_( PySimpleIf( PySrc( 'True' ),  [] ).compileAsStmt()  ==  pysrc1 )

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

	def test_PyAssign_SideEffects(self):
		self.assert_( PyAssign_SideEffects( PySrc( 'a' ), PySrc( '1' ) ).compileAsStmt()  ==  [ 'a = 1' ] )

	def test_PyDel_SideEffects(self):
		self.assert_( PyDel_SideEffects( PySrc( 'a' ) ).compileAsStmt()  ==  [ 'del a' ] )
		
		
	def test_precedence(self):
		self.assert_( PyBinOp( PySrc( 'a' ), '+', PyBinOp( PySrc( 'b' ), '+', PySrc( 'c' ) ) ).compileAsExpr()  ==  'a + (b + c)' )
		self.assert_( PyBinOp( PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) ), '+', PySrc( 'c' ) ).compileAsExpr()  ==  'a + b + c' )
		self.assert_( PyBinOp( PySrc( 'a' ), '*', PyBinOp( PySrc( 'b' ), '+', PySrc( 'c' ) ) ).compileAsExpr()  ==  'a * (b + c)' )
		self.assert_( PyBinOp( PyBinOp( PySrc( 'a' ), '*', PySrc( 'b' ) ), '+', PySrc( 'c' ) ).compileAsExpr()  ==  'a * b + c' )
		self.assert_( PyMethodCall( PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) ), 'upper', [] ).compileAsExpr()  ==  '(a + b).upper()' )
		self.assert_( PyCall( PyBinOp( PySrc( 'a' ), '+', PySrc( 'b' ) ), [] ).compileAsExpr()  ==  '(a + b)()' )
		self.assert_( PyCall( PyCall( PySrc( 'a' ), [] ), [] ).compileAsExpr()  ==  'a()()' )
		self.assert_( PyVar( 'a' ).attr( 'b' )[0].compileAsExpr()  ==  'a.b[0]' )
		self.assert_( PyVar( 'a' ).attr( 'b' )[0].attr( 'c' ).compileAsExpr()  ==  'a.b[0].c' )
		self.assert_( PyVar( 'a' )().attr( 'b' )[0].attr( 'c' ).compileAsExpr()  ==  'a().b[0].c' )
		self.assert_( PyVar( 'a' ).attr( 'b' )()[0].attr( 'c' ).compileAsExpr()  ==  'a.b()[0].c' )
		self.assert_( PyVar( 'a' ).attr( 'b' )[0]().attr( 'c' ).compileAsExpr()  ==  'a.b[0]().c' )
		self.assert_( PyVar( 'a' ).attr( 'b' )[0].attr( 'c' )().compileAsExpr()  ==  'a.b[0].c()' )
		self.assert_( PyVar( 'a' ).methodCall( 'm' ).attr( 'b' )[0].attr( 'c' ).compileAsExpr()  ==  'a.m().b[0].c' )
		self.assert_( PyVar( 'a' ).attr( 'b' ).methodCall( 'm' )[0].attr( 'c' ).compileAsExpr()  ==  'a.b.m()[0].c' )
		self.assert_( PyVar( 'a' ).attr( 'b' )[0].methodCall( 'm' ).attr( 'c' ).compileAsExpr()  ==  'a.b[0].m().c' )
		self.assert_( PyVar( 'a' ).attr( 'b' )[0].attr( 'c' ).methodCall( 'm' ).compileAsExpr()  ==  'a.b[0].c.m()' )
	
		


class TestCase_PyCodeGen_build (unittest.TestCase):
	def _compileExprTest(self, tree, expectedResult):
		self.assert_( tree.compileAsExpr() == expectedResult )
	
	def _compileStmtTest(self, tree, expectedResult):
		self.assert_( tree.compileAsStmt() == expectedResult )

		

	def test_coerce(self):
		self.assert_( pyt_compare( pyt_coerce( None ), PyLiteralValue( None ) ) )
		self.assert_( pyt_compare( pyt_coerce( False ), PyLiteralValue( False ) ) )
		self.assert_( pyt_compare( pyt_coerce( True ), PyLiteralValue( True ) ) )
		self.assert_( pyt_compare( pyt_coerce( 1 ), PyLiteralValue( 1 ) ) )
		self.assert_( pyt_compare( pyt_coerce( 2L ), PyLiteralValue( 2L ) ) )
		self.assert_( pyt_compare( pyt_coerce( 3.141 ), PyLiteralValue( 3.141 ) ) )
		self.assert_( pyt_compare( pyt_coerce( 'hi' ), PyLiteralValue( 'hi' ) ) )
		self.assert_( pyt_compare( pyt_coerce( u'hi' ), PyLiteralValue( u'hi' ) ) )
		
	def test_getattr(self):
		self._compileExprTest( PyVar( 'abc' ).attr( 'x' ),  'abc.x' )
		
	def test_identity(self):
		self._compileExprTest( PyVar( 'abc' ).is_( PyVar( 'xyz' ) ),  'abc is xyz' )
		
	def test_boolean(self):
		self._compileExprTest( PyVar( 'abc' ).and_( PyVar( 'xyz' ) ),  'abc and xyz' )
		self._compileExprTest( PyVar( 'abc' ).or_( PyVar( 'xyz' ) ),  'abc or xyz' )
		self._compileExprTest( PyVar( 'abc' ).not_(),  'not abc' )
		
	def test_unary(self):
		self._compileExprTest( -PyVar( 'abc' ),  '-abc' )
		self._compileExprTest( ~PyVar( 'abc' ),  '~abc' )
		
	def test_binary(self):
		self._compileExprTest( PyVar( 'x' )  +  PyVar( 'y' ),   'x + y' )
		self._compileExprTest( PyVar( 'x' )  -  PyVar( 'y' ),   'x - y' )
		self._compileExprTest( PyVar( 'x' )  *  PyVar( 'y' ),   'x * y' )
		self._compileExprTest( PyVar( 'x' )  /  PyVar( 'y' ),   'x / y' )
		self._compileExprTest( PyVar( 'x' )  %  PyVar( 'y' ),   'x % y' )
		self._compileExprTest( PyVar( 'x' )  **  PyVar( 'y' ),   'x ** y' )
		self._compileExprTest( PyVar( 'x' )  <<  PyVar( 'y' ),   'x << y' )
		self._compileExprTest( PyVar( 'x' )  >>  PyVar( 'y' ),   'x >> y' )
		self._compileExprTest( PyVar( 'x' )  &  PyVar( 'y' ),   'x & y' )
		self._compileExprTest( PyVar( 'x' )  |  PyVar( 'y' ),   'x | y' )
		self._compileExprTest( PyVar( 'x' )  ^  PyVar( 'y' ),   'x ^ y' )
		
	def test_comparison(self):
		self._compileExprTest( PyVar( 'x' )  <  PyVar( 'y' ),   'x < y' )
		self._compileExprTest( PyVar( 'x' )  <=  PyVar( 'y' ),   'x <= y' )
		self._compileExprTest( PyVar( 'x' )  ==  PyVar( 'y' ),   'x == y' )
		self._compileExprTest( PyVar( 'x' )  !=  PyVar( 'y' ),   'x != y' )
		self._compileExprTest( PyVar( 'x' )  >  PyVar( 'y' ),   'x > y' )
		self._compileExprTest( PyVar( 'x' )  >=  PyVar( 'y' ),   'x >= y' )
		

	def test_container(self):
		self._compileExprTest( PyVar( 'x' ).len_(),   'len( x )' )
		self._compileExprTest( PyVar( 'x' ).in_( PyVar( 'y' ) ),   'x in y' )
		self._compileExprTest( PyVar( 'x' )[0],   'x[0]' )
		self._compileExprTest( PyVar( 'x' )[0:],   'x[0:]' )
		self._compileExprTest( PyVar( 'x' )[:0],   'x[:0]' )
		self._compileExprTest( PyVar( 'x' )[::0],   'x[::0]' )
		self._compileExprTest( PyVar( 'x' )[0:2],   'x[0:2]' )
		self._compileExprTest( PyVar( 'x' )[0::2],   'x[0::2]' )
		self._compileExprTest( PyVar( 'x' )[:0:2],   'x[:0:2]' )
		self._compileExprTest( PyVar( 'x' )[0:2:1],   'x[0:2:1]' )

	
	def test_call(self):
		self._compileExprTest( PyVar( 'x' )(1,2,3),   'x( 1, 2, 3 )' )
		self._compileExprTest( PyVar( 'x' )(1,2,3,PyKWParam('a',12)),   'x( 1, 2, 3, a=12 )' )
		
		
	def test_methodCall(self):
		self._compileExprTest( PyVar( 'x' ).methodCall( 'x', 1,2,3),   'x.x( 1, 2, 3 )' )
		self._compileExprTest( PyVar( 'x' ).methodCall( 'x', 1,2,3,PyKWParam('a',12)),   'x.x( 1, 2, 3, a=12 )' )
		
		
	def test_isinstance(self):
		self._compileExprTest( PyVar( 'x' ).isinstance_( PyVar( 'y' ) ),   'isinstance( x, y )' )
		
		
	def test_return(self):
		self._compileStmtTest( PyVar( 'x' ).return_(),   [ 'return x' ] )

	def test_raise(self):
		self._compileStmtTest( PyVar( 'ValueError' ).raise_(),   [ 'raise ValueError' ] )

	def test_try(self):
		pysrc2 = [
			"try:",
			"  a",
			"except ValueError:",
			"  b",
		]

		pysrc3 = [
			"try:",
			"  a",
			"except ValueError:",
			"  b",
			"except TypeError:",
			"  c",
		]

		pysrc4 = [
			"try:",
			"  a",
			"except ValueError:",
			"  b",
			"except TypeError:",
			"  c",
			"else:",
			"  d",
		]

		pysrc5 = [
			"try:",
			"  a",
			"except ValueError:",
			"  b",
			"except TypeError:",
			"  c",
			"else:",
			"  d",
			"finally:",
			"  e",
		]

		pysrc6 = [
			"try:",
			"  a",
			"except ValueError:",
			"  b",
			"except TypeError:",
			"  c",
			"finally:",
			"  e",
		]

		
		self.assert_( PyTry( [ PyVar( 'a' ) ] ).except_( PyVar( 'ValueError' ), [ PyVar( 'b' ) ] ).compileAsStmt()  ==  pysrc2 )
		self.assert_( PyTry( [ PyVar( 'a' ) ] ).except_( PyVar( 'ValueError' ), [ PyVar( 'b' ) ] ).except_( PyVar( 'TypeError' ), [ PyVar( 'c' ) ] ).compileAsStmt()  ==  pysrc3 )
		self.assert_( PyTry( [ PyVar( 'a' ) ] ).except_( PyVar( 'ValueError' ), [ PyVar( 'b' ) ] ).except_( PyVar( 'TypeError' ), [ PyVar( 'c' ) ] ).else_( [ PyVar( 'd' ) ] ).compileAsStmt()  ==  pysrc4 )
		self.assert_( PyTry( [ PyVar( 'a' ) ] ).except_( PyVar( 'ValueError' ), [ PyVar( 'b' ) ] ).except_( PyVar( 'TypeError' ), [ PyVar( 'c' ) ] ).else_( [ PyVar( 'd' ) ] ).finally_( [ PyVar( 'e' ) ] ).compileAsStmt()  ==  pysrc5 )
		self.assert_( PyTry( [ PyVar( 'a' ) ] ).except_( PyVar( 'ValueError' ), [ PyVar( 'b' ) ] ).except_( PyVar( 'TypeError' ), [ PyVar( 'c' ) ] ).finally_( [ PyVar( 'e' ) ] ).compileAsStmt()  ==  pysrc6 )

	def test_assign(self):
		self._compileStmtTest( PyVar( 'x' ).assign_sideEffects( pyt_coerce( 1 ) ),   [ 'x = 1' ] )
		self._compileStmtTest( pyt_coerce( 1 ).assignTo_sideEffects( PyVar( 'x' ) ),   [ 'x = 1' ] )
		self._compileStmtTest( pyt_coerce( 1 ).assignToVar_sideEffects( 'x' ),   [ 'x = 1' ] )

	def test_del(self):
		self._compileStmtTest( PyVar( 'x' ).del_sideEffects(),   [ 'del x' ] )
		
	def test_ifTrue(self):
		self._compileStmtTest( PyVar( 'a' ).ifTrue( [ PyVar( 'x' )() ] ),   [ 'if a:', '  x()' ] )
		
	def test_elif(self):
		self._compileStmtTest( PyVar( 'a' ).ifTrue( [ PyVar( 'x' )() ] ).elif_( PyVar( 'b' ), [ PyVar( 'y' )() ] ),   [ 'if a:', '  x()', 'elif b:', '  y()' ] )

	def test_else(self):
		self._compileStmtTest( PyVar( 'a' ).ifTrue( [ PyVar( 'x' )() ] ).elif_( PyVar( 'b' ), [ PyVar( 'y' )() ] ).else_( [ PyVar( 'z' )() ] ),   [ 'if a:', '  x()', 'elif b:', '  y()', 'else:', '  z()' ] )

		
		
		
class TestCase_PyCodeGen_Node_children (unittest.TestCase):
	def _childrenTest(self, tree, expectedResult):
		children = tree.getChildren()
		self.assert_( len( children )  ==  len( expectedResult ) )
		for a, b in zip( children, expectedResult ):
			if a is not b:
				self.fail()
		
		
	
	def test_PySrc(self):
		self._childrenTest( PySrc( 'a' ), [] )
	
	def test_PyVar(self):
		self._childrenTest( PyVar( 'a' ), [] )

	def test_PyLiteral(self):
		self._childrenTest( PyLiteral( '1' ), [] )

	def test_PyLiteralValue(self):
		self._childrenTest( PyLiteralValue( 1 ), [] )

	def test_PyListLiteral(self):
		subs = [ pyt_coerce( x )   for x in xrange( 0, 5 ) ]
		self._childrenTest( PyListLiteral( subs ), subs  )
		
	def test_PyListComprehension(self):
		itemExpr = PyVar( 'a' ).attr( 'x' )
		itemName = 'a'
		srcIterableExpr = PyVar( 'x' ).attr( 'items' )()
		filterExpr = pyt_coerce( True )
		self._childrenTest( PyListComprehension( itemExpr, itemName, srcIterableExpr, None ), [ itemExpr, srcIterableExpr ] )
		self._childrenTest( PyListComprehension( itemExpr, itemName, srcIterableExpr, filterExpr ), [ itemExpr, srcIterableExpr, filterExpr ] )
		
	def test_PyGetAttr(self):
		x = PyVar( 'x' )
		self._childrenTest( x.attr( 'abc' ), [ x ] )
		
	def test_PyGetItem(self):
		x = PyVar( 'x' )
		a, b, c = [ pyt_coerce( i )   for i in xrange( 0, 3 ) ]
		self._childrenTest( x[a], [ x, a ] )
		self._childrenTest( x[a:b], [ x, a, b ] )
		self._childrenTest( x[a:b:c], [ x, a, b, c ] )
		
	def test_PyUnOp(self):
		x = PyVar( 'x' )
		self._childrenTest( -x, [x] )
		self._childrenTest( ~x, [x] )
		self._childrenTest( x.not_(), [x] )

	def test_PyBinOp(self):
		x = PyVar( 'x' )
		y = PyVar( 'y' )
		self._childrenTest( x + y, [ x, y ] )
		
	def test_PyCall(self):
		x = PyVar( 'x' )
		y = PyVar( 'y' )
		self._childrenTest( x( y ), [ x, y ] )

	def test_PyMethodCall(self):
		x = PyVar( 'x' )
		y = PyVar( 'y' )
		self._childrenTest( x.methodCall( 'test', y ), [ x, y ] )
		
	def test_PyIsInstance(self):
		x = PyVar( 'x' )
		y = PyVar( 'y' )
		self._childrenTest( x.isinstance_( y ), [ x, y ] )

	def test_PyReturn(self):
		x = PyVar( 'x' )
		self._childrenTest( PyReturn( x ), [x] )

	def test_PyRaise(self):
		x = PyVar( 'x' )
		self._childrenTest( PyRaise( x ), [x] )

	def test_PyTry(self):
		a = PyVar( 'a' )
		b = PyVar( 'b' )
		c = PyVar( 'c' )
		d = PyVar( 'd' )
		e = PyVar( 'e' )
		f = PyVar( 'f' )
		g = PyVar( 'g' )
		
		self._childrenTest( PyTry( [ a ] ).except_( b, [c] ),    [ a, b, c ] )
		self._childrenTest( PyTry( [ a ] ).except_( b, [c] ).except_( d, [e] ),    [ a, b, c, d, e ] )
		self._childrenTest( PyTry( [ a ] ).except_( b, [c] ).except_( d, [e] ).else_( [f] ),    [ a, b, c, d, e, f ] )
		self._childrenTest( PyTry( [ a ] ).except_( b, [c] ).except_( d, [e] ).else_( [f] ).finally_( [g] ),    [ a, b, c, d, e, f, g ] )

	def test_PyIf(self):
		a = PyVar( 'a' )
		b = PyVar( 'b' )
		x = PyVar( 'x' )()
		y = PyVar( 'y' )()
		z = PyVar( 'z' )()
		
		self._childrenTest( a.ifTrue( [ x ] ), [ a, x ] )
		self._childrenTest( a.ifTrue( [ x ] ).elif_( b, [ y ] ), [ a, x, b, y ] )
		self._childrenTest( a.ifTrue( [ x ] ).elif_( b, [ y ] ).else_( [ z ] ), [ a, x, b, y, z ] )



	def test_PyDef(self):
		x = PyVar( 'x' )()
		self._childrenTest( PyDef( 'foo', [], [ x ] ),  [ x ] )

		
	def test_PyAssign_SideEffects(self):
		a = PyVar( 'a' )
		b = PyVar( 'b' )
		self._childrenTest( a.assign_sideEffects( b ),  [ a, b ] )

	def test_PyDel_SideEffects(self):
		a = PyVar( 'a' )
		self._childrenTest( a.del_sideEffects(),  [ a ] )
		
		
		

if __name__ == '__main__':
	unittest.main()
