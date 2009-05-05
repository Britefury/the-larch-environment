##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


from BritefuryJ.Parser import *
from BritefuryJ.Parser.Utils import *
from BritefuryJ.Parser.Utils.OperatorParser import Prefix, Suffix, InfixLeft, InfixRight, InfixChain, PrecedenceLevel, OperatorTable


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
		return ( self.identifier()  +  ( '.'  +  self.identifier() ).zeroOrMore() ).action( lambda input, pos, x: [ x[0] ] + [ a[1]   for a in x[1] ] )
	
	
	# Integer literal
	@Rule
	def decimalIntLiteral(self):
		return Tokens.decimalIntegerNoOctal.action( lambda input, pos, xs: Nodes.IntLiteral( format='decimal', numType='int', value=xs ) )

	@Rule
	def decimalLongLiteral(self):
		return ( Tokens.decimalIntegerNoOctal + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, pos, xs: Nodes.IntLiteral( format='decimal', numType='long', value=xs[0] ) )

	@Rule
	def hexIntLiteral(self):
		return Tokens.hexInteger.action( lambda input, pos, xs: Nodes.IntLiteral( format='hex', numType='int', value=xs ) )

	@Rule
	def hexLongLiteral(self):
		return ( Tokens.hexInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, pos, xs: Nodes.IntLiteral( format='hex', numType='long', value=xs[0] ) )

	@Rule
	def octIntLiteral(self):
		return Tokens.octalInteger.action( lambda input, pos, xs: Nodes.IntLiteral( format='oct', numType='int', value=xs ) )

	@Rule
	def octLongLiteral(self):
		return ( Tokens.octalInteger + Suppress( Literal( 'l' )  |  Literal( 'L' ) ) ).action( lambda input, pos, xs: Nodes.IntLiteral( format='oct', numType='long', value=xs[0] ) )

	@Rule
	def integerLiteral(self):
		return self.hexLongLiteral() | self.hexIntLiteral() | self.octLongLiteral() | self.octIntLiteral() | self.decimalLongLiteral() | self.decimalIntLiteral()
	




	# Float literal
	@Rule
	def floatLiteral(self):
		return Tokens.floatingPoint.action( lambda input, pos, xs: Nodes.FloatLiteral( value=xs ) )

	
	
	# Character literal
	@Rule
	def charLiteral(self):
		return Tokens.javaCharacterLiteral.action( lambda input, pos, xs: Nodes.CharLiteral( value=xs[1:-1] ) )

	
	
	# String literal
	@Rule
	def stringLiteral(self):
		return Tokens.javaStringLiteral.action( lambda input, pos, xs: Nodes.StringLiteral( value=xs[1:-1] ) )
	
	

	# Boolean Literal
	@Rule
	def booleanLiteral(self):
		return ( Keyword( 'false' ) | Keyword( 'true' ) ).action( lambda input, pos, x: Nodes.BooleanLiteral( value=x ) )
	
	
	
	# Null Literal
	@Rule
	def nullLiteral(self):
		return Keyword( 'null' ).action( lambda input, pos, x: Nodes.NullLiteral() )
	
	
	
	
	
	
	




import unittest
from Britefury.Tests.BritefuryJ.Parser import ParserTestCase


class TestCase_JavaGrammar (ParserTestCase.ParserTestCase):
	def test_identifier(self):
		g = JavaGrammar()
		self._matchTest( g.identifier(), 'abc', 'abc' )
	
