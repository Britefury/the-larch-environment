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




class Operator (object):
	def __init__(self, opExpression, action):
		super( Operator, self ).__init__()
		self._opExpression = parserCoerce( opExpression )
		self._action = action
		
	@abstractmethod
	def parser(self, forwardDefinition, previousLevelParser, prefixOnlyParser):
		pass
		
		
		
class Prefix (Operator):
	def parser(self, forwardDefinition, previousLevelParser, prefixOnlyParser):
		return ( self._opExpression + forwardDefinition ).action( lambda input, begin, tokens: self._action( tokens[1] ) )


class Suffix (Operator):
	def parser(self, forwardDefinition, previousLevelParser, prefixOnlyParser):
		return ( forwardDefinition + self._opExpression ).action( lambda input, begin, tokens: self._action( tokens[0] ) )


class InfixLeft (Operator):
	def parser(self, forwardDefinition, previousLevelParser, prefixOnlyParser):
		return ( forwardDefinition + self._opExpression + previousLevelParser ).action( lambda input, begin, tokens: self._action( tokens[0], tokens[2] ) )
		
		
class InfixRight (Operator):
	def parser(self, forwardDefinition, previousLevelParser, prefixOnlyParser):
		return ( previousLevelParser + self._opExpression + prefixOnlyParser ).action( lambda input, begin, tokens: self._action( tokens[0], tokens[2] ) )
		
		

class PrecedenceLevel (object):
	def __init__(self, *operators):
		super( PrecedenceLevel, self ).__init__()
		self._operators = operators
		
		
	def _buildLevelChoice(self, forwardDefTable, operators):
		opParsers = []
		for op in operators:
			opParsers.append( operators
		
		
	def _buildForwardDefinitions(self, forwardDefTable):
		for op in self._operators:
			forwardDefTable[op] = Forward()
			
			
	def _buildPrefixParser(self, previousLevel):
		prefixOperators = [ op   for op in self._operators   if isinstance( op, Prefix ) ]
		
		
		
		
	def makeParser(self, forwardDefTable, previousLevel):
		pass
		


	
def makeOperatorParser(precedenceLevels):
	forwardDefTable = {}
	for level in precedenceLevels:
		level._buildForwardDefinitions( forwardDefTable )



class TestCase_Operators (ParserTestCase):
	def testOperators(self):
		def _unaryOpAction(input, begin, tokens):
			return [ tokens[1], tokens[0] ]

		_expression = Forward()
		_parenForm = Production( Literal( '(' ) + _expression + ')' ).action( lambda input, begin, tokens: tokens[1] )
		_enclosure = Production( _parenForm )
		_atom = Production( identifier | _enclosure )
		
		_primary = Production( _atom )
		
		_power = Forward()
		_unary = Forward()
		
		_power  <<  Production( ( _primary  +  '**'  +  _unary )  |  _primary )
		_unary  <<  Production( ( ( Literal( '~' ) | '-' | 'not' )  +  _unary ).action( _unaryOpAction )  |  _power )
		
		_mulDivMod = Forward()
		_mulDivMod  <<  Production( ( _mulDivMod + ( Literal( '*' ) | '/' | '%' ) + _unary )  |  _unary )
		_addSub = Forward()
		_addSub  <<  Production( ( _addSub + ( Literal( '+' ) | '-' ) + _mulDivMod )  |  _mulDivMod )
		_shift = Forward()
		_shift  <<  Production( ( _shift + ( Literal( '<<' ) | '>>' ) + _addSub )  |  _addSub )
		_bitAnd = Forward()
		_bitAnd  <<  Production( ( _bitAnd + '&' + _shift )  |  _shift )
		_bitXor = Forward()
		_bitXor  <<  Production( ( _bitXor + '^' + _bitAnd )  |  _bitAnd )
		_bitOr = Forward()
		_bitOr  <<  Production( ( _bitOr + '|' + _bitXor)  |  _bitXor )
		_cmp = Forward()
		_cmp  <<  Production( ( _cmp + ( Literal( '<' ) | '<=' | '==' | '!=' | '>=' | '>' ) + _bitOr )  |  _bitOr )
		_isIn = Forward()
		_isIn  <<  Production( ( _isIn + 'is' + 'not' + _cmp ).action( lambda input, begin, tokens: [ [ tokens[0], tokens[1], tokens[3] ], 'not' ]  )  |  \
				       ( _isIn + 'not' + 'in' + _cmp ).action( lambda input, begin, tokens: [ [ tokens[0], tokens[2], tokens[3] ], 'not' ]  )  |  \
				     ( _isIn + 'is' + _cmp)  |  \
				     ( _isIn + 'in' + _cmp)  |  \
				     _cmp )
		_and = Forward()
		_and  <<  Production( ( _and + 'and' + _isIn )  |  _isIn )
		_or = Forward()
		_or  <<  Production( ( _or + 'or' + _and )  |  _and )
		
		_expression  <<  Production( _or )
		

