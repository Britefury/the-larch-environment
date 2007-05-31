##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import string

from Britefury.CodeViewBehavior.CodeViewBehavior import *


class CVBBlockParametersBehavior (CodeViewBehavior):
	@CVBCharInputHandlerMethod( ',' )
	def _addParameter(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.addParameter( '' )
		viewNode._f_commandHistoryThaw()
		return True


	@CVBCharInputHandlerMethod( string.ascii_letters + '_' )
	def _addNamedParameter(self, viewNode, receivingNodePath, widget, event):
		viewNode._f_commandHistoryFreeze()
		viewNode.addParameter( event.keyString )
		viewNode._f_commandHistoryThaw()
		return True

