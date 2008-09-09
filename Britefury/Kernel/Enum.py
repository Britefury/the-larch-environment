##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
class EnumClass (type):
	def __init__(cls, clsName, bases, clsDict):
		usedNames = set()

		for name, value in clsDict.items():
			if isinstance( value, int ):
				if name in usedNames:
					raise ValueError, 'value %d already assigned' % ( value, )
				usedNames.add( name )
				enum = cls( value )
				setattr( cls, name, enum )
				enum._name = name

		super( EnumClass, cls ).__init__( clsName, bases, clsDict )



class Enum (int):
	__metaclass__ = EnumClass

	def __init__(self, value):
		super( Enum, self ).__init__( value )
		self._name = None


	def __str__(self):
		return '%s.%s' % ( type( self ).__name__, self._name )




if __name__ == '__main__':
	class MyEnum (Enum):
		A = 1
		B = 2
		C = 3

	print MyEnum.A, MyEnum.B, MyEnum.C

