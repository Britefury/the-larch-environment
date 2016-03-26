//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.IncrementalUnit;

import BritefuryJ.Live.LiveValue;


public class LiteralIncrementalUnitTest extends IncrementalUnitTest_base
{
	public void testLiteral()
	{
		LiveValue cell = new LiveValue( 1 );
	
		assertEquals( cell.getValue(), 1 );
		
		cell.setLiteralValue( new Integer( 20 ) );
	
		assertEquals( cell.getValue(), 20 );
	}



	public void testListener()
	{
		assertEquals( getSignalCount( "changed" ), 0 );
		
		LiveValue cell = new LiveValue( 1 );
		
		cell.addListener( makeListener( "" ) );
		
		cell.setLiteralValue( new Integer( 20 ) );
		
		assertEquals( getSignalCount( "changed" ), 1 );
	}
}
