##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Parser.Parser import getErrorLine, parserCoerce, Bind, Action, Condition, Forward, Group, Production, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot
	



def _getLineIndentation(line):
	return line[: line.index( line.strip() ) ]

def _getLineWithoutIndentation(line):
	return line[line.index( line.strip() ):]

def _processIndentation(indentationStack, indentation, indentToken, dedentToken, currentLevel):
	prevIndentation = indentationStack[-1]
	if indentation != prevIndentation:
		if indentation.startswith( prevIndentation ):
			indentationStack.append( indentation )
			currentLevel += 1
			return indentation + indentToken, currentLevel
		elif prevIndentation.startswith( indentation ):
			dedents = ''
			while indentationStack[-1].startswith( indentation )  and  indentation != indentationStack[-1]:
				del indentationStack[-1]
				dedents += dedentToken
				currentLevel -= 1
			return indentation + dedents, currentLevel
		else:
			raise IndentationError
	else:
		return indentation, currentLevel


def indentedBlocksPrePass(text, indentToken='$<indent>$', dedentToken='$<dedent>$'):
	"""
	Processe text whose blocks are determined by indentation, by inserting indent and dedent tokens
	
	indentedBlocksPrePass(text, indentToken='$<indent>$', dedentToken='$<dedent>$')  ->  text with indent and detent tokens
	
	For example
	a
	b
	  c
	  d
	e
	f
	
	==>>
	
	a
	b
	  $<indent>$c
	  d
	$<dedent>$e
	f	
	"""
	lines = text.split( '\n' )
	
	if len( lines ) > 0:
		indentationStack = []
		indentationStack.append( _getLineIndentation( lines[0] ) )
		
		indentationLevel = 0
		
		for i, line in enumerate( lines ):
			if line.strip() != '':
				indentation = _getLineIndentation( line )
				content = _getLineWithoutIndentation( line )
				
				processedIndentation, indentationLevel = _processIndentation( indentationStack, indentation, indentToken, dedentToken, indentationLevel )
				lines[i] = processedIndentation +  content
				currentIndentation = indentation
				
		bAppendBlankLine = indentationLevel > 0
				
		for i in xrange( 0, indentationLevel ):
			lines.append( dedentToken )
			
		if bAppendBlankLine:
			lines.append( '' )
	
	return '\n'.join( lines )
				
				




def _separatedListAction(input, begin, tokens):
	if tokens is None:
		return []
	else:
		return [ tokens[0] ]  +  [ x[1]   for x in tokens[1] ]

def _separatedListActionOneOrMore(input, begin, tokens):
	return [ tokens[0] ]  +  [ x[1]  for x in tokens[1] ]

def separatedList(subexp, separator=',', bOneOrMore=False):
	"""
	Separated list
	
	separatedList(subexpression, separator=',', bOneOrMore=False)  ->  ParserExpression
	
	Creates a parser expression that will match a separated list.
	  subexpression - a parser expression that defines the elements of the list
	  separator - a parser expression the defines the separator that separates list entries
	  bOneOrMore - if True, then will only match with a minimum of one sub-expression
	
	For example:
	  separatedList( identifier, ',' )
	
	will construct a parser expression that will parse:
	   a, b, c, d
	to:
	   [ 'a', 'b', 'c', 'd' ]
	"""
	if bOneOrMore:
		return ( subexp +  ZeroOrMore( parserCoerce( separator )  +  subexp ) ).action( _separatedListActionOneOrMore )
	else:
		return Optional( subexp  +  ZeroOrMore( parserCoerce( separator )  +  subexp ) ).action( _separatedListAction )
	
	
	
def delimitedSeparatedList(subexp, beginDelim, endDelim, separator=','):
	"""
	Delimited separated list
	
	delimitedSeparatedList(subexpression, beginDelimiter, endDelimiter, separator=',')  ->  ParserExpression
	
	Creates a parser expression that will match a delimited separated list.
	  subexpression - a parser expression that defines the elements of the list
	  beginDelimiter - a parser expression that defines the delimiter that marks the beginning of the list
	  endDelimiter - a parser expression that defines the delimiter that marks the end of the list
	  separator - a parser expression the defines the separator that separates list entries
	
	For example:
	  delimitedSeparatedList( identifier, '{', '}', ',' )
	
	will construct a parser expression that will parse:
	   { a, b, c, d }
	to:
	   [ 'a', 'b', 'c', 'd' ]
	"""
	return ( Suppress( beginDelim )  +  Optional( subexp  +  ZeroOrMore( parserCoerce( separator )  +  subexp ) ).action( _separatedListAction )  +  Suppress( endDelim ) ).action( lambda input, begin, tokens: tokens[0] )
	



			


#
# Definitions of singleQuotedString, doubleQuotedString, and quotedString taken from pyparsing, which is Copyright (c) 2003-2007  Paul T. McGuire
#

