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

from Britefury.CodeViewBehavior.CVBWrapInSendMessageBehavior import *



class CVExpression (CVBorderNode):
	treeNodeClass = CVTExpression


	treeNode = SheetRefField( CVTExpression )


	behaviors = [ CVBWrapInSendMessageBehavior() ]



	def wrapInSendMessage(self):
		sendCVT = self.treeNode.wrapInSendMessage()
		self._view.refresh()
		sendCV = self._view.getViewNodeForTreeNode( sendCVT )
		sendCV.startEditingMessageName()

