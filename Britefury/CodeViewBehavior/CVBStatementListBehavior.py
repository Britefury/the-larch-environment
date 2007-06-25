##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeViewBehavior.CodeViewBehavior import *

from Britefury.CodeViewTreeOperations.CVTOStatementListOperations import *


class CVBStatementListBehavior (CodeViewBehavior):
	@staticmethod
	def _p_getPosition(viewNode, receivingNodePath):
		return viewNode.getInsertPosition( receivingNodePath )


	@CVBAccelInputHandlerMethod( '<alt>r' )
	def _addReturn(self, viewNode, receivingNodePath, widget, event):
		position = CVBStatementListBehavior._p_getPosition( viewNode, receivingNodePath )
		viewNode._f_commandHistoryFreeze()
		cvto_addReturnStatement( viewNode.treeNode, position )
		localVarCV = viewNode.statementNodes[position]
		localVarCV.startEditing()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBAccelInputHandlerMethod( '<alt>v' )
	def _addLocalVar(self, viewNode, receivingNodePath, widget, event):
		position = CVBStatementListBehavior._p_getPosition( viewNode, receivingNodePath )
		viewNode._f_commandHistoryFreeze()
		cvto_addLocalVarStatement( viewNode.treeNode, position )
		localVarCV = viewNode.statementNodes[position]
		localVarCV.startEditing()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBAccelInputHandlerMethod( '<alt>i' )
	def _addIf(self, viewNode, receivingNodePath, widget, event):
		position = CVBStatementListBehavior._p_getPosition( viewNode, receivingNodePath )
		viewNode._f_commandHistoryFreeze()
		cvto_addIfStatement( viewNode.treeNode, position )
		ifCV = viewNode.statementNodes[position]
		ifCV.startEditing()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBAccelInputHandlerMethod( '<alt>w' )
	def _addWhile(self, viewNode, receivingNodePath, widget, event):
		position = CVBStatementListBehavior._p_getPosition( viewNode, receivingNodePath )
		viewNode._f_commandHistoryFreeze()
		cvto_addWhileStatement( viewNode.treeNode, position )
		whileCV = viewNode.statementNodes[position]
		whileCV.startEditing()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBAccelInputHandlerMethod( '<alt>d' )
	def _addDef(self, viewNode, receivingNodePath, widget, event):
		position = CVBStatementListBehavior._p_getPosition( viewNode, receivingNodePath )
		viewNode._f_commandHistoryFreeze()
		cvto_addDefStatement( viewNode.treeNode, position )
		defCV = viewNode.statementNodes[position]
		defCV.startEditing()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBAccelInputHandlerMethod( '<alt>c' )
	def _addClass(self, viewNode, receivingNodePath, widget, event):
		position = CVBStatementListBehavior._p_getPosition( viewNode, receivingNodePath )
		viewNode._f_commandHistoryFreeze()
		cvto_addClassStatement( viewNode.treeNode, position )
		defCV = viewNode.statementNodes[position]
		defCV.startEditing()
		viewNode._f_commandHistoryThaw()
		return True
