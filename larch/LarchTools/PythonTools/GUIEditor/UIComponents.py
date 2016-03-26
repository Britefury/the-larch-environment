##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from java.awt import Color

from BritefuryJ.Graphics import FillPainter

from BritefuryJ.Pres.Primitive import Primitive, Label, StaticText, Text, Arrow, Spacer, Row, Column, Paragraph, FlowGrid, RGrid, GridRow
from BritefuryJ.Pres.UI import Form, UITitle, SectionHeading1, SectionHeading2, SectionHeading3, NotesText, UINormalText

from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Controls import TextEntry, RealSpinEntry, SwitchButton

from BritefuryJ.Live import LiveValue

from LarchCore.Languages.Python2 import Schema as Py

from LarchTools.PythonTools.GUIEditor.ComponentPalette import paletteItem, registerPaletteSubsection

from LarchTools.PythonTools.GUIEditor.DataModel import TypedEvalField
from LarchTools.PythonTools.GUIEditor.LeafComponent import GUILeafComponent
from LarchTools.PythonTools.GUIEditor.BranchComponent import GUIBranchComponent, GUISequenceComponent



class _GUIUIText (GUILeafComponent):
	componentName = None
	__pres_type__ = None

	text = TypedEvalField(str, 'Text')

	def _presentLeafContents(self, fragment, inheritedState):
		assert self.__pres_type__ is not None, 'abstract'
		return self.__pres_type__(self.text.getValueForEditor())

	def _editUIFormSections(self):
		text = Form.SmallSection('Text', None, self.text.editUI(lambda live: TextEntry.textEntryCommitOnChange(live)))
		superSections = super(_GUIUIText, self)._editUIFormSections()
		return [text] + superSections

	def __component_py_evalmodel__(self, codeGen):
		assert self.__pres_type__ is not None, 'abstract'
		py_pres_type = codeGen.embeddedValue(self.__pres_type__)
		return Py.Call( target=py_pres_type, args=[ self.text.__py_evalmodel__(codeGen) ] )




class GUIUITitle (_GUIUIText):
	componentName = 'UI title'
	__pres_type__ = UITitle

_uiTitleItem = paletteItem(Label('UI title'), lambda: GUIUITitle(text='Title'))



class GUISectionHeading1 (_GUIUIText):
	componentName = 'UI H1'
	__pres_type__ = SectionHeading1

_uiSecH1Item = paletteItem(Label('Sec H1'), lambda: GUISectionHeading1(text='Heading'))



class GUISectionHeading2 (_GUIUIText):
	componentName = 'UI H2'
	__pres_type__ = SectionHeading2

_uiSecH2Item = paletteItem(Label('Sec H2'), lambda: GUISectionHeading2(text='Heading'))



class GUISectionHeading3 (_GUIUIText):
	componentName = 'UI H3'
	__pres_type__ = SectionHeading3

_uiSecH3Item = paletteItem(Label('Sec H3'), lambda: GUISectionHeading3(text='Heading'))



class GUINotesText (_GUIUIText):
	componentName = 'UI notes'
	__pres_type__ = NotesText

_uiNotesTextItem = paletteItem(Label('Notes txt'), lambda: GUINotesText(text='Notes'))



class GUIUINormalText (_GUIUIText):
	componentName = 'UI text'
	__pres_type__ = UINormalText

_uiNormalTextItem = paletteItem(Label('Nrm text'), lambda: GUIUINormalText(text='Text'))



registerPaletteSubsection('UI', [_uiTitleItem, _uiSecH1Item, _uiSecH2Item, _uiSecH3Item, _uiNotesTextItem, _uiNormalTextItem])
