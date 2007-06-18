##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pygtk
pygtk.require( '2.0' )
import gtk

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTBlockStatements import CVTBlockStatements

from Britefury.CodeView.CVBorderNode import *

from Britefury.CodeViewBehavior.CVBStatementListBehavior import *
from Britefury.CodeViewBehavior.CVBCreateExpressionBehavior import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection



class CVBlockStatements (CVBorderNode):
	treeNodeClass = CVTBlockStatements


	treeNode = SheetRefField( CVTBlockStatements )


	behaviors = [ CVBStatementListBehavior(), CVBCreateExpressionBehavior() ]


	@FunctionField
	def statementNodes(self):
		return [ self._view.buildView( childTreeNode, self )   for childTreeNode in self.treeNode.statementNodes ]

	@FunctionField
	def statementWidgets(self):
		return [ node.widget   for node in self.statementNodes ]


	@FunctionField
	def refreshCell(self):
		self._box[:] = self.statementWidgets




	def __init__(self, treeNode, view):
		super( CVBlockStatements, self ).__init__( treeNode, view )
		self._box = DTBox( DTDirection.TOP_TO_BOTTOM, minorDirectionAlignment=DTBox.ALIGN_TOPLEFT, spacing=4.0 )
		self.widget.child = self._box
		self.widget.bottomMargin = 10.0




	def startEditing(self):
		self.makeCurrent()



	def horizontalNavigationList(self):
		return self.verticalNavigationList()

	def verticalNavigationList(self):
		return self.statementNodes



	def deleteChild(self, child, moveFocus):
		child._o_moveFocus( moveFocus )
		self.treeNode.deleteStatement( child.treeNode )
		return True



	def _o_isExtendable(self):
		return True
