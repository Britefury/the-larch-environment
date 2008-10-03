##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

from BritefuryJ.Parser import *
from BritefuryJ.Parser.Utils import *
from BritefuryJ.Parser.Utils.OperatorParser import Prefix, Suffix, InfixLeft, InfixRight, PrecedenceLevel, OperatorTable
from BritefuryJ.Parser.Utils.Tokens import identifier


from Britefury.Tests.BritefuryJ.Parser.ParserTestCase import ParserTestCase




def _coerceLevel(x):
	if isinstance( x, PrecedenceLevel ):
		return x
	else:
		return PrecedenceLevel( x )

def buildOperatorParser(operatorTable, rootParser):
	return OperatorTable( [ _coerceLevel( level )   for level in operatorTable ], rootParser ).buildParser()






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
-- empty :			[ [] ]
		       a			->			a


-- prefix :			[ [ Prefix( '~' ) ] ] 
		       a			->			a
		       ~a			->			(~ a)
		       ~~a			->			(~ (~ a))
-- suffix :			[ [ Suffix( '!' ) ] ] 
		       a			->			a
		       a!			->			(! a)
		       a!!			->			(! (! a))
-- left :			[ [ InfixLeft( '*' ) ] ]
		       a			->			a
		       a * b			->			(* a b)
		       a * b * c		->			(* (* a b) c)
-- right :			[ [ InfixRight( '$' ) ] ]
		       a			->			a
		       a $ b			->			($ a b)
		       a $ b $ c		->			($ a ($ b c))


-- prefixprefix :		[ [ Prefix( '~' ), Prefix( '!' ) ] ]
		       a			->			a
		       ~a			->			(~ a)
		       !a			->			(! a)
		       ~!a			->			(~ (! a))
		       !~a			->			(! (~ a))
-- suffixsuffix :		[ [ Suffix( '~' ), Suffix( '!' ) ] ]
		       a			->			a
		       a~			->			(~ a)
		       a!			->			(! a)
		       a!~			->			(~ (! a))
		       a~!			->			(! (~ a))
-- leftleft :		[ [ InfixLeft( '*' ), InfixLeft( '/' ) ] ]
		       a			->			a
		       a * b			->			(* a b)
		       a / b			->			(/ a b)
		       a * b / c * d	->			(* (/ (* a b) c) d)
		       a / b * c / d	->			(/ (* (/ a b) c) d)
-- rightright :		[ [ InfixRight( '$' ), InfixRight( '@' ) ] ]
		       a			->			a
		       a $ b			->			($ a b)
		       a @ b		->			(@ a b)
		       a $ b @ c $ d	->			($ a (@ b ($ c d)))
		       a @ b $ c @ d	->			(@ a ($ b (@ c d)))


-- prefix_prefix :	[ [ Prefix( '~' ) ], [ Prefix( '!' ) ] ]
		       a			->			a
		       ~a			->			(~ a)
		       !a			->			(! a)
		       ~!a			->			<<ERROR>>
		       !~a			->			(! (~ a))
		       !!~~a		->			(! (! (~ (~ a))))
		       !~!~a		->			<<ERROR>>
-- prefix_sufffix :	[ [ Prefix( '~' ) ], [ Suffix( '!' ) ] ]
		       a			->			a
		       ~a			->			(~ a)
		       a!			->			(! a)
		       ~a!			->			(! (~ a))
		       ~~a!!		->			(! (! (~ (~ a))))
-- prefix_left :		[ [ Prefix( '~' ) ], [ InfixLeft( '*' ) ] ]
		       a			->			a
		       ~a			->			(~ a)
		       a * b * c		->			(* (* a b) c)
		       ~a * b * c		->			(* (* (~ a) b) c)
		       a * ~b * c		->			(* (* a (~ b)) c)
		       a * b * ~c		->			(* (* a b) (~ c))
-- prefix_right :		[ [ Prefix( '~' ) ], [ InfixRight( '$' ) ] ]
		       a			->			a
		       ~a			->			(~ a)
		       a $ b $ c		->			($ a ($ b c))
		       ~a $ b $ c		->			($ (~ a) ($ b c))
		       a $ ~b $ c		->			($ a ($ (~ b) c))
		       a $ b $ ~c		->			($ a ($ b (~ c)))
