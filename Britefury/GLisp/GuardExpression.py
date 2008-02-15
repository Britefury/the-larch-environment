##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

from Britefury.Kernel.Abstract import abstractmethod

from Britefury.DocModel.DMListInterface import DMListInterface


_stringType = str
_stringTypeSrc = 'str'
_listType = DMListInterface
_listTypeSrc = 'DMListInterface'


class GuardError (Exception):
	pass


def _indent(src):
	return [ '\t' + s   for s in src ]



def _bind(result, name, value):
	existingValue = result.setdefault( name, value )
	if existingValue != value:
		raise GuardError

	
class  _GuardMatch (object):
	def __init__(self):
		super( _GuardMatch, self ).__init__()
		self.bindName = None
		
		
	@abstractmethod
	def emitSourceAndVarNames(self, valueSrc, varNames):
		pass

	
	def _p_emitMatchFailed(self):
		return [ 'raise GuardError' ]
	
	def _p_emitBind(self, valueSrc, varNames):
		if self.bindName is not None:
			varNames.add( self.bindName )
			return [ '_bind( result, \'%s\', %s )'  %  ( self.bindName, valueSrc ) ]
		else:
			return []
		



class _GuardMatchAnything (_GuardMatch):
	def emitSourceAndVarNames(self, valueSrc, varNames):
		return self._p_emitBind( valueSrc, varNames )


class _GuardMatchAnyString (_GuardMatch):
	def emitSourceAndVarNames(self, valueSrc, varNames):
		return [ 'if not isinstance( %s, %s ):'  %  ( valueSrc, _stringTypeSrc ) ]  +  \
		       _indent( self._p_emitMatchFailed()  )  +  \
		       self._p_emitBind( valueSrc, varNames )


class _GuardMatchAnyList (_GuardMatch):
	def emitSourceAndVarNames(self, valueSrc, varNames):
		return [ 'if not isinstance( %s, %s ):'  %  ( valueSrc, _listTypeSrc ) ]  +  \
		       _indent( self._p_emitMatchFailed()  )  +  \
		       self._p_emitBind( valueSrc, varNames )


class _GuardMatchString (_GuardMatch):
	def __init__(self, constant):
		super( _GuardMatchString, self ).__init__()
		self._constant = constant

	def emitSourceAndVarNames(self, valueSrc, varNames):
		return [ 'if %s != \'%s\':'  %  ( valueSrc, self._constant ) ]  +  \
		       _indent( self._p_emitMatchFailed()  )  +  \
		       self._p_emitBind( valueSrc, varNames )


