//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Layout;

import BritefuryJ.DocPresent.Layout.FractionLayout;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocHelper;
import BritefuryJ.DocPresent.Layout.LReqBox;

public class Test_FractionLayout extends Test_Layout_base
{
	//
	//
	// REQUISITION TESTS
	//
	//
	
	private void reqTest(LReqBox numerator, LReqBox bar, LReqBox denominator, double hPadding, double vSpacing, double baselinePos, LReqBox expectedParentBox)
	{
		LReqBox box = new LReqBox();
		FractionLayout.computeRequisitionX( box, numerator, bar, denominator, hPadding, vSpacing, baselinePos );
		FractionLayout.computeRequisitionY( box, numerator, bar, denominator, hPadding, vSpacing, baselinePos );

		assertBoxesEqual( box, expectedParentBox, "PARENT BOX" );
	}
	
	
	public void test_requisition()
	{
		// max of numerator and denominator, add padding, ignore bar
		reqTest( box( 20, 40, 20, 40, 10, 0 ), box( 50, 60, 50, 60, 10, 0 ), box( 10, 20, 10, 20, 20, 0 ),
				5, 2, 5,
				rbox( 30, 50, 30, 50,  10 + 2 + 10 + 2 + 20,  0,  10 + 2 + 10 * 0.5 + 5 ) );

		// max of numerator  and denominator, add padding, ignore bar, different v-spacing, different baseline position
		reqTest( box( 10, 20, 10, 20, 20, 0 ), box( 50, 60, 50, 60, 10, 0 ), box( 20, 40, 20, 40, 10, 0 ),
				6, 4, 7,
				rbox( 32, 52, 32, 52,  20 + 4 + 10 + 4 + 10,  0,  20 + 4 + 10 * 0.5 + 7 ) );    

		// no denominator
		reqTest( box( 20, 40, 20, 40, 10, 0 ), box( 50, 60, 50, 60, 10, 0 ), null,
				5, 2, 5,
				rbox( 30, 50, 30, 50,  10 + 2 + 10,  0,  10 + 2 + 10 * 0.5 + 5 ) );

		// no numerator
		reqTest( null, box( 50, 60, 50, 60, 10, 0 ), box( 10, 20, 10, 20, 20, 0 ),
				5, 2, 5,
				rbox( 20, 30, 20, 30,  10 + 2 + 20,  0,  10 * 0.5 + 5 ) );

		// no bar
		reqTest( box( 20, 40, 20, 40, 10, 0 ), null, box( 10, 20, 10, 20, 20, 0 ),
				5, 2, 5,
				rbox( 30, 50, 30, 50,  10 + 2 + 20,  0,  10 + 2 * 0.5 + 5 ) );
	}





	//
	//
	// ALLOCATION TESTS
	//
	//
	
	private void allocTest(LReqBox numerator, LReqBox bar, LReqBox denominator, double hPadding, double vSpacing, double baselinePos,
			double allocX, double allocY, LAllocBox expectecNumAlloc, LAllocBox expectedBarAlloc, LAllocBox expectedDenomAlloc)
	{
		LReqBox box = new LReqBox();
		LAllocBox allocBox = new LAllocBox( null );
		LAllocBox numAlloc = new LAllocBox( null ), barAlloc = new LAllocBox( null ), denomAlloc = new LAllocBox( null );
		FractionLayout.computeRequisitionX( box, numerator, bar, denominator, hPadding, vSpacing, baselinePos );
		LAllocHelper.allocateX( allocBox, box, 0.0, allocX );
		FractionLayout.allocateX( box, numerator, bar, denominator, allocBox, numAlloc, barAlloc, denomAlloc, hPadding, vSpacing, baselinePos );

		FractionLayout.computeRequisitionY( box, numerator, bar, denominator, hPadding, vSpacing, baselinePos );
		LAllocHelper.allocateY( allocBox, box, 0.0, allocY );
		FractionLayout.allocateY( box, numerator, bar, denominator, allocBox, numAlloc, barAlloc, denomAlloc, hPadding, vSpacing, baselinePos );

		
		if ( numerator != null )
		{
			assertAllocsEqual( numAlloc, expectecNumAlloc, "NUMERATOR ALLOCATION" );
		}
		if ( bar != null )
		{
			assertAllocsEqual( barAlloc, expectedBarAlloc, "BAR ALLOCATION" );
		}
		if ( denominator != null )
		{
			assertAllocsEqual( denomAlloc, expectedDenomAlloc, "DENOMINATOR ALLOCATION" );
		}
	}



	public void test_allocation()
	{
		double barHeight = 1.5;
		
		// numerator, bar, denominator
		allocTest( box( 20, 40, 0, 0, 10, 0 ), box( 50, 60, 0, 0, barHeight, 0 ), box( 10, 20, 0, 0, 20, 0 ),
				5, 2, 5,
				100, 200,
				alloc( 5, 0, 40, 10 ), alloc( 0, 12, 50, barHeight ), alloc( 15, 12 + barHeight + 2, 20, 20 ) );

		// numerator and denominator swapped, different hpadding, vspacing, baseline offset
		allocTest( box( 10, 20, 0, 0, 20, 0 ), box( 50, 60, 0, 0, barHeight, 0 ), box( 20, 40, 0, 0, 10, 0 ),
				6, 4, 7,
				100, 200,
				alloc( 16, 0, 20, 20 ), alloc( 0, 24, 52, barHeight ), alloc( 6, 24 + barHeight + 4, 40, 10 ) );

		// no denominator
		allocTest( box( 20, 40, 0, 0, 10, 0 ), box( 50, 60, 0, 0, barHeight, 0 ), null,
				5, 2, 5,
				100, 200,
				alloc( 5, 0, 40, 10 ), alloc( 0, 12, 50, barHeight ), null );

		// no numerator
		allocTest( null, box( 50, 60, 0, 0, barHeight, 0 ), box( 10, 20, 0, 0, 20, 0 ),
				5, 2, 5,
				100, 200,
				null, alloc( 0, 2, 30, barHeight, 50 ), alloc( 5, 2 + barHeight + 2, 20, 20 ) );

		// no bar
		allocTest( box( 20, 40, 0, 0, 10, 0 ), null, box( 10, 20, 0, 0, 20, 0 ),
				5, 2, 5,
				100, 200,
				alloc( 5, 0, 40, 10 ), null, alloc( 15, 12 + 2, 20, 20 ) );
	}
}
