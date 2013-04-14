##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color

from BritefuryJ.Live import LiveFunction

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Pres.Primitive import Primitive, Label, Row
from BritefuryJ.Pres.UI import Form

from BritefuryJ.StyleSheet import StyleSheet

from LarchCore.Languages.Python2 import Schema as Py
from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Expr

from LarchTools.PythonTools.GUIEditor.LeafComponent import GUILeafComponent
from LarchTools.PythonTools.GUIEditor.ComponentPalette import paletteItem, registerPaletteSubsection
from LarchTools.PythonTools.GUIEditor.Component import exprBorder




_evalLabelStyle = StyleSheet.style(Primitive.fontItalic(True), Primitive.fontSize( 11 ), Primitive.foreground(Color(0.3, 0.4, 0.5)))
_evalLabel = _evalLabelStyle.applyTo(Label('<eval>'))
_liveEvalLabel = _evalLabelStyle.applyTo(Label('<live()>'))

_evalItemStyleF = StyleSheet.style(Primitive.fontItalic(True), Primitive.foreground(Color(0.2, 0.4, 0.6)))
_evalItemStyleParens = StyleSheet.style(Primitive.foreground(Color(0.4, 0.4, 0.4)))




class GUIEval (GUILeafComponent):
	componentName = 'Eval'

	def __init__(self, expr=None):
		super(GUIEval, self).__init__()
		if expr is None:
			expr = EmbeddedPython2Expr()
		self._expr = expr


	@property
	def expr(self):
		return self._expr


	def _presentLeafContents(self, fragment, inheritedState):
		return _evalLabel

	def _editUI(self):
		expr = Form.SmallSection('Expression', None, exprBorder.surround( self._expr ))
		return Form(None, [expr])

	def __py_evalmodel__(self, codeGen):
		return self._expr.model

_evalItem = paletteItem(_evalItemStyleF(Label('Eval')), lambda: GUIEval())




class GUILiveEval (GUIEval):
	componentName = 'Live Eval'

	def __init__(self, expr=None):
		super(GUILiveEval, self).__init__(expr)


	def _presentLeafContents(self, fragment, inheritedState):
		return _liveEvalLabel

	def __py_evalmodel__(self, codeGen):
		liveFun = codeGen.embeddedValue(LiveFunction)
		lmb = Py.LambdaExpr(params=[], expr=self._expr.model)
		live = Py.Call(target=liveFun, args=[lmb])
		return live

_liveEvalItem = paletteItem(Row([Label('Live '), _evalItemStyleF(Label('f')), _evalItemStyleParens(Label('()'))]), lambda: GUILiveEval())


registerPaletteSubsection('Code', [_evalItem, _liveEvalItem])