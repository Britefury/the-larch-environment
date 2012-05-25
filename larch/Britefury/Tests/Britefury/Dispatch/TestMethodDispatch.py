##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import unittest

from Britefury.Dispatch.MethodDispatch import DispatchError, ObjectDispatchMethod, DMObjectNodeDispatchMethod, methodDispatch
from BritefuryJ.DocModel import DMSchema


class TestCase_objectMethodDispatch (unittest.TestCase):
	schema = DMSchema( 'MethodDispatch', 'dispatch', 'Britefury.Tests.Dispatch.TestMethodDispatch' )
	DMA = schema.newClass( 'A', [ 'x', 'y' ] )
	DMB = schema.newClass( 'B', DMA, [ 'p', 'q' ] )
	DMC = schema.newClass( 'C', [ 's', 't' ] )


	class A (object):
		pass

	class B (A):
		pass

	class C (A):
		pass

	class D (B, C):
		pass

	def setUp(self):
		class DispatchX (object):
			__dispatch_num_args__ = 0

			@ObjectDispatchMethod( self.A )
			def xA(self, node):
				return 'a'

			@ObjectDispatchMethod( self.B )
			def xB(self, node):
				return 'b'

			@ObjectDispatchMethod( self.C )
			def xC(self, node):
				return 'c'

			@ObjectDispatchMethod( self.D )
			def xD(self, node):
				return 'd'

			@DMObjectNodeDispatchMethod( self.DMA )
			def dmA(self, node, x, y):
				return x + y


		class DispatchY (object):
			__dispatch_num_args__ = 0

			@ObjectDispatchMethod( self.A )
			def yA(self, node):
				return 'a'

			@ObjectDispatchMethod( self.B )
			def yB(self, node):
				return 'b'

		class DispatchZ (object):
			__dispatch_num_args__ = 0

			@ObjectDispatchMethod( self.A )
			def zA(self, node):
				return 'a'

			@ObjectDispatchMethod( self.C )
			def zC(self, node):
				return 'c'

		class DispatchW (object):
			__dispatch_num_args__ = 0

		class DispatchMulti (object):
			__dispatch_num_args__ = 0

			@ObjectDispatchMethod( self.B, self.C )
			def zA(self, node):
				return 'bc'


		self.DispatchX = DispatchX
		self.DispatchY = DispatchY
		self.DispatchZ = DispatchZ
		self.DispatchW = DispatchW
		self.DispatchMulti = DispatchMulti

		self.a = self.A()
		self.b = self.B()
		self.c = self.C()
		self.d = self.D()


		self.dma = self.DMA( x='a', y='b' )
		self.dmb = self.DMB( x='a', y='b', p='c', q='d' )
		self.dmc = self.DMC( s='a', t='b' )





	def tearDown(self):
		self.a = None
		self.b = None
		self.c = None
		self.d = None
		self.dma = None
		self.dmb = None
		self.dmc = None


	def testDispatch(self):
		x = self.DispatchX()
		self.assert_( methodDispatch( x, self.a )  ==  'a' )
		self.assert_( methodDispatch( x, self.b )  ==  'b' )
		self.assert_( methodDispatch( x, self.c )  ==  'c' )
		self.assert_( methodDispatch( x, self.d )  ==  'd' )
		self.assert_( methodDispatch( x, self.dma )  ==  'ab' )
		self.assert_( methodDispatch( x, self.dmb )  ==  'ab' )

	def testDispatchSubclass(self):
		x = self.DispatchX()
		y = self.DispatchY()
		z = self.DispatchZ()
		self.assert_( methodDispatch( y, self.d )  ==  'b' )
		self.assert_( methodDispatch( z, self.d )  ==  'c' )
		self.assert_( methodDispatch( x, self.dma )  ==  'ab' )
		self.assert_( methodDispatch( x, self.dmb )  ==  'ab' )

	def testDispatchNoClass(self):
		w = self.DispatchW()
		self.assertRaises( DispatchError, lambda: methodDispatch( w, self.c ) )
		self.assertRaises( DispatchError, lambda: methodDispatch( w, self.dmc ) )

	def testDispatchMulti(self):
		m = self.DispatchMulti()
		self.assertRaises( DispatchError, lambda: methodDispatch( m, self.a ) )
		self.assert_( methodDispatch( m, self.b )  ==  'bc' )
		self.assert_( methodDispatch( m, self.c )  ==  'bc' )
		self.assert_( methodDispatch( m, self.d )  ==  'bc' )
