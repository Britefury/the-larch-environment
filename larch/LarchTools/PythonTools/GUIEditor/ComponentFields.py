##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Live import TrackedLiveValue

from BritefuryJ.Pres.Primitive import Primitive, Label, Spacer, Row, Column

from BritefuryJ.Controls import Button

from BritefuryJ.StyleSheet import StyleSheet

from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Expr



exprBorder = SolidBorder( 1.0, 2.0, 5.0, 5.0, Color( 0.0, 0.25, 0.75 ), None )




class ValueField (object):
	"""
	A field that contains a value, which can alternatively have an expression that generates the required value
	"""
	def __init__(self, value, valueControlFactoryFn, valueToModelFn):
		self.__value = TrackedLiveValue(value)
		self.__valueControlFactoryFn = valueControlFactoryFn
		self.__valueToModelFn = valueToModelFn
		self.__expr = None
		self.__incr = IncrementalValueMonitor()


	def getValueForEditor(self):
		return self.__value.getValue()


	def __py_evalmodel__(self, codeGen):
		if self.__expr is None:
			return self.__valueToModelFn(self.__value.getValue())
		else:
			return self.__expr.model


	def editUI(self):
		self.__incr.onAccess()
		valueControl = self.__valueControlFactoryFn(self.__value)

		if self.__expr is None:
			def _onAdd(button, event):
				self.__expr = EmbeddedPython2Expr()
				self.__incr.onChanged()

			addButton = Button(self._addButtonContents, _onAdd)

			return Row([valueControl, Spacer(10.0, 0.0), addButton])
		else:
			def _onRemove(button, event):
				self.__expr = None
				self.__incr.onChanged()

			removeButton = Button(self._removeButtonContents, _onRemove)

			return Column([valueControl, Row([removeButton, Spacer(10.0, 0.0), exprBorder.surround(self.__expr)])])


	_addStyle = StyleSheet.style(Primitive.foreground(Color(0.0, 0.5, 0.0)), Primitive.fontBold(True), Primitive.fontSize(11))
	_removeStyle = StyleSheet.style(Primitive.foreground(Color(0.5, 0.0, 0.0)), Primitive.fontBold(True), Primitive.fontSize(11))
	_fStyle = StyleSheet.style(Primitive.foreground(Color(0.0, 0.25, 0.5)), Primitive.fontItalic(True), Primitive.fontSize(11))
	_parenStyle = StyleSheet.style(Primitive.foreground(Color(0.3, 0.3, 0.3)), Primitive.fontSize(11))

	_addButtonContents = Row([_addStyle(Label('+ ')), _fStyle(Label('f')), _parenStyle(Label('()'))])
	_removeButtonContents = Row([_removeStyle(Label('- ')), _fStyle(Label('f')), _parenStyle(Label('()'))])
