//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Parser;

import BritefuryJ.Parser.Anything;
import BritefuryJ.Parser.BestChoice;
import BritefuryJ.Parser.Choice;
import BritefuryJ.Parser.Combine;
import BritefuryJ.Parser.Literal;
import BritefuryJ.Parser.ListNode;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Peek;
import BritefuryJ.Parser.PeekNot;
import BritefuryJ.Parser.Sequence;
import BritefuryJ.Parser.ZeroOrMore;

public class Test_Parser_Node extends ParserTestCase
{
	public void testLiteral()
	{
		// Test literal
		matchNodeTestSX( new Literal( "abcxyz" ), "abcxyz", "abcxyz" );
		matchNodeFailTestSX( new Literal( "abcxyz" ), "qwerty" );
		
		// Incomplete parse should result in failure
		matchNodeFailTestSX( new Literal( "abcxyz" ), "abcxyzpq" );
	}
	
	public void testAnything()
	{
		// Test anything
		matchNodeTestSX( new Anything(), "abcxyz", "abcxyz" );
	}
	
	public void testListNode()
	{
		ParserExpression parser1 = new ListNode( new Object[] { new Literal( "abc" ) } );
		matchNodeTestSX( parser1, "(abc)", "(abc)" );
		matchNodeFailTestSX( parser1, "(abcde)" );
		matchNodeFailTestSX( parser1, "(abc de)" );

		ParserExpression parser2 = new ListNode( new Object[] { new Literal( "abc" ), new Literal( "def" ) } );
		matchNodeTestSX( parser2, "(abc def)", "(abc def)" );
		matchNodeFailTestSX( parser2, "(abcx def)" );
		matchNodeFailTestSX( parser2, "(abc defx)" );
		matchNodeFailTestSX( parser2, "(abcx defx)" );

		ParserExpression parser3 = new ListNode( new Object[] { new Literal( "abc" ), new ListNode( new Object[] { new Literal( "d" ), new Literal( "e" ) } ) } );
		matchNodeTestSX( parser3, "(abc (d e))", "(abc (d e))" );
		matchNodeFailTestSX( parser3, "(abc (de))" );
		matchNodeFailTestSX( parser3, "(abc de)" );
		matchNodeFailTestSX( parser3, "(abc (dx e))" );
		matchNodeFailTestSX( parser3, "(abc (d ex))" );
		matchNodeFailTestSX( parser3, "(abc (dx ex))" );

		ParserExpression parser4 = new ListNode( new Object[] { new Literal( "abc" ), new ListNode( new Object[] { new Literal( "d" ) } ) } );
		matchNodeTestSX( parser4, "(abc (d))", "(abc (d))" );
		matchNodeFailTestSX( parser4, "(abc d)" );
	}

	public void testChoice()
	{
		ParserExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new Choice( new Object[] { new Literal( "b" ), new Literal( "c" ) } ) } );
		matchNodeTestSX( parser1, "(a b)", "(a b)" );
		matchNodeTestSX( parser1, "(a c)", "(a c)" );
		matchNodeFailTestSX( parser1, "(a b c)" );
		matchNodeFailTestSX( parser1, "(a (b))" );
		matchNodeFailTestSX( parser1, "(a (c))" );
		matchNodeFailTestSX( parser1, "(a (b c))" );

		ParserExpression parser2 = new ListNode( new Object[] { new Literal( "a" ), new Choice( new Object[] { new Literal( "b" ), new Sequence( new Object[] { new Literal( "b" ), new Literal( "c" ) } ) } ) } );
		matchNodeTestSX( parser2, "(a b)", "(a b)" );
		matchNodeFailTestSX( parser2, "(a b c)" );
		matchNodeFailTestSX( parser2, "(a (b))" );
		matchNodeFailTestSX( parser2, "(a (c))" );
		matchNodeFailTestSX( parser2, "(a (b c))" );
	}

	public void testBestChoice()
	{
		ParserExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new BestChoice( new Object[] { new Literal( "b" ), new Sequence( new Object[] { new Literal( "b" ), new Literal( "c" ) } ) } ) } );
		matchNodeTestSX( parser1, "(a b)", "(a b)" );
		matchNodeTestSX( parser1, "(a b c)", "(a (b c))" );
		matchNodeFailTestSX( parser1, "(a (b))" );
		matchNodeFailTestSX( parser1, "(a (c))" );
		matchNodeFailTestSX( parser1, "(a (b c))" );
	}

	public void testSequence()
	{
		ParserExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new Sequence( new Object[] { new Literal( "b" ), new Literal( "c" ) } ) } );
		matchNodeTestSX( parser1, "(a b c)", "(a (b c))" );
		matchNodeFailTestSX( parser1, "(a (b c))" );
	}

	public void testCombine()
	{
		ParserExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new Combine( new Object[] { new Literal( "b" ), new Literal( "c" ) } ) } );
		matchNodeTestSX( parser1, "(a b c)", "(a (b c))" );
		matchNodeFailTestSX( parser1, "(a (b c))" );

		ParserExpression parser2 = new ListNode( new Object[] { new Literal( "a" ), new Combine( new Object[] { new Sequence( new Object[] { new Literal( "b" ), new Literal( "c" ) } ), new Sequence( new Object[] { new Literal( "d" ), new Literal( "e" ) } ) } ) } );
		matchNodeTestSX( parser2, "(a b c d e)", "(a (b c d e))" );
	}

	public void testZeroOrMore()
	{
		ParserExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new ZeroOrMore( new Literal( "b" ) ) } );
		matchNodeTestSX( parser1, "(a)", "(a ())" );
		matchNodeTestSX( parser1, "(a b)", "(a (b))" );
		matchNodeTestSX( parser1, "(a b b)", "(a (b b))" );
		matchNodeTestSX( parser1, "(a b b b)", "(a (b b b))" );

		ParserExpression parser2 = new ListNode( new Object[] { new Literal( "a" ), new ZeroOrMore( new ListNode( new Object[] { new Literal( "d" ), new Literal( "e" ) } ) ) } );
		matchNodeTestSX( parser2, "(a)", "(a ())" );
		matchNodeTestSX( parser2, "(a (d e))", "(a ((d e)))" );
		matchNodeTestSX( parser2, "(a (d e) (d e))", "(a ((d e) (d e)))" );

		ParserExpression parser3 = new ListNode( new Object[] { new Literal( "a" ), new ZeroOrMore( new Sequence( new Object[] { new Literal( "d" ), new Literal( "e" ) } ) ) } );
		matchNodeTestSX( parser3, "(a)", "(a ())" );
		matchNodeTestSX( parser3, "(a d e)", "(a ((d e)))" );
		matchNodeTestSX( parser3, "(a d e d e)", "(a ((d e) (d e)))" );
	}

	public void testPeek()
	{
		ParserExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new Peek( new Literal( "b" ) ), new Anything() } );
		matchNodeFailTestSX( parser1, "(a)" );
		matchNodeTestSX( parser1, "(a b)", "(a b)" );
		matchNodeFailTestSX( parser1, "(a b b)" );
	}

	public void testPeekNot()
	{
		ParserExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new PeekNot( new Literal( "b" ) ), new Anything() } );
		matchNodeFailTestSX( parser1, "(a)" );
		matchNodeFailTestSX( parser1, "(a b)" );
		matchNodeFailTestSX( parser1, "(a b b)" );
		matchNodeTestSX( parser1, "(a c)", "(a c)" );
		matchNodeTestSX( parser1, "(a (x y z))", "(a (x y z))" );
	}
}
