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

from Britefury.CodeViewTree.CVTParametersDoc import CVTParametersDoc

from Britefury.CodeView.CVNode import *

from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection



class CVParametersDoc (CVNode):
	treeNodeClass = CVTParametersDoc


	treeNode = SheetRefField( CVTParametersDoc )


	@FunctionField
	def paramDocNodes(self):
		return [ self._view.buildView( paramNode, self )   for paramNode in self.treeNode.paramDocNodes ]


	@FunctionRefField
	def expandParamDocNode(self):
		if self.treeNode.expandParamDocNode is None:
			return None
		else:
			return self._view.buildView( self.treeNode.expandParamDocNode, self )


	@FunctionField
	def paramDocWidgets(self):
		if self.expandParamDocNode is None:
			return [ paramNode.widget   for paramNode in self.paramDocNodes ]
		else:
			return [ paramNode.widget   for paramNode in self.paramDocNodes ]  +  [ DTLabel( '*' ), self.expandParamDocNode.widget ]


	@FunctionField
	def refreshCell(self):
		self.widget[:] = self.paramDocWidgets



	def __init__(self, treeNode, view):
		super( CVParametersDoc, self ).__init__( treeNode, view )
		self.widget = DTBox( direction=DTDirection.TOP_TO_BOTTOM, minorDirectionAlignment=DTBox.ALIGN_EXPAND )



	def horizontalNavigationList(self):
		return self.verticalNavigationList()


	def verticalNavigationList(self):
		expandParamDocNode = self.expandParamDocNode
		if expandParamDocNode is not None:
			return self.paramDocNodes + [ expandParamDocNode ]
		else:
			return self.paramDocNodes
