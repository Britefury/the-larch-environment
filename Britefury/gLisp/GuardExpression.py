##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

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
_listType = list
_listTypeSrc = 'list'


class BindError (Exception):
	pass

def _bind(result, name, value):
	existingValue = result.setdefault( name, value )
	if existingValue is not value:
		raise BindError
	
		
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
			'\traise BindError'
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
			'\traise BindError'
			]
	else:
		# constant or var or ignore
		if xs[0] == '!':
			#stringVar
			return [
				'if isinstance( %s, %s ):'  %  ( inputName, _stringTypeSrc ),
				'\t_bind( result, \'%s\', %s )'  %  ( xs[1:], inputName ),
				'else:',
				'\traise BindError'
			]
		elif xs[0] == '*':
			#listVar
			return [
				'if isinstance( %s, %s ):'  %  ( inputName, _listTypeSrc ),
				'\t_bind( result, \'%s\', %s )'  %  ( xs[1:], inputName ),
				'else:',
				'\traise BindError'
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
				'\traise BindError'
			]

		
def compileGuardExpression(xs, functionName='guard'):
	pySrcHdr = 'def %s(xs):\n'  %  ( functionName, )
	
	
	result = []
	for guard in xs:
		result.extend( [ 'try:' ] )
		result.extend( [ '\tresult = {}' ] )
		result.extend( [ '\t' + x   for x in _compileGuardItem( guard, 'xs' ) ] )
		result.extend( [ '\treturn result' ] )
		result.extend( [ 'except BindError:', '\tpass', '' ] )
	result.extend( [ 'raise BindError' ] )
	
	result = [ '\t' + x  for x in result ]
	
	src = pySrcHdr + '\n'.join( result )
	
	lcl = { '_bind' : _bind, 'BindError' : BindError }
	
	exec src in lcl
	
	return lcl[functionName]



#print compileGuardExpression( [ 'hi', 'there', '!a', '*b', '$c', [ 'q' ], '_' ] )
#print compileGuardExpression(  )

guardExpression = [ [ 'hi', 'there', '!a', '*b', '$c', [ 'q', 'r', '!a', ':p' ], '_' ] ]
testData = [ 'hi', 'there', 'foo', [ 'x', 'y', 'z' ], [ 'abc' ], [ 'q', 'r', 'foo', 'i', 'j', [ 'k' ] ], '4' ]
testData2 = [ 'hi', 'there', 'foo', [ 'x', 'y', 'z' ], [ 'abc' ], [ 'q', 'r', 'bar', 'i', 'j', [ 'k' ] ], '4' ]
print compileGuardExpression( guardExpression )( testData )
print compileGuardExpression( guardExpression )( testData2 )


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

	