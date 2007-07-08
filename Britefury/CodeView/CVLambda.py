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

from Britefury.Math.Math import Colour3f

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTLambda import CVTLambda

from Britefury.CodeView.CVExpression import *
from Britefury.CodeView.CVLabel import *

from Britefury.CodeViewBehavior.CVBLambdaBehavior import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTBorder import DTBorder
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection



class CVLambda (CVExpression):
	treeNodeClass = CVTLambda


	treeNode = SheetRefField( CVTLambda )


	behaviors = [ CVBLambdaBehavior() ]


	@FunctionRefField
	def lambdaLabelNode(self):
		label = self._view.buildView( self.treeNode, self, CVLabel )
		label.setMarkup( 'L<span size="small">AMBDA</span>' )
		label.setColour( Colour3f( 0.0, 0.6, 0.0 ) )
		label.setFont( 'Sans bold 11' )
		return label

	@FunctionRefField
	def lambdaLabelWidget(self):
		return self.lambdaLabelNode.widget


	@FunctionRefField
	def paramsNode(self):
		paramsView = self._view.buildView( self.treeNode.paramsNode, self )
		paramsView.disableParens()
		return paramsView

	@FunctionRefField
	def paramsWidget(self):
		return self.paramsNode.widget


	@FunctionRefField
	def valueExprNode(self):
		return self._view.buildView( self.treeNode.valueExprNode, self )

	@FunctionRefField
	def valueExprWidget(self):
		return self.valueExprNode.widget


	@FunctionField
	def _refreshLambdaLabel(self):
		self._lambdaBox[0] = self.lambdaLabelWidget

	@FunctionField
	def _refreshParams(self):
		self._lambdaBox[1] = self.paramsWidget

	@FunctionField
	def _refreshValueExpr(self):
		self._lambdaBox[3] = self.valueExprWidget

	@FunctionField
	def refreshCell(self):
		self._refreshLambdaLabel
		self._refreshParams
		self._refreshValueExpr





	def __init__(self, treeNode, view):
		super( CVLambda, self ).__init__( treeNode, view )
		self._lambdaBox = DTBox( spacing=1.0, minorDirectionAlignment=DTBox.ALIGN_EXPAND )
		self._lambdaBox.append( DTLabel( 'nil' ) )
		self._lambdaBox.append( DTLabel( 'nil' ) )
		self._lambdaBox.append( DTLabel( ':' ) )
		self._lambdaBox.append( DTLabel( 'nil' ) )
		self.widget.child = self._lambdaBox


	def deleteChild(self, child, moveFocus):
		if child is self.lambdaLabelNode:
			self.deleteNode( moveFocus )
		elif child is self.valueExprNode:
			self.valueExprNode.treeNode.replaceWithNullExpression()
			self._view.refresh()
			self.valueExprNode.startEditing()


	def horizontalNavigationList(self):
		return [ self.lambdaLabelNode, self.paramsNode, self.valueExprNode ]


	def startEditingParameters(self):
		self.paramsNode.startEditing()

	def startEditingValueExpr(self):
		self.valueExprNode.makeCurrent()

