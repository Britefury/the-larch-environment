##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Parser.Parser import getErrorLine, parserCoerce, Bind, Action, Condition, Forward, Group, Production, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot, ParserTestCase
from Britefury.Parser.GrammarUtils.Tokens import identifier



def _separatedListAction(input, begin, xs):
	if xs is None:
		return []
	else:
		return [ xs[0] ]  +  [ x[1]   for x in xs[1] ]

def _separatedListActionOneOrMore(input, begin, xs):
	return [ xs[0] ]  +  [ x[1]  for x in xs[1] ]

def separatedList(subexp, separator=',', bNeedAtLeastOne=False, bAllowTrailingSeparator=False, bRequireTrailingSeparatorForLengthOne=False):
	"""
	Separated list
	
	separatedList(subexpression, separator=',', bOneOrMore=False)  ->  ParserExpression
	
	Creates a parser expression that will match a separated list.
	  subexpression - a parser expression that defines the elements of the list
	  separator - a parser expression the defines the separator that separates list entries
	  bNeedAtLeastOne - if True, then will only match with a minimum of one sub-expression
	  bAllowTrailingSepartor - if True, will consume a trailing separator
	  bRequireTrailingSeparatorForLengthOne - if True, will require a trailing separator for a list of length 1
	
	For example:
	  separatedList( identifier, ',' )
	
	will construct a parser expression that will parse:
	   a, b, c, d
	to:
	   [ 'a', 'b', 'c', 'd' ]
	"""
	sep = parserCoerce( separator )
	
	if bRequireTrailingSeparatorForLengthOne:
		afterOne = OneOrMore( sep  +  subexp )
		if bAllowTrailingSeparator:
			afterOne = afterOne - Suppress( Optional( sep ) )
		p = subexp +  ( afterOne  |  sep.action( lambda input, pos, xs: [] ) )
	else:
		p = subexp +  ZeroOrMore( sep  +  subexp )
		if bAllowTrailingSeparator:
			p = p + Suppress( Optional( sep ) )
		
		
	if bNeedAtLeastOne:
		return p.action( _separatedListActionOneOrMore )
	else:
		return Optional( p ).action( _separatedListAction )

	
	
	
def delimitedSeparatedList(subexp, beginDelim, endDelim, separator=',', bNeedAtLeastOne=False, bAllowTrailingSeparator=False, bRequireTrailingSeparatorForLengthOne=False):
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
	return ( parserCoerce( beginDelim )  +  separatedList( subexp, separator, bNeedAtLeastOne, bAllowTrailingSeparator, bRequireTrailingSeparatorForLengthOne )  +  parserCoerce( endDelim ) ).action( lambda input, begin, xs: xs[1] )



