##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
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
			i_x = IsolationBarrier( x )
			i_y = IsolationBarrier( y )
			i_z = IsolationBarrier( z )
			rootx = Data( i_x, i_y )
			root = Data( rootx, i_z )
			return root
		
		def testData1(root):
			rootx = root.x
			i_x = rootx.x
			i_y = rootx.y
			self.assertTrue( i_x.value.x is i_y.value.x )
		
		self._check( data1, testData1 )
			
		
		
	def test_isolated_partition_refernces_root_object(self):
		def data2():
			a = Data( 0, 1 )
			b = Data( 2, 3 )
			x = Data( a, 0 )
			y = Data( a, 1 )
			z = Data( b, 2 )
			i_x = IsolationBarrier( x )
			i_y = IsolationBarrier( y )
			i_z = IsolationBarrier( z )
			rootx = Data( i_x, i_y )
			root = Data( rootx, i_z )
			z.y = rootx
			return root
		
		def testData2(root):
			rootx = root.x
			i_x = rootx.x
			i_y = rootx.y
			i_z = root.y
			self.assertTrue( i_x.value.x is i_y.value.x )
			self.assertTrue( i_z.value.y is rootx )
		
		self._check( data2, testData2 )
			
		
		
	def test_isolated_object_directly_referenced_by_partition_of_another(self):
		def data3():
			a = Data( 0, 1 )
			b = Data( 2, 3 )
			x = Data( a, 0 )
			y = Data( a, 1 )
			z = Data( b, y )
			i_x = IsolationBarrier( x )
			i_y = IsolationBarrier( y )
			i_z = IsolationBarrier( z )
			rootx = Data( i_x, i_y )
			root = Data( rootx, i_z )
			return root
		
		def testData3(root):
			rootx = root.x
			i_x = rootx.x
			i_y = rootx.y
			i_z = root.y
			self.assertTrue( i_x.value.x is i_y.value.x )
			self.assertTrue( i_z.value.y is i_y.value )
		
		self._check( data3, testData3 )

	
		
	def test_isolated_value_is_root_object(self):
		def data4():
			a = Data( 0, 1 )
			b = Data( 2, 3 )
			x = Data( a, 0 )
			y = Data( a, 1 )
			z = Data( b, y )
			i_x = IsolationBarrier( x )
			i_y = IsolationBarrier( y )
			i_z = IsolationBarrier( z )
			rootx = Data( i_x, i_y )
			i_w = IsolationBarrier( rootx )
			rooty = Data( i_z, i_w )
			root = Data( rootx, rooty )
			return root
		
		def testData4(root):
			rootx = root.x
			rooty = root.y
			i_x = rootx.x
			i_y = rootx.y
			i_z = rooty.x
			i_w = rooty.y
			self.assertTrue( i_x.value.x is i_y.value.x )
			self.assertTrue( i_z.value.y is i_y.value )
			self.assertTrue( i_w.value is rootx )
		
		self._check( data4, testData4 )

	
		
	def test_nested_isolated_value(self):
		def data5():
			a = Data( 0, 1 )
			b = Data( 2, 3 )
			x = Data( a, 0 )
			y = Data( a, 1 )
			z = Data( b, y )
			
			p = Data( 5, 6 )
			r = Data( p, 7 )
			s = Data( p, 8 )
			i_r = IsolationBarrier( r )
			i_s = IsolationBarrier( s )
			
			t = Data( i_r, i_s )
			w = Data( t, 0 )
			
			i_x = IsolationBarrier( x )
			i_y = IsolationBarrier( y )
			i_z = IsolationBarrier( z )
			i_w = IsolationBarrier( w )
			rootx = Data( i_x, i_y )
			rooty = Data( i_z, i_w )
			root = Data( rootx, rooty )
			return root
		
		def testData5(root):
			rootx = root.x
			rooty = root.y
			i_x = rootx.x
			i_y = rootx.y
			i_z = rooty.x
			i_w = rooty.y
			w = i_w.value
			t = w.x
			
			self.assertTrue( i_x.value.x is i_y.value.x )
			self.assertTrue( i_z.value.y is i_y.value )
			self.assertTrue( t.x.value.x is t.y.value.x )
		
		self._check( data5, testData5 )
	

		
	def test_nested_isolated_value_referencing_outer_nested_value(self):
		def data6():
			a = Data( 0, 1 )
			b = Data( 2, 3 )
			x = Data( a, 0 )
			y = Data( a, 1 )
			z = Data( b, y )
			
			p = Data( 5, 6 )
			r = Data( p, 7 )
			s = Data( p, 8 )
			i_r = IsolationBarrier( r )
			i_s = IsolationBarrier( s )
			
			t = Data( i_r, i_s )
			w = Data( t, 0 )
			s.y = t
			
			i_x = IsolationBarrier( x )
			i_y = IsolationBarrier( y )
			i_z = IsolationBarrier( z )
			i_w = IsolationBarrier( w )
			rootx = Data( i_x, i_y )
			rooty = Data( i_z, i_w )
			root = Data( rootx, rooty )
			return root
		
		def testData6(root):
			rootx = root.x
			rooty = root.y
			i_x = rootx.x
			i_y = rootx.y
			i_z = rooty.x
			i_w = rooty.y
			w = i_w.value
			t = w.x
			
			self.assertTrue( i_x.value.x is i_y.value.x )
			self.assertTrue( i_z.value.y is i_y.value )
			self.assertTrue( t.x.value.x is t.y.value.x )
			self.assertTrue( t.y.value.y is t )
		
		self._check( data6, testData6 )

		
		
		
	

		
	def test_nested_isolated_value_referencing_root_value(self):
		def data7():
			a = Data( 0, 1 )
			b = Data( 2, 3 )
			x = Data( a, 0 )
			y = Data( a, 1 )
			z = Data( b, y )
			
			p = Data( 5, 6 )
			r = Data( p, 7 )
			s = Data( p, 8 )
			i_r = IsolationBarrier( r )
			i_s = IsolationBarrier( s )
			
			t = Data( i_r, i_s )
			w = Data( t, 0 )
			
			i_x = IsolationBarrier( x )
			i_y = IsolationBarrier( y )
			i_z = IsolationBarrier( z )
			i_w = IsolationBarrier( w )
			rootx = Data( i_x, i_y )
			rooty = Data( i_z, i_w )
			s.y = rooty
			root = Data( rootx, rooty )
			return root
		
		def testData7(root):
			rootx = root.x
			rooty = root.y
			i_x = rootx.x
			i_y = rootx.y
			i_z = rooty.x
			i_w = rooty.y
			w = i_w.value
			t = w.x
			
			self.assertTrue( i_x.value.x is i_y.value.x )
			self.assertTrue( i_z.value.y is i_y.value )
			self.assertTrue( t.x.value.x is t.y.value.x )
			self.assertTrue( t.y.value.y is rooty )
		
		self._check( data7, testData7 )
