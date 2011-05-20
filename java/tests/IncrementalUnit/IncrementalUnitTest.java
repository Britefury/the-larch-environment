//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package tests.IncrementalUnit;

import BritefuryJ.IncrementalUnit.Unit;
import BritefuryJ.IncrementalUnit.UnitEvaluator;

public class IncrementalUnitTest extends IncrementalUnitTest_base
{
	public void testLiteral()
	{
		Unit cell = new Unit();
		
		cell.setLiteralValue( new Integer( 1 ) );
	
		assertEquals( cell.getValue(), 1 );
		
		cell.setLiteralValue( new Integer( 20 ) );
	
		assertEquals( cell.getValue(), 20 );
	}



	public void testListener()
	{
		assertEquals( getSignalCount( "changed" ), 0 );
		
		Unit cell = new Unit();
		
		cell.setLiteralValue( new Integer( 1 ) );
		assertEquals( cell.getValue(), 1 );
		
		cell.addListener( makeListener( "" ) );
		
		cell.setLiteralValue( new Integer( 20 ) );
		assertEquals( cell.getValue(), 20 );
		
		assertEquals( getSignalCount( "changed" ), 1 );
	}
	
	
	
	public void testFunction()
	{
		UnitEvaluator evaluator = new UnitEvaluator()
		{
			public Object evaluate()
			{
				return 20;
			}
		};
		
		
		assertEquals( getSignalCount( "changed" ), 0 );

		Unit cell = new Unit();

		cell.setLiteralValue( new Integer( 1 ) );
		assertEquals( cell.getValue(), 1 );
		
		cell.addListener( makeListener( "" ) );
		
		cell.setEvaluator( evaluator );
		assertEquals( cell.getValue(), 20 );
		
		assertEquals( getSignalCount( "changed" ), 1 );
	}
	
	
	public void testChain()
	{
		final Unit cell1 = new Unit();
		final Unit cell2 = new Unit();
		
		UnitEvaluator evaluator = new UnitEvaluator()
		{
			public Object evaluate()
			{
				return ( (Integer)cell1.getValue() ).intValue() * 3;
			}
		};
		
		
		cell1.setLiteralValue( new Integer( 1 ) );
		cell2.setEvaluator( evaluator );
		
		assertEquals( cell1.getValue(), 1 );
		assertEquals( cell2.getValue(), 3 );
		
		cell1.setLiteralValue( new Integer( 12 ) );

		assertEquals( cell1.getValue(), 12 );
		assertEquals( cell2.getValue(), 36 );
	}
	
	
	public void testValueCache()
	{
		final int[] callCount = { 0 };
		final Unit cell1 = new Unit();
		final Unit cell2 = new Unit();
		
		UnitEvaluator evaluator = new UnitEvaluator()
		{
			public Object evaluate()
			{
				callCount[0]++;
				return ( (Integer)cell1.getValue() ).intValue() * 3;
			}
		};
		
		
		cell1.setLiteralValue( new Integer( 1 ) );

		cell2.setEvaluator( evaluator );
		
		assertEquals( callCount[0], 0 );
		assertEquals( cell1.getValue(), 1 );
		assertEquals( cell2.getValue(), 3 );
		assertEquals( callCount[0], 1 );
		assertEquals( cell2.getValue(), 3 );
		assertEquals( callCount[0], 1 );
		
		cell1.setLiteralValue( new Integer( 12 ) );

		assertEquals( cell2.getValue(), 36 );
		assertEquals( callCount[0], 2 );
		assertEquals( cell2.getValue(), 36 );
		assertEquals( callCount[0], 2 );
	}



	public void testListenerWithChain()
	{
		assertEquals( getSignalCount( "1changed" ), 0 );
		assertEquals( getSignalCount( "3changed" ), 0 );
		
		final Unit cell1 = new Unit();
		final Unit cell2 = new Unit();
		final Unit cell3 = new Unit();
		
		UnitEvaluator evaluator2 = new UnitEvaluator()
		{
			public Object evaluate()
			{
				return ( (Integer)cell1.getValue() ).intValue() * 3;
			}
		};
		
		UnitEvaluator evaluator3 = new UnitEvaluator()
		{
			public Object evaluate()
			{
				return ( (Integer)cell2.getValue() ).intValue() * 2;
			}
		};
		
		
		cell1.setLiteralValue( new Integer( 1 ) );
		cell2.setEvaluator( evaluator2 );
		cell3.setEvaluator( evaluator3 );

		cell1.addListener( makeListener( "1" ) );
		cell3.addListener( makeListener( "3" ) );
		
		assertEquals( cell1.getValue(), 1 );
		assertEquals( cell2.getValue(), 3 );
		assertEquals( cell3.getValue(), 6 );

		assertEquals( getSignalCount( "1changed" ), 0 );
		assertEquals( getSignalCount( "3changed" ), 0 );

		cell1.setLiteralValue( new Integer( 12 ) );

		assertEquals( cell1.getValue(), 12 );
		assertEquals( cell2.getValue(), 36 );
		assertEquals( cell3.getValue(), 72 );
		assertEquals( getSignalCount( "1changed" ), 1 );
		assertEquals( getSignalCount( "3changed" ), 1 );

		cell3.setEvaluator( evaluator3 );

		assertEquals( cell1.getValue(), 12 );
		assertEquals( cell2.getValue(), 36 );
		assertEquals( cell3.getValue(), 72 );
		assertEquals( getSignalCount( "1changed" ), 1 );
		assertEquals( getSignalCount( "3changed" ), 2 );
	}
}
