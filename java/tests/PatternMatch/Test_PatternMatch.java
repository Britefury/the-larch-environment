//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.PatternMatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import BritefuryJ.DocModel.DMModule;
import BritefuryJ.DocModel.DMModuleResolver;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMModule.ClassAlreadyDefinedException;
import BritefuryJ.DocModel.DMObjectClass.InvalidFieldNameException;
import BritefuryJ.PatternMatch.Action;
import BritefuryJ.PatternMatch.Anything;
import BritefuryJ.PatternMatch.BestChoice;
import BritefuryJ.PatternMatch.Choice;
import BritefuryJ.PatternMatch.Condition;
import BritefuryJ.PatternMatch.Forward;
import BritefuryJ.PatternMatch.ListNode;
import BritefuryJ.PatternMatch.Literal;
import BritefuryJ.PatternMatch.ObjectMatch;
import BritefuryJ.PatternMatch.OneOrMore;
import BritefuryJ.PatternMatch.MatchAction;
import BritefuryJ.PatternMatch.MatchCondition;
import BritefuryJ.PatternMatch.MatchExpression;
import BritefuryJ.PatternMatch.Peek;
import BritefuryJ.PatternMatch.PeekNot;
import BritefuryJ.PatternMatch.Production;
import BritefuryJ.PatternMatch.RegEx;
import BritefuryJ.PatternMatch.Sequence;
import BritefuryJ.PatternMatch.ZeroOrMore;

public class Test_PatternMatch extends PatternMatchTestCase
{
	static MatchExpression identifier = new RegEx( "[A-Za-z_][A-Za-z0-9_]*" );
	
	protected DMModule M;
	protected DMObjectClass A, Add, Sub, Mul;
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
		
