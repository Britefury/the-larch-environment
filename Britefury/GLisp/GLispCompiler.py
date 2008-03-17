##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.GLispUtil import isGLispList, stripGLispComments, gLispSrcToString
from Britefury.GLisp.PyCodeGen import PyCodeGenError, PySrc, PyVar, PyLiteral, PyLiteralValue, PyListLiteral, PyGetAttr, PyGetItem, PyGetSlice, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyReturn, PyIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects
import Britefury.GLisp.PatternMatch





_TEMP_PREFIX = '__gsym__'


def _log(x):
	print x
	return x


class _TempNameAllocator (object):
	def __init__(self):
		super( _TempNameAllocator, self ).__init__()
		self._tempCounters = {}


	def allocateTempName(self, prefix):
		index = self._tempCounters.get( prefix, 0 )
		self._tempCounters[prefix] = index + 1
		return _TEMP_PREFIX + prefix + '_' + str( index )
	
		
		
		
class _PyScope (object):
	def __init__(self):
		super( _PyScope, self ).__init__()
		self._visibleArguments = set()


	def registerArgument(self, name):
		self._visibleArguments.add( name )
		
	def getVisibleArguments(self):
		return self._visibleArguments
		



class _CompilationContext (object):
	def __init__(self, temps, scope):
		self.temps = temps
		self.scope = scope
		self.body = []
		
		
	def innerContext(self):
		return _CompilationContext( self.temps, self.scope )
		
	def functionInnerContext(self):
		return _CompilationContext( self.temps, _PyScope() )
		
		
		

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
	expressions = stripGLispComments( expressions )
	
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
		if wrappedResultTree is not None:
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
	
	bindings = xs[1]
	expressions = xs[2:]
	
	if not isGLispList( bindings ):
		raise GLispCompilerWhereExprInvalidBindingListType( xs )
	
	contentTrees = []
	valueName = None
	exitTrees = []
	
	whereTrees = []
	

	whereFnName = context.temps.allocateTempName( 'where_fn' )

	
	# Make binding code
	boundNames = []
	bindings = stripGLispComments( bindings )
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
		valueExprPyTree = _compileGLispExprToPyTree( valueExpr, bindingContext, True, compileSpecial )
		assignmentPyTree = valueExprPyTree.assignToVar_sideEffects( name ).debug( binding )
		
		whereTrees.extend( bindingContext.body )
		whereTrees.append( assignmentPyTree )
		
		boundNames.append( name )
		

	
	# Where expression code
	trees, resultStorePyTree = _compileExpressionListToPyTreeStatements( expressions, context, True, compileSpecial, lambda tree, x: PyReturn( tree, dbgSrc=x ) )
	whereTrees.extend( trees )
	
	
	# Turn it into a function (def)
	fnTree = PyDef( whereFnName, [], whereTrees, dbgSrc=xs )

	context.body.append( fnTree )
	
	callTree = PyVar( whereFnName )().debug( xs )
			
	return callTree





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
		resultVarName = context.temps.allocateTempName( 'if_result' )
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
		context.body.append( PyVar( resultVarName ).assign_sideEffects( None ).debug( xs ) )
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
	
	functionName = context.temps.allocateTempName( 'lambda' )
	
	if not isGLispList( argNamesXs ):
		raise GLispCompilerLambdaExprInvalidFunctionNameType( xs )
	
	for argName in argNamesXs:
		if not isinstance( argName, str ):
			raise GLispCompilerLambdaExprInvalidArgNameType( xs )
		if argName[0] != '@':
			raise GLispCompilerLambdaExprArgMustStartWithAt( xs )
		
	argNames = [ argName[1:]   for argName in argNamesXs ]
	
	
	fnBodyContext = context.functionInnerContext()
	for argName in argNames:
		fnBodyContext.scope.registerArgument( argName )
	fnBodyPyTrees, wrappedResultTree = _compileExpressionListToPyTreeStatements( codeXs, fnBodyContext, True, compileSpecial, lambda t, x: PyReturn( t, dbgSrc=x ) )
	
	context.body.append( PyDef( functionName, argNames, fnBodyPyTrees, dbgSrc=xs ) )
	
	if bNeedResult:
		return PyVar( functionName, dbgSrc=xs )
	else:
		return None

	
	
	
	
