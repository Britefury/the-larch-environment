##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.Command import CommandSet, CommandSetRegistry

from BritefuryJ.LSpace.TextFocus import TextSelection

from LarchCore.Worksheet.WorksheetEditor.RichTextEditor import WorksheetRichTextEditor
from LarchCore.Worksheet.WorksheetEditor import EditorSchema


def _isValidInsertPoint(marker):
	editor = WorksheetRichTextEditor.getEditorForElement( marker.element )
	return editor is WorksheetRichTextEditor.instance


def InlineEmbeddedObjectAtCaretAction(valueAtCaretFactory):
	"""
	valueAtCaretFactory - function( caret )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	def _action(context):
		element = context
		rootElement = element.getRootElement()

		caret = rootElement.getCaret()
		if caret.isValid()  and  caret.isEditable()  and  _isValidInsertPoint( caret.marker ):
			value = valueAtCaretFactory( caret )
			if value is not None:
				def _makeInline():
					return EditorSchema.InlineEmbeddedObjectEditor.newInlineEmbeddedObjectModel( value )
				WorksheetRichTextEditor.instance.insertInlineEmbedAtMarker( caret.marker, _makeInline )
				return True

		return False

	return _action


def ParagraphEmbeddedObjectAtCaretAction(valueAtCaretFactory):
	"""
	valueAtCaretFactory - function( caret )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	def _action(context):
		element = context
		rootElement = element.getRootElement()

		caret = rootElement.getCaret()
		if caret.isValid()  and  caret.isEditable()  and  _isValidInsertPoint( caret.marker ):
			value = valueAtCaretFactory( caret )
			if value is not None:
				def _makeParagraph():
					return EditorSchema.ParagraphEmbeddedObjectEditor.newParagraphEmbeddedObject( value )
				WorksheetRichTextEditor.instance.insertParagraphAtMarker( caret.marker, _makeParagraph )
				return True

		return False

	return _action




def chainActions(*actions):
	def _action(context):
		for action in actions:
			if action( context ):
				return True
		return False
	return _action



worksheetCommands = CommandSetRegistry( 'LarchCore.Worksheet' )



class WorksheetCommandSet (CommandSet):
	def __init__(self, name, commands):
		super( WorksheetCommandSet, self ).__init__( name, commands )
		worksheetCommands.registerCommandSet( self )
