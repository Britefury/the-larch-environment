##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.FileIO.IOXml import ioXmlReadStringProp, ioXmlWriteStringProp, ioObjectFactoryRegister, ioReadObjectFromString, ioWriteObjectAsString

from Britefury.DocModel.DMAtom import DMAtom



class DMString (DMAtom):
	__slots__ = [ '_value', '_format' ]

	formatInt = 'i'
	formatLong = 'l'
	formatFloat = 'f'
	formatSingle = "'"
	formatDouble = '"'


	def __init__(self, value='', format="'"):
		self._value = value
		self._format = format



	def __writecontentsx__(self, stream, nodeToIndex):
		if self._format == self.formatInt  or  self._format == self.formatFloat  or  self._format == self.formatLong:
			stream.write( self._value )
		else:
			s = self._value
			s = s.replace( '"', '\\"' ).replace( "'", "\\'" ).replace( '\\', '\\\\' )
			stream.write( self._format + s + self._format )


	def __readxml__(self, xmlNode):
		if xmlNode.isValid():
			self._value = ioXmlReadStringProp( xmlNode.property( 'value' ), '_' )
		else:
			self._value = '_'


	def __writexml__(self, xmlNode):
		if xmlNode.isValid():
			ioXmlWriteStringProp( xmlNode.property( 'value' ), self._value )


	def __cmp__(self, x):
		if isinstance( x, DMString ):
			return cmp( self._value, x._value )
		else:
			return cmp( self._value, x )

	def __str__(self):
		if self._format == self.formatInt  or  self._format == self.formatFloat  or  self._format == self.formatLong:
			return self._value
		else:
			return self._format + self._value + self._format



	def getValue(self):
		return self._value


	value = property( getValue )



ioObjectFactoryRegister( 'DMString', DMString )









import unittest



class TestCase_String (unittest.TestCase):
	def testStringCtor(self):
		x = DMString( 'x' )

		self.assert_( x.getValue() == 'x' )
		self.assert_( x.value == 'x' )


	def testIOXml(self):
		x = DMString( 'x' )

		s = ioWriteObjectAsString( x )
		y = ioReadObjectFromString( s )

		self.assert_( x.value == y.value )



if __name__ == '__main__':
	unittest.main()
