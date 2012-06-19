##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.Command import CommandSet, CommandSetRegistry

from BritefuryJ.LSpace.TextFocus import TextSelection

from LarchCore.Languages.Python2.PythonEditor.PythonEditOperations import insertSpecialFormExpressionAtCaret, insertSpecialFormStatementAtCaret, pyReplaceStatementRangeWithStatement, \
	getSelectedExpression, getSelectedStatement, getSelectedStatementRange, pyReplaceNode
from LarchCore.Languages.Python2.Builder import embeddedExpression, embeddedStatement



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


def _makeWrapSelectionInSpecialFormAction(getSelectedNodeFn, specialFormAtSelectionFactory):
	"""
	getSelectedNodeFn - function( selection )  ->  selected_node
	valueAtSelectionFactory - function( selected_node, selection )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	def _action(context, pageController):
		element = context
		rootElement = element.getRootElement()

		selection = rootElement.getSelection()
		if isinstance( selection, TextSelection )  and  selection.isValid()  and  selection.isEditable():
			node = getSelectedNodeFn( selection )
			if node is not None:
				specialForm = specialFormAtSelectionFactory( node, selection )
				if specialForm is not None:
					pyReplaceNode( node, specialForm )
					return True

		return False

	return _action







def SpecialFormExprAtCaretAction(specialFormAtCaretFactory):
	"""
	specialFormAtCaretFactory - function( caret )  ->  specialForm
	"""
	return _makeInsertSpecialFormAtCaretAction(specialFormAtCaretFactory, insertSpecialFormExpressionAtCaret)




def SpecialFormStmtAtCaretAction(specialFormAtCaretFactory):
	"""
	specialFormAtCaretFactory - function( caret )  ->  specialForm
	"""
	return _makeInsertSpecialFormAtCaretAction(specialFormAtCaretFactory, insertSpecialFormStatementAtCaret)




def WrapSelectionInSpecialFormExprAction(specialFormAtSelectionFactory):
	"""
	valueAtSelectionFactory - function( selected_node, selection )  ->  value
	"""
	return _makeWrapSelectionInSpecialFormAction( getSelectedExpression, specialFormAtSelectionFactory )

def WrapSelectionInSpecialFormStmtAction(specialFormAtSelectionFactory):
	"""
	valueAtSelectionFactory - function( selected_node, selection )  ->  value
	"""
	return _makeWrapSelectionInSpecialFormAction( getSelectedStatement, specialFormAtSelectionFactory )




def WrapSelectedStatementRangeInSpecialFormAction(specialFormAtSelectionFactory):
	"""
	valueAtSelectionFactory - function( statements, selection )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	def _action(context, pageController):
		element = context
		rootElement = element.getRootElement()

		selection = rootElement.getSelection()
		if isinstance( selection, TextSelection )  and  selection.isValid()  and  selection.isEditable():
			stmtRange = getSelectedStatementRange( selection )
			if stmtRange is not None:
				suite, i, j = stmtRange
				specialForm = specialFormAtSelectionFactory( list( suite[i:j] ), selection )
				if specialForm is not None:
					pyReplaceStatementRangeWithStatement( suite, i, j, specialForm )
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




def _makeInsertEmbeddedObjectExprAtCaretAction(valueAtCaretFactory, embedFn):
	"""
	valueAtCaretFactory - function( caret )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	return _makeInsertEmbeddedObjectAtCaretAction(valueAtCaretFactory, embedFn, insertSpecialFormExpressionAtCaret)




def _makeInsertEmbeddedObjectStmtAtCaretAction(valueAtCaretFactory, embedFn):
	"""
	valueAtCaretFactory - function( caret )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	return _makeInsertEmbeddedObjectAtCaretAction(valueAtCaretFactory, embedFn, insertSpecialFormStatementAtCaret)




def _makeWrapSelectionInEmbeddedObjectAction(getSelectedNodeFn, valueAtSelectionFactory, embedFn):
	"""
	getSelectedNodeFn - function( selection )  ->  selected_node
	valueAtSelectionFactory - function( selected_node, selection )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	def specialFormAtSelection(node, selection):
		value = valueAtSelectionFactory( node, selection )
		return embedFn( value )   if value is not None   else None

	return _makeWrapSelectionInSpecialFormAction( getSelectedNodeFn, specialFormAtSelection )

			


def EmbeddedExpressionAtCaretAction(valueAtCaretFactory):
	"""
	valueAtCaretFactory - function( caret )  ->  value
	"""
	return _makeInsertEmbeddedObjectExprAtCaretAction( valueAtCaretFactory, embeddedExpression )
			
def EmbeddedStatementAtCaretAction(valueAtCaretFactory):
	"""
	valueAtCaretFactory - function( caret )  ->  value
	"""
	return _makeInsertEmbeddedObjectStmtAtCaretAction( valueAtCaretFactory, embeddedStatement )

			

def WrapSelectionInEmbeddedExpressionAction(valueAtSelectionFactory):
	"""
	valueAtSelectionFactory - function( selected_node, selection )  ->  value
	"""
	return _makeWrapSelectionInEmbeddedObjectAction( getSelectedExpression, valueAtSelectionFactory, embeddedExpression )

def WrapSelectionInEmbeddedStatementAction(valueAtSelectionFactory):
	"""
	valueAtSelectionFactory - function( selected_node, selection )  ->  value
	"""
	return _makeWrapSelectionInEmbeddedObjectAction( getSelectedStatement, valueAtSelectionFactory, embeddedStatement )



def WrapSelectedStatementRangeInEmbeddedObjectAction(valueAtSelectionFactory):
	"""
	valueAtSelectionFactory - function( statements, selection )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	def specialFormAtSelection(statements, selection):
		value = valueAtSelectionFactory( statements, selection )
		return embeddedStatement( value )   if value is not None   else None

	return WrapSelectedStatementRangeInSpecialFormAction( specialFormAtSelection )

			



def chainActions(*actions):
	def _action(context, pageController):
		for action in actions:
			if action( context, pageController ):
				return True
		return False
	return _action



pythonCommands = CommandSetRegistry( 'LarchCore.Languages.Python2' )
pythonTargetCommands = CommandSetRegistry( 'LarchCore.Languages.Python2.Target' )



def pythonCommandSet(name, commands):
	commandSet = CommandSet( name, commands )
	pythonCommands.registerCommandSet( commandSet )
	return commandSet



def pythonTargetCommandSet(name, commands):
	commandSet = CommandSet( name, commands )
	pythonTargetCommands.registerCommandSet( commandSet )
	return commandSet





