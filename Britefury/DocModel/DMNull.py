##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.FileIO.IOXml import ioObjectFactoryRegister, ioReadObjectFromString, ioWriteObjectAsString

from Britefury.DocModel.DMNode import DMNode



class DMNull (DMNode):
	def __readxml__(self, xmlNode):
		pass


	def __writexml__(self, xmlNode):
		pass


	def __cmp__(self, x):
		return isinstance( x, DMNull )





ioObjectFactoryRegister( 'DMNull', DMNull )









import unittest



class TestCase_Null (unittest.TestCase):
	def testStringCtor(self):
		x = DMNull()


	def testIOXml(self):
		x = DMNull()

		s = ioWriteObjectAsString( x )
		y = ioReadObjectFromString( s )

		self.assert_( x == y )



if __name__ == '__main__':
	unittest.main()
