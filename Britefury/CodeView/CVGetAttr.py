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

from Britefury.CodeViewTree.CVTGetAttr import CVTGetAttr

from Britefury.CodeView.CVExpression import *

from Britefury.CodeViewBehavior.CVBGetAttrBehavior import *
from Britefury.CodeViewBehavior.CVBWrapInAssignmentBehavior import *

from Britefury.DocView.Toolkit.DTWrappedLine import DTWrappedLine
from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVGetAttr (CVExpression):
	treeNodeClass = CVTGetAttr


	treeNode = SheetRefField( CVTGetAttr )



	behaviors = [ CVBGetAttrBehavior(), CVBWrapInAssignmentBehavior() ]


	@FunctionRefField
	def targetObjectNode(self):
		if self.treeNode.targetObjectNode is not None:
			return self._view.buildView( self.treeNode.targetObjectNode, self )
		else:
			return None

	@FunctionRefField
	def targetObjectWidget(self):
		if self.targetObjectNode is not None:
			return self.targetObjectNode.widget
		else:
			return None



	@FunctionRefField
	def attrNameNode(self):
		return self._view.buildView( self.treeNode.attrNameNode, self )

	@FunctionRefField
	def attrNameWidget(self):
		return self.attrNameNode.widget


	@FunctionField
	def _refreshTargetObject(self):
		if self.targetObjectWidget is not None:
			self._box[0] = self.targetObjectWidget
		else:
			self._box[0] = DTLabel( 'nil' )

	@FunctionField
	def _refreshAttrName(self):
		self._box[2] = self.attrNameWidget

	@FunctionField
	def refreshCell(self):
		self._refreshTargetObject
		self._refreshAttrName






	def __init__(self, treeNode, view):
		super( CVGetAttr, self ).__init__( treeNode, view )
		self._box = DTBox()
		self._box.append( DTLabel( 'nil' ) )
		self._box.append( DTLabel( '.' ) )
		self._box.append( DTLabel( 'nil' ) )
		self.widget.child = self._box


	def deleteChild(self, child, moveFocus):
		if child is self.attrNameNode:
			self.treeNode.unwrapGetAttr()
			self._view.refresh()
			self.targetObjectNode.startEditing()
		elif child is self.targetObjectNode:
			self.targetObjectNode.treeNode.replaceWithNullExpression()
			self._view.refresh()
			self.targetObjectNode.startEditing()


	def startEditingAttrName(self):
		self.attrNameNode.makeCurrent()

	def stopEditingAttrName(self):
		self.makeCurrent()



	def horizontalNavigationList(self):
		return [ self.targetObjectNode, self.attrNameNode ]
