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

from GSymCore.Worksheet import Schema, ViewSchema
from GSymCore.Worksheet.WorksheetEditor.NodeOperations import NodeRequest




class QuoteLocationRequest (NodeRequest):
	def applyToParagraphNode(self, paragraph, element):
		return self._insertAfter( paragraph, element )
		
	def applyToQuoteLocationNode(self, pythonCode, element):
		return self._insertAfter( pythonCode, element )
	
	def _createModel(self):
		return ViewSchema.QuoteLocationView.newQuoteLocationModel()




class QuoteLocationNodeEventListener (TreeEventListenerObjectDispatch):
	def __init__(self):
		pass


	@ObjectDispatchMethod( NodeRequest )
	def onNodeRequest(self, element, sourceElement, event):
		return event.applyToQuoteLocationNode( element.getFragmentContext().getModel(), element )


QuoteLocationNodeEventListener.instance = QuoteLocationNodeEventListener()		


	


		
