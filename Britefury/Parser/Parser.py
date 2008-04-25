##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

"""
This module is an implementation of a Packrat parser modified to handle parsing expression grammars that are directly or indirecly left-recursive.
The interface to the system is based on the pyparsing library  (http://pyparsing.wikispaces.com).
The code is based on the paper "Packrat Parsers Can Support Left Recursion" by Allesandro Warth, James R. Douglass, Todd Millstein; Viewpoints Research Institute.
The test that involves a simplified part of the Java grammar is from the same paper.
"""

import string
import operator
from copy import copy
import re

from Britefury.Parser.ParserState import ParserState


def _parser_coerce(x):
	if isinstance( x, ParserExpression ):
		return x
	elif isinstance( x, list )  or  isinstance( x, tuple ):
		return Sequence( _parser_coerce( [ _parser_coerce( a )   for a in x ] ) )
	else:
		return Literal( x )



	
		
		

	
		
class ParseResult (object):
	"""
	Parse result
	Members:
	   result - the result (returned by the parse expression action function, otherwise a list/tree of strings)
	      None indicates an empty result. This is created by Peek expressions, and can be created by Repetition epxressions.
	   begin - the start index of the result, in the input string
	   end - the end index of the result, in the input string
	"""
	def __init__(self, result, begin, end):
		self.result = result
		self.begin = begin
		self.end = end
		
		

		
		
		

class ParserExpression (object):
	"""Parser expression base class"""
	#def _o_match(self, input, start, stop, state=None):
		#if state is None:
			#state = {}
			
		#key = start, self
		#try:
			#memoEntry = state[key]
			#return memoEntry.answer, memoEntry.pos
		#except KeyError:
			#memoEntry = _MemoEntry( None, start )
			#state[key] = memoEntry
			#answer, pos = self._o_evaluate( state, input, start, stop )
			#memoEntry.answer = answer
			#memoEntry.pos = pos
			#return answer, pos
		
		
	def parseString(self, input, start=0, stop=None, ignoreChars=string.whitespace):
		"""
		Parse a string
		parseString(input, start, stop=None)  ->  ParseResult
		   input - the input string
		   start - the start index of the substring (of input) to parse
		   stop - the end index of the substring (of input) to parse
		   bEatWhitespace - If True, whitespace will be suppressed
		   
		The result of parsing the expression is placed in the @result member of the ParseResult object.
		
		If parseString returns None, then the parse failed.
		"""
		if stop is None:
			stop = len( input )
		state = ParserState( ignoreChars )
		answer, pos = self._o_match( state, input, start, stop )
		if answer is not None:
			answer.end = state.chomp( input, answer.end, stop )
		return answer
	

	
	def _o_match(self, state, input, start, stop):
		return state._f_memoisedMatch( self, input, start, stop )
		
		
		
	def _o_evaluate(self, state, input, start, stop):
		pass

		
	def __eq__(self, x):
		if isinstance( x, type( self ) ):
			return self._o_compare( x )
		else:
			return False
		
		
	def __ne__(self, x):
		if isinstance( x, type( self ) ):
			return not self._o_compare( x )
		else:
			return True

		
	def __add__(self, x):
		return Sequence( [ self, x ] )
	
	def __or__(self, x):
		return FirstOf( [ self, x ] )
	
	def __xor__(self, x):
		return BestOf( [ self, x ] )
	
	
	def _o_compare(self, x):
		return False
	
	
	def _o_copySettingsFrom(self, x):
		pass
	

	
	
class ParserExpressionWithAction (ParserExpression):
	"""Parser expression that can take actions; base class"""
	def __init__(self):
		super( ParserExpressionWithAction, self ).__init__()
		self._actionFn = None
		
		
	def setAction(self, f):
		"""
		Set the action function
		setAction( f )  ->  self
		This function is called when the parser expression successfully matches some text.
		It is of the form:
		   f(input, start, end, tokens)  ->  resultTokens
		      input  -  the original string being parsed
		      start, end  -  start and end indices indicating the substring of @input that was parsed
		      tokens  -  the tokens from sub-parse-expressions
		     resultTokens  -  is passed as @tokens to the parent parser expression action function
		"""
		element = copy( self )
		element._actionFn = f
		return element
	

	def _p_action(self, input, start, end, tokens):
		result = None
		if self._actionFn is not None:
			result = self._actionFn( input, start, end, tokens )
		if result is None:
			return tokens
		else:
			return result
		

	def _o_copySettingsFrom(self, x):
		super( ParserExpressionWithAction, self )._o_copySettingsFrom( x )
		self._actionFn = x._actionFn
	
	
	
class Forward (ParserExpressionWithAction):
	"""
	Forward
	
	Used for recursive grammars; ue Forward when you wish to make use of a parser expression @x in another expression, and provide
	the definition of @x later.
	
	For example:
	   # Construct an empty forward:
	   x = Forward()
	   # Now, make something that uses @x before it is defined/implemented
	   y = Literal( 'a' ) + Optional( x )
	   # Now, define @x
	   x  <<  y
	
	The parse result is just the parse result of the sub-expression provided by the << operator.
	"""
	def __init__(self):
		super( Forward, self ).__init__()
		self._subexp = None
		

	def _o_evaluate(self, state, input, start, stop):
		return self._subexp._o_match( state, input, start, stop )

	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp 
	
	
	def __lshift__(self, subexp):
		"""
		Set the subexpression to be matched.
		"""
		self._subexp = subexp
		return self


	

class Group (ParserExpressionWithAction):
	"""
	Group
	Used to group a sub-expression
	
	When building expressions using '+', '|', etc, they will keep adding on.
	E.g.:
	a = b + c  ->  Sequence( [ b, c ] )
	q = a + p  ->  Sequence( [ b, c, p ] )
	
	Use group:
	a = Group( b + c )  ->  Group( Sequence( [ b, c ] ) )
	q = Group( a + p )  ->  Group( Sequence( [ a, p ] ) )
	"""
	def __init__(self, subexp):
		"""
		subexp - the sub-expression in the group
		"""
		super( Group, self ).__init__()
		self._subexp = _parser_coerce( subexp )
		

	def _o_evaluate(self, state, input, start, stop):
		res, pos = self._subexp._o_match( state, input, start, stop )
		if res is None:
			return res, pos
		else:
			return ParseResult( self._p_action( input, start, pos, res.result ), start, pos ),  pos

	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp 

	
	
	
