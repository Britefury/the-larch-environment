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
from Britefury.CodeGraph.CGUnboundRef import CGUnboundRef

from Britefury.CodeViewTree.CVTExpression import CVTExpression



class CVTLocalRef (CVTExpression):
	graphNodeClass = CGLocalRef


	graphNode = SheetRefField( CGLocalRef )


	def _varName(self):
		return self.graphNode.variable[0].node.name


	varName = FunctionField( _varName )




	def rebind(self, varName):
		targetGraphNode = self.graphNode.getReferenceableNodeByName( varName )
		if targetGraphNode is not None:
			self.graphNode.variable[0] = targetGraphNode.references
			return self
		else:
			parentCGSink = self.graphNode.parent[0]
			unboundRefGraphNode = CGUnboundRef()
			unboundRefGraphNode.targetName = varName
			parentCGSink.replace( self.graphNode.parent, unboundRefGraphNode.parent )
			return self._tree.buildNode( unboundRefGraphNode )
