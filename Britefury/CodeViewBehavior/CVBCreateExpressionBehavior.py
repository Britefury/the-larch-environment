##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import string

from Britefury.CodeViewBehavior.CodeViewBehavior import *

from Britefury.CodeViewTreeOperations.CVTOCreateExpressionOperations import *



class CVBCreateExpressionBehavior (CodeViewBehavior):
	@CVBCharInputHandlerMethod( '\'' )
	def _replaceWithStringLiteral(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		insertPosition = viewNode.getInsertPosition( receivingNodePath )
		strLitCVT = cvto_insertStringLiteral( viewNode.treeNode, insertPosition )
		viewNode.refreshFromParent()
		strLitCV = viewNode._view.getViewNodeForTreeNode( strLitCVT )
		strLitCV.stringValueWidget.startEditing()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBCharInputHandlerMethod( '-' + string.digits )
	def _replaceWithIntLiteral(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		insertPosition = viewNode.getInsertPosition( receivingNodePath )
		intLitCVT = cvto_insertIntLiteral( viewNode.treeNode, insertPosition )
		intLitCVT.strValue = event.keyString
		viewNode.refreshFromParent()
		intLitCV = viewNode._view.getViewNodeForTreeNode( intLitCVT )
		intLitCV.stringValueWidget.startEditing()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBCharInputHandlerMethod( string.ascii_letters + '_' )
	def _replaceWithRef(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		insertPosition = viewNode.getInsertPosition( receivingNodePath )
		unboundRefCVT = cvto_insertUnboundRef( viewNode.treeNode, insertPosition )
		unboundRefCVT.targetName = event.keyString
		viewNode.refreshFromParent()
		unboundRefCV = viewNode._view.getViewNodeForTreeNode( unboundRefCVT )
		unboundRefCV.startEditing()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBAccelInputHandlerMethod( '<alt>minus' )
	def _replaceWithNegate(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		insertPosition = viewNode.getInsertPosition( receivingNodePath )
		negateCVT = cvto_insertNegate( viewNode.treeNode, insertPosition )
		viewNode.refreshFromParent()
		negateCV = viewNode._view.getViewNodeForTreeNode( negateCVT )
		negateCV.startEditingExpression()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBAccelInputHandlerMethod( '<alt>n' )
	def _replaceWithNot(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		insertPosition = viewNode.getInsertPosition( receivingNodePath )
		notCVT = cvto_insertNot( viewNode.treeNode, insertPosition )
		viewNode.refreshFromParent()
		notCV = viewNode._view.getViewNodeForTreeNode( notCVT )
		notCV.startEditingExpression()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBAccelInputHandlerMethod( '<alt>l' )
	def _replaceWithLambda(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		insertPosition = viewNode.getInsertPosition( receivingNodePath )
		lambdaCVT = cvto_insertLambda( viewNode.treeNode, insertPosition )
		viewNode.refreshFromParent()
		lambdaCV = viewNode._view.getViewNodeForTreeNode( lambdaCVT )
		lambdaCV.startEditingParameters()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBAccelInputHandlerMethod( '<alt>t' )
	def _replaceWithTuple(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		insertPosition = viewNode.getInsertPosition( receivingNodePath )
		tupleCVT = cvto_insertTuple( viewNode.treeNode, insertPosition )
		viewNode.refreshFromParent()
		tupleCV = viewNode._view.getViewNodeForTreeNode( tupleCVT )
		tupleCV.startEditing()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBCharInputHandlerMethod( '[' )
	def _replaceWithList(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		insertPosition = viewNode.getInsertPosition( receivingNodePath )
		listCVT = cvto_insertList( viewNode.treeNode, insertPosition )
		viewNode.refreshFromParent()
		listCV = viewNode._view.getViewNodeForTreeNode( listCVT )
		listCV.startEditing()
		viewNode._f_commandHistoryThaw()
		return True
