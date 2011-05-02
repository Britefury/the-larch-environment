##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.Command import CommandSetRegistry

from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import insertSpecialFormAtCaret
from GSymCore.Languages.Python25.Builder import embeddedExpression




def makeInsertEmbeddedExpressionAction(valueFactory):
	def _action(context):
		element = context
		rootElement = element.getRootElement()
		caret = rootElement.getCaret()
		if caret.isValid():
			value = valueFactory()
			specialForm = embeddedExpression( value )
			insertSpecialFormAtCaret( caret, specialForm )
	return _action

			
def makeInsertEmbeddedStatementAction(valueFactory):
	def _action(context):
		element = context
		rootElement = element.getRootElement()
		caret = rootElement.getCaret()
		if caret.isValid():
			value = valueFactory()
			specialForm = embeddedStatement( value )
			insertSpecialFormAtCaret( caret, specialForm )
	return _action

			

pythonCommands = CommandSetRegistry( 'GSymCore.Languages.Python25' )
