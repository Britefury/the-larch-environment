##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.Parser import *
from BritefuryJ.Parser.Utils.OperatorParser import *


from Britefury.Grammar.Grammar import Grammar, Rule, RuleList


from Britefury.Tests.BritefuryJ.Parser.ParserTestCase import ParserTestCase


from BritefuryJ.Parser.Utils import Tokens


class GrammarTestCase (ParserTestCase):
	
	class TestGrammarSimple (Grammar):
		@Rule
		def a(self):
			return Literal( 'a' )
		
		@Rule
		def b(self):
			return Literal( 'b' )
		
		@Rule
		def ab(self):
			return self.a() + self.b()
		
		@Rule
		def expr(self):
			return self.ab()
		
		
		
	class TestGrammarSimpleOverload (TestGrammarSimple):
		@Rule
		def a(self):
			return Literal( 'a' ) | Literal( 'c' )
		
		@Rule
		def b(self):
			return Literal( 'b' ) | Literal( 'd' )
		
		
		
	class TestGrammarRecursive (Grammar):
		@Rule
		def identifier(self):
			return Tokens.identifier
		
		@Rule
		def atom(self):
			return self.identifier()
		
		@Rule
		def mul(self):
			return ( self.mul() + '*' + self.atom() ).action( lambda input, begin, end, xs, bindings: [ 'mul', xs[0], xs[2] ] )  |  self.atom()
		
		@Rule
		def expr(self):
			return self.mul()
		
		
		
	class TestGrammarRecursiveOverload (TestGrammarRecursive):
		@Rule
		def add(self):
			return ( self.add() + '+' + self.mul() ).action( lambda input, begin, end, xs, bindings: [ 'add', xs[0], xs[2] ] )  |  self.mul()
		
		@Rule
		def expr(self):
			return self.add()
		
		
		
	class TestGrammarIndirectRecursiveOverload (TestGrammarRecursiveOverload):
		@Rule
		def paren(self):
			return ( Literal( '(' ) + self.expr() + Literal( ')' ) ).action( lambda input, begin, end, xs, bindings: xs[1] )
		
		@Rule
		def atom(self):
			return self.identifier() | self.paren()
		
		
		
	class TestGrammarRecursiveList (Grammar):
		@Rule
		def identifier(self):
			return Tokens.identifier
		
		@Rule
		def paren(self):
			return ( Literal( '(' ) + self.expr() + Literal( ')' ) ).action( lambda input, begin, end, xs, bindings: xs[1] )
		
		@Rule
		def atom(self):
			return self.identifier() | self.paren()
		
		@RuleList( [ 'mul', 'add' ] )
		def ops(self):
			opTable = OperatorTable( 
				[
					InfixLeftLevel( [ BinaryOperator( Literal( '*' ),  lambda input, pos, end, left, right: [ 'mul', left, right ] ) ] ),
					InfixLeftLevel( [ BinaryOperator( Literal( '+' ),  lambda input, pos, end, left, right: [ 'add', left, right ] ) ] ),
				],  self.atom() )
			
			return opTable.buildParsers()
		
		@Rule
		def mul(self):
			return self.ops()[0]
		
		@Rule
		def add(self):
			return self.ops()[1]

		@Rule
		def expr(self):
			return self.add()
		
		
		
		
	def testProduction(self):
		g = self.TestGrammarSimple()
		g2 = self.TestGrammarRecursive()
		
		a1 = g.a()
		a2 = g2.mul()
		
		self.assert_( isinstance( a1, Production ) )
		self.assert_( a1.getExpressionName() == 'a' )
		self.assert_( isinstance( a2, Production ) )
		self.assert_( a2.getExpressionName() == 'mul' )

		
	def testRuleCacheing(self):
		g = self.TestGrammarSimple()
		
		a1 = g.a()
		a2 = g.a()
		self.assert_( a1 is a2 )
		
		
	def testSimpleGrammar(self):
		g = self.TestGrammarSimple()
		
		parser = g.expr()
		
		self._matchTest( parser, 'ab', [ 'a', 'b' ] )
		self._matchFailTest( parser, 'cb' )
		self._matchFailTest( parser, 'ad' )
		self._matchFailTest( parser, 'cd' )

		
	def testGrammarSimpleOverload(self):
		g = self.TestGrammarSimpleOverload()
		
		parser = g.expr()
		
		self._matchTest( parser, 'ab', [ 'a', 'b' ] )
		self._matchTest( parser, 'cb', [ 'c', 'b' ] )
		self._matchTest( parser, 'ad', [ 'a', 'd' ] )
		self._matchTest( parser, 'cd', [ 'c', 'd' ] )
		
		
	def testGrammarRecursive(self):
		g = self.TestGrammarRecursive()
		
		parser = g.expr()
		
		self._matchTest( parser, 'a', 'a' )
		self._matchTest( parser, 'a*b', [ 'mul', 'a', 'b' ] )
		self._matchTest( parser, 'a*b*c', [ 'mul', [ 'mul', 'a', 'b' ], 'c' ] )

		
	def testGrammarRecursiveOverload(self):
		g = self.TestGrammarRecursiveOverload()
		
		parser = g.expr()
		
		self._matchTest( parser, 'a', 'a' )
		self._matchTest( parser, 'a*b', [ 'mul', 'a', 'b' ] )
		self._matchTest( parser, 'a*b*c', [ 'mul', [ 'mul', 'a', 'b' ], 'c' ] )
		self._matchTest( parser, 'a+b', [ 'add', 'a', 'b' ] )
		self._matchTest( parser, 'a+b*c', [ 'add', 'a', [ 'mul', 'b', 'c' ] ] )
		self._matchTest( parser, 'a*b+c', [ 'add', [ 'mul', 'a', 'b' ], 'c' ] )

		
	def testGrammarIndirectRecursiveOverload(self):
		g = self.TestGrammarIndirectRecursiveOverload()
		
		parser = g.expr()
		
		self._matchTest( parser, 'a', 'a' )
		self._matchTest( parser, 'a*b', [ 'mul', 'a', 'b' ] )
		self._matchTest( parser, 'a*b*c', [ 'mul', [ 'mul', 'a', 'b' ], 'c' ] )
		self._matchTest( parser, 'a+b', [ 'add', 'a', 'b' ] )
		self._matchTest( parser, 'a+b*c', [ 'add', 'a', [ 'mul', 'b', 'c' ] ] )
		self._matchTest( parser, 'a*b+c', [ 'add', [ 'mul', 'a', 'b' ], 'c' ] )
		self._matchTest( parser, '(a+b)*c', [ 'mul', [ 'add', 'a', 'b' ], 'c' ] )
		self._matchTest( parser, 'a*(b+c)', [ 'mul', 'a', [ 'add', 'b', 'c' ] ] )

		
	def testGrammarRecursiveList(self):
		g = self.TestGrammarRecursiveList()
		
		x = g.ops()
		atom = g.atom()
		paren = g.paren()
		expr = g.expr()
		add = g.add()
		mul = g.mul()
		
		
		self.assert_( isinstance( atom, Production ) )
		atomC = atom.getExpression()
		self.assert_( isinstance( atomC, Choice ) )
		atomC1 = atomC.getSubExpressions()[1]
		self.assert_( atomC1 is paren )
		
		self.assert_( isinstance( paren, Production ) )
		parenA = paren.getExpression()
		self.assert_( isinstance( parenA, Action ) )
		parenS = parenA.getSubExpression()
		self.assert_( isinstance( parenS, Sequence ) )
		parenSE = parenS.getSubExpressions()[1]
		self.assert_( parenSE is expr )
		
		self.assert_( isinstance( expr, Production ) )
		self.assert_( expr.getExpression() is add )

		self.assert_( isinstance( add, Production ) )
		self.assert_( add.getExpression() is x[1] )

		self.assert_( isinstance( mul, Production ) )
		self.assert_( mul.getExpression() is x[0] )

		self._matchTest( g.expr(), 'a', 'a' )
		self._matchTest( g.expr(), 'a*b', [ 'mul', 'a', 'b' ] )
		self._matchTest( g.expr(), 'a*b*c', [ 'mul', [ 'mul', 'a', 'b' ], 'c' ] )
		self._matchTest( g.expr(), 'a+b', [ 'add', 'a', 'b' ] )
		self._matchTest( g.expr(), 'a+b*c', [ 'add', 'a', [ 'mul', 'b', 'c' ] ] )
		self._matchTest( g.expr(), 'a*b+c', [ 'add', [ 'mul', 'a', 'b' ], 'c' ] )
		self._matchTest( g.expr(), '(a+b)*c', [ 'mul', [ 'add', 'a', 'b' ], 'c' ] )
		self._matchTest( g.expr(), 'a*(b+c)', [ 'mul', 'a', [ 'add', 'b', 'c' ] ] )

		
		