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

from Britefury.CodeViewTree.CVTList import CVTList

from Britefury.CodeView.CVNode import *
from Britefury.CodeView.CVExpression import *

from Britefury.CodeViewBehavior.CVBListBehavior import *
from Britefury.CodeViewBehavior.CVBCreateExpressionBehavior import *

from Britefury.DocView.Toolkit.DTWrappedLineWithSeparators import DTWrappedLineWithSeparators
from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel





class CVList (CVExpression):
	treeNodeClass = CVTList


	treeNode = SheetRefField( CVTList )



	behaviors = [ CVBListBehavior(), CVBCreateExpressionBehavior() ]



	@FunctionField
	def argNodes(self):
		return [ self._view.buildView( argNode, self )   for argNode in self.treeNode.argNodes ]

	@FunctionField
	def argWidgets(self):
		return [ node.widget   for node in self.argNodes ]




	@FunctionField
	def _refreshArgs(self):
		self._argsLine[:] = self.argWidgets

	@FunctionField
	def refreshCell(self):
		self._refreshArgs




	def __init__(self, treeNode, view):
		super( CVList, self ).__init__( treeNode, view )
		self._argsLine = DTWrappedLineWithSeparators( spacing=5.0 )
		self._box = DTBox()
		self._box.append( DTLabel( '[', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) ) )
		self._box.append( self._argsLine )
		self._box.append( DTLabel( ']', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) ) )
		self.widget.child = self._box



	def addArgument(self):
		argCVT = self.treeNode.addArgument()
		self._view.refresh()
		argCV = self._view.getViewNodeForTreeNode( argCVT )
		argCV.startEditing()


	def deleteChild(self, child, moveFocus):
		child._o_moveFocus( moveFocus )
		self.treeNode.deleteArgument( child.treeNode )
		self._view.refresh()



	def startEditing(self):
		self.widget.grabFocus()



	def horizontalNavigationList(self):
		return self.argNodes




	def getInsertPosition(self, receivingNodePath):
		if len( receivingNodePath ) > 1:
			child = receivingNodePath[1]
			try:
				return self.argNodes.index( child )
			except ValueError:
				return 0
		return 0