class Production (Group):
	"""
	Production
	Group a subexpression into the right hand side of a grammar rule
	"""
	pass
	

	

class Suppress (ParserExpression):
	"""
	Suppress
	Matches the sub-expression but returns an empty result
	"""
	def __init__(self, subexp):
		"""
		subexp - the sub-expression to be matched
		"""
		super( Suppress, self ).__init__()
		self._subexp = _parser_coerce( subexp )
		

	def _o_evaluate(self, state, input, start, stop):
		res, pos = self._subexp._o_match( state, input, start, stop )
		if res is None:
			return res, pos
		else:
			return ParseResult( None, start, pos ),  pos

	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp 

	
	
	

class Literal (ParserExpressionWithAction):
	"""
	Literal
	
	Matches a supplied string
	
	The parse result is a string.
	"""
	
	def __init__(self, matchString):
		"""
		matchString - the string to match
		"""
		super( Literal, self ).__init__()
		self._matchString = unicode( matchString )
		
		
	def getMatchString(self):
		return self._matchString
		
	
	def _o_evaluate(self, state, input, start, stop):
		start = state.chomp( input, start, stop )
		
		end = start + len( self._matchString )
		if end <= stop:
			x = input[start:end]
			if x == self._matchString:
				return ParseResult( self._p_action( input, start, end, x ), start, end ),  end
		return None, start
		
	def _o_compare(self, x):
		return self._matchString  ==  x._matchString


class Word (ParserExpressionWithAction):
	"""
	Word
	
	Matches a word composed of only the specified characters

	The parse result is a string.
	"""
	def __init__(self, initChars, bodyChars=None):
		"""
		Word( chars )   -   matches a word composed of characters in @chars
		Word( initChars, bodyChars )   -   matches a word composed of characters in @bodyChars, followed by one character from @initChars
		"""
		super( Word, self ).__init__()
		if bodyChars is None:
			bodyChars = initChars
			initChars = None
		self._initChars = initChars
		self._bodyChars = bodyChars
		
		
	def getInitChars(self):
		return self._initChars
	
	def getBodyChars(self):
		return self._bodyChars
		
	
	def _o_evaluate(self, state, input, start, stop):
		start = state.chomp( input, start, stop )
		
		if start >= stop:
			return None, start
		
		pos = start
		if self._initChars is not None:
			if input[start] not in self._initChars:
				return None, start
			pos = pos + 1
			
		end = stop
		for i in xrange( pos, stop ):
			c = input[i]
			if c not in self._bodyChars:
				end = i
				break
			
		if end == start:
			return None, start
		else:
			x = input[start:end]
			return ParseResult( self._p_action( input, start, end, x ), start, end ),  end


	def _o_compare(self, x):
		return self._initChars  ==  x._initChars  and  self._bodyChars  ==  x._bodyChars
	
	
	
	
class RegEx (ParserExpressionWithAction):
	"""
	RegEx
	
	Matches a regular expression
	
	The parse result is a string.
	"""
	
	def __init__(self, pattern, flags=0, bEatWhitespace=True):
		"""
		pattern - the regular expression pattern
		"""
		super( RegEx, self ).__init__()
		self._re = re.compile( pattern, flags )
		self._bEatWhitespace = bEatWhitespace
		
		
	def getRE(self):
		return self._re 
		
	
	def _o_evaluate(self, state, input, start, stop):
		if self._bEatWhitespace:
			start = state.chomp( input, start, stop )
			
		m = self._re.match( input, start, stop )
		
		if m is not None:		
			matchString = m.group()
			if len( matchString ) > 0:
				end = start + len( matchString )
				return ParseResult( self._p_action( input, start, end, matchString ), start, end ),  end
		return None, start
		
	def _o_compare(self, x):
		return self._re  ==  x._re


	
	
	
class Sequence (ParserExpressionWithAction):
	"""
	Sequence
	
	Matches a sequence of sub-expressions (in order)

	The parse result is a list of the parse results of the sub-expressions in the sequence.
	"""
	def __init__(self, subexps):
		"""
		subexps - the sub expressions that are to be matched (in order)
		"""
		super( Sequence, self ).__init__()
		assert len( subexps ) > 0
		self._subexps = [ _parser_coerce( x )   for x in subexps ]
		
		
	def getSubExpressions(self):
		return self._subexps
		
	
	def _o_evaluate(self, state, input, start, stop):
		subexpResults = []
		
		pos = start
		for i, subexp in enumerate( self._subexps ):
			if pos > stop:
				return None, start
			res, pos = subexp._o_match( state, input, pos, stop )
			if res is None:
				return None, start
			else:
				if res.result is not None:
					subexpResults.append( res.result )
		
		return ParseResult( self._p_action( input, start, pos, subexpResults ), start, pos ),  pos


	def __add__(self, x):
		s = Sequence( self._subexps  +  [ x ] )
		s._o_copySettingsFrom( self )
		return s
	
	
	def _o_compare(self, x):
		return self._subexps  ==  x._subexps


	
	
class Combine (ParserExpressionWithAction):
	"""
	Combine
	
	Matches a sequence of sub-expressions (in order), and combines their results

	The parse result is the result of adding the parse results of the sub-expressions in the sequence.
	"""
	def __init__(self, subexps):
		"""
		subexps - the sub expressions that are to be matched (in order)
		"""
		super( Combine, self ).__init__()
		assert len( subexps ) > 0
		self._subexps = [ _parser_coerce( x )   for x in subexps ]
		
		
	def getSubExpressions(self):
		return self._subexps
		
	
	def _o_evaluate(self, state, input, start, stop):
		subexpResults = []
		
		pos = start
		for i, subexp in enumerate( self._subexps ):
			if pos > stop:
				return None, start
			res, pos = subexp._o_match( state, input, pos, stop )
			if res is None:
				return None, start
			else:
				if res.result is not None:
					subexpResults.append( res.result )
					
		result = reduce( operator.__add__, subexpResults )
		
		return ParseResult( self._p_action( input, start, pos, result ), start, pos ),  pos


	def _o_compare(self, x):
		return self._subexps  ==  x._subexps


	
	
