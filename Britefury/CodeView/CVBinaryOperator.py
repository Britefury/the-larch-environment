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

from Britefury.CodeViewTree.CVTBinaryOperator import *

from Britefury.CodeView.CVExpression import *

from Britefury.DocView.Toolkit.DTWrappedLine import DTWrappedLine
from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTHLine import DTHLine
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVBinaryOperator (CVExpression):
	treeNodeClass = CVTBinaryOperator


	treeNode = SheetRefField( CVTBinaryOperator )



	@FunctionRefField
	def leftNode(self):
		if self.treeNode.leftNode is not None:
			return self._view.buildView( self.treeNode.leftNode, self )
		else:
			return None

	@FunctionRefField
	def leftWidget(self):
		if self.leftNode is not None:
			return self.leftNode.widget
		else:
			return None



	@FunctionRefField
	def rightNode(self):
		if self.treeNode.rightNode is not None:
			return self._view.buildView( self.treeNode.rightNode, self )
		else:
			return None

	@FunctionRefField
	def rightWidget(self):
		if self.rightNode is not None:
			return self.rightNode.widget
		else:
			return None



	@FunctionField
	def _refreshLeft(self):
		if self.leftWidget is not None:
			self._box[0] = self.leftWidget
		else:
			self._box[0] = DTLabel( 'nil' )

	@FunctionField
	def _refreshRight(self):
		if self.leftWidget is not None:
			self._box[2] = self.rightWidget
		else:
			self._box[2] = DTLabel( 'nil' )

	@FunctionField
	def refreshCell(self):
		self._refreshLeft
		self._refreshRight






	def __init__(self, treeNode, view):
		super( CVBinaryOperator, self ).__init__( treeNode, view )
		self._box = self._o_makeBox()
		self._box.append( DTLabel( 'nil' ) )
		self._o_packOperatorWidget( self._box )
		self._box.append( DTLabel( 'nil' ) )
		self.widget.child = self._box


	def deleteChild(self, child, moveFocus):
		if child is self.attrNameNode:
			self.treeNode.unwrapBinaryOperator()
			self._view.refresh()
			self.targetObjectNode.startEditing()
		elif child is self.targetObjectNode:
			self.targetObjectNode.treeNode.replaceWithNullExpression()
			self._view.refresh()
			self.targetObjectNode.startEditing()


	def startEditingLeft(self):
		self.leftNode.makeCurrent()

	def startEditingRight(self):
		self.rightNode.makeCurrent()


	def horizontalNavigationList(self):
		return [ self.leftNode, self.rightNode ]



	def _o_makeBox(self):
		return DTBox()


	def _o_packOperatorWidget(self, box):
		raise TypeError, 'abstract'





class CVBinaryOperatorSimple (CVBinaryOperator):
	treeNodeClass = CVTBinaryOperator
	treeNode = SheetRefField( CVTBinaryOperator )
	operatorCharacterString = None


	def _o_packOperatorWidget(self, box):
		assert self.operatorCharacterString is not None
		box.append( DTLabel( self.operatorCharacterString, font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) ) )



class CVBinOpAdd (CVBinaryOperatorSimple):
	treeNodeClass = CVTBinOpAdd
	treeNode = SheetRefField( CVTBinOpAdd )
	operatorCharacterString = '+'

class CVBinOpSub (CVBinaryOperatorSimple):
	treeNodeClass = CVTBinOpSub
	treeNode = SheetRefField( CVTBinOpSub )
	operatorCharacterString = '-'

class CVBinOpMul (CVBinaryOperatorSimple):
	treeNodeClass = CVTBinOpMul
	treeNode = SheetRefField( CVTBinOpMul )
	operatorCharacterString = '*'


class CVBinOpDiv (CVBinaryOperator):
	treeNodeClass = CVTBinOpDiv
	treeNode = SheetRefField( CVTBinOpDiv )

	def _o_makeBox(self):
		return DTBox( direction=DTDirection.TOP_TO_BOTTOM, spacing=2.0 )

	def _o_packOperatorWidget(self, box):
		box.append( DTHLine( 2.0, colour=Colour3f( 0.0, 0.6, 0.0 ) ), minorDirectionAlignment=DTBox.ALIGN_EXPAND )


class CVBinOpPow (CVBinaryOperatorSimple):
	treeNodeClass = CVTBinOpPow
	treeNode = SheetRefField( CVTBinOpPow )
	operatorCharacterString = '**'

class CVBinOpMod (CVBinaryOperatorSimple):
	treeNodeClass = CVTBinOpMod
	treeNode = SheetRefField( CVTBinOpMod )
	operatorCharacterString = '%'

class CVBinOpBitAnd (CVBinaryOperatorSimple):
	treeNodeClass = CVTBinOpBitAnd
	treeNode = SheetRefField( CVTBinOpBitAnd )
	operatorCharacterString = '&'

class CVBinOpBitOr (CVBinaryOperatorSimple):
	treeNodeClass = CVTBinOpBitOr
	treeNode = SheetRefField( CVTBinOpBitOr )
	operatorCharacterString = '|'

class CVBinOpBitXor (CVBinaryOperatorSimple):
	treeNodeClass = CVTBinOpBitXor
	treeNode = SheetRefField( CVTBinOpBitXor )
	operatorCharacterString = '^'

class CVBinOpEq (CVBinaryOperatorSimple):
	treeNodeClass = CVTBinOpEq
	treeNode = SheetRefField( CVTBinOpEq )
	operatorCharacterString = '=='

class CVBinOpNEq (CVBinaryOperatorSimple):
	treeNodeClass = CVTBinOpNEq
	treeNode = SheetRefField( CVTBinOpNEq )
	operatorCharacterString = '!='

class CVBinOpLT (CVBinaryOperatorSimple):
	treeNodeClass = CVTBinOpLT
	treeNode = SheetRefField( CVTBinOpLT )
	operatorCharacterString = '<'

class CVBinOpGT (CVBinaryOperatorSimple):
	treeNodeClass = CVTBinOpGT
	treeNode = SheetRefField( CVTBinOpGT )
	operatorCharacterString = '>'

class CVBinOpLTE (CVBinaryOperatorSimple):
	treeNodeClass = CVTBinOpLTE
	treeNode = SheetRefField( CVTBinOpLTE )
	operatorCharacterString = '<='

class CVBinOpGTE (CVBinaryOperatorSimple):
	treeNodeClass = CVTBinOpGTE
	treeNode = SheetRefField( CVTBinOpGTE )
	operatorCharacterString = '>='
