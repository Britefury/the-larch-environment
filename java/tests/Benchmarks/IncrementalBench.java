//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package tests.Benchmarks;

import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalValueMonitor;

public class IncrementalBench
{
	private static final int WARMUP = 10;
	private static final int REPEATS = 3000;
	private static final int MONITORS = 1000;
	
	private static IncrementalValueMonitor[] createValues(int N)
	{
		IncrementalValueMonitor[] values = new IncrementalValueMonitor[N];
		for (int i = 0; i < N; i++)
		{
			values[i] = new IncrementalValueMonitor();
		}
		return values;
	}

	private static IncrementalFunctionMonitor[] createFunctions(int N)
	{
		IncrementalFunctionMonitor[] functions = new IncrementalFunctionMonitor[N];
		for (int i = 0; i < N; i++)
		{
			functions[i] = new IncrementalFunctionMonitor();
		}
		return functions;
	}
	
	private static void changeValues(IncrementalValueMonitor[] values)
	{
		for (IncrementalValueMonitor value: values)
		{
			value.onChanged();
		}
	}

	private static void refreshFunctions(IncrementalValueMonitor[] values, IncrementalFunctionMonitor[] functions)
	{
		for (int i = 0; i < values.length; i++)
		{
			Object refreshState = functions[i].onRefreshBegin();
			values[i].onAccess();
			functions[i].onRefreshEnd( refreshState );
		}
	}

	public static void main(String[] args)
	{
//		IncrementalValueMonitor[] values = createValues(MONITORS);
//		IncrementalFunctionMonitor[] functions = createFunctions(MONITORS);
		
		for (int i = 0; i < WARMUP; i++)
		{
			IncrementalValueMonitor[] values = createValues(MONITORS);
			IncrementalFunctionMonitor[] functions = createFunctions(MONITORS);
			refreshFunctions(values, functions);
			changeValues(values);
		}
		
		long t1 = System.nanoTime();
		
		for (int i = 0; i < REPEATS; i++)
		{
			IncrementalValueMonitor[] values = createValues(MONITORS);
			IncrementalFunctionMonitor[] functions = createFunctions(MONITORS);
			refreshFunctions(values, functions);
			changeValues(values);
		}
		
		long t2 = System.nanoTime();
		
		System.out.println( "Building/Refreshing/changing " + MONITORS + " monitors, repeated " + REPEATS + " times took " + (t2-t1)*1.0e-9 + " seconds");
	}

}
