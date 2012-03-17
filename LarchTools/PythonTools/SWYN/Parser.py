##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import re
import string

from copy import deepcopy

from BritefuryJ.DocModel import DMObject, DMNode

from BritefuryJ.Parser import Literal, Keyword, RegEx, Word, SeparatedList, ObjectNode
from BritefuryJ.Parser.Utils import Tokens
from BritefuryJ.Parser.Utils.OperatorParser import PrefixLevel, SuffixLevel, InfixLeftLevel, InfixRightLevel, InfixChainLevel, UnaryOperator, BinaryOperator, ChainOperator, OperatorTable

from Britefury.Tests.BritefuryJ.Parser.ParserTestCase import ParserTestCase

from Britefury.Grammar.Grammar import Grammar, Rule, RuleList

from LarchTools.PythonTools.SWYN import Schema



class SWYNGrammar (Grammar):
	@Rule
	def pythonEscapedChar(self):
		return ( Literal( '\\' ) + RegEx( '[abnfrt]|(x[0-9a-fA-F]{2})|(u[0-9a-fA-F]{4})|(U[0-9a-fA-F]{8})' ) ).action( lambda input, begin, end, x, bindings: Schema.PythonEscapedChar( char=x[1] ) )


	@Rule
	def escapedRegexChar(self):
		return ( Literal( '\\' ) + RegEx( '.' ) ).action( lambda input, begin, end, x, bindings: Schema.EscapedChar( char=x[1] ) )


	@Rule
	def escapedChar(self):
		return self.pythonEscapedChar() | self.escapedRegexChar()


	# Literal Character - escaped or not escaped
	@Rule
	def literalChar(self):
		return self.escapedChar() | RegEx( '[^\\.^$\\[\\\\()|+*?]' ).action( lambda input, begin, end, x, bindings: Schema.LiteralChar( char=x ) )



	# Specials
	@Rule
	def anyChar(self):
		return Literal( '.' ).action( lambda input, begin, end, x, bindings: Schema.AnyChar() )

	@Rule
	def startOfLine(self):
		return Literal( '^' ).action( lambda input, begin, end, x, bindings: Schema.StartOfLine() )

	@Rule
	def endOfLine(self):
		return Literal( '$' ).action( lambda input, begin, end, x, bindings: Schema.EndOfLine() )

	@Rule
	def specials(self):
		return self.anyChar() | self.startOfLine() | self.endOfLine()



	@Rule
	def charClass(self):
		return ( Literal( '\\' ) + RegEx( '[AbBdDsSwWZ]' ) ).action( lambda input, begin, end, x, bindings: Schema.CharClass( cls=x[1] ) )



	# Character set
	@Rule
	def charSetChar(self):
		return self.escapedChar() | RegEx( '[^\\]\\-\\\\]' ).action( lambda input, begin, end, x, bindings: Schema.LiteralChar( char=x ) )

	@Rule
	def charSetItemChar(self):
		return ( self.charClass() | self.charSetChar() ).action( lambda input, begin, end, x, bindings: Schema.CharSetChar( char=x ) )

	@Rule
	def charSetItemRange(self):
		return ( self.charSetChar() + Literal( '-' ) + self.charSetChar() ).action( lambda input, begin, end, x, bindings: Schema.CharSetRange( min=x[0], max=x[2] ) )

	@Rule
	def charSetItem(self):
		return self.charSetItemRange() | self.charSetItemChar()

	@Rule
	def charSet(self):
		return ( Literal( '[' ) + Literal( '^' ).optional() + self.charSetItem().oneOrMore() + Literal( ']' ) ).action( lambda input, begin, end, x, bindings: Schema.CharSet( invert=( '1'   if x[1] is not None   else None ), items=x[2] ) )



	# Item - character with optional repetition
	@Rule
	def character(self):
		return self.specials() | self.charClass() | self.charSet() | self.literalChar()



	# Parentheses
	@Rule
	def group(self):
		return ( Literal( '(' ) + self.choice() + Literal( ')' ) ).action( lambda input, begin, end, x, bindings: Schema.Group( capturing='1', subexp=x[1] ) )

	@Rule
	def nonCapturingGroup(self):
		return ( Literal( '(?:' ) + self.choice() + Literal( ')' ) ).action( lambda input, begin, end, x, bindings: Schema.Group( capturing=None, subexp=x[1] ) )

	@Rule
	def defineNamedGroup(self):
		return ( Literal( '(?P<' ) + Tokens.identifier + Literal( '>' ) + self.choice() + Literal( ')' ) ).action( lambda input, begin, end, x, bindings: Schema.DefineNamedGroup( name=x[1], subexp=x[3] ) )

	@Rule
	def matchNamedGroup(self):
		return ( Literal( '(?P=' ) + Tokens.identifier + Literal( ')' ) ).action( lambda input, begin, end, x, bindings: Schema.MatchNamedGroup( name=x[1] ) )

	@Rule
	def lookahead(self):
		return ( Literal( '(?=' ) + self.choice() + Literal( ')' ) ).action( lambda input, begin, end, x, bindings: Schema.Lookahead( subexp=x[1], positive='1' ) )

	@Rule
	def negativeLookahead(self):
		return ( Literal( '(?!' ) + self.choice() + Literal( ')' ) ).action( lambda input, begin, end, x, bindings: Schema.Lookahead( subexp=x[1] ) )

	@Rule
	def lookbehind(self):
		return ( Literal( '(?<=' ) + self.choice() + Literal( ')' ) ).action( lambda input, begin, end, x, bindings: Schema.Lookbehind( subexp=x[1], positive='1' ) )

	@Rule
	def negativeLookbehind(self):
		return ( Literal( '(?<!' ) + self.choice() + Literal( ')' ) ).action( lambda input, begin, end, x, bindings: Schema.Lookbehind( subexp=x[1] ) )

	@Rule
	def setFlags(self):
		return ( Literal( '(?' ) + RegEx( '[iLmsux]' ) + Literal( ')' ) ).action( lambda input, begin, end, x, bindings: Schema.SetFlags( flags=x[1] ) )

	@Rule
	def comment(self):
		return ( Literal( '(?#' ) + RegEx( '[^)]*' ) + Literal( ')' ) ).action( lambda input, begin, end, x, bindings: Schema.Comment( text=x[1] ) )



	@Rule
	def item(self):
		return self.setFlags() | self.nonCapturingGroup() | self.defineNamedGroup() | self.matchNamedGroup() | self.comment() | \
		       self.lookahead() | self.negativeLookahead() | self.lookbehind() | self.negativeLookbehind() | self.group() | self.character()



	# Repetition
	@Rule
	def zeroOrMore(self):
		return ( self.item() + Literal( '*' ) + Literal( '?' ).optional() ).action( lambda input, begin, end, x, bindings: Schema.ZeroOrMore( subexp=x[0], greedy=( '1'   if x[2] is not None   else None ) ) )

	@Rule
	def oneOrMore(self):
		return ( self.item() + Literal( '+' ) + Literal( '?' ).optional() ).action( lambda input, begin, end, x, bindings: Schema.OneOrMore( subexp=x[0], greedy=( '1'   if x[2] is not None   else None ) ) )

	@Rule
	def optional(self):
		return ( self.item() + Literal( '?' ) + Literal( '?' ).optional() ).action( lambda input, begin, end, x, bindings: Schema.Optional( subexp=x[0], greedy=( '1'   if x[2] is not None   else None ) ) )

	@Rule
	def repeatRange(self):
		# Note - greedy option is inverted
		return ( self.item() + Literal( '{' ) + Tokens.decimalInteger + Literal( ',' ) + Tokens.decimalInteger + Literal( '}' ) + Literal( '?' ).optional() ).action(
			lambda input, begin, end, x, bindings: Schema.RepeatRange( subexp=x[0], min=x[2], max=x[4], greedy=( '1'   if x[6] is None   else None ) ) )

	@Rule
	def repeat(self):
		return ( self.item() + Literal( '{' ) + Tokens.decimalInteger + Literal( '}' ) ).action( lambda input, begin, end, x, bindings: Schema.Repeat( subexp=x[0], repetitions=x[2] ) )

	@Rule
	def repeatedItem(self):
		return self.zeroOrMore() | self.oneOrMore() | self.optional() | self.repeatRange() | self.repeat() | self.item()



	@Rule
	def sequence(self):
		return self.repeatedItem().oneOrMore().action( lambda input, begin, end, x, bindings: Schema.Sequence( subexps=x )   if len( x ) > 1   else x[0] )

	@Rule
	def choice(self):
		return ( self.sequence()  +  ( Literal( '|' ) + self.sequence() ).oneOrMore() ).action( lambda input, begin, end, x, bindings: Schema.Choice( subexps=[ x[0] ] + [ p[1]   for p in x[1] ] ) )  |  self.sequence()



	@Rule
	def regex(self):
		return self.choice()





