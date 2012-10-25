##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from copy import deepcopy

from BritefuryJ.DocModel import DMNode

from BritefuryJ.LSpace.Clipboard import *
from BritefuryJ.LSpace.TextFocus import TextSelection
from BritefuryJ.LSpace.StyleParams import *
from BritefuryJ.LSpace import *

from BritefuryJ.Editor.Sequential import SequentialClipboardHandler, SelectionEditTreeEvent

from BritefuryJ.Editor.SyntaxRecognizing import SyntaxRecognizingController


from Britefury.SequentialEditor.Helpers import *


from LarchTools.PythonTools.VisualRegex import Schema, Parser





def vreReplaceNode(data, replacement):
	data.become( replacement )


def _isValidUnparsedValue(value):
	return True

def _commitUnparsed(model, value):
	unparsed = Schema.UNPARSED( value=value.getItemValues() )
	# In some cases, we will be replacing @model with an UNPARSED node that contains a reference to @model.
	# Since vreReplaceNode calls model.become(), this causes severe problems, due to circular references.
	# The call to deepcopy eliminates this possibility.
	vreReplaceNode( model, deepcopy( unparsed ) )

def _commitInnerUnparsed(model, value):
	unparsed = Schema.UNPARSED( value=value.getItemValues() )
	# In some cases, we will be replacing @model with an UNPARSED node that contains a reference to @model.
	# Since vreReplaceNode calls model.become(), this causes severe problems, due to circular references.
	# The call to deepcopy eliminates this possibility.
	vreReplaceNode( model, deepcopy( unparsed ) )


def _isValidExprOrTargetOuterUnparsed(value):
	return '\n' not in value


def _commitExprOuterValid(model, parsed):
	expr = model['expr']
	if parsed != expr:
		model['expr'] = parsed

def _commitExprOuterEmpty(model, parsed):
	model['expr'] = Schema.UNPARSED( value=[ '' ] )

def _commitExprOuterUnparsed(model, value):
	values = value.getItemValues()
	if values == []:
		values = [ '' ]
	model['expr'] = Schema.UNPARSED( value=values )









class VisualRegexSyntaxRecognizingController (SyntaxRecognizingController):
	def __init__(self, name='VREEdit'):
		super( VisualRegexSyntaxRecognizingController, self ).__init__( name )

		self._grammar = Parser.VisualRegexGrammar()


		self._expr = self.parsingEditFilter( 'Expression', self._grammar.regex(), vreReplaceNode )
		self._exprOuterValid = self.parsingEditFilter( 'Expression-outer-valid', self._grammar.regex(), _commitExprOuterValid, _commitExprOuterEmpty )
		self._exprOuterInvalid = self.unparsedEditFilter( 'Expression-outer-invalid', _isValidExprOrTargetOuterUnparsed, _commitExprOuterUnparsed )
		self._topLevel = self.topLevelEditFilter()

		self._exprUnparsed = self.unparsedEditFilter( 'Unparsed expression', _isValidUnparsedValue, _commitUnparsed, _commitInnerUnparsed )


		self._expressionEditRule = self.editRule( [ self._expr, self._exprUnparsed ] )
		self._unparsedEditRule = self.editRule( [ self._expr ] )
		self._expressionTopLevelEditRule = self.softStructuralEditRule( [ self._exprOuterValid, self._exprOuterInvalid, self._topLevel ] )




	expression = EditRuleDecorator( lambda self: self._expressionEditRule )
	unparsed = EditRuleDecorator( lambda self: self._unparsedEditRule )
	expressionTopLevel = EditRuleDecorator( lambda self: self._expressionTopLevelEditRule )



	def isClipboardEditLevelFragmentView(self, fragment):
		model = fragment.model
		return isinstance( model, DMNode )  and  model.isInstanceOf( Schema.PythonRegEx )






VisualRegexSyntaxRecognizingController.instance = VisualRegexSyntaxRecognizingController()

