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
from LarchTools.GrammarEditor.Parser import GrammarEditorGrammar
from LarchTools.GrammarEditor.View import perspective as GrammarPerspective
# from LarchTools.GrammarEditor.CodeGenerator import VisualRegexCodeGenerator


_grammarBorder = SolidBorder( 2.0, 4.0, 10.0, 10.0, Color( 0.5, 0.4, 0.7 ), None )


class GrammarEditor (object):
	def __init__(self, grammar_def=None):
		if grammar_def is None:
			grammar_def = Schema.GrammarDefinition(rules=[])

		if isinstance( grammar_def, DMNode ):
			if not grammar_def.isInstanceOf( Schema.GrammarDefinition ):
				raise TypeError, 'Wrong schema'

			self.grammar = grammar_def
		else:
			raise TypeError, 'Invalid grammar definition type'


	def __clipboard_copy__(self, memo):
		return GrammarEditor(memo.copy(self.grammar))


	__embed_hide_frame__ = True


	def __present__(self, fragment, inherited_state):
		e = Row( [ HiddenText( u'\ue000' ), GrammarPerspective( self.grammar ), HiddenText( u'\ue000' ) ] )
		e = _grammarBorder.surround( e )
		return e



class GrammarExpressionEditor (object):
	def __init__(self, grammar_expr=None):
		if grammar_expr is None:
			grammar_expr = Schema.GrammarExpression(expr=None)

		if isinstance( grammar_expr, DMNode ):
			if not grammar_expr.isInstanceOf( Schema.GrammarExpression ):
				raise TypeError, 'Wrong schema'

			self.grammar_expr = grammar_expr
		else:
			raise TypeError, 'Invalid grammar expression type'


	def parser_expression(self, context):
		return context.expr_evaluator(self.grammar_expr)


	def __clipboard_copy__(self, memo):
		return GrammarExpressionEditor(memo.copy(self.grammar_expr))

	@classmethod
	def __import_from_plain_text__(cls, importData):
		return cls.fromText( importData )

	__embed_hide_frame__ = True


	def __present__(self, fragment, inherited_state):
		e = Row( [ HiddenText( u'\ue000' ), GrammarPerspective( self.grammar_expr ), HiddenText( u'\ue000' ) ] )
		e = _grammarBorder.surround( e )
		return e

	@staticmethod
	def fromText(text):
		_grammar = GrammarEditorGrammar()
		parseResult = _grammar.expression().parseStringChars( text )
		if parseResult.isValid():
			return GrammarExpressionEditor( Schema.GrammarExpression( expr=parseResult.getValue() ) )
		else:
			return GrammarExpressionEditor( Schema.GrammarExpression( expr=Schema.UNPARSED( value=[ text ] ) ) )


@EmbeddedStatementAtCaretAction
def _newGrammarEditorAtCaret(caret):
	return GrammarEditor()



_gramCommand = Command( '&Grammar &Editor', _newGrammarEditorAtCaret )

pythonCommandSet( 'LarchTools.GrammarEditor.GrammarEditor', [ _gramCommand ] )

