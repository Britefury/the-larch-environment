##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGLocalRef import CGLocalRef
from Britefury.CodeGraph.CGLocalAssignment import CGLocalAssignment
from Britefury.CodeGraph.CGNullExpression import CGNullExpression

from Britefury.CodeViewTree.CVTExpression import CVTExpression



class CVTLocalRef (CVTExpression):
	graphNodeClass = CGLocalRef


	graphNode = SheetRefField( CGLocalRef )


	@FunctionRefField
	def varNode(self):
		return self._tree.buildNode( self.graphNode.variable[0].node )



	def replaceWithLocalAssignment(self):
		localAssignment = CGLocalAssignment()
		self.graph.nodes.append( localAssignment )

		nullExpression = CGNullExpression()
		self.graph.nodes.append( nullExpression )

		localAssignment.variable.append( self.graphNode.variable[0] )
		localAssignment.value.append( nullExpression.parent )
		del self.graphNode.variable[0]

		parentCGSink = self.graphNode.parent[0]
		parentCGSink.replace( self.graphNode.parent, localAssignment.parent )
		self.graphNode.destroySubtree()
		return self._tree.buildNode( localAssignment )

