##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color, BasicStroke

from BritefuryJ.LSpace import ElementHighlighter
from BritefuryJ.LSpace.Interactor import HoverElementInteractor

from BritefuryJ.Graphics import SolidBorder, FilledOutlinePainter

from BritefuryJ.Pres.Primitive import Primitive, Label, Row, Column, Paragraph, FlowGrid, Blank
from BritefuryJ.Pres.UI import Section, SectionHeading1, SectionHeading2, SectionHeading3, ControlsRow

from BritefuryJ.Controls import Button

from LarchTools.PythonTools.GUIEditor.Properties import GUICProp, GUIEdProp
from LarchTools.PythonTools.GUIEditor.SidePanel import showSidePanel, hideSidePanel


#
#
#Context menu:
class ComponentAssociationPropertyKey (object):
	pass

ComponentAssociationPropertyKey.instance = ComponentAssociationPropertyKey()

componentHighlighter = ElementHighlighter(FilledOutlinePainter(Color(0.0, 0.8, 0.0, 0.125), Color(0.0, 0.8, 0.0, 0.25), BasicStroke(1.0)))

class ComponentHighlightInteractor (HoverElementInteractor):
	def pointerEnter(self, element, event):
		p = element.getProperty(ComponentAssociationPropertyKey.instance)
		componentHighlighter.highlight(p.value)

	def pointerLeave(self, element, event):
		p = element.getProperty(ComponentAssociationPropertyKey.instance)
		componentHighlighter.unhighlight(p.value)

ComponentHighlightInteractor.instance = ComponentHighlightInteractor()

componentUnderPointerBorder = SolidBorder(1.0, 2.0, Color(0.8, 0.8, 0.8), None, Color(0.6, 0.6, 0.6), Color(0.9, 0.9, 0.9))


def _presentComponentsUnderPointer(elementUnderPointer, rootElement):
	propValues = elementUnderPointer.gatherPropertyInAncestryTo(GUICProp.instance, rootElement)

	# Components under pointer
	components = []
	for p in propValues:
		c = componentUnderPointerBorder.surround(Label(p.value.componentName).pad(4.0, 2.0)).alignHExpand()
		c = c.withProperty(ComponentAssociationPropertyKey.instance, p.element)
		c = c.withElementInteractor(ComponentHighlightInteractor.instance)
		components.append(c)
	return FlowGrid(3, components)



def componentContextMenu(element, menu):
	rootProp = element.findPropertyInAncestors(GUIEdProp.instance)
	rootElement = rootProp.element

	# Components under pointer
	menu.add(Section(SectionHeading3('Components under pointer'), _presentComponentsUnderPointer(element, rootElement)))

	# Side panel
	def _onShow(button, event):
		showSidePanel(button.element)

	def _onHide(button, event):
		hideSidePanel(button.element)

	panelButtons = ControlsRow([Button.buttonWithLabel('Show', _onShow), Button.buttonWithLabel('Hide', _onHide)])
	panelSection = Section(SectionHeading3('Side panel'), panelButtons)
	menu.add(panelSection)


	return True


