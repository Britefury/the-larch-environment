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

from Britefury.CodeViewTree.CVTAssignment import CVTAssignment

from Britefury.CodeView.CVBorderNode import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection



class CVAssignment (CVBorderNode):
	treeNodeClass = CVTAssignment


	treeNode = SheetRefField( CVTAssignment )


	@FunctionRefField
	def targetRefNode(self):
		return self._view.buildView( self.treeNode.targetRefNode, self )

	@FunctionRefField
	def targetRefWidget(self):
		return self.targetRefNode.widget


	@FunctionRefField
	def valueNode(self):
		return self._view.buildView( self.treeNode.valueNode, self )

	@FunctionRefField
	def valueWidget(self):
		return self.valueNode.widget


	@FunctionField
	def _refreshVar(self):
		self._box[0] = self.targetRefWidget

	@FunctionField
	def _refreshValue(self):
		self._box[2] = self.valueWidget

	@FunctionField
	def refreshCell(self):
		self._refreshVar
		self._refreshValue





	def __init__(self, treeNode, view):
		super( CVAssignment, self ).__init__( treeNode, view )
		self._box = DTBox( spacing=10.0 )
		self._box.append( DTLabel( 'nil' ) )
		self._box.append( DTLabel( '=' ) )
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
				self.treeNode.removeAssignment()
				self._view.refresh()
		elif child is self.targetRefNode:
			child._o_moveFocus( moveFocus )
			self.treeNode.removeAssignment()
			self._view.refresh()



	def horizontalNavigationList(self):
		return [ self.targetRefNode, self.valueNode ]


	def startEditingValue(self):
		self.valueNode.makeCurrent()
