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

from Britefury.CodeViewTree.CVTParameterDoc import CVTParameterDoc

from Britefury.CodeView.CVNode import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVParameterDoc (CVNode):
	treeNodeClass = CVTParameterDoc


	treeNode = SheetRefField( CVTParameterDoc )


	@FunctionRefField
	def nameWidget(self):
		return DTLabel( self.treeNode.varName, font='Sans bold 11' )

	@FunctionRefField
	def paramDocWidget(self):
		entry = DVCStringCellEditEntryLabel()
		entry.keyHandler = self
		entry.attachCell( self.treeNode.cells.paramDoc )
		return entry.entry


	@FunctionField
	def _refreshName(self):
		self.widget[0] = self.nameWidget

	@FunctionField
	def _refreshParamDoc(self):
		self.widget[2] = ( self.paramDocWidget, True, True, True, DTBox.ALIGN_CENTRE, 0.0 )

	@FunctionField
	def refreshCell(self):
		self._refreshName
		self._refreshParamDoc


	def __init__(self, treeNode, view):
		super( CVParameterDoc, self ).__init__( treeNode, view )
		self.widget = DTBox()
		self.widget.append( DTLabel( 'nil' ) )
		self.widget.append( DTLabel( '-' ), padding=5.0 )
		self.widget.append( DTLabel( 'nil' ) )



	def startEditing(self):
		self.paramDocWidget.startEditing()

	def finishEditing(self):
		self.paramDocWidget.finishEditing()


	def startEditingOnLeft(self):
		self.paramDocWidget.startEditingOnLeft()

	def startEditingOnRight(self):
		self.paramDocWidget.startEditingOnRight()

	def startEditingAtPosition(self, pos):
		widgetPos = self.widget.getPointRelativeTo( self.paramDocWidget, pos )
		self.paramDocWidget.startEditingAtPositionX( widgetPos.x )

	def getCursorPosition(self):
		return self.paramDocWidget.getPointRelativeTo( self.widget, self.paramDocWidget.getCursorPosition() )


	def makeCurrent(self):
		self.startEditing()
