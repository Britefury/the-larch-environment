##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from java.awt.event import KeyEvent

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.Interactor import *

from BritefuryJ.Editor.Sequential import StreamEditListener


from Britefury.Kernel.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod

from GSymCore.Worksheet import Schema, ViewSchema
from GSymCore.Worksheet.WorksheetEditor.SequentialEditor import WorksheetSequentialEditor
from GSymCore.Worksheet.WorksheetEditor.NodeOperations import NodeRequest




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

	def applyToPythonCodeNode(self, pythonCode, element):
		return self._insertAfter( pythonCode, element )

	def _createModel(self):
		return ViewSchema.ParagraphView.newParagraphModel( '', self._style )




class TextNodeEditListener (StreamEditListener):
	def __init__(self):
		super( TextNodeEditListener, self ).__init__()
	
	def getSequentialEditor(self):
		return WorksheetSequentialEditor.instance
	
	def handleValue(self, element, sourceElement, fragment, event, model, value):
		bHandled = self._performTextEdit( element, model, value.textualValue() )
		return StreamEditListener.HandleEditResult.HANDLED   if bHandled   else StreamEditListener.HandleEditResult.NOT_HANDLED

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

TextNodeEditListener.instance = TextNodeEditListener()


class TextNodeEventListener (TreeEventListenerObjectDispatch):
	def __init__(self):
		pass


	@ObjectDispatchMethod( NodeRequest )
	def onNodeRequest(self, element, sourceElement, event):
		return event.applyToParagraphNode( element.getFragmentContext().getModel(), element )


TextNodeEventListener.instance = TextNodeEventListener()		




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
			elif event.getKeyCode() == KeyEvent.VK_C:
				self._insertPythonCode( ctx, element, node )
				return True
			else:
				return False

			return True

		return False



	def keyReleased(self, element, event):
		return False



	def _insertPythonCode(self, ctx, element, node):
		#return element.postTreeEvent( InsertPythonCodeOperation( node.getModel() ) )
		return True

TextNodeInteractor.instance = TextNodeInteractor()	
