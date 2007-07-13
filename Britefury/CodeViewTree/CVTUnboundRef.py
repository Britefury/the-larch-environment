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
from Britefury.CodeGraph.CGBlock import CGBlock

from Britefury.CodeViewTree.CVTExpression import CVTExpression
from Britefury.CodeViewTree.CodeViewTree import *

from Britefury.CodeViewTreeOperations.CVTOWrapInAssignment import cvto_wrapInAssignment
from Britefury.CodeViewTreeOperations.CVTOStatementListOperations import *



class CVTUnboundRef (CVTExpression):
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



	# Replace with @graphNodeToInsert
	def insertNode(self, graphNodeToInsert, position):
		parentCGSink = self.graphNode.parent[0]
		parentCGSink.replace( self.graphNode.parent, graphNodeToInsert.parent )
		self.graphNode.destroySubtree()
		return self._tree.buildNode( graphNodeToInsert )



	def wrapInAssignment(self):
		return cvto_wrapInAssignment( self )



	def replaceWithStatement(self, subgraphFunction):
		if isinstance( self.graphNode.parent[0].node, CGBlock ):
			subgraph = subgraphFunction( self )
			self.graphNode.parent[0].replace( self.graphNode.parent, subgraph.parent )
			self.graphNode.destroySubtree()
			return self._tree.buildNode( subgraph )
		else:
			return None


	def replaceWithReturn(self):
		return self.replaceWithStatement( cvto_createReturnSubgraph )

	def replaceWithIf(self):
		return self.replaceWithStatement( cvto_createIfSubgraph )

	def replaceWithWhile(self):
		return self.replaceWithStatement( cvto_createWhileSubgraph )

	def replaceWithBreak(self):
		return self.replaceWithStatement( cvto_createBreakSubgraph )

	def replaceWithContinue(self):
		return self.replaceWithStatement( cvto_createContinueSubgraph )

	def replaceWithDef(self):
		return self.replaceWithStatement( cvto_createDefSubgraph )

	def replaceWithClass(self):
		return self.replaceWithStatement( cvto_createClassSubgraph )

	def replaceWithImport(self):
		return self.replaceWithStatement( cvto_createImportSubgraph )




class CVTRuleUnboundRef (CVTRuleSimple):
	graphNodeClass = CGUnboundRef
	cvtNodeClass = CVTUnboundRef

CVTRuleUnboundRef.register()

