##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.Command import Command, CommandSet

from LarchCore.Languages.Python25.PythonCommands import pythonCommands, makeInsertEmbeddedExpressionAtCaretAction, makeWrapSelectionInEmbeddedExpressionAction,	\
	makeWrapSelectedStatementRangeInEmbeddedObjectAction, makeInsertEmbeddedStatementAtCaretAction, chainActions

from LarchTools.EmbeddedData.IntEditorSpinEntry import IntEditorSpinEntry
from LarchTools.EmbeddedData.RealEditorSpinEntry import RealEditorSpinEntry
from LarchTools.EmbeddedData.StringEditorTextEntry import StringEditorTextEntry
from LarchTools.EmbeddedData.StringEditorTextArea import StringEditorTextArea



def _newIntSpinEntryAtCaret(caret):
	return IntEditorSpinEntry()

_intSpinAtCaret = makeInsertEmbeddedExpressionAtCaretAction( _newIntSpinEntryAtCaret )
_intSpinCommand = Command( '&Integer &Spin &Entry', _intSpinAtCaret )


def _newRealSpinEntryAtCaret(caret):
	return RealEditorSpinEntry()

_realSpinAtCaret = makeInsertEmbeddedExpressionAtCaretAction( _newRealSpinEntryAtCaret )
_realSpinCommand = Command( '&Real &Spin &Entry', _realSpinAtCaret )


def _newTextEntryAtCaret(caret):
	return StringEditorTextEntry()

_textEntryAtCaret = makeInsertEmbeddedExpressionAtCaretAction( _newTextEntryAtCaret )
_textEntryCommand = Command( '&Text &Entry', _textEntryAtCaret )


def _newTextAreaAtCaret(caret):
	return StringEditorTextArea()

_textAreaAtCaret = makeInsertEmbeddedExpressionAtCaretAction( _newTextAreaAtCaret )
_textAreaCommand = Command( '&Text &Area', _textAreaAtCaret )


_edCommands = CommandSet( 'LarchTools.EmbeddedData', [ _intSpinCommand, _realSpinCommand, _textEntryCommand, _textAreaCommand ] )

pythonCommands.registerCommandSet( _edCommands )