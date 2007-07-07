##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeViewBehavior.CodeViewBehavior import *


class CVBWrapInAssignmentBehavior (CodeViewBehavior):
	@CVBCharInputHandlerMethod( '=' )
	def _wrapInAssignment(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		localAssignCVT = viewNode.treeNode.wrapInAssignment()
		if localAssignCVT is not None:
			viewNode._view.refresh()
			localAssignCV = viewNode._view.getViewNodeForTreeNode( localAssignCVT )
			localAssignCV.startEditingValue()
		viewNode._f_commandHistoryThaw()
		return True



