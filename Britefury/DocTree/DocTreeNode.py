##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************



class DocTreeNode (object):
	def __init__(self, tree, node, parentTreeNode, indexInParent):
		self._dtn_tree = tree
		self._dtn_node = node
		self._dtn_parentTreeNode = parentTreeNode
		self._dtn_indexInParent = indexInParent
		
		
	def __getattr__(self, name):
		return getattr( self._dtn_node, name )

	
	def getNode(self):
		return self._dtn_node
	
	def getParentTreeNode(self):
		return self._dtn_parentTreeNode
	
	def getIndexInParent(self):
		return self._dtn_indexInParent
	
	
	def __cmp__(self, x):
		if isinstance( x, DocTreeNode ):
			return cmp( self._dtn_node, x._dtn_node )
		else:
			return cmp( self._dtn_node, x )


		
	node = property( getNode )
	parentTreeNode = property( getParentTreeNode )
	indexInParent = property( getIndexInParent )

	
