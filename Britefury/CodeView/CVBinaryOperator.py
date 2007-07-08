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

from Britefury.CodeViewTree.CVTBinaryOperator import *

from Britefury.CodeView.CVExpression import *

from Britefury.DocView.Toolkit.DTWrappedLine import DTWrappedLine
from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTScript import DTScript
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTHLine import DTHLine
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel


class _CVBinaryOperatorPrecedence (Enum):
	NONE = -1
	CMP = 1
	BIT_OR = 2
	BIT_XOR = 3
	BIT_AND = 4
	SHIFT = 5
	ADD_SUB = 6
	MUL_DIV_MOD = 7
	POW = 8




class CVBinaryOperator (CVExpression):
	precedence = _CVBinaryOperatorPrecedence.NONE
	disableLeftChildParens = False
	disableRightChildParens = False
	treeNodeClass = CVTBinaryOperator


	treeNode = SheetRefField( CVTBinaryOperator )



	@FunctionRefField
	def leftNode(self):
		if self.treeNode.leftNode is not None:
			left = self._view.buildView( self.treeNode.leftNode, self )
			if isinstance( left, CVBinaryOperator ):
				if self.disableLeftChildParens:
					left._p_hideParens()
				else:
					if self.precedence > left.precedence:
						left._p_showParens()
					else:
						left._p_hideParens()
			return left
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
			right = self._view.buildView( self.treeNode.rightNode, self )
			if isinstance( right, CVBinaryOperator ):
				if self.disableRightChildParens:
					right._p_hideParens()
				else:
					if self.precedence >= right.precedence:
						right._p_showParens()
					else:
						right._p_hideParens()
			return right
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
			self._o_setLeftChildWidget( self._container, self.leftWidget )
		else:
			self._o_setLeftChildWidget( self._container, DTLabel( '<nil>' ) )

	@FunctionField
	def _refreshRight(self):
		if self.rightWidget is not None:
			self._o_setRightChildWidget( self._container, self.rightWidget )
		else:
			self._o_setRightChildWidget( self._container, DTLabel( '<nil>' ) )

	@FunctionField
	def refreshCell(self):
		self._refreshLeft
		self._refreshRight






	def __init__(self, treeNode, view):
		super( CVBinaryOperator, self ).__init__( treeNode, view )
		self._bShowParens = False
		self._parensBox = DTBox()
		self._parensBox.append( DTLabel( '(', font='Sans bold 11' ) )
		self._parensBox.append( DTLabel( 'nil' ) )
		self._parensBox.append( DTLabel( ')', font='Sans bold 11' ) )
		self._container = self._o_makeContainer()
		self.widget.child = self._container


	def _p_showParens(self):
		if not self._bShowParens:
			self._bShowParens = True
			self._parensBox[1] = self._container
			self.widget.child = self._parensBox

	def _p_hideParens(self):
		if self._bShowParens:
			self._bShowParens = False
			self.widget.child = self._container


	def deleteChild(self, child, moveFocus):
		if child is self.leftNode:
			right = self.treeNode.removeLeft()
			self._view.refresh()
			self._view.buildView( right, self._parent ).startEditing()
		elif child is self.rightNode:
			left = self.treeNode.removeRight()
			self._view.refresh()
			self._view.buildView( left, self._parent ).startEditing()


	def startEditingLeft(self):
		self.leftNode.makeCurrent()

	def startEditingRight(self):
		self.rightNode.makeCurrent()


	def horizontalNavigationList(self):
		return [ self.leftNode, self.rightNode ]


	def _o_setLeftChildWidget(self, container, child):
		raise TypeError, 'abstract'

	def _o_setRightChildWidget(self, container, child):
		raise TypeError, 'abstract'



	def _o_makeContainer(self):
		raise TypeError, 'abstract'





class CVBinaryOperatorBox (CVBinaryOperator):
	treeNodeClass = CVTBinaryOperator
	treeNode = SheetRefField( CVTBinaryOperator )
	operatorCharacterString = None


	def _o_makeContainer(self):
		box = self._o_makeBox()
		box.append( DTLabel( 'nil' ) )
		self._o_packOperatorWidget( box )
		box.append( DTLabel( 'nil' ) )
		return box

	def _o_makeBox(self):
		return DTBox( spacing=2.0 )

	def _o_packOperatorWidget(self, box):
		assert self.operatorCharacterString is not None
		box.append( DTLabel( self.operatorCharacterString, font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) ) )


	def _o_setLeftChildWidget(self, container, child):
		container[0] = child

	def _o_setRightChildWidget(self, container, child):
		container[2] = child