class TestCase_SeparatedList (ParserTestCase):
	def testSeparatedList(self):
		parser0 = separatedList( identifier )
		parser1 = separatedList( identifier, bNeedAtLeastOne=True )
		parser0T = separatedList( identifier, bAllowTrailingSeparator=True )
		parser1T = separatedList( identifier, bNeedAtLeastOne=True, bAllowTrailingSeparator=True )
		parser0R = separatedList( identifier, bRequireTrailingSeparatorForLengthOne=True )
		parser1R = separatedList( identifier, bNeedAtLeastOne=True, bRequireTrailingSeparatorForLengthOne=True )
		parser0TR = separatedList( identifier, bAllowTrailingSeparator=True, bRequireTrailingSeparatorForLengthOne=True )
		parser1TR = separatedList( identifier, bNeedAtLeastOne=True, bAllowTrailingSeparator=True, bRequireTrailingSeparatorForLengthOne=True )

		self._matchTest( parser0, '', [] )
		self._matchFailTest( parser0, ',' )
		self._matchTest( parser0, 'ab', [ 'ab' ] )
		self._matchFailTest( parser0, 'ab,' )
		self._matchTest( parser0, 'ab,cd', [ 'ab', 'cd' ] )
		self._matchFailTest( parser0, 'ab,cd,' )
		self._matchTest( parser0, 'ab,cd,ef', [ 'ab', 'cd', 'ef' ] )
		self._matchFailTest( parser0, 'ab,cd,ef,' )
		
		self._matchFailTest( parser1, '' )
		self._matchFailTest( parser1, ',' )
		self._matchTest( parser1, 'ab', [ 'ab' ] )
		self._matchFailTest( parser1, 'ab,' )
		self._matchTest( parser1, 'ab,cd', [ 'ab', 'cd' ] )
		self._matchFailTest( parser1, 'ab,cd,' )
		self._matchTest( parser1, 'ab,cd,ef', [ 'ab', 'cd', 'ef' ] )
		self._matchFailTest( parser1, 'ab,cd,ef,' )

		self._matchTest( parser0T, '', [] )
		self._matchFailTest( parser0T, ',' )
		self._matchTest( parser0T, 'ab', [ 'ab' ] )
		self._matchTest( parser0T, 'ab,', [ 'ab' ] )
		self._matchTest( parser0T, 'ab,cd', [ 'ab', 'cd' ] )
		self._matchTest( parser0T, 'ab,cd,', [ 'ab', 'cd' ] )
		self._matchTest( parser0T, 'ab,cd,ef', [ 'ab', 'cd', 'ef' ] )
		self._matchTest( parser0T, 'ab,cd,ef,', [ 'ab', 'cd', 'ef' ] )
		
		self._matchFailTest( parser1T, '' )
		self._matchFailTest( parser1T, ',' )
		self._matchTest( parser1T, 'ab', [ 'ab' ] )
		self._matchTest( parser1T, 'ab,', [ 'ab' ] )
		self._matchTest( parser1T, 'ab,cd', [ 'ab', 'cd' ] )
		self._matchTest( parser1T, 'ab,cd,', [ 'ab', 'cd' ] )
		self._matchTest( parser1T, 'ab,cd,ef', [ 'ab', 'cd', 'ef' ] )
		self._matchTest( parser1T, 'ab,cd,ef,', [ 'ab', 'cd', 'ef' ] )
		
		
		self._matchTest( parser0R, '', [] )
		self._matchFailTest( parser0R, ',' )
		self._matchFailTest( parser0R, 'ab' )
		self._matchTest( parser0R, 'ab,', [ 'ab' ] )
		self._matchTest( parser0R, 'ab,cd', [ 'ab', 'cd' ] )
		self._matchFailTest( parser0R, 'ab,cd,' )
		self._matchTest( parser0R, 'ab,cd,ef', [ 'ab', 'cd', 'ef' ] )
		self._matchFailTest( parser0R, 'ab,cd,ef,' )
		
		self._matchFailTest( parser1R, '' )
		self._matchFailTest( parser1R, ',' )
		self._matchFailTest( parser1R, 'ab' )
		self._matchTest( parser1R, 'ab,', [ 'ab' ] )
		self._matchTest( parser1R, 'ab,cd', [ 'ab', 'cd' ] )
		self._matchFailTest( parser1R, 'ab,cd,' )
		self._matchTest( parser1R, 'ab,cd,ef', [ 'ab', 'cd', 'ef' ] )
		self._matchFailTest( parser1R, 'ab,cd,ef,' )

		self._matchTest( parser0TR, '', [] )
		self._matchFailTest( parser0TR, ',' )
		self._matchFailTest( parser0TR, 'ab' )
		self._matchTest( parser0TR, 'ab,', [ 'ab' ] )
		self._matchTest( parser0TR, 'ab,cd', [ 'ab', 'cd' ] )
		self._matchTest( parser0TR, 'ab,cd,', [ 'ab', 'cd' ] )
		self._matchTest( parser0TR, 'ab,cd,ef', [ 'ab', 'cd', 'ef' ] )
		self._matchTest( parser0TR, 'ab,cd,ef,', [ 'ab', 'cd', 'ef' ] )
		
		self._matchFailTest( parser1TR, '' )
		self._matchFailTest( parser1TR, ',' )
		self._matchFailTest( parser1TR, 'ab' )
		self._matchTest( parser1TR, 'ab,', [ 'ab' ] )
		self._matchTest( parser1TR, 'ab,cd', [ 'ab', 'cd' ] )
		self._matchTest( parser1TR, 'ab,cd,', [ 'ab', 'cd' ] )
		self._matchTest( parser1TR, 'ab,cd,ef', [ 'ab', 'cd', 'ef' ] )
		self._matchTest( parser1TR, 'ab,cd,ef,', [ 'ab', 'cd', 'ef' ] )

		
		
	def testDelimitedSeparatedList(self):
		parser = delimitedSeparatedList( identifier, '[', ']' )
		self._matchFailTest( parser, '' )
		self._matchFailTest( parser, 'ab' )
		self._matchTest( parser, '[]', [] )
		self._matchTest( parser, '[ab]', [ 'ab' ] )
		self._matchTest( parser, '[ab,cd]', [ 'ab', 'cd' ] )
		self._matchFailTest( parser, 'ab,cd]' )
		self._matchFailTest( parser, '[ab,cd' )
		
		
		
if __name__ == '__main__':
	parser0R = separatedList( identifier, bRequireTrailingSeparatorForLengthOne=True )
	result, pos, dot = parser0R.debugParseString( 'ab' )
	print dot
	

