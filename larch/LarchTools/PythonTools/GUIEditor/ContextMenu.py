##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color, BasicStroke

from BritefuryJ.LSpace import ElementHighlighter
from BritefuryJ.LSpace.Interactor import HoverElementInteractor, ClickElementInteractor

from BritefuryJ.Graphics import SolidBorder, FilledOutlinePainter

from BritefuryJ.Pres.Primitive import Label, FlowGrid
from BritefuryJ.Pres.UI import Section, SectionHeading3, ControlsRow

from BritefuryJ.Controls import Button

from LarchTools.PythonTools.GUIEditor.Properties import GUICProp, GUIEdProp
from LarchTools.PythonTools.GUIEditor.SidePanel import showSidePanel, hideSidePanel
from LarchTools.PythonTools.GUIEditor.Target import GUIEditorTarget, GUITargetListener
from LarchTools.PythonTools.GUIEditor.CurrentComponentEditor import currentComponentEditor


componentHighlighter = ElementHighlighter(FilledOutlinePainter(Color(0.0, 0.8, 0.0, 0.125), Color(0.0, 0.8, 0.0, 0.25), BasicStroke(1.0)))

class ComponentHighlightInteractor (HoverElementInteractor, ClickElementInteractor):
	def __init__(self, component, componentElement):
		self.__component = component
		self.__componentElement = componentElement

	def pointerEnter(self, element, event):
		componentHighlighter.highlight(self.__componentElement)

	def pointerLeave(self, element, event):
		componentHighlighter.unhighlight(self.__componentElement)


	def testClickEvent(self, element, event):
		return event.button == 1

	def buttonClicked(self, element, event):
		rootElement = self.__componentElement.rootElement
		rootElement.setTarget(GUIEditorTarget(self.__component, self.__componentElement))



componentUnderPointerBorder = SolidBorder(1.0, 2.0, Color(0.8, 0.8, 0.8), None, Color(0.6, 0.6, 0.6), Color(0.9, 0.9, 0.9))


def _presentComponentsUnderPointer(elementUnderPointer, guiEditorRootElement):
	propValues = elementUnderPointer.gatherPropertyInAncestryTo(GUICProp.instance, guiEditorRootElement)

	# Components under pointer
	components = []
	for p in propValues:
		component = p.value
		if not component.isRootGUIEditorComponent:
			interactor = ComponentHighlightInteractor(p.value, p.element)
			c = componentUnderPointerBorder.surround(Label(component.componentName).pad(4.0, 2.0)).alignHExpand()
			c = c.withElementInteractor(interactor)
			components.append(c)
	return FlowGrid(3, components)



def _addPanelButtons(sourceElement, menu):
	# Side panel
	def _onShow(button, event):
		showSidePanel(sourceElement.rootElement)

	def _onHide(button, event):
		hideSidePanel(sourceElement.rootElement)

	panelButtons = ControlsRow([Button.buttonWithLabel('Show', _onShow), Button.buttonWithLabel('Hide', _onHide)])
	panelSection = Section(SectionHeading3('Side panel'), panelButtons)
	menu.add(panelSection)


def componentContextMenu(element, menu):
	# Components under pointer
	guiEditorRootProp = element.findPropertyInAncestors(GUIEdProp.instance)
	guiEditorRootElement = guiEditorRootProp.element

	# Components under pointer
	menu.add(Section(SectionHeading3('Components under pointer'), _presentComponentsUnderPointer(element, guiEditorRootElement)))

	current = currentComponentEditor(element.rootElement)
	menu.add(current)


	# Side panel
	_addPanelButtons(element, menu)

	return True



def guiEditorContextMenu(element, menu):
	# Side panel
	_addPanelButtons(menu)

	menu.addSeparator()

	return False