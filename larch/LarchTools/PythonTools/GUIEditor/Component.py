##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color

from BritefuryJ.LSpace.Layout import HAlignment, VAlignment

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Pres.Primitive import Blank
from BritefuryJ.Pres.UI import Form

from LarchCore.Languages.Python2 import Schema as Py

from LarchTools.PythonTools.GUIEditor.DataModel import GUINode, EnumEvalField
from LarchTools.PythonTools.GUIEditor.Properties import GUICProp
from LarchTools.PythonTools.GUIEditor.Target import GUITargetInteractor, GUIScrollInteractor
from LarchTools.PythonTools.GUIEditor.ContextMenu import componentContextMenu



componentBorder = SolidBorder(1.0, 2.0, Color(0.8, 0.8, 0.8), None)




def blankCallModel(codeGen):
	blank = codeGen.embeddedValue(Blank)
	return Py.Call(target=blank, args=[])



class GUIComponent (GUINode):
	isRootGUIEditorComponent = False
	componentName = '<abstract-Component>'


	hAlignment = EnumEvalField()


	def __init__(self, **values):
		super(GUIComponent, self).__init__(**values)


	@property
	def guiEditor(self):
		return self._parent.guiEditor   if self._parent is not None   else None


	def _presentContents(self, fragment, inheritedState):
		raise NotImplementedError, 'abstract'

	def _lookFor(self, x):
		return False


	def _editUI(self):
		return self._editUIForm()

	def _editUIForm(self):
		sections = self._editUIFormSections()
		if len(sections) > 0:
			return Form(None, sections)
		else:
			return Blank()

	def _editUIFormSections(self):

		return []

	def __present__(self, fragment, inheritedState):
		p = self._presentContents(fragment, inheritedState)
		p = componentBorder.surround(p)
		p = p.withContextMenuInteractor(componentContextMenu)
		p = p.withElementInteractor(GUITargetInteractor())
		p = p.withElementInteractor(GUIScrollInteractor())
		p = p.withProperty(GUICProp.instance, self)
		return p

	def __py_evalmodel__(self, codeGen):
		raise NotImplementedError, 'abstract'







