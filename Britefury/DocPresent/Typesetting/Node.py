##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************



class Node (object):
	def __init__(self):
		self._x = 0.0
		self._y = 0.0

	def reqWidth(self):
		return 0.0
	
	
	def reqAscentDescentOffset(self):
		return 0.0, 0.0, None
	
	
	def allocX(self, x, width):
		pass
	
	def allocY(self, y, height):
		pass

	
	
import unittest

class TestCase_Node (unittest.TestCase):
	def testNode(self):
		n = Node()
		
		self.assert_( n.reqWidth() == 0.0 )
		self.assert_( n.reqAscentDescentOffset() == ( 0.0, 0.0, None ) )
		
