//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package tests.Incremental;

import BritefuryJ.Incremental.IncrementalFunction;
import BritefuryJ.Incremental.IncrementalValue;
import BritefuryJ.Incremental.IncrementalValueListener;

public class IncrementalFunctionTest extends IncrementalTest_base
{
	public void testListener()
	{
		assertEquals( getSignalCount( "changed" ), 0 );
		
		IncrementalFunction inc = new IncrementalFunction();
		
		IncrementalValueListener listener = makeListener( "" );
		
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
		IncrementalFunction inc1 = new IncrementalFunction(), inc2 = new IncrementalFunction(), inc3 = new IncrementalFunction(), inc4 = new IncrementalFunction();
		
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
		
		checkOutgoingDependencies( inc1, new IncrementalFunction[] { inc2 } );
		checkIncomingDependencies( inc2, new IncrementalFunction[] { inc1 } );
		
		rs3 = inc3.onRefreshBegin();
		inc2.onAccess();
		inc3.onRefreshEnd( rs3 );

		checkOutgoingDependencies( inc2, new IncrementalFunction[] { inc3 } );
		checkIncomingDependencies( inc3, new IncrementalFunction[] { inc2 } );
		
		rs4 = inc4.onRefreshBegin();
		inc2.onAccess();
		inc4.onRefreshEnd( rs4 );

		checkOutgoingDependencies( inc2, new IncrementalFunction[] { inc3, inc4 } );
		checkIncomingDependencies( inc4, new IncrementalFunction[] { inc2 } );
		
		
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

		checkOutgoingDependencies( inc1, new IncrementalFunction[] { inc2 } );
		checkOutgoingDependencies( inc2, new IncrementalFunction[] { inc3, inc4 } );
		checkOutgoingDependencies( inc3, new IncrementalFunction[] {} );
		checkOutgoingDependencies( inc4, new IncrementalFunction[] {} );
		checkIncomingDependencies( inc1, new IncrementalFunction[] {} );
		checkIncomingDependencies( inc2, new IncrementalFunction[] { inc1 } );
		checkIncomingDependencies( inc3, new IncrementalFunction[] { inc2 } );
		checkIncomingDependencies( inc4, new IncrementalFunction[] { inc2 } );

		inc1.onChanged();
		assertEquals( 4, getSignalCount( "1changed" ) );
		assertEquals( 2, getSignalCount( "2changed" ) );
		assertEquals( 2, getSignalCount( "3changed" ) );
		assertEquals( 2, getSignalCount( "4changed" ) );
	}



	public void testBlockAccessTracking()
	{
		IncrementalFunction inc1 = new IncrementalFunction(), inc2 = new IncrementalFunction(), inc3 = new IncrementalFunction(), inc4 = new IncrementalFunction();
		
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
		IncrementalFunction f = IncrementalValue.blockAccessTracking();
		inc2.onAccess();
		IncrementalValue.unblockAccessTracking( f );
		inc3.onRefreshEnd( rs3 );
		
		checkOutgoingDependencies( inc1, new IncrementalFunction[] { inc2 } );
		checkOutgoingDependencies( inc2, new IncrementalFunction[] { inc4 } );
		checkOutgoingDependencies( inc3, new IncrementalFunction[] {} );
		checkOutgoingDependencies( inc4, new IncrementalFunction[] {} );
		checkIncomingDependencies( inc1, new IncrementalFunction[] {} );
		checkIncomingDependencies( inc2, new IncrementalFunction[] { inc1 } );
		checkIncomingDependencies( inc3, new IncrementalFunction[] {} );
		checkIncomingDependencies( inc4, new IncrementalFunction[] { inc2 } );

		inc1.onChanged();
		assertEquals( 1, getSignalCount( "1changed" ) );
		assertEquals( 1, getSignalCount( "2changed" ) );
		assertEquals( 0, getSignalCount( "3changed" ) );
		assertEquals( 1, getSignalCount( "4changed" ) );
	}
}