		try
		{
			A = M.newClass( "A", new String[] { "x", "y" } );
			Add = M.newClass( "Add", new String[] { "a", "b" } );
			Sub = M.newClass( "Sub", new String[] { "a", "b" } );
			Mul = M.newClass( "Mul", new String[] { "a", "b" } );
		}
		catch (ClassAlreadyDefinedException e)
		{
			System.out.println( "Class already defined" );
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void tearDown()
	{
		M = null;
		A = null;
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
	
	
	public void testAnything()
	{
		// Test anything
		matchNodeTestSX( new Anything(), "abcxyz", "abcxyz" );
		matchNodeTestSX( new Anything(), "[abcxyz]", "[abcxyz]" );
		matchNodeTestSX( new Anything(), "{M=Tests.PatternMatch : (M A x=a y=b)}", "{M=Tests.PatternMatch : (M A x=a y=b)}" );
	}
	
	
	public void testLiteral()
	{
		// Test literal
		matchNodeTestSX( new Literal( "abcxyz" ), "abcxyz", "abcxyz" );
		matchNodeFailTestSX( new Literal( "abcxyz" ), "qwerty" );
		
		// Incomplete parse should result in failure
		matchNodeFailTestSX( new Literal( "abcxyz" ), "abcxyzpq" );

		matchNodeTestSX( new Literal( "abcxyz" ), "[abcxyz]", "abcxyz" );
		matchNodeFailTestSX( new Literal( "abcxyz" ), "{M=Tests.PatternMatch : (M A x=abcxyz y=b)}" );
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
		matchNodeTestSX( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ), "[abc_123]", "abc_123" );
		matchNodeFailTestSX( new RegEx( "[A-Za-z_][A-Za-z0-9_]*" ), "{M=Tests.PatternMatch : (M A x=abc_123 y=b)}" );
	}

	
	public void testAction()
	{
		MatchAction f = new MatchAction()
		{
			public Object invoke(Object input, Object value, Map<String, Object> bindings, Object arg)
			{
				String v = (String)value;
				return v + v;
			}
		};

		MatchAction g = new MatchAction()
		{
			public Object invoke(Object input, Object value, Map<String, Object> bindings, Object arg)
			{
				String v = (String)value;
				return v + v + v;
			}
		};

		MatchAction h = new MatchAction()
		{
			public Object invoke(Object input, Object value, Map<String, Object> bindings, Object arg)
			{
				return Arrays.asList( new Object[] { value, value, value } );
			}
		};

		assertTrue( new Action( "abc", f ).compareTo( new Action( "abc", f ) ) );
		assertFalse( new Action( "abc", f ).compareTo( new Action( "def", f ) ) );
		assertFalse( new Action( "abc", f ).compareTo( new Action( "abc", g ) ) );
		assertTrue( new Action( "abc", f ).compareTo( new Literal( "abc" ).action( f ) ) );
		
		MatchExpression parser1 = new Literal( "abc" ).bindTo( "x" ).action( f );
		matchNodeTestSX( parser1, "abc", "abcabc" );
		bindingsNodeTestSX( parser1, "abc", "[[x abc]]" );

	
		MatchExpression parser2 = new Literal( "abc" ).action( h );
		matchNodeTestSX( parser2, "abc", "[abc abc abc]" );

		
		MatchExpression parser3 = new ListNode( new Object[] { new Literal( "abc" ).mergeUpAction( h ) } );
		matchNodeTestSX( parser3, "[abc]", "[abc abc abc]" );

	
		MatchExpression parser4 = new Anything().action( h );
		matchNodeTestSX( parser4, "{M=Tests.PatternMatch : (M A x=abcxyz y=b)}", "{M=Tests.PatternMatch : [(M A x=abcxyz y=b) (M A x=abcxyz y=b) (M A x=abcxyz y=b)]}" );
	}

	
	public void testCondition()
	{
		MatchCondition f = new MatchCondition()
		{
			public boolean test(Object input, Object value, Map<String, Object> bindings, Object arg)
			{
				String v = (String)value;
				return v.startsWith( "hello" );
			}
		};

		MatchCondition g = new MatchCondition()
		{
			public boolean test(Object input, Object value, Map<String, Object> bindings, Object arg)
			{
				String v = (String)value;
				return v.startsWith( "there" );
			}
		};

		assertTrue( new Condition( "abc", f ).compareTo( new Condition( "abc", f ) ) );
		assertFalse( new Condition( "abc", f ).compareTo( new Condition( "def", f ) ) );
		assertFalse( new Condition( "abc", f ).compareTo( new Condition( "abc", g ) ) );
		assertTrue( new Condition( "abc", f ).compareTo( new Literal( "abc" ).condition( f ) ) );
		
		MatchExpression parser = identifier.bindTo( "x" ).condition( f );
		
		matchNodeTestSX( parser, "helloworld", "helloworld" );
		matchNodeFailTestSX( parser, "xabcdef" );
		bindingsNodeTestSX( parser, "helloworld", "[[x helloworld]]" );
	}
	
	
	public void testBind()
	{
		MatchExpression parser1 = identifier.bindTo(  "x" );
		
		matchNodeTestSX( parser1, "abc", "abc" );
		bindingsNodeTestSX( parser1, "abc", "[[x abc]]" );

	
		MatchExpression parser2 = identifier.bindTo( "x" ).bindTo( "y" );
		
		matchNodeTestSX( parser2, "abc", "abc" );
		bindingsNodeTestSX( parser2, "abc", "[[x abc] [y abc]]" );
	}
	
	
	public void testClearBindings()
	{
		MatchExpression parser1 = identifier.bindTo(  "x" ).clearBindings();
		
		matchNodeTestSX( parser1, "abc", "abc" );
		bindingsNodeTestSX( parser1, "abc", "[]" );
	}
	
	
	public void testListNode()
	{
		MatchExpression parser1 = new ListNode( new Object[] { new Literal( "abc" ) } );
		matchNodeTestSX( parser1, "[abc]", "[abc]" );
		matchNodeFailTestSX( parser1, "[abcde]" );
		matchNodeFailTestSX( parser1, "[abc de]" );

		MatchExpression parser2 = new ListNode( new Object[] { new Literal( "abc" ), new Literal( "def" ) } );
		matchNodeTestSX( parser2, "[abc def]", "[abc def]" );
		matchNodeFailTestSX( parser2, "[abcx def]" );
		matchNodeFailTestSX( parser2, "[abc defx]" );
		matchNodeFailTestSX( parser2, "[abcx defx]" );

		MatchExpression parser3 = new ListNode( new Object[] { new Literal( "abc" ), new ListNode( new Object[] { new Literal( "d" ), new Literal( "e" ) } ) } );
		matchNodeTestSX( parser3, "[abc [d e]]", "[abc [d e]]" );
		matchNodeFailTestSX( parser3, "[abc [de]]" );
		matchNodeFailTestSX( parser3, "[abc de]" );
		matchNodeFailTestSX( parser3, "[abc [dx e]]" );
		matchNodeFailTestSX( parser3, "[abc [d ex]]" );
		matchNodeFailTestSX( parser3, "[abc [dx ex]]" );

		MatchExpression parser4 = new ListNode( new Object[] { new Literal( "abc" ), new ListNode( new Object[] { new Literal( "d" ) } ).bindTo( "x" ) } );
		matchNodeTestSX( parser4, "[abc [d]]", "[abc [d]]" );
		matchNodeFailTestSX( parser4, "[abc d]" );
		bindingsNodeTestSX( parser4, "[abc [d]]", "[[x [d]]]" );

		MatchExpression parser5 = new ListNode( new Object[] { identifier.bindTo( "x" ), identifier.bindTo( "x" ) } );
		matchNodeTestSX( parser5, "[abc abc]", "[abc abc]" );
		bindingsNodeTestSX( parser5, "[abc abc]", "[[x abc]]" );
		matchNodeFailTestSX( parser5, "[abc d]" );

		MatchExpression parser6 = new ListNode( new Object[] { identifier.bindTo( "x" ), new ListNode( new Object[] { identifier.bindTo( "x" ) } ) } );
		matchNodeTestSX( parser6, "[abc [abc]]", "[abc [abc]]" );
		bindingsNodeTestSX( parser6, "[abc [abc]]", "[[x abc]]" );
		matchNodeFailTestSX( parser6, "[abc [d]]" );
	}

	public void testObjectNode() throws InvalidFieldNameException
	{
		MatchExpression parser1 = new ObjectMatch( A, new String[] { "x" }, new Object[] { new Literal( "abc" ) } );
		matchNodeTestSX( parser1, "{M=Tests.PatternMatch : (M A x=abc y=xyz)}", "{M=Tests.PatternMatch : (M A x=abc y=xyz)}" );
		matchNodeTestSX( parser1, "{M=Tests.PatternMatch : (M A x=abc y=pqr)}", "{M=Tests.PatternMatch : (M A x=abc y=pqr)}" );
		matchNodeFailTestSX( parser1, "{M=Tests.PatternMatch : (M A x=pqr y=xyz)}" );
		
		MatchExpression parser2 = new ObjectMatch( A, new String[] { "y" }, new Object[] { new Literal( "xyz" ) } );
		matchNodeTestSX( parser2, "{M=Tests.PatternMatch : (M A x=abc y=xyz)}", "{M=Tests.PatternMatch : (M A x=abc y=xyz)}" );
		matchNodeTestSX( parser2, "{M=Tests.PatternMatch : (M A x=pqr y=xyz)}", "{M=Tests.PatternMatch : (M A x=pqr y=xyz)}" );
		matchNodeFailTestSX( parser2, "{M=Tests.PatternMatch : (M A x=abc y=pqr)}" );
		
		MatchExpression parser3 = new ObjectMatch( A, new String[] { "x", "y" }, new Object[] { new Literal( "abc" ), new Literal( "xyz" ) } );
		matchNodeTestSX( parser3, "{M=Tests.PatternMatch : (M A x=abc y=xyz)}", "{M=Tests.PatternMatch : (M A x=abc y=xyz)}" );
		matchNodeFailTestSX( parser3, "{M=Tests.PatternMatch : (M A x=pqr y=xyz)}" );
		matchNodeFailTestSX( parser3, "{M=Tests.PatternMatch : (M A x=abc y=pqr)}" );
		
		MatchExpression parser4 = new ObjectMatch( A, new String[] { "x", "y" }, new Object[] { new Literal( "abc" ), new ListNode( new Object[] { new Literal( "d" ), new Literal( "e" ) } ) } );
		matchNodeTestSX( parser4, "{M=Tests.PatternMatch : (M A x=abc y=[d e])}", "{M=Tests.PatternMatch : (M A x=abc y=[d e])}" );
		matchNodeFailTestSX( parser4, "{M=Tests.PatternMatch : (M A x=pqr y=xyz)}" );
		matchNodeFailTestSX( parser4, "{M=Tests.PatternMatch : (M A x=abc y=[p q])}" );
		
		MatchExpression parser5 = new ObjectMatch( A, new String[] { "x", "y" }, new Object[] { new Literal( "abc" ), new ListNode( new Object[] { new Literal( "d" ) } ).bindTo( "x" ) } );
		matchNodeTestSX( parser5, "{M=Tests.PatternMatch : (M A x=abc y=[d])}", "{M=Tests.PatternMatch : (M A x=abc y=[d])}" );
		bindingsNodeTestSX( parser5, "{M=Tests.PatternMatch : (M A x=abc y=[d])}", "[[x [d]]]" );
		
		MatchExpression parser6 = new ObjectMatch( A, new String[] { "x", "y" }, new Object[] { identifier.bindTo( "x" ), identifier.bindTo( "x" ) } );
		matchNodeTestSX( parser6, "{M=Tests.PatternMatch : (M A x=abc y=abc)}", "{M=Tests.PatternMatch : (M A x=abc y=abc)}" );
		matchNodeTestSX( parser6, "{M=Tests.PatternMatch : (M A x=def y=def)}", "{M=Tests.PatternMatch : (M A x=def y=def)}" );
		bindingsNodeTestSX( parser6, "{M=Tests.PatternMatch : (M A x=abc y=abc)}", "[[x abc]]" );
		matchNodeFailTestSX( parser6, "{M=Tests.PatternMatch : (M A x=abc y=def)}" );
	}

	public void testChoice()
	{
		MatchExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new Choice( new Object[] { new Literal( "b" ), new Literal( "c" ) } ) } );
		matchNodeTestSX( parser1, "[a b]", "[a b]" );
		matchNodeTestSX( parser1, "[a c]", "[a c]" );
		matchNodeFailTestSX( parser1, "[a b c]" );
		matchNodeFailTestSX( parser1, "[a [b]]" );
		matchNodeFailTestSX( parser1, "[a [c]]" );
		matchNodeFailTestSX( parser1, "[a [b c]]" );

