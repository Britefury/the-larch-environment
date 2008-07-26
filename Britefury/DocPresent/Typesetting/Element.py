##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Typesetting.Node import Node


class Element (Node):
	__slots__ = [ '_w', '_a', '_d', '_o' ]

	def __init__(self, width, ascent, descent, baselineOffset=None):
		self._w = width
		self._a = ascent
		self._d = descent
		self._o = baselineOffset
	
		
	def reqWidth(self):
		return self._w
	
	
	def reqAscentDescentOffset(self):
		return self._a, self._d, self._o

	
	
	
import unittest

class TestCase_Element (unittest.TestCase):
	def testElement(self):
		e = Element( 1.0, 2.0, 3.0 )
		e2 = Element( 1.0, 2.0, 3.0, -2.0 )
		
		self.assert_( e.reqWidth() == 1.0 )
		self.assert_( e.reqAscentDescentOffset() == ( 2.0, 3.0, None ) )
		self.assert_( e2.reqAscentDescentOffset() == ( 2.0, 3.0, -2.0 ) )
	
	
	