-- suffix_prefix :	[ [ Suffix( '!' ) ], [ Prefix( '~' ) ] ]
		       a			->			a
		       ~a			->			(~ a)
		       a!			->			(! a)
		       ~a!			->			(~ (! a))
		       ~~a!!		->			(~ (~ (! (! a))))
-- suffix_suffix :	[ [ Suffix( '~' ) ], [ Suffix( '!' ) ] ]
		       a			->			a
		       a~			->			(~ a)
		       a!			->			(! a)
		       a~!			->			(! (~ a))
		       a!~			->			<<ERROR>>
		       a~~!!		->			(! (! (~ (~ a))))
		       a~!~!		->			<<ERROR>>
-- suffix_left :		[ [ Suffix( '!' ) ], [ InfixLeft( '*' ) ] ]
		       a			->			a
		       a!			->			(! a)
		       a * b * c		->			(* (* a b) c)
		       a! * b * c		->			(* (* (! a) b) c)
		       a * b! * c		->			(* (* a (! b)) c)
		       a * b * c!		->			(* (* a b) (! c))
-- suffix_right :		[ [ Suffix( '!' ) ], [ InfixRight( '$' ) ] ]
		       a			->			a
		       a!			->			(! a)
		       a $ b $ c		->			($ a ($ b c))
		       a! $ b $ c		->			($ (! a) ($ b c))
		       a $ b! $ c		->			($ a ($ (! b) c))
		       a $ b $ c!		->			($ a ($ b (! c)))
-- left_prefix :		[ [ InfixLeft( '*' ) ], [ Prefix( '~' ) ] ]
		       a			->			a
		       ~a			->			(~ a)
		       a * b * c		->			(* (* a b) c)
		       ~a * b * c		->			(~ (* (* a b) c))
		       a * ~b * c		->			(* a (~ (* b c)))
		       a * b * ~c		->			(* (* a b) (~ c))
-- left_suffix :		[ [ InfixLeft( '*' ) ], [ Suffix( '!' ) ] ]
		       a			->			a
		       a!			->			(! a)
		       a * b * c		->			(* (* a b) c)
		       a! * b * c		->			<<ERROR>>
		       a * b! * c		->			<<ERROR>>
		       a * b * c!		->			(! (* (* a b) c))
-- left_left :		[ [ InfixLeft( '*' ) ], [ InfixLeft( '+' ) ] ]
		       a			->			a
		       a * b			->			(* a b)
		       a + b			->			(+ a b)
		       a * b + c		->			(+ (* a b) c)
		       a + b * c		->			(+ a (* b c))
		       a * b + c * d	->			(+ (* a b) (* c d))
		       a + b * c + d	->			(+ (+ a (* b c)) d)
-- left_right :		[ [ InfixLeft( '*' ) ], [ InfixRight( '$' ) ] ]
		       a			->			a
		       a * b			->			(* a b)
		       a $ b			->			($ a b)
		       a * b * c * d	->			(* (* (* a b) c) d)
		       a * b * c $ d	->			($ (* (* a b) c) d)
		       a * b $ c * d	->			($ (* a b) (* c d))
		       a $ b * c * d	->			($ a (* (* b c) d))
		       a $ b $ c $ d	->			($ a ($ b ($ c d)))
		       a * b $ c $ d	->			($ (* a b) ($ c d))
		       a $ b * c $ d	->			($ a ($ (* b c) d))
		       a $ b $ c * d	->			($ a ($ b (* c d)))
-- right_prefix :		[ [ InfixRight( '$' ) ], [ Prefix( '~' ) ] ]
		       a			->			a
		       ~a			->			(~ a)
		       a $ b $ c		->			($ a ($ b c))
		       ~a $ b $ c		->			(~ ($ a ($ b c)))
		       a $ ~b $ c		->			($ a (~ ($ b c)))
		       a $ b $ ~c		->			($ a ($ b (~ c)))
-- right_suffix :		[ [ InfixRight( '$' ) ], [ Suffix( '!' ) ] ]
		       a			->			a
		       a!			->			(! a)
		       a $ b $ c		->			($ a ($ b c))
		       a! $ b $ c		->			<<ERROR>>
		       a $ b! $ c		->			<<ERROR>>
		       a $ b $ c!		->			(! ($ a ($ b c)))