		MatchExpression parser2 = new ListNode( new Object[] { new Literal( "a" ), new Choice( new Object[] { new Sequence( new Object[] { new Literal( "b" ), new Literal( "c" ) } ).bindTo( "x" ), new Literal( "b" ).bindTo( "x" ) } ) } );
		matchNodeTestSX( parser2, "[a b]", "[a b]" );
		matchNodeTestSX( parser2, "[a b c]", "[a b c]" );
		matchNodeFailTestSX( parser2, "[a [b]]" );
		matchNodeFailTestSX( parser2, "[a [c]]" );
		matchNodeFailTestSX( parser2, "[a [b c]]" );
		bindingsNodeTestSX( parser2, "[a b]", "[[x b]]" );
		bindingsNodeTestSX( parser2, "[a b c]", "[[x [b c]]]" );
	}

	public void testBestChoice()
	{
		MatchExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new BestChoice( new Object[] { new Literal( "b" ).bindTo( "x" ), new Sequence( new Object[] { new Literal( "b" ), new Literal( "c" ) } ).bindTo( "x" ) } ) } );
		matchNodeTestSX( parser1, "[a b]", "[a b]" );
		matchNodeTestSX( parser1, "[a b c]", "[a b c]" );
		matchNodeFailTestSX( parser1, "[a [b]]" );
		matchNodeFailTestSX( parser1, "[a [c]]" );
		matchNodeFailTestSX( parser1, "[a [b c]]" );
		bindingsNodeTestSX( parser1, "[a b]", "[[x b]]" );
		bindingsNodeTestSX( parser1, "[a b c]", "[[x [b c]]]" );
	}

	public void testSequence()
	{
		MatchExpression parser1 = new ListNode( new Object[] { identifier, new Sequence( new Object[] { identifier, identifier.bindTo( "x" ) } ) } );
		matchNodeTestSX( parser1, "[a b c]", "[a b c]" );
		matchNodeFailTestSX( parser1, "[a [b c]]" );
		bindingsNodeTestSX( parser1, "[a b c]", "[[x c]]" );
	}

	public void testOptional()
	{
		MatchExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new Sequence( new Object[] { new Literal( "b" ).bindTo( "x" ).optional(), new Literal( "c" ) } ) } );
		matchNodeTestSX( parser1, "[a b c]", "[a b c]" );
		matchNodeTest( parser1, Arrays.asList( new Object[] { "a", "c" } ), Arrays.asList( new Object[] { "a", "c" } ) );
		matchNodeFailTestSX( parser1, "[a [b c]]" );
		bindingsNodeTestSX( parser1, "[a b c]", "[[x b]]" );
	}

	public void testZeroOrMore()
	{
		MatchExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new ZeroOrMore( new Literal( "b" ) ) } );
		matchNodeTestSX( parser1, "[a]", "[a]" );
		matchNodeTestSX( parser1, "[a b]", "[a b]" );
		matchNodeTestSX( parser1, "[a b b]", "[a b b]" );
		matchNodeTestSX( parser1, "[a b b b]", "[a b b b]" );

		MatchExpression parser2 = new ListNode( new Object[] { new Literal( "a" ), new ZeroOrMore( new ListNode( new Object[] { new Literal( "d" ), new Literal( "e" ) } ) ) } );
		matchNodeTestSX( parser2, "[a]", "[a]" );
		matchNodeTestSX( parser2, "[a [d e]]", "[a [d e]]" );
		matchNodeTestSX( parser2, "[a [d e] [d e]]", "[a [d e] [d e]]" );

		MatchExpression parser3 = new ListNode( new Object[] { new Literal( "a" ), new ZeroOrMore( new Sequence( new Object[] { new Literal( "d" ), new Literal( "e" ) } ) ) } );
		matchNodeTestSX( parser3, "[a]", "[a]" );
		matchNodeTestSX( parser3, "[a d e]", "[a d e]" );
		matchNodeTestSX( parser3, "[a d e d e]", "[a d e d e]" );

		MatchExpression parser4 = new ListNode( new Object[] { new Literal( "a" ), new ZeroOrMore( identifier.bindTo( "x" ) ), new Anything().zeroOrMore() } );
		matchNodeTestSX( parser4, "[a]", "[a]" );
		matchNodeTestSX( parser4, "[a b]", "[a b]" );
		matchNodeTestSX( parser4, "[a b b]", "[a b b]" );
		matchNodeTestSX( parser4, "[a b b c]", "[a b b c]" );
		bindingsNodeTestSX( parser4, "[a b]", "[[x b]]" );
		bindingsNodeTestSX( parser4, "[a b b]", "[[x b]]" );
		bindingsNodeTestSX( parser4, "[a b b c]", "[[x b]]" );
	}

	public void testOneOrMore()
	{
		MatchExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new OneOrMore( new Literal( "b" ) ) } );
		matchNodeFailTestSX( parser1, "[a]" );
		matchNodeTestSX( parser1, "[a b]", "[a b]" );
		matchNodeTestSX( parser1, "[a b b]", "[a b b]" );
		matchNodeTestSX( parser1, "[a b b b]", "[a b b b]" );

		MatchExpression parser2 = new ListNode( new Object[] { new Literal( "a" ), new OneOrMore( new ListNode( new Object[] { new Literal( "d" ), new Literal( "e" ) } ) ) } );
		matchNodeFailTestSX( parser2, "[a]" );
		matchNodeTestSX( parser2, "[a [d e]]", "[a [d e]]" );
		matchNodeTestSX( parser2, "[a [d e] [d e]]", "[a [d e] [d e]]" );

		MatchExpression parser3 = new ListNode( new Object[] { new Literal( "a" ), new OneOrMore( new Sequence( new Object[] { new Literal( "d" ), new Literal( "e" ) } ) ) } );
		matchNodeFailTestSX( parser3, "[a]" );
		matchNodeTestSX( parser3, "[a d e]", "[a d e]" );
		matchNodeTestSX( parser3, "[a d e d e]", "[a d e d e]" );

		MatchExpression parser4 = new ListNode( new Object[] { new Literal( "a" ), new OneOrMore( identifier.bindTo( "x" ) ), new Anything().zeroOrMore() } );
		matchNodeFailTestSX( parser4, "[a]" );
		matchNodeTestSX( parser4, "[a b]", "[a b]" );
		matchNodeTestSX( parser4, "[a b b]", "[a b b]" );
		matchNodeTestSX( parser4, "[a b b c]", "[a b b c]" );
		bindingsNodeTestSX( parser4, "[a b]", "[[x b]]" );
		bindingsNodeTestSX( parser4, "[a b b]", "[[x b]]" );
		bindingsNodeTestSX( parser4, "[a b b c]", "[[x b]]" );
	}

	public void testPeek()
	{
		MatchExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new Peek( new Literal( "b" ).bindTo( "x" ) ), new Anything() } );
		matchNodeFailTestSX( parser1, "[a]" );
		matchNodeTestSX( parser1, "[a b]", "[a b]" );
		matchNodeFailTestSX( parser1, "[a b b]" );
		bindingsNodeTestSX( parser1, "[a b]", "[[x b]]" );
	}

	public void testPeekNot()
	{
		MatchExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new PeekNot( new Literal( "b" ).bindTo( "x" ) ), new Anything() } );
		matchNodeFailTestSX( parser1, "[a]" );
		matchNodeFailTestSX( parser1, "[a b]" );
		matchNodeFailTestSX( parser1, "[a b b]" );
		matchNodeTestSX( parser1, "[a c]", "[a c]" );
		matchNodeTestSX( parser1, "[a [x y z]]", "[a [x y z]]" );
		bindingsNodeTestSX( parser1, "[a c]", "[]" );
	}

	public void testSuppress()
	{
		MatchExpression parser1 = new ListNode( new Object[] { new Literal( "a" ), new Sequence( new Object[] { new Literal( "b" ).bindTo( "x" ).suppress(), new Literal( "c" ) } ) } );
		matchNodeTestSX( parser1, "[a b c]", "[a c]" );
		matchNodeFailTestSX( parser1, "[a [b c]]" );
		bindingsNodeTestSX( parser1, "[a b c]", "[[x b]]" );
	}
	
	
	
	public void testLerpRefactor()
	{
		MatchAction lerpAction = new MatchAction()
		{
			public Object invoke(Object input, Object x, Map<String, Object> bindings, Object arg)
			{
				return deepArrayToList( new Object[] { "+", bindings.get( "a" ), new Object[] { "*", new Object[] { "-", bindings.get( "b" ), bindings.get( "a" ) }, bindings.get( "t" ) } } );
			}
		};
		
		MatchExpression oneMinusT = MatchExpression.toMatchExpression( new Object[] { "-", "1.0", new Anything().bindTo( "t" ) } );
		bindingsNodeTestSX( oneMinusT, "[- 1.0 x]", "[[t x]]" );

		MatchExpression bTimesT = MatchExpression.toMatchExpression( new Object[] { "*", new Anything().bindTo( "b" ), new Anything().bindTo( "t" ) } );
		bindingsNodeTestSX( bTimesT, "[* q x]", "[[b q] [t x]]" );

		MatchExpression aTimesOneMinusT = MatchExpression.toMatchExpression( new Object[] { "*", new Anything().bindTo( "a" ), oneMinusT } );
		bindingsNodeTestSX( aTimesOneMinusT, "[* p [- 1.0 x]]", "[[a p] [t x]]" );
		
		MatchExpression lerp = MatchExpression.toMatchExpression( new Object[] { "+", aTimesOneMinusT, bTimesT } );
		bindingsNodeTestSX( lerp, "[+ [* p [- 1.0 x]] [* q x]]", "[[a p] [b q] [t x]]" );

		MatchExpression lerpRefactor = lerp.action( lerpAction );
		matchNodeTestSX( lerpRefactor, "[+ [* p [- 1.0 x]] [* q x]]", "[+ p [* [- q p] x]]" );
	}

	
	
	public void testLerpRefactorObject() throws InvalidFieldNameException
	{
		MatchAction lerpAction = new MatchAction()
		{
			public Object invoke(Object input, Object x, Map<String, Object> bindings, Object arg)
			{
				return Add.newInstance( new Object[] { bindings.get( "a" ), Mul.newInstance( new Object[] {  Sub.newInstance( new Object[] { bindings.get( "b" ), bindings.get( "a" ) } ), bindings.get( "t" ) } ) } );
			}
		};
		
		MatchExpression oneMinusT = new ObjectMatch( Sub, new Object[] { "1.0", new Anything().bindTo( "t" ) } );
		bindingsNodeTestSX( oneMinusT, "{M=Tests.PatternMatch : (M Sub a=1.0 b=x)}", "[[t x]]" );

		MatchExpression bTimesT = new ObjectMatch( Mul, new Object[] { new Anything().bindTo( "b" ), new Anything().bindTo( "t" ) } );
		bindingsNodeTestSX( bTimesT, "{M=Tests.PatternMatch : (M Mul a=q b=x)}", "[[b q] [t x]]" );

		MatchExpression aTimesOneMinusT = new ObjectMatch( Mul, new Object[] { new Anything().bindTo( "a" ), oneMinusT } );
		bindingsNodeTestSX( aTimesOneMinusT, "{M=Tests.PatternMatch : (M Mul a=p b=(M Sub a=1.0 b=x))}", "[[a p] [t x]]" );
		
		MatchExpression lerp = new ObjectMatch( Add, new Object[] { aTimesOneMinusT, bTimesT } );
		bindingsNodeTestSX( lerp, "{M=Tests.PatternMatch : (M Add a=(M Mul a=p b=(M Sub a=1.0 b=x)) b=(M Mul a=q b=x))}", "[[a p] [b q] [t x]]" );

		MatchExpression lerpRefactor = lerp.action( lerpAction );
		matchNodeTestSX( lerpRefactor, "{M=Tests.PatternMatch : (M Add a=(M Mul a=p b=(M Sub a=1.0 b=x)) b=(M Mul a=q b=x))}", "{M=Tests.PatternMatch : (M Add a=p b=(M Mul a=(M Sub a=q b=p) b=x))}" );
	}

	
	
	private static class MethodCallRefactorHelper
	{
		static MatchExpression getAttr(MatchExpression target, MatchExpression name)
		{
			return MatchExpression.toMatchExpression( new Object[] { "getAttr", target, name } );
		}
		
		static MatchExpression call(MatchExpression target, MatchExpression params)
		{
			return MatchExpression.toMatchExpression( new Object[] { "call", target, params } );
		}

		static MatchExpression methodCall(MatchExpression target, MatchExpression name, MatchExpression params)
		{
			return call( getAttr( target, name ), params );
		}
	}


	public void testMethodCallRefactor()
	{
		MatchAction methodCallRefactorAction = new MatchAction()
		{
			public Object invoke(Object input, Object x, Map<String, Object> bindings, Object arg)
			{
				return deepArrayToList( new Object[] { "invokeMethod", bindings.get( "target" ), bindings.get( "name" ), bindings.get( "params" ) } );
			}
		};
		
		MatchExpression load = MatchExpression.toMatchExpression( new Object[] { "load", new Anything() } );
		MatchExpression params = MatchExpression.toMatchExpression( new Object[] { "params" } );
		
		Forward expression = new Forward();
		MatchExpression methodCall = new Production( MethodCallRefactorHelper.methodCall( expression.bindTo( "target" ), identifier.bindTo( "name" ), params.bindTo( "params" ) ).action( methodCallRefactorAction ) );
		MatchExpression call = new Production( MethodCallRefactorHelper.call( expression, params ) );
		MatchExpression getAttr = new Production( MethodCallRefactorHelper.getAttr( expression, identifier ) );
		expression.setExpression( new Production( new Choice( new Object[] { methodCall, call, getAttr, load } ) ) );
		
		matchNodeTestSX( expression, "[load x]", "[load x]" );
		matchNodeTestSX( expression, "[call [load x] [params]]", "[call [load x] [params]]" );
		matchNodeTestSX( expression, "[getAttr [load x] blah]", "[getAttr [load x] blah]" );
		matchNodeTestSX( expression, "[call [getAttr [load x] blah] [params]]", "[invokeMethod [load x] blah [params]]" );
		matchNodeTestSX( expression, "[call [getAttr [getAttr [load y] foo] blah] [params]]", "[invokeMethod [getAttr [load y] foo] blah [params]]" );
		matchNodeTestSX( expression, "[call [getAttr [call [load y] [params]] blah] [params]]", "[invokeMethod [call [load y] [params]] blah [params]]" );
		matchNodeTestSX( expression, "[call [getAttr [call [getAttr [load x] foo] [params]] blah] [params]]", "[invokeMethod [invokeMethod [load x] foo [params]] blah [params]]" );
		matchNodeTestSX( expression, "[call [getAttr [call [call [getAttr [load x] blah] [params]] [params]] blah] [params]]", "[invokeMethod [call [invokeMethod [load x] blah [params]] [params]] blah [params]]" );
		matchNodeTestSX( expression, "[call [getAttr [getAttr [call [getAttr [load x] blah] [params]] foo] blah] [params]]", "[invokeMethod [getAttr [invokeMethod [load x] blah [params]] foo] blah [params]]" );
	}
}
