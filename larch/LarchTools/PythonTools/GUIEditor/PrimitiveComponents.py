##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color

from BritefuryJ.Graphics import FillPainter

from BritefuryJ.LSpace import LSSpaceBin

from BritefuryJ.Pres.Primitive import Primitive, Label, StaticText, Text, Arrow, Spacer, Bin, SpaceBin, Row, Column, Paragraph, FlowGrid, RGrid, GridRow
from BritefuryJ.Pres.UI import Form

from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Controls import TextEntry, RealSpinEntry, SwitchButton

from BritefuryJ.Live import LiveValue, LiveFunction

from LarchCore.Languages.Python2 import Schema as Py

from LarchTools.PythonTools.GUIEditor.ComponentPalette import paletteItem, registerPaletteSubsection

from LarchTools.PythonTools.GUIEditor.DataModel import TypedEvalField, TypedField
from LarchTools.PythonTools.GUIEditor.LeafComponent import GUILeafComponent
from LarchTools.PythonTools.GUIEditor.BranchComponent import GUIUnaryBranchComponent, GUISequenceComponent
from LarchTools.PythonTools.GUIEditor.FieldEditor import enumSwitchButtonEditor, enumSwitchButtonEditorWithLabels
from LarchTools.PythonTools.GUIEditor.Borders import AbstractGUIBorder, SolidGUIBorder



class GUILabel (GUILeafComponent):
	componentName = 'Label'

	text = TypedEvalField(str, 'Label')

	def _presentLeafContents(self, fragment, inheritedState):
		return Label(self.text.getValueForEditor())

	def _editUIFormSections(self):
		text = Form.SmallSection('Text', None, self.text.editUI(lambda live: TextEntry.textEntryCommitOnChange(live)))
		superSections = super(GUILabel, self)._editUIFormSections()
		return [text] + superSections

	def __component_py_evalmodel__(self, codeGen):
		label = codeGen.embeddedValue(Label)
		return Py.Call( target=label, args=[ self.text.__py_evalmodel__(codeGen) ] )

_labelItem = paletteItem(Label('Label'), lambda: GUILabel(text='Label'))




class GUIStaticText (GUILeafComponent):
	componentName = 'Static text'

	text = TypedEvalField(str, 'Label')

	def _presentLeafContents(self, fragment, inheritedState):
		return StaticText(self.text.getValueForEditor())

	def _editUIFormSections(self):
		text = Form.SmallSection('Text', None, self.text.editUI(lambda live: TextEntry.textEntryCommitOnChange(live)))
		superSections = super(GUIStaticText, self)._editUIFormSections()
		return [text] + superSections

	def __component_py_evalmodel__(self, codeGen):
		staticText = codeGen.embeddedValue(StaticText)
		return Py.Call( target=staticText, args=[ self.text.__py_evalmodel__(codeGen) ] )

_staticTextItem = paletteItem(Label('Static text'), lambda: GUIStaticText(text='Text'))




class GUIText (GUILeafComponent):
	componentName = 'Text'

	text = TypedEvalField(str, 'Label')

	def _presentLeafContents(self, fragment, inheritedState):
		return Text(self.text.getValueForEditor())

	def _editUIFormSections(self):
		text = Form.SmallSection('Text', None, self.text.editUI(lambda live: TextEntry.textEntryCommitOnChange(live)))
		superSections = super(GUIText, self)._editUIFormSections()
		return [text] + superSections

	def __component_py_evalmodel__(self, codeGen):
		textPresClass = codeGen.embeddedValue(Text)
		return Py.Call( target=textPresClass, args=[ self.text.__py_evalmodel__(codeGen) ] )

_textItem = paletteItem(Label('Text'), lambda: GUIText('Text'))




class GUISpacer (GUILeafComponent):
	componentName = 'Spacer'

	width = TypedEvalField(float, 10.0)
	height = TypedEvalField(float, 10.0)

	def _presentLeafContents(self, fragment, inheritedState):
		return Spacer(self.width.getValueForEditor(), self.height.getValueForEditor())

	def _editUIFormSections(self):
		width = Form.SmallSection('Width', None, self.width.editUI(lambda live: RealSpinEntry(live, 0.0, 1048576.0, 1.0, 10.0)))
		height = Form.SmallSection('Height', None, self.height.editUI(lambda live: RealSpinEntry(live, 0.0, 1048576.0, 1.0, 10.0)))
		superSections = super(GUISpacer, self)._editUIFormSections()
		return [width, height] + superSections

	def __component_py_evalmodel__(self, codeGen):
		spacer = codeGen.embeddedValue(Spacer)
		return Py.Call( target=spacer, args=[ self.width.__py_evalmodel__(codeGen),
						      self.height.__py_evalmodel__(codeGen) ] )

