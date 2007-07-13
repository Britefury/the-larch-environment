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

from Britefury.CodeViewTree.CVTReturn import CVTReturn
from Britefury.CodeViewTree.CVTNullExpression import CVTNullExpression

from Britefury.CodeView.CVStatement import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection



class CVReturn (CVStatement):
	treeNodeClass = CVTReturn


	treeNode = SheetRefField( CVTReturn )


	@FunctionRefField
	def valueNode(self):
		return self._view.buildView( self.treeNode.valueNode, self )

	@FunctionRefField
	def valueWidget(self):
		return self.valueNode.widget


	@FunctionField
	def refreshCell(self):
		self._box[1] = self.valueWidget




	def __init__(self, treeNode, view):
		super( CVReturn, self ).__init__( treeNode, view )
		self._box = DTBox( spacing=10.0 )
		self._box.append( DTLabel( markup=_( 'R<span size="small">ETURN</span>' ), font='Sans bold 11', colour=Colour3f( 0.0, 0.5, 0.0 ) ) )
		self._box.append( DTLabel( 'nil' ) )
		self.widget.child = self._box



	def startEditing(self):
		self.valueNode.startEditing()


	def horizontalNavigationList(self):
		return [ self.valueNode ]



	def deleteChild(self, child, moveFocus):
		if child is self.valueNode:
			if isinstance( child.treeNode, CVTNullExpression ):
				self.deleteNode( moveFocus )
			else:
				self.valueNode.treeNode.replaceWithNullExpression()
				self._view.refresh()
				self.valueNode.startEditing()
