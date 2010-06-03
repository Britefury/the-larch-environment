##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import unittest

from Britefury.Dispatch.Dispatch import DispatchError
from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod, DMObjectNodeMethodDispatchMetaClass, dmObjectNodeMethodDispatch
from BritefuryJ.DocModel import DMSchema


class TestCase_objectNodeMethodDispatch (unittest.TestCase):
	def setUp(self):
		self.schema = DMSchema( 'NodeMethodDispatch', 'dispatch', 'Tests.NodeMethodDispatch' )
		self.A = self.schema.newClass( 'A', [ 'x', 'y' ] )
		self.B = self.schema.newClass( 'B', self.A, [ 'p', 'q' ] )
		self.C = self.schema.newClass( 'C', [ 's', 't' ] )
		self.a = self.A( x='a', y='b' )
		self.b = self.B( x='a', y='b', p='c', q='d' )
		self.c = self.C( s='a', t='b' )
	
		class DispatchTest (object):
			__metaclass__ = DMObjectNodeMethodDispatchMetaClass
			__dispatch_num_args__ = 0
			
			@DMObjectNodeDispatchMethod( self.A )
			def A(self, node, x, y):
				return x + y
		
			#def B(self, node, x, y, p, q):
			#	return x + y + p + q
		
		self.DispatchTest = DispatchTest
		
		
	def tearDown(self):
		self.a = None
		
		
	def testDispatch(self):
		d = self.DispatchTest()
		self.assert_( dmObjectNodeMethodDispatch( d, self.a )  ==  'ab' )

	def testDispatchSubclass(self):
		d = self.DispatchTest()
		self.assert_( dmObjectNodeMethodDispatch( d, self.b )  ==  'ab' )

	def testDispatchNoClass(self):
		d = self.DispatchTest()
		self.assertRaises( DispatchError, lambda: dmObjectNodeMethodDispatch( d, self.c ) )
