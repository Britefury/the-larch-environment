##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
import copy

from BritefuryJ.DocModel import DMNode

from BritefuryJ.Pres.Primitive import Text

from BritefuryJ.LSpace.TextFocus import TextSelection

from BritefuryJ.Editor.Sequential import SequentialClipboardHandler, SelectionEditTreeEvent

from BritefuryJ.Editor.SyntaxRecognizing import SyntaxRecognizingController

from BritefuryJ.Editor.SyntaxRecognizing.Precedence import PrecedenceHandler
from BritefuryJ.Editor.SyntaxRecognizing import SyntaxRecognizingController

from BritefuryJ.ModelAccess.DocModel import ClassAttributeReader, ObjectFieldReader


from LarchTools.GrammarEditor.Precedence import parensRequired, nodePrecedence
from LarchTools.GrammarEditor import Schema, Parser





def gramReplaceNode(data, replacement):
	data.become( replacement )


def _isValidUnparsedValue(value):
	return True

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


def _isValidUnparsedStatementValue(value):
	# Unparsed statement is only valid if there is ONE newline, and it is at the end
	i = value.indexOf( '\n' )
	return i != -1   and   i == len( value ) - 1

def _isValidUnparsedSpecialFormStatementValue(value):
	# Unparsed statement is only valid if there is ONE newline, and it is at the end
	unparseable = False
	for item in value.getItems():
		if not item.isStructural():
			if len(item.getValue().strip()) > 0:
				unparseable = True
				break
	if unparseable:
		return _isValidUnparsedStatementValue(value)
	else:
		return False

def _commitUnparsedStatment(model, value):
	withoutNewline = value[:-1]
	unparsed = Schema.UnparsedStmt( value=Schema.UNPARSED( value=withoutNewline.getItemValues() ) )
	# In some cases, we will be replacing @model with an UNPARSED node that contains a reference to @model.
	# Since pyReplaceNode calls model.become(), this causes severe problems, due to circular references.
	# The call to deepcopy eliminates this possibility.
	gramReplaceNode( model, copy.deepcopy( unparsed ) )

def _commitInnerUnparsed(model, value):
	unparsed = Schema.UNPARSED( value=value.getItemValues() )
	# In some cases, we will be replacing @model with an UNPARSED node that contains a reference to @model.
	# Since pyReplaceNode calls model.become(), this causes severe problems, due to circular references.
	# The call to deepcopy eliminates this possibility.
	gramReplaceNode( model, copy.deepcopy( unparsed ) )



def modifySuiteMinimisingChanges(target, modified):
	commonPrefixLen = 0
	for i, (t, m) in enumerate( zip( target, modified ) ):
		if t != m:
			commonPrefixLen = i
			break

	commonSuffixLen = 0
	for i, (t, m) in enumerate( zip( reversed( target ), reversed( modified ) ) ):
		if t != m:
			commonSuffixLen = i
			break

	minLength = min( len( target ), len( modified ) )
	remaining = minLength - commonPrefixLen
	commonSuffixLen = min( commonSuffixLen, remaining )

	xs = modified
	for i, x in enumerate( modified[commonPrefixLen:len(modified)-commonSuffixLen] ):
		if x in target[:commonPrefixLen]  or  x in target[len(target)-commonSuffixLen:]:
			if xs is modified:
				xs = copy.copy( modified )
			xs[commonPrefixLen+i] = copy.deepcopy( x )

	target[commonPrefixLen:len(target)-commonSuffixLen] = xs[commonPrefixLen:len(modified)-commonSuffixLen]


def _makeSuiteCommitFn(suite):
	def _commit(model, parsed):
		modifySuiteMinimisingChanges( suite, parsed )
	return _commit


openParen = Text( '(' )
closeParen = Text( ')' )


_grammarPrecedenceHandler = PrecedenceHandler( ClassAttributeReader( parensRequired ), ObjectFieldReader( 'parens' ).stringToInteger( -1 ), ClassAttributeReader( nodePrecedence ), openParen, closeParen )



class GrammarEditorSyntaxRecognizingController (SyntaxRecognizingController):
	def __init__(self, name='GrammarEdit'):
		super( GrammarEditorSyntaxRecognizingController, self ).__init__( name )

		self._grammar = Parser.GrammarEditorGrammar()

		self._expr = self.parsingEditFilter( 'Expression', self._grammar.expression(), gramReplaceNode )
		self._stmt = self.parsingEditFilter( 'Statement', self._grammar.single_line_statement_valid(), gramReplaceNode )
		self._stmtUnparsed = self.unparsedEditFilter( 'Unparsed statement', _isValidUnparsedStatementValue, _commitUnparsedStatment, _commitInnerUnparsed )
		self._specialFormStmtUnparsed = self.unparsedEditFilter( 'Unparsed special-form stmt', _isValidUnparsedSpecialFormStatementValue, _commitUnparsedStatment, _commitInnerUnparsed )
		self._topLevel = self.topLevelEditFilter()
		self._exprOuterValid = self.parsingEditFilter( 'Expression-outer-valid', self._grammar.expression(), _commitExprOuterValid, _commitExprOuterEmpty )
		self._exprOuterInvalid = self.unparsedEditFilter( 'Expression-outer-invalid', _isValidExprOrTargetOuterUnparsed, _commitExprOuterUnparsed )

		self.expressionEditRule = self.editRule( _grammarPrecedenceHandler, [ self._expr ] )
		self.structuralExpressionEditRule = self.softStructuralEditRule( _grammarPrecedenceHandler, [ self._expr ] )
		self.unparsedEditRule = self.editRule( [ self._expr ] )
		self.statementEditRule = self.softStructuralEditRule( [ self._stmt, self._stmtUnparsed ] )
		self.unparsedStatementEditRule = self.editRule( [ self._stmt, self._stmtUnparsed ] )
		self.specialFormStatementEditRule = self.softStructuralEditRule( [ self._stmt, self._specialFormStmtUnparsed ] )
		self.expressionTopLevelEditRule = self.softStructuralEditRule( _grammarPrecedenceHandler, [ self._exprOuterValid, self._exprOuterInvalid, self._topLevel ] )



	def isClipboardEditLevelFragmentView(self, fragment):
		model = fragment.model
		return isinstance( model, DMNode )  and  model.isInstanceOf( Schema.GrammarDefinition )



	def _makeGrammarEditFilter(self, suite):
		return self.parsingEditFilter( 'Suite', self._grammar.suite(), _makeSuiteCommitFn( suite ) )



GrammarEditorSyntaxRecognizingController.instance = GrammarEditorSyntaxRecognizingController()