class CVBinaryOperatorScript (CVBinaryOperator):
	treeNodeClass = CVTBinaryOperator
	treeNode = SheetRefField( CVTBinaryOperator )
	scriptMode = DTScript.SUPERSCRIPT


	def _o_makeContainer(self):
		return DTScript( self.scriptMode )

	def _o_setLeftChildWidget(self, container, child):
		container.leftChild = child

	def _o_setRightChildWidget(self, container, child):
		container.rightChild = child



class CVBinaryOperatorSimple (CVBinaryOperatorBox):
	treeNodeClass = CVTBinaryOperator
	treeNode = SheetRefField( CVTBinaryOperator )
	operatorCharacterString = None


	def _o_packOperatorWidget(self, box):
		assert self.operatorCharacterString is not None
		box.append( DTLabel( self.operatorCharacterString, font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) ) )



class CVBinOpAdd (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.ADD_SUB
	treeNodeClass = CVTBinOpAdd
	treeNode = SheetRefField( CVTBinOpAdd )
	operatorCharacterString = '+'

class CVBinOpSub (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.ADD_SUB
	treeNodeClass = CVTBinOpSub
	treeNode = SheetRefField( CVTBinOpSub )
	operatorCharacterString = '-'

class CVBinOpMul (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.MUL_DIV_MOD
	treeNodeClass = CVTBinOpMul
	treeNode = SheetRefField( CVTBinOpMul )
	operatorCharacterString = '*'


class CVBinOpDiv (CVBinaryOperatorBox):
	precedence = _CVBinaryOperatorPrecedence.MUL_DIV_MOD
	disableLeftChildParens = True
	disableRightChildParens = True
	treeNodeClass = CVTBinOpDiv
	treeNode = SheetRefField( CVTBinOpDiv )

	def _o_makeBox(self):
		return DTBox( direction=DTDirection.TOP_TO_BOTTOM, spacing=2.0 )

	def _o_packOperatorWidget(self, box):
		box.append( DTHLine( 2.0, colour=Colour3f( 0.0, 0.6, 0.0 ) ), minorDirectionAlignment=DTBox.ALIGN_EXPAND )


class CVBinOpPow (CVBinaryOperatorScript):
	precedence = _CVBinaryOperatorPrecedence.POW
	disableRightChildParens = True
	treeNodeClass = CVTBinOpPow
	treeNode = SheetRefField( CVTBinOpPow )
	scriptMode = DTScript.SUPERSCRIPT

class CVBinOpMod (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.MUL_DIV_MOD
	treeNodeClass = CVTBinOpMod
	treeNode = SheetRefField( CVTBinOpMod )
	operatorCharacterString = '%'

class CVBinOpBitAnd (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.BIT_AND
	treeNodeClass = CVTBinOpBitAnd
	treeNode = SheetRefField( CVTBinOpBitAnd )
	operatorCharacterString = '&'

class CVBinOpBitOr (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.BIT_OR
	treeNodeClass = CVTBinOpBitOr
	treeNode = SheetRefField( CVTBinOpBitOr )
	operatorCharacterString = '|'

class CVBinOpBitXor (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.BIT_XOR
	treeNodeClass = CVTBinOpBitXor
	treeNode = SheetRefField( CVTBinOpBitXor )
	operatorCharacterString = '^'

class CVBinOpEq (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.CMP
	treeNodeClass = CVTBinOpEq
	treeNode = SheetRefField( CVTBinOpEq )
	operatorCharacterString = '=='

class CVBinOpNEq (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.CMP
	treeNodeClass = CVTBinOpNEq
	treeNode = SheetRefField( CVTBinOpNEq )
	operatorCharacterString = '!='

class CVBinOpLT (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.CMP
	treeNodeClass = CVTBinOpLT
	treeNode = SheetRefField( CVTBinOpLT )
	operatorCharacterString = '<'

class CVBinOpGT (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.CMP
	treeNodeClass = CVTBinOpGT
	treeNode = SheetRefField( CVTBinOpGT )
	operatorCharacterString = '>'

class CVBinOpLTE (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.CMP
	treeNodeClass = CVTBinOpLTE
	treeNode = SheetRefField( CVTBinOpLTE )
	operatorCharacterString = '<='

class CVBinOpGTE (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.CMP
	treeNodeClass = CVTBinOpGTE
	treeNode = SheetRefField( CVTBinOpGTE )
	operatorCharacterString = '>='

class CVBinOpLShift (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.SHIFT
	treeNodeClass = CVTBinOpLShift
	treeNode = SheetRefField( CVTBinOpLShift )
	operatorCharacterString = '<<'

class CVBinOpRShift (CVBinaryOperatorSimple):
	precedence = _CVBinaryOperatorPrecedence.SHIFT
	treeNodeClass = CVTBinOpRShift
	treeNode = SheetRefField( CVTBinOpRShift )
	operatorCharacterString = '>>'