-- right_left :		[ [ InfixRight( '$' ) ], [ InfixLeft( '*' ) ] ]
		       a			->			a
		       a $ b			->			($ a b)
		       a * b			->			(* a b)
		       a $ b $ c $ d	->			($ a ($ b ($ c d)))
		       a * b $ c $ d	->			(* a ($ b ($ c d)))
		       a $ b * c $ d	->			(* ($ a b) ($ c d))
		       a $ b $ c * d	->			(* ($ a ($ b c )) d)
		       a * b * c * d	->			(* (* (* a b) c) d)
		       a * b * c $ d	->			(* (* a b) ($ c d))
		       a * b $ c * d	->			(* (* a ($ b c)) d)
		       a $ b * c * d	->			(* (* ($ a b) c) d)
-- right_right :		[ [ InfixRight( '$' ) ], [ InfixRight( '@' ) ] ]
		       a			->			a
		       a $ b			->			($ a b)
		       a @ b		->			(@ a b)
		       a $ b @ c		->			(@ ($ a b) c)
		       a @ b $ c		->			(@ a ($ b c))
		       a $ b $ c $ d	->			($ a ($ b ($ c d)))
		       a $ b @ c $ d	->			(@ ($ a b) ($ c d))


-- prefix_left_left :	[ [ Prefix( '~' ) ],  [ InfixLeft( '*' ) ],  [ InfixLeft( '+' ) ] ]
		       a			->			a
		       ~a			->			(~ a)
		       a * b			->			(* a b)
		       a + b			->			(+ a b)
		       a * b * c * d	->			(* (* (* a b) c) d)
		       a * b + c * d	->			(+ (* a b) (* c d))
		       ~a * ~b * ~c * ~d	->		(* (* (* (~ a) (~ b)) (~ c)) (~ d))
		       ~a * ~b + ~c * ~d	->		(+ (* (~ a) (~ b)) (* (~ c) (~ d)))
-- left_prefix_left :	[ [ InfixLeft( '*' ) ],  [ Prefix( '~' ) ],  [ InfixLeft( '+' ) ] ]
		       a			->			a
		       ~a			->			(~ a)
		       a * b			->			(* a b)
		       a + b			->			(+ a b)
		       a * b * c * d	->			(* (* (* a b) c) d)
		       a * b + c * d	->			(+ (* a b) (* c d))
		       ~a * b + c * d	->			(+ (~ (* a b)) (* c d))
		       a * b + ~c * d	->			(+ (* a b) (~ (* c d)))
		       a * ~b * c * d	->			(* a (~ (* (* b c) d)))
		       a * ~b * c + d	->			(+ (* a (~ (* b c))) d)
		       ~a * b * c + d	->			(+ (~ (* (* a b) c)) d)
-- left_left_prefix :	[ [ InfixLeft( '*' ) ],  [ InfixLeft( '+' ) ],  [ Prefix( '~' ) ] ]
		       a			->			a
		       ~a			->			(~ a)
		       a * b			->			(* a b)
		       a + b			->			(+ a b)
		       a * b * c * d	->			(* (* (* a b) c) d)
		       a * b + c * d	->			(+ (* a b) (* c d))
		       ~a * b + c * d	->			(~ (+ (* a b) (* c d)))
		       a * ~b + c * d	->			(* a (~ (+ b (* c d))))
		       a * b + ~c * d	->			(+ (* a b) (~ (* c d)))

-- suffix_left_left :	[ [ Suffix( '!' ) ],  [ InfixLeft( '*' ) ],  [ InfixLeft( '+' ) ] ]
		       a			->			a
		       a!			->			(! a)
		       a * b			->			(* a b)
		       a + b			->			(+ a b)
		       a * b * c * d	->			(* (* (* a b) c) d)
		       a! * b! * c! * d!	->			(* (* (* (! a) (! b)) (! c)) (! d))
		       a! * b! + c! * d!	->			(+ (* (! a) (! b)) (* (! c) (! d)))
-- left_suffix_left :	[ [ InfixLeft( '*' ) ],  [ Suffix( '!' ) ],  [ InfixLeft( '+' ) ] ]
		       a			->			a
		       a!			->			(! a)
		       a * b			->			(* a b)
		       a + b			->			(+ a b)
		       a * b * c * d	->			(* (* (* a b) c) d)
		       a * b + c * d	->			(+ (* a b) (* c d))
		       a * b! + c * d	->			(+ (! (* a b)) (* c d))
		       a * b + c * d!	->			(+ (* a b) (! (* c d)))
		       a * b * c! * d	->			<<ERROR>>
		       a + b * c! * d	->			<<ERROR>>
		       a + b * c * d!	->			(+ a (! (* (* b c) d)))
