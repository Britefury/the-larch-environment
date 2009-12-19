//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package tests.Cell;

import java.util.HashMap;

import junit.framework.TestCase;
import BritefuryJ.Cell.CellInterface;
import BritefuryJ.Cell.CellListener;

public class CellTest_base extends TestCase
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
		

	
	protected CellListener makeListener(final String prefix)
	{
		final CellTest_base tester = this;

		CellListener listener = new CellListener()
		{
			public void onCellChanged(CellInterface cell)
			{
				tester.onSignal( prefix + "changed" );
			}
		};
		
		return listener;
	}
}
