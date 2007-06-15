##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGLambda import CGLambda

from Britefury.CodeViewTree.CVTExpression import CVTExpression



class CVTLambda (CVTExpression):
	graphNodeClass = CGLambda

	graphNode = SheetRefField( CGLambda )


	@FunctionRefField
	def paramsNode(self):
		return self._tree.buildNode( self.graphNode.parameters[0].node )

	@FunctionRefField
	def valueExprNode(self):
		return self._tree.buildNode( self.graphNode.valueExpr[0].node )

