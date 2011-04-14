##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************

import unittest

from BritefuryJ.Isolation import IsolationBarrier, IsolationPickle


class Data (object):
	def __init__(self, x=None, y=None):
		self.x = x
		self.y = y
	
	def __str__(self):
		return 'Data( x=%s, y=%s )'  %  ( self.x, self.y )
	
	def __repr__(self):
		return 'Data( x=%s, y=%s )'  %  ( self.x, self.y )
	
	
class Test_IsolationPickle (unittest.TestCase):
	def _check(self, dataFn, testFn):
		data = dataFn()
		s = IsolationPickle.dumps( data )
		pickled = IsolationPickle.loads( s )
		testFn( pickled )
	
	
	def test_isolated_objects_share_reachable_objects(self):
		def data1():
			a = Data( 0, 1 )
			b = Data( 2, 3 )
			x = Data( a, 0 )
			y = Data( a, 1 )
			z = Data( b, 2 )
			iX = IsolationBarrier( x )
			iY = IsolationBarrier( y )
			iZ = IsolationBarrier( z )
			rootx = Data( iX, iY )
			root = Data( rootx, iZ )
			return root
		
		def testData1(root):
			rootx = root.x
			iX = rootx.x
			iY = rootx.y
			self.assertTrue( iX.value.x is iY.value.x )
		
		self._check( data1, testData1 )
			
		
		
	def test_isolated_partition_refernces_root_object(self):
		def data2():
			a = Data( 0, 1 )
			b = Data( 2, 3 )
			x = Data( a, 0 )
			y = Data( a, 1 )
			z = Data( b, 2 )
			iX = IsolationBarrier( x )
			iY = IsolationBarrier( y )
			iZ = IsolationBarrier( z )
			rootx = Data( iX, iY )
			root = Data( rootx, iZ )
			z.y = rootx
			return root
		
		def testData2(root):
			rootx = root.x
			iX = rootx.x
			iY = rootx.y
			iZ = root.y
			self.assertTrue( iX.value.x is iY.value.x )
			self.assertTrue( iZ.value.y is rootx )
		
		self._check( data2, testData2 )
			
		
		
	def test_isolated_object_directly_referenced_by_partition_of_another(self):
		def data3():
			a = Data( 0, 1 )
			b = Data( 2, 3 )
			x = Data( a, 0 )
			y = Data( a, 1 )
			z = Data( b, y )
			iX = IsolationBarrier( x )
			iY = IsolationBarrier( y )
			iZ = IsolationBarrier( z )
			rootx = Data( iX, iY )
			root = Data( rootx, iZ )
			return root
		
		def testData3(root):
			rootx = root.x
			iX = rootx.x
			iY = rootx.y
			iZ = root.y
			self.assertTrue( iX.value.x is iY.value.x )
			self.assertTrue( iZ.value.y is iY.value )
		
		self._check( data3, testData3 )

	
		
	def test_isolated_value_is_root_object(self):
		def data4():
			a = Data( 0, 1 )
			b = Data( 2, 3 )
			x = Data( a, 0 )
			y = Data( a, 1 )
			z = Data( b, y )
			iX = IsolationBarrier( x )
			iY = IsolationBarrier( y )
			iZ = IsolationBarrier( z )
			rootx = Data( iX, iY )
			iW = IsolationBarrier( rootx )
			rooty = Data( iZ, iW )
			root = Data( rootx, rooty )
			return root
		
		def testData4(root):
			rootx = root.x
			rooty = root.y
			iX = rootx.x
			iY = rootx.y
			iZ = rooty.x
			iW = rooty.y
			self.assertTrue( iX.value.x is iY.value.x )
			self.assertTrue( iZ.value.y is iY.value )
			self.assertTrue( iW.value is rootx )
		
		self._check( data4, testData4 )
	
