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
		result = viewNode.cursorUp()
		viewNode._f_commandHistoryThaw()
		return result

	@CVBAccelInputHandlerMethod( 'Down' )
	def _down(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		result = viewNode.cursorDown()
		viewNode._f_commandHistoryThaw()
		return result



	@CVBAccelInputHandlerMethod( 'Left' )
	def _left(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		result = viewNode.cursorLeft()
		viewNode._f_commandHistoryThaw()
		return result

	@CVBAccelInputHandlerMethod( 'Right' )
	def _right(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		result = viewNode.cursorRight()
		viewNode._f_commandHistoryThaw()
		return result


	@CVBAccelInputHandlerMethod( '<control>Left' )
	def _itemToLeft(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		result = viewNode.cursorLeft( True )
		viewNode._f_commandHistoryThaw()
		return result

	@CVBAccelInputHandlerMethod( '<control>Right' )
	def _itemToRight(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		result = viewNode.cursorRight( True )
		viewNode._f_commandHistoryThaw()
		return result




	@CVBAccelInputHandlerMethod( '<alt><control>Left' )
	def _leftSibling(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		result = viewNode.cursorToLeftSibling()
		viewNode._f_commandHistoryThaw()
		return result

	@CVBAccelInputHandlerMethod( '<alt><control>Right' )
	def _rightSibling(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		result = viewNode.cursorToRightSibling()
		viewNode._f_commandHistoryThaw()
		return result


	@CVBAccelInputHandlerMethod( [ '<alt>Up', '<alt><control>Up' ] )
	def _leftParent(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		result = viewNode.cursorToParent()
		viewNode._f_commandHistoryThaw()
		return result




	@CVBAccelInputHandlerMethod( '<alt>Left' )
	def _leftChild(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		result = viewNode.cursorToLeftChild()
		viewNode._f_commandHistoryThaw()
		return result

	@CVBAccelInputHandlerMethod( '<alt>Right' )
	def _rightChild(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		result = viewNode.cursorToRightChild()
		viewNode._f_commandHistoryThaw()
		return result





