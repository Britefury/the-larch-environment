##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocTree.DocTreeNode import DocTreeNode



class DocTreeUnicode (unicode, DocTreeNode):
	def __init__(self, node):
		unicode.__init__( node )
		DocTreeNode.__init__( self, None, node, None, -1 )
		
		
	@staticmethod
	def _build(tree, value, parent, indexInParent):
		x = DocTreeUnicode( value )
		x._dtn_tree = tree
		x._dtn_parentTreeNode = parent
		x._dtn_indexInParent = indexInParent
		return x


	
	
import unittest
	
class TestCase_DocTreeUnicode(unittest.TestCase):
	def testId(self):
		x = u'x'
		x2 = u'x'
		dx = DocTreeUnicode( u'x' )
		dx2 = DocTreeUnicode( u'x' )
		
		self.assert_( x is x )
		self.assert_( x is x2 )
		self.assert_( x is not dx )
		self.assert_( x is not dx2 )
		
		self.assert_( dx is dx )
		self.assert_( dx is not dx2 )

		
	def testCmp(self):
		x = u'x'
		dx = DocTreeUnicode( u'x' )
		
		self.assert_( x == dx )
		self.assert_( dx == x )
		

