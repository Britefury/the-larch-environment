##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.PatternMatch import PatternMatcher, Guard
from BritefuryJ.PatternMatch.Pattern import Anything


from Britefury.Dispatch.Dispatch import DispatchError



class PatternDispatch (object):
	def __init__(self, matcher):
		self._matcher = matcher
	
	
	def __call__(self, node, *args):
		try:
			if len( args ) == 0:
				return self._matcher.match( node )
			else:
				return self._matcher.match( node, args )
		except PatternMatcher.MatchFailureException:
			raise DispatchError

		
		
import unittest

class TestCase_PatternDispatch (unittest.TestCase):
	d = PatternDispatch( PatternMatcher( [ Guard( [ 'add', [ 'mul', 'a' << Anything(), [ 'sub', '1.0', 't' << Anything() ] ], [ 'mul', 'b' << Anything(), 't' << Anything() ] ], lambda node, a, b, t: [ 'add', a, [ 'mul', [ 'sub', b, a ], t ] ] ) ] ) )

	
	def test_PatternDispatchNoMatchSimple(self):
		self.assertRaises( DispatchError, lambda: self.d( [ 'hi', 'there' ] ) )
		
		
	def test_PatternDispatchNoMatchWrongName(self):
		self.assertRaises( DispatchError, lambda: self.d( [ 'add', [ 'mul', 'x', [ 'sub', '1.0', 'z' ] ], [ 'mul', 'y', 'w' ] ] ) )
		
		
	def test_PatternDispatch(self):
		self.assert_( self.d( [ 'add', [ 'mul', 'x', [ 'sub', '1.0', 'q' ] ], [ 'mul', 'y', 'q' ] ] )  ==  [ 'add', 'x', [ 'mul', [ 'sub', 'y', 'x' ], 'q' ] ] )
		
		
		