//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.Incremental;

import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalValueMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;

public class IncrementalFunctionTest extends IncrementalTest_base
{
	public void testListener()
	{
		assertEquals( getSignalCount( "changed" ), 0 );
		
		IncrementalFunctionMonitor inc = new IncrementalFunctionMonitor();
		
		IncrementalMonitorListener listener = makeListener( "" );
		
		inc.addListener( listener );
		
		inc.onChanged();
		assertEquals( 1, getSignalCount( "changed" ) );

		inc.onChanged();
		assertEquals( 1, getSignalCount( "changed" ) );

		Object refreshState = inc.onRefreshBegin();
		inc.onRefreshEnd( refreshState );
		inc.onChanged();
		assertEquals( 2, getSignalCount( "changed" ) );
	}

	
	
	
	
	public void testChain()
	{
		IncrementalFunctionMonitor inc1 = new IncrementalFunctionMonitor(), inc2 = new IncrementalFunctionMonitor(), inc3 = new IncrementalFunctionMonitor(), inc4 = new IncrementalFunctionMonitor();
		
		inc1.addListener( makeListener( "1") );
		inc2.addListener( makeListener( "2") );
		inc3.addListener( makeListener( "3") );
		inc4.addListener( makeListener( "4") );
		
		Object rs1, rs2, rs3, rs4;
		
		rs1 = inc1.onRefreshBegin();
		inc1.onRefreshEnd( rs1 );
		inc1.onChanged();
		assertEquals( 1, getSignalCount( "1changed" ) );

		rs2 = inc2.onRefreshBegin();
		rs1 = inc1.onRefreshBegin();
		inc1.onRefreshEnd( rs1 );
		inc1.onAccess();
		inc2.onRefreshEnd( rs2 );
		
		checkOutgoingDependencies( inc1, new IncrementalFunctionMonitor[] { inc2 } );
		checkIncomingDependencies( inc2, new IncrementalFunctionMonitor[] { inc1 } );
		
		rs3 = inc3.onRefreshBegin();
		inc2.onAccess();
		inc3.onRefreshEnd( rs3 );

		checkOutgoingDependencies( inc2, new IncrementalFunctionMonitor[] { inc3 } );
		checkIncomingDependencies( inc3, new IncrementalFunctionMonitor[] { inc2 } );
		
		rs4 = inc4.onRefreshBegin();
		inc2.onAccess();
		inc4.onRefreshEnd( rs4 );

		checkOutgoingDependencies( inc2, new IncrementalFunctionMonitor[] { inc3, inc4 } );
		checkIncomingDependencies( inc4, new IncrementalFunctionMonitor[] { inc2 } );
		
		
		inc1.onChanged();
		assertEquals( 2, getSignalCount( "1changed" ) );
		assertEquals( 1, getSignalCount( "2changed" ) );
		assertEquals( 1, getSignalCount( "3changed" ) );
		assertEquals( 1, getSignalCount( "4changed" ) );

		rs1 = inc1.onRefreshBegin();
		inc1.onRefreshEnd( rs1 );
		inc1.onChanged();
		assertEquals( 3, getSignalCount( "1changed" ) );
		assertEquals( 1, getSignalCount( "2changed" ) );
		assertEquals( 1, getSignalCount( "3changed" ) );
		assertEquals( 1, getSignalCount( "4changed" ) );

		rs4 = inc4.onRefreshBegin();
		rs2 = inc2.onRefreshBegin();
		rs1 = inc1.onRefreshBegin();
		inc1.onRefreshEnd( rs1 );
		inc1.onAccess();
		inc2.onRefreshEnd( rs2 );
		inc2.onAccess();
		inc4.onRefreshEnd( rs4 );
		rs3 = inc3.onRefreshBegin();
		inc2.onAccess();
		inc3.onRefreshEnd( rs3 );

		checkOutgoingDependencies( inc1, new IncrementalFunctionMonitor[] { inc2 } );
		checkOutgoingDependencies( inc2, new IncrementalFunctionMonitor[] { inc3, inc4 } );
		checkOutgoingDependencies( inc3, new IncrementalFunctionMonitor[] {} );
		checkOutgoingDependencies( inc4, new IncrementalFunctionMonitor[] {} );
		checkIncomingDependencies( inc1, new IncrementalFunctionMonitor[] {} );
		checkIncomingDependencies( inc2, new IncrementalFunctionMonitor[] { inc1 } );
		checkIncomingDependencies( inc3, new IncrementalFunctionMonitor[] { inc2 } );
		checkIncomingDependencies( inc4, new IncrementalFunctionMonitor[] { inc2 } );

		inc1.onChanged();
		assertEquals( 4, getSignalCount( "1changed" ) );
		assertEquals( 2, getSignalCount( "2changed" ) );
		assertEquals( 2, getSignalCount( "3changed" ) );
		assertEquals( 2, getSignalCount( "4changed" ) );
	}



	public void testBlockAccessTracking()
	{
		IncrementalFunctionMonitor inc1 = new IncrementalFunctionMonitor(), inc2 = new IncrementalFunctionMonitor(), inc3 = new IncrementalFunctionMonitor(), inc4 = new IncrementalFunctionMonitor();
		
		inc1.addListener( makeListener( "1") );
		inc2.addListener( makeListener( "2") );
		inc3.addListener( makeListener( "3") );
		inc4.addListener( makeListener( "4") );
		
		Object rs1, rs2, rs3, rs4;
		

		rs4 = inc4.onRefreshBegin();
		rs2 = inc2.onRefreshBegin();
		rs1 = inc1.onRefreshBegin();
		inc1.onRefreshEnd( rs1 );
		inc1.onAccess();
		inc2.onRefreshEnd( rs2 );
		inc2.onAccess();
		inc4.onRefreshEnd( rs4 );
		rs3 = inc3.onRefreshBegin();
		IncrementalFunctionMonitor f = IncrementalValueMonitor.blockAccessTracking();
		inc2.onAccess();
		IncrementalValueMonitor.unblockAccessTracking( f );
		inc3.onRefreshEnd( rs3 );
		
		checkOutgoingDependencies( inc1, new IncrementalFunctionMonitor[] { inc2 } );
		checkOutgoingDependencies( inc2, new IncrementalFunctionMonitor[] { inc4 } );
		checkOutgoingDependencies( inc3, new IncrementalFunctionMonitor[] {} );
		checkOutgoingDependencies( inc4, new IncrementalFunctionMonitor[] {} );
		checkIncomingDependencies( inc1, new IncrementalFunctionMonitor[] {} );
		checkIncomingDependencies( inc2, new IncrementalFunctionMonitor[] { inc1 } );
		checkIncomingDependencies( inc3, new IncrementalFunctionMonitor[] {} );
		checkIncomingDependencies( inc4, new IncrementalFunctionMonitor[] { inc2 } );

		inc1.onChanged();
		assertEquals( 1, getSignalCount( "1changed" ) );
		assertEquals( 1, getSignalCount( "2changed" ) );
		assertEquals( 0, getSignalCount( "3changed" ) );
		assertEquals( 1, getSignalCount( "4changed" ) );
	}
}
