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
		~x!		->		(~ (! x))		

Putting unary operators (prefix and suffix) in different precedence levels can introduce incompatibilities:
	[ [ Prefix( '~' ) ],  [ Suffix( '!' ] ]
		!~x		->		(! (~ x))
		~!x		->		<ERROR>
	This is similar to Pythons not operator. "not ~x" is valid Python syntax. "~ not x" is not valid Python syntax.

Mixing unary and binary operators in the same precedence level often does not make sense, and results in an inconsistent syntax eg:
	[ [ Prefix( '~' ),  InfixLeft( '+' ) ] ]
		!x+y		->		(! (+ x y))
		x+!y		->		<ERROR>
	[ [ Suffix( '!' ),  InfixLeft( '+' ) ] ]
		!x+y		->		(! (+ x y))
		x+!y		->		<ERROR>
"""



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
	def _defaultAction(self, input, begin, tokens):
		pass

	@abstractmethod
	def _buildParser(self, thisLevelParser, previousLevelParser, operatorTable, thisLevel):
		pass


	def parser(self, thisLevelParser, previousLevelParser, operatorTable, thisLevel):
		p = self._buildParser( thisLevelParser, previousLevelParser, operatorTable, thisLevel )
		if self._action is not None:
			return p.action( self._buildActionFn )
		else:
			return p.action( self._defaultAction )


class UnaryOperator (Operator):
	pass


class BinaryOperator (Operator):
	pass


class Prefix (UnaryOperator):
	def _buildActionFn(self):
		return lambda input, begin, tokens: self._action( tokens[1] )

	def _defaultAction(self, input, begin, tokens):
		return [ self._op, tokens[1] ]

	def _buildParser(self, thisLevelParser, previousLevelParser, operatorTable, thisLevel):
		return self._opExpression + thisLevelParser


class Suffix (UnaryOperator):
	"""
	Suffix binds more tightly than prefix.
	"""
	def _buildActionFn(self):
		return lambda input, begin, tokens: self._action( tokens[0] )

	def _defaultAction(self, input, begin, tokens):
		return [ self._op, tokens[0] ]

	def _buildParser(self, thisLevelParser, previousLevelParser, operatorTable, thisLevel):
		return thisLevelParser + self._opExpression


class InfixLeft (BinaryOperator):
	def _buildActionFn(self):
		return lambda input, begin, tokens: self._action( tokens[0], tokens[2] )

	def _defaultAction(self, input, begin, tokens):
		return [ self._op, tokens[0], tokens[2] ]

	def _buildParser(self, thisLevelParser, previousLevelParser, operatorTable, thisLevel):
		return thisLevelParser + self._opExpression + previousLevelParser


class InfixRight (BinaryOperator):
	def _buildActionFn(self):
		return lambda input, begin, tokens: self._action( tokens[0], tokens[2] )

	def _defaultAction(self, input, begin, tokens):
		return [ self._op, tokens[0], tokens[2] ]

	def _buildParser(self, thisLevelParser, previousLevelParser, operatorTable, thisLevel):
		return previousLevelParser + self._opExpression + operatorTable._getPrefixParserFromLevel( thisLevel, thisLevelParser )



class PrecedenceLevel (object):
	def __init__(self, operators):
		super( PrecedenceLevel, self ).__init__()
		self._operators = operators


	def _fixedPrecedenceLevels(self):
		unaryOps = [ op   for op in self._operators   if isinstance( op, UnaryOperator ) ]
		binaryOps = [ op   for op in self._operators   if isinstance( op, BinaryOperator ) ]
		if len( unaryOps ) == 0  or  len( binaryOps ) == 0:
			return [ self ]
		else:
			return [ PrecedenceLevel( unaryOps ), PrecedenceLevel( binaryOps ) ]


	def _buildLevelChoice(self, operators, rootParser):
		opParsers = []
		prevLevelParser = rootParser
		for forward, op in zip( forwardDefs, operators ):
			opParsers.append( operators.parser( forward, prevLevelParser, prefixOnly ) )


	def _getPrefixOperators(self):
		return [ op   for op in self._operators   if isinstance( op, Prefix ) ]


	def _buildParser(self, previousLevelParser, operatorTable):
		parser = Forward()
		opParsers = [ op.parser( parser, previousLevelParser, operatorTable, self )   for op in self._operators ]
		parser  <<  Production( Choice( opParsers + [ previousLevelParser ] ) )
		return parser





class OperatorTable (object):
	def __init__(self, levels):
		super( OperatorTable, self ).__init__()
		lvls = [ self.__coerceLevel( lvl )   for lvl in levels ]
		self._levels = []
		for l in lvls:
			self._levels.extend( l._fixedPrecedenceLevels() )
		#self._levels = lvls



	def __coerceLevel(self, level):
		if isinstance( level, list )  or  isinstance( level, tuple ):
			return PrecedenceLevel( level )
		else:
			return level


	def _getPrefixParserFromLevel(self, level, levelParser):
		index = self._levels.index( level )
		prefixOps = []
		for lvl in self._levels[index:]:
			prefixOps.extend( lvl._getPrefixOperators() )
		parser = Forward()
		prefixParsers = [ op.parser( parser, None, self, None )   for op in prefixOps ]
		parser  <<  Production( Choice( prefixParsers + [ levelParser ] ) )
		return parser



	def buildParser(self, rootParser):
		parser = rootParser
		for level in self._levels:
			parser = level._buildParser( parser, self )
		return parser






def makeOperatorParser(operatorTable, rootParser):
	if isinstance( operatorTable, list )  or  isinstance( operatorTable, tuple ):
		operatorTable = OperatorTable( operatorTable )
	return operatorTable.buildParser( rootParser )






from Britefury.Parser.GrammarUtils.Tokens import identifier
from Britefury.DocModel.DMIO import readSX


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
	x			->			x

	
-- prefix :			[ [ Prefix( '~' ) ] ] 
	x			->			x
	~x			->			(~ x)
	~~x			->			(~ (~ x))
-- suffix :			[ [ Suffix( '!' ) ] ] 
	x			->			x
	x!			->			(! x)
	x!!			->			(! (! x))
-- left :			[ [ InfixLeft( '*' ) ] ]
	x			->			x
	x * y			->			(* x y)
	x * y * z		->			(* (* x y) z)
-- right :			[ [ InfixRight( '$' ) ] ]
	x			->			x
	x $ y			->			($ x y)
	x $ y $ z		->			($ x ($ y z))
	
	
-- prefixprefix :		[ [ Prefix( '~' ), Prefix( '!' ) ] ]
	x			->			x
	~x			->			(~ x)
	!x			->			(! x)
	~!x			->			(~ (! x))
	!~x			->			(! (~ x))
-- suffixsuffix :		[ [ Suffix( '~' ), Suffix( '!' ) ] ]
	x			->			x
	x~			->			(~ x)
	x!			->			(! x)
	x!~			->			(~ (! x))
	x~!			->			(! (~ x))
-- leftleft :		[ [ InfixLeft( '*' ), InfixLeft( '/' ) ] ]
	x			->			x
	x * y			->			(* x y)
	x / y			->			(/ x y)
	x * y / z		->			(/ (* x y) z)
	x / y * z		->			(* (/ x y) z)
-- rightright :		[ [ InfixRight( '$' ), InfixRight( '@' ) ] ]
	x			->			x
	x $ y			->			($ x y)
	x @ y		->			(@ x y)
	x $ y @ z		->			($ x (@ y z))
	x @ y $ z		->			(@ x ($ y z))
	
	
-- prefixsuffix :		[ [ Prefix( '~' ), Suffix( '!' ) ] ]
	~x			->			(~ x)
	x!			->			(! x)
	~x!			->			(~ (! x))
	~~x!			->			(~ (~ (! x)))
	~x!!			->			(~ (! (! x)))
	~~x!!		->			(~ (~ (! (! x))))
-- suffixprefix :		[ [ Suffix( '!' ), Prefix( '~' ) ] ]
	~x			->			(~ x)
	x!			->			(! x)
	~x!			->			(~ (! x))
	~~x!			->			(~ (~ (! x)))
	~x!!			->			(~ (! (! x)))
	~~x!!		->			(~ (~ (! (! x))))
-- prefixleft :		[ [ Prefix( '~' ), InfixLeft( '+' ) ] ]
	~x			->			(~ x)
	x+y			->			(+ x y)
	~x+y			->			(+ (~ x) y)
	x+~y			->			(+ x (~ y))
	x+y+z		->			(+ (+ x y) z)
	~x+y+z		->			(+ (+ (~ x) y) z)
	x+~y+z		->			(+ (+ x (~ y)) z)
	x+y+~z		->			(+ (+ x y) (~ z))
-- suffixleft :		[ [ Suffix( '!' ), InfixLeft( '+' ) ] ]
	x!			->			(! x)
	x+y			->			(+ x y)
	x!+y			->			(+ (! x) y)
	x+y!			->			(+ x (! y))
	x+y+z		->			(+ (+ x y) z)
	x!+y+z		->			(+ (+ (! x) y) z)
	x+y!+z		->			(+ (+ x (! y)) z)
	x+y+z!		->			(+ (+ x y) (! z))
-- prefixright :		[ [ Prefix( '~' ), InfixRight( '$' ) ] ]
	~x			->			(~ x)
	x$y			->			($ x y)
	~x$y			->			($ (~ x) y)
	x$~y			->			($ x (~ y))
	x$y$z		->			($ x ($ y z))
	~x$y$z		->			($ (~ x) ($ y z))
	x$~y$z		->			($ x ($ (~ y) z))
	x$y$~z		->			($ x ($ y (~ z)))
-- suffixright :		[ [ Suffix( '!' ), InfixRight( '$' ) ] ]
	x!			->			(! x)
	x$y			->			($ x y)
	x!$y			->			($ (! x) y)
	x$y!			->			($ x (! y))
	x$y$z		->			($ x ($ y z))
	x!$y$z		->			($ (! x) ($ y z))
	x$y!$z		->			($ x ($ (! y) z))
	x$y$z!		->			($ x ($ y (! z)))
-- leftright :		[ [ InfixLeft( '*' ), InfixRight( '$' ) ] ]
	x			->			x
	x * y			->			(* x y)
	x * y * z		->			(* (* x y) z)
	x $ y			->			($ x y)
	x $ y $ z		->			($ x ($ y z))
	x $ y * z		->			(* ($ x y) z)
	x * y $ z		->			(* x ($ y z))

	
-- prefix_prefix :	[ [ Prefix( '~' ) ], [ Prefix( '!' ) ] ]
	x			->			x
	~x			->			(~ x)
	!x			->			(! x)
	~!x			->			
	!~x			->			(! (~ x))
-- suffix_suffix :	[ [ Suffix( '~' ) ], [ Suffix( '!' ) ] ]
	x			->			x
	x~			->			(~ x)
	x!			->			(! x)
	x~!			->			(! (~ x))
	x!~			->			
-- left_left :		[ [ InfixLeft( '*' ) ], [ InfixLeft( '+' ) ] ]
	x			->			x
	x * y			->			(* x y)
	x + y			->			(+ x y)
	x * y + z		->			(+ (* x y) z)
	x + y * z		->			(+ x (* y z))
-- right_right :		[ [ InfixRight( '$' ) ], [ InfixRight( '@' ) ] ]
	x			->			x
	x $ y			->			($ x y)
	x @ y		->			(@ x y)
	x $ y @ z		->			(@ ($ x y) z)
	x @ y $ z		->			(@ x ($ y z))
"""




def _makeTestMethod(parserSpec, name, tests):
	"""
	Make a unit testing test method
	"""
	def m(self):
		parser = makeOperatorParser( parserSpec, identifier )
		for input, result in tests:
			if result is None:
				self._matchFailTest( parser, input )
			else:
				self._matchTest( parser, input, readSX( result ) )
	m.__name__ = name
	return m


def _makeTestCaseClassImpl(specs):
	"""
	Generate a unit test class from a specification that takes the form:

	[ method_spec0, method_spec1, ...]

	method_specN :=
		( name, parserSpec, [ test0, test1, ... ] )
		
	parserSpec :=
		A structure passed to makeOperatorParser()
		
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
	pass


	#def testOperators(self):
		#def _unaryOpAction(input, begin, tokens):
			#return [ tokens[1], tokens[0] ]

		#_expression = Forward()
		#_parenForm = Production( Literal( '(' ) + _expression + ')' ).action( lambda input, begin, tokens: tokens[1] )
		#_enclosure = Production( _parenForm )
		#_atom = Production( identifier | _enclosure )

		#_primary = Production( _atom )

		#_power = Forward()
		#_unary = Forward()

		#_power  <<  Production( ( _primary  +  '**'  +  _unary )  |  _primary )
		#_unary  <<  Production( ( ( Literal( '~' ) | '-' | 'not' )  +  _unary ).action( _unaryOpAction )  |  _power )

		#_mulDivMod = Forward()
		#_mulDivMod  <<  Production( ( _mulDivMod + ( Literal( '*' ) | '/' | '%' ) + _unary )  |  _unary )
		#_addSub = Forward()
		#_addSub  <<  Production( ( _addSub + ( Literal( '+' ) | '-' ) + _mulDivMod )  |  _mulDivMod )
		#_shift = Forward()
		#_shift  <<  Production( ( _shift + ( Literal( '<<' ) | '>>' ) + _addSub )  |  _addSub )
		#_bitAnd = Forward()
		#_bitAnd  <<  Production( ( _bitAnd + '&' + _shift )  |  _shift )
		#_bitXor = Forward()
		#_bitXor  <<  Production( ( _bitXor + '^' + _bitAnd )  |  _bitAnd )
		#_bitOr = Forward()
		#_bitOr  <<  Production( ( _bitOr + '|' + _bitXor)  |  _bitXor )
		#_cmp = Forward()
		#_cmp  <<  Production( ( _cmp + ( Literal( '<' ) | '<=' | '==' | '!=' | '>=' | '>' ) + _bitOr )  |  _bitOr )
		#_isIn = Forward()
		#_isIn  <<  Production( ( _isIn + 'is' + 'not' + _cmp ).action( lambda input, begin, tokens: [ [ tokens[0], tokens[1], tokens[3] ], 'not' ]  )  |  \
					#( _isIn + 'not' + 'in' + _cmp ).action( lambda input, begin, tokens: [ [ tokens[0], tokens[2], tokens[3] ], 'not' ]  )  |  \
					#( _isIn + 'is' + _cmp)  |  \
					#( _isIn + 'in' + _cmp)  |  \
					#_cmp )
		#_and = Forward()
		#_and  <<  Production( ( _and + 'and' + _isIn )  |  _isIn )
		#_or = Forward()
		#_or  <<  Production( ( _or + 'or' + _and )  |  _and )

		#_expression  <<  Production( _or )


if __name__ == '__main__':
	import unittest
	unittest.main()
