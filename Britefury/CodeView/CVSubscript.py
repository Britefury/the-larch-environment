##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copykey Geoffrey French 1999-2007.
##-*************************
import pygtk
pygtk.require( '2.0' )
import gtk

from Britefury.Util import RegExpStrings

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTSubscript import *
from Britefury.CodeViewTree.CVTNullExpression import CVTNullExpression

from Britefury.CodeView.CVExpression import *

from Britefury.CodeViewBehavior.CVBSubscriptBehavior import *
from Britefury.CodeViewBehavior.CVBWrapInAssignmentBehavior import *

from Britefury.DocView.Toolkit.DTWrappedLine import DTWrappedLine
from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTScript import DTScript
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTHLine import DTHLine
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVSubscript (CVExpression):
	treeNodeClass = CVTSubscript


	treeNode = SheetRefField( CVTSubscript )



	behaviors = [ CVBSubscriptBehavior(), CVBWrapInAssignmentBehavior() ]



	@FunctionRefField
	def targetNode(self):
		if self.treeNode.targetNode is not None:
			return self._view.buildView( self.treeNode.targetNode, self )
		else:
			return None

	@FunctionRefField
	def targetWidget(self):
		if self.targetNode is not None:
			return self.targetNode.widget
		else:
			return None



	@FunctionRefField
	def keyNode(self):
		if self.treeNode.keyNode is not None:
			return self._view.buildView( self.treeNode.keyNode, self )
		else:
			return None

	@FunctionRefField
	def keyWidget(self):
		if self.keyNode is not None:
			return self.keyNode.widget
		else:
			return None



	@FunctionField
	def _refreshTarget(self):
		if self.targetWidget is not None:
			self._script.leftChild = self.targetWidget
		else:
			self._script.leftChild = DTLabel( '<nil>' )

	@FunctionField
	def _refreshKey(self):
		if self.keyWidget is not None:
			self._keyBox[1] = self.keyWidget
		else:
			self._keyBox[1] = DTLabel( '<nil>' )

	@FunctionField
	def refreshCell(self):
		self._refreshTarget
		self._refreshKey






	def __init__(self, treeNode, view):
		super( CVSubscript, self ).__init__( treeNode, view )
		self._keyBox = DTBox()
		self._keyBox.append( DTLabel( '[', font='Sans 11', colour=Colour3f( 0.5, 0.5, 0.5 ) ) )
		self._keyBox.append( DTLabel( '<nil>' ) )
		self._keyBox.append( DTLabel( ']', font='Sans 11', colour=Colour3f( 0.5, 0.5, 0.5 ) ) )
		self._script = DTScript( DTScript.SUBSCRIPT )
		self._script.rightChild = self._keyBox
		self.widget.child = self._script


	def deleteChild(self, child, moveFocus):
		if isinstance( child.treeNode, CVTNullExpression ):
			if child is self.targetNode:
				keyNode = self.keyNode
				self.treeNode.removeTarget()
				self.refreshFromParent()
				keyNode.startEditing()
			elif child is self.keyNode:
				targetNode = self.targetNode
				self.treeNode.removeKey()
				self.refreshFromParent()
				targetNode.startEditing()
		elif child is self.targetNode  or  child is self.keyNode:
			nullExpCVT = child.treeNode.replaceWithNullExpression()
			self.refresh()
			self._o_getViewNode( nullExpCVT ).startEditing()


	def startEditingTarget(self):
		self.targetNode.makeCurrent()

	def startEditingKey(self):
		self.keyNode.makeCurrent()


	def horizontalNavigationList(self):
		return [ self.targetNode, self.keyNode ]
