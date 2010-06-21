##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.DocPresent import *

from BritefuryJ.Logging import LogEntry

from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod

from GSymCore.Worksheet import Schema, ViewSchema
from GSymCore.Worksheet.WorksheetEditor.SelectionEditor import WorksheetSelectionEditTreeEvent
from GSymCore.Worksheet.WorksheetEditor.NodeOperations import NodeRequest



class TitleOperation (object):
	def apply(self, worksheetNode):
		pass


class TitleJoinOperation (TitleOperation):
	def __init__(self):
		pass
		

	def apply(self, worksheetNode):
		return worksheetNode.joinTitle()



class TitleSplitOperation (TitleOperation):
	def __init__(self, textLines):
		self._textLines = textLines
		

	def apply(self, worksheetNode):
		worksheetNode.splitTitle( self._textLines )
		return True



class TitleEventListener (TreeEventListenerObjectDispatch):
	def __init__(self):
		pass

	@ObjectDispatchMethod( TextEditEvent )
	def onTextEdit(self, element, sourceElement, event):
		value = element.getTextRepresentation()
		ctx = element.getFragmentContext()
		node = ctx.getDocNode()
		if value.endswith( '\n' ):
			value = value[:-1]
			if '\n' not in value:
				node.setTitle( value )
				return True
			else:
				return element.postTreeEvent( TitleSplitOperation( value.split( '\n' ) ) )
		else:
			return element.postTreeEvent( TitleJoinOperation() )
		return True


	@ObjectDispatchMethod( NodeRequest )
	def onNodeRequest(self, element, sourceElement, event):
		return event.applyToTitle( element.getFragmentContext().getDocNode(), element )




TitleEventListener.instance = TitleEventListener()	