class _GuardMatchList (_GuardMatch):
	def __init__(self, subMatches):
		super( _GuardMatchList, self ).__init__()
		internalIndex = None
		for i, m in enumerate( subMatches ):
			if isinstance( m, _GuardMatchListInternal ):
				if internalIndex is not None:
					raise ValueError, 'guard expression compilation: only one list interior (*, +, -) inside a guard match list'
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
		
	def emitSourceAndVarNames(self, valueSrc, varNames):
		# Check the lengths
		src = []
		
		src.append( 'if not isinstance( %s, %s ):'  %  ( valueSrc, _listTypeSrc ) )
		src.extend( _indent( self._p_emitMatchFailed() ) )
		src.append( '' )

		if self._minLength == self._maxLength:
			# min and max length the same; only one length permissable
			src.append( 'if len( %s )  !=  %d:'  %  ( valueSrc, self._minLength ) )
			src.extend( _indent( self._p_emitMatchFailed() ) )
			src.append( '' )
		elif self._minLength == 0  and  self._maxLength is not None:
			# no min length, a max length
			src.append( 'if len( %s )  >  %d:'  %  ( valueSrc, self._maxLength ) )
			src.extend( _indent( self._p_emitMatchFailed() ) )
			src.append( '' )
		elif self._minLength > 0:
			# there is a min length
			if self._maxLength is None:
				# no max length
				src.append( 'if len( %s )  <  %d:'  %  ( valueSrc, self._minLength ) )
				src.extend( _indent( self._p_emitMatchFailed() ) )
				src.append( '' )
			else:
				# max length
				src.append( 'if len( %s )  <  %d   or   len( %s )  >  %d:'  %  ( valueSrc, self._minLength, valueSrc, self._maxLength ) )
				src.extend( _indent( self._p_emitMatchFailed() ) )
				src.append( '' )
				
		#guardItem*  (front)
		for i, item in enumerate( self._front ):
			itemNameSrc = '%s[%d]'  %  ( valueSrc, i )
			itemSrc = item.emitSourceAndVarNames( itemNameSrc, varNames )
			src.extend( itemSrc )
					
		#guardItem*  (back)
		for i, item in enumerate( reversed( self._back ) ):
			itemNameSrc = '%s[-%d]'  %  ( valueSrc, i + 1 )
			itemSrc = item.emitSourceAndVarNames( itemNameSrc, varNames )
			src.extend( itemSrc )
			
		if self._internal is not None:
			src.append( '' )
			start = len( self._front )
			if len( self._back ) > 0:
				src.extend( self._internal.emitSourceAndVarNames( '%s[%d:-%d]'  %  ( valueSrc, len( self._front ), len( self._back ) ), varNames ) )
			else:
				src.extend( self._internal.emitSourceAndVarNames( '%s[%d:]'  %  ( valueSrc, len( self._front ) ), varNames ) )
		
		src.append( '' )
		src.extend( self._p_emitBind( valueSrc, varNames ) )
		
		return src

	
		
class _GuardMatchListInternal (_GuardMatch):
	def __init__(self, min=0, max=None):
		super( _GuardMatchListInternal, self ).__init__()
		self.min = min
		self.max = max
		
	def emitSourceAndVarNames(self, valueSrc, varNames):
		return self._p_emitBind( valueSrc, varNames )
		
	

	
	
		
def _buildMatchForGuardList(xs):
	# guardItem*
	return _GuardMatchList( [ _buildMatchForGuardItem( x, True )   for x in xs ] )



def _buildMatchForGuardItem(xs, bInsideList=False):
	if isinstance( xs, _listType ):
		if xs[0] == ':':
			# Bind
			if len( xs ) != 3:
				raise ValueError, 'guard expressions: bind expression must take the form (: <var_name> sub_exp)'
			if xs[1][0] != '@':
				raise ValueError, 'guard expressions: variable names (to be bound) must start with @'
			varName = xs[1][1:]
			match = _buildMatchForGuardItem( xs[2], bInsideList )
			match.bindName = varName
		elif xs[0] == '-'  and  bInsideList:
			# List interior range
			if len( xs ) != 3:
				raise ValueError, 'guard expressions: list interior range expression must take the form (- <#min> <#max>)'
			if xs[1][0] != '#'  or  xs[2][0] != '#':
				raise ValueError, 'guard expressions: list interior range numbers must start with #'
			return _GuardMatchListInternal( int( xs[1][1:] ), int( xs[2][1:] ) )
		else:
			match = _buildMatchForGuardList( xs )
		return match
	else:
		if xs == '!':
			# match anything
			return _GuardMatchAnything()
		elif xs == '^':
			# match any string
			return _GuardMatchAnyString()
		elif xs == '/':
			# match any list
			return _GuardMatchAnyList()
		elif xs == '+'  and  bInsideList:
			# list interior +
			return _GuardMatchListInternal( 1, None )
		elif xs == '*'  and  bInsideList:
			# list interior *
			return _GuardMatchListInternal( 0, None )
		else:
			# match a constant
			constant = xs.replace( '!!', '!' ).replace( '^^', '^' ).replace( '//', '/' ).replace( '++', '+' ).replace( '**', '*' ).replace( '--', '-' ).replace( '::', ':' )
			return _GuardMatchString( constant )


		
