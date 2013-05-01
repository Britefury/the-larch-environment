##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
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

