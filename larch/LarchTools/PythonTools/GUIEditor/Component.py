##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from java.awt import Color

from BritefuryJ.LSpace.Layout import HAlignment, VAlignment

from BritefuryJ.Controls import Button, SwitchButton

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Primitive, Blank, Label, Spacer, Row, Column
from BritefuryJ.Pres.UI import Form, ControlsRow

from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Live import LiveFunction

from LarchCore.Languages.Python2 import Schema as Py

from LarchTools.PythonTools.GUIEditor.DataModel import GUINode, TypedEvalField, TypedField
from LarchTools.PythonTools.GUIEditor.Properties import GUICProp
from LarchTools.PythonTools.GUIEditor.Target import GUITargetInteractor, GUIScrollInteractor
from LarchTools.PythonTools.GUIEditor.ContextMenu import componentContextMenu
from LarchTools.PythonTools.GUIEditor.Padding import AbstractPadding, UniformPadding, NonUniformPadding



componentBorder = SolidBorder(1.0, 2.0, Color(0.8, 0.8, 0.8), None)






def blankCallModel(codeGen):
	blank = codeGen.embeddedValue(Blank)
	return Py.Call(target=blank, args=[])



_noAlignmentStyle = StyleSheet.style(Primitive.foreground(Color(0.4, 0.4, 0.4)), Primitive.fontSize(11))

def _hAlignmentEditor(live):
	@LiveFunction
	def displayLive():
		x = live.getValue()
		if x is None:
			return 0
		else:
			return x.ordinal() + 1

	options = [_noAlignmentStyle(Label('None')), Label('Pack'), Label('Left'), Label('Centre'), Label('Right'), Label('Expand')]

	def _onChoice(control, prevChoice, choice):
		if choice == 0:
			live.setLiteralValue(None)
		else:
			live.setLiteralValue(HAlignment.values()[choice-1])

	return SwitchButton(options, options, SwitchButton.Orientation.HORIZONTAL, displayLive, _onChoice)


def _vAlignmentEditor(live):
	@LiveFunction
	def displayLive():
		x = live.getValue()
		if x is None:
			return 0
		else:
			return x.ordinal() + 1

	options = [_noAlignmentStyle(Label('None')), Label('Ref-Y'), Label('Ref-Y Exp'), Label('Top'), Label('Centre'), Label('Bottom'), Label('Expand')]

	def _onChoice(control, prevChoice, choice):
		if choice == 0:
			live.setLiteralValue(None)
		else:
			live.setLiteralValue(VAlignment.values()[choice-1])

	return SwitchButton(options, options, SwitchButton.Orientation.HORIZONTAL, displayLive, _onChoice)



class GUIComponent (GUINode):
	isRootGUIEditorComponent = False
	componentName = '<abstract-Component>'


	hAlignment = TypedEvalField([HAlignment, type(None)], None)
	vAlignment = TypedEvalField([VAlignment, type(None)], None)
	padding = TypedField([AbstractPadding, type(None)], None)


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
		hAlign = Form.SmallSection('H alignment', None, self.hAlignment.editUI(_hAlignmentEditor))
		vAlign = Form.SmallSection('V alignment', None, self.vAlignment.editUI(_vAlignmentEditor))

		@LiveFunction
		def paddingUI():
			padding = self.padding.value

			if padding is None:
				def onPadUniform(button, event):
					self.padding.value = UniformPadding()

				def onPadNonUniform(button, event):
					self.padding.value = NonUniformPadding()

				return ControlsRow([Button.buttonWithLabel('Uniform', onPadUniform), Button.buttonWithLabel('Non-uniform', onPadNonUniform)]).alignHPack()
			else:
				def onRemove(button, event):
					self.padding.value = None

				def setPadding(padding):
					self.padding.value = padding

				removeButton = Button.buttonWithLabel('Remove padding', onRemove).alignHPack()

				return Column([removeButton.alignHPack(), Spacer(0.0, 5.0), padding.editUI(setPadding)])

		padding = Form.SmallSection('Padding', None, paddingUI)

		return [hAlign, vAlign, padding]

	def __present__(self, fragment, inheritedState):
		hAlign = self.hAlignment.getValueForEditor()
		vAlign = self.vAlignment.getValueForEditor()
		p = self._presentContents(fragment, inheritedState)
		p = componentBorder.surround(p)
		padding = self.padding.value
		if padding is not None:
			p = padding.apply(p)
		if hAlign is not None:
			p = p.alignH(hAlign)
		if vAlign is not None:
			p = p.alignV(vAlign)
		p = p.withContextMenuInteractor(componentContextMenu)
		p = p.withElementInteractor(GUITargetInteractor())
		p = p.withElementInteractor(GUIScrollInteractor())
		p = p.withProperty(GUICProp.instance, self)
		return p

	def __py_evalmodel__(self, codeGen):
		hasHAlign = not self.hAlignment.isConstant()  or  self.hAlignment.constantValue is not None
		hasVAlign = not self.vAlignment.isConstant()  or  self.vAlignment.constantValue is not None
		hAlign = self.hAlignment.__py_evalmodel__(codeGen)
		vAlign = self.vAlignment.__py_evalmodel__(codeGen)
		py_p = self.__component_py_evalmodel__(codeGen)
		padding = self.padding.value
		if padding is not None:
			py_p = padding.__apply_py_evalmodel__(codeGen, py_p)
		if hasHAlign  or  hasVAlign:
			py_pres = codeGen.embeddedValue(Pres)
			py_pres_coerce = Py.AttributeRef(target=py_pres, name='coerce')
			py_p = Py.Call(target=py_pres_coerce, args=[py_p])
			if hasHAlign:
				py_p = Py.Call(target=Py.AttributeRef(target=py_p, name='alignH'), args=[hAlign])
			if hasVAlign:
				py_p = Py.Call(target=Py.AttributeRef(target=py_p, name='alignV'), args=[vAlign])
		return py_p


	def __component_py_evalmodel__(self, codeGen):
		raise NotImplementedError, 'abstract'







