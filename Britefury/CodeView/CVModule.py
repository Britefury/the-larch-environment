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

from Britefury.CodeViewTree.CVTModule import CVTModule

from Britefury.CodeView.CVBorderNode import *

from Britefury.CodeViewBehavior.CVBStatementListBehavior import *
from Britefury.CodeViewBehavior.CVBCreateExpressionBehavior import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection



class CVModule (CVBorderNode):
	treeNodeClass = CVTModule


	treeNode = SheetRefField( CVTModule )



	@FunctionRefField
	def statementsNode(self):
		return self._view.buildView( self.treeNode.statementsNode, self )

	@FunctionRefField
	def statementsWidget(self):
		return self.statementsNode.widget

	def _refreshCell(self):
		self.widget.child = self.statementsWidget

	refreshCell = FunctionField( _refreshCell )






	def __init__(self, treeNode, view):
		super( CVModule, self ).__init__( treeNode, view )
		self.widget.child = DTLabel( 'nil' )




	def horizontalNavigationList(self):
		return self.verticalNavigationList()

	def verticalNavigationList(self):
		return [ self.statementsNode ]



