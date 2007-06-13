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

from Britefury.CodeViewTree.CVTArguments import CVTArguments

from Britefury.CodeView.CVBorderNode import *

from Britefury.CodeViewBehavior.CVBArgumentsBehavior import *
from Britefury.CodeViewBehavior.CVBCreateExpressionBehavior import *

from Britefury.DocView.Toolkit.DTWrappedLineWithSeparators import DTWrappedLineWithSeparators
from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVArguments (CVBorderNode):
	treeNodeClass = CVTArguments


	treeNode = SheetRefField( CVTArguments )



	behaviors = [ CVBArgumentsBehavior(), CVBCreateExpressionBehavior() ]



	@FunctionField
	def argNodes(self):
		return [ self._view.buildView( argNode, self )   for argNode in self.treeNode.argNodes ]

	@FunctionField
	def argWidgets(self):
		return [ node.widget   for node in self.argNodes ]


	@FunctionRefField
	def expandArgNode(self):
		if self.treeNode.expandArgNode is not None:
			return self._view.buildView( self.treeNode.expandArgNode, self )
		else:
			return None

	@FunctionRefField
	def expandArgWidget(self):
		if self.expandArgNode is not None:
			return self.expandArgNode.widget
		else:
			return None


	@FunctionRefField
	def argsWidget(self):
		w = DTWrappedLineWithSeparators( spacing=5.0 )
		w.extend( self.argWidgets )
		if self.expandArgWidget is not None:
			x = DTBox( spacing=3.0 )
			x.append( '*' )
			x.append( self.expandArgWidget )
			w.append( x )
		return w



	@FunctionField
	def _refreshArgs(self):
		self._box[1] = self.argsWidget

	@FunctionField
	def refreshCell(self):
		self._refreshArgs




	def __init__(self, treeNode, view):
		super( CVArguments, self ).__init__( treeNode, view )
		self._box = DTBox()
		self._box.append( DTLabel( '(' ) )
		self._box.append( DTLabel( 'nil' ) )
		self._box.append( DTLabel( ')' ) )
		self.widget.child = self._box



	def addArgument(self):
		argCVT = self.treeNode.addArgument()
		self._view.refresh()
		argCV = self._view.getViewNodeForTreeNode( argCVT )
		argCV.startEditing()


	def deleteChild(self, child):
		self.treeNode.deleteArgument( child.treeNode )
		self._view.refresh()
		return True



	def startEditing(self):
		self.widget.grabFocus()



	def horizontalNavigationList(self):
		expandArgNode = self.expandArgNode
		if expandArgNode is not None:
			return self.argNodes + [ expandArgNode ]
		else:
			return self.argNodes


