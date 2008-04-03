##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.GLispUtil import isGLispList, isGLispString, stripGLispComments, gLispSrcToString
from Britefury.GLisp.PyCodeGen import pyt_coerce, PyCodeGenError, PyKWParam, PyVar, PyLiteral, PyLiteralValue, PyListLiteral, PyDictLiteral, PyListComprehension, PyGetAttr, PyGetItem, PyGetSlice, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyTry, PyRaise, PyReturn, PyIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects
import Britefury.GLisp.PatternMatch



_B_DEBUG_LOG_GENERATED_SOURCE = True

_TEMP_PREFIX = '__gsym__'


def raiseCompilerError(exceptionClass, src, reason):
	raise exceptionClass, reason  +  '   ::   '  +  gLispSrcToString( src, 3 )

def raiseRuntimeError(exceptionClass, src, reason):
	raise exceptionClass, reason  +  '   ::   '  +  gLispSrcToString( src, 3 )



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
	def __init__(self, temps=None, scope=None):
		if temps is None:
			temps = _TempNameAllocator()
		if scope is None:
			scope = _PyScope()
		self.temps = temps
		self.scope = scope
		self.body = []
		
		
	def innerContext(self):
		return _CompilationContext( self.temps, self.scope )
		
	def functionInnerContext(self):
		return _CompilationContext( self.temps, _PyScope() )
		
		
		



class GLispModule (object):
	def __init__(self, bindings):
		super( GLispModule, self ).__init__()
		self.__dict__.update( bindings )






