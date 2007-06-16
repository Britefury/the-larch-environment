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

from Britefury.CodeViewTree.CVTDef import CVTDef

from Britefury.CodeView.CVStatement import *

from Britefury.CodeViewBehavior.CVBDefBehavior import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTBorder import DTBorder
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVDef (CVStatement):
	treeNodeClass = CVTDef


	treeNode = SheetRefField( CVTDef )


	behaviors = [ CVBDefBehavior() ]



	@FunctionRefField
	def declVarNode(self):
		return self._view.buildView( self.treeNode.declVarNode, self )

	@FunctionRefField
	def declVarWidget(self):
		return self.declVarNode.widget



	@FunctionRefField
	def paramsNode(self):
		return self._view.buildView( self.treeNode.paramsNode, self )

	@FunctionRefField
	def paramsWidget(self):
		return self.paramsNode.widget



	@FunctionRefField
	def statementsNode(self):
		return self._view.buildView( self.treeNode.statementsNode, self )

	@FunctionRefField
	def statementsWidget(self):
		return self.statementsNode.widget



	@FunctionField
	def _refreshDeclVar(self):
		self._declBox[1] = self.declVarWidget

	@FunctionField
	def _refreshParams(self):
		self._declBox[2] = self.paramsWidget

	@FunctionField
	def _refreshStatements(self):
		self._statementsBorder.child = self.statementsWidget


	@FunctionField
	def refreshCell(self):
		self._refreshDeclVar
		self._refreshParams
		self._refreshStatements




	def __init__(self, treeNode, view):
		super( CVDef, self ).__init__( treeNode, view )
		self._declBox = DTBox( spacing=5.0 )
		self._declBox.append( DTLabel( 'def', font='Sans bold 11' ) )
		self._declBox.append( DTLabel( 'nil' ) )
		self._declBox.append( DTLabel( 'nil' ) )
		self._declBox.append( DTLabel( ':' ) )
		self._statementsBorder = DTBorder( leftMargin=30.0 )
		self._statementsBorder.child = DTLabel( 'nil' )
		self._box = DTBox( spacing=5.0, direction=DTDirection.TOP_TO_BOTTOM, minorDirectionAlignment=DTBox.ALIGN_EXPAND )
		self._box.append( self._declBox )
		self._box.append( self._statementsBorder )
		self.widget.child = self._box



	def startEditingParameters(self):
		self.declVarNode.finishEditing()
		self.paramsNode.startEditing()

	def startEditingStatements(self):
		self.statementsNode.startEditing()


	def startEditing(self):
		self.declVarNode.startEditing()


	def horizontalNavigationList(self):
		return [ self.declVarNode, self.paramsNode, self.statementsNode ]

	def verticalNavigationList(self):
		return [ self.declVarNode, self.statementsNode ]