-- left_left_suffix :	[ [ InfixLeft( '*' ) ],  [ InfixLeft( '+' ) ],  [ Suffix( '!' ) ] ]
		       a			->			a
		       a!			->			(! a)
		       a * b			->			(* a b)
		       a + b			->			(+ a b)
		       a * b * c * d	->			(* (* (* a b) c) d)
		       a * b + c * d	->			(+ (* a b) (* c d))
		       a * b + c * d!	->			(! (+ (* a b) (* c d)))
		       a * b + c! * d	->			<<ERROR>>
		       a * b! + c * d	->			<<ERROR>>


-- prefix_right_right :	[ [ Prefix( '~' ) ],  [ InfixRight( '$' ) ],  [ InfixRight( '@' ) ] ]
		       a			->			a
		       ~a			->			(~ a)
		       a $ b			->			($ a b)
		       a @ b		->			(@ a b)
		       a $ b $ c $ d	->			($ a ($ b ($ c d)))
		       ~a $ ~b $ ~c $ ~d	->		($ (~ a) ($ (~ b) ($ (~ c) (~ d))))
		       ~a $ ~b @ ~c $ ~d	->		(@ ($ (~ a) (~ b)) ($ (~ c) (~ d)))
-- right_prefix_right :	[ [ InfixRight( '$' ) ],  [ Prefix( '~' ) ],  [ InfixRight( '@' ) ] ]
		       a			->			a
		       ~a			->			(~ a)
		       a $ b			->			($ a b)
		       a @ b		->			(@ a b)
		       a $ b $ c $ d	->			($ a ($ b ($ c d)))
		       a $ b @ c $ d	->			(@ ($ a b) ($ c d))
		       ~a $ b @ c $ d	->			(@ (~ ($ a b)) ($ c d))
		       a $ b @ ~c $ d	->			(@ ($ a b) (~ ($ c d)))
		       a $ ~b $ c $ d	->			($ a (~ ($ b ($ c d))))
		       a $ ~b $ c @ d	->			(@ ($ a (~ ($ b c))) d)
		       ~a $ b $ c @ d	->			(@ (~ ($ a ($ b c))) d)
-- right_right_prefix :	[ [ InfixRight( '$' ) ],  [ InfixRight( '@' ) ],  [ Prefix( '~' ) ] ]
		       a			->			a
		       ~a			->			(~ a)
		       a $ b			->			($ a b)
		       a @ b		->			(@ a b)
		       a $ b $ c $ d	->			($ a ($ b ($ c d)))
		       a $ b @ c $ d	->			(@ ($ a b) ($ c d))
		       ~a $ b @ c $ d	->			(~ (@ ($ a b) ($ c d)))
		       a $ ~b @ c $ d	->			($ a (~ (@ b ($ c d))))
		       a $ b @ ~c $ d	->			(@ ($ a b) (~ ($ c d)))

-- suffix_right_right :	[ [ Suffix( '!' ) ],  [ InfixRight( '$' ) ],  [ InfixRight( '@' ) ] ]
		       a			->			a
		       a!			->			(! a)
		       a $ b			->			($ a b)
		       a @ b		->			(@ a b)
		       a $ b $ c $ d	->			($ a ($ b ($ c d)))
		       a $ b @ c $ d	->			(@ ($ a b) ($ c d))
		       a! $ b! $ c! $ d!	->			($ (! a) ($ (! b) ($ (! c) (! d))))
		       a! $ b! @ c! $ d!	->		(@ ($ (! a) (! b)) ($ (! c) (! d)))
-- right_suffix_right :	[ [ InfixRight( '$' ) ],  [ Suffix( '!' ) ],  [ InfixRight( '@' ) ] ]
		       a			->			a
		       a!			->			(! a)
		       a $ b			->			($ a b)
		       a @ b		->			(@ a b)
		       a $ b $ c $ d	->			($ a ($ b ($ c d)))
		       a $ b @ c $ d	->			(@ ($ a b) ($ c d))
		       a $ b! @ c $ d	->			(@ (! ($ a b)) ($ c d))
		       a $ b @ c $ d!	->			(@ ($ a b) (! ($ c d)))
		       a $ b $ c! $ d	->			<<ERROR>>
		       a @ b $ c! $ d	->			<<ERROR>>
		       a @ b $ c $ d!	->			(@ a (! ($ b ($ c d))))
