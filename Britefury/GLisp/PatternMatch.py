##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

from Britefury.Kernel.Abstract import abstractmethod

from Britefury.DocModel.DMListInterface import DMListInterface
from Britefury.GLisp.GLispUtil import isGLispList, isGLispComment, stripGLispComments
from Britefury.GLisp.PyCodeGen import pyt_coerce, PyCodeGenError, PySrc, PyVar, PyLiteral, PyListLiteral, PyGetAttr, PyGetItem, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyReturn, PyIf, PySimpleIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects
import Britefury.GLisp.GLispCompiler


class NoMatchError (Exception):
	pass



def _pyt_isGLispList(py_expression):
	return PyVar( '__isGLispList__' )( py_expression )



class  _MatchNode (object):
	def __init__(self, srcXs):
		super( _MatchNode, self ).__init__()
		self.bindName = None
		self.conditionVarName = None
		self.conditionExprXs = None
		self.srcXs = srcXs
		
		
	@abstractmethod
	def emitPyTreeAndBindings(self, outerTreeFactory, py_value, bindings):
		pass
	
	def handleBinding(self, outerTreeFactory, py_value, bindings):
		if self.bindName is not None:
			if self.bindName in bindings:
				return self._p_conditionWrap( outerTreeFactory, py_value == bindings[self.bindName] )
			else:
				bindings[self.bindName] = py_value
				return outerTreeFactory
		return outerTreeFactory
	
	def gatherConditions(self, py_value):
		if self.conditionVarName is None:
			return []
		else:
			return [ ( py_value, self.conditionVarName, self.conditionExprXs ) ]
	
	def _p_emitMatchFailed(self):
		return [ 'raise NoMatchError' ]
	
	
	def _p_conditionWrap(self, outerTreeFactory, py_condition):
		return lambda innerTrees: outerTreeFactory( [ py_condition.ifTrue( innerTrees ).debug( self.srcXs ) ] )
		
		
		



class _MatchAnything (_MatchNode):
	def emitPyTreeAndBindings(self, outerTreeFactory, py_value, bindings):
		return self.handleBinding( outerTreeFactory, py_value, bindings )
	

class _MatchTerminal (_MatchNode):
	def emitPyTreeAndBindings(self, outerTreeFactory, py_value, bindings):
		conditionTreeFactory = self._p_conditionWrap( outerTreeFactory, _pyt_isGLispList( py_value ).not_() )
		return self.handleBinding( conditionTreeFactory, py_value, bindings )
	


class _MatchNonTerminal (_MatchNode):
	def emitPyTreeAndBindings(self, outerTreeFactory, py_value, bindings):
		conditionTreeFactory = self._p_conditionWrap( outerTreeFactory, _pyt_isGLispList( py_value ) )
		return self.handleBinding( conditionTreeFactory, py_value, bindings )

	
class _MatchConstant (_MatchNode):
	def __init__(self, constant, srcXs):
		super( _MatchConstant, self ).__init__( srcXs=srcXs )
		self._constant = constant

	def emitPyTreeAndBindings(self, outerTreeFactory, py_value, bindings):
		conditionTreeFactory = self._p_conditionWrap( outerTreeFactory, py_value == self._constant )
		return self.handleBinding( conditionTreeFactory, py_value, bindings )


