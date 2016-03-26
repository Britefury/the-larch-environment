##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from BritefuryJ.DocModel import DMObject

from BritefuryJ.Editor.RichText import RichTextController

from LarchCore.Worksheet.WorksheetEditor import EditorSchema
from LarchCore.Worksheet import AbstractViewSchema


class WorksheetRichTextController (RichTextController):
	def setModelContents(self, model, contents):
		model.setContents( contents )

	def modelToEditorModel(self, model):
		return model._editorModel



	def buildInlineEmbed(self, value):
#		assert isinstance( value, DMObject )
#		if value.isInstanceOf( Schema.Link ):
#			return EditorSchema.LinkEditor.newLinkForModel( value )
#		elif value.isInstanceOf( Schema.InlineEmbeddedObject ):
#			return EditorSchema.InlineEmbeddedObjectEditor.newInlineEmbeddedObject( value['embeddedValue'].getValue() )
#		else:
#			raise TypeError, 'Unknown inline embed type {0}'.format( value.getDMClass().getName() )
		assert not isinstance( value, DMObject )
		if isinstance( value, EditorSchema.InlinePythonCodeEditor )  or  isinstance( value, EditorSchema.LinkEditor )  or  isinstance( value, EditorSchema.InlineEmbeddedObjectEditor ):
			return value.copy()
		else:
			raise TypeError, 'Unknown inline embed proxy type {0}'.format( type( value ) )

	def buildParagraphEmbed(self, value):
#		assert isinstance( value, DMObject )
#		if value.isInstanceOf( Schema.PythonCode ):
#			return EditorSchema.PythonCodeEditor( None, value )
#		elif value.isInstanceOf( Schema.ParagraphEmbeddedObject ):
#			return EditorSchema.ParagraphEmbeddedObjectEditor( None, value )
#		else:
#			raise TypeError, 'cannot create paragraph embed for %s'  %  value.getDMClass().getName()
		assert not isinstance( value, DMObject )
		if isinstance( value, EditorSchema.PythonCodeEditor )  or  isinstance( value, EditorSchema.ParagraphEmbeddedObjectEditor ):
			return value.copy()
		else:
			raise TypeError, 'Unknown paragraph embed proxy type {0}'.format( type( value ) )


	def buildParagraph(self, contents, paraAttrs):
		paraAttrs = {k: paraAttrs.getValue(k, 0)   for k in paraAttrs.keySet()}
		style = paraAttrs.get( 'style', 'normal' )
		return EditorSchema.ParagraphEditor.newParagraph( contents, style )

	def buildSpan(self, contents, spanAttrs):
		styleAttrs = {k: spanAttrs.getValue(k, 0)   for k in spanAttrs.keySet()}
		return EditorSchema.TextSpanEditor.newTextSpan( contents, styleAttrs )



	def isDataModelObject(self, x):
		return isinstance( x, AbstractViewSchema.NodeAbstractView )

	def insertParagraphIntoBlockAfter(self, block, paragraph, p):
		block.insertEditorNodeAfter( paragraph, p )

	def deleteParagraphFromBlock(self, block, paragraph):
		block.deleteEditorNode( paragraph )



WorksheetRichTextController.instance = WorksheetRichTextController( 'WsEdit' )

