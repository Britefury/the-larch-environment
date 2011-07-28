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
from LarchCore.Worksheet import WorksheetEditor2





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
		self._editorModel = WorksheetEditor2.RichTextEditor.WorksheetRichTextEditor.instance.editorModelBlock( [] )



	def appendEditorNode(self, editor):
		self._model['contents'].append( editor.getModel() )

	def insertEditorNodeAfter(self, editor, pos):
		try:
			index = self.getContents().index( pos )
		except ValueError:
			return False
		self._model['contents'].insert( index + 1, editor.getModel() )
		return True

	def deleteEditorNode(self, editor):
		try:
			index = self.getContents().index( editor )
		except ValueError:
			return False
		del self._model['contents'][index]
		return True



	def setContents(self, contents):
		modelContents = [ x.getModel()   for x in contents ]
		self._model['contents'] = modelContents


	def _computeContents(self):
		blank = BlankParagraphEditor( self._worksheet, self )
		xs = [ self._viewOf( x )   for x in self._model['contents'] ]  +  [ blank ]
		self._editorModel.setModelContents( WorksheetEditor2.RichTextEditor.WorksheetRichTextEditor.instance, xs )
		return xs



class BlankParagraphEditor (AbstractViewSchema.NodeAbstractView):
	def __init__(self, worksheet, blockEditor):
		super( BlankParagraphEditor, self ).__init__( worksheet, None )
		self._style = 'normal'
		self._editorModel = WorksheetEditor2.RichTextEditor.WorksheetRichTextEditor.instance.editorModelParagraph( [ '' ], { 'style' : self._style } )
		self._incr = IncrementalValueMonitor()
		self._blockEditor = blockEditor


	def getText(self):
		self._incr.onAccess()
		return ''

	def setContents(self, contents):
		if len( contents ) == 0:
			return
		elif len( contents ) == 1  and  contents[0] == '':
			return
		p = ParagraphEditor.newParagraph( contents, self._style )
		self._blockEditor.appendEditorNode( p )


	def getStyle(self):
		self._incr.onAccess()
		return self._style

	def setStyle(self, style):
		self._style = style
		self._incr.onChanged()


	def _refreshResults(self, module):
		pass




class ParagraphEditor (AbstractViewSchema.ParagraphAbstractView):
	def __init__(self, worksheet, model):
		super( ParagraphEditor, self ).__init__( worksheet, model )
		self._editorModel = WorksheetEditor2.RichTextEditor.WorksheetRichTextEditor.instance.editorModelParagraph( [ model['text'] ], { 'style' : model['style'] } )
	

	def setContents(self, contents):
		modelContents = [ ( x   if isinstance( x, str ) or isinstance( x, unicode )   else x.getModel() )   for x in contents ]
		self._model['text'] = modelContents[0]   if len( modelContents ) > 0   else ''
		self._editorModel.setModelContents( WorksheetEditor2.RichTextEditor.WorksheetRichTextEditor.instance, modelContents )


	def setStyle(self, style):
		self._model['style'] = style
		self._editorModel.setStyleAttrs( { 'style' : style } )
		
		
	@staticmethod
	def newParagraph(contents, style):
		m = ParagraphEditor.newParagraphModel( contents[0]   if len( contents ) > 0   else '', style )
		return ParagraphEditor( None, m )

	@staticmethod
	def newParagraphModel(text, style):
		return Schema.Paragraph( text=text, style=style )
		
		
		
class TextSpanEditor (AbstractViewSchema.TextSpanAbstractView):
	def __init__(self, worksheet, model):
		super( TextSpanEditor, self ).__init__( worksheet, model )
		styleAttrs = {}
		for key, value in model['styleAttrs']:
			styleAttrs[key] = value
		self._editorModel = WorksheetEditor2.RichTextEditor.WorksheetRichTextEditor.instance.editorModelSpan( [ model['text'] ], styleAttrs )

		
	def setContents(self, contents):
		modelContents = [ ( x   if isinstance( x, str ) or isinstance( x, unicode )   else x.getModel() )   for x in contents ]
		self._model['text'] = modelContents[0]
		self._editorModel.setModelContents( WorksheetEditor2.RichTextEditor.WorksheetRichTextEditor.instance, modelContents )

	
	def setStyleAttrs(self, styleAttrs):
		modelAttrs = [ [ key, value ]   for key, value in styleAttrs.items() ]
		self._model['styleAttrs'] = modelAttrs
		self._editorModel.setStyleAttrs( styleAttrs )

		
	@staticmethod
	def newTextSpan(contents, styleAttrs):
		m = TextSpanEditor.newTextSpanModel( contents[0]   if len( contents ) > 0   else '', styleAttrs )
		return TextSpanEditor( None, m )

	@staticmethod
	def newTextSpanModel(text, styleAttrs):
		return Schema.TextSpan( text=text, styleAttrs=styleAttrs )
		
		
		
class PythonCodeEditor (AbstractViewSchema.PythonCodeAbstractView):
	def __init__(self, worksheet, model):
		super( PythonCodeEditor, self ).__init__( worksheet, model )
		self._editorModel = WorksheetEditor2.RichTextEditor.WorksheetRichTextEditor.instance.editorModelParagraphEmbed( model )

		
	def setCode(self, code):
		self._model['code'] = code
		
		
		
	def setStyle(self, style):
		try:
			name = self._styleToName[style]
		except KeyError:
			raise ValueError, 'invalid style'
		self._model['style'] = name


	@staticmethod
	def newPythonCode():
		m = PythonCodeEditor.newPythonCodeModel()
		return PythonCodeEditor( None, m )

	@staticmethod
	def newPythonCodeModel():
		return Schema.PythonCode( style='code_result', code=Python25.py25NewModule() )

	
	
class QuoteLocationEditor (AbstractViewSchema.QuoteLocationAbstractView):
	def __init__(self, worksheet, model):
		super( QuoteLocationEditor, self ).__init__( worksheet, model )
		self._editorModel = WorksheetEditor2.RichTextEditor.WorksheetRichTextEditor.instance.editorModelParagraphEmbed( model )

		
	def setLocation(self, location):
		self._model['location'] = location
		
		
		
	def setStyle(self, style):
		try:
			name = self._styleToName[style]
		except KeyError:
			raise ValueError, 'invalid style'
		self._model['style'] = name
		
		
	@staticmethod
	def newQuoteLocation():
		m = QuoteLocationEditor.newQuoteLocationModel()
		return QuoteLocationEditor( None, m )

	@staticmethod
	def newQuoteLocationModel():
		return Schema.QuoteLocation( location='', style='normal' )
