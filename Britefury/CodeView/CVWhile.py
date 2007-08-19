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

from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTBorder import DTBorder
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection
from Britefury.DocPresent.CellEdit.DPCStringCellEditEntryLabel import DPCStringCellEditEntryLabel



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



	@CVChildNodeSlotFunctionField
	def blockNode(self):
		return self._view.buildView( self.treeNode.blockNode, self )

	@FunctionRefField
	def blockWidget(self):
		return self.blockNode.widget



	@FunctionRefField
	def elseBlockNode(self):
		if self.treeNode.elseBlockNode is not None:
			return self._view.buildView( self.treeNode.elseBlockNode, self )
		else:
			return None

	@FunctionRefField
	def elseBlockWidget(self):
		if self.elseBlockNode is not None:
			return self.elseBlockNode.widget
		else:
			return None



	@FunctionField
	def _refreshWhileExpr(self):
		self._whileBox[1] = self.whileExprWidget

	@FunctionField
	def _refreshBlock(self):
		self._blockBorder.child = self.blockWidget

	@FunctionField
	def _refreshElseBlock(self):
		elseBlock = self.elseBlockWidget
		if elseBlock is not None:
			self._elseBin.child = elseBlock
		else:
			self._elseBin.child = None


	@FunctionField
	def refreshCell(self):
		self._refreshWhileExpr
		self._refreshBlock
		self._refreshElseBlock




	def __init__(self, treeNode, view):
		super( CVWhile, self ).__init__( treeNode, view )
		self._elseBin = DTBin()

		self._whileBox = DTBox( spacing=5.0 )
		self._whileBox.append( DTLabel( markup=_( 'W<span size="small">HILE</span>' ), font='Sans bold 11', colour=Colour3f( 0.0, 0.5, 0.0 ) ) )
		self._whileBox.append( DTLabel( 'nil' ) )
		self._whileBox.append( DTLabel( ':' ) )
		self._whileBox.backgroundColour = Colour3f( 1.0, 1.0, 0.75 )
		self._blockBorder = DTBorder( leftMargin=30.0 )
		self._blockBorder.child = DTLabel( 'nil' )
		self._box = DTBox( spacing=5.0, direction=DTDirection.TOP_TO_BOTTOM, minorDirectionAlignment=DTBox.ALIGN_EXPAND )
		self._box.append( self._whileBox )
		self._box.append( self._blockBorder )
		self._box.append( self._elseBin )
		self.widget.child = self._box



	def startEditingBlock(self):
		self.blockNode.startEditing()


	def startEditing(self):
		self.whileExprNode.startEditing()


	def verticalNavigationList(self):
		if self.elseBlockNode is not None:
			return [ self.whileExprNode, self.blockNode, self.elseBlockNode ]
		else:
			return [ self.whileExprNode, self.blockNode ]



	def deleteChild(self, child, moveFocus):
		if child is self.whileExprNode:
			self.whileExprNode.treeNode.replaceWithNullExpression()
			self.refresh()
			self.whileExprNode.startEditing()
		elif child is self.elseBlockNode:
			self._o_moveFocus( moveFocus )
			self.treeNode.removeElse()



	def addElse(self):
		elseBlockCVT = self.treeNode.addElse()
		elseBlockCV = self._view.buildView( elseBlockCVT, self )
		elseBlockCV.startEditing()



