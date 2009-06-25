//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.ParserNew;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import BritefuryJ.DocModel.DMModule;
import BritefuryJ.DocModel.DMModuleResolver;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMModule.ClassAlreadyDefinedException;
import BritefuryJ.DocModel.DMObjectClass.InvalidFieldNameException;
import BritefuryJ.ParserNew.Action;
import BritefuryJ.ParserNew.Choice;
import BritefuryJ.ParserNew.Combine;
import BritefuryJ.ParserNew.Condition;
import BritefuryJ.ParserNew.DebugParseResult;
import BritefuryJ.ParserNew.Keyword;
import BritefuryJ.ParserNew.Literal;
import BritefuryJ.ParserNew.ObjectNode;
import BritefuryJ.ParserNew.OneOrMore;
import BritefuryJ.ParserNew.Optional;
import BritefuryJ.ParserNew.ParseAction;
import BritefuryJ.ParserNew.ParseCondition;
import BritefuryJ.ParserNew.ParserExpression;
import BritefuryJ.ParserNew.Peek;
import BritefuryJ.ParserNew.PeekNot;
import BritefuryJ.ParserNew.Production;
import BritefuryJ.ParserNew.RegEx;
import BritefuryJ.ParserNew.Repetition;
import BritefuryJ.ParserNew.SeparatedList;
import BritefuryJ.ParserNew.Sequence;
import BritefuryJ.ParserNew.Suppress;
import BritefuryJ.ParserNew.Word;
import BritefuryJ.ParserNew.ZeroOrMore;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;
import BritefuryJ.ParserNew.ParserExpression.ParserCoerceException;
import BritefuryJ.ParserNew.SeparatedList.CannotApplyConditionAfterActionException;
import BritefuryJ.ParserNew.SeparatedList.CannotApplyMoreThanOneActionException;
import BritefuryJ.ParserNew.SeparatedList.CannotApplyMoreThanOneConditionException;
import BritefuryJ.ParserDebugViewer.ParseViewFrame;

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
		matchTestStringAndStream( new Literal( "abcxyz" ), "abcxyz", "abcxyz" );
		matchFailTestStringAndStream( new Literal( "abcxyz" ), "qwerty" );
		matchSubTestStringAndStream( new Literal( "abcxyz" ), "abcxyz123", "abcxyz", 6 );
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
	}
	
	
