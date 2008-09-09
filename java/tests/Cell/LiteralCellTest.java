//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package tests.Cell;

import BritefuryJ.Cell.CellListener;
import BritefuryJ.Cell.LiteralCell;


public class LiteralCellTest extends CellTest_base
{
	public void testValidity()
	{
		LiteralCell cell = new LiteralCell( new Integer( 1 ) );
		
		assertTrue( cell.isValid() );
	}



	public void testLiteral()
	{
		LiteralCell cell = new LiteralCell( new Integer( 1 ) );
	
		assertEquals( cell.getValue(), new Integer( 1 ) );
		
		cell.setLiteralValue( new Integer( 20 ) );
	
		assertEquals( cell.getValue(), new Integer( 20 ) );
	}



	public void testListener()
	{
		assertEquals( getSignalCount( "evaluator" ), 0 );
		assertEquals( getSignalCount( "changed" ), 0 );
		
		LiteralCell cell = new LiteralCell( new Integer( 1 ) );
		
		CellListener listener = makeListener( "" );
		
		cell.addListener( listener );
		
		cell.setLiteralValue( new Integer( 20 ) );
		
		assertEquals( getSignalCount( "evaluator" ), 1 );
		assertEquals( getSignalCount( "changed" ), 1 );
	}
}
