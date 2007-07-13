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

from Britefury.CodeViewTree.CVTExpression import CVTExpression

from Britefury.CodeView.CVBorderNode import *

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

