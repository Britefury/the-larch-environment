##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.GLispInterpreter import isGLispList
from Britefury.GLisp.PyCodeGen import PyCodeGenError, PySrc, PyVar, PyLiteral, PyListLiteral, PyGetAttr, PyGetItem, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyReturn, PyIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects




TEMP_VAR_NAME_PREFIX = '__gsym__temp_'
TEMP_FUNCTION_NAME_PREFIX = '__gsym__fn_'


class _PyScope (object):
	def __init__(self):
		super( _PyScope, self ).__init__()
		self._varTempCounter = 0
		self._functionTempCounter = 0
		self._boundLocals = set()


	def allocateTempName(self):
		tempName = TEMP_VAR_NAME_PREFIX + str( self._varTempCounter )
		self._varTempCounter += 1
		return tempName
	
	def allocateTempFunctionName(self):
		tempName = TEMP_FUNCTION_NAME_PREFIX + str( self._functionTempCounter )
		self._functionTempCounter += 1
		return tempName
	
	
	def isLocalBound(self, name):
		return name in self._boundLocals
	
	def bindLocal(self, name):
		self._boundLocals.add( name )
		
	def unbindLocal(self, name):
		self._boundLocals.remove( name )
		
		
		
class _CompilationContext (object):
	def __init__(self, scope):
		self.scope = scope
		self.body = []
		
		
	def innerContext(self):
		return _CompilationContext( self.scope )
		
		
		
		

def _compileExpressionListToPyTreeStatements(expressions, context, bNeedResult, compileSpecial, buildResultHandlerTreeNode):
	"""Compiles an expression list to a list of statements
	_compileExpressionListToPyTreeStatements( expressions, context, bNeedResult, compileSpecial, buildResultHandlerTreeNode)  ->  trees, wrappedResultTree
	
	expressions - the list of expressions to compile
	context - the compilation context
	bNeedResult - True if the result is required by an outside computation, False otherwise
	compileSpecial - function to compile special expressions
	buildResultHandlerTreeNode - if bNeedResult is True, and there are 1 or more expressions, this function will be called to create a node to wrap the result of the last expression
	   buildResultHandlerTreeNode( lastResultTree, lastExpressionXs )  ->  Py Tree
	
	trees - a list of Py Tree nodes
	wrappedResultTree - the result node wrapped in the node created by buildResultHandlerTreeNode()
	"""
	# Expression code
	if bNeedResult  and  len( expressions ) > 0:
		initialExpressions = expressions[:-1]
		lastExpression = expressions[-1]
		bHasLastExpression = True
	else:
		initialExpressions = expressions
		lastExpression = None
		bHasLastExpression = False
		
		
	trees = []
	wrappedResultTree = None
	for expr in initialExpressions:
		exprContext = context.innerContext()
		exprTree = _compileGLispExprToPyTree( expr, exprContext, False, compileSpecial )
		trees.extend( exprContext.body )
		if exprTree is not None:
			trees.append( exprTree )
	if bHasLastExpression:
		exprContext = context.innerContext()
		wrappedResultTree = buildResultHandlerTreeNode( _compileGLispExprToPyTree( lastExpression, exprContext, True, compileSpecial ), lastExpression )
		trees.extend( exprContext.body )
		trees.append( wrappedResultTree )
	return trees, wrappedResultTree
	

	




class GLispCompilerError (PyCodeGenError):
	pass

class GLispCompilerCouldNotCompileSpecial (PyCodeGenError):
	pass

class GLispCompilerVariableNameMustStartWithAt (PyCodeGenError):
	pass



class GLispCompilerWhereExprParamListInsufficient (PyCodeGenError):
	pass

class GLispCompilerWhereExprInvalidBindingListType (PyCodeGenError):
	pass

class GLispCompilerWhereExprInvalidBindingListFormat (PyCodeGenError):
	pass

class GLispCompilerWhereExprCannotRebindVariable (PyCodeGenError):
	pass

