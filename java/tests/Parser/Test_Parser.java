//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Parser;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import BritefuryJ.Parser.Action;
import BritefuryJ.Parser.BestChoice;
import BritefuryJ.Parser.Bind;
import BritefuryJ.Parser.Choice;
import BritefuryJ.Parser.Combine;
import BritefuryJ.Parser.Condition;
import BritefuryJ.Parser.Keyword;
import BritefuryJ.Parser.Literal;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParseCondition;
import BritefuryJ.Parser.ParseResult;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.RegEx;
import BritefuryJ.Parser.Sequence;
import BritefuryJ.Parser.Suppress;
import BritefuryJ.Parser.Word;
import BritefuryJ.Parser.ParserExpression.ParserCoerceException;

public class Test_Parser extends ParserTestCase
{
	public void bindingsTest(ParserExpression parser, String input, String[][] expectedBindings)
	{
		bindingsTest( parser, input, expectedBindings, "[ \t\n]*" );
	}

	public void bindingsTest(ParserExpression parser, String input, String[][] expectedBindings, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseString( input );
		assertTrue( result.isValid() );
		
		HashMap<String, String> expected = new HashMap<String, String>();
		for (int i = 0; i < expectedBindings.length; i++)
		{
			expected.put( expectedBindings[i][0], expectedBindings[i][1] );
		}
		
		if ( !expected.equals( result.bindings ) )
		{
			System.out.println( "EXPECTED BINDINGS:" );
			System.out.println( expected.toString() );
			System.out.println( "RESULT BINDINGS:" );
			System.out.println( result.bindings.toString() );
		}
		
		assertTrue( expected.equals( result.bindings ) );
	}

	
	
	
	public void testLiteral()
	{
		assertTrue( new Literal( "abc" ).compareTo( new Literal( "abc" ) ) );
		assertFalse( new Literal( "abc" ).compareTo( new Literal( "def" ) ) );
		matchTest( new Literal( "abcxyz" ), "abcxyz", "abcxyz" );
		matchFailTest( new Literal( "abcxyz" ), "qwerty" );
		matchSubTest( new Literal( "abcxyz" ), "abcxyz123", "abcxyz", 6 );
	}


