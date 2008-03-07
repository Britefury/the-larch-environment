##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

from Britefury.Kernel.Abstract import abstractmethod

from Britefury.DocModel.DMListInterface import DMListInterface
from Britefury.GLisp.GLispInterpreter import isGLispList


_stringType = str
_stringTypeSrc = 'str'
_listType = DMListInterface
_listTypeSrc = 'DMListInterface'


class NoMatchError (Exception):
	pass


def _indent(src):
	return [ '  ' + s   for s in src ]

def _indentN(src, n):
	return [ '  ' * n  +  s   for s in src ]




class _SrcBuilder (object):
	def __init__(self):
		self.src = []
		self._indentation = 0
		
		
	def append(self, line):
		self.src.append( '  ' * self._indentation  +  line )
	
	def extend(self, src):
		self.src.extend( [ '  ' * self._indentation  +  s   for s in src ] )
	
	def condition(self, conditionExprSrc):
		self.append( 'if %s:'  %  ( conditionExprSrc, ) )
		self._indentation += 1


class  _MatchNode (object):
	def __init__(self):
		super( _MatchNode, self ).__init__()
		self.bindName = None
		
		
	@abstractmethod
	def emitSourceVarNamesAndBindings(self, src, valueSrc, valueIndirection, varNameToValueIndirection, bindings):
		pass
	
	def handleBinding(self, src, valueSrc, valueIndirection, varNameToValueIndirection, bindings):
		if self.bindName is not None:
			varNameToValueIndirection.setdefault( self.bindName, valueIndirection )
			if self.bindName in bindings:
				src.condition( '%s == %s'  %  ( valueSrc, bindings[self.bindName] ) )
			else:
				bindings[self.bindName] = valueSrc
		return []
	
	def _p_emitMatchFailed(self):
		return [ 'raise NoMatchError' ]
	
		
		
		



class _MatchAnything (_MatchNode):
	def emitSourceVarNamesAndBindings(self, src, valueSrc, valueIndirection, varNameToValueIndirection, bindings):
		self.handleBinding( src, valueSrc, valueIndirection, varNameToValueIndirection, bindings )
	

class _MatchTerminal (_MatchNode):
	def emitSourceVarNamesAndBindings(self, src, valueSrc, valueIndirection, varNameToValueIndirection, bindings):
		src.condition( 'not __isGLispList__( %s )'  %  ( valueSrc, ) )
		self.handleBinding( src, valueSrc, valueIndirection, varNameToValueIndirection, bindings )
	


class _MatchNonTerminal (_MatchNode):
	def emitSourceVarNamesAndBindings(self, src, valueSrc, valueIndirection, varNameToValueIndirection, bindings):
		src.condition( '__isGLispList__( %s )'  %  ( valueSrc, ) )
		self.handleBinding( src, valueSrc, valueIndirection, varNameToValueIndirection, bindings )

	
class _MatchConstant (_MatchNode):
	def __init__(self, constant):
		super( _MatchConstant, self ).__init__()
		self._constant = constant

	def emitSourceVarNamesAndBindings(self, src, valueSrc, valueIndirection, varNameToValueIndirection, bindings):
		src.condition( '%s == \'%s\''  %  ( valueSrc, self._constant ))
		self.handleBinding( src, valueSrc, valueIndirection, varNameToValueIndirection, bindings )


