##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pyparsing




class DocViewToken (object):
	def __init__(self, tokenClassName, text):
		self.tokenClassName = tokenClassName
		self.text = text


	def __repr__(self):
		return 'DocViewToken( %s, %s )'  %  ( self.tokenClassName, self.text )



class DocViewTokenDefinition (object):
	def __init__(self, tokenClassName, parser):
		self._tokenClassName = tokenClassName
		self._parser = parser.setParseAction( self._p_parseAction )


	def _p_parseAction(self, tokens):
		return DocViewToken( self._tokenClassName, tokens[0] )




class DocViewTokeniser (object):
	def __init__(self, tokenDefinitions=None):
		if tokenDefinitions is not None  and  len( tokenDefinitions ) > 0:
			toks = tokenDefinitions[0]._parser
			for tok in tokenDefinitions[1:]:
				toks = toks | tok._parser
			self._parser = pyparsing.ZeroOrMore( toks )
			self._parser.leaveWhitespace()
		else:
			self._parser = None


	def tokenise(self, text):
		if self._parser is not None:
			return self._parser.parseString( text ).asList()
		else:
			return [ DocViewToken( '', text ) ]

