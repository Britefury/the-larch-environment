##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from weakref import WeakKeyDictionary



class LowLevelCodeTree (object):
	_nodeClassTable = {}

	def __init__(self, graph):
		super( LowLevelCodeTree, self ).__init__()

		self._graph = graph
		self._graphNodeToTreeNode = {}



	def __getitem__(self, graphNode):
		return self._graphNodeToTreeNode[graphNode]

	def __setitem__(self, graphNode, treeNode):
		self._graphNodeToTreeNode[graphNode] = treeNode

	def __contains__(self, graphNode):
		return graphNode in self._graphNodeToTreeNode

	def has_key(self, graphNode):
		return self._graphNodeToTreeNode.has_key( graphNode )

