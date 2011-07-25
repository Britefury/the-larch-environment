//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Layout;

import BritefuryJ.DocPresent.Layout.HorizontalLayout;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocHelper;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;

public class Test_HorizontalLayout extends Test_Layout_base
{
	//
	//
	// REQUISITION TESTS
	//
	//
	
	public void test_requisitionX()
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing

		LReqBox result = new LReqBox();
		
		// requisitionX()  ->  <0,0>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] {},  0.0 );
		assertBoxesEqual( result, new LReqBox() );

		// requisitionX( [ <0,0> ] )  ->  <0,0>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { new LReqBox() },  0.0 );
		assertBoxesEqual( result, new LReqBox() );

		// requisitionX( [ <10,10> ] )  ->  <10,10>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 10.0 ) },  0.0 );
		assertBoxesEqual( result, xbox( 10.0, 10.0 ) );

		// Padding 'consumes' h-spacing
		// requisitionX( [ <10,11> ] )  ->  <10,11>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 11.0 ) },  0.0 );
		assertBoxesEqual( result, xbox( 10.0, 11.0 ) );

		// requisitionX( [ <0,0>, <0,0> ] )  ->  <0,0>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { new LReqBox(), new LReqBox() },  0.0 );
		assertBoxesEqual( result, new LReqBox() );

		// Width accumulates
		// requisitionX( [ <10,10>, <5,5> ] )  ->  <15,15>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 10.0 ), xbox( 5.0, 5.0 ) },  0.0 );
		assertBoxesEqual( result, xbox( 15.0, 15.0 ) );

		// H-spacing of child puts space before next child
		// requisitionX( [ <10,12>, <5,5> ] )  ->  <17,17>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 12.0 ), xbox( 5.0, 5.0 ) },  0.0 );
		assertBoxesEqual( result, xbox( 17.0, 17.0 ) );

		// H-spacing of last child gets put onto the result
		// requisitionX( [ <10,12>, <5,6> ] )  ->  <17,18>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 12.0 ), xbox( 5.0, 6.0 ) },  0.0 );
		assertBoxesEqual( result, xbox( 17.0, 18.0 ) );

		// Spacing between children adds extra width
		// requisitionX( [ <0,0>, <0,0> ], spacing=1 )  ->  <1,0>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { new LReqBox(), new LReqBox() },  1.0 );
		assertBoxesEqual( result, xbox( 1.0, 1.0 ) );
		// requisitionX( [ <10,10>, <5,5> ], spacing=1 )  ->  <16,16>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 10.0 ), xbox( 5.0, 5.0 ) },  1.0 );
		assertBoxesEqual( result, xbox( 16.0, 16.0 ) );

