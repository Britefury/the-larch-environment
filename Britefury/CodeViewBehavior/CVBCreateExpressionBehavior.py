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
		strLitCVT = cvto_insertStringLiteral( viewNode.treeNode, [ node.treeNode  for node in receivingNodePath ] )
		viewNode._view.refresh()
		strLitCV = viewNode._view.getViewNodeForTreeNode( strLitCVT )
		strLitCV.stringValueWidget.startEditing()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBCharInputHandlerMethod( string.digits )
	def _replaceWithIntLiteral(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		intLitCVT = cvto_insertIntLiteral( viewNode.treeNode, [ node.treeNode  for node in receivingNodePath ] )
		intLitCVT.strValue = event.keyString
		viewNode._view.refresh()
		intLitCV = viewNode._view.getViewNodeForTreeNode( intLitCVT )
		intLitCV.stringValueWidget.startEditing()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBCharInputHandlerMethod( string.ascii_letters + '_' )
	def _replaceWithRef(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		unboundRefCVT = cvto_insertUnboundRef( viewNode.treeNode, [ node.treeNode  for node in receivingNodePath ] )
		unboundRefCVT.targetName = event.keyString
		viewNode._view.refresh()
		unboundRefCV = viewNode._view.getViewNodeForTreeNode( unboundRefCVT )
		unboundRefCV.startEditing()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBAccelInputHandlerMethod( '<alt>l' )
	def _replaceWithLambda(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		lambdaCVT = cvto_insertLambda( viewNode.treeNode, [ node.treeNode  for node in receivingNodePath ] )
		viewNode._view.refresh()
		lambdaCV = viewNode._view.getViewNodeForTreeNode( lambdaCVT )
		lambdaCV.startEditingParameters()
		viewNode._f_commandHistoryThaw()
		return True

