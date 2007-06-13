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

from Britefury.Util import RegExpStrings

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTLocalRef import CVTLocalRef

from Britefury.CodeView.CVExpression import *

from Britefury.CodeViewBehavior.CVBLocalRefBehavior import CVBLocalRefBehavior

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTEntryLabel import DTEntryLabel




class CVLocalRef (CVExpression):
	treeNodeClass = CVTLocalRef


	treeNode = SheetRefField( CVTLocalRef )


	behaviors = [ CVBLocalRefBehavior() ]


	@FunctionRefField
	def varNode(self):
		return self._view.buildView( self.treeNode.varNode, self )

	@FunctionRefField
	def varWidget(self):
		return self.varNode.widget

	@FunctionField
	def refreshCell(self):
		self.widget.child = self.varWidget




	def __init__(self, treeNode, view):
		super( CVLocalRef, self ).__init__( treeNode, view )
		self.widget.child = DTLabel( 'nil' )



	def startEditing(self):
		self.varNode.startEditing()


	def startEditingOnLeft(self):
		self.varNode.startEditingOnLeft()

	def startEditingOnRight(self):
		self.varNode.startEditingOnRight()

