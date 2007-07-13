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

from Britefury.CodeViewTree.CVTDef import CVTDef

from Britefury.CodeView.CVStatement import *
from Britefury.CodeView.CodeView import *

from Britefury.CodeViewBehavior.CVBDefBehavior import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTBorder import DTBorder
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVDef (CVStatement):
	treeNodeClass = CVTDef


	treeNode = SheetRefField( CVTDef )


	behaviors = [ CVBDefBehavior() ]



	@FunctionRefField
	def docNode(self):
		return self._view.buildView( self.treeNode.docNode, self )

	@FunctionRefField
	def docWidget(self):
		return self.docNode.widget



	@FunctionRefField
	def declVarNode(self):
		return self._view.buildView( self.treeNode.declVarNode, self )

	@FunctionRefField
	def declVarWidget(self):
		return self.declVarNode.widget



	@FunctionRefField
	def paramsNode(self):
		return self._view.buildView( self.treeNode.paramsNode, self )

	@FunctionRefField
	def paramsWidget(self):
		return self.paramsNode.widget



	@FunctionRefField
	def blockNode(self):
		return self._view.buildView( self.treeNode.blockNode, self )

	@FunctionRefField
	def blockWidget(self):
		return self.blockNode.widget



	#@FunctionField
	#def _refreshDoc(self):
	#	self._box[0] = self.docWidget

	@FunctionField
	def _refreshDeclVar(self):
		self._declBox[1] = self.declVarWidget

	@FunctionField
	def _refreshParams(self):
		self._declBox[2] = self.paramsWidget

	@FunctionField
	def _refreshBlock(self):
		self._blockBorder.child = self.blockWidget


	@FunctionField
	def refreshCell(self):
		#self._refreshDoc
		self._refreshDeclVar
		self._refreshParams
		self._refreshBlock

		boxContents = []
		if codeViewSettings.bShowDoc:
			boxContents.append( self.docWidget )
		boxContents.append( self._declBox )
		if codeViewSettings.bShowCode:
			boxContents.append( self._blockBorder )
		self._box[:] = boxContents




	def __init__(self, treeNode, view):
		super( CVDef, self ).__init__( treeNode, view )
		self._declBox = DTBox( spacing=5.0 )
		self._declBox.append( DTLabel( markup=_( 'D<span size="small">EF</span>' ), font='Sans bold 11', colour=Colour3f( 0.0, 0.5, 0.0 ) ) )
		self._declBox.append( DTLabel( 'nil' ) )
		self._declBox.append( DTLabel( 'nil' ) )
		self._declBox.append( DTLabel( ':', font='Sans bold 11' ) )
		self._declBox.backgroundColour = Colour3f( 0.825, 0.925, 0.825 )
		self._blockBorder = DTBorder( leftMargin=30.0 )
		self._blockBorder.child = DTLabel( 'nil' )
		self._box = DTBox( spacing=5.0, direction=DTDirection.TOP_TO_BOTTOM, minorDirectionAlignment=DTBox.ALIGN_EXPAND )
		self._box.append( DTLabel( 'nil' ) )
		self._box.append( self._declBox )
		self._box.append( self._blockBorder )
		self.widget.child = self._box
		self.widget.borderColour = Colour3f( 0.6, 0.8, 0.6 )



	def startEditingParameters(self):
		self.paramsNode.startEditing()

	def startEditingBlock(self):
		self.blockNode.startEditing()


	def startEditing(self):
		self.declVarNode.startEditing()


	def horizontalNavigationList(self):
		return [ self.docNode, self.declVarNode, self.paramsNode, self.blockNode ]

	def verticalNavigationList(self):
		return [ self.docNode, self.declVarNode, self.blockNode ]



	def deleteChild(self, child, moveFocus):
		if child is self.declVarNode:
			self.deleteNode( moveFocus )




