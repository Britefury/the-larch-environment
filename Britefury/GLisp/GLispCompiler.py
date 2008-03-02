##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.GLispInterpreter import isGLispList
from Britefury.GLisp.PyCodeGen import PyCodeGenError, PySrc, PyVar, PyLiteral, PyListLiteral, PyGetAttr, PyGetItem, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyReturn, PyIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects




TEMP_NAME_PREFIX = '__gsym__temp_'


class _PyScope (object):
	def __init__(self):
		super( _PyScope, self ).__init__()
		self._tempCounter = 0
		self._boundLocals = set()


	def allocateTempName(self):
		tempName = TEMP_NAME_PREFIX + str( self._tempCounter )
		self._tempCounter += 1
		return tempName
	
	
	def isLocalBound(self, name):
		return name in self._boundLocals
	
	def bindLocal(self, name):
		self._boundLocals.add( name )
		
	def unbindLocal(self, name):
		self._boundLocals.remove( name )
		
	
	




class GLispCompilerError (PyCodeGenError):
	pass

class GLispCompilerCouldNotCompileSpecial (PyCodeGenError):
	pass

class GLispCompilerVariableNameMustStartWithAt (PyCodeGenError):
	pass


def compileGLispExprToPyTree(xs, compileSpecialExpr=None):
	"""
	compileGLispExprToPyTree( xs, compileSpecialExpr=None )  ->  PyNode tree
	
	Compiles the GLisp code in @xs to a PyNode tree
	   @xs - GLisp tree
	   @compileSpecialExpr - function( xs, compileSpecialExpr )  ->  PyNode tree
	      A function used to compile special expressions
	      A special expression is an expression where the first element of the GLisp node is a string starting with '/'
	         @xs is the GLisp tree to be compiled by compileSpecialExpr
		 @compileSpecialExpr is the value passed to compileGLispExprToPyTree

	"""
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
			return PyListLiteral( [ compileGLispExprToPyTree( e, compileSpecialExpr )   for e in xs[1:] ], dbgSrc=xs )
		elif isinstance( xs[0], str )  and  xs[0][0] == '/'  and  compileSpecialExpr is not None:
			res = compileSpecialExpr( xs, compileSpecialExpr )
			if res is not None:
				return res
			else:
				raise GLispCompilerCouldNotCompileSpecial( xs )
		elif len(xs) == 1:
			return compileGLispExprToPyTree( xs[0], compileSpecialExpr )
		else:
			method = xs[1]
			if method == '.'  and  len(xs) == 3:
				return PyGetAttr( compileGLispExprToPyTree( xs[0], compileSpecialExpr ), xs[2], dbgSrc=xs )
			elif method == '[]'  and  len(xs) == 3:
				return PyGetItem( compileGLispExprToPyTree( xs[0], compileSpecialExpr ), compileGLispExprToPyTree( xs[2], compileSpecialExpr ), dbgSrc=xs )
			elif method == '[:]'  and  len(xs) == 4:
				return PyGetItem( compileGLispExprToPyTree( xs[0], compileSpecialExpr ), compileGLispExprToPyTree( xs[2], compileSpecialExpr ), compileGLispExprToPyTree( xs[3], compileSpecialExpr ), dbgSrc=xs )
			elif method in PyUnOp.operators   and   len(xs) == 2:
				return PyUnOp( xs[1], compileGLispExprToPyTree( xs[0], compileSpecialExpr ), dbgSrc=xs )
			elif method in PyBinOp.operators   and   len(xs) == 3:
				return PyBinOp( compileGLispExprToPyTree( xs[0], compileSpecialExpr ), xs[1], compileGLispExprToPyTree( xs[2], compileSpecialExpr ), dbgSrc=xs )
			elif method == '-<>':
				return PyCall( compileGLispExprToPyTree( xs[0], compileSpecialExpr ), [ compileGLispExprToPyTree( e, compileSpecialExpr )   for e in xs[2:] ], dbgSrc=xs )
			else:
				return PyMethodCall( compileGLispExprToPyTree( xs[0], compileSpecialExpr ), xs[1], [ compileGLispExprToPyTree( e, compileSpecialExpr )   for e in xs[2:] ], dbgSrc=xs )


def compileGLispExprToPySrc(xs, compileSpecialExpr=None):
	return compileGLispExprToPyTree( xs, compileSpecialExpr ).compileAsExpr()

			
			
class GLispCompilerWhereStmtParamListInsufficient (PyCodeGenError):
	pass

class GLispCompilerWhereStmtInvalidBindingListType (PyCodeGenError):
	pass

