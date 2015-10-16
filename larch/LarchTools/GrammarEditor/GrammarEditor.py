##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************

"""
Python visual regular expression

Inspired by the visual regular expression presentation system in SWYN (Say What You Need), by Alan Blackwell.
"""

import re

from java.awt import Color

from BritefuryJ.Command import Command

from BritefuryJ.DocModel import DMNode

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Pres.Primitive import Row, HiddenText

from LarchCore.Languages.Python2.PythonCommands import pythonCommandSet, chainActions, EmbeddedStatementAtCaretAction
from LarchCore.Languages.Python2 import Schema as Py

from LarchTools.GrammarEditor import Schema
# from LarchTools.GrammarEditor.Parser import VisualRegexGrammar
from LarchTools.GrammarEditor.View import perspective as GrammarPerspective
# from LarchTools.GrammarEditor.CodeGenerator import VisualRegexCodeGenerator


_grammarBorder = SolidBorder( 2.0, 4.0, 10.0, 10.0, Color( 0.5, 0.4, 0.7 ), None )


class GrammarEditor (object):
	# _codeGen = VisualRegexCodeGenerator()


	def __init__(self, grammar_def=None):
		if grammar_def is None:
			grammar_def = Schema.GrammarDefinition(rules=[])

		if isinstance( grammar_def, DMNode ):
			if not grammar_def.isInstanceOf( Schema.GrammarDefinition ):
				raise TypeError, 'Wrong schema'

			self.grammar = grammar_def
		else:
			raise TypeError, 'Invalid regular expression type'


	def __clipboard_copy__(self, memo):
		return GrammarEditor(memo.copy(self.grammar))


	__embed_hide_frame__ = True


	def __present__(self, fragment, inherited_state):
		e = Row( [ HiddenText( u'\ue000' ), GrammarPerspective( self.grammar ), HiddenText( u'\ue000' ) ] )
		e = _grammarBorder.surround( e )
		return e



@EmbeddedStatementAtCaretAction
def _newGrammarEditorAtCaret(caret):
	return GrammarEditor()



_gramCommand = Command( '&Grammar &Editor', _newGrammarEditorAtCaret )

pythonCommandSet( 'LarchTools.GrammarEditor.GrammarEditor', [ _gramCommand ] )