class _MatchList (_MatchNode):
	def __init__(self, subMatches):
		super( _MatchList, self ).__init__()
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
		
	def emitSourceVarNamesAndBindings(self, src, valueSrc, valueIndirection, varNameToValueIndirection, bindings):
		# Check the lengths
		src.condition( '__isGLispList__( %s )'  %  ( valueSrc, ) )

		if self._minLength == self._maxLength:
			# min and max length the same; only one length permissable
			src.condition( 'len( %s )  ==  %d'  %  ( valueSrc, self._minLength ) )
		elif self._minLength == 0  and  self._maxLength is not None:
			# no min length, a max length
			src.condition( 'len( %s )  <=  %d'  %  ( valueSrc, self._maxLength ) )
		elif self._minLength > 0:
			# there is a min length
			if self._maxLength is None:
				# no max length
				src.condition( 'len( %s )  >=  %d'  %  ( valueSrc, self._minLength ) )
			else:
				# max length
				src.condition( 'len( %s )  >=  %d   and   len( %s )  <=  %d'  %  ( valueSrc, self._minLength, valueSrc, self._maxLength ) )
				
		#matchItem*  (front)
		for i, item in enumerate( self._front ):
			itemNameSrc = '%s[%d]'  %  ( valueSrc, i )
			item.emitSourceVarNamesAndBindings( src, itemNameSrc, valueIndirection + [ i ], varNameToValueIndirection, bindings )
					
		#matchItem*  (back)
		for i, item in enumerate( reversed( self._back ) ):
			itemNameSrc = '%s[-%d]'  %  ( valueSrc, i + 1 )
			item.emitSourceVarNamesAndBindings( src, itemNameSrc, valueIndirection + [ -(i+1) ], varNameToValueIndirection, bindings )
			
		if self._internal is not None:
			start = len( self._front )
			end = len( self._back )
			if end  > 0:
				self._internal.emitSourceVarNamesAndBindings( src, '%s[%d:%d]'  %  ( valueSrc, start, -end ), valueIndirection + [ (start,-end) ], varNameToValueIndirection, bindings )
			else:
				self._internal.emitSourceVarNamesAndBindings( src, '%s[%d:]'  %  ( valueSrc, start ), valueIndirection + [ (start,None) ], varNameToValueIndirection, bindings )
		
		self.handleBinding( src, valueSrc, valueIndirection, varNameToValueIndirection, bindings )
		
		return src

	
class _MatchSublist (_MatchNode):
	def __init__(self, min=0, max=None):
		super( _MatchSublist, self ).__init__()
		self.min = min
		self.max = max
		
	def emitSourceVarNamesAndBindings(self, src, valueSrc, valueIndirection, varNameToValueIndirection, bindings):
		self.handleBinding( src, valueSrc, valueIndirection, varNameToValueIndirection, bindings )
		
	

	
	
		
def _buildMatchNodeForMatchList(xs):
	# matchItem*
	return _MatchList( [ _buildMatchNodeForMatchItem( x, True )   for x in xs ] )



def _buildMatchNodeForMatchItem(xs, bInsideList=False):
	if isGLispList( xs ):
		if xs[0] == ':':
			# Bind
			if len( xs ) != 3:
				raise ValueError, 'match expressions: bind expression must take the form (: <var_name> sub_exp)'
			if xs[1][0] != '@':
				raise ValueError, 'match expressions: variable names (to be bound) must start with @'
			varName = xs[1][1:]
			match = _buildMatchNodeForMatchItem( xs[2], bInsideList )
			match.bindName = varName
		elif xs[0] == '-'  and  bInsideList:
			# Sub-list range
			if len( xs ) != 3:
				raise ValueError, 'match expressions: list interior range expression must take the form (- <#min> <#max>)'
			if xs[1][0] != '#'  or  xs[2][0] != '#':
				raise ValueError, 'match expressions: list interior range numbers must start with #'
			return _MatchSublist( int( xs[1][1:] ), int( xs[2][1:] ) )
		else:
			match = _buildMatchNodeForMatchList( xs )
		return match
	else:
		if xs == '^':
			# match anything
			return _MatchAnything()
		elif xs == '!':
			# match any terminal
			return _MatchTerminal()
		elif xs == '/':
			# match any non-terminal
			return _MatchNonTerminal()
		elif xs == '+'  and  bInsideList:
			# sub-list +
			return _MatchSublist( 1, None )
		elif xs == '*'  and  bInsideList:
			# sub-list *
			return _MatchSublist( 0, None )
		else:
			# match a constant
			constant = xs.replace( '!!', '!' ).replace( '^^', '^' ).replace( '//', '/' ).replace( '++', '+' ).replace( '**', '*' ).replace( '--', '-' ).replace( '::', ':' )
			return _MatchConstant( constant )


		
		
def _bindingMapToBindingDictSrc(bindings):
	return '{ '  +  ', '.join( [ '\'%s\' : %s'  %  ( name, valueSrc )   for name, valueSrc in bindings.items() ] )  +  ' }'
	
	
	
		
