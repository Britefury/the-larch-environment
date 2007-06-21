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

from Britefury.Util import RegExpStrings

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTNot import CVTNot

from Britefury.CodeView.CVExpression import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.Toolkit.DTLabel import DTLabel



class CVNot (CVExpression):
	treeNodeClass = CVTNot


	treeNode = SheetRefField( CVTNot )



	@FunctionRefField
	def exprNode(self):
		if self.treeNode.exprNode is not None:
			return self._view.buildView( self.treeNode.exprNode, self )
		else:
			return None

	@FunctionRefField
	def exprWidget(self):
		if self.exprNode is not None:
			return self.exprNode.widget
		else:
			return None



	@FunctionField
	def _refreshExpr(self):
		if self.exprWidget is not None:
			self._box[1] = self.exprWidget
		else:
			self._box[1] = DTLabel( 'nil' )

	@FunctionField
	def refreshCell(self):
		self._refreshExpr






	def __init__(self, treeNode, view):
		super( CVNot, self ).__init__( treeNode, view )
		self._box = DTBox( spacing=5.0 )
		self._box.append( DTLabel( 'not', font='Sans bold 11' ) )
		self._box.append( DTLabel( 'nil' ) )
		self.widget.child = self._box



	def deleteChild(self, child, moveFocus):
		if child is self.targetObjectNode:
			self.exprNode.treeNode.replaceWithNullExpression()
			self._view.refresh()
			self.exprNode.startEditing()


	def deleteNode(self, moveFocus):
		self.treeNode.unwrapNot()
		self._view.refresh()
		self.exprNode.startEditing()



	def startEditingExpression(self):
		self.exprNode.startEditing()



	def horizontalNavigationList(self):
		return [ self.exprNode ]
