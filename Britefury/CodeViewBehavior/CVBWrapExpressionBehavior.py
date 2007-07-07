##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeViewBehavior.CodeViewBehavior import *

from Britefury.CodeGraph.CGBinaryOperator import *



def _makeBinOpWrapFunction(graphNodeClass):
	def _wrapInBinOp(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.wrapInBinaryOperator( graphNodeClass )
		viewNode._f_commandHistoryThaw()
		return True
	return _wrapInBinOp


class CVBWrapExpressionBehavior (CodeViewBehavior):
	@CVBCharInputHandlerMethod( '(' )
	def _wrapInCall(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.wrapInCall()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBCharInputHandlerMethod( '.' )
	def _wrapInGetAttr(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.wrapInGetAttr()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBAccelInputHandlerMethod( '<alt>minus' )
	def _wrapInNegate(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.wrapInNegate()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBAccelInputHandlerMethod( '<alt>n' )
	def _wrapInNot(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.wrapInNot()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBAccelInputHandlerMethod( '<alt>t' )
	def _wrapInTuple(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.wrapInTuple()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBCharInputHandlerMethod( '[' )
	def _wrapInGetItem(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.wrapInGetItem()
		viewNode._f_commandHistoryThaw()
		return True


	_wrapInAdd = CVBCharInputHandlerMethod( '+' )( _makeBinOpWrapFunction( CGBinOpAdd ) )
	_wrapInSub = CVBCharInputHandlerMethod( '-' )( _makeBinOpWrapFunction( CGBinOpSub ) )
	_wrapInMul = CVBCharInputHandlerMethod( '*' )( _makeBinOpWrapFunction( CGBinOpMul ) )
	_wrapInDiv = CVBCharInputHandlerMethod( '/' )( _makeBinOpWrapFunction( CGBinOpDiv ) )
	_wrapInPow = CVBAccelInputHandlerMethod( '<alt>8' )( _makeBinOpWrapFunction( CGBinOpPow ) )
	_wrapInMod = CVBCharInputHandlerMethod( '%' )( _makeBinOpWrapFunction( CGBinOpMod ) )
	_wrapInBitAnd = CVBCharInputHandlerMethod( '&' )( _makeBinOpWrapFunction( CGBinOpBitAnd ) )
	_wrapInBitOr = CVBCharInputHandlerMethod( '|' )( _makeBinOpWrapFunction( CGBinOpBitOr ) )
	_wrapInBitXor = CVBCharInputHandlerMethod( '^' )( _makeBinOpWrapFunction( CGBinOpBitXor ) )
	_wrapInEq = CVBAccelInputHandlerMethod( '<alt>equal' )( _makeBinOpWrapFunction( CGBinOpEq ) )
	_wrapInNEq = CVBAccelInputHandlerMethod( '<alt>1' )( _makeBinOpWrapFunction( CGBinOpNEq ) )
	_wrapInLT = CVBCharInputHandlerMethod( '<' )( _makeBinOpWrapFunction( CGBinOpLT ) )
	_wrapInGT = CVBCharInputHandlerMethod( '>' )( _makeBinOpWrapFunction( CGBinOpGT ) )
	_wrapInLTE = CVBAccelInputHandlerMethod( '<alt>comma' )( _makeBinOpWrapFunction( CGBinOpLTE ) )
	_wrapInGTE = CVBAccelInputHandlerMethod( '<alt>period' )( _makeBinOpWrapFunction( CGBinOpGTE ) )