class FirstOf (ParserExpressionWithAction):
	"""
	FirstOf
	
	Matches one of a list of sub-expressions
	The first sub-expression to match successfully is the one that is used.
	
	The parse result is the parse result of the successfully match sub-expression.
	"""
	def __init__(self, subexps):
		"""
		subexps - the sub expressions, one of which is to be matched
		"""
		super( FirstOf, self ).__init__()
		self._subexps = [ _parser_coerce( x )   for x in subexps ]
		
	
	def _o_evaluate(self, state, input, start, stop):
		for i, subexp in enumerate( self._subexps ):
			res, pos = subexp._o_match( state, input, start, stop )
			if res is not None:
				return ParseResult( self._p_action( input, start, pos, res.result ), start, res.end ),  pos
			
		return None, start

	
	def __or__(self, x):
		f = FirstOf( self._subexps  +  [ x ] )
		f._o_copySettingsFrom( self )
		return f
	
	
	def _o_compare(self, x):
		return self._subexps  ==  x._subexps



class BestOf (ParserExpressionWithAction):
	"""
	BestOf
	
	Matches one of a list of sub-expressions
	The sub-expression that successfully matches the most input is the one that is used.
	
	The parse result is the parse result of the successfully match sub-expression.
	"""
	def __init__(self, subexps):
		"""
		subexps - the sub expressions, one of which is to be matched
		"""
		super( BestOf, self ).__init__()
		self._subexps = [ _parser_coerce( x )   for x in subexps ]
		
	
	def _o_evaluate(self, state, input, start, stop):
		bestResult = None
		bestPos = start
		for i, subexp in enumerate( self._subexps ):
			res, pos = subexp._o_match( state, input, start, stop )
			if res is not None  and  pos > bestPos:
				bestResult = res
				bestPos = pos
		
		if bestResult is not None:
			return ParseResult( self._p_action( input, start, bestPos, bestResult.result ), start, bestResult.end ),  bestPos
		else:
			return None, start

	
	def __xor__(self, x):
		b = BestOf( self._subexps  +  [ x ] )
		b._o_copySettingsFrom( self )
		return b
	
	
	def _o_compare(self, x):
		return self._subexps  ==  x._subexps



class Repetition (ParserExpressionWithAction):
	"""
	Repetition
	
	Matches a sub-expression a number of times within a certain range
	
	The parse result is a list of the parse results of the repeated application of the sub-expression.
	
	If the @bSuppressIfZero argument of the constuctor is True, no result will be generated if zero occurrences of the sub-expression are matched.
	If it is False, an empty list will be returned.
	If @bSuppressIfZero is True, and this expression is contained within an outer expression that generates a list as a parse result (e.g. Sequence, or an outer Repetition),
	the None parse result that is generated in the case of zero repetitions will not be present in the list generated by the outer expression.
	"""
	def __init__(self, subexp, min, max, bSuppressIfZero=False):
		"""
		subexp - the sub expression to match
		min - the minimum number of occurrences of @subexp to match
		max - the maximum number of occurrences of @subexp to match
		bSuppressIfZero - When true, will generate an empty result (The result field of the ParseResult will be None) if 0 occurrences are matched
		"""
		super( Repetition, self ).__init__()
		self._subexp = _parser_coerce( subexp )
		self._min = min
		self._max = max
		self._bSuppressIfZero = bSuppressIfZero
		
	
	def _o_evaluate(self, state, input, start, stop):
		subexpResults = []
		
		pos = start
		i = 0
		while pos <= stop  and  ( self._max is None  or  i < self._max ):
			res, pos = self._subexp._o_match( state, input, pos, stop )
			if res is None:
				break
			else:
				if res.result is not None:
					subexpResults.append( res.result )
			i += 1
			
			
		if i < self._min  or  ( self._max is not None   and   i > self._max ):
			return None, start
		else:
			if self._bSuppressIfZero  and  i == 0:
				return ParseResult( None, start, pos ),  pos
			else:
				return ParseResult( self._p_action( input, start, pos, subexpResults ), start, pos ),  pos
	
	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp  and  self._min  ==  x._min  and  self._max  ==  x._max


class Optional (Repetition):
	"""
	Optional
	
	Optionally matches a sub-expression

	The parse result is a list of the parse results of the repeated application of the sub-expression.
	"""
	def __init__(self, subexp, bSuppressIfZero=True):
		"""
		subexp - the sub-expression to optionally match
		bSuppressIfZero - When true, will generate an empty result (The result field of the ParseResult will be None) if no match is made
		"""
		super( Optional, self ).__init__( subexp, 0, 1, bSuppressIfZero )
		

class ZeroOrMore (Repetition):
	"""
	ZeroOrMore
	
	Matches a sub-expression 0 or more times

	The parse result is a list of the parse results of the repeated application of the sub-expression.
	"""
	def __init__(self, subexp, bSuppressIfZero=False):
		"""
		subexp - the sub-expression to match
		bSuppressIfZero - When true, will generate an empty result (The result field of the ParseResult will be None) if 0 occurrences are matched
		"""
		super( ZeroOrMore, self ).__init__( subexp, 0, None, bSuppressIfZero )
		

class OneOrMore (Repetition):
	"""
	OneOrMore
	
	Matches a sub-expression 1 or more times

	The parse result is a list of the parse results of the repeated application of the sub-expression.
	"""
	def __init__(self, subexp):
		"""
		subexp - the sub-expression to match
		"""
		super( OneOrMore, self ).__init__( subexp, 1, None )
		
		
		
class Peek (ParserExpression):
	"""
	Peek
	
	Matches a sub-expression but does not consume it
	
	The parse result is an empty result.
	"""
	def __init__(self, subexp):
		"""
		subexp - the sub-expression to match
		"""
		super( Peek, self ).__init__()
		self._subexp = _parser_coerce( subexp )
		
	
	def _o_evaluate(self, state, input, start, stop):
		res, pos = self._subexp._o_match( state, input, start, stop )
		if res is not None:
			return ParseResult( None, start, start ),  start
		else:
			return None, start

	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp 
	


