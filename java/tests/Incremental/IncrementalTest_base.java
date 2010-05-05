//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package tests.Incremental;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import junit.framework.TestCase;
import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;

public class IncrementalTest_base extends TestCase
{
	private HashMap<String, Integer> sigs;
	
	
	protected void setUp()
	{
		sigs = new HashMap<String, Integer>();
	}
		
	protected void tearDown()
	{
		sigs = null;
	}
	
	protected void onSignal(String name)
	{
		Integer value = sigs.get( name );
		
		if ( value == null )
		{
			value = new Integer( 1 );
		}
		else
		{
			value = new Integer( value.intValue() + 1 );
		}
		
		sigs.put( name, value );
	}
	
	protected int getSignalCount(String name)
	{
		Integer value = sigs.get( name );

		if ( value != null )
		{
			return value.intValue();
		}
		else
		{
			return 0;
		}
	}
		

	
	protected IncrementalMonitorListener makeListener(final String prefix)
	{
		final IncrementalTest_base tester = this;

		IncrementalMonitorListener listener = new IncrementalMonitorListener()
		{
			public void onIncrementalMonitorChanged(IncrementalMonitor inc)
			{
				tester.onSignal( prefix + "changed" );
			}
		};
		
		return listener;
	}
	
	
	protected void checkOutgoingDependencies(IncrementalMonitor inc, IncrementalFunctionMonitor expected[])
	{
		HashSet<IncrementalFunctionMonitor> expectedSet = new HashSet<IncrementalFunctionMonitor>();
		expectedSet.addAll( Arrays.asList( expected ) );
		assertEquals( expectedSet, inc.getOutgoingDependecies() );
	}

	protected void checkIncomingDependencies(IncrementalFunctionMonitor inc, IncrementalMonitor expected[])
	{
		HashSet<IncrementalMonitor> expectedSet = new HashSet<IncrementalMonitor>();
		expectedSet.addAll( Arrays.asList( expected ) );
		assertEquals( expectedSet, inc.getIncomingDependencies() );
	}
}
