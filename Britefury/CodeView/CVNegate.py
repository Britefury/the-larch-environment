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

from Britefury.Math.Math import Colour3f

from Britefury.Util import RegExpStrings

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTNegate import CVTNegate

from Britefury.CodeView.CVExpression import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.Toolkit.DTLabel import DTLabel



class CVNegate (CVExpression):
	treeNodeClass = CVTNegate


	treeNode = SheetRefField( CVTNegate )



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
		super( CVNegate, self ).__init__( treeNode, view )
		self._box = DTBox( spacing=5.0 )
		self._box.append( DTLabel( '-', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) ) )
		self._box.append( DTLabel( 'nil' ) )
		self.widget.child = self._box



	def deleteChild(self, child, moveFocus):
		if child is self.exprNode:
			self.exprNode.treeNode.replaceWithNullExpression()
			self._view.refresh()
			self.exprNode.startEditing()


	def deleteNode(self, moveFocus):
		self.treeNode.unwrapNegate()
		self._view.refresh()
		self.exprNode.startEditing()



	def startEditingExpression(self):
		self.exprNode.startEditing()



	def horizontalNavigationList(self):
		return [ self.exprNode ]
