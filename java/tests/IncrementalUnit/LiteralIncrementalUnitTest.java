//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package tests.IncrementalUnit;

import BritefuryJ.IncrementalUnit.LiteralUnit;


public class LiteralIncrementalUnitTest extends IncrementalUnitTest_base
{
	public void testLiteral()
	{
		LiteralUnit cell = new LiteralUnit( new Integer( 1 ) );
	
		assertEquals( cell.getValue(), new Integer( 1 ) );
		
		cell.setLiteralValue( new Integer( 20 ) );
	
		assertEquals( cell.getValue(), new Integer( 20 ) );
	}



	public void testListener()
	{
		assertEquals( getSignalCount( "changed" ), 0 );
		
		LiteralUnit cell = new LiteralUnit( new Integer( 1 ) );
		
		cell.addListener( makeListener( "" ) );
		
		cell.setLiteralValue( new Integer( 20 ) );
		
		assertEquals( getSignalCount( "changed" ), 1 );
	}
}
