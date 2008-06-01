##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Parser import Parser

from Britefury.gSym.View.InteractorEvent import InteractorEventTokenList






class TokenDefinition (object):
	def __init__(self, tokenClass, parser):
		self._tokenClass = tokenClass
		self._parser = parser.action( self._p_parseAction )


	def _p_parseAction(self, input, begin, token):
		return InteractorEventTokenList.Token( self._tokenClass, token )




class Tokeniser (object):
	def __init__(self, tokenDefinitions=None):
		if tokenDefinitions is not None  and  len( tokenDefinitions ) > 0:
			toks = tokenDefinitions[0]._parser
			for tok in tokenDefinitions[1:]:
				toks = toks | tok._parser
			self._parser = Parser.ZeroOrMore( toks )
		else:
			self._parser = None


	def tokenise(self, text):
		if self._parser is not None:
			parseResult, end = self._parser.parseString( text, ignoreChars='' )
			if parseResult is not None:
				return parseResult.result
			else:
				return []
		else:
			return [ InteractorEventTokenList.Token( '', text ) ]

		
