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

from Britefury.CodeViewTree.CVTCall import CVTCall
from Britefury.CodeViewTree.CVTNullExpression import CVTNullExpression

from Britefury.CodeView.CVExpression import *

from Britefury.CodeViewBehavior.CVBCallBehavior import *

from Britefury.DocPresent.Toolkit.DTWrappedLine import DTWrappedLine
from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.CellEdit.DPCStringCellEditEntryLabel import DPCStringCellEditEntryLabel



class CVCall (CVExpression):
	treeNodeClass = CVTCall


	treeNode = SheetRefField( CVTCall )



	behaviors = [ CVBCallBehavior() ]


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
	def argumentsNode(self):
		return self._view.buildView( self.treeNode.argumentsNode, self )

	@FunctionRefField
	def argumentsWidget(self):
		return self.argumentsNode.widget


	@FunctionField
	def _refreshTargetObject(self):
		if self.targetObjectWidget is not None:
			self._box[0] = self.targetObjectWidget
		else:
			self._box[0] = DTLabel( 'nil' )

	@FunctionField
	def _refreshArgs(self):
		self._box[1] = self.argumentsWidget

	@FunctionField
	def refreshCell(self):
		self._refreshTargetObject
		self._refreshArgs






	def __init__(self, treeNode, view):
		super( CVCall, self ).__init__( treeNode, view )
		self._box = DTBox()
		self._box.append( DTLabel( 'nil' ) )
		self._box.append( DTLabel( 'nil' ) )
		self.widget.child = self._box
		self._messageNameEntry = None


	def deleteChild(self, child, moveFocus):
		if child is self.argumentsNode:
			self.targetObjectNode.makeCurrent()
			self.treeNode.unwrapCall()
		elif child is self.targetObjectNode:
			if isinstance( child.treeNode, CVTNullExpression ):
				self.deleteNode( moveFocus )
			else:
				self.targetObjectNode.treeNode.replaceWithNullExpression()
				self.refresh()
				self.targetObjectNode.startEditing()


	def startEditingArguments(self):
		self.argumentsNode.makeCurrent()

	def stopEditingArguments(self):
		self.makeCurrent()



	def horizontalNavigationList(self):
		return [ self.targetObjectNode, self.argumentsNode ]
