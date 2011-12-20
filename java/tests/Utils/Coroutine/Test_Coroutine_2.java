//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package tests.Utils.Coroutine;

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
				
				x = Coroutine.yieldToParent( "1" ); // -> root
				assertEquals( "2", x );
				
				b.append( "I" );
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
				
				x = Coroutine.yieldToParent( "2" ); // -> A
				assertEquals( "3", x );
				
				b.append( "H" );
				
				x = coA.yieldTo( "2" );
				assertEquals( null, x );
				
				b.append( "J" );
			}
		};

		Runnable rC = new Runnable()
		{
			@Override
			public void run()
			{
				Object x;

				b.append( "C" );
				
				x = Coroutine.yieldToParent( "3" ); // -> B
				assertEquals( "0", x );
				
				b.append( "G" );
				
				x = coB.yieldTo( "3" );
				assertEquals( null, x );
				
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
		
		assertTrue( coA.isRunning() );
		assertTrue( coB.isRunning() );
		assertTrue( coC.isRunning() );
		assertFalse( coA.isFinished() );
		assertFalse( coB.isFinished() );
		assertFalse( coC.isFinished() );
		
		x = coC.yieldTo( "0" );
		assertEquals( null, x );
		b.append( "L" );

		assertEquals( "ABCDEFGHIJKL", b.toString() );
		assertFalse( coA.isRunning() );
		assertFalse( coB.isRunning() );
		assertFalse( coC.isRunning() );
		assertTrue( coA.isFinished() );
		assertTrue( coB.isFinished() );
		assertTrue( coC.isFinished() );
	}
}
