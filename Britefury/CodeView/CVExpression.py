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

from Britefury.extlibs.piemenu.piemenu import *

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTExpression import CVTExpression

from Britefury.CodeView.CVBorderNode import *
from Britefury.CodeView.MoveFocus import *

from Britefury.CodeViewBehavior.CVBWrapExpressionBehavior import *



class CVExpression (CVBorderNode):
	treeNodeClass = CVTExpression


	treeNode = SheetRefField( CVTExpression )


	behaviors = [ CVBWrapExpressionBehavior() ]



	def wrapInCall(self):
		parent = self._parent
		callCVT = self.treeNode.wrapInCall()
		parent.refresh()
		callCV = self._o_getViewNode( callCVT )
		callCV.startEditingArguments()


	def wrapInGetAttr(self):
		parent = self._parent
		getAttrCVT = self.treeNode.wrapInGetAttr()
		parent.refresh()
		getAttrCV = self._o_getViewNode( getAttrCVT )
		getAttrCV.startEditingAttrName()


	def wrapInNegate(self):
		parent = self._parent
		negateCVT = self.treeNode.wrapInNegate()
		parent.refresh()
		negateCV = self._o_getViewNode( negateCVT )
		negateCV.makeCurrent()


	def wrapInNot(self):
		parent = self._parent
		notCVT = self.treeNode.wrapInNot()
		parent.refresh()
		notCV = self._o_getViewNode( notCVT )
		notCV.makeCurrent()


	def wrapInBinaryOperator(self, graphNodeClass):
		parent = self._parent
		binOpCVT = self.treeNode.wrapInBinaryOperator( graphNodeClass )
		parent.refresh()
		binOpCV = self._o_getViewNode( binOpCVT )
		binOpCV.startEditingRight()


	def wrapInTuple(self):
		parent = self._parent
		tupleCVT = self.treeNode.wrapInTuple()
		parent.refresh()
		tupleCV = self._o_getViewNode( tupleCVT )
		tupleCV.makeCurrent()


	def wrapInSubscript(self):
		parent = self._parent
		getItemCVT = self.treeNode.wrapInSubscript()
		parent.refresh()
		getItemCV = self._o_getViewNode( getItemCVT )
		getItemCV.startEditingKey()



	def _o_createPieMenu(self):
		refactorMenu = PieMenu( header='Refactor', header_font='Sans bold 11', fixed_radius=60, ring_radius=50 )
		extractToVariableItem = PieItem( label='Extract var', label_font='Sans 11' )
		refactorMenu.add_item( extractToVariableItem )

		menu = PieMenu( header='Expression', header_font='Sans bold 11', fixed_radius=60, ring_radius=50 )
		okItem = PieItem( label='OK', label_font='Sans 11' )
		deleteItem = PieItem( label='Delete', label_font='Sans 11' )
		refactorItem = PieItem( label='Refactor', label_font='Sans 11', sub_pie=refactorMenu )
		refactorItem.sub_menu = refactorMenu

		menu.add_item( okItem )
		menu.add_item( deleteItem )
		menu.add_item( refactorItem )

		deleteItem.activateSignal.connect( self._p_onMenuDelete )
		extractToVariableItem.activateSignal.connect( self._p_onMenuExtractToVariable )
		return menu


	def _p_onMenuDelete(self, menu, item):
		self.deleteNode( MoveFocus.RIGHT )


	def _p_onMenuExtractToVariable(self, menu, item):
		varDeclCVT = self.treeNode.extractToVariable()
		self._view.refresh()
		varDeclCV = self._o_getViewNode( varDeclCVT.varNode )
		varDeclCV.startEditing()

