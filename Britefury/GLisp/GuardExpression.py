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


def _indent(self, src):
	return [ '\t' + s   for s in src ]



def _bind(result, name, value):
	existingValue = result.setdefault( name, value )
	if existingValue is not value:
		raise GuardError

	
class  _GuardMatch (object):
	def __init__(self):
		super( _GuardItem, self ).__init__()
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
			return [], set()
		


class _GuardMatchAnything (_GuardMatch):
	def emitSourceAndVarNames(self, valueSrc, varNames):
		return self._p_emitBind( valueSrc )
	
class _GuardMatchAnyString (_GuardMatch):
	def emitSourceAndVarNames(self, valueSrc, varNames):
		return [ 'if isinstance( %s, %s ):'  %  ( valueSrc, _stringTypeSrc ) ]  +  \
			_indent( self._p_emitBind( valueSrc, varNames ) )  +  \
			[ 'else:' ]  +  \
			_indent( self._p_emitMatchFailed()  )
	
class _GuardMatchAnyList (_GuardMatch):
	def emitSourceAndVarNames(self, valueSrc, varNames):
		return [ 'if isinstance( %s, %s ):'  %  ( valueSrc, _listTypeSrc ) ]  +  \
			_indent( self._p_emitBind( valueSrc, varNames ) )  +  \
			[ 'else:' ]  +  \
			_indent( self._p_emitMatchFailed()  )

class _GuardMatchString (_GuardMatch):
	def __init__(self, constant):
		super( _GuardMatchString, self ).__init__()
		self._constant = constant

	def emitSourceAndVarNames(self, valueSrc, varNames):
		return [ 'if %s == \'%s\':'  %  ( valueSrc, self._constant ) ]  +  \
			_indent( self._p_emitBind( valueSrc, varNames ) )  +  \
			[ 'else:' ]  +  \
			_indent( self._p_emitMatchFailed()  )

class _GuardMatchList (_GuardMatch):
	def __init__(self, subMatches):
		internalIndex = None
		for i, m in enumerate( subMatches ):
			if isinstance( m, _GuardMatchListInternal ):
				if internalIndex is not None:
					raise ValueError, 'only one _GuardMatchListInternal inside a guard match list'
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
		if self._minLength == self._maxLength:
			# min and max length the same; only one length permissable
			src.append( 'if len( %s )  !=  %d:'  %  ( valueSrc, self._minLength ) )
			src.append( _indent( self._p_emitMatchFailed() ) )
			src.append( '' )
		elif self._minLength == 0  and  self._maxLength is not None:
			# no min length, a max length
			src.append( 'if len( %s )  >  %d:'  %  ( valueSrc, self._maxLength ) )
			src.append( _indent( self._p_emitMatchFailed() ) )
			src.append( '' )
		elif self._minLength > 0:
			# there is a min length
			if self._maxLength is None:
				# no max length
				src.append( 'if len( %s )  <  %d:'  %  ( valueSrc, self._minLength ) )
				src.append( _indent( self._p_emitMatchFailed() ) )
				src.append( '' )
			else:
				# no max length
				src.append( 'if len( %s )  <  %d   or   len( %s )  >  %d:'  %  ( valueSrc, self._minLength, valueSrc, self._maxLength ) )
				src.append( _indent( self._p_emitMatchFailed() ) )
				src.append( '' )
				
	#guardItem*  (front)
	for i, item in enumerate( self._front ):
		itemNameSrc = '%s[%d]'  %  ( valueSrc, i )
		itemSrc = item.emitSourceAndVarNames( itemNameSrc, varNames )
		src.extend( itemSrc )
				
	#guardItem*  (back)
	for i, item in enumerate( reversed( self._back ) ):
		itemNameSrc = '%s[-%d]'  %  ( valueSrc, i )
		itemSrc = item.emitSourceAndVarNames( itemNameSrc, varNames )
		src.extend( itemSrc )
		
	if self._internal is not None:
		self._internal.emitSourceAndVarNames( '%s[%d:-%d]'  %  ( valueSrc, len( self._front ), len( self._back ) ), varNames )

		
		
