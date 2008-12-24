##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************



class DispatchError (Exception):
	pass


class DispatchDataError (DispatchError):
	pass



class Dispatcher (object):
	def __init__(self, dispatchers):
		self._dispatchers = []
		
		for d in dispatchers:
			if isinstance( d, Dispatcher ):
				self._dispatchers.extend( d._dispatchers )
			else:
				self._dispatchers.append( d )
		
	
	def __call__(self, node, *args):
		for d in self._dispatchers:
			try:
				return d( node, *args )
			except DispatchError:
				pass
		raise DispatchError


	
	
import unittest


class TestCase_Dispatcher (unittest.TestCase):
	def _makeDispatchFunction(self, name):
		def _f(node):
			if node[0] == name:
				return node[1]
			else:
				raise DispatchError
		_f.__name__ = name
		return _f
		
		
	def testDispatcher(self):
		f = self._makeDispatchFunction( 'f' )
		g = self._makeDispatchFunction( 'g' )
		d = Dispatcher( [ f, g ] )
		
		self.assert_( d( [ 'f', 1 ] )  ==  1 )
		self.assert_( d( [ 'g', 2 ] )  ==  2 )
		self.assertRaises( DispatchError, lambda: d( [ 'h', 3 ] ) )
		

	def testNestedDispatcher(self):
		f = self._makeDispatchFunction( 'f' )
		g = self._makeDispatchFunction( 'g' )
		h = self._makeDispatchFunction( 'h' )
		d1 = Dispatcher( [ f, g ] )
		d2 = Dispatcher( [ d1, h ] )
		
		
		self.assert_( d2( [ 'f', 1 ] )  ==  1 )
		self.assert_( d2( [ 'g', 2 ] )  ==  2 )
		self.assert_( d2( [ 'h', 3 ] )  ==  3 )
		self.assertRaises( DispatchError, lambda: d2( [ 'i', 4 ] ) )
		
		self.assert_( d2._dispatchers == [ f, g, h ] )
