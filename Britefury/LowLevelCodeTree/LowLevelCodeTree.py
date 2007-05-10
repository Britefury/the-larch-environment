##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.VMMachine import VMMachine
from Britefury.VirtualMachine.VMTag import VMTag



class LowLevelCodeTree (object):
	_nodeClassTable = {}

	def __init__(self, graph):
		super( LowLevelCodeTree, self ).__init__()

		self._graph = graph
		self._graphNodeToTreeNode = {}

		self.tag_Object = VMMachine.tag_Object
		self.tag_Class = VMMachine.tag_Class
		self.tag_Bool = VMMachine.tag_Bool
		self.tag_String = VMMachine.tag_String
		self.tag_List = VMMachine.tag_List
		self.tag_Closure = VMMachine.tag_Closure
		self.tag_Frame = VMMachine.tag_Frame
		self.tag_Block = VMMachine.tag_Block
		self.tag_Module = VMMachine.tag_Module
		self.tag_none = VMMachine.tag_none
		self.tag_false = VMMachine.tag_false
		self.tag_true = VMMachine.tag_true



	def __getitem__(self, graphNode):
		return self._graphNodeToTreeNode[graphNode]

	def __setitem__(self, graphNode, treeNode):
		self._graphNodeToTreeNode[graphNode] = treeNode

	def __contains__(self, graphNode):
		return graphNode in self._graphNodeToTreeNode

	def has_key(self, graphNode):
		return self._graphNodeToTreeNode.has_key( graphNode )








