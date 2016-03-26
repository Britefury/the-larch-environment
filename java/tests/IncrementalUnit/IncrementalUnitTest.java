//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.IncrementalUnit;

import BritefuryJ.Live.LiveFunction;

public class IncrementalUnitTest extends IncrementalUnitTest_base
{
	public void testLiteral()
	{
		LiveFunction cell = new LiveFunction();
		
		cell.setLiteralValue( new Integer( 1 ) );
	
		assertEquals( cell.getValue(), 1 );
		
		cell.setLiteralValue( new Integer( 20 ) );
	
		assertEquals( cell.getValue(), 20 );
	}



	public void testListener()
	{
		assertEquals( getSignalCount( "changed" ), 0 );
		
		LiveFunction cell = new LiveFunction();
		
		cell.setLiteralValue( new Integer( 1 ) );
		assertEquals( cell.getValue(), 1 );
		
		cell.addListener( makeListener( "" ) );
		
		cell.setLiteralValue( new Integer( 20 ) );
		assertEquals( cell.getValue(), 20 );
		
		assertEquals( getSignalCount( "changed" ), 1 );
	}
	
	
	
	public void testFunction()
	{
		LiveFunction.Function evaluator = new LiveFunction.Function()
		{
			public Object evaluate()
			{
				return 20;
			}
		};
		
		
		assertEquals( getSignalCount( "changed" ), 0 );

		LiveFunction cell = new LiveFunction();

		cell.setLiteralValue( new Integer( 1 ) );
		assertEquals( cell.getValue(), 1 );
		
		cell.addListener( makeListener( "" ) );
		
		cell.setFunction( evaluator );
		assertEquals( cell.getValue(), 20 );
		
		assertEquals( getSignalCount( "changed" ), 1 );
	}
	
	
	public void testChain()
	{
		final LiveFunction cell1 = new LiveFunction();
		final LiveFunction cell2 = new LiveFunction();
		
		LiveFunction.Function evaluator = new LiveFunction.Function()
		{
			public Object evaluate()
			{
				return ( (Integer)cell1.getValue() ).intValue() * 3;
			}
		};
		
		
		cell1.setLiteralValue( new Integer( 1 ) );
		cell2.setFunction( evaluator );
		
		assertEquals( cell1.getValue(), 1 );
		assertEquals( cell2.getValue(), 3 );
		
		cell1.setLiteralValue( new Integer( 12 ) );

		assertEquals( cell1.getValue(), 12 );
		assertEquals( cell2.getValue(), 36 );
	}
	
	
	public void testValueCache()
	{
		final int[] callCount = { 0 };
		final LiveFunction cell1 = new LiveFunction();
		final LiveFunction cell2 = new LiveFunction();
		
		LiveFunction.Function evaluator = new LiveFunction.Function()
		{
			public Object evaluate()
			{
				callCount[0]++;
				return ( (Integer)cell1.getValue() ).intValue() * 3;
			}
		};
		
		
		cell1.setLiteralValue( new Integer( 1 ) );

		cell2.setFunction( evaluator );
		
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
		
		final LiveFunction cell1 = new LiveFunction();
		final LiveFunction cell2 = new LiveFunction();
		final LiveFunction cell3 = new LiveFunction();
		
		LiveFunction.Function evaluator2 = new LiveFunction.Function()
		{
			public Object evaluate()
			{
				return ( (Integer)cell1.getValue() ).intValue() * 3;
			}
		};
		
		LiveFunction.Function evaluator3 = new LiveFunction.Function()
		{
			public Object evaluate()
			{
				return ( (Integer)cell2.getValue() ).intValue() * 2;
			}
		};
		
		
		cell1.setLiteralValue( new Integer( 1 ) );
		cell2.setFunction( evaluator2 );
		cell3.setFunction( evaluator3 );

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

		cell3.setFunction( evaluator3 );

		assertEquals( cell1.getValue(), 12 );
		assertEquals( cell2.getValue(), 36 );
		assertEquals( cell3.getValue(), 72 );
		assertEquals( getSignalCount( "1changed" ), 1 );
		assertEquals( getSignalCount( "3changed" ), 2 );
	}
}