		// Spacing between children is added to the child's own spacing
		// requisitionX( [ <10,12>, <5,6> ], spacing=1 )  ->  <18,19>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 12.0 ), xbox( 5.0, 6.0 ) },  1.0 );
		assertBoxesEqual( result, xbox( 18.0, 19.0 ) );
	}




	public void test_requisitionY()
	{
		LReqBox result = new LReqBox();
		
		
		// First, test the simple cases of no baseline alignment, or with baseline alignment with children that do not have baselines
		
		// Empty list should result in empty
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] {}, new int[] {} );
		assertBoxesEqual( result, new LReqBox() );

		// List of one empty box should result in empty
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { new LReqBox() }, new int[] { 0 } );
		assertBoxesEqual( result, new LReqBox( 0.0, 0.0, 0.0, 0.0, 0.0 ) );

		// 1 Box of height 1 should result in same
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 1.0, 0.0 ) }, new int[] { VCENTRE } );
		assertBoxesEqual( result, ybox( 1.0, 0.0 ) );

		// 1 Box of height 10, vspacing 1 should result in same
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 1.0 ) }, new int[] { VCENTRE } );
		assertBoxesEqual( result, ybox( 10.0, 1.0 ) );

		// requisitionY( [ <10,1>, <20,1> ] )  ->  <20,1>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 1.0 ),  ybox( 20.0, 1.0 ) }, new int[] { VCENTRE, VCENTRE } );
		assertBoxesEqual( result, ybox( 20.0, 1.0 ) );

		// requisitionY( [ <1,10>, <2,20> ] )  ->  <2,20>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 1.0, 10.0 ),  ybox( 2.0, 20.0 ) }, new int[] { VCENTRE, VCENTRE } );
		assertBoxesEqual( result, ybox( 2.0, 20.0 ) );

		// requisitionY( [ <10,3>, <11,1> ] )  ->  <11,2>
		// The first box advances X the most overall, although the second has the greater height
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 3.0 ),  ybox( 11.0, 1.0 ) }, new int[] { VCENTRE, VCENTRE } );
		assertBoxesEqual( result, ybox( 11.0, 2.0 ) );

		// requisitionY( [ <10,5>, <5,10> ] )  ->  <10,5>
		// Both advance X by the same amount (15 units), but the first has the greater height
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 5.0 ),  ybox( 5.0, 10.0 ) }, new int[] { VCENTRE, VCENTRE } );
		assertBoxesEqual( result, ybox( 10.0, 5.0 ) );

	
	

		// Now, test the cases with baseline alignment, with children that do not have baselines
		
		// 1 Box of height 1 should result in same
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 1.0, 0.0 ) }, new int[] { VREFY } );
		assertBoxesEqual( result, yrbox( 1.0, 0.0, 0.5 ) );

		// 1 Box of height 10, vspacing 1 should result in same
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 1.0 ) }, new int[] { VREFY } );
		assertBoxesEqual( result, yrbox( 10.0, 1.0, 5.0 ) );

		// requisitionY( [ <10,1>, <20,1> ] )  ->  <20,1>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 1.0 ),  ybox( 20.0, 1.0 ) }, new int[] { VREFY, VREFY } );
		assertBoxesEqual( result, yrbox( 20.0, 1.0, 10.0 ) );

		// requisitionY( [ <1,10>, <2,20> ] )  ->  <2,20>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 1.0, 10.0 ),  ybox( 2.0, 20.0 ) }, new int[] { VREFY, VREFY } );
		assertBoxesEqual( result, yrbox( 2.0, 20.0, 1.0 ) );

		// requisitionY( [ <10,3>, <11,1> ] )  ->  <11,2.5>
		// The first box advances X the most overall, although the second has the greater height
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 3.0 ),  ybox( 11.0, 1.0 ) }, new int[] { VREFY, VREFY } );
		assertBoxesEqual( result, yrbox( 11.0, 2.5, 5.5 ) );

		// requisitionY( [ <10,5>, <5,10> ] )  ->  <10,5>
		// Both advance X by the same amount (15 units), but the first has the greater height
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 5.0 ),  ybox( 5.0, 10.0 ) }, new int[] { VREFY, VREFY } );
		assertBoxesEqual( result, yrbox( 10.0, 7.5, 5.0 ) );



		
		// Now test the cases where baselines are used
		
		// 1 Box of 3:2 should result in same
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { yrbox( 5.0, 0.0, 3.0 ) }, new int[] { VREFY } );
		assertBoxesEqual( result, yrbox( 5.0, 0.0, 3.0 ) );

		// 1 Box of height 3:2, vspacing 1 should result in same
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { yrbox(  5.0, 1.0, 3.0 ) }, new int[] { VREFY } );
		assertBoxesEqual( result, yrbox( 5.0, 1.0, 3.0 ) );

		// requisitionY( [ <5:3,0>, <2:4,0> ] )  ->  <5:4,0>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { yrbox( 8.0, 0.0, 5.0 ),  yrbox( 6.0, 0.0, 2.0 ) }, new int[] { VREFY, VREFY } );
		assertBoxesEqual( result, yrbox( 9.0, 0.0, 5.0 ) );

		// requisitionY( [ <5:3,1>, <2:4,1> ] )  ->  <5:4,1>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { yrbox( 8.0, 1.0, 5.0 ),  yrbox( 6.0, 1.0, 2.0 ) }, new int[] { VREFY, VREFY } );
		assertBoxesEqual( result, yrbox( 9.0, 1.0, 5.0 ) );

		// requisitionY( [ <5:3,1>, <2:4,2> ] )  ->  <5:4,2>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { yrbox( 8.0, 1.0, 5.0 ),  yrbox( 6.0, 2.0, 2.0 ) }, new int[] { VREFY, VREFY } );
		assertBoxesEqual( result, yrbox( 9.0, 2.0, 5.0 ) );

		// requisitionY( [ <5:3,3>, <2:4,1> ] )  ->  <5:4,2>
		// The first box advances Y (below baseline) the most overall, although the second has the greater descent
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { yrbox( 8.0, 3.0, 5.0 ),  yrbox( 6.0, 1.0, 2.0 ) }, new int[] { VREFY, VREFY } );
		assertBoxesEqual( result, yrbox( 9.0, 2.0, 5.0 ) );

		// requisitionY( [ <2:4,2>, <5:2,4> ] )  ->  <5:4,2>
		// Both advance Y (below baseline) by the same amount (6 units), but the first has the greater descent
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { yrbox( 6.0, 2.0, 2.0 ),  yrbox( 7.0, 4.0, 5.0 ) }, new int[] { VREFY, 0 } );
		assertBoxesEqual( result, yrbox( 9.0, 2.0, 5.0 ) );
		
		
		
		// Now test the situation where baseline alignment is used, but the some children do not have baselines

		// requisitionY( [ <6:3,0>, <8,0> ] )  ->  requisitionY( [ <6:3,0>, <4:4,0> ] )  ->  <6:4,0>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { yrbox( 9.0, 0.0, 6.0 ),  ybox( 8.0, 0.0 ) }, new int[] { VREFY, VREFY } );
		assertBoxesEqual( result, yrbox( 10.0, 0.0, 6.0 ) );

		// requisitionY( [ <6:3,3>, <8,1> ] )  ->  requisitionY( [ <6:3,3>, <4:4,1> ] )  ->  <6:4,2>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { yrbox( 9.0, 3.0, 6.0 ),  ybox( 8.0, 1.0 ) }, new int[] { VREFY, VREFY } );
		assertBoxesEqual( result, yrbox( 10.0, 2.0, 6.0 ) );

		// requisitionY( [ <6:3,1>, <8,2> ] )  ->  requisitionY( [ <6:3,1>, <4:4,2> ] )  ->  <6:4,2>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { yrbox( 9.0, 1.0, 6.0 ),  ybox( 8.0, 2.0 ) }, new int[] { VREFY, VREFY } );
		assertBoxesEqual( result, yrbox( 10.0, 2.0, 6.0 ) );
	}







	//
	//
	// ALLOCATION TESTS
	//
	//

	private void allocXTest(LReqBox children[], int childAllocFlags[], double spacing, LReqBox expectedBox, double boxAllocation, double expectedSize[], double expectedPosition[])
	{ 
		LReqBox box = new LReqBox();
		LAllocBox boxAlloc = new LAllocBox( null );
		LAllocBox childrenAlloc[] = new LAllocBox[children.length];
		for (int i = 0; i < children.length; i++)
		{
			childrenAlloc[i] = new LAllocBox( null );
		}

		HorizontalLayout.computeRequisitionX( box, children, spacing );

		assertBoxesEqual( box, expectedBox, "PARENT BOX" );

		LAllocHelper.allocateX( boxAlloc, box, 0.0, boxAllocation );
		HorizontalLayout.allocateX( box, children, boxAlloc, childrenAlloc, childAllocFlags, spacing );
		for (int i = 0; i < children.length; i++)
		{
			if ( childrenAlloc[i].getAllocationX() != expectedSize[i] )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSize[i] + ", result=" + childrenAlloc[i].getAllocationX() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( childrenAlloc[i].getAllocationX(), expectedSize[i] );

			if ( childrenAlloc[i].getAllocPositionInParentSpaceX() != expectedPosition[i] )
			{
				System.out.println( "Child position for " + i + " is not as expected; expected=" + expectedPosition[i] + ", result=" + childrenAlloc[i].getAllocPositionInParentSpaceX() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( childrenAlloc[i].getAllocPositionInParentSpaceX(), expectedPosition[i] );
		}
	}
	
	private void allocXTests(LReqBox children[], int childAllocFlags[], double spacing, LReqBox expectedBox, double boxAllocations[], double expectedSize[][], double expectedPosition[][])
	{
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			allocXTest( children, childAllocFlags, spacing, expectedBox, boxAllocations[i], expectedSize[i], expectedPosition[i] );
		}
	}



	public void test_allocateX()
	{
		// Test simple case; single child, simple advance
		// hpackX( [ <100-200,100-200> ], spacing=0 )
		// 	boxAllocation=300   ->   [ 200 ] @ [ 0 ]		- no expansion
		// 	boxAllocation=200   ->   [ 200 ] @ [ 0 ]		- all allocated to 1 child
		// 	boxAllocation=150   ->   [ 150 ] @ [ 0 ]		- all allocated to 1 child
		// 	boxAllocation=100   ->   [ 100 ] @ [ 0 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 100 ] @ [ 0 ]			- will not go below minimum
		// No padding, no expand
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 100.0, 200.0 ) }, new int[] { 0 }, 0.0,
				xbox( 100.0, 200.0, 100.0, 200.0 ),
				new double[] { 300.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 200.0 },
					new double[] { 200.0 },
					new double[] { 150.0 },
					new double[] { 100.0 },
					new double[] { 100.0 } },
				new double[][] {
					new double[] { 0.0 },
					new double[] { 0.0 },
					new double[] { 0.0 },
					new double[] { 0.0 },
					new double[] { 0.0 } } );
	
		
		// Single child, expand
		// hpackX( [ <100-200,100-200,H.EXPAND> ], spacing=0 )
		// 	boxAllocation=300   ->   [ 300 ] @ [ 0 ]		- expansion; extra space allocated to child
		// 	boxAllocation=200   ->   [ 200 ] @ [ 0 ]		- all allocated to 1 child
		// 	boxAllocation=150   ->   [ 150 ] @ [ 0 ]		- all allocated to 1 child
		// 	boxAllocation=100   ->   [ 100 ] @ [ 0 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 100 ] @ [ 0 ]			- will not go below minimum
		// No padding, expand
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 100.0, 200.0 ) }, new int[] { HEXPAND }, 0.0,
				xbox( 100.0, 200.0, 100.0, 200.0 ),
				new double[] { 300.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 300.0 },
					new double[] { 200.0 },
					new double[] { 150.0 },
					new double[] { 100.0 },
					new double[] { 100.0 } },
				new double[][] {
					new double[] { 0.0 },
					new double[] { 0.0 },
					new double[] { 0.0 },
					new double[] { 0.0 },
					new double[] { 0.0 } } );


		// Extra advance applied to 1 child should not make a difference, since it is the last child
		// hpackX( [ <100-200,110-210> ], spacing=0 )
		// 	boxAllocation=300   ->   [ 200 ] @ [ 0 ]		- no expansion
		// 	boxAllocation=200   ->   [ 200 ] @ [ 0 ]		- all allocated to 1 child
		// 	boxAllocation=150   ->   [ 150 ] @ [ 0 ]		- all allocated to 1 child
		// 	boxAllocation=100   ->   [ 100 ] @ [ 0 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 100 ] @ [ 0 ]			- will not go below minimum
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 110.0, 210.0 ) }, new int[] { 0 }, 0.0,
				xbox( 100.0, 200.0, 110.0, 210.0 ),
				new double[] { 300.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 200.0 },
					new double[] { 200.0 },
					new double[] { 150.0 },
					new double[] { 100.0 },
					new double[] { 100.0 } },
				new double[][] {
					new double[] { 0.0 },
					new double[] { 0.0 },
					new double[] { 0.0 },
					new double[] { 0.0 },
					new double[] { 0.0 } } );
		

		
		
		

		
		
		// 2 children, packed one after another
		// hpackX( [ <100-200,100-200>, <50-70,50-70> ], spacing=0 )
		// 	boxAllocation=300   ->   [ 200, 70 ] @ [ 0, 200 ]		- no expansion
		// 	boxAllocation=270   ->   [ 200, 70 ] @ [ 0, 200 ]		- preferred sizes
		// 	boxAllocation=210   ->   [ 150, 60 ] @ [ 0, 150 ]		- space above minimum distributed evenly
		// 	boxAllocation=150   ->   [ 100, 50 ] @ [ 0, 100 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 100 ]		- will not go below minimum
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 100.0, 200.0 ),  xbox( 50.0, 70.0, 50.0, 70.0 ) }, new int[] { 0, 0 }, 0.0,
				xbox( 150.0, 270.0, 150.0, 270.0 ),
				new double[] { 300.0, 270.0, 210.0, 150.0, 100.0 },
				new double[][] {
					new double[] { 200.0, 70.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } },
				new double[][] {
					new double[] { 0.0, 200.0 },
					new double[] { 0.0, 200.0 },
					new double[] { 0.0, 150.0 },
					new double[] { 0.0, 100.0 },
					new double[] { 0.0, 100.0 } } );


		// 2 children, packed one after another, 10 spacing units
		// hpackX( [ <100-200,0-0>, <50-70,0-0> ], spacing=10 )
		// 	boxAllocation=300   ->   [ 200, 70 ] @ [ 0, 210 ]		- no expansion
		// 	boxAllocation=280   ->   [ 200, 70 ] @ [ 0, 210 ]		- preferred sizes
		// 	boxAllocation=220   ->   [ 150, 60 ] @ [ 0, 160 ]		- space above minimum distributed evenly
		// 	boxAllocation=160   ->   [ 100, 50 ] @ [ 0, 110 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 110 ]		- will not go below minimum
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 100.0, 200.0 ),  xbox( 50.0, 70.0, 50.0, 70.0 ) }, new int[] { 0, 0 }, 10.0,
				xbox( 160.0, 280.0, 160.0, 280.0 ),
				new double[] { 300.0, 280.0, 220.0, 160.0, 100.0 },
				new double[][] {
					new double[] { 200.0, 70.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } },
				new double[][] {
					new double[] { 0.0, 210.0 },
					new double[] { 0.0, 210.0 },
					new double[] { 0.0, 160.0 },
					new double[] { 0.0, 110.0 },
					new double[] { 0.0, 110.0 } } );


		
		// 2 children with extra advance on child 0, packed one after another
		// hpackX( [ <100-200,115-215>, <50-70,50-70> ], spacing=0 )
		// 	boxAllocation=400   ->   [ 200, 70 ] @ [ 0, 215 ]		- no expansion
		// 	boxAllocation=335   ->   [ 200, 70 ] @ [ 0, 215 ]		- preferred sizes
		// 	boxAllocation=225   ->   [ 150, 60 ] @ [ 0, 165 ]		- space above minimum distributed evenly
		// 	boxAllocation=165   ->   [ 100, 50 ] @ [ 0, 115 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 115 ]		- will not go below minimum
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 115.0, 215.0 ),  xbox( 50.0, 70.0, 50.0, 70.0 ) }, new int[] { 0, 0 }, 0.0,
				xbox( 165.0, 285.0, 165.0, 285.0 ),
				new double[] { 400.0, 335.0, 225.0, 165.0, 100.0 },
				new double[][] {
					new double[] { 200.0, 70.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } },
				new double[][] {
					new double[] { 0.0, 215.0 },
					new double[] { 0.0, 215.0 },
					new double[] { 0.0, 165.0 },
					new double[] { 0.0, 115.0 },
					new double[] { 0.0, 115.0 } } );


		
		// 2 children with reduced advance on child 0, packed one after another
		// hpackX( [ <100-200,85-185>, <50-70,50-70> ], spacing=0 )
		// 	boxAllocation=300   ->   [ 200, 70 ] @ [ 0, 185 ]		- no expansion
		// 	boxAllocation=255   ->   [ 200, 70 ] @ [ 0, 185 ]		- preferred sizes
		// 	boxAllocation=195   ->   [ 150, 60 ] @ [ 0, 165 ]		- space above minimum distributed evenly
		// 	boxAllocation=135   ->   [ 100, 50 ] @ [ 0, 85 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 115 ]		- will not go below minimum
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 85.0, 185.0 ),  xbox( 50.0, 70.0, 50.0, 70.0 ) }, new int[] { 0, 0 }, 0.0,
				xbox( 135.0, 255.0, 135.0, 255.0 ),
				new double[] { 300.0, 255.0, 195.0, 135.0, 100.0 },
				new double[][] {
					new double[] { 200.0, 70.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } },
				new double[][] {
					new double[] { 0.0, 185.0 },
					new double[] { 0.0, 185.0 },
					new double[] { 0.0, 135.0 },
					new double[] { 0.0, 85.0 },
					new double[] { 0.0, 85.0 } } );


		
		// 2 children with extra advance on both, packed one after another, 10 units of spacing
		// hpackX( [ <100-200,115-215>, <50-70,60-80> ], spacing=10 )
		// 	boxAllocation=400   ->   [ 200, 70 ] @ [ 0, 215 ]		- no expansion
		// 	boxAllocation=335   ->   [ 200, 70 ] @ [ 0, 215 ]		- preferred sizes
		// 	boxAllocation=235   ->   [ 150, 60 ] @ [ 0, 175 ]		- space above minimum distributed evenly
		// 	boxAllocation=165   ->   [ 100, 50 ] @ [ 0, 125 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 125 ]		- will not go below minimum
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 115.0, 215.0 ),  xbox( 50.0, 70.0, 70.0, 80.0 ) }, new int[] { 0, 0 }, 10.0,
				xbox( 175.0, 295.0, 195.0, 305.0 ),
				new double[] { 400.0, 335.0, 235.0, 165.0, 100.0 },
				new double[][] {
					new double[] { 200.0, 70.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } },
				new double[][] {
					new double[] { 0.0, 225.0 },
					new double[] { 0.0, 225.0 },
					new double[] { 0.0, 175.0 },
					new double[] { 0.0, 125.0 },
					new double[] { 0.0, 125.0 } } );

	
	
	
		// hpackX( [ <100-200,115-215,H.EXPAND>, <50-70,60-80> ], spacing=0 )
		// 	boxAllocation=300   ->   [ 215, 70 ] @ [ 0, 230 ]		- space above preferred goes to first child, none to second
		// 	boxAllocation=285   ->   [ 200, 70 ] @ [ 0, 215 ]		- preferred sizes
		// 	boxAllocation=225   ->   [ 150, 60 ] @ [ 0, 165 ]		- space above minimum distributed evenly
		// 	boxAllocation=165   ->   [ 100, 50 ] @ [ 0, 115 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 115 ]		- will not go below minimum
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 115.0, 215.0 ),  xbox( 50.0, 70.0, 70.0, 80.0 ) }, new int[] { HEXPAND, 0 }, 0.0,
				xbox( 165.0, 285.0, 185.0, 295.0 ),
				new double[] { 300.0, 285.0, 225.0, 165.0, 100.0 },
				new double[][] {
					new double[] { 215.0, 70.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } },
				new double[][] {
					new double[] { 0.0, 230.0 },
					new double[] { 0.0, 215.0 },
					new double[] { 0.0, 165.0 },
					new double[] { 0.0, 115.0 },
					new double[] { 0.0, 115.0 } } );
					
		

		// hpackX( [ <100-200,115-215>, <50-70,60-80,H.EXPAND> ], spacing=0 )
		// 	boxAllocation=300   ->   [ 200, 100 ] @ [ 0, 230 ]		- space above preferred goes to second child, none to first
		// 	boxAllocation=285   ->   [ 200, 70 ] @ [ 0, 215 ]		- preferred sizes
		// 	boxAllocation=225   ->   [ 150, 60 ] @ [ 0, 165 ]		- space above minimum distributed evenly
		// 	boxAllocation=165   ->   [ 100, 50 ] @ [ 0, 115 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 115 ]		- will not go below minimum
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 115.0, 215.0 ),  xbox( 50.0, 70.0, 70.0, 80.0 ) }, new int[] { 0, HEXPAND }, 0.0,
				xbox( 165.0, 285.0, 185.0, 295.0 ),
				new double[] { 300.0, 285.0, 225.0, 165.0, 100.0 },
				new double[][] {
					new double[] { 200.0, 85.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } },
				new double[][] {
					new double[] { 0.0, 215.0 },
					new double[] { 0.0, 215.0 },
					new double[] { 0.0, 165.0 },
					new double[] { 0.0, 115.0 },
					new double[] { 0.0, 115.0 } } );
		


		// hpackX( [ <100-200,115-215,H.EXPAND>, <50-70,60-80,H.EXPAND> ], spacing=0 )
		// 	boxAllocation=315   ->   [ 215, 85 ] @ [ 0, 230 ]		- space above preferred goes to second child, none to first
		// 	boxAllocation=285   ->   [ 200, 70 ] @ [ 0, 215 ]		- preferred sizes
		// 	boxAllocation=225   ->   [ 150, 60 ] @ [ 0, 165 ]		- space above minimum distributed evenly
		// 	boxAllocation=165   ->   [ 100, 50 ] @ [ 0, 115 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 115 ]		- will not go below minimum
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 115.0, 215.0 ),  xbox( 50.0, 70.0, 70.0, 80.0 ) }, new int[] { HEXPAND, HEXPAND }, 0.0,
				xbox( 165.0, 285.0, 185.0, 295.0 ),
				new double[] { 315.0, 285.0, 225.0, 165.0, 100.0 },
				new double[][] {
					new double[] { 215.0, 85.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } },
				new double[][] {
					new double[] { 0.0, 230.0 },
					new double[] { 0.0, 215.0 },
					new double[] { 0.0, 165.0 },
					new double[] { 0.0, 115.0 },
					new double[] { 0.0, 115.0 } } );
	}





	
	private void allocYTest(LReqBox children[], int childAllocFlags[], LReqBox expectedBox, LAllocV boxAllocation, LAllocV expectedSize[], double expectedPosition[])
	{ 
		LReqBox box = new LReqBox();
		LAllocBox boxAlloc = new LAllocBox( null );
		LAllocBox childrenAlloc[] = new LAllocBox[children.length];
		for (int i = 0; i < children.length; i++)
		{
			childrenAlloc[i] = new LAllocBox( null );
		}

		
		HorizontalLayout.computeRequisitionY( box, children, childAllocFlags );

		assertBoxesEqual( box, expectedBox, "PARENT BOX" );

		LAllocHelper.allocateY( boxAlloc, box, 0.0, boxAllocation );
		HorizontalLayout.allocateY( box, children, boxAlloc, childrenAlloc, childAllocFlags );
		for (int i = 0; i < children.length; i++)
		{
			if ( !childrenAlloc[i].getAllocV().equals( expectedSize[i] ) )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSize[i] + ", result=" + childrenAlloc[i].getAllocV() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( childrenAlloc[i].getAllocV(), expectedSize[i] );

			if ( childrenAlloc[i].getAllocPositionInParentSpaceY() != expectedPosition[i] )
			{
				System.out.println( "Child position for " + i + " is not as expected; expected=" + expectedPosition[i] + ", result=" + childrenAlloc[i].getAllocPositionInParentSpaceY() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( childrenAlloc[i].getAllocPositionInParentSpaceY(), expectedPosition[i] );
		}
	}
	
	private void allocYTests(LReqBox children[], int childAllocFlags[], LReqBox expectedBox, LAllocV boxAllocations[], LAllocV expectedSize[][], double expectedPosition[][])
	{
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			allocYTest( children, childAllocFlags, expectedBox, boxAllocations[i], expectedSize[i], expectedPosition[i] );
		}
	}



	public void test_allocateY()
	{
		// vpackX( [ <300,0,V.TOP>, <200,0,V.TOP> ] )
		// 	boxAllocation=400   ->   [ 300, 200 ] @ [ 0, 0 ]		- no expansion, no expansion
		// 	boxAllocation=300   ->   [ 300, 200 ] @ [ 0, 0 ]		- req size, no expansion
		// 	boxAllocation=200   ->   [ 300, 200 ] @ [ 0, 0 ]		- below req size, req size
		// 	boxAllocation=100   ->   [ 300, 200 ] @ [ 0, 0 ]		- below req size, below req size
		allocYTests( new LReqBox[] { ybox( 300.0, 0.0 ),  ybox( 200.0, 0.0 ) }, new int[] { VTOP, VTOP },
				ybox( 300.0, 0.0 ),
				new LAllocV[] { new LAllocV( 400.0 ), new LAllocV( 300.0 ), new LAllocV( 200.0 ), new LAllocV( 100.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) } },
				new double[][] {
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );

		// vpackX( [ <300,0,V.TOP>, <200,0,V.TOP> ] )
		// 	boxAllocation=200:200   ->   [ 300, 200 ] @ [ 0, 0 ]		- no expansion, no expansion
		// 	boxAllocation=150:150   ->   [ 300, 200 ] @ [ 0, 0 ]		- req size, no expansion
		// 	boxAllocation=100:100   ->   [ 300, 200 ] @ [ 0, 0 ]		- below req size, req size
		// 	boxAllocation=50:50       ->   [ 300, 200 ] @ [ 0, 0 ]		- below req size, below req size
		allocYTests( new LReqBox[] { ybox( 300.0, 0.0 ),  ybox( 200.0, 0.0 ) },  new int[] { VTOP, VTOP },
				ybox( 300.0, 0.0 ),
				new LAllocV[] { new LAllocV( 200.0, 200.0 ), new LAllocV( 150.0, 150.0 ), new LAllocV( 100.0, 100.0 ), new LAllocV( 50.0, 50.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) } },
				new double[][] {
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );

		// vpackX( [ <150:150,V.TOP>, <100:100,V.TOP> ] )
		// 	boxAllocation=200:200   ->   [ 300, 200 ] @ [ 0, 0 ]		- no expansion, no expansion
		// 	boxAllocation=150:150   ->   [ 300, 200 ] @ [ 0, 0 ]		- req size, no expansion
		// 	boxAllocation=100:100   ->   [ 300, 200 ] @ [ 0, 0 ]		- below req size, req size
		// 	boxAllocation=50:50       ->   [ 300, 200 ] @ [ 0, 0 ]		- below req size, below req size
		allocYTests( new LReqBox[] { yrbox( 300.0, 0.0, 150.0 ),  yrbox( 200.0, 0.0, 100.0 ) }, new int[] { VTOP, VTOP },
				ybox( 300.0, 0.0 ),
				new LAllocV[] { new LAllocV( 200.0, 200.0 ), new LAllocV( 150.0, 150.0 ), new LAllocV( 100.0, 100.0 ), new LAllocV( 50.0, 50.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 300.0, 150.0 ), new LAllocV( 200.0, 100.0 ) },
					new LAllocV[] { new LAllocV( 300.0, 150.0 ), new LAllocV( 200.0, 100.0 ) },
					new LAllocV[] { new LAllocV( 300.0, 150.0 ), new LAllocV( 200.0, 100.0 ) },
					new LAllocV[] { new LAllocV( 300.0, 150.0 ), new LAllocV( 200.0, 100.0 ) } },
				new double[][] {
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );



		// vpackX( [ <300,0,V.CENTRE>, <200,0,V.CENTRE> ] )
		// 	boxAllocation=400   ->   [ 300, 200 ] @ [ 50, 100 ]	- no expansion, no expansion
		// 	boxAllocation=300   ->   [ 300, 200 ] @ [ 0, 50 ]		- req size, no expansion
		// 	boxAllocation=200   ->   [ 200, 200 ] @ [ 0, 50 ]		- below req size, req size  (parent box size of 300 results in second child being centred in a larger box)
		// 	boxAllocation=100   ->   [ 200, 200 ] @ [ 0, 50 ]		- below req size, below req size  (parent box size of 300 results in second child being centred in a larger box)
		allocYTests( new LReqBox[] { ybox( 300.0, 0.0 ),  ybox( 200.0, 0.0 ) }, new int[] { VCENTRE, VCENTRE },
				ybox( 300.0, 0.0 ),
				new LAllocV[] { new LAllocV( 400.0 ), new LAllocV( 300.0 ), new LAllocV( 200.0 ), new LAllocV( 100.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) } },
				new double[][] {
					new double[] { 50.0, 100.0 },
					new double[] { 0.0, 50.0 },
					new double[] { 0.0, 50.0 },
					new double[] { 0.0, 50.0 } } );



		// vpackX( [ <300,0,V.BOTTOM>, <200,0,V.BOTTOM> ] )
		// 	boxAllocation=400   ->   [ 300, 200 ] @ [ 100, 200 ]	- no expansion, no expansion
		// 	boxAllocation=300   ->   [ 300, 200 ] @ [ 0, 100 ]		- req size, no expansion
		// 	boxAllocation=200   ->   [ 200, 200 ] @ [ 0, 0 ]		- below req size, req size  (parent box size of 300 results in second child being at the bottom of a larger box)
		// 	boxAllocation=100   ->   [ 200, 200 ] @ [ 0, 0 ]		- below req size, below req size  (parent box size of 300 results in second child being at the bottom of a larger box)
		allocYTests( new LReqBox[] { ybox( 300.0, 0.0 ),  ybox( 200.0, 0.0 ) }, new int[] { VBOTTOM, VBOTTOM },
				ybox( 300.0, 0.0 ),
				new LAllocV[] { new LAllocV( 400.0 ), new LAllocV( 300.0 ), new LAllocV( 200.0 ), new LAllocV( 100.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) } },
				new double[][] {
					new double[] { 100.0, 200.0 },
					new double[] { 0.0, 100.0 },
					new double[] { 0.0, 100.0 },
					new double[] { 0.0, 100.0 } } );

	

		// hpackY( [ <300,0,V.EXPAND>, <200,0,V.EXPAND> ] )
		// 	boxAllocation=400   ->   [ 400, 400 ] @ [ 0, 0 ]		- expansion, expansion
		// 	boxAllocation=300   ->   [ 300, 300 ] @ [ 0, 0 ]		- pref size, expansion
		// 	boxAllocation=200   ->   [ 300, 200 ] @ [ 0, 0 ]		- below req size, req size
		// 	boxAllocation=100   ->   [ 300, 200 ] @ [ 0, 0 ]		- below req size, below req size
		allocYTests( new LReqBox[] { ybox( 300.0, 0.0 ),  ybox( 200.0, 0.0 ) }, new int[] { VEXPAND, VEXPAND },
				ybox( 300.0, 0.0 ),
				new LAllocV[] { new LAllocV( 400.0 ), new LAllocV( 300.0 ), new LAllocV( 200.0 ), new LAllocV( 100.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 400.0 ), new LAllocV( 400.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 300.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 300.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 300.0 ) } },
				new double[][] {
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );

		// hpackY( [ <300,0,V.EXPAND>, <200,0,V.EXPAND> ] )
		// 	boxAllocation=200:200   ->   [ 400, 400 ] @ [ 0, 0 ]		- expansion, expansion
		// 	boxAllocation=150:150   ->   [ 300, 300 ] @ [ 0, 0 ]		- pref size, expansion
		// 	boxAllocation=100:100   ->   [ 300, 200 ] @ [ 0, 0 ]		- below req size, req size
		// 	boxAllocation=50:50       ->   [ 300, 200 ] @ [ 0, 0 ]		- below req size, below req size
		allocYTests( new LReqBox[] { ybox( 300.0, 0.0 ),  ybox( 200.0, 0.0 ) }, new int[] { VEXPAND, VEXPAND },
				ybox( 300.0, 0.0 ),
				new LAllocV[] { new LAllocV( 400.0, 200.0 ), new LAllocV( 300.0, 150.0 ), new LAllocV( 200.0, 100.0 ), new LAllocV( 100.0, 50.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 400.0 ), new LAllocV( 400.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 300.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 300.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 300.0 ) } },
				new double[][] {
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );

		// vpackX( [ <150:150,V.EXPAND>, <100:100,V.EXPAND> ] )
		// 	boxAllocation=200:200   ->   [ 400, 400 ] @ [ 0, 0 ]		- no expansion, no expansion
		// 	boxAllocation=150:150   ->   [ 300, 300 ] @ [ 0, 0 ]		- req size, no expansion
		// 	boxAllocation=100:100   ->   [ 300, 300 ] @ [ 0, 0 ]		- below req size, req size
		// 	boxAllocation=50:50       ->   [ 300, 300 ] @ [ 0, 0 ]		- below req size, below req size
		allocYTests( new LReqBox[] { yrbox( 300.0, 0.0, 150.0 ),  yrbox( 200.0, 0.0, 100.0 ) },  new int[] { VEXPAND, VEXPAND },
				ybox( 300.0, 0.0 ),
				new LAllocV[] { new LAllocV( 400.0, 200.0 ), new LAllocV( 300.0, 150.0 ), new LAllocV( 200.0, 100.0 ), new LAllocV( 100.0, 50.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 400.0, 200.0 ), new LAllocV( 400.0, 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0, 150.0 ), new LAllocV( 300.0, 150.0 ) },
					new LAllocV[] { new LAllocV( 300.0, 150.0 ), new LAllocV( 300.0, 150.0 ) },
					new LAllocV[] { new LAllocV( 300.0, 150.0 ), new LAllocV( 300.0, 150.0 ) } },
				new double[][] {
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );

	
	
		
		// Baselines mode; ensure that distance is distributed equally between ascent and descent
		// vpackX( [ <300,0,V.BASELINES>, <200,0,V.BASELINES> ] )
		// 	boxAllocation=400   ->   [ 300, 200 ] @ [ 50, 100 ]	- no expansion, no expansion
		// 	boxAllocation=300   ->   [ 300, 200 ] @ [ 0, 50 ]		- req size, no expansion
		// 	boxAllocation=200   ->   [ 300, 200 ] @ [ 0, 50 ]		- below req size, req size  (parent box size of 300 results in second child being centred in a larger box)
		// 	boxAllocation=100   ->   [ 300, 200 ] @ [ 0, 50 ]		- below req size, below req size  (parent box size of 300 results in second child being centred in a larger box)
		allocYTests( new LReqBox[] { ybox( 300.0, 0.0 ),  ybox( 200.0, 0.0 ) }, new int[] { VREFY, VREFY },
				yrbox( 300.0, 0.0, 150.0 ),
				new LAllocV[] { new LAllocV( 400.0 ), new LAllocV( 300.0 ), new LAllocV( 200.0 ), new LAllocV( 100.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 200.0 ) } },
				new double[][] {
					new double[] { 50.0, 100.0 },
					new double[] { 0.0, 50.0 },
					new double[] { 0.0, 50.0 },
					new double[] { 0.0, 50.0 } } );

	
		
		// hpackY( [ <300:200,0,V.BASELINES>, <200:300,0,V.BASELINES> ] )
		// 	boxAllocation=800          ->   [ 300:200, 200:300 ] @ [ 100, 200 ]		- centre, centre
		// 	boxAllocation=600          ->   [ 300:200, 200:300 ] @ [ 0, 100 ]		- matches parent box req size
		// 	boxAllocation=300          ->   [ 300:200, 200:300 ] @ [ 0, 100 ]		- below parent box req size
		// 	boxAllocation=350:550   ->   [ 300:200, 200:300 ] @ [ 50, 150 ]		- centred around main baseline
		allocYTests( new LReqBox[] { yrbox( 500.0, 0.0, 300.0 ),  yrbox( 500.0, 0.0, 200.0 ) }, new int[] { VREFY, VREFY },
				yrbox( 600.0, 0.0, 300.0 ),
				new LAllocV[] { new LAllocV( 800.0 ), new LAllocV( 600.0 ), new LAllocV( 300.0 ), new LAllocV( 900.0, 350.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 500.0, 300.0 ), new LAllocV( 500.0, 200.0 ) },
					new LAllocV[] { new LAllocV( 500.0, 300.0 ), new LAllocV( 500.0, 200.0 ) },
					new LAllocV[] { new LAllocV( 500.0, 300.0 ), new LAllocV( 500.0, 200.0 ) },
					new LAllocV[] { new LAllocV( 500.0, 300.0 ), new LAllocV( 500.0, 200.0 ) }},
				new double[][] {
					new double[] { 100.0, 200.0 },
					new double[] { 0.0, 100.0 },
					new double[] { 0.0, 100.0 },
					new double[] { 50.0, 150.0 } } );

	
	
		// hpackY( [ <300:200,0,V.BASELINES>, <200:300,0,V.BASELINES>, <400,0,V.BASELINES> ] )
		// 	boxAllocation=800   ->   [ 500, 500, 400 ] @ [ 100, 200, 200 ]		- centre, cetnre
		// 	boxAllocation=600   ->   [ 500, 500, 400 ] @ [ 0, 100, 100 ]			- matches parent box req size
		// 	boxAllocation=300   ->   [ 500, 500, 400 ] @ [ 0, 100, 100 ]			- below parent box req size
		allocYTests( new LReqBox[] { yrbox( 500.0, 0.0, 300.0 ),  yrbox( 500.0, 0.0, 200.0 ),  ybox( 400.0, 0.0 ) }, new int[] { VREFY, VREFY, VREFY },
				yrbox( 600.0, 0.0, 300.0 ),
				new LAllocV[] { new LAllocV( 800.0 ), new LAllocV( 600.0 ), new LAllocV( 300.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 500.0, 300.0 ), new LAllocV( 500.0, 200.0 ), new LAllocV( 400.0 ) },
					new LAllocV[] { new LAllocV( 500.0, 300.0 ), new LAllocV( 500.0, 200.0 ), new LAllocV( 400.00 ) },
					new LAllocV[] { new LAllocV( 500.0, 300.0 ), new LAllocV( 500.0, 200.0 ), new LAllocV( 400.0 ) } },
				new double[][] {
					new double[] { 100.0, 200.0, 200.0 },
					new double[] { 0.0, 100.0, 100.0 },
					new double[] { 0.0, 100.0, 100.0 } } );

	
	
	
		// hpackY( [ <300:200,0,V.BASELINES_EXPAND>, <200:300,0,V.BASELINES_EXPAND> ] )
		// 	boxAllocation=800          ->   [ 400:400, 400:400 ] @ [ 0, 0 ]		- centre, centre
		// 	boxAllocation=600          ->   [ 300:300, 300:300 ] @ [ 0, 0 ]		- matches parent box req size
		// 	boxAllocation=300          ->   [ 300:300, 300:300 ] @ [ 0, 0 ]		- below parent box req size
		// 	boxAllocation=350:550   ->   [ 350:550, 350:550 ] @ [ 0, 0 ]		- expand, expand
		allocYTests( new LReqBox[] { yrbox( 500.0, 0.0, 300.0 ),  yrbox( 500.0, 0.0, 200.0 ) }, new int[] { VREFY_EXPAND, VREFY_EXPAND },
				yrbox( 600.0, 0.0, 300.0 ),
				new LAllocV[] { new LAllocV( 800.0 ), new LAllocV( 600.0 ), new LAllocV( 300.0 ), new LAllocV( 900.0, 350.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 800.0, 400.0 ), new LAllocV( 800.0, 400.0 ) },
					new LAllocV[] { new LAllocV( 600.0, 300.0 ), new LAllocV( 600.0, 300.0 ) },
					new LAllocV[] { new LAllocV( 600.0, 300.0 ), new LAllocV( 600.0, 300.0 ) },
					new LAllocV[] { new LAllocV( 900.0, 350.0 ), new LAllocV( 900.0, 350.0 ) } },
				new double[][] {
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );
	}
}
