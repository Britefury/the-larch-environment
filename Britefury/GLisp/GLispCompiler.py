##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.PyCodeGen import PyCodeGenError, PySrc, PyVar, PyLiteral, PyListLiteral, PyGetAttr, PyGetItem, PyAssign_SideEffects, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyReturn, PyIf, PyDef




class GLispCompilerError (PyCodeGenError):
	pass

class GLispCompilerCouldNotCompileSpecial (PyCodeGenError):
	pass

class GLispCompilerVariableNameMustStartWithAt (PyCodeGenError):
	pass


def compileGLispExprToPyTree(xs, compileSpecialExpr=None):
	if xs is None:
		return PyLiteral( 'None', dbgSrc=xs )
	elif isinstance( xs, str ):
		if xs[0] == '@':
			return PyVar( xs[1:], dbgSrc=xs )
		elif xs[0] == '#':
			return PyLiteral( xs[1:], dbgSrc=xs )
		else:
			return PyLiteral( '\'' + xs.replace( '\'', '\\\'' ) + '\'', dbgSrc=xs )
	else:
		if len(xs) == 0:
			return PyLiteral( 'None', dbgSrc=xs )
		elif xs[0] == '/list':
			return PyListLiteral( [ _compileGLispExprToPyNode( e, compileSpecialExpr )   for e in xs[1:] ], dbgSrc=xs )
		elif isinstance( xs[0], str )  and  xs[0][0] == '/'  and  compileSpecialExpr is not None:
			res = compileSpecialExpr( xs )
			if res is not None:
				return res
			else:
				raise GLispCompilerCouldNotCompileSpecial( xs )
		elif len(xs) == 1:
			return _compileGLispExprToPyNode( xs[0], compileSpecialExpr )
		else:
			method = xs[1]
			if method == '.'  and  len(xs) == 3:
				return PyGetAttr( _compileGLispExprToPyNode( xs[0], compileSpecialExpr ), xs[2], dbgSrc=xs )
			elif method == '[]'  and  len(xs) == 3:
				return PyGetItem( _compileGLispExprToPyNode( xs[0], compileSpecialExpr ), _compileGLispExprToPyNode( xs[2], compileSpecialExpr ), dbgSrc=xs )
			elif method == '[:]'  and  len(xs) == 4:
				return PyGetItem( _compileGLispExprToPyNode( xs[0], compileSpecialExpr ), _compileGLispExprToPyNode( xs[2], compileSpecialExpr ), _compileGLispExprToPyNode( xs[3], compileSpecialExpr ), dbgSrc=xs )
			elif method in PyUnOp.operators   and   len(xs) == 2:
				return PyUnOp( xs[1], _compileGLispExprToPyNode( xs[0], compileSpecialExpr ), dbgSrc=xs )
			elif method in PyBinOp.operators   and   len(xs) == 3:
				return PyBinOp( _compileGLispExprToPyNode( xs[0], compileSpecialExpr ), xs[1], _compileGLispExprToPyNode( xs[2], compileSpecialExpr ), dbgSrc=xs )
			elif method == '-<>':
				return PyCall( _compileGLispExprToPyNode( xs[0], compileSpecialExpr ), [ _compileGLispExprToPyNode( e, compileSpecialExpr )   for e in xs[2:] ], dbgSrc=xs )
			else:
				return PyMethodCall( _compileGLispExprToPyNode( xs[0], compileSpecialExpr ), xs[1], [ _compileGLispExprToPyNode( e, compileSpecialExpr )   for e in xs[2:] ], dbgSrc=xs )


			
			
class GLispCompilerWhereStmtParamListInsufficient (PyCodeGenError):
	pass

class GLispCompilerWhereStmtInvalidBindingListType (PyCodeGenError):
	pass

class GLispCompilerWhereStmtInvalidBindingListFormat (PyCodeGenError):
	pass

class GLispCompilerWhereStmtCannotRebindVariable (PyCodeGenError):
	pass



