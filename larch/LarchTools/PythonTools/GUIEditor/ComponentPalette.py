##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.lang import Object

from java.awt import Color

from BritefuryJ.Graphics import SolidBorder, FilledOutlinePainter

from BritefuryJ.Pres.Primitive import Primitive, Label, Row, Column, Paragraph, FlowGrid, Blank
from BritefuryJ.Pres.UI import Section, SectionHeading1, SectionHeading2, SectionHeading3

from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Editor.List import EditableListController, AbstractEditableListDrag

from LarchTools.PythonTools import GUIEditor


class PaletteItemFactory (object):
	def __init__(self, visual, factoryCallable):
		self.visual = visual
		self.factoryCallable = factoryCallable

#
#
#Palette items:
class PaletteComponentIntoListDrag (AbstractEditableListDrag):
	def __init__(self, controller, factory):
		super(PaletteComponentIntoListDrag, self).__init__(controller)
		self.factory = factory

	def getEditableList(self):
		return None

	def getItem(self):
		return self.factory()


class PaletteComponentDrag (Object):
	def __init__(self, factory):
		self.factory = factory

	def getItem(self):
		return self.factory()


#
#
#Control palette:
_paletteItemStyle = StyleSheet.style(Primitive.fontSize(11), Primitive.foreground(Color(0.3, 0.3, 0.3)), Primitive.background(FilledOutlinePainter(Color.white, Color(0.8, 0.8, 0.8))))
_paletteItemBorder = SolidBorder(1.0, 2.0, Color(0.7, 0.7, 0.7), None).highlight(Color(0.6, 0.6, 0.6), Color(0.9, 0.9, 0.9))



_paletteSections = []



def paletteItem(contents, factoryCallable):
	p = _paletteItemStyle(_paletteItemBorder.surround(contents)).alignHExpand().alignVRefYExpand()
	p = p.withDragSource(PaletteComponentDrag, lambda element, aspect: PaletteComponentDrag(factoryCallable))
	p = p.withDragSource(PaletteComponentIntoListDrag, lambda element, aspect: PaletteComponentIntoListDrag(GUIEditor.SequentialComponent.SequentialGUIController.instance, factoryCallable))
	return p



def createPalette():
	return Column(_paletteSections).alignVRefY()



def registerPaletteSubsection(title, items):
	sec = Section( SectionHeading3( title ), FlowGrid( 4, items ) )
	_paletteSections.append( sec )

def registerPaletteSection(title, items):
	sec = Section( SectionHeading2( title ), FlowGrid( 4, items ) )
	_paletteSections.append( sec )



