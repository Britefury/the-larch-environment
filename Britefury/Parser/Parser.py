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


def parserCoerce(x):
	if isinstance( x, ParserExpression ):
		return x
	elif isinstance( x, list )  or  isinstance( x, tuple ):
		return Sequence( parserCoerce( [ parserCoerce( a )   for a in x ] ) )
	else:
		return Literal( x )
	
	
	
def getErrorLine(source, pos):
	untilPos = source[:pos]
	line = untilPos.count( '\n' )
	return line,  source.split( '\n' )[line]
	



	
class ParseResult (object):
	__slots__ = [ 'result', 'begin', 'end', 'bindings', 'bSuppressed' ]
	
	"""
	Parse result
	Members:
	   result - the result (returned by the parse expression action function, otherwise a list/tree of strings)
	      None indicates an empty result. This is created by Peek expressions, and can be created by Repetition epxressions.
	   begin - the start index of the result, in the input string
	   end - the end index of the result, in the input string
	"""
	def __init__(self, result, begin, end, bindings={}, bSuppressed=False):
		self.result = result
		self.begin = begin
		self.end = end
		self.bindings = bindings
		self.bSuppressed = bSuppressed
		
		

		
		
		

class ParserExpression (object):
	"""Parser expression base class"""
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
		answer, pos = self.evaluate( state, input, start, stop )
		if answer is not None:
			answer.end = state.chomp( input, answer.end, stop )
			pos = answer.end
		return answer, pos
	

	
	def evaluate(self, state, input, start, stop):
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
	
	def __sub__(self, x):
		return Combine( [ self, x ] )
	
	def __or__(self, x):
		return Choice( [ self, x ] )
	
	def __pow__(self, name):
		return Bind( name, self )
	
	def __rshift__(self, f):
		return Action( self, f )
	
	def __and__(self, f):
		return Condition( self, f )
	
	
	def _o_compare(self, x):
		return False
	
	
	def _o_copySettingsFrom(self, x):
		pass
	
	def action(self, actionFn):
		return Action( self, actionFn )

	
	
class Bind (ParserExpression):
	"""
	Parser expression that binds the result of a sub-expression to a name
	"""
	def __init__(self, name, subexp):
		super( Bind, self ).__init__()
		self._name = name
		self._subexp = parserCoerce( subexp )
		
		
	def evaluate(self, state, input, start, stop):
		res, pos = self._subexp.evaluate( state, input, start, stop )
		if res is not None:
			b = copy( res.bindings )
			b[self._name] = res.result
			return ParseResult( res.result, start, pos, b ),  pos
		else:
			return res, pos

	
	def _o_compare(self, x):
		return self._subexp == x._subexp  and  self._name == x._name
	
	
	

class Action (ParserExpression):
	"""
	Parser expression that applies an action function to the results of parsing the sub-expression
	The function is of the form:
	f(inputText, position, subExpResult, <bound_vars>)  ->  result
	"""
	def __init__(self, subexp, actionFn):
		super( Action, self ).__init__()
		self._subexp = parserCoerce( subexp )
		self._actionFn = actionFn
		
		
	def evaluate(self, state, input, start, stop):
		res, pos = self._subexp.evaluate( state, input, start, stop )
		if res is not None:
			return ParseResult( self._actionFn( input, start, res.result, **res.bindings ), start, pos ),  pos
		else:
			return res, pos

	
	def _o_compare(self, x):
		return self._subexp == x._subexp  and  self._actionFn == x._actionFn
	
	
	


class Condition (ParserExpression):
	"""
	Parser expression that applies an condition function to the results of parsing the sub-expression
	The function is of the form:
	f(inputText, position, subExpResult, <bound_vars>)  ->  boolean
	"""
	def __init__(self, subexp, conditionFn):
		super( Condition, self ).__init__()
		self._subexp = parserCoerce( subexp )
		self._conditionFn = conditionFn
		
		
	def evaluate(self, state, input, start, stop):
		res, pos = self._subexp.evaluate( state, input, start, stop )
		if res is not None:
			if self._conditionFn( input, start, res.result, **res.bindings ):
				return res, pos
			else:
				return None, pos
		else:
			return res, pos

	
	def _o_compare(self, x):
		return self._subexp == x._subexp  and  self._conditionFn == x._conditionFn
	
	
	


