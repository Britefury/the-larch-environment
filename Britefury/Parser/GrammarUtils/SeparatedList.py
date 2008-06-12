##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Parser.Parser import getErrorLine, parserCoerce, Bind, Action, Condition, Forward, Group, Production, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot, ParserTestCase
from Britefury.Parser.GrammarUtils.Tokens import identifier



def _separatedListAction(input, begin, tokens):
	if tokens is None:
		return []
	else:
		return [ tokens[0] ]  +  [ x[1]   for x in tokens[1] ]

def _separatedListActionOneOrMore(input, begin, tokens):
	return [ tokens[0] ]  +  [ x[1]  for x in tokens[1] ]

def separatedList(subexp, separator=',', bOneOrMore=False):
	"""
	Separated list
	
	separatedList(subexpression, separator=',', bOneOrMore=False)  ->  ParserExpression
	
	Creates a parser expression that will match a separated list.
	  subexpression - a parser expression that defines the elements of the list
	  separator - a parser expression the defines the separator that separates list entries
	  bOneOrMore - if True, then will only match with a minimum of one sub-expression
	
	For example:
	  separatedList( identifier, ',' )
	
	will construct a parser expression that will parse:
	   a, b, c, d
	to:
	   [ 'a', 'b', 'c', 'd' ]
	"""
	if bOneOrMore:
		return ( subexp +  ZeroOrMore( parserCoerce( separator )  +  subexp ) ).action( _separatedListActionOneOrMore )
	else:
		return Optional( subexp  +  ZeroOrMore( parserCoerce( separator )  +  subexp ) ).action( _separatedListAction )
	
	
	
def delimitedSeparatedList(subexp, beginDelim, endDelim, separator=','):
	"""
	Delimited separated list
	
	delimitedSeparatedList(subexpression, beginDelimiter, endDelimiter, separator=',')  ->  ParserExpression
	
	Creates a parser expression that will match a delimited separated list.
	  subexpression - a parser expression that defines the elements of the list
	  beginDelimiter - a parser expression that defines the delimiter that marks the beginning of the list
	  endDelimiter - a parser expression that defines the delimiter that marks the end of the list
	  separator - a parser expression the defines the separator that separates list entries
	
	For example:
	  delimitedSeparatedList( identifier, '{', '}', ',' )
	
	will construct a parser expression that will parse:
	   { a, b, c, d }
	to:
	   [ 'a', 'b', 'c', 'd' ]
	"""
	return ( Suppress( beginDelim )  +  Optional( subexp  +  ZeroOrMore( parserCoerce( separator )  +  subexp ) ).action( _separatedListAction )  +  Suppress( endDelim ) ).action( lambda input, begin, tokens: tokens[0] )




class TestCase_SeparatedList (ParserTestCase):
	def testSeparatedList(self):
		parser = separatedList( identifier )
		parser2 = separatedList( identifier, bOneOrMore=True )
		self._matchTest( parser, '', [] )
		self._matchTest( parser, 'ab', [ 'ab' ] )
		self._matchTest( parser, 'cd', [ 'cd' ] )
		self._matchTest( parser, 'ab,cd', [ 'ab', 'cd' ] )
		self._matchFailTest( parser2, '' )
		self._matchTest( parser2, 'ab', [ 'ab' ] )
		self._matchTest( parser2, 'cd', [ 'cd' ] )
		self._matchTest( parser2, 'ab,cd', [ 'ab', 'cd' ] )
		
		
	def testDelimitedSeparatedList(self):
		parser = delimitedSeparatedList( identifier, '[', ']' )
		self._matchFailTest( parser, '' )
		self._matchFailTest( parser, 'ab' )
		self._matchTest( parser, '[]', [] )
		self._matchTest( parser, '[ab]', [ 'ab' ] )
		self._matchTest( parser, '[cd]', [ 'cd' ] )
		self._matchTest( parser, '[ab,cd]', [ 'ab', 'cd' ] )
		self._matchFailTest( parser, 'ab,cd]' )
		self._matchFailTest( parser, '[ab,cd' )
		
		

