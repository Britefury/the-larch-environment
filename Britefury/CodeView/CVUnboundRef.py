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

import string

from Britefury.Math.Math import Colour3f

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTUnboundRef import CVTUnboundRef

from Britefury.CodeView.CVExpression import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVUnboundRef (CVExpression):
	treeNodeClass = CVTUnboundRef


	treeNode = SheetRefField( CVTUnboundRef )


	@FunctionRefField
	def targetNameWidget(self):
		entry = DVCStringCellEditEntryLabel()
		entry.entry.textColour = Colour3f( 0.5, 0.0, 0.0 )
		entry.entry.highlightedTextColour = Colour3f( 1.0, 0.5, 0.5 )
		entry.keyHandler = self
		entry.attachCell( self.treeNode.cells.targetName )
		entry.finishSignal.connect( self._p_onEntryFinish )
		entry.grabChars = string.ascii_letters + '_'
		return entry.entry


	@FunctionField
	def _refreshTargetName(self):
		self.widget = self.targetNameWidget

	@FunctionField
	def refreshCell(self):
		self._refreshTargetName



	def __init__(self, treeNode, view):
		super( CVUnboundRef, self ).__init__( treeNode, view )
		self.widget = None



	def startEditing(self):
		self.targetNameWidget.startEditing()



	def _p_onEntryFinish(self, entry, text):
		self._replaceWithRef()


	def _replaceWithRef(self):
		refCVT = self.treeNode.replaceWithRef()
		if refCVT is not None:
			self._view.refresh()
			refCV = self._view.getViewNodeForTreeNode( refCVT )
			return refCV
		else:
			return None