identifier = RegEx( "[A-Za-z_][A-Za-z0-9_]*" )
singleQuotedString = RegEx( r"'(?:[^'\n\r\\]|(?:'')|(?:\\x[0-9a-fA-F]+)|(?:\\.))*'" )
doubleQuotedString = RegEx( r'"(?:[^"\n\r\\]|(?:"")|(?:\\x[0-9a-fA-F]+)|(?:\\.))*"' )
quotedString = RegEx( r'''(?:"(?:[^"\n\r\\]|(?:"")|(?:\\x[0-9a-fA-F]+)|(?:\\.))*")|(?:'(?:[^'\n\r\\]|(?:'')|(?:\\x[0-9a-fA-F]+)|(?:\\.))*')''' )
unicodeString = Combine( [ 'u', quotedString ] )
decimalInteger = RegEx( r"[\-]?[0-9]+" )
hexInteger = RegEx( r"0x[0-9A-Fa-f]+" )
integer = decimalInteger  |  hexInteger
floatingPoint = RegEx( r"[\-]?(([0-9]+\.[0-9]*)|(\.[0-9]+))(e[\-]?[0-9]+)?" )





import unittest
import string


class TestCase_GrammarUtils (unittest.TestCase):
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
		
		
		
		
	def testIndentedBlocksPrePass(self):
		src1 = '\n'.join( [
			"a",
			"b",
			"  c",
			"  d",
			"e",
			"f", ] )  +  '\n'
		
		expected1 = '\n'.join( [
			"a",
			"b",
			"  $<indent>$c",
			"  d",
			"$<dedent>$e",
			"f", ] )  +  '\n'
		
		
		src2 = '\n'.join( [
			"  a",
			"  b",
			"    c",
			"    d",
			"  e",
			"  f", ] )  +  '\n'
		
		expected2 = '\n'.join( [
			"  a",
			"  b",
			"    $<indent>$c",
			"    d",
			"  $<dedent>$e",
			"  f", ] )  +  '\n'
		

		src3 = '\n'.join( [
			"  a",
			"  b",
			"    c",
			"    d",
			"      e",
			"      f", ] )  +  '\n'
		
		expected3 = '\n'.join( [
			"  a",
			"  b",
			"    $<indent>$c",
			"    d",
			"      $<indent>$e",
			"      f",
			"",
			"$<dedent>$",
			"$<dedent>$", ] )  +  '\n'
		

		src4 = '\n'.join( [
			"  a",
			"  b",
			"    c",
			"    d",
			"      e",
			"      f",
			"  g",
			"  h", ] )  +  '\n'
		
		expected4 = '\n'.join( [
			"  a",
			"  b",
			"    $<indent>$c",
			"    d",
			"      $<indent>$e",
			"      f",
			"  $<dedent>$$<dedent>$g",
			"  h" ] )  +  '\n'
		
		self.assert_( indentedBlocksPrePass( src1 )  ==  expected1 )
		self.assert_( indentedBlocksPrePass( src2 )  ==  expected2 )
		self.assert_( indentedBlocksPrePass( src3 )  ==  expected3 )
		self.assert_( indentedBlocksPrePass( src4 )  ==  expected4 )
		
	
		
		
		
				
	def testIIdentifier(self):
		parser = identifier
		self._matchTest( parser, 'ab', 'ab' )
		self._matchTest( parser, 'ab12', 'ab12' )
		self._matchFailTest( parser, '12ab' )
		self._matchTest( parser, '_ab', '_ab' )
		
		
	def testSingleQuotedString(self):
		parser = singleQuotedString
		self._matchTest( parser, "'abc'", "'abc'" )
		self._matchTest( parser, r"'ab\'c'", r"'ab\'c'" )
		self._matchTest( parser, "'abc'113", "'abc'" )
	
		
	def testDoubleQuotedString(self):
		parser = doubleQuotedString
		self._matchTest( parser, '"abc"', '"abc"' )
		self._matchTest( parser, r'"ab\"c"', r'"ab\"c"' )
		self._matchTest( parser, '"abc"113', '"abc"' )
	
		
	def testQuotedString(self):
		parser = quotedString
		self._matchTest( parser, "'abc'", "'abc'" )
		self._matchTest( parser, r"'ab\'c'", r"'ab\'c'" )
		self._matchTest( parser, "'abc'113", "'abc'" )
		self._matchTest( parser, '"abc"', '"abc"' )
		self._matchTest( parser, r'"ab\"c"', r'"ab\"c"' )
		self._matchTest( parser, '"abc"113', '"abc"' )
	
		
	def testUnicodeString(self):
		parser = unicodeString
		self._matchTest( parser, "u'abc'", "u'abc'" )
		self._matchTest( parser, r"u'ab\'c'", r"u'ab\'c'" )
		self._matchTest( parser, "u'abc'113", "u'abc'" )
		self._matchTest( parser, 'u"abc"', 'u"abc"' )
		self._matchTest( parser, r'u"ab\"c"', r'u"ab\"c"' )
		self._matchTest( parser, 'u"abc"113', 'u"abc"' )
		
		
	def testDecimalInteger(self):
		parser = decimalInteger
		self._matchTest( parser, "123", "123" )
		self._matchTest( parser, "-123", "-123" )
		
		
	def testHexadecimalInteger(self):
		parser = hexInteger
		self._matchTest( parser, "0x123", "0x123" )
		self._matchTest( parser, "0x0123456789abcdef", "0x0123456789abcdef" )
		self._matchTest( parser, "0x0123456789ABCDEF", "0x0123456789ABCDEF" )
		
		

	def testFloatingPoint(self):
		parser = floatingPoint
		self._matchTest( parser, "3.14", "3.14" )
		self._matchTest( parser, "-3.14", "-3.14" )
		self._matchTest( parser, "3.", "3." )
		self._matchTest( parser, "-3.", "-3." )
		self._matchTest( parser, ".14", ".14" )
		self._matchTest( parser, "-.14", "-.14" )

		self._matchTest( parser, "3.14e5", "3.14e5" )
		self._matchTest( parser, "3.14e-5", "3.14e-5" )
		self._matchTest( parser, "-3.14e5", "-3.14e5" )
		self._matchTest( parser, "-3.14e-5", "-3.14e-5" )
		self._matchTest( parser, "3.e5", "3.e5" )
		self._matchTest( parser, "3.e-5", "3.e-5" )
		self._matchTest( parser, "-3.e5", "-3.e5" )
		self._matchTest( parser, "-3.e-5", "-3.e-5" )
		self._matchTest( parser, ".14e5", ".14e5" )
		self._matchTest( parser, ".14e-5", ".14e-5" )
		self._matchTest( parser, "-.14e5", "-.14e5" )
		self._matchTest( parser, "-.14e-5", "-.14e-5" )

		
		
	def testSeparatedList(self):
		parser = separatedList( identifier )
		parser2 = separatedList( identifier, bOneOrMore=True )
		self._matchTest( parser, '', [] )
		self._matchTest( parser, 'ab', [ 'ab' ] )
		self._matchTest( parser, 'cd', [ 'cd' ] )
		self._matchTest( parser, 'ab,cd', [ 'ab', 'cd' ] )
		self._matchFailTest( parser2, '' )
		self._matchTest( parser2, 'ab', [ 'ab' ] )
		self._matchTest( parser2, 'cd', [ 'cd' ] )
		self._matchTest( parser2, 'ab,cd', [ 'ab', 'cd' ] )
		
		
	def testDelimitedSeparatedList(self):
		parser = delimitedSeparatedList( identifier, '[', ']' )
		self._matchFailTest( parser, '' )
		self._matchFailTest( parser, 'ab' )
		self._matchTest( parser, '[]', [] )
		self._matchTest( parser, '[ab]', [ 'ab' ] )
		self._matchTest( parser, '[cd]', [ 'cd' ] )
		self._matchTest( parser, '[ab,cd]', [ 'ab', 'cd' ] )
		self._matchFailTest( parser, 'ab,cd]' )
		self._matchFailTest( parser, '[ab,cd' )
		
		

	def testIndentedGrammar(self):
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

		parameterList = Production( separatedList( expression ) )
		messageSend = Forward()
		messageSend  <<  Production( ( messageSend + '.' + messageName + '(' + parameterList + ')' )  |  atom )

		mul = Forward()
		mul  <<  Production( ( mul + mulop + messageSend )  |  messageSend )
		add = Forward()
		add  <<  Production( ( add  + addop + mul )  |  mul )
		expression  <<  Production( add )
		
		
		singleStatement = Production( ( expression + Suppress( ';' ) )  >>  ( lambda input, start, tokens: tokens[0] ) )

		statement = Forward()
		block = Production( ZeroOrMore( statement ) )
		compoundStatement = Production( ( Literal( '$<indent>$' )  +  block  +  Literal( '$<dedent>$' ) )  >>  ( lambda input, start, tokens: tokens[1] ) )
		statement  <<  Production( compoundStatement  |  singleStatement )
		
		
		parser = block
		
		
		src1 = """
self.x();
		"""
		
		src2 = """
self.y();
   a.b();
   c.d();
		"""
		self._matchTest( parser, indentedBlocksPrePass( src1 ), [ [ 'self', '.', 'x', '(', [], ')' ] ] )
		self._matchTest( parser, indentedBlocksPrePass( src2 ), [ [ 'self', '.', 'y', '(', [], ')' ], [ [ 'a', '.', 'b', '(', [], ')' ], [ 'c', '.', 'd', '(', [], ')' ] ] ] )
		

		
	def testOperators(self):
		_expression = Forward()
		_parenForm = Production( Literal( '(' ) + _expression + ')' ).action( lambda input, begin, tokens: tokens[1] )
		_enclosure = Production( _parenForm )
		_atom = Production( identifier | _enclosure )
		
		_primary  <<  Production( _atom )
		
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
		
		#_expression  <<  Production( _terminalLiteral | _listLiteral | _setLiteral | _lambdaExpression | _mapExpression | _filterExpression | _reduceExpression | _raiseExpression | _tryExpression | _ifExpression | _whereExpression | _moduleExpression | _matchExpression | _loadLocal )
		_expression  <<  Production( _or )
		

