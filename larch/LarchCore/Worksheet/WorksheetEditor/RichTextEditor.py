##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from copy import copy, deepcopy

from BritefuryJ.DocModel import DMObject

from BritefuryJ.Editor.RichText import RichTextEditor

from Britefury.Util.Abstract import abstractmethod

from LarchCore.Worksheet.WorksheetEditor import EditorSchema
from LarchCore.Worksheet import AbstractViewSchema
from LarchCore.Worksheet import Schema


class WorksheetRichTextEditor (RichTextEditor):
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
		if isinstance( value, EditorSchema.LinkEditor )  or  isinstance( value, EditorSchema.InlineEmbeddedObjectEditor ):
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


	def buildParagraph(self, contents, styleAttrs):
		styleAttrs = dict( styleAttrs )
		style = styleAttrs.get( 'style', 'normal' )
		return EditorSchema.ParagraphEditor.newParagraph( contents, style )

	def buildSpan(self, contents, styleAttrs):
		return EditorSchema.TextSpanEditor.newTextSpan( contents, dict( styleAttrs ) )



	def isDataModelObject(self, x):
		return isinstance( x, AbstractViewSchema.NodeAbstractView )

	def insertParagraphIntoBlockAfter(self, block, paragraph, p):
		block.insertEditorNodeAfter( paragraph, p )

	def deleteParagraphFromBlock(self, block, paragraph):
		block.deleteEditorNode( paragraph )



WorksheetRichTextEditor.instance = WorksheetRichTextEditor( 'WsEdit' )

