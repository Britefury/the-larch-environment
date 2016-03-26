//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.Util.Coroutine;

import BritefuryJ.Util.Coroutine.Coroutine;
import junit.framework.TestCase;

public class Test_Coroutine_2 extends TestCase
{
	private StringBuilder b;
	private Coroutine coA, coB, coC;
	
	
	public void setUp()
	{
		Runnable rA = new Runnable()
		{
			@Override
			public void run()
			{
				Object x;
				
				b.append( "A" );
				
				x = coB.yieldTo( "1" );
				assertEquals( "2", x );
				
				b.append( "E" );
				
				x = coA.yieldToParent( "1" ); // -> root
				assertEquals( "2", x );
				
				b.append( "I" );
				
				x = coB.yieldTo( "1" );
				assertEquals( "0", x );
			}
		};

		Runnable rB = new Runnable()
		{
			@Override
			public void run()
			{
				Object x;

				b.append( "B" );
				
				x = coC.yieldTo( "2" );
				assertEquals( "3", x );
				
				b.append( "D" );
				
				x = coA.yieldTo( "2" ); // -> A
				assertEquals( "3", x );
				
				b.append( "H" );
				
				x = coA.yieldTo( "2" );
				assertEquals( "1", x );
				
				b.append( "J" );
				
				x = coC.yieldTo( "2" );
				assertEquals( "0", x );
			}
		};

		Runnable rC = new Runnable()
		{
			@Override
			public void run()
			{
				Object x;

				b.append( "C" );
				
				x = coB.yieldTo( "3" ); // -> B
				assertEquals( "0", x );
				
				b.append( "G" );
				
				x = coB.yieldTo( "3" );
				assertEquals( "2", x );
				
				b.append( "K" );
			}
		};
		
		b = new StringBuilder();
		coA = new Coroutine( rA, "A" );
		coB = new Coroutine( rB, "B" );
		coC = new Coroutine( rC, "C" );
	}
	
	public void tearDown()
	{
		b = null;
		coA = coB = coC = null;
	}
	
	
	
	public void test_2()
	{
		Object x;
		x = coA.yieldTo( "0" );
		assertEquals( "1", x );
		b.append( "F" );
		
		assertTrue( coA.hasStarted() );
		assertTrue( coB.hasStarted() );
		assertTrue( coC.hasStarted() );
		assertFalse( coA.isFinished() );
		assertFalse( coB.isFinished() );
		assertFalse( coC.isFinished() );
		
		x = coC.yieldTo( "0" );
		assertEquals( null, x );
		b.append( "L" );

		assertEquals( "ABCDEFGHIJKL", b.toString() );
		assertTrue( coA.hasStarted() );
		assertTrue( coB.hasStarted() );
		assertTrue( coC.hasStarted() );
		assertFalse( coA.isFinished() );
		assertFalse( coB.isFinished() );
		assertTrue( coC.isFinished() );
		
		x = coA.yieldTo( "0" );
		assertEquals( null, x );

		assertTrue( coA.isFinished() );
		assertFalse( coB.isFinished() );
		assertTrue( coC.isFinished() );
		
		x = coB.yieldTo( "0" );
		assertEquals( null, x );

		assertTrue( coA.isFinished() );
		assertTrue( coB.isFinished() );
		assertTrue( coC.isFinished() );
	}
}
