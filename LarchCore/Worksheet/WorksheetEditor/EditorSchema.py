##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.DocModel import DMObject, DMNode

from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.IncrementalUnit import Unit

from BritefuryJ.Pres import InnerFragment


from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod, dmObjectNodeMethodDispatch

from LarchCore.Languages.Python25 import Python25

from LarchCore.Worksheet import Schema
from LarchCore.Worksheet import AbstractViewSchema
from LarchCore.Worksheet import WorksheetEditor as WSEditor





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

	@DMObjectNodeDispatchMethod( Schema.InlineEmbeddedObject )
	def inlineEmbeddedObject(self, worksheet, node):
		return InlineEmbeddedObjectEditor( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.ParagraphEmbeddedObject )
	def paragraphEmbeddedObject(self, worksheet, node):
		return ParagraphEmbeddedObjectEditor( worksheet, node )





class WorksheetEditor (AbstractViewSchema.WorksheetAbstractView):
	_projection = _Projection()

		


class BodyEditor (AbstractViewSchema.BodyAbstractView):
	def __init__(self, worksheet, model):
		super( BodyEditor, self ).__init__( worksheet, model )
		self._editorModel = WSEditor.RichTextEditor.WorksheetRichTextEditor.instance.editorModelBlock( [] )



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
		modelContents = [ x.getModel()   for x in contents   if not isinstance( x, BlankParagraphEditor ) ]
		self._model['contents'] = modelContents


	def _computeContents(self):
		blank = BlankParagraphEditor( self._worksheet, self )
		xs = [ self._viewOf( x )   for x in self._model['contents'] ]  +  [ blank ]
		self._editorModel.setModelContents( WSEditor.RichTextEditor.WorksheetRichTextEditor.instance, xs )
		return xs



class BlankParagraphEditor (AbstractViewSchema.NodeAbstractView):
	def __init__(self, worksheet, blockEditor):
		super( BlankParagraphEditor, self ).__init__( worksheet, None )
		self._style = 'normal'
		self._editorModel = WSEditor.RichTextEditor.WorksheetRichTextEditor.instance.editorModelParagraph( [ '' ], { 'style' : self._style } )
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
	def __init__(self, worksheet, model, projectedContents=None):
		super( ParagraphEditor, self ).__init__( worksheet, model )
		if projectedContents is None:
			projectedContents = self._computeText()
		self._editorModel = WSEditor.RichTextEditor.WorksheetRichTextEditor.instance.editorModelParagraph( projectedContents, { 'style' : model['style'] } )
	

	def setContents(self, contents):
		modelContents = self._textToModel( contents )
		self._model['text'] = modelContents
		self._editorModel.setModelContents( WSEditor.RichTextEditor.WorksheetRichTextEditor.instance, contents )

	def _removeInlineEmbed(self, embed):
		index = self.getText().index( embed )
		del self._model['text'][index]


	def setStyle(self, style):
		self._model['style'] = style
		self._editorModel.setStyleAttrs( { 'style' : style } )
		
		
	@staticmethod
	def newParagraph(contents, style):
		m = ParagraphEditor.newParagraphModel( ParagraphEditor._textToModel( contents ), style )
		return ParagraphEditor( None, m, contents )

	@staticmethod
	def newParagraphModel(text, style):
		return Schema.Paragraph( text=text, style=style )
		
		
		
class TextSpanEditor (AbstractViewSchema.TextSpanAbstractView):
	def __init__(self, worksheet, model, projectedContents=None):
		super( TextSpanEditor, self ).__init__( worksheet, model )
		styleAttrs = {}
		for a in model['styleAttrs']:
			styleAttrs[a['name']] = a['value']
		if projectedContents is None:
			projectedContents = self._computeText()
		self._editorModel = WSEditor.RichTextEditor.WorksheetRichTextEditor.instance.editorModelSpan( projectedContents, styleAttrs )

		
	def setContents(self, contents):
		modelContents = self._textToModel( contents )
		self._model['text'] = modelContents
		self._editorModel.setModelContents( WSEditor.RichTextEditor.WorksheetRichTextEditor.instance, contents )

	def _removeInlineEmbed(self, embed):
		index = self.getText().index( embed )
		del self._model['text'][index]

	
	def setStyleAttrs(self, styleMap):
		styleAttrs = dict( [ ( n, v )   for n, v in styleMap.items()   if v is not None ] )
		modelAttrs = [ Schema.StyleAttr( name=n, value=v )   for n, v in styleAttrs.items() ]

		self._model['styleAttrs'] = modelAttrs
		self._editorModel.setStyleAttrs( styleAttrs )

		
	@staticmethod
	def newTextSpan(contents, styleMap):
		styleAttrs = [ Schema.StyleAttr( name=n, value=v )   for n, v in styleMap.items()   if v is not None ]
		m = TextSpanEditor.newTextSpanModel( ParagraphEditor._textToModel( contents ), styleAttrs )
		return TextSpanEditor( None, m, contents )

	@staticmethod
	def newTextSpanModel(text, styleAttrs):
		return Schema.TextSpan( text=text, styleAttrs=styleAttrs )
		
		
		
class PythonCodeEditor (AbstractViewSchema.PythonCodeAbstractView):
	def __init__(self, worksheet, model):
		super( PythonCodeEditor, self ).__init__( worksheet, model )
		self._editorModel = WSEditor.RichTextEditor.WorksheetRichTextEditor.instance.editorModelParagraphEmbed( model )

		
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
		self._editorModel = WSEditor.RichTextEditor.WorksheetRichTextEditor.instance.editorModelParagraphEmbed( model )

		
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



class InlineEmbeddedObjectEditor (AbstractViewSchema.InlineEmbeddedObjectAbstractView):
	def __init__(self, worksheet, model):
		super( InlineEmbeddedObjectEditor, self ).__init__( worksheet, model )
		self._editorModel = WSEditor.RichTextEditor.WorksheetRichTextEditor.instance.editorModelInlineEmbed( model )


	@staticmethod
	def newInlineEmbeddedObject(value):
		m = InlineEmbeddedObjectEditor.newInlineEmbeddedObjectModel( value )
		return InlineEmbeddedObjectEditor( None, m )

	@staticmethod
	def newInlineEmbeddedObjectModel(value):
		embeddedValue = DMNode.embedIsolated( value )
		return Schema.InlineEmbeddedObject( embeddedValue=embeddedValue )




class ParagraphEmbeddedObjectEditor (AbstractViewSchema.ParagraphEmbeddedObjectAbstractView):
	def __init__(self, worksheet, model):
		super( ParagraphEmbeddedObjectEditor, self ).__init__( worksheet, model )
		self._editorModel = WSEditor.RichTextEditor.WorksheetRichTextEditor.instance.editorModelParagraphEmbed( model )


	@staticmethod
	def newParagraphEmbeddedObject(value):
		m = ParagraphEmbeddedObjectEditor.newParagraphEmbeddedObjectModel( value )
		return ParagraphEmbeddedObjectEditor( None, m )

	@staticmethod
	def newParagraphEmbeddedObjectModel(value):
		embeddedValue = DMNode.embedIsolated( value )
		return Schema.ParagraphEmbeddedObject( embeddedValue=embeddedValue )

