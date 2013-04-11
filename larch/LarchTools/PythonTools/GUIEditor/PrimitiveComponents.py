##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from BritefuryJ.Pres.Primitive import Label, Row, Column, Paragraph, FlowGrid, RGrid, GridRow

from BritefuryJ.Live import LiveValue

from LarchTools.PythonTools.GUIEditor.ComponentPalette import PaletteItemFactory, registerPaletteItemFactory

from LarchTools.PythonTools.GUIEditor.LeafComponent import GUILeafComponent
from LarchTools.PythonTools.GUIEditor.SequentialComponent import GUISequenceComponent
from LarchTools.PythonTools.GUIEditor.BranchComponent import GUIBranchComponent



class GUILabel (GUILeafComponent):
	componentName = 'Label'

	def __init__(self, text):
		self._text = LiveValue(text)

	def _presentItemContents(self, fragment, inheritedState):
		return Label(self._text.getValue())

registerPaletteItemFactory(PaletteItemFactory(Label('Label'), lambda: GUILabel('Label')))




class GUIRow (GUISequenceComponent):
	componentName = 'Row'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return Row(contents)

registerPaletteItemFactory(PaletteItemFactory(Label('Row'), lambda: GUIRow()))




class GUIColumn (GUISequenceComponent):
	componentName = 'Column'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return Column(contents)

registerPaletteItemFactory(PaletteItemFactory(Label('Column'), lambda: GUIColumn()))




class GUIParagraph (GUISequenceComponent):
	componentName = 'Paragraph'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return Paragraph(contents)

registerPaletteItemFactory(PaletteItemFactory(Label('Paragraph'), lambda: GUIParagraph()))




class GUIFlowGrid (GUISequenceComponent):
	componentName = 'Flow grid'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return FlowGrid(contents)

registerPaletteItemFactory(PaletteItemFactory(Label('Flow grid'), lambda: FlowGrid()))



class GUIRGrid (GUISequenceComponent):
	componentName = 'Grid'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return RGrid(contents)

registerPaletteItemFactory(PaletteItemFactory(Label('Grid'), lambda: RGrid()))





class GUIGridRow (GUISequenceComponent):
	componentName = 'Grid row'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return GridRow(contents)

registerPaletteItemFactory(PaletteItemFactory(Label('Grid row'), lambda: GridRow()))
