##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from weakref import WeakKeyDictionary

from java.awt.event import KeyEvent

from BritefuryJ.Incremental import IncrementalOwner, IncrementalValueMonitor
from BritefuryJ.Cell import Cell

from BritefuryJ.DocPresent import TextEditEvent, ElementValueFunction
from BritefuryJ.DocPresent.Combinators.Primitive import *
from BritefuryJ.DocPresent.Combinators.RichText import *
from BritefuryJ.DocPresent.Interactor import *
from BritefuryJ.DocPresent.StyleSheet import *

from BritefuryJ.GSym.PresCom import InnerFragment

from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod

from GSymCore.PresTemplate import Schema

from GSymCore.PresTemplate.TemplateEditor.NodeView import NodeView
from GSymCore.PresTemplate.TemplateEditor.SelectionEditor import TemplateSelectionEditTreeEvent
from GSymCore.PresTemplate.TemplateEditor.NodeOperations import NodeRequest, TextNodeJoinOperation, TextNodeSplitOperation, PargraphRequest



_paragraphStyle = StyleSheet.instance.withAttr( RichText.appendNewlineToParagraphs, True )




class ParagraphView (NodeView):
	def __init__(self, template, model):
		super( ParagraphView, self ).__init__( template, model )
	
		
	def getText(self):
		return self._model['text']
	
	def setText(self, text):
		self._model['text'] = text
		
	
	def getStyle(self):
		return self._model['style']
	
	def setStyle(self, style):
		self._model['style'] = style
		
		
	def partialModel(self):
		return Schema.PartialParagraph( style=self._model['style'] )
		
		
	@staticmethod
	def newParagraphModel(text, style):
		return Schema.Paragraph( text=text, style=style )
	
	
	
	def templateEditorPresent(self, fragment, inheritedState):
		text = self.getText()
		style = self.getStyle()
		if style == 'normal':
			p = NormalText( text )
		elif style == 'h1':
			p = Heading1( text )
		elif style == 'h2':
			p = Heading2( text )
		elif style == 'h3':
			p = Heading3( text )
		elif style == 'h4':
			p = Heading4( text )
		elif style == 'h5':
			p = Heading5( text )
		elif style == 'h6':
			p = Heading6( text )
		elif style == 'title':
			p = TitleBar( text )
		p = _paragraphStyle.applyTo( p )
		p = withParagraphStreamValueFn( p, self.partialModel() )
		w = Span( [ p ] )
		w = w.withTreeEventListener( TextNodeEventListener.instance )
		w = w.withElementInteractor( TextNodeInteractor.instance )
		w = w.withFixedValue( self.getModel() )
		return w


	
	
	
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
		return element.getDefaultValue()
	
	def addStreamValuePrefixToStream(self, builder, element):
		if self._prefix is not None:
			builder.append( self._prefix )
		
	def addStreamValueSuffixToStream(self, builder, element):
		pass
			
			
def withParagraphStreamValueFn(pres, prefix):
	return pres.withValueFunction( _ParagraphStreamValueFn( prefix ) )




class TextNodeInteractor (KeyElementInteractor):
	def __init__(self):
		pass
		
		
	def keyTyped(self, element, event):
		return False
		
		
	def keyPressed(self, element, event):
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
	
	
	
	def keyReleased(self, element, event):
		return False



		
TextNodeInteractor.instance = TextNodeInteractor()	
