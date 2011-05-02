##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import unittest

from BritefuryJ.Dispatch import DispatchError
from Britefury.Dispatch.ObjectMethodDispatch import ObjectDispatchMethod, objectMethodDispatch
from BritefuryJ.DocModel import DMSchema


class TestCase_objectMethodDispatch (unittest.TestCase):
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
		
		
	def tearDown(self):
		self.a = None
		self.b = None
		self.c = None
		self.d = None
		
		
	def testDispatch(self):
		x = self.DispatchX()
		self.assert_( objectMethodDispatch( x, self.a )  ==  'a' )
		self.assert_( objectMethodDispatch( x, self.b )  ==  'b' )
		self.assert_( objectMethodDispatch( x, self.c )  ==  'c' )
		self.assert_( objectMethodDispatch( x, self.d )  ==  'd' )

	def testDispatchSubclass(self):
		y = self.DispatchY()
		z = self.DispatchZ()
		self.assert_( objectMethodDispatch( y, self.d )  ==  'b' )
		self.assert_( objectMethodDispatch( z, self.d )  ==  'c' )

	def testDispatchNoClass(self):
		w = self.DispatchW()
		self.assertRaises( DispatchError, lambda: objectMethodDispatch( w, self.c ) )

	def testDispatchMulti(self):
		m = self.DispatchMulti()
		self.assertRaises( DispatchError, lambda: objectMethodDispatch( m, self.a ) )
		self.assert_( objectMethodDispatch( m, self.b )  ==  'bc' )
		self.assert_( objectMethodDispatch( m, self.c )  ==  'bc' )
		self.assert_( objectMethodDispatch( m, self.d )  ==  'bc' )
