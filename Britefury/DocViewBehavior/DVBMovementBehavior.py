##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocViewBehavior.DocViewBehavior import *


class DVBMovementBehavior (DocViewBehavior):
	@DVBAccelInputHandlerMethod( 'Up' )
	def _up(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorUp()
		viewNode._f_commandHistoryThaw()
		return True

	@DVBAccelInputHandlerMethod( [ 'Down', 'Return' ] )
	def _down(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorDown()
		viewNode._f_commandHistoryThaw()
		return True



	@DVBAccelInputHandlerMethod( 'Left' )
	def _left(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorLeft()
		viewNode._f_commandHistoryThaw()
		return True

	@DVBAccelInputHandlerMethod( 'Right' )
	def _right(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorRight()
		viewNode._f_commandHistoryThaw()
		return True


	@DVBAccelInputHandlerMethod( '<control>Left' )
	def _itemToLeft(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorLeft( True )
		viewNode._f_commandHistoryThaw()
		return True

	@DVBAccelInputHandlerMethod( '<control>Right' )
	def _itemToRight(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorRight( True )
		viewNode._f_commandHistoryThaw()
		return True



	@DVBAccelInputHandlerMethod( '<alt>Left' )
	def _leftChild(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorToLeftChild()
		viewNode._f_commandHistoryThaw()
		return True

	@DVBAccelInputHandlerMethod( '<alt>Right' )
	def _rightChild(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorToRightChild()
		viewNode._f_commandHistoryThaw()
		return True

	@DVBAccelInputHandlerMethod( '<alt>Up' )
	def _toParent(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.cursorToParent()
		viewNode._f_commandHistoryThaw()
		return True





