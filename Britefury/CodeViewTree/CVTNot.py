##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGNot import CGNot

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CVTExpression import CVTExpression
from Britefury.CodeViewTree.CVTAttrName import CVTAttrName
from Britefury.CodeViewTree.CodeViewTree import *

from Britefury.CodeViewTreeOperations.CVTOWrapInAssignment import cvto_wrapInAssignment



class CVTNot (CVTExpression):
	graphNode = SheetRefField( CGNot )


	exprNode = CVTSimpleSinkProductionSingleField( CGNot.expr )





	def unwrapNot(self):
		parentCGSink = self.graphNode.parent[0]
		exprSource = self.graphNode.expr[0]

		self.graphNode.expr.remove( exprSource )

		parentCGSink.replace( self.graphNode.parent, exprSource )

		self.graphNode.destroySubtree()




class CVTRuleNot (CVTRuleSimple):
	graphNodeClass = CGNot
	cvtNodeClass = CVTNot

CVTRuleNot.register()

