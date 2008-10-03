//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Parser;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import BritefuryJ.Parser.Action;
import BritefuryJ.Parser.BestChoice;
import BritefuryJ.Parser.Choice;
import BritefuryJ.Parser.Combine;
import BritefuryJ.Parser.Condition;
import BritefuryJ.Parser.Forward;
import BritefuryJ.Parser.Keyword;
import BritefuryJ.Parser.Literal;
import BritefuryJ.Parser.OneOrMore;
import BritefuryJ.Parser.Optional;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParseCondition;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Peek;
import BritefuryJ.Parser.PeekNot;
import BritefuryJ.Parser.Production;
import BritefuryJ.Parser.RegEx;
import BritefuryJ.Parser.Repetition;
import BritefuryJ.Parser.Sequence;
import BritefuryJ.Parser.Suppress;
import BritefuryJ.Parser.Word;
import BritefuryJ.Parser.ZeroOrMore;
import BritefuryJ.Parser.ParserExpression.ParserCoerceException;

public class Test_Parser extends ParserTestCase
{
	private List<Object> arrayToList2D(Object[][] a)
	{
		Vector<Object> v = new Vector<Object>();
		for (int i = 0; i < a.length; i++)
		{
			Vector<Object> v2 = new Vector<Object>();
			for (int j = 0; j < a[i].length; j++)
			{
				v2.add( a[i][j] );
			}
			v.add( v2 );
		}
		
		return v;
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
	
	
	public void testAction()
	{
		ParseAction f = new ParseAction()
		{
			public Object invoke(String input, int begin, Object value)
			{
				String v = (String)value;
				return v + v;
			}
		};

		ParseAction g = new ParseAction()
		{
			public Object invoke(String input, int begin, Object value)
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
	}


	public void testCondition()
	{
		ParseCondition f = new ParseCondition()
		{
			public boolean test(String input, int begin, Object value)
			{
				String v = (String)value;
				return v.startsWith( "hello" );
			}
		};

		ParseCondition g = new ParseCondition()
		{
			public boolean test(String input, int begin, Object value)
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

			Object[] subs = { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) };
			ParserExpression parser = new Sequence( subs );
			
			String[] result = { "ab", "qw", "fh" };
			
			matchTest( parser, "abqwfh", Arrays.asList( result ) );
			matchFailTest( parser, "abfh" );
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

			Object[] subs = { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) };
			ParserExpression parser = new Combine( subs );
			
			matchTest( parser, "abqwfh", "abqwfh" );
			matchFailTest( parser, "abfh" );

		
		
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

			Object[] subs = { new Literal( "ab" ), new Literal( "qw" ).suppress(), new Literal( "fh" ) };
			ParserExpression parser = new Sequence( subs );
			
			String[] result = { "ab", "fh" };
			
			matchTest( parser, "abqwfh", Arrays.asList( result ) );
			matchFailTest( parser, "abfh" );
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

			Object[] subs = { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) };
			ParserExpression parser = new Choice( subs );
			
			matchTest( parser, "ab", "ab" );
			matchTest( parser, "qw", "qw" );
			matchTest( parser, "fh", "fh" );
			matchFailTest( parser, "xy" );
			matchSubTest( new Literal( "ab" ).__or__( "abcd" ), "ab", "ab", 2 );
			matchSubTest( new Literal( "ab" ).__or__( "abcd" ), "abcd", "ab", 2 );
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

			Object[] subs = { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) };
			ParserExpression parser = new BestChoice( subs );
			
