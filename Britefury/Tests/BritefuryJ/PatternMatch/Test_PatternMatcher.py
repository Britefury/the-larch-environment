##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.PatternMatch import PatternMatcher, Guard
from BritefuryJ.PatternMatch.Pattern import Anything


import unittest



class TestCase_PatternMatcher (unittest.TestCase):
	m = PatternMatcher( [ Guard( [ 'add', [ 'mul', 'a' << Anything(), [ 'sub', '1.0', 't' << Anything() ] ], [ 'mul', 'b' << Anything(), 't' << Anything() ] ], lambda node, a, b, t: [ 'add', a, [ 'mul', [ 'sub', b, a ], t ] ] ) ] )
	m2 = PatternMatcher( [ Guard( [ 'add', [ 'mul', 'a' << Anything(), [ 'sub', '1.0', 't' << Anything() ] ], [ 'mul', 'b' << Anything(), 't' << Anything() ] ], lambda p, q, r, node, a, b, t: [ p, q, r, [ 'add', a, [ 'mul', [ 'sub', b, a ], t ] ] ] ) ] )

	
	def test_NoMatchSimple(self):
		self.assertRaises( PatternMatcher.MatchFailureException, lambda: self.m.match( [ 'hi', 'there' ] ) )
		
		
	def test_NoMatchWrongName(self):
		self.assertRaises( PatternMatcher.MatchFailureException, lambda: self.m.match( [ 'add', [ 'mul', 'x', [ 'sub', '1.0', 'z' ] ], [ 'mul', 'y', 'w' ] ] ) )
		
		
	def test_PatternMatch(self):
		self.assert_( self.m.match( [ 'add', [ 'mul', 'x', [ 'sub', '1.0', 'q' ] ], [ 'mul', 'y', 'q' ] ] )  ==  [ 'add', 'x', [ 'mul', [ 'sub', 'y', 'x' ], 'q' ] ] )

		
	def test_PatternMatchWithArgs(self):
		self.assert_( self.m2.match( [ 'add', [ 'mul', 'x', [ 'sub', '1.0', 'q' ] ], [ 'mul', 'y', 'q' ] ],  ( 1, 2, 3 ) )  ==  [ 1, 2, 3, [ 'add', 'x', [ 'mul', [ 'sub', 'y', 'x' ], 'q' ] ] ] )
	