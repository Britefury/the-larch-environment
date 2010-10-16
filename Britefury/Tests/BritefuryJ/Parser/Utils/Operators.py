##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

from BritefuryJ.Parser import *
from BritefuryJ.Parser.Utils import *
from BritefuryJ.Parser.Utils.OperatorParser import PrefixLevel, SuffixLevel, InfixLeftLevel, InfixRightLevel, InfixChainLevel, OperatorTable, UnaryOperator, BinaryOperator, ChainOperator
from BritefuryJ.Parser.Utils.Tokens import identifier


from Britefury.Tests.BritefuryJ.Parser.ParserTestCase import ParserTestCase




def buildOperatorParser(operatorTable, rootParser):
	return OperatorTable( [ level   for level in operatorTable ], rootParser ).buildParsers()[-1]






#
#
#
#
# THE UNIT TESTS ARE SPECIFIED BELOW
# The specification is processed by a function defined below that generates a class that a unit testing class can derive from.
#
#
#
#

_unitTestSpecification = """
-- prefix :			[ self._prefixLvl( [ self._unOp( '~' ) ] ) ] 
		       a			->			a
		       ~a			->			[~ a]
		       ~~a			->			[~ [~ a]]
-- suffix :			[ self._suffixLvl( [ self._unOp( '!' ) ] ) ] 
		       a			->			a
		       a!			->			[! a]
		       a!!			->			[! [! a]]
-- left :			[ self._infixLeftLvl( [ self._binOp( '*' ) ] ) ]
		       a			->			a
		       a * b			->			[* a b]
		       a * b * c		->			[* [* a b] c]
-- right :			[ self._infixRightLvl( [ self._binOp( '$' ) ] ) ]
		       a			->			a
		       a $ b			->			[$ a b]
		       a $ b $ c		->			[$ a [$ b c]]


-- prefixprefix :		[ self._prefixLvl( [ self._unOp( '~' ), self._unOp( '!' ) ] ) ] 
		       a			->			a
		       ~a			->			[~ a]
		       !a			->			[! a]
		       ~!a			->			[~ [! a]]
		       !~a			->			[! [~ a]]
-- suffixsuffix :		[ self._suffixLvl( [ self._unOp( '~' ), self._unOp( '!' ) ] ) ] 
		       a			->			a
		       a~			->			[~ a]
		       a!			->			[! a]
		       a!~			->			[~ [! a]]
		       a~!			->			[! [~ a]]
-- leftleft :		[ self._infixLeftLvl( [ self._binOp( '*' ), self._binOp( '/' ) ] ) ] 
		       a			->			a
		       a * b			->			[* a b]
		       a / b			->			[/ a b]
		       a * b / c * d	->			[* [/ [* a b] c] d]
		       a / b * c / d	->			[/ [* [/ a b] c] d]
-- rightright :		[ self._infixRightLvl( [ self._binOp( '$' ), self._binOp( '@' ) ] ) ] 
		       a			->			a
		       a $ b			->			[$ a b]
		       a @ b		->			[@ a b]
		       a $ b @ c $ d	->			[$ a [@ b [$ c d]]]
		       a @ b $ c @ d	->			[@ a [$ b [@ c d]]]


-- prefix_prefix :	[ self._prefixLvl( [ self._unOp( '~' ) ] ), self._prefixLvl( [ self._unOp( '!' ) ] ) ]
		       a			->			a
		       ~a			->			[~ a]
		       !a			->			[! a]
		       ~!a			->			<<ERROR>>
		       !~a			->			[! [~ a]]
		       !!~~a		->			[! [! [~ [~ a]]]]
		       !~!~a		->			<<ERROR>>
-- prefix_sufffix :	[ self._prefixLvl( [ self._unOp( '~' ) ] ), self._suffixLvl( [ self._unOp( '!' ) ] ) ]
		       a			->			a
		       ~a			->			[~ a]
		       a!			->			[! a]
		       ~a!			->			[! [~ a]]
		       ~~a!!		->			[! [! [~ [~ a]]]]
-- prefix_left :		[ self._prefixLvl( [ self._unOp( '~' ) ] ), self._infixLeftLvl( [ self._binOp( '*' ) ] ) ]
		       a			->			a
		       ~a			->			[~ a]
		       a * b * c		->			[* [* a b] c]
		       ~a * b * c		->			[* [* [~ a] b] c]
		       a * ~b * c		->			[* [* a [~ b]] c]
		       a * b * ~c		->			[* [* a b] [~ c]]
-- prefix_right :		[ self._prefixLvl( [ self._unOp( '~' ) ] ), self._infixRightLvl( [ self._binOp( '$' ) ] ) ]
		       a			->			a
		       ~a			->			[~ a]
		       a $ b $ c		->			[$ a [$ b c]]
		       ~a $ b $ c		->			[$ [~ a] [$ b c]]
		       a $ ~b $ c		->			[$ a [$ [~ b] c]]
		       a $ b $ ~c		->			[$ a [$ b [~ c]]]
-- suffix_prefix :	[ self._suffixLvl( [ self._unOp( '!' ) ] ), self._prefixLvl( [ self._unOp( '~' ) ] ) ]
		       a			->			a
		       ~a			->			[~ a]
		       a!			->			[! a]
		       ~a!			->			[~ [! a]]
		       ~~a!!		->			[~ [~ [! [! a]]]]
-- suffix_suffix :	[ self._suffixLvl( [ self._unOp( '~' ) ] ), self._suffixLvl( [ self._unOp( '!' ) ] ) ]
		       a			->			a
		       a~			->			[~ a]
		       a!			->			[! a]
		       a~!			->			[! [~ a]]
		       a!~			->			<<ERROR>>
		       a~~!!		->			[! [! [~ [~ a]]]]
		       a~!~!		->			<<ERROR>>
-- suffix_left :		[ self._suffixLvl( [ self._unOp( '!' ) ] ), self._infixLeftLvl( [ self._binOp( '*' ) ] ) ]
		       a			->			a
		       a!			->			[! a]
		       a * b * c		->			[* [* a b] c]
		       a! * b * c		->			[* [* [! a] b] c]
		       a * b! * c		->			[* [* a [! b]] c]
		       a * b * c!		->			[* [* a b] [! c]]
-- suffix_right :		[ self._suffixLvl( [ self._unOp( '!' ) ] ), self._infixRightLvl( [ self._binOp( '$' ) ] ) ]
		       a			->			a
		       a!			->			[! a]
		       a $ b $ c		->			[$ a [$ b c]]
		       a! $ b $ c		->			[$ [! a] [$ b c]]
		       a $ b! $ c		->			[$ a [$ [! b] c]]
		       a $ b $ c!		->			[$ a [$ b [! c]]]
-- left_prefix :		[ self._infixLeftLvl( [ self._binOp( '*' ) ] ), self._prefixLvl( [ self._unOp( '~' ) ] ) ]
		       a			->			a
		       ~a			->			[~ a]
		       a * b * c		->			[* [* a b] c]
		       ~a * b * c		->			[~ [* [* a b] c]]
		       a * ~b * c		->			[* a [~ [* b c]]]
		       a * b * ~c		->			[* [* a b] [~ c]]
-- left_suffix :		[ self._infixLeftLvl( [ self._binOp( '*' ) ] ), self._suffixLvl( [ self._unOp( '!' ) ] ) ]
		       a			->			a
		       a!			->			[! a]
		       a * b * c		->			[* [* a b] c]
		       a! * b * c		->			<<ERROR>>
		       a * b! * c		->			<<ERROR>>
		       a * b * c!		->			[! [* [* a b] c]]
-- left_left :		[ self._infixLeftLvl( [ self._binOp( '*' ) ] ), self._infixLeftLvl( [ self._binOp( '+' ) ] ) ]
		       a			->			a
		       a * b			->			[* a b]
		       a + b			->			[+ a b]
		       a * b + c		->			[+ [* a b] c]
		       a + b * c		->			[+ a [* b c]]
		       a * b + c * d	->			[+ [* a b] [* c d]]
		       a + b * c + d	->			[+ [+ a [* b c]] d]
-- left_right :		[ self._infixLeftLvl( [ self._binOp( '*' ) ] ), self._infixRightLvl( [ self._binOp( '$' ) ] ) ]
		       a			->			a
		       a * b			->			[* a b]
		       a $ b			->			[$ a b]
		       a * b * c * d	->			[* [* [* a b] c] d]
		       a * b * c $ d	->			[$ [* [* a b] c] d]
		       a * b $ c * d	->			[$ [* a b] [* c d]]
		       a $ b * c * d	->			[$ a [* [* b c] d]]
		       a $ b $ c $ d	->			[$ a [$ b [$ c d]]]
		       a * b $ c $ d	->			[$ [* a b] [$ c d]]
		       a $ b * c $ d	->			[$ a [$ [* b c] d]]
		       a $ b $ c * d	->			[$ a [$ b [* c d]]]
-- right_prefix :		[ self._infixRightLvl( [ self._binOp( '$' ) ] ), self._prefixLvl( [ self._unOp( '~' ) ] ) ]
		       a			->			a
		       ~a			->			[~ a]
		       a $ b $ c		->			[$ a [$ b c]]
		       ~a $ b $ c		->			[~ [$ a [$ b c]]]
		       a $ ~b $ c		->			[$ a [~ [$ b c]]]
		       a $ b $ ~c		->			[$ a [$ b [~ c]]]
-- right_suffix :		[ self._infixRightLvl( [ self._binOp( '$' ) ] ), self._suffixLvl( [ self._unOp( '!' ) ] ) ]
		       a			->			a
		       a!			->			[! a]
		       a $ b $ c		->			[$ a [$ b c]]
		       a! $ b $ c		->			<<ERROR>>
		       a $ b! $ c		->			<<ERROR>>
		       a $ b $ c!		->			[! [$ a [$ b c]]]
-- right_left :		[ self._infixRightLvl( [ self._binOp( '$' ) ] ), self._infixLeftLvl( [ self._binOp( '*' ) ] ) ]
		       a			->			a
		       a $ b			->			[$ a b]
		       a * b			->			[* a b]
		       a $ b $ c $ d	->			[$ a [$ b [$ c d]]]
		       a * b $ c $ d	->			[* a [$ b [$ c d]]]
		       a $ b * c $ d	->			[* [$ a b] [$ c d]]
		       a $ b $ c * d	->			[* [$ a [$ b c ]] d]
		       a * b * c * d	->			[* [* [* a b] c] d]
		       a * b * c $ d	->			[* [* a b] [$ c d]]
		       a * b $ c * d	->			[* [* a [$ b c]] d]
		       a $ b * c * d	->			[* [* [$ a b] c] d]
-- right_right :		[ self._infixRightLvl( [ self._binOp( '$' ) ] ), self._infixRightLvl( [ self._binOp( '@' ) ] ) ]
		       a			->			a
		       a $ b			->			[$ a b]
		       a @ b		->			[@ a b]
		       a $ b @ c		->			[@ [$ a b] c]
		       a @ b $ c		->			[@ a [$ b c]]
		       a $ b $ c $ d	->			[$ a [$ b [$ c d]]]
		       a $ b @ c $ d	->			[@ [$ a b] [$ c d]]


-- prefix_left_left :	[ self._prefixLvl( [ self._unOp( '~' ) ] ),  self._infixLeftLvl( [ self._binOp( '*' ) ] ),  self._infixLeftLvl( [ self._binOp( '+' ) ] ) ]
		       a			->			a
		       ~a			->			[~ a]
		       a * b			->			[* a b]
		       a + b			->			[+ a b]
		       a * b * c * d	->			[* [* [* a b] c] d]
		       a * b + c * d	->			[+ [* a b] [* c d]]
		       ~a * ~b * ~c * ~d	->		[* [* [* [~ a] [~ b]] [~ c]] [~ d]]
		       ~a * ~b + ~c * ~d	->		[+ [* [~ a] [~ b]] [* [~ c] [~ d]]]
-- left_prefix_left :	[ self._infixLeftLvl( [ self._binOp( '*' ) ] ),  self._prefixLvl( [ self._unOp( '~' ) ] ),  self._infixLeftLvl( [ self._binOp( '+' ) ] ) ]
		       a			->			a
		       ~a			->			[~ a]
		       a * b			->			[* a b]
		       a + b			->			[+ a b]
		       a * b * c * d	->			[* [* [* a b] c] d]
		       a * b + c * d	->			[+ [* a b] [* c d]]
		       ~a * b + c * d	->			[+ [~ [* a b]] [* c d]]
		       a * b + ~c * d	->			[+ [* a b] [~ [* c d]]]
		       a * ~b * c * d	->			[* a [~ [* [* b c] d]]]
		       a * ~b * c + d	->			[+ [* a [~ [* b c]]] d]
		       ~a * b * c + d	->			[+ [~ [* [* a b] c]] d]
-- left_left_prefix :	[ self._infixLeftLvl( [ self._binOp( '*' ) ] ),  self._infixLeftLvl( [ self._binOp( '+' ) ] ),  self._prefixLvl( [ self._unOp( '~' ) ] ) ]
		       a			->			a
		       ~a			->			[~ a]
		       a * b			->			[* a b]
		       a + b			->			[+ a b]
		       a * b * c * d	->			[* [* [* a b] c] d]
		       a * b + c * d	->			[+ [* a b] [* c d]]
		       ~a * b + c * d	->			[~ [+ [* a b] [* c d]]]
		       a * ~b + c * d	->			[* a [~ [+ b [* c d]]]]
		       a * b + ~c * d	->			[+ [* a b] [~ [* c d]]]

-- suffix_left_left :	[ self._suffixLvl( [ self._unOp( '!' ) ] ),  self._infixLeftLvl( [ self._binOp( '*' ) ] ),  self._infixLeftLvl( [ self._binOp( '+' ) ] ) ]
		       a			->			a
		       a!			->			[! a]
		       a * b			->			[* a b]
		       a + b			->			[+ a b]
		       a * b * c * d	->			[* [* [* a b] c] d]
		       a! * b! * c! * d!	->			[* [* [* [! a] [! b]] [! c]] [! d]]
		       a! * b! + c! * d!	->			[+ [* [! a] [! b]] [* [! c] [! d]]]
-- left_suffix_left :	[ self._infixLeftLvl( [ self._binOp( '*' ) ] ),  self._suffixLvl( [ self._unOp( '!' ) ] ),  self._infixLeftLvl( [ self._binOp( '+' ) ] ) ]
		       a			->			a
		       a!			->			[! a]
		       a * b			->			[* a b]
		       a + b			->			[+ a b]
		       a * b * c * d	->			[* [* [* a b] c] d]
		       a * b + c * d	->			[+ [* a b] [* c d]]
		       a * b! + c * d	->			[+ [! [* a b]] [* c d]]
		       a * b + c * d!	->			[+ [* a b] [! [* c d]]]
		       a * b * c! * d	->			<<ERROR>>
		       a + b * c! * d	->			<<ERROR>>
		       a + b * c * d!	->			[+ a [! [* [* b c] d]]]
-- left_left_suffix :	[ self._infixLeftLvl( [ self._binOp( '*' ) ] ),  self._infixLeftLvl( [ self._binOp( '+' ) ] ),  self._suffixLvl( [ self._unOp( '!' ) ] ) ]
		       a			->			a
		       a!			->			[! a]
		       a * b			->			[* a b]
		       a + b			->			[+ a b]
		       a * b * c * d	->			[* [* [* a b] c] d]
		       a * b + c * d	->			[+ [* a b] [* c d]]
		       a * b + c * d!	->			[! [+ [* a b] [* c d]]]
		       a * b + c! * d	->			<<ERROR>>
		       a * b! + c * d	->			<<ERROR>>


-- prefix_right_right :	[ self._prefixLvl( [ self._unOp( '~' ) ] ),  self._infixRightLvl( [ self._binOp( '$' ) ] ),  self._infixRightLvl( [ self._binOp( '@' ) ] ) ]
		       a			->			a
		       ~a			->			[~ a]
		       a $ b			->			[$ a b]
		       a @ b		->			[@ a b]
		       a $ b $ c $ d	->			[$ a [$ b [$ c d]]]
		       ~a $ ~b $ ~c $ ~d	->		[$ [~ a] [$ [~ b] [$ [~ c] [~ d]]]]
		       ~a $ ~b @ ~c $ ~d	->		[@ [$ [~ a] [~ b]] [$ [~ c] [~ d]]]
-- right_prefix_right :	[ self._infixRightLvl( [ self._binOp( '$' ) ] ),  self._prefixLvl( [ self._unOp( '~' ) ] ),  self._infixRightLvl( [ self._binOp( '@' ) ] ) ]
		       a			->			a
		       ~a			->			[~ a]
		       a $ b			->			[$ a b]
		       a @ b		->			[@ a b]
		       a $ b $ c $ d	->			[$ a [$ b [$ c d]]]
		       a $ b @ c $ d	->			[@ [$ a b] [$ c d]]
		       ~a $ b @ c $ d	->			[@ [~ [$ a b]] [$ c d]]
		       a $ b @ ~c $ d	->			[@ [$ a b] [~ [$ c d]]]
		       a $ ~b $ c $ d	->			[$ a [~ [$ b [$ c d]]]]
		       a $ ~b $ c @ d	->			[@ [$ a [~ [$ b c]]] d]
		       ~a $ b $ c @ d	->			[@ [~ [$ a [$ b c]]] d]
-- right_right_prefix :	[ self._infixRightLvl( [ self._binOp( '$' ) ] ),  self._infixRightLvl( [ self._binOp( '@' ) ] ),  self._prefixLvl( [ self._unOp( '~' ) ] ) ]
		       a			->			a
		       ~a			->			[~ a]
		       a $ b			->			[$ a b]
		       a @ b		->			[@ a b]
		       a $ b $ c $ d	->			[$ a [$ b [$ c d]]]
		       a $ b @ c $ d	->			[@ [$ a b] [$ c d]]
		       ~a $ b @ c $ d	->			[~ [@ [$ a b] [$ c d]]]
		       a $ ~b @ c $ d	->			[$ a [~ [@ b [$ c d]]]]
		       a $ b @ ~c $ d	->			[@ [$ a b] [~ [$ c d]]]

-- suffix_right_right :	[ self._suffixLvl( [ self._unOp( '!' ) ] ),  self._infixRightLvl( [ self._binOp( '$' ) ] ),  self._infixRightLvl( [ self._binOp( '@' ) ] ) ]
		       a			->			a
		       a!			->			[! a]
		       a $ b			->			[$ a b]
		       a @ b		->			[@ a b]
		       a $ b $ c $ d	->			[$ a [$ b [$ c d]]]
		       a $ b @ c $ d	->			[@ [$ a b] [$ c d]]
		       a! $ b! $ c! $ d!	->			[$ [! a] [$ [! b] [$ [! c] [! d]]]]
		       a! $ b! @ c! $ d!	->		[@ [$ [! a] [! b]] [$ [! c] [! d]]]
-- right_suffix_right :	[ self._infixRightLvl( [ self._binOp( '$' ) ] ),  self._suffixLvl( [ self._unOp( '!' ) ] ),  self._infixRightLvl( [ self._binOp( '@' ) ] ) ]
		       a			->			a
		       a!			->			[! a]
		       a $ b			->			[$ a b]
		       a @ b		->			[@ a b]
		       a $ b $ c $ d	->			[$ a [$ b [$ c d]]]
		       a $ b @ c $ d	->			[@ [$ a b] [$ c d]]
		       a $ b! @ c $ d	->			[@ [! [$ a b]] [$ c d]]
		       a $ b @ c $ d!	->			[@ [$ a b] [! [$ c d]]]
		       a $ b $ c! $ d	->			<<ERROR>>
		       a @ b $ c! $ d	->			<<ERROR>>
		       a @ b $ c $ d!	->			[@ a [! [$ b [$ c d]]]]
-- right_right_suffix :	[ self._infixRightLvl( [ self._binOp( '$' ) ] ),  self._infixRightLvl( [ self._binOp( '@' ) ] ),  self._suffixLvl( [ self._unOp( '!' ) ] ) ]
		       a			->			a
		       a!			->			[! a]
		       a $ b			->			[$ a b]
		       a @ b		->			[@ a b]
		       a $ b $ c $ d	->			[$ a [$ b [$ c d]]]
		       a $ b @ c $ d	->			[@ [$ a b] [$ c d]]
		       a $ b @ c $ d!	->			[! [@ [$ a b] [$ c d]]]
		       a $ b @ c! $ d	->			<<ERROR>>
		       a $ b! @ c $ d	->			<<ERROR>>



-- left_right_prefix_suffix :	[ self._infixLeftLvl( [ self._binOp( '*' ) ] ),  self._infixRightLvl( [ self._binOp( '$' ) ] ),  self._prefixLvl( [ self._unOp( '~' ) ] ),  self._suffixLvl( [ self._unOp( '!' ) ] ) ]
		       a					->	a
		       ~a					->	[~ a]
		       a!					->	[! a]
		       a * b					->	[* a b]
		       a $ b					->	[$ a b]
		       a * b * c * d			->	[* [* [* a b] c] d]
		       a * b * c $ d			->	[$ [* [* a b] c] d]
		       a * b $ c * d			->	[$ [* a b] [* c d]]
		       a $ b * c * d			->	[$ a [* [* b c] d]]
		       a $ b $ c $ d			->	[$ a [$ b [$ c d]]]
		       a * b $ c $ d			->	[$ [* a b] [$ c d]]
		       a $ b * c $ d			->	[$ a [$ [* b c] d]]
		       a $ b $ c * d			->	[$ a [$ b [* c d]]]

		       ~a * b $ c * d			->	[~ [$ [* a b] [* c d]]]
		       a * ~b $ c * d			->	[* a [~ [$ b [* c d]]]]
		       a * b $ ~c * d			->	[$ [* a b] [~ [* c d]]]
		       a * b $ c * ~d			->	[$ [* a b] [* c [~ d]]]

		       a * b $ c * d!			->	[! [$ [* a b] [* c d]]]
		       a * b $ c! * d			->	<<ERROR>>
		       a * b! $ c * d			->	<<ERROR>>
		       a! * b $ c * d			->	<<ERROR>>

		       a * b! $ ~c * d			->	<<ERROR>>
		       a! * b $ c * ~d			->	<<ERROR>>
		       a * ~b $ c! * d			->	<<ERROR>>
		       a * ~b $ c * d!			->	[! [* a [~ [$ b [* c d]]]]]
		       ~a * b $ c! * d			->	<<ERROR>>


-- left_right_suffix_prefix :	[ self._infixLeftLvl( [ self._binOp( '*' ) ] ),  self._infixRightLvl( [ self._binOp( '$' ) ] ),  self._suffixLvl( [ self._unOp( '!' ) ] ),  self._prefixLvl( [ self._unOp( '~' ) ] ) ]
		       a * ~b $ c! * d			->	[* [* a [~ [! [$ b c]]]] d]
		       
		       
-- infix_chain :				[ self._infixChainLvl( 'ic',    [ self._chainOp( '<' ), self._chainOp( '>' ), self._chainOp( '<>' ) ]    ) ]
			a < b					->	[ic a [< b]]
			a > b					->	[ic a [> b]]
			a <> b				->	[ic a [<> b]]
			a < b < c				->	[ic a [< b] [< c]]
			a < b > c				->	[ic a [< b] [> c]]
			a > b < c				->	[ic a [> b] [< c]]
			a <> b < c				->	[ic a [<> b] [< c]]
			a < b <> c				->	[ic a [< b] [<> c]]
			a < b > c <> d			->	[ic a [< b] [> c] [<> d]]
			a <> b > c < d			->	[ic a [<> b] [> c] [< d]]
"""




