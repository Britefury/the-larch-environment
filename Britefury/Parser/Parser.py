##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************




class Node (object):
	def __init__(self):
		super( Node, self ).__init__()
		self._actionFn = None
		
	def match(self, input, start, stop):
		pass

	def _p_action(self, input):
		if self._actionFn is not None:
			return self._actionFn( input )
		else:
			return input
		
		
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
		return Choice( [ self, x ] )
	
	
	def _o_compare(self, x):
		return False

	

class Literal (Node):
	def __init__(self, matchString):
		super( Literal, self ).__init__()
		self._matchString = unicode( matchString )
		
	
	def match(self, input, start, stop):
		end = start + len( self._matchString )
		if end <= stop:
			x = input[start:end]
			if x == self._matchString:
				return ParseResult( self._p_action( x ), start, end )
		return ParseResult( None, start, stop )
		
	def _o_compare(self, x):
		return self._matchString  ==  x._matchString


class Word (Node):
	def __init__(self, initChars, bodyChars=None):
		super( Word, self ).__init__()
		if bodyChars is None:
			bodyChars = initChars
			initChars = None
		self._initChars = initChars
		self._bodyChars = bodyChars
		
	
	def match(self, input, start, stop):
		pos = start
		if self._initChars is not None:
			if input[start] not in self._initChars:
				return ParseResult( None, start, stop )
			pos = pos + 1
			
		end = stop
		for i in xrange( pos, stop ):
			c = input[i]
			if c not in self._bodyChars:
				end = i
				break
			
		if end == start:
			return ParseResult( None, start, stop )
		else:
			x = input[start:end]
			return ParseResult( self._p_action( x ), start, end )


	def _o_compare(self, x):
		return self._initChars  ==  x._initChars  and  self._bodyChars  ==  x._bodyChars
	
	
	
	
class Sequence (Node):
	def __init__(self, subexps):
		super( Sequence, self ).__init__()
		assert len( subexps ) > 0
		self._subexps = subexps
		
	
	def match(self, input, start, stop):
		subexpResults = [ None ] * len( self._subexps )
		
		pos = start
		for i, subexp in enumerate( self._subexps ):
			res = subexp.match( input, pos, stop )
			if res.result is None:
				return ParseResult( None, start, stop )
			subexpResults[i] = res.result
			pos = res.end
		
		return ParseResult( self._p_action( subexpResults ), start, pos )


	def __add__(self, x):
		return Sequence( self._subexps  +  [ x ] )
	
	
	def _o_compare(self, x):
		return self._subexps  ==  x._subexps


	
class Choice (Node):
	def __init__(self, subexps):
		super( Choice, self ).__init__()
		self._subexps = subexps
		
	
	def match(self, input, start, stop):
		for i, subexp in enumerate( self._subexps ):
			res = subexp.match( input, start, stop )
			if res.result is not None:
				return ParseResult( self._p_action( res.result ), start, res.end )
		
		return ParseResult( None, start, stop)

	
	def __or__(self, x):
		return Choice( self._subexps  +  [ x ] )
	
	
	def _o_compare(self, x):
		return self._subexps  ==  x._subexps



class _Repetition (Node):
	def __init__(self, subexp, min, max):
		super( _Repetition, self ).__init__()
		self._subexp = subexp
		self._min = min
		self._max = max
		
	
	def match(self, input, start, stop):
		subexpResults = []
		
		pos = start
		i = 0
		while self._max is None  or  i < self._max:
			res = self._subexp.match( input, pos, stop )
			if res.result is None:
				break
			subexpResults.append( res.result )
			pos = res.end
			i += 1
			
			
		if i < self._min  or  ( self._max is not None   and   i > self._max ):
			return ParseResult( None, start, stop )
		else:
			return ParseResult( self._p_action( subexpResults ), start, pos )
	
	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp  and  self._min  ==  x._min  and  self._max  ==  x._max


class Optional (_Repetition):
	def __init__(self, subexp):
		super( Optional, self ).__init__( subexp, 0, 1 )
		

class ZeroOrMore (_Repetition):
	def __init__(self, subexp):
		super( ZeroOrMore, self ).__init__( subexp, 0, None )
		

class OneOrMore (_Repetition):
	def __init__(self, subexp):
		super( OneOrMore, self ).__init__( subexp, 1, None )
		



class ParseResult (object):
	def __init__(self, result, begin, end):
		self.result = result
		self.begin = begin
		self.end = end


		
import unittest


class TestCase_Parser (unittest.TestCase):
	def _matchTest(self, parser, input, expected, begin=None, end=None):
		result = parser.match( input, 0, len( input ) )
		res = result.result
		if res != expected:
			print 'EXPECTED:'
			print expected
			print ''
			print 'RESULT:'
			print res
		self.assert_( res == expected )
		
		if begin is not None:
			self.assert_( begin == result.begin )
		if end is not None:
			self.assert_( end == result.end )
		
	
	def testLiteral(self):
		self.assert_( Literal( 'abc' )  ==  Literal( 'abc' ) )
		self.assert_( Literal( 'abc' )  !=  Literal( 'def' ) )
		self._matchTest( Literal( 'abcxyz' ), 'abcxyz', 'abcxyz', 0, 6 )
		self._matchTest( Literal( 'abcxyz' ), 'qwerty', None )
		self._matchTest( Literal( 'abcxyz' ), 'abcxyz123', 'abcxyz', 0, 6 )
		
	def testWord(self):
		self.assert_( Word( 'abc' )  ==  Word( 'abc' ) )
		self.assert_( Word( 'abc' )  !=  Word( 'def' ) )
		self._matchTest( Word( 'abc' ), 'aabbcc', 'aabbcc', 0, 6 )
		self._matchTest( Word( 'abc' ), 'aabbccxx', 'aabbcc', 0, 6 )
		self._matchTest( Word( 'abc' ), 'aabbccxxaa', 'aabbcc', 0, 6 )
		self._matchTest( Word( 'abc' ), 'a', 'a', 0, 1 )
		self._matchTest( Word( 'abc' ), 'x', None, 0, 1 )

		self.assert_( Word( 'abc', 'xyz' )  ==  Word( 'abc', 'xyz' ) )
		self.assert_( Word( 'abc', 'xyz' )  !=  Word( 'def', 'xyz' ) )
		self.assert_( Word( 'abc', 'xyz' )  !=  Word( 'abc', 'ijk' ) )
		self._matchTest( Word( 'abc', 'def' ), 'addeeff', 'addeeff', 0, 7 )
		self._matchTest( Word( 'abc', 'def' ), 'addeeffxx', 'addeeff', 0, 7 )
		self._matchTest( Word( 'abc', 'def' ), 'bddeeff', 'bddeeff', 0, 7 )
		self._matchTest( Word( 'abc', 'def' ), 'baddeeff', 'b', 0, 1 )
		self._matchTest( Word( 'abc', 'def' ), 'ddeeff', None, 0, 6 )

		
	def testSequence(self):
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Sequence( [ Literal( 'ab' ), Literal( 'qw' ) ] ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Sequence( [ Literal( 'qb' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  +  Literal( 'qw' )  +  Literal( 'fh' ) )

		parser = Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )
		self._matchTest( parser, 'abqwfh', [ 'ab', 'qw', 'fh' ], 0, 6 )
		self._matchTest( parser, 'abfh', None, 0, 4 )
	
		
	def testChoice(self):
		self.assert_( Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Choice( [ Literal( 'ab' ), Literal( 'qw' ) ] ) )
		self.assert_( Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Choice( [ Literal( 'qb' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  |  Literal( 'qw' )  |  Literal( 'fh' ) )

		parser = Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )
		self._matchTest( parser, 'ab', 'ab', 0, 2 )
		self._matchTest( parser, 'qw', 'qw', 0, 2 )
		self._matchTest( parser, 'fh', 'fh', 0, 2 )
		self._matchTest( parser, 'xy', None, 0, 2 )
		
		
	def testOptional(self):
		self.assert_( Optional( Literal( 'ab' ) )   ==   Optional( Literal( 'ab' ) ) )
		self.assert_( Optional( Literal( 'ab' ) )   !=   Optional( Literal( 'qb' ) ) )
		
		parser = Optional( Literal( 'ab' ) ) 
		self._matchTest( parser, '', [], 0, 0 )
		self._matchTest( parser, 'ab', [ 'ab' ], 0, 2 )
		self._matchTest( parser, 'abab', [ 'ab' ], 0, 2 )

		
	def testZeroOrMore(self):
		self.assert_( ZeroOrMore( Literal( 'ab' ) )   ==   ZeroOrMore( Literal( 'ab' ) ) )
		self.assert_( ZeroOrMore( Literal( 'ab' ) )   !=   ZeroOrMore( Literal( 'qb' ) ) )
		
		parser = ZeroOrMore( Literal( 'ab' ) ) 
		self._matchTest( parser, '', [], 0, 0 )
		self._matchTest( parser, 'ab', [ 'ab' ], 0, 2 )
		self._matchTest( parser, 'abab', [ 'ab', 'ab' ], 0, 4 )

		
	def testOneOrMore(self):
		self.assert_( OneOrMore( Literal( 'ab' ) )   ==   OneOrMore( Literal( 'ab' ) ) )
		self.assert_( OneOrMore( Literal( 'ab' ) )   !=   OneOrMore( Literal( 'qb' ) ) )
		
		parser = OneOrMore( Literal( 'ab' ) ) 
		self._matchTest( parser, '', None, 0, 0 )
		self._matchTest( parser, 'ab', [ 'ab' ], 0, 2 )
		self._matchTest( parser, 'abab', [ 'ab', 'ab' ], 0, 4 )
