//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.NodeParser;

import java.util.Arrays;
import java.util.Map;

import BritefuryJ.NodeParser.Anything;
import BritefuryJ.NodeParser.BestChoice;
import BritefuryJ.NodeParser.Choice;
import BritefuryJ.NodeParser.Literal;
import BritefuryJ.NodeParser.ListNode;
import BritefuryJ.NodeParser.OneOrMore;
import BritefuryJ.NodeParser.ParserExpression;
import BritefuryJ.NodeParser.Peek;
import BritefuryJ.NodeParser.PeekNot;
import BritefuryJ.NodeParser.Sequence;
import BritefuryJ.NodeParser.ZeroOrMore;
import BritefuryJ.NodeParser.RegEx;
import BritefuryJ.NodeParser.Action;
import BritefuryJ.NodeParser.ParseAction;
import BritefuryJ.NodeParser.Condition;
import BritefuryJ.NodeParser.ParseCondition;

public class Test_NodeParser extends NodeParserTestCase
{
	static ParserExpression identifier = new RegEx( "[A-Za-z_][A-Za-z0-9_]*" );
	
	
	public void testAnything()
	{
		// Test anything
		matchNodeTestSX( new Anything(), "abcxyz", "abcxyz" );
	}
	
	
	public void testLiteral()
	{
		// Test literal
		matchNodeTestSX( new Literal( "abcxyz" ), "abcxyz", "abcxyz" );
		matchNodeFailTestSX( new Literal( "abcxyz" ), "qwerty" );
		
		// Incomplete parse should result in failure
		matchNodeFailTestSX( new Literal( "abcxyz" ), "abcxyzpq" );
	}
	
	
	public void testRegEx()
	{
		assertTrue( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ).compareTo( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ) ) );
		assertFalse( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ).compareTo( new RegEx( "[A-Za-z_][A-Za-z0-9_]*abc" ) ) );
		matchNodeTestSX( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ), "abc_123", "abc_123" );
		matchNodeFailTestSX( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ), "9abc" );
		matchNodeFailTestSX( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ), "abc_xyz...");
		matchNodeTestSX( new RegEx( "[A-Za-z_]*" ), "abc_", "abc_" );
		matchNodeFailTestSX( new RegEx( "[A-Za-z_]*" ), "." );
	}

	
	public void testAction()
	{
		ParseAction f = new ParseAction()
		{
			public Object invoke(Object input, int begin, Object value, Map<String, Object> bindings)
			{
				String v = (String)value;
				return v + v;
			}
		};

		ParseAction g = new ParseAction()
		{
			public Object invoke(Object input, int begin, Object value, Map<String, Object> bindings)
			{
				String v = (String)value;
				return v + v + v;
			}
		};

		assertTrue( new Action( "abc", f ).compareTo( new Action( "abc", f ) ) );
		assertFalse( new Action( "abc", f ).compareTo( new Action( "def", f ) ) );
		assertFalse( new Action( "abc", f ).compareTo( new Action( "abc", g ) ) );
		assertTrue( new Action( "abc", f ).compareTo( new Literal( "abc" ).action( f ) ) );
		
		ParserExpression parser = new Literal( "abc" ).bindTo( "x" ).action( f );
		
		matchNodeTestSX( parser, "abc", "abcabc" );
		bindingsNodeTestSX( parser, "abc", "((x abc))" );
	}

	
	public void testCondition()
	{
		ParseCondition f = new ParseCondition()
		{
			public boolean test(Object input, int begin, Object value, Map<String, Object> bindings)
			{
				String v = (String)value;
				return v.startsWith( "hello" );
			}
		};

		ParseCondition g = new ParseCondition()
		{
			public boolean test(Object input, int begin, Object value, Map<String, Object> bindings)
			{
				String v = (String)value;
				return v.startsWith( "there" );
			}
		};

		assertTrue( new Condition( "abc", f ).compareTo( new Condition( "abc", f ) ) );
		assertFalse( new Condition( "abc", f ).compareTo( new Condition( "def", f ) ) );
		assertFalse( new Condition( "abc", f ).compareTo( new Condition( "abc", g ) ) );
		assertTrue( new Condition( "abc", f ).compareTo( new Literal( "abc" ).condition( f ) ) );
		
		ParserExpression parser = identifier.bindTo( "x" ).condition( f );
		
		matchNodeTestSX( parser, "helloworld", "helloworld" );
		matchNodeFailTestSX( parser, "xabcdef" );
		bindingsNodeTestSX( parser, "helloworld", "((x helloworld))" );
	}
	
	
	public void testBind()
	{
		ParserExpression parser1 = identifier.bindTo(  "x" );
		
		matchNodeTestSX( parser1, "abc", "abc" );
		bindingsNodeTestSX( parser1, "abc", "((x abc))" );

	
		ParserExpression parser2 = identifier.bindTo( "x" ).bindTo( "y" );
		
		matchNodeTestSX( parser2, "abc", "abc" );
		bindingsNodeTestSX( parser2, "abc", "((x abc) (y abc))" );

	
//		ParserExpression parser3 = new ListNode( new Object[] { new OneOrMore( identifier.bindTo( "x" ) ) } );
//		
//		matchNodeTestSX( parser3, "(abc)", "((abc))" );
//		bindingsNodeTestSX( parser3, "(abc)", "((x abc))" );
//		matchNodeTestSX( parser3, "(abc abc)", "((abc abc))" );
//		bindingsNodeTestSX( parser3, "(abc abc)", "((x abc))" );
//		matchNodeFailTestSX( parser3, "(abc def)" );
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

		ParserExpression parser4 = new ListNode( new Object[] { new Literal( "abc" ), new ListNode( new Object[] { new Literal( "d" ) } ).bindTo( "x" ) } );
		matchNodeTestSX( parser4, "(abc (d))", "(abc (d))" );
		matchNodeFailTestSX( parser4, "(abc d)" );
		bindingsNodeTestSX( parser4, "(abc (d))", "((x (d)))" );

		ParserExpression parser5 = new ListNode( new Object[] { identifier.bindTo( "x" ), identifier.bindTo( "x" ) } );
		matchNodeTestSX( parser5, "(abc abc)", "(abc abc)" );
		bindingsNodeTestSX( parser5, "(abc abc)", "((x abc))" );
		matchNodeFailTestSX( parser5, "(abc d)" );

		ParserExpression parser6 = new ListNode( new Object[] { identifier.bindTo( "x" ), new ListNode( new Object[] { identifier.bindTo( "x" ) } ) } );
		matchNodeTestSX( parser6, "(abc (abc))", "(abc (abc))" );
		bindingsNodeTestSX( parser6, "(abc (abc))", "((x abc))" );
		matchNodeFailTestSX( parser6, "(abc (d))" );
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

		ParserExpression parser2 = new ListNode( new Object[] { new Literal( "a" ), new Choice( new Object[] { new Sequence( new Object[] { new Literal( "b" ), new Literal( "c" ) } ).bindTo( "x" ), new Literal( "b" ).bindTo( "x" ) } ) } );
		matchNodeTestSX( parser2, "(a b)", "(a b)" );
		matchNodeTestSX( parser2, "(a b c)", "(a (b c))" );
		matchNodeFailTestSX( parser2, "(a (b))" );
		matchNodeFailTestSX( parser2, "(a (c))" );
		matchNodeFailTestSX( parser2, "(a (b c))" );
		bindingsNodeTestSX( parser2, "(a b)", "((x b))" );
		bindingsNodeTestSX( parser2, "(a b c)", "((x (b c)))" );
	}

	public void testBestChoice()
	{
		ParserExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new BestChoice( new Object[] { new Literal( "b" ).bindTo( "x" ), new Sequence( new Object[] { new Literal( "b" ), new Literal( "c" ) } ).bindTo( "x" ) } ) } );
		matchNodeTestSX( parser1, "(a b)", "(a b)" );
		matchNodeTestSX( parser1, "(a b c)", "(a (b c))" );
		matchNodeFailTestSX( parser1, "(a (b))" );
		matchNodeFailTestSX( parser1, "(a (c))" );
		matchNodeFailTestSX( parser1, "(a (b c))" );
		bindingsNodeTestSX( parser1, "(a b)", "((x b))" );
		bindingsNodeTestSX( parser1, "(a b c)", "((x (b c)))" );
	}

	public void testSequence()
	{
		ParserExpression parser1 = new ListNode( new Object[] { identifier, new Sequence( new Object[] { identifier, identifier.bindTo( "x" ) } ) } );
		matchNodeTestSX( parser1, "(a b c)", "(a (b c))" );
		matchNodeFailTestSX( parser1, "(a (b c))" );
		bindingsNodeTestSX( parser1, "(a b c)", "((x c))" );
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

	public void testOneOrMore()
	{
		ParserExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new OneOrMore( new Literal( "b" ) ) } );
		matchNodeFailTestSX( parser1, "(a)" );
		matchNodeTestSX( parser1, "(a b)", "(a (b))" );
		matchNodeTestSX( parser1, "(a b b)", "(a (b b))" );
		matchNodeTestSX( parser1, "(a b b b)", "(a (b b b))" );

		ParserExpression parser2 = new ListNode( new Object[] { new Literal( "a" ), new OneOrMore( new ListNode( new Object[] { new Literal( "d" ), new Literal( "e" ) } ) ) } );
		matchNodeFailTestSX( parser2, "(a)" );
		matchNodeTestSX( parser2, "(a (d e))", "(a ((d e)))" );
		matchNodeTestSX( parser2, "(a (d e) (d e))", "(a ((d e) (d e)))" );

		ParserExpression parser3 = new ListNode( new Object[] { new Literal( "a" ), new OneOrMore( new Sequence( new Object[] { new Literal( "d" ), new Literal( "e" ) } ) ) } );
		matchNodeFailTestSX( parser3, "(a)" );
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

	public void testSuppress()
	{
		ParserExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new Sequence( new Object[] { new Literal( "b" ).suppress(), new Literal( "c" ) } ) } );
		matchNodeTestSX( parser1, "(a b c)", "(a (c))" );
		matchNodeFailTestSX( parser1, "(a (b c))" );
	}

	public void testOptional()
	{
		ParserExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new Sequence( new Object[] { new Literal( "b" ).optional(), new Literal( "c" ) } ) } );
		matchNodeTestSX( parser1, "(a b c)", "(a (b c))" );
		matchNodeTest( parser1, Arrays.asList( new Object[] { "a", "c" } ), Arrays.asList( new Object[] { "a", Arrays.asList( new Object[] { null, "c" } ) } ) );
		matchNodeFailTestSX( parser1, "(a (b c))" );
	}
}