//	public void testStructuralNode()
//	{
//		assertTrue( new StructuralItem().compareTo( new StructuralItem() ) );
//
//		ItemStreamBuilder builder1 = new ItemStreamBuilder();
//		builder1.appendStructuralValue( new Integer( 12 ) );
//		
//		ItemStreamBuilder builder2 = new ItemStreamBuilder();
//		builder2.appendStructuralValue( new Integer( 12 ) );
//		builder2.appendStructuralValue( new Integer( 13 ) );
//
//		matchTestStream( new StructuralItem(), builder1.stream(), new Integer( 12 ) );
//		matchSubTestStream( new StructuralItem(), builder2.stream(), new Integer( 12 ), 1 );
//	}
//
//
//	public void testStructuralObject() throws ClassAlreadyDefinedException
//	{
//		DMObjectClass A = M.newClass( "A", new String[] {} );
//		DMObjectClass B = M.newClass( "B", A, new String[] {} );
//
//		assertTrue( new StructuralObject( A ).compareTo( new StructuralObject( A ) ) );
//		assertFalse( new StructuralObject( A ).compareTo( new StructuralObject( B ) ) );
//
//		ItemStreamBuilder builder1 = new ItemStreamBuilder();
//		builder1.appendStructuralValue( A.newInstance() );
//		
//		matchTestStream( new StructuralObject( A ), builder1.stream(), A.newInstance() );
//		matchFailTestStream( new StructuralObject( B ), builder1.stream() );
//		
//		
//		ItemStreamBuilder builder2 = new ItemStreamBuilder();
//		builder2.appendStructuralValue( A.newInstance() );
//		builder2.appendTextValue( " : " );
//		builder2.appendStructuralValue( B.newInstance() );
//
//		ItemStreamBuilder builder3 = new ItemStreamBuilder();
//		builder3.appendStructuralValue( B.newInstance() );
//		builder3.appendTextValue( " : " );
//		builder3.appendStructuralValue( B.newInstance() );
//
//		ItemStreamBuilder builder4 = new ItemStreamBuilder();
//		builder4.appendStructuralValue( A.newInstance() );
//		builder4.appendTextValue( " : " );
//		builder4.appendStructuralValue( A.newInstance() );
//
//		ParserExpression parser = new Sequence( new ParserExpression[] { new StructuralObject( A ), new Literal( ":" ), new StructuralObject( B ) } );
//		matchTestStream( parser, builder2.stream(), Arrays.asList( new Object[] { A.newInstance(), ":", B.newInstance() } ) );
//		matchTestStream( parser, builder3.stream(), Arrays.asList( new Object[] { B.newInstance(), ":", B.newInstance() } ) );
//		matchFailTestStream( parser, builder4.stream() );
//	}


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

		assertTrue( new Action( "abc", f ).compareTo( new Action( "abc", f ) ) );
		assertFalse( new Action( "abc", f ).compareTo( new Action( "def", f ) ) );
		assertFalse( new Action( "abc", f ).compareTo( new Action( "abc", g ) ) );
		assertTrue( new Action( "abc", f ).compareTo( new Literal( "abc" ).action( f ) ) );
		
		ParserExpression parser = new Literal( "abc" ).action( f );
		
		matchTestStringAndStream( parser, "abc", "abcabc" );
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
	}


	public void testSuppress() throws ParserExpression.ParserCoerceException
	{
		assertTrue( new Suppress( new Literal( "ab" ) ).compareTo( new Suppress( new Literal( "ab" ) ) ) );
		assertFalse( new Suppress( new Literal( "ab" ) ).compareTo( new Suppress( new Literal( "cd" ) ) ) );
		assertTrue( new Suppress( new Literal( "ab" ) ).compareTo( new Literal( "ab" ).suppress() ) );

		Object[] subs = { new Literal( "ab" ), new Literal( "qw" ).suppress(), new Literal( "fh" ) };
		ParserExpression parser = new Sequence( subs );
		
		String[] result = { "ab", "fh" };
		
		matchTestStringAndStream( parser, "abqwfh", Arrays.asList( result ) );
		matchFailTestStringAndStream( parser, "abfh" );
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
		
		matchTestStringAndStream( parser, "ab", "ab" );
		matchTestStringAndStream( parser, "qw", "qw" );
		matchTestStringAndStream( parser, "fh", "fh" );
		matchFailTestStringAndStream( parser, "xy" );
		matchSubTestStringAndStream( new Literal( "ab" ).__or__( "abcd" ), "ab", "ab", 2 );
		matchSubTestStringAndStream( new Literal( "ab" ).__or__( "abcd" ), "abcd", "ab", 2 );
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
	}



	public void testPeek() throws ParserExpression.ParserCoerceException
	{
		assertTrue( new Peek( new Literal( "ab" ) ).compareTo( new Peek( new Literal( "ab" ) ) ) );
		assertFalse( new Peek( new Literal( "ab" ) ).compareTo( new Peek( new Literal( "cd" ) ) ) );
		assertTrue( new Peek( new Literal( "ab" ) ).compareTo( new Peek( "ab" ) ) );

		ParserExpression parser = new OneOrMore( new Word( "a", "b" ) ).__add__( new Peek( new Word( "c", "d" ) ) );
		
		String[][] result1 = { { "ab" } };
		String[][] result2 = { { "ab", "ab" } };

		matchFailTestStringAndStream( parser, "" );
		matchFailTestStringAndStream( parser, "ab" );
		matchFailTestStringAndStream( parser, "abab" );
		matchSubTestStringAndStream( parser, "abcd", arrayToList2D( result1 ), 2 );
		matchSubTestStringAndStream( parser, "ababcd", arrayToList2D( result2 ), 4 );
	}



	public void testPeekNot() throws ParserExpression.ParserCoerceException
	{
		assertTrue( new PeekNot( new Literal( "ab" ) ).compareTo( new PeekNot( new Literal( "ab" ) ) ) );
		assertFalse( new PeekNot( new Literal( "ab" ) ).compareTo( new PeekNot( new Literal( "cd" ) ) ) );
		assertTrue( new PeekNot( new Literal( "ab" ) ).compareTo( new PeekNot( "ab" ) ) );

		ParserExpression parser = new OneOrMore( new Word( "a", "b" ) ).__add__( new PeekNot( new Word( "c", "d" ) ) );
		
		String[][] result1 = { { "ab" } };
		String[][] result2 = { { "ab", "ab" } };

		matchFailTestStringAndStream( parser, "" );
		matchFailTestStringAndStream( parser, "abcd" );
		matchFailTestStringAndStream( parser, "ababcd" );
		matchTestStringAndStream( parser, "ab", arrayToList2D( result1 ) );
		matchTestStringAndStream( parser, "abab", arrayToList2D( result2 ) );
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

	public void testLeftRecursion() throws Production.CannotOverwriteProductionExpressionException, ClassAlreadyDefinedException, InvalidFieldNameException
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

		matchTestStreamSX( parser, builder1.stream(), "{m=Tests.PatternMatch : [1 + [[(m Num x=2) * 3] * 4]]}" );
		matchTestStreamSX( parser, builder2.stream(), "{m=Tests.PatternMatch : [[1 * (m Num x=2)] + [3 * 4]]}" );
		matchTestStreamSX( parser, builder3.stream(), "{m=Tests.PatternMatch : [[[1 * (m Num x=2)] * 3] + 4]}" );
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
	
	
	public static void main(String[] args) throws ParserCoerceException, Production.CannotOverwriteProductionExpressionException
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

		
		DebugParseResult d = fieldAccessOrArrayAccess.debugParseStringChars( "this[i]" );
		new ParseViewFrame( d );
	}
}


/*
*/