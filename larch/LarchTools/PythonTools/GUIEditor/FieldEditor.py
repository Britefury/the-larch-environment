##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Controls import SwitchButton, Button, TextEntry

from BritefuryJ.Pres.Primitive import Primitive, Label, Image, Spacer, Row
from BritefuryJ.Pres.UI import Form

from BritefuryJ.StyleSheet import StyleSheet

from LarchTools.PythonTools import GUIEditor



def enumSwitchButtonEditor(live, enumType, offOptions, onOptions=None):
	if onOptions is None:
		onOptions = offOptions

	@LiveFunction
	def displayLive():
		return live.getValue().ordinal()

	def _onChoice(control, prevChoice, choice):
		live.setLiteralValue(enumType.values()[choice])

	return SwitchButton(offOptions, onOptions, SwitchButton.Orientation.HORIZONTAL, displayLive, _onChoice)



def enumSwitchButtonEditorWithLabels(live, enumType, labelTexts):
	return enumSwitchButtonEditor(live, enumType, [Label(t)   for t in labelTexts])
	@LiveFunction
	def displayLive():
		return live.getValue().ordinal()

	def _onChoice(control, prevChoice, choice):
		live.setLiteralValue(enumType.values()[choice])

	options = [Label(t)   for t in ['Larger', 'Smaller', 'Fixed', 'None']]

	return SwitchButton(options, options, SwitchButton.Orientation.HORIZONTAL, displayLive, _onChoice)



_plusStyle = StyleSheet.style(Primitive.fontBold(True), Primitive.foreground(Color(0.0, 0.6, 0.0)), Primitive.fontSize(12))

def optionalTypedEditor(live, initialValue, editorFn):
	valueEditor = LiveValue(editorFn(live))

	@LiveFunction
	def editor():
		x = live.getValue()

		if x is None:
			def on_add(button, event):
				live.setLiteralValue(initialValue)
			return Button(_plusStyle(Label('+')), on_add).alignHPack()
		else:
			def on_delete(button, event):
				live.setLiteralValue(None)

			deleteButton = Button(Image.systemIcon('delete'), on_delete)

			return Row([deleteButton.alignHPack(), Spacer(5.0, 0.0).alignHPack(), valueEditor])

	return editor






exprBorder = SolidBorder( 1.0, 2.0, 5.0, 5.0, Color( 0.0, 0.25, 0.75 ), None )




def unaryBranchChildEditUIFormSections(branch):
	child = branch.child.node
	if child is not None:
		if isinstance(child, GUIEditor.PrimitiveComponents.GUILabel):
			textField = child.text
			if textField.isConstant():
				textLive = textField.constantValueLive
				return [Form.SmallSection('Label text', None, TextEntry.textEntryCommitOnChange(textLive))]
	return []