class GLispCompilerWhereStmtInvalidBindingListFormat (PyCodeGenError):
	pass

class GLispCompilerWhereStmtCannotRebindVariable (PyCodeGenError):
	pass



def _compileWhereStatement(xs, scope, compileSpecialStatement, compileSpecialExpr):
	"""
	($where ((name0 value0) (name1 value1) ... (nameN valueN)) (statements_to_execute))
	"""
	if len( xs ) < 2:
		raise GLispCompilerWhereStmtParamListInsufficient( xs )

	bindings = xs[1]
	statements = xs[2:]
	
	if not isGLispList( bindings ):
		raise GLispCompilerWhereStmtInvalidBindingListType( xs )
	
	entryTrees = []
	contentTrees = []
	exitTrees = []
	
	boundNames = []
	backupNames = {}
	for binding in bindings:
		if not isGLispList( binding )  or  len( binding ) != 2:
			raise GLispCompilerWhereStmtInvalidBindingListFormat( binding )
		
		if binding[0][0] != '@':
			raise GLispCompilerVariableNameMustStartWithAt( binding[0] )
		
		name = binding[0][1:]
		valueExpr = binding[1]
		if name in boundNames:
			raise GLispCompilerWhereStmtCannotRebindVariable( binding )
		
		# If this name is already bound in thise scope, we need to back it up first
		if scope.isLocalBound( name ):
			backupName = scope.allocateTempName()
			backupPyTree = PyAssign_SideEffects( PyVar( backupName, dbgSrc=binding ), PyVar( name, dbgSrc=binding ), dbgSrc=binding )
			entryTrees.append( backupPyTree )
			backupNames[name] = backupName
		else:
			scope.bindLocal( name )

		valueExprPyTree = compileGLispExprToPyTree( valueExpr, compileSpecialExpr )
		assignmentPyTree = PyAssign_SideEffects( PyVar( name, dbgSrc=binding ), valueExprPyTree, dbgSrc=binding )
		entryTrees.append( assignmentPyTree )
		
		scope.bindLocal( name )
		
		boundNames.append( name )
		
	for statement in statements:
		contentTrees.extend( _compileGLispStatementToPyTree( statement, scope, compileSpecialStatement, compileSpecialExpr ) )
		
	# Restore/delete the bound variables
	for binding in reversed( bindings ):
		name = binding[0][1:]
		try:
			backupName = backupNames[name]
		except KeyError:
			# This name is not backed up; delete it
			delPyTree = PyDel_SideEffects( PyVar( name, dbgSrc=binding ), dbgSrc=binding )
			exitTrees.append( delPyTree )
			scope.unbindLocal( name )
		else:
			# This name is backed up: restore it
			restorePyTree = PyAssign_SideEffects( PyVar( name, dbgSrc=binding ), PyVar( backupName, dbgSrc=binding ), dbgSrc=binding )
			# Delete the backup name
			delPyTree = PyDel_SideEffects( PyVar( backupName, dbgSrc=binding ), dbgSrc=binding )
			exitTrees.append( restorePyTree )
			exitTrees.append( delPyTree )
			
			
	return entryTrees + contentTrees + exitTrees



class GLispCompilerIfStmtParamListInsufficient (PyCodeGenError):
	pass

class GLispCompilerIfStmtNeedConditionCodePairs (PyCodeGenError):
	pass


def _compileIfStatement(xs, scope, compileSpecialStatement, compileSpecialExpr):
	"""
	( $if (condition0 code0A code0B ...) (condition1 code1A code1B ...)) [($else codeElseA codeElseB ...))] )
	"""
	if len( xs ) < 2:
		raise GLispCompilerIfStmtParamListInsufficient( xs )

	pairs = xs[1:]
	
	# Extrace the if and elif blocks, and the else block (if present)
	if pairs[-1][0] == '$else':
		ifElifXs = pairs[:-1]
		elseXs = pairs[-1]
		if len( ifElifXs ) < 1:
			raise GLispCompilerIfStmtNeedConditionCodePairs( xs )
	else:
		ifElifXs = pairs
		elseXs = None
		
	
	def _conditionAndCodeXsToPyTree(conditionAndCodeXs):
		conditionXs = conditionAndCodeXs[0]
		codeXs = conditionAndCodeXs[1:]
		conditionPyTree = compileGLispExprToPyTree( conditionXs, compileSpecialExpr )
		codePyTrees = []
		for x in codeXs:
			codePyTrees.extend( _compileGLispStatementToPyTree( x, scope, compileSpecialStatement, compileSpecialExpr ) )
		return conditionPyTree, codePyTrees
		
	def _elseCodeXsToPyTree(elseCodeXs):
		codeXs = elseCodeXs[1:]
		codePyTrees = []
		for x in codeXs:
			codePyTrees.extend( _compileGLispStatementToPyTree( x, scope, compileSpecialStatement, compileSpecialExpr ) )
		return codePyTrees

	ifElifSpecs = [ _conditionAndCodeXsToPyTree( x )   for x in ifElifXs ]
	elseSpecs = None
	if elseXs is not None:
		elseSpecs = _elseCodeXsToPyTree( elseXs )
	return [ PyIf( ifElifSpecs, elseSpecs, dbgSrc=xs ) ]