def _compileWhere(xs, context, bNeedResult=False, compileSpecial=None):
	"""
	($where ((name0 value0) (name1 value1) ... (nameN valueN)) (expressions_to_execute))
	"""
	if len( xs ) < 2:
		raise GLispCompilerWhereExprParamListInsufficient( xs )
	
	scope = context.scope

	bindings = xs[1]
	expressions = xs[2:]
	
	if not isGLispList( bindings ):
		raise GLispCompilerWhereExprInvalidBindingListType( xs )
	
	contentTrees = []
	valueName = None
	exitTrees = []
	
	whereTrees = []
	
	# Make binding code
	boundNames = []
	backupNames = {}
	for binding in bindings:
		bindingContext = context.innerContext()
		if not isGLispList( binding )  or  len( binding ) != 2:
			raise GLispCompilerWhereExprInvalidBindingListFormat( binding )
		
		if binding[0][0] != '@':
			raise GLispCompilerVariableNameMustStartWithAt( binding[0] )
		
		name = binding[0][1:]
		valueExpr = binding[1]
		if name in boundNames:
			raise GLispCompilerWhereExprCannotRebindVariable( binding )
		
		# If this name is already bound in thise scope, we need to back it up first
		if scope.isLocalBound( name ):
			backupName = scope.allocateTempName()
			backupPyTree = PyAssign_SideEffects( PyVar( backupName, dbgSrc=binding ), PyVar( name, dbgSrc=binding ), dbgSrc=binding )
			whereTrees.append( backupPyTree )
			backupNames[name] = backupName
		else:
			scope.bindLocal( name )

		valueExprPyTree = _compileGLispExprToPyTree( valueExpr, bindingContext, True, compileSpecial )
		assignmentPyTree = PyAssign_SideEffects( PyVar( name, dbgSrc=binding ), valueExprPyTree, dbgSrc=binding )
		
		whereTrees.extend( bindingContext.body )
		whereTrees.append( assignmentPyTree )
		
		scope.bindLocal( name )
		
		boundNames.append( name )
		

	
	# Where expression code
	if bNeedResult:
		valueName = scope.allocateTempName()
	trees, resultStorePyTree = _compileExpressionListToPyTreeStatements( expressions, context, bNeedResult, compileSpecial, lambda tree, x: PyAssign_SideEffects( PyVar( valueName, dbgSrc=x ), tree, dbgSrc=x ) )
	whereTrees.extend( trees )
	
		
		

	# Restore/delete the bound variables
	for binding in reversed( bindings ):
		name = binding[0][1:]
		try:
			backupName = backupNames[name]
		except KeyError:
			# This name is not backed up; delete it
			delPyTree = PyDel_SideEffects( PyVar( name, dbgSrc=binding ), dbgSrc=binding )
			whereTrees.append( delPyTree )
			scope.unbindLocal( name )
		else:
			# This name is backed up: restore it
			restorePyTree = PyAssign_SideEffects( PyVar( name, dbgSrc=binding ), PyVar( backupName, dbgSrc=binding ), dbgSrc=binding )
			# Delete the backup name
			delPyTree = PyDel_SideEffects( PyVar( backupName, dbgSrc=binding ), dbgSrc=binding )
			whereTrees.append( restorePyTree )
			whereTrees.append( delPyTree )
			
			
	context.body.extend( whereTrees )
			
	if resultStorePyTree is not None:
		return PyVar( valueName, dbgSrc=xs )
	else:
		if bNeedResult:
			return PyLiteral( 'None', dbgSrc=xs )
		else:
			return None





class GLispCompilerIfExprParamListInsufficient (PyCodeGenError):
	pass

class GLispCompilerIfExprNeedConditionCodePairs (PyCodeGenError):
	pass


