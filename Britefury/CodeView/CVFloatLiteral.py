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

from Britefury.Util import RegExpStrings

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTFloatLiteral import CVTFloatLiteral

from Britefury.CodeView.CVExpression import *
from Britefury.CodeView.MoveFocus import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel






def _floatMarkup(text):
	if text.endswith( 'e' ):
		return text.replace( 'e', '' )
	elif 'e' in text:
		return text.replace( 'e', '*10<sup>' ) + '</sup>'
	else:
		return text



class CVFloatLiteral (CVExpression):
	treeNodeClass = CVTFloatLiteral


	treeNode = SheetRefField( CVTFloatLiteral )




	@FunctionRefField
	def stringValueWidget(self):
		entry = DVCStringCellEditEntryLabel( labelFilter=_floatMarkup, bLabelMarkup=True, regexp=RegExpStrings.floatRegex )
		entry.entry.textColour=Colour3f( 0.0, 0.0, 0.75 )
		entry.keyHandler = self
		entry.attachCell( self.treeNode.cells.strValue )
		entry.finishSignal.connect( self._p_onEntryFinish )
		return entry.entry





	@FunctionField
	def refreshCell(self):
		self.widget.child = self.stringValueWidget




	def __init__(self, treeNode, view):
		super( CVFloatLiteral, self ).__init__( treeNode, view )
		self.widget.child = DTLabel( '<nil>' )


	def startEditingOnLeft(self):
		self.stringValueWidget.startEditingOnLeft()

	def startEditingOnRight(self):
		self.stringValueWidget.startEditingOnRight()



	def _p_onEntryFinish(self, entry, text, bUserEvent):
		if text == '':
			self.deleteNode( MoveFocus.RIGHT )
		else:
			if bUserEvent:
				self.cursorRight()
