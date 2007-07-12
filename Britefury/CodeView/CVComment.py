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

from Britefury.CodeViewTree.CVTComment import CVTComment

from Britefury.CodeView.CVStatement import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.CellEdit.DVCStringCellEditMultilineLabel import DVCStringCellEditMultilineLabel







class CVComment (CVStatement):
	treeNodeClass = CVTComment


	treeNode = SheetRefField( CVTComment )


	@FunctionRefField
	def textWidget(self):
		entry = DVCStringCellEditMultilineLabel( bMarkup=True )
		entry.attachCell( self.treeNode.cells.text )
		entry.finishSignal.connect( self._p_onEntryFinish )
		return entry.entry





	@FunctionField
	def refreshCell(self):
		self.widget.child = self.textWidget
		self.widget.backgroundColour = Colour3f( 0.85, 0.85, 0.85 )




	def __init__(self, treeNode, view):
		super( CVComment, self ).__init__( treeNode, view )
		self.widget.child = DTLabel( 'nil' )


	def startEditing(self):
		self.textWidget.startEditing()


	def startEditingOnLeft(self):
		self.textWidget.startEditingOnLeft()

	def startEditingOnRight(self):
		self.textWidget.startEditingOnRight()



	def _p_onEntryFinish(self, entry, text):
		pass
		#self.cursorRight()
