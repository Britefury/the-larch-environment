//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package tests.Utils.Coroutine;

import junit.framework.TestCase;
import BritefuryJ.Util.Coroutine.Coroutine;

public class Test_Coroutine_1 extends TestCase
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
				b.append( "A" );
				
				coB.yieldTo();
				
				b.append( "G" );
				
				coC.yieldTo();
				
				b.append( "I" );
				
				coC.yieldTo();
				
				b.append( "M" );
				
				coB.yieldTo();
				
				b.append( "Q" );
			}
		};

		Runnable rB = new Runnable()
		{
			@Override
			public void run()
			{
				b.append( "B" );
				
				coC.yieldTo();
				
				b.append( "D" );
				
				coC.yieldTo();

				b.append( "F" );
				
				Coroutine.yieldToParent();
				
				b.append( "K" );
				
				Coroutine.yieldToParent();
				
				b.append( "N" );
				
				coC.yieldTo();
				
				b.append( "P" );
			}
		};

		Runnable rC = new Runnable()
		{
			@Override
			public void run()
			{
				b.append( "C" );
				
				Coroutine.yieldToParent();
				
				b.append( "E" );
				
				Coroutine.yieldToParent();

				b.append( "H" );
				
				Coroutine.yieldToParent();

				b.append( "J" );
				
				coB.yieldTo();

				b.append( "L" );
				
				Coroutine.yieldToParent();
				
				b.append( "O" );
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
	
	
	
	public void test_nothing()
	{
		assertEquals( "", b.toString() );
		assertFalse( coA.isRunning() );
		assertFalse( coB.isRunning() );
		assertFalse( coC.isRunning() );
	}

	public void test_startAtA()
	{
		coA.yieldTo();
		assertEquals( "ABCDEFGHIJKLMNOPQ", b.toString() );
		assertFalse( coA.isRunning() );
		assertFalse( coB.isRunning() );
		assertFalse( coC.isRunning() );
		assertTrue( coA.isFinished() );
		assertTrue( coB.isFinished() );
		assertTrue( coC.isFinished() );
	}
}
