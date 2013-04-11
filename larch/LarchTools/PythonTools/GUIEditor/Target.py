##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.awt import Color, BasicStroke
from java.awt.geom import Rectangle2D

from BritefuryJ.LSpace.Interactor import ScrollElementInteractor, TargetElementInteractor

from BritefuryJ.LSpace.Focus import Target, SelectionPoint


#
#Selection point and target:
class GUIEditorSelectionPoint (SelectionPoint):
	def __init__(self, component, element):
		self.__component = component
		self.__element = element

	def isValid(self):
		return self.__element.isRealised()

	def createSelectionTo(self, point):
		return None


class GUIEditorTarget (Target):
	def __init__(self, component, element):
		self.__component = component
		self.__element = element

	@property
	def component(self):
		return self.__component

	@property
	def element(self):
		return self.__element

	def createSelectionPoint(self):
		return GUIEditorSelectionPoint(self.__component, self.__element)

	def getElement(self):
		return self.__element

	def isEditable(self):
		return self._element.region.isEditable()

	def draw(self, graphics):
		sz = self.__element.getActualSize()
		prevPaint = graphics.getPaint()
		prevStroke = graphics.getStroke()
		current = self.__element.pushLocalToRootGraphicsTransform(graphics)
		graphics.setPaint(Color(0.0, 0.3, 0.6, 0.5))
		graphics.setStroke(BasicStroke(2.0))
		graphics.draw(Rectangle2D.Double(1.0, 1.0, sz.x - 2.0, sz.y - 2.0))
		graphics.setPaint(prevPaint)
		graphics.setStroke(prevStroke)
		self.__element.popGraphicsTransform(graphics, current)



#
#
#Interactors:
class GUITargetInteractor (TargetElementInteractor):
	def targetDragBegin(self, element, event):
		if event.button == 1:
			model = element.fragmentContext.model

			target = element.getRootElement().getTarget()
			if isinstance(target, GUIEditorTarget) and target.component is model or target.element.isInSubtreeRootedAt(element):
				return None

			target = GUIEditorTarget(model, element)
			element.getRootElement().setTarget(target)

			return GUIEditorSelectionPoint(model, element)

	def targetDragEnd(self, element, event, dragStartPos, dragButton):
		pass

	def targetDragMotion(self, element, event, dragStartPos, dragButton):
		return None

class GUIScrollInteractor (ScrollElementInteractor):
	def scroll(self, element, event):
		prevTarget = element.getRootElement().getTarget()
		model = element.fragmentContext.model

		if isinstance(prevTarget, GUIEditorTarget):
			current = prevTarget.component
			y = event.scrollY
			if y < 0:
				if element.isInSubtreeRootedAt(prevTarget.element):
					prev = None
					val = element.findPropertyInAncestors(GUICProp.instance)
					while val is not None and val.value is not current:
						prev = val
						val = val.element.parent.findPropertyInAncestors(GUICProp.instance)
					if prev is not None:
						target = GUIEditorTarget(prev.value, prev.element)
						element.getRootElement().setTarget(target)
				else:
					target = GUIEditorTarget(model, element)
					element.getRootElement().setTarget(target)
			elif y > 0:
				val = prevTarget.element.parent.findPropertyInAncestors(GUICProp.instance)
				if val is not None:
					target = GUIEditorTarget(val.value, val.element)
					element.getRootElement().setTarget(target)
		else:
			target = GUIEditorTarget(model, element)
			element.getRootElement().setTarget(target)
		return True