def compileMatchExpression(xs, exprIndirection=[], functionName='match', bSrc=False):
	"""
	compileMatchExpression(xs, exprIndirection=[], functionName='match', bSrc=False)   ->   fn, varNames
		xs is a list of match expressions. It is a GLisp list (a list or a DMListInterface)
		exprIndirection is a list that specifies the indrection necessary to get the match expression from each item in xs
			Examples:
				(match0 match1 ... matchN)    ->     [] (no indirection)
				((match0 action0) (match1 action1) ... (matchN actionN))     ->    [0] (get the first element of each item)
				((? (? ? match0) ?) (? (? ? match1) ?) ... (? (? ? matchN) ?))     ->     [1,2] (get the third element of the second element of each item)
		functionName is the name of the python function that will be returned by compileMatchExpression()  (fn.__name__)
		Return values:
			fn: the function that is generated by compiling the match expression. It is of the form:
				fn(xs) -> vars, index
					where:
						xs: the list to be processed by that match espression
					returns:
						vars: a dictionary mapping variable name (specified in the match expression) to value
						index: the index of the match expression that was matched
			varNameToValueIndirectionTable: a list of dictionaties; one for each match expression. Each dictionary maps the names of the variables generated by the corresponding match expression,
			        to a list representing the indirection required to get from the root to the corresponding expression that is bound to the variable name
				for example, if 'a' is bound to xs[1][3][6], then the dictionary will contain { 'a' : [1,3,6] }.
				Where a 'listInternal' (+, *, (- #min #max); see below) is in the match expression, an entry will contain a tuple which represents the sublist. For example [1,3,(2,4)] means xs[1][3][2:4]
				Since 'listInternal' entries cannot be processed further by a match expression, the tuple would always be the last entry on the list.
	   
	   
		Match expression format:
	   
			bind(x)  :=  [':' <var_name> x]  |  x
			
			matchX := matchItem
			matchItem := anything | terminal | nonTerminal | constant | list
			anything := bind( '^' )
			terminal := bind( '!' )
			nonTerminal := bind( '/' )
			constant := bind( <string> )
			list := bind( [matchItem* listInternal? matchItem*] )
			listInternal := bind( '+'  |  '*'  |  ['-' #min #max] )
			
			The characters : ! - / + * are assigned special meaning, so use :: !! -- // ++ ** to get the characters as constants
			
		Example:
			((a b c))  =>  matches (a b c)
			((a b c) (d e f)  =>  matches (a b c)  or  (d e f)
			((a ! ^ /))  =>  matches (a <anything> <terminal> <nonTerminal>)
			((a ! ^ (x y /))  =>  matches (a <anything> <terminal> (x y <nonTerminal>))
			((a * z))  =>  matches (a ... z)   where ... consists of 0 or more elements
			((a + z))  =>  matches (a ... z)   where ... consists of 1 or more elements
			((a (- #2 #4) z))  =>  matches (a ... z)   where ... consists of 2 to 4 elements
			((a (: @a ^) z))  =>  matches (a [a]<terminal> z)   [a] indicates that the expression that follows is bound to the variable a
			((a (: @a (i j (: @b ^))) z))  =>  matches (a [a](i j [b]<terminal>) z)   where 'a' is bound to (i j [b]<any_string>)   and 'b' is bound to the string
			((:: !! -- // ++ **))  =>  matches (: ! - / + *)
	"""
	
	pySrcHdr = 'def %s(xs):\n'  %  ( functionName, )
	
	if not isGLispList( xs ):
		raise ValueError, 'need a list'
	
	
	result = []
	varNameToValueIndirectionTable = []
	for index, match in enumerate( xs ):
		for i in exprIndirection:
			match = match[i]
		matchNode = _buildMatchNodeForMatchItem( match )

		src = _SrcBuilder()
		matchItemVarNameToValueIndirection = {}
		bindings = {}
		matchNode.emitSourceVarNamesAndBindings( src, 'xs', [], matchItemVarNameToValueIndirection, bindings)
		
		src.append( 'return '  +  _bindingMapToBindingDictSrc( bindings )  +  ', %d'  %  ( index, ) )

		varNameToValueIndirectionTable.append( matchItemVarNameToValueIndirection )
		
		result += src.src
	result.extend( [ 'raise NoMatchError' ] )
	
	result = _indent( result )
	
	src = pySrcHdr + '\n'.join( result )
	
	if bSrc:
		return src, varNameToValueIndirectionTable
	else:	
		lcl = { '__isGLispList__'  :  isGLispList, 'NoMatchError' : NoMatchError, '%s' % ( _listTypeSrc, )  :  _listType }
		
		exec src in lcl
		
		return lcl[functionName], varNameToValueIndirectionTable



