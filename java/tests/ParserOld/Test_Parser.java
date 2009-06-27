//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.ParserOld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocModel.DMModule;
import BritefuryJ.DocModel.DMModuleResolver;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMModule.ClassAlreadyDefinedException;
import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;
import BritefuryJ.ParserDebugViewer.ParseViewFrame;
import BritefuryJ.ParserOld.Action;
import BritefuryJ.ParserOld.BestChoice;
import BritefuryJ.ParserOld.Choice;
import BritefuryJ.ParserOld.Combine;
import BritefuryJ.ParserOld.Condition;
import BritefuryJ.ParserOld.DebugParseResult;
import BritefuryJ.ParserOld.Keyword;
import BritefuryJ.ParserOld.Literal;
import BritefuryJ.ParserOld.OneOrMore;
import BritefuryJ.ParserOld.Optional;
import BritefuryJ.ParserOld.ParseAction;
import BritefuryJ.ParserOld.ParseCondition;
import BritefuryJ.ParserOld.ParserExpression;
import BritefuryJ.ParserOld.Peek;
import BritefuryJ.ParserOld.PeekNot;
import BritefuryJ.ParserOld.Production;
import BritefuryJ.ParserOld.RegEx;
import BritefuryJ.ParserOld.Repetition;
import BritefuryJ.ParserOld.SeparatedList;
import BritefuryJ.ParserOld.Sequence;
import BritefuryJ.ParserOld.StructuralItem;
import BritefuryJ.ParserOld.StructuralObject;
import BritefuryJ.ParserOld.Suppress;
import BritefuryJ.ParserOld.Word;
import BritefuryJ.ParserOld.ZeroOrMore;
import BritefuryJ.ParserOld.ParserExpression.ParserCoerceException;
import BritefuryJ.ParserOld.SeparatedList.CannotApplyConditionAfterActionException;
import BritefuryJ.ParserOld.SeparatedList.CannotApplyMoreThanOneActionException;
import BritefuryJ.ParserOld.SeparatedList.CannotApplyMoreThanOneConditionException;

public class Test_Parser extends ParserTestCase
{
	public static ParserExpression identifier = new RegEx( "[A-Za-z_][A-Za-z0-9_]*" );

	protected DMModule M;
	protected DMModuleResolver resolver = new DMModuleResolver()
	{
		public DMModule getModule(String location) throws CouldNotResolveModuleException
		{
			return location.equals( "Tests.PatternMatch" )  ?  M  :  null;
		}
	};
	
	
	
	protected DMModuleResolver getModuleResolver()
	{
		return resolver;
	}
	

	
	public void setUp()
	{
		M = new DMModule( "PatternMatchTest", "m", "Tests.PatternMatch" );
	}
	