def compileGuardExpression(xs, guardIndirection=[], functionName='guard', bSrc=False):
	"""
	compileGuardExpression(xs, guardIndirection=[], functionName='guard')   ->   fn, varNames
		xs is a list of guard expressions. It is of type @_listType (normally DMListInterface).
		guardIndirection is a list that specifies the indrection necessary to get the guard expression from each item in xs
			Examples:
				(guard0 guard1 ... guardN)    ->     [] (no indirection)
				((guard0 action0) (guard1 action1) ... (guardN actionN))     ->    [0] (get the first element of each item)
				((? (? ? guard0) ?) (? (? ? guard1) ?) ... (? (? ? guardN) ?))     ->     [1,2] (get the third element of the second element of each item)
		functionName is the name of the python function that will be returned by compileGuardExpression()  (fn.__name__)
		Return values:
			fn: the function that is generated by compiling the guard expression. It is of the form:
				fn(xs) -> vars, index
					where:
						xs: the list to be processed by that guard espression
					returns:
						vars: a dictionary mapping variable name (specified in the gaurd expression) to value
						index: the index of the guard expression that was matched
			varNames: a list of sets; one for each guard expression. Each set contains the names of the variables generated by the corresponding guard expression
	   
	   
		Guard expression format:
	   
			bind(x)  :=  [':' <var_name> x]  |  x
			
			guardX := guardItem
			guardItem := anything | anyString | anyList | constantString | list
			anything := bind( '!' )
			anyString := bind( '^' )
			anyList := bind( '/' )
			constantString := bind( <string> )
			list := bind( [guardItem* listInternal? guardItem*] )
			listInternal := bind( '+'  |  '*'  |  ['-' #min #max] )
			
			The characters : ! - / + * are assigned special meaning, so use :: !! -- // ++ ** to get the characters as constants
			
		Example:
			((a b c))  =>  matches (a b c)
			((a b c) (d e f)  =>  matches (a b c)  or  (d e f)
			((a ! ^ /))  =>  matches (a <anything> <any_string> <any_list>)
			((a ! ^ (x y /))  =>  matches (a <anything> <any_string> (x y <any_list>))
			((a * z))  =>  matches (a ... z)   where ... consists of 0 or more elements
			((a + z))  =>  matches (a ... z)   where ... consists of 1 or more elements
			((a (- #2 #4) z))  =>  matches (a ... z)   where ... consists of 2 to 4 elements
			((a (: @a ^) z))  =>  matches (a [a]<any_string> z)   [a] indicates that the expression that follows is bound to the variable a
			((a (: @a (i j (: @b ^))) z))  =>  matches (a [a](i j [b]<any_string>) z)   where 'a' is bound to (i j [b]<any_string>)   and 'b' is bound to the string
			((:: !! -- // ++ **))  =>  matches (: ! - / + *)
	"""
	
	pySrcHdr = 'def %s(xs):\n'  %  ( functionName, )
	
	if not isinstance( xs, _listType ):
		raise ValueError, 'need a list'
	
	
	result = []
	varNames = []
	for guard in xs:
		guardItemVarNames = set()
		for i in guardIndirection:
			guard = guard[i]
		match = _buildMatchForGuardItem( guard )
		guardItemSrc = match.emitSourceAndVarNames( 'xs', guardItemVarNames )
		result.extend( [ 'try:' ] )
		result.extend( [ '\tresult = {}' ] )
		result.extend( _indent( guardItemSrc ) )
		result.extend( [ '\treturn result' ] )
		result.extend( [ 'except GuardError:', '\tpass', '' ] )
		varNames.append( guardItemVarNames )
	result.extend( [ 'raise GuardError' ] )
	
	result = [ '\t' + x  for x in result ]
	
	src = pySrcHdr + '\n'.join( result )
	
	if bSrc:
		return src
	else:	
		lcl = { '_bind' : _bind, 'GuardError' : GuardError, '%s' % ( _listTypeSrc, )  :  _listType }
		
		exec src in lcl
		
		return lcl[functionName], varNames