_spacerItem = paletteItem(Label('Spacer'), lambda: GUISpacer(width=10.0, height=10.0))




class GUIArrow (GUILeafComponent):
	componentName = 'Arrow'

	size = TypedEvalField(float, 12.0)
	direction = TypedEvalField(Arrow.Direction, Arrow.Direction.RIGHT)

	def _presentLeafContents(self, fragment, inheritedState):
		return Arrow(self.direction.getValueForEditor(), self.size.getValueForEditor())

	_offStyle = StyleSheet.style(Primitive.shapePainter(FillPainter(Color(0.4, 0.4, 0.4))))
	_onStyle = StyleSheet.style(Primitive.shapePainter(FillPainter(Color(0.2, 0.2, 0.2))))

	@staticmethod
	def _arrowDirectionEditor(live):
		offOptions = [GUIArrow._offStyle(Arrow(dir, 14.0).alignVCentre())   for dir in Arrow.Direction.values()]
		onOptions = [GUIArrow._onStyle(Arrow(dir, 14.0).alignVCentre())   for dir in Arrow.Direction.values()]

		return enumSwitchButtonEditor(live, Arrow.Direction, offOptions, onOptions)


	def _editUIFormSections(self):
		direction = Form.SmallSection('Direction', None, self.direction.editUI(self._arrowDirectionEditor))
		size = Form.SmallSection('Size', None, self.size.editUI(lambda live: RealSpinEntry(live, 0.0, 1048576.0, 1.0, 10.0)))
		superSections = super(GUIArrow, self)._editUIFormSections()
		return [direction, size] + superSections

	def __component_py_evalmodel__(self, codeGen):
		arrow = codeGen.embeddedValue(Arrow)
		direction = self.direction.__py_evalmodel__(codeGen)
		size = self.size.__py_evalmodel__(codeGen)
		return Py.Call(target=arrow, args=[direction, size])

_arrowItem = paletteItem(Label('Arrow'), lambda: GUIArrow(direction=Arrow.Direction.DOWN, size=12.0))




class GUIBin (GUIUnaryBranchComponent):
	componentName = 'Bin'

	def _presentBranchContents(self, child, fragment, inheritedState):
		return Bin(child)

	def _presentEditableBranchContents(self, child,fragment, inheritedState):
		return Bin(child)

	def __component_py_evalmodel__(self, codeGen):
		py_bin = codeGen.embeddedValue(Bin)
		return Py.Call( target=py_bin, args=[ self.child.node.__py_evalmodel__(codeGen) ] )

_binItem = paletteItem(Label('Bin'), lambda: GUIBin())




def _sizeConstraintEditor(live):
	return enumSwitchButtonEditorWithLabels(live, LSSpaceBin.SizeConstraint, ['Larger', 'Smaller', 'Fixed', 'None'])


class GUISpaceBin (GUIUnaryBranchComponent):
	componentName = 'Space bin'

	width = TypedEvalField(float, 100.0)
	height = TypedEvalField(float, 100.0)
	sizeConstraintX = TypedEvalField(LSSpaceBin.SizeConstraint, LSSpaceBin.SizeConstraint.LARGER)
	sizeConstraintY = TypedEvalField(LSSpaceBin.SizeConstraint, LSSpaceBin.SizeConstraint.LARGER)

	def _presentBranchContents(self, child, fragment, inheritedState):
		return SpaceBin(self.width.getValueForEditor(), self.height.getValueForEditor(), self.sizeConstraintX.getValueForEditor(), self.sizeConstraintY.getValueForEditor(), child)

	def _presentEditableBranchContents(self, child,fragment, inheritedState):
		return SpaceBin(self.width.getValueForEditor(), self.height.getValueForEditor(), self.sizeConstraintX.getValueForEditor(), self.sizeConstraintY.getValueForEditor(), child)

	def _editUIFormSections(self):
		width = Form.SmallSection('Width', None, self.width.editUI(lambda live: RealSpinEntry(live, 0.0, 1048576.0, 1.0, 10.0)))
		height = Form.SmallSection('Height', None, self.height.editUI(lambda live: RealSpinEntry(live, 0.0, 1048576.0, 1.0, 10.0)))
		sizeConstraintX = Form.SmallSection('Constraint-X', None, self.sizeConstraintX.editUI(_sizeConstraintEditor))
		sizeConstraintY = Form.SmallSection('Constraint-Y', None, self.sizeConstraintY.editUI(_sizeConstraintEditor))
		superSections = super(GUISpaceBin, self)._editUIFormSections()
		return [width, height, sizeConstraintX, sizeConstraintY] + superSections

	def __component_py_evalmodel__(self, codeGen):
		py_spaceBin = codeGen.embeddedValue(SpaceBin)
		width = self.width.__py_evalmodel__(codeGen)
		height = self.height.__py_evalmodel__(codeGen)
		sizeConstraintX = self.sizeConstraintX.__py_evalmodel__(codeGen)
		sizeConstraintY = self.sizeConstraintY.__py_evalmodel__(codeGen)
		return Py.Call( target=py_spaceBin, args=[ width, height, sizeConstraintX, sizeConstraintY, self.child.node.__py_evalmodel__(codeGen) ] )