class _MatchList (_MatchNode):
	def __init__(self, subMatches, srcXs):
		super( _MatchList, self ).__init__( srcXs=srcXs )
		internalIndex = None
		for i, m in enumerate( subMatches ):
			if isinstance( m, _MatchSublist ):
				if internalIndex is not None:
					raise ValueError, 'match expression compilation: only one list interior (*, +, -) inside a match list'
				internalIndex = i

		if internalIndex is not None:
			self._front = subMatches[:internalIndex]
			self._back = subMatches[internalIndex+1:]
			self._internal = subMatches[internalIndex]
		else:
			self._front = subMatches
			self._back = []
			self._internal = None
			
		if self._internal is not None:
			self._minLength = len( self._front ) + len( self._back ) + self._internal.min
			if self._internal.max is not None:
				self._maxLength = len( self._front ) + len( self._back ) + self._internal.max
			else:
				self._maxLength = None
		else:
			self._minLength = self._maxLength = len( self._front )
		
	def emitPyTreeAndBindings(self, outerTreeFactory, py_value, bindings):
		treeFac = outerTreeFactory
		# Check type
		treeFac = self._p_conditionWrap( treeFac, _pyt_isGLispList( py_value ) )

		# Check the lengths
		if self._minLength == self._maxLength:
			# min and max length the same; only one length permissable
			treeFac = self._p_conditionWrap( treeFac, py_value.len_() == self._minLength )
		elif self._minLength == 0  and  self._maxLength is not None:
			# no min length, a max length
			treeFac = self._p_conditionWrap( treeFac, py_value.len_() <= self._maxLength )
		elif self._minLength > 0:
			# there is a min length
			if self._maxLength is None:
				# no max length
				treeFac = self._p_conditionWrap( treeFac, py_value.len_() >= self._minLength )
			else:
				# max length
				treeFac = self._p_conditionWrap( treeFac, ( py_value.len_() >= self._minLength ).and_( py_value.len_() <= self._maxLength ) )
				
		#matchItem*  (front)
		for i, item in enumerate( self._front ):
			treeFac = item.emitPyTreeAndBindings( treeFac, py_value[i], bindings )
					
		#matchItem*  (back)
		for i, item in enumerate( reversed( self._back ) ):
			treeFac = item.emitPyTreeAndBindings( treeFac, py_value[-(i+1)], bindings )
			
		#internal sublist
		if self._internal is not None:
			start = len( self._front )
			end = len( self._back )
			if end  >  0:
				treeFac = self._internal.emitPyTreeAndBindings( treeFac, py_value[start:-end], bindings )
			else:
				treeFac = self._internal.emitPyTreeAndBindings( treeFac, py_value[start:], bindings )
		
		#binding
		treeFac = self.handleBinding( treeFac, py_value, bindings )
		
		return treeFac

	
	def gatherConditions(self, py_value):
		conditions = super( _MatchList, self ).gatherConditions( py_value )

		#front
		for i, item in enumerate( self._front ):
			conditions.extend( item.gatherConditions( py_value[i] ) )

		#back
		for i, item in enumerate( reversed( self._back ) ):
			conditions.extend( item.gatherConditions( py_value[-(i+1)] ) )

		#internal sublist
		if self._internal is not None:
			start = len( self._front )
			end = len( self._back )
			if end  > 0:
				conditions.extend( self._internal.gatherConditions( py_value[start:-end] ) )
			else:
				conditions.extend( self._internal.gatherConditions( py_value[start:] ) )
		
		return conditions
		
		


	
class _MatchSublist (_MatchNode):
	def __init__(self, min, max, srcXs):
		super( _MatchSublist, self ).__init__( srcXs=srcXs )
		self.min = min
		self.max = max
		
	def emitPyTreeAndBindings(self, outerTreeFactory, py_value, bindings):
		return self.handleBinding( outerTreeFactory, py_value, bindings )
		
	

	
	
		
def _buildMatchNodeForMatchList(xs):
	# matchItem*
	return _MatchList( [ _buildMatchNodeForMatchItem( x, True )   for x in xs ], srcXs=xs )



