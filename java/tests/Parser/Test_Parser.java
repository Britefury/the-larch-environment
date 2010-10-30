//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Parser;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMSchema;
import BritefuryJ.DocPresent.StreamValue.StreamValueBuilder;
import BritefuryJ.Parser.Action;
import BritefuryJ.Parser.AnyList;
import BritefuryJ.Parser.AnyNode;
import BritefuryJ.Parser.AnyObject;
import BritefuryJ.Parser.AnyString;
import BritefuryJ.Parser.Choice;
import BritefuryJ.Parser.Combine;
import BritefuryJ.Parser.Condition;
import BritefuryJ.Parser.Delegate;
import BritefuryJ.Parser.Keyword;
import BritefuryJ.Parser.ListNode;
import BritefuryJ.Parser.Literal;
import BritefuryJ.Parser.LiteralNode;
import BritefuryJ.Parser.ObjectNode;
import BritefuryJ.Parser.OneOrMore;
import BritefuryJ.Parser.Optional;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParseCondition;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.ParserExpression.ParserCoerceException;
import BritefuryJ.Parser.Peek;
import BritefuryJ.Parser.PeekNot;
import BritefuryJ.Parser.Production;
import BritefuryJ.Parser.Production.CannotOverwriteProductionExpressionException;
import BritefuryJ.Parser.RegEx;
import BritefuryJ.Parser.Repetition;
import BritefuryJ.Parser.SeparatedList;
import BritefuryJ.Parser.SeparatedList.CannotApplyConditionAfterActionException;
import BritefuryJ.Parser.SeparatedList.CannotApplyMoreThanOneActionException;
import BritefuryJ.Parser.SeparatedList.CannotApplyMoreThanOneConditionException;
import BritefuryJ.Parser.Sequence;
import BritefuryJ.Parser.StringNode;
import BritefuryJ.Parser.StructuralObject;
import BritefuryJ.Parser.Suppress;
import BritefuryJ.Parser.TracedParseResult;
import BritefuryJ.Parser.Word;
import BritefuryJ.Parser.ZeroOrMore;

public class Test_Parser extends ParserTestCase
{
	public static ParserExpression identifier = new RegEx( "[A-Za-z_][A-Za-z0-9_]*" );
	public ParserExpression foo, bar, bar2;

	protected static DMSchema s;
	protected static DMObjectClass Foo, Bar, Bar2, A, Add, Sub, Mul;
	

	
	static
	{
		s = new DMSchema( "s", "m", "tests.Parser.Test_Parser.s" );
		Foo = s.newClass( "Foo", new String[] { "a" } );
		Bar = s.newClass( "Bar", new String[] { "b" } );
		Bar2 = s.newClass( "Bar2", Bar, new String[] { "c" } );
		A = s.newClass( "A", new String[] { "x", "y" } );
		Add = s.newClass( "Add", new String[] { "a", "b" } );
		Sub = s.newClass( "Sub", new String[] { "a", "b" } );
		Mul = s.newClass( "Mul", new String[] { "a", "b" } );
	}

	
	
	private List<Object> arrayToList2D(Object[][] a)
	{
		ArrayList<Object> v = new ArrayList<Object>();
		for (int i = 0; i < a.length; i++)
		{
			ArrayList<Object> v2 = new ArrayList<Object>();
			for (int j = 0; j < a[i].length; j++)
			{
				v2.add( a[i][j] );
			}
			v.add( v2 );
		}
		
		return v;
	}

	
	
	
	public void testAction() throws ParserExpression.ParserCoerceException
	{
		ParseAction f = new ParseAction()
		{
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				String v = (String)value;
				return v + v;
			}
		};

