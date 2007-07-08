##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGBinaryOperator import *

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CVTExpression import CVTExpression
from Britefury.CodeViewTree.CVTAttrName import *
from Britefury.CodeViewTree.CodeViewTree import *



class CVTBinaryOperator (CVTExpression):
	graphNode = SheetRefField( CGBinaryOperator )


	leftNode = CVTSimpleSinkProductionSingleField( CGBinaryOperator.left )
	rightNode = CVTSimpleSinkProductionSingleField( CGBinaryOperator.right )



	def removeLeft(self):
		parentCGSink = self.graphNode.parent[0]
		rightSource = self.graphNode.right[0]

		self.graphNode.right.remove( rightSource )

		parentCGSink.replace( self.graphNode.parent, rightSource )

		self.graphNode.destroySubtree()

		return self._tree.buildNode( rightSource.node )



	def removeRight(self):
		parentCGSink = self.graphNode.parent[0]
		leftSource = self.graphNode.left[0]

		self.graphNode.left.remove( leftSource )

		parentCGSink.replace( self.graphNode.parent, leftSource )

		self.graphNode.destroySubtree()

		return self._tree.buildNode( leftSource.node )



class CVTRuleBinaryOperator (CVTRuleSimple):
	graphNodeClass = CGBinaryOperator
	cvtNodeClass = CVTBinaryOperator

CVTRuleBinaryOperator.register()




def _makeSimpleBinOpCVT(binOpGraphNodeClass, cvtClassName, ruleClassName):
	class _CVTBinOp (CVTBinaryOperator):
		graphNode = SheetRefField( binOpGraphNodeClass )


	_CVTBinOp.__name__ = cvtClassName


	class _CVTRuleBinOp (CVTRuleSimple):
		graphNodeClass = binOpGraphNodeClass
		cvtNodeClass = _CVTBinOp


	_CVTRuleBinOp.__name__ = ruleClassName

	_CVTRuleBinOp.register()

	return _CVTBinOp, _CVTRuleBinOp



CVTBinOpAdd, CVTRuleBinOpAdd = _makeSimpleBinOpCVT( CGBinOpAdd, 'CVTBinOpAdd', 'CVTRuleBinOpAdd' )
CVTBinOpSub, CVTRuleBinOpSub = _makeSimpleBinOpCVT( CGBinOpSub, 'CVTBinOpSub', 'CVTRuleBinOpSub' )
CVTBinOpMul, CVTRuleBinOpMul = _makeSimpleBinOpCVT( CGBinOpMul, 'CVTBinOpMul', 'CVTRuleBinOpMul' )
CVTBinOpDiv, CVTRuleBinOpDiv = _makeSimpleBinOpCVT( CGBinOpDiv, 'CVTBinOpDiv', 'CVTRuleBinOpDiv' )
CVTBinOpPow, CVTRuleBinOpPow = _makeSimpleBinOpCVT( CGBinOpPow, 'CVTBinOpPow', 'CVTRuleBinOpPow' )
CVTBinOpMod, CVTRuleBinOpMod = _makeSimpleBinOpCVT( CGBinOpMod, 'CVTBinOpMod', 'CVTRuleBinOpMod' )
CVTBinOpBitAnd, CVTRuleBinOpBitAnd = _makeSimpleBinOpCVT( CGBinOpBitAnd, 'CVTBinOpBitAnd', 'CVTRuleBinOpBitAnd' )
CVTBinOpBitOr, CVTRuleBinOpBitOr = _makeSimpleBinOpCVT( CGBinOpBitOr, 'CVTBinOpBitOr', 'CVTRuleBinOpBitOr' )
CVTBinOpBitXor, CVTRuleBinOpBitXor = _makeSimpleBinOpCVT( CGBinOpBitXor, 'CVTBinOpBitXor', 'CVTRuleBinOpBitXor' )
CVTBinOpEq, CVTRuleBinOpEq = _makeSimpleBinOpCVT( CGBinOpEq, 'CVTBinOpEq', 'CVTRuleBinOpEq' )
CVTBinOpNEq, CVTRuleBinOpNEq = _makeSimpleBinOpCVT( CGBinOpNEq, 'CVTBinOpNEq', 'CVTRuleBinOpNEq' )
CVTBinOpLT, CVTRuleBinOpLT = _makeSimpleBinOpCVT( CGBinOpLT, 'CVTBinOpLT', 'CVTRuleBinOpLT' )
CVTBinOpGT, CVTRuleBinOpGT = _makeSimpleBinOpCVT( CGBinOpGT, 'CVTBinOpGT', 'CVTRuleBinOpGT' )
CVTBinOpLTE, CVTRuleBinOpLTE = _makeSimpleBinOpCVT( CGBinOpLTE, 'CVTBinOpLTE', 'CVTRuleBinOpLTE' )
CVTBinOpGTE, CVTRuleBinOpGTE = _makeSimpleBinOpCVT( CGBinOpGTE, 'CVTBinOpGTE', 'CVTRuleBinOpGTE' )
CVTBinOpLShift, CVTRuleBinOpLShift = _makeSimpleBinOpCVT( CGBinOpLShift, 'CVTBinOpLShift', 'CVTRuleBinOpLShift' )
CVTBinOpRShift, CVTRuleBinOpRShift = _makeSimpleBinOpCVT( CGBinOpRShift, 'CVTBinOpRShift', 'CVTRuleBinOpRShift' )

