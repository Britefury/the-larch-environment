##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Kernel.Abstract import abstractmethod

from Britefury.Parser.Parser import getErrorLine, parserCoerce, Bind, Action, Condition, Forward, Group, Production, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot, ParserTestCase
from Britefury.Parser.GrammarUtils.Tokens import identifier
from Britefury.Parser.GrammarUtils.SeparatedList import separatedList

"""
Operator expression grammar builder


NOTES:


Suffix binds more tightly than prefix:
	[ [ Prefix( '~' ), Suffix( '!' ) ] ]
		~a!		->		(~ (! a))		

		
Putting unary operators (prefix and suffix) in different precedence levels can introduce incompatibilities:
	[ [ Prefix( '~' ) ],  [ Suffix( '!' ] ]
		!~a		->		(! (~ a))
		~!a		->		<ERROR>
	This is similar to Pythons not operator. "not ~a" is valid Python syntax. "~ not a" is not valid Python syntax.

	
Operator types (prefix, suffix, infix-left, infix-right) cannot be mixed in the same precedence level.
Each precedence level may consist of a number of operators; however, they must all be of the same type.
Attempting to create a precedence level with operators of differing types will raise OperatorParserPrecedenceLevelCannotMixOperatorTypesError.
"""




class OperatorParserPrecedenceLevelCannotMixOperatorTypesError (Exception):
	pass



class Operator (object):
	def __init__(self, opExpression, action=None):
		super( Operator, self ).__init__()
		self._op = opExpression
		self._opExpression = parserCoerce( opExpression )
		self._action = action

	@abstractmethod
	def _buildActionFn(self):
		pass

	@abstractmethod
	def _defaultAction(self, input, pos, xs):
		pass

	@abstractmethod
	def _buildParser(self, operatorTable, levelParserForwardDeclarations, thisLevel, thisLevelParser, previousLevel, previousLevelParser):
		pass


	def parser(self, operatorTable, levelParserForwardDeclarations, thisLevel, thisLevelParser, previousLevel, previousLevelParser):
		p = self._buildParser( operatorTable, levelParserForwardDeclarations, thisLevel, thisLevelParser, previousLevel, previousLevelParser )
		if self._action is not None:
			return p.action( self._buildActionFn() )
		else:
			return p.action( self._defaultAction )




class Prefix (Operator):
	def _buildActionFn(self):
		return lambda input, pos, xs: self._action( xs[0], xs[1] )

	def _defaultAction(self, input, pos, xs):
		return [ self._op, xs[1] ]

	def _buildParser(self, operatorTable, levelParserForwardDeclarations, thisLevel, thisLevelParser, previousLevel, previousLevelParser):
		return self._opExpression + thisLevelParser



class Suffix (Operator):
	"""
	Suffix binds more tightly than prefix.
	"""
	def _buildActionFn(self):
		return lambda input, pos, xs: self._action( xs[1], xs[0] )

	def _defaultAction(self, input, pos, xs):
		return [ self._op, xs[0] ]

	def _buildParser(self, operatorTable, levelParserForwardDeclarations, thisLevel, thisLevelParser, previousLevel, previousLevelParser):
		return thisLevelParser + self._opExpression


	
class InfixLeft (Operator):
	def _buildActionFn(self):
		return lambda input, pos, xs: self._action( xs[1], xs[0], xs[2] )

	def _defaultAction(self, input, pos, xs):
		return [ self._op, xs[0], xs[2] ]

	def _buildParser(self, operatorTable, levelParserForwardDeclarations, thisLevel, thisLevelParser, previousLevel, previousLevelParser):
		prefix = operatorTable._getLowestPrecedenceUnaryOperatorLevelParserAbove( levelParserForwardDeclarations, thisLevel, Prefix )
		#suffix = operatorTable._getLowestPrecedenceUnaryOperatorLevelParserAbove( levelParserForwardDeclarations, thisLevel, Suffix )

		#if suffix is not None:
			#left = suffix
		#else:
			#left = thisLevelParser
			
		if prefix is not None:
			right = ( previousLevelParser | prefix )
		else:
			right = previousLevelParser
			
		return thisLevelParser + self._opExpression + right