def _buildMatchNodeForMatchItem(xs, bInsideList=False):
	if isGLispList( xs ):
		if len( xs ) > 0  and  xs[0] == ':':
			# Bind
			if len( xs ) != 3:
				raise ValueError, 'match expressions: bind expression must take the form (: <var_name> sub_exp)'
			if xs[1][0] != '@':
				raise ValueError, 'match expressions: variable names (to be bound) must start with @'
			varName = xs[1][1:]
			match = _buildMatchNodeForMatchItem( xs[2], bInsideList )
			match.bindName = varName
		elif len( xs ) > 0  and  xs[0] == '?':
			# Condition
			if len( xs ) != 4:
				raise ValueError, 'match expressions:condition expression must take the form (? <var_name> <condition_expr> sub_exp)'
			if xs[1][0] != '@':
				raise ValueError, 'match expressions: condition variable name must start with @'
			conditionVarName = xs[1][1:]
			conditionExprXs = xs[2]
			match = _buildMatchNodeForMatchItem( xs[3], bInsideList )
			match.conditionVarName = conditionVarName
			match.conditionExprXs = conditionExprXs
		elif  len( xs ) > 0  and  xs[0] == '-'  and  bInsideList:
			# Sub-list range
			if len( xs ) != 3:
				raise ValueError, 'match expressions: list interior range expression must take the form (- <#min> <#max>)'
			if xs[1][0] != '#'  or  xs[2][0] != '#':
				raise ValueError, 'match expressions: list interior range numbers must start with #'
			min = int( xs[1][1:] )
			if xs[2] == '#None':
				max = None
			else:
				max = int( xs[2][1:] )
			return _MatchSublist( min, max, srcXs=xs )
		else:
			match = _buildMatchNodeForMatchList( xs )
		return match
	else:
		if xs == '^':
			# match anything
			return _MatchAnything( srcXs=xs )
		elif xs == '!':
			# match any terminal
			return _MatchTerminal( srcXs=xs )
		elif xs == '/':
			# match any non-terminal
			return _MatchNonTerminal( srcXs=xs )
		elif xs == '+'  and  bInsideList:
			# sub-list +
			return _MatchSublist( 1, None, srcXs=xs )
		elif xs == '*'  and  bInsideList:
			# sub-list *
			return _MatchSublist( 0, None, srcXs=xs )
		else:
			# match a constant
			constant = xs.replace( '!!', '!' ).replace( '^^', '^' ).replace( '//', '/' ).replace( '++', '+' ).replace( '**', '*' ).replace( '--', '-' ).replace( '::', ':' ).replace( '??', '?' )
			return _MatchConstant( constant, srcXs=xs )


		
		
	
		
class MatchBlockInvalidType (PyCodeGenError):
	pass

class MatchExprInvalidType (PyCodeGenError):
	pass

class MatchExprNoPattern (PyCodeGenError):
	pass