def _makeTestMethod(parserSpec, name, tests):
	"""
	Make a unit testing test method
	"""
	def m(self):
		parser = buildOperatorParser( eval( parserSpec ), identifier )
		for input, result in tests:
			print 'Testing ', input
			if result is None:
				self._parseStringFailTest( parser, input )
			else:
				self._parseStringTestSX( parser, input, result )
	m.__name__ = name
	return m


def _makeTestCaseClassImpl(specs):
	"""
	Generate a unit test class from a specification that takes the form:

	[ method_spec0, method_spec1, ...]

	method_specN :=
		( name, parserSpec, [ test0, test1, ... ] )

	parserSpec :=
		A structure passed to buildOperatorParser()

	testN :=
		( input_text, result_text )

	input_text :=
		The text to be parsed

	result_text :=
		The result in S-expression form
	"""
	class _Impl (object):
		pass

	for testSpec in specs:
		name = testSpec[0]
		parserSpec = testSpec[1]
		tests = testSpec[2]
		name = 'test_auto_' + name
		m = _makeTestMethod( parserSpec, name, tests )
		setattr( _Impl, name, m )

	return  _Impl


def _makeTestCaseClassImplFromText(text):
	"""
	Generate a unit test class described by the specification in @text

	Format:
	-- name0 : parser_spec0
		input0 -> result0
		input1 -> result1
		...
	-- name1 : parser_spec1
		input0 -> result0
		input1 -> result1
		...
	...
	# comment

	This text is convered to a structure that is passed to _makeTestCaseClassImpl(), whose result is returned
	"""
	lines = text.split( '\n' )
	specs = []
	currentSpec = None
	currentTests = None
	for line in lines:
		line = line.strip()
		if line == '':
			# blank
			pass
		elif line.startswith( '#' ):
			# comment
			pass
		elif line.startswith( '--' ):
			# -- name : parser_expression
			if currentSpec is not None:
				specs.append( currentSpec )
			nameAndParserDef = line.split( '--' )[1]
			name, parserDef = nameAndParserDef.split( ':' )
			name = name.strip()
			parserSpec = parserDef.strip()
			currentTests = []
			currentSpec = name, parserSpec, currentTests
		elif '->' in line:
			# input -> result
			input, result = line.split( '->' )
			input = input.strip()
			result = result.strip()
			if result == '<<ERROR>>'  or  result == '':
				result = None
			currentTests.append( ( input, result ) )
		else:
			# <unknown>
			raise ValueError, 'unknown form in line %s'  %  line
	if currentSpec is not None:
		specs.append( currentSpec )
	return _makeTestCaseClassImpl( specs )	





