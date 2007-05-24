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

from Britefury.CodeViewTree.CVTClosure import CVTClosure

from Britefury.CodeView.CVExpression import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTBorder import DTBorder
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection



class CVClosure (CVExpression):
	treeNodeClass = CVTClosure


	treeNode = SheetRefField( CVTClosure )


	@FunctionRefField
	def paramsNode(self):
		return self._view.buildView( self.treeNode.paramsNode, self )

	@FunctionRefField
	def paramsWidget(self):
		return self.paramsNode.widget


	@FunctionRefField
	def statementsNode(self):
		return self._view.buildView( self.treeNode.statementsNode, self )

	@FunctionRefField
	def statementsWidget(self):
		return self.statementsNode.widget


	@FunctionField
	def _refreshParams(self):
		self._lambdaBox[2] = self.paramsWidget

	@FunctionField
	def _refreshStatements(self):
		self._statementsBorder.child = self.statementsWidget

	@FunctionField
	def refreshCell(self):
		self._refreshParams
		self._refreshStatements





	def __init__(self, treeNode, view):
		super( CVClosure, self ).__init__( treeNode, view )
		self._lambdaBox = DTBox( spacing=1.0 )
		self._lambdaBox.append( DTLabel( 'lambda' ) )
		self._lambdaBox.append( DTLabel( '(' ) )
		self._lambdaBox.append( DTLabel( 'nil' ) )
		self._lambdaBox.append( DTLabel( '):' ) )
		self._statementsBorder = DTBorder( 30.0, 0.0, 0.0, 0.0 )
		self._statementsBorder.child = DTLabel( 'nil' )
		self._box = DTBox( DTDirection.TOP_TO_BOTTOM, spacing=4.0 )
		self._box.append( self._lambdaBox )
		self._box.append( self._statementsBorder )
		self.widget.child = self._box