	public void tearDown()
	{
		M = null;
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
	
	
	public void testStructuralNode()
	{
		assertTrue( new StructuralItem().compareTo( new StructuralItem() ) );

		ItemStreamBuilder builder1 = new ItemStreamBuilder();
		builder1.appendStructuralValue( new Integer( 12 ) );
		
		ItemStreamBuilder builder2 = new ItemStreamBuilder();
		builder2.appendStructuralValue( new Integer( 12 ) );
		builder2.appendStructuralValue( new Integer( 13 ) );

		matchTest( new StructuralItem(), builder1.stream(), new Integer( 12 ) );
		matchSubTest( new StructuralItem(), builder2.stream(), new Integer( 12 ), 1 );
	}


	public void testStructuralObject() throws ClassAlreadyDefinedException
	{
		DMObjectClass A = M.newClass( "A", new String[] {} );
		DMObjectClass B = M.newClass( "B", A, new String[] {} );

		assertTrue( new StructuralObject( A ).compareTo( new StructuralObject( A ) ) );
		assertFalse( new StructuralObject( A ).compareTo( new StructuralObject( B ) ) );

		ItemStreamBuilder builder1 = new ItemStreamBuilder();
		builder1.appendStructuralValue( A.newInstance() );
		
		matchTest( new StructuralObject( A ), builder1.stream(), A.newInstance() );
		matchFailTest( new StructuralObject( B ), builder1.stream() );
		
		
		ItemStreamBuilder builder2 = new ItemStreamBuilder();
		builder2.appendStructuralValue( A.newInstance() );
		builder2.appendTextValue( " : " );
		builder2.appendStructuralValue( B.newInstance() );

		ItemStreamBuilder builder3 = new ItemStreamBuilder();
		builder3.appendStructuralValue( B.newInstance() );
		builder3.appendTextValue( " : " );
		builder3.appendStructuralValue( B.newInstance() );

		ItemStreamBuilder builder4 = new ItemStreamBuilder();
		builder4.appendStructuralValue( A.newInstance() );
		builder4.appendTextValue( " : " );
		builder4.appendStructuralValue( A.newInstance() );

		ParserExpression parser = new Sequence( new ParserExpression[] { new StructuralObject( A ), new Literal( ":" ), new StructuralObject( B ) } );
		matchTest( parser, builder2.stream(), Arrays.asList( new Object[] { A.newInstance(), ":", B.newInstance() } ) );
		matchTest( parser, builder3.stream(), Arrays.asList( new Object[] { B.newInstance(), ":", B.newInstance() } ) );
		matchFailTest( parser, builder4.stream() );
	}


	public void testAction() throws ParserExpression.ParserCoerceException
	{
		ParseAction f = new ParseAction()
		{
			public Object invoke(ItemStreamAccessor input, int begin, Object value)
			{
				String v = (String)value;
				return v + v;
			}
		};

		ParseAction g = new ParseAction()
		{
			public Object invoke(ItemStreamAccessor input, int begin, Object value)
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


	public void testCondition() throws ParserExpression.ParserCoerceException
	{
		ParseCondition f = new ParseCondition()
		{
			public boolean test(ItemStreamAccessor input, int begin, Object value)
			{
				String v = (String)value;
				return v.startsWith( "hello" );
			}
		};

		ParseCondition g = new ParseCondition()
		{
			public boolean test(ItemStreamAccessor input, int begin, Object value)
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
		
		matchTest( parser, "abqwfh", Arrays.asList( result ) );
		matchFailTest( parser, "abfh" );
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


	public void testSuppress() throws ParserExpression.ParserCoerceException
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

		Object[] subs = { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) };
		ParserExpression parser = new Choice( subs );
		
		matchTest( parser, "ab", "ab" );
		matchTest( parser, "qw", "qw" );
		matchTest( parser, "fh", "fh" );
		matchFailTest( parser, "xy" );
		matchSubTest( new Literal( "ab" ).__or__( "abcd" ), "ab", "ab", 2 );
		matchSubTest( new Literal( "ab" ).__or__( "abcd" ), "abcd", "ab", 2 );
	}

	public void testBestChoice() throws ParserExpression.ParserCoerceException
	{
		Object[] abqwfh = { new Literal( "ab" ), new Literal( "qw" ), new Literal( "fh" ) };
		Object[] abqw = { new Literal( "ab" ), new Literal( "qw" ) };
		Object[] qbqwfh = { new Literal( "qb" ), new Literal( "qw" ), new Literal( "fh" ) };
		
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


	public void testOptional() throws ParserExpression.ParserCoerceException
	{
		assertTrue( new Optional( new Literal( "ab" ) ).compareTo( new Optional( new Literal( "ab" ) ) ) );
		assertFalse( new Optional( new Literal( "ab" ) ).compareTo( new Optional( new Literal( "cd" ) ) ) );
		assertTrue( new Optional( new Literal( "ab" ) ).compareTo( new Optional( "ab" ) ) );

		ParserExpression parser = new Word( "a", "b" ).optional();
		
		matchTest( parser, "", null );
		matchTest( parser, "abb", "abb" );
		matchSubTest( parser, "abbabb", "abb", 3 );
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
		
		matchTest( parserO, "", arrayToList2D( result0 ) );
		matchTest( parserN, "", null );
		
		matchTest( parserO, "abcd", arrayToList2D( result1 ) );
		matchTest( parserN, "abcd", arrayToList2D( result1 ) );
		
		matchTest( parserO, "abcdabbcdd", arrayToList2D( result2 ) );
		matchTest( parserN, "abcdabbcdd", arrayToList2D( result2 ) );
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
		
		matchFailTest( parser, "" );
		
		matchTest( parser, "abcd", arrayToList2D( result1 ) );
		
		matchTest( parser, "abcdabbcdd", arrayToList2D( result2 ) );
		
		matchTest( parser, "abcdabbcddabbbcddd", arrayToList2D( result3 ) );
	}



	public void testPeek() throws ParserExpression.ParserCoerceException
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



	public void testPeekNot() throws ParserExpression.ParserCoerceException
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
			public boolean test(ItemStreamAccessor input, int begin, List<Object> elements, boolean gotTrailingSeparator)
			{
				return elements.size() % 2  ==  0;
			}
		};
		
		SeparatedList.ListAction action = new SeparatedList.ListAction()
		{
			public Object invoke(ItemStreamAccessor input, int begin, List<Object> elements, boolean gotTrailingSeparator)
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

		
		
		matchTestSX( parser0N, "", "[]" );
		matchIncompleteTest( parser0N, "," );
		matchTestSX( parser0N, "ab", "[ab]" );
		matchIncompleteTest( parser0N, "ab," );
		matchTestSX( parser0N, "ab,cd", "[ab cd]" );
		matchIncompleteTest( parser0N, "ab,cd," );
		matchTestSX( parser0N, "ab,cd,ef", "[ab cd ef]" );
		matchIncompleteTest( parser0N, "ab,cd,ef," );
		
		matchFailTest( parser1N, "" );
		matchFailTest( parser1N, "," );
		matchTestSX( parser1N, "ab", "[ab]" );
		matchIncompleteTest( parser1N, "ab," );
		matchTestSX( parser1N, "ab,cd", "[ab cd]" );
		matchIncompleteTest( parser1N, "ab,cd," );
		matchTestSX( parser1N, "ab,cd,ef", "[ab cd ef]" );
		matchIncompleteTest( parser1N, "ab,cd,ef," );
		
		matchFailTest( parser2N, "" );
		matchFailTest( parser2N, "," );
		matchFailTest( parser2N, "ab" );
		matchFailTest( parser2N, "ab," );
		matchTestSX( parser2N, "ab,cd", "[ab cd]" );
		matchIncompleteTest( parser2N, "ab,cd," );
		matchTestSX( parser2N, "ab,cd,ef", "[ab cd ef]" );
		matchIncompleteTest( parser2N, "ab,cd,ef," );

		matchFailTest( parser23N, "" );
		matchFailTest( parser23N, "," );
		matchFailTest( parser23N, "ab" );
		matchFailTest( parser23N, "ab," );
		matchTestSX( parser23N, "ab,cd", "[ab cd]" );
		matchIncompleteTest( parser23N, "ab,cd," );
		matchTestSX( parser23N, "ab,cd,ef", "[ab cd ef]" );
		matchIncompleteTest( parser23N, "ab,cd,ef," );
		matchIncompleteTest( parser23N, "ab,cd,ef,gh" );
		matchIncompleteTest( parser23N, "ab,cd,ef,gh," );

		matchTestSX( parserDN, "[]", "[]" );
		matchFailTest( parserDN, "[,]" );
		matchTestSX( parserDN, "[ab]", "[ab]" );
		matchFailTest( parserDN, "[ab,]" );
		matchTestSX( parserDN, "[ab,cd]", "[ab cd]" );
		matchFailTest( parserDN, "[ab,cd,]" );
		matchTestSX( parserDN, "[ab,cd,ef]", "[ab cd ef]" );
		matchFailTest( parserDN, "[ab,cd,ef,]" );
		

		
		matchTestSX( parser0O, "", "[]" );
		matchIncompleteTest( parser0O, "," );
		matchTestSX( parser0O, "ab", "[ab]" );
		matchTestSX( parser0O, "ab,", "[ab]" );
		matchTestSX( parser0O, "ab,cd", "[ab cd]" );
		matchTestSX( parser0O, "ab,cd,", "[ab cd]" );
		matchTestSX( parser0O, "ab,cd,ef", "[ab cd ef]" );
		matchTestSX( parser0O, "ab,cd,ef,", "[ab cd ef]" );
		
		matchFailTest( parser1O, "" );
		matchFailTest( parser1O, "," );
		matchTestSX( parser1O, "ab", "[ab]" );
		matchTestSX( parser1O, "ab,", "[ab]" );
		matchTestSX( parser1O, "ab,cd", "[ab cd]" );
		matchTestSX( parser1O, "ab,cd,", "[ab cd]" );
		matchTestSX( parser1O, "ab,cd,ef", "[ab cd ef]" );
		matchTestSX( parser1O, "ab,cd,ef,", "[ab cd ef]" );
		
		matchFailTest( parser2O, "" );
		matchFailTest( parser2O, "," );
		matchFailTest( parser2O, "ab" );
		matchFailTest( parser2O, "ab," );
		matchTestSX( parser2O, "ab,cd", "[ab cd]" );
		matchTestSX( parser2O, "ab,cd,", "[ab cd]" );
		matchTestSX( parser2O, "ab,cd,ef", "[ab cd ef]" );
		matchTestSX( parser2O, "ab,cd,ef,", "[ab cd ef]" );
	
		matchFailTest( parser23O, "" );
		matchFailTest( parser23O, "," );
		matchFailTest( parser23O, "ab" );
		matchFailTest( parser23O, "ab," );
		matchTestSX( parser23O, "ab,cd", "[ab cd]" );
		matchTestSX( parser23O, "ab,cd,", "[ab cd]" );
		matchTestSX( parser23O, "ab,cd,ef", "[ab cd ef]" );
		matchTestSX( parser23O, "ab,cd,ef,", "[ab cd ef]" );
		matchIncompleteTest( parser23O, "ab,cd,ef,gh" );
		matchIncompleteTest( parser23O, "ab,cd,ef,gh," );
	
		matchTestSX( parserDO, "[]", "[]" );
		matchFailTest( parserDO, "[,]" );
		matchTestSX( parserDO, "[ab]", "[ab]" );
		matchTestSX( parserDO, "[ab,]", "[ab]" );
		matchTestSX( parserDO, "[ab,cd]", "[ab cd]" );
		matchTestSX( parserDO, "[ab,cd,]", "[ab cd]" );
		matchTestSX( parserDO, "[ab,cd,ef]", "[ab cd ef]" );
		matchTestSX( parserDO, "[ab,cd,ef,]", "[ab cd ef]" );
		

		
		matchTestSX( parser0R, "", "[]" );
		matchIncompleteTest( parser0R, "," );
		matchIncompleteTest( parser0R, "ab" );
		matchTestSX( parser0R, "ab,", "[ab]" );
		matchIncompleteTest( parser0R, "ab,cd" );
		matchTestSX( parser0R, "ab,cd,", "[ab cd]" );
		matchIncompleteTest( parser0R, "ab,cd,ef" );
		matchTestSX( parser0R, "ab,cd,ef,", "[ab cd ef]" );
		
		matchFailTest( parser1R, "" );
		matchFailTest( parser1R, "," );
		matchFailTest( parser1R, "ab" );
		matchTestSX( parser1R, "ab,", "[ab]" );
		matchIncompleteTest( parser1R, "ab,cd" );
		matchTestSX( parser1R, "ab,cd,", "[ab cd]" );
		matchIncompleteTest( parser1R, "ab,cd,ef" );
		matchTestSX( parser1R, "ab,cd,ef,", "[ab cd ef]" );
		
		matchFailTest( parser2R, "" );
		matchFailTest( parser2R, "," );
		matchFailTest( parser2R, "ab" );
		matchFailTest( parser2R, "ab," );
		matchFailTest( parser2R, "ab,cd" );
		matchTestSX( parser2R, "ab,cd,", "[ab cd]" );
		matchIncompleteTest( parser2R, "ab,cd,ef" );
		matchTestSX( parser2R, "ab,cd,ef,", "[ab cd ef]" );

		matchFailTest( parser23R, "" );
		matchFailTest( parser23R, "," );
		matchFailTest( parser23R, "ab" );
		matchFailTest( parser23R, "ab," );
		matchFailTest( parser23R, "ab,cd" );
		matchTestSX( parser23R, "ab,cd,", "[ab cd]" );
		matchIncompleteTest( parser23R, "ab,cd,ef" );
		matchTestSX( parser23R, "ab,cd,ef,", "[ab cd ef]" );
		matchIncompleteTest( parser23O, "ab,cd,ef,gh" );
		matchIncompleteTest( parser23O, "ab,cd,ef,gh," );
		
		matchTestSX( parserDR, "[]", "[]" );
		matchFailTest( parserDR, "[,]" );
		matchFailTest( parserDR, "[ab]" );
		matchTestSX( parserDR, "[ab,]", "[ab]" );
		matchFailTest( parserDR, "[ab,cd]");
		matchTestSX( parserDR, "[ab,cd,]", "[ab cd]" );
		matchFailTest( parserDR, "[ab,cd,ef]" );
		matchTestSX( parserDR, "[ab,cd,ef,]", "[ab cd ef]" );

	
	
		matchTestSX( parserDOC, "[]", "[]" );
		matchFailTest( parserDOC, "[,]" );
		matchFailTest( parserDOC, "[ab]" );
		matchFailTest( parserDOC, "[ab,]" );
		matchTestSX( parserDOC, "[ab,cd]", "[ab cd]" );
		matchTestSX( parserDOC, "[ab,cd,]", "[ab cd]" );
		matchFailTest( parserDOC, "[ab,cd,ef]" );
		matchFailTest( parserDOC, "[ab,cd,ef,]" );
		matchTestSX( parserDOC, "[ab,cd,ef,gh]", "[ab cd ef gh]" );
		matchTestSX( parserDOC, "[ab,cd,ef,gh,]", "[ab cd ef gh]" );
		
		matchTestSX( parserDOA, "[]", "[]" );
		matchFailTest( parserDOA, "[,]" );
		matchTestSX( parserDOA, "[ab]", "[ab]" );
		matchTestSX( parserDOA, "[ab,]", "[ab sep]" );
		matchTestSX( parserDOA, "[ab,cd]", "[ab cd]" );
		matchTestSX( parserDOA, "[ab,cd,]", "[ab cd sep]" );
		matchTestSX( parserDOA, "[ab,cd,ef]", "[ab cd ef]" );
		matchTestSX( parserDOA, "[ab,cd,ef,]", "[ab cd ef sep]" );
		matchTestSX( parserDOA, "[ab,cd,ef,gh]", "[ab cd ef gh]" );
		matchTestSX( parserDOA, "[ab,cd,ef,gh,]", "[ab cd ef gh sep]" );
		
		matchTestSX( parserDOCA, "[]", "[]" );
		matchFailTest( parserDOCA, "[,]" );
		matchFailTest( parserDOCA, "[ab]" );
		matchFailTest( parserDOCA, "[ab,]" );
		matchTestSX( parserDOCA, "[ab,cd]", "[ab cd]" );
		matchTestSX( parserDOCA, "[ab,cd,]", "[ab cd sep]" );
		matchFailTest( parserDOCA, "[ab,cd,ef]" );
		matchFailTest( parserDOCA, "[ab,cd,ef,]" );
		matchTestSX( parserDOCA, "[ab,cd,ef,gh]", "[ab cd ef gh]" );
		matchTestSX( parserDOCA, "[ab,cd,ef,gh,]", "[ab cd ef gh sep]" );
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
			public Object invoke(ItemStreamAccessor input, int begin, Object x)
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
			public Object invoke(ItemStreamAccessor input, int begin, Object x)
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
		
		matchTest( parser, "123", "123" );
		
		matchTestSX( parser, "1*2", "[1 * 2]" );
		matchTestSX( parser, "1*2*3", "[1 * 2 * 3]" );

		matchTestSX( parser, "1+2", "[1 + 2]" );
		matchTestSX( parser, "1+2+3", "[1 + 2 + 3]" );

		matchTestSX( parser, "1+2*3", "[1 + [2 * 3]]" );
		matchTestSX( parser, "1*2+3", "[[1 * 2] + 3]" );

		matchTestSX( parser, "1*2+3+4", "[[1 * 2] + 3 + 4]" );
		matchTestSX( parser, "1+2*3+4", "[1 + [2 * 3] + 4]" );
		matchTestSX( parser, "1+2+3*4", "[1 + 2 + [3 * 4]]" );

		matchTestSX( parser, "1+2*3*4", "[1 + [2 * 3 * 4]]" );
		matchTestSX( parser, "1*2+3*4", "[[1 * 2] + [3 * 4]]" );
		matchTestSX( parser, "1*2*3+4", "[[1 * 2 * 3] + 4]" );
	}


	public void testRightRecursion() throws ParserExpression.ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		Production x = new Production( "_x", "x" );
		Production y = new Production( "_y" );
		y.setExpression( x.__add__( y ).__or__( "y" ) );
		
		matchTestSX( y, "xxxy", "[x [x [x y]]]" );
	}


	public void testDirectLeftRecursion() throws ParserExpression.ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		Production x = new Production( "_x", "x" );
		Production y = new Production( "_y" );
		y.setExpression( y.__add__( x ).__or__( "y" ) );
		
		matchTestSX( y, "yxxx", "[[[y x] x] x]" );
	}

	public void testIndirectLeftRecursion() throws ParserExpression.ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		Production x = new Production( "_x", "x" );
		Production z = new Production( "_z" );
		Production y = new Production( "_y", z.__add__( x ).__or__( "z" ) );
		z.setExpression( y.__or__( "y" ) );
		
		matchTestSX( z, "zxxx", "[[[z x] x] x]" );
		matchTestSX( z, "yxxx", "[[[y x] x] x]" );
	}