class _GuardMatchListInternal (_GuardMatch):
	def __init__(self, min=0, max=None):
		super( _GuardMatchListInternal, self ).__init__()
		self.min = min
		self.max = max
		
	def emitSourceAndVarNames(self, valueSrc, varNames):
		self._p_emitBind( valueSrc, varNames )
		
	

"""
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
"""
	
	
		
def _buildMatchForGuardList(xs):
	def _processItem(xs):
		if xs == '+':
			return _GuardMatchListInternal( 1, None )
		elif xs == '*':
			return _GuardMatchListInternal( 0, None )
		elif isinstance( xs, _listTypeSrc )  and  len( xs ) == 3  and  xs[0] == '-':
			if xs[1][0] != '#'  or  xs[2][0] != '#':
				raise ValueError, 'range numbers must start with #'
			return _GuardMatchListInternal( int( xs[1][1:] ), int( xs[2][1:] ) )
		else:
			return _buildMatchForGuardItem( xs )
	
	# guardItem*
	return _GuardMatchList( [ _processItem( x )   for x in xs ] )



def _buildMatchForGuardItem(xs):
	if isinstance( xs, _listType ):
		if xs[0] == ':':
			# Bind
			if xs[1][0] != '@':
				raise ValueError, 'variable names (to be bound) must start with @'
			varName = xs[1][1:]
			match = _buildMatchForGuardItem( xs[2] )
			match.bindName = varName
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
		else:
			# match a constant
			constant = xs.replace( '!!', '!' ).replace( '^^', '^' ).replace( '//', '/' ).replace( '++', '+' ).replace( '**', '*' ).replace( '--', '-' ).replace( '::', ':' )
			return _GuardMatchString( constant )


		
def compileGuardExpression(xs, guardIndirection=[], functionName='guard'):
	"""compileGuardExpression(xs, guardIndirection=[], functionName='guard')   ->   fn, varNames
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
	   
	    where:
		guardX := guardItem
		guardItem := ignore | constant | var | guardList
		ignore := '_'
		constant := <string>
		var := stringVar | listVar | anyVar
		stringVar := !<name>
		listVar := '*'<name>
		anyVar := '$'<name>
		guardList := (guardItem* listRemainderVar?)
		listRemainderVar := ':'<name>"""
	pySrcHdr = 'def %s(xs):\n'  %  ( functionName, )
	
	if not isinstance( xs, _listType ):
		raise ValueError, 'need a list'
	
	
	result = []
	varNames = []
	for guard in xs:
		for i in guardIndirection:
			guard = guard[i]
		guardItemSrc, guardItemVarNames = _compileGuardItem( guard, 'xs' )
		result.extend( [ 'try:' ] )
		result.extend( [ '\tresult = {}' ] )
		result.extend( [ '\t' + x   for x in guardItemSrc ] )
		result.extend( [ '\treturn result' ] )
		result.extend( [ 'except GuardError:', '\tpass', '' ] )
		varNames.append( guardItemVarNames )
	result.extend( [ 'raise GuardError' ] )
	
	result = [ '\t' + x  for x in result ]
	
	src = pySrcHdr + '\n'.join( result )
	
	lcl = { '_bind' : _bind, 'GuardError' : GuardError, '%s' % ( _listTypeSrc, )  :  _listType }
	
	exec src in lcl
	
	return lcl[functionName], varNames



import unittest
from Britefury.DocModel.DMIO import readSX