		ParseAction g = new ParseAction()
		{
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				String v = (String)value;
				return v + v + v;
			}
		};

		ParseAction f_l = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				ArrayList<Object> x = new ArrayList<Object>();
				x.addAll( v );
				x.addAll( v );
				return x;
			}
		};

		assertTrue( new Action( "abc", f ).compareTo( new Action( "abc", f ) ) );
		assertFalse( new Action( "abc", f ).compareTo( new Action( "def", f ) ) );
		assertFalse( new Action( "abc", f ).compareTo( new Action( "abc", g ) ) );
		assertTrue( new Action( "abc", f ).compareTo( new Literal( "abc" ).action( f ) ) );
		

		ParserExpression parser = new Literal( "abc" ).action( f );
		matchTestStringAndStream( parser, "abc", "abcabc" );
		
		matchTestNodeSX( new AnyNode().action( f_l ), "[a b c]", "[a b c a b c]" );
		matchTestListSX( new AnyNode().action( f_l ), "[[a b c]]", "[a b c a b c]" );
	}



	public void testAction_mergeUp() throws ParserExpression.ParserCoerceException
	{
		ParseAction f = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				ArrayList<Object> x = new ArrayList<Object>();
				x.addAll( v );
				x.addAll( v );
				return x;
			}
		};

		assertTrue( new Action( "abc", f, true ).compareTo( new Literal( "abc" ).mergeUpAction( f ) ) );
		

		ParserExpression parser = ( ( new Literal( "a" ).__add__( "b" ).__add__( "c" ) ).mergeUpAction( f ) ).__add__( "d" );
		matchTestStringAndStreamSX( parser, "abcd", "[a b c a b c d]" );
		
		matchTestListSX( parser, "[a b c d]", "[a b c a b c d]" );
	}



	public void testAnyList()
	{
		assertTrue( new AnyList().compareTo( new AnyList() ) );
		
		matchFailTestStringAndStream( new AnyList(), "abc" );
		
		matchTestStreamSX( new AnyList(), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( readInputSX( "[a b]" ) ) } ).stream(), "[a b]" );
		matchFailTestStream( new AnyList(), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( readInputSX( "a" ) ) } ).stream() );
		
		matchTestNodeSX( new AnyList(), "[a b c]", "[a b c]" );
		matchFailTestNodeSX( new AnyList(), "a" );
		
		matchTestListSX( new AnyList(), "[[a b c]]", "[a b c]" );
		matchFailTestListSX( new AnyList(), "[a b c]" );
	}

	public void testAnyNode()
	{
		assertTrue( new AnyNode().compareTo( new AnyNode() ) );

		matchFailTestStringAndStream( new AnyNode(), "abc" );
		
		matchTestStreamSX( new AnyNode(), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( readInputSX( "[a b]" ) ) } ).stream(), "[a b]" );
		matchTestStreamSX( new AnyNode(), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( readInputSX( "a" ) ) } ).stream(), "a" );
		
		matchTestNodeSX( new AnyNode(), "a", "a" );
		
		matchTestListSX( new AnyNode(), "[a]", "a" );
		matchTestListSX( new AnyNode(), "[[a]]", "[a]" );
	}

	public void testAnyObject()
	{
		assertTrue( new AnyObject().compareTo( new AnyObject() ) );
	
		matchFailTestStringAndStream( new AnyObject(), "abc" );
		
		matchTestStreamSX( new AnyObject(), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( readInputSX( "{m=tests.Parser.Test_Parser.s : (m Foo a=xyz)}" ) ) } ).stream(), "{m=tests.Parser.Test_Parser.s : (m Foo a=xyz)}" );
		matchFailTestStream( new AnyObject(), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( readInputSX( "a" ) ) } ).stream() );
		
		matchTestNodeSX( new AnyObject(), "{m=tests.Parser.Test_Parser.s : (m Foo a=xyz)}", "{m=tests.Parser.Test_Parser.s : (m Foo a=xyz)}" );
		matchFailTestNodeSX( new AnyObject(), "[a]" );
		matchFailTestNodeSX( new AnyObject(), "a" );
		
		matchTestListSX( new AnyObject(), "{m=tests.Parser.Test_Parser.s : [(m Foo a=xyz)]}", "{m=tests.Parser.Test_Parser.s : (m Foo a=xyz)}" );
		matchFailTestListSX( new AnyObject(), "[a]", "a" );
		matchFailTestListSX( new AnyObject(), "[[a b c]]" );
	}

	public void testAnyString()
	{
		assertTrue( new AnyString().compareTo( new AnyString() ) );
	
		matchFailTestStringAndStream( new AnyString(), "abc" );
		
		matchTestStreamSX( new AnyString(), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( readInputSX( "a" ) ) } ).stream(), "a" );
		matchFailTestStream( new AnyString(), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( readInputSX( "[a b]" ) ) } ).stream() );

		matchTestNodeSX( new AnyString(), "a", "a" );
		matchFailTestNodeSX( new AnyString(), "[a]" );
		
		matchTestListSX( new AnyString(), "[a]", "a" );
		matchFailTestListSX( new AnyString(), "[[a b c]]" );
	}


	public void testBind()
	{
		ParserExpression parser1 = identifier.bindTo(  "x" );
		
		matchTestStringAndStreamSX( parser1, "abc", "abc" );
		bindingsTestStringAndStreamSX( parser1, "abc", "[[x abc]]" );

		ParserExpression parser2 = identifier.bindTo( "x" ).bindTo( "y" );
		
		matchTestStringAndStreamSX( parser2, "abc", "abc" );
		bindingsTestStringAndStreamSX( parser2, "abc", "[[x abc] [y abc]]" );
		
		bindingsTestNodeSX( new ObjectNode( Foo ).bindTo( "x" ),"{m=tests.Parser.Test_Parser.s : (m Foo a=a)}", "{m=tests.Parser.Test_Parser.s : [[x (m Foo a=a)]]}" );
		bindingsTestListSX( new AnyString().bindTo( "x" ),"[a]", "{m=tests.Parser.Test_Parser.s : [[x a]]}" );
	}

	
	public void testChoice() throws ParserExpression.ParserCoerceException
	{
		Object[] abqwfh = { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) };
		Object[] abqw = { new Literal( "ab" ), new Literal( "qw" ) };
		Object[] qbqwfh = { new Literal( "qb" ), new Literal( "qw" ), new Literal( "fh" ) };
		
		assertTrue( new Choice( abqwfh ).compareTo( new Choice( abqwfh ) ) );
		assertFalse( new Choice( abqwfh ).compareTo( new Choice( abqw ) ) );
		assertFalse( new Choice( abqwfh ).compareTo( new Choice( qbqwfh ) ) );
		assertTrue( new Choice( abqwfh ).compareTo( new Literal( "ab" ).__or__( new Literal( "qw" ) ).__or__( new Literal( "fh" ) ) ) );
		assertTrue( new Choice( abqwfh ).compareTo( new Literal( "ab" ).__or__( "qw" ).__or__( "fh" ) ) );

		
		ParserExpression parser = new Choice( new Object[] { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) } );
		ParserExpression parserWithBindings = new Choice( new Object[] { new Literal( "ab" ).bindTo( "x" ), new Literal( "cd" ).bindTo( "y" ), new Literal( "ef" ).bindTo( "z" ) } );
		
		matchTestStringAndStream( parser, "ab", "ab" );
		matchTestStringAndStream( parser, "qw", "qw" );
		matchTestStringAndStream( parser, "fh", "fh" );
		matchFailTestStringAndStream( parser, "xy" );
		matchSubTestStringAndStream( new Literal( "ab" ).__or__( "abcd" ), "ab", "ab", 2 );
		matchSubTestStringAndStream( new Literal( "ab" ).__or__( "abcd" ), "abcd", "ab", 2 );
		
		bindingsTestStringAndStreamSX( parserWithBindings, "ab", "[[x ab]]" );
		bindingsTestStringAndStreamSX( parserWithBindings, "cd", "[[y cd]]" );
		bindingsTestStringAndStreamSX( parserWithBindings, "ef", "[[z ef]]" );


		
		ParserExpression nodeParser = new Choice( new Object[] { new ObjectNode( Foo ), new ObjectNode( Bar2 ) } );
		ParserExpression nodeParserWithBindings = new Choice( new Object[] { new ObjectNode( Foo ).bindTo( "x" ), new ObjectNode( Bar2 ).bindTo( "y" ) } );

		matchTestNodeSX( nodeParser, "{m=tests.Parser.Test_Parser.s : (m Foo a=x)}", "{m=tests.Parser.Test_Parser.s : (m Foo a=x)}" );
		matchTestNodeSX( nodeParser, "{m=tests.Parser.Test_Parser.s : (m Bar2 b=x c=y)}", "{m=tests.Parser.Test_Parser.s : (m Bar2 b=x c=y)}" );
		matchFailTestNodeSX( nodeParser, "{m=tests.Parser.Test_Parser.s : (m Bar b=x)}" );
		
		bindingsTestNodeSX( nodeParserWithBindings, "{m=tests.Parser.Test_Parser.s : (m Foo a=x)}", "{m=tests.Parser.Test_Parser.s : [[x (m Foo a=x)]]}" );
		bindingsTestNodeSX( nodeParserWithBindings, "{m=tests.Parser.Test_Parser.s : (m Bar2 b=x c=y)}", "{m=tests.Parser.Test_Parser.s : [[y (m Bar2 b=x c=y)]]}" );
		
		
		matchTestListSX( parser, "[ab]", "ab" );
		matchTestListSX( parser, "[qw]", "qw" );
		matchTestListSX( parser, "[fh]", "fh" );
		matchFailTestListSX( parser, "[xy]" );

		bindingsTestListSX( parserWithBindings, "[ab]", "[[x ab]]" );
		bindingsTestListSX( parserWithBindings, "[cd]", "[[y cd]]" );
		bindingsTestListSX( parserWithBindings, "[ef]", "[[z ef]]" );
	}


	public void testClearBindings()
	{
		ParserExpression parser1 = identifier.bindTo(  "x" );
		
		matchTestStringAndStreamSX( parser1, "abc", "abc" );
		bindingsTestStringAndStreamSX( parser1, "abc", "[[x abc]]" );
	
		ParserExpression parser2 = parser1.clearBindings();
		
		matchTestStringAndStreamSX( parser2, "abc", "abc" );
		bindingsTestStringAndStreamSX( parser2, "abc", "[]" );
		
		
		matchTestNodeSX( new ObjectNode( Foo ).bindTo( "x" ).clearBindings(), "{m=tests.Parser.Test_Parser.s : (m Foo a=x)}", "{m=tests.Parser.Test_Parser.s : (m Foo a=x)}" );
		bindingsTestNodeSX( new ObjectNode( Foo ).bindTo( "x" ).clearBindings(), "{m=tests.Parser.Test_Parser.s : (m Foo a=x)}", "[]" );

		matchTestListSX( new AnyString().bindTo( "x" ).clearBindings(), "[a]", "a" );
		bindingsTestListSX( new AnyString().bindTo( "x" ).clearBindings(), "[a]", "[]" );
	}

	
	public void testCombine() throws ParserExpression.ParserCoerceException
	{
		Object[] abqwfh = { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) };
		Object[] abqw = { new Literal( "ab" ), new Literal( "qw" ) };
		Object[] qbqwfh = { new Literal( "qb" ), new Literal( "qw" ), new Literal( "fh" ) };
		
		assertTrue( new Combine( abqwfh ).compareTo( new Combine( abqwfh ) ) );
		assertFalse( new Combine( abqwfh ).compareTo( new Combine( abqw ) ) );
		assertFalse( new Combine( abqwfh ).compareTo( new Combine( qbqwfh ) ) );
		assertTrue( new Combine( abqwfh ).compareTo( new Literal( "ab" ).__sub__( new Literal( "qw" ) ).__sub__( new Literal( "fh" ) ) ) );
		assertTrue( new Combine( abqwfh ).compareTo( new Literal( "ab" ).__sub__( "qw" ).__sub__( "fh" ) ) );
	
		ParserExpression parser = new Combine( new Object[] { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) } );
		
		matchTestStringAndStream( parser, "abqwfh", "abqwfh" );
		matchFailTestStringAndStream( parser, "abfh" );
	
	
	
		Object[] subsB1 = { new Literal( "ab" ), new Literal( "cd" ) };
		Object[] subsB2 = { new Literal( "ef" ), new Literal( "gh" ) };
		Object[] subsB3 = { new Literal( "ij" ), new Literal( "kl" ) };
		Object[] subsB = { new Sequence( subsB1 ), new Sequence( subsB2 ), new Sequence( subsB3 ) };
		ParserExpression parser2 = new Combine( subsB );
		String[] result2 = { "ab", "cd", "ef", "gh", "ij", "kl" };
		matchTestStringAndStream( parser2, "abcdefghijkl", Arrays.asList( result2 ) );
	
		Object[] subsC = { new Sequence( subsB1 ), new Sequence( subsB2 ), new Sequence( subsB3 ), new Literal( "xyz" ) };
		ParserExpression parser3 = new Combine( subsC );
		String[] result3 = { "ab", "cd", "ef", "gh", "ij", "kl", "xyz" };
		matchTestStringAndStream( parser3, "abcdefghijklxyz", Arrays.asList( result3 ) );

	
	
		ParserExpression parserWithBindings = new Combine( new Object[] { new Literal( "ab" ).bindTo( "x" ), new Literal( "qw" ).bindTo( "y" ), new Literal( "fh" ).bindTo( "x" ) } );
		
		matchTestStringAndStream( parserWithBindings, "abqwfh", "abqwfh" );
		bindingsTestStringAndStreamSX( parserWithBindings, "abqwfh", "[[x fh] [y qw]]" );

	
	
		ParserExpression objectParserWithBindings = new Combine( new Object[] { new ObjectNode( Foo ).bindTo( "x" ), new ObjectNode( Bar ).bindTo( "y" ), new ObjectNode( Foo ).bindTo( "x" ) } );
		matchFailTestNodeSX( objectParserWithBindings, "{m=tests.Parser.Test_Parser.s : [(m Foo a=x) (m Bar b=y) (m Foo a=z)]}" );

		ParserExpression listParserWithBindings = new Combine( new Object[] { new AnyList().bindTo( "x" ), new AnyList().bindTo( "y" ), new AnyList().bindTo( "x" ) } );
		matchTestListSX( listParserWithBindings, "[[a] [b] [c]]", "[a b c]" );
		bindingsTestListSX( listParserWithBindings, "[[a] [b] [c]]", "[[x [c]] [y [b]]]" );
	}



	public void testCondition() throws ParserExpression.ParserCoerceException
	{
		ParseCondition f = new ParseCondition()
		{
			public boolean test(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				String v = (String)value;
				return v.startsWith( "hello" );
			}
		};

		ParseCondition g = new ParseCondition()
		{
			public boolean test(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				String v = (String)value;
				return v.startsWith( "there" );
			}
		};

		assertTrue( new Condition( "abc", f ).compareTo( new Condition( "abc", f ) ) );
		assertFalse( new Condition( "abc", f ).compareTo( new Condition( "def", f ) ) );
		assertFalse( new Condition( "abc", f ).compareTo( new Condition( "abc", g ) ) );
		assertTrue( new Condition( "abc", f ).compareTo( new Literal( "abc" ).condition( f ) ) );
		
		ParserExpression parser = new Word( "abcdefghijklmnopqrstuvwxyz" ).condition( f );
		
		matchTestStringAndStream( parser, "helloworld", "helloworld" );
		matchFailTestStringAndStream( parser, "xabcdef" );
	}



	public void testDelegate() throws ParserExpression.ParserCoerceException
	{
		ParseAction f_s = new ParseAction()
		{
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				String v = (String)value;
				return v + v;
			}
		};

		ParseAction f_l = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				ArrayList<Object> x = new ArrayList<Object>();
				x.addAll( v );
				x.addAll( v );
				return x;
			}
		};

		assertTrue( new Delegate( "abc" ).compareTo( new Delegate( "abc" ) ) );
		assertFalse( new Delegate( "abc" ).compareTo( new Delegate( "def" ) ) );
		

		ParserExpression parser = new Delegate( new Literal( "abc" ) );
		matchTestStringAndStream( parser, "abc", "abcabc", f_s );
		
		matchTestNodeSX( new Delegate( new AnyNode() ), "[a b c]", "[a b c a b c]", f_l );
		matchTestListSX( new Delegate( new AnyNode() ), "[[a b c]]", "[a b c a b c]", f_l );
	}



	public void testKeyword()
	{
		assertTrue( new Keyword( "abc" ).compareTo( new Keyword( "abc" ) ) );
		assertFalse( new Keyword( "abc" ).compareTo( new Keyword( "def" ) ) );
		assertTrue( new Keyword( "abc", "xyz" ).compareTo( new Keyword( "abc", "xyz" ) ) );
		assertFalse( new Keyword( "abc", "xyz" ).compareTo( new Keyword( "def", "xyz" ) ) );
		assertFalse( new Keyword( "abc", "xyz" ).compareTo( new Keyword( "abc", "pqr" ) ) );
		
		matchTestStringAndStream( new Keyword( "hello" ), "hello", "hello" );
		matchFailTestStringAndStream( new Keyword( "hello" ), "helloq" );
		matchSubTestStringAndStream( new Keyword( "hello", "abc" ), "hello", "hello", 5 );
		matchSubTestStringAndStream( new Keyword( "hello", "abc" ), "helloxx", "hello", 5 );
		matchFailTestStringAndStream( new Keyword( "hello", "abc" ), "helloaa" );
		matchTestStringAndStreamSX( new Keyword( "hello" ).__add__( new Keyword( "there" ) ), "hello there", "[hello there]" );
		
		matchTestStream( new Keyword( "hello" ), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( "hello" ) } ).stream(), "hello" );

		matchTestNode( new Keyword( "hello" ), "hello", "hello" );

		matchTestListSX( new Keyword( "hello" ), "[hello]", "hello" );
	}
	
	
	public void testListNode() throws ParserCoerceException
	{
		ParserExpression parser1 = new ListNode( new Object[] { new Literal( "abc" ) } );
		matchTestNodeSX( parser1, "[abc]", "[abc]" );
		matchFailTestNodeSX( parser1, "[abcde]" );
		matchFailTestNodeSX( parser1, "[abc de]" );
		matchFailTestString( parser1, "abc" );
		matchTestStreamSX( parser1, new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( readInputSX( "[abc]" ) ) } ).stream(), "[abc]" );
		matchTestListSX( parser1, "[[abc]]", "[abc]" );

		ParserExpression parser2 = new ListNode( new Object[] { new Literal( "abc" ), new Literal( "def" ) } );
		matchTestNodeSX( parser2, "[abc def]", "[abc def]" );
		matchFailTestNodeSX( parser2, "[abcx def]" );
		matchFailTestNodeSX( parser2, "[abc defx]" );
		matchFailTestNodeSX( parser2, "[abcx defx]" );

		ParserExpression parser3 = new ListNode( new Object[] { new Literal( "abc" ), new ListNode( new Object[] { new Literal( "d" ), new Literal( "e" ) } ) } );
		matchTestNodeSX( parser3, "[abc [d e]]", "[abc [d e]]" );
		matchFailTestNodeSX( parser3, "[abc [de]]" );
		matchFailTestNodeSX( parser3, "[abc de]" );
		matchFailTestNodeSX( parser3, "[abc [dx e]]" );
		matchFailTestNodeSX( parser3, "[abc [d ex]]" );
		matchFailTestNodeSX( parser3, "[abc [dx ex]]" );

		ParserExpression parser4 = new ListNode( new Object[] { new Literal( "abc" ), new ListNode( new Object[] { new Literal( "d" ) } ).bindTo( "x" ) } );
		matchTestNodeSX( parser4, "[abc [d]]", "[abc [d]]" );
		matchFailTestNodeSX( parser4, "[abc d]" );
		bindingsTestNodeSX( parser4, "[abc [d]]", "[[x [d]]]" );
		matchFailTestString( parser4, "abcd" );
		matchTestStreamSX( parser4, new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( readInputSX( "[abc [d]]" ) ) } ).stream(), "[abc [d]]" );
		bindingsTestStreamSX( parser4, new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( readInputSX( "[abc [d]]" ) ) } ).stream(), "[[x [d]]]" );
		matchTestListSX( parser4, "[[abc [d]]]", "[abc [d]]" );
		bindingsTestListSX( parser4, "[[abc [d]]]", "[[x [d]]]" );

		ParserExpression parser5 = new ListNode( new Object[] { identifier.bindTo( "x" ), identifier.bindTo( "x" ) } );
		matchTestNodeSX( parser5, "[abc abc]", "[abc abc]" );
		bindingsTestNodeSX( parser5, "[abc abc]", "[[x abc]]" );
		matchTestNodeSX( parser5, "[abc d]", "[abc d]" );
		bindingsTestNodeSX( parser5, "[abc d]", "[[x d]]" );

		ParserExpression parser6 = new ListNode( new Object[] { identifier.bindTo( "x" ), new ListNode( new Object[] { identifier.bindTo( "x" ) } ) } );
		matchTestNodeSX( parser6, "[abc [abc]]", "[abc [abc]]" );
		bindingsTestNodeSX( parser6, "[abc [abc]]", "[[x abc]]" );
		matchTestNodeSX( parser6, "[abc [d]]", "[abc [d]]" );
		bindingsTestNodeSX( parser6, "[abc [d]]", "[[x d]]" );

		ParserExpression parser7 = new ListNode( new Object[] { new Literal( "abc" ), null } );
		matchTestNodeSX( parser7, "[abc `null`]", "[abc `null`]" );
		matchFailTestNodeSX( parser7, "[abcx def]" );
		matchFailTestNodeSX( parser7, "[abc defx]" );
		matchFailTestNodeSX( parser7, "[abcx defx]" );
	}

	
	
	public void testLiteral()
	{
		assertTrue( new Literal( "abc" ).compareTo( new Literal( "abc" ) ) );
		assertFalse( new Literal( "abc" ).compareTo( new Literal( "def" ) ) );
		
		matchTestStringAndStream( new Literal( "abcxyz" ), "abcxyz", "abcxyz" );
		matchFailTestStringAndStream( new Literal( "abcxyz" ), "qwerty" );
		matchSubTestStringAndStream( new Literal( "abcxyz" ), "abcxyz123", "abcxyz", 6 );

		
		matchTestStream( new Literal( "hello" ), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( "hello" ) } ).stream(), "hello" );

		matchTestNode( new Literal( "hello" ), "hello", "hello" );

		matchTestListSX( new Literal( "hello" ), "[hello]", "hello" );
	}


	public void testLiteralNode()
	{
		assertTrue( new LiteralNode( "abc" ).compareTo( new LiteralNode( "abc" ) ) );
		assertFalse( new LiteralNode( "abc" ).compareTo( new LiteralNode( "def" ) ) );
		
		matchTestNode( new LiteralNode( "hello" ), "hello", "hello" );
		matchFailTestStringAndStream( new LiteralNode( "hello" ), "hello" );
		matchTestStream( new LiteralNode( "hello" ), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( "hello" ) } ).stream(), "hello" );
		matchTestListSX( new LiteralNode( "hello" ), "[hello]", "hello" );

		matchTestNodeSX( new LiteralNode( Foo.newInstance( new Object[] { "x" } ) ), "{m=tests.Parser.Test_Parser.s : (m Foo a=x)}", "{m=tests.Parser.Test_Parser.s : (m Foo a=x)}" );
		matchTestStreamSX( new LiteralNode( Foo.newInstance( new Object[] { "x" } ) ),
				new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( readInputSX( "{m=tests.Parser.Test_Parser.s : (m Foo a=x)}" ) ) } ).stream(), "{m=tests.Parser.Test_Parser.s : (m Foo a=x)}" );
		matchTestListSX( new LiteralNode( Foo.newInstance( new Object[] { "x" } ) ), "{m=tests.Parser.Test_Parser.s : [(m Foo a=x)]}", "{m=tests.Parser.Test_Parser.s : (m Foo a=x)}" );
	}


	public void testObjectNode() throws ParserCoerceException
	{
		ParserExpression parser1 = new ObjectNode( A, new String[] { "x" }, new Object[] { new Literal( "abc" ) } );
		matchTestNodeSX( parser1, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=xyz)}", "{m=tests.Parser.Test_Parser.s : (m A x=abc y=xyz)}" );
		matchTestNodeSX( parser1, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=pqr)}", "{m=tests.Parser.Test_Parser.s : (m A x=abc y=pqr)}" );
		matchFailTestNodeSX( parser1, "{m=tests.Parser.Test_Parser.s : (m A x=pqr y=xyz)}" );
		matchFailTestString( parser1, "abc" );
		matchTestStreamSX( parser1, new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( readInputSX( "{m=tests.Parser.Test_Parser.s : (m A x=abc y=xyz)}" ) ) } ).stream(),
				"{m=tests.Parser.Test_Parser.s : (m A x=abc y=xyz)}" );
		matchTestListSX( parser1, "{m=tests.Parser.Test_Parser.s : [(m A x=abc y=xyz)]}", "{m=tests.Parser.Test_Parser.s : (m A x=abc y=xyz)}" );
		
		ParserExpression parser2 = new ObjectNode( A, new String[] { "y" }, new Object[] { new Literal( "xyz" ) } );
		matchTestNodeSX( parser2, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=xyz)}", "{m=tests.Parser.Test_Parser.s : (m A x=abc y=xyz)}" );
		matchTestNodeSX( parser2, "{m=tests.Parser.Test_Parser.s : (m A x=pqr y=xyz)}", "{m=tests.Parser.Test_Parser.s : (m A x=pqr y=xyz)}" );
		matchFailTestNodeSX( parser2, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=pqr)}" );
		
		ParserExpression parser3 = new ObjectNode( A, new String[] { "x", "y" }, new Object[] { new Literal( "abc" ), new Literal( "xyz" ) } );
		matchTestNodeSX( parser3, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=xyz)}", "{m=tests.Parser.Test_Parser.s : (m A x=abc y=xyz)}" );
		matchFailTestNodeSX( parser3, "{m=tests.Parser.Test_Parser.s : (m A x=pqr y=xyz)}" );
		matchFailTestNodeSX( parser3, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=pqr)}" );
		
		ParserExpression parser4 = new ObjectNode( A, new String[] { "x", "y" }, new Object[] { new Literal( "abc" ), new ListNode( new Object[] { new Literal( "d" ), new Literal( "e" ) } ) } );
		matchTestNodeSX( parser4, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=[d e])}", "{m=tests.Parser.Test_Parser.s : (m A x=abc y=[d e])}" );
		matchFailTestNodeSX( parser4, "{m=tests.Parser.Test_Parser.s : (m A x=pqr y=xyz)}" );
		matchFailTestNodeSX( parser4, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=[p q])}" );
		
		ParserExpression parser5 = new ObjectNode( A, new String[] { "x", "y" }, new Object[] { new Literal( "abc" ), new ListNode( new Object[] { new Literal( "d" ) } ).bindTo( "x" ) } );
		matchTestNodeSX( parser5, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=[d])}", "{m=tests.Parser.Test_Parser.s : (m A x=abc y=[d])}" );
		bindingsTestNodeSX( parser5, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=[d])}", "[[x [d]]]" );
		
		ParserExpression parser6 = new ObjectNode( A, new String[] { "x", "y" }, new Object[] { identifier.bindTo( "x" ), identifier.bindTo( "x" ) } );
		matchTestNodeSX( parser6, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=abc)}", "{m=tests.Parser.Test_Parser.s : (m A x=abc y=abc)}" );
		matchTestNodeSX( parser6, "{m=tests.Parser.Test_Parser.s : (m A x=def y=def)}", "{m=tests.Parser.Test_Parser.s : (m A x=def y=def)}" );
		bindingsTestNodeSX( parser6, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=abc)}", "[[x abc]]" );
		bindingsTestNodeSX( parser6, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=def)}", "[[x def]]" );
		bindingsTestStreamSX( parser6, new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( readInputSX( "{m=tests.Parser.Test_Parser.s : (m A x=abc y=def)}" ) ) } ).stream(),
		"[[x def]]" );
		bindingsTestListSX( parser6, "{m=tests.Parser.Test_Parser.s : [(m A x=abc y=def)]}", "[[x def]]" );
		
		ParserExpression parser7 = new ObjectNode( Bar, new String[] { "b" }, new Object[] { identifier } );
		matchTestNodeSX( parser7, "{m=tests.Parser.Test_Parser.s : (m Bar b=abc)}", "{m=tests.Parser.Test_Parser.s : (m Bar b=abc)}" );
		matchTestNodeSX( parser7, "{m=tests.Parser.Test_Parser.s : (m Bar2 b=abc c=def)}", "{m=tests.Parser.Test_Parser.s : (m Bar2 b=abc c=def)}" );

		ParserExpression parser8 = new ObjectNode( A, new String[] { "y" }, new Object[] { null } );
		matchTestNodeSX( parser8, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=`null`)}", "{m=tests.Parser.Test_Parser.s : (m A x=abc y=`null`)}" );
		matchTestNodeSX( parser8, "{m=tests.Parser.Test_Parser.s : (m A x=pqr y=`null`)}", "{m=tests.Parser.Test_Parser.s : (m A x=pqr y=`null`)}" );
		matchFailTestNodeSX( parser8, "{m=tests.Parser.Test_Parser.s : (m A x=abc y=pqr)}" );
	}


	
	
	public void testOneOrMore() throws ParserExpression.ParserCoerceException
	{
		assertTrue( new OneOrMore( new Literal( "ab" ) ).compareTo( new OneOrMore( new Literal( "ab" ) ) ) );
		assertFalse( new OneOrMore( new Literal( "ab" ) ).compareTo( new OneOrMore( new Literal( "cd" ) ) ) );
		assertTrue( new OneOrMore( new Literal( "ab" ) ).compareTo( new OneOrMore( "ab" ) ) );
	
		ParserExpression parser = new OneOrMore( new Word( "a", "b" ).__add__( new Word( "c", "d" ) ) );
		
		String[][] result1 = { { "ab", "cd", } };
		String[][] result2 = { { "ab", "cd", },   { "abb", "cdd" } };
		String[][] result3 = { { "ab", "cd", },   { "abb", "cdd" },   { "abbb", "cddd" } };
		
		matchFailTestStringAndStream( parser, "" );
		
		matchTestStringAndStream( parser, "abcd", arrayToList2D( result1 ) );
		matchTestStringAndStream( parser, "abcdabbcdd", arrayToList2D( result2 ) );
		matchTestStringAndStream( parser, "abcdabbcddabbbcddd", arrayToList2D( result3 ) );
		
		ParserExpression parser2 = new OneOrMore( new Word( "a", "b" ) );
		matchFailTestListSX( parser2, "[]" );
		matchTestListSX( parser2, "[ab]", "[ab]" );
		matchTestListSX( parser2, "[ab abb abbb]", "[ab abb abbb]" );
		matchFailTestStream( parser2, new StreamValueBuilder().stream() );
		matchTestStreamSX( parser2, new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( "ab" ) } ).stream(), "[ab]" );
		matchTestStreamSX( parser2, new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.TextItem( "ab" ), new StreamValueBuilder.StructuralItem( "ab" ) } ).stream(),
		"[ab ab]" );
	}



	public void testOptional() throws ParserExpression.ParserCoerceException
	{
		assertTrue( new Optional( new Literal( "ab" ) ).compareTo( new Optional( new Literal( "ab" ) ) ) );
		assertFalse( new Optional( new Literal( "ab" ) ).compareTo( new Optional( new Literal( "cd" ) ) ) );
		assertTrue( new Optional( new Literal( "ab" ) ).compareTo( new Optional( "ab" ) ) );
	
		ParserExpression parser = new Word( "a", "b" ).optional();
		
		matchTestStringAndStream( parser, "", null );
		matchTestStringAndStream( parser, "abb", "abb" );
		matchSubTestStringAndStream( parser, "abbabb", "abb", 3 );
		
		matchTestNode( parser, "abb", "abb" );
		matchTestNode( parser, "", null );

		matchTestListSX( parser, "[abb]", "abb" );
		matchTestListSX( parser, "[]", "`null`" );
	}



	public void testPeek() throws ParserExpression.ParserCoerceException
	{
		assertTrue( new Peek( new Literal( "ab" ) ).compareTo( new Peek( new Literal( "ab" ) ) ) );
		assertFalse( new Peek( new Literal( "ab" ) ).compareTo( new Peek( new Literal( "cd" ) ) ) );
		assertTrue( new Peek( new Literal( "ab" ) ).compareTo( new Peek( "ab" ) ) );
		assertTrue( new Peek( new Literal( "ab" ) ).compareTo( new Literal( "ab" ).peek() ) );
	
		ParserExpression parser = new OneOrMore( new Word( "a", "b" ) ).__add__( new Peek( new Word( "c", "d" ) ) );
		
		String[][] result1 = { { "ab" } };
		String[][] result2 = { { "ab", "ab" } };
	
		matchFailTestStringAndStream( parser, "" );
		matchFailTestStringAndStream( parser, "ab" );
		matchFailTestStringAndStream( parser, "abab" );
		matchSubTestStringAndStream( parser, "abcd", arrayToList2D( result1 ), 2 );
		matchSubTestStringAndStream( parser, "ababcd", arrayToList2D( result2 ), 4 );
		
		matchTestNodeSX( new Word( "a", "b" ).peek(), "ab", "`null`" );

		matchFailTestListSX( parser, "[ab]" );
		matchSubTestListSX( parser, "[ab cd]", "[[ab]]", 1 );
	}



	public void testPeekNot() throws ParserExpression.ParserCoerceException
	{
		assertTrue( new PeekNot( new Literal( "ab" ) ).compareTo( new PeekNot( new Literal( "ab" ) ) ) );
		assertFalse( new PeekNot( new Literal( "ab" ) ).compareTo( new PeekNot( new Literal( "cd" ) ) ) );
		assertTrue( new PeekNot( new Literal( "ab" ) ).compareTo( new PeekNot( "ab" ) ) );
		assertTrue( new PeekNot( new Literal( "ab" ) ).compareTo( new Literal( "ab" ).peekNot() ) );
	
		ParserExpression parser = new OneOrMore( new Word( "a", "b" ) ).__add__( new PeekNot( new Word( "c", "d" ) ) );
		
		String[][] result1 = { { "ab" } };
		String[][] result2 = { { "ab", "ab" } };
	
		matchFailTestStringAndStream( parser, "" );
		matchFailTestStringAndStream( parser, "abcd" );
		matchFailTestStringAndStream( parser, "ababcd" );
		matchTestStringAndStream( parser, "ab", arrayToList2D( result1 ) );
		matchTestStringAndStream( parser, "abab", arrayToList2D( result2 ) );
		
		matchFailTestNodeSX( new Word( "a", "b" ).peekNot(), "ab" );
		matchTestNodeSX( new Word( "a", "b" ).peekNot(), "cd", "`null`" );

		matchTestListSX( parser, "[ab]", "[[ab]]" );
		matchFailTestListSX( parser, "[ab cd]" );
		matchSubTestListSX( parser, "[ab xy]", "[[ab]]", 1 );
	}



	public void testRegEx()
	{
		assertTrue( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ).compareTo( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ) ) );
		assertFalse( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ).compareTo( new RegEx( "[A-Za-z_][A-Za-z0-9_]*abc" ) ) );
	
		matchTestStringAndStream( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ), "abc_123", "abc_123" );
		matchFailTestStringAndStream( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ), "9abc" );
		matchSubTestStringAndStream( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ), "abc_xyz...", "abc_xyz", 7 );
		matchTestStringAndStream( new RegEx( "[A-Za-z_]*" ), "abc_", "abc_" );
		matchFailTestStringAndStream( new RegEx( "[A-Za-z_]*" ), "." );
	

		matchTestStream( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( "abc_123" ) } ).stream(), "abc_123" );

		matchTestNode( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ), "abc_123", "abc_123" );

		matchTestListSX( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ), "[abc_123]", "abc_123" );
	}
	
	
	public void testRepetition() throws ParserExpression.ParserCoerceException
	{
		assertTrue( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Repetition( new Literal( "ab" ), 0, 1, false ) ) );
		assertFalse( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Repetition( new Literal( "cd" ), 0, 1, false ) ) );
		assertFalse( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Repetition( new Literal( "ab" ), 1, 1, false ) ) );
		assertFalse( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Repetition( new Literal( "ab" ), 0, 2, false ) ) );
		assertFalse( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Repetition( new Literal( "ab" ), 0, 1, true ) ) );
		assertTrue( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Repetition( new Literal( "ab" ), 0, 1 ) ) );
		assertTrue( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Repetition( "ab", 0, 1 ) ) );
		assertTrue( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Literal( "ab" ).repeat( 0, 1, false ) ) );
		assertTrue( new Repetition( new Literal( "ab" ), 0, 1, true ).compareTo( new Literal( "ab" ).repeat( 0, 1, true ) ) );
	
		ParserExpression parser_2, parser02, parser02N, parser24, parser2_;
		parser_2 = new Repetition( new Word( "a", "b" ).__add__( new Word( "c", "d" ) ),  -1, 2 );
		parser02 = new Repetition( new Word( "a", "b" ).__add__( new Word( "c", "d" ) ),  0, 2 );
		parser02N = new Repetition( new Word( "a", "b" ).__add__( new Word( "c", "d" ) ),  0, 2, true );
		parser24 = new Repetition( new Word( "a", "b" ).__add__( new Word( "c", "d" ) ),  2, 4 );
		parser2_ = new Repetition( new Word( "a", "b" ).__add__( new Word( "c", "d" ) ),  2, -1 );
		
		String[][] result0 = {};
		String[][] result1 = { { "ab", "cd", } };
		String[][] result2 = { { "ab", "cd", },   { "abb", "cdd" } };
		String[][] result3 = { { "ab", "cd", },   { "abb", "cdd" },   { "abbb", "cddd" } };
		String[][] result4 = { { "ab", "cd", },   { "abb", "cdd" },   { "abbb", "cddd" },   { "abbbb", "cdddd" } };
		String[][] result5 = { { "ab", "cd", },   { "abb", "cdd" },   { "abbb", "cddd" },   { "abbbb", "cdddd" },   { "abbbbb", "cddddd" } };
		
		matchTestStringAndStream( parser_2, "", arrayToList2D( result0 ) );
		matchTestStringAndStream( parser02, "", arrayToList2D( result0 ) );
		matchTestStringAndStream( parser02N, "", null );
		matchFailTestStringAndStream( parser24, "" );
		matchFailTestStringAndStream( parser2_, "" );
		
		matchTestStringAndStream( parser_2, "abcd", arrayToList2D( result1 ) );
		matchTestStringAndStream( parser02, "abcd", arrayToList2D( result1 ) );
		matchTestStringAndStream( parser02N, "abcd", arrayToList2D( result1 ) );
		matchFailTestStringAndStream( parser24, "abcd" );
		matchFailTestStringAndStream( parser2_, "abcd" );
		
		matchTestStringAndStream( parser_2, "abcdabbcdd", arrayToList2D( result2 ) );
		matchTestStringAndStream( parser02, "abcdabbcdd", arrayToList2D( result2  ) );
		matchTestStringAndStream( parser02N, "abcdabbcdd", arrayToList2D( result2  ) );
		matchTestStringAndStream( parser24, "abcdabbcdd", arrayToList2D( result2  ) );
		matchTestStringAndStream( parser2_, "abcdabbcdd", arrayToList2D( result2  ) );
		
		matchSubTestStringAndStream( parser_2, "abcdabbcddabbbcddd", arrayToList2D( result2 ), 10 );
		matchSubTestStringAndStream( parser02, "abcdabbcddabbbcddd", arrayToList2D( result2 ), 10 );
		matchSubTestStringAndStream( parser02N, "abcdabbcddabbbcddd", arrayToList2D( result2 ), 10 );
		matchTestStringAndStream( parser24, "abcdabbcddabbbcddd", arrayToList2D( result3 ) );
		matchTestStringAndStream( parser2_, "abcdabbcddabbbcddd", arrayToList2D( result3 ) );
		
		matchSubTestStringAndStream( parser_2, "abcdabbcddabbbcdddabbbbcdddd", arrayToList2D( result2 ), 10 );
		matchSubTestStringAndStream( parser02, "abcdabbcddabbbcdddabbbbcdddd", arrayToList2D( result2 ), 10 );
		matchSubTestStringAndStream( parser02N, "abcdabbcddabbbcdddabbbbcdddd", arrayToList2D( result2 ), 10 );
		matchTestStringAndStream( parser24, "abcdabbcddabbbcdddabbbbcdddd", arrayToList2D( result4 ) );
		matchTestStringAndStream( parser2_, "abcdabbcddabbbcdddabbbbcdddd", arrayToList2D( result4 ) );
		
		matchSubTestStringAndStream( parser_2, "abcdabbcddabbbcdddabbbbcddddabbbbbcddddd", arrayToList2D( result2 ), 10 );
		matchSubTestStringAndStream( parser02, "abcdabbcddabbbcdddabbbbcddddabbbbbcddddd", arrayToList2D( result2 ), 10 );
		matchSubTestStringAndStream( parser02N, "abcdabbcddabbbcdddabbbbcddddabbbbbcddddd", arrayToList2D( result2 ), 10 );
		matchSubTestStringAndStream( parser24, "abcdabbcddabbbcdddabbbbcddddabbbbbcddddd", arrayToList2D( result4 ), 28 );
		matchTestStringAndStream( parser2_, "abcdabbcddabbbcdddabbbbcddddabbbbbcddddd", arrayToList2D( result5 ) );

	
		ParserExpression parserStructural = new Word( "a", "b" ).repeat( 2, 4 );
		matchFailTestListSX( parserStructural, "[ab]" );
		matchTestListSX( parserStructural, "[ab abb]", "[ab abb]" );
		matchTestListSX( parserStructural, "[ab abb abbb]", "[ab abb abbb]" );
		matchTestListSX( parserStructural, "[ab abb abbb abbb]", "[ab abb abbb abbb]" );
		matchSubTestListSX( parserStructural, "[ab abb abbb abbb ab]", "[ab abb abbb abbb]", 4 );
		matchTestStreamSX( parserStructural, new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.TextItem( "ab" ), new StreamValueBuilder.StructuralItem( "ab" ) } ).stream(),
		"[ab ab]" );
	}



	public void testSeparatedList() throws ParserExpression.ParserCoerceException, CannotApplyMoreThanOneConditionException, CannotApplyConditionAfterActionException, CannotApplyMoreThanOneActionException
	{
		assertTrue( new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).compareTo(
				new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) ) );
		assertFalse( new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).compareTo(
				new SeparatedList( new Literal( "a" ), new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) ) );
		assertFalse( new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).compareTo(
				new SeparatedList( identifier, new Literal( "." ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) ) );
		assertFalse( new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).compareTo(
				new SeparatedList( identifier, new Literal( "," ), new Literal( "{" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) ) );
		assertFalse( new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).compareTo(
				new SeparatedList( identifier, new Literal( "," ), null, new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) ) );
		assertFalse( new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).compareTo(
				new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "}" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) ) );
		assertFalse( new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).compareTo(
				new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), null, 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) ) );
		assertFalse( new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).compareTo(
				new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) ) );
		assertFalse( new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).compareTo(
				new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, 10, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) ) );
		assertFalse( new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).compareTo(
				new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.NEVER ) ) );
		
		assertTrue( new SeparatedList( identifier, new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).compareTo(
				new SeparatedList( identifier, new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) ) );
		assertTrue( new SeparatedList( identifier, new Literal( "," ), null, null, 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).compareTo(
				new SeparatedList( identifier, new Literal( "," ), 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) ) );
		assertTrue( new SeparatedList( identifier, new Literal( "," ), null, null, 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).compareTo(
				new SeparatedList( identifier, 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) ) );
		assertTrue( new SeparatedList( new Literal( "a" ), new Literal( "," ), new Literal( "[" ), new Literal( "]" ), 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).compareTo(
				new SeparatedList( "a", ",", "[", "]", 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ) ) );
	
		
		SeparatedList.ListCondition condition = new SeparatedList.ListCondition()
		{
			public boolean test(Object input, int begin, int end, List<Object> elements, Map<String, Object> bindings, boolean gotTrailingSeparator)
			{
				return elements.size() % 2  ==  0;
			}
		};
		
		SeparatedList.ListAction action = new SeparatedList.ListAction()
		{
			public Object invoke(Object input, int begin, int end, List<Object> elements, Map<String, Object> bindings, boolean gotTrailingSeparator)
			{
				if ( gotTrailingSeparator )
				{
					ArrayList<Object> values = new ArrayList<Object>();
					values.addAll( elements );
					values.add( "sep" );
					return values;
				}
				else
				{
					return elements;
				}
			}
		};
		
		ParserExpression parser0N = new SeparatedList( identifier, ",", 0, -1, SeparatedList.TrailingSeparatorPolicy.NEVER );
		ParserExpression parser1N = new SeparatedList( identifier, ",", 1, -1, SeparatedList.TrailingSeparatorPolicy.NEVER );
		ParserExpression parser2N = new SeparatedList( identifier, ",", 2, -1, SeparatedList.TrailingSeparatorPolicy.NEVER );
		ParserExpression parser23N = new SeparatedList( identifier, ",", 2, 3, SeparatedList.TrailingSeparatorPolicy.NEVER );
		ParserExpression parserDN = new SeparatedList( identifier, ",", "[", "]", 0, -1, SeparatedList.TrailingSeparatorPolicy.NEVER );
		ParserExpression parser0O = new SeparatedList( identifier, ",", 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL );
		ParserExpression parser1O = new SeparatedList( identifier, ",", 1, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL );
		ParserExpression parser2O = new SeparatedList( identifier, ",", 2, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL );
		ParserExpression parser23O = new SeparatedList( identifier, ",", 2, 3, SeparatedList.TrailingSeparatorPolicy.OPTIONAL );
		ParserExpression parserDO = new SeparatedList( identifier, ",", "[", "]", 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL );
		ParserExpression parser0R = new SeparatedList( identifier, ",", 0, -1, SeparatedList.TrailingSeparatorPolicy.REQUIRED );
		ParserExpression parser1R = new SeparatedList( identifier, ",", 1, -1, SeparatedList.TrailingSeparatorPolicy.REQUIRED );
		ParserExpression parser2R = new SeparatedList( identifier, ",", 2, -1, SeparatedList.TrailingSeparatorPolicy.REQUIRED );
		ParserExpression parser23R = new SeparatedList( identifier, ",", 2, 3, SeparatedList.TrailingSeparatorPolicy.REQUIRED );
		ParserExpression parserDR = new SeparatedList( identifier, ",", "[", "]", 0, -1, SeparatedList.TrailingSeparatorPolicy.REQUIRED );
	
		ParserExpression parserDOC = new SeparatedList( identifier, ",", "[", "]", 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).listCondition( condition );
		ParserExpression parserDOA = new SeparatedList( identifier, ",", "[", "]", 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).listAction( action );
		ParserExpression parserDOCA = new SeparatedList( identifier, ",", "[", "]", 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL ).listCondition( condition ).listAction( action );
	
		
		
		matchTestStringAndStreamSX( parser0N, "", "[]" );
		matchIncompleteTestStringAndStream( parser0N, "," );
		matchTestStringAndStreamSX( parser0N, "ab", "[ab]" );
		matchIncompleteTestStringAndStream( parser0N, "ab," );
		matchTestStringAndStreamSX( parser0N, "ab,cd", "[ab cd]" );
		matchIncompleteTestStringAndStream( parser0N, "ab,cd," );
		matchTestStringAndStreamSX( parser0N, "ab,cd,ef", "[ab cd ef]" );
		matchIncompleteTestStringAndStream( parser0N, "ab,cd,ef," );
		
		matchFailTestStringAndStream( parser1N, "" );
		matchFailTestStringAndStream( parser1N, "," );
		matchTestStringAndStreamSX( parser1N, "ab", "[ab]" );
		matchIncompleteTestStringAndStream( parser1N, "ab," );
		matchTestStringAndStreamSX( parser1N, "ab,cd", "[ab cd]" );
		matchIncompleteTestStringAndStream( parser1N, "ab,cd," );
		matchTestStringAndStreamSX( parser1N, "ab,cd,ef", "[ab cd ef]" );
		matchIncompleteTestStringAndStream( parser1N, "ab,cd,ef," );
		
		matchFailTestStringAndStream( parser2N, "" );
		matchFailTestStringAndStream( parser2N, "," );
		matchFailTestStringAndStream( parser2N, "ab" );
		matchFailTestStringAndStream( parser2N, "ab," );
		matchTestStringAndStreamSX( parser2N, "ab,cd", "[ab cd]" );
		matchIncompleteTestStringAndStream( parser2N, "ab,cd," );
		matchTestStringAndStreamSX( parser2N, "ab,cd,ef", "[ab cd ef]" );
		matchIncompleteTestStringAndStream( parser2N, "ab,cd,ef," );
	
		matchFailTestStringAndStream( parser23N, "" );
		matchFailTestStringAndStream( parser23N, "," );
		matchFailTestStringAndStream( parser23N, "ab" );
		matchFailTestStringAndStream( parser23N, "ab," );
		matchTestStringAndStreamSX( parser23N, "ab,cd", "[ab cd]" );
		matchIncompleteTestStringAndStream( parser23N, "ab,cd," );
		matchTestStringAndStreamSX( parser23N, "ab,cd,ef", "[ab cd ef]" );
		matchIncompleteTestStringAndStream( parser23N, "ab,cd,ef," );
		matchIncompleteTestStringAndStream( parser23N, "ab,cd,ef,gh" );
		matchIncompleteTestStringAndStream( parser23N, "ab,cd,ef,gh," );
	
		matchTestStringAndStreamSX( parserDN, "[]", "[]" );
		matchFailTestStringAndStream( parserDN, "[,]" );
		matchTestStringAndStreamSX( parserDN, "[ab]", "[ab]" );
		matchFailTestStringAndStream( parserDN, "[ab,]" );
		matchTestStringAndStreamSX( parserDN, "[ab,cd]", "[ab cd]" );
		matchFailTestStringAndStream( parserDN, "[ab,cd,]" );
		matchTestStringAndStreamSX( parserDN, "[ab,cd,ef]", "[ab cd ef]" );
		matchFailTestStringAndStream( parserDN, "[ab,cd,ef,]" );
		
	
		
		matchTestStringAndStreamSX( parser0O, "", "[]" );
		matchIncompleteTestStringAndStream( parser0O, "," );
		matchTestStringAndStreamSX( parser0O, "ab", "[ab]" );
		matchTestStringAndStreamSX( parser0O, "ab,", "[ab]" );
		matchTestStringAndStreamSX( parser0O, "ab,cd", "[ab cd]" );
		matchTestStringAndStreamSX( parser0O, "ab,cd,", "[ab cd]" );
		matchTestStringAndStreamSX( parser0O, "ab,cd,ef", "[ab cd ef]" );
		matchTestStringAndStreamSX( parser0O, "ab,cd,ef,", "[ab cd ef]" );
		
		matchFailTestStringAndStream( parser1O, "" );
		matchFailTestStringAndStream( parser1O, "," );
		matchTestStringAndStreamSX( parser1O, "ab", "[ab]" );
		matchTestStringAndStreamSX( parser1O, "ab,", "[ab]" );
		matchTestStringAndStreamSX( parser1O, "ab,cd", "[ab cd]" );
		matchTestStringAndStreamSX( parser1O, "ab,cd,", "[ab cd]" );
		matchTestStringAndStreamSX( parser1O, "ab,cd,ef", "[ab cd ef]" );
		matchTestStringAndStreamSX( parser1O, "ab,cd,ef,", "[ab cd ef]" );
		
		matchFailTestStringAndStream( parser2O, "" );
		matchFailTestStringAndStream( parser2O, "," );
		matchFailTestStringAndStream( parser2O, "ab" );
		matchFailTestStringAndStream( parser2O, "ab," );
		matchTestStringAndStreamSX( parser2O, "ab,cd", "[ab cd]" );
		matchTestStringAndStreamSX( parser2O, "ab,cd,", "[ab cd]" );
		matchTestStringAndStreamSX( parser2O, "ab,cd,ef", "[ab cd ef]" );
		matchTestStringAndStreamSX( parser2O, "ab,cd,ef,", "[ab cd ef]" );
	
		matchFailTestStringAndStream( parser23O, "" );
		matchFailTestStringAndStream( parser23O, "," );
		matchFailTestStringAndStream( parser23O, "ab" );
		matchFailTestStringAndStream( parser23O, "ab," );
		matchTestStringAndStreamSX( parser23O, "ab,cd", "[ab cd]" );
		matchTestStringAndStreamSX( parser23O, "ab,cd,", "[ab cd]" );
		matchTestStringAndStreamSX( parser23O, "ab,cd,ef", "[ab cd ef]" );
		matchTestStringAndStreamSX( parser23O, "ab,cd,ef,", "[ab cd ef]" );
		matchIncompleteTestStringAndStream( parser23O, "ab,cd,ef,gh" );
		matchIncompleteTestStringAndStream( parser23O, "ab,cd,ef,gh," );
	
		matchTestStringAndStreamSX( parserDO, "[]", "[]" );
		matchFailTestStringAndStream( parserDO, "[,]" );
		matchTestStringAndStreamSX( parserDO, "[ab]", "[ab]" );
		matchTestStringAndStreamSX( parserDO, "[ab,]", "[ab]" );
		matchTestStringAndStreamSX( parserDO, "[ab,cd]", "[ab cd]" );
		matchTestStringAndStreamSX( parserDO, "[ab,cd,]", "[ab cd]" );
		matchTestStringAndStreamSX( parserDO, "[ab,cd,ef]", "[ab cd ef]" );
		matchTestStringAndStreamSX( parserDO, "[ab,cd,ef,]", "[ab cd ef]" );
		
	
		
		matchTestStringAndStreamSX( parser0R, "", "[]" );
		matchIncompleteTestStringAndStream( parser0R, "," );
		matchIncompleteTestStringAndStream( parser0R, "ab" );
		matchTestStringAndStreamSX( parser0R, "ab,", "[ab]" );
		matchIncompleteTestStringAndStream( parser0R, "ab,cd" );
		matchTestStringAndStreamSX( parser0R, "ab,cd,", "[ab cd]" );
		matchIncompleteTestStringAndStream( parser0R, "ab,cd,ef" );
		matchTestStringAndStreamSX( parser0R, "ab,cd,ef,", "[ab cd ef]" );
		
		matchFailTestStringAndStream( parser1R, "" );
		matchFailTestStringAndStream( parser1R, "," );
		matchFailTestStringAndStream( parser1R, "ab" );
		matchTestStringAndStreamSX( parser1R, "ab,", "[ab]" );
		matchIncompleteTestStringAndStream( parser1R, "ab,cd" );
		matchTestStringAndStreamSX( parser1R, "ab,cd,", "[ab cd]" );
		matchIncompleteTestStringAndStream( parser1R, "ab,cd,ef" );
		matchTestStringAndStreamSX( parser1R, "ab,cd,ef,", "[ab cd ef]" );
		
		matchFailTestStringAndStream( parser2R, "" );
		matchFailTestStringAndStream( parser2R, "," );
		matchFailTestStringAndStream( parser2R, "ab" );
		matchFailTestStringAndStream( parser2R, "ab," );
		matchFailTestStringAndStream( parser2R, "ab,cd" );
		matchTestStringAndStreamSX( parser2R, "ab,cd,", "[ab cd]" );
		matchIncompleteTestStringAndStream( parser2R, "ab,cd,ef" );
		matchTestStringAndStreamSX( parser2R, "ab,cd,ef,", "[ab cd ef]" );
	
		matchFailTestStringAndStream( parser23R, "" );
		matchFailTestStringAndStream( parser23R, "," );
		matchFailTestStringAndStream( parser23R, "ab" );
		matchFailTestStringAndStream( parser23R, "ab," );
		matchFailTestStringAndStream( parser23R, "ab,cd" );
		matchTestStringAndStreamSX( parser23R, "ab,cd,", "[ab cd]" );
		matchIncompleteTestStringAndStream( parser23R, "ab,cd,ef" );
		matchTestStringAndStreamSX( parser23R, "ab,cd,ef,", "[ab cd ef]" );
		matchIncompleteTestStringAndStream( parser23O, "ab,cd,ef,gh" );
		matchIncompleteTestStringAndStream( parser23O, "ab,cd,ef,gh," );
		
		matchTestStringAndStreamSX( parserDR, "[]", "[]" );
		matchFailTestStringAndStream( parserDR, "[,]" );
		matchFailTestStringAndStream( parserDR, "[ab]" );
		matchTestStringAndStreamSX( parserDR, "[ab,]", "[ab]" );
		matchFailTestStringAndStream( parserDR, "[ab,cd]");
		matchTestStringAndStreamSX( parserDR, "[ab,cd,]", "[ab cd]" );
		matchFailTestStringAndStream( parserDR, "[ab,cd,ef]" );
		matchTestStringAndStreamSX( parserDR, "[ab,cd,ef,]", "[ab cd ef]" );
	
	
	
		matchTestStringAndStreamSX( parserDOC, "[]", "[]" );
		matchFailTestStringAndStream( parserDOC, "[,]" );
		matchFailTestStringAndStream( parserDOC, "[ab]" );
		matchFailTestStringAndStream( parserDOC, "[ab,]" );
		matchTestStringAndStreamSX( parserDOC, "[ab,cd]", "[ab cd]" );
		matchTestStringAndStreamSX( parserDOC, "[ab,cd,]", "[ab cd]" );
		matchFailTestStringAndStream( parserDOC, "[ab,cd,ef]" );
		matchFailTestStringAndStream( parserDOC, "[ab,cd,ef,]" );
		matchTestStringAndStreamSX( parserDOC, "[ab,cd,ef,gh]", "[ab cd ef gh]" );
		matchTestStringAndStreamSX( parserDOC, "[ab,cd,ef,gh,]", "[ab cd ef gh]" );
		
		matchTestStringAndStreamSX( parserDOA, "[]", "[]" );
		matchFailTestStringAndStream( parserDOA, "[,]" );
		matchTestStringAndStreamSX( parserDOA, "[ab]", "[ab]" );
		matchTestStringAndStreamSX( parserDOA, "[ab,]", "[ab sep]" );
		matchTestStringAndStreamSX( parserDOA, "[ab,cd]", "[ab cd]" );
		matchTestStringAndStreamSX( parserDOA, "[ab,cd,]", "[ab cd sep]" );
		matchTestStringAndStreamSX( parserDOA, "[ab,cd,ef]", "[ab cd ef]" );
		matchTestStringAndStreamSX( parserDOA, "[ab,cd,ef,]", "[ab cd ef sep]" );
		matchTestStringAndStreamSX( parserDOA, "[ab,cd,ef,gh]", "[ab cd ef gh]" );
		matchTestStringAndStreamSX( parserDOA, "[ab,cd,ef,gh,]", "[ab cd ef gh sep]" );
		
		matchTestStringAndStreamSX( parserDOCA, "[]", "[]" );
		matchFailTestStringAndStream( parserDOCA, "[,]" );
		matchFailTestStringAndStream( parserDOCA, "[ab]" );
		matchFailTestStringAndStream( parserDOCA, "[ab,]" );
		matchTestStringAndStreamSX( parserDOCA, "[ab,cd]", "[ab cd]" );
		matchTestStringAndStreamSX( parserDOCA, "[ab,cd,]", "[ab cd sep]" );
		matchFailTestStringAndStream( parserDOCA, "[ab,cd,ef]" );
		matchFailTestStringAndStream( parserDOCA, "[ab,cd,ef,]" );
		matchTestStringAndStreamSX( parserDOCA, "[ab,cd,ef,gh]", "[ab cd ef gh]" );
		matchTestStringAndStreamSX( parserDOCA, "[ab,cd,ef,gh,]", "[ab cd ef gh sep]" );
		
		
		ParserExpression parserList = new SeparatedList( identifier, "/", "<", ">", 0, -1, SeparatedList.TrailingSeparatorPolicy.OPTIONAL );
		matchTestListSX( parserList, "[\"<\" \">\"]", "[]"  );
		matchFailTestListSX( parserList, "[\"<\" / \">\"]" );
		matchTestListSX( parserList, "[\"<\" ab \">\"]", "[ab]" );
		matchTestListSX( parserList, "[\"<\" ab / \">\"]", "[ab]" );
		matchTestListSX( parserList, "[\"<\" ab / cd \">\" ]", "[ab cd]" );
		matchTestListSX( parserList, "[\"<\" ab / cd / \">\"]", "[ab cd]" );
		matchTestListSX( parserList, "[\"<\" ab / cd / ef \">\"]", "[ab cd ef]" );
		matchTestListSX( parserList, "[\"<\" ab / cd / ef / \">\"]", "[ab cd ef]" );
	}



	public void testSequence() throws ParserExpression.ParserCoerceException
	{
		Object[] abqwfh = { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) };
		Object[] abqw = { new Literal( "ab" ), new Literal( "qw" ) };
		Object[] qbqwfh = { new Literal( "qb" ), new Literal( "qw" ), new Literal( "fh" ) };
		
		assertTrue( new Sequence( abqwfh ).compareTo( new Sequence( abqwfh ) ) );
		assertFalse( new Sequence( abqwfh ).compareTo( new Sequence( abqw ) ) );
		assertFalse( new Sequence( abqwfh ).compareTo( new Sequence( qbqwfh ) ) );
		assertTrue( new Sequence( abqwfh ).compareTo( new Literal( "ab" ).__add__( new Literal( "qw" ) ).__add__( new Literal( "fh" ) ) ) );
		assertTrue( new Sequence( abqwfh ).compareTo( new Literal( "ab" ).__add__( "qw" ).__add__( "fh" ) ) );

		Object[] subs = { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) };
		ParserExpression parser = new Sequence( subs );
		
		String[] result = { "ab", "qw", "fh" };
		
		matchTestStringAndStream( parser, "abqwfh", Arrays.asList( result ) );
		matchFailTestStringAndStream( parser, "abfh" );

		
		ParserExpression parser2 = new Word( "a", "b" ).__add__( new Word( "a", "b" ) ).__add__( new Word( "a", "b" ) );
		matchTestListSX( parser2, "[ab abb abbb]", "[ab abb abbb]" );
		matchTestStreamSX( parser2, new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.TextItem( "ab" ), new StreamValueBuilder.StructuralItem( "ab" ), new StreamValueBuilder.TextItem( "ab" ) } ).stream(),
		"[ab ab ab]" );
	}



	public void testStringNode() throws ParserCoerceException
	{
		assertTrue( new StringNode( "abc" ).compareTo( new StringNode( new Literal( "abc" ) ) ) );
		assertFalse( new StringNode( "abc" ).compareTo( new StringNode( new Literal( "def" ) ) ) );
		
		matchTestNode( new StringNode( "hello" ), "hello", "hello" );
		matchFailTestNode( new StringNode( "hello" ), "hellothere", "hello" );

		matchFailTestString( new StringNode( "abcxyz" ), "abcxyz" );
		
		matchFailTestStream( new StringNode( "hello" ), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.TextItem( "hello" ) } ).stream(), "hello" );
		matchTestStream( new StringNode( "hello" ), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( "hello" ) } ).stream(), "hello" );
		matchFailTestStream( new StringNode( "hello" ), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( "hellothere" ) } ).stream() );

		matchTestListSX( new StringNode( "hello" ), "[hello]", "hello" );
		matchFailTestListSX( new StringNode( "hello" ), "[hellothere]" );
	}


	public void testStructuralObject() throws ParserCoerceException
	{
		ParserExpression parser1 = new StructuralObject( Color.class );
		matchTestNode( parser1, Color.RED, Color.RED );
		matchFailTestNode( parser1, parser1 );

		ParserExpression parser2 = new StructuralObject( Paint.class );
		matchTestNode( parser2, Color.RED, Color.RED );
		LinearGradientPaint linearPaint = new LinearGradientPaint( 0.0f, 0.0f, 1.0f, 1.0f, new float[] { 0.0f, 1.0f }, new Color[] { Color.RED, Color.GREEN } );
		matchTestNode( parser2, linearPaint, linearPaint );
		matchFailTestNode( parser2, parser1 );
	}


	public void testSuppress() throws ParserExpression.ParserCoerceException
	{
		assertTrue( new Suppress( new Literal( "ab" ) ).compareTo( new Suppress( new Literal( "ab" ) ) ) );
		assertFalse( new Suppress( new Literal( "ab" ) ).compareTo( new Suppress( new Literal( "cd" ) ) ) );
		assertTrue( new Suppress( new Literal( "ab" ) ).compareTo( new Literal( "ab" ).suppress() ) );

		Object[] subs = { new Literal( "ab" ), new Literal( "qw" ).suppress(), new Literal( "fh" ) };
		ParserExpression parser = new Sequence( subs );
		
		matchTestStringAndStreamSX( parser, "abqwfh", "[ab fh]" );
		matchFailTestStringAndStream( parser, "abfh" );

		matchTestListSX( parser, "[ab qw fh]", "[ab fh]" );
		matchFailTestListSX( parser, "[ab fh]" );
	}



	public void testWord()
	{
		assertTrue( new Word( "abc" ).compareTo( new Word( "abc" ) ) );
		assertFalse( new Word( "abc" ).compareTo( new Word( "def" ) ) );
		
		matchTestStringAndStream( new Word( "abc" ), "aabbcc", "aabbcc" );
		matchTestStringAndStream( new Word( "abc" ), "ccbbaa", "ccbbaa" );
		matchSubTestStringAndStream( new Word( "abc" ), "aabbccxx", "aabbcc", 6 );
		matchSubTestStringAndStream( new Word( "abc" ), "aabbccxxaa", "aabbcc", 6 );
		matchFailTestStringAndStream( new Word( "abc" ), "x" );
		
		assertTrue( new Word( "abc", "xyz" ).compareTo( new Word( "abc", "xyz" ) ) );
		assertFalse( new Word( "abc", "xyz" ).compareTo( new Word( "def", "xyz" ) ) );
		assertFalse( new Word( "abc", "xyz" ).compareTo( new Word( "abc", "pqr" ) ) );
		
		matchTestStringAndStream( new Word( "abc", "def" ), "addeeff", "addeeff" );
		matchTestStringAndStream( new Word( "abc", "def" ), "affeedd", "affeedd" );
		matchSubTestStringAndStream( new Word( "abc", "def" ), "affeeddxx", "affeedd", 7 );
		matchSubTestStringAndStream( new Word( "abc", "def" ), "affeeddxxa", "affeedd", 7 );
		matchSubTestStringAndStream( new Word( "abc", "def" ), "affeeddxxf", "affeedd", 7 );
		matchFailTestStringAndStream( new Word( "abc", "def" ), "ddeeff" );
		matchFailTestStringAndStream( new Word( "abc", "def" ), "x" );
		matchFailTestStringAndStream( new Word( "abc", "def" ), "dadeeff" );
	
		

		matchTestStream( new Word( "abc", "def" ), new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.StructuralItem( "addeeff" ) } ).stream(), "addeeff" );

		matchTestNode( new Word( "abc", "def" ), "addeeff", "addeeff" );

		matchTestListSX( new Word( "abc", "def" ), "[addeeff]", "addeeff" );
	}
	
	
	public void testZeroOrMore() throws ParserExpression.ParserCoerceException
	{
		assertTrue( new ZeroOrMore( new Literal( "ab" ), false ).compareTo( new ZeroOrMore( new Literal( "ab" ), false ) ) );
		assertFalse( new ZeroOrMore( new Literal( "ab" ), false ).compareTo( new ZeroOrMore( new Literal( "cd" ), false ) ) );
		assertFalse( new ZeroOrMore( new Literal( "ab" ), false ).compareTo( new ZeroOrMore( new Literal( "ab" ), true ) ) );
		assertTrue( new ZeroOrMore( new Literal( "ab" ), false ).compareTo( new ZeroOrMore( new Literal( "ab" ) ) ) );
		assertTrue( new ZeroOrMore( new Literal( "ab" ), false ).compareTo( new ZeroOrMore( "ab" ) ) );

		ParserExpression parserO, parserN;
		parserO = new ZeroOrMore( new Word( "a", "b" ).__add__( new Word( "c", "d" ) ) );
		parserN = new ZeroOrMore( new Word( "a", "b" ).__add__( new Word( "c", "d" ) ), true );
		
		String[][] result0 = {};
		String[][] result1 = { { "ab", "cd", } };
		String[][] result2 = { { "ab", "cd", },   { "abb", "cdd" } };
		
		matchTestStringAndStream( parserO, "", arrayToList2D( result0 ) );
		matchTestStringAndStream( parserN, "", null );
		
		matchTestStringAndStream( parserO, "abcd", arrayToList2D( result1 ) );
		matchTestStringAndStream( parserN, "abcd", arrayToList2D( result1 ) );
		
		matchTestStringAndStream( parserO, "abcdabbcdd", arrayToList2D( result2 ) );
		matchTestStringAndStream( parserN, "abcdabbcdd", arrayToList2D( result2 ) );
		
		ParserExpression parser2 = new ZeroOrMore( new Word( "a", "b" ) );
		matchTestListSX( parser2, "[ab abb abbb]", "[ab abb abbb]" );
		matchTestListSX( parser2, "[]", "[]" );
		matchTestStreamSX( parser2, new StreamValueBuilder( new StreamValueBuilder.Item[] { new StreamValueBuilder.TextItem( "ab" ), new StreamValueBuilder.StructuralItem( "ab" ) } ).stream(),
		"[ab ab]" );
		matchTestStreamSX( parser2, new StreamValueBuilder().stream(), "[]" );
	}



	public void testNonRecursiveCalculator()
	{
		ParserExpression integer = new Word( "0123456789" );
		ParserExpression plus = new Literal( "+" );
		ParserExpression minus = new Literal( "-" );
		ParserExpression star = new Literal( "*" );
		ParserExpression slash = new Literal( "/" );
	
		ParserExpression addop = plus.__or__(  minus );
		ParserExpression mulop = star.__or__(  slash );
		
		ParseAction flattenAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
			{
				ArrayList<Object> y = new ArrayList<Object>();
				List<Object> xx = (List<Object>)x;
				for (Object a: xx)
				{
					y.addAll( (List<Object>)a );
				}
				return y;
			}
		};

		ParseAction action = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
			{
				List<Object> xx = (List<Object>)x;
				if ( xx.get( 1 ).equals( new ArrayList<Object>() ) )
				{
					return xx.get( 0 );
				}
				else
				{
					ArrayList<Object> res = new ArrayList<Object>();
					res.add( xx.get( 0 ) );
					res.addAll( (List<Object>)xx.get( 1 ) );
					return res;
				}
			}
		};
		
		Production mul = new Production( "mul", ( integer.__add__( new ZeroOrMore( mulop.__add__( integer ) ).action( flattenAction ) ) ).action( action ) );
		Production add = new Production( "add", ( mul.__add__( new ZeroOrMore( addop.__add__( mul ) ).action( flattenAction ) ) ).action( action ) );
		ParserExpression parser = add;
		
		matchTestStringAndStream( parser, "123", "123" );
		
		matchTestStringAndStreamSX( parser, "1*2", "[1 * 2]" );
		matchTestStringAndStreamSX( parser, "1*2*3", "[1 * 2 * 3]" );

		matchTestStringAndStreamSX( parser, "1+2", "[1 + 2]" );
		matchTestStringAndStreamSX( parser, "1+2+3", "[1 + 2 + 3]" );

		matchTestStringAndStreamSX( parser, "1+2*3", "[1 + [2 * 3]]" );
		matchTestStringAndStreamSX( parser, "1*2+3", "[[1 * 2] + 3]" );

		matchTestStringAndStreamSX( parser, "1*2+3+4", "[[1 * 2] + 3 + 4]" );
		matchTestStringAndStreamSX( parser, "1+2*3+4", "[1 + [2 * 3] + 4]" );
		matchTestStringAndStreamSX( parser, "1+2+3*4", "[1 + 2 + [3 * 4]]" );

		matchTestStringAndStreamSX( parser, "1+2*3*4", "[1 + [2 * 3 * 4]]" );
		matchTestStringAndStreamSX( parser, "1*2+3*4", "[[1 * 2] + [3 * 4]]" );
		matchTestStringAndStreamSX( parser, "1*2*3+4", "[[1 * 2 * 3] + 4]" );
	}


	public void testRightRecursion() throws ParserExpression.ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		Production x = new Production( "_x", "x" );
		Production y = new Production( "_y" );
		y.setExpression( x.__add__( y ).__or__( "y" ) );
		
		matchTestStringAndStreamSX( y, "xxxy", "[x [x [x y]]]" );
	}


	public void testDirectLeftRecursion() throws ParserExpression.ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		Production x = new Production( "_x", "x" );
		Production y = new Production( "_y" );
		y.setExpression( y.__add__( x ).__or__( "y" ) );
		
		matchTestStringAndStreamSX( y, "yxxx", "[[[y x] x] x]" );
	}

	public void testIndirectLeftRecursion() throws ParserExpression.ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		Production x = new Production( "_x", "x" );
		Production z = new Production( "_z" );
		Production y = new Production( "_y", z.__add__( x ).__or__( "z" ) );
		z.setExpression( y.__or__( "y" ) );
		
		matchTestStringAndStreamSX( z, "zxxx", "[[[z x] x] x]" );
		matchTestStringAndStreamSX( z, "yxxx", "[[[y x] x] x]" );
	}

	public void testLeftRecursion() throws Production.CannotOverwriteProductionExpressionException
	{
		DMObjectClass Num = s.newClass( "Num", new String[] { "x" } );
		
		
		ParserExpression integer = new Word( "0123456789" );
		ParserExpression plus = new Literal( "+" );
		ParserExpression minus = new Literal( "-" );
		ParserExpression star = new Literal( "*" );
		ParserExpression slash = new Literal( "/" );
	
		ParserExpression addop = plus.__or__(  minus );
		ParserExpression mulop = star.__or__(  slash );

		Production number = new Production( "number" );
		number.setExpression( integer.__or__( new ObjectNode( Num ) ) );
		
		Production mul = new Production( "mul" );
		mul.setExpression( ( mul.__add__( mulop ).__add__( number ) ).__or__( number ) );
		Production add = new Production( "add" );
		add.setExpression( ( add.__add__( addop ).__add__( mul ) ).__or__( mul ) );
		ParserExpression parser = add;
		
		matchTestStringAndStream( parser, "123", "123" );
		
		matchTestStringAndStreamSX( parser, "1*2", "[1 * 2]" );
		matchTestStringAndStreamSX( parser, "1*2*3", "[[1 * 2] * 3]" );

		matchTestStringAndStreamSX( parser, "1+2", "[1 + 2]" );
		matchTestStringAndStreamSX( parser, "1+2+3", "[[1 + 2] + 3]" );

		matchTestStringAndStreamSX( parser, "1+2*3", "[1 + [2 * 3]]" );
		matchTestStringAndStreamSX( parser, "1*2+3", "[[1 * 2] + 3]" );

		matchTestStringAndStreamSX( parser, "1*2+3+4", "[[[1 * 2] + 3] + 4]" );
		matchTestStringAndStreamSX( parser, "1+2*3+4", "[[1 + [2 * 3]] + 4]" );
		matchTestStringAndStreamSX( parser, "1+2+3*4", "[[1 + 2] + [3 * 4]]" );

		matchTestStringAndStreamSX( parser, "1+2*3*4", "[1 + [[2 * 3] * 4]]" );
		matchTestStringAndStreamSX( parser, "1*2+3*4", "[[1 * 2] + [3 * 4]]" );
		matchTestStringAndStreamSX( parser, "1*2*3+4", "[[[1 * 2] * 3] + 4]" );
		

		StreamValueBuilder builder1 = new StreamValueBuilder();
		builder1.appendTextValue( "1+" );
		builder1.appendStructuralValue( Num.newInstance( new Object[] { "2" } ) );
		builder1.appendTextValue( "*3*4" );

		StreamValueBuilder builder2 = new StreamValueBuilder();
		builder2.appendTextValue( "1*" );
		builder2.appendStructuralValue( Num.newInstance( new Object[] { "2" } ) );
		builder2.appendTextValue( "+3*4" );

		StreamValueBuilder builder3 = new StreamValueBuilder();
		builder3.appendTextValue( "1*" );
		builder3.appendStructuralValue( Num.newInstance( new Object[] { "2" } ) );
		builder3.appendTextValue( "*3+4" );

		matchTestStreamSX( parser, builder1.stream(), "{m=tests.Parser.Test_Parser.s : [1 + [[(m Num x=2) * 3] * 4]]}" );
		matchTestStreamSX( parser, builder2.stream(), "{m=tests.Parser.Test_Parser.s : [[1 * (m Num x=2)] + [3 * 4]]}" );
		matchTestStreamSX( parser, builder3.stream(), "{m=tests.Parser.Test_Parser.s : [[[1 * (m Num x=2)] * 3] + 4]}" );
	}



	public void testLeftRecursionSimplifiedJavaPrimary() throws ParserExpression.ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		ParseAction arrayAccessAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( new Object[] { "arrayAccess", v.get( 0 ), v.get( 2 ) } );
			}
		};
		
		ParseAction fieldAccessAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( new Object[] { "fieldAccess", v.get( 0 ), v.get( 2 ) } );
			}
		};
		
		ParseAction objectMethodInvocationAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( new Object[] { "methodInvoke", v.get( 0 ), v.get( 2 ) } );
			}
		};
		
		ParseAction thisMethodInvocationAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( new Object[] { "methodInvoke", v.get( 0 ) } );
			}
		};
		
		Production primary = new Production( "primary" );
		
		ParserExpression expression = new Production( "expression", new Literal( "i" ).__or__( new Literal( "j" ) ) );
		ParserExpression methodName = new Production( "methodName", new Literal( "m" ).__or__( new Literal( "n" ) ) );
		ParserExpression interfaceTypeName = new Production( "interfaceTypeName", new Literal( "I" ).__or__( new Literal( "J" ) ) );
		ParserExpression className = new Production( "className", new Literal( "C" ).__or__( new Literal( "D" ) ) );

		ParserExpression classOrInterfaceType = new Production( "classOrInterfaceType", className.__or__( interfaceTypeName ) );

		ParserExpression identifier = new Production( "identifier", new Literal( "x" ).__or__( new Literal( "y" ).__or__( classOrInterfaceType ) ) );
		ParserExpression expressionName = new Production( "expressionName", identifier );

		ParserExpression arrayAccess = new Production( "arrayAccess", ( primary.__add__( "[" ).__add__( expression ).__add__( "]" ) ).action( arrayAccessAction ).__or__(
											( expressionName.__add__( "[" ).__add__( expression ).__add__( "]" ) ).action( arrayAccessAction ) ) );
		ParserExpression fieldAccess = new Production( "fieldAccess", ( primary.__add__( "." ).__add__( identifier ) ).action( fieldAccessAction ).__or__(
				( new Literal( "super" ).__add__( "." ).__add__( identifier ) ).action( fieldAccessAction ) ) );
		ParserExpression methodInvocation = new Production( "methodInvocation", ( primary.__add__( "." ).__add__( methodName ).__add__( "()" ) ).action( objectMethodInvocationAction ).__or__(
				( methodName.__add__( "()" ) ).action( thisMethodInvocationAction ) ) );
		
		ParserExpression classInstanceCreationExpression = new Production( "classInstanceCreationExpression", ( new Literal( "new" ).__add__( classOrInterfaceType ).__add__( "()" ) ).__or__(
				primary.__add__( "." ).__add__( "new" ).__add__( identifier ).__add__( "()" ) ) );
		
		ParserExpression fieldAccessOrArrayAccess = new Production( "fieldAccessOrArrayAccess", fieldAccess.__or__( arrayAccess ) );
		ParserExpression primaryNoNewArray = new Production( "primaryNoNewArray", classInstanceCreationExpression.__or__( methodInvocation ).__or__( fieldAccessOrArrayAccess ).__or__( "this" ) );
		primary.setExpression( primaryNoNewArray );
		
	
		matchTestStringAndStreamSX( primary, "this", "this" );
		matchTestStringAndStreamSX( primary, "this.x", "[fieldAccess this x]" );
		matchTestStringAndStreamSX( primary, "this.x[i]", "[arrayAccess [fieldAccess this x] i]" );
		matchTestStringAndStreamSX( primary, "this.x.y", "[fieldAccess [fieldAccess this x] y]" );
		matchTestStringAndStreamSX( primary, "this.x.m()", "[methodInvoke [fieldAccess this x] m]" );
		matchTestStringAndStreamSX( primary, "this.x.m().n()", "[methodInvoke [methodInvoke [fieldAccess this x] m] n]" );
		matchTestStringAndStreamSX( primary, "x[i][j].y", "[fieldAccess [arrayAccess [arrayAccess x i] j] y]" );

		matchTestStringAndStreamSX( methodInvocation, "this.m()", "[methodInvoke this m]" );
		matchTestStringAndStreamSX( methodInvocation, "this.m().n()", "[methodInvoke [methodInvoke this m] n]" );
		matchTestStringAndStreamSX( methodInvocation, "this.x.m()", "[methodInvoke [fieldAccess this x] m]" );
		matchTestStringAndStreamSX( methodInvocation, "this.x.y.m()", "[methodInvoke [fieldAccess [fieldAccess this x] y] m]" );
		matchTestStringAndStreamSX( methodInvocation, "this[i].m()", "[methodInvoke [arrayAccess this i] m]" );
		matchTestStringAndStreamSX( methodInvocation, "this[i][j].m()", "[methodInvoke [arrayAccess [arrayAccess this i] j] m]" );
		matchTestStringAndStreamSX( arrayAccess, "this[i]", "[arrayAccess this i]" );
		matchTestStringAndStreamSX( arrayAccess, "this[i][j]", "[arrayAccess [arrayAccess this i] j]" );
		matchTestStringAndStreamSX( arrayAccess, "this.x[i]", "[arrayAccess [fieldAccess this x] i]" );
		matchTestStringAndStreamSX( arrayAccess, "this.x.y[i]", "[arrayAccess [fieldAccess [fieldAccess this x] y] i]" );
		matchTestStringAndStreamSX( arrayAccess, "this.m()[i]", "[arrayAccess [methodInvoke this m] i]" );
		matchTestStringAndStreamSX( arrayAccess, "this.m().n()[i]", "[arrayAccess [methodInvoke [methodInvoke this m] n] i]" );
		matchTestStringAndStreamSX( fieldAccess, "this.x", "[fieldAccess this x]" );
		matchTestStringAndStreamSX( fieldAccess, "this.x.y", "[fieldAccess [fieldAccess this x] y]" );
		matchTestStringAndStreamSX( fieldAccess, "this[i].x", "[fieldAccess [arrayAccess this i] x]" );
		matchTestStringAndStreamSX( fieldAccess, "this[i][j].x", "[fieldAccess [arrayAccess [arrayAccess this i] j] x]" );
		matchTestStringAndStreamSX( fieldAccessOrArrayAccess, "this[i]", "[arrayAccess this i]" );
		matchTestStringAndStreamSX( fieldAccessOrArrayAccess, "this[i].x", "[fieldAccess [arrayAccess this i] x]" );
		matchTestStringAndStreamSX( fieldAccessOrArrayAccess, "this.x[i]", "[arrayAccess [fieldAccess this x] i]" );
	}
	
	
	public static TracedParseResult getJavaPrimaryTestDebugParseResult() throws ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		ParseAction arrayAccessAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( new Object[] { "arrayAccess", v.get( 0 ), v.get( 2 ) } );
			}
		};
		
		ParseAction fieldAccessAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( new Object[] { "fieldAccess", v.get( 0 ), v.get( 2 ) } );
			}
		};
		
		ParseAction objectMethodInvocationAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( new Object[] { "methodInvoke", v.get( 0 ), v.get( 2 ) } );
			}
		};
		
		ParseAction thisMethodInvocationAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( new Object[] { "methodInvoke", v.get( 0 ) } );
			}
		};
		
		Production primary = new Production( "primary" );
		
		ParserExpression expression = new Production( "expression", new Literal( "i" ).__or__( new Literal( "j" ) ) );
		ParserExpression methodName = new Production( "methodName", new Literal( "m" ).__or__( new Literal( "n" ) ) );
		ParserExpression interfaceTypeName = new Production( "interfaceTypeName", new Literal( "I" ).__or__( new Literal( "J" ) ) );
		ParserExpression className = new Production( "className", new Literal( "C" ).__or__( new Literal( "D" ) ) );

		ParserExpression classOrInterfaceType = new Production( "classOrInterfaceType", className.__or__( interfaceTypeName ) );

		ParserExpression identifier = new Production( "identifier", new Literal( "x" ).__or__( new Literal( "y" ).__or__( classOrInterfaceType ) ) );
		ParserExpression expressionName = new Production( "expressionName", identifier );

		ParserExpression arrayAccess = new Production( "arrayAccess", ( primary.__add__( "[" ).__add__( expression ).__add__( "]" ) ).action( arrayAccessAction ).__or__(
											( expressionName.__add__( "[" ).__add__( expression ).__add__( "]" ) ).action( arrayAccessAction ) ) );
		ParserExpression fieldAccess = new Production( "fieldAccess", ( primary.__add__( "." ).__add__( identifier ) ).action( fieldAccessAction ).__or__(
				( new Literal( "super" ).__add__( "." ).__add__( identifier ) ).action( fieldAccessAction ) ) );
		ParserExpression methodInvocation = new Production( "methodInvocation", ( primary.__add__( "." ).__add__( methodName ).__add__( "()" ) ).action( objectMethodInvocationAction ).__or__(
				( methodName.__add__( "()" ) ).action( thisMethodInvocationAction ) ) );
		
		ParserExpression classInstanceCreationExpression = new Production( "classInstanceCreationExpression", ( new Literal( "new" ).__add__( classOrInterfaceType ).__add__( "()" ) ).__or__(
				primary.__add__( "." ).__add__( "new" ).__add__( identifier ).__add__( "()" ) ) );
		
