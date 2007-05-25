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
		return viewNode.cursorUp()

	@CVBAccelInputHandlerMethod( 'Down' )
	def _down(self, viewNode, receivingNodePath, widget, event):
		return viewNode.cursorDown()



	@CVBAccelInputHandlerMethod( '<alt>Left' )
	def _itemToLeft(self, viewNode, receivingNodePath, widget, event):
		return viewNode.cursorLeft( True )

	@CVBAccelInputHandlerMethod( '<alt>Right' )
	def _itemToRight(self, viewNode, receivingNodePath, widget, event):
		return viewNode.cursorRight( True )




	@CVBAccelInputHandlerMethod( 'Left' )
	def _left(self, viewNode, receivingNodePath, widget, event):
		return viewNode.cursorLeft()

	@CVBAccelInputHandlerMethod( 'Right' )
	def _right(self, viewNode, receivingNodePath, widget, event):
		return viewNode.cursorRight()



