##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGAssignment import CGAssignment
from Britefury.CodeGraph.CGNullExpression import CGNullExpression

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CodeViewTree import *



class CVTAssignment (CVTNode):
	graphNode = SheetRefField( CGAssignment )


	targetNodes = CVTSimpleSinkProductionMultipleField( CGAssignment.targets )
	valueNode = CVTSimpleSinkProductionSingleField( CGAssignment.value )



	def removeTarget(self, target):
		assert len( self.graphNode.targets ) > 0
		assert target in self.targetNodes
		if len( self.graphNode.targets ) == 1:
			# Replace with value
			valueCGSource = self.graphNode.value[0]

			parentCGSink = self.graphNode.parent[0]

			del self.graphNode.value[0]
			parentCGSink.replace( self.graphNode.parent, valueCGSource )

			self.graphNode.destroySubtree()
		else:
			targetCG = target.graphNode
			self.graphNode.targets.remove( targetCG.parent )
			targetCG.destroySubtree()



	def removeValue(self):
		assert len( self.graphNode.targets ) > 0
		if len( self.graphNode.targets ) == 1:
			# Replace with target
			targetCGSource = self.graphNode.targets[0]

			parentCGSink = self.graphNode.parent[0]

			del self.graphNode.targets[0]
			parentCGSink.replace( self.graphNode.parent, targetCGSource )

			self.graphNode.destroySubtree()
		else:
			# Replace value with last target, and destroy value subtree
			valueCG = self.graphNode.value[0].node

			targetCGSource = self.graphNode.targets[-1]
			del self.graphNode.targets[-1]

			self.graphNode.value[0] = targetCGSource

			valueCG.destroySubtree()

		return self._tree.buildNode( targetCGSource.node )


	def assign(self, targetCVT):
		if targetCVT is self.valueNode:
			if self.moveValueToTarget():
				return self
			else:
				return None
		elif targetCVT in self.targetNodes:
			index = self.targetNodes.index( targetCVT ) + 1
			nullExpression = CGNullExpression()
			self.graph.nodes.append( nullExpression )

			self.graphNode.targets.insert( index, nullExpression.parent )

			return self._tree.buildNode( nullExpression )
		else:
			assert False, 'error, invalid assignment target'


	def moveValueToTarget(self):
		valueCG = self.graphNode.value[0].node
		if valueCG.isAssignable():
			nullExpression = CGNullExpression()
			self.graph.nodes.append( nullExpression )

			self.graphNode.value[0] = nullExpression.parent

			self.graphNode.targets.append( valueCG.parent )
			return True
		else:
			return False




class CVTRuleAssignment (CVTRuleSimple):
	graphNodeClass = CGAssignment
	cvtNodeClass = CVTAssignment

CVTRuleAssignment.register()