def _compileWhere(xs, compileSpecialExpr):
	"""
	($where ((name0 value0) (name1 value1) ... (nameN valueN)) (statements_to_execute))
	"""
	if len( xs ) < 2:
		raise GLispCompilerWhereStmtParamListInsufficient( xs )

	bindings = xs[1]
	statements = xs[2:]
	
	if not isGLispList( bindings ):
		raise GLispCompilerWhereStmtInvalidBindingListType( xs )
	
	assignments = []
	
	boundNames = set()
	for binding in bindings:
		if not isGLispList( binding )  or  len( binding ) != 2:
			raise GLispCompilerWhereStmtInvalidBindingListFormat( binding )
		
		if binding[0][0] != '@':
			raise GLispCompilerVariableNameMustStartWithAt( binding[0] )
		
		name = binding[0][1:]
		valueExpr = binding[1]
		if name in boundNames:
			raise GLispCompilerWhereStmtCannotRebindVariable( binding )
		
		valueExprPyTree = compileGLispExprToPyTree( valueExpr, compileSpecialExpr )
		assignmentPyTree = PyAssign_SideEffects( PyVar( name ), valueExprPyTree, binding )
		assignments.append( assignmentPyTree )
		
		boundNames.add( name )
		
	for srcLine in statements:
		pyLines.extend( compileGLispStatementToPySrc( srcLine, compileSpecialExpr ) )

	return pyLines
			
	


import unittest
from Britefury.DocModel.DMIO import readSX


class TestCase_GLispCompiler_compileGLispExprToPyNode (unittest.TestCase):
	def _compileTest(self, srcText, expectedValue, compileSpecialExpr=None):
		if isinstance( expectedValue, str ):
			self.assert_( compileGLispExprToPyTree( readSX( srcText ), compileSpecialExpr ).compileAsExpr()  ==  expectedValue )
		else:
			self.assertRaises( expectedValue, lambda: compileGLispExprToPyTree( readSX( srcText ), compileSpecialExpr ).compileAsExpr() )

	def _printCompileTest(self, srcText, expectedValue, compileSpecialExpr=None):
		print compileGLispExprToPyTree( readSX( srcText ), compileSpecialExpr ).compileAsExpr()

	def testNone(self):
		self._compileTest( '`nil`', 'None' )

	def testVar(self):
		self._compileTest( '@a', 'a' )

	def testNumLit(self):
		self._compileTest( '#1', '1' )

	def testStrLit(self):
		self._compileTest( 'a', '\'a\'' )




	def testEmptyList(self):
		self._compileTest( '()', 'None' )

	def testSingleElementList(self):
		self._compileTest( '(@a)', 'a' )
		self._compileTest( '((@a))', 'a' )

	def testListLiteral(self):
		self._compileTest( '(/list @a @b @c)', '[ a, b, c ]' )
		
	def testCompileSpecial(self):
		def compileSpecialExpr(xs):
			if xs[0] == '/special':
				return PySrc( 'special' )
			return None
		self._compileTest( '(/special)', 'special', compileSpecialExpr )
		self._compileTest( '(/abc123)', GLispCompilerCouldNotCompileSpecialError, compileSpecialExpr )

	def testGetAttr(self):
		self._compileTest( '(@a . b)', 'a.b' )

	def testGetItem(self):
		self._compileTest( '(@a [] @b)', 'a[b]' )
		self._compileTest( '(@a [:] @b @c) ', 'a[b:c]' )

	def testUnOp(self):
		self._compileTest( '(@a -)', '-a' )
		self._compileTest( '(@a ~)', '~a' )
		self._compileTest( '(@a not)', 'not a' )

	def testBinOp(self):
		for op in PyBinOp.operators:
			self._compileTest( '(@a %s @b)'  %  ( op, ),   'a %s b'  %  ( op, ) )

	def testCall(self):
		self._compileTest( '(@a -<>)', 'a()' )
		self._compileTest( '(@a -<> @b @c)', 'a( b, c )' )

	def testMethodCall(self):
		self._compileTest( '(@a foo)', 'a.foo()' )
		self._compileTest( '(@a foo @b @c)', 'a.foo( b, c )' )


if __name__ == '__main__':
	unittest.main()