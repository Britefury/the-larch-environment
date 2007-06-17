##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGArguments import CGArguments
from Britefury.CodeGraph.CGNullExpression import CGNullExpression

from Britefury.CodeViewTree.CVTNode import CVTNode



class CVTArguments (CVTNode):
	graphNodeClass = CGArguments

	graphNode = SheetRefField( CGArguments )



	@FunctionField
	def argNodes(self):
		return [ self._tree.buildNode( argSource.node )   for argSource in self.graphNode.args ]


	@FunctionRefField
	def expandArgNode(self):
		if len( self.graphNode.expandArg ) > 0:
			return self._tree.buildNode( self.graphNode.expandArg[0].node )
		else:
			return None



	def addArgument(self):
		sendMsgCG = self.graphNode
		argCG = CGNullExpression()
		self.graph.nodes.append( argCG )
		sendMsgCG.args.append( argCG.parent )
		return self._tree.buildNode( argCG )


	def deleteArgument(self, argument):
		if argument.graphNode.parent in self.graphNode.args:
			self.graphNode.args.remove( argument.graphNode.parent )
			argument.graphNode.destroySubtree()



	def insertNode(self, graphNodeToInsert, treeNodePath):
		position = len( self.argNodes )
		if len( treeNodePath ) > 1:
			try:
				n = self.argNodes.index( treeNodePath[1] )
			except ValueError:
				pass
			else:
				position = n
		self.graphNode.args.insert( position, graphNodeToInsert.parent )
		return self._tree.buildNode( graphNodeToInsert )