class PeekNot (ParserExpression):
	"""
	PeekNot
	
	Matches anything but a specified sub-expression but does not consume it

	The parse result is an empty result.
	"""
	def __init__(self, subexp):
		"""
		subexp - the sub-expression to not match
		"""
		super( PeekNot, self ).__init__()
		self._subexp = _parser_coerce( subexp )
		
	
	def _o_evaluate(self, state, input, start, stop):
		res, pos = self._subexp._o_match( state, input, start, stop )
		if res is None  or  res.result is None:
			return ParseResult( None, start, start ), start
		else:
			return None, start

	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp 
	

	
	
def _delimitedListAction(input, begin, end, tokens):
	if len( tokens )  ==  0:
		return []
	else:
		return [ tokens[0][0] ]  +  [ x[1]   for x in tokens[0][1] ]

def delimitedList(subexp, delimiter=','):
	"""
	Delimited list
	
	delimitedList(subexpression, delimiter=',')  ->  ParserExpression
	
	Creates a parser expression that will match a delimited list.
	  subexpression - a parser expression that defines the elements of the list
	  delimiter - a parser expression the defines the delimiter that separates list entries
	
	For example:
	  delimitedList( identifier, ',' )
	
	will construct a parser expression that will parse:
	   a, b, c, d
	to:
	   [ 'a', 'b', 'c', 'd' ]
	"""
	return ( Optional( subexp  +  ZeroOrMore( _parser_coerce( delimiter )  +  subexp ), False ) ).setAction( _delimitedListAction )



def _getLineIndentation(line):
	return line[: line.index( line.strip() ) ]

def _getLineWithoutIndentation(line):
	return line[line.index( line.strip() ):]

def _processIndentation(indentationStack, indentation, indentToken, dedentToken, currentLevel):
	prevIndentation = indentationStack[-1]
	if indentation != prevIndentation:
		if indentation.startswith( prevIndentation ):
			indentationStack.append( indentation )
			currentLevel += 1
			return indentation + indentToken, currentLevel
		elif prevIndentation.startswith( indentation ):
			dedents = ''
			while indentationStack[-1].startswith( indentation )  and  indentation != indentationStack[-1]:
				del indentationStack[-1]
				dedents += dedentToken
				currentLevel -= 1
			return indentation + dedents, currentLevel
		else:
			raise IndentationError
	else:
		return indentation, currentLevel


def indentedBlocksPrePass(text, indentToken='$<indent>$', dedentToken='$<dedent>$'):
	"""
	Processe text whose blocks are determined by indentation, by inserting indent and dedent tokens
	
	indentedBlocksPrePass(text, indentToken='$<indent>$', dedentToken='$<dedent>$')  ->  text with indent and detent tokens
	
	For example
	a
	b
	  c
	  d
	e
	f
	
	==>>
	
	a
	b
	  $<indent>$c
	  d
	$<dedent>$e
	f	
	"""
	lines = text.split( '\n' )
	
	if len( lines ) > 0:
		indentationStack = []
		indentationStack.append( _getLineIndentation( lines[0] ) )
		
		indentationLevel = 0
		
		for i, line in enumerate( lines ):
			if line.strip() != '':
				indentation = _getLineIndentation( line )
				content = _getLineWithoutIndentation( line )
				
				processedIndentation, indentationLevel = _processIndentation( indentationStack, indentation, indentToken, dedentToken, indentationLevel )
				lines[i] = processedIndentation +  content
				currentIndentation = indentation
				
		bAppendBlankLine = indentationLevel > 0
				
		for i in xrange( 0, indentationLevel ):
			lines.append( dedentToken )
			
		if bAppendBlankLine:
			lines.append( '' )
	
	return '\n'.join( lines )
				
				
			


#
# Definitions of singleQuotedString, doubleQuotedString, and quotedString taken from pyparsing, which is Copyright (c) 2003-2007  Paul T. McGuire
#

identifier = RegEx( "[A-Za-z_][A-Za-z0-9]*" )
singleQuotedString = RegEx( r"'(?:[^'\n\r\\]|(?:'')|(?:\\x[0-9a-fA-F]+)|(?:\\.))*'" )
doubleQuotedString = RegEx( r'"(?:[^"\n\r\\]|(?:"")|(?:\\x[0-9a-fA-F]+)|(?:\\.))*"' )
quotedString = RegEx( r'''(?:"(?:[^"\n\r\\]|(?:"")|(?:\\x[0-9a-fA-F]+)|(?:\\.))*")|(?:'(?:[^'\n\r\\]|(?:'')|(?:\\x[0-9a-fA-F]+)|(?:\\.))*')''' )
unicodeString = Combine( [ 'u', quotedString ] )
integer = RegEx( r"[\-]?[0-9]+" )
floatingPoint = RegEx( r"[\-]?(([0-9]+\.[0-9]*)|(\.[0-9]+))(e[\-]?[0-9]+)?" )



import unittest