import unittest
from Britefury.DocModel.DMIO import readSX

class TestCase_MatchExpression (unittest.TestCase):
	def _printSrc(self, matchSrc):
		print compileMatchExpression( readSX( matchSrc ), bSrc=True )

	def _printResult(self, matchSrc, dataSrc):
		print compileMatchExpression( readSX( matchSrc ) )[0]( readSX( dataSrc ) )

	def _matchTest(self, matchSrc, dataSrc, expected, indirection=[]):
		if expected is NoMatchError:
			self.failUnlessRaises( NoMatchError, lambda: compileMatchExpression( readSX( matchSrc ) )[0]( readSX( dataSrc ) ) )
		else:
			result, index = compileMatchExpression( readSX( matchSrc ), indirection )[0]( readSX( dataSrc ) )
			self.assert_( result == expected )

	def _failurePrintSrc(self, reason, matchSrc, dataSrc):
		print 'FAILURE ' + reason
		print '#######################'
		print matchSrc
		print dataSrc
		print '#######################'
		print compileMatchExpression( readSX( matchSrc ), bSrc=True )[0]
		print '#######################'
		print ''
			

	def _matchTestCheckIndex(self, matchSrc, dataSrc, expected, expectedIndex, indirection=[]):
		if expected is NoMatchError:
			self.failUnlessRaises( NoMatchError, lambda: compileMatchExpression( readSX( matchSrc ) )[0]( readSX( dataSrc ) ) )
		else:
			result, index = compileMatchExpression( readSX( matchSrc ), indirection )[0]( readSX( dataSrc ) )
			self.assert_( result == expected )
			self.assert_( index == expectedIndex )

	def testEmpty(self):
		self._matchTest( '()', '()', NoMatchError )

	def testMatchAnything(self):
		self._matchTest( '(^)', 'a', {} ) 
		self._matchTest( '(^)', '(a b c)', {} ) 
	
	def testMatchAnythingBind(self):
		self._matchTest( '((: @a ^))', 'a', { 'a' : 'a' } ) 
		self._matchTest( '((: @a ^))', '(a b c)', { 'a' : ['a', 'b', 'c'] } ) 
	
	def testMatchAnyTerminal(self):
		self._matchTest( '(!)', 'a', {} ) 
		self._matchTest( '(!)', '(a b c)', NoMatchError ) 
	
	def testMatchAnyTerminalBind(self):
		self._matchTest( '((: @a !))', 'a', { 'a' : 'a' } ) 
		self._matchTest( '((: @a !))', '(a b c)', NoMatchError ) 
	
	def testMatchAnyNonTerminal(self):
		self._matchTest( '(/)', 'a', NoMatchError ) 
		self._matchTest( '(/)', '(a b c)', {} ) 
	
	def testMatchAnyNonTerminalBind(self):
		self._matchTest( '((: @a /))', 'a', NoMatchError ) 
		self._matchTest( '((: @a /))', '(a b c)', { 'a' : ['a', 'b', 'c'] } ) 
	
	def testMatchConstant(self):
		self._matchTest( '(abc)', 'abc', {} ) 
		self._matchTest( '(abc)', 'xyz', NoMatchError ) 
		self._matchTest( '(abc)', '(a b c)', NoMatchError ) 
	
	def testMatchConstantBind(self):
		self._matchTest( '((: @a abc))', 'abc', { 'a' : 'abc' } ) 
		self._matchTest( '((: @a abc))', 'xyz', NoMatchError ) 
		self._matchTest( '((: @a abc))', '(a b c)', NoMatchError ) 
		
	def testMatchFlatList(self):
		self._matchTest( '((a b c))', 'abc', NoMatchError ) 
		self._matchTest( '((a b c))', '(a b c)', {} ) 
		self._matchTest( '((a b c))', '(x y z)', NoMatchError ) 
		self._matchTest( '((a b c d))', '(a b c)', NoMatchError ) 
		self._matchTest( '((a b c d))', '(a b c d)', {} ) 
		
	def testMatchFlatListBind(self):
		self._matchTest( '((: @a (a b c)))', 'abc', NoMatchError ) 
		self._matchTest( '((: @a (a b c)))', '(a b c)', { 'a' : ['a','b','c'] } ) 
		self._matchTest( '((: @a (a b c)))', '(x y z)', NoMatchError ) 
		self._matchTest( '((: @a (a b c d)))', '(a b c)', NoMatchError ) 
		self._matchTest( '((: @a (a b c d)))', '(a b c d)', { 'a' : ['a','b','c','d'] } ) 
		
	def testMatchNestedList(self):
		self._matchTest( '((a b c (d e f)))', 'abc', NoMatchError ) 
		self._matchTest( '((a b c (d e f)))', '(a b c d e f)', NoMatchError ) 
		self._matchTest( '((a b c (d e f)))', '(a b c (d e f))', {} ) 
		self._matchTest( '((a b c (^ e f)))', '(a b c (d e f))', {} ) 
		self._matchTest( '((a b c (^ e f)))', '(a b c ((x y) e f))', {} ) 
		self._matchTest( '((a b c (! e f)))', '(a b c (d e f))', {} ) 
		self._matchTest( '((a b c (! e f)))', '(a b c ((x y) e f))', NoMatchError ) 
		self._matchTest( '((a b c (/ e f)))', '(a b c (d e f))', NoMatchError ) 
		self._matchTest( '((a b c (/ e f)))', '(a b c ((x y) e f))', {} ) 
		
	def testMatchNestedListBind(self):
		self._matchTest( '((: @a (a b c (: @b (d e f)))))', 'abc', NoMatchError ) 
		self._matchTest( '((: @a (a b c (: @b (d e f)))))', '(a b c d e f)', NoMatchError ) 
		self._matchTest( '((: @a (a b c (: @b (d e f)))))', '(a b c (d e f))', { 'a': ['a','b','c',['d','e','f']], 'b': ['d','e','f'] } ) 
		self._matchTest( '((: @a (a b c (: @b ((: @c ^) e f)))))', '(a b c (d e f))', { 'a': ['a','b','c',['d','e','f']], 'b': ['d','e','f'], 'c': 'd' } ) 
		self._matchTest( '((: @a (a b c (: @b ((: @c ^) e f)))))', '(a b c ((x y) e f))', { 'a': ['a','b','c',[['x','y'],'e','f']], 'b': [['x','y'],'e','f'], 'c': ['x','y'] } ) 
		self._matchTest( '((: @a (a b c (: @b ((: @c !) e f)))))', '(a b c (d e f))', { 'a': ['a','b','c',['d','e','f']], 'b': ['d','e','f'], 'c': 'd' } ) 
		self._matchTest( '((: @a (a b c (: @b ((: @c !) e f)))))', '(a b c ((x y) e f))', NoMatchError ) 
		self._matchTest( '((: @a (a b c (: @b ((: @c /) e f)))))', '(a b c (d e f))', NoMatchError ) 
		self._matchTest( '((: @a (a b c (: @b ((: @c /) e f)))))', '(a b c ((x y) e f))', { 'a': ['a','b','c',[['x','y'],'e','f']], 'b': [['x','y'],'e','f'], 'c': ['x','y'] } ) 
	
	def testMatchListWithInteriorStar(self):
		self._matchTest( '((a b * x y))', '(a b x y)', {} )
		self._matchTest( '((a b * x y))', '(a b i j k x y)', {} )
	
	def testMatchListWithInteriorStarBind(self):
		self._matchTest( '((a b (: @a *) x y))', '(a b x y)', { 'a': [] } )
		self._matchTest( '((a b (: @a *) x y))', '(a b i j k x y)', { 'a': ['i','j','k'] } )
		
		
	def testMatchListWithInteriorPlus(self):
		self._matchTest( '((a b + x y))', '(a b x y)', NoMatchError )
		self._matchTest( '((a b + x y))', '(a b i j k x y)', {} )
	
	def testMatchListWithInteriorPlusBind(self):
		self._matchTest( '((a b (: @a +) x y))', '(a b x y)', NoMatchError)
		self._matchTest( '((a b (: @a +) x y))', '(a b i j k x y)', { 'a': ['i','j','k'] } )

		
	def testMatchListLengths(self):
		# A: fixed length
		self._matchTest( '((: @a (a b c d)))', '(a b c d)', { 'a': ['a','b','c','d'] } )
		# B: min length = 0, max length = 3
		self._matchTest( '(((: @a (- #0 #3))))', '(a b)', { 'a': ['a','b'] } )
		self._matchTest( '(((: @a (- #0 #3))))', '()', { 'a': [] } )
		self._matchTest( '(((: @a (- #0 #3))))', '(a b c d)', NoMatchError )
		# C: min length > 0, no max length
		self._matchTest( '((a b (: @a *) x y))', '(a b i j k x y)', { 'a': ['i','j','k'] } )
		# D: min length = 4, max length = 6
		self._matchTest( '(((: @a (- #4 #6))))', '(a b c d e)', { 'a': ['a','b','c','d','e'] } )
		self._matchTest( '(((: @a (- #4 #6))))', '()', NoMatchError )
		self._matchTest( '(((: @a (- #4 #6))))', '(a b c d e f g h)', NoMatchError )
		self._matchTest( '((a (: @a (- #2 #4)) z))', '(a i j k z)', { 'a': ['i','j','k'] } )
		self._matchTest( '((a (: @a (- #2 #4)) z))', '(a z)', NoMatchError )
		self._matchTest( '((a (: @a (- #2 #4)) z))', '(a i j k l m n o z)', NoMatchError )
		# E: min length = 0, no max length
		self._matchTest( '(((: @a *)))', '(a b)', { 'a': ['a','b'] } )
	
		
	def testMultiBind(self):
		self._matchTest( '((a b (: @a *) x y (: @a /)))', '(a b i j k x y (i j k))', { 'a': ['i','j','k'] } )
		self._matchTest( '((a b (: @a *) x y (: @a /)))', '(a b i j k x y (m n o))', NoMatchError )
		self._matchTest( '((a b (: @a !) x y (: @a !)))', '(a b q x y q)', { 'a': 'q' } )
		self._matchTest( '((a b (: @a !) x y (: @a !)))', '(a b q x y w)', NoMatchError )
		self._matchTest( '((a b (: @a ^) x y (: @a ^)))', '(a b q x y q)', { 'a': 'q' } )
		self._matchTest( '((a b (: @a ^) x y (: @a ^)))', '(a b q x y (i j))', NoMatchError )
		self._matchTest( '((a b (: @a ^) x y (: @a ^)))', '(a b (i j) x y q)', NoMatchError )
		self._matchTest( '((a b (: @a ^) x y (: @a ^)))', '(a b (i j) x y (i j))', { 'a': ['i','j'] } )
		
	def testMultiMatch(self):
		self._matchTestCheckIndex( '((a (: @a !))  (b (: @b !))  (c (: @c !))  (d (: @d !)))', '(a x)', { 'a' : 'x' }, 0 )
		self._matchTestCheckIndex( '((a (: @a !))  (b (: @b !))  (c (: @c !))  (d (: @d !)))', '(b x)', { 'b' : 'x' }, 1 )
		self._matchTestCheckIndex( '((a (: @a !))  (b (: @b !))  (c (: @c !))  (d (: @d !)))', '(c x)', { 'c' : 'x' }, 2 )
		self._matchTestCheckIndex( '((a (: @a !))  (b (: @b !))  (c (: @c !))  (d (: @d !)))', '(d x)', { 'd' : 'x' }, 3 )
		self._matchTestCheckIndex( '((a (: @a !))  (b (: @b !))  (c (: @c !))  (d (: @d !)))', '(e x)', NoMatchError, None )
		
	def testIndirection(self):
		self._matchTest( '(((a (: @a !)))  ((b (: @b !)))  ((c (: @c !)))  ((d (: @d !))))', '(a x)', { 'a' : 'x' }, [0] )
		
	def testVarNames(self):
		result = compileMatchExpression( readSX( '((a (: @foo ^) (: @bar !) (: @doh /) (: @re *)))' ) )[1]
		varNamesToIndirection = result[0]
		varNames = set( varNamesToIndirection.keys() )
		self.assert_( varNames == set( [ 'foo', 'bar', 'doh', 're' ] ) )

	def testSpecials(self):
		self._matchTest( '((:: !! -- // ++ **))', '(: ! - / + *)', {} )
		self._matchTest( '((::a:: !!a!! --a-- //a// ++a++ **a**))', '(:a: !a! -a- /a/ +a+ *a*)', {} )


		
if __name__ == '__main__':
	unittest.main()