class GLispCompilerMatchExprParamListInsufficient (PyCodeGenError):
	pass


def _compileMatch(xs, context, bNeedResult, compileSpecial):
	"""
	($match data_expression_to_match
	  (pattern0 action0...)
	  (pattern1 action1...)
	  ...
	  (patternN actionN...)
	)
	"""
	if len( xs ) < 2:
		raise GLispCompilerMatchExprParamListInsufficient( xs )
	
	dataExprXs = xs[1]
	matchBlockXs = xs[2:]
	
	
	# Compile the data expression
	dataVarName = context.temps.allocateTempName( 'match_data' )
	context.body.append( PyVar( dataVarName ).assign_sideEffects( _compileGLispExprToPyTree( dataExprXs, context, True, compileSpecial ) ).debug( xs ) )
	
	matchTrees, resultTree = Britefury.GLisp.PatternMatch.compileMatchBlockToPyTrees( xs, matchBlockXs, context, bNeedResult, dataVarName, compileSpecial )
	
	context.body.extend( matchTrees )
	
	return resultTree
	
	
							     



class GLispCompilerSyntaxError (PyCodeGenError):
	pass

class GLispCompilerInvalidLiteral (PyCodeGenError):
	pass

def _compileGLispExprToPyTree(xs, context, bNeedResult=False, compileSpecial=None):
	"""
	_compileGLispExprToPyTree( xs, context, bNeedResult=False, compileSpecial=None )  ->  PyNode tree
	
	Compiles the GLisp code in @xs to a PyNode tree
	   @xs - GLisp tree
	   @context - the context in which to compile @xs
	   @bNeedResult - True if the result is required for further computation
	   @compileSpecial - function( xs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree )  ->  PyNode tree
	      A function used to compile special expressions
	      A special expression is an expression where the first element of the GLisp node is a string starting with '/'
	         @xs is the GLisp tree to be compiled by compileSpecial
	         @context is the compilation context
		 @bNeedResult - True if the result is required for further computation
		 @compileSpecial is the value passed to _compileGLispExprToPyTree
		 @compileGLispExprToPyTree is the function to call (will be passed _compileGLispExprToPyTree) to compile sub-expressions

	"""
	if xs is None:
		return PyLiteral( 'None', dbgSrc=xs )
	elif isinstance( xs, str )  or  isinstance( xs, unicode ):
		if xs[0] == '@':
			if xs[1] == '@':
				return PyLiteralValue( xs[1:] )
			else:
				return PyVar( xs[1:], dbgSrc=xs )
		elif xs[0] == '#':
			value = xs[1:]
			if value == 'None'  or  value == 'False'  or  value == 'True':
				return PyLiteral( value )
			elif value.startswith( "'" ):
				return PyLiteralValue( value[1:] )
			else:
				try:
					return PyLiteralValue( int( value ) )
				except ValueError:
					try:
						return PyLiteralValue( float( value ) )
					except ValueError:
						pass
			raise GLispCompilerInvalidLiteral( xs )
	else:
		if len(xs) == 0:
			return PyLiteral( 'None', dbgSrc=xs )
		elif xs[0] == '$list':
			return PyListLiteral( [ _compileGLispExprToPyTree( e, context, True, compileSpecial )   for e in xs[1:] ], dbgSrc=xs )
		elif xs[0] == '$set':
			return PyCall( PyVar( 'set', dbgSrc=xs ), [ PyListLiteral( [ _compileGLispExprToPyTree( e, context, True, compileSpecial )   for e in xs[1:] ], dbgSrc=xs ) ], dbgSrc=xs )
		elif xs[0] == '$lambda':
			return _compileLambda( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$if':
			return _compileIf( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$where':
			return _compileWhere( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$match':
			return _compileMatch( xs, context, bNeedResult, compileSpecial )
		elif isinstance( xs[0], str )  and  xs[0][0] == '$':
			if compileSpecial is None:
				raise GLispCompilerCouldNotCompileSpecial( xs )
			else:
				return compileSpecial( xs, context, bNeedResult, compileSpecial, _compileGLispExprToPyTree )
		elif len(xs) == 1:
			return _compileGLispExprToPyTree( xs[0], context, bNeedResult, compileSpecial )
		else:
			method = xs[1]
			if method == '.'  and  len(xs) == 3:
				return PyGetAttr( _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ), xs[2], dbgSrc=xs )
			elif method == '[]'  and  len(xs) == 3:
				return PyGetItem( _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ), _compileGLispExprToPyTree( xs[2], context, True, compileSpecial ), dbgSrc=xs )
			elif method == '[:]'  and  len(xs) == 4:
				return PyGetSlice( _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ),
						  _compileGLispExprToPyTree( xs[2], context, True, compileSpecial ),
						  _compileGLispExprToPyTree( xs[3], context, True, compileSpecial ), dbgSrc=xs )
			elif method in PyUnOp.operators   and   len(xs) == 2:
				return PyUnOp( xs[1], _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ), dbgSrc=xs )
			elif method in PyBinOp.operators   and   len(xs) == 3:
				return PyBinOp( _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ), xs[1], _compileGLispExprToPyTree( xs[2], context, True, compileSpecial ), dbgSrc=xs )
			elif method == '<-':
				return PyCall( _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ), [ _compileGLispExprToPyTree( e, context, True, compileSpecial )   for e in xs[2:] ], dbgSrc=xs )
			else:
				return PyMethodCall( _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ), xs[1], [ _compileGLispExprToPyTree( e, context, True, compileSpecial )   for e in xs[2:] ], dbgSrc=xs )
	raise GLispCompilerSyntaxError( xs )


