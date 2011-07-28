##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.IncrementalUnit import Unit

from BritefuryJ.Pres import InnerFragment


from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod, dmObjectNodeMethodDispatch

from LarchCore.Languages.Python25 import Python25

from LarchCore.Worksheet import Schema
from LarchCore.Worksheet import AbstractViewSchema





class _Projection (object):
	__dispatch_num_args__ = 1
	

	def __call__(self, node, worksheet):
		return dmObjectNodeMethodDispatch( self, node, worksheet )

	@DMObjectNodeDispatchMethod( Schema.Worksheet )
	def worksheet(self, worksheet, node):
		return WorksheetEditor( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.Body )
	def body(self, worksheet, node):
		return BodyEditor( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.Paragraph )
	def paragraph(self, worksheet, node):
		return ParagraphEditor( worksheet, node )
	
	@DMObjectNodeDispatchMethod( Schema.TextSpan )
	def textSpan(self, worksheet, node):
		return TextSpanEditor( worksheet, node )
	
	@DMObjectNodeDispatchMethod( Schema.PythonCode )
	def pythonCode(self, worksheet, node):
		return PythonCodeEditor( worksheet, node )
	
	@DMObjectNodeDispatchMethod( Schema.QuoteLocation )
	def quoteLocation(self, worksheet, node):
		return QuoteLocationEditor( worksheet, node )





class WorksheetEditor (AbstractViewSchema.WorksheetAbstractView):
	_projection = _Projection()

	
	def __init__(self, worksheet, model):
		super( WorksheetEditor, self ).__init__( worksheet, model )

		


class BodyEditor (AbstractViewSchema.BodyAbstractView):
	def __init__(self, worksheet, model):
		super( BodyEditor, self ).__init__( worksheet, model )



	def insertEditorAfter(self, editor, pos):
		try:
			index = self.getContents().index( pos )
		except ValueError:
			return False
		self._model['contents'].insert( index + 1, editor.getModel() )
		return True

	def deleteEditor(self, editor):
		try:
			index = self.getContents().index( node )
		except ValueError:
			return False
		del self._model['contents'][index]
		return True







	
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
		assert isinstance( firstNode, ParagraphEditor )
		contents = self.getContents()
		
		try:
			index = contents.index( firstNode )
		except ValueError:
			return False
		
		if ( index + 1)  <  len( contents ):
			next = contents[index+1]
			if isinstance( next, ParagraphEditor ):
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

		


class ParagraphEditor (AbstractViewSchema.ParagraphAbstractView):
	def __init__(self, worksheet, model):
		super( ParagraphEditor, self ).__init__( worksheet, model )
	
		
	def setText(self, text):
		self._model['text'] = text
		
	
	def setStyle(self, style):
		self._model['style'] = style
		
		
	def partialModel(self):
		return Schema.PartialParagraph( style=self._model['style'] )
		
		
	@staticmethod
	def newParagraph(contents, style):
		m = ParagraphEditor.newParagraphModel( contents, style )
		return ParagraphEditor( None, m )

	@staticmethod
	def newParagraphModel(text, style):
		return Schema.Paragraph( text=text, style=style )
		
		
		
class TextSpanEditor (AbstractViewSchema.TextSpanAbstractView):
	def __init__(self, worksheet, model):
		super( TextSpanEditor, self ).__init__( worksheet, model )
	
		
	def setText(self, text):
		self._model['text'] = text
		
	
	def setStyleAttrs(self, styleAttrs):
		self._model['styleAttrs'] = styleAttrs
		
		
	@staticmethod
	def newTextSpan(contents, styleAttrs):
		m = TextSpanEditor.newTextSpanModel( contents, styleAttrs )
		return TextSpanEditor( None, m )

	@staticmethod
	def newTextSpanModel(text, styleAttrs):
		return Schema.TextSpan( text=text, styleAttrs=styleAttrs )
		
		
		
class PythonCodeEditor (AbstractViewSchema.PythonCodeAbstractView):
	def __init__(self, worksheet, model):
		super( PythonCodeEditor, self ).__init__( worksheet, model )

		
	def setCode(self, code):
		self._model['code'] = code
		
		
		
	def setStyle(self, style):
		try:
			name = self._styleToName[style]
		except KeyError:
			raise ValueError, 'invalid style'
		self._model['style'] = name


	@staticmethod
	def newPythonCodeModel():
		return Schema.PythonCode( style='code_result', code=Python25.py25NewModule() )

	
	
class QuoteLocationEditor (AbstractViewSchema.QuoteLocationAbstractView):
	def __init__(self, worksheet, model):
		super( QuoteLocationEditor, self ).__init__( worksheet, model )

		
	def setLocation(self, location):
		self._model['location'] = location
		
		
		
	def setStyle(self, style):
		try:
			name = self._styleToName[style]
		except KeyError:
			raise ValueError, 'invalid style'
		self._model['style'] = name
		
		
	@staticmethod
	def newQuoteLocationModel():
		return Schema.QuoteLocation( location='', style='normal' )