import unittest
from Britefury.DocModel.DMIO import readSX

class TestCase_GuardExpression (unittest.TestCase):
	def _printSrc(self, guardSrc):
		print compileGuardExpression( readSX( guardSrc ), bSrc=True )

	def _printResult(self, guardSrc, dataSrc):
		print compileGuardExpression( readSX( guardSrc ) )[0]( readSX( dataSrc ) )

	def _guardTest(self, guardSrc, dataSrc, expected, indirection=[]):
		if expected is GuardError:
			self.failUnlessRaises( GuardError, lambda: compileGuardExpression( readSX( guardSrc ) )[0]( readSX( dataSrc ) ), )
		else:
			result = compileGuardExpression( readSX( guardSrc ), indirection )[0]( readSX( dataSrc ) )
			self.assert_( result == expected )

	def testEmpty(self):
		self._guardTest( '()', '()', GuardError )

	def testMatchAnything(self):
		self._guardTest( '(!)', 'a', {} ) 
		self._guardTest( '(!)', '(a b c)', {} ) 
	
	def testMatchAnythingBind(self):
		self._guardTest( '((: @a !))', 'a', { 'a' : 'a' } ) 
		self._guardTest( '((: @a !))', '(a b c)', { 'a' : ['a', 'b', 'c'] } ) 
	
	def testMatchAnyString(self):
		self._guardTest( '(^)', 'a', {} ) 
		self._guardTest( '(^)', '(a b c)', GuardError ) 
	
	def testMatchAnyStringBind(self):
		self._guardTest( '((: @a ^))', 'a', { 'a' : 'a' } ) 
		self._guardTest( '((: @a ^))', '(a b c)', GuardError ) 
	
	def testMatchAnyList(self):
		self._guardTest( '(/)', 'a', GuardError ) 
		self._guardTest( '(/)', '(a b c)', {} ) 
	
	def testMatchAnyListBind(self):
		self._guardTest( '((: @a /))', 'a', GuardError ) 
		self._guardTest( '((: @a /))', '(a b c)', { 'a' : ['a', 'b', 'c'] } ) 
	
	def testMatchConstantString(self):
		self._guardTest( '(abc)', 'abc', {} ) 
		self._guardTest( '(abc)', 'xyz', GuardError ) 
		self._guardTest( '(abc)', '(a b c)', GuardError ) 
	
	def testMatchConstantStringBind(self):
		self._guardTest( '((: @a abc))', 'abc', { 'a' : 'abc' } ) 
		self._guardTest( '((: @a abc))', 'xyz', GuardError ) 
		self._guardTest( '((: @a abc))', '(a b c)', GuardError ) 
		
	def testMatchFlatList(self):
		self._guardTest( '((a b c))', 'abc', GuardError ) 
		self._guardTest( '((a b c))', '(a b c)', {} ) 
		self._guardTest( '((a b c))', '(x y z)', GuardError ) 
		self._guardTest( '((a b c d))', '(a b c)', GuardError ) 
		self._guardTest( '((a b c d))', '(a b c d)', {} ) 
		
	def testMatchFlatListBind(self):
		self._guardTest( '((: @a (a b c)))', 'abc', GuardError ) 
		self._guardTest( '((: @a (a b c)))', '(a b c)', { 'a' : ['a','b','c'] } ) 
		self._guardTest( '((: @a (a b c)))', '(x y z)', GuardError ) 
		self._guardTest( '((: @a (a b c d)))', '(a b c)', GuardError ) 
		self._guardTest( '((: @a (a b c d)))', '(a b c d)', { 'a' : ['a','b','c','d'] } ) 
		
	def testMatchNestedList(self):
		self._guardTest( '((a b c (d e f)))', 'abc', GuardError ) 
		self._guardTest( '((a b c (d e f)))', '(a b c d e f)', GuardError ) 
		self._guardTest( '((a b c (d e f)))', '(a b c (d e f))', {} ) 
		self._guardTest( '((a b c (! e f)))', '(a b c (d e f))', {} ) 
		self._guardTest( '((a b c (! e f)))', '(a b c ((x y) e f))', {} ) 
		self._guardTest( '((a b c (^ e f)))', '(a b c (d e f))', {} ) 
		self._guardTest( '((a b c (^ e f)))', '(a b c ((x y) e f))', GuardError ) 
		self._guardTest( '((a b c (/ e f)))', '(a b c (d e f))', GuardError ) 
		self._guardTest( '((a b c (/ e f)))', '(a b c ((x y) e f))', {} ) 
		
	def testMatchNestedListBind(self):
		self._guardTest( '((: @a (a b c (: @b (d e f)))))', 'abc', GuardError ) 
		self._guardTest( '((: @a (a b c (: @b (d e f)))))', '(a b c d e f)', GuardError ) 
		self._guardTest( '((: @a (a b c (: @b (d e f)))))', '(a b c (d e f))', { 'a': ['a','b','c',['d','e','f']], 'b': ['d','e','f'] } ) 
		self._guardTest( '((: @a (a b c (: @b ((: @c !) e f)))))', '(a b c (d e f))', { 'a': ['a','b','c',['d','e','f']], 'b': ['d','e','f'], 'c': 'd' } ) 
		self._guardTest( '((: @a (a b c (: @b ((: @c !) e f)))))', '(a b c ((x y) e f))', { 'a': ['a','b','c',[['x','y'],'e','f']], 'b': [['x','y'],'e','f'], 'c': ['x','y'] } ) 
		self._guardTest( '((: @a (a b c (: @b ((: @c ^) e f)))))', '(a b c (d e f))', { 'a': ['a','b','c',['d','e','f']], 'b': ['d','e','f'], 'c': 'd' } ) 
		self._guardTest( '((: @a (a b c (: @b ((: @c ^) e f)))))', '(a b c ((x y) e f))', GuardError ) 
		self._guardTest( '((: @a (a b c (: @b ((: @c /) e f)))))', '(a b c (d e f))', GuardError ) 
		self._guardTest( '((: @a (a b c (: @b ((: @c /) e f)))))', '(a b c ((x y) e f))', { 'a': ['a','b','c',[['x','y'],'e','f']], 'b': [['x','y'],'e','f'], 'c': ['x','y'] } ) 
	
	def testMatchListWithInteriorStar(self):
		self._guardTest( '((a b * x y))', '(a b x y)', {} )
		self._guardTest( '((a b * x y))', '(a b i j k x y)', {} )
	
	def testMatchListWithInteriorStarBind(self):
		self._guardTest( '((a b (: @a *) x y))', '(a b x y)', { 'a': [] } )
		self._guardTest( '((a b (: @a *) x y))', '(a b i j k x y)', { 'a': ['i','j','k'] } )
		
		
	def testMatchListWithInteriorPlus(self):
		self._guardTest( '((a b + x y))', '(a b x y)', GuardError )
		self._guardTest( '((a b + x y))', '(a b i j k x y)', {} )
	
	def testMatchListWithInteriorPlusBind(self):
		self._guardTest( '((a b (: @a +) x y))', '(a b x y)', GuardError)
		self._guardTest( '((a b (: @a +) x y))', '(a b i j k x y)', { 'a': ['i','j','k'] } )

		
	def testMatchListLengths(self):
		# A: fixed length
		self._guardTest( '((: @a (a b c d)))', '(a b c d)', { 'a': ['a','b','c','d'] } )
		# B: min length = 0, max length = 3
		self._guardTest( '(((: @a (- #0 #3))))', '(a b)', { 'a': ['a','b'] } )
		self._guardTest( '(((: @a (- #0 #3))))', '()', { 'a': [] } )
		self._guardTest( '(((: @a (- #0 #3))))', '(a b c d)', GuardError )
		# C: min length > 0, no max length
		self._guardTest( '((a b (: @a *) x y))', '(a b i j k x y)', { 'a': ['i','j','k'] } )
		# D: min length = 4, max length = 6
		self._guardTest( '(((: @a (- #4 #6))))', '(a b c d e)', { 'a': ['a','b','c','d','e'] } )
		self._guardTest( '(((: @a (- #4 #6))))', '()', GuardError )
		self._guardTest( '(((: @a (- #4 #6))))', '(a b c d e f g h)', GuardError )
		self._guardTest( '((a (: @a (- #2 #4)) z))', '(a i j k z)', { 'a': ['i','j','k'] } )
		self._guardTest( '((a (: @a (- #2 #4)) z))', '(a z)', GuardError )
		self._guardTest( '((a (: @a (- #2 #4)) z))', '(a i j k l m n o z)', GuardError )
		# E: min length = 0, no max length
		self._guardTest( '(((: @a *)))', '(a b)', { 'a': ['a','b'] } )
	
		
	def testMultiBind(self):
		self._guardTest( '((a b (: @a *) x y (: @a /)))', '(a b i j k x y (i j k))', { 'a': ['i','j','k'] } )
		self._guardTest( '((a b (: @a *) x y (: @a /)))', '(a b i j k x y (m n o))', GuardError )
		self._guardTest( '((a b (: @a ^) x y (: @a ^)))', '(a b q x y q)', { 'a': 'q' } )
		self._guardTest( '((a b (: @a ^) x y (: @a ^)))', '(a b q x y w)', GuardError )
		self._guardTest( '((a b (: @a !) x y (: @a !)))', '(a b q x y q)', { 'a': 'q' } )
		self._guardTest( '((a b (: @a !) x y (: @a !)))', '(a b q x y (i j))', GuardError )
		self._guardTest( '((a b (: @a !) x y (: @a !)))', '(a b (i j) x y q)', GuardError )
		self._guardTest( '((a b (: @a !) x y (: @a !)))', '(a b (i j) x y (i j))', { 'a': ['i','j'] } )
		
	def testMultiGuard(self):
		self._guardTest( '((a (: @a ^))  (b (: @b ^))  (c (: @c ^))  (d (: @d ^)))', '(a x)', { 'a' : 'x' } )
		self._guardTest( '((a (: @a ^))  (b (: @b ^))  (c (: @c ^))  (d (: @d ^)))', '(b x)', { 'b' : 'x' } )
		self._guardTest( '((a (: @a ^))  (b (: @b ^))  (c (: @c ^))  (d (: @d ^)))', '(c x)', { 'c' : 'x' } )
		self._guardTest( '((a (: @a ^))  (b (: @b ^))  (c (: @c ^))  (d (: @d ^)))', '(d x)', { 'd' : 'x' } )
		self._guardTest( '((a (: @a ^))  (b (: @b ^))  (c (: @c ^))  (d (: @d ^)))', '(e x)', GuardError )
		
	def testIndirection(self):
		self._guardTest( '(((a (: @a ^)))  ((b (: @b ^)))  ((c (: @c ^)))  ((d (: @d ^))))', '(a x)', { 'a' : 'x' }, [0] )
		
	def testVarNames(self):
		result = compileGuardExpression( readSX( '((a (: @foo !) (: @bar ^) (: @doh /) (: @re *)))' ) )[1]
		self.assert_( result[0] == set( [ 'foo', 'bar', 'doh', 're' ] ) )

	def testSpecials(self):
		self._guardTest( '((:: !! -- // ++ **))', '(: ! - / + *)', {} )
		self._guardTest( '((::a:: !!a!! --a-- //a// ++a++ **a**))', '(:a: !a! -a- /a/ +a+ *a*)', {} )


		
if __name__ == '__main__':
	unittest.main()