def compileGLispExprToPyTrees(xs, compileSpecial=None, ):
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
	context = _CompilationContext( _TempNameAllocator(), _PyScope() )
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



def compileGLispExprToPyFunctionPyTree(functionName, argNames, xs, compileSpecial=None, prefixTrees=[], resultPyTreePostProcess=lambda tree, xs: tree):
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
	   @prefixTrees - a list of PyTrees that should be compiled
           @resultPyTreePostProcess - function( tree, xs )  ->  PyNode tree
	      A function used to post process the result tree
	         @tree - the result tree (to be post-processed or wrapped)
		 @xs - the GLisp source
	"""
	context = _CompilationContext( _TempNameAllocator(), _PyScope() )
	
	fnBodyPyTrees, wrappedResultTree = _compileExpressionListToPyTreeStatements( [ xs ], context, True, compileSpecial, lambda t, x: PyReturn( resultPyTreePostProcess( t, x ), dbgSrc=x ) )
	fnBodyPyTrees = prefixTrees + fnBodyPyTrees
	
	return PyDef( functionName, argNames, fnBodyPyTrees, dbgSrc=xs )



def compileGLispExprToPyFunctionSrc(functionName, argNames, xs, compileSpecial=None, prefixTrees=[], resultPyTreePostProcess=lambda tree, xs: tree):
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
	tree = compileGLispExprToPyFunctionPyTree( functionName, argNames, xs, compileSpecial, prefixTrees, resultPyTreePostProcess )
	return '\n'.join( tree.compileAsStmt() )  +  '\n'



def compileGLispExprToPyFunction(moduleName, functionName, argNames, xs, compileSpecial=None, lcls={}, prefixTrees=[], resultPyTreePostProcess=lambda tree, xs: tree):
	"""
	compileGLispExprToPyFunctionSrc( moduleName, functionName, argNames, xs, compileSpecial=None )  ->  python source
	
	Compile the expression @xs into python source code that describes a function that returns the result of @xs.
	   @moduleName - the name of the module that will be built
	   @functionName - the name of the function that will be defined
	   @argNames - the names of the function arguments
	   @xs - the GLisp source
	   @compileSpecial - function( xs, compileSpecial )  ->  PyNode tree
	      A function used to compile special expressions
	      A special expression is an expression where the first element of the GLisp node is a string starting with '/'
	         @xs is the GLisp tree to be compiled by compileSpecial
		 @compileSpecial is the value passed to _compileGLispExprToPyTree
	"""
	src = compileGLispExprToPyFunctionSrc( functionName, argNames, xs, compileSpecial, prefixTrees, resultPyTreePostProcess )
	lcls['__isGLispList__'] = isGLispList
	lcls['NoMatchError'] = Britefury.GLisp.PatternMatch.NoMatchError
	lcls['gLispAsString'] = gLispSrcToString
	lcls['log'] = _log
	codeObject = compile( src, moduleName, 'exec' )
	exec codeObject in lcls
	return lcls[functionName]




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
			
	def _evalTest(self, srcText, expectedResult, argValuePairs=[], compileSpecial=None, lcls={}):
		argNames = [ p[0]   for p in argValuePairs ]
		argValues = [ p[1]   for p in argValuePairs ]
		fn = compileGLispExprToPyFunction( 'testModule', 'test', argNames, readSX( srcText ), compileSpecial, lcls=lcls )
		result = fn( *argValues )
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
			print 'FULL RESULT'
			print result

	def testSyntaxError(self):
		self._compileTest( 'a', GLispCompilerSyntaxError )

	def testVar(self):
		self._compileTest( '@a', 'a' )
		self._evalTest( '@a', 2, [ ( 'a', 2 ) ] )

	def testNone(self):
		self._compileTest( '#None', 'None' )
		self._evalTest( '#None', None )

	def testBool(self):
		self._compileTest( '#False', 'False' )
		self._compileTest( '#True', 'True' )
		self._evalTest( '#False', False )
		self._evalTest( '#True', True )

	def testIntLit(self):
		self._compileTest( '#1', '1' )
		self._evalTest( '#1', 1 )

	def testFloatLit(self):
		self._compileTest( '#1.1', repr( 1.1 ) )
		self._evalTest( '#1.1', 1.1 )

	def testStrLit(self):
		self._compileTest( "\"#'a\"", '\'a\'' )
		self._compileTest( "\"#'1\"", '\'1\'' )
		self._evalTest( "\"#'a\"", 'a' )





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
		def compileSpecialExpr(xs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
			if xs[0] == '$special':
				return PySrc( 'special' )
			else:
				raise GLispCompilerCouldNotCompileSpecial( xs )
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
		self._compileTest( '(@a <-)', 'a()' )
		self._compileTest( '(@a <- @b @c)', 'a( b, c )' )
		self._evalTest( '(@f <- #5)', 8, [ ( 'f', _f ) ] )

	def testMethodCall(self):
		self._compileTest( '(@a foo)', 'a.foo()' )
		self._compileTest( '(@a foo @b @c)', 'a.foo( b, c )' )
		self._evalTest( "(\"#'f\" upper)", 'F' )


	def test_Where(self):
		xssrc1 = """
		($where
		  (
		    (@a #1)
		    (@b "#'test")
	          )
		  (@b split)
		  (@a + #1)
		)
		"""
		
		pysrc1 = [
			'def __gsym__where_fn_0():',
			'  a = 1',
			'  b = \'test\'',
			
			'  b.split()',
			'  return a + 1',

			'__gsym__where_fn_0()',
		]
		
		
		
		xssrc2 = """
		($where
		  (
		    (@a "#'1")
		    (@b "#'test")
		  )
		  (@b split)
		  ($where
		    (
		      (@c #1)
		      (@d "#'test")
		    )
		    (@d split)
		  )
		  (@a split)
		)
		"""

		pysrc2 = [
			'def __gsym__where_fn_0():',
			'  a = \'1\'',
			'  b = \'test\'',
			
			'  b.split()',
			'  def __gsym__where_fn_1():',
			'    c = 1',
			'    d = \'test\'',
			
			'    return d.split()',
			'  __gsym__where_fn_1()',
			'  return a.split()',
			'__gsym__where_fn_0()',
		]
		

		
		xssrc3 = """
		($where
		  (
		    (@a #1)
		    (@b
		      ($where
		        (
			  (@x "#'abc")
			  (@y "#'def")
			)
			(@x + @y)
		      )
		    )
		  )
		  (@a + #1)
		  (@b split)
		)
		"""
		
		pysrc3 = [
			'def __gsym__where_fn_0():',
			'  a = 1',
			'  def __gsym__where_fn_1():',
			'    x = \'abc\'',
			'    y = \'def\'',
			'    return x + y',
			'  b = __gsym__where_fn_1()',
			
			'  a + 1',
			'  return b.split()',
			'__gsym__where_fn_0()',
		]



		xssrc4 = """
		($where
		  (
		    (@a #1)
		    (@b "#'test")
		  )
		)
		"""

		pysrc4 = [
			'def __gsym__where_fn_0():',
			'  a = 1',
			'  b = \'test\'',
			'__gsym__where_fn_0()',
		]

		
		
		xssrc5 = """
		(@x + 
		  ($where
		    (
		      (@a #1)
		      (@b #2)
		    )
		    (@a + @b)
		  )
		)
		"""
		
		pysrc5 = [
			'def __gsym__where_fn_0():',
			'  a = 1',
			'  b = 2',
			'  return a + b',
			
			'x + __gsym__where_fn_0()',
		]

		self._compileTest( xssrc1, pysrc1 )
		self._compileTest( xssrc2, pysrc2 )
		self._compileTest( xssrc3, pysrc3 )
		self._compileTest( xssrc4, pysrc4 )
		self._compileTest( xssrc5, pysrc5 )
		self._evalTest( """($where   (  (@a "#'hi") (@b "#'there")  )   @a )""", 'hi' )
		self._evalTest( """($where   (  (@a "#'hi") (@b "#'there")  )   @b )""", 'there' )
		self._evalTest( """($where   (  (@a "#'hi") (@b "#'there")  )   ((@a + @b) upper))2""", 'HITHERE' )


		
		
		
	def test_If(self):
		pysrc1 = [
			'__gsym__if_result_0 = None',
			'if a == \'Hi\':',
			'  __gsym__if_result_0 = a.split()',
			'__gsym__if_result_0',
		]

		pysrc2 = [
			'if a == \'Hi\':',
			'  __gsym__if_result_0 = a.split()',
			'else:',
			'  __gsym__if_result_0 = c.split()',
			'__gsym__if_result_0',
		]
		
		pysrc3 = [
			'__gsym__if_result_0 = None',
			'if a == \'Hi\':',
			'  __gsym__if_result_0 = a.split()',
			'elif b == \'There\':',
			'  __gsym__if_result_0 = b.split()',
			'__gsym__if_result_0',
		]
		
		pysrc4 = [
			'if a == \'Hi\':',
			'  __gsym__if_result_0 = a.split()',
			'elif b == \'There\':',
			'  __gsym__if_result_0 = b.split()',
			'else:',
			'  __gsym__if_result_0 = c.split()',
			'__gsym__if_result_0',
		]
		
		self._compileTest( '($if   ( (@a == "#\'Hi")  (@a split) )  )', pysrc1 )
		self._compileTest( '($if   ( (@a == "#\'Hi")  (@a split) )   ( $else (@c split) )  )', pysrc2 )
		self._compileTest( '($if   ( (@a == "#\'Hi")  (@a split) )   ( (@b == "#\'There") (@b split) )  )', pysrc3 )
		self._compileTest( '($if   ( (@a == "#\'Hi")  (@a split) )   ( (@b == "#\'There") (@b split) )   ( $else (@c split) )  )', pysrc4 )
		self._evalTest( '($if   ( (@a == #1)  "#\'hi")   ( (@a == #2) "#\'there") )', 'hi', [ ( 'a', 1 ) ] )
		self._evalTest( '($if   ( (@a == #1)  "#\'hi")   ( (@a == #2) "#\'there") )', None, [ ( 'a', 4 ) ] )

	
	def test_Lambda(self):
		pysrc1 = [
			'def __gsym__lambda_0(x, y, z):',
			'  x.split()',
			'  return x + (y + z)',
			'__gsym__lambda_0',
		]

		
		self._compileTest( '($lambda   (@x @y @z)  (@x split) (@x + (@y + @z))  )', pysrc1 )
		self._evalTest( '( ($lambda   (@x @y @z)  (@x + (@y + @z))  ) <- @a @b @c)', 6, [ ( 'a', 1 ), ( 'b', 2 ), ( 'c', 3 ) ] )

		
	def test_Match(self):
		xssrc1 = """
		($match ($list "#'a" "#'b" "#'c" "#'d" "#'e" "#'f" "#'g" "#'h")
		   (   (a b (: @a !) (: @b !) (: @c *))   ($list @a @b @c)   )
		)
		"""
		
		xssrc2 = """
		($match ($list "#'a" "#'b" "#'c" "#'d" "#'e" "#'f" "#'g" "#'h")
		   (   (a b (: @a !) (: @b !) (: @c *))   ($list @a @b @c)   )
		   (   (x b (: @a !) (: @b !) (: @c *))   ($list @a @b @c)   )
		)
		"""
		
		xssrc3 = """
		($match ($list "#'a" "#'b" "#'c" "#'deadbeef" "#'e" "#'f" "#'g" "#'h")
		   (   (a b (: @a !) (? @x (@x startswith "#\'dead") (: @b !)) (: @c *))   ($list @a @b @c)   )
		)
		"""
		
		pysrc1 = [
			"__gsym__match_data_0 = [ 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' ]",
			"__gsym__match_bMatched_0 = [ False ]",
			"if __isGLispList__( __gsym__match_data_0 ):",
			"  if len( __gsym__match_data_0 ) >= 4:",
			"    if __gsym__match_data_0[0] == 'a':",
			"      if __gsym__match_data_0[1] == 'b':",
			"        if not __isGLispList__( __gsym__match_data_0[2] ):",
			"          if not __isGLispList__( __gsym__match_data_0[3] ):",
			"            def __gsym__match_fn_0(a, b, c):",
			"              __gsym__match_bMatched_0[0] = True",
			"              return [ a, b, c ]",
			"            __gsym__match_result_0 = __gsym__match_fn_0( __gsym__match_data_0[2], __gsym__match_data_0[3], __gsym__match_data_0[4:] )",
			"if not __gsym__match_bMatched_0[0]:",
			"  raise NoMatchError",
			"__gsym__match_result_0",
		]

		pysrc2 = [
			"__gsym__match_data_0 = [ 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' ]",
			"__gsym__match_bMatched_0 = [ False ]",
			"if __isGLispList__( __gsym__match_data_0 ):",
			"  if len( __gsym__match_data_0 ) >= 4:",
			"    if __gsym__match_data_0[0] == 'a':",
			"      if __gsym__match_data_0[1] == 'b':",
			"        if not __isGLispList__( __gsym__match_data_0[2] ):",
			"          if not __isGLispList__( __gsym__match_data_0[3] ):",
			"            def __gsym__match_fn_0(a, b, c):",
			"              __gsym__match_bMatched_0[0] = True",
			"              return [ a, b, c ]",
			"            __gsym__match_result_0 = __gsym__match_fn_0( __gsym__match_data_0[2], __gsym__match_data_0[3], __gsym__match_data_0[4:] )",
			"if not __gsym__match_bMatched_0[0]:",
			"  if __isGLispList__( __gsym__match_data_0 ):",
			"    if len( __gsym__match_data_0 ) >= 4:",
			"      if __gsym__match_data_0[0] == 'x':",
			"        if __gsym__match_data_0[1] == 'b':",
			"          if not __isGLispList__( __gsym__match_data_0[2] ):",
			"            if not __isGLispList__( __gsym__match_data_0[3] ):",
			"              def __gsym__match_fn_1(a, b, c):",
			"                __gsym__match_bMatched_0[0] = True",
			"                return [ a, b, c ]",
			"              __gsym__match_result_0 = __gsym__match_fn_1( __gsym__match_data_0[2], __gsym__match_data_0[3], __gsym__match_data_0[4:] )",
			"if not __gsym__match_bMatched_0[0]:",
			"  raise NoMatchError",
			"__gsym__match_result_0",
		]

		pysrc3 = [
			"__gsym__match_data_0 = [ 'a', 'b', 'c', 'deadbeef', 'e', 'f', 'g', 'h' ]",
			"__gsym__match_bMatched_0 = [ False ]",
			"if __isGLispList__( __gsym__match_data_0 ):",
			"  if len( __gsym__match_data_0 ) >= 4:",
			"    if __gsym__match_data_0[0] == 'a':",
			"      if __gsym__match_data_0[1] == 'b':",
			"        if not __isGLispList__( __gsym__match_data_0[2] ):",
			"          if not __isGLispList__( __gsym__match_data_0[3] ):",
			"            def __gsym__match_fn_0(a, b, c):",
			"              def __gsym__match_condition_fn_0(x):",
			"                return x.startswith( 'dead' )",
			"              if not __gsym__match_condition_fn_0( __gsym__match_data_0[3] ):",
			"                return None",
			"              __gsym__match_bMatched_0[0] = True",
			"              return [ a, b, c ]",
			"            __gsym__match_result_0 = __gsym__match_fn_0( __gsym__match_data_0[2], __gsym__match_data_0[3], __gsym__match_data_0[4:] )",
			"if not __gsym__match_bMatched_0[0]:",
			"  raise NoMatchError",
			"__gsym__match_result_0",
		]

		self._compileTest( xssrc1, pysrc1 )
		self._compileTest( xssrc2, pysrc2 )
		self._compileTest( xssrc3, pysrc3 )
		self._evalTest( xssrc1, [ 'c', 'd', [ 'e', 'f', 'g', 'h' ] ], [] )
		self._evalTest( xssrc3, [ 'c', 'deadbeef', [ 'e', 'f', 'g', 'h' ] ], [] )


	def test_Where_If_Lambda(self):
		xssrc1 = """
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
		     (@d <- @a @b @c)
		  )
		)
		"""
		pysrc1 = [
			'def __gsym__where_fn_0():',
			'  a = 1',
			'  b = 23',
			'  __gsym__if_result_0 = None',
			'  if q == 1:',
			'    __gsym__if_result_0 = 5',
			'  elif q == 2:',
			'    __gsym__if_result_0 = 6',
			'  elif q == 3:',
			'    __gsym__if_result_0 = 7',
			'  c = __gsym__if_result_0',
			'  def __gsym__lambda_0(i, j, k):',
			'    return i + (j + k)',
			'  d = __gsym__lambda_0',
			'  return d( a, b, c )',
			'x + __gsym__where_fn_0()',
		]
		self._compileTest( xssrc1, pysrc1 )



	def test_Where_Lambda(self):
		xssrc1 = """
		(
		  ($where
	             (
		       (@a #1)
		       (@b #23)
		     )
		     ($lambda () (@a + @b))
		  )
		  <-
		)
		"""
		pysrc1 = [
			'def __gsym__where_fn_0():',
			'  a = 1',
			'  b = 23',
			'  def __gsym__lambda_0():',
			'    return a + b',
			'  return __gsym__lambda_0',
			'__gsym__where_fn_0()()'
		]
		self._compileTest( xssrc1, pysrc1 )
		self._evalTest( xssrc1, 24, [] )



	def test_Lambda_Where(self):
		xssrc1 = """
		(
		  ($lambda (@a @b @c)
		    ($where
		      (
		        (@x (@a + @b))
			(@y (@b + @c))
		      )
		      (@a + (@x + @y))
		    )
		  )
		  <- #1 #2 #3
		)
		"""
		
		pysrc1 = [
			'def __gsym__lambda_0(a, b, c):',
			'  def __gsym__where_fn_0():',
			'    x = a + b',
			'    y = b + c',
			'    return a + (x + y)',
			'  return __gsym__where_fn_0()',
			'__gsym__lambda_0( 1, 2, 3 )',
		]
		
		
		self._compileTest( xssrc1, pysrc1 )
		self._evalTest( xssrc1, 9, [] )

		
		
		
		
	def test_Where_Match(self):
		xssrc1 = """
		($where
		  (
		    (@a ($list "#'a" "#'b" "#'c" "#'d" "#'e" "#'f" "#'g" "#'h"))
		    (@b #2)
		  )
		  ($match @a
		     (   (a b (: @a !) (: @b !) (: @c *))   ($list @a @b @c)   )
		  )
		)
		"""
		
		
		pysrc1 = [
			"def __gsym__where_fn_0():",
			"  a = [ 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' ]",
			"  b = 2",
			"  __gsym__match_data_0 = a",
			"  __gsym__match_bMatched_0 = [ False ]",
			"  if __isGLispList__( __gsym__match_data_0 ):",
			"    if len( __gsym__match_data_0 ) >= 4:",
			"      if __gsym__match_data_0[0] == 'a':",
			"        if __gsym__match_data_0[1] == 'b':",
			"          if not __isGLispList__( __gsym__match_data_0[2] ):",
			"            if not __isGLispList__( __gsym__match_data_0[3] ):",
			"              def __gsym__match_fn_0(a, b, c):",
			"                __gsym__match_bMatched_0[0] = True",
			"                return [ a, b, c ]",
			"              __gsym__match_result_0 = __gsym__match_fn_0( __gsym__match_data_0[2], __gsym__match_data_0[3], __gsym__match_data_0[4:] )",
			"  if not __gsym__match_bMatched_0[0]:",
			"    raise NoMatchError",
			"  return __gsym__match_result_0",
			"__gsym__where_fn_0()",
		]


		self._compileTest( xssrc1, pysrc1 )
		self._evalTest( xssrc1, [ 'c', 'd', [ 'e', 'f', 'g', 'h' ] ], [] )

		
		
	def test_Match_Where(self):
		xssrc1 = """
		($match ($list "#'a" "#'b" "#'c" "#'d" "#'e" "#'f" "#'g" "#'h")
		   (   (a b (: @a !) (: @b !) (: @c *))
		       ($where
		         (
			   (@x ($list @a @b))
			 )
			 ($list @x @c)
		       )
		   )
		)
		"""
		
		
		pysrc1 = [
			"__gsym__match_data_0 = [ 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' ]",
			"__gsym__match_bMatched_0 = [ False ]",
			"if __isGLispList__( __gsym__match_data_0 ):",
			"  if len( __gsym__match_data_0 ) >= 4:",
			"    if __gsym__match_data_0[0] == 'a':",
			"      if __gsym__match_data_0[1] == 'b':",
			"        if not __isGLispList__( __gsym__match_data_0[2] ):",
			"          if not __isGLispList__( __gsym__match_data_0[3] ):",
			"            def __gsym__match_fn_0(a, b, c):",
			"              __gsym__match_bMatched_0[0] = True",
			"              def __gsym__where_fn_0():",
			"                x = [ a, b ]",
			"                return [ x, c ]",
			"              return __gsym__where_fn_0()",
			"            __gsym__match_result_0 = __gsym__match_fn_0( __gsym__match_data_0[2], __gsym__match_data_0[3], __gsym__match_data_0[4:] )",
			"if not __gsym__match_bMatched_0[0]:",
			"  raise NoMatchError",
			"__gsym__match_result_0",
		]


		self._compileTest( xssrc1, pysrc1 )
		self._evalTest( xssrc1, [ [ 'c', 'd' ], [ 'e', 'f', 'g', 'h' ] ], [] )

		
if __name__ == '__main__':
	unittest.main()