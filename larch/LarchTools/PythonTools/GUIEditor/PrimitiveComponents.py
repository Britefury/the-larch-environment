##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color

from BritefuryJ.Graphics import FillPainter

from BritefuryJ.Pres.Primitive import Primitive, Label, StaticText, Text, Arrow, Spacer, Row, Column, Paragraph, FlowGrid, RGrid, GridRow
from BritefuryJ.Pres.UI import Form

from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Controls import TextEntry, RealSpinEntry, SwitchButton

from BritefuryJ.Live import LiveValue

from LarchCore.Languages.Python2 import Schema as Py

from LarchTools.PythonTools.GUIEditor.ComponentPalette import paletteItem, registerPaletteSubsection

from LarchTools.PythonTools.GUIEditor.LeafComponent import GUILeafComponent
from LarchTools.PythonTools.GUIEditor.BranchComponent import GUIBranchComponent, GUISequenceComponent



class GUILabel (GUILeafComponent):
	componentName = 'Label'

	def __init__(self, text):
		super(GUILabel, self).__init__()
		self._text = LiveValue(text)

	def _presentLeafContents(self, fragment, inheritedState):
		return Label(self._text.getValue())

	def _editUI(self):
		text = Form.SmallSection('Text', None, TextEntry.textEntryCommitOnChange(self._text))
		return Form(None, [text])

	def __py_evalmodel__(self, codeGen):
		label = codeGen.embeddedValue(Label)
		return Py.Call( target=label, args=[ Py.strToStrLiteral( self._text.getValue() ) ] )

_labelItem = paletteItem(Label('Label'), lambda: GUILabel('Label'))




class GUIStaticText (GUILeafComponent):
	componentName = 'Static text'

	def __init__(self, text):
		super(GUIStaticText, self).__init__()
		self._text = LiveValue(text)

	def _presentLeafContents(self, fragment, inheritedState):
		return StaticText(self._text.getValue())

	def _editUI(self):
		text = Form.SmallSection('Text', None, TextEntry.textEntryCommitOnChange(self._text))
		return Form(None, [text])

	def __py_evalmodel__(self, codeGen):
		staticText = codeGen.embeddedValue(StaticText)
		return Py.Call( target=staticText, args=[ Py.strToStrLiteral( self._text.getValue() ) ] )

_staticTextItem = paletteItem(Label('Static text'), lambda: GUIStaticText('Text'))




class GUIText (GUILeafComponent):
	componentName = 'Text'

	def __init__(self, text):
		super(GUIText, self).__init__()
		self._text = LiveValue(text)

	def _presentLeafContents(self, fragment, inheritedState):
		return Text(self._text.getValue())

	def _editUI(self):
		text = Form.SmallSection('Text', None, TextEntry.textEntryCommitOnChange(self._text))
		return Form(None, [text])

	def __py_evalmodel__(self, codeGen):
		textPresClass = codeGen.embeddedValue(Text)
		return Py.Call( target=textPresClass, args=[ Py.strToStrLiteral( self._text.getValue() ) ] )

_textItem = paletteItem(Label('Text'), lambda: GUIText('Text'))




class GUISpacer (GUILeafComponent):
	componentName = 'Spacer'

	def __init__(self, width, height):
		super(GUISpacer, self).__init__()
		self._width = LiveValue(width)
		self._height = LiveValue(height)

	def _presentLeafContents(self, fragment, inheritedState):
		return Spacer(self._width.getValue(), self._height.getValue())

	def _editUI(self):
		width = Form.SmallSection('Width', None, RealSpinEntry(self._width, 0.0, 1048576.0, 1.0, 10.0))
		height = Form.SmallSection('Height', None, RealSpinEntry(self._height, 0.0, 1048576.0, 1.0, 10.0))
		return Form(None, [width, height])

	def __py_evalmodel__(self, codeGen):
		spacer = codeGen.embeddedValue(Spacer)
		return Py.Call( target=spacer, args=[ Py.FloatLiteral( value=repr(self._width.getValue()) ),
					 Py.FloatLiteral( value=repr(self._height.getValue() ) ) ] )

_spacerItem = paletteItem(Label('Spacer'), lambda: GUISpacer(10.0, 10.0))




class GUIArrow (GUILeafComponent):
	componentName = 'Arrow'

	_arrowDirections = [Arrow.Direction.LEFT, Arrow.Direction.RIGHT, Arrow.Direction.UP, Arrow.Direction.DOWN]
	_directionToIndex = {d:i   for i, d in enumerate(_arrowDirections)}

	def __init__(self, direction, size):
		super(GUIArrow, self).__init__()
		self._directionIndex = LiveValue(self._directionToIndex[direction])
		self._size = LiveValue(size)

	def _presentLeafContents(self, fragment, inheritedState):
		direction = self._arrowDirections[self._directionIndex.getValue()]
		return Arrow(direction, self._size.getValue())

	_offStyle = StyleSheet.style(Primitive.shapePainter(FillPainter(Color(0.4, 0.4, 0.4))))
	_onStyle = StyleSheet.style(Primitive.shapePainter(FillPainter(Color(0.2, 0.2, 0.2))))

	def _editUI(self):
		offOptions = [self._offStyle(Arrow(dir, 14.0).alignVCentre())   for dir in self._arrowDirections]
		onOptions = [self._onStyle(Arrow(dir, 14.0).alignVCentre())   for dir in self._arrowDirections]
		direction = Form.SmallSection('Direction', None, SwitchButton(offOptions, onOptions, SwitchButton.Orientation.HORIZONTAL, self._directionIndex))
		size = Form.SmallSection('Size', None, RealSpinEntry(self._size, 0.0, 1048576.0, 1.0, 10.0))
		return Form(None, [direction, size])

	def __py_evalmodel__(self, codeGen):
		arrow = codeGen.embeddedValue(Arrow)
		direction = codeGen.embeddedValue(self._arrowDirections[self._directionIndex.getValue()])
		size = Py.FloatLiteral(value=repr(self._size.getValue()))
		return Py.Call(target=arrow, args=[direction, size])

_arrowItem = paletteItem(Label('Arrow'), lambda: GUIArrow(Arrow.Direction.DOWN, 12.0))




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

_gridRowItem = paletteItem(Label('Grid row'), lambda: GUIGridRow())



registerPaletteSubsection('Basic', [_labelItem, _staticTextItem, _textItem, _spacerItem, _arrowItem])
registerPaletteSubsection('Containers', [_rowItem, _columnItem, _paraItem, _flowGridItem, _gridItem, _gridRowItem])