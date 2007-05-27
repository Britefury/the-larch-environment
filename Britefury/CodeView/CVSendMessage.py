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

from Britefury.CodeViewTree.CVTSendMessage import CVTSendMessage

from Britefury.CodeView.CVExpression import *

from Britefury.CodeViewBehavior.CVBSendMessageBehavior import *

from Britefury.DocView.Toolkit.DTWrappedLine import DTWrappedLine
from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVSendMessage (CVExpression):
	treeNodeClass = CVTSendMessage


	treeNode = SheetRefField( CVTSendMessage )



	behaviors = [ CVBSendMessageBehavior() ]


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
	def messageNameNode(self):
		return self._view.buildView( self.treeNode.messageNameNode, self )

	@FunctionRefField
	def messageNameWidget(self):
		return self.messageNameNode.widget



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
	def _refreshMessageName(self):
		self._box[2] = self.messageNameWidget

	@FunctionField
	def _refreshArgs(self):
		self._box[3] = self.argumentsWidget

	@FunctionField
	def refreshCell(self):
		self._refreshTargetObject
		self._refreshMessageName
		self._refreshArgs






	def __init__(self, treeNode, view):
		super( CVSendMessage, self ).__init__( treeNode, view )
		self._box = DTBox( spacing=5.0 )
		self._box.append( DTLabel( 'nil' ) )
		self._box.append( DTLabel( '.' ) )
		self._box.append( DTLabel( 'nil' ) )
		self._box.append( DTLabel( 'nil' ) )
		self.widget.child = self._box
		self._messageNameEntry = None


	def deleteChild(self, child):
		if child is self.messageNameNode:
			self.treeNode.unwrapSendMessage()
			self._view.refresh()
			return True
		elif child is self.targetObjectNode:
			self.targetObjectNode.treeNode.replaceWithNullExpression()
			self._view.refresh()
			self.targetObjectNode.startEditing()
			return False
		else:
			return False


	def startEditingMessageName(self):
		self.messageNameNode.startEditing()


	def startEditingArguments(self):
		self.argumentsNode.makeCurrent()

	def stopEditingArguments(self):
		self.makeCurrent()



	def horizontalNavigationList(self):
		return [ self.targetObjectNode, self.messageNameNode, self.argumentsNode ]
