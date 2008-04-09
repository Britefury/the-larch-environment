##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import string


def _parser_coerce(x):
	if isinstance( x, Node ):
		return x
	elif isinstance( x, list )  or  isinstance( x, tuple ):
		return Sequence( _parser_coerce( [ _parser_coerce( a )   for a in x ] ) )
	else:
		return Literal( x )


	
class ParseResult (object):
	def __init__(self, result, begin, end):
		self.result = result
		self.begin = begin
		self.end = end
		
		
		
		
		
class Node (object):
	def __init__(self):
		super( Node, self ).__init__()
		self._actionFn = None
		
		
	def setAction(self, f):
		self._actionFn = f
		return self
	
		
	def match(self, input, start, stop, context=None):
		if context is None:
			context = {}

		key = start, self
		try:
			return context[key]
		except KeyError:
			res = self._o_match( context, input, start, stop )
			context[key] = res
			return res
		
		
		
	def _o_match(self, context, input, start, stop):
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
	
	
	
class Forward (Node):
	def __init__(self):
		super( Forward, self ).__init__()
		self._subexp = None
		

	def _o_match(self, context, input, start, stop):
		return self._subexp.match( input, start, stop, context )

	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp 
	
	
	def __lshift__(self, subexp):
		self._subexp = subexp
		return self


	

class Literal (Node):
	def __init__(self, matchString):
		super( Literal, self ).__init__()
		self._matchString = unicode( matchString )
		
	
	def _o_match(self, context, input, start, stop):
		end = start + len( self._matchString )
		if end <= stop:
			x = input[start:end]
			if x == self._matchString:
				return ParseResult( self._p_action( x ), start, end )
		return None
		
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
		
	
	def _o_match(self, context, input, start, stop):
		pos = start
		if self._initChars is not None:
			if input[start] not in self._initChars:
				return None
			pos = pos + 1
			
		end = stop
		for i in xrange( pos, stop ):
			c = input[i]
			if c not in self._bodyChars:
				end = i
				break
			
		if end == start:
			return None
		else:
			x = input[start:end]
			return ParseResult( self._p_action( x ), start, end )


	def _o_compare(self, x):
		return self._initChars  ==  x._initChars  and  self._bodyChars  ==  x._bodyChars
	
	
	
	
class Sequence (Node):
	def __init__(self, subexps):
		super( Sequence, self ).__init__()
		assert len( subexps ) > 0
		self._subexps = [ _parser_coerce( x )   for x in subexps ]
		
	
	def _o_match(self, context, input, start, stop):
		subexpResults = []
		
		pos = start
		for i, subexp in enumerate( self._subexps ):
			res = subexp.match( input, pos, stop, context )
			if res is None:
				return None
			else:
				if res.result is not None:
					subexpResults.append( res.result )
			pos = res.end
		
		return ParseResult( self._p_action( subexpResults ), start, pos )


	def __add__(self, x):
		return Sequence( self._subexps  +  [ x ] )
	
	
	def _o_compare(self, x):
		return self._subexps  ==  x._subexps


	
class Choice (Node):
	def __init__(self, subexps):
		super( Choice, self ).__init__()
		self._subexps = [ _parser_coerce( x )   for x in subexps ]
		
	
	def _o_match(self, context, input, start, stop):
		for i, subexp in enumerate( self._subexps ):
			res = subexp.match( input, start, stop, context )
			if res is not None:
				return ParseResult( self._p_action( res.result ), start, res.end )
			
		return None

	
	def __or__(self, x):
		return Choice( self._subexps  +  [ x ] )
	
	
	def _o_compare(self, x):
		return self._subexps  ==  x._subexps



class _Repetition (Node):
	def __init__(self, subexp, min, max, bSuppressIfZero=False):
		super( _Repetition, self ).__init__()
		self._subexp = _parser_coerce( subexp )
		self._min = min
		self._max = max
		self._bSuppressIfZero = bSuppressIfZero
		
	
	def _o_match(self, context, input, start, stop):
		subexpResults = []
		
		pos = start
		i = 0
		while self._max is None  or  i < self._max:
			res = self._subexp.match( input, pos, stop, context )
			if res is None:
				break
			else:
				if res.result is not None:
					subexpResults.append( res.result )
			pos = res.end
			i += 1
			
			
		if i < self._min  or  ( self._max is not None   and   i > self._max ):
			return None
		else:
			if self._bSuppressIfZero  and  i == 0:
				return ParseResult( None, start, pos )
			else:
				return ParseResult( self._p_action( subexpResults ), start, pos )
	
	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp  and  self._min  ==  x._min  and  self._max  ==  x._max


class Optional (_Repetition):
	def __init__(self, subexp, bSuppressIfZero=True):
		super( Optional, self ).__init__( subexp, 0, 1, bSuppressIfZero )
		

class ZeroOrMore (_Repetition):
	def __init__(self, subexp, bSuppressIfZero=False):
		super( ZeroOrMore, self ).__init__( subexp, 0, None, bSuppressIfZero )
		

