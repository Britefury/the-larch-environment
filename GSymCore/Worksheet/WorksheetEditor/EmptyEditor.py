##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.DocPresent import *


from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod

from GSymCore.Worksheet import Schema, ViewSchema
from GSymCore.Worksheet.WorksheetEditor.NodeOperations import NodeRequest



class EmptyEventListener (TreeEventListenerObjectDispatch):
	def __init__(self):
		pass

	@ObjectDispatchMethod( TextEditEvent )
	def onTextEdit(self, element, sourceElement, event):
		value = element.getTextRepresentation()
		ctx = element.getFragmentContext()
		node = ctx.getDocNode()
		lines = value.split( '\n' )
		if lines[-1] == ''  and  len( lines ) > 1:
			del lines[-1]
		for line in lines:
			node.appendModel( ViewSchema.ParagraphView.newParagraphModel( text=line, style='normal' ) )
		return True

	
	@ObjectDispatchMethod( NodeRequest )
	def onNodeRequest(self, element, sourceElement, event):
		return event.applyToEmpty( element.getFragmentContext().getDocNode(), element )


EmptyEventListener.instance = EmptyEventListener()

