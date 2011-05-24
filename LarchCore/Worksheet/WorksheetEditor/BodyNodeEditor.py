##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from java.awt.event import KeyEvent

from BritefuryJ.DocPresent import *

from BritefuryJ.Logging import LogEntry

from BritefuryJ.Editor.Sequential import StreamEditListener


from Britefury.Kernel.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod

from LarchCore.Worksheet import Schema, ViewSchema
from LarchCore.Worksheet.WorksheetEditor.SequentialEditor import WorksheetSequentialEditor
from LarchCore.Worksheet.WorksheetEditor.TextNodeEditor import TextNodeJoinOperation, TextNodeSplitOperation
from LarchCore.Worksheet.WorksheetEditor.NodeOperations import AddNodeOperation, NodeRequest



class DeleteNodeOperation (object):
	def __init__(self, node):
		self._node = node
		
	def apply(self, bodyNode):
		return bodyNode.deleteNode( self._node )





class BodyNodeEditListener (StreamEditListener):
	def __init__(self):
		super( BodyNodeEditListener, self ).__init__()
	
	def getSequentialEditor(self):
		return WorksheetSequentialEditor.instance
	
	def handleValue(self, element, sourceElement, fragment, event, model, value):
		log = element.getFragmentContext().getView().getLog()
		if log.isRecording():
			log.log( LogEntry( 'WsEdit' ).hItem( 'description', 'Body - selection edit' ).vItem( 'editedStream', value ) )
			
		
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
				partialValue.append( Schema.Paragraph( text=t, style='normal' ) )

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
		
		model.getModel()['contents'] = xs
		
		return StreamEditListener.HandleEditResult.HANDLED

	
	
BodyNodeEditListener.instance = BodyNodeEditListener()




class BodyNodeEventListener (TreeEventListenerObjectDispatch):
	def __init__(self):
		pass

	
	
	@ObjectDispatchMethod( DeleteNodeOperation, AddNodeOperation, TextNodeJoinOperation, TextNodeSplitOperation )
	def onDeleteNode(self, element, sourceElement, event):
		return event.apply( element.getFragmentContext().getModel() )
	
	
BodyNodeEventListener.instance = BodyNodeEventListener()





class EmptyEditListener (StreamEditListener):
	def __init__(self):
		super( EmptyEditListener, self ).__init__()
	
	def getSequentialEditor(self):
		return WorksheetSequentialEditor.instance
	
	def handleValue(self, element, sourceElement, fragment, event, model, value):
		lines = value.textualValue().split( '\n' )
		if lines[-1] == ''  and  len( lines ) > 1:
			del lines[-1]
		for line in lines:
			model.appendModel( ViewSchema.ParagraphView.newParagraphModel( text=line, style='normal' ) )
		return StreamEditListener.HandleEditResult.HANDLED

EmptyEditListener.instance = EmptyEditListener()




class EmptyEventListener (TreeEventListenerObjectDispatch):
	def __init__(self):
		pass

	@ObjectDispatchMethod( NodeRequest )
	def onNodeRequest(self, element, sourceElement, event):
		return event.applyToEmpty( element.getFragmentContext().getModel(), element )


EmptyEventListener.instance = EmptyEventListener()

