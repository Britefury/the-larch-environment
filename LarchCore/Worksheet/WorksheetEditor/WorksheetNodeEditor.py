##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from java.awt.event import KeyEvent

from BritefuryJ.DocPresent.Interactor import KeyElementInteractor


from Britefury.Kernel.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod

from LarchCore.Worksheet import Schema, ViewSchema
from LarchCore.Worksheet.WorksheetEditor.NodeOperations import AddNodeOperation


class WorksheetNodeInteractor (KeyElementInteractor):
	def __init__(self):
		pass


	def keyPressed(self, element, event):
		if event.getKeyCode() == KeyEvent.VK_F5:
			ctx = element.getFragmentContext()
			node = ctx.getModel()
			node.refreshResults()
			return True
		else:
			return False

	def keyReleased(self, element, event):
		return False

	def keyTyped(self, element, event):
		return False

WorksheetNodeInteractor.instance = WorksheetNodeInteractor()		
