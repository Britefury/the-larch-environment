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
	def getName(self):
		return 'WsEdit'


	def setModelContents(self, model, contents):
		model.setContents( contents )

	def modelToEditorModel(self, model):
		return model._editorModel



	@abstractmethod
	def buildInlineEmbed(self, value):
		pass

	def buildParagraphEmbed(self, value):
		assert isinstance( value, DMObject )
		if value.isInstanceOf( Schema.PythonCode ):
			return EditorSchema.PythonCodeEditor( None, value )
		elif value.isInstanceOf( Schema.QuoteLocation ):
			return EditorSchema.QuoteLocationEditor( None, value )
		else:
			raise TypeError, 'cannot create paragraph embed for %s'  %  value.getDMClass().getName()


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

	@abstractmethod
	def removeInlineEmbed(self, model, embed):
		pass

	def deepCopyInlineEmbedValue(self, value):
		return deepcopy( value )

	def deepCopyParagraphEmbedValue(self, value):
		return deepcopy( value )



WorksheetRichTextEditor.instance = WorksheetRichTextEditor()

