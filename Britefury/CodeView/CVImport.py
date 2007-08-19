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

from Britefury.CodeViewTree.CVTImport import CVTImport

from Britefury.CodeView.CVStatement import *

from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection
from Britefury.DocPresent.CellEdit.DPCStringCellEditEntryLabel import DPCStringCellEditEntryLabel



class CVImport (CVStatement):
	treeNodeClass = CVTImport


	treeNode = SheetRefField( CVTImport )


	@FunctionRefField
	def moduleNameWidget(self):
		entry = DPCStringCellEditEntryLabel( regexp=RegExpStrings.identifier )
		entry.entry.textColour = Colour3f( 0.0, 0.0, 0.0 )
		entry.entry.font = 'Sans bold 11'
		entry.keyHandler = self
		entry.attachCell( self.treeNode.cells.moduleName )
		entry.finishSignal.connect( self._p_onEntryFinish )
		return entry.entry


	@FunctionField
	def refreshCell(self):
		self._box[1] = self.moduleNameWidget




	def __init__(self, treeNode, view):
		super( CVImport, self ).__init__( treeNode, view )
		self._box = DTBox( spacing=10.0 )
		self._box.append( DTLabel( markup=_( 'I<span size="small">MPORT</span>' ), font='Sans bold 11', colour=Colour3f( 0.0, 0.5, 0.0 ) ) )
		self._box.append( DTLabel( 'nil' ) )
		self.widget.child = self._box



	def startEditing(self):
		self.moduleNameWidget.startEditing()

	def startEditingOnLeft(self):
		self.moduleNameWidget.startEditingOnLeft()

	def startEditingOnRight(self):
		self.moduleNameWidget.startEditingOnRight()

	def startEditingAtPosition(self, pos):
		widgetPos = self.widget.getPointRelativeTo( self.moduleNameWidget, pos )
		self.moduleNameWidget.startEditingAtPositionX( widgetPos.x )

	def getCursorPosition(self):
		return self.moduleNameWidget.getPointRelativeTo( self.widget, self.moduleNameWidget.getCursorPosition() )



	def _p_onEntryFinish(self, entry, text, bUserEvent):
		if bUserEvent:
			self.cursorRight()
