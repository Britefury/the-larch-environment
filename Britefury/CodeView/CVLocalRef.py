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

from Britefury.CodeViewTree.CVTLocalRef import CVTLocalRef

from Britefury.CodeView.CVExpression import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection



class CVLocalRef (CVExpression):
	treeNodeClass = CVTLocalRef


	treeNode = SheetRefField( CVTLocalRef )


	def _nameWidget(self):
		return DTLabel( self.treeNode.varName )

	def _refreshCell(self):
		self.widget.child = self.nameWidget

	nameWidget = FunctionField( _nameWidget )
	refreshCell = FunctionField( _refreshCell )



	def __init__(self, treeNode, view):
		super( CVLocalRef, self ).__init__( treeNode, view )


