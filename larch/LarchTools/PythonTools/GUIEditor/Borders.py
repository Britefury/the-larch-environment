##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from java.awt import Color

from BritefuryJ.Graphics import FilledOutlinePainter, SolidBorder

from BritefuryJ.Live import LiveFunction, LiveValue

from BritefuryJ.Controls import Button, RealSpinEntry, ColourPicker

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Primitive, Label, Bin, Row, Column, Table
from BritefuryJ.Pres.UI import Form

from BritefuryJ.StyleSheet import StyleSheet

from LarchCore.Languages.Python2 import Schema as Py

from LarchTools.PythonTools.GUIEditor.DataModel import GUIObject, TypedEvalField
from LarchTools.PythonTools.GUIEditor.FieldEditor import optionalTypedEditor



_uniformStyle = StyleSheet.style(Primitive.foreground(Color(0.2, 0.25, 0.3)), Primitive.fontSize(11))
_boxStyle = StyleSheet.style(Primitive.background(FilledOutlinePainter(Color(0.7, 0.75, 0.8), Color(0.6, 0.65, 0.7))), Primitive.fontSize(11), Primitive.foreground(Color(0.0, 0.0, 0.0, 0.5)), Primitive.fontItalic(True))
_tableStyle = StyleSheet.style(Primitive.tableColumnSpacing(2.0), Primitive.tableRowSpacing(2.0))
_liveLabelStyle = StyleSheet.style(Primitive.foreground(Color(0.4, 0.45, 0.5)))



class AbstractGUIBorder (GUIObject):
	def formSections(self):
		raise NotImplementedError, 'abstract'

	def makeBorder(self):
		raise NotImplementedError, 'abstract'

	def apply(self, p):
		return self.makeBorder().surround(p)

	def __makeBorder_py_evalmodel__(self, codeGen):
		raise NotImplementedError, 'abstract'

	def __apply_py_evalmodel__(self, codeGen, py_p):
		py_border = self.__makeBorder_py_evalmodel__(codeGen)
		return Py.Call(target=Py.AttributeRef(target=py_border, name='surround'), args=[py_p])



def _liveLabel(live):
	@LiveFunction
	def label():
		return _liveLabelStyle(Label(str(live.getValue())))
	return label



class SolidGUIBorder (AbstractGUIBorder):
	thickness = TypedEvalField(float, 1.0)
	inset = TypedEvalField(float, 1.0)
	roundingX = TypedEvalField(float, 0.0)
	roundingY = TypedEvalField(float, 0.0)
	borderPaint = TypedEvalField(Color, Color.BLACK)
	backgroundPaint = TypedEvalField((Color, type(None)), None)
	highlightBorderPaint =TypedEvalField((Color, type(None)), None)
	highlightBackgroundPaint =TypedEvalField((Color, type(None)), None)

	def formSections(self):
		return [
			Form.SmallSection('Thickness', None, self.thickness.editUI(lambda live: RealSpinEntry(live, 0.0, 10240.0, 0.1, 10.0))),
			Form.SmallSection('Inset', None, self.inset.editUI(lambda live: RealSpinEntry(live, 0.0, 10240.0, 0.1, 10.0))),
			Form.SmallSection('Round-X', None, self.roundingX.editUI(lambda live: RealSpinEntry(live, 0.0, 10240.0, 0.1, 10.0))),
			Form.SmallSection('Round-Y', None, self.roundingY.editUI(lambda live: RealSpinEntry(live, 0.0, 10240.0, 0.1, 10.0))),
			Form.SmallSection('Border colour', None, self.borderPaint.editUI(lambda live: ColourPicker(live).alignHPack())),
			Form.SmallSection('Background colour', None, self.backgroundPaint.editUI(lambda live: optionalTypedEditor(live, Color.WHITE, lambda live: ColourPicker(live).alignHPack()))),
			Form.SmallSection('Hover border colour', None, self.highlightBorderPaint.editUI(lambda live: optionalTypedEditor(live, Color.BLACK, lambda live: ColourPicker(live).alignHPack()))),
			Form.SmallSection('Hover background colour', None, self.highlightBackgroundPaint.editUI(lambda live: optionalTypedEditor(live, Color.WHITE, lambda live: ColourPicker(live).alignHPack()))),
		]


	def makeBorder(self):
		thickness = self.thickness.getValueForEditor()
		inset = self.inset.getValueForEditor()
		roundingX = self.roundingX.getValueForEditor()
		roundingY = self.roundingY.getValueForEditor()
		borderPaint = self.borderPaint.getValueForEditor()
		backgroundPaint = self.backgroundPaint.getValueForEditor()
		highlightBorderPaint = self.highlightBorderPaint.getValueForEditor()
		highlightBackgroundPaint = self.highlightBackgroundPaint.getValueForEditor()
		return SolidBorder(thickness, inset, roundingX, roundingY, borderPaint, backgroundPaint).highlight(highlightBorderPaint, highlightBackgroundPaint)


	def __makeBorder_py_evalmodel__(self, codeGen):
		thickness = self.thickness.__py_evalmodel__(codeGen)
		inset = self.inset.__py_evalmodel__(codeGen)
		roundingX = self.roundingX.__py_evalmodel__(codeGen)
		roundingY = self.roundingY.__py_evalmodel__(codeGen)
		borderPaint = self.borderPaint.__py_evalmodel__(codeGen)
		backgroundPaint = self.backgroundPaint.__py_evalmodel__(codeGen)
		highlightBorderPaint = self.highlightBorderPaint.__py_evalmodel__(codeGen)
		highlightBackgroundPaint = self.highlightBackgroundPaint.__py_evalmodel__(codeGen)

		py_SolidBorder = codeGen.embeddedValue(SolidBorder)
		return Py.Call(target=py_SolidBorder, args=[thickness, inset, roundingX, roundingY, borderPaint, backgroundPaint, highlightBorderPaint, highlightBackgroundPaint])






