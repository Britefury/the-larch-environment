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

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTWhile import CVTWhile

from Britefury.CodeView.CVStatement import *

from Britefury.CodeViewBehavior.CVBWhileBehavior import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTBorder import DTBorder
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVWhile (CVStatement):
	treeNodeClass = CVTWhile


	treeNode = SheetRefField( CVTWhile )


	behaviors = [ CVBWhileBehavior() ]



	@FunctionRefField
	def whileExprNode(self):
		return self._view.buildView( self.treeNode.whileExprNode, self )

	@FunctionRefField
	def whileExprWidget(self):
		return self.whileExprNode.widget



	@FunctionRefField
	def statementsNode(self):
		return self._view.buildView( self.treeNode.statementsNode, self )

	@FunctionRefField
	def statementsWidget(self):
		return self.statementsNode.widget



	@FunctionField
	def _refreshWhileExpr(self):
		self._whileBox[1] = self.whileExprWidget

	@FunctionField
	def _refreshStatements(self):
		self._statementsBorder.child = self.statementsWidget


	@FunctionField
	def refreshCell(self):
		self._refreshWhileExpr
		self._refreshStatements




	def __init__(self, treeNode, view):
		super( CVWhile, self ).__init__( treeNode, view )
		self._whileBox = DTBox( spacing=5.0 )
		self._whileBox.append( DTLabel( markup='W<span size="small">HILE</span>', font='Sans bold 11', colour=Colour3f( 0.0, 0.5, 0.0 ) ) )
		self._whileBox.append( DTLabel( 'nil' ) )
		self._whileBox.append( DTLabel( ':' ) )
		self._statementsBorder = DTBorder( leftMargin=30.0 )
		self._statementsBorder.child = DTLabel( 'nil' )
		self._box = DTBox( spacing=5.0, direction=DTDirection.TOP_TO_BOTTOM, minorDirectionAlignment=DTBox.ALIGN_EXPAND )
		self._box.append( self._whileBox )
		self._box.append( self._statementsBorder )
		self.widget.child = self._box



	def startEditingStatements(self):
		self.statementsNode.startEditing()


	def startEditing(self):
		self.whileExprNode.startEditing()


	def horizontalNavigationList(self):
		return [ self.whileExprNode, self.statementsNode ]

	def verticalNavigationList(self):
		return [ self.whileExprNode, self.statementsNode ]



	def deleteChild(self, child, moveFocus):
		if child is self.whileExprNode:
			self.whileExprNode.treeNode.replaceWithNullExpression()
			self._view.refresh()
			self.whileExprNode.startEditing()