class GLispCompilerDefStmtParamListInsufficient (PyCodeGenError):
	pass

class GLispCompilerDefStmtInvalidFunctionNameType (PyCodeGenError):
	pass

class GLispCompilerDefStmtInvalidArgListType (PyCodeGenError):
	pass

class GLispCompilerDefStmtInvalidArgNameType (PyCodeGenError):
	pass



def _compileDefStatement(xs, scope, compileSpecialStatement, compileSpecialExpr):
	"""
	( $def functionName (arg0Name arg1Name ...) code0 code1 code2 ... )
	"""
	if len( xs ) < 3:
		raise GLispCompilerDefStmtParamListInsufficient( xs )

	functionName = xs[1]
	argNamesXs = xs[2]
	codeXs = xs[3:]
	
	if not isinstance( functionName, str ):
		raise GLispCompilerDefStmtInvalidFunctionNameType( xs )
	
	if not isGLispList( argNamesXs ):
		raise GLispCompilerDefStmtInvalidFunctionNameType( xs )
	
	for argName in argNamesXs:
		if not isinstance( argName, str ):
			raise GLispCompilerDefStmtInvalidArgNameType( xs )
		
	argNames = argNamesXs[:]
	
	statments = []
	if len( codeXs ) > 0:
		if _isGLispStatement( codeXs[-1] ):
			statementsXs = codeXs[:-1]
			returnExprXs = codeXs[-1]
		else:
			statementsXs = codeXs
			returnExprXs = None
		
		for stmtXs in statementsXs:
			statements.extend( _compileGLispStatementToPyTree( stmtXs, scope, compileSpecialStatement, compileSpecialExpr ) )
		
		if returnExprXs is not None:
			statements.append( PyReturn( compileGLispExprToPyTree( returnExprXs, compileSpecialExpr ), dbgSrc=returnExprXs ) )
	
	return [ PyDef( functionName, argNames, statements, dbgSrc=xs ) ]



def _compileGLispStatementToPyTree(xs, scope, compileSpecialStatement, compileSpecialExpr):
	if isGLispList( xs ):
		if len( xs ) >= 1:
			if xs[0] == '$where':
				return _compileWhereStatement( xs, scope, compileSpecialStatement, compileSpecialExpr)
			elif xs[0] == '$if':
				return _compileIfStatement( xs, scope, compileSpecialStatement, compileSpecialExpr)
			elif xs[0] == '$def':
				return _compileDefStatement( xs, scope, compileSpecialStatement, compileSpecialExpr)
			elif xs[0][0] == '$':
				return compileSpecialStatement( xs, scope, compileSpecialStatement, compileSpecialExpr )
				
	return [ compileGLispExprToPyTree( xs, compileSpecialExpr ) ]


def _isGLispStatement(xs):
	if isGLispList( xs ):
		if len( xs ) >= 1:
			return xs[0][0] == '$'
	return False
	



def compileGLispStatementToPyTree(xs, compileSpecialStmt=None, compileSpecialExpr=None):
	return _compileGLispStatementToPyTree( xs, _PyScope(), compileSpecialStmt, compileSpecialExpr )


def compileGLispStatementToPySrc(xs, compileSpecialStmt=None, compileSpecialExpr=None):
	srcLines = []
	for tree in compileGLispStatementToPyTree( xs, compileSpecialStmt, compileSpecialExpr ):
		srcLines.extend( tree.compileAsStmt() )
		
	return '\n'.join( srcLines ) + '\n'



import unittest
from Britefury.DocModel.DMIO import readSX


