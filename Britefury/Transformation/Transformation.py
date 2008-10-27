##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


class Transformation (object):
	def __init__(self, identity, xform):
		self._identity = identity
		
		
	def __call__(self, node, xform):
		return self._identity( xform, node )
	
	def __apply__(self, node):
		return self._identity.__apply__( node )



import unittest
from Britefury.Dispatch.Dispatch import Dispatcher
from Britefury.Dispatch.MethodDispatch import methodDispatch
from Britefury.Transformation.TransformationInterface import TransformationInterface


class TestCase_Transformation (unittest.TestCase):
	class IdentityTransformation (TransformationInterface):
		def twoStrings(self, xform, node, x, y):
			return node
		
		def twoNodes(self, xform, node, x, y):
			return [ 'twoNodes', xform( x ), xform( y ) ]
		
		def stringNode(self, xform, node, x, y):
			return [ 'stringNode', x, xform( y ) ]
		
		
		
	class TestTransformation (TransformationInterface):
		def stringNode(self, xform, node, x, y):
			return [ 'stringNode', x + 'jk', xform( y ) ]

		
		
	def setUp(self):
		self.data_ss = [ 'twoStrings', 'a', 'b' ]
		self.data_nn = [ 'twoNodes', [ 'twoStrings', 'a', 'b' ], [ 'twoStrings', 'c', 'd' ] ]
		self.data_sn = [ 'stringNode', 'a', [ 'twoStrings', 'b', 'c' ] ]

		self.data_sn_x = [ 'stringNode', 'ajk', [ 'twoStrings', 'b', 'c' ] ]

		
		
		
	def test_test_identity(self):
		ix = self.IdentityTransformation()
		x = ix.__apply__
		
		self.assert_( x( self.data_ss )  is  self.data_ss )
		self.assert_( x( self.data_nn )  is not  self.data_nn )
		self.assert_( x( self.data_nn )  ==  self.data_nn )
		self.assert_( x( self.data_sn )  is not  self.data_sn )
		self.assert_( x( self.data_sn )  ==  self.data_sn )

		
	def test_test_xform(self):
		ix = self.IdentityTransformation()
		xx = self.TestTransformation()
		d = Dispatcher( [ xx, ix ] )
		
		def transform(node):
			return d( node, transform )
		
		self.assert_( transform( self.data_ss )  is  self.data_ss )
		self.assert_( transform( self.data_nn )  is not  self.data_nn )
		self.assert_( transform( self.data_nn )  ==  self.data_nn )
		self.assert_( transform( self.data_sn )  is not  self.data_sn )
		self.assert_( transform( self.data_sn )  ==  self.data_sn_x )