class Forward (ParserExpression):
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
		

	def evaluate(self, state, input, start, stop):
		return self._subexp.evaluate( state, input, start, stop )

	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp 
	
	
	def __lshift__(self, subexp):
		"""
		Set the subexpression to be matched.
		"""
		self._subexp = parserCoerce( subexp )
		return self


	

class Group (ParserExpression):
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
		self._subexp = parserCoerce( subexp )
		

	def evaluate(self, state, input, start, stop):
		return self._subexp.evaluate( state, input, start, stop )

	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp 

	
	
	
class Production (Group):
	"""
	Production
	Group a subexpression into the right hand side of a grammar rule
	"""
	def evaluate(self, state, input, start, stop):
		# Invoke the memoisation and left-recursion handling system
		return state.memoisedMatch( self._subexp, input, start, stop )

	
	def action(self, actionFn):
		# Wrap the inner sub-expression in the Action expression
		return Production( Action( self._subexp, actionFn ) )
	

	

class Suppress (ParserExpression):
	"""
	Suppress
	Matches the sub-expression but returns an empty result
	Bindings pass through.
	"""
	def __init__(self, subexp):
		"""
		subexp - the sub-expression to be matched
		"""
		super( Suppress, self ).__init__()
		self._subexp = parserCoerce( subexp )
		

	def evaluate(self, state, input, start, stop):
		res, pos = self._subexp.evaluate( state, input, start, stop )
		if res is not None:
			return ParseResult( None, start, pos, res.bindings, True ),  pos
		else:
			return res, pos

	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp 

	
	
	

class Literal (ParserExpression):
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
		
	
	def evaluate(self, state, input, start, stop):
		start = state.chomp( input, start, stop )
		
		end = start + len( self._matchString )
		if end <= stop:
			sub = input[start:end]
			if sub == self._matchString:
				return ParseResult( sub, start, end ),  end
		return None, start
		
	def _o_compare(self, x):
		return self._matchString  ==  x._matchString




class Keyword (ParserExpression):
	"""
	Keyword
	
	Matches a supplied string, provided that it is not followed by a character from a specified list of characters.
	Useful to match a keyword such as 'while' while not matching when it is followed by other alpha-numeric characters,
	e.g. Keyword( 'while' ) would not match the first 5 characters of 'whilexyz'.
	
	The parse result is a string.
	"""
	
	def __init__(self, keyword, disallowedSubsequentChars=( string.ascii_letters + string.digits + '_' ) ):
		"""
		keyword - the keyword to match
		disallowedSubsequentChars - the list of characters that cannot follow @keyword
		"""
		super( Keyword, self ).__init__()
		self._keyword = unicode( keyword )
		self._disallowedSubsequentChars = unicode( disallowedSubsequentChars )
		
		
	def getKeyword(self):
		return self._keyword
		
	def getDisallowedSubsequentChars(self):
		return self._disallowedSubsequentChars
		
	
	def evaluate(self, state, input, start, stop):
		start = state.chomp( input, start, stop )
		
		end = start + len( self._keyword )
		if end <= stop:
			sub = input[start:end]
			if sub == self._keyword:
				if end == stop  or  input[end] not in self._disallowedSubsequentChars:
					return ParseResult( sub, start, end ),  end
		return None, start
		
	def _o_compare(self, x):
		return self._keyword  ==  x._keyword   and   self._disallowedSubsequentChars  ==  x._disallowedSubsequentChars




class RegEx (ParserExpression):
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
		
	
	def evaluate(self, state, input, start, stop):
		if self._bEatWhitespace:
			start = state.chomp( input, start, stop )
			
		m = self._re.match( input, start, stop )
		
		if m is not None:		
			matchString = m.group()
			if len( matchString ) > 0:
				end = start + len( matchString )
				return ParseResult( matchString, start, end ),  end
		return None, start
		
	def _o_compare(self, x):
		return self._re  ==  x._re


	
	
	
