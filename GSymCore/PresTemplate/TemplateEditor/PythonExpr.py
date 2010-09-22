##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.DocPresent import *


from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod

from GSymCore.Languages.Python25 import Python25

from GSymCore.PresTemplate import Schema
from GSymCore.PresTemplate.TemplateEditor.NodeOperations import NodeRequest
from GSymCore.PresTemplate.TemplateEditor.PythonExprView import PythonExprView




class PythonExprRequest (NodeRequest):
	def applyToParagraphNode(self, paragraph, element):
		return self._insertAfter( paragraph, element )
		
	def applyToPythonExprNode(self, pythonExpr, element):
		return self._insertAfter( pythonExpr, element )
	
	def _createModel(self):
		return PythonExprView.newPythonExprModel()




class PythonExprNodeEventListener (TreeEventListenerObjectDispatch):
	def __init__(self):
		pass


	@ObjectDispatchMethod( NodeRequest )
	def onNodeRequest(self, element, sourceElement, event):
		return event.applyToPythonExprNode( element.getFragmentContext().getModel(), element )


PythonExprNodeEventListener.instance = PythonExprNodeEventListener()		


	


		
