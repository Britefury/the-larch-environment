##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color

from BritefuryJ.Graphics import FilledOutlinePainter

from BritefuryJ.Live import LiveFunction, LiveValue

from BritefuryJ.Controls import Button, RealSpinEntry

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Primitive, Label, Bin, Row, Column, Table

from BritefuryJ.StyleSheet import StyleSheet

from LarchCore.Languages.Python2 import Schema as Py

from LarchTools.PythonTools.GUIEditor.DataModel import GUIObject, TypedEvalField



_uniformStyle = StyleSheet.style(Primitive.foreground(Color(0.2, 0.25, 0.3)), Primitive.fontSize(11))
_boxStyle = StyleSheet.style(Primitive.background(FilledOutlinePainter(Color(0.7, 0.75, 0.8), Color(0.6, 0.65, 0.7))), Primitive.fontSize(11), Primitive.foreground(Color(0.0, 0.0, 0.0, 0.5)), Primitive.fontItalic(True))
_tableStyle = StyleSheet.style(Primitive.tableColumnSpacing(2.0), Primitive.tableRowSpacing(2.0))
_liveLabelStyle = StyleSheet.style(Primitive.foreground(Color(0.4, 0.45, 0.5)))



class AbstractPadding (GUIObject):
	def apply(self, p):
		raise NotImplementedError, 'abstract'

	def __apply_py_evalmodel__(self, codeGen, py_p):
		raise NotImplementedError, 'abstract'



def _liveLabel(live):
	@LiveFunction
	def label():
		return _liveLabelStyle(Label(str(live.getValue())))
	return label



class UniformPadding (AbstractPadding):
	x = TypedEvalField(float, 0.0)
	y = TypedEvalField(float, 0.0)

	def editUI(self, setPaddingFn):
		box = _boxStyle(Bin(Label('Padded element'))).alignHExpand().alignVExpand()

		left = self.x.editUI(lambda live: RealSpinEntry(live, 0.0, 1048576.0, 1.0, 10.0))
		right = _liveLabel(self.x.constantValueLive)
		top = self.y.editUI(lambda live: RealSpinEntry(live, 0.0, 1048576.0, 1.0, 10.0))
		bottom = _liveLabel(self.y.constantValueLive)

		return _tableStyle(Table([[None, top, None], [left, box, right], [None, bottom, None]]))


	def apply(self, p):
		x = self.x.getValueForEditor()
		y = self.y.getValueForEditor()
		return Pres.coerce(p).pad(x, y)


	def __apply_py_evalmodel__(self, codeGen, py_p):
		x = self.x.__py_evalmodel__(codeGen)
		y = self.y.__py_evalmodel__(codeGen)

		py_pres = codeGen.embeddedValue(Pres)
		py_pres_coerce = Py.AttributeRef(target=py_pres, name='coerce')
		py_p = Py.Call(target=py_pres_coerce, args=[py_p])
		py_p = Py.Call(target=Py.AttributeRef(target=py_p, name='pad'), args=[x, y])

		return py_p






class NonUniformPadding (AbstractPadding):
	left = TypedEvalField(float, 0.0)
	right = TypedEvalField(float, 0.0)
	top = TypedEvalField(float, 0.0)
	bottom = TypedEvalField(float, 0.0)

	def editUI(self, setPaddingFn):
		box = _boxStyle(Bin(Label('Padded element'))).alignHExpand().alignVExpand()

		left = self.left.editUI(lambda live: RealSpinEntry(live, 0.0, 1048576.0, 1.0, 10.0))
		right = self.right.editUI(lambda live: RealSpinEntry(live, 0.0, 1048576.0, 1.0, 10.0))
		top = self.top.editUI(lambda live: RealSpinEntry(live, 0.0, 1048576.0, 1.0, 10.0))
		bottom = self.bottom.editUI(lambda live: RealSpinEntry(live, 0.0, 1048576.0, 1.0, 10.0))

		return _tableStyle(Table([[None, top, None], [left, box, right], [None, bottom, None]]))


	def apply(self, p):
		left = self.left.getValueForEditor()
		right = self.right.getValueForEditor()
		top = self.top.getValueForEditor()
		bottom = self.bottom.getValueForEditor()
		return Pres.coerce(p).pad(left, right, top, bottom)


	def __apply_py_evalmodel__(self, codeGen, py_p):
		left = self.left.__py_evalmodel__(codeGen)
		right = self.right.__py_evalmodel__(codeGen)
		top = self.top.__py_evalmodel__(codeGen)
		bottom = self.bottom.__py_evalmodel__(codeGen)

		py_pres = codeGen.embeddedValue(Pres)
		py_pres_coerce = Py.AttributeRef(target=py_pres, name='coerce')
		py_p = Py.Call(target=py_pres_coerce, args=[py_p])
		py_p = Py.Call(target=Py.AttributeRef(target=py_p, name='pad'), args=[left, right, top, bottom])

		return py_p



