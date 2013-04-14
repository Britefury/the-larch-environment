##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************

from LarchTools.PythonTools.GUIEditor.Component import GUIComponent
from LarchTools.PythonTools.GUIEditor.BranchComponent import SequentialGUIController




class GUILeafComponent (GUIComponent):
	def _presentLeafContents(self, fragment, inheritedState):
		raise NotImplementedError, 'abstract'


	def _presentContents(self, fragment, inheritedState):
		p = self._presentLeafContents(fragment, inheritedState)
		p = SequentialGUIController.instance.item(self, p)
		return p
