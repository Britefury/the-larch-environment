##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

from Britefury.Dispatch.Dispatch import DispatchError


class Transformation (object):
	def __init__(self, identity, xforms):
		self._identity = identity
		self._xforms =  xforms
		
		
	def __call__(self, node, *args):
		a = _TransformApplication( self._identity, self._xforms )
		return a( node )
	
	
	
	
class _TransformApplication (object):
	def __init__(self, identity, xforms):
		self._identity = identity
		self._xforms = xforms
		self._stack = [ False ]
		
		
	def __call__(self, node, *args):
		self._stack.append( False )
		
		bTransformed = False
		for x in self._xforms:
			try:
				node = x( node, self )
				bTransformed = True
			except DispatchError:
				pass
				
		
		if bTransformed:
			# Content modified, set the outer stack entry to True
			self._stack[-2] = True
			self._stack.pop()
			return node
		else:
			transformedNode = self._identity( node, self )
			if self._stack[-1]:
				# Inner application transformed the content
				self._stack.pop()
				# Propagate; outer applications should use the identity transform too
				if len( self._stack ) > 0:
					self._stack[-1] = True
				return transformedNode
			else:
				# Unmodified tree
				self._stack.pop()
				return node
			
		

		
	


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
		
		
		
	class TestTransformation1 (TransformationInterface):
		def stringNode(self, xform, node, x, y):
			return [ 'stringNode', x + 'jk', xform( y ) ]

	class TestTransformation2 (TransformationInterface):
		def stringNode(self, xform, node, x, y):
			return [ 'stringNode', x + 'pq', xform( y ) ]

	class TestTransformation3 (TransformationInterface):
		def stringNode(self, xform, node, x, y):
			if x.startswith( 'x' ):
				return [ 'stringNode', 'pq' + x, xform( y ) ]
			else:
				raise DispatchError

		
		
	def setUp(self):
		self.data_s = [ 'twoStrings', 'a', 'b' ]
		
		self.data_nss = [ 'twoNodes', [ 'twoStrings', 'a', 'b' ], [ 'twoStrings', 'c', 'd' ] ]

		self.data_bs = [ 'stringNode', 'a', [ 'twoStrings', 'b', 'c' ] ]
		self.data_bs_x1 = [ 'stringNode', 'ajk', [ 'twoStrings', 'b', 'c' ] ]
		self.data_bs_x2 = [ 'stringNode', 'apq', [ 'twoStrings', 'b', 'c' ] ]
		self.data_bs_x12 = [ 'stringNode', 'ajkpq', [ 'twoStrings', 'b', 'c' ] ]
		
		self.data_nbss = [ 'twoNodes', [ 'stringNode', 'a', [ 'twoStrings', 'b', 'c' ] ], [ 'twoStrings', 'd', 'e' ] ]
		self.data_nbss_x1 = [ 'twoNodes', [ 'stringNode', 'ajk', [ 'twoStrings', 'b', 'c' ] ], [ 'twoStrings', 'd', 'e' ] ]
		self.data_nbss_x2 = [ 'twoNodes', [ 'stringNode', 'apq', [ 'twoStrings', 'b', 'c' ] ], [ 'twoStrings', 'd', 'e' ] ]
		self.data_nbss_x12 = [ 'twoNodes', [ 'stringNode', 'ajkpq', [ 'twoStrings', 'b', 'c' ] ], [ 'twoStrings', 'd', 'e' ] ]
		
		
	def test_test_identity(self):
		ix = self.IdentityTransformation()
		x = ix.__apply__
		
		self.assert_( x( self.data_s )  is  self.data_s )
		self.assert_( x( self.data_nss )  is not  self.data_nss )
		self.assert_( x( self.data_nss )  ==  self.data_nss )
		self.assert_( x( self.data_bs )  is not  self.data_bs )
		self.assert_( x( self.data_bs )  ==  self.data_bs )

		
	def test_test_xform1(self):
		ix = self.IdentityTransformation()
		xx1 = self.TestTransformation1()
		xf = Transformation( ix, [ xx1 ] )
		
		self.assert_( xf( self.data_s )  is  self.data_s )
		self.assert_( xf( self.data_nss )  is  self.data_nss )
		self.assert_( xf( self.data_bs )  is not  self.data_bs )
		self.assert_( xf( self.data_bs )  ==  self.data_bs_x1 )
		self.assert_( xf( self.data_nbss )  is not  self.data_nbss )
		self.assert_( xf( self.data_nbss )  ==  self.data_nbss_x1 )

		
	def test_test_xform2(self):
		ix = self.IdentityTransformation()
		xx2 = self.TestTransformation2()
		xf = Transformation( ix, [ xx2 ] )
		
		self.assert_( xf( self.data_s )  is  self.data_s )
		self.assert_( xf( self.data_nss )  is  self.data_nss )
		self.assert_( xf( self.data_bs )  is not  self.data_bs )
		self.assert_( xf( self.data_bs )  ==  self.data_bs_x2 )
		self.assert_( xf( self.data_nbss )  is not  self.data_nbss )
		self.assert_( xf( self.data_nbss )  ==  self.data_nbss_x2 )

		
	def test_test_xform12(self):
		ix = self.IdentityTransformation()
		xx1 = self.TestTransformation1()
		xx2 = self.TestTransformation2()
		xf = Transformation( ix, [ xx1, xx2 ] )
		
		self.assert_( xf( self.data_s )  is  self.data_s )
		self.assert_( xf( self.data_nss )  is  self.data_nss )
		self.assert_( xf( self.data_bs )  is not  self.data_bs )
		self.assert_( xf( self.data_bs )  ==  self.data_bs_x12 )
		self.assert_( xf( self.data_nbss )  is not  self.data_nbss )
		self.assert_( xf( self.data_nbss )  ==  self.data_nbss_x12 )
		
		
	def test_propagation(self):
		"""
		Test propagation:
		When:
		   - the original data is a nested node, 3 levels deep: Node1( Node2( focusNode( innerNode() ) ) )
		   - we perform a transformation that affects only 'focusNode'
		Ensure that the result is what is expected:
		   - the root node is not the same object (by identity), although the contents are equal, all the way down to the transformed version of 'focusNode'
		   - Result should be Node1'( Node2'( focusNode'( innerNode() ) ) )
		"""
		
		ix = self.IdentityTransformation()
		xx1 = self.TestTransformation3()
		xf = Transformation( ix, [ xx1 ] )

		data = [ 'stringNode', 'a', [ 'stringNode', 'b', [ 'stringNode', 'x', [ 'twoStrings', 'a', 'b' ] ] ] ]
		result = [ 'stringNode', 'a', [ 'stringNode', 'b', [ 'stringNode', 'pqx', [ 'twoStrings', 'a', 'b' ] ] ] ]
		dataXf = xf( data )
		self.assert_( dataXf is not data )
		self.assert_( dataXf  ==  result )
		
