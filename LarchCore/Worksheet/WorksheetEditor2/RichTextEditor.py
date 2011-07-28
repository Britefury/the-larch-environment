##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.Editor.RichText import RichTextEditor

from Britefury.Util.Abstract import abstractmethod

from LarchCore.Worksheet.WorksheetEditor2 import EditorSchema
from LarchCore.Worksheet import AbstractViewSchema

class WorksheetRichTextEditor (RichTextEditor):
	def getName(self):
		return 'WsEdit'


	@abstractmethod
	def setModelContents(self, model, contents):
		pass

	@abstractmethod
	def modelToEditorModel(self, model):
		pass



	@abstractmethod
	def buildInlineEmbed(self, value):
		pass

	@abstractmethod
	def buildParagraphEmbed(self, value):
		pass


	def buildParagraph(self, contents, styleAttrs):
		styleAttrs = dict( styleAttrs )
		style = styleAttrs.get( 'style', 'normal' )
		return EditorSchema.ParagraphEditor.newParagraph( contents, style )

	def buildSpan(self, contents, styleAttrs):
		return EditorSchema.TextSpanEditor.newTextSpan( contents, dict( styleAttrs ) )



	def isDataModelObject(self, x):
		return isinstance( x, AbstractViewSchema.NodeAbstractView )

	def insertParagraphIntoBlockAfter(self, block, paragraph, p):
		block.insertEditorAfter( paragraph, p )

	@abstractmethod
	def deleteParagraphFromBlock(self, block, paragraph):
		pass

	@abstractmethod
	def removeInlineEmbed(self, model, embed):
		pass

	@abstractmethod
	def deepCopyInlineEmbedValue(self, value):
		pass



WorksheetRichTextEditor.instance = WorksheetRichTextEditor()

