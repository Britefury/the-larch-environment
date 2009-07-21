##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

from BritefuryJ import Parser

from Britefury.Tests.BritefuryJ.Parser.ParserTestCase import ParserTestCase

from Britefury.Grammar.Grammar import Grammar, Rule, RuleList

import GSymCore.Languages.Python25.NodeClasses as Nodes




class Python25StructureGrammar (Grammar):
	@Rule
	def unparsed(self):
		return  Parser.ObjectNode( Nodes.UNPARSED )
	
	
	@Rule
	def blankLine(self):
		return  Parser.ObjectNode( Nodes.BlankLine )
	
	
	@Rule
	def comment(self):
		return  Parser.ObjectNode( Nodes.CommentStmt )
	
	
	@Rule
	def simpleStmt(self):
		return Parser.ObjectNode( Nodes.SimpleStmt )
	
	
	@Rule
	def compoundStmtHeader(self):
		return Parser.ObjectNode( Nodes.CompountStmtHeader )
	
	
	
	@Rule
	def ifStmt(self):
		return ( Parser.ObjectNode( Nodes.IfStmtHeader )  +  self.indentedSuite()  +  self.elifBlock().zeroOrMore()  +  self.elseBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Nodes.IfStmt( condition=xs[0]['condition'], suite=xs[1], elifBlocks=xs[2], elseSuite=xs[3] ) )
		
	@Rule
	def elifBlock(self):
		return ( Parser.ObjectNode( Nodes.ElifStmtHeader )  +  self.indentedSuite() ).action( lambda input, begin, end, xs, bindings: Nodes.ElifBlock( condition=xs[0]['condition'], suite=xs[1] ) )
	
	@Rule
	def elseBlock(self):
		return ( Parser.ObjectNode( Nodes.ElseStmtHeader )  +  self.indentedSuite() ).action( lambda input, begin, end, xs, bindings: xs[1] )
	
	
	@Rule
	def whileStmt(self):
		return ( Parser.ObjectNode( Nodes.WhileStmtHeader )  +  self.indentedSuite()  +  self.elseBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Nodes.WhileStmt( condition=xs[0]['condition'], suite=xs[1], elseSuite=xs[2] ) )
		

	@Rule
	def forStmt(self):
		return ( Parser.ObjectNode( Nodes.ForStmtHeader )  +  self.indentedSuite()  +  self.elseBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Nodes.ForStmt( target=xs[0]['target'], source=xs[0]['source'], suite=xs[1], elseSuite=xs[2] ) )
		
		
	@Rule
	def tryStmt(self):
		tryStmt1 = ( Parser.ObjectNode( Nodes.TryStmtHeader )  +  self.indentedSuite()  +  self.exceptBlock().oneOrMore()  +  self.elseBlock().optional()  +  self.finallyBlock().optional() ).action(
			lambda input, begin, end, xs, bindings: Nodes.TryStmt( suite=xs[1], exceptBlocks=xs[2], elseSuite=xs[3], finallySuite=xs[4] ) )
		tryStmt2 = ( Parser.ObjectNode( Nodes.TryStmtHeader )  +  self.indentedSuite()  +  self.finallyBlock() ).action(
			lambda input, begin, end, xs, bindings: Nodes.TryStmt( suite=xs[1], exceptBlocks=[], elseSuite=None, finallySuite=xs[2] ) )
		return tryStmt1 | tryStmt2
	
	@Rule
	def exceptBlock(self):
		return ( Parser.ObjectNode( Nodes.ExceptStmtHeader )  +  self.indentedSuite() ).action( lambda input, begin, end, xs, bindings: Nodes.ExceptBlock( exception=xs[0]['exception'], target=xs[0]['target'], suite=xs[1] ) )
	
	@Rule
	def finallyBlock(self):
		return ( Parser.ObjectNode( Nodes.FinallyStmtHeader )  +  self.indentedSuite() ).action( lambda input, begin, end, xs, bindings: xs[1] )
	
	
	@Rule
	def withStmt(self):
		return ( Parser.ObjectNode( Nodes.WithStmtHeader )  +  self.indentedSuite() ).action(
			lambda input, begin, end, xs, bindings: Nodes.WithStmt( expr=xs[0]['expr'], target=xs[0]['target'], suite=xs[1] ) )
		

	@Rule
	def defStmt(self):
		return ( self.decorator().zeroOrMore()  +  Parser.ObjectNode( Nodes.DefStmtHeader )  +  self.indentedSuite() ).action(
			lambda input, begin, end, xs, bindings: Nodes.DefStmt( decorators=xs[0], name=xs[1]['name'], params=xs[1]['params'], paramsTrailingSeparator=xs[1]['paramsTrailingSeparator'] , suite=xs[2] ) )
	
	@Rule
	def decorator(self):
		return Parser.ObjectNode( Nodes.DecoStmtHeader ).action( lambda input, begin, end, xs, bindings: Nodes.Decorator( name=xs['name'], args=xs['args'], argsTrailingSeparator=xs['argsTrailingSeparator'] ) )
	
	
	@Rule
	def classStmt(self):
		return ( Parser.ObjectNode( Nodes.ClassStmtHeader )  +  self.indentedSuite() ).action(
			lambda input, begin, end, xs, bindings: Nodes.ClassStmt( name=xs[0]['name'], bases=xs[0]['bases'], basesTrailingSeparator=xs[0]['basesTrailingSeparator'] , suite=xs[1] ) )
	
	
	@Rule
	def indentedBlock(self):
		return self.indentedSuite().action( lambda input, begin, end, xs, bindings: Nodes.IndentedBlock( suite=xs ) )
	
	
	@Rule
	def compoundStmt(self):
		return Parser.ObjectNode( Nodes.CompoundStmt )  |  self.ifStmt()  |  self.whileStmt()  |  self.forStmt()  |  self.tryStmt()  |  self.withStmt()  |  self.defStmt()  |  self.classStmt()
		       
	
	
	@Rule
	def suiteItem(self):
		return self.unparsed()  |  self.blankLine()  |  self.comment()  |  self.simpleStmt()  |  self.compoundStmt()  |  self.indentedBlock()  |  self.compoundStmtHeader()
	
	
	@Rule
	def singleIndentedSuite(self):
		return ( Parser.ObjectNode( Nodes.Indent )  +  self.suiteItem().zeroOrMore()  +  Parser.ObjectNode( Nodes.Dedent ) ).action( lambda input, begin, end, xs, bindings: xs[1] )
	
	@Rule
	def indentedSuite(self):
		return self.singleIndentedSuite().oneOrMore().action( lambda input, begin, end, xs, bindings: reduce( lambda a, b: list(a)+list(b), xs ) )

	
	@Rule
	def suite(self):
		return self.suiteItem().zeroOrMore()

	
	
	
