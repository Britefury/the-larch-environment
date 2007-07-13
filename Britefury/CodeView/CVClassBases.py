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

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTClassBases import CVTClassBases

from Britefury.CodeView.CVBorderNode import *

from Britefury.CodeViewBehavior.CVBClassBasesBehavior import *
from Britefury.CodeViewBehavior.CVBCreateExpressionBehavior import *

from Britefury.DocView.Toolkit.DTWrappedLineWithSeparators import DTWrappedLineWithSeparators
from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVClassBases (CVBorderNode):
	treeNodeClass = CVTClassBases


	treeNode = SheetRefField( CVTClassBases )



	behaviors = [ CVBClassBasesBehavior(), CVBCreateExpressionBehavior() ]



	@FunctionField
	def baseNodes(self):
		return [ self._view.buildView( baseNode, self )   for baseNode in self.treeNode.baseNodes ]

	@FunctionField
	def baseWidgets(self):
		return [ node.widget   for node in self.baseNodes ]


	@FunctionRefField
	def basesWidget(self):
		self._basesLine[:] = self.baseWidgets
		return self._basesLine



	@FunctionField
	def _refreshBases(self):
		self.basesWidget

	@FunctionField
	def refreshCell(self):
		self._refreshBases




	def __init__(self, treeNode, view):
		super( CVClassBases, self ).__init__( treeNode, view )
		self._basesLine = DTWrappedLineWithSeparators( spacing=5.0 )
		self._box = DTBox()
		self._box.append( DTLabel( '(' ) )
		self._box.append( self._basesLine )
		self._box.append( DTLabel( ')' ) )
		self.widget.child = self._box



	def addBase(self):
		baseCVT = self.treeNode.addBase()
		self.refresh()
		baseCV = self._view.getViewNodeForTreeNode( baseCVT )
		baseCV.startEditing()


	def deleteChild(self, child, moveFocus):
		child._o_moveFocus( moveFocus )
		self.treeNode.deleteBase( child.treeNode )



	def startEditing(self):
		self.widget.grabFocus()



	def horizontalNavigationList(self):
		return self.baseNodes



	def getInsertPosition(self, receivingNodePath):
		if len( receivingNodePath ) > 1:
			child = receivingNodePath[1]
			try:
				return self.baseNodes.index( child )
			except ValueError:
				return 0
		return 0