def compileExpressionListToPyTreeStatements(expressions, context, bNeedResult, compileSpecial, buildResultHandlerTreeNode=lambda t, xs: t):
	"""Compiles an expression list to a list of statements
	compileExpressionListToPyTreeStatements( expressions, context, bNeedResult, compileSpecial, buildResultHandlerTreeNode)  ->  trees, wrappedResultTree
	
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

class GLispCompilerInvalidFormType (PyCodeGenError):
	pass

class GLispCompilerInvalidFormLength (PyCodeGenError):
	pass

class GLispCompilerInvalidItem (PyCodeGenError):
	pass



def _compileQuoted(xs, context, compileSpecial=None):
	if isGLispString( xs ):
		return PyLiteralValue( xs )
	elif isGLispList( xs ):
		if len( xs ) >= 1:
			if xs[0] == '$<':
				if len( xs ) == 2:
					return _compileGLispExprToPyTree( xs[1], context, True, compileSpecial )
				else:
					raise GLispCompilerInvalidFormLength( xs )
		return PyListLiteral( [ _compileQuoted( x, context, compileSpecial )   for x in xs ] )


def _compileQuote(xs, context, bNeedResult=False, compileSpecial=None):
	"""
	( $! <form> )
	
	The gLisp structure of form is maintained
	
	If within <form>, gLisp finds:
	( $< <form> )
	then <form> will be compiled as normal
	"""
	if len( xs ) != 2:
		raise GLispCompilerInvalidFormLength( xs )
	
	if bNeedResult:
		return _compileQuoted( xs[1], context, compileSpecial )
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
	fnBodyPyTrees, wrappedResultTree = compileExpressionListToPyTreeStatements( codeXs, fnBodyContext, True, compileSpecial, lambda t, x: PyReturn( t, dbgSrc=x ) )
	
	context.body.append( PyDef( functionName, argNames, fnBodyPyTrees, dbgSrc=xs ) )
	
	if bNeedResult:
		return PyVar( functionName, dbgSrc=xs )
	else:
		return None

	
	
	
	
class GLispCompilerMapExprParamListInvalid (PyCodeGenError):
	pass


def _compileMap(xs, context, bNeedResult=False, compileSpecial=None):
	"""
	( $map function list )
	"""
	if len( xs ) != 3:
		raise GLispCompilerMapFExprParamListInvalid( xs )

	functionXs = xs[1]
	listXs = xs[2]
	
	itemVarName = context.temps.allocateTempName( 'map_item' )
	py_itemExpr = _compileGLispExprToPyTree( functionXs, context, True, compileSpecial )( PyVar( itemVarName, dbgSrc=xs ) )
	py_listExpr = _compileGLispExprToPyTree( listXs, context, True, compileSpecial )
	
	if bNeedResult:
		return PyListComprehension( py_itemExpr, itemVarName, py_listExpr, dbgSrc=xs )
	else:
		return None





class GLispCompilerMapXExprParamListInvalid (PyCodeGenError):
	pass


def _compileMapX(xs, context, bNeedResult=False, compileSpecial=None):
	"""
	( $mapx item_expression varname list )
	"""
	if len( xs ) != 4:
		raise GLispCompilerMapXExprParamListInvalid( xs )

	itemExprXs = xs[1]
	varNameXs = xs[2]
	listXs = xs[3]
	
	if varNameXs[0] != '@':
		raise GLispCompilerVariableNameMustStartWithAt( varNameXs )
	varName = varNameXs[1:]
	
	itemExprContext = context.innerContext()
	py_itemExpr = _compileGLispExprToPyTree( itemExprXs, itemExprContext, True, compileSpecial )
	py_listExpr = _compileGLispExprToPyTree( listXs, context, True, compileSpecial )
	
	if bNeedResult:
		if len( itemExprContext.body )  >  0:
			funName = context.temps.allocateTempName( 'mapx_fn' )
			py_fun = PyDef( funName, varName, itemExprContext.body  +  [ py_itemExpr.return_().debug( xs ) ], dbgSrc=xs )
			context.body.append( py_fun )
			return PyListComprehension( PyVar( funName )( PyVar( varName ) ).debug( xs ), varName, py_listExpr, dbgSrc=xs )
		else:
			return PyListComprehension( py_itemExpr, varName, py_listExpr, dbgSrc=xs )
	else:
		return None





class GLispCompilerFilterExprParamListInvalid (PyCodeGenError):
	pass


def _compileFilter(xs, context, bNeedResult=False, compileSpecial=None):
	"""
	( $filter function list )
	"""
	if len( xs ) != 3:
		raise GLispCompilerFilterFExprParamListInvalid( xs )

	functionXs = xs[1]
	listXs = xs[2]
	
	itemVarName = context.temps.allocateTempName( 'filter_item' )
	py_itemExpr = _compileGLispExprToPyTree( functionXs, context, True, compileSpecial )( PyVar( itemVarName, dbgSrc=xs ) )
	py_listExpr = _compileGLispExprToPyTree( listXs, context, True, compileSpecial )
	
	if bNeedResult:
		return PyListComprehension( PyVar( itemVarName, dbgSrc=xs), itemVarName, py_listExpr, py_itemExpr, dbgSrc=xs )
	else:
		return None





class GLispCompilerFilterXExprParamListInvalid (PyCodeGenError):
	pass


def _compileFilterX(xs, context, bNeedResult=False, compileSpecial=None):
	"""
	( $filterx item_expression varname list )
	"""
	if len( xs ) != 4:
		raise GLispCompilerFilterXExprParamListInvalid( xs )

	filterExprXs = xs[1]
	varNameXs = xs[2]
	listXs = xs[3]
	
	if varNameXs[0] != '@':
		raise GLispCompilerVariableNameMustStartWithAt( varNameXs )
	varName = varNameXs[1:]
	
	filterExprContext = context.innerContext()
	py_filterExpr = _compileGLispExprToPyTree( filterExprXs, filterExprContext, True, compileSpecial )
	py_listExpr = _compileGLispExprToPyTree( listXs, context, True, compileSpecial )
	
	if bNeedResult:
		if len( filterExprContext.body )  >  0:
			funName = context.temps.allocateTempName( 'filterx_fn' )
			py_fun = PyDef( funName, varName, filterExprContext.body  +  [ py_filterExpr.return_().debug( xs ) ], dbgSrc=xs )
			context.body.append( py_fun )
			return PyListComprehension( PyVar( varName, dbgSrc=xs), varName, py_listExpr, PyVar( funName )( PyVar( varName ) ).debug( xs ), dbgSrc=xs )
		else:
			return PyListComprehension( PyVar( varName, dbgSrc=xs), varName, py_listExpr, py_filterExpr, dbgSrc=xs )
	else:
		return None





class GLispCompilerReduceExprParamListInvalid (PyCodeGenError):
	pass


def _compileReduce(xs, context, bNeedResult=False, compileSpecial=None):
	"""
	( $reduce function list initialiser? )
	"""
	if len( xs ) < 3  or  len( xs ) > 4:
		raise GLispCompilerMapFExprParamListInvalid( xs )

	functionXs = xs[1]
	listXs = xs[2]
	initialiserXs = None
	if len( xs ) == 4:
		initialiserXs = xs[3]
	
	py_func = _compileGLispExprToPyTree( functionXs, context, True, compileSpecial )
	py_list = _compileGLispExprToPyTree( listXs, context, True, compileSpecial )
	py_initialiser = None
	if initialiserXs is not None:
		py_initialiser = _compileGLispExprToPyTree( initialiserXs, context, True, compileSpecial )
	
	if bNeedResult:
		if initialiserXs is not None:
			return PyVar( 'reduce', dbgSrc=xs )( py_func, py_list, py_initialiser )
		else:
			return PyVar( 'reduce', dbgSrc=xs )( py_func, py_list )
	else:
		return None





class GLispCompilerRaiseExprParamListInvalid (PyCodeGenError):
	pass


def _compileRaise(xs, context, bNeedResult=False, compileSpecial=None):
	"""
	( $raise exception )
	"""
	if len( xs ) != 2:
		raise GLispCompilerRaiseExprParamListInvalid( xs )

	exceptionXs = xs[1]
	
	py_exception = _compileGLispExprToPyTree( exceptionXs, context, True, compileSpecial )
	context.body.append( PyRaise( py_exception ).debug( xs ) )
	
	if bNeedResult:
		return pyt_coerce( None ).debug( xs )
	else:
		return None





class GLispCompilerTryExprParamListInsufficient (PyCodeGenError):
	pass

class GLispCompilerTryExprElseMustFollowExcept (PyCodeGenError):
	pass

class GLispCompilerTryExprFinallyMustFollowExcept (PyCodeGenError):
	pass

class GLispCompilerTryExprFinallyMustFollowElse (PyCodeGenError):
	pass

class GLispCompilerTryExprNeedsExceptClause (PyCodeGenError):
	pass



def _compileTry(xs, context, bNeedResult=False, compileSpecial=None):
	"""
	( $try
	   (
	      <try_statements...>
	   )
	   ( $except <exception1> <except_statements...> )   [0 or more]
	   ( $else <else_statements...> )  [optional]
	   ( $finally <finally_statements...> )  [optional]
	)
	"""
	if len( xs ) < 2:
		raise GLispCompilerTryExprParamListInsufficient( xs )
	
	tryStmtsXs = xs[1]
	exceptBlocksXs = []
	elseXs = None
	finallyXs = None
	
	
	
	if bNeedResult:
		resultVarName = context.temps.allocateTempName( 'try_result' )
	else:
		resultVarName = None
		

	context.body.append( PyVar( resultVarName ).assign_sideEffects( None ).debug( xs ) )
	
		
	# Gather the source expressions
	for x in xs[2:]:
		if not isGLispList( x ):
			raiseCompilerError( GLispCompilerInvalidFormType, x, 'try: sub-forms must be lists' )
		if len( x ) < 1:
			raiseCompilerError( GLispCompilerInvalidFormLength, x, 'try: sub-forms must be of length 1 or more' )
		
		if x[0] == '$except':
			if elseXs is not None:
				raiseCompilerError( GLispCompilerTryExprElseMustFollowExcept, x, 'try: else must follow except' )
			if finallyXs is not None:
				raiseCompilerError( GLispCompilerTryExprFinallyMustFollowExcept, x, 'try: finally must follow except' )
			if len( x ) < 2:
				raiseCompilerError( GLispCompilerInvalidFormLength, x, 'try: except form requires 1 parameter; the exception' )
			exceptBlocksXs.append( x )
		elif x[0] == '$else':
			if len( exceptBlocksXs ) < 1:
				raiseCompilerError( GLispCompilerTryExprNeedsExceptClause, x, 'try: must have at least 1 except clause' )
			if finallyXs is not None:
				raiseCompilerError( GLispCompilerTryExprFinallyMustFollowElse, x, 'try: finally must follow else' )
			elseXs = x
		elif x[0] == '$finally':
			if len( exceptBlocksXs ) < 1:
				raiseCompilerError( GLispCompilerTryExprNeedsExceptClause, x, 'try: must have at least 1 except clause' )
			finallyXs = x
		
	

			
	buildResultHandleTreeNode = lambda t, x: PyAssign_SideEffects( PyVar( resultVarName, dbgSrc=x ), t, dbgSrc=x )

	
	tryContext = context.innerContext()
	py_tryStmts, _resultTree = compileExpressionListToPyTreeStatements( tryStmtsXs, tryContext, bNeedResult  and  elseXs is None  and  finallyXs is None, compileSpecial, buildResultHandleTreeNode )
	exceptSpecs = []
	py_elseStmts = None
	py_finallyStmts = None
	
	
	for x in exceptBlocksXs:
		exceptionXs = x[1]
		exceptStmtsXs = x[2:]
		exceptStmtsContext = context.innerContext()
		py_exception = _compileGLispExprToPyTree( exceptionXs, context, True, compileSpecial )
		py_exceptStmts, _resultTree = compileExpressionListToPyTreeStatements( exceptStmtsXs, exceptStmtsContext, bNeedResult  and  finallyXs is None, compileSpecial, buildResultHandleTreeNode )
		exceptSpecs.append( ( py_exception, py_exceptStmts ) )
		
	if elseXs is not None:
		elseStmtsContext = context.innerContext()
		py_elseStmts, _resultTree = compileExpressionListToPyTreeStatements( elseXs[1:], elseStmtsContext, bNeedResult  and  finallyXs is None, compileSpecial, buildResultHandleTreeNode )
		
	if finallyXs is not None:
		finallyStmtsContext = context.innerContext()
		py_finallyStmts, _resultTree = compileExpressionListToPyTreeStatements( finallyXs[1:], finallyStmtsContext, bNeedResult, compileSpecial, buildResultHandleTreeNode )
		
	
	context.body.append( PyTry( py_tryStmts, exceptSpecs, py_elseStmts, py_finallyStmts ).debug( xs ) )
	
	if bNeedResult:
		return PyVar( resultVarName, dbgSrc=xs )
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
		resultVarName = context.temps.allocateTempName( 'if_result' )
	else:
		resultVarName = None
		
	
	def _conditionAndCodeXsToPyTree(conditionAndCodeXs):
		conditionXs = conditionAndCodeXs[0]
		codeXs = conditionAndCodeXs[1:]
		conditionPyTree = _compileGLispExprToPyTree( conditionXs, context, True, compileSpecial )
		
		codePyTrees, wrappedResultTree = compileExpressionListToPyTreeStatements( codeXs, context, bNeedResult, compileSpecial, lambda t, x: PyAssign_SideEffects( PyVar( resultVarName, dbgSrc=x ), t, dbgSrc=x ) )
		
		if wrappedResultTree is None  and  bNeedResult:
			codePyTrees.append( PyAssign_SideEffects( PyVar( resultVarName, dbgSrc=conditionAndCodeXs ), PyLiteral( 'None', dbgSrc=conditionAndCodeXs ), dbgSrc=conditionAndCodeXs ) )
			
		return conditionPyTree, codePyTrees
		
	
	def _elseCodeXsToPyTree(elseCodeXs):
		codeXs = elseCodeXs[1:]
		codePyTrees, wrappedResultTree = compileExpressionListToPyTreeStatements( codeXs, context, bNeedResult, compileSpecial, lambda t, x: PyAssign_SideEffects( PyVar( resultVarName, dbgSrc=x ), t, dbgSrc=x ) )
		
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
	trees, resultStorePyTree = compileExpressionListToPyTreeStatements( expressions, context, True, compileSpecial, lambda tree, x: PyReturn( tree, dbgSrc=x ) )
	whereTrees.extend( trees )
	
	
	# Turn it into a function (def)
	fnTree = PyDef( whereFnName, [], whereTrees, dbgSrc=xs )

	context.body.append( fnTree )
	
	callTree = PyVar( whereFnName )().debug( xs )
			
	return callTree





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
	
	
							     



class GLispCompilerModuleExprInvalidBindingListType (PyCodeGenError):
	pass

class GLispCompilerModuleExprInvalidBindingListFormat (PyCodeGenError):
	pass

class GLispCompilerModuleExprCannotRebindVariable (PyCodeGenError):
	pass

def _compileModule(xs, context, bNeedResult=False, compileSpecial=None):
	"""
	($module (name0 value0) (name1 value1) ... (nameN valueN))
	"""
	bindings = xs[1:]
	
	if not isGLispList( bindings ):
		raise GLispCompilerModuleExprInvalidBindingListType( xs )
	
	moduleTrees = []
	

	moduleFnName = context.temps.allocateTempName( 'module_fn' )

	
	# Make binding code
	boundNames = []
	bindings = stripGLispComments( bindings )
	for binding in bindings:
		bindingContext = context.innerContext()
		if not isGLispList( binding )  or  len( binding ) != 2:
			raise GLispCompilerModuleExprInvalidBindingListFormat( binding )
		
		if binding[0][0] != '@':
			raise GLispCompilerVariableNameMustStartWithAt( binding[0] )
		
		name = binding[0][1:]
		valueExpr = binding[1]
		if name in boundNames:
			raise GLispCompilerModuleExprCannotRebindVariable( binding )
		
		# If this name is already bound in thise scope, we need to back it up first
		valueExprPyTree = _compileGLispExprToPyTree( valueExpr, bindingContext, True, compileSpecial )
		assignmentPyTree = valueExprPyTree.assignToVar_sideEffects( name ).debug( binding )
		
		moduleTrees.extend( bindingContext.body )
		moduleTrees.append( assignmentPyTree )
		
		boundNames.append( name )
		

	# Build and return the module object
	py_module = PyReturn( PyVar( 'GLispModule' )( PyDictLiteral( [ ( PyLiteralValue( name ), PyVar( name ) )   for name in boundNames ] ) ) ).debug( xs )
	moduleTrees.append( py_module )
	
	
	# Turn it into a function (def)
	fnTree = PyDef( moduleFnName, [], moduleTrees, dbgSrc=xs )

	context.body.append( fnTree )
	
	callTree = PyVar( moduleFnName )().debug( xs )
			
	return callTree




def compileGLispCallParamToPyTree(xs, context, compileSpecial):
	"""
	Compile a call parameter to a PyNode tree; handles keyword parameters
	
	(:name <value>)    results in a keyword parameter
	anything else is intrepreted as a value
	"""
	if isGLispList( xs ):
		if len( xs ) == 2:
			if isGLispString( xs[0] ):
				if xs[0][0] == ':':
					return PyKWParam( xs[0][1:], _compileGLispExprToPyTree( xs[1], context, True, compileSpecial ) )
	return _compileGLispExprToPyTree( xs, context, True, compileSpecial )


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
		elif xs[0] == '$!':
			return _compileQuote( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$lambda':
			return _compileLambda( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$map':
			return _compileMap( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$mapx':
			return _compileMapX( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$filter':
			return _compileFilter( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$filterx':
			return _compileFilterX( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$reduce':
			return _compileReduce( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$raise':
			return _compileRaise( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$try':
			return _compileTry( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$if':
			return _compileIf( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$where':
			return _compileWhere( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$match':
			return _compileMatch( xs, context, bNeedResult, compileSpecial )
		elif xs[0] == '$module':
			return _compileModule( xs, context, bNeedResult, compileSpecial )
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
				return PyCall( _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ), [ compileGLispCallParamToPyTree( e, context, compileSpecial )   for e in xs[2:] ], dbgSrc=xs )
			else:
				return PyMethodCall( _compileGLispExprToPyTree( xs[0], context, True, compileSpecial ), xs[1], [ compileGLispCallParamToPyTree( e, context, compileSpecial )   for e in xs[2:] ], dbgSrc=xs )
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
	
	fnBodyPyTrees, wrappedResultTree = compileExpressionListToPyTreeStatements( [ xs ], context, True, compileSpecial, lambda t, x: PyReturn( resultPyTreePostProcess( t, x ), dbgSrc=x ) )
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
	lcls['GLispModule'] = GLispModule
	lcls['gLispAsString'] = gLispSrcToString
	lcls['log'] = _log
	if _B_DEBUG_LOG_GENERATED_SOURCE:
		f =open( moduleName + '.py', 'w' )
		f.write( '#DEBUG: Source generated by GLispCompiler.compileGLispExprToPyFunction()\n' )
		f.write( src )
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
		if isinstance( expectedValue, str ):
			result = compileGLispExprToPySrc( readSX( srcText ), compileSpecial )
			if result != expectedValue:
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
			self.assert_( result ==  expectedValue )
		else:
			self.assertRaises( expectedValue, lambda: compileGLispExprToPySrc( readSX( srcText ), compileSpecial ) )

	def _evalTest(self, srcText, expectedResult, argValuePairs=[], compileSpecial=None, lcls={}):
		argNames = [ p[0]   for p in argValuePairs ]
		argValues = [ p[1]   for p in argValuePairs ]
		fn = compileGLispExprToPyFunction( 'testModule', 'test', argNames, readSX( srcText ), compileSpecial, lcls=lcls )
		result = fn( *argValues )
		self.assert_( result == expectedResult )

	def _evalExceptionTest(self, srcText, expectedException, argValuePairs=[], compileSpecial=None, lcls={}):
		argNames = [ p[0]   for p in argValuePairs ]
		argValues = [ p[1]   for p in argValuePairs ]
		fn = compileGLispExprToPyFunction( 'testModule', 'test', argNames, readSX( srcText ), compileSpecial, lcls=lcls )
		self.assertRaises( expectedException, lambda: fn( *argValues ) )

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
				return PyVar( 'special' )
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
		self._compileTest( '(@a <- @b (:x @c))', 'a( b, x=c )' )
		self._evalTest( '(@f <- #5)', 8, [ ( 'f', _f ) ] )

	def testMethodCall(self):
		self._compileTest( '(@a foo)', 'a.foo()' )
		self._compileTest( '(@a foo @b @c)', 'a.foo( b, c )' )
		self._compileTest( '(@a foo @b (:x @c))', 'a.foo( b, x=c )' )
		self._evalTest( "(\"#'f\" upper)", 'F' )
		
		
	def test_Quote(self):
		self._compileTest( "($! (a b c))",  "[ 'a', 'b', 'c' ]" )
		self._compileTest( "($! (a b c (d e f)))",  "[ 'a', 'b', 'c', [ 'd', 'e', 'f' ] ]" )
		self._compileTest( "($! (a b c (d e f ($< @x))))",  "[ 'a', 'b', 'c', [ 'd', 'e', 'f', x ] ]" )


	def test_Lambda(self):
		pysrc1 = [
			'def __gsym__lambda_0(x, y, z):',
			'  x.split()',
			'  return x + (y + z)',
			'__gsym__lambda_0',
		]

		
		self._compileTest( '($lambda   (@x @y @z)  (@x split) (@x + (@y + @z))  )', pysrc1 )
		self._evalTest( '( ($lambda   (@x @y @z)  (@x + (@y + @z))  ) <- @a @b @c)', 6, [ ( 'a', 1 ), ( 'b', 2 ), ( 'c', 3 ) ] )
		
		
	def test_map(self):
		xssrc1 = """
		($map ($lambda (@x) (@x + #2)) ($list #1 #2 #3 #4 #5))
		"""
		pysrc1 = [
			'def __gsym__lambda_0(x):',
			'  return x + 2',
			'[ __gsym__lambda_0( __gsym__map_item_0 )   for __gsym__map_item_0 in [ 1, 2, 3, 4, 5 ] ]',
		]

		self._compileTest( xssrc1, pysrc1 )
		self._evalTest( xssrc1, [ 3, 4, 5, 6, 7 ] )
		
		
	def test_mapX(self):
		xssrc1 = """
		($mapx (@x + #2) @x ($list #1 #2 #3 #4 #5))
		"""
		pysrc1 = [
			'[ x + 2   for x in [ 1, 2, 3, 4, 5 ] ]',
		]

		self._compileTest( xssrc1, pysrc1 )
		self._evalTest( xssrc1, [ 3, 4, 5, 6, 7 ] )
		
		
	def test_filter(self):
		xssrc1 = """
		($filter ($lambda (@x) (@x > #2)) ($list #1 #2 #3 #4 #5))
		"""
		pysrc1 = [
			'def __gsym__lambda_0(x):',
			'  return x > 2',
			'[ __gsym__filter_item_0   for __gsym__filter_item_0 in [ 1, 2, 3, 4, 5 ]   if __gsym__lambda_0( __gsym__filter_item_0 ) ]',
		]

		self._compileTest( xssrc1, pysrc1 )
		self._evalTest( xssrc1, [ 3, 4, 5 ] )
		
		
	def test_filterX(self):
		xssrc1 = """
		($filterx (@x > #2) @x ($list #1 #2 #3 #4 #5))
		"""
		pysrc1 = [
			'[ x   for x in [ 1, 2, 3, 4, 5 ]   if x > 2 ]',
		]

		self._compileTest( xssrc1, pysrc1 )
		self._evalTest( xssrc1, [ 3, 4, 5 ] )
		
		
	def test_reduce(self):
		xssrc1 = """
		($reduce ($lambda (@x @y) (@x + @y)) ($list #1 #2 #3 #4 #5))
		"""
		xssrc2 = """
		($reduce ($lambda (@x @y) (@x + @y)) ($list #1 #2 #3 #4 #5) #12)
		"""
		pysrc1 = [
			'def __gsym__lambda_0(x, y):',
			'  return x + y',
			'reduce( __gsym__lambda_0, [ 1, 2, 3, 4, 5 ] )',
		]
		pysrc2 = [
			'def __gsym__lambda_0(x, y):',
			'  return x + y',
			'reduce( __gsym__lambda_0, [ 1, 2, 3, 4, 5 ], 12 )',
		]

		self._compileTest( xssrc1, pysrc1 )
		self._compileTest( xssrc2, pysrc2 )
		self._evalTest( xssrc1, 15 )
		self._evalTest( xssrc2, 27 )
		
		
	def test_Raise(self):
		xssrc1 = """
		($raise @ValueError)
		"""
		pysrc1 = [
			"raise ValueError",
			"None"
		]

		self._compileTest( xssrc1, pysrc1 )
		self._evalExceptionTest( xssrc1, ValueError )
		
		
	def test_Try(self):
		xssrc1 = """
		($try
		  ()
		  ($except @ValueError)
		)
		"""
		pysrc1 = [
			"__gsym__try_result_0 = None",
			"try:",
			"  pass",
			"except ValueError:",
			"  pass",
			"__gsym__try_result_0",
		]

		xssrc2 = """
		($try
		  (
	            ($if
		      (#True #123)
		      ($else #345)
		     )
		  )
		  ($except @ValueError)
		)
		"""
		pysrc2 = [
			"__gsym__try_result_0 = None",
			"try:",
			"  if True:",
			"    __gsym__if_result_0 = 123",
			"  else:",
			"    __gsym__if_result_0 = 345",
			"  __gsym__try_result_0 = __gsym__if_result_0",
			"except ValueError:",
			"  pass",
			"__gsym__try_result_0",
		]

		xssrc3 = """
		($try
		  (
		    ($raise @TypeError)
		  )
		  ($except @ValueError)
		)
		"""
		pysrc3 = [
			"__gsym__try_result_0 = None",
			"try:",
			"  raise TypeError",
			"  __gsym__try_result_0 = None",
			"except ValueError:",
			"  pass",
			"__gsym__try_result_0",
		]

		xssrc4 = """
		($try
		  (
		    ($raise @ValueError)
		  )
		  ($except @ValueError #456)
		)
		"""
		pysrc4 = [
			"__gsym__try_result_0 = None",
			"try:",
			"  raise ValueError",
			"  __gsym__try_result_0 = None",
			"except ValueError:",
			"  __gsym__try_result_0 = 456",
			"__gsym__try_result_0",
		]

		xssrc5 = """
		($try
		  (
		    ($raise @TypeError)
		  )
		  ($except @ValueError #456)
		  ($except @TypeError #123)
		)
		"""
		pysrc5 = [
			"__gsym__try_result_0 = None",
			"try:",
			"  raise TypeError",
			"  __gsym__try_result_0 = None",
			"except ValueError:",
			"  __gsym__try_result_0 = 456",
			"except TypeError:",
			"  __gsym__try_result_0 = 123",
			"__gsym__try_result_0",
		]

		xssrc6 = """
		($try
		  (
		    ($raise @ValueError)
		  )
		  ($except @ValueError #456)
		  ($else #789)
		)
		"""
		pysrc6 = [
			"__gsym__try_result_0 = None",
			"try:",
			"  raise ValueError",
			"except ValueError:",
			"  __gsym__try_result_0 = 456",
			"else:",
			"  __gsym__try_result_0 = 789",
			"__gsym__try_result_0",
		]

		xssrc7 = """
		($try
		  (
		    #123
		  )
		  ($except @ValueError #456)
		  ($else #789)
		)
		"""
		pysrc7 = [
			"__gsym__try_result_0 = None",
			"try:",
			"  123",
			"except ValueError:",
			"  __gsym__try_result_0 = 456",
			"else:",
			"  __gsym__try_result_0 = 789",
			"__gsym__try_result_0",
		]

		xssrc8 = """
		($try
		  (
		    #123
		  )
		  ($except @ValueError #456)
		  ($else #789)
		  ($finally #1000)
		)
		"""
		pysrc8 = [
			"__gsym__try_result_0 = None",
			"try:",
			"  123",
			"except ValueError:",
			"  456",
			"else:",
			"  789",
			"finally:",
			"  __gsym__try_result_0 = 1000",
			"__gsym__try_result_0",
		]

		xssrc9 = """
		($try
		  (
		    ($raise @ValueError)
		  )
		  ($except @ValueError #456)
		  ($else #789)
		  ($finally #1000)
		)
		"""
		pysrc9 = [
			"__gsym__try_result_0 = None",
			"try:",
			"  raise ValueError",
			"except ValueError:",
			"  456",
			"else:",
			"  789",
			"finally:",
			"  __gsym__try_result_0 = 1000",
			"__gsym__try_result_0",
		]

		xssrc10 = """
		($try
		  (
		    ($raise @TypeError)
		  )
		  ($except @ValueError #456)
		  ($else #789)
		  ($finally #1000)
		)
		"""
		pysrc10 = [
			"__gsym__try_result_0 = None",
			"try:",
			"  raise TypeError",
			"except ValueError:",
			"  456",
			"else:",
			"  789",
			"finally:",
			"  __gsym__try_result_0 = 1000",
			"__gsym__try_result_0",
		]

		self._compileTest( xssrc1, pysrc1 )
		self._evalTest( xssrc1, None )
		self._compileTest( xssrc2, pysrc2 )
		self._evalTest( xssrc2, 123 )
		self._compileTest( xssrc3, pysrc3 )
		self._evalExceptionTest( xssrc3, TypeError )
		self._compileTest( xssrc4, pysrc4 )
		self._evalTest( xssrc4, 456 )
		self._compileTest( xssrc5, pysrc5 )
		self._evalTest( xssrc5, 123 )
		self._compileTest( xssrc6, pysrc6 )
		self._evalTest( xssrc6, 456 )
		self._compileTest( xssrc7, pysrc7 )
		self._evalTest( xssrc7, 789 )
		self._compileTest( xssrc8, pysrc8 )
		self._evalTest( xssrc8, 1000 )
		self._compileTest( xssrc9, pysrc9 )
		self._evalTest( xssrc9, 1000 )
		self._compileTest( xssrc10, pysrc10 )
		self._evalExceptionTest( xssrc10, TypeError )
		
		
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


	def test_Module(self):
		xssrc1 = """
		($module
		  (@a #1)
		  (@b "#'test")
		)
		"""
		
		pysrc1 = [
			"def __gsym__module_fn_0():",
			"  a = 1",
			"  b = 'test'",
			"  return GLispModule( { 'a' : a, 'b' : b } )",
			
			"__gsym__module_fn_0()",
		]
		
		self._compileTest( xssrc1, pysrc1 )


		
		
		
	def test_mapX_Match(self):
		xssrc1 = """
		($mapx
		  ($match @x
		    ( a  #1 )
		    ( b  #2 )
		    ( c  #3 )
		  )
		  @x
		  ($list "#'a" "#'b" "#'c" "#'b" "#'a")
		)
		"""
		pysrc1 = [
			"def __gsym__mapx_fn_0(x):",
			"  __gsym__match_data_0 = x",
			"  __gsym__match_bMatched_0 = [ False ]",
			"  if __gsym__match_data_0 == 'a':",
			"    def __gsym__match_fn_0():",
			"      __gsym__match_bMatched_0[0] = True",
			"      return 1",
			"    __gsym__match_result_0 = __gsym__match_fn_0()",
			"  if not __gsym__match_bMatched_0[0]:",
			"    if __gsym__match_data_0 == 'b':",
			"      def __gsym__match_fn_1():",
			"        __gsym__match_bMatched_0[0] = True",
			"        return 2",
			"      __gsym__match_result_0 = __gsym__match_fn_1()",
			"  if not __gsym__match_bMatched_0[0]:",
			"    if __gsym__match_data_0 == 'c':",
			"      def __gsym__match_fn_2():",
			"        __gsym__match_bMatched_0[0] = True",
			"        return 3",
			"      __gsym__match_result_0 = __gsym__match_fn_2()",
			"  if not __gsym__match_bMatched_0[0]:",
			"    raise NoMatchError",
			"  return __gsym__match_result_0",
			"[ __gsym__mapx_fn_0( x )   for x in [ 'a', 'b', 'c', 'b', 'a' ] ]",
		]

		self._compileTest( xssrc1, pysrc1 )
		self._evalTest( xssrc1, [ 1, 2, 3, 2, 1 ] )
		
		
	def test_filterX_Match(self):
		xssrc1 = """
		($filterx
		  ($match @x
		    ( a  #False )
		    ( b  #True )
		    ( c  #True )
		  )
		  @x
		  ($list "#'a" "#'b" "#'c" "#'b" "#'a")
		)
		"""
		pysrc1 = [
			"def __gsym__filterx_fn_0(x):",
			"  __gsym__match_data_0 = x",
			"  __gsym__match_bMatched_0 = [ False ]",
			"  if __gsym__match_data_0 == 'a':",
			"    def __gsym__match_fn_0():",
			"      __gsym__match_bMatched_0[0] = True",
			"      return False",
			"    __gsym__match_result_0 = __gsym__match_fn_0()",
			"  if not __gsym__match_bMatched_0[0]:",
			"    if __gsym__match_data_0 == 'b':",
			"      def __gsym__match_fn_1():",
			"        __gsym__match_bMatched_0[0] = True",
			"        return True",
			"      __gsym__match_result_0 = __gsym__match_fn_1()",
			"  if not __gsym__match_bMatched_0[0]:",
			"    if __gsym__match_data_0 == 'c':",
			"      def __gsym__match_fn_2():",
			"        __gsym__match_bMatched_0[0] = True",
			"        return True",
			"      __gsym__match_result_0 = __gsym__match_fn_2()",
			"  if not __gsym__match_bMatched_0[0]:",
			"    raise NoMatchError",
			"  return __gsym__match_result_0",
			"[ x   for x in [ 'a', 'b', 'c', 'b', 'a' ]   if __gsym__filterx_fn_0( x ) ]",
		]

		self._compileTest( xssrc1, pysrc1 )
		self._evalTest( xssrc1, [ 'b', 'c', 'b' ] )
		
		
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