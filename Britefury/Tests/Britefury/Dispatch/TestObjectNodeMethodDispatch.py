##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import unittest

from Britefury.Dispatch.Dispatch import DispatchError
from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeDispatchMethod, ObjectNodeMethodDispatchMetaClass, objectNodeMethodDispatch
from BritefuryJ.DocModel import DMModule


class TestCase_objectNodeMethodDispatch (unittest.TestCase):
	def setUp(self):
		self.Module = DMModule( 'NodeMethodDispatch', 'dispatch', 'Tests.NodeMethodDispatch' )
		self.A = self.Module.newClass( 'A', [ 'x', 'y' ] )
		self.B = self.Module.newClass( 'B', self.A, [ 'p', 'q' ] )
		self.C = self.Module.newClass( 'C', [ 's', 't' ] )
		self.a = self.A( x='a', y='b' )
		self.b = self.B( x='a', y='b', p='c', q='d' )
		self.c = self.C( s='a', t='b' )
	
		class DispatchTest (object):
			__metaclass__ = ObjectNodeMethodDispatchMetaClass
			__dispatch_num_args__ = 0
			
			@ObjectNodeDispatchMethod( self.A )
			def A(self, node, x, y):
				return x + y
		
			#def B(self, node, x, y, p, q):
			#	return x + y + p + q
		
		self.DispatchTest = DispatchTest
		
		
	def tearDown(self):
		self.a = None
		
		
	def testDispatch(self):
		d = self.DispatchTest()
		self.assert_( objectNodeMethodDispatch( d, self.a )  ==  'ab' )

	def testDispatchSubclass(self):
		d = self.DispatchTest()
		self.assert_( objectNodeMethodDispatch( d, self.b )  ==  'ab' )

	def testDispatchNoClass(self):
		d = self.DispatchTest()
		self.assertRaises( DispatchError, lambda: objectNodeMethodDispatch( d, self.c ) )