class TestCase_Parser (unittest.TestCase):
	def _matchTest(self, parser, input, expected, begin=None, end=None, ignoreChars=string.whitespace):
		result = parser.parseString( input, ignoreChars=ignoreChars )
		self.assert_( result is not None )
		res = result.result
		if res != expected:
			print 'EXPECTED:'
			print expected
			print ''
			print 'RESULT:'
			print res
		self.assert_( res == expected )
		
		if result is not None:
			if begin is not None:
				self.assert_( begin == result.begin )
			if end is not None:
				self.assert_( end == result.end )
		
	
	def _matchFailTest(self, parser, input, ignoreChars=string.whitespace):
		result = parser.parseString( input, ignoreChars=ignoreChars )
		if result is not None   and   result.end == len( input ):
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print result
		self.assert_( result is None  or  result.end != len( input ) )
		
				
	def testLiteral(self):
		self.assert_( Literal( 'abc' )  ==  Literal( 'abc' ) )
		self.assert_( Literal( 'abc' )  !=  Literal( 'def' ) )
		self._matchTest( Literal( 'abcxyz' ), 'abcxyz', 'abcxyz', 0, 6 )
		self._matchFailTest( Literal( 'abcxyz' ), 'qwerty' )
		self._matchTest( Literal( 'abcxyz' ), 'abcxyz123', 'abcxyz', 0, 6 )
		
	def testWord(self):
		self.assert_( Word( 'abc' )  ==  Word( 'abc' ) )
		self.assert_( Word( 'abc' )  !=  Word( 'def' ) )
		self._matchTest( Word( 'abc' ), 'aabbcc', 'aabbcc', 0, 6 )
		self._matchTest( Word( 'abc' ), 'aabbccxx', 'aabbcc', 0, 6 )
		self._matchTest( Word( 'abc' ), 'aabbccxxaa', 'aabbcc', 0, 6 )
		self._matchTest( Word( 'abc' ), 'a', 'a', 0, 1 )
		self._matchFailTest( Word( 'abc' ), 'x' )

		self.assert_( Word( 'abc', 'xyz' )  ==  Word( 'abc', 'xyz' ) )
		self.assert_( Word( 'abc', 'xyz' )  !=  Word( 'def', 'xyz' ) )
		self.assert_( Word( 'abc', 'xyz' )  !=  Word( 'abc', 'ijk' ) )
		self._matchTest( Word( 'abc', 'def' ), 'addeeff', 'addeeff', 0, 7 )
		self._matchTest( Word( 'abc', 'def' ), 'addeeffxx', 'addeeff', 0, 7 )
		self._matchTest( Word( 'abc', 'def' ), 'bddeeff', 'bddeeff', 0, 7 )
		self._matchTest( Word( 'abc', 'def' ), 'baddeeff', 'b', 0, 1 )
		self._matchFailTest( Word( 'abc', 'def' ), 'ddeeff' )

		
	def testRegEx(self):
		self.assert_( RegEx( r"[A-Za-z_][A-Za-z0-9]*" )  ==  RegEx( r"[A-Za-z_][A-Za-z0-9]*" ) )
		self.assert_( RegEx( r"[A-Za-z_][A-Za-z0-9]*" )  !=  RegEx( r"[A-Za-z_][A-Za-z0-9]*abc" ) )
		self._matchTest( RegEx( r"[A-Za-z_][A-Za-z0-9]*" ), 'abc123', 'abc123', 0, 6 )
		self._matchFailTest( RegEx( r"[A-Za-z_][A-Za-z0-9]*" ), '9abc' )
		self._matchTest( RegEx( r"[A-Za-z_][A-Za-z0-9]*" ), 'abcxyz...', 'abcxyz', 0, 6 )
		

	def testSequence(self):
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Sequence( [ Literal( 'ab' ), Literal( 'qw' ) ] ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Sequence( [ Literal( 'qb' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  +  Literal( 'qw' )  +  Literal( 'fh' ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  +  'qw'  +  'fh' )

		parser = Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )
		self._matchTest( parser, 'abqwfh', [ 'ab', 'qw', 'fh' ], 0, 6 )
		self._matchFailTest( parser, 'abfh' )
	
		
	def testCombine(self):
		self.assert_( Combine( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Combine( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Combine( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Combine( [ Literal( 'ab' ), Literal( 'qw' ) ] ) )
		self.assert_( Combine( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Combine( [ Literal( 'qb' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )

		parser = Combine( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )
		self._matchTest( parser, 'abqwfh', 'abqwfh', 0, 6 )
		self._matchFailTest( parser, 'abfh' )
	
		
	def testSuppress(self):
		self.assert_( Suppress( 'abc' )  ==  Suppress( 'abc' ) )
		self.assert_( Suppress( 'abc' )  !=  Suppress( 'def' ) )
		
		parser = Literal( 'ab' )  +  Suppress( 'cd' )  +  Literal( 'ef' )
		
		self._matchTest( parser, 'abcdef', [ 'ab', 'ef' ], 0, 6 )
		self._matchFailTest( parser, 'abef' )
		

	def testFirstOf(self):
		self.assert_( FirstOf( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   FirstOf( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( FirstOf( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   FirstOf( [ Literal( 'ab' ), Literal( 'qw' ) ] ) )
		self.assert_( FirstOf( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   FirstOf( [ Literal( 'qb' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( FirstOf( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  |  Literal( 'qw' )  |  Literal( 'fh' ) )
		self.assert_( FirstOf( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  |  'qw'  |  'fh' )

		parser = FirstOf( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )
		self._matchTest( parser, 'ab', 'ab', 0, 2 )
		self._matchTest( parser, 'qw', 'qw', 0, 2 )
		self._matchTest( parser, 'fh', 'fh', 0, 2 )
		self._matchFailTest( parser, 'xy' )
		self._matchTest( Literal( 'ab' )  |  Literal( 'abcd' ),   'ab', 'ab', 0, 2 )
		self._matchTest( Literal( 'ab' )  |  Literal( 'abcd' ),   'abcd', 'ab', 0, 2 )
		
		
	def testBestOf(self):
		self.assert_( BestOf( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   BestOf( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( BestOf( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   BestOf( [ Literal( 'ab' ), Literal( 'qw' ) ] ) )
		self.assert_( BestOf( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   BestOf( [ Literal( 'qb' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( BestOf( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  ^  Literal( 'qw' )  ^  Literal( 'fh' ) )
		self.assert_( BestOf( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  ^  'qw'  ^  'fh' )

		parser = BestOf( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )
		self._matchTest( parser, 'ab', 'ab', 0, 2 )
		self._matchTest( parser, 'qw', 'qw', 0, 2 )
		self._matchTest( parser, 'fh', 'fh', 0, 2 )
		self._matchFailTest( parser, 'xy' )
		self._matchTest( Literal( 'ab' )  ^  Literal( 'abcd' ),   'ab', 'ab', 0, 2 )
		self._matchTest( Literal( 'ab' )  ^  Literal( 'abcd' ),   'abcd', 'abcd', 0, 4 )
		
		
	def testOptional(self):
		self.assert_( Optional( Literal( 'ab' ) )   ==   Optional( Literal( 'ab' ) ) )
		self.assert_( Optional( Literal( 'ab' ) )   !=   Optional( Literal( 'qb' ) ) )
		self.assert_( Optional( Literal( 'ab' ) )   ==   Optional( 'ab' ) )
		
		parser = Optional( Literal( 'ab' ) ) 
		self._matchTest( parser, '', None, 0, 0 )
		self._matchTest( parser, 'ab', [ 'ab' ], 0, 2 )
		self._matchTest( parser, 'abab', [ 'ab' ], 0, 2 )

		
	def testZeroOrMore(self):
		self.assert_( ZeroOrMore( Literal( 'ab' ) )   ==   ZeroOrMore( Literal( 'ab' ) ) )
		self.assert_( ZeroOrMore( Literal( 'ab' ) )   !=   ZeroOrMore( Literal( 'qb' ) ) )
		self.assert_( ZeroOrMore( Literal( 'ab' ) )   ==   ZeroOrMore( 'ab' ) )
		
		parser = ZeroOrMore( Literal( 'ab' ) + Literal( 'cd' ) ) 
		self._matchTest( parser, '', [], 0, 0 )
		self._matchTest( parser, 'abcd', [ [ 'ab', 'cd' ] ], 0, 4 )
		self._matchTest( parser, 'abcdabcd', [ [ 'ab', 'cd' ], [ 'ab', 'cd' ] ], 0, 8 )

		
	def testOneOrMore(self):
		self.assert_( OneOrMore( Literal( 'ab' ) )   ==   OneOrMore( Literal( 'ab' ) ) )
		self.assert_( OneOrMore( Literal( 'ab' ) )   !=   OneOrMore( Literal( 'qb' ) ) )
		self.assert_( OneOrMore( Literal( 'ab' ) )   ==   OneOrMore( 'ab' ) )
		
		parser = OneOrMore( Literal( 'ab' ) ) 
		self._matchFailTest( parser, '' )
		self._matchTest( parser, 'ab', [ 'ab' ], 0, 2 )
		self._matchTest( parser, 'abab', [ 'ab', 'ab' ], 0, 4 )
		
		
	def testPeek(self):
		self.assert_( Peek( Literal( 'ab' ) )   ==   Peek( 'ab' ) )

		parser = OneOrMore( Literal( 'ab' ) )  +  Peek( Literal( 'cd' ) )
		self._matchFailTest( parser, '' )
		self._matchFailTest( parser, 'ab' )
		self._matchFailTest( parser, 'abab' )
		self._matchTest( parser, 'abcd', [ [ 'ab' ] ], 0, 2 )
		self._matchTest( parser, 'ababcd', [ [ 'ab', 'ab' ] ], 0, 4 )

		
	def testPeekNot(self):
		self.assert_( PeekNot( Literal( 'ab' ) )   ==   PeekNot( 'ab' ) )

		parser = OneOrMore( Literal( 'ab' ) )  +  PeekNot( Literal( 'cd' ) )
		self._matchFailTest( parser, '' )
		self._matchTest( parser, 'ab', [ [ 'ab' ] ], 0, 2 )
		self._matchFailTest( parser, 'abcd' )
		self._matchTest( parser, 'abef', [ [ 'ab' ] ], 0, 2 )
		self._matchTest( parser, 'abab', [ [ 'ab', 'ab' ] ], 0, 4 )
		self._matchFailTest( parser, 'ababcd' )
		self._matchTest( parser, 'ababef', [ [ 'ab', 'ab' ] ], 0, 4 )
		
		
	def testIIdentifier(self):
		parser = identifier
		self._matchTest( parser, 'ab', 'ab' )
		self._matchTest( parser, 'ab12', 'ab12' )
		self._matchFailTest( parser, '12ab' )
		self._matchTest( parser, '_ab', '_ab' )
		
		
	def testSingleQuotedString(self):
		parser = singleQuotedString
		self._matchTest( parser, "'abc'", "'abc'" )
		self._matchTest( parser, r"'ab\'c'", r"'ab\'c'" )
		self._matchTest( parser, "'abc'113", "'abc'" )
	
		
	def testDoubleQuotedString(self):
		parser = doubleQuotedString
		self._matchTest( parser, '"abc"', '"abc"' )
		self._matchTest( parser, r'"ab\"c"', r'"ab\"c"' )
		self._matchTest( parser, '"abc"113', '"abc"' )
	
		
	def testQuotedString(self):
		parser = quotedString
		self._matchTest( parser, "'abc'", "'abc'" )
		self._matchTest( parser, r"'ab\'c'", r"'ab\'c'" )
		self._matchTest( parser, "'abc'113", "'abc'" )
		self._matchTest( parser, '"abc"', '"abc"' )
		self._matchTest( parser, r'"ab\"c"', r'"ab\"c"' )
		self._matchTest( parser, '"abc"113', '"abc"' )
	
		
	def testUnicodeString(self):
		parser = unicodeString
		self._matchTest( parser, "u'abc'", "u'abc'" )
		self._matchTest( parser, r"u'ab\'c'", r"u'ab\'c'" )
		self._matchTest( parser, "u'abc'113", "u'abc'" )
		self._matchTest( parser, 'u"abc"', 'u"abc"' )
		self._matchTest( parser, r'u"ab\"c"', r'u"ab\"c"' )
		self._matchTest( parser, 'u"abc"113', 'u"abc"' )
		
		
	def testInteger(self):
		parser = integer
		self._matchTest( parser, "123", "123" )
		self._matchTest( parser, "-123", "-123" )
		
		

	
		
	def testFloatingPoint(self):
		parser = floatingPoint
		self._matchTest( parser, "3.14", "3.14" )
		self._matchTest( parser, "-3.14", "-3.14" )
		self._matchTest( parser, "3.", "3." )
		self._matchTest( parser, "-3.", "-3." )
		self._matchTest( parser, ".14", ".14" )
		self._matchTest( parser, "-.14", "-.14" )

		self._matchTest( parser, "3.14e5", "3.14e5" )
		self._matchTest( parser, "3.14e-5", "3.14e-5" )
		self._matchTest( parser, "-3.14e5", "-3.14e5" )
		self._matchTest( parser, "-3.14e-5", "-3.14e-5" )
		self._matchTest( parser, "3.e5", "3.e5" )
		self._matchTest( parser, "3.e-5", "3.e-5" )
		self._matchTest( parser, "-3.e5", "-3.e5" )
		self._matchTest( parser, "-3.e-5", "-3.e-5" )
		self._matchTest( parser, ".14e5", ".14e5" )
		self._matchTest( parser, ".14e-5", ".14e-5" )
		self._matchTest( parser, "-.14e5", "-.14e5" )
		self._matchTest( parser, "-.14e-5", "-.14e-5" )

		
		
	def testDelimitedList(self):
		parser = delimitedList( identifier )
		self._matchTest( parser, '', [] )
		self._matchTest( parser, 'ab', [ 'ab' ] )
		self._matchTest( parser, 'cd', [ 'cd' ] )
		self._matchTest( parser, 'ab,cd', [ 'ab', 'cd' ] )
		
		
	def testIndentedBlocksPrePass(self):
		src1 = '\n'.join( [
			"a",
			"b",
			"  c",
			"  d",
			"e",
			"f", ] )  +  '\n'
		
		expected1 = '\n'.join( [
			"a",
			"b",
			"  $<indent>$c",
			"  d",
			"$<dedent>$e",
			"f", ] )  +  '\n'
		
		
		src2 = '\n'.join( [
			"  a",
			"  b",
			"    c",
			"    d",
			"  e",
			"  f", ] )  +  '\n'
		
		expected2 = '\n'.join( [
			"  a",
			"  b",
			"    $<indent>$c",
			"    d",
			"  $<dedent>$e",
			"  f", ] )  +  '\n'
		

		src3 = '\n'.join( [
			"  a",
			"  b",
			"    c",
			"    d",
			"      e",
			"      f", ] )  +  '\n'
		
		expected3 = '\n'.join( [
			"  a",
			"  b",
			"    $<indent>$c",
			"    d",
			"      $<indent>$e",
			"      f",
			"",
			"$<dedent>$",
			"$<dedent>$", ] )  +  '\n'
		

		src4 = '\n'.join( [
			"  a",
			"  b",
			"    c",
			"    d",
			"      e",
			"      f",
			"  g",
			"  h", ] )  +  '\n'
		
		expected4 = '\n'.join( [
			"  a",
			"  b",
			"    $<indent>$c",
			"    d",
			"      $<indent>$e",
			"      f",
			"  $<dedent>$$<dedent>$g",
			"  h" ] )  +  '\n'
		
		self.assert_( indentedBlocksPrePass( src1 )  ==  expected1 )
		self.assert_( indentedBlocksPrePass( src2 )  ==  expected2 )
		self.assert_( indentedBlocksPrePass( src3 )  ==  expected3 )
		self.assert_( indentedBlocksPrePass( src4 )  ==  expected4 )
		
	
		
		
	def testNonRecursiveCalculator(self):
		integer = Word( string.digits )
		plus = Literal( '+' )
		minus = Literal( '-' )
		star = Literal( '*' )
		slash = Literal( '/' )
		
		addop = plus | minus
		mulop = star | slash
		
		def flattenAction(input, begin, end, x):
			y = []
			for a in x:
				y.extend( a )
			return y
			
			
		def action(input, start, end, x):
			if len( x ) == 1:
				return x[0]
			else:
				return [ x[0] ]  +  x[1]

				
		
		mul = Production( ( integer  +  ZeroOrMore( mulop + integer, True ).setAction( flattenAction ) ).setAction( action ) )
		add = Production( ( mul  +  ZeroOrMore( addop + mul, True ).setAction( flattenAction ) ).setAction( action ) )
		expr = add
		
		parser = expr
		
		self._matchTest( parser, '123', '123' )

		self._matchTest( parser, '1*2', [ '1', '*', '2' ] )
		self._matchTest( parser, '1*2*3', [ '1', '*', '2', '*', '3' ] )

		self._matchTest( parser, '1+2', [ '1', '+', '2' ] )
		self._matchTest( parser, '1+2+3', [ '1', '+', '2', '+', '3' ] )

		self._matchTest( parser, '1+2*3', [ '1', '+', [ '2', '*', '3' ] ] )
		self._matchTest( parser, '1*2+3', [ [ '1', '*',  '2' ], '+', '3' ] )

		self._matchTest( parser, '1+2*3+4', [ '1', '+', [ '2', '*', '3' ], '+', '4' ] )
		self._matchTest( parser, '1*2+3*4', [ [ '1', '*',  '2' ], '+', [ '3', '*', '4' ] ] )
		
		self._matchTest( parser, '0+1+2*3', [ '0', '+', '1', '+', [ '2', '*', '3' ] ] )
		self._matchTest( parser, '0*1*2+3', [ [ '0', '*', '1', '*',  '2' ], '+', '3' ] )
		
		
		
		
	def testLeftRecursion(self):
		integer = Word( string.digits )
		plus = Literal( '+' )
		minus = Literal( '-' )
		star = Literal( '*' )
		slash = Literal( '/' )
		
		addop = plus | minus
		mulop = star | slash
		
		def _flatten(x):
			y = []
			for a in x:
				y.extend( a )
			return y
			
			
		def action(input, start, end, x):
			if len( x ) == 1:
				return x[0]
			else:
				return [ x[0] ]  +  _flatten( x[1] )
				
		
		mul = Forward()
		add = Forward()
		mul  <<  Production( ( mul + mulop + integer )  |  integer )
		add  <<  Production( ( add + addop + mul )  |  mul )
		
		expr = add
		
		parser = expr
		
		self._matchTest( parser, '123', '123' )
		self._matchTest( parser, '1*2*3', [ [ '1', '*', '2' ], '*', '3' ] )
		self._matchTest( parser, '1+2+3', [ [ '1', '+', '2' ], '+', '3' ] )
		self._matchTest( parser, '1*2+3', [ [ '1', '*', '2' ], '+', '3' ] )
		self._matchTest( parser, '1+2*3', [ '1', '+', [ '2', '*', '3' ] ] )

		
		
		
	def testLeftRecursionJavaPrimary(self):
		primary = Forward()
		
		
		expression = Production( Literal( 'i' )  |  Literal( 'j' ) )
		methodName = Production( Literal( 'm' )  |  Literal( 'n' ) )
		interfaceTypeName = Production( Literal( 'I' )  |  Literal( 'J' ) )
		className = Production( Literal( 'C' )  |  Literal( 'D' ) )

		classOrInterfaceType = Production( className | interfaceTypeName )
		
		identifier = Production( Literal( 'x' )  |  Literal( 'y' )  |  classOrInterfaceType )
		expressionName = Production( identifier )
		
		arrayAccess = Production( ( primary + '[' + expression + ']' )   |   ( expressionName + '[' + expression + ']' ) )
		fieldAccess = Production( ( primary + '.' + identifier )   |   ( Literal( 'super' ) + '.' + identifier ) )
		methodInvocation = Production( ( primary + '.' + methodName + '()' )   |   ( methodName + '()' ) )
		
		classInstanceCreationExpression = Production( ( Literal( 'new' )  +  classOrInterfaceType  +  '()' )  |  ( primary + '.' + 'new' + identifier + '()' ) )
		
		primaryNoNewArray = Production( classInstanceCreationExpression | methodInvocation | fieldAccess | arrayAccess | 'this' )
		
		primary  <<  primaryNoNewArray
		
				
		parser = primary
		
		self._matchTest( parser, 'this', 'this' )
		self._matchTest( parser, 'this.x', [ 'this', '.', 'x' ] )
		self._matchTest( parser, 'this.x.y', [ [ 'this', '.', 'x' ], '.', 'y' ] )
		self._matchTest( parser, 'this.x.m()', [ [ 'this', '.', 'x' ], '.', 'm', '()' ] )
		self._matchTest( parser, 'x[i][j].y', [ [ [ 'x', '[', 'i', ']' ], '[', 'j', ']' ], '.', 'y' ] )
		
		
		
		
	def testSimpleMessagePassingGrammar(self):
		loadlLocal = Production( identifier )
		messageName = Production( identifier )
		plus = Literal( '+' )
		minus = Literal( '-' )
		star = Literal( '*' )
		slash = Literal( '/' )
		
		addop = plus | minus
		mulop = star | slash
				
		expression = Forward()
		parenExpression = Production( Literal( '(' )  +  expression  +  ')' )
		atom = Production( loadlLocal  |  parenExpression )

		parameterList = Production( delimitedList( expression ) )
		messageSend = Forward()
		messageSend  <<  Production( ( messageSend + '.' + messageName + '(' + parameterList + ')' )  |  atom )

		mul = Forward()
		mul  <<  Production( ( mul + mulop + messageSend )  |  messageSend )
		add = Forward()
		add  <<  Production( ( add   + addop + mul )  |  mul )
		expression  <<  Production( add )
		
		
		parser = expression
		
		self._matchTest( parser, 'self', 'self' )
		self._matchTest( parser, 'self.x()', [ 'self', '.', 'x', '(', [], ')' ] )
		self._matchTest( parser, 'self.x( a )', [ 'self', '.', 'x', '(', [ 'a' ], ')' ] )
		self._matchTest( parser, 'self.x( a, b.y() )', [ 'self', '.', 'x', '(', [ 'a', [ 'b', '.', 'y','(', [], ')' ] ], ')' ] )
		self._matchTest( parser, 'self.x( a, b.y() ).q()', [ [ 'self', '.', 'x', '(', [ 'a', [ 'b', '.', 'y','(', [], ')' ] ], ')' ], '.', 'q', '(', [], ')' ] )
		self._matchTest( parser, '(self)', [ '(', 'self', ')' ] )
		self._matchTest( parser, '(self.x())', [ '(', [ 'self', '.', 'x', '(', [], ')' ], ')' ] )
		self._matchTest( parser, 'x + y', [ 'x', '+', 'y' ] )
		self._matchTest( parser, 'x * y', [ 'x', '*', 'y' ] )
		self._matchTest( parser, 'x + y * z', [ 'x', '+', [ 'y', '*', 'z' ] ] )
		self._matchTest( parser, '(x + y * z).q()', [ [ '(', [ 'x', '+', [ 'y', '*', 'z' ] ], ')' ], '.', 'q', '(', [], ')' ] )
		self._matchTest( parser, 'x + y.f() * z', [ 'x', '+', [ [ 'y', '.', 'f', '(', [], ')' ], '*', 'z' ] ] )
		self._matchFailTest( parser, 'x + y.f() * z', '' )
		


	def testIndentedGrammar(self):
		loadlLocal = Production( identifier )
		messageName = Production( identifier )
		plus = Literal( '+' )
		minus = Literal( '-' )
		star = Literal( '*' )
		slash = Literal( '/' )
		
		addop = plus | minus
		mulop = star | slash
				
		expression = Forward()
		parenExpression = Production( Literal( '(' )  +  expression  +  ')' )
		atom = Production( loadlLocal  |  parenExpression )

		parameterList = Production( delimitedList( expression ) )
		messageSend = Forward()
		messageSend  <<  Production( ( messageSend + '.' + messageName + '(' + parameterList + ')' )  |  atom )

		mul = Forward()
		mul  <<  Production( ( mul + mulop + messageSend )  |  messageSend )
		add = Forward()
		add  <<  Production( ( add  + addop + mul )  |  mul )
		expression  <<  Production( add )
		
		
		singleStatement = Production( expression + Suppress( ';' ) ).setAction( lambda input, start, end, tokens: tokens[0] )

		statement = Forward()
		block = Production( ZeroOrMore( statement ) )
		compoundStatement = Production( Literal( '$<indent>$' )  +  block  +  Literal( '$<dedent>$' ) ).setAction( lambda input, start, end, tokens: tokens[1] )
		statement  <<  Production( compoundStatement  |  singleStatement )
		
		
		parser = block
		
		
		src1 = """
self.x();
		"""
		
		src2 = """
self.y();
   a.b();
   c.d();
		"""
		self._matchTest( parser, indentedBlocksPrePass( src1 ), [ [ 'self', '.', 'x', '(', [], ')' ] ] )
		self._matchTest( parser, indentedBlocksPrePass( src2 ), [ [ 'self', '.', 'y', '(', [], ')' ], [ [ 'a', '.', 'b', '(', [], ')' ], [ 'c', '.', 'd', '(', [], ')' ] ] ] )
		