	public void testLeftRecursion() throws Production.CannotOverwriteProductionExpressionException, ClassAlreadyDefinedException
	{
		DMObjectClass Num = M.newClass( "Num", new String[] { "x" } );
		
		
		ParserExpression integer = new Word( "0123456789" );
		ParserExpression plus = new Literal( "+" );
		ParserExpression minus = new Literal( "-" );
		ParserExpression star = new Literal( "*" );
		ParserExpression slash = new Literal( "/" );
	
		ParserExpression addop = plus.__or__(  minus );
		ParserExpression mulop = star.__or__(  slash );

		Production number = new Production( "number" );
		number.setExpression( integer.__or__( new StructuralObject( Num ) ) );
		
		Production mul = new Production( "mul" );
		mul.setExpression( ( mul.__add__( mulop ).__add__( number ) ).__or__( number ) );
		Production add = new Production( "add" );
		add.setExpression( ( add.__add__( addop ).__add__( mul ) ).__or__( mul ) );
		ParserExpression parser = add;
		
		matchTest( parser, "123", "123" );
		
		matchTestSX( parser, "1*2", "[1 * 2]" );
		matchTestSX( parser, "1*2*3", "[[1 * 2] * 3]" );

		matchTestSX( parser, "1+2", "[1 + 2]" );
		matchTestSX( parser, "1+2+3", "[[1 + 2] + 3]" );

		matchTestSX( parser, "1+2*3", "[1 + [2 * 3]]" );
		matchTestSX( parser, "1*2+3", "[[1 * 2] + 3]" );

		matchTestSX( parser, "1*2+3+4", "[[[1 * 2] + 3] + 4]" );
		matchTestSX( parser, "1+2*3+4", "[[1 + [2 * 3]] + 4]" );
		matchTestSX( parser, "1+2+3*4", "[[1 + 2] + [3 * 4]]" );

		matchTestSX( parser, "1+2*3*4", "[1 + [[2 * 3] * 4]]" );
		matchTestSX( parser, "1*2+3*4", "[[1 * 2] + [3 * 4]]" );
		matchTestSX( parser, "1*2*3+4", "[[[1 * 2] * 3] + 4]" );
		
		
		ItemStreamBuilder builder1 = new ItemStreamBuilder();
		builder1.appendTextValue( "1+" );
		builder1.appendStructuralValue( Num.newInstance( new Object[] { "2" } ) );
		builder1.appendTextValue( "*3*4" );

		ItemStreamBuilder builder2 = new ItemStreamBuilder();
		builder2.appendTextValue( "1*" );
		builder2.appendStructuralValue( Num.newInstance( new Object[] { "2" } ) );
		builder2.appendTextValue( "+3*4" );

		ItemStreamBuilder builder3 = new ItemStreamBuilder();
		builder3.appendTextValue( "1*" );
		builder3.appendStructuralValue( Num.newInstance( new Object[] { "2" } ) );
		builder3.appendTextValue( "*3+4" );

		matchTestSX( parser, builder1.stream(), "{m=Tests.PatternMatch : [1 + [[(m Num x=2) * 3] * 4]]}" );
		matchTestSX( parser, builder2.stream(), "{m=Tests.PatternMatch : [[1 * (m Num x=2)] + [3 * 4]]}" );
		matchTestSX( parser, builder3.stream(), "{m=Tests.PatternMatch : [[[1 * (m Num x=2)] * 3] + 4]}" );
	}



