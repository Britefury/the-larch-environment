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

from Britefury.CodeViewTree.CVTBlockParameters import CVTBlockParameters

from Britefury.CodeView.CVBorderNode import *

from Britefury.CodeViewBehavior.CVBBlockParametersBehavior import *

from Britefury.DocView.Toolkit.DTWrappedLineWithSeparators import DTWrappedLineWithSeparators
from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel



class CVBlockParameters (CVBorderNode):
	treeNodeClass = CVTBlockParameters


	treeNode = SheetRefField( CVTBlockParameters )


	behaviors = [ CVBBlockParametersBehavior() ]


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
		super( CVBlockParameters, self ).__init__( treeNode, view )
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



	def startEditing(self):
		self.makeCurrent()


