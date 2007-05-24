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

from Britefury.CodeViewTree.CVTLocalVarDeclaration import CVTLocalVarDeclaration

from Britefury.CodeView.CVBorderNode import *

from Britefury.CodeViewBehavior.CVBLocalVarDeclarationBehavior import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVLocalVarDeclaration (CVBorderNode):
	treeNodeClass = CVTLocalVarDeclaration


	treeNode = SheetRefField( CVTLocalVarDeclaration )



	@CVChildNodeSlotFunctionField
	def varNode(self):
		return self._view.buildView( self.treeNode.varNode, self )

	_varNodeBehaviors = [ CVBLocalVarDeclarationBehavior() ]
	varNode.setBehaviors( _varNodeBehaviors )



	@FunctionRefField
	def varWidget(self):
		return self.varNode.widget


	@CVChildNodeSlotFunctionField
	def valueNode(self):
		if self.treeNode.valueNode is not None:
			return self._view.buildView( self.treeNode.valueNode, self )
		else:
			return None

	@FunctionRefField
	def valueWidget(self):
		if self.treeNode.valueNode is not None:
			w = DTBox( spacing=10.0 )
			w.append( DTLabel( '=' ) )
			w.append( self.valueNode.widget )
			return w
		else:
			return None


	@FunctionField
	def _refreshVar(self):
		self._box[1] = self.varWidget

	@FunctionField
	def _refreshValue(self):
		valueWidget = self.valueWidget

		if valueWidget is None:
			if len( self._box ) > 2:
				del self._box[2]
		else:
			if len( self._box ) > 2:
				self._box[2] = valueWidget
			else:
				self._box.append( valueWidget )


	@FunctionField
	def refreshCell(self):
		self._refreshVar
		self._refreshValue



	def __init__(self, treeNode, view):
		super( CVLocalVarDeclaration, self ).__init__( treeNode, view )
		self._box = DTBox( spacing=10.0, minorDirectionAlignment=DTBox.ALIGN_CENTRE )
		self._box.append( DTLabel( 'var', font='Sans bold 11' ) )
		self._box.append( DTLabel( 'nil' ) )
		self.widget.child = self._box



	def startEditingValue(self):
		self.treeNode.ensureHasValue()
		valueCV = self.valueNode
		valueCV.startEditing()


	def startEditing(self):
		self.varNode.startEditing()


	def horizontalNavigationList(self):
		return [ self.varNode, self.valueNode ]
