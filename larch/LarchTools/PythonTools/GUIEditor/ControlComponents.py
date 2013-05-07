##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from BritefuryJ.Controls import Button

from BritefuryJ.Pres.Primitive import Primitive, Label, Row
from BritefuryJ.Pres.UI import Form

from LarchCore.Languages.Python2 import Schema as Py

from LarchTools.PythonTools.GUIEditor.DataModel import ExprField, exprBorder
from LarchTools.PythonTools.GUIEditor.FieldEditor import unaryBranchChildEditUIFormSections
from LarchTools.PythonTools.GUIEditor.Component import blankCallModel
from LarchTools.PythonTools.GUIEditor.BranchComponent import GUIUnaryBranchComponent, emptyLabel
from LarchTools.PythonTools.GUIEditor.ComponentPalette import paletteItem, registerPaletteSubsection
from LarchTools.PythonTools.GUIEditor.PrimitiveComponents import GUILabel


class GUIButton (GUIUnaryBranchComponent):
	componentName = 'Button'

	onClick = ExprField()


	def _presentBranchContents(self, child, fragment, inheritedState):
		return Button(child, None)

	def _editUIFormSections(self):
		sections = []
		sections.extend( unaryBranchChildEditUIFormSections(self) )
		sections.append(Form.SmallSection('On click', None, exprBorder.surround( self.onClick.editUI() )))

		superSections = super(GUIButton, self)._editUIFormSections()
		return sections + superSections

	def __component_py_evalmodel__(self, codeGen):
		onClick = self.onClick.__py_evalmodel__(codeGen)
		button = codeGen.embeddedValue(Button)
		child = self.child
		childArg = child.__py_evalmodel__(codeGen)   if child is not None   else blankCallModel(codeGen)
		return Py.Call(target=button, args=[childArg, onClick])

_buttonItem = paletteItem(Label('Button'), lambda: GUIButton(child=GUILabel(text='Button')))


registerPaletteSubsection('Controls', [_buttonItem])