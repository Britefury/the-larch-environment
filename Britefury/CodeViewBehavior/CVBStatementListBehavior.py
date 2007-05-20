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
	@CVBAccelInputHandlerMethod( '<alt>v' )
	def _addLocalVar(self, viewNode, receivingNodePath, entry, event):
		if len( receivingNodePath ) > 1:
			position = viewNode.treeNode.statementNodes.index( receivingNodePath[1].treeNode )
		else:
			position = len( viewNode.treeNode.statementNodes )
		cvto_addLocalVarStatement( viewNode.treeNode, position )
		localVarCV = viewNode.statementNodes[position]
		localVarCV.startEditing()
		return True



