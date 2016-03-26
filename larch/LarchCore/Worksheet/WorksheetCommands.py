##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from BritefuryJ.Command import CommandSet, CommandSetRegistry

from BritefuryJ.LSpace.TextFocus import TextSelection

from LarchCore.Worksheet.WorksheetEditor.RichTextController import WorksheetRichTextController
from LarchCore.Worksheet.WorksheetEditor import EditorSchema


def _isValidInsertPoint(marker):
	editor = WorksheetRichTextController.getEditorForElement( marker.element )
	return editor is WorksheetRichTextController.instance


def InlineEmbeddedObjectAtCaretAction(valueAtCaretFactory):
	"""
	valueAtCaretFactory - function( caret )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	def _action(context, pageController):
		element = context
		rootElement = element.getRootElement()

		caret = rootElement.getCaret()
		if caret.isValid()  and  caret.isEditable()  and  _isValidInsertPoint( caret.marker ):
			value = valueAtCaretFactory( caret )
			if value is not None:
				def _makeInline():
					return EditorSchema.InlineEmbeddedObjectEditor.newInlineEmbeddedObjectModel( value )
				WorksheetRichTextController.instance.insertInlineEmbedAtMarker( caret.marker, _makeInline )
				return True

		return False

	return _action


def ParagraphEmbeddedObjectAtCaretAction(valueAtCaretFactory):
	"""
	valueAtCaretFactory - function( caret )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	def _action(context, pageController):
		element = context
		rootElement = element.getRootElement()

		caret = rootElement.getCaret()
		if caret.isValid()  and  caret.isEditable()  and  _isValidInsertPoint( caret.marker ):
			value = valueAtCaretFactory( caret )
			if value is not None:
				def _makeParagraph():
					return EditorSchema.ParagraphEmbeddedObjectEditor.newParagraphEmbeddedObject( value )
				WorksheetRichTextController.instance.insertParagraphAtMarker( caret.marker, _makeParagraph )
				return True

		return False

	return _action




def chainActions(*actions):
	def _action(context, pageController):
		for action in actions:
			if action( context, pageController ):
				return True
		return False
	return _action



worksheetCommands = CommandSetRegistry( 'LarchCore.Worksheet' )



def worksheetCommandSet(name, commands):
	commandSet = CommandSet( name, commands )
	worksheetCommands.registerCommandSet( commandSet )
	return commandSet
