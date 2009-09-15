##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


from BritefuryJ.Parser import *
from BritefuryJ.Parser.Utils import *
from BritefuryJ.Parser.Utils.OperatorParser import PrefixLevel, SuffixLevel, InfixLeftLevel, InfixRightLevel, InfixChainLevel, UnaryOperator, BinaryOperator, OperatorTable


from Britefury.Grammar.Grammar import Grammar, Rule


from GSymCore.Languages.Java import NodeClasses as Nodes



class JavaGrammar (Grammar):
	decimalInteger = RegEx( r"[\-]?[1-9][0-9]*" )  |  Literal( "0" )
	hexInteger = RegEx( r"0[xX][0-9A-Fa-f]+" )
	octalInteger = RegEx( r"0[0-7]+" )
	
	
	
	@Rule
	def identifier(self):
		return Tokens.javaIdentifier
	
	@Rule
	def qualifiedIdentifier(self):
		return ( self.identifier()  +  ( Literal( '.' )  +  self.identifier() ).zeroOrMore() ).action( lambda input, begin, end, x, bindings: [ x[0] ] + [ a[1]   for a in x[1] ] )
	
	
	# Integer literal
	@Rule
	def decimalIntLiteral(self):
		return Tokens.decimalIntegerNoOctal.action( lambda input, begin, end, x, bindings: Nodes.IntLiteral( format='decimal', numType='int', value=x ) )

	@Rule
	def decimalLongLiteral(self):
		return ( Tokens.decimalIntegerNoOctal + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, x, bindings: Nodes.IntLiteral( format='decimal', numType='long', value=x[0] ) )

	@Rule
	def hexIntLiteral(self):
		return Tokens.hexInteger.action( lambda input, begin, end, x, bindings: Nodes.IntLiteral( format='hex', numType='int', value=x ) )

	@Rule
	def hexLongLiteral(self):
		return ( Tokens.hexInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, x, bindings: Nodes.IntLiteral( format='hex', numType='long', value=x[0] ) )

	@Rule
	def octIntLiteral(self):
		return Tokens.octalInteger.action( lambda input, begin, end, x, bindings: Nodes.IntLiteral( format='oct', numType='int', value=x ) )

	@Rule
	def octLongLiteral(self):
		return ( Tokens.octalInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, begin, end, x, bindings: Nodes.IntLiteral( format='oct', numType='long', value=x[0] ) )

	@Rule
	def integerLiteral(self):
		return self.hexLongLiteral() | self.hexIntLiteral() | self.octLongLiteral() | self.octIntLiteral() | self.decimalLongLiteral() | self.decimalIntLiteral()
	




	# Float literal
	@Rule
	def floatLiteral(self):
		return Tokens.floatingPoint.action( lambda input, begin, end, x, bindings: Nodes.FloatLiteral( value=x ) )

	
	
	# Character literal
	@Rule
	def charLiteral(self):
		return Tokens.javaCharacterLiteral.action( lambda input, begin, end, x, bindings: Nodes.CharLiteral( value=x[1:-1] ) )

	
	
	# String literal
	@Rule
	def stringLiteral(self):
		return Tokens.javaStringLiteral.action( lambda input, begin, end, x, bindings: Nodes.StringLiteral( value=x[1:-1] ) )
	
	

	# Boolean Literal
	@Rule
	def booleanLiteral(self):
		return ( Keyword( 'false' ) | Keyword( 'true' ) ).action( lambda input, begin, end, x, bindings: Nodes.BooleanLiteral( value=x ) )
	
	
	
	# Null Literal
	@Rule
	def nullLiteral(self):
		return Keyword( 'null' ).action( lambda input, begin, end, x, bindings: Nodes.NullLiteral() )
	
	
	
	
	
	
	




import unittest
from Britefury.Tests.BritefuryJ.Parser import ParserTestCase


class TestCase_JavaGrammar (ParserTestCase.ParserTestCase):
	def test_identifier(self):
		g = JavaGrammar()
		self._parseStringTest( g.identifier(), 'abc', 'abc' )
	
	def test_qualifiedIdentifier(self):
		g = JavaGrammar()
		self._parseStringTestSX( g.qualifiedIdentifier(), 'abc', '[abc]' )
		self._parseStringTestSX( g.qualifiedIdentifier(), 'abc.xyz', '[abc xyz]' )
		self._parseStringTestSX( g.qualifiedIdentifier(), 'abc.xyz.pqr', '[abc xyz pqr]' )
		self._parseStringFailTest( g.qualifiedIdentifier(), 'abc.xyz.pqr.' )
		self._parseStringFailTest( g.qualifiedIdentifier(), '.abc.xyz.pqr' )
		self._parseStringFailTest( g.qualifiedIdentifier(), 'abc..xyz.pqr' )
