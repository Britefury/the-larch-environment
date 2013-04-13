##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from BritefuryJ.Pres.Primitive import Label, Row, Column, Paragraph, FlowGrid, RGrid, GridRow
from BritefuryJ.Pres.UI import Form

from BritefuryJ.Controls import TextEntry

from BritefuryJ.Live import LiveValue

from LarchTools.PythonTools.GUIEditor.ComponentPalette import paletteItem, registerPaletteSubsection

from LarchTools.PythonTools.GUIEditor.LeafComponent import GUILeafComponent
from LarchTools.PythonTools.GUIEditor.SequentialComponent import GUISequenceComponent
from LarchTools.PythonTools.GUIEditor.BranchComponent import GUIBranchComponent



class GUILabel (GUILeafComponent):
	componentName = 'Label'

	def __init__(self, text):
		self._text = LiveValue(text)

	def _presentItemContents(self, fragment, inheritedState):
		return Label(self._text.getValue())

	def _editUI(self):
		text = Form.Section('Text', None, TextEntry.textEntryCommitOnChange(self._text))
		return Form(None, [text])

_labelItem = paletteItem(Label('Label'), lambda: GUILabel('Label'))




class GUIRow (GUISequenceComponent):
	componentName = 'Row'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return Row(contents)

_rowItem = paletteItem(Label('Row'), lambda: GUIRow())




class GUIColumn (GUISequenceComponent):
	componentName = 'Column'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return Column(contents)

_columnItem = paletteItem(Label('Column'), lambda: GUIColumn())




class GUIParagraph (GUISequenceComponent):
	componentName = 'Paragraph'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return Paragraph(contents)

_paraItem = paletteItem(Label('Paragraph'), lambda: GUIParagraph())




class GUIFlowGrid (GUISequenceComponent):
	componentName = 'Flow grid'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return FlowGrid(contents)

_flowGridItem = paletteItem(Label('Flow grid'), lambda: GUIFlowGrid())



class GUIRGrid (GUISequenceComponent):
	componentName = 'Grid'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return RGrid(contents)

_gridItem = paletteItem(Label('Grid'), lambda: GUIRGrid())





class GUIGridRow (GUISequenceComponent):
	componentName = 'Grid row'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return GridRow(contents)

_gridRowItem = paletteItem(Label('Grid row'), lambda: GridRow())



registerPaletteSubsection('Basic', [_labelItem])
registerPaletteSubsection('Containers', [_rowItem, _columnItem, _paraItem, _flowGridItem, _gridItem, _gridRowItem])