_spaceBinItem = paletteItem(Label('SpaceBin'), lambda: GUISpaceBin())




class GUIBorder (GUIUnaryBranchComponent):
	componentName = 'Border'

	border = TypedField(AbstractGUIBorder).defaultValueFactory(lambda: SolidGUIBorder())

	def _presentBranchContents(self, child, fragment, inheritedState):
		return self.border.value.makeBorder().surround(child)

	def _presentEditableBranchContents(self, child,fragment, inheritedState):
		return self.border.value.makeBorder().surround(child)

	def _editUIFormSections(self):
		borderSections = self.border.value.formSections()
		superSections = super(GUIBorder, self)._editUIFormSections()
		return borderSections + superSections

	def __component_py_evalmodel__(self, codeGen):
		py_child = self.child.node.__py_evalmodel__(codeGen)
		return self.border.value.__apply_py_evalmodel__(codeGen, py_child)

_borderItem = paletteItem(Label('Border'), lambda: GUIBorder())




class GUIRow (GUISequenceComponent):
	componentName = 'Row'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return Row(contents)

	def __component_py_evalmodel__(self, codeGen):
		row = codeGen.embeddedValue(Row)
		return Py.Call( target=row, args=[ self._py_evalmodel_forChildren( codeGen ) ] )

_rowItem = paletteItem(Label('Row'), lambda: GUIRow())




class GUIColumn (GUISequenceComponent):
	componentName = 'Column'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return Column(contents)

	def __component_py_evalmodel__(self, codeGen):
		column = codeGen.embeddedValue(Column)
		return Py.Call( target=column, args=[ self._py_evalmodel_forChildren( codeGen ) ] )

_columnItem = paletteItem(Label('Column'), lambda: GUIColumn())




class GUIParagraph (GUISequenceComponent):
	componentName = 'Paragraph'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return Paragraph(contents)

	def __component_py_evalmodel__(self, codeGen):
		paragraph = codeGen.embeddedValue(Paragraph)
		return Py.Call( target=paragraph, args=[ self._py_evalmodel_forChildren( codeGen ) ] )

_paraItem = paletteItem(Label('Paragraph'), lambda: GUIParagraph())




class GUIFlowGrid (GUISequenceComponent):
	componentName = 'Flow grid'

	#targetNumColumns = IntEvalField()

	def __init__(self, targetNumColumns=None):
		super(GUIFlowGrid, self).__init__()
		self._targetNumColumns = LiveValue(targetNumColumns)

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return FlowGrid(contents)

	def __component_py_evalmodel__(self, codeGen):
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

	def __component_py_evalmodel__(self, codeGen):
		rGrid = codeGen.embeddedValue(RGrid)
		return Py.Call( target=rGrid, args=[ self._py_evalmodel_forChildren( codeGen ) ] )

_gridItem = paletteItem(Label('Grid'), lambda: GUIRGrid())





class GUIGridRow (GUISequenceComponent):
	componentName = 'Grid row'

	def _presentSequenceContents(self, contents, fragment, inheritedState):
		return GridRow(contents)

	def __component_py_evalmodel__(self, codeGen):
		gridRow = codeGen.embeddedValue(GridRow)
		return Py.Call( target=gridRow, args=[ self._py_evalmodel_forChildren( codeGen ) ] )

_gridRowItem = paletteItem(Label('Grid row'), lambda: GUIGridRow())



registerPaletteSubsection('Basic', [_labelItem, _staticTextItem, _textItem, _spacerItem, _arrowItem])
registerPaletteSubsection('Containers', [_binItem, _spaceBinItem, _borderItem, _rowItem, _columnItem, _paraItem, _flowGridItem, _gridItem, _gridRowItem])