	public void testLeftRecursionSimplifiedJavaPrimary() throws ParserExpression.ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		ParseAction arrayAccessAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(ItemStreamAccessor input, int begin, Object value)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( new Object[] { "arrayAccess", v.get( 0 ), v.get( 2 ) } );
			}
		};
		
		ParseAction fieldAccessAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(ItemStreamAccessor input, int begin, Object value)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( new Object[] { "fieldAccess", v.get( 0 ), v.get( 2 ) } );
			}
		};
		
		ParseAction objectMethodInvocationAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(ItemStreamAccessor input, int begin, Object value)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( new Object[] { "methodInvoke", v.get( 0 ), v.get( 2 ) } );
			}
		};
		
		ParseAction thisMethodInvocationAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(ItemStreamAccessor input, int begin, Object value)
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
		
	
		matchTestSX( primary, "this", "this" );
		matchTestSX( primary, "this.x", "[fieldAccess this x]" );
		matchTestSX( primary, "this.x[i]", "[arrayAccess [fieldAccess this x] i]" );
		matchTestSX( primary, "this.x.y", "[fieldAccess [fieldAccess this x] y]" );
		matchTestSX( primary, "this.x.m()", "[methodInvoke [fieldAccess this x] m]" );
		matchTestSX( primary, "this.x.m().n()", "[methodInvoke [methodInvoke [fieldAccess this x] m] n]" );
		matchTestSX( primary, "x[i][j].y", "[fieldAccess [arrayAccess [arrayAccess x i] j] y]" );

		matchTestSX( methodInvocation, "this.m()", "[methodInvoke this m]" );
		matchTestSX( methodInvocation, "this.m().n()", "[methodInvoke [methodInvoke this m] n]" );
		matchTestSX( methodInvocation, "this.x.m()", "[methodInvoke [fieldAccess this x] m]" );
		matchTestSX( methodInvocation, "this.x.y.m()", "[methodInvoke [fieldAccess [fieldAccess this x] y] m]" );
		matchTestSX( methodInvocation, "this[i].m()", "[methodInvoke [arrayAccess this i] m]" );
		matchTestSX( methodInvocation, "this[i][j].m()", "[methodInvoke [arrayAccess [arrayAccess this i] j] m]" );
		matchTestSX( arrayAccess, "this[i]", "[arrayAccess this i]" );
		matchTestSX( arrayAccess, "this[i][j]", "[arrayAccess [arrayAccess this i] j]" );
		matchTestSX( arrayAccess, "this.x[i]", "[arrayAccess [fieldAccess this x] i]" );
		matchTestSX( arrayAccess, "this.x.y[i]", "[arrayAccess [fieldAccess [fieldAccess this x] y] i]" );
		matchTestSX( arrayAccess, "this.m()[i]", "[arrayAccess [methodInvoke this m] i]" );
		matchTestSX( arrayAccess, "this.m().n()[i]", "[arrayAccess [methodInvoke [methodInvoke this m] n] i]" );
		matchTestSX( fieldAccess, "this.x", "[fieldAccess this x]" );
		matchTestSX( fieldAccess, "this.x.y", "[fieldAccess [fieldAccess this x] y]" );
		matchTestSX( fieldAccess, "this[i].x", "[fieldAccess [arrayAccess this i] x]" );
		matchTestSX( fieldAccess, "this[i][j].x", "[fieldAccess [arrayAccess [arrayAccess this i] j] x]" );
		matchTestSX( fieldAccessOrArrayAccess, "this[i]", "[arrayAccess this i]" );
		matchTestSX( fieldAccessOrArrayAccess, "this[i].x", "[fieldAccess [arrayAccess this i] x]" );
		matchTestSX( fieldAccessOrArrayAccess, "this.x[i]", "[arrayAccess [fieldAccess this x] i]" );
	}
	
	
	public static void main(String[] args) throws ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		ParseAction arrayAccessAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(ItemStreamAccessor input, int begin, Object value)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( new Object[] { "arrayAccess", v.get( 0 ), v.get( 2 ) } );
			}
		};
		
		ParseAction fieldAccessAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(ItemStreamAccessor input, int begin, Object value)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( new Object[] { "fieldAccess", v.get( 0 ), v.get( 2 ) } );
			}
		};
		
		ParseAction objectMethodInvocationAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(ItemStreamAccessor input, int begin, Object value)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( new Object[] { "methodInvoke", v.get( 0 ), v.get( 2 ) } );
			}
		};
		
		ParseAction thisMethodInvocationAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(ItemStreamAccessor input, int begin, Object value)
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

		
		DebugParseResult d = fieldAccessOrArrayAccess.debugParseString( "this[i]" );
		new ParseViewFrame( d );
	}
}


/*
*/