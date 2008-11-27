##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.Parser import *


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
			return ( self.mul() + '*' + self.atom() ).action( lambda input, begin, xs: [ 'mul', xs[0], xs[2] ] )  |  self.atom()
		
		@Rule
		def expr(self):
			return self.mul()
		
		
		
	class TestGrammarRecursiveOverload (TestGrammarRecursive):
		@Rule
		def add(self):
			return ( self.add() + '+' + self.mul() ).action( lambda input, begin, xs: [ 'add', xs[0], xs[2] ] )  |  self.mul()
		
		@Rule
		def expr(self):
			return self.add()
		
		
		
	class TestGrammarIndirectRecursiveOverload (TestGrammarRecursiveOverload):
		@Rule
		def paren(self):
			return ( Literal( '(' ) + self.expr() + Literal( ')' ) ).action( lambda input, begin, xs: xs[1] )
		
		@Rule
		def atom(self):
			return self.identifier() | self.paren()
		
		
		
	class TestGrammarRecursiveList (Grammar):
		@Rule
		def identifier(self):
			return Tokens.identifier
		
		@Rule
		def paren(self):
			return ( Literal( '(' ) + self.expr() + Literal( ')' ) ).action( lambda input, begin, xs: xs[1] )
		
		@Rule
		def atom(self):
			return self.identifier() | self.paren()
		
		@RuleList( [ 'mul', 'add' ] )
		def ops(self):
			mul = Forward()
			mul.setExpression( Production( ( mul + '*' + self.atom() ).action( lambda input, begin, xs: [ 'mul', xs[0], xs[2] ] )  |  self.atom() ) )
			
			add = Forward()
			add.setExpression( Production( ( add + '+' + mul ).action( lambda input, begin, xs: [ 'add', xs[0], xs[2] ] )  |  mul ) )
			
			return [ mul, add ]
		
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
		
		a1 = g.a()
		
		self.assert_( isinstance( a1, Production ) )
		self.assert_( a1.getDebugName() == 'a' )

		
	def testRuleCacheing(self):
		g = self.TestGrammarSimple()
		
		a1 = g.a()
		a2 = g.a()
		self.assert_( a1 is a2 )
		
		
	def testForward(self):
		g = self.TestGrammarRecursive()
		
		m = g.mul()
		
		self.assert_( isinstance( m, Forward ) )
		

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
		
		
		self.assert_( isinstance( x[0], Forward ) )
		self.assert_( isinstance( x[1], Forward ) )
		bf0, bf1 = x
		p0, p1 = [ bf.getExpression()   for bf in [bf0,bf1] ]
		self.assert_( isinstance( p0, Production ) )
		self.assert_( isinstance( p1, Production ) )
		f0, f1 = [ p.getSubExpression()   for p in [p0,p1] ]
		self.assert_( isinstance( f0, Forward ) )
		self.assert_( isinstance( f1, Forward ) )
		ip0, ip1 = [ f.getExpression()   for f in [f0,f1] ]
		self.assert_( isinstance( ip0, Production ) )
		self.assert_( isinstance( ip1, Production ) )
		c0, c1 = [ ip.getSubExpression()   for ip in [ip0,ip1] ]
		self.assert_( isinstance( c0, Choice ) )
		self.assert_( isinstance( c1, Choice ) )
		self.assert_( c0.getSubExpressions()[-1] is atom )
		self.assert_( c1.getSubExpressions()[-1] is f0 )
		a0, a1 = [ c.getSubExpressions()[0]   for c in [c0,c1] ]
		self.assert_( isinstance( a0, Action ) )
		self.assert_( isinstance( a1, Action ) )
		s0, s1 = [ a.getSubExpression()   for a in [a0,a1] ]
		s0l, s0r = s0.getSubExpressions()[0], s0.getSubExpressions()[2]
		s1l, s1r = s1.getSubExpressions()[0], s1.getSubExpressions()[2]
		self.assert_( s0l is f0 )
		self.assert_( s0r is atom )
		self.assert_( s1l is f1 )
		self.assert_( s1r is f0 )
		
		self.assert_( isinstance( atom, Production ) )
		atomC = atom.getSubExpression()
		self.assert_( isinstance( atomC, Choice ) )
		atomC1 = atomC.getSubExpressions()[1]
		self.assert_( atomC1 is paren )
		
		self.assert_( isinstance( paren, Production ) )
		parenA = paren.getSubExpression()
		self.assert_( isinstance( parenA, Action ) )
		parenS = parenA.getSubExpression()
		self.assert_( isinstance( parenS, Sequence ) )
		parenSE = parenS.getSubExpressions()[1]
		self.assert_( parenSE is expr )
		
		self.assert_( isinstance( expr, Production ) )
		self.assert_( expr.getSubExpression() is add )

		self.assert_( isinstance( add, Production ) )
		self.assert_( add.getSubExpression() is bf1 )

		self.assert_( isinstance( mul, Production ) )
		self.assert_( mul.getSubExpression() is bf0 )

		parser = g.expr()
		
		self._matchTest( parser, 'a', 'a' )
		self._matchTest( parser, 'a*b', [ 'mul', 'a', 'b' ] )
		self._matchTest( parser, 'a*b*c', [ 'mul', [ 'mul', 'a', 'b' ], 'c' ] )
		self._matchTest( parser, 'a+b', [ 'add', 'a', 'b' ] )
		self._matchTest( parser, 'a+b*c', [ 'add', 'a', [ 'mul', 'b', 'c' ] ] )
		self._matchTest( parser, 'a*b+c', [ 'add', [ 'mul', 'a', 'b' ], 'c' ] )
		self._matchTest( parser, '(a+b)*c', [ 'mul', [ 'add', 'a', 'b' ], 'c' ] )
		self._matchTest( parser, 'a*(b+c)', [ 'mul', 'a', [ 'add', 'b', 'c' ] ] )

		
		