class InfixRight (Operator):
	def _buildActionFn(self):
		return lambda input, pos, xs: self._action( xs[1], xs[0], xs[2] )

	def _defaultAction(self, input, pos, xs):
		return [ self._op, xs[0], xs[2] ]

	def _buildParser(self, operatorTable, levelParserForwardDeclarations, thisLevel, thisLevelParser, previousLevel, previousLevelParser):
		prefix = operatorTable._getLowestPrecedenceUnaryOperatorLevelParserAbove( levelParserForwardDeclarations, thisLevel, Prefix )
		#suffix = operatorTable._getLowestPrecedenceUnaryOperatorLevelParserAbove( levelParserForwardDeclarations, thisLevel, Suffix )

		#if suffix is not None:
			#left = suffix
		#else:
			#left = previousLevelParser
			
		if prefix is not None:
			right = ( thisLevelParser | prefix )
		else:
			right = thisLevelParser
			
		return previousLevelParser + self._opExpression + right

	
	


class PrecedenceLevel (object):
	def __init__(self, operators):
		super( PrecedenceLevel, self ).__init__()
		# Check the operator classes; ensure that they are all the same
		operatorClass = None
		for op in operators:
			c = type( op )
			if operatorClass is None:
				operatorClass = c
			else:
				if c is not operatorClass:
					raise OperatorParserPrecedenceLevelCannotMixOperatorTypesError
		self._operators = operators


	def _buildLevelChoice(self, operators, rootParser):
		opParsers = []
		prevLevelParser = rootParser
		for forward, op in zip( forwardDefs, operators ):
			opParsers.append( operators.parser( forward, prevLevelParser, prefixOnly ) )


	def _areOperatorsOfClass(self, unaryOperatorClass):
		if len( self._operators ) > 0:
			return isinstance( self._operators[0], unaryOperatorClass )


	def _buildParser(self, operatorTable, levelParserForwardDeclarations, forwardDeclaration, previousLevel, previousLevelParser):
		opParsers = [ op.parser( operatorTable, levelParserForwardDeclarations, self, forwardDeclaration, previousLevel, previousLevelParser )   for op in self._operators ]
		forwardDeclaration  <<  Production( Choice( opParsers + [ previousLevelParser ] ) )





class OperatorTable (object):
	def __init__(self, levels, rootParser):
		super( OperatorTable, self ).__init__()
		self._rootParser = rootParser
		self._levels = [ self.__coerceLevel( lvl )   for lvl in levels ]



	def __coerceLevel(self, level):
		if isinstance( level, list )  or  isinstance( level, tuple ):
			return PrecedenceLevel( level )
		else:
			return level


	def _getLowestPrecedenceUnaryOperatorLevelParserAbove(self, levelParserForwardDeclarations, aboveLevel, unaryOperatorClass):
		index = self._levels.index( aboveLevel )
		unaryOps = []
		for lvl, f in reversed( zip( self._levels[index:], levelParserForwardDeclarations[index:] ) ):
			if lvl._areOperatorsOfClass( unaryOperatorClass ):
				return f
		return None


	def buildParser(self):
		parser = self._rootParser
		levelParserForwardDeclarations = [ Forward()   for lvl in self._levels ]
		prevLevel = None
		for level, forward in zip( self._levels, levelParserForwardDeclarations ):
			level._buildParser( self, levelParserForwardDeclarations, forward, prevLevel, parser )
			parser = forward
			prevLevel = level
		return parser






def buildOperatorParser(operatorTable, rootParser):
	if isinstance( operatorTable, list )  or  isinstance( operatorTable, tuple ):
		operatorTable = OperatorTable( operatorTable, rootParser )
	return operatorTable.buildParser()






from Britefury.Parser.GrammarUtils.Tokens import identifier


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


class TestCase_Operators (ParserTestCase, TestCase_Impl):
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
		
	