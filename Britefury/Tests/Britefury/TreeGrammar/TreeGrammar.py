##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import *

from BritefuryJ.TreeParser import *


from Britefury.TreeGrammar.TreeGrammar import TreeGrammar, Rule


from Britefury.Tests.BritefuryJ.TreeParser.TreeParserTestCase import TreeParserTestCase





class GrammarTestCase (TreeParserTestCase):
	m = DMModule( 'M', 'm', 'M' )
	A = m.newClass( 'A', [ 'x', 'y' ] )
	B = m.newClass( 'B', [ 'x', 'y' ] )
	C = m.newClass( 'C', [ 'x' ] )
	Mul = m.newClass( 'Mul', [ 'x', 'y' ] )
	Add = m.newClass( 'Add', [ 'x', 'y' ] )
	Paren = m.newClass( 'Paren', [ 'x' ] )
	resolver = TreeParserTestCase.Resolver()
	resolver.modules['M'] = m
	
		
	
	
	class TestGrammarSimple (TreeGrammar):
		@Rule
		def a(self):
			return TreeParserExpression.coerce( 'a' ).action( lambda input, node, bindings: 'x' )
		
		@Rule
		def b(self):
			return TreeParserExpression.coerce( 'b' ).action( lambda input, node, bindings: 'y' )
		
		@Rule
		def ab(self):
			return GrammarTestCase.A.treeParser( x=self.a(), y=self.b() )
		
		@Rule
		def expr(self):
			return self.ab()
		
		
		
	class TestGrammarSimpleOverload (TestGrammarSimple):
		@Rule
		def a(self):
			return TreeParserExpression.coerce( 'a' ).action( lambda input, node, bindings: 'x' ) | TreeParserExpression.coerce( 'c' ).action( lambda input, node, bindings: 'p' )
		
		@Rule
		def b(self):
			return TreeParserExpression.coerce( 'b' ).action( lambda input, node, bindings: 'y' ) | TreeParserExpression.coerce( 'd' ).action( lambda input, node, bindings: 'q' )
		
		
		
	class TestGrammarRecursive (TreeGrammar):
		@Rule
		def identifier(self):
			return AnyString()
		
		@Rule
		def atom(self):
			return self.identifier()
		
		@Rule
		def mul(self):
			return GrammarTestCase.A.treeParser( x=self.mul(), y=self.atom() ).action( lambda input, xs, bindings: GrammarTestCase.Mul.newInstance( x=xs['x'], y=xs['y'] ) )  |  self.atom()
		
		@Rule
		def expr(self):
			return self.mul()
		
		
		
	class TestGrammarRecursiveOverload (TestGrammarRecursive):
		@Rule
		def add(self):
			return GrammarTestCase.B.treeParser( x=self.add(), y=self.mul() ).action( lambda input, xs, bindings: GrammarTestCase.Add.newInstance( x=xs['x'], y=xs['y'] ) )  |  self.mul()
		
		@Rule
		def expr(self):
			return self.add()
		
		
		
	class TestGrammarIndirectRecursiveOverload (TestGrammarRecursiveOverload):
		@Rule
		def paren(self):
			return GrammarTestCase.C.treeParser( x=self.expr() ).action( lambda input, xs, bindings: GrammarTestCase.Paren.newInstance( x=xs['x'] ) )
		
		@Rule
		def atom(self):
			return self.identifier() | self.paren()
		
		
		
		
		
		
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
		
		self._matchTestSX( parser, '{m=M : (m A x=a y=b)}', '{m=M : (m A x=x y=y)}' )
		self._matchFailTestSX( parser, '{m=M : (m A x=c y=b)}' )
		self._matchFailTestSX( parser, '{m=M : (m A x=a y=d)}' )
		self._matchFailTestSX( parser, '{m=M : (m A x=c y=d)}' )

		
	def testGrammarSimpleOverload(self):
		g = self.TestGrammarSimpleOverload()
		
		parser = g.expr()
		
		self._matchTestSX( parser, '{m=M : (m A x=a y=b)}', '{m=M : (m A x=x y=y)}' )
		self._matchTestSX( parser, '{m=M : (m A x=c y=b)}', '{m=M : (m A x=p y=y)}' )
		self._matchTestSX( parser, '{m=M : (m A x=a y=d)}', '{m=M : (m A x=x y=q)}' )
		self._matchTestSX( parser, '{m=M : (m A x=c y=d)}', '{m=M : (m A x=p y=q)}' )
		
		
	def testGrammarRecursive(self):
		g = self.TestGrammarRecursive()
		
		parser = g.expr()
		
		self._matchTestSX( parser, 'a', 'a' )
		self._matchTestSX( parser, '{m=M : (m A x=a y=b)}', '{m=M : (m Mul x=a y=b)}' )
		self._matchTestSX( parser, '{m=M : (m A x=(m A x=a y=b) y=b)}', '{m=M : (m Mul x=(m Mul x=a y=b) y=b)}' )

		
	def testGrammarRecursiveOverload(self):
		g = self.TestGrammarRecursiveOverload()
		
		parser = g.expr()
		
		self._matchTestSX( parser, 'a', 'a' )
		self._matchTestSX( parser, '{m=M : (m A x=a y=b)}', '{m=M : (m Mul x=a y=b)}' )
		self._matchTestSX( parser, '{m=M : (m B x=a y=b)}', '{m=M : (m Add x=a y=b)}' )
		self._matchTestSX( parser, '{m=M : (m B x=(m A x=a y=b) y=b)}', '{m=M : (m Add x=(m Mul x=a y=b) y=b)}' )
		self._matchTestSX( parser, '{m=M : (m B x=(m B x=a y=b) y=b)}', '{m=M : (m Add x=(m Add x=a y=b) y=b)}' )
		self._matchTestSX( parser, '{m=M : (m A x=(m A x=a y=b) y=b)}', '{m=M : (m Mul x=(m Mul x=a y=b) y=b)}' )

		
	def testGrammarIndirectRecursiveOverload(self):
		g = self.TestGrammarIndirectRecursiveOverload()
		
		parser = g.expr()
		
		self._matchTestSX( parser, 'a', 'a' )
		self._matchTestSX( parser, '{m=M : (m A x=a y=b)}', '{m=M : (m Mul x=a y=b)}' )
		self._matchTestSX( parser, '{m=M : (m B x=a y=b)}', '{m=M : (m Add x=a y=b)}' )
		self._matchTestSX( parser, '{m=M : (m B x=(m A x=a y=b) y=b)}', '{m=M : (m Add x=(m Mul x=a y=b) y=b)}' )
		self._matchTestSX( parser, '{m=M : (m B x=(m B x=a y=b) y=b)}', '{m=M : (m Add x=(m Add x=a y=b) y=b)}' )
		self._matchTestSX( parser, '{m=M : (m A x=(m A x=a y=b) y=b)}', '{m=M : (m Mul x=(m Mul x=a y=b) y=b)}' )
		self._matchTestSX( parser, '{m=M : (m B x=(m C x=(m A x=a y=b)) y=b)}', '{m=M : (m Add x=(m Paren x=(m Mul x=a y=b)) y=b)}' )
		self._matchTestSX( parser, '{m=M : (m B x=(m C x=(m B x=a y=b)) y=b)}', '{m=M : (m Add x=(m Paren x=(m Add x=a y=b)) y=b)}' )
		self._matchTestSX( parser, '{m=M : (m A x=(m C x=(m A x=a y=b)) y=b)}', '{m=M : (m Mul x=(m Paren x=(m Mul x=a y=b)) y=b)}' )


		
