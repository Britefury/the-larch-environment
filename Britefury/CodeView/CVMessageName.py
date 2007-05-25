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

from Britefury.CodeViewTree.CVTMessageName import CVTMessageName

from Britefury.CodeView.CVNode import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVMessageName (CVNode):
	treeNodeClass = CVTMessageName


	treeNode = SheetRefField( CVTMessageName )


	@FunctionRefField
	def nameWidget(self):
		entry = DVCStringCellEditEntryLabel( regexp=RegExpStrings.identifier )
		entry.keyHandler = self
		entry.attachCell( self.treeNode.cells.name )
		return entry.entry


	@FunctionField
	def _refreshName(self):
		self.widget = self.nameWidget

	@FunctionField
	def refreshCell(self):
		self._refreshName


	def startEditingOnLeft(self):
		self.widget.startEditingOnLeft()

	def startEditingOnRight(self):
		self.widget.startEditingOnRight()