def compileMatchBlockToPyTrees(matchXs, xs, context, bNeedResult, dataVarName, compileSpecial):
	"""
	compileMatchBlockToPyTrees(matchXs, xs, context, bNeedResult, dataVarName, compileSpecial)  ->  [PyTree]
	
	@matchXs - the overall match expression (for debug information)
	@xs - GLisp expressions to compile
	@context - the compilation context
	@bNeedResult - True if the result is required
	@dataVarName - the name of the variable containing the data that is to be matched to the patterns supplied in @xs
        @compileSpecial is the value passed to _compileGLispExprToPyTree
	
	FORMAT:
	(pattern_expr0 action_expr0)
	(pattern_expr1 action_expr1)
	...
	(pattern_exprN action_exprN)
	
		Pattern expression format:
	   
			bind(x)  :=  [':' @<var_name> x]  |  x
			condition(x)  :=  ['?' @<var_name> <condition_expression> bind(x)]  |  bind(x)
			
			matchX := matchItem
			matchItem := anything | terminal | nonTerminal | constant | list
			anything := bind( '^' )
			terminal := bind( '!' )
			nonTerminal := bind( '/' )
			constant := bind( <string> )
			list := bind( [matchItem* listInternal? matchItem*] )
			listInternal := bind( '+'  |  '*'  |  ['-' #min #max] )
			
			The characters : ? ! - / + * are assigned special meaning, so use :: ?? !! -- // ++ ** to get the characters as constants
			
		Example:
			(a b c)  =>  matches (a b c)
			(a ^ ! /))  =>  matches (a <anything> <terminal> <nonTerminal>)
			(a ^ ! (x y /))  =>  matches (a <anything> <terminal> (x y <nonTerminal>))
			(a * z))  =>  matches (a ... z)   where ... consists of 0 or more elements
			(a + z))  =>  matches (a ... z)   where ... consists of 1 or more elements
			(a (- #2 #4) z))  =>  matches (a ... z)   where ... consists of 2 to 4 elements
			(a (: @a !) z))  =>  matches (a [a]<terminal> z)   [a] indicates that the expression that follows is bound to the variable a
			(a (: @a (i j (: @b !))) z))  =>  matches (a [a](i j [b]<terminal>) z)   where 'a' is bound to (i j [b]<any_string>)   and 'b' is bound to the string
			(a (? @x (@x == q) !) b))  =>  matches (a q b);  the condition binds xs[1] to @x, and evaluates (x == 'q')
			(:: ?? !! -- // ++ **))  =>  matches (: ?? ! - / + *)
	"""
	
	if not isGLispList( xs ):
		raise MatchBlockInvalidType( xs )
	
	bMatchedName = context.temps.allocateTempName( 'match_bMatched' )
	if bNeedResult:
		resultVarName = context.temps.allocateTempName( 'match_result' )
		
	context.body.append( PyVar( bMatchedName ).assign_sideEffects( [ False ] ).debug( matchXs ) )
	
	xs = stripGLispComments( xs )
	matchTrees = []
	bFirst = True
	for match in xs:
		if not isGLispList( match ):
			raise MatchExprInvalidType( match )
		
		if len( match )  <  1:
			raise MatchExprNoPattern( match )
			
		# Get the pattern, and actions
		pattern = match[0]
		actionXs = match[1:]
		
		# Build the matching nodes for the pattern
		patternNode = _buildMatchNodeForMatchItem( pattern )

		# Build the pattern tree factory
		bindings = {}
		patternTreeFac = patternNode.emitPyTreeAndBindings( lambda innerTrees: innerTrees, PyVar( dataVarName ), bindings)
		
		# Gather the conditions
		conditions = patternNode.gatherConditions( PyVar( dataVarName ) )
		
		actionContext = context.innerContext()

		# Action function name
		actionFnName = actionContext.temps.allocateTempName( 'match_fn' )

		# Build the action tree
		actionFnContext = context.innerContext()
		# Bind variables (in alphabetical order)
		bindingPairs = bindings.items()
		bindingPairs.sort( lambda x, y: cmp( x[0], y[0] ) )
			
		# Process the conditions
		for py_value, conditionVarName, conditionExprXs in conditions:
			conditionFnName = actionFnContext.temps.allocateTempName( 'match_condition_fn' )
			conditionFnTree = Britefury.GLisp.GLispCompiler.compileGLispExprToPyFunctionPyTree( conditionFnName, [ conditionVarName ], conditionExprXs, compileSpecial )
			_conditionFnCallTree = PyVar( conditionFnName )( py_value ).debug( conditionExprXs )
			conditionIfTree = _conditionFnCallTree.not_().ifTrue( [ pyt_coerce( None ).return_() ] ).debug( conditionExprXs )
			actionFnContext.body.append( conditionFnTree )
			actionFnContext.body.append( conditionIfTree )		
		
		# Matched
		matchedTrueTree = pyt_coerce( True ).assignTo_sideEffects( PyVar( bMatchedName )[0] ).debug( match )
		actionFnContext.body.append( matchedTrueTree )
		
		# Action expression code
		trees, resultStorePyTree = Britefury.GLisp.GLispCompiler._compileExpressionListToPyTreeStatements( actionXs, actionFnContext, True, compileSpecial, lambda tree, x: PyReturn( tree, dbgSrc=x ) )
		actionFnContext.body.extend( trees )
		
		# Make a function define
		actionFnTree = PyDef( actionFnName, [ pair[0]   for pair in bindingPairs ], actionFnContext.body, dbgSrc=matchXs )
		_actionFnCallTree = PyVar( actionFnName, dbgSrc=actionXs )( *[ pair[1].debug( actionXs )   for pair in bindingPairs ] ).debug( matchXs )
		actionResultAssignTree = PyAssign_SideEffects( PyVar( resultVarName, dbgSrc=matchXs ), _actionFnCallTree, dbgSrc=matchXs )
		
		actionTrees = [ actionFnTree,  actionResultAssignTree ]

	
		matchItemTrees = patternTreeFac( actionTrees )
		
		if not bFirst:
			matchItemTrees = [ PySimpleIf( PyVar( bMatchedName )[0].not_(), matchItemTrees ).debug( match ) ]
			
		matchTrees.extend( matchItemTrees )
		
		bFirst = False

	# Raise NoMatchError if no match found
	matchTrees.append( PySimpleIf( PyVar( bMatchedName )[0].not_(), [ PySrc( 'raise NoMatchError', dbgSrc=xs ) ] ).debug( xs ) )
	
	if bNeedResult:
		resultTree = PyVar( resultVarName ).debug( matchXs )
	else:
		resultTree = None
	return matchTrees, resultTree




	
	
	
	

import unittest
from Britefury.DocModel.DMIO import readSX
from Britefury.GLisp.GLispUtil import gLispSrcToString

