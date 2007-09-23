##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Toolkit.DTEntry import *

from Britefury.DocView.MoveFocus import MoveFocus

from Britefury.DocViewBehavior.DocViewBehavior import *


class DVBDeleteNodeBehavior (DocViewBehavior):
	@DVBAccelInputHandlerMethod( 'BackSpace' )
	def _backspaceNode(self, viewNode, receivingNodePath, widget, event):
		if isinstance( widget, DTEntry ):
			if widget.text != '':
				return True
		viewNode._f_commandHistoryFreeze()
		viewNode.deleteNode( MoveFocus.LEFT )
		viewNode._f_commandHistoryThaw()
		return True


	@DVBAccelInputHandlerMethod( 'Delete' )
	def _deleteNode(self, viewNode, receivingNodePath, widget, event):
		if isinstance( widget, DTEntry ):
			if widget.text != '':
				return True
		viewNode._f_commandHistoryFreeze()
		viewNode.deleteNode( MoveFocus.RIGHT )
		viewNode._f_commandHistoryThaw()
		return True