			matchTest( parser, "ab", "ab" );
			matchTest( parser, "qw", "qw" );
			matchTest( parser, "fh", "fh" );
			matchFailTest( parser, "xy" );
			matchSubTest( new Literal( "ab" ).__xor__( "abcd" ), "ab", "ab", 2 );
			matchSubTest( new Literal( "ab" ).__xor__( "abcd" ), "abcd", "abcd", 4 );
		}
		catch (ParserCoerceException e)
		{
			throw new RuntimeException();
		}
	}


	public void testOptional()
	{
		assertTrue( new Optional( new Literal( "ab" ) ).compareTo( new Optional( new Literal( "ab" ) ) ) );
		assertFalse( new Optional( new Literal( "ab" ) ).compareTo( new Optional( new Literal( "cd" ) ) ) );
		assertTrue( new Optional( new Literal( "ab" ) ).compareTo( new Optional( "ab" ) ) );

		ParserExpression parser = new Optional( new Word( "a", "b" ) );
		
		matchTest( parser, "", null );
		matchTest( parser, "abb", "abb" );
		matchSubTest( parser, "abbabb", "abb", 3 );
	}


	public void testRepetition()
	{
		assertTrue( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Repetition( new Literal( "ab" ), 0, 1, false ) ) );
		assertFalse( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Repetition( new Literal( "cd" ), 0, 1, false ) ) );
		assertFalse( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Repetition( new Literal( "ab" ), 1, 1, false ) ) );
		assertFalse( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Repetition( new Literal( "ab" ), 0, 2, false ) ) );
		assertFalse( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Repetition( new Literal( "ab" ), 0, 1, true ) ) );
		assertTrue( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Repetition( new Literal( "ab" ), 0, 1 ) ) );
		assertTrue( new Repetition( new Literal( "ab" ), 0, 1, false ).compareTo( new Repetition( "ab", 0, 1 ) ) );

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
		
		matchTest( parser_2, "", arrayToList2D( result0 ) );
		matchTest( parser02, "", arrayToList2D( result0 ) );
		matchTest( parser02N, "", null );
		matchFailTest( parser24, "" );
		matchFailTest( parser2_, "" );
		
		matchTest( parser_2, "abcd", arrayToList2D( result1 ) );
		matchTest( parser02, "abcd", arrayToList2D( result1 ) );
		matchTest( parser02N, "abcd", arrayToList2D( result1 ) );
		matchFailTest( parser24, "abcd" );
		matchFailTest( parser2_, "abcd" );
		
		matchTest( parser_2, "abcdabbcdd", arrayToList2D( result2 ) );
		matchTest( parser02, "abcdabbcdd", arrayToList2D( result2  ) );
		matchTest( parser02N, "abcdabbcdd", arrayToList2D( result2  ) );
		matchTest( parser24, "abcdabbcdd", arrayToList2D( result2  ) );
		matchTest( parser2_, "abcdabbcdd", arrayToList2D( result2  ) );
		
		matchSubTest( parser_2, "abcdabbcddabbbcddd", arrayToList2D( result2 ), 10 );
		matchSubTest( parser02, "abcdabbcddabbbcddd", arrayToList2D( result2 ), 10 );
		matchSubTest( parser02N, "abcdabbcddabbbcddd", arrayToList2D( result2 ), 10 );
		matchTest( parser24, "abcdabbcddabbbcddd", arrayToList2D( result3 ) );
		matchTest( parser2_, "abcdabbcddabbbcddd", arrayToList2D( result3 ) );
		
		matchSubTest( parser_2, "abcdabbcddabbbcdddabbbbcdddd", arrayToList2D( result2 ), 10 );
		matchSubTest( parser02, "abcdabbcddabbbcdddabbbbcdddd", arrayToList2D( result2 ), 10 );
		matchSubTest( parser02N, "abcdabbcddabbbcdddabbbbcdddd", arrayToList2D( result2 ), 10 );
		matchTest( parser24, "abcdabbcddabbbcdddabbbbcdddd", arrayToList2D( result4 ) );
		matchTest( parser2_, "abcdabbcddabbbcdddabbbbcdddd", arrayToList2D( result4 ) );
		
		matchSubTest( parser_2, "abcdabbcddabbbcdddabbbbcddddabbbbbcddddd", arrayToList2D( result2 ), 10 );
		matchSubTest( parser02, "abcdabbcddabbbcdddabbbbcddddabbbbbcddddd", arrayToList2D( result2 ), 10 );
		matchSubTest( parser02N, "abcdabbcddabbbcdddabbbbcddddabbbbbcddddd", arrayToList2D( result2 ), 10 );
		matchSubTest( parser24, "abcdabbcddabbbcdddabbbbcddddabbbbbcddddd", arrayToList2D( result4 ), 28 );
		matchTest( parser2_, "abcdabbcddabbbcdddabbbbcddddabbbbbcddddd", arrayToList2D( result5 ) );
	}


	public void testZeroOrMore()
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
		
		matchTest( parserO, "", arrayToList2D( result0 ) );
		matchTest( parserN, "", null );
		
		matchTest( parserO, "abcd", arrayToList2D( result1 ) );
		matchTest( parserN, "abcd", arrayToList2D( result1 ) );
		
		matchTest( parserO, "abcdabbcdd", arrayToList2D( result2 ) );
		matchTest( parserN, "abcdabbcdd", arrayToList2D( result2 ) );
	}



	public void testOneOrMore()
	{
		assertTrue( new OneOrMore( new Literal( "ab" ) ).compareTo( new OneOrMore( new Literal( "ab" ) ) ) );
		assertFalse( new OneOrMore( new Literal( "ab" ) ).compareTo( new OneOrMore( new Literal( "cd" ) ) ) );
		assertTrue( new OneOrMore( new Literal( "ab" ) ).compareTo( new OneOrMore( "ab" ) ) );

		ParserExpression parser = new OneOrMore( new Word( "a", "b" ).__add__( new Word( "c", "d" ) ) );
		
		String[][] result1 = { { "ab", "cd", } };
		String[][] result2 = { { "ab", "cd", },   { "abb", "cdd" } };
		String[][] result3 = { { "ab", "cd", },   { "abb", "cdd" },   { "abbb", "cddd" } };
		
		matchFailTest( parser, "" );
		
		matchTest( parser, "abcd", arrayToList2D( result1 ) );
		
		matchTest( parser, "abcdabbcdd", arrayToList2D( result2 ) );
		
		matchTest( parser, "abcdabbcddabbbcddd", arrayToList2D( result3 ) );
	}



	public void testPeek()
	{
		assertTrue( new Peek( new Literal( "ab" ) ).compareTo( new Peek( new Literal( "ab" ) ) ) );
		assertFalse( new Peek( new Literal( "ab" ) ).compareTo( new Peek( new Literal( "cd" ) ) ) );
		assertTrue( new Peek( new Literal( "ab" ) ).compareTo( new Peek( "ab" ) ) );

		ParserExpression parser = new OneOrMore( new Word( "a", "b" ) ).__add__( new Peek( new Word( "c", "d" ) ) );
		
		String[][] result1 = { { "ab" } };
		String[][] result2 = { { "ab", "ab" } };

		matchFailTest( parser, "" );
		matchFailTest( parser, "ab" );
		matchFailTest( parser, "abab" );
		matchSubTest( parser, "abcd", arrayToList2D( result1 ), 2 );
		matchSubTest( parser, "ababcd", arrayToList2D( result2 ), 4 );
	}



	public void testPeekNot()
	{
		assertTrue( new PeekNot( new Literal( "ab" ) ).compareTo( new PeekNot( new Literal( "ab" ) ) ) );
		assertFalse( new PeekNot( new Literal( "ab" ) ).compareTo( new PeekNot( new Literal( "cd" ) ) ) );
		assertTrue( new PeekNot( new Literal( "ab" ) ).compareTo( new PeekNot( "ab" ) ) );

		ParserExpression parser = new OneOrMore( new Word( "a", "b" ) ).__add__( new PeekNot( new Word( "c", "d" ) ) );
		
		String[][] result1 = { { "ab" } };
		String[][] result2 = { { "ab", "ab" } };

		matchFailTest( parser, "" );
		matchFailTest( parser, "abcd" );
		matchFailTest( parser, "ababcd" );
		matchTest( parser, "ab", arrayToList2D( result1 ) );
		matchTest( parser, "abab", arrayToList2D( result2 ) );
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
			public Object invoke(String input, int begin, Object x)
			{
				Vector<Object> y = new Vector<Object>();
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
			public Object invoke(String input, int begin, Object x)
			{
				List<Object> xx = (List<Object>)x;
				if ( xx.get( 1 ).equals( new Vector<Object>() ) )
				{
					return xx.get( 0 );
				}
				else
				{
					Vector<Object> res = new Vector<Object>();
					res.add( xx.get( 0 ) );
					res.addAll( (List<Object>)xx.get( 1 ) );
					return res;
				}
			}
		};
		
		ParserExpression mul = new Production( ( integer.__add__( new ZeroOrMore( mulop.__add__( integer ) ).action( flattenAction ) ) ) ).action( action );
		ParserExpression add = new Production( ( mul.__add__( new ZeroOrMore( addop.__add__( mul ) ).action( flattenAction ) ) ) ).action( action );
		ParserExpression parser = add;
		
		matchTest( parser, "123", "123" );
		
		matchTestSX( parser, "1*2", "(1 * 2)" );
		matchTestSX( parser, "1*2*3", "(1 * 2 * 3)" );

		matchTestSX( parser, "1+2", "(1 + 2)" );
		matchTestSX( parser, "1+2+3", "(1 + 2 + 3)" );

		matchTestSX( parser, "1+2*3", "(1 + (2 * 3))" );
		matchTestSX( parser, "1*2+3", "((1 * 2) + 3)" );

		matchTestSX( parser, "1*2+3+4", "((1 * 2) + 3 + 4)" );
		matchTestSX( parser, "1+2*3+4", "(1 + (2 * 3) + 4)" );
		matchTestSX( parser, "1+2+3*4", "(1 + 2 + (3 * 4))" );

		matchTestSX( parser, "1+2*3*4", "(1 + (2 * 3 * 4))" );
		matchTestSX( parser, "1*2+3*4", "((1 * 2) + (3 * 4))" );
		matchTestSX( parser, "1*2*3+4", "((1 * 2 * 3) + 4)" );
	}


	public void testRightRecursion()
	{
		ParserExpression x = new Production( "x" );
		Forward y = new Forward();
		y.setExpression( new Production( x.__add__( y ).__or__( "y" ) ) );
		
		matchTestSX( y, "xxxy", "(x (x (x y)))" );
	}


	public void testDirectLeftRecursion()
	{
		ParserExpression x = new Production( "x" );
		Forward y = new Forward();
		y.setExpression( new Production( y.__add__( x ).__or__( "y" ) ) );
		
		matchTestSX( y, "yxxx", "(((y x) x) x)" );
	}

	public void testIndirectLeftRecursion()
	{
		Production x = new Production( "x" );
		Forward z = new Forward();
		Production y = new Production( z.__add__( x ).__or__( "z" ) );
		z.setExpression( new Production( y.__or__( "y" ) ) );
		
		matchTestSX( z, "zxxx", "(((z x) x) x)" );
		matchTestSX( z, "yxxx", "(((y x) x) x)" );
	}

	public void testLeftRecursion()
	{
		ParserExpression integer = new Word( "0123456789" );
		ParserExpression plus = new Literal( "+" );
		ParserExpression minus = new Literal( "-" );
		ParserExpression star = new Literal( "*" );
		ParserExpression slash = new Literal( "/" );
	
		ParserExpression addop = plus.__or__(  minus );
		ParserExpression mulop = star.__or__(  slash );

		Forward mul = new Forward();
		mul.setExpression( new Production( ( mul.__add__( mulop ).__add__( integer ) ).__or__( integer ) ) );
		Forward add = new Forward();
		add.setExpression( new Production( ( add.__add__( addop ).__add__( mul ) ).__or__( mul ) ) );
		ParserExpression parser = add;
		
		matchTest( parser, "123", "123" );
		
		matchTestSX( parser, "1*2", "(1 * 2)" );
		matchTestSX( parser, "1*2*3", "((1 * 2) * 3)" );

		matchTestSX( parser, "1+2", "(1 + 2)" );
		matchTestSX( parser, "1+2+3", "((1 + 2) + 3)" );

		matchTestSX( parser, "1+2*3", "(1 + (2 * 3))" );
		matchTestSX( parser, "1*2+3", "((1 * 2) + 3)" );

		matchTestSX( parser, "1*2+3+4", "(((1 * 2) + 3) + 4)" );
		matchTestSX( parser, "1+2*3+4", "((1 + (2 * 3)) + 4)" );
		matchTestSX( parser, "1+2+3*4", "((1 + 2) + (3 * 4))" );

		matchTestSX( parser, "1+2*3*4", "(1 + ((2 * 3) * 4))" );
		matchTestSX( parser, "1*2+3*4", "((1 * 2) + (3 * 4))" );
		matchTestSX( parser, "1*2*3+4", "(((1 * 2) * 3) + 4)" );
	}



	public void testLeftRecursionSimplifiedJavaPrimary()
	{
		Forward primary = new Forward();
		
		
		ParserExpression expression = new Production( new Literal( "i" ).__or__( new Literal( "j" ) ) ).debug( "expression" );
		ParserExpression methodName = new Production( new Literal( "m" ).__or__( new Literal( "n" ) ) ).debug( "methodName" );
		ParserExpression interfaceTypeName = new Production( new Literal( "I" ).__or__( new Literal( "J" ) ) ).debug( "interfaceTypeName" );
		ParserExpression className = new Production( new Literal( "C" ).__or__( new Literal( "D" ) ) ).debug( "className" );

		ParserExpression classOrInterfaceType = new Production( className.__or__( interfaceTypeName ) ).debug( "classOrInterfaceType" );

		ParserExpression identifier = new Production( new Literal( "x" ).__or__( new Literal( "y" ).__or__( classOrInterfaceType ) ) ).debug( "identifier" );
		ParserExpression expressionName = new Production( identifier ).debug( "expressionName" );

		ParserExpression arrayAccess = new Production( ( primary.__add__( "[" ).__add__( expression ).__add__( "]" ) ).__or__(
											expressionName.__add__( "[" ).__add__( expression ).__add__( "]" ) ) ).debug( "expressionName" );
		ParserExpression fieldAccess = new Production( ( primary.__add__( "." ).__add__( identifier ) ).__or__(
				new Literal( "super" ).__add__( "." ).__add__( identifier ) ) ).debug( "expressionName" );
		ParserExpression methodInvocation = new Production( ( primary.__add__( "." ).__add__( methodName ).__add__( "()" ) ).__or__(
				methodName.__add__( "()" ) ) ).debug( "methodInvocation" );
		
		ParserExpression classInstanceCreationExpression = new Production( ( new Literal( "new" ).__add__( classOrInterfaceType ).__add__( "()" ) ).__or__(
				primary.__add__( "." ).__add__( "new" ).__add__( identifier ).__add__( "()" ) ) ).debug( "classInstanceCreationExpression" );
		
		ParserExpression primaryNoNewArray = new Production( classInstanceCreationExpression.__or__( methodInvocation ).__or__( fieldAccess ).__or__( arrayAccess ).__or__( "this" ) ).debug( "primaryNoNewArray" );

		primary.setExpression( new Production( primaryNoNewArray ).debug( "primary" ) );
		
		ParserExpression fieldAccessOrArrayAccess = new Production( fieldAccess.__xor__( arrayAccess ) );
	
		
	
		matchTestSX( primary, "this", "this" );
		matchTestSX( primary, "this.x", "(this . x)" );
		matchTestSX( primary, "this.x[i]", "((this . x) [ i ])" );
		matchTestSX( primary, "this.x.y", "((this . x) . y)" );
		matchTestSX( primary, "this.x.m()", "((this . x) . m \"()\")" );
		matchTestSX( primary, "this.x.m().n()", "(((this . x) . m \"()\") . n \"()\")" );
		matchTestSX( primary, "x[i][j].y", "(((x [ i ]) [ j ]) . y)" );

		matchTestSX( methodInvocation, "this.m()", "(this . m \"()\")" );
		matchTestSX( methodInvocation, "this.m().n()", "((this . m \"()\") . n \"()\")" );
		matchTestSX( methodInvocation, "this.x.m()", "((this . x) . m \"()\")" );
		matchTestSX( methodInvocation, "this.x.y.m()", "(((this . x) . y) . m \"()\")" );
		matchTestSX( methodInvocation, "this[i].m()", "((this [ i ]) . m \"()\")" );
		matchTestSX( methodInvocation, "this[i][j].m()", "(((this [ i ]) [ j ]) . m \"()\")" );
		matchTestSX( arrayAccess, "this[i]", "(this [ i ])" );
		matchTestSX( arrayAccess, "this[i][j]", "((this [ i ]) [ j ])" );
		matchTestSX( arrayAccess, "this.x[i]", "((this . x) [ i ])" );
		matchTestSX( arrayAccess, "this.x.y[i]", "(((this . x) . y) [ i ])" );
		matchTestSX( arrayAccess, "this.m()[i]", "((this . m \"()\") [ i ])" );
		matchTestSX( arrayAccess, "this.m().n()[i]", "(((this . m \"()\") . n \"()\") [ i ])" );
		matchTestSX( fieldAccess, "this.x", "(this . x)" );
		matchTestSX( fieldAccess, "this.x.y", "((this . x) . y)" );
		matchTestSX( fieldAccess, "this[i].x", "((this [ i ]) . x)" );
		matchTestSX( fieldAccess, "this[i][j].x", "(((this [ i ]) [ j ]) . x)" );
		matchTestSX( fieldAccessOrArrayAccess, "this[i]", "(this [ i ])" );
		matchTestSX( fieldAccessOrArrayAccess, "this[i].x", "((this [ i ]) . x)" );
		matchTestSX( fieldAccessOrArrayAccess, "this.x[i]", "((this . x) [ i ])" );
	}
}


/*
*/