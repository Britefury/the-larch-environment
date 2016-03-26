##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from java.awt import Color

from BritefuryJ.Live import LiveFunction

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Controls import TextEntry

from BritefuryJ.Pres.Primitive import Primitive, Label, Row
from BritefuryJ.Pres.UI import Form

from BritefuryJ.StyleSheet import StyleSheet

from LarchCore.Languages.Python2 import Schema as Py
from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Expr

from LarchTools.PythonTools.GUIEditor.DataModel import ExprField, TypedField
from LarchTools.PythonTools.GUIEditor.LeafComponent import GUILeafComponent
from LarchTools.PythonTools.GUIEditor.ComponentPalette import paletteItem, registerPaletteSubsection




_evalLabelStyle = StyleSheet.style(Primitive.fontItalic(True), Primitive.fontSize( 11 ), Primitive.foreground(Color(0.3, 0.4, 0.5)))
_liveEvalLabelStyle = StyleSheet.style(Primitive.fontItalic(True), Primitive.fontSize( 11 ), Primitive.foreground(Color(0.4, 0.3, 0.5)))

_evalItemStyleF = StyleSheet.style(Primitive.fontItalic(True), Primitive.foreground(Color(0.2, 0.4, 0.6)))
_evalItemStyleParens = StyleSheet.style(Primitive.foreground(Color(0.4, 0.4, 0.4)))




class GUIEval (GUILeafComponent):
	componentName = 'Eval'

	expr = ExprField()
	displayedText = TypedField(str, 'expr')

	def _presentLeafContents(self, fragment, inheritedState):
		displayedText = self.displayedText.value
		return _evalLabelStyle.applyTo(Label('=<' + displayedText + '>'))

	def _editUIFormSections(self):
		expr = Form.SmallSection('Expression', None, self.expr.editUI())
		displayedText = Form.SmallSection('Displayed text', None, TextEntry.textEntryCommitOnChange(self.displayedText.live))
		superSections = super(GUIEval, self)._editUIFormSections()
		return [expr, displayedText] + superSections

	def __component_py_evalmodel__(self, codeGen):
		return self.expr.expr.model

_evalItem = paletteItem(_evalItemStyleF(Label('Eval')), lambda: GUIEval())




class GUILiveEval (GUIEval):
	componentName = 'Live Eval'

	def _presentLeafContents(self, fragment, inheritedState):
		displayedText = self.displayedText.value
		if displayedText != '':
			return _liveEvalLabelStyle.applyTo(Label('=<' + displayedText + '>...'))
		else:
			return self.expr.editUI()

	def __component_py_evalmodel__(self, codeGen):
		liveFun = codeGen.embeddedValue(LiveFunction)
		lmb = Py.LambdaExpr(params=[], expr=self.expr.expr.model)
		live = Py.Call(target=liveFun, args=[lmb])
		return live

_liveEvalItem = paletteItem(Row([Label('Live '), _evalItemStyleF(Label('f')), _evalItemStyleParens(Label('()'))]), lambda: GUILiveEval())


registerPaletteSubsection('Code', [_evalItem, _liveEvalItem])