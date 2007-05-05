##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from weakref import WeakKeyDictionary



class CodeViewTree (object):
	_nodeClassTable = {}

	def __init__(self, graph):
		super( CodeViewTree, self ).__init__()

		self._nodeTable = WeakKeyDictionary()
		self._graph = graph



	def buildNode(self, graphNode, nodeClass=None):
		if graphNode is None:
			return None
		else:
			graphNodeClass = graphNode.__class__

			if nodeClass is None:
				try:
					nodeClass = self._nodeClassTable[graphNodeClass]
				except KeyError:
					raise TypeError, 'could not get tree node class for graph node class %s'  %  ( graphNodeClass.__name__, )

			try:
				subTable = self._nodeTable[graphNode]
			except KeyError:
				subTable = {}
				self._nodeTable[graphNode] = subTable

			try:
				treeNode = subTable[nodeClass]
			except KeyError:
				treeNode = nodeClass( graphNode, self )
				subTable[nodeClass] = treeNode

			return treeNode


