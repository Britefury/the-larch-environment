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

from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTBorder import DTBorder
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection
from Britefury.DocPresent.CellEdit.DPCStringCellEditEntryLabel import DPCStringCellEditEntryLabel



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
	def blockNode(self):
		return self._view.buildView( self.treeNode.blockNode, self )

	@FunctionRefField
	def blockWidget(self):
		return self.blockNode.widget



	@FunctionField
	def _refreshDeclVar(self):
		self._declBox[1] = self.declVarWidget

	@FunctionField
	def _refreshBases(self):
		self._declBox[2] = self.basesWidget

	@FunctionField
	def _refreshBlock(self):
		self._blockBorder.child = self.blockWidget


	@FunctionField
	def refreshCell(self):
		self._refreshDeclVar
		self._refreshBases
		self._refreshBlock




	def __init__(self, treeNode, view):
		super( CVClass, self ).__init__( treeNode, view )
		self._declBox = DTBox( spacing=5.0 )
		self._declBox.append( DTLabel( markup=_( 'C<span size="small">LASS</span>' ), font='Sans bold 11', colour=Colour3f( 0.1, 0.3, 0.5 ) ) )
		self._declBox.append( DTLabel( 'nil' ) )
		self._declBox.append( DTLabel( 'nil' ) )
		self._declBox.append( DTLabel( ':', font='Sans bold 11' ) )
		self._declBox.backgroundColour = Colour3f( 0.8, 0.825, 0.85 )
		self._blockBorder = DTBorder( leftMargin=30.0 )
		self._blockBorder.child = DTLabel( 'nil' )
		self._box = DTBox( spacing=5.0, direction=DTDirection.TOP_TO_BOTTOM, minorDirectionAlignment=DTBox.ALIGN_EXPAND )
		self._box.append( self._declBox )
		self._box.append( self._blockBorder )
		self.widget.child = self._box
		self.widget.borderColour = Colour3f( 0.3, 0.5, 0.7 )



	def startEditingBases(self):
		self.declVarNode.finishEditing()
		self.basesNode.startEditing()

	def startEditingBlock(self):
		self.blockNode.startEditing()


	def startEditing(self):
		self.declVarNode.startEditing()


	def horizontalNavigationList(self):
		return [ self.declVarNode, self.basesNode, self.blockNode ]

	def verticalNavigationList(self):
		return [ self.declVarNode, self.blockNode ]



	def deleteChild(self, child, moveFocus):
		if child is self.declVarNode:
			self.deleteNode( moveFocus )