class TestCase_GuardExpression (unittest.TestCase):
	def _guardTest(self, guardSrc, dataSrc, expected, indirection=[]):
		if expected is GuardError:
			self.failUnlessRaises( GuardError, lambda: compileGuardExpression( readSX( guardSrc ) )[0]( readSX( dataSrc ) ), )
		else:
			result = compileGuardExpression( readSX( guardSrc ), indirection )[0]( readSX( dataSrc ) )
			self.assert_( result == expected )

	def testEmpty(self):
		self._guardTest( '()', '()', GuardError )

	def testIgnore(self):
		self._guardTest( '(_)', 'a', {} ) 
		self._guardTest( '(_)', 'b', {} ) 
	
	def testConstant(self):
		self._guardTest( '(a)', 'a', {} ) 
		self._guardTest( '(a)', 'b', GuardError ) 
	
	def testStringVar(self):
		self._guardTest( '(!a)', 'test', { 'a' : 'test' } ) 
		self._guardTest( '(!a)', '(a b c)', GuardError ) 

	def testListVar(self):
		self._guardTest( '(*a)', '(a b c)', { 'a' : ['a', 'b', 'c'] } ) 
		self._guardTest( '(*a)', 'test', GuardError ) 

	def testAnyVar(self):
		self._guardTest( '($a)', 'test', { 'a' : 'test' } ) 
		self._guardTest( '($a)', '(a b c)', { 'a' : ['a', 'b', 'c'] } )
		
	def testList(self):
		self._guardTest( '((a b c))', '(a b c)', {} ) 
		self._guardTest( '((a b c))', '(a b c d)', GuardError ) 

	def testListWithVars(self):
		self._guardTest( '((a !foo *bar $doh))', '(a b (c d e) f)', { 'foo' : 'b', 'bar' : [ 'c', 'd', 'e' ], 'doh' : 'f' } ) 
		self._guardTest( '((a !foo *bar $doh))', '(a b (c d e) (f g h))', { 'foo' : 'b', 'bar' : [ 'c', 'd', 'e' ], 'doh' : [ 'f', 'g', 'h' ] } ) 

	def testListRemainder(self):
		self._guardTest( '((a !foo *bar $doh :re))', '(a b (c d e) f g h (i j k) l)', { 'foo' : 'b', 'bar' : [ 'c', 'd', 'e' ], 'doh' : 'f', 're' : [ 'g', 'h', [ 'i', 'j', 'k' ], 'l' ] } )
		self._guardTest( '((a !foo *bar $doh :re))', '(a b (c d e) f g h (i j k) l m)', { 'foo' : 'b', 'bar' : [ 'c', 'd', 'e' ], 'doh' : 'f', 're' : [ 'g', 'h', [ 'i', 'j', 'k' ], 'l', 'm' ] } )
		
	def testMultiBind(self):
		self._guardTest( '((a !foo (x y !foo z w)))', '(a b (x y b z w))', { 'foo' : 'b' } )
		self._guardTest( '((a !foo (x y !foo z w)))', '(a b (x y q z w))', GuardError )
		
	def testMultiGuard(self):
		self._guardTest( '((a !a) (b !b) (c !c) (d !d))', '(a x)', { 'a' : 'x' } )
		self._guardTest( '((a !a) (b !b) (c !c) (d !d))', '(b x)', { 'b' : 'x' } )
		self._guardTest( '((a !a) (b !b) (c !c) (d !d))', '(c x)', { 'c' : 'x' } )
		self._guardTest( '((a !a) (b !b) (c !c) (d !d))', '(d x)', { 'd' : 'x' } )
		self._guardTest( '((a !a) (b !b) (c !c) (d !d))', '(e x)', GuardError )
		
	def testIndirection(self):
		self._guardTest( '(((a !a)) ((b !b)))', '(a x)', { 'a' : 'x' }, [0] )
		
	def testVarNames(self):
		result = compileGuardExpression( readSX( '((a !foo *bar $doh :re))' ) )[1]
		self.assert_( result[0] == set( [ 'foo', 'bar', 'doh', 're' ] ) )



if __name__ == '__main__':
	unittest.main()
