##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGUnboundRef import CGUnboundRef

from Britefury.CodeViewTree.CVTExpression import CVTExpression

from Britefury.CodeViewTreeOperations.CVTOWrapInAssignment import cvto_wrapInAssignment



class CVTUnboundRef (CVTExpression):
	graphNodeClass = CGUnboundRef


	graphNode = SheetRefField( CGUnboundRef )

	targetName = FieldProxy( graphNode.targetName )



	def replaceWithRef(self):
		targetName = self.targetName
		targetGraphNode = self.graphNode.getReferenceableNodeByName( targetName )
		if targetGraphNode is not None:
			parentCGSink = self.graphNode.parent[0]
			refGraphNode = targetGraphNode.createRefNode()
			self.graph.nodes.append( refGraphNode )
			parentCGSink.replace( self.graphNode.parent, refGraphNode.parent )
			self.graphNode.destroySubtree()
			return self._tree.buildNode( refGraphNode )
		else:
			return None



	def wrapInLocalAssignment(self):
		return cvto_wrapInAssignment( self )
