##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import string


from Britefury.FileIO.IOXml import ioXmlReadStringProp, ioXmlWriteStringProp, ioObjectFactoryRegister, ioReadObjectFromString, ioWriteObjectAsString

from Britefury.DocModel.DMAtom import DMAtom



class DMSymbol (DMAtom):
	__slots__ = [ '_name' ]


	symbolChars = string.ascii_letters + string.digits + '_+-*/%^&|!$@<>='



	def __init__(self, name='_'):
		self._name = intern( name )


	def __writesx__(self, stream):
		stream.write( str( self ) )


	def __readxml__(self, xmlNode):
		if xmlNode.isValid():
			self._name = ioXmlReadStringProp( xmlNode.property( 'name' ), '_' )
		else:
			self._name = '_'


	def __writexml__(self, xmlNode):
		if xmlNode.isValid():
			ioXmlWriteStringProp( xmlNode.property( 'name' ), self._name )


	def __str__(self):
		return self._name


	def __cmp__(self, x):
		if isinstance( x, DMSymbol ):
			return cmp( self._name, x._name )
		else:
			return cmp( self._name, x )


	def __hash__(self):
		return hash( self._name )



	def getName(self):
		return self._name






	name = property( getName )



ioObjectFactoryRegister( 'DMSymbol', DMSymbol )










import unittest



class TestCase_Symbol (unittest.TestCase):
	def testSymbolCtor(self):
		x = DMSymbol( 'x' )

		self.assert_( x.getName() == 'x' )
		self.assert_( x.name == 'x' )



	def testSymbolCmpSymbol(self):
		x1 = DMSymbol( 'x' )
		x2 = DMSymbol( 'x' )
		y = DMSymbol( 'y' )

		self.assert_( x1 == x2 )
		self.assert_( x1 != y )


	def testSymbolCmpStr(self):
		x = DMSymbol( 'x' )

		self.assert_( x == 'x' )
		self.assert_( x != 'y' )



	def testIOXml(self):
		x = DMSymbol( 'x' )

		s = ioWriteObjectAsString( x )
		y = ioReadObjectFromString( s )

		self.assert_( x == y )



if __name__ == '__main__':
	unittest.main()