//		ParserExpression primaryNoNewArray = new Production( classInstanceCreationExpression.__or__( methodInvocation ).__or__( fieldAccess ).__or__( arrayAccess ).__or__( "this" ) ).debug( "primaryNoNewArray" );

//		primary.setExpression( new Production( primaryNoNewArray ).debug( "primary" ) );
		
//		ParserExpression fieldAccessOrArrayAccess = new Production( fieldAccess.__xor__( arrayAccess ) ).debug( "fieldAccessOrArrayAccess" );
	
		ParserExpression fieldAccessOrArrayAccess = new Production( "fieldAccessOrArrayAccess", fieldAccess.__or__( arrayAccess ) );
		ParserExpression primaryNoNewArray = new Production( "primaryNoNewArray", classInstanceCreationExpression.__or__( methodInvocation ).__or__( fieldAccessOrArrayAccess ).__or__( "this" ) );
		primary.setExpression( primaryNoNewArray );

		
		return fieldAccessOrArrayAccess.traceParseStringChars( "this[i]" );
	}





	static Object deepArrayToList(Object[] xs)
	{
		ArrayList<Object> l = new ArrayList<Object>();
		
		for (Object x: xs)
		{
			if ( x.getClass().isArray() )
			{
				l.add( deepArrayToList( (Object[])x ) );
			}
			else
			{
				l.add( x );
			}
		}
		
		return l;
	}

	
	public void testLerpRefactor() throws ParserCoerceException
	{
		ParseCondition lerpCondition = new ParseCondition()
		{
			public boolean test(Object input, int begin, int end, Object x, Map<String, Object> bindings)
			{
				Object t1 = bindings.get( "t1" );
				Object t2 = bindings.get( "t2" );
				return t1 != null  &&  t2 != null  &&  t1.equals( t2 );
			}
		};
		
		ParseAction lerpAction = new ParseAction()
		{
			public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
			{
				return deepArrayToList( new Object[] { "+", bindings.get( "a" ), new Object[] { "*", new Object[] { "-", bindings.get( "b" ), bindings.get( "a" ) }, bindings.get( "t1" ) } } );
			}
		};
		
		ParserExpression oneMinusT = new ListNode( new Object[] { "-", "1.0", new AnyNode().bindTo( "t1" ) } );
		bindingsTestNodeSX( oneMinusT, "[- 1.0 x]", "[[t1 x]]" );

		ParserExpression bTimesT = new ListNode( new Object[] { "*", new AnyNode().bindTo( "b" ), new AnyNode().bindTo( "t2" ) } );
		bindingsTestNodeSX( bTimesT, "[* q x]", "[[b q] [t2 x]]" );

		ParserExpression aTimesOneMinusT = new ListNode( new Object[] { "*", new AnyNode().bindTo( "a" ), oneMinusT } );
		bindingsTestNodeSX( aTimesOneMinusT, "[* p [- 1.0 x]]", "[[a p] [t1 x]]" );
		
		ParserExpression lerp = new ListNode( new Object[] { "+", aTimesOneMinusT, bTimesT } ).condition( lerpCondition );
		bindingsTestNodeSX( lerp, "[+ [* p [- 1.0 x]] [* q x]]", "[[a p] [b q] [t1 x] [t2 x]]" );

		ParserExpression lerpRefactor = lerp.action( lerpAction );
		matchTestNodeSX( lerpRefactor, "[+ [* p [- 1.0 x]] [* q x]]", "[+ p [* [- q p] x]]" );
	}

	
	
	public void testLerpRefactorObject() throws ParserCoerceException
	{
		ParseCondition lerpCondition = new ParseCondition()
		{
			public boolean test(Object input, int begin, int end, Object x, Map<String, Object> bindings)
			{
				Object t1 = bindings.get( "t1" );
				Object t2 = bindings.get( "t2" );
				return t1 != null  &&  t2 != null  &&  t1.equals( t2 );
			}
		};
		
		ParseAction lerpAction = new ParseAction()
		{
			public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
			{
				return Add.newInstance( new Object[] { bindings.get( "a" ), Mul.newInstance( new Object[] {  Sub.newInstance( new Object[] { bindings.get( "b" ), bindings.get( "a" ) } ), bindings.get( "t1" ) } ) } );
			}
		};
		
		ParserExpression oneMinusT = new ObjectNode( Sub, new Object[] { "1.0", new AnyNode().bindTo( "t1" ) } );
		bindingsTestNodeSX( oneMinusT, "{m=tests.Parser.Test_Parser.s : (m Sub a=1.0 b=x)}", "[[t1 x]]" );

		ParserExpression bTimesT = new ObjectNode( Mul, new Object[] { new AnyNode().bindTo( "b" ), new AnyNode().bindTo( "t2" ) } );
		bindingsTestNodeSX( bTimesT, "{m=tests.Parser.Test_Parser.s : (m Mul a=q b=x)}", "[[b q] [t2 x]]" );

		ParserExpression aTimesOneMinusT = new ObjectNode( Mul, new Object[] { new AnyNode().bindTo( "a" ), oneMinusT } );
		bindingsTestNodeSX( aTimesOneMinusT, "{m=tests.Parser.Test_Parser.s : (m Mul a=p b=(m Sub a=1.0 b=x))}", "[[a p] [t1 x]]" );
		
		ParserExpression lerp = new ObjectNode( Add, new Object[] { aTimesOneMinusT, bTimesT } ).condition( lerpCondition );
		bindingsTestNodeSX( lerp, "{m=tests.Parser.Test_Parser.s : (m Add a=(m Mul a=p b=(m Sub a=1.0 b=x)) b=(m Mul a=q b=x))}", "[[a p] [b q] [t1 x] [t2 x]]" );

		ParserExpression lerpRefactor = lerp.action( lerpAction );
		matchTestNodeSX( lerpRefactor, "{m=tests.Parser.Test_Parser.s : (m Add a=(m Mul a=p b=(m Sub a=1.0 b=x)) b=(m Mul a=q b=x))}", "{m=tests.Parser.Test_Parser.s : (m Add a=p b=(m Mul a=(m Sub a=q b=p) b=x))}" );
	}

	
	
	private static class MethodCallRefactorHelper
	{
		static ParserExpression getAttr(ParserExpression target, ParserExpression name) throws ParserCoerceException
		{
			return new ListNode( new Object[] { "getAttr", target, name } );
		}
		
		static ParserExpression call(ParserExpression target, ParserExpression params) throws ParserCoerceException
		{
			return new ListNode( new Object[] { "call", target, params } );
		}

		static ParserExpression methodCall(ParserExpression target, ParserExpression name, ParserExpression params) throws ParserCoerceException
		{
			return call( getAttr( target, name ), params );
		}
	}


	public void testMethodCallRefactor() throws ParserCoerceException, CannotOverwriteProductionExpressionException
	{
		ParseAction methodCallRefactorAction = new ParseAction()
		{
			public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
			{
				return deepArrayToList( new Object[] { "invokeMethod", bindings.get( "target" ), bindings.get( "name" ), bindings.get( "params" ) } );
			}
		};
		
		ParserExpression load = new ListNode( new Object[] { "load", new AnyNode() } );
		ParserExpression params = new ListNode( new Object[] { "params" } );
		
		Production expression = new Production( "expression" );
		ParserExpression methodCall = new Production( "methodCall", MethodCallRefactorHelper.methodCall( expression.bindTo( "target" ), identifier.bindTo( "name" ), params.bindTo( "params" ) ).action( methodCallRefactorAction ) );
		ParserExpression call = new Production( "call", MethodCallRefactorHelper.call( expression, params ) );
		ParserExpression getAttr = new Production( "getAttr", MethodCallRefactorHelper.getAttr( expression, identifier ) );
		expression.setExpression( new Choice( new Object[] { methodCall, call, getAttr, load } ) );
		
		matchTestNodeSX( expression, "[load x]", "[load x]" );
		matchTestNodeSX( expression, "[call [load x] [params]]", "[call [load x] [params]]" );
		matchTestNodeSX( expression, "[getAttr [load x] blah]", "[getAttr [load x] blah]" );
		matchTestNodeSX( expression, "[call [getAttr [load x] blah] [params]]", "[invokeMethod [load x] blah [params]]" );
		matchTestNodeSX( expression, "[call [getAttr [getAttr [load y] foo] blah] [params]]", "[invokeMethod [getAttr [load y] foo] blah [params]]" );
		matchTestNodeSX( expression, "[call [getAttr [call [load y] [params]] blah] [params]]", "[invokeMethod [call [load y] [params]] blah [params]]" );
		matchTestNodeSX( expression, "[call [getAttr [call [getAttr [load x] foo] [params]] blah] [params]]", "[invokeMethod [invokeMethod [load x] foo [params]] blah [params]]" );
		matchTestNodeSX( expression, "[call [getAttr [call [call [getAttr [load x] blah] [params]] [params]] blah] [params]]", "[invokeMethod [call [invokeMethod [load x] blah [params]] [params]] blah [params]]" );
		matchTestNodeSX( expression, "[call [getAttr [getAttr [call [getAttr [load x] blah] [params]] foo] blah] [params]]", "[invokeMethod [getAttr [invokeMethod [load x] blah [params]] foo] blah [params]]" );
	}
}

