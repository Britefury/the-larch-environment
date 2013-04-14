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

from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Pres.Primitive import Primitive, Label, Row, Column, Paragraph, FlowGrid, Blank, Proxy
from BritefuryJ.Pres.UI import Section, SectionHeading1, SectionHeading2, SectionHeading3, ControlsRow

from BritefuryJ.Controls import Button

from LarchTools.PythonTools.GUIEditor.Properties import GUICProp, GUIEdProp
from LarchTools.PythonTools.GUIEditor.SidePanel import showSidePanel, hideSidePanel
from LarchTools.PythonTools.GUIEditor.Target import GUIEditorTarget, GUITargetListener


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
		if component.componentName is not None:
			interactor = ComponentHighlightInteractor(p.value, p.element)
			c = componentUnderPointerBorder.surround(Label(component.componentName).pad(4.0, 2.0)).alignHExpand()
			c = c.withElementInteractor(interactor)
			components.append(c)
	return FlowGrid(3, components)



def _addPanelButtons(menu):
	# Side panel
	def _onShow(button, event):
		showSidePanel(button.element)

	def _onHide(button, event):
		hideSidePanel(button.element)

	panelButtons = ControlsRow([Button.buttonWithLabel('Show', _onShow), Button.buttonWithLabel('Hide', _onHide)])
	panelSection = Section(SectionHeading3('Side panel'), panelButtons)
	menu.add(panelSection)


def componentContextMenu(element, menu):
	# Components under pointer
	guiEditorRootProp = element.findPropertyInAncestors(GUIEdProp.instance)
	guiEditorRootElement = guiEditorRootProp.element
	target = element.rootElement.getTarget()
	target = target   if isinstance(target, GUIEditorTarget)   else None

	# Components under pointer
	menu.add(Section(SectionHeading3('Components under pointer'), _presentComponentsUnderPointer(element, guiEditorRootElement)))


	currentComponent = LiveValue()

	# Component UI
	def refreshComponent(component):
		currentComponent.setLiteralValue(component)

	targetListener = GUITargetListener(refreshComponent)

	refreshComponent(target.component   if target is not None   else None)


	@LiveFunction
	def currentComponentUI():
		component = currentComponent.getValue()
		if component is not None:
			editUI = component._editUI()
			return Section(SectionHeading3(component.componentName), editUI)
		else:
			return Blank()


	componentUI = targetListener.tieToLifeTimeOf(currentComponentUI, element.rootElement)
	menu.add(componentUI)


	# Side panel
	_addPanelButtons(menu)

	return True



def guiEditorContextMenu(element, menu):
	# Side panel
	_addPanelButtons(menu)

	menu.addSeparator()

	return False