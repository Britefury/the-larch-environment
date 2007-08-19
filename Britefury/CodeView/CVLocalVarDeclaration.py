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

from Britefury.CodeViewTree.CVTLocalVarDeclaration import CVTLocalVarDeclaration
from Britefury.CodeViewTree.CVTNullExpression import CVTNullExpression

from Britefury.CodeView.CVStatement import *

from Britefury.CodeViewBehavior.CVBLocalVarDeclarationBehavior import *

from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection
from Britefury.DocPresent.CellEdit.DPCStringCellEditEntryLabel import DPCStringCellEditEntryLabel



class CVLocalVarDeclaration (CVStatement):
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
		self._box = DTBox( spacing=10.0 )
		self._box.append( DTLabel( markup=_( 'V<span size="small">AR</span>' ), font='Sans bold 11', colour=Colour3f( 0.0, 0.5, 0.0 ) ) )
		self._box.append( DTLabel( 'nil' ) )
		self.widget.child = self._box



	def deleteChild(self, child, moveFocus):
		if child is self.varNode:
			if self._parent is not None:
				self._parent.deleteChild( self, moveFocus )
		elif child is self.valueNode:
			if isinstance( child.treeNode, CVTNullExpression ):
				self._o_moveFocus( moveFocus )
				self.treeNode.deleteValue()
			else:
				nullExpCVT = child.treeNode.replaceWithNullExpression()
				self._view.buildView( nullExpCVT, self ).startEditing()
			return True
		return False




	def startEditingValue(self):
		self.varNode.finishEditing()
		self.treeNode.ensureHasValue()
		valueCV = self.valueNode
		valueCV.startEditing()


	def startEditing(self):
		self.varNode.startEditing()


	def horizontalNavigationList(self):
		return [ self.varNode, self.valueNode ]



