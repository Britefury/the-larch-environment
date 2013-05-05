##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color

from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Controls import SwitchButton, Button

from BritefuryJ.Pres.Primitive import Primitive, Label, Image, Spacer, Row

from BritefuryJ.StyleSheet import StyleSheet



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
			return Button(_plusStyle(Label('+')), on_add)
		else:
			def on_delete(button, event):
				live.setLiteralValue(None)

			deleteButton = Button(Image.systemIcon('delete'), on_delete)

			return Row([deleteButton, Spacer(5.0, 0.0), valueEditor])

	return editor