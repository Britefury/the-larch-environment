##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGNullExpression import CGNullExpression
from Britefury.CodeGraph.CGStringLiteral import CGStringLiteral
from Britefury.CodeGraph.CGUnboundRef import CGUnboundRef

from Britefury.CodeViewTree.CVTNode import CVTNode



class CVTNullExpression (CVTNode):
	graphNodeClass = CGNullExpression


	graphNode = SheetRefField( CGNullExpression )



	# Replace with @graphNodeToInsert
	def insertNode(self, graphNodeToInsert, treeNodePath):
		parentCGSink = self.graphNode.parent[0]
		parentCGSink.replace( self.graphNode.parent, graphNodeToInsert.parent )
		self.graphNode.destroySubtree()
		return self._tree.buildNode( graphNodeToInsert )