class TestCase_GLispCompiler_compileGLispExprToPySrc (unittest.TestCase):
	def _compileTest(self, srcText, expectedValue, compileSpecialExpr=None):
		if isinstance( expectedValue, str ):
			self.assert_( compileGLispExprToPySrc( readSX( srcText ), compileSpecialExpr ) ==  expectedValue )
		else:
			self.assertRaises( expectedValue, lambda: compileGLispExprToPySrc( readSX( srcText ), compileSpecialExpr ) )

	def _printCompileTest(self, srcText, expectedValue, compileSpecialExpr=None):
		print compileGLispExprToPySrc( readSX( srcText ), compileSpecialExpr )

	def testNone(self):
		self._compileTest( '`nil`', 'None' )

	def testVar(self):
		self._compileTest( '@a', 'a' )

	def testNumLit(self):
		self._compileTest( '#1', '1' )

	def testStrLit(self):
		self._compileTest( 'a', '\'a\'' )
		self._compileTest( '1', '\'1\'' )





	def testEmptyList(self):
		self._compileTest( '()', 'None' )

	def testSingleElementList(self):
		self._compileTest( '(@a)', 'a' )
		self._compileTest( '((@a))', 'a' )

	def testListLiteral(self):
		self._compileTest( '(/list @a @b @c)', '[ a, b, c ]' )
		
	def testCompileSpecial(self):
		def compileSpecialExpr(xs, compileSpecialExpr):
			if xs[0] == '/special':
				return PySrc( 'special' )
			return None
		self._compileTest( '(/special)', 'special', compileSpecialExpr )
		self._compileTest( '(/abc123)', GLispCompilerCouldNotCompileSpecial, compileSpecialExpr )

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




class TestCase_GLispCompiler_compileGLispStatementToPySrc (unittest.TestCase):
	def _compileTest(self, srcText, expectedValue, compileSpecialStmt=None, compileSpecialExpr=None):
		expectedValue = '\n'.join( expectedValue ) + '\n'
		if isinstance( expectedValue, str ):
			self.assert_( compileGLispStatementToPySrc( readSX( srcText ), compileSpecialStmt, compileSpecialExpr ) ==  expectedValue )
		else:
			self.assertRaises( expectedValue, lambda: compileGLispStatementToPySrc( readSX( srcText ), compileSpecialStmt, compileSpecialExpr ) )

	def _printCompileTest(self, srcText, expectedValue, compileSpecialStmt=None, compileSpecialExpr=None):
		expectedValue = '\n'.join( expectedValue ) + '\n'
		result = compileGLispStatementToPySrc( readSX( srcText ), compileSpecialStmt, compileSpecialExpr )
		print result
		e = min( len( result ), len( expectedValue ) )
		for i in xrange( e, 0, -1 ):
			if result.startswith( expectedValue[:i] ):
				print ''
				print 'First %d characters match (result/expected)'  %  ( i, )
				print result[:i+1]
				print ''
				print expectedValue[:i+1]
				print ''
				break
				
		



		
	def test_Where(self):
		pysrc1 = [
			'a = 1',
			'b = \'test\'',
			'b.split()',
			'del b',
			'del a',
		]

		pysrc2 = [
			'a = \'1\'',
			'b = \'test\'',
			
			'b.split()',
			
			'__gsym__temp_0 = a',
			'a = 1',
			'__gsym__temp_1 = b',
			'b = \'test\'',
			'b.split()',
			'b = __gsym__temp_1',
			'del __gsym__temp_1',
			'a = __gsym__temp_0',
			'del __gsym__temp_0',
			
			'a.split()',		

			'del b',
			'del a',
		]
		
		self._compileTest( '($where   (  (@a #1) (@b test)  )   (@b split))', pysrc1 )
		self._compileTest( '($where   (  (@a 1) (@b test)  )   (@b split)  ($where   (  (@a #1) (@b test)  )   (@b split))  (@a split)   )', pysrc2 )


	def test_If(self):
		pysrc1 = [
			'if a == \'Hi\':',
			'  a.split()',
		]

		pysrc2 = [
			'if a == \'Hi\':',
			'  a.split()',
			'else:',
			'  c.split()',
		]
		
		pysrc3 = [
			'if a == \'Hi\':',
			'  a.split()',
			'elif b == \'There\':',
			'  b.split()',
		]
		
		pysrc4 = [
			'if a == \'Hi\':',
			'  a.split()',
			'elif b == \'There\':',
			'  b.split()',
			'else:',
			'  c.split()',
		]
		
		self._compileTest( '($if   ( (@a == Hi)  (@a split) )  )', pysrc1 )
		self._compileTest( '($if   ( (@a == Hi)  (@a split) )   ( $else (@c split) )  )', pysrc2 )
		self._compileTest( '($if   ( (@a == Hi)  (@a split) )   ( (@b == There) (@b split) )  )', pysrc3 )
		self._compileTest( '($if   ( (@a == Hi)  (@a split) )   ( (@b == There) (@b split) )   ( $else (@c split) )  )', pysrc4 )

		
if __name__ == '__main__':
	unittest.main()