##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.FileIO.IOXml import ioXmlReadStringProp, ioXmlWriteStringProp, ioObjectFactoryRegister, ioReadObjectFromString, ioWriteObjectAsString

from Britefury.DocModel.DMNode import DMNode




def needsQuotes(s):
	return ' ' in s  or  '\t' in s  or  '\n' in s  or  '\r' in s  or  '\x0b' in s  or  '\x0c' in s  or  '(' in s  or  ')' in s  or  '`' in s  or  '{' in s  or  '}' in s  or  '\'' in s

def escape(s):
	return s.replace( '\'', '\\\'' ).replace( '"', '\\"' ).replace( '\\', '\\\\' ).replace( '\t', '\\t' ).replace( '\n', '\\n' ).replace( '\r', '\\r' ).replace( '\x0b', '\\x0b' ).replace( '\x0c', '\\x0c' )

def unescape(s):
	return s.replace( '\\\'', '\'' ).replace( '\\"', '"' ).replace( '\\t', '\t' ).replace( '\\n', '\n' ).replace( '\\r', '\r' ).replace( '\\x0b', '\x0b' ).replace( '\\x0c', '\x0c' ).replace( '\\', '\\\\' )



class DMString (DMNode):
	__slots__ = [ '_value', '_format' ]

	def __init__(self, value='_'):
		self._value = value



	def __writecontentsx__(self, stream, nodeToIndex):
		s = self._value
		if _needsQuotes( self._value ):
			stream.write( escape( self._value ) )
		else:
			stream.write( self._value )


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


	def __hash__(self):
		return hash( self._name )


	def __str__(self):
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



	def _testEscape(self, s, s2):
		self.assert_( escape( s ) == s2 )

	def testEscape(self):
		self._testEscape( 'a', 'a' )
		self._testEscape( """\t\n""", """\\t\\n""" )



if __name__ == '__main__':
	unittest.main()
