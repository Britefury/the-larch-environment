##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Pres.Primitive import Blank

from LarchTools.PythonTools.GUIEditor.Properties import GUICProp
from LarchTools.PythonTools.GUIEditor.Target import GUITargetInteractor, GUIScrollInteractor
from LarchTools.PythonTools.GUIEditor.ContextMenu import componentContextMenu



componentBorder = SolidBorder(1.0, 2.0, Color(0.8, 0.8, 0.8), None)


class GUIComponent (object):
	componentName = 'Component'

	def _presentContents(self, fragment, inheritedState):
		raise NotImplementedError, 'abstract'

	def _lookFor(self, x):
		return False


	def _editUI(self):
		return Blank()

	def __present__(self, fragment, inheritedState):
		x = self._presentContents(fragment, inheritedState)
		x = componentBorder.surround(x)
		x = x.withContextMenuInteractor(componentContextMenu)
		x = x.withElementInteractor(GUITargetInteractor())
		x = x.withElementInteractor(GUIScrollInteractor())
		x = x.withProperty(GUICProp.instance, self)
		return x

	def __py_evalmodel__(self, codeGen):
		raise NotImplementedError, 'abstract'