class TestCase_Python25StructureParser (ParserTestCase):
	def test_unparsed(self):
		g = Python25StructureGrammar()
		self._parseNodeTest( g.suiteItem(), Nodes.UNPARSED( value='x' ), Nodes.UNPARSED( value='x' ) )

		
	def test_blankLine(self):
		g = Python25StructureGrammar()
		self._parseNodeTest( g.suiteItem(), Nodes.BlankLine(), Nodes.BlankLine() )

		
	def test_comment(self):
		g = Python25StructureGrammar()
		self._parseNodeTest( g.suiteItem(), Nodes.CommentStmt( comment='x' ), Nodes.CommentStmt( comment='x' ) )

		
	def test_simpleStmt(self):
		g = Python25StructureGrammar()
		self._parseNodeTest( g.suiteItem(), Nodes.ReturnStmt( value=Nodes.Load( name='a' ) ), Nodes.ReturnStmt( value=Nodes.Load( name='a' ) ) )
		
		
	def test_ifStmt(self):
		g = Python25StructureGrammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.IfStmtHeader( condition=Nodes.Load( name='a' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elifBlocks=[] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.IfStmtHeader( condition=Nodes.Load( name='a' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				     Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elifBlocks=[], elseSuite=[ Nodes.CommentStmt( comment='x' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.IfStmtHeader( condition=Nodes.Load( name='a' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ElifStmtHeader( condition=Nodes.Load( name='b' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent() ],
				     Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elifBlocks=[ Nodes.ElifBlock( condition=Nodes.Load( name='b' ), suite=[ Nodes.CommentStmt( comment='y' ) ] ) ] ) )

		
	def test_whileStmt(self):
		g = Python25StructureGrammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				     Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elseSuite=[ Nodes.CommentStmt( comment='x' ) ] ) )
	
		
	def test_forStmt(self):
		g = Python25StructureGrammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.ForStmtHeader( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.ForStmt( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ), suite=[ Nodes.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.ForStmtHeader( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				     Nodes.ForStmt( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ), suite=[ Nodes.BlankLine() ], elseSuite=[ Nodes.CommentStmt( comment='x' ) ] ) )

		
	def test_tryStmt(self):
		g = Python25StructureGrammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.FinallyStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[], finallySuite=[ Nodes.CommentStmt( comment='x' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent(),
					     Nodes.ExceptStmtHeader( exception=Nodes.Load( name='k' ), target=Nodes.SingleTarget( name='q' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ),
												Nodes.ExceptBlock( exception=Nodes.Load( name='k' ), target=Nodes.SingleTarget( name='q' ), suite=[ Nodes.CommentStmt( comment='y' ) ] )] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent(),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ],
						    elseSuite=[ Nodes.CommentStmt( comment='y' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent(),
					     Nodes.FinallyStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ],
						    finallySuite=[ Nodes.CommentStmt( comment='y' ) ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent(),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent(),
					     Nodes.FinallyStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='z' ),
					     Nodes.Dedent() ],
				     Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ],
						    elseSuite=[ Nodes.CommentStmt( comment='y' ) ], finallySuite=[ Nodes.CommentStmt( comment='z' ) ] ) )

		self._parseListFailTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ] )
		self._parseListFailTest( g.suiteItem(),
				     [
					     Nodes.TryStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.ElseStmtHeader(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='y' ),
					     Nodes.Dedent() ] )

		
	def test_withStmt(self):
		g = Python25StructureGrammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.WithStmtHeader( expr=Nodes.SingleTarget( name='a' ), target=Nodes.Load( name='x' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.WithStmt( expr=Nodes.SingleTarget( name='a' ), target=Nodes.Load( name='x' ), suite=[ Nodes.BlankLine() ] ) )
	
		
	def test_defStmt(self):
		g = Python25StructureGrammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.DefStmtHeader( name='f', params=[ Nodes.SimpleParam( name='x' ) ] ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.DefStmt( decorators=[], name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[ Nodes.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.DecoStmtHeader( name='a', args=[ Nodes.Load( name='x' ) ] ),
					     Nodes.DefStmtHeader( name='f', params=[ Nodes.SimpleParam( name='x' ) ] ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.DefStmt( decorators=[ Nodes.Decorator( name='a', args=[ Nodes.Load( name='x' ) ] ) ], name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[ Nodes.BlankLine() ] ) )
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.DecoStmtHeader( name='a', args=[ Nodes.Load( name='x' ) ] ),
					     Nodes.DecoStmtHeader( name='b', args=[ Nodes.Load( name='y' ) ] ),
					     Nodes.DefStmtHeader( name='f', params=[ Nodes.SimpleParam( name='x' ) ] ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.DefStmt( decorators=[ Nodes.Decorator( name='a', args=[ Nodes.Load( name='x' ) ] ), Nodes.Decorator( name='b', args=[ Nodes.Load( name='y' ) ] ) ],
						    name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[ Nodes.BlankLine() ] ) )
	
		
	def test_classStmt(self):
		g = Python25StructureGrammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.ClassStmtHeader( name='A', bases=[ Nodes.Load( name='x' ) ] ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				     Nodes.ClassStmt( name='A', bases=[ Nodes.Load( name='x' ) ], suite=[ Nodes.BlankLine() ] ) )
		
		
	def test_nestedStructure(self):
		g = Python25StructureGrammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.ClassStmtHeader( name='A', bases=[ Nodes.Load( name='x' ) ] ),
					     Nodes.Indent(),
					     	Nodes.DefStmtHeader( name='f', params=[ Nodes.SimpleParam( name='x' ) ] ),
						Nodes.Indent(),
					     		Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ),
					     		Nodes.Indent(),
								Nodes.BlankLine(),
					     		Nodes.Dedent(),
						Nodes.Dedent(),
					     Nodes.Dedent() ],
				     Nodes.ClassStmt( name='A', bases=[ Nodes.Load( name='x' ) ], suite=[
					     Nodes.DefStmt( decorators=[], name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[ Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ] ) ] ) ] ) )
	
		
	def test_suite(self):
		g = Python25StructureGrammar()
		self._parseListTest( g.suite(),
				     [
					     Nodes.CommentStmt( comment='x' ),
					     Nodes.BlankLine() ],
				      [
					     Nodes.CommentStmt( comment='x' ),
					     Nodes.BlankLine() ] )
		

	
		
	def test_headers(self):
		g = Python25StructureGrammar()
		self._parseListTest( g.suite(),
				     [
					     Nodes.IfStmtHeader( condition=Nodes.Load( name='x' ) ),
					     Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				      [
					     Nodes.IfStmtHeader( condition=Nodes.Load( name='x' ) ),
					     Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ] ) ] )

		
		
	def test_indentedBlock(self):
		g = Python25StructureGrammar()
		self._parseListTest( g.suite(),
				     [
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent() ],
				      [
					     Nodes.IndentedBlock( suite=[ Nodes.BlankLine() ] ) ] )


		
	def test_multiSuite(self):
		g = Python25StructureGrammar()
		self._parseListTest( g.suiteItem(),
				     [
					     Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ),
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				     Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine(), Nodes.CommentStmt( comment='x' ) ] ) )
		self._parseListTest( g.suite(),
				     [
					     Nodes.Indent(),
					     	Nodes.BlankLine(),
					     Nodes.Dedent(),
					     Nodes.Indent(),
					     	Nodes.CommentStmt( comment='x' ),
					     Nodes.Dedent() ],
				      [
					     Nodes.IndentedBlock( suite=[ Nodes.BlankLine(), Nodes.CommentStmt( comment='x' ) ] ) ] )
		