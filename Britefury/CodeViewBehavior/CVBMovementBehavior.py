##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeViewBehavior.CodeViewBehavior import *


class CVBMovementBehavior (CodeViewBehavior):
	@CVBAccelInputHandlerMethod( 'Up' )
	def _up(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorUp()
		viewNode._f_commandHistoryThaw()
		return True

	@CVBAccelInputHandlerMethod( [ 'Down', 'Return' ] )
	def _down(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorDown()
		viewNode._f_commandHistoryThaw()
		return True



	@CVBAccelInputHandlerMethod( 'Left' )
	def _left(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorLeft()
		viewNode._f_commandHistoryThaw()
		return True

	@CVBAccelInputHandlerMethod( 'Right' )
	def _right(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorRight()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBAccelInputHandlerMethod( '<control>Left' )
	def _itemToLeft(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorLeft( True )
		viewNode._f_commandHistoryThaw()
		return True

	@CVBAccelInputHandlerMethod( '<control>Right' )
	def _itemToRight(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorRight( True )
		viewNode._f_commandHistoryThaw()
		return True



	@CVBAccelInputHandlerMethod( '<alt>Left' )
	def _leftChild(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorToLeftChild()
		viewNode._f_commandHistoryThaw()
		return True

	@CVBAccelInputHandlerMethod( '<alt>Right' )
	def _rightChild(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorToRightChild()
		viewNode._f_commandHistoryThaw()
		return True

	@CVBAccelInputHandlerMethod( '<alt>Up' )
	def _toParent(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorToParent()
		viewNode._f_commandHistoryThaw()
		return True





