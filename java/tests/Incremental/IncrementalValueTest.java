//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package tests.Incremental;

import BritefuryJ.Incremental.IncrementalValueMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;


public class IncrementalValueTest extends IncrementalTest_base
{
	public void testListener()
	{
		assertEquals( getSignalCount( "changed" ), 0 );
		
		IncrementalValueMonitor inc = new IncrementalValueMonitor();
		
		IncrementalMonitorListener listener = makeListener( "" );
		
		inc.addListener( listener );
		
		inc.onChanged();
		assertEquals( 1, getSignalCount( "changed" ) );

		inc.onChanged();
		assertEquals( 1, getSignalCount( "changed" ) );

		inc.onAccess();
		inc.onChanged();
		assertEquals( 2, getSignalCount( "changed" ) );
	}
}
