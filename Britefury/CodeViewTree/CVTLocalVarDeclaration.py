##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGLocalVarDeclaration import CGLocalVarDeclaration
from Britefury.CodeGraph.CGNullExpression import CGNullExpression

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CVTStatement import CVTStatement
from Britefury.CodeViewTree.CodeViewTree import *



class CVTLocalVarDeclaration (CVTStatement):
	graphNode = SheetRefField( CGLocalVarDeclaration )


	varNode = CVTSimpleSinkProductionSingleField( CGLocalVarDeclaration.variable )
	valueNode = CVTSimpleSinkProductionOptionalField( CGLocalVarDeclaration.value )




	def ensureHasValue(self):
		if len( self.graphNode.value ) == 0:
			value = CGNullExpression()
			self.graph.nodes.append( value )
			self.graphNode.value.append( value.parent )


	def deleteValue(self):
		if len( self.graphNode.value ) == 1:
			valueNode = self.graphNode.value[0].node
			del self.graphNode.value[0]
			valueNode.destroySubtree()




class CVTRuleLocalVarDeclaration (CVTRuleSimple):
	graphNodeClass = CGLocalVarDeclaration
	cvtNodeClass = CVTLocalVarDeclaration

CVTRuleLocalVarDeclaration.register()

