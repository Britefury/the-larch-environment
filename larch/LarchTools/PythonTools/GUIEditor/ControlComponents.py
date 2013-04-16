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
from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Expr

from LarchTools.PythonTools.GUIEditor.ComponentFields import exprBorder
from LarchTools.PythonTools.GUIEditor.Component import blankCallModel
from LarchTools.PythonTools.GUIEditor.BranchComponent import GUIUnaryBranchComponent, emptyLabel
from LarchTools.PythonTools.GUIEditor.ComponentPalette import paletteItem, registerPaletteSubsection


class GUIButton (GUIUnaryBranchComponent):
	componentName = 'Button'

	def __init__(self, child=None, onClick=None):
		super(GUIButton, self).__init__(child)
		if onClick is None:
			onClick = EmbeddedPython2Expr.fromText('None')
		self._onClick = onClick


	@property
	def onClick(self):
		return self._onClick


	def _presentBranchContents(self, fragment, inheritedState):
		child = self._presentChild()
		return Button(child, None)

	def _editUI(self):
		onClick = Form.SmallSection('On click', None, exprBorder.surround( self._onClick ))
		return Form(None, [onClick])

	def __py_evalmodel__(self, codeGen):
		onClick = self._onClick.model
		button = codeGen.embeddedValue(Button)
		child = self.child
		childArg = child.__py_evalmodel__(codeGen)   if child is not None   else blankCallModel(codeGen)
		return Py.Call(target=button, args=[childArg, onClick])

_buttonItem = paletteItem(Label('Button'), lambda: GUIButton())


registerPaletteSubsection('Controls', [_buttonItem])