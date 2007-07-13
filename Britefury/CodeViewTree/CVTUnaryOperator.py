##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGUnaryOperator import *

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CVTExpression import CVTExpression
from Britefury.CodeViewTree.CVTAttrName import *
from Britefury.CodeViewTree.CodeViewTree import *



class CVTUnaryOperator (CVTExpression):
	graphNode = SheetRefField( CGUnaryOperator )


	exprNode = CVTSimpleSinkProductionSingleField( CGUnaryOperator.expr )



	def unwrapUnaryOperator(self):
		parentCGSink = self.graphNode.parent[0]
		exprSource = self.graphNode.expr[0]
		if isinstance( exprSource.node, CGNullExpression ):
			parentCGSink.remove( self.graphNode.parent )
		else:
			self.graphNode.expr.remove( exprSource )
			parentCGSink.replace( self.graphNode.parent, exprSource )

		self.graphNode.destroySubtree()



class CVTRuleUnaryOperator (CVTRuleSimple):
	graphNodeClass = CGUnaryOperator
	cvtNodeClass = CVTUnaryOperator

CVTRuleUnaryOperator.register()




def _makeSimpleUnaryOpCVT(unaryOpGraphNodeClass, cvtClassName, ruleClassName):
	class _CVTUnaryOp (CVTUnaryOperator):
		graphNode = SheetRefField( unaryOpGraphNodeClass )


	_CVTUnaryOp.__name__ = cvtClassName


	class _CVTRuleUnaryOp (CVTRuleSimple):
		graphNodeClass = unaryOpGraphNodeClass
		cvtNodeClass = _CVTUnaryOp


	_CVTRuleUnaryOp.__name__ = ruleClassName

	_CVTRuleUnaryOp.register()

	return _CVTUnaryOp, _CVTRuleUnaryOp



CVTNegate, CVTRuleNegate = _makeSimpleUnaryOpCVT( CGNegate, 'CVTNegate', 'CVTRuleNegate' )
CVTNot, CVTRuleNot = _makeSimpleUnaryOpCVT( CGNot, 'CVTNot', 'CVTRuleNot' )