class Word (ParserExpression):
	"""
	Word
	
	Matches a word composed of only the specified characters

	The parse result is a string.
	"""
	
	def __init__(self, initChars, bodyChars=None):
		"""
		Word( chars )   -   matches a word composed of one or more characters from @chars
		Word( initChars, bodyChars )   -   matches a word composed of one character from @initChars, followed by zero or more from @bodyChars
		"""
		super( Word, self ).__init__()
		if bodyChars is None:
			bodyChars = initChars
			initChars = None

		if initChars is None:
			pattern = '[%s]+'  %  ( re.escape( bodyChars ), )
		else:
			pattern = '[%s][%s]*'  %  ( re.escape( initChars ), re.escape( bodyChars ) )
		self._re = re.compile( pattern, 0 )
		self._initChars = initChars
		self._bodyChars = bodyChars
		
		
	def getInitChars(self):
		return self._initChars
	
	def getBodyChars(self):
		return self._bodyChars
		
		
	def evaluate(self, state, input, start, stop):
		m = self._re.match( input, start, stop )
		
		if m is not None:		
			matchString = m.group()
			if len( matchString ) > 0:
				end = start + len( matchString )
				return ParseResult( matchString, start, end ),  end
		return None, start
		
	def _o_compare(self, x):
		return self._initChars  ==  x._initChars  and  self._bodyChars  ==  x._bodyChars



class Sequence (ParserExpression):
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
		self._subexps = [ parserCoerce( x )   for x in subexps ]
		
		
	def getSubExpressions(self):
		return self._subexps
		
	
	def evaluate(self, state, input, start, stop):
		subexpResults = []
		bindings = {}
		
		pos = start
		for i, subexp in enumerate( self._subexps ):
			if pos > stop:
				return None, pos
			res, pos = subexp.evaluate( state, input, pos, stop )
			if res is None:
				return None, pos
			else:
				bindings.update( res.bindings )
				if not res.bSuppressed:
					subexpResults.append( res.result )
		
		return ParseResult( subexpResults, start, pos, bindings ),  pos


	def __add__(self, x):
		s = Sequence( self._subexps  +  [ x ] )
		s._o_copySettingsFrom( self )
		return s
	
	
	def _o_compare(self, x):
		return self._subexps  ==  x._subexps


	
	
class Combine (ParserExpression):
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
		self._subexps = [ parserCoerce( x )   for x in subexps ]
		
		
	def getSubExpressions(self):
		return self._subexps
		
	
	def evaluate(self, state, input, start, stop):
		subexpResults = []
		bindings = {}
		
		pos = start
		for i, subexp in enumerate( self._subexps ):
			if pos > stop:
				return None, pos
			res, pos = subexp.evaluate( state, input, pos, stop )
			if res is None:
				return None, pos
			else:
				bindings.update( res.bindings )
				if not res.bSuppressed:
					subexpResults.append( res.result )
					
		result = reduce( operator.__add__, subexpResults )
		
		return ParseResult( result, start, pos, bindings ),  pos


	def __sub__(self, x):
		s = Combine( self._subexps  +  [ x ] )
		s._o_copySettingsFrom( self )
		return s
	
	
	def _o_compare(self, x):
		return self._subexps  ==  x._subexps


	
	
class Choice (ParserExpression):
	"""
	Choice
	
	Matches one of a list of sub-expressions
	The first sub-expression to match successfully is the one that is used.
	
	The parse result is the parse result of the successfully match sub-expression.
	"""
	def __init__(self, subexps):
		"""
		subexps - the sub expressions, one of which is to be matched
		"""
		super( Choice, self ).__init__()
		self._subexps = [ parserCoerce( x )   for x in subexps ]
		
	
	def evaluate(self, state, input, start, stop):
		maxErrorPos = start
		for i, subexp in enumerate( self._subexps ):
			res, pos = subexp.evaluate( state, input, start, stop )
			if res is not None:
				return res, pos
			else:
				maxErrorPos = max( maxErrorPos, pos )
			
		return None, maxErrorPos

	
	def __or__(self, x):
		f = Choice( self._subexps  +  [ x ] )
		f._o_copySettingsFrom( self )
		return f
	
	
	def _o_compare(self, x):
		return self._subexps  ==  x._subexps



class Optional (ParserExpression):
	"""
	Optional
	
	Optionally matches a sub-expression
	"""
	def __init__(self, subexp):
		"""
		subexp - the sub expression to match
		"""
		super( Optional, self ).__init__()
		self._subexp = parserCoerce( subexp )
		
	
	def evaluate(self, state, input, start, stop):
		res, pos = self._subexp.evaluate( state, input, start, stop )
		if res is None:
			return ParseResult( None, start, pos ),  pos
		else:
			return res, pos
	
	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp
		