	public void testKeyword()
	{
		assertTrue( new Keyword( "abc" ).compareTo( new Keyword( "abc" ) ) );
		assertFalse( new Keyword( "abc" ).compareTo( new Keyword( "def" ) ) );
		assertTrue( new Keyword( "abc", "xyz" ).compareTo( new Keyword( "abc", "xyz" ) ) );
		assertFalse( new Keyword( "abc", "xyz" ).compareTo( new Keyword( "def", "xyz" ) ) );
		assertFalse( new Keyword( "abc", "xyz" ).compareTo( new Keyword( "abc", "pqr" ) ) );
		matchTest( new Keyword( "hello" ), "hello", "hello" );
		matchFailTest( new Keyword( "hello" ), "helloq" );
		matchSubTest( new Keyword( "hello", "abc" ), "hello", "hello", 5 );
		matchSubTest( new Keyword( "hello", "abc" ), "helloxx", "hello", 5 );
		matchFailTest( new Keyword( "hello", "abc" ), "helloaa" );
	}
	
	
	public void testRegEx()
	{
		assertTrue( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ).compareTo( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ) ) );
		assertFalse( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ).compareTo( new RegEx( "[A-Za-z_][A-Za-z0-9_]*abc" ) ) );
		matchTest( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ), "abc_123", "abc_123" );
		matchFailTest( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ), "9abc" );
		matchSubTest( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ), "abc_xyz...", "abc_xyz", 7 );
		matchTest( new RegEx( "[A-Za-z_]*" ), "abc_", "abc_" );
		matchFailTest( new RegEx( "[A-Za-z_]*" ), "." );
	}
	
	
	public void testWord()
	{
		assertTrue( new Word( "abc" ).compareTo( new Word( "abc" ) ) );
		assertFalse( new Word( "abc" ).compareTo( new Word( "def" ) ) );
		matchTest( new Word( "abc" ), "aabbcc", "aabbcc" );
		matchTest( new Word( "abc" ), "ccbbaa", "ccbbaa" );
		matchSubTest( new Word( "abc" ), "aabbccxx", "aabbcc", 6 );
		matchSubTest( new Word( "abc" ), "aabbccxxaa", "aabbcc", 6 );
		matchFailTest( new Word( "abc" ), "x" );
		
		assertTrue( new Word( "abc", "xyz" ).compareTo( new Word( "abc", "xyz" ) ) );
		assertFalse( new Word( "abc", "xyz" ).compareTo( new Word( "def", "xyz" ) ) );
		assertFalse( new Word( "abc", "xyz" ).compareTo( new Word( "abc", "pqr" ) ) );
		matchTest( new Word( "abc", "def" ), "addeeff", "addeeff" );
		matchTest( new Word( "abc", "def" ), "affeedd", "affeedd" );
		matchSubTest( new Word( "abc", "def" ), "affeeddxx", "affeedd", 7 );
		matchSubTest( new Word( "abc", "def" ), "affeeddxxa", "affeedd", 7 );
		matchSubTest( new Word( "abc", "def" ), "affeeddxxf", "affeedd", 7 );
		matchFailTest( new Word( "abc", "def" ), "ddeeff" );
		matchFailTest( new Word( "abc", "def" ), "x" );
		matchFailTest( new Word( "abc", "def" ), "dadeeff" );
	}
	
	
	public void testBind()
	{
		assertTrue( new Bind( "abc", "x" ).compareTo( new Bind( "abc", "x" ) ) );
		assertFalse( new Bind( "abc", "x" ).compareTo( new Bind( "def", "x" ) ) );
		assertFalse( new Bind( "abc", "x" ).compareTo( new Bind( "abc", "y" ) ) );
		assertTrue( new Bind( "abc", "x" ).compareTo( new Literal( "abc" ).bindTo( "x" ) ) );
		
		ParserExpression parser = new Literal( "abc" ).bindTo( "x" );
		
		matchTest( parser, "abc", "abc" );
		
		String[][] bindings = { { "x", "abc" } };
		bindingsTest( parser, "abc", bindings );
	}


	public void testAction()
	{
		ParseAction f = new ParseAction()
		{
			public Object invoke(String input, int begin, Object value, Map<String, Object> bindings)
			{
				String v = (String)value;
				return v + v;
			}
		};

		ParseAction g = new ParseAction()
		{
			public Object invoke(String input, int begin, Object value, Map<String, Object> bindings)
			{
				String v = (String)value;
				return v + v + v;
			}
		};

		assertTrue( new Action( "abc", f ).compareTo( new Action( "abc", f ) ) );
		assertFalse( new Action( "abc", f ).compareTo( new Action( "def", f ) ) );
		assertFalse( new Action( "abc", f ).compareTo( new Action( "abc", g ) ) );
		assertTrue( new Action( "abc", f ).compareTo( new Literal( "abc" ).action( f ) ) );
		
		ParserExpression parser = new Literal( "abc" ).action( f );
		
		matchTest( parser, "abc", "abcabc" );
		
		String[][] bindings = {};
		bindingsTest( parser, "abc", bindings );
	}


	public void testCondition()
	{
		ParseCondition f = new ParseCondition()
		{
			public boolean test(String input, int begin, Object value, Map<String, Object> bindings)
			{
				String v = (String)value;
				return v.startsWith( "hello" );
			}
		};

		ParseCondition g = new ParseCondition()
		{
			public boolean test(String input, int begin, Object value, Map<String, Object> bindings)
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
		
		matchTest( parser, "helloworld", "helloworld" );
		matchFailTest( parser, "xabcdef" );
	}
	
	
	public void testSequence()
	{
		Object[] abqwfh = { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) };
		Object[] abqw = { new Literal( "ab" ), new Literal( "qw" ) };
		Object[] qbqwfh = { new Literal( "qb" ), new Literal( "qw" ), new Literal( "fh" ) };
		
		try
		{
			assertTrue( new Sequence( abqwfh ).compareTo( new Sequence( abqwfh ) ) );
			assertFalse( new Sequence( abqwfh ).compareTo( new Sequence( abqw ) ) );
			assertFalse( new Sequence( abqwfh ).compareTo( new Sequence( qbqwfh ) ) );
			assertTrue( new Sequence( abqwfh ).compareTo( new Literal( "ab" ).__add__( new Literal( "qw" ) ).__add__( new Literal( "fh" ) ) ) );
			assertTrue( new Sequence( abqwfh ).compareTo( new Literal( "ab" ).__add__( "qw" ).__add__( "fh" ) ) );

			Object[] subs = { new Literal( "ab" ).bindTo(  "x" ), new Literal( "qw" ).bindTo(  "y" ), new Literal( "fh" ).bindTo(  "z" ) };
			ParserExpression parser = new Sequence( subs );
			
			String[] result = { "ab", "qw", "fh" };
			
			matchTest( parser, "abqwfh", Arrays.asList( result ) );
			matchFailTest( parser, "abfh" );
			String[][] bindings = { { "x", "ab"},  { "y", "qw" },  { "z", "fh" } };
			bindingsTest( parser, "abqwfh", bindings );
		}
		catch (ParserCoerceException e)
		{
			throw new RuntimeException();
		}
	}


	public void testCombine()
	{
		Object[] abqwfh = { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) };
		Object[] abqw = { new Literal( "ab" ), new Literal( "qw" ) };
		Object[] qbqwfh = { new Literal( "qb" ), new Literal( "qw" ), new Literal( "fh" ) };
		
		try
		{
			assertTrue( new Combine( abqwfh ).compareTo( new Combine( abqwfh ) ) );
			assertFalse( new Combine( abqwfh ).compareTo( new Combine( abqw ) ) );
			assertFalse( new Combine( abqwfh ).compareTo( new Combine( qbqwfh ) ) );
			assertTrue( new Combine( abqwfh ).compareTo( new Literal( "ab" ).__sub__( new Literal( "qw" ) ).__sub__( new Literal( "fh" ) ) ) );
			assertTrue( new Combine( abqwfh ).compareTo( new Literal( "ab" ).__sub__( "qw" ).__sub__( "fh" ) ) );

			Object[] subs = { new Literal( "ab" ).bindTo(  "x" ), new Literal( "qw" ).bindTo(  "y" ), new Literal( "fh" ).bindTo(  "z" ) };
			ParserExpression parser = new Combine( subs );
			
			matchTest( parser, "abqwfh", "abqwfh" );
			matchFailTest( parser, "abfh" );
			String[][] bindings = { { "x", "ab"},  { "y", "qw" },  { "z", "fh" } };
			bindingsTest( parser, "abqwfh", bindings );

		
		
			Object[] subsB1 = { new Literal( "ab" ), new Literal( "cd" ) };
			Object[] subsB2 = { new Literal( "ef" ), new Literal( "gh" ) };
			Object[] subsB3 = { new Literal( "ij" ), new Literal( "kl" ) };
			Object[] subsB = { new Sequence( subsB1 ), new Sequence( subsB2 ), new Sequence( subsB3 ) };
			ParserExpression parser2 = new Combine( subsB );
			String[] result2 = { "ab", "cd", "ef", "gh", "ij", "kl" };
			matchTest( parser2, "abcdefghijkl", Arrays.asList( result2 ) );

			Object[] subsC = { new Sequence( subsB1 ), new Sequence( subsB2 ), new Sequence( subsB3 ), new Literal( "xyz" ) };
			ParserExpression parser3 = new Combine( subsC );
			String[] result3 = { "ab", "cd", "ef", "gh", "ij", "kl", "xyz" };
			matchTest( parser3, "abcdefghijklxyz", Arrays.asList( result3 ) );
		}
		catch (ParserCoerceException e)
		{
			throw new RuntimeException();
		}
	}


	public void testSuppress()
	{
		try
		{
			assertTrue( new Suppress( new Literal( "ab" ) ).compareTo( new Suppress( new Literal( "ab" ) ) ) );
			assertFalse( new Suppress( new Literal( "ab" ) ).compareTo( new Suppress( new Literal( "cd" ) ) ) );
			assertTrue( new Suppress( new Literal( "ab" ) ).compareTo( new Literal( "ab" ).suppress() ) );

			Object[] subs = { new Literal( "ab" ).bindTo(  "x" ), new Literal( "qw" ).bindTo(  "y" ).suppress(), new Literal( "fh" ).bindTo(  "z" ) };
			ParserExpression parser = new Sequence( subs );
			
			String[] result = { "ab", "fh" };
			
			matchTest( parser, "abqwfh", Arrays.asList( result ) );
			matchFailTest( parser, "abfh" );
			String[][] bindings = { { "x", "ab"},  { "z", "fh" } };
			bindingsTest( parser, "abqwfh", bindings );
		}
		catch (ParserCoerceException e)
		{
			throw new RuntimeException();
		}
	}


	public void testChoice()
	{
		Object[] abqwfh = { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) };
		Object[] abqw = { new Literal( "ab" ), new Literal( "qw" ) };
		Object[] qbqwfh = { new Literal( "qb" ), new Literal( "qw" ), new Literal( "fh" ) };
		
		try
		{
			assertTrue( new Choice( abqwfh ).compareTo( new Choice( abqwfh ) ) );
			assertFalse( new Choice( abqwfh ).compareTo( new Choice( abqw ) ) );
			assertFalse( new Choice( abqwfh ).compareTo( new Choice( qbqwfh ) ) );
			assertTrue( new Choice( abqwfh ).compareTo( new Literal( "ab" ).__or__( new Literal( "qw" ) ).__or__( new Literal( "fh" ) ) ) );
			assertTrue( new Choice( abqwfh ).compareTo( new Literal( "ab" ).__or__( "qw" ).__or__( "fh" ) ) );

			Object[] subs = { new Literal( "ab" ).bindTo(  "x" ), new Literal( "qw" ).bindTo(  "y" ), new Literal( "fh" ).bindTo(  "z" ) };
			ParserExpression parser = new Choice( subs );
			
			matchTest( parser, "ab", "ab" );
			matchTest( parser, "qw", "qw" );
			matchTest( parser, "fh", "fh" );
			matchFailTest( parser, "xy" );
			matchSubTest( new Literal( "ab" ).__or__( "abcd" ), "ab", "ab", 2 );
			matchSubTest( new Literal( "ab" ).__or__( "abcd" ), "abcd", "ab", 2 );

			String[][] bindings1 = { { "x", "ab"} };
			bindingsTest( parser, "ab", bindings1 );

			String[][] bindings2 = { { "y", "qw"} };
			bindingsTest( parser, "qw", bindings2 );

			String[][] bindings3 = { { "z", "fh"} };
			bindingsTest( parser, "fh", bindings3 );
		}
		catch (ParserCoerceException e)
		{
			throw new RuntimeException();
		}
	}

	public void testBestChoice()
	{
		Object[] abqwfh = { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) };
		Object[] abqw = { new Literal( "ab" ), new Literal( "qw" ) };
		Object[] qbqwfh = { new Literal( "qb" ), new Literal( "qw" ), new Literal( "fh" ) };
		
		try
		{
			assertTrue( new BestChoice( abqwfh ).compareTo( new BestChoice( abqwfh ) ) );
			assertFalse( new BestChoice( abqwfh ).compareTo( new BestChoice( abqw ) ) );
			assertFalse( new BestChoice( abqwfh ).compareTo( new BestChoice( qbqwfh ) ) );
			assertTrue( new BestChoice( abqwfh ).compareTo( new Literal( "ab" ).__xor__( new Literal( "qw" ) ).__xor__( new Literal( "fh" ) ) ) );
			assertTrue( new BestChoice( abqwfh ).compareTo( new Literal( "ab" ).__xor__( "qw" ).__xor__( "fh" ) ) );

			Object[] subs = { new Literal( "ab" ).bindTo(  "x" ), new Literal( "qw" ).bindTo(  "y" ), new Literal( "fh" ).bindTo(  "z" ) };
			ParserExpression parser = new BestChoice( subs );
			
			matchTest( parser, "ab", "ab" );
			matchTest( parser, "qw", "qw" );
			matchTest( parser, "fh", "fh" );
			matchFailTest( parser, "xy" );
			matchSubTest( new Literal( "ab" ).__xor__( "abcd" ), "ab", "ab", 2 );
			matchSubTest( new Literal( "ab" ).__xor__( "abcd" ), "abcd", "abcd", 4 );

			String[][] bindings1 = { { "x", "ab"} };
			bindingsTest( parser, "ab", bindings1 );

			String[][] bindings2 = { { "y", "qw"} };
			bindingsTest( parser, "qw", bindings2 );

			String[][] bindings3 = { { "z", "fh"} };
			bindingsTest( parser, "fh", bindings3 );
		}
		catch (ParserCoerceException e)
		{
			throw new RuntimeException();
		}
	}


}


