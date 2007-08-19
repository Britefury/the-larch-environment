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

from Britefury.CodeViewTree.CVTIf import CVTIf

from Britefury.CodeView.CVStatement import *

from Britefury.CodeViewBehavior.CVBIfBehavior import *

from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTBorder import DTBorder
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection
from Britefury.DocPresent.CellEdit.DPCStringCellEditEntryLabel import DPCStringCellEditEntryLabel



class CVIf (CVStatement):
	treeNodeClass = CVTIf


	treeNode = SheetRefField( CVTIf )


	behaviors = [ CVBIfBehavior() ]



	@FunctionRefField
	def ifBlockNode(self):
		return self._view.buildView( self.treeNode.ifBlockNode, self )

	@FunctionRefField
	def ifBlockWidget(self):
		return self.ifBlockNode.widget



	@FunctionField
	def elseIfBlockNodes(self):
		return [ self._view.buildView( elseIfBlockNode, self )   for elseIfBlockNode in self.treeNode.elseIfBlockNodes ]

	@FunctionField
	def elseIfBlockWidgets(self):
		return [ node.widget   for node in self.elseIfBlockNodes ]



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
	def _refreshIfBlock(self):
		self._box[0] = self.ifBlockWidget

	@FunctionField
	def _refreshElseIfs(self):
		self._box[1:-1] = self.elseIfBlockWidgets

	@FunctionField
	def _refreshElseBlock(self):
		elseBlock = self.elseBlockWidget
		if elseBlock is not None:
			self._elseBin.child = elseBlock
		else:
			self._elseBin.child = None


	@FunctionField
	def refreshCell(self):
		self._refreshIfBlock
		self._refreshElseIfs
		self._refreshElseBlock




	def __init__(self, treeNode, view):
		super( CVIf, self ).__init__( treeNode, view )
		self._elseBin = DTBin()

		self._box = DTBox( spacing=5.0, direction=DTDirection.TOP_TO_BOTTOM, minorDirectionAlignment=DTBox.ALIGN_EXPAND )
		self._box.append( DTLabel( 'nil' ) )
		self._box.append( self._elseBin )

		self.widget.child = self._box



	def startEditing(self):
		self.ifBlockNode.startEditing()


	def verticalNavigationList(self):
		if self.elseBlockNode is not None:
			return [ self.ifBlockNode ]  +  self.elseIfBlockNodes  +  [ self.elseBlockNode ]
		else:
			return [ self.ifBlockNode ]  +  self.elseIfBlockNodes



	def addElseIf(self, receivingNodePath):
		ifBlock = receivingNodePath[1]
		if ifBlock is self.ifBlockNode:
			elseIfCVT = self.treeNode.addElseIf( 0 )
		else:
			try:
				n = self.elseIfBlockNodes.index( ifBlock ) + 1
			except ValueError:
				n = len( self.elseIfBlockNodes )
			elseIfCVT = self.treeNode.addElseIf( n )
		elseIfCV = self._view.buildView( elseIfCVT, self )
		elseIfCV.startEditing()


	def addElse(self):
		elseBlockCVT = self.treeNode.addElse()
		elseBlockCV = self._view.buildView( elseBlockCVT, self )
		elseBlockCV.startEditing()



	def deleteChild(self, child, moveFocus):
		if child is self.elseBlockNode:
			self._o_moveFocus( moveFocus )
			self.treeNode.removeElse()
		elif child is self.ifBlockNode:
			if self.treeNode.hasElseIfs():
				self._o_moveFocus( moveFocus )
				self.treeNode.removeIf()
			else:
				self.deleteNode( moveFocus )
		elif child in self.elseIfBlockNodes:
			self._o_moveFocus( moveFocus )
			self.treeNode.removeElseIf( child.treeNode )




