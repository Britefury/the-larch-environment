##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.Command import Command, CommandSet, CommandSetRegistry

from LarchTools.GrammarEditor import Schema, Precedence, helpers, Properties, grammar_unit_test
from LarchTools.GrammarEditor.Parser import GrammarEditorGrammar
from LarchTools.GrammarEditor.SRController import GrammarEditorSyntaxRecognizingController


def insertSpecialFormStatementAtMarker(marker, specialForm):
	element = marker.getElement()

	stmtVal = element.findPropertyInAncestors( Properties.StatementProperty.instance )
	grammar_def_val = element.findPropertyInAncestors( Properties.GrammarDefProperty.instance )

	if grammar_def_val is not None  and  grammar_def_val.getElement().getRegion() is element.getRegion():
		grammar_def = grammar_def_val.getValue()
		rules = grammar_def['rules']
		if stmtVal is not None  and  stmtVal.getElement().getRegion() is element.getRegion():
			stmt = stmtVal.getValue()
			index = rules.indexOfById( stmt )

			if index != -1:
				if stmt.isInstanceOf( Schema.BlankLine ):
					rules[index] = specialForm
					return
				else:
					i = marker.getClampedIndexInSubtree( stmtVal.getElement() )
					if i > 0:
						index += 1
					rules.insert( index, specialForm )
					return
		rules.append( specialForm )
	else:
		if grammar_def_val is not None:
			print 'insertSpecialFormStatementAtMarker: Could not find suite'
		else:
			print 'insertSpecialFormStatementAtMarker: Could not find suite; regions did not match'


def _makeInsertSpecialFormAtCaretAction(specialFormAtCaretFactory, insertSpecialFormAtCaretFn):
	"""
	valueAtCaretFactory - function( caret )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	def _action(context, pageController):
		element = context
		rootElement = element.getRootElement()

		caret = rootElement.getCaret()
		if caret.isValid()  and  caret.isEditable():
			specialForm = specialFormAtCaretFactory( caret )
			if specialForm is not None:
				insertSpecialFormAtCaretFn( caret, specialForm )
				return True

		return False

	return _action


def _makeInsertEmbeddedObjectAtCaretAction(valueAtCaretFactory, embedFn, insertSpecialFormAtCaretFn):
	"""
	valueAtCaretFactory - function( caret )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	def _specialFormAtCaret(caret):
		value = valueAtCaretFactory( caret )
		return embedFn( value )   if value is not None   else None

	return _makeInsertSpecialFormAtCaretAction( _specialFormAtCaret, insertSpecialFormAtCaretFn )

def _makeInsertEmbeddedObjectStmtAtCaretAction(valueAtCaretFactory, embedFn):
	"""
	valueAtCaretFactory - function( caret )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	return _makeInsertEmbeddedObjectAtCaretAction(valueAtCaretFactory, embedFn, insertSpecialFormStatementAtCaret)


def insertSpecialFormStatementAtCaret(caret, specialForm):
	return insertSpecialFormStatementAtMarker( caret.getMarker(), specialForm )


def SpecialFormStmtAtCaretAction(specialFormAtCaretFactory):
	"""
	specialFormAtCaretFactory - function( caret )  ->  specialForm
	"""
	return _makeInsertSpecialFormAtCaretAction(specialFormAtCaretFactory, insertSpecialFormStatementAtCaret)



@SpecialFormStmtAtCaretAction
def _new_python_helper(caret):
	return Schema.HelperBlockPy(py=helpers.new_python_helper_suite())

@SpecialFormStmtAtCaretAction
def _newOperatorTableAtCaret(caret):
	from LarchTools.GrammarEditor.operator_table import GrammarOperatorTable
	return Schema.OperatorTable(op_table=GrammarOperatorTable())

@SpecialFormStmtAtCaretAction
def _newGrammarTestTableAtCaret(caret):
	return Schema.UnitTestTable(test_table=grammar_unit_test.GrammarTestTable())

_pythonHelperCommand = Command( '&P&ython &Helper', _new_python_helper )
_op_table_command = Command('&Grammar &Operator &Table', _newOperatorTableAtCaret)
_unitTestCommand = Command('&Grammar &Table &Test', _newGrammarTestTableAtCaret)


grammarCommandSet = CommandSet( 'LarchTools.GrammarEditor.GrammarEditor', [_pythonHelperCommand, _op_table_command, _unitTestCommand] )


grammarCommands = CommandSetRegistry( 'LarchTools.GrammarEditor' )
grammarCommands.registerCommandSet(grammarCommandSet)



