##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeViewBehavior.CodeViewBehavior import *


class CVBWhileBehavior (CodeViewBehavior):
	@CVBCharInputHandlerMethod( ':' )
	def _gotoStatements(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.startEditingStatements()
		viewNode._f_commandHistoryThaw()
		return True

	@CVBAccelInputHandlerMethod( '<alt>e' )
	def _addElse(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.addElse()
		viewNode._f_commandHistoryThaw()
		return True