def _compileIf(xs, context, bNeedResult=False, compileSpecial=None):
	"""
	( $if (condition0 code0A code0B ...) (condition1 code1A code1B ...)) [($else codeElseA codeElseB ...))] )
	"""
	if len( xs ) < 2:
		raise GLispCompilerIfExprParamListInsufficient( xs )

	pairs = xs[1:]
	
	# Extrace the if and elif blocks, and the else block (if present)
	if pairs[-1][0] == '$else':
		ifElifXs = pairs[:-1]
		elseXs = pairs[-1]
		if len( ifElifXs ) < 1:
			raise GLispCompilerIfExprNeedConditionCodePairs( xs )
	else:
		ifElifXs = pairs
		elseXs = None
		
		
	if bNeedResult:
		resultVarName = context.scope.allocateTempName()
	else:
		resultVarName = None
		
	
	def _conditionAndCodeXsToPyTree(conditionAndCodeXs):
		conditionXs = conditionAndCodeXs[0]
		codeXs = conditionAndCodeXs[1:]
		conditionPyTree = _compileGLispExprToPyTree( conditionXs, context, True, compileSpecial )
		
		codePyTrees, wrappedResultTree = _compileExpressionListToPyTreeStatements( codeXs, context, bNeedResult, compileSpecial, lambda t, x: PyAssign_SideEffects( PyVar( resultVarName, dbgSrc=x ), t, dbgSrc=x ) )
		
		if wrappedResultTree is None  and  bNeedResult:
			codePyTrees.append( PyAssign_SideEffects( PyVar( resultVarName, dbgSrc=conditionAndCodeXs ), PyLiteral( 'None', dbgSrc=conditionAndCodeXs ), dbgSrc=conditionAndCodeXs ) )
			
		return conditionPyTree, codePyTrees
		
	
	def _elseCodeXsToPyTree(elseCodeXs):
		codeXs = elseCodeXs[1:]
		codePyTrees, wrappedResultTree = _compileExpressionListToPyTreeStatements( codeXs, context, bNeedResult, compileSpecial, lambda t, x: PyAssign_SideEffects( PyVar( resultVarName, dbgSrc=x ), t, dbgSrc=x ) )
		
		if wrappedResultTree is None  and  bNeedResult:
			codePyTrees.append( PyAssign_SideEffects( PyVar( resultVarName, dbgSrc=elseCodeXs ), PyLiteral( 'None', dbgSrc=elseCodeXs ), dbgSrc=elseCodeXs ) )

		return codePyTrees


	ifElifSpecs = [ _conditionAndCodeXsToPyTree( x )   for x in ifElifXs ]
	elseSpecs = None
	if elseXs is not None:
		elseSpecs = _elseCodeXsToPyTree( elseXs )
	else:
		context.body.append( PyAssign_SideEffects( PyVar( resultVarName, dbgSrc=xs ), PyLiteral( 'None', dbgSrc=xs ), dbgSrc=xs ) )
	context.body.append( PyIf( ifElifSpecs, elseSpecs, dbgSrc=xs ) )
	
	if bNeedResult:
		return PyVar( resultVarName, dbgSrc=xs )
	else:
		return None





class GLispCompilerLambdaExprParamListInsufficient (PyCodeGenError):
	pass

class GLispCompilerLambdaExprInvalidFunctionNameType (PyCodeGenError):
	pass

class GLispCompilerLambdaExprInvalidArgListType (PyCodeGenError):
	pass

class GLispCompilerLambdaExprInvalidArgNameType (PyCodeGenError):
	pass

class GLispCompilerLambdaExprArgMustStartWithAt (PyCodeGenError):
	pass



def _compileLambda(xs, context, bNeedResult=False, compileSpecial=None):
	"""
	( $lambda (arg0Name arg1Name ...) code0 code1 code2 ... )
	"""
	if len( xs ) < 2:
		raise GLispCompilerLambdaExprParamListInsufficient( xs )

	argNamesXs = xs[1]
	codeXs = xs[2:]
	
	functionName = context.scope.allocateTempFunctionName()
	
	if not isGLispList( argNamesXs ):
		raise GLispCompilerLambdaExprInvalidFunctionNameType( xs )
	
	for argName in argNamesXs:
		if not isinstance( argName, str ):
			raise GLispCompilerLambdaExprInvalidArgNameType( xs )
		if argName[0] != '@':
			raise GLispCompilerLambdaExprArgMustStartWithAt( xs )
		
	argNames = [ argName[1:]   for argName in argNamesXs ]
	
	
	fnBodyPyTrees, wrappedResultTree = _compileExpressionListToPyTreeStatements( codeXs, context.innerContext(), True, compileSpecial, lambda t, x: PyReturn( t, dbgSrc=x ) )
	
	context.body.append( PyDef( functionName, argNames, fnBodyPyTrees, dbgSrc=xs ) )
	
	if bNeedResult:
		return PyVar( functionName, dbgSrc=xs )
	else:
		return none





