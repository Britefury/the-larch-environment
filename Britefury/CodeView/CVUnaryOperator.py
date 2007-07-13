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

from Britefury.Kernel.Enum import *

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTUnaryOperator import *
from Britefury.CodeViewTree.CVTNullExpression import *

from Britefury.CodeView.CVExpression import *

from Britefury.DocView.Toolkit.DTWrappedLine import DTWrappedLine
from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTScript import DTScript
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTHLine import DTHLine
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel


class CVUnaryOperator (CVExpression):
	treeNodeClass = CVTUnaryOperator


	treeNode = SheetRefField( CVTUnaryOperator )



	@FunctionRefField
	def exprNode(self):
		if self.treeNode.exprNode is not None:
			return self._view.buildView( self.treeNode.exprNode, self )
		else:
			return None

	@FunctionRefField
	def exprWidget(self):
		if self.exprNode is not None:
			return self.exprNode.widget
		else:
			return None



	@FunctionField
	def _refreshExpr(self):
		if self.exprWidget is not None:
			self._box[1] = self.exprWidget
		else:
			self._box[1] = DTLabel( 'nil' )

	@FunctionField
	def refreshCell(self):
		self._refreshExpr






	def __init__(self, treeNode, view):
		super( CVUnaryOperator, self ).__init__( treeNode, view )
		self._box = DTBox( spacing=5.0 )
		self._box.append( self._o_makeUnaryOperatorWidget() )
		self._box.append( DTLabel( 'nil' ) )
		self.widget.child = self._box



	def deleteChild(self, child, moveFocus):
		if child is self.exprNode:
			if isinstance( child.treeNode, CVTNullExpression ):
				self.deleteNode( moveFocus )
			else:
				self.exprNode.treeNode.replaceWithNullExpression()
				self.refresh()
				self.exprNode.startEditing()



	def startEditingExpression(self):
		self.exprNode.startEditing()



	def horizontalNavigationList(self):
		return [ self.exprNode ]



	def _o_makeUnaryOperatorWidget(self):
		raise TypeError, 'abstract'




class CVUnaryOperatorSimple (CVUnaryOperator):
	treeNodeClass = CVTUnaryOperator
	operatorCharacterString = None


	def _o_makeUnaryOperatorWidget(self):
		return DTLabel( self.operatorCharacterString, font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) )



class CVNegate (CVUnaryOperatorSimple):
	treeNodeClass = CVTNegate
	treeNode = SheetRefField( CVTNegate )
	operatorCharacterString = '-'

class CVNot (CVUnaryOperatorSimple):
	treeNodeClass = CVTNot
	treeNode = SheetRefField( CVTNot )
	operatorCharacterString = 'not'

