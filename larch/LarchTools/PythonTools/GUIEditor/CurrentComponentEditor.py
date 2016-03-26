##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Pres.Primitive import Blank
from BritefuryJ.Pres.UI import Section, SectionHeading3

from LarchTools.PythonTools.GUIEditor.Target import GUIEditorTarget, GUITargetListener



def currentComponentEditor(rootElement):
	# Components under pointer
	target = rootElement.getTarget()
	target = target   if target.isValid()  and  isinstance(target, GUIEditorTarget)   else None

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


	componentUI = targetListener.tieToLifeTimeOf(currentComponentUI, rootElement)
	return componentUI