class Repetition (ParserExpression):
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
		self._subexp = parserCoerce( subexp )
		self._min = min
		self._max = max
		self._bSuppressIfZero = bSuppressIfZero
		
	
	def evaluate(self, state, input, start, stop):
		subexpResults = []
		bindings = {}
		
		pos = start
		i = 0
		while pos <= stop  and  ( self._max is None  or  i < self._max ):
			res, pos = self._subexp.evaluate( state, input, pos, stop )
			if res is None:
				break
			else:
				bindings.update( res.bindings )
				if not res.bSuppressed:
					subexpResults.append( res.result )
			i += 1
			
			
		if i < self._min  or  ( self._max is not None   and   i > self._max ):
			return None, pos
		else:
			if self._bSuppressIfZero  and  i == 0:
				return ParseResult( None, start, pos, bindings ),  pos
			else:
				return ParseResult( subexpResults, start, pos, bindings ),  pos
	
	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp  and  self._min  ==  x._min  and  self._max  ==  x._max


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
	
	Bindings are blocked.
	"""
	def __init__(self, subexp):
		"""
		subexp - the sub-expression to match
		"""
		super( Peek, self ).__init__()
		self._subexp = parserCoerce( subexp )
		
	
	def evaluate(self, state, input, start, stop):
		res, pos = self._subexp.evaluate( state, input, start, stop )
		if res is not None:
			return ParseResult( None, start, start, {}, True ),  start
		else:
			return None, start

	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp 
	


class PeekNot (ParserExpression):
	"""
	PeekNot
	
	Matches anything but a specified sub-expression but does not consume it

	The parse result is an empty result.
	
	Bindings are blocked.
	"""
	def __init__(self, subexp):
		"""
		subexp - the sub-expression to not match
		"""
		super( PeekNot, self ).__init__()
		self._subexp = parserCoerce( subexp )
		
	
	def evaluate(self, state, input, start, stop):
		res, pos = self._subexp.evaluate( state, input, start, stop )
		if res is None:
			return ParseResult( None, start, start, {}, True ), start
		else:
			return None, start

	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp 
	

	
	



import unittest


class ParserTestCase (unittest.TestCase):
	def _matchTest(self, parser, input, expected, begin=None, end=None, ignoreChars=string.whitespace):
		result, pos = parser.parseString( input, ignoreChars=ignoreChars )
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
		result, pos = parser.parseString( input, ignoreChars=ignoreChars )
		if result is not None   and   result.end == len( input ):
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print result
		self.assert_( result is None  or  result.end != len( input ) )



class TestCase_Parser (ParserTestCase):
	def _bindingsTest(self, parser, input, expectedBindings, ignoreChars=string.whitespace):
		result, pos = parser.parseString( input, ignoreChars=ignoreChars )
		self.assert_( result is not None )
		if result.bindings != expectedBindings:
			print 'EXPECTED BINDINGS:'
			print expectedBindings
			print ''
			print 'RESULT BINDINGS:'
			print result.bindings
		self.assert_( result.bindings == expectedBindings )
		
				
	def testLiteral(self):
		self.assert_( Literal( 'abc' )  ==  Literal( 'abc' ) )
		self.assert_( Literal( 'abc' )  !=  Literal( 'def' ) )
		self._matchTest( Literal( 'abcxyz' ), 'abcxyz', 'abcxyz', 0, 6 )
		self._matchFailTest( Literal( 'abcxyz' ), 'qwerty' )
		self._matchTest( Literal( 'abcxyz' ), 'abcxyz123', 'abcxyz', 0, 6 )
		
		
	def testKeyword(self):
		self.assert_( Keyword( 'abc' )  ==  Keyword( 'abc' ) )
		self.assert_( Keyword( 'abc' )  !=  Keyword( 'def' ) )
		self.assert_( Keyword( 'abc', 'xyz' )  ==  Keyword( 'abc', 'xyz' ) )
		self.assert_( Keyword( 'abc', 'xyz' )  !=  Keyword( 'def', 'xyz' ) )
		self.assert_( Keyword( 'abc', 'xyz' )  !=  Keyword( 'abc', 'pqr' ) )
		self._matchTest( Keyword( 'hello' ), 'hello', 'hello', 0, 5 )
		self._matchFailTest( Keyword( 'hello' ), 'helloq' )
		self._matchTest( Keyword( 'hello', 'abc' ), 'hello', 'hello', 0, 5 )
		self._matchTest( Keyword( 'hello', 'abc' ), 'helloxx', 'hello', 0, 5 )
		self._matchFailTest( Keyword( 'hello', 'abc' ), 'helloaa' )
		
		
	def testRegEx(self):
		self.assert_( RegEx( r"[A-Za-z_][A-Za-z0-9]*" )  ==  RegEx( r"[A-Za-z_][A-Za-z0-9]*" ) )
		self.assert_( RegEx( r"[A-Za-z_][A-Za-z0-9]*" )  !=  RegEx( r"[A-Za-z_][A-Za-z0-9]*abc" ) )
		self._matchTest( RegEx( r"[A-Za-z_][A-Za-z0-9]*" ), 'abc123', 'abc123', 0, 6 )
		self._matchFailTest( RegEx( r"[A-Za-z_][A-Za-z0-9]*" ), '9abc' )
		self._matchTest( RegEx( r"[A-Za-z_][A-Za-z0-9]*" ), 'abcxyz...', 'abcxyz', 0, 6 )
		
		
	
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

		self._matchTest( Word( 'abc', '+*[]\\' ), 'a++**[[]]\\\\', 'a++**[[]]\\\\', 0, 11 )

		
	def testBind(self):
		self.assert_( Bind( 'x', 'abc' )  ==  Bind( 'x', 'abc' ) )
		self.assert_( Bind( 'x', 'abc' )  !=  Bind( 'y', 'abc' ) )
		self.assert_( Bind( 'x', 'abc' )  !=  Bind( 'x', 'def' ) )
		self.assert_( Bind( 'x', 'abc' )  ==  Literal( 'abc' )  **  'x' )
		
		parser = Literal( 'abc' )  **  'x'
		
		self._matchTest( parser, 'abc', 'abc', 0, 3 )
		self._bindingsTest( parser, 'abc', {'x' : 'abc'} )
		

	def testAction(self):
		f = lambda input, pos, res, x: x + x
		g = lambda input, pos, res, y: y
		self.assert_( Action( 'abc', f )  ==  Action( 'abc', f ) )
		self.assert_( Action( 'abc', f )  !=  Action( 'def', f ) )
		self.assert_( Action( 'abc', f )  !=  Action( 'abc', g ) )
		self.assert_( Action( 'abc', f )  ==  Literal( 'abc' )  >>  f )
		
		parser = ( Literal( 'abc' )  **  'x' )  >>  f
		
		self._matchTest( parser, 'abc', 'abcabc', 0, 3 )
		self._bindingsTest( parser, 'abc', {} )
		

	def testCondition(self):
		f = lambda input, pos, res: not res.startswith( 'hello' )
		g = lambda input, pos, res: not res.startswith( 'there' )
		self.assert_( Condition( 'abc', f )  ==  Condition( 'abc', f ) )
		self.assert_( Condition( 'abc', f )  !=  Condition( 'def', f ) )
		self.assert_( Condition( 'abc', f )  !=  Condition( 'abc', g ) )
		self.assert_( Condition( 'abc', f )  ==  Literal( 'abc' )  &  f )
		
		parser = Word( string.ascii_letters )  &  f
		
		self._matchTest( parser, 'abc', 'abc', 0, 3 )
		self._matchFailTest( parser, 'helloworld' )
		

	def testSequence(self):
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Sequence( [ Literal( 'ab' ), Literal( 'qw' ) ] ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Sequence( [ Literal( 'qb' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  +  Literal( 'qw' )  +  Literal( 'fh' ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  +  'qw'  +  'fh' )

		parser = Sequence( [ Literal( 'ab' ) ** 'x', Literal( 'qw' ) ** 'y', Literal( 'fh' ) ** 'z' ] )
		self._matchTest( parser, 'abqwfh', [ 'ab', 'qw', 'fh' ], 0, 6 )
		self._matchFailTest( parser, 'abfh' )
		self._bindingsTest( parser, 'abqwfh', { 'x':'ab',  'y':'qw',  'z':'fh' } )
	
		
	def testCombine(self):
		self.assert_( Combine( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Combine( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Combine( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Combine( [ Literal( 'ab' ), Literal( 'qw' ) ] ) )
		self.assert_( Combine( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Combine( [ Literal( 'qb' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )

		parser = Combine( [ Literal( 'ab' ) ** 'x', Literal( 'qw' ) ** 'y', Literal( 'fh' ) ** 'z' ] )
		self._matchTest( parser, 'abqwfh', 'abqwfh', 0, 6 )
		self._matchFailTest( parser, 'abfh' )
		self._bindingsTest( parser, 'abqwfh', { 'x':'ab',  'y':'qw',  'z':'fh' } )
	
		
	def testSuppress(self):
		self.assert_( Suppress( 'abc' )  ==  Suppress( 'abc' ) )
		self.assert_( Suppress( 'abc' )  !=  Suppress( 'def' ) )
		
		parser = Literal( 'ab' )  **  'x'  +  Suppress( Literal( 'cd' )  **  'y' )  +  Literal( 'ef' )  **  'z'
		
		self._matchTest( parser, 'abcdef', [ 'ab', 'ef' ], 0, 6 )
		self._matchFailTest( parser, 'abef' )
		self._bindingsTest( parser, 'abcdef', { 'x':'ab',  'y':'cd',  'z':'ef' } )
		

	def testChoice(self):
		self.assert_( Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Choice( [ Literal( 'ab' ), Literal( 'qw' ) ] ) )
		self.assert_( Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Choice( [ Literal( 'qb' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  |  Literal( 'qw' )  |  Literal( 'fh' ) )
		self.assert_( Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  |  'qw'  |  'fh' )

		parser = Choice( [ Literal( 'ab' ) ** 'x', Literal( 'qw' ) ** 'x', Literal( 'fh' ) ** 'x' ] )
		self._matchTest( parser, 'ab', 'ab', 0, 2 )
		self._matchTest( parser, 'qw', 'qw', 0, 2 )
		self._matchTest( parser, 'fh', 'fh', 0, 2 )
		self._matchFailTest( parser, 'xy' )
		self._matchTest( Literal( 'ab' )  |  Literal( 'abcd' ),   'ab', 'ab', 0, 2 )
		self._matchTest( Literal( 'ab' )  |  Literal( 'abcd' ),   'abcd', 'ab', 0, 2 )
		self._bindingsTest( parser, 'ab', { 'x':'ab' } )
		self._bindingsTest( parser, 'qw', { 'x':'qw' } )
		self._bindingsTest( parser, 'fh', { 'x':'fh' } )
		
		
	def testOptional(self):
		self.assert_( Optional( Literal( 'ab' ) )   ==   Optional( Literal( 'ab' ) ) )
		self.assert_( Optional( Literal( 'ab' ) )   !=   Optional( Literal( 'qb' ) ) )
		self.assert_( Optional( Literal( 'ab' ) )   ==   Optional( 'ab' ) )
		
		parser = Optional( Word( 'a', 'b' )  **  'x' )
		self._matchTest( parser, '', None, 0, 0 )
		self._matchTest( parser, 'abb', 'abb', 0, 3 )
		self._matchTest( parser, 'abbabb', 'abb', 0, 3 )
		self._bindingsTest( parser, '', {} )
		self._bindingsTest( parser, 'abb', { 'x':'abb' } )

		
	def testZeroOrMore(self):
		self.assert_( ZeroOrMore( Literal( 'ab' ) )   ==   ZeroOrMore( Literal( 'ab' ) ) )
		self.assert_( ZeroOrMore( Literal( 'ab' ) )   !=   ZeroOrMore( Literal( 'qb' ) ) )
		self.assert_( ZeroOrMore( Literal( 'ab' ) )   ==   ZeroOrMore( 'ab' ) )
		
		parser = ZeroOrMore( Word( 'a', 'b' ) ** 'x' + Word( 'c', 'd' ) ** 'y' )
		self._matchTest( parser, '', [], 0, 0 )
		self._matchTest( parser, 'abbcdd', [ [ 'abb', 'cdd' ] ], 0, 6 )
		self._matchTest( parser, 'abbcddabbbcddd', [ [ 'abb', 'cdd' ], [ 'abbb', 'cddd' ] ], 0, 14 )
		self._bindingsTest( parser, '', {} )
		self._bindingsTest( parser, 'abcdabbbcddd', { 'x':'abbb', 'y':'cddd' } )

		
	def testOneOrMore(self):
		self.assert_( OneOrMore( Literal( 'ab' ) )   ==   OneOrMore( Literal( 'ab' ) ) )
		self.assert_( OneOrMore( Literal( 'ab' ) )   !=   OneOrMore( Literal( 'qb' ) ) )
		self.assert_( OneOrMore( Literal( 'ab' ) )   ==   OneOrMore( 'ab' ) )
		
		parser = OneOrMore( Word( 'a', 'b' )  **  'x' ) 
		self._matchFailTest( parser, '' )
		self._matchTest( parser, 'abb', [ 'abb' ], 0, 3 )
		self._matchTest( parser, 'ababb', [ 'ab', 'abb' ], 0, 5 )
		self._bindingsTest( parser, 'ab', { 'x':'ab' } )
		self._bindingsTest( parser, 'ababbb', { 'x':'abbb' } )
		
		
	def testPeek(self):
		self.assert_( Peek( Literal( 'ab' ) )   ==   Peek( 'ab' ) )

		parser = OneOrMore( Literal( 'ab' ) )  +  Peek( Literal( 'cd' )  **  'x' )
		self._matchFailTest( parser, '' )
		self._matchFailTest( parser, 'ab' )
		self._matchFailTest( parser, 'abab' )
		self._matchTest( parser, 'abcd', [ [ 'ab' ] ], 0, 2 )
		self._matchTest( parser, 'ababcd', [ [ 'ab', 'ab' ] ], 0, 4 )
		self._bindingsTest( parser, 'abcd', {} )

		
	def testPeekNot(self):
		self.assert_( PeekNot( Literal( 'ab' ) )   ==   PeekNot( 'ab' ) )

		parser = OneOrMore( Literal( 'ab' ) )  +  PeekNot( Literal( 'cd' )  **  'x' )
		self._matchFailTest( parser, '' )
		self._matchTest( parser, 'ab', [ [ 'ab' ] ], 0, 2 )
		self._matchFailTest( parser, 'abcd' )
		self._matchTest( parser, 'abef', [ [ 'ab' ] ], 0, 2 )
		self._matchTest( parser, 'abab', [ [ 'ab', 'ab' ] ], 0, 4 )
		self._matchFailTest( parser, 'ababcd' )
		self._matchTest( parser, 'ababef', [ [ 'ab', 'ab' ] ], 0, 4 )
		self._bindingsTest( parser, 'abef', {} )
		
		
	def testNonRecursiveCalculator(self):
		integer = Word( string.digits )
		plus = Literal( '+' )
		minus = Literal( '-' )
		star = Literal( '*' )
		slash = Literal( '/' )
		
		addop = plus | minus
		mulop = star | slash
		
		def flattenAction(input, begin, x):
			y = []
			for a in x:
				y.extend( a )
			return y
			
			
		def action(input, start, x):
			if x[1] == []:
				return x[0]
			else:
				return [ x[0] ]  +  x[1]

				
		
		mul = Production( ( integer  +  ( ZeroOrMore( mulop + integer )  >>  flattenAction ) )  >>  action )
		add = Production( ( mul  +  ( ZeroOrMore( addop + mul )  >>   flattenAction ) )  >>  action )
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
		
		mul = Forward()
		mul  <<  Production( ( mul + mulop + integer )  |  integer )
		add = Forward()
		add  <<  Production( ( add + addop + mul )  |  mul )
		
		expr = add
		
		parser = expr
		
		self._matchTest( parser, '123', '123' )
		self._matchTest( parser, '1*2*3', [ [ '1', '*', '2' ], '*', '3' ] )
		self._matchTest( parser, '1+2+3', [ [ '1', '+', '2' ], '+', '3' ] )
		self._matchTest( parser, '1*2+3', [ [ '1', '*', '2' ], '+', '3' ] )
		self._matchTest( parser, '1+2*3', [ '1', '+', [ '2', '*', '3' ] ] )
		self._matchTest( parser, '1*2+3*4', [ [ '1', '*', '2' ], '+', [ '3', '*', '4' ] ] )
		self._matchTest( parser, '1+2*3+4', [ [ '1', '+', [ '2', '*', '3' ] ], '+', '4' ] )

		
		
		
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
		identifier = RegEx( "[A-Za-z_][A-Za-z0-9_]*" )

		def _listAction(input, begin, tokens):
			if tokens is None:
				return []
			else:
				return [ tokens[0] ]  +  [ x[1]   for x in tokens[1] ]
		
		def commaSeparatedList(subexp):
				return Optional( subexp  +  ZeroOrMore( parserCoerce( ',' )  +  subexp ) )  >>  _listAction
		
		
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

		parameterList = Production( commaSeparatedList( expression ) )
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
		


