##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from BritefuryJ.Pres.Primitive import Label, StaticText, Text, Spacer, Row, Column, Paragraph, FlowGrid, RGrid, GridRow
from BritefuryJ.Pres.UI import Form

from BritefuryJ.Controls import TextEntry, RealSpinEntry

from BritefuryJ.Live import LiveValue

from LarchCore.Languages.Python2 import Schema as Py

from LarchTools.PythonTools.GUIEditor.ComponentPalette import paletteItem, registerPaletteSubsection

from LarchTools.PythonTools.GUIEditor.LeafComponent import GUILeafComponent
from LarchTools.PythonTools.GUIEditor.SequentialComponent import GUISequenceComponent
from LarchTools.PythonTools.GUIEditor.BranchComponent import GUIBranchComponent



class GUILabel (GUILeafComponent):
	componentName = 'Label'

	def __init__(self, text):
		super(GUILabel, self).__init__()
		self._text = LiveValue(text)

	def _presentItemContents(self, fragment, inheritedState):
		return Label(self._text.getValue())

	def _editUI(self):
		text = Form.Section('Text', None, TextEntry.textEntryCommitOnChange(self._text))
		return Form(None, [text])

	def __py_evalmodel__(self, codeGen):
		label = codeGen.embeddedValue(Label)
		return Py.Call( target=label, args=[ Py.strToStrLiteral( self._text.getValue() ) ] )

_labelItem = paletteItem(Label('Label'), lambda: GUILabel('Label'))




class GUISpacer (GUILeafComponent):
	componentName = 'Spacer'

	def __init__(self, width, height):
		super(GUISpacer, self).__init__()
		self._width = LiveValue(width)
		self._height = LiveValue(height)

	def _presentItemContents(self, fragment, inheritedState):
		return Spacer(self._width.getValue(), self._height.getValue())

	def _editUI(self):
		width = Form.Section('Width', None, RealSpinEntry(self._width, 0.0, 1048576.0, 1.0, 10.0))
		height = Form.Section('Height', None, RealSpinEntry(self._height, 0.0, 1048576.0, 1.0, 10.0))
		return Form(None, [width, height])

	def __py_evalmodel__(self, codeGen):
		spacer = codeGen.embeddedValue(Spacer)
		return Py.Call( target=spacer, args=[ Py.FloatLiteral( value=repr(self._width.getValue()) ),
					 Py.FloatLiteral( value=repr(self._height.getValue() ) ) ] )

_spacerItem = paletteItem(Label('Spacer'), lambda: GUISpacer(10.0, 10.0))




class GUIRow (GUISequenceComponent):
	componentName = 'Row'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return Row(contents)

	def __py_evalmodel__(self, codeGen):
		row = codeGen.embeddedValue(Row)
		return Py.Call( target=row, args=[ self._py_evalmodel_forChildren( codeGen ) ] )

_rowItem = paletteItem(Label('Row'), lambda: GUIRow())




class GUIColumn (GUISequenceComponent):
	componentName = 'Column'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return Column(contents)

	def __py_evalmodel__(self, codeGen):
		column = codeGen.embeddedValue(Column)
		return Py.Call( target=column, args=[ self._py_evalmodel_forChildren( codeGen ) ] )

_columnItem = paletteItem(Label('Column'), lambda: GUIColumn())




class GUIParagraph (GUISequenceComponent):
	componentName = 'Paragraph'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return Paragraph(contents)

	def __py_evalmodel__(self, codeGen):
		paragraph = codeGen.embeddedValue(Paragraph)
		return Py.Call( target=paragraph, args=[ self._py_evalmodel_forChildren( codeGen ) ] )

_paraItem = paletteItem(Label('Paragraph'), lambda: GUIParagraph())




class GUIFlowGrid (GUISequenceComponent):
	componentName = 'Flow grid'

	def __init__(self, targetNumColumns=None):
		super(GUIFlowGrid, self).__init__()
		self._targetNumColumns = LiveValue(targetNumColumns)

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return FlowGrid(contents)

	def __py_evalmodel__(self, codeGen):
		flowGrid = codeGen.embeddedValue(FlowGrid)
		targetNumColumns = self._targetNumColumns.getValue()
		args = []
		if targetNumColumns is not None:
			args.append( Py.IntLiteral( value=repr( targetNumColumns ) ) )
		args.append( self._py_evalmodel_forChildren( codeGen ) )
		return Py.Call( target=flowGrid, args=args )

_flowGridItem = paletteItem(Label('Flow grid'), lambda: GUIFlowGrid())



class GUIRGrid (GUISequenceComponent):
	componentName = 'Grid'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return RGrid(contents)

	def __py_evalmodel__(self, codeGen):
		rGrid = codeGen.embeddedValue(RGrid)
		return Py.Call( target=rGrid, args=[ self._py_evalmodel_forChildren( codeGen ) ] )

_gridItem = paletteItem(Label('Grid'), lambda: GUIRGrid())





class GUIGridRow (GUISequenceComponent):
	componentName = 'Grid row'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return GridRow(contents)

	def __py_evalmodel__(self, codeGen):
		gridRow = codeGen.embeddedValue(GridRow)
		return Py.Call( target=gridRow, args=[ self._py_evalmodel_forChildren( codeGen ) ] )

_gridRowItem = paletteItem(Label('Grid row'), lambda: GridRow())



registerPaletteSubsection('Basic', [_labelItem])
registerPaletteSubsection('Containers', [_rowItem, _columnItem, _paraItem, _flowGridItem, _gridItem, _gridRowItem])