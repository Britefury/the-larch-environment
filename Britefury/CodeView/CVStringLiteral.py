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

from Britefury.CodeViewTree.CVTStringLiteral import CVTStringLiteral

from Britefury.CodeView.CVExpression import *

from Britefury.CodeViewBehavior.CVBStringLiteralBehavior import *

from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection
from Britefury.DocPresent.CellEdit.DPCStringCellEditEntryLabel import DPCStringCellEditEntryLabel







class CVStringLiteral (CVExpression):
	treeNodeClass = CVTStringLiteral


	treeNode = SheetRefField( CVTStringLiteral )


	behaviors = [ CVBStringLiteralBehavior() ]



	@FunctionRefField
	def stringValueWidget(self):
		entry = DPCStringCellEditEntryLabel( regexp='[^\']*' )
		entry.entry.textColour=Colour3f( 0.0, 0.0, 0.75 )
		entry.keyHandler = self
		entry.attachCell( self.treeNode.cells.stringValue )
		entry.finishSignal.connect( self._p_onEntryFinish )
		return entry.entry





	@FunctionField
	def refreshCell(self):
		self._box[1] = self.stringValueWidget




	def __init__(self, treeNode, view):
		super( CVStringLiteral, self ).__init__( treeNode, view )
		self._box = DTBox()
		self._box.append( DTLabel( '\'' ) )
		self._box.append( DTLabel( 'nil', colour=Colour3f( 0.0, 0.0, 0.5 ) ) )
		self._box.append( DTLabel( '\'' ) )
		self.widget.child = self._box


	def closeString(self):
		self.stringValueWidget.finishEditing()
		self.widget.grabFocus()


	def startEditingOnLeft(self):
		self.stringValueWidget.startEditingOnLeft()

	def startEditingOnRight(self):
		self.stringValueWidget.startEditingOnRight()

	def startEditingAtPosition(self, pos):
		widgetPos = self.widget.getPointRelativeTo( self.stringValueWidget, pos )
		self.stringValueWidget.startEditingAtPositionX( widgetPos.x )

	def getCursorPosition(self):
		return self.stringValueWidget.getPointRelativeTo( self.widget, self.stringValueWidget.getCursorPosition() )


	def _p_onEntryFinish(self, entry, text, bUserEvent):
		if bUserEvent:
			self.cursorRight()
