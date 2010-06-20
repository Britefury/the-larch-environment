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
from GSymCore.Worksheet.WorksheetEditor.TextNodeEditor import TextNodeJoinOperation, TextNodeSplitOperation
from GSymCore.Worksheet.WorksheetEditor.SelectionEditor import WorksheetSelectionEditTreeEvent



class DeleteNodeOperation (object):
	def __init__(self, node):
		self._node = node
		
	def apply(self, bodyNode):
		return bodyNode.deleteNode( self._node )


class BodyNodeEventListener (TreeEventListenerObjectDispatch):
	def __init__(self):
		pass

	
	
	@ObjectDispatchMethod( DeleteNodeOperation )
	def onDeleteNode(self, element, sourceElement, event):
		ctx = element.getFragmentContext()
		node = ctx.getDocNode()
		
		return event.apply( node )
	

	@ObjectDispatchMethod( AddPythonCodeOperation )
	def onAddPythonCode(self, element, sourceElement, event):
		ctx = element.getFragmentContext()
		node = ctx.getDocNode()
		
		return event.apply( node )
	
	
	@ObjectDispatchMethod( TextNodeJoinOperation )
	def onTextJoin(self, element, sourceElement, event):
		ctx = element.getFragmentContext()
		node = ctx.getDocNode()
		
		return event.apply( node )

	
	@ObjectDispatchMethod( TextNodeSplitOperation )
	def onTextSplit(self, element, sourceElement, event):
		ctx = element.getFragmentContext()
		node = ctx.getDocNode()
		
		return event.apply( node )


	@ObjectDispatchMethod( WorksheetSelectionEditTreeEvent )
	def onSelectionEdit(self, element, sourceElement, event):
		element.clearStructuralValue()
		value = element.getLinearRepresentation()
		node = element.getFragmentContext().getDocNode()
		
		xs = []
		
		partialValue = []
		def _addPartial():
			if len( partialValue ) > 0:
				xs.append( partialValue[0] )
				del partialValue[0]
		def _newPartial(p):
			if p.isInstanceOf( Schema.PartialParagraph ):
				partialValue.append( Schema.Paragraph( text='', style=p['style'] ) )
			else:
				raise TypeError
		def _partialText(t):
			if len( partialValue ) > 0:
				partialValue[0]['text'] = partialValue[0]['text'] + t
			else:
				return False

		for item in value.getItems():
			if item.isStructural():
				x = item.getStructuralValue()
				if x.isInstanceOf( Schema.WorksheetNode ):
					_addPartial()
					xs.append( x )
				elif x.isInstanceOf( Schema.WorksheetPartialNode ):
					_addPartial()
					_newPartial( x )
				else:
					raise TypeError
			else:
				_partialText( item.getTextValue() )
		
		_addPartial()
		
		node.getModel()['contents'] = xs
		
		return True
	
	
BodyNodeEventListener.instance = BodyNodeEventListener()
