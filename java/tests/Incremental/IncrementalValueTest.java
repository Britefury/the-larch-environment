//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
