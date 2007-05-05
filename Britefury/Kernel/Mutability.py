##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
_immutableValueTypes = set()

def registerImmutableValueType(valueType):
	_immutableValueTypes.add( valueType )

def isValueImmutable(value):
	return type( value ) in _immutableValueTypes



registerImmutableValueType( bool )
registerImmutableValueType( int )
registerImmutableValueType( float )
registerImmutableValueType( str )
