##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from java.awt.event import KeyEvent

from BritefuryJ.DocPresent import *


from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod

from GSymCore.Worksheet import Schema, ViewSchema
from GSymCore.Worksheet.WorksheetEditor.PythonCode import AddPythonCodeOperation
from GSymCore.Worksheet.WorksheetEditor.TextNodeEditor import TextNodeJoinOperation


class WorksheetNodeEventListener (TreeEventListenerObjectDispatch):
	def __init__(self):
		pass

	@ObjectDispatchMethod( AddPythonCodeOperation )
	def onAddPythonCode(self, element, sourceElement, event):
		ctx = element.getFragmentContext()
		node = ctx.getDocNode().getModel()
		
		return event.apply( node )
	
	@ObjectDispatchMethod( TextNodeJoinOperation )
	def onTextJoin(self, element, sourceElement, event):
		ctx = element.getFragmentContext()
		node = ctx.getDocNode().getModel()
		
		return event.apply( node )
		
	
WorksheetNodeEventListener.instance = WorksheetNodeEventListener()



class WorksheetNodeInteractor (ElementInteractor):
	def __init__(self):
		pass
		
		
	def onKeyPress(self, element, event):
		if event.getKeyCode() == KeyEvent.VK_F5:
			ctx = element.getFragmentContext()
			node = ctx.getDocNode()
			node.refreshResults()
			return True
		
WorksheetNodeInteractor.instance = WorksheetNodeInteractor()		
