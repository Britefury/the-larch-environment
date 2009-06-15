//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Layout;

import BritefuryJ.DocPresent.Layout.FractionLayout;
import BritefuryJ.DocPresent.Layout.LBox;

public class Test_FractionLayout extends Test_Layout_base
{
	//
	//
	// REQUISITION TESTS
	//
	//
	
	private void reqTest(LBox numerator, LBox bar, LBox denominator, double hPadding, double vSpacing, double baselinePos, LBox expectedParentBox)
	{
		LBox box = new LBox( null );
		FractionLayout.computeRequisitionX( box, numerator, bar, denominator, hPadding, vSpacing, baselinePos );
		FractionLayout.computeRequisitionY( box, numerator, bar, denominator, hPadding, vSpacing, baselinePos );

		assertBoxesEqual( box, expectedParentBox, "PARENT BOX" );
	}
	
	
	public void test_requisition()
	{
		// max of numerator and denominator, add padding, ignore bar
		reqTest( box( 20, 40, 0, 0, 10, 0 ), box( 50, 60, 0, 0, 10, 0 ), box( 10, 20, 0, 0, 20, 0 ),
				5, 2, 5,
				box( 30, 50, 0, 0, 10 + 2 + FractionLayout.getBarHeight() * 0.5  +  5, 20 + 2 + FractionLayout.getBarHeight() * 0.5  -  5, 0 ) );

		// max of numerator  and denominator, add padding, ignore bar, different v-spacing, different baseline position
		reqTest( box( 10, 20, 0, 0, 20, 0 ), box( 50, 60, 0, 0, 10, 0 ), box( 20, 40, 0, 0, 10, 0 ),
				6, 4, 7,
				box( 32, 52, 0, 0, 20 + 4 + FractionLayout.getBarHeight() * 0.5  +  7, 10 + 4 + FractionLayout.getBarHeight() * 0.5  -  7, 0 ) );

		// no denominator
		reqTest( box( 20, 40, 0, 0, 10, 0 ), box( 50, 60, 0, 0, 10, 0 ), null,
				5, 2, 5,
				box( 30, 50, 0, 0, 10 + 2 + FractionLayout.getBarHeight() * 0.5  +  5, 0 + 2 + FractionLayout.getBarHeight() * 0.5  -  5, 0 ) );

		// no numerator
		reqTest( null, box( 50, 60, 0, 0, 10, 0 ), box( 10, 20, 0, 0, 20, 0 ),
				5, 2, 5,
				box( 20, 30, 0, 0, 0 + 2 + FractionLayout.getBarHeight() * 0.5  +  5, 20 + 2 + FractionLayout.getBarHeight() * 0.5  -  5, 0 ) );

		// no bar
		reqTest( box( 20, 40, 0, 0, 10, 0 ), null, box( 10, 20, 0, 0, 20, 0 ),
				5, 2, 5,
				box( 30, 50, 0, 0, 10 + 2  +  5, 20 + 2  -  5, 0 ) );
	}





	//
	//
	// ALLOCATION TESTS
	//
	//
	
	private void allocTest(LBox numerator, LBox bar, LBox denominator, double hPadding, double vSpacing, double baselinePos,
			double allocX, double allocY, LBox expectecNumAlloc, LBox expectedBarAlloc, LBox expectedDenomAlloc)
	{
		LBox expectedNumerator = null, expectedBar = null, expectedDenominator = null;
		if ( numerator != null )
		{
			expectedNumerator = numerator.copy( null );
			expectedNumerator.setAllocation( expectecNumAlloc );
		}
		if ( bar != null )
		{
			expectedBar = bar.copy( null );
			expectedBar.setAllocation( expectedBarAlloc );
		}
		if ( denominator != null )
		{
			expectedDenominator = denominator.copy( null );
			expectedDenominator.setAllocation( expectedDenomAlloc );
		}
		
		
		LBox box = new LBox( null );
		FractionLayout.computeRequisitionX( box, numerator, bar, denominator, hPadding, vSpacing, baselinePos );
		box.setAllocationX( allocX );
		FractionLayout.allocateX( box, numerator, bar, denominator, hPadding, vSpacing, baselinePos );

		FractionLayout.computeRequisitionY( box, numerator, bar, denominator, hPadding, vSpacing, baselinePos );
		box.setAllocationY( allocY );
		FractionLayout.allocateY( box, numerator, bar, denominator, hPadding, vSpacing, baselinePos );

		
		if ( numerator != null )
		{
			assertBoxesEqual( numerator, expectedNumerator, "NUMERATOR ALLOCATION" );
		}
		if ( bar != null )
		{
			assertBoxesEqual( bar, expectedBar, "BAR ALLOCATION" );
		}
		if ( denominator != null )
		{
			assertBoxesEqual( denominator, expectedDenominator, "DENOMINATOR ALLOCATION" );
		}
	}



	public void test_allocation()
	{
		// numerator, bar, denominator
		allocTest( box( 20, 40, 0, 0, 10, 0 ), box( 50, 60, 0, 0, 10, 0 ), box( 10, 20, 0, 0, 20, 0 ),
				5, 2, 5,
				100, 200,
				alloc( 5, 0, 40, 10 ), alloc( 0, 12, 50, FractionLayout.getBarHeight() ), alloc( 15, 12 + FractionLayout.getBarHeight() + 2, 20, 20 ) );

		// numerator and denominator swapped, different hpadding, vspacing, baseline offset
		allocTest( box( 10, 20, 0, 0, 20, 0 ), box( 50, 60, 0, 0, 10, 0 ), box( 20, 40, 0, 0, 10, 0 ),
				6, 4, 7,
				100, 200,
				alloc( 16, 0, 20, 20 ), alloc( 0, 24, 52, FractionLayout.getBarHeight() ), alloc( 6, 24 + FractionLayout.getBarHeight() + 4, 40, 10 ) );

		// no denominator
		allocTest( box( 20, 40, 0, 0, 10, 0 ), box( 50, 60, 0, 0, 10, 0 ), null,
				5, 2, 5,
				100, 200,
				alloc( 5, 0, 40, 10 ), alloc( 0, 12, 50, FractionLayout.getBarHeight() ), null );

		// no numerator
		allocTest( null, box( 50, 60, 0, 0, 10, 0 ), box( 10, 20, 0, 0, 20, 0 ),
				5, 2, 5,
				100, 200,
				null, alloc( 0, 2, 30, FractionLayout.getBarHeight() ), alloc( 5, 2 + FractionLayout.getBarHeight() + 2, 20, 20 ) );

		// no bar
		allocTest( box( 20, 40, 0, 0, 10, 0 ), null, box( 10, 20, 0, 0, 20, 0 ),
				5, 2, 5,
				100, 200,
				alloc( 5, 0, 40, 10 ), null, alloc( 15, 12 + 2, 20, 20 ) );
	}
}
