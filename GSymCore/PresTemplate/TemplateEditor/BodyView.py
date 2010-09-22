##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from weakref import WeakKeyDictionary

from BritefuryJ.Incremental import IncrementalOwner, IncrementalValueMonitor
from BritefuryJ.Cell import Cell

from BritefuryJ.Logging import LogEntry

from BritefuryJ.DocPresent import TextEditEvent
from BritefuryJ.DocPresent.StyleSheet import StyleSheet
from BritefuryJ.DocPresent.Combinators import *
from BritefuryJ.DocPresent.Combinators.Primitive import *
from BritefuryJ.DocPresent.Combinators.RichText import *

from BritefuryJ.GSym.PresCom import InnerFragment

from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod

from GSymCore.PresTemplate import Schema

from GSymCore.PresTemplate.TemplateEditor.NodeView import NodeView
from GSymCore.PresTemplate.TemplateEditor.ParagraphView import ParagraphView
from GSymCore.PresTemplate.TemplateEditor.SelectionEditor import TemplateSelectionEditTreeEvent
from GSymCore.PresTemplate.TemplateEditor.NodeOperations import AddNodeOperation, NodeRequest, DeleteNodeOperation, TextNodeJoinOperation, TextNodeSplitOperation



class BodyView (NodeView):
	def __init__(self, template, model):
		super( BodyView, self ).__init__( template, model )
		self._contentsCell = Cell( self._computeContents )
		
		
	def getContents(self):
		return self._contentsCell.getValue()
	
	
	def appendModel(self, node):
		self._model['contents'].append( node )
	
	def insertModelAfterNode(self, node, model):
		try:
			index = self.getContents().index( node )
		except ValueError:
			return False
		self._model['contents'].insert( index + 1, model )
		return True

	def deleteNode(self, node):
		try:
			index = self.getContents().index( node )
		except ValueError:
			return False
		del self._model['contents'][index]
		return True
		
		
		
	def joinConsecutiveTextNodes(self, firstNode):
		assert isinstance( firstNode, ParagraphView )
		contents = self.getContents()
		
		try:
			index = contents.index( firstNode )
		except ValueError:
			return False
		
		if ( index + 1)  <  len( contents ):
			next = contents[index+1]
			if isinstance( next, ParagraphView ):
				firstNode.setText( firstNode.getText() + next.getText() )
				del self._model['contents'][index+1]
				return True
		return False
	
	def splitTextNodes(self, textNode, textLines):
		style = textNode.getStyle()
		textModels = [ Schema.Paragraph( text=t, style=style )   for t in textLines ]
		try:
			index = self.getContents().index( textNode )
		except ValueError:
			return False
		self._model['contents'][index:index+1] = textModels
		return True
		
		
	def _computeContents(self):
		return [ self._viewOf( x )   for x in self._model['contents'] ]
	
		
	
	def templateEditorPresent(self, fragment, inheritedState):
		emptyLine = Paragraph( [ Text( '' ) ] )
		emptyLine = emptyLine.withTreeEventListener( EmptyEventListener.instance )
		contentViews = list( InnerFragment.map( self.getContents() ) )  +  [ emptyLine ]
		
		w = Body( contentViews )
		w = w.withTreeEventListener( BodyNodeEventListener.instance )
		return w






class BodyNodeEventListener (TreeEventListenerObjectDispatch):
	def __init__(self):
		pass

	
	
	@ObjectDispatchMethod( DeleteNodeOperation )
	def onDeleteNode(self, element, sourceElement, event):
		ctx = element.getFragmentContext()
		node = ctx.getModel()
		
		return event.apply( node )
	

	@ObjectDispatchMethod( AddNodeOperation )
	def onAddNode(self, element, sourceElement, event):
		ctx = element.getFragmentContext()
		node = ctx.getModel()
		
		return event.apply( node )
	
	
	@ObjectDispatchMethod( TextNodeJoinOperation )
	def onTextJoin(self, element, sourceElement, event):
		ctx = element.getFragmentContext()
		node = ctx.getModel()
		
		return event.apply( node )

	
	@ObjectDispatchMethod( TextNodeSplitOperation )
	def onTextSplit(self, element, sourceElement, event):
		ctx = element.getFragmentContext()
		node = ctx.getModel()
		
		return event.apply( node )


	@ObjectDispatchMethod( TemplateSelectionEditTreeEvent )
	def onSelectionEdit(self, element, sourceElement, event):
		value = element.getStreamValue()
		node = element.getFragmentContext().getModel()
		
		log = element.getFragmentContext().getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'TmplEdit' ).hItem( 'description', 'Body - selection edit' ).vItem( 'editedStream', value ) )
			
		
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
				if x.isInstanceOf( Schema.TemplateNode ):
					_addPartial()
					xs.append( x )
				elif x.isInstanceOf( Schema.TemplatePartialNode ):
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





class EmptyEventListener (TreeEventListenerObjectDispatch):
	def __init__(self):
		pass

	@ObjectDispatchMethod( TextEditEvent )
	def onTextEdit(self, element, sourceElement, event):
		value = element.getTextRepresentation()
		ctx = element.getFragmentContext()
		node = ctx.getModel()
		lines = value.split( '\n' )
		if lines[-1] == ''  and  len( lines ) > 1:
			del lines[-1]
		for line in lines:
			node.appendModel( ParagraphView.newParagraphModel( text=line, style='normal' ) )
		return True

	
	@ObjectDispatchMethod( NodeRequest )
	def onNodeRequest(self, element, sourceElement, event):
		return event.applyToEmpty( element.getFragmentContext().getModel(), element )


EmptyEventListener.instance = EmptyEventListener()
