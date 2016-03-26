##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
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
