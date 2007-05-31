##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeViewBehavior.CodeViewBehavior import *


class CVBSendMessageBehavior (CodeViewBehavior):
	@CVBCharInputHandlerMethod( '(' )
	def _editArguments(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.startEditingArguments()
		viewNode._f_commandHistoryThaw()
		return True


	@CVBCharInputHandlerMethod( ')' )
	def _stopEditingArguments(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.stopEditingArguments()
		viewNode._f_commandHistoryThaw()
		return True



