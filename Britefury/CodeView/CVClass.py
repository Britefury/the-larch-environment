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

from Britefury.CodeViewTree.CVTClass import CVTClass

from Britefury.CodeView.CVStatement import *

from Britefury.CodeViewBehavior.CVBClassBehavior import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTBorder import DTBorder
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVClass (CVStatement):
	treeNodeClass = CVTClass


	treeNode = SheetRefField( CVTClass )


	behaviors = [ CVBClassBehavior() ]



	@FunctionRefField
	def declVarNode(self):
		return self._view.buildView( self.treeNode.declVarNode, self )

	@FunctionRefField
	def declVarWidget(self):
		return self.declVarNode.widget



	@FunctionRefField
	def basesNode(self):
		return self._view.buildView( self.treeNode.basesNode, self )

	@FunctionRefField
	def basesWidget(self):
		return self.basesNode.widget



	@FunctionRefField
	def statementsNode(self):
		return self._view.buildView( self.treeNode.statementsNode, self )

	@FunctionRefField
	def statementsWidget(self):
		return self.statementsNode.widget



	@FunctionField
	def _refreshDeclVar(self):
		self._declBox[1] = self.declVarWidget

	@FunctionField
	def _refreshBases(self):
		self._declBox[2] = self.basesWidget

	@FunctionField
	def _refreshStatements(self):
		self._statementsBorder.child = self.statementsWidget


	@FunctionField
	def refreshCell(self):
		self._refreshDeclVar
		self._refreshBases
		self._refreshStatements




	def __init__(self, treeNode, view):
		super( CVClass, self ).__init__( treeNode, view )
		self._declBox = DTBox( spacing=5.0 )
		self._declBox.append( DTLabel( markup=_( 'C<span size="small">LASS</span>' ), font='Sans bold 11', colour=Colour3f( 0.1, 0.3, 0.5 ) ) )
		self._declBox.append( DTLabel( 'nil' ) )
		self._declBox.append( DTLabel( 'nil' ) )
		self._declBox.append( DTLabel( ':', font='Sans bold 11' ) )
		self._declBox.backgroundColour = Colour3f( 0.8, 0.825, 0.85 )
		self._statementsBorder = DTBorder( leftMargin=30.0 )
		self._statementsBorder.child = DTLabel( 'nil' )
		self._box = DTBox( spacing=5.0, direction=DTDirection.TOP_TO_BOTTOM, minorDirectionAlignment=DTBox.ALIGN_EXPAND )
		self._box.append( self._declBox )
		self._box.append( self._statementsBorder )
		self.widget.child = self._box
		self.widget.borderColour = Colour3f( 0.3, 0.5, 0.7 )



	def startEditingBases(self):
		self.declVarNode.finishEditing()
		self.basesNode.startEditing()

	def startEditingStatements(self):
		self.statementsNode.startEditing()


	def startEditing(self):
		self.declVarNode.startEditing()


	def horizontalNavigationList(self):
		return [ self.declVarNode, self.basesNode, self.statementsNode ]

	def verticalNavigationList(self):
		return [ self.declVarNode, self.statementsNode ]



	def deleteChild(self, child, moveFocus):
		if child is self.declVarNode:
			self.deleteNode( moveFocus )




