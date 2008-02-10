##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

from Britefury.DocModel.DMListInterface import DMListInterface

#
# Guard expression format:
#
# ( guard0 guard1 ... guardN )
#
# where:
#     guardX := guardItem
#     guardItem := ignore | constant | var | guardList
#     ignore := '_'
#     constant := <string>
#     var := stringVar | listVar | anyVar
#     stringVar := !<name>
#     listVar := '*'<name>
#     anyVar := '$'<name>
#     guardList := (guardItem* listRemainderVar?)
#     listRemainderVar := ':'<name>

# compileGuardExpression will return a function f:
# f(xs) -> result
#  where xs is the input list
#   and result is a dictionary mapping variable names to values


_stringType = str
_stringTypeSrc = 'str'
_listType = DMListInterface
_listTypeSrc = 'DMListInterface'


class GuardError (Exception):
	pass

def _bind(result, name, value):
	existingValue = result.setdefault( name, value )
	if existingValue is not value:
		raise GuardError
	
		
def _compileGuardList(xs, inputName):
	result = []	

	#detect listRemainderVar
	last = None
	if xs[-1][0] == ':':
		last = -1
		
	#check length
	if last is None:
		#no remainder variable; fixed length
		result.extend( [
			'if len( %s ) != %d:'  %  ( inputName, len( xs ) ),
			'\traise GuardError'
			] )
		
	#guardItem*
	for i, item in enumerate( xs[:last] ):
		itemName = '%s[%d]'  %  ( inputName, i )
		result.extend( _compileGuardItem( item, itemName ) )

	#listRemainderVar
	if last == -1:
		result.extend( [ '_bind( result, \'%s\', %s[%d:] )'  %  ( xs[-1][1:], inputName, len( xs ) - 1 ) ] )

	return result
		

def _compileGuardItem(xs, inputName):
	if isinstance( xs, _listType ):
		return [ 'if isinstance( %s, %s ):'  %  ( inputName, _listTypeSrc, ) ]  +  [ '\t' + x   for x in _compileGuardList( xs, inputName ) ] + [
			'else:',
			'\traise GuardError'
			]
	else:
		# constant or var or ignore
		if xs[0] == '!':
			#stringVar
			return [
				'if isinstance( %s, %s ):'  %  ( inputName, _stringTypeSrc ),
				'\t_bind( result, \'%s\', %s )'  %  ( xs[1:], inputName ),
				'else:',
				'\traise GuardError'
			]
		elif xs[0] == '*':
			#listVar
			return [
				'if isinstance( %s, %s ):'  %  ( inputName, _listTypeSrc ),
				'\t_bind( result, \'%s\', %s )'  %  ( xs[1:], inputName ),
				'else:',
				'\traise GuardError'
			]
		elif xs[0] == '$':
			#anyVar
			return [ '_bind( result, \'%s\', %s )'  %  ( xs[1:], inputName )
			 ]
		elif xs == '_':
			#ignore
			return []
		else:
			#constant
			return [
				'if %s != \'%s\':'  %  ( inputName, xs ),
				'\traise GuardError'
			]

		
def compileGuardExpression(xs, functionName='guard'):
	pySrcHdr = 'def %s(xs):\n'  %  ( functionName, )
	
	if not isinstance( xs, _listType ):
		raise ValueError, 'need a list'
	
	
	result = []
	for guard in xs:
		result.extend( [ 'try:' ] )
		result.extend( [ '\tresult = {}' ] )
		result.extend( [ '\t' + x   for x in _compileGuardItem( guard, 'xs' ) ] )
		result.extend( [ '\treturn result' ] )
		result.extend( [ 'except GuardError:', '\tpass', '' ] )
	result.extend( [ 'raise GuardError' ] )
	
	result = [ '\t' + x  for x in result ]
	
	src = pySrcHdr + '\n'.join( result )
	
	lcl = { '_bind' : _bind, 'GuardError' : GuardError, '%s' % ( _listTypeSrc, )  :  _listType }
	
	exec src in lcl
	
	return lcl[functionName]



import unittest
from Britefury.DocModel.DMIO import readSX

class TestCase_GuardExpression (unittest.TestCase):
	def _guardTest(self, guardSrc, dataSrc, expected):
		if expected is GuardError:
			self.failUnlessRaises( GuardError, lambda: compileGuardExpression( readSX( guardSrc ) )( readSX( dataSrc ) ), )
		else:
			result = compileGuardExpression( readSX( guardSrc ) )( readSX( dataSrc ) )
			self.assert_( result == expected )

# ( guard0 guard1 ... guardN )
#
# where:
#     guardX := guardItem
#     guardItem := ignore | constant | var | guardList
#     ignore := '_'
#     constant := <string>
#     var := stringVar | listVar | anyVar
#     stringVar := !<name>
#     listVar := '*'<name>
#     anyVar := '$'<name>
#     guardList := (guardItem* listRemainderVar?)
#     listRemainderVar := ':'<name>

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
	


if __name__ == '__main__':
	unittest.main()
