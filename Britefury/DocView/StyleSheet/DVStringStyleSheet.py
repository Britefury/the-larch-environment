##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pyparsing

from Britefury.DocModel.DMString import DMString

from Britefury.DocView.StyleSheet.DVAtomStyleSheet import DVAtomStyleSheet




class DVStringStyleSheet (DVAtomStyleSheet):
	parser = pyparsing.ZeroOrMore( pyparsing.Word( pyparsing.alphas + '_', pyparsing.alphanums + '_' ) | pyparsing.Literal('(') | pyparsing.Literal(')') | pyparsing.Word( ' \t') )

	def _f_handleTokenList(self, nodeView, tokens, parentDocNode, indexInParent):
		pass

	def _f_handleText(self, nodeView, text, parentDocNode, indexInParent, bUserEvent):
		nodeView._f_commandHistoryFreeze()
		if bUserEvent:
			nodeView.cursorRight()
		if text == '':
			nodeView.deleteNode( MoveFocus.RIGHT )
		else:
			parentDocNode[indexInParent] = self._f_textToNode( text )
		nodeView._f_commandHistoryThaw()


	@abstractmethod
	def _f_textToNode(self, text):
		pass


	def _f_textToNode(self, text):
		return DMString( text )
