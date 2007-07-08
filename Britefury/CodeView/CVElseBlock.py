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

from Britefury.CodeViewTree.CVTElseBlock import CVTElseBlock

from Britefury.CodeView.CVBorderNode import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTBorder import DTBorder
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVElseBlock (CVBorderNode):
	treeNodeClass = CVTElseBlock


	treeNode = SheetRefField( CVTElseBlock )




	@FunctionRefField
	def statementsNode(self):
		return self._view.buildView( self.treeNode.statementsNode, self )

	@FunctionRefField
	def statementsWidget(self):
		return self.statementsNode.widget



	@FunctionField
	def _refreshStatements(self):
		self._statementsBorder.child = self.statementsWidget


	@FunctionField
	def refreshCell(self):
		self._refreshStatements




	def __init__(self, treeNode, view):
		super( CVElseBlock, self ).__init__( treeNode, view )
		self._elseBin = DTBin()
		self._elseBin.child = DTLabel( markup=_( 'E<span size="small">LSE:</span>' ), font='Sans bold 11', colour=Colour3f( 0.0, 0.5, 0.0 ) )
		self._elseBin.backgroundColour = Colour3f( 1.0, 1.0, 0.75 )
		self._statementsBorder = DTBorder( leftMargin=30.0 )
		self._statementsBorder.child = DTLabel( 'nil' )
		self._box = DTBox( spacing=5.0, direction=DTDirection.TOP_TO_BOTTOM )
		self._box.append( self._elseBin, minorDirectionAlignment=DTBox.ALIGN_LEFT )
		self._box.append( self._statementsBorder, minorDirectionAlignment=DTBox.ALIGN_EXPAND )
		self.widget.child = self._box



	def startEditingStatements(self):
		self.statementsNode.startEditing()



	def horizontalNavigationList(self):
		return [ self.statementsNode ]



	def deleteChild(self, child, moveFocus):
		if child is self.statementsNode:
			self.deleteNode( moveFocus )