class TestCase_ReParser (ParserTestCase):
	__junk_regex__ = ''

	def test_literalChar(self):
		g = SWYNGrammar()
		self._parseStringTest( g.literalChar(), 'a', Schema.LiteralChar( char='a' ) )
		self._parseStringTest( g.literalChar(), '\\.', Schema.EscapedChar( char='.' ) )
		self._parseStringTest( g.literalChar(), '\\^', Schema.EscapedChar( char='^' ) )
		self._parseStringTest( g.literalChar(), '\\$', Schema.EscapedChar( char='$' ) )
		self._parseStringTest( g.literalChar(), '\\[', Schema.EscapedChar( char='[' ) )
		self._parseStringTest( g.literalChar(), '\\\\', Schema.EscapedChar( char='\\' ) )
		self._parseStringTest( g.literalChar(), '\\(', Schema.EscapedChar( char='(' ) )
		self._parseStringTest( g.literalChar(), '\\|', Schema.EscapedChar( char='|' ) )
		self._parseStringTest( g.literalChar(), '\\+', Schema.EscapedChar( char='+' ) )
		self._parseStringTest( g.literalChar(), '\\*', Schema.EscapedChar( char='*' ) )
		self._parseStringTest( g.literalChar(), '\\?', Schema.EscapedChar( char='?' ) )
		self._parseStringFailTest( g.literalChar(), '.' )
		self._parseStringFailTest( g.literalChar(), '^' )
		self._parseStringFailTest( g.literalChar(), '$' )
		self._parseStringFailTest( g.literalChar(), '[' )
		self._parseStringFailTest( g.literalChar(), '\\' )
		self._parseStringFailTest( g.literalChar(), '(' )
		self._parseStringFailTest( g.literalChar(), '|' )
		self._parseStringFailTest( g.literalChar(), '+' )
		self._parseStringFailTest( g.literalChar(), '*' )
		self._parseStringFailTest( g.literalChar(), '?' )

		
	def test_specials(self):
		g = SWYNGrammar()
		self._parseStringTest( g.specials(), '.', Schema.AnyChar() )
		self._parseStringTest( g.specials(), '^', Schema.StartOfLine() )
		self._parseStringTest( g.specials(), '$', Schema.EndOfLine() )

		
	def test_charClass(self):
		g = SWYNGrammar()
		self._parseStringTest( g.charClass(), '\\A', Schema.CharClass( cls='A' ) )
		self._parseStringTest( g.charClass(), '\\b', Schema.CharClass( cls='b' ) )
		self._parseStringTest( g.charClass(), '\\B', Schema.CharClass( cls='B' ) )
		self._parseStringTest( g.charClass(), '\\d', Schema.CharClass( cls='d' ) )
		self._parseStringTest( g.charClass(), '\\D', Schema.CharClass( cls='D' ) )
		self._parseStringTest( g.charClass(), '\\s', Schema.CharClass( cls='s' ) )
		self._parseStringTest( g.charClass(), '\\S', Schema.CharClass( cls='S' ) )
		self._parseStringTest( g.charClass(), '\\w', Schema.CharClass( cls='w' ) )
		self._parseStringTest( g.charClass(), '\\W', Schema.CharClass( cls='W' ) )
		self._parseStringTest( g.charClass(), '\\Z', Schema.CharClass( cls='Z' ) )

		
	def test_charSet(self):
		g = SWYNGrammar()
		self._parseStringFailTest( g.charSet(), '[]' )
		self._parseStringTest( g.charSet(), '[abc]', Schema.CharSet( items=[ Schema.CharSetChar( char=Schema.LiteralChar( char='a' ) ),
										     Schema.CharSetChar( char=Schema.LiteralChar( char='b' ) ),
										     Schema.CharSetChar( char=Schema.LiteralChar( char='c' ) ) ] ) )
		self._parseStringTest( g.charSet(), '[^abc]', Schema.CharSet( invert='1', items=[ Schema.CharSetChar( char=Schema.LiteralChar( char='a' ) ),
												  Schema.CharSetChar( char=Schema.LiteralChar( char='b' ) ),
												  Schema.CharSetChar( char=Schema.LiteralChar( char='c' ) ) ] ) )
		self._parseStringTest( g.charSet(), '[a-z]', Schema.CharSet( items=[ Schema.CharSetRange( min=Schema.LiteralChar( char='a' ), max=Schema.LiteralChar( char='z' ) ) ] ) )
		self._parseStringTest( g.charSet(), '[a-zA-Z0-9_]', Schema.CharSet( items=[ Schema.CharSetRange( min=Schema.LiteralChar( char='a' ), max=Schema.LiteralChar( char='z' ) ),
											    Schema.CharSetRange( min=Schema.LiteralChar( char='A' ), max=Schema.LiteralChar( char='Z' ) ),
											    Schema.CharSetRange( min=Schema.LiteralChar( char='0' ), max=Schema.LiteralChar( char='9' ) ),
											    Schema.CharSetChar( char=Schema.LiteralChar( char='_' ) ) ] ) )
		self._parseStringTest( g.charSet(), '[^^]', Schema.CharSet( invert='1', items=[ Schema.CharSetChar( char=Schema.LiteralChar( char='^' ) ) ] ) )


	def test_group(self):
		g = SWYNGrammar()
		self._parseStringTest( g.item(), '(a)', Schema.Group( capturing='1', subexp=Schema.LiteralChar( char='a' ) ) )
		self._parseStringTest( g.item(), '(?i)', Schema.SetFlags( flags='i' ) )
		self._parseStringTest( g.item(), '(?:a)', Schema.Group( subexp=Schema.LiteralChar( char='a' ) ) )
		self._parseStringTest( g.item(), '(?P<id>a)', Schema.DefineNamedGroup( subexp=Schema.LiteralChar( char='a' ), name='id' ) )
		self._parseStringTest( g.item(), '(?P=id)', Schema.MatchNamedGroup( name='id' ) )
		self._parseStringTest( g.item(), '(?#abc)', Schema.Comment( text='abc' ) )
		self._parseStringTest( g.item(), '(?=a)', Schema.Lookahead( subexp=Schema.LiteralChar( char='a' ), positive='1' ) )
		self._parseStringTest( g.item(), '(?!a)', Schema.Lookahead( subexp=Schema.LiteralChar( char='a' ) ) )
		self._parseStringTest( g.item(), '(?<=a)', Schema.Lookbehind( subexp=Schema.LiteralChar( char='a' ), positive='1' ) )
		self._parseStringTest( g.item(), '(?<!a)', Schema.Lookbehind( subexp=Schema.LiteralChar( char='a' ) ) )


	def test_repetition(self):
		g = SWYNGrammar()
		self._parseStringTest( g.repeatedItem(), 'a*', Schema.ZeroOrMore( subexp=Schema.LiteralChar( char='a' ) ) )
		self._parseStringTest( g.repeatedItem(), 'a*?', Schema.ZeroOrMore( subexp=Schema.LiteralChar( char='a' ), greedy='1' ) )
		self._parseStringTest( g.repeatedItem(), 'a+', Schema.OneOrMore( subexp=Schema.LiteralChar( char='a' ) ) )
		self._parseStringTest( g.repeatedItem(), 'a+?', Schema.OneOrMore( subexp=Schema.LiteralChar( char='a' ), greedy='1' ) )
		self._parseStringTest( g.repeatedItem(), 'a?', Schema.Optional( subexp=Schema.LiteralChar( char='a' ) ) )
		self._parseStringTest( g.repeatedItem(), 'a??', Schema.Optional( subexp=Schema.LiteralChar( char='a' ), greedy='1' ) )
		self._parseStringTest( g.repeatedItem(), 'a{5}', Schema.Repeat( subexp=Schema.LiteralChar( char='a' ), repetitions='5' ) )
		self._parseStringTest( g.repeatedItem(), 'a{1,2}', Schema.RepeatRange( subexp=Schema.LiteralChar( char='a' ), min='1', max='2', greedy='1' ) )
		self._parseStringTest( g.repeatedItem(), 'a{1,2}?', Schema.RepeatRange( subexp=Schema.LiteralChar( char='a' ), min='1', max='2' ) )


	def test_sequence(self):
		g = SWYNGrammar()
		self._parseStringTest( g.sequence(), 'abc', Schema.Sequence( subexps=[ Schema.LiteralChar( char='a' ), Schema.LiteralChar( char='b' ), Schema.LiteralChar( char='c' ) ] ) )
		self._parseStringTest( g.sequence(), 'a*bc', Schema.Sequence( subexps=[ Schema.ZeroOrMore( subexp=Schema.LiteralChar( char='a' ) ), Schema.LiteralChar( char='b' ), Schema.LiteralChar( char='c' ) ] ) )
		self._parseStringTest( g.sequence(), 'ab*c', Schema.Sequence( subexps=[ Schema.LiteralChar( char='a' ), Schema.ZeroOrMore( subexp=Schema.LiteralChar( char='b' ) ), Schema.LiteralChar( char='c' ) ] ) )
		self._parseStringTest( g.sequence(), 'abc*', Schema.Sequence( subexps=[ Schema.LiteralChar( char='a' ), Schema.LiteralChar( char='b' ), Schema.ZeroOrMore( subexp=Schema.LiteralChar( char='c' ) ) ] ) )
		self._parseStringTest( g.sequence(), '[a-z]bc', Schema.Sequence( subexps=[ Schema.CharSet( items=[ Schema.CharSetRange( min=Schema.LiteralChar( char='a' ), max=Schema.LiteralChar( char='z' ) ) ] ),
											   Schema.LiteralChar( char='b' ), Schema.LiteralChar( char='c' ) ] ) )
		self._parseStringTest( g.sequence(), '\\wbc*', Schema.Sequence( subexps=[ Schema.CharClass( cls='w' ), Schema.LiteralChar( char='b' ), Schema.ZeroOrMore( subexp=Schema.LiteralChar( char='c' ) ) ] ) )


	def test_choice(self):
		g = SWYNGrammar()
		self._parseStringTest( g.choice(), 'abc|def', Schema.Choice( subexps=[ Schema.Sequence( subexps=[ Schema.LiteralChar( char='a' ), Schema.LiteralChar( char='b' ), Schema.LiteralChar( char='c' ) ] ),
										       Schema.Sequence( subexps=[ Schema.LiteralChar( char='d' ), Schema.LiteralChar( char='e' ), Schema.LiteralChar( char='f' ) ] ) ] ) )


	def test_regex(self):
		g = SWYNGrammar()
		self._parseStringTest( g.regex(), 'a', Schema.LiteralChar( char='a' ) )
		self._parseStringTest( g.regex(), 'abc|def', Schema.Choice( subexps=[ Schema.Sequence( subexps=[ Schema.LiteralChar( char='a' ), Schema.LiteralChar( char='b' ), Schema.LiteralChar( char='c' ) ] ),
										      Schema.Sequence( subexps=[ Schema.LiteralChar( char='d' ), Schema.LiteralChar( char='e' ), Schema.LiteralChar( char='f' ) ] ) ] ) )
		self._parseStringTest( g.regex(), r'[\w\-][\w\-\.]+@[\w\-][\w\-\.]+[a-zA-Z]{1,4}',
				       Schema.Sequence( subexps=[
					       			Schema.CharSet( items=[ Schema.CharSetChar( char=Schema.CharClass( cls='w' ) ), Schema.CharSetChar( char=Schema.EscapedChar( char='-' ) ) ] ),
								Schema.OneOrMore( subexp=Schema.CharSet( items=[ Schema.CharSetChar( char=Schema.CharClass( cls='w' ) ), Schema.CharSetChar( char=Schema.EscapedChar( char='-' ) ),
											  Schema.CharSetChar( char=Schema.EscapedChar( char='.' ) ) ] ) ),
								Schema.LiteralChar( char='@' ),
								Schema.CharSet( items=[ Schema.CharSetChar( char=Schema.CharClass( cls='w' ) ), Schema.CharSetChar( char=Schema.EscapedChar( char='-' ) ) ] ),
								Schema.OneOrMore( subexp=Schema.CharSet( items=[ Schema.CharSetChar( char=Schema.CharClass( cls='w' ) ), Schema.CharSetChar( char=Schema.EscapedChar( char='-' ) ),
														 Schema.CharSetChar( char=Schema.EscapedChar( char='.' ) ) ] ) ),
								Schema.RepeatRange( subexp=Schema.CharSet( items=[ Schema.CharSetRange( min=Schema.LiteralChar( char='a' ), max=Schema.LiteralChar( char='z' ) ),
														   Schema.CharSetRange( min=Schema.LiteralChar( char='A' ), max=Schema.LiteralChar( char='Z' ) ) ] ), min='1', max='4',
										    greedy='1' )
								] ) )