TestCase_Impl = _makeTestCaseClassImplFromText( _unitTestSpecification )


class TestCase_Operators (ParserTestCase, TestCase_Impl):
	def _infixLeftLvl(self, x):
		return InfixLeftLevel( x )
	
	def _infixRightLvl(self, x):
		return InfixRightLevel( x )
	
	def _prefixLvl(self, x, bReachUp=True):
		return PrefixLevel( x, bReachUp )
	
	def _suffixLvl(self, x):
		return SuffixLevel( x )
	
	def _infixChainLvl(self, prefix, xs):
		return InfixChainLevel( xs, lambda input, begin, end, x, ys: [ prefix, x ] + ys )
	
	def _unOp(self, x):
		return UnaryOperator( x, lambda input, begin, end, subexp: [ x, subexp ] )
	
	def _binOp(self, x):
		return BinaryOperator( x, lambda input, begin, end, left, right: [ x, left, right ] )
	
	def _chainOp(self, x):
		return ChainOperator( x, lambda input, begin, end, subexp: [ x, subexp ] )
	
	
		
	def testLeft_Prefix_Auto(self):
		atom = identifier

		parser = buildOperatorParser( [ self._infixLeftLvl( [ self._binOp( '*' ) ] ),  self._prefixLvl( [ self._unOp( '~' ) ] ) ], atom )

		self._parseStringTestSX( parser, 'x * y * z',    '[* [* x y] z]' )
		self._parseStringTestSX( parser, '~x * y * z',    '[~ [* [* x y] z]]' )
		self._parseStringTestSX( parser, 'x * ~y * z',    '[* x [~ [* y z]]]' )
		self._parseStringTestSX( parser, 'x * y * ~z',     '[* [* x y] [~ z]]' )



	def testLeft_Suffix_Auto(self):
		atom = identifier

		parser = buildOperatorParser( [ self._infixLeftLvl( [ self._binOp( '*' ) ] ),  self._suffixLvl( [ self._unOp( '!' ) ] ) ], atom )

		self._parseStringTestSX( parser, 'x * y * z',    '[* [* x y] z]' )
		self._parseStringTestSX( parser, 'x * y * z!',    '[! [* [* x y] z]]' )
		self._parseStringFailTest( parser, 'x * y! * z' )
		self._parseStringFailTest( parser, 'x! * y * z' )



	def testRight_Prefix_Auto(self):
		atom = identifier

		parser = buildOperatorParser( [ self._infixRightLvl( [ self._binOp( '$' ) ] ),  self._prefixLvl( [ self._unOp( '~' ) ] ) ], atom )

		self._parseStringTestSX( parser, 'x $ y $ z',    '[$ x [$ y z]]' )
		self._parseStringTestSX( parser, 'x $ y $ ~z',    '[$ x [$ y [~ z]]]' )
		self._parseStringTestSX( parser, 'x $ ~y $ z',    '[$ x [~ [$ y z]]]' )
		self._parseStringTestSX( parser, '~x $ y $ z',    '[~ [$ x [$ y z]]]' )



	def testRight_Suffix_Auto(self):
		atom = identifier

		parser = buildOperatorParser( [ self._infixRightLvl( [ self._binOp( '$' ) ] ),  self._suffixLvl( [ self._unOp( '!' ) ] ) ], atom )

		self._parseStringTestSX( parser, 'x $ y $ z',    '[$ x [$ y z]]' )
		self._parseStringTestSX( parser, 'x $ y $ z!',    '[! [$ x [$ y z]]]' )
		self._parseStringFailTest( parser, 'x $ y! $ z' )
		self._parseStringFailTest( parser, 'x! $ y $ z' )





	def testRight_Right_Prefix_Auto(self):
		atom = identifier

		parser = buildOperatorParser( [ self._infixRightLvl( [ self._binOp( '$' ) ] ),  self._infixRightLvl( [ self._binOp( '@' ) ] ),  self._prefixLvl( [ self._unOp( '~' ) ] ) ], atom )

		self._parseStringTestSX( parser, 'x $ y $ z $ w',    '[$ x [$ y [$ z w]]]' )
		self._parseStringTestSX( parser, 'x $ y @ z $ w',    '[@ [$ x y] [$ z w]]' )
		self._parseStringTestSX( parser, 'x $ y @ z $ ~w',    '[@ [$ x y] [$ z [~ w]]]' )
		self._parseStringTestSX( parser, 'x $ y @ ~z $ w',    '[@ [$ x y] [~ [$ z w]]]' )
		self._parseStringTestSX( parser, 'x $ ~y @ z $ w',    '[$ x [~ [@ y [$ z w]]]]' )
		self._parseStringTestSX( parser, '~x $ y @ z $ w',    '[~ [@ [$ x y] [$ z w]]]' )
		