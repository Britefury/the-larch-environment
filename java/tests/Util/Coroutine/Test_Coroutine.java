//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package tests.Util.Coroutine;

import junit.framework.TestCase;
import BritefuryJ.Util.Coroutine.Coroutine;
import BritefuryJ.Util.Coroutine.RootCoroutine;

public class Test_Coroutine extends TestCase
{
	public void test_empty_Coroutine()
	{
		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
			}
		};
		
		Coroutine co = new Coroutine( run );
		
		co.yieldTo();
	}

	public void test_parent()
	{
		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
			}
		};
		
		Coroutine co = new Coroutine( run );
		
		co.yieldTo();
		
		assertSame( RootCoroutine.getRootCoroutine(), co.getParent() );
		assertSame( RootCoroutine.getRootCoroutine(), Coroutine.getCurrent() );
	}

	public void test_excpetion_passing()
	{
		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				throw new RuntimeException();
			}
		};
		
		Coroutine co = new Coroutine( run );
		
		boolean caught = false;
		try
		{
			co.yieldTo();
		}
		catch (RuntimeException e)
		{
			caught = true;
		}
		
		assertTrue( caught );
	}
	
	
	public void test_unfinished_Coroutine()
	{
		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				Coroutine.getCurrent().yieldToParent();
			}
		};
		
		Coroutine co = new Coroutine( run );
		
		co.yieldTo();
	}
	
	public void test_terminate()
	{
		final int x[] = new int[] { 0 };
		
		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				x[0] = 1;
				Coroutine.getCurrent().yieldToParent();
				x[0] = 2;
			}
		};
		
		Coroutine co = new Coroutine( run );
		
		assertFalse( co.hasStarted() );
		
		assertEquals( 0, x[0] );
		
		co.yieldTo();
		
		assertEquals( 1, x[0] );
		
		co.terminate();

		assertEquals( 1, x[0] );
		
		assertTrue( co.isFinished() );
	}
	
	public void test_serial_coroutines()
	{
		Runnable run1 = new Runnable()
		{
			@Override
			public void run()
			{
				Coroutine.getCurrent().yieldToParent();
				Coroutine.getCurrent().yieldToParent();
				throw new RuntimeException();
			}
		};
		
		Coroutine co1 = new Coroutine( run1 );
		
		co1.yieldTo();
		co1.yieldTo();
		

	
		Runnable run2 = new Runnable()
		{
			@Override
			public void run()
			{
				Coroutine.getCurrent().yieldToParent();
				Coroutine.getCurrent().yieldToParent();
				throw new RuntimeException();
			}
		};
		
		Coroutine co2 = new Coroutine( run2 );
		
		co2.yieldTo();
		co2.yieldTo();
	}
}