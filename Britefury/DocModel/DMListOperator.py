##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Kernel.Abstract import abstractmethod





class DMListOperator (object):
	def __init__(self):
		pass


	@abstractmethod
	def __call__(self):
		pass



	@abstractmethod
	def append(self, x):
		pass


	@abstractmethod
	def extend(self, x):
		pass


	@abstractmethod
	def __setitem__(self, i, x):
		pass






class DMListOpMap (object):
	def __init__(self, src, f, invF):
		self._src = src
		self._f = f
		self._invF = invF


	def __call__(self):
		return [ self._f( x )   for x in self._src ]


	def append(self, x):
		self._src.append( self._invF( x ) )

	def extend(self, x):
		self._src.extend( [ self._invF( p )   for p in x ] )

	def __setitem__(self, i, x):
		self._src[i] = [ self._invF( p )   for p in x ]



import unittest



class TestCase_String (unittest.TestCase):
	def testStringCtor(self):
		x = DMString( 'x' )

		self.assert_( x.getValue() == 'x' )
		self.assert_( x.value == 'x' )



if __name__ == '__main__':
	unittest.main()
