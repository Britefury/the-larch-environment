##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import unittest

from BritefuryJ.DocModel import DMSchema
from BritefuryJ.Cell import Cell
from BritefuryJ.Incremental import IncrementalMonitorListener

from Britefury.AttributeVisitor.AttributeVisitor import Attribute, SynthesizedAttribute, InheritedAttribute, AttributeEvaluationMethod, IncrementalAttributeVisitor, NoAttributeEvaluationFunctionException




class _CountedCell (IncrementalMonitorListener):
	def __init__(self, fn):
		self._cell = Cell()
		self._cell.setFunction( fn )
		self._cell.addListener( self )
		self.count = 0
		
	def _getValue(self):
		return self._cell.getValue()
	
	def onIncrementalMonitorChanged(self, inc):
		self.count += 1
	
	value = property( _getValue )


class TestCase_attributeVisitor (unittest.TestCase):
	def setUp(self):
		self.schema = DMSchema( 'AttributeVisitor', 'attr', 'Tests.AttributeVisitor' )
		self.A = self.schema.newClass( 'A', [ 'x', 'y' ] )
		self.B = self.schema.newClass( 'B', self.A, [ 'p', 'q' ] )
		self.C = self.schema.newClass( 'C', [ 's', 't' ] )
		self.a = self.A( x='a', y='b' )
		self.b = self.B( x='a', y='b', p='c', q='d' )
		self.c = self.C( s='a', t='b' )
	
		class AttributeTest (IncrementalAttributeVisitor):
			e = Attribute( 'e' )
			
			@AttributeEvaluationMethod( e, self.A )
			def A(self, node, x, y):
				return x + y



		class InheritedAttributeTest (IncrementalAttributeVisitor):
			e = InheritedAttribute( 'e' )
			
			@AttributeEvaluationMethod( e, self.A )
			def A(self, node, x, y):
				return x



		class SynthesizedAttributeTest (IncrementalAttributeVisitor):
			e = SynthesizedAttribute( 'e' )
			
			@AttributeEvaluationMethod( e, self.A )
			def A(self, node, x, y):
				return x



		class NestedAttributeTest (IncrementalAttributeVisitor):
			e = Attribute( 'e' )
			
			@AttributeEvaluationMethod( e, self.A )
			def A(self, node, x, y):
				return self.e( x ) + self.e( y )
		
			@AttributeEvaluationMethod( e, self.C )
			def C(self, node, s, t):
				return s + t
		

		self.attributeTest = AttributeTest()
		self.inheritedAttributeTest = InheritedAttributeTest()
		self.synthesizedAttributeTest = SynthesizedAttributeTest()
		self.nestedAttributeTest = NestedAttributeTest()
		
		
		
		

	def tearDown(self):
		self.a = None
	
	
	
	
	def testAttribute(self):
		a = self.A( x='a', y='b' )
		self.assertEquals( self.attributeTest.e( a ), 'ab' )

	def testAttribute_incremental(self):
		a = self.A( x='a', y='b' )
		aCell = _CountedCell( lambda: self.attributeTest.e( a ) )
		self.assertEquals( aCell.value, 'ab' )
		a['y'] = 'c'
		self.assertEquals( aCell.count, 1 )
		self.assertEquals( aCell.value, 'ac' )

	def testAttributeSubclass(self):
		b = self.B( x='a', y='b', p='c', q='d' )
		self.assertEquals( self.attributeTest.e( b ), 'ab' )

	def testAttributeNoFn(self):
		c = self.C( s='a', t='b' )
		self.assertRaises( NoAttributeEvaluationFunctionException, lambda: self.attributeTest.e( c ) )


		
	def testInheritedAttribute(self):
		c = self.C( s='a', t='b' )
		a = self.A( x='a', y=c )
		self.assertEquals( self.inheritedAttributeTest.e( a ), 'a' )
		self.assertEquals( self.inheritedAttributeTest.e( c ), 'a' )

	def testInheritedAttribute_incremental(self):
		c = self.C( s='a', t='b' )
		a = self.A( x='a', y=c )
		aCell = _CountedCell( lambda: self.inheritedAttributeTest.e( a ) )
		cCell = _CountedCell( lambda: self.inheritedAttributeTest.e( c ) )
		self.assertEquals( aCell.value, 'a' )
		self.assertEquals( cCell.value, 'a' )
		a['x'] = 'c'
		self.assertEquals( aCell.count, 1 )
		self.assertEquals( cCell.count, 1 )
		self.assertEquals( aCell.value, 'c' )
		self.assertEquals( cCell.value, 'c' )

	def testInheritedAttributeSubclass(self):
		c = self.C( s='a', t='b' )
		b = self.B( x='a', y=c, p='c', q='d' )
		self.assertEquals( self.inheritedAttributeTest.e( b ), 'a' )
		self.assertEquals( self.inheritedAttributeTest.e( c ), 'a' )

	def testInheritedAttributeNoFn(self):
		c = self.C( s='a', t='b' )
		c2 = self.C( s='a', t=c )
		self.assertRaises( NoAttributeEvaluationFunctionException, lambda: self.inheritedAttributeTest.e( c2 ) )
		self.assertRaises( NoAttributeEvaluationFunctionException, lambda: self.inheritedAttributeTest.e( c ) )



	def testSynthesizedAttribute(self):
		k = self.C( s='a', t='b' )
		j = self.A( x='a', y=k )
		i = self.C( s='a', t=j )
		self.assertEquals( self.synthesizedAttributeTest.e( k ), None )
		self.assertEquals( self.synthesizedAttributeTest.e( j ), 'a' )
		self.assertEquals( self.synthesizedAttributeTest.e( i ), None )

	def testSynthesizedAttribute_incremental(self):
		k = self.C( s='a', t='b' )
		j = self.A( x='a', y=k )
		i = self.C( s='a', t=j )
		iCell = _CountedCell( lambda: self.synthesizedAttributeTest.e( i ) )
		jCell = _CountedCell( lambda: self.synthesizedAttributeTest.e( j ) )
		kCell = _CountedCell( lambda: self.synthesizedAttributeTest.e( k ) )
		self.assertEquals( kCell.value, None )
		self.assertEquals( jCell.value, 'a' )
		self.assertEquals( iCell.value, None )
		j['x'] = 'c'
		self.assertEquals( kCell.count, 0 )
		self.assertEquals( jCell.count, 1 )
		self.assertEquals( iCell.count, 1 )
		self.assertEquals( kCell.value, None )
		self.assertEquals( jCell.value, 'c' )
		self.assertEquals( iCell.value, None )

		

	def testNestedAttribute(self):
		a = self.A( x=self.C( s='a', t='b' ), y=self.C( s='c', t='d' ) )
		self.assertEquals( self.nestedAttributeTest.e( a ), 'abcd' )


	def testNestedAttribute_incremental(self):
		a = self.A( x=self.C( s='a', t='b' ), y=self.C( s='c', t='d' ) )
		aCell = _CountedCell( lambda: self.nestedAttributeTest.e( a ) )
		self.assertEquals( aCell.value, 'abcd' )
		a['x']['s'] = 'p'
		self.assertEquals( aCell.count, 1 )
		self.assertEquals( aCell.value, 'pbcd' )
		a['y']['t'] = 'r'
		self.assertEquals( aCell.count, 2 )
		self.assertEquals( aCell.value, 'pbcr' )