class TestCase_MatchExpression (unittest.TestCase):
	def _prepareSrc(self, matchSrc):
		"""
		($match @xs
		...
		)
		"""
		actionXs = [ '@locals', '<-' ]
		if isinstance( matchSrc, list ):
			return [ '$match', '@xs', ]  +  [ [ readSX( m ), actionXs ]   for m in matchSrc ]
		else:
			return [ '$match', '@xs', [ readSX( matchSrc ), actionXs ] ]
	
	def _filterResult(self, result):
		x = {}
		for key, value in result.items():
			if not key.startswith( '__gsym__match' ):
				x[key] = value
		return x
		
		
	
	def _computeResult(self, matchSrc, dataSrc):
		matchXs = self._prepareSrc( matchSrc )
		dataXs = readSX( dataSrc )
		fn = Britefury.GLisp.GLispCompiler.compileGLispExprToPyFunction( 'matchTest', '_match', [ 'xs' ], matchXs )
		return fn( dataXs )

	def _printSrc(self, matchSrc):
		matchXs = self._prepareSrc( matchSrc )
		print Britefury.GLisp.GLispCompiler.compileGLispExprToPyFunctionSrc( '_match', [ 'xs' ], matchXs )

	def _printResult(self, matchSrc, dataSrc):
		print self._filterResult( self._computeResult( matchSrc, dataSrc ) )

	def _matchTest(self, matchSrc, dataSrc, expected, indirection=[]):
		if isinstance( expected, type )  and  issubclass( expected, Exception ):
			self.failUnlessRaises( expected, lambda: Britefury.GLisp.GLispCompiler.compileGLispExprToPyFunction( 'matchTest', '_match', [ 'xs' ], self._prepareSrc( matchSrc ) )( readSX( dataSrc ) ) )
		else:
			result = self._filterResult( self._computeResult( matchSrc, dataSrc ) )
			self.assert_( result == expected )

			

	#def _matchTestCheckIndex(self, matchSrc, dataSrc, expected, expectedIndex, indirection=[]):
		#if expected is NoMatchError:
			#self.failUnlessRaises( NoMatchError, lambda: compileMatchExpressionToPyFunction( readSX( matchSrc ) )[0]( readSX( dataSrc ) ) )
		#else:
			#result, index = compileMatchExpressionToPyFunction( readSX( matchSrc ), indirection )[0]( readSX( dataSrc ) )
			#self.assert_( result == expected )
			#self.assert_( index == expectedIndex )

	def testMatchAnything(self):
		self._matchTest( '^', 'a', {} ) 
		self._matchTest( '^', '(a b c)', {} ) 
	
	def testMatchAnythingBind(self):
		self._matchTest( '(: @a ^)', 'a', { 'a' : 'a' } ) 
		self._matchTest( '(: @a ^)', '(a b c)', { 'a' : ['a', 'b', 'c'] } ) 
	
	def testMatchAnyTerminal(self):
		self._matchTest( '!', 'a', {} ) 
		self._matchTest( '!', '(a b c)', Britefury.GLisp.PatternMatch.NoMatchError ) 
	
	def testMatchAnyTerminalBind(self):
		self._matchTest( '(: @a !)', 'a', { 'a' : 'a' } ) 
		self._matchTest( '(: @a !)', '(a b c)', Britefury.GLisp.PatternMatch.NoMatchError ) 
	
	def testMatchAnyTerminalWithCondition(self):
		self._matchTest( '(? @x (@x startswith ab) !)', 'abc', {} ) 
		self._matchTest( '(? @x (@x startswith ab) !)', 'xyz', Britefury.GLisp.PatternMatch.NoMatchError ) 
	
	def testMatchAnyTerminalBindWithCondition(self):
		self._matchTest( '(? @x (@x startswith ab) (: @a !))', 'abc', { 'a' : 'abc' } ) 
		self._matchTest( '(? @x (@x startswith ab) (: @a !))', 'xyz', Britefury.GLisp.PatternMatch.NoMatchError ) 
	
	def testMatchAnyNonTerminal(self):
		self._matchTest( '/', 'a', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '/', '(a b c)', {} ) 
	
	def testMatchAnyNonTerminalBind(self):
		self._matchTest( '(: @a /)', 'a', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(: @a /)', '(a b c)', { 'a' : ['a', 'b', 'c'] } ) 
	
	def testMatchConstant(self):
		self._matchTest( 'abc', 'abc', {} ) 
		self._matchTest( 'abc', 'xyz', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( 'abc', '(a b c)', Britefury.GLisp.PatternMatch.NoMatchError ) 
	
	def testMatchConstantBind(self):
		self._matchTest( '(: @a abc)', 'abc', { 'a' : 'abc' } ) 
		self._matchTest( '(: @a abc)', 'xyz', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(: @a abc)', '(a b c)', Britefury.GLisp.PatternMatch.NoMatchError ) 
		
	def testMatchFlatList(self):
		self._matchTest( '(a b c)', 'abc', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(a b c)', '(a b c)', {} ) 
		self._matchTest( '(a b c)', '(x y z)', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(a b c d)', '(a b c)', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(a b c d)', '(a b c d)', {} ) 
		
	def testMatchFlatListBind(self):
		self._matchTest( '(: @a (a b c))', 'abc', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(: @a (a b c))', '(a b c)', { 'a' : ['a','b','c'] } ) 
		self._matchTest( '(: @a (a b c))', '(x y z)', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(: @a (a b c d))', '(a b c)', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(: @a (a b c d))', '(a b c d)', { 'a' : ['a','b','c','d'] } ) 
		
	def testMatchNestedList(self):
		self._matchTest( '(a b c (d e f))', 'abc', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(a b c (d e f))', '(a b c d e f)', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(a b c (d e f))', '(a b c (d e f))', {} ) 
		self._matchTest( '(a b c (^ e f))', '(a b c (d e f))', {} ) 
		self._matchTest( '(a b c (^ e f))', '(a b c ((x y) e f))', {} ) 
		self._matchTest( '(a b c (! e f))', '(a b c (d e f))', {} ) 
		self._matchTest( '(a b c (! e f))', '(a b c ((x y) e f))', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(a b c (/ e f))', '(a b c (d e f))', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(a b c (/ e f))', '(a b c ((x y) e f))', {} ) 
		
	def testMatchNestedListBind(self):
		self._matchTest( '(: @a (a b c (: @b (d e f))))', 'abc', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(: @a (a b c (: @b (d e f))))', '(a b c d e f)', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(: @a (a b c (: @b (d e f))))', '(a b c (d e f))', { 'a': ['a','b','c',['d','e','f']], 'b': ['d','e','f'] } ) 
		self._matchTest( '(: @a (a b c (: @b ((: @c ^) e f))))', '(a b c (d e f))', { 'a': ['a','b','c',['d','e','f']], 'b': ['d','e','f'], 'c': 'd' } ) 
		self._matchTest( '(: @a (a b c (: @b ((: @c ^) e f))))', '(a b c ((x y) e f))', { 'a': ['a','b','c',[['x','y'],'e','f']], 'b': [['x','y'],'e','f'], 'c': ['x','y'] } ) 
		self._matchTest( '(: @a (a b c (: @b ((: @c !) e f))))', '(a b c (d e f))', { 'a': ['a','b','c',['d','e','f']], 'b': ['d','e','f'], 'c': 'd' } ) 
		self._matchTest( '(: @a (a b c (: @b ((: @c !) e f))))', '(a b c ((x y) e f))', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(: @a (a b c (: @b ((: @c /) e f))))', '(a b c (d e f))', Britefury.GLisp.PatternMatch.NoMatchError ) 
		self._matchTest( '(: @a (a b c (: @b ((: @c /) e f))))', '(a b c ((x y) e f))', { 'a': ['a','b','c',[['x','y'],'e','f']], 'b': [['x','y'],'e','f'], 'c': ['x','y'] } ) 
	
	def testMatchListWithInteriorStar(self):
		self._matchTest( '(a b * x y)', '(a b x y)', {} )
		self._matchTest( '(a b * x y)', '(a b i j k x y)', {} )
	
	def testMatchListWithInteriorStarBind(self):
		self._matchTest( '(a b (: @a *) x y)', '(a b x y)', { 'a': [] } )
		self._matchTest( '(a b (: @a *) x y)', '(a b i j k x y)', { 'a': ['i','j','k'] } )
		
		
	def testMatchListWithInteriorPlus(self):
		self._matchTest( '(a b + x y)', '(a b x y)', Britefury.GLisp.PatternMatch.NoMatchError )
		self._matchTest( '(a b + x y)', '(a b i j k x y)', {} )
	
	def testMatchListWithInteriorPlusBind(self):
		self._matchTest( '(a b (: @a +) x y)', '(a b x y)', Britefury.GLisp.PatternMatch.NoMatchError)
		self._matchTest( '(a b (: @a +) x y)', '(a b i j k x y)', { 'a': ['i','j','k'] } )

		
	def testMatchListLengths(self):
		# A: fixed length
		self._matchTest( '(: @a (a b c d))', '(a b c d)', { 'a': ['a','b','c','d'] } )
		# B: min length = 0, max length = 3
		self._matchTest( '((: @a (- #0 #3)))', '(a b)', { 'a': ['a','b'] } )
		self._matchTest( '((: @a (- #0 #3)))', '()', { 'a': [] } )
		self._matchTest( '((: @a (- #0 #3)))', '(a b c d)', Britefury.GLisp.PatternMatch.NoMatchError )
		# C: min length > 0, no max length
		self._matchTest( '(a b (: @a *) x y)', '(a b i j k x y)', { 'a': ['i','j','k'] } )
		# D: min length = 4, max length = 6
		self._matchTest( '((: @a (- #4 #6)))', '(a b c d e)', { 'a': ['a','b','c','d','e'] } )
		self._matchTest( '((: @a (- #4 #6)))', '()', Britefury.GLisp.PatternMatch.NoMatchError )
		self._matchTest( '((: @a (- #4 #6)))', '(a b c d e f g h)', Britefury.GLisp.PatternMatch.NoMatchError )
		self._matchTest( '(a (: @a (- #2 #4)) z)', '(a i j k z)', { 'a': ['i','j','k'] } )
		self._matchTest( '(a (: @a (- #2 #4)) z)', '(a z)', Britefury.GLisp.PatternMatch.NoMatchError )
		self._matchTest( '(a (: @a (- #2 #4)) z)', '(a i j k l m n o z)', Britefury.GLisp.PatternMatch.NoMatchError )
		# E: min length = 0, no max length
		self._matchTest( '((: @a *))', '(a b)', { 'a': ['a','b'] } )
	
		
	def testMultiBind(self):
		self._matchTest( '(a b (: @a *) x y (: @a /))', '(a b i j k x y (i j k))', { 'a': ['i','j','k'] } )
		self._matchTest( '(a b (: @a *) x y (: @a /))', '(a b i j k x y (m n o))', Britefury.GLisp.PatternMatch.NoMatchError )
		self._matchTest( '(a b (: @a !) x y (: @a !))', '(a b q x y q)', { 'a': 'q' } )
		self._matchTest( '(a b (: @a !) x y (: @a !))', '(a b q x y w)', Britefury.GLisp.PatternMatch.NoMatchError )
		self._matchTest( '(a b (: @a ^) x y (: @a ^))', '(a b q x y q)', { 'a': 'q' } )
		self._matchTest( '(a b (: @a ^) x y (: @a ^))', '(a b q x y (i j))', Britefury.GLisp.PatternMatch.NoMatchError )
		self._matchTest( '(a b (: @a ^) x y (: @a ^))', '(a b (i j) x y q)', Britefury.GLisp.PatternMatch.NoMatchError )
		self._matchTest( '(a b (: @a ^) x y (: @a ^))', '(a b (i j) x y (i j))', { 'a': ['i','j'] } )
		
	def testMultiMatch(self):
		self._matchTest( [ '(a (: @a !))',  '(b (: @b !))',  '(c (: @c !))',  '(d (: @d !))' ], '(a x)', { 'a' : 'x' } )
		self._matchTest( [ '(a (: @a !))',  '(b (: @b !))',  '(c (: @c !))',  '(d (: @d !))' ], '(b x)', { 'b' : 'x' } )
		self._matchTest( [ '(a (: @a !))',  '(b (: @b !))',  '(c (: @c !))',  '(d (: @d !))' ], '(c x)', { 'c' : 'x' } )
		self._matchTest( [ '(a (: @a !))',  '(b (: @b !))',  '(c (: @c !))',  '(d (: @d !))' ], '(d x)', { 'd' : 'x' } )
		self._matchTest( [ '(a (: @a !))',  '(b (: @b !))',  '(c (: @c !))',  '(d (: @d !))' ], '(e x)', Britefury.GLisp.PatternMatch.NoMatchError )
		
	def testMultiMatchWithCondition(self):
		self._matchTest( [ '(a (? @x (@x startswith xx) (: @a !)))',  '(b (? @x (@x startswith yy) (: @b !)))',  '(c (? @x (@x startswith zz) (: @c !)))',  '(d (? @x (@x startswith ww) (: @d !)))' ], '(a xxx)', { 'a' : 'xxx' } )
		self._matchTest( [ '(a (? @x (@x startswith xx) (: @a !)))',  '(b (? @x (@x startswith yy) (: @b !)))',  '(c (? @x (@x startswith zz) (: @c !)))',  '(d (? @x (@x startswith ww) (: @d !)))' ], '(a aaa)', Britefury.GLisp.PatternMatch.NoMatchError )
		self._matchTest( [ '(a (? @x (@x startswith xx) (: @a !)))',  '(b (? @x (@x startswith yy) (: @b !)))',  '(c (? @x (@x startswith zz) (: @c !)))',  '(d (? @x (@x startswith ww) (: @d !)))' ], '(b yyy)', { 'b' : 'yyy' } )
		self._matchTest( [ '(a (? @x (@x startswith xx) (: @a !)))',  '(b (? @x (@x startswith yy) (: @b !)))',  '(c (? @x (@x startswith zz) (: @c !)))',  '(d (? @x (@x startswith ww) (: @d !)))' ], '(b aaa)', Britefury.GLisp.PatternMatch.NoMatchError )
		self._matchTest( [ '(a (? @x (@x startswith xx) (: @a !)))',  '(b (? @x (@x startswith yy) (: @b !)))',  '(c (? @x (@x startswith zz) (: @c !)))',  '(d (? @x (@x startswith ww) (: @d !)))' ], '(c zzz)', { 'c' : 'zzz' } )
		self._matchTest( [ '(a (? @x (@x startswith xx) (: @a !)))',  '(b (? @x (@x startswith yy) (: @b !)))',  '(c (? @x (@x startswith zz) (: @c !)))',  '(d (? @x (@x startswith ww) (: @d !)))' ], '(c aaa)', Britefury.GLisp.PatternMatch.NoMatchError )
		self._matchTest( [ '(a (? @x (@x startswith xx) (: @a !)))',  '(b (? @x (@x startswith yy) (: @b !)))',  '(c (? @x (@x startswith zz) (: @c !)))',  '(d (? @x (@x startswith ww) (: @d !)))' ], '(d www)', { 'd' : 'www' } )
		self._matchTest( [ '(a (? @x (@x startswith xx) (: @a !)))',  '(b (? @x (@x startswith yy) (: @b !)))',  '(c (? @x (@x startswith zz) (: @c !)))',  '(d (? @x (@x startswith ww) (: @d !)))' ], '(d aaa)', Britefury.GLisp.PatternMatch.NoMatchError )
		self._matchTest( [ '(a (? @x (@x startswith xx) (: @a !)))',  '(b (? @x (@x startswith yy) (: @b !)))',  '(c (? @x (@x startswith zz) (: @c !)))',  '(d (? @x (@x startswith ww) (: @d !)))' ], '(e www)', Britefury.GLisp.PatternMatch.NoMatchError )
		self._matchTest( [ '(a (? @x (@x startswith xx) (: @a !)))',  '(b (? @x (@x startswith yy) (: @b !)))',  '(c (? @x (@x startswith zz) (: @c !)))',  '(d (? @x (@x startswith ww) (: @d !)))' ], '(e aaa)', Britefury.GLisp.PatternMatch.NoMatchError )
		
	def testSpecials(self):
		self._matchTest( '(:: ?? !! -- // ++ **)', '(: ? ! - / + *)', {} )
		self._matchTest( '(::a:: ??a?? !!a!! --a-- //a// ++a++ **a**)', '(:a: ?a? !a! -a- /a/ +a+ *a*)', {} )


		
if __name__ == '__main__':
	unittest.main()
