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

import string


from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTNullExpression import CVTNullExpression

from Britefury.CodeView.CVNode import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTEntryLabel import DTEntryLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection



class CVNullExpression (CVNode):
	treeNodeClass = CVTNullExpression


	treeNode = SheetRefField( CVTNullExpression )


	def _refreshCell(self):
		pass

	refreshCell = FunctionField( _refreshCell )



	@CVCharInputHandlerMethod( '\'' )
	def _replaceWithStringLiteral(self, receivingNodePath, entry, event):
		strLitCVT = self.treeNode.replaceWithStringLiteral()
		self._view.refresh()
		strLitCV = self._view.getViewNodeForTreeNode( strLitCVT )
		strLitCV.stringValueWidget.startEditing()
		return True


	@CVCharInputHandlerMethod( string.ascii_letters )
	def _replaceWithRef(self, receivingNodePath, entry, event):
		unboundRefCVT = self.treeNode.replaceWithUnboundRef()
		unboundRefCVT.targetName = event.keyString
		self._view.refresh()
		unboundRefCV = self._view.getViewNodeForTreeNode( unboundRefCVT )
		unboundRefCV.startEditing()
		return True



	def __init__(self, treeNode, view):
		super( CVNullExpression, self ).__init__( treeNode, view )
		self.widget = DTEntryLabel( '<nil>', font='Sans italic 11' )
		self.widget.bEditable = False
		self.widget.keyHandler = self



	def startEditing(self):
		self.widget.startEditing()