-- right_right_suffix :	[ [ InfixRight( '$' ) ],  [ InfixRight( '@' ) ],  [ Suffix( '!' ) ] ]
		       a			->			a
		       a!			->			(! a)
		       a $ b			->			($ a b)
		       a @ b		->			(@ a b)
		       a $ b $ c $ d	->			($ a ($ b ($ c d)))
		       a $ b @ c $ d	->			(@ ($ a b) ($ c d))
		       a $ b @ c $ d!	->			(! (@ ($ a b) ($ c d)))
		       a $ b @ c! $ d	->			<<ERROR>>
		       a $ b! @ c $ d	->			<<ERROR>>



-- left_right_prefix_suffix :	[ [ InfixLeft( '*' ) ],  [ InfixRight( '$' ) ],  [ Prefix( '~' ) ],  [ Suffix( '!' ) ] ]
		       a					->	a
		       ~a					->	(~ a)
		       a!					->	(! a)
		       a * b					->	(* a b)
		       a $ b					->	($ a b)
		       a * b * c * d			->	(* (* (* a b) c) d)
		       a * b * c $ d			->	($ (* (* a b) c) d)
		       a * b $ c * d			->	($ (* a b) (* c d))
		       a $ b * c * d			->	($ a (* (* b c) d))
		       a $ b $ c $ d			->	($ a ($ b ($ c d)))
		       a * b $ c $ d			->	($ (* a b) ($ c d))
		       a $ b * c $ d			->	($ a ($ (* b c) d))
		       a $ b $ c * d			->	($ a ($ b (* c d)))

		       ~a * b $ c * d			->	(~ ($ (* a b) (* c d)))
		       a * ~b $ c * d			->	(* a (~ ($ b (* c d))))
		       a * b $ ~c * d			->	($ (* a b) (~ (* c d)))
		       a * b $ c * ~d			->	($ (* a b) (* c (~ d)))

		       a * b $ c * d!			->	(! ($ (* a b) (* c d)))
		       a * b $ c! * d			->	<<ERROR>>
		       a * b! $ c * d			->	<<ERROR>>
		       a! * b $ c * d			->	<<ERROR>>

		       a * b! $ ~c * d			->	<<ERROR>>
		       a! * b $ c * ~d			->	<<ERROR>>
		       a * ~b $ c! * d			->	<<ERROR>>
		       a * ~b $ c * d!			->	(! (* a (~ ($ b (* c d)))))
		       ~a * b $ c! * d			->	<<ERROR>>


-- left_right_suffix_prefix :	[ [ InfixLeft( '*' ) ],  [ InfixRight( '$' ) ],  [ Suffix( '!' ) ],  [ Prefix( '~' ) ] ]
		       a * ~b $ c! * d			->	(* (* a (~ (! ($ b c )))) d)
