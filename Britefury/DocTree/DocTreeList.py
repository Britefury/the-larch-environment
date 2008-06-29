##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocTree.DocTreeNode import DocTreeNode
from Britefury.DocModel.DMListInterface import DMListInterface


class DocTreeList (DocTreeNode):
	def __setitem__(self, i, x):
		self._dtn_node[i] = x

	def __delitem__(self, i):
		del self._dtn_node[i]

	def __getitem__(self, i):
		if isinstance( i, slice ):
			indices = xrange( *i.indices( len( self._dtn_node ) ) )
			return [ self._dtn_tree.treeNode( x, self, j )   for j, x in zip( indices, self._dtn_node[i] ) ]
		else:
			return self._dtn_tree.treeNode( self._dtn_node[i], self, i )

	def __contains__(self, x):
		return x in self._dtn_node

	def __iter__(self):
		for i, x in enumerate( self._dtn_node ):
			yield self._dtn_tree.treeNode( x, self, i )

	def __add__(self, xs):
		if isinstance( xs, DocTreeList ):
			return DocTreeList( self._dtn_tree, self._dtn_node + xs._dtn_node, None, -1 )
		else:
			return self._dtn_node + xs

	def __radd__(self, xs):
		if isinstance( xs, DocTreeList ):
			return DocTreeList( self._dtn_tree, xs._dtn_node + self._dtn_node, None, -1 )
		else:
			return xs + self._dtn_node

	def __len__(self):
		return len( self._dtn_node )



	def __copy__(self):
		return DocTreeList( self._dtn_tree, copy( self._dtn_node ), None, -1 )

	def __deepcopy__(self, memo):
		return DocTreeList( self._dtn_tree, deepcopy( self._dtn_node, memo ), None, -1 )

