##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeViewBehavior.CodeViewBehavior import *


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
	def _wrapInList(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.wrapInList()
		viewNode._f_commandHistoryThaw()
		return True

