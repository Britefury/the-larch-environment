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

from Britefury.CodeViewTree.CVTParameters import CVTParameters

from Britefury.CodeView.CVBorderNode import *

from Britefury.CodeViewBehavior.CVBParametersBehavior import *

from Britefury.DocView.Toolkit.DTWrappedLineWithSeparators import DTWrappedLineWithSeparators
from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel



class CVParameters (CVBorderNode):
	treeNodeClass = CVTParameters


	treeNode = SheetRefField( CVTParameters )


	behaviors = [ CVBParametersBehavior() ]


	@FunctionField
	def paramNodes(self):
		return [ self._view.buildView( paramNode, self )   for paramNode in self.treeNode.paramNodes ]


	@FunctionRefField
	def expandParamNode(self):
		if self.treeNode.expandParamNode is None:
			return None
		else:
			return self._view.buildView( self.treeNode.expandParamNode, self )


	@FunctionField
	def paramWidgets(self):
		if self.expandParamNode is None:
			return [ paramNode.widget   for paramNode in self.paramNodes ]
		else:
			return [ paramNode.widget   for paramNode in self.paramNodes ]  +  [ DTLabel( '*' ), self.expandParamNode.widget ]


	@FunctionField
	def refreshCell(self):
		self._line[:] = self.paramWidgets



	def __init__(self, treeNode, view):
		super( CVParameters, self ).__init__( treeNode, view )
		self._line = DTWrappedLineWithSeparators( spacing=10.0 )
		self._box = DTBox()
		self._box.append( DTLabel( '(' ) )
		self._box.append( self._line )
		self._box.append( DTLabel( ')' ) )
		self.widget.child = self._box



	def horizontalNavigationList(self):
		expandParamNode = self.expandParamNode
		if expandParamNode is not None:
			return self.paramNodes + [ expandParamNode ]
		else:
			return self.paramNodes




	def addParameter(self, name):
		paramCVT = self.treeNode.addParameter( name )
		self._view.refresh()
		paramCVT = self._view.getViewNodeForTreeNode( paramCVT )
		paramCVT.startEditing()

	def deleteChild(self, child):
		self.treeNode.deleteParameter( child.treeNode )
		self._view.refresh()
		return True


	def startEditing(self):
		self.makeCurrent()


