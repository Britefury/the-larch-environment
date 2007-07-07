##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGSubscript import *

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CVTExpression import CVTExpression
from Britefury.CodeViewTree.CVTAttrName import *
from Britefury.CodeViewTree.CodeViewTree import *

from Britefury.CodeViewTreeOperations.CVTOWrapInAssignment import cvto_wrapInAssignment



class CVTSubscript (CVTExpression):
	graphNode = SheetRefField( CGSubscript )


	targetNode = CVTSimpleSinkProductionSingleField( CGSubscript.target )
	keyNode = CVTSimpleSinkProductionSingleField( CGSubscript.key )



	def removeTarget(self):
		parentCGSink = self.graphNode.parent[0]
		keySource = self.graphNode.key[0]

		self.graphNode.key.remove( keySource )

		parentCGSink.replace( self.graphNode.parent, keySource )

		self.graphNode.destroySubtree()



	def removeKey(self):
		parentCGSink = self.graphNode.parent[0]
		targetSource = self.graphNode.target[0]

		self.graphNode.target.remove( targetSource )

		parentCGSink.replace( self.graphNode.parent, targetSource )

		self.graphNode.destroySubtree()


	def wrapInAssignment(self):
		return cvto_wrapInAssignment( self )



class CVTRuleSubscript (CVTRuleSimple):
	graphNodeClass = CGSubscript
	cvtNodeClass = CVTSubscript

CVTRuleSubscript.register()



