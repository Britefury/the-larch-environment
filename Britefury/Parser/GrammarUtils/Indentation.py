##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Parser.Parser import getErrorLine, parserCoerce, Bind, Action, Condition, Forward, Group, Production, Suppress, Literal, Keyword, RegEx, Word, Sequence, Combine, Choice, Optional, Repetition, ZeroOrMore, OneOrMore, Peek, PeekNot, ParserTestCase
from Britefury.Parser.GrammarUtils.Tokens import identifier
from Britefury.Parser.GrammarUtils.SeparatedList import separatedList



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





class TestCase_Indentation (ParserTestCase):
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


		singleStatement = Production( ( expression + Suppress( ';' ) )  >>  ( lambda input, pos, xs: xs[0] ) )

		statement = Forward()
		block = Production( ZeroOrMore( statement ) )
		compoundStatement = Production( ( Literal( '$<indent>$' )  +  block  +  Literal( '$<dedent>$' ) )  >>  ( lambda input, pos, xs: xs[1] ) )
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


