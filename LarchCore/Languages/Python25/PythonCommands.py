##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.Command import CommandSetRegistry

from BritefuryJ.DocPresent.Selection import TextSelection

from LarchCore.Languages.Python25.PythonEditor.PythonEditOperations import insertSpecialFormAtCaret, pyReplaceNode, getSelectedExpression, getSelectedStatement
from LarchCore.Languages.Python25.Builder import embeddedExpression, embeddedStatement



def _makeInsertEmbeddedObjectAtCaretAction(valueAtCaretFactory, embedFn):
	"""
	valueAtCaretFactory - function( caret )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	def _action(context):
		element = context
		rootElement = element.getRootElement()
		
		caret = rootElement.getCaret()
		if caret.isValid()  and  caret.isEditable():
			value = valueAtCaretFactory( caret )
			if value is not None:
				specialForm = embedFn( value )
				insertSpecialFormAtCaret( caret, specialForm )
				return True
		
		return False
	
	return _action

			


def _makeWrapSelectionInEmbeddedObjectAction(getSelectedNodeFn, valueAtSelectionFactory, embedFn):
	"""
	getSelectedNodeFn - function( selection )  ->  selected_node
	valueAtSelectionFactory - function( selected_node, selection )  ->  value
	embedFn - function( value )  ->  AST node
	"""
	def _action(context):
		element = context
		rootElement = element.getRootElement()
		
		selection = rootElement.getSelection()
		if isinstance( selection, TextSelection )  and  selection.isValid()  and  selection.isEditable():
			node = getSelectedNodeFn( selection )
			if node is not None:
				value = valueAtSelectionFactory( node, selection )
				if value is not None:
					specialForm = embedFn( value )
					pyReplaceNode( node, specialForm )
					return True
	
		return False
	
	return _action

			


def makeInsertEmbeddedExpressionAtCaretAction(valueAtCaretFactory):
	"""
	valueAtCaretFactory - function( caret )  ->  value
	"""
	return _makeInsertEmbeddedObjectAtCaretAction( valueAtCaretFactory, embeddedExpression )
			
def makeInsertEmbeddedStatementAtCaretAction(valueAtCaretFactory):
	"""
	valueAtCaretFactory - function( caret )  ->  value
	"""
	return _makeInsertEmbeddedObjectAtCaretAction( valueAtCaretFactory, embeddedStatement )

			

def makeWrapSelectionInEmbeddedExpressionAction(valueAtSelectionFactory):
	"""
	valueAtSelectionFactory - function( selected_node, selection )  ->  value
	"""
	return _makeWrapSelectionInEmbeddedObjectAction( getSelectedExpression, valueAtSelectionFactory, embeddedExpression )

def makeWrapSelectionInEmbeddedStatementAction(valueAtSelectionFactory):
	"""
	valueAtSelectionFactory - function( selected_node, selection )  ->  value
	"""
	return _makeWrapSelectionInEmbeddedObjectAction( getSelectedStatement, valueAtSelectionFactory, embeddedExpression )



def chainActions(*actions):
	def _action(context):
		for action in actions:
			if action( context ):
				return True
		return False
	return _action



pythonCommands = CommandSetRegistry( 'LarchCore.Languages.Python25' )
pythonTargetCommands = CommandSetRegistry( 'LarchCore.Languages.Python25.Target' )
