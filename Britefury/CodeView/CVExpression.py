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

from Britefury.CodeViewBehavior.CVBWrapInCallBehavior import *
from Britefury.CodeViewBehavior.CVBWrapInGetAttrBehavior import *
from Britefury.CodeViewBehavior.CVBWrapInNotBehavior import *



class CVExpression (CVBorderNode):
	treeNodeClass = CVTExpression


	treeNode = SheetRefField( CVTExpression )


	behaviors = [ CVBWrapInCallBehavior(), CVBWrapInGetAttrBehavior(), CVBWrapInNotBehavior()  ]



	def wrapInCall(self):
		callCVT = self.treeNode.wrapInCall()
		self._view.refresh()
		callCV = self._view.getViewNodeForTreeNode( callCVT )
		callCV.startEditingArguments()


	def wrapInGetAttr(self):
		getAttrCVT = self.treeNode.wrapInGetAttr()
		self._view.refresh()
		getAttrCV = self._view.getViewNodeForTreeNode( getAttrCVT )
		getAttrCV.startEditingAttrName()


	def wrapInNot(self):
		notCVT = self.treeNode.wrapInNot()
		self._view.refresh()
		notCV = self._view.getViewNodeForTreeNode( notCVT )
		notCV.startEditingAttrName()
