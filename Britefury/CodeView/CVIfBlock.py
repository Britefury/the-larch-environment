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

from Britefury.CodeViewTree.CVTIfBlock import CVTIfBlock

from Britefury.CodeView.CVBorderNode import *

from Britefury.CodeViewBehavior.CVBIfBlockBehavior import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTBorder import DTBorder
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVIfBlock (CVBorderNode):
	treeNodeClass = CVTIfBlock


	treeNode = SheetRefField( CVTIfBlock )


	behaviors = [ CVBIfBlockBehavior() ]



	@FunctionRefField
	def conditionNode(self):
		return self._view.buildView( self.treeNode.conditionNode, self )

	@FunctionRefField
	def conditionWidget(self):
		return self.conditionNode.widget



	@FunctionRefField
	def statementsNode(self):
		return self._view.buildView( self.treeNode.statementsNode, self )

	@FunctionRefField
	def statementsWidget(self):
		return self.statementsNode.widget



	@FunctionField
	def _refreshCondition(self):
		self._ifBox[1] = self.conditionWidget

	@FunctionField
	def _refreshStatements(self):
		self._statementsBorder.child = self.statementsWidget


	@FunctionField
	def refreshCell(self):
		self._refreshCondition
		self._refreshStatements




	def __init__(self, treeNode, view):
		super( CVIfBlock, self ).__init__( treeNode, view )
		self._ifBox = DTBox( spacing=5.0 )
		self._ifBox.append( self._o_makeTitleLabel() )
		self._ifBox.append( DTLabel( 'nil' ) )
		self._ifBox.append( DTLabel( ':', font='Sans bold 11' ) )
		self._ifBox.backgroundColour = Colour3f( 1.0, 1.0, 0.75 )
		self._statementsBorder = DTBorder( leftMargin=30.0 )
		self._statementsBorder.child = DTLabel( 'nil' )
		self._box = DTBox( spacing=5.0, direction=DTDirection.TOP_TO_BOTTOM, minorDirectionAlignment=DTBox.ALIGN_EXPAND )
		self._box.append( self._ifBox )
		self._box.append( self._statementsBorder )
		self.widget.child = self._box



	def startEditingStatements(self):
		self.statementsNode.startEditing()


	def startEditing(self):
		self.conditionNode.startEditing()


	def horizontalNavigationList(self):
		return [ self.conditionNode, self.statementsNode ]



	def deleteChild(self, child, moveFocus):
		if child is self.conditionNode:
			self.deleteNode( moveFocus )



	def _o_makeTitleLabel(self):
		return DTLabel( markup=_( 'I<span size="small">F</span>' ), font='Sans bold 11', colour=Colour3f( 0.0, 0.5, 0.0 ) )

