##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import re
from copy import copy

from Britefury.DocModel.DMSymbol import DMSymbol
from Britefury.DocModel.DMString import DMString
from Britefury.DocModel.DMInterpreter import DMInterpreterEnv



class TokenDefinition (object):
	def __init__(self, tokenClass, regex, replacements=[]):
		super( TokenDefinition, self ).__init__()
		self._tokenClass = intern( tokenClass )
		self._regex = re.compile( regex )
		self._replacements = replacements


	def performReplacements(self, text):
		for rep in self._replacements:
			text = text.replace( rep[0], rep[1] )
		return text



class Token (object):
	def __init__(self, tokenClass, text):
		super( Token, self ).__init__()
		self.text = text
		self.tokenClass = tokenClass


	def __cmp__(self, x):
		return cmp( ( self.tokenClass, self.text ),  ( x.tokenClass, x.text ) )



class TokenDefinitionList (object):
	def __init__(self):
		self._tokenDefs = []


	def defineToken(self, tokenDef):
		self._tokenDefs.append( tokenDef )


	def __add__(self, x):
		r = TokenDefinitionList()
		r._tokenDefs = copy( self._tokenDefs )
		for t in x._tokenDefs:
			if t not in r._tokenDefs:
				r._tokenDefs.append( t )
		return r




class Tokeniser (object):
	def __init__(self, defList):
		super( Tokeniser, self ).__init__()
		self._tokenDefs = copy( defList._tokenDefs )




	def tokenise(self, text):
		tokens = []

		while len( text ) > 0:
			bestLen = 0
			bestText = ''
			bestTokenDef = None

			for td in self._tokenDefs:
				match = td._regex.match( text )
				if match is not None:
					span = match.span( 0 )
					if span[0] == 0:
						if span[1] > bestLen:
							bestLen = span[1]
							bestText = text[:bestLen]
							bestTokenDef = td

			if bestTokenDef is not None:
				tokens.append( Token( bestTokenDef._tokenClass, bestTokenDef.performReplacements( bestText ) ) )
				text = text[bestLen:]
			else:
				tokens.append( Token( intern( '' ), text ) )
				text = ''

		return tokens





def _tokenDefList(env, *tokenDefs):
	def _tokenDef(env, tokenClassName, regex):
		if not isinstance( tokenClassName, DMSymbol ):
			raise TypeError, 'token definition token class name must be a symbol, it is a %s' % ( type( tokenClassName ), )
		if not isinstance( regex, DMString ):
			raise TypeError, 'token definition regex must be a string, it is a %s' % ( type( regex ), )
		td = TokenDefinition( tokenClassName.name, regex.value )
		defList.defineToken( td )


	defList = TokenDefinitionList()

	env = env.funcs( [ _tokenDef ] )

	for t in tokenDefs:
		env.dmEval( t )

	return defList



def _tokeniser(env, *tokenDefLists):
	finalDefList = TokenDefinitionList()

	for x in tokenDefLists:
		finalDefList = finalDefList + env.dmEval( x )

	return Tokeniser( finalDefList )





def dmInterpRegisterTokeniser(env):
	return env.funcs( [ _tokenDefList, _tokeniser ] )





import unittest
from Britefury.Util import RegExpStrings
from Britefury.DocModel.DMIO import readSX



class TestCase_Tokeniser (unittest.TestCase):
	def testTokenise(self):
		toks = TokenDefinitionList()
		toks.defineToken( TokenDefinition( 'i', RegExpStrings.identifier ) )
		toks.defineToken( TokenDefinition( 'w', '[ ]*' ) )
		toks.defineToken( TokenDefinition( 'o', '\(' ) )
		toks.defineToken( TokenDefinition( 'c', '\)' ) )

		tokeniser = Tokeniser( toks )
		tokens = tokeniser.tokenise( '(((a b cpq) x) y)' )

		tokens = [ ( t.tokenClass, t.text )   for t in tokens ]

		self.assert_( tokens == [ ( 'o', '(' ),  ( 'o', '(' ),  ( 'o', '(' ),  ( 'i', 'a' ),  ( 'w', ' ' ),  ( 'i', 'b' ),  ( 'w', ' ' ),  ( 'i', 'cpq' ),  ( 'c', ')' ),  ( 'w', ' ' ),  ( 'i', 'x' ),  ( 'c', ')' ),  ( 'w', ' ' ),  ( 'i', 'y' ),  ( 'c', ')' ) ] )


	def testTokeniserProgram(self):
		programSource = \
		"""
		(tokeniser
			(tokenDefList
				(tokenDef i '[a-zA-Z_][a-zA-Z0-9_]*')
				(tokenDef w '[ ]*')
			)
			(tokenDefList
				(tokenDef o '\(')
				(tokenDef c '\)')
			)
		)
		"""

		program = readSX( programSource )

		env = DMInterpreterEnv()
		env = dmInterpRegisterTokeniser( env )

		tokeniser = env.dmEval( program )
		tokens = tokeniser.tokenise( '(((a b cpq) x) y)' )

		tokens = [ ( t.tokenClass, t.text )   for t in tokens ]

		self.assert_( tokens == [ ( 'o', '(' ),  ( 'o', '(' ),  ( 'o', '(' ),  ( 'i', 'a' ),  ( 'w', ' ' ),  ( 'i', 'b' ),  ( 'w', ' ' ),  ( 'i', 'cpq' ),  ( 'c', ')' ),  ( 'w', ' ' ),  ( 'i', 'x' ),  ( 'c', ')' ),  ( 'w', ' ' ),  ( 'i', 'y' ),  ( 'c', ')' ) ] )



if __name__ == '__main__':
	unittest.main()
