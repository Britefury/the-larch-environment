##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Typesetting.Node import Node


class HBox (Node):
	def __init__(self, contents, spacing=0.0):
		self._spacing = spacing
		self._contents = contents
		
		
	def reqWidth(self):
		if len( self._contents ) > 0:
			return reduce( lambda w, node: w + node.reqWidth(),  self._contents, 0.0 )  +  ( self._spacing * ( len( self._contents ) - 1 ) )
		else:
			return 0.0
	
	
	def reqAscentDescentOffset(self):
		ascent = 0.0
		descent = 0.0
		baseline = 0.0
		bOffset = False
		for node in self._contents:
			a, d, o = node.reqAscentDescentOffset()
			if bOffset:
				a -= baseline
				d += baseline
			ascent = max( ascent, a )
			descent = max( descent, d )
			if o is not None:
				bOffset = True
				baseline += o
		return ascent,  descent, baseline   if bOffset   else   None


	
	
	
import unittest
from Britefury.DocPresent.Typesetting.Element import Element

class TestCase_HBox (unittest.TestCase):
	def testEmpty(self):
		b = HBox( [] )
		self.assert_( b.reqWidth() == 0.0 )
		self.assert_( b.reqAscentDescentOffset() == ( 0.0, 0.0, None ) )
	
	def testTwo(self):
		e1 = Element( 10.0, 5.0, 3.0 )
		e2 = Element( 15.0, 8.0, 2.0 )
		b1 = HBox( [ e1, e2 ] )
		b2 = HBox( [ e1, e2 ], 7.0 )
		self.assert_( b1.reqWidth() == 25.0 )
		self.assert_( b1.reqAscentDescentOffset() == ( 8.0, 3.0, None ) )
		self.assert_( b2.reqWidth() == 32.0 )
		self.assert_( b2.reqAscentDescentOffset() == ( 8.0, 3.0, None ) )

	def testOffsetD(self):
		e1 = Element( 10.0, 6.0, 3.0 )
		e2 = Element( 10.0, 7.0, 2.0, 7.0 )
		e3 = Element( 10.0, 5.0, 4.0 )
		b = HBox( [ e1, e2, e3 ] )
		self.assert_( b.reqWidth() == 30.0 )
		self.assert_( b.reqAscentDescentOffset() == ( 7.0, 11.0, 7.0 ) )

	def testOffsetU(self):
		e1 = Element( 10.0, 6.0, 3.0 )
		e2 = Element( 10.0, 7.0, 2.0, -7.0 )
		e3 = Element( 10.0, 5.0, 4.0 )
		b = HBox( [ e1, e2, e3 ] )
		self.assert_( b.reqWidth() == 30.0 )
		self.assert_( b.reqAscentDescentOffset() == ( 12.0, 3.0, -7.0 ) )

	def testOffsetDU(self):
		e1 = Element( 10.0, 6.0, 3.0 )
		e2 = Element( 10.0, 7.0, 2.0, 7.0 )
		e3 = Element( 10.0, 5.0, 4.0 )
		e4 = Element( 10.0, 7.0, 2.0, -5.0 )
		e5 = Element( 10.0, 5.0, 4.0 )
		b = HBox( [ e1, e2, e3, e4, e5 ] )
		self.assert_( b.reqWidth() == 50.0 )
		self.assert_( b.reqAscentDescentOffset() == ( 7.0, 11.0, 2.0 ) )

	def testOffsetUD(self):
		e1 = Element( 10.0, 6.0, 3.0 )
		e2 = Element( 10.0, 7.0, 2.0, -7.0 )
		e3 = Element( 10.0, 5.0, 4.0 )
		e4 = Element( 10.0, 7.0, 2.0, 5.0 )
		e5 = Element( 10.0, 5.0, 4.0 )
		b = HBox( [ e1, e2, e3, e4, e5 ] )
		self.assert_( b.reqWidth() == 50.0 )
		self.assert_( b.reqAscentDescentOffset() == ( 14.0, 3.0, -2.0 ) )