def _compileGLispExprToPyTree(xs, context, bNeedResult=False, compileSpecial=None):
	"""
	_compileGLispExprToPyTree( xs, context, bNeedResult=False, compileSpecial=None )  ->  PyNode tree
	
	Compiles the GLisp code in @xs to a PyNode tree
	   @xs - GLisp tree
	   @context - the context in which to compile @xs
	   @bNeedResult - True if the result is required for further computation
	   @compileSpecial - function( xs, compileSpecial )  ->  PyNode tree
	      A function used to compile special expressions
	      A special expression is an expression where the first element of the GLisp node is a string starting with '/'
	         @xs is the GLisp tree to be compiled by compileSpecial
		 @compileSpecial is the value passed to _compileGLispExprToPyTree

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
		elif xs[0] == '$list':
			return PyListLiteral( [ _compileGLispExprToPyTree( e, context, True, compileSpecial )   for e in xs[1:] ], dbgSrc=xs )
		elif xs[0] == '$where':
			return _compileWhere( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$if':
			return _compileIf( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$lambda':
			return _compileLambda( xs, context, bNeedResult, compileSpecial )
		elif isinstance( xs[0], str )  and  xs[0][0] == '$'  and  compileSpecial is not None:
			res = compileSpecial( xs, compileSpecial )
			if res is not None:
				return res
			else:
				raise GLispCompilerCouldNotCompileSpecial( xs )
		elif len(xs) == 1:
			return _compileGLispExprToPyTree( xs[0], context, bNeedResult, compileSpecial )
		else:
			method = xs[1]
			if method == '.'  and  len(xs) == 3:
				return PyGetAttr( _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ), xs[2], dbgSrc=xs )
			elif method == '[]'  and  len(xs) == 3:
				return PyGetItem( _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ), _compileGLispExprToPyTree( xs[2], context, True, compileSpecial ), dbgSrc=xs )
			elif method == '[:]'  and  len(xs) == 4:
				return PyGetItem( _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ),
						  _compileGLispExprToPyTree( xs[2], context, True, compileSpecial ),
						  _compileGLispExprToPyTree( xs[3], context, True, compileSpecial ), dbgSrc=xs )
			elif method in PyUnOp.operators   and   len(xs) == 2:
				return PyUnOp( xs[1], _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ), dbgSrc=xs )
			elif method in PyBinOp.operators   and   len(xs) == 3:
				return PyBinOp( _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ), xs[1], _compileGLispExprToPyTree( xs[2], context, True, compileSpecial ), dbgSrc=xs )
			elif method == '<->':
				return PyCall( _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ), [ _compileGLispExprToPyTree( e, context, True, compileSpecial )   for e in xs[2:] ], dbgSrc=xs )
			else:
				return PyMethodCall( _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ), xs[1], [ _compileGLispExprToPyTree( e, context, True, compileSpecial )   for e in xs[2:] ], dbgSrc=xs )


def compileGLispExprToPyTrees(xs, compileSpecial=None):
	"""
	compileGLispExprToPyTrees( xs, compileSpecial )  ->  trees, resultTree
	
	Compiles the GLisp code in @xs to a PyNode tree
	   @xs - GLisp tree
	   @compileSpecial - function( xs, compileSpecial )  ->  PyNode tree
	      A function used to compile special expressions
	      A special expression is an expression where the first element of the GLisp node is a string starting with '/'
	         @xs is the GLisp tree to be compiled by compileSpecial
		 @compileSpecial is the value passed to _compileGLispExprToPyTree
	"""
	context = _CompilationContext( _PyScope() )
	resultTree = _compileGLispExprToPyTree( xs, context, True, compileSpecial )
	return context.body  +  [ resultTree ],  resultTree

			
			
def compileGLispExprToPySrc(xs, compileSpecial=None):
	"""
	compileGLispExprToPySrc( xs, compileSpecial )  ->  source
	
	Compiles the GLisp code in @xs to a Python source code
	   @xs - GLisp tree
	   @compileSpecial - function( xs, compileSpecial )  ->  PyNode tree
	      A function used to compile special expressions
	      A special expression is an expression where the first element of the GLisp node is a string starting with '/'
	         @xs is the GLisp tree to be compiled by compileSpecial
		 @compileSpecial is the value passed to _compileGLispExprToPyTree
	"""
	trees, resultTree = compileGLispExprToPyTrees( xs, compileSpecial )
	srcLines = []
	for t in trees:
		srcLines.extend( t.compileAsStmt() )
	return '\n'.join( srcLines )  +  '\n'



def compileGLispExprToPyFunctionPyTree(functionName, argNames, xs, compileSpecial=None):
	"""
	compileGLispExprToPyFunctionPyTree( functionName, argNames, xs, compileSpecial=None )  ->  PyTree
	
	Compile the expression @xs into a PyTree that represents a function that returns the result of @xs.
	   @functionName - the name of the function that will be defined
	   @argNames - the names of the function arguments
	   @xs - the GLisp source
	   @compileSpecial - function( xs, compileSpecial )  ->  PyNode tree
	      A function used to compile special expressions
	      A special expression is an expression where the first element of the GLisp node is a string starting with '/'
	         @xs is the GLisp tree to be compiled by compileSpecial
		 @compileSpecial is the value passed to _compileGLispExprToPyTree
	"""
	context = _CompilationContext( _PyScope() )
	
	fnBodyPyTrees, wrappedResultTree = _compileExpressionListToPyTreeStatements( [ xs ], context, True, compileSpecial, lambda t, x: PyReturn( t, dbgSrc=x ) )
	
	return PyDef( functionName, argNames, fnBodyPyTrees, dbgSrc=xs )



def compileGLispExprToPyFunctionSrc(functionName, argNames, xs, compileSpecial=None):
	"""
	compileGLispExprToPyFunctionSrc( functionName, argNames, xs, compileSpecial=None )  ->  python source
	
	Compile the expression @xs into python source code that describes a function that returns the result of @xs.
	   @functionName - the name of the function that will be defined
	   @argNames - the names of the function arguments
	   @xs - the GLisp source
	   @compileSpecial - function( xs, compileSpecial )  ->  PyNode tree
	      A function used to compile special expressions
	      A special expression is an expression where the first element of the GLisp node is a string starting with '/'
	         @xs is the GLisp tree to be compiled by compileSpecial
		 @compileSpecial is the value passed to _compileGLispExprToPyTree
	"""
	tree = compileGLispExprToPyFunctionPyTree( functionName, argNames, xs, compileSpecial )
	return '\n'.join( tree.compileAsStmt() )  +  '\n'





import unittest
from Britefury.DocModel.DMIO import readSX


class TestCase_GLispCompiler_compileGLispExprToPySrc (unittest.TestCase):
	def _compileTest(self, srcText, expectedValue, compileSpecial=None):
		if isinstance( expectedValue, list ):
			expectedValue = '\n'.join( expectedValue )  +  '\n'
		if isinstance( expectedValue, str ):
			if len( expectedValue ) == 0  or  expectedValue[-1] != '\n':
				expectedValue += '\n'
			self.assert_( compileGLispExprToPySrc( readSX( srcText ), compileSpecial ) ==  expectedValue )
		else:
			self.assertRaises( expectedValue, lambda: compileGLispExprToPySrc( readSX( srcText ), compileSpecial ) )
			
	def _evalTest(self, srcText, expectedResult, argValuePairs=[], compileSpecial=None):
		argNames = [ p[0]   for p in argValuePairs ]
		argValues = [ p[1]   for p in argValuePairs ]
		src = compileGLispExprToPyFunctionSrc( 'test', argNames, readSX( srcText ), compileSpecial )
		lcls = {}
		exec src in lcls
		result = lcls['test']( *argValues )
		if result != expectedResult:
			print 'RESULT = ', result
			print 'EXPECTED RESULT = ', expectedResult
			print 'SOURCE'
			print src
		self.assert_( result == expectedResult )

	def _printCompileTest(self, srcText, expectedValue, compileSpecialExpr=None):
		if isinstance( expectedValue, list ):
			expectedValue = '\n'.join( expectedValue )  +  '\n'
		if isinstance( expectedValue, str ):
			if len( expectedValue ) == 0  or  expectedValue[-1] != '\n':
				expectedValue += '\n'
		result = compileGLispExprToPySrc( readSX( srcText ), compileSpecialExpr )
		if result == expectedValue:
			print result
		else:
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

	def testNone(self):
		self._compileTest( '`nil`', 'None' )
		self._evalTest( '`nil`', None )

	def testVar(self):
		self._compileTest( '@a', 'a' )
		self._evalTest( '@a', 2, [ ( 'a', 2 ) ] )

	def testNumLit(self):
		self._compileTest( '#1', '1' )
		self._evalTest( '#1', 1 )

	def testStrLit(self):
		self._compileTest( 'a', '\'a\'' )
		self._compileTest( '1', '\'1\'' )
		self._evalTest( 'a', 'a' )





	def testEmptyList(self):
		self._compileTest( '()', 'None' )
		self._evalTest( '()', None )

	def testSingleElementList(self):
		self._compileTest( '(@a)', 'a' )
		self._compileTest( '((@a))', 'a' )
		self._evalTest( '(#1)', 1 )

	def testListLiteral(self):
		self._compileTest( '($list @a @b @c)', '[ a, b, c ]' )
		self._evalTest( '($list #1 #2 #3)', [ 1, 2, 3 ] )
		
	def testCompileSpecial(self):
		def compileSpecialExpr(xs, compileSpecial):
			if xs[0] == '$special':
				return PySrc( 'special' )
			return None
		self._compileTest( '($special)', 'special', compileSpecialExpr )
		self._compileTest( '($abc123)', GLispCompilerCouldNotCompileSpecial, compileSpecialExpr )
		self._evalTest( '($special)', 123, [ ( 'special', 123 ) ], compileSpecialExpr )

	def testGetAttr(self):
		class _C (object):
			def __init__(self, b):
				self.b = b
		self._compileTest( '(@a . b)', 'a.b' )
		self._evalTest( '(@a . b)', 123, [ ( 'a', _C( 123 ) ) ] )

	def testGetItem(self):
		self._compileTest( '(@a [] @b)', 'a[b]' )
		self._compileTest( '(@a [:] @b @c) ', 'a[b:c]' )
		self._evalTest( '(@a [] #5)', 5, [ ( 'a', range(0,10) ) ] )
		self._evalTest( '(@a [:] #1 #5)', range(1,5), [ ( 'a', range(0,10) ) ] )

	def testUnOp(self):
		self._compileTest( '(@a -)', '-a' )
		self._compileTest( '(@a ~)', '~a' )
		self._compileTest( '(@a not)', 'not a' )
		self._evalTest( '(#5 -)', -5 )
		self._evalTest( '(#5 ~)', -6 )
		self._evalTest( '(#True not)', False )

	def testBinOp(self):
		for op in PyBinOp.operators:
			self._compileTest( '(@a %s @b)'  %  ( op, ),   'a %s b'  %  ( op, ) )
		self._evalTest( '(#7 * #5)', 35 )

	def testCall(self):
		def _f(x):
			return x + 3
		self._compileTest( '(@a <->)', 'a()' )
		self._compileTest( '(@a <-> @b @c)', 'a( b, c )' )
		self._evalTest( '(@f <-> #5)', 8, [ ( 'f', _f ) ] )

	def testMethodCall(self):
		self._compileTest( '(@a foo)', 'a.foo()' )
		self._compileTest( '(@a foo @b @c)', 'a.foo( b, c )' )
		self._evalTest( '(f upper)', 'F' )


	def test_Where(self):
		pysrc1 = [
			'a = 1',
			'b = \'test\'',
			'b.split()',
			'__gsym__temp_0 = a + 1',
			'del b',
			'del a',

			'__gsym__temp_0',
		]

		pysrc2 = [
			'a = \'1\'',
			'b = \'test\'',
			
			'b.split()',
			
			'__gsym__temp_1 = a',
			'a = 1',
			'__gsym__temp_2 = b',
			'b = \'test\'',
			'b.split()',
			'b = __gsym__temp_2',
			'del __gsym__temp_2',
			'a = __gsym__temp_1',
			'del __gsym__temp_1',
			
			'__gsym__temp_0 = a.split()',		

			'del b',
			'del a',

			'__gsym__temp_0',
		]
		
		pysrc3 = [
			'a = 1',
			
			'x = \'abc\'',
			'y = \'def\'',
			'__gsym__temp_0 = x + y',
			'del y',
			'del x',
			
			'b = __gsym__temp_0',
			'a + 1',
			'__gsym__temp_1 = b.split()',
			'del b',
			'del a',

			'__gsym__temp_1',
		]

		pysrc4 = [
			'a = 1',
			'b = \'test\'',
			'del b',
			'del a',

			'None',
		]

		pysrc5 = [
			'a = 1',
			'b = 2',
			'__gsym__temp_0 = a + b',
			'del b',
			'del a',

			'x + __gsym__temp_0',
		]

		self._compileTest( '($where   (  (@a #1) (@b test)  )   (@b split) (@a + #1))', pysrc1 )
		self._compileTest( '($where   (  (@a 1) (@b test)  )   (@b split)  ($where   (  (@a #1) (@b test)  )   (@b split))  (@a split)   )', pysrc2 )
		self._compileTest( '($where   (  (@a #1) (@b    ($where ( (@x abc) (@y def) )   (@x + @y)   )  )   )   (@a + #1) (@b split) )', pysrc3 )
		self._compileTest( '($where   (  (@a #1) (@b test)  )   )', pysrc4 )
		self._compileTest( '(@x + ($where   (  (@a #1) (@b #2)  )   (@a + @b)))', pysrc5 )
		self._evalTest( '($where   (  (@a hi) (@b there)  )   @a )', 'hi' )
		self._evalTest( '($where   (  (@a hi) (@b there)  )   @b )', 'there' )
		self._evalTest( '($where   (  (@a hi) (@b there)  )   ((@a + @b) upper))', 'HITHERE' )


		
		
		
	def test_If(self):
		pysrc1 = [
			'__gsym__temp_0 = None',
			'if a == \'Hi\':',
			'  __gsym__temp_0 = a.split()',
			'__gsym__temp_0',
		]

		pysrc2 = [
			'if a == \'Hi\':',
			'  __gsym__temp_0 = a.split()',
			'else:',
			'  __gsym__temp_0 = c.split()',
			'__gsym__temp_0',
		]
		
		pysrc3 = [
			'__gsym__temp_0 = None',
			'if a == \'Hi\':',
			'  __gsym__temp_0 = a.split()',
			'elif b == \'There\':',
			'  __gsym__temp_0 = b.split()',
			'__gsym__temp_0',
		]
		
		pysrc4 = [
			'if a == \'Hi\':',
			'  __gsym__temp_0 = a.split()',
			'elif b == \'There\':',
			'  __gsym__temp_0 = b.split()',
			'else:',
			'  __gsym__temp_0 = c.split()',
			'__gsym__temp_0',
		]
		
		self._compileTest( '($if   ( (@a == Hi)  (@a split) )  )', pysrc1 )
		self._compileTest( '($if   ( (@a == Hi)  (@a split) )   ( $else (@c split) )  )', pysrc2 )
		self._compileTest( '($if   ( (@a == Hi)  (@a split) )   ( (@b == There) (@b split) )  )', pysrc3 )
		self._compileTest( '($if   ( (@a == Hi)  (@a split) )   ( (@b == There) (@b split) )   ( $else (@c split) )  )', pysrc4 )
		self._printCompileTest( '($if   ( (@a == #1)  hi)   ( (@a == #2) there) )', 'hi' )
		self._evalTest( '($if   ( (@a == #1)  hi)   ( (@a == #2) there) )', 'hi', [ ( 'a', 1 ) ] )
		self._evalTest( '($if   ( (@a == #1)  hi)   ( (@a == #2) there) )', None, [ ( 'a', 4 ) ] )

	
	def test_Lambda(self):
		pysrc1 = [
			'def __gsym__fn_0(x, y, z):',
			'  x.split()',
			'  return x + (y + z)',
			'__gsym__fn_0',
		]

		
		self._compileTest( '($lambda   (@x @y @z)  (@x split) (@x + (@y + @z))  )', pysrc1 )
		self._evalTest( '( ($lambda   (@x @y @z)  (@x + (@y + @z))  ) <-> @a @b @c)', 6, [ ( 'a', 1 ), ( 'b', 2 ), ( 'c', 3 ) ] )

	
	def test_Where_If_Lambda(self):
		xssrc1 = \
		"""
		(@x +
		  ($where
	             (
		       (@a #1)
		       (@b #23)
		       (@c
		         ($if
			   ( (@q == #1) #5 )
			   ( (@q == #2) #6 )
			   ( (@q == #3) #7 )
			 )
		       )
		       (@d
		         ($lambda (@i @j @k)
			    (@i + (@j + @k))
			 )
		       )
		     )
		     (@d <-> @a @b @c)
		  )
		)
		"""
		pysrc1 = [
			'a = 1',
			'b = 23',
			'__gsym__temp_0 = None',
			'if q == 1:',
			'  __gsym__temp_0 = 5',
			'elif q == 2:',
			'  __gsym__temp_0 = 6',
			'elif q == 3:',
			'  __gsym__temp_0 = 7',
			'c = __gsym__temp_0',
			'def __gsym__fn_0(i, j, k):',
			'  return i + (j + k)',
			'd = __gsym__fn_0',
			'__gsym__temp_1 = d( a, b, c )',
			'del d',
			'del c',
			'del b',
			'del a',
			'x + __gsym__temp_1',
		]
		self._compileTest( xssrc1, pysrc1 )





		
if __name__ == '__main__':
	unittest.main()