class OneOrMore (_Repetition):
	def __init__(self, subexp):
		super( OneOrMore, self ).__init__( subexp, 1, None )
		
		
		
class LookAhead (Node):
	def __init__(self, subexp):
		super( LookAhead, self ).__init__()
		self._subexp = _parser_coerce( subexp )
		
	
	def _o_match(self, context, input, start, stop):
		res = self._subexp.match( input, start, stop, context )
		if res is not None:
			return ParseResult( None, start, start )
		else:
			return None

	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp 
	


class LookAheadNot (Node):
	def __init__(self, subexp):
		super( LookAheadNot, self ).__init__()
		self._subexp = _parser_coerce( subexp )
		
	
	def _o_match(self, context, input, start, stop):
		res = self._subexp.match( input, start, stop, context )
		if res is None  or  res.result is None:
			return ParseResult( None, start, start )
		else:
			return None

	
	def _o_compare(self, x):
		return self._subexp  ==  x._subexp 
	

	
	
def delimitedList(subexp, delimiter=','):
	return ( _parser_coerce( subexp )  +  ZeroOrMore( _parser_coerce( delimiter )  +  subexp ) ).setAction( lambda x: [ x[0] ]  +  [ a[1]   for a in x[1] ] )

identifier = Word( string.ascii_letters  +  '_',  string.ascii_letters + string.digits + '_' )



import unittest


class TestCase_Parser (unittest.TestCase):
	def _matchTest(self, parser, input, expected, begin=None, end=None):
		result = parser.match( input, 0, len( input ) )
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
		
	
	def _matchFailTest(self, parser, input):
		result = parser.match( input, 0, len( input ) )
		if result is not None:
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print res
		self.assert_( result is None )
		
				
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

		
	def testSequence(self):
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Sequence( [ Literal( 'ab' ), Literal( 'qw' ) ] ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Sequence( [ Literal( 'qb' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  +  Literal( 'qw' )  +  Literal( 'fh' ) )
		self.assert_( Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  +  'qw'  +  'fh' )

		parser = Sequence( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )
		self._matchTest( parser, 'abqwfh', [ 'ab', 'qw', 'fh' ], 0, 6 )
		self._matchFailTest( parser, 'abfh' )
	
		
	def testChoice(self):
		self.assert_( Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Choice( [ Literal( 'ab' ), Literal( 'qw' ) ] ) )
		self.assert_( Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   !=   Choice( [ Literal( 'qb' ), Literal( 'qw' ), Literal( 'fh' ) ] ) )
		self.assert_( Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  |  Literal( 'qw' )  |  Literal( 'fh' ) )
		self.assert_( Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )   ==   Literal( 'ab' )  |  'qw'  |  'fh' )

		parser = Choice( [ Literal( 'ab' ), Literal( 'qw' ), Literal( 'fh' ) ] )
		self._matchTest( parser, 'ab', 'ab', 0, 2 )
		self._matchTest( parser, 'qw', 'qw', 0, 2 )
		self._matchTest( parser, 'fh', 'fh', 0, 2 )
		self._matchFailTest( parser, 'xy' )
		
		
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
		
		
	def testLookAhead(self):
		self.assert_( LookAhead( Literal( 'ab' ) )   ==   LookAhead( 'ab' ) )

		parser = OneOrMore( Literal( 'ab' ) )  +  LookAhead( Literal( 'cd' ) )
		self._matchFailTest( parser, '' )
		self._matchFailTest( parser, 'ab' )
		self._matchFailTest( parser, 'abab' )
		self._matchTest( parser, 'abcd', [ [ 'ab' ] ], 0, 2 )
		self._matchTest( parser, 'ababcd', [ [ 'ab', 'ab' ] ], 0, 4 )

		
	def testLookAheadNot(self):
		self.assert_( LookAheadNot( Literal( 'ab' ) )   ==   LookAheadNot( 'ab' ) )

		parser = OneOrMore( Literal( 'ab' ) )  +  LookAheadNot( Literal( 'cd' ) )
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
	
		
	def testDelimitedList(self):
		parser = delimitedList( identifier )
		self._matchTest( parser, 'ab', [ 'ab' ] )
		self._matchTest( parser, 'cd', [ 'cd' ] )
		self._matchTest( parser, 'ab,cd', [ 'ab', 'cd' ] )
		
	
		
	def testCalculator(self):
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
			
			
		def makeAction(name):
			def action(x):
				if len( x ) == 1:
					return x[0]
				else:
					return [ x[0] ]  +  _flatten( x[1] )

			def namedAction(x):
				if len( x ) == 1:
					return [ name, x[0] ]
				else:
					return [ name ]  +  [ x[0] ]  +  _flatten( x[1] )
			return action
				
		
		mul = Forward()
		mul  <<  ( integer  +  ZeroOrMore( mulop + integer, True ) ).setAction( makeAction( 'mul' ) )
		add = ( mul  +  ZeroOrMore( addop + mul, True ) ).setAction( makeAction( 'add' ) )
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
