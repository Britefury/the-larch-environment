##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.FileIO.IOXml import ioObjectFactoryRegister

from Britefury.DocModel.DMNode import DMNode



class DMAtom (DMNode):
	pass





ioObjectFactoryRegister( 'DMAtom', DMAtom )









import unittest



class TestCase_Null (unittest.TestCase):
	def testStringCtor(self):
		x = DMAtom()



if __name__ == '__main__':
	unittest.main()
