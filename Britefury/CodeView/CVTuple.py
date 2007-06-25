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

from Britefury.CodeViewTree.CVTTuple import CVTTuple

from Britefury.CodeView.CVNode import *
from Britefury.CodeView.CVExpression import *

from Britefury.CodeViewBehavior.CVBTupleBehavior import *
from Britefury.CodeViewBehavior.CVBCreateExpressionBehavior import *
from Britefury.CodeViewBehavior.CVBWrapInAssignmentBehavior import *

from Britefury.DocView.Toolkit.DTWrappedLineWithSeparators import DTWrappedLineWithSeparators
from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTCustomSymbol import DTCustomSymbol
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class _TupleOpen (DTCustomSymbol):
	def _o_getSymbolSizeRequest(self):
		return Vector2( self._size*1.25, self._size*1.25 )

	def _o_drawSymbol(self, context):
		context.set_line_width( 1.0 / (11.0*1.25) )
		context.scale( self._size*1.25, self._size*1.25 )
		context.move_to( 0.4, 0.0 )
		context.line_to( 0.1, 0.5 )
		context.line_to( 0.4, 1.0 )

		context.move_to( 0.8, 0.0 )
		context.line_to( 0.6, 0.333 )
		context.line_to( 0.6, 0.667 )
		context.line_to( 0.8, 1.0 )
		context.stroke()




class _TupleClose (DTCustomSymbol):
	def _o_getSymbolSizeRequest(self):
		return Vector2( self._size*1.25, self._size*1.25 )

	def _o_drawSymbol(self, context):
		context.set_line_width( 1.0 / (11.0*1.25) )
		context.scale( self._size*1.25, self._size*1.25 )
		context.move_to( 0.2, 0.0 )
		context.line_to( 0.4, 0.333 )
		context.line_to( 0.4, 0.667 )
		context.line_to( 0.2, 1.0 )

		context.move_to( 0.6, 0.0 )
		context.line_to( 0.9, 0.5 )
		context.line_to( 0.6, 1.0 )
		context.stroke()





class CVTuple (CVExpression):
	treeNodeClass = CVTTuple


	treeNode = SheetRefField( CVTTuple )



	behaviors = [ CVBTupleBehavior(), CVBCreateExpressionBehavior(), CVBWrapInAssignmentBehavior() ]



	@FunctionField
	def argNodes(self):
		return [ self._view.buildView( argNode, self )   for argNode in self.treeNode.argNodes ]

	@FunctionField
	def argWidgets(self):
		return [ node.widget   for node in self.argNodes ]




	@FunctionField
	def _refreshArgs(self):
		self._argsLine[:] = self.argWidgets

	@FunctionField
	def refreshCell(self):
		self._refreshArgs




	def __init__(self, treeNode, view):
		super( CVTuple, self ).__init__( treeNode, view )
		self._argsLine = DTWrappedLineWithSeparators( spacing=5.0 )
		self._box = DTBox()
		self._box.append( _TupleOpen() )
		self._box.append( self._argsLine )
		self._box.append( _TupleClose() )
		self.widget.child = self._box



	def addArgument(self):
		argCVT = self.treeNode.addArgument()
		self._view.refresh()
		argCV = self._view.getViewNodeForTreeNode( argCVT )
		argCV.startEditing()


	def deleteChild(self, child, moveFocus):
		child._o_moveFocus( moveFocus )
		self.treeNode.deleteArgument( child.treeNode )
		self._view.refresh()



	def startEditing(self):
		self.widget.grabFocus()



	def horizontalNavigationList(self):
		return self.argNodes




	def getInsertPosition(self, receivingNodePath):
		if len( receivingNodePath ) > 1:
			child = receivingNodePath[1]
			try:
				return self.argNodes.index( child )
			except ValueError:
				return 0
		return 0


