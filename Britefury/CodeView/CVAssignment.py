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

from Britefury.CodeViewTree.CVTAssignment import CVTAssignment

from Britefury.CodeView.CVBorderNode import *

from Britefury.CodeViewBehavior.CVBAssignmentBehavior import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTWrappedLine import DTWrappedLine
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection



class CVAssignment (CVBorderNode):
	treeNodeClass = CVTAssignment


	treeNode = SheetRefField( CVTAssignment )


	behaviors = [ CVBAssignmentBehavior() ]


	@FunctionRefField
	def targetNodes(self):
		return [ self._view.buildView( treeNode, self )   for treeNode in self.treeNode.targetNodes ]

	@FunctionRefField
	def targetWidgets(self):
		return [ node.widget   for node in self.targetNodes ]


	@FunctionRefField
	def valueNode(self):
		return self._view.buildView( self.treeNode.valueNode, self )

	@FunctionRefField
	def valueWidget(self):
		return self.valueNode.widget


	@staticmethod
	def makeTargetBox(targetWidget):
		box = DTBox( spacing=10.0 )
		box.append( targetWidget )
		box.append( DTLabel( '=', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) ) )
		return box


	@FunctionField
	def _refreshTargets(self):
		self._targetsLine[:] = [ self.makeTargetBox( widget )   for widget in self.targetWidgets ]

	@FunctionField
	def _refreshValue(self):
		self._box[1] = self.valueWidget

	@FunctionField
	def refreshCell(self):
		self._refreshTargets
		self._refreshValue





	def __init__(self, treeNode, view):
		super( CVAssignment, self ).__init__( treeNode, view )
		self._targetsLine = DTWrappedLine()
		self._box = DTBox()
		self._box.append( self._targetsLine )
		self._box.append( DTLabel( 'nil' ) )
		self.widget.child = self._box




	def deleteChild(self, child, moveFocus):
		if child is self.valueNode:
			if moveFocus == MoveFocus.RIGHT:
				self.valueNode.treeNode.replaceWithNullExpression()
				self._view.refresh()
				self.valueNode.startEditing()
			else:
				child._o_moveFocus( moveFocus )
				self.treeNode.removeValue()
				self._view.refresh()
		elif child in self.targetNodes:
			child._o_moveFocus( moveFocus )
			self.treeNode.removeTarget( child.treeNode )
			self._view.refresh()



	def horizontalNavigationList(self):
		return self.targetNodes + [ self.valueNode ]


	def startEditingValue(self):
		self.valueNode.makeCurrent()
