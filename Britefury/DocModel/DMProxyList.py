##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from weakref import WeakKeyDictionary
from copy import copy

from Britefury.Cell.Cell import RefCell

from Britefury.DocModel.DocModelLayer import DocModelLayer
from Britefury.DocModel.DMListInterface import DMListInterface
from Britefury.DocModel.DMLiteralList import DMLiteralList






class DMProxyList (DMListInterface):
	def __init__(self, layer, src):
		self._layer = layer
		self._src = src



	def append(self, x):
		self._src.append( x )

	def extend(self, xs):
		self._src.extend( xs )

	def insertBefore(self, before, x):
		self._src.insertBefore( before, x )

	def insertAfter(self, after, x):
		self._src.insertAfter( after, x )

	def remove(self, x):
		self._src.remove( x )

	def __setitem__(self, i, x):
		self._src[i] = x


	def __getitem__(self, i):
		return self._src[i]

	def __contains__(self, x):
		return x in self._src

	def __iter__(self):
		return iter( self._src )

	def __add__(self, xs):
		return self._src + xs

	def __len__(self):
		return len( self._src )

	def index(self, x):
		return self._src.index( x )


	def getLayer(self):
		return self._layer


	def getDestList(self, layer):
		return layer.getDestList( self )

	def getSrcList(self, layer):
		return layer.getSrcList( self )



	def __copy__(self):
		return DMProxyList( self._layer, self._src )


	def __deepcopy__(self, memo):
		return DMProxyList( self._layer, self._src )







import unittest



class TestCase_ProxyList (unittest.TestCase):
	def testProxy(self):
		layer1 = DocModelLayer()
		layer2 = DocModelLayer()
		x = DMLiteralList( layer1 )
		x.extend( [ 1, 2, 3 ] )

		y = DMProxyList( layer2, x )
		self.assert_( x[:] == [ 1, 2, 3 ] )
		self.assert_( y[:] == [ 1, 2, 3 ] )
		x[1] = 4
		self.assert_( x[:] == [ 1, 4, 3 ] )
		self.assert_( y[:] == [ 1, 4, 3 ] )
		y[1] = 5
		self.assert_( x[:] == [ 1, 5, 3 ] )
		self.assert_( y[:] == [ 1, 5, 3 ] )



1
if __name__ == '__main__':
	unittest.main()
