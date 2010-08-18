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

from GSymCore.PresTemplate import Schema, ViewSchema
from GSymCore.PresTemplate.TemplateEditor.NodeOperations import AddNodeOperation


class TemplateNodeEventListener (TreeEventListenerObjectDispatch):
	def __init__(self):
		pass

	
	@ObjectDispatchMethod( AddNodeOperation )
	def onAddNode(self, element, sourceElement, event):
		ctx = element.getFragmentContext()
		node = ctx.getModel()
		
		return event.apply( node.getBody() )


TemplateNodeEventListener.instance = TemplateNodeEventListener()