"""




def _makeTestMethod(parserSpec, name, tests):
	"""
	Make a unit testing test method
	"""
	def m(self):
		parser = buildOperatorParser( parserSpec, identifier )
		for input, result in tests:
			if result is None:
				self._matchFailTest( parser, input )
			else:
				self._matchTestSX( parser, input, result )
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
			parserDef = parserDef.strip()
			parserSpec = eval( parserDef )
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


#class TestCase_Operators (ParserTestCase, TestCase_Impl):
class TestCase_Operators (ParserTestCase):
	def testLeft_Prefix_Manual(self):
		atom = identifier

		infixLeft = Forward()
		prefix = Forward()

		infixLeft  <<  Production( ( infixLeft + '*' + ( atom | prefix ) ).action( lambda input, pos, xs: [ '*', xs[0], xs[2] ] )  |  atom  )
		prefix  <<  Production( ( Literal( '~' )  +  prefix ).action( lambda input, pos, xs: [ '~', xs[1] ] )  | infixLeft )

		#infixLeft  <<  Production( atom  +  ZeroOrMore( Literal( '*' ) + ( atom | prefix ) ) ).action( _infixLeftActionIterative )
		#prefix  <<  Production( ZeroOrMore( Literal( '~' ) )  +  infixLeft ).action( _prefixActionIterative )


		parser = prefix

		self._matchTestSX( parser, 'x * y * z',		'(* (* x y) z)' )
		self._matchTestSX( parser, '~x * y * z',    '(~ (* (* x y) z))' )
		self._matchTestSX( parser, 'x * ~y * z',    '(* x (~ (* y z)))' )
		self._matchTestSX( parser, 'x * y * ~z',     '(* (* x y) (~ z))' )


	def testLeft_Prefix_Auto(self):
		atom = identifier

		parser = buildOperatorParser( [ [ InfixLeft( '*' ) ],  [ Prefix( '~' ) ] ], atom )

		self._matchTestSX( parser, 'x * y * z',    '(* (* x y) z)' )
		self._matchTestSX( parser, '~x * y * z',    '(~ (* (* x y) z))' )
		self._matchTestSX( parser, 'x * ~y * z',    '(* x (~ (* y z)))' )
		self._matchTestSX( parser, 'x * y * ~z',     '(* (* x y) (~ z))' )



	def testLeft_Suffix_Manual(self):
		atom = identifier

		infixLeft = Forward()
		suffix = Forward()

		infixLeft  <<  Production( ( infixLeft + '*' + atom ).action( lambda input, pos, xs: [ '*', xs[0], xs[2] ] )  |  atom )
		suffix  <<  Production( ( suffix + '!' ).action( lambda input, pos, xs: [ '!', xs[0] ] )  |  infixLeft )

		#infixLeft  <<  Production( atom  +  ZeroOrMore( Literal( '*' ) + atom ) ).action( _infixLeftActionIterative )
		#suffix  <<  Production( infixLeft  +  ZeroOrMore( Literal( '!' ) ) ).action( _suffixActionIterative )

		parser = suffix

		self._matchTestSX( parser, 'x * y * z',    '(* (* x y) z)' )
		self._matchTestSX( parser, 'x * y * z!',    '(! (* (* x y) z))' )
		self._matchFailTest( parser, 'x * y! * z' )
		self._matchFailTest( parser, 'x! * y * z' )


	def testLeft_Suffix_Auto(self):
		atom = identifier

		parser = buildOperatorParser( [ [ InfixLeft( '*' ) ],  [ Suffix( '!' ) ] ], atom )

		self._matchTestSX( parser, 'x * y * z',    '(* (* x y) z)' )
		self._matchTestSX( parser, 'x * y * z!',    '(! (* (* x y) z))' )
		self._matchFailTest( parser, 'x * y! * z' )
		self._matchFailTest( parser, 'x! * y * z' )



	def testRight_Prefix_Manual(self):
		atom = identifier

		infixRight = Forward()
		prefix = Forward()

		infixRight  <<  Production( ( atom + '$' + prefix ).action( lambda input, pos, xs: [ '$', xs[0], xs[2] ] )  |  atom  )
		prefix  <<  Production( ( Literal( '~' )  +  prefix ).action( lambda input, pos, xs: [ '~', xs[1] ] )  | infixRight )

		#infixRight  <<  Production( atom  +  ZeroOrMore( Literal( '$' ) + ( atom | prefix ) ) ).action( _infixRightActionIterative )
		#prefix  <<  Production( ZeroOrMore( Literal( '~' ) )  +  infixRight ).action( _prefixActionIterative )

		parser = prefix

		self._matchTestSX( parser, 'x $ y $ z',    '($ x ($ y z))' )
		self._matchTestSX( parser, 'x $ y $ ~z',    '($ x ($ y (~ z)))' )
		self._matchTestSX( parser, 'x $ ~y $ z',    '($ x (~ ($ y z)))' )
		self._matchTestSX( parser, '~x $ y $ z',    '(~ ($ x ($ y z)))' )


	def testRight_Prefix_Auto(self):
		atom = identifier

		parser = buildOperatorParser( [ [ InfixRight( '$' ) ],  [ Prefix( '~' ) ] ], atom )

		self._matchTestSX( parser, 'x $ y $ z',    '($ x ($ y z))' )
		self._matchTestSX( parser, 'x $ y $ ~z',    '($ x ($ y (~ z)))' )
		self._matchTestSX( parser, 'x $ ~y $ z',    '($ x (~ ($ y z)))' )
		self._matchTestSX( parser, '~x $ y $ z',    '(~ ($ x ($ y z)))' )



	def testRight_Suffix_Manual(self):
		atom = identifier

		infixRight = Forward()
		suffix = Forward()

		infixRight  <<  Production( ( atom + '$' + infixRight ).action( lambda input, pos, xs: [ '$', xs[0], xs[2] ] )  |  atom  )
		suffix  <<  Production( ( suffix + '!' ).action( lambda input, pos, xs: [ '!', xs[0] ] )  |  infixRight )

		#infixRight  <<  Production( atom  +  ZeroOrMore( Literal( '$' ) + atom ) ).action( _infixRightActionIterative )
		#suffix  <<  Production( infixRight  +  ZeroOrMore( Literal( '!' ) ) ).action( _suffixActionIterative )


		parser = suffix

		self._matchTestSX( parser, 'x $ y $ z',    '($ x ($ y z))' )
		self._matchTestSX( parser, 'x $ y $ z!',    '(! ($ x ($ y z)))' )
		self._matchFailTest( parser, 'x $ y! $ z' )
		self._matchFailTest( parser, 'x! $ y $ z' )


	def testRight_Suffix_Auto(self):
		atom = identifier

		parser = buildOperatorParser( [ [ InfixRight( '$' ) ],  [ Suffix( '!' ) ] ], atom )

		self._matchTestSX( parser, 'x $ y $ z',    '($ x ($ y z))' )
		self._matchTestSX( parser, 'x $ y $ z!',    '(! ($ x ($ y z)))' )
		self._matchFailTest( parser, 'x $ y! $ z' )
		self._matchFailTest( parser, 'x! $ y $ z' )





	def testRight_Right_Prefix_Manual(self):
		atom = identifier

		infixRightDollar = Forward()
		infixRightAt = Forward()
		prefix = Forward()

		infixRightDollar  <<  Production( ( atom + '$' + ( infixRightDollar | prefix ) ).action( lambda input, pos, xs: [ '$', xs[0], xs[2] ] )  |  atom  )
		infixRightAt  <<  Production( ( infixRightDollar + '@' + prefix ).action( lambda input, pos, xs: [ '@', xs[0], xs[2] ] )  |  infixRightDollar  )
		prefix  <<  Production( ( Literal( '~' )  +  prefix ).action( lambda input, pos, xs: [ '~', xs[1] ] )  |  infixRightAt )


		#infixRightDollar  <<  Production( atom  +  ZeroOrMore( Literal( '$' ) + ( atom | prefix ) ) ).action( _infixRightActionIterative )
		#infixRightAt  <<  Production( infixRightDollar  +  ZeroOrMore( Literal( '@' ) + ( infixRightDollar | prefix ) ) ).action( _infixRightActionIterative )
		#prefix  <<  Production( ZeroOrMore( Literal( '~' ) )  +  infixRightAt ).action( _prefixActionIterative )


		parser = prefix

		self._matchTestSX( parser, 'x $ y $ z $ w',    '($ x ($ y ($ z w)))' )
		self._matchTestSX( parser, 'x $ y @ z $ w',    '(@ ($ x y) ($ z w))' )
		self._matchTestSX( parser, 'x $ y @ z $ ~w',    '(@ ($ x y) ($ z (~ w)))' )
		self._matchTestSX( parser, 'x $ y @ ~z $ w',    '(@ ($ x y) (~ ($ z w)))' )
		self._matchTestSX( parser, 'x $ ~y @ z $ w',    '($ x (~ (@ y ($ z w))))' )
		self._matchTestSX( parser, '~x $ y @ z $ w',    '(~ (@ ($ x y) ($ z w)))' )


	def testRight_Right_Prefix_Auto(self):
		atom = identifier

		parser = buildOperatorParser( [ [ InfixRight( '$' ) ],  [ InfixRight( '@' ) ],  [ Prefix( '~' ) ] ], atom )

		self._matchTestSX( parser, 'x $ y $ z $ w',    '($ x ($ y ($ z w)))' )
		self._matchTestSX( parser, 'x $ y @ z $ w',    '(@ ($ x y) ($ z w))' )
		self._matchTestSX( parser, 'x $ y @ z $ ~w',    '(@ ($ x y) ($ z (~ w)))' )
		self._matchTestSX( parser, 'x $ y @ ~z $ w',    '(@ ($ x y) (~ ($ z w)))' )
		self._matchTestSX( parser, 'x $ ~y @ z $ w',    '($ x (~ (@ y ($ z w))))' )
		self._matchTestSX( parser, '~x $ y @ z $ w',    '(~ (@ ($ x y) ($ z w)))' )

