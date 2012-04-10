##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.Command import Command, CommandSet

from LarchCore.Languages.Python25.PythonCommands import PythonCommandSet, EmbeddedExpressionAtCaretAction

from LarchTools.EmbeddedData.IntEditorSpinEntry import IntEditorSpinEntry
from LarchTools.EmbeddedData.RealEditorSpinEntry import RealEditorSpinEntry
from LarchTools.EmbeddedData.StringEditorTextEntry import StringEditorTextEntry
from LarchTools.EmbeddedData.StringEditorTextArea import StringEditorTextArea
from LarchTools.EmbeddedData.ColourEditorPicker import ColourEditorPicker



@EmbeddedExpressionAtCaretAction
def _newIntSpinEntryAtCaret(caret):
	return IntEditorSpinEntry()

_intSpinCommand = Command( '&Integer &Spin &Entry', _newIntSpinEntryAtCaret )


@EmbeddedExpressionAtCaretAction
def _newRealSpinEntryAtCaret(caret):
	return RealEditorSpinEntry()

_realSpinCommand = Command( '&Real &Spin &Entry', _newRealSpinEntryAtCaret )


@EmbeddedExpressionAtCaretAction
def _newTextEntryAtCaret(caret):
	return StringEditorTextEntry()

_textEntryCommand = Command( '&Text &Entry', _newTextEntryAtCaret )


@EmbeddedExpressionAtCaretAction
def _newTextAreaAtCaret(caret):
	return StringEditorTextArea()

_textAreaCommand = Command( '&Text &Area', _newTextAreaAtCaret )


@EmbeddedExpressionAtCaretAction
def _newColourPickerAtcaret(caret):
	return ColourEditorPicker()

_colourPickerCommand = Command( '&C&o&lour picker', _newColourPickerAtcaret )


PythonCommandSet( 'LarchTools.EmbeddedData', [ _intSpinCommand, _realSpinCommand, _textEntryCommand, _textAreaCommand,
                                                       _colourPickerCommand] )
