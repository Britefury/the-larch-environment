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

from Britefury.CodeViewTree.CVTNode import CVTNode
from Britefury.CodeViewTree.CodeViewTree import *



class CVTAssignment (CVTNode):
	graphNode = SheetRefField( CGAssignment )


	@FunctionRefField
	def targetRefNode(self):
		return self._tree.buildNode( self.graphNode.targetRef[0].node )

	@FunctionRefField
	def valueNode(self):
		return self._tree.buildNode( self.graphNode.value[0].node )



	def removeAssignment(self):
		targetRefCG = self.graphNode.targetRef[0].node

		valueCGSource = self.graphNode.value[0]

		parentCGSink = self.graphNode.parent[0]

		del self.graphNode.value[0]
		parentCGSink.replace( self.graphNode.parent, valueCGSource )

		self.graphNode.destroySubtree()

		return valueCGSource.node



class CVTRuleAssignment (CVTRuleSimple):
	graphNodeClass = CGAssignment
	cvtNodeClass = CVTAssignment

CVTRuleAssignment.register()


