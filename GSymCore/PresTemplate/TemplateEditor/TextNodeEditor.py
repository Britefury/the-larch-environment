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
from GSymCore.PresTemplate.TemplateEditor.SelectionEditor import TemplateSelectionEditTreeEvent
from GSymCore.PresTemplate.TemplateEditor.NodeOperations import NodeRequest




class TextNodeJoinOperation (object):
	def __init__(self, textNode):
		self._textNode = textNode
		

	def apply(self, bodyNode):
		return bodyNode.joinConsecutiveTextNodes( self._textNode )



class TextNodeSplitOperation (object):
	def __init__(self, textNode, textLines):
		self._textNode = textNode
		self._textLines = textLines
		

	def apply(self, bodyNode):
		return bodyNode.splitTextNodes( self._textNode, self._textLines )



class PargraphRequest (NodeRequest):
	def __init__(self, style):
		self._style = style
		
	def applyToParagraphNode(self, paragraph, element):
		paragraph.setStyle( self._style )
		return True
		
	def applyToPythonExprNode(self, pythonExpr, element):
		return self._insertAfter( pythonExpr, element )
	
	def _createModel(self):
		return ViewSchema.ParagraphView.newParagraphModel( '', self._style )




class TextNodeEventListener (TreeEventListenerObjectDispatch):
	def __init__(self):
		pass


	@ObjectDispatchMethod( TextEditEvent )
	def onTextEdit(self, element, sourceElement, event):
		value = element.getTextRepresentation()
		ctx = element.getFragmentContext()
		node = ctx.getModel()
		return self._performTextEdit( element, node, value )


	@ObjectDispatchMethod( NodeRequest )
	def onNodeRequest(self, element, sourceElement, event):
		return event.applyToParagraphNode( element.getFragmentContext().getModel(), element )


	@ObjectDispatchMethod( TemplateSelectionEditTreeEvent )
	def onSelectionEdit(self, element, sourceElement, event):
		value = element.getStreamValue()
		node = element.getFragmentContext().getModel()
		if value.isTextual():
			return self._performTextEdit( element, node, value.textualValue() )
		else:
			return False


	def _performTextEdit(self, element, node, value):
		if value.endswith( '\n' ):
			value = value[:-1]
			if '\n' not in value:
				node.setText( value )
				return True
			else:
				return element.postTreeEvent( TextNodeSplitOperation( node, value.split( '\n' ) ) )
		else:
			return element.postTreeEvent( TextNodeJoinOperation( node ) )


TextNodeEventListener.instance = TextNodeEventListener()		




class _ParagraphStreamValueFn (ElementValueFunction):
	def __init__(self, prefix):
		self._prefix = prefix
		
	def computeElementValue(self, element):
		return element.getValue()
	
	def addStreamValuePrefixToStream(self, builder, element):
		if self._prefix is not None:
			builder.append( self._prefix )
		
	def addStreamValueSuffixToStream(self, builder, element):
		pass
			
			
def withParagraphStreamValueFn(pres, prefix):
	return pres.withValueFunction( _ParagraphStreamValueFn( prefix ) )




class TextNodeInteractor (ElementInteractor):
	def __init__(self):
		pass
		
		
	def onKeyTyped(self, element, event):
		return False
		
		
	def onKeyPress(self, element, event):
		if event.getModifiers() & KeyEvent.ALT_MASK  !=  0:
			ctx = element.getFragmentContext()
			node = ctx.getModel()

			if event.getKeyCode() == KeyEvent.VK_N:
				node.setStyle( 'normal' )
			elif event.getKeyCode() == KeyEvent.VK_1:
				node.setStyle( 'h1' )
			elif event.getKeyCode() == KeyEvent.VK_2:
				node.setStyle( 'h2' )
			elif event.getKeyCode() == KeyEvent.VK_3:
				node.setStyle( 'h3' )
			elif event.getKeyCode() == KeyEvent.VK_4:
				node.setStyle( 'h4' )
			elif event.getKeyCode() == KeyEvent.VK_5:
				node.setStyle( 'h5' )
			elif event.getKeyCode() == KeyEvent.VK_6:
				node.setStyle( 'h6' )
			elif event.getKeyCode() == KeyEvent.VK_T:
				node.setStyle( 'title' )
			else:
				return False
			
			return True
			
		return False
	
	
	
	def onKeyRelease(self, element, event):
		return False



		
TextNodeInteractor.instance = TextNodeInteractor()	
