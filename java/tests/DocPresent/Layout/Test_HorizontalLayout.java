//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Layout;

import BritefuryJ.DocPresent.Layout.BoxPackingParams;
import BritefuryJ.DocPresent.Layout.HorizontalLayout;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.VAlignment;

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
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] {},  0.0, null );
		assertEquals( result, new LReqBox() );

		// requisitionX( [ <0,0> ] )  ->  <0,0>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { new LReqBox() },  0.0, null );
		assertEquals( result, new LReqBox() );

		// requisitionX( [ <0,0>:pad=1 ] )  ->  <2,0>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { new LReqBox() },  0.0, new BoxPackingParams[] { new BoxPackingParams( 1.0 ) } );
		assertEquals( result, xbox( 2.0, 0.0 ) );

		// requisitionX( [ <10,0>:pad=2 ] )  ->  <14,0>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 0.0 ) },  0.0, new BoxPackingParams[] { new BoxPackingParams( 2.0 ) } );
		assertEquals( result, xbox( 14.0, 0.0 ) );

		// Padding 'consumes' h-spacing
		// requisitionX( [ <10,1>:pad=2 ] )  ->  <14,0>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 1.0 ) },  0.0, new BoxPackingParams[] { new BoxPackingParams( 2.0 ) } );
		assertEquals( result, xbox( 14.0, 0.0 ) );

		// Padding 'consumes' all h-spacing
		// requisitionX( [ <10,3>:pad=2 ] )  ->  <14,1>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 3.0 ) },  0.0, new BoxPackingParams[] { new BoxPackingParams( 2.0 ) } );
		assertEquals( result, xbox( 14.0, 1.0 ) );

		// requisitionX( [ <0,0>, <0,0> ] )  ->  <0,0>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { new LReqBox(), new LReqBox() },  0.0, null );
		assertEquals( result, new LReqBox() );

		// Width accumulates
		// requisitionX( [ <10,0>, <5,0> ] )  ->  <15,0>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 0.0 ), xbox( 5.0, 0.0 ) },  0.0, null );
		assertEquals( result, xbox( 15.0, 0.0 ) );

		// H-spacing of child puts space before next child
		// requisitionX( [ <10,2>, <5,0> ] )  ->  <17,0>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 2.0 ), xbox( 5.0, 0.0 ) },  0.0, null );
		assertEquals( result, xbox( 17.0, 0.0 ) );

		// H-spacing of last child gets put onto the result
		// requisitionX( [ <10,2>, <5,1> ] )  ->  <17,1>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 2.0 ), xbox( 5.0, 1.0 ) },  0.0, null );
		assertEquals( result, xbox( 17.0, 1.0 ) );

		// Spacing between children adds extra width
		// requisitionX( [ <0,0>, <0,0> ], spacing=1 )  ->  <1,0>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { new LReqBox(), new LReqBox() },  1.0, null );
		assertEquals( result, xbox( 1.0, 0.0 ) );
		// requisitionX( [ <10,0>, <5,0> ], spacing=1 )  ->  <15,0>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 0.0 ), xbox( 5.0, 0.0 ) },  1.0, null );
		assertEquals( result, xbox( 16.0, 0.0 ) );

		// Spacing between children is added to the child's own spacing
		// requisitionX( [ <10,2>, <5,1> ], spacing=1 )  ->  <18,1>
		HorizontalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 2.0 ), xbox( 5.0, 1.0 ) },  1.0, null );
		assertEquals( result, xbox( 18.0, 1.0 ) );
	}




	public void test_requisitionY()
	{
		LReqBox result = new LReqBox();
		
		
		// First, test the simple cases of no baseline alignment, or with baseline alignment with children that do not have baselines
		
		// Empty list should result in empty
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] {}, VAlignment.CENTRE );
		assertEquals( result, new LReqBox() );
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] {}, VAlignment.BASELINES );
		assertEquals( result, new LReqBox() );

		// List of one empty box should result in empty
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { new LReqBox() }, VAlignment.CENTRE );
		assertEquals( result, new LReqBox() );
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { new LReqBox() }, VAlignment.BASELINES );
		assertEquals( result, new LReqBox() );

		// 1 Box of height 1 should result in same
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 1.0, 0.0 ) }, VAlignment.CENTRE );
		assertEquals( result, ybox( 1.0, 0.0 ) );
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 1.0, 0.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybox( 1.0, 0.0 ) );

		// 1 Box of height 10, vspacing 1 should result in same
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 1.0 ) }, VAlignment.CENTRE );
		assertEquals( result, ybox( 10.0, 1.0 ) );
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 1.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybox( 10.0, 1.0 ) );

		// requisitionY( [ <10,1>, <20,1> ] )  ->  <20,1>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 1.0),  ybox( 20.0, 1.0 ) }, VAlignment.CENTRE );
		assertEquals( result, ybox( 20.0, 1.0 ) );
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 1.0),  ybox( 20.0, 1.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybox( 20.0, 1.0 ) );

		// requisitionY( [ <1,10>, <2,20> ] )  ->  <2,20>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 1.0, 10.0 ),  ybox( 2.0, 20.0 ) }, VAlignment.CENTRE );
		assertEquals( result, ybox( 2.0, 20.0 ) );
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 1.0, 10.0 ),  ybox( 2.0, 20.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybox( 2.0, 20.0 ) );

		// requisitionY( [ <10,3>, <11,1> ] )  ->  <11,2>
		// The first box advances X the most overall, although the second has the greater height
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 3.0 ),  ybox( 11.0, 1.0 ) }, VAlignment.CENTRE );
		assertEquals( result, ybox( 11.0, 2.0 ) );
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 3.0 ),  ybox( 11.0, 1.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybox( 11.0, 2.0 ) );

		// requisitionY( [ <10,5>, <5,10> ] )  ->  <10,5>
		// Both advance X by the same amount (15 units), but the first has the greater height
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 5.0 ),  ybox( 5.0, 10.0 ) }, VAlignment.CENTRE );
		assertEquals( result, ybox( 10.0, 5.0 ) );
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 5.0 ),  ybox( 5.0, 10.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybox( 10.0, 5.0 ) );

	
	
	
		// Now test the cases where baselines are used
		
		// 1 Box of 3:2 should result in same
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybbox( 3.0, 2.0, 0.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 3.0, 2.0, 0.0 ) );

		// 1 Box of height 3:2, vspacing 1 should result in same
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybbox( 3.0, 2.0, 1.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 3.0, 2.0, 1.0 ) );

		// requisitionY( [ <5:3,0>, <2:4,0> ] )  ->  <5:4,0>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybbox( 5.0, 3.0, 0.0 ),  ybbox( 2.0, 4.0, 0.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 5.0, 4.0, 0.0 ) );

		// requisitionY( [ <5:3,1>, <2:4,1> ] )  ->  <5:4,1>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybbox( 5.0, 3.0, 1.0 ),  ybbox( 2.0, 4.0, 1.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 5.0, 4.0, 1.0 ) );

		// requisitionY( [ <5:3,1>, <2:4,2> ] )  ->  <5:4,2>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybbox( 5.0, 3.0, 1.0 ),  ybbox( 2.0, 4.0, 2.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 5.0, 4.0, 2.0 ) );

		// requisitionY( [ <5:3,3>, <2:4,1> ] )  ->  <5:4,2>
		// The first box advances Y (below baseline) the most overall, although the second has the greater descent
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybbox( 5.0, 3.0, 3.0 ),  ybbox( 2.0, 4.0, 1.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 5.0, 4.0, 2.0 ) );

		// requisitionY( [ <2:4,2>, <5:2,4> ] )  ->  <5:4,2>
		// Both advance T (below baseline) by the same amount (6 units), but the first has the greater descent
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybbox( 2.0, 4.0, 2.0 ),  ybbox( 5.0, 2.0, 4.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 5.0, 4.0, 2.0 ) );
		
		
		
		// Now test the situation where baseline alignment is used, but the some children do not have baselines

		// requisitionY( [ <6:3,0>, <8,0> ] )  ->  requisitionY( [ <6:3,0>, <4:4,0> ] )  ->  <6:4,0>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybbox( 6.0, 3.0, 0.0 ),  ybox( 8.0, 0.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 6.0, 4.0, 0.0 ) );

		// requisitionY( [ <6:3,3>, <8,1> ] )  ->  requisitionY( [ <6:3,3>, <4:4,1> ] )  ->  <6:4,2>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybbox( 6.0, 3.0, 3.0 ),  ybox( 8.0, 1.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 6.0, 4.0, 2.0 ) );

		// requisitionY( [ <6:3,1>, <8,2> ] )  ->  requisitionY( [ <6:3,1>, <4:4,2> ] )  ->  <6:4,2>
		HorizontalLayout.computeRequisitionY( result, new LReqBox[] { ybbox( 6.0, 3.0, 1.0 ),  ybox( 8.0, 2.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 6.0, 4.0, 2.0 ) );
	}







	//
	//
	// SPACE ALLOCATION TESTS
	//
	//

	private void allocXSpaceTest(LReqBox children[], double spacing, BoxPackingParams packingParams[], LReqBox expectedBox, double boxAllocation, double expectedSpaceAllocation[])
	{ 
		LReqBox box = new LReqBox();
		LAllocBox boxAlloc = new LAllocBox( null );
		LAllocBox childrenAlloc[] = new LAllocBox[children.length];
		for (int i = 0; i < children.length; i++)
		{
			childrenAlloc[i] = new LAllocBox( null );
		}
		
		HorizontalLayout.computeRequisitionX( box, children, spacing, packingParams );
		
		assertBoxesEqual( box, expectedBox, "PARENT BOX" );

		boxAlloc.setAllocationX( boxAllocation );
		HorizontalLayout.allocateSpaceX( box, children, boxAlloc, childrenAlloc, packingParams );
		for (int i = 0; i < children.length; i++)
		{
			if ( childrenAlloc[i].getAllocationX() != expectedSpaceAllocation[i] )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSpaceAllocation[i] + ", result=" + childrenAlloc[i].getAllocationX() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( childrenAlloc[i].getAllocationX(), expectedSpaceAllocation[i] );
		}
	}

	
	private void allocXSpaceTests(LReqBox children[], double spacing, BoxPackingParams packingParams[], LReqBox expectedBox, double boxAllocations[], double expectedSpaceAllocations[][])
	{
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			allocXSpaceTest( children, spacing, packingParams, expectedBox, boxAllocations[i], expectedSpaceAllocations[i] );
		}
	}



	public void test_allocateWidth()
	{
		// We need to test for the following conditions:
		//	- allocation < minimum
		//		- spacing and padding have no effect
		//		- expand has no effect
		//	- allocation == minimum
		//		- allocation must include spacing and padding
		//		- expand has no effect
		//	- minimum < allocation < preferred
		//		- allocation must include spacing and padding
		//		- expand has no effect
		//	- allocation == preferred
		//		- allocation must include spacing and padding
		//		- expand has no effect
		//	- allocation > preferred
		//		- allocation must include spacing and padding
		//		- expansion distributed among children		
		
		
		// hpackXSpace( [ <100-200,0-0> ], spacing=0, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200 ]		- no expansion
		// 	boxAllocation=200   ->   [ 200 ]		- all allocated to 1 child
		// 	boxAllocation=150   ->   [ 150 ]		- all allocated to 1 child
		// 	boxAllocation=100   ->   [ 100 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 100 ]		- will not go below minimum
		// No padding, no expand
		allocXSpaceTests( new LReqBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ) }, 0.0, null,
				xbox( 100.0, 200.0, 0.0, 0.0 ),
				new double[] { 300.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 200.0 },
					new double[] { 200.0 },
					new double[] { 150.0 },
					new double[] { 100.0 },
					new double[] { 100.0 } } );
		
		
		// hpackXSpace( [ <100-200,0-0> ], spacing=0, padding=10, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200 ]		- no expansion
		// 	boxAllocation=220   ->   [ 200 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=200   ->   [ 180 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=150   ->   [ 130 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=120   ->   [ 100 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=100   ->   [ 100 ]		- will not go below minimum, 20 to padding
		// 	boxAllocation=50   ->   [ 100 ]		- will not go below minimum, 20 to padding
		// 10 padding, no expand
		allocXSpaceTests( new LReqBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( 10.0 ) },
				xbox( 120.0, 220.0, 0.0, 0.0 ),
				new double[] { 300.0, 220.0, 200.0, 150.0, 120.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 200.0 },
					new double[] { 200.0 },
					new double[] { 180.0 },
					new double[] { 130.0 },
					new double[] { 100.0 },
					new double[] { 100.0 },
					new double[] { 100.0 } } );
		
		
		// hpackXSpace( [ <100-200,0-0> ], spacing=0, padding=0, packFlags=EXPAND )
		// 	boxAllocation=300   ->   [ 300 ]		- expansion; extra space allocated to child
		// 	boxAllocation=200   ->   [ 200 ]		- all allocated to 1 child
		// 	boxAllocation=150   ->   [ 150 ]		- all allocated to 1 child
		// 	boxAllocation=100   ->   [ 100 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 100 ]		- will not go below minimum
		// No padding, expand
		allocXSpaceTests( new LReqBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( true ) },
				xbox( 100.0, 200.0, 0.0, 0.0 ),
				new double[] { 300.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 300.0 },
					new double[] { 200.0 },
					new double[] { 150.0 },
					new double[] { 100.0 },
					new double[] { 100.0 } } );


		// hpackXSpace( [ <100-200,0-0> ], spacing=0, padding=10, packFlags=EXPAND )
		// 	boxAllocation=300   ->   [ 280 ]		- expansion; extra space allocated to child, 20 go to padding
		// 	boxAllocation=220   ->   [ 200 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=200   ->   [ 180 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=150   ->   [ 130 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=120   ->   [ 100 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=100   ->   [ 100 ]		- will not go below minimum, 20 to padding
		// 	boxAllocation=50   ->   [ 100 ]		- will not go below minimum, 20 to padding
		// 10 padding, expand
		allocXSpaceTests( new LReqBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( 10.0, true ) },
				xbox( 120.0, 220.0, 0.0, 0.0 ),
				new double[] { 300.0, 220.0, 200.0, 150.0, 120.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 280.0 },
					new double[] { 200.0 },
					new double[] { 180.0 },
					new double[] { 130.0 },
					new double[] { 100.0 },
					new double[] { 100.0 },
					new double[] { 100.0 } } );

		
		// h-spacing applied to 1 child should not make a difference, since it is the last child
		// hpackXSpace( [ <100-200,10-10> ], spacing=0, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200 ]		- no expansion
		// 	boxAllocation=200   ->   [ 200 ]		- all allocated to 1 child
		// 	boxAllocation=150   ->   [ 150 ]		- all allocated to 1 child
		// 	boxAllocation=100   ->   [ 100 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 100 ]		- will not go below minimum
		allocXSpaceTests( new LReqBox[] { xbox( 100.0, 200.0, 10.0, 10.0 ) }, 0.0, null,
				xbox( 100.0, 200.0, 10.0, 10.0 ),
				new double[] { 300.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 200.0 },
					new double[] { 200.0 },
					new double[] { 150.0 },
					new double[] { 100.0 },
					new double[] { 100.0 } } );
		

		
		
		
		
		
		
		
		// hpackXSpace( [ <100-200,0-0>, <50-70,0-0> ], spacing=0, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200, 70 ]		- no expansion
		// 	boxAllocation=270   ->   [ 200, 70 ]		- preferred sizes
		// 	boxAllocation=210   ->   [ 150, 60 ]		- space above minimum distributed evenly
		// 	boxAllocation=150   ->   [ 100, 50 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ]		- will not go below minimum
		allocXSpaceTests( new LReqBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 0.0, null,
				xbox( 150.0, 270.0, 0.0, 0.0 ),
				new double[] { 300.0, 270.0, 210.0, 150.0, 100.0 },
				new double[][] {
					new double[] { 200.0, 70.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } } );
		
		
		// hpackXSpace( [ <100-200,0-0>, <50-70,0-0> ], spacing=0, padding=0, packFlags=[ EXPAND, 0 ] )
		// 	boxAllocation=300   ->   [ 230, 70 ]		- space above preferred goes to first child, none to second
		// 	boxAllocation=270   ->   [ 200, 70 ]		- preferred sizes
		// 	boxAllocation=210   ->   [ 150, 60 ]		- space above minimum distributed evenly
		// 	boxAllocation=150   ->   [ 100, 50 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ]		- will not go below minimum
		allocXSpaceTests( new LReqBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( true ), new BoxPackingParams( false ) },
				xbox( 150.0, 270.0, 0.0, 0.0 ),
				new double[] { 300.0, 270.0, 210.0, 150.0, 100.0 },
				new double[][] {
					new double[] { 230.0, 70.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } } );
		

		// hpackXSpace( [ <100-200,0-0>, <50-70,0-0> ], spacing=0, padding=0, packFlags=[ 0, EXPAND ] )
		// 	boxAllocation=300   ->   [ 200, 100 ]		- space above preferred goes to secnd child, none to first
		// 	boxAllocation=270   ->   [ 200, 70 ]		- preferred sizes
		// 	boxAllocation=210   ->   [ 150, 60 ]		- space above minimum distributed evenly
		// 	boxAllocation=150   ->   [ 100, 50 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ]		- will not go below minimum
		allocXSpaceTests( new LReqBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( false ), new BoxPackingParams( true ) },
				xbox( 150.0, 270.0, 0.0, 0.0 ),
				new double[] { 300.0, 270.0, 210.0, 150.0, 100.0 },
				new double[][] {
					new double[] { 200.0, 100.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } } );
		

		// hpackXSpace( [ <100-200,0-0>, <50-70,0-0> ], spacing=0, padding=0, packFlags=[ EXPAND, EXPAND ] )
		// 	boxAllocation=300   ->   [ 215, 85 ]		- space above preferred gets distributed between both children
		// 	boxAllocation=270   ->   [ 200, 70 ]		- preferred sizes
		// 	boxAllocation=210   ->   [ 150, 60 ]		- space above minimum distributed evenly
		// 	boxAllocation=150   ->   [ 100, 50 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ]		- will not go below minimum
		allocXSpaceTests( new LReqBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( true ), new BoxPackingParams( true ) },
				xbox( 150.0, 270.0, 0.0, 0.0 ),
				new double[] { 300.0, 270.0, 210.0, 150.0, 100.0 },
				new double[][] {
					new double[] { 215.0, 85.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } } );
	}







	//
	//
	// ALLOCATION TESTS
	//
	//

	private void allocXTest(LReqBox children[], double spacing, BoxPackingParams packingParams[], LReqBox expectedBox, double boxAllocation, double expectedSize[], double expectedPosition[])
	{ 
		LReqBox box = new LReqBox();
		LAllocBox boxAlloc = new LAllocBox( null );
		LAllocBox childrenAlloc[] = new LAllocBox[children.length];
		for (int i = 0; i < children.length; i++)
		{
			childrenAlloc[i] = new LAllocBox( null );
		}

		HorizontalLayout.computeRequisitionX( box, children, spacing, packingParams );

		assertBoxesEqual( box, expectedBox, "PARENT BOX" );

		boxAlloc.setAllocationX( boxAllocation );
		HorizontalLayout.allocateX( box, children, boxAlloc, childrenAlloc, spacing, packingParams );
		for (int i = 0; i < children.length; i++)
		{
			if ( childrenAlloc[i].getAllocationX() != expectedSize[i] )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSize[i] + ", result=" + childrenAlloc[i].getAllocationX() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( childrenAlloc[i].getAllocationX(), expectedSize[i] );

			if ( childrenAlloc[i].getPositionInParentSpaceX() != expectedPosition[i] )
			{
				System.out.println( "Child position for " + i + " is not as expected; expected=" + expectedPosition[i] + ", result=" + childrenAlloc[i].getPositionInParentSpaceX() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( childrenAlloc[i].getPositionInParentSpaceX(), expectedPosition[i] );
		}
	}
	
	private void allocXTests(LReqBox children[], double spacing, BoxPackingParams packingParams[], LReqBox expectedBox, double boxAllocations[], double expectedSize[][], double expectedPosition[][])
	{
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			allocXTest( children, spacing, packingParams, expectedBox, boxAllocations[i], expectedSize[i], expectedPosition[i] );
		}
	}



	public void test_allocateX()
	{
		// hpackX( [ <100-200,0-0>, <50-70,0-0> ], spacing=0, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200, 70 ] @ [ 0, 200 ]		- no expansion
		// 	boxAllocation=270   ->   [ 200, 70 ] @ [ 0, 200 ]		- preferred sizes
		// 	boxAllocation=210   ->   [ 150, 60 ] @ [ 0, 150 ]		- space above minimum distributed evenly
		// 	boxAllocation=150   ->   [ 100, 50 ] @ [ 0, 100 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 100 ]		- will not go below minimum
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 0.0, null,
				xbox( 150.0, 270.0, 0.0, 0.0 ),
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

	
		// hpackX( [ <100-200,0-0>, <50-70,0-0> ], spacing=10, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200, 70 ] @ [ 0, 210 ]		- no expansion
		// 	boxAllocation=280   ->   [ 200, 70 ] @ [ 0, 210 ]		- preferred sizes
		// 	boxAllocation=220   ->   [ 150, 60 ] @ [ 0, 160 ]		- space above minimum distributed evenly
		// 	boxAllocation=160   ->   [ 100, 50 ] @ [ 0, 110 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 110 ]		- will not go below minimum
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 10.0, null,
				xbox( 160.0, 280.0, 0.0, 0.0 ),
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

	
		// hpackX( [ <100-200,0-0>, <50-70,0-0> ], spacing=0, padding=[ 10, 20 ], packFlags=0 )
		// 	boxAllocation=400   ->   [ 200, 70 ] @ [ 0, 240 ]		- no expansion
		// 	boxAllocation=330   ->   [ 200, 70 ] @ [ 0, 240 ]		- preferred sizes
		// 	boxAllocation=270   ->   [ 150, 60 ] @ [ 0, 190 ]		- space above minimum distributed evenly
		// 	boxAllocation=210   ->   [ 100, 50 ] @ [ 0, 140 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 140 ]		- will not go below minimum
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( 10.0 ), new BoxPackingParams( 20.0 ) },
				xbox( 210.0, 330.0, 0.0, 0.0 ),
				new double[] { 400.0, 330.0, 270.0, 210.0, 100.0 },
				new double[][] {
					new double[] { 200.0, 70.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } },
				new double[][] {
					new double[] { 10.0, 240.0 },
					new double[] { 10.0, 240.0 },
					new double[] { 10.0, 190.0 },
					new double[] { 10.0, 140.0 },
					new double[] { 10.0, 140.0 } } );

		
		// hpackX( [ <100-200,15-15>, <50-70,0-0> ], spacing=0, padding=[ 10, 20 ], packFlags=0 )
		// 	boxAllocation=400   ->   [ 200, 70 ] @ [ 0, 245 ]		- no expansion
		// 	boxAllocation=335   ->   [ 200, 70 ] @ [ 0, 245 ]		- preferred sizes
		// 	boxAllocation=275   ->   [ 150, 60 ] @ [ 0, 195 ]		- space above minimum distributed evenly
		// 	boxAllocation=215   ->   [ 100, 50 ] @ [ 0, 145 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 145 ]		- will not go below minimum
		allocXTests( new LReqBox[] { xbox( 100.0, 200.0, 15.0, 15.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( 10.0 ), new BoxPackingParams( 20.0 ) },
				xbox( 215.0, 335.0, 0.0, 0.0 ),
				new double[] { 400.0, 335.0, 275.0, 215.0, 100.0 },
				new double[][] {
					new double[] { 200.0, 70.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } },
				new double[][] {
					new double[] { 10.0, 245.0 },
					new double[] { 10.0, 245.0 },
					new double[] { 10.0, 195.0 },
					new double[] { 10.0, 145.0 },
					new double[] { 10.0, 145.0 } } );
	}






	private void allocYTest(LReqBox children[], VAlignment alignment, LReqBox expectedBox, double boxAllocation, double expectedSize[], double expectedPosition[])
	{ 
		LReqBox box = new LReqBox();
		LAllocBox boxAlloc = new LAllocBox( null );
		LAllocBox childrenAlloc[] = new LAllocBox[children.length];
		for (int i = 0; i < children.length; i++)
		{
			childrenAlloc[i] = new LAllocBox( null );
		}

		
		HorizontalLayout.computeRequisitionY( box, children, alignment );

		assertBoxesEqual( box, expectedBox, "PARENT BOX" );

		boxAlloc.setAllocationY( boxAllocation );
		HorizontalLayout.allocateY( box, children, boxAlloc, childrenAlloc, alignment );
		for (int i = 0; i < children.length; i++)
		{
			if ( childrenAlloc[i].getAllocationY() != expectedSize[i] )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSize[i] + ", result=" + childrenAlloc[i].getAllocationY() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( childrenAlloc[i].getAllocationY(), expectedSize[i] );

			if ( childrenAlloc[i].getPositionInParentSpaceY() != expectedPosition[i] )
			{
				System.out.println( "Child position for " + i + " is not as expected; expected=" + expectedPosition[i] + ", result=" + childrenAlloc[i].getPositionInParentSpaceY() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( childrenAlloc[i].getPositionInParentSpaceY(), expectedPosition[i] );
		}
	}
	
	private void allocYTests(LReqBox children[], VAlignment alignment, LReqBox expectedBox, double boxAllocations[], double expectedSize[][], double expectedPosition[][])
	{
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			allocYTest( children, alignment, expectedBox, boxAllocations[i], expectedSize[i], expectedPosition[i] );
		}
	}



	public void test_hpackY()
	{
		// vpackX( [ <300,0>, <200,0> ], alignment=TOP )
		// 	boxAllocation=400   ->   [ 300, 200 ] @ [ 0, 0 ]		- no expansion, no expansion
		// 	boxAllocation=300   ->   [ 300, 200 ] @ [ 0, 0 ]		- req size, no expansion
		// 	boxAllocation=200   ->   [ 300, 200 ] @ [ 0, 0 ]		- below req size, req size
		// 	boxAllocation=100   ->   [ 300, 200 ] @ [ 0, 0 ]		- below req size, below req size
		allocYTests( new LReqBox[] { ybox( 300.0, 0.0 ),  ybox( 200.0, 0.0 ) }, VAlignment.TOP,
				ybox( 300.0, 0.0 ),
				new double[] { 400.0, 300.0, 200.0, 100.0 },
				new double[][] {
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 } },
				new double[][] {
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );

	
	
		// vpackX( [ <300,0>, <200,0> ], alignment=CENTRE )
		// 	boxAllocation=400   ->   [ 300, 200 ] @ [ 50, 100 ]	- no expansion, no expansion
		// 	boxAllocation=300   ->   [ 300, 200 ] @ [ 0, 50 ]		- req size, no expansion
		// 	boxAllocation=200   ->   [ 200, 200 ] @ [ 0, 50 ]		- below req size, req size  (parent box size of 300 results in second child being centred in a larger box)
		// 	boxAllocation=100   ->   [ 200, 200 ] @ [ 0, 50 ]		- below req size, below req size  (parent box size of 300 results in second child being centred in a larger box)
		allocYTests( new LReqBox[] { ybox( 300.0, 0.0 ),  ybox( 200.0, 0.0 ) }, VAlignment.CENTRE,
				ybox( 300.0, 0.0 ),
				new double[] { 400.0, 300.0, 200.0, 100.0 },
				new double[][] {
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 } },
				new double[][] {
					new double[] { 50.0, 100.0 },
					new double[] { 0.0, 50.0 },
					new double[] { 0.0, 50.0 },
					new double[] { 0.0, 50.0 } } );

		
		
		// vpackX( [ <300,0>, <200,0> ], alignment=BOTTOM )
		// 	boxAllocation=400   ->   [ 300, 200 ] @ [ 100, 200 ]	- no expansion, no expansion
		// 	boxAllocation=300   ->   [ 300, 200 ] @ [ 0, 100 ]		- req size, no expansion
		// 	boxAllocation=200   ->   [ 200, 200 ] @ [ 0, 0 ]		- below req size, req size  (parent box size of 300 results in second child being at the bottom of a larger box)
		// 	boxAllocation=100   ->   [ 200, 200 ] @ [ 0, 0 ]		- below req size, below req size  (parent box size of 300 results in second child being at the bottom of a larger box)
		allocYTests( new LReqBox[] { ybox( 300.0, 0.0 ),  ybox( 200.0, 0.0 ) }, VAlignment.BOTTOM,
				ybox( 300.0, 0.0 ),
				new double[] { 400.0, 300.0, 200.0, 100.0 },
				new double[][] {
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 } },
				new double[][] {
					new double[] { 100.0, 200.0 },
					new double[] { 0.0, 100.0 },
					new double[] { 0.0, 100.0 },
					new double[] { 0.0, 100.0 } } );

	
	
		// hpackY( [ <300,0>, <200,0> ], alignment=EXPAND )
		// 	boxAllocation=400   ->   [ 400, 400 ] @ [ 0, 0 ]		- expansion, expansion
		// 	boxAllocation=300   ->   [ 300, 300 ] @ [ 0, 0 ]		- pref size, expansion
		// 	boxAllocation=200   ->   [ 300, 200 ] @ [ 0, 0 ]		- below req size, req size
		// 	boxAllocation=100   ->   [ 300, 200 ] @ [ 0, 0 ]		- below req size, below req size
		allocYTests( new LReqBox[] { ybox( 300.0, 0.0 ),  ybox( 200.0, 0.0 ) }, VAlignment.EXPAND,
				ybox( 300.0, 0.0 ),
				new double[] { 400.0, 300.0, 200.0, 100.0 },
				new double[][] {
					new double[] { 400.0, 400.0 },
					new double[] { 300.0, 300.0 },
					new double[] { 300.0, 300.0 },
					new double[] { 300.0, 300.0 } },
				new double[][] {
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );

	
	
	
		// Ensure that 'baselines' mode acts like 'centre' mode when no children have baselines
		// vpackX( [ <300,0>, <200,0> ], alignment=BASELINES )
		// 	boxAllocation=400   ->   [ 300, 200 ] @ [ 50, 100 ]	- no expansion, no expansion
		// 	boxAllocation=300   ->   [ 300, 200 ] @ [ 0, 50 ]		- req size, no expansion
		// 	boxAllocation=200   ->   [ 200, 200 ] @ [ 0, 50 ]		- below req size, req size  (parent box size of 300 results in second child being centred in a larger box)
		// 	boxAllocation=100   ->   [ 200, 200 ] @ [ 0, 50 ]		- below req size, below req size  (parent box size of 300 results in second child being centred in a larger box)
		allocYTests( new LReqBox[] { ybox( 300.0, 0.0 ),  ybox( 200.0, 0.0 ) }, VAlignment.BASELINES,
				ybox( 300.0, 0.0 ),
				new double[] { 400.0, 300.0, 200.0, 100.0 },
				new double[][] {
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 } },
				new double[][] {
					new double[] { 50.0, 100.0 },
					new double[] { 0.0, 50.0 },
					new double[] { 0.0, 50.0 },
					new double[] { 0.0, 50.0 } } );

		
		
		// hpackY( [ <300:200,0>, <200:300,0> ], alignment=BASELINES )
		// 	boxAllocation=800   ->   [ 500, 500 ] @ [ 100, 200 ]		- centre, centre
		// 	boxAllocation=600   ->   [ 500, 500 ] @ [ 0, 100 ]			- matches parent box req size
		// 	boxAllocation=300   ->   [ 500, 500 ] @ [ 0, 100 ]			- below parent box req size
		allocYTests( new LReqBox[] { ybbox( 300.0, 200.0, 0.0 ),  ybbox( 200.0, 300.0, 0.0 ) }, VAlignment.BASELINES,
				ybbox( 300.0, 300.0, 0.0 ),
				new double[] { 800.0, 600.0, 300.0 },
				new double[][] {
					new double[] { 500.0, 500.0 },
					new double[] { 500.0, 500.0 },
					new double[] { 500.0, 500.0 } },
				new double[][] {
					new double[] { 100.0, 200.0 },
					new double[] { 0.0, 100.0 },
					new double[] { 0.0, 100.0 } } );

	
	
		// hpackY( [ <300:200,0>, <200:300,0>, <400,0> ], alignment=BASELINES )
		// 	boxAllocation=800   ->   [ 500, 500, 400 ] @ [ 100, 200, 200 ]		- centre, cetnre
		// 	boxAllocation=600   ->   [ 500, 500, 400 ] @ [ 0, 100, 100 ]			- matches parent box req size
		// 	boxAllocation=300   ->   [ 500, 500, 400 ] @ [ 0, 100, 100 ]			- below parent box req size
		allocYTests( new LReqBox[] { ybbox( 300.0, 200.0, 0.0 ),  ybbox( 200.0, 300.0, 0.0 ),  ybox( 400.0, 0.0 ) }, VAlignment.BASELINES,
				ybbox( 300.0, 300.0, 0.0 ),
				new double[] { 800.0, 600.0, 300.0 },
				new double[][] {
					new double[] { 500.0, 500.0, 400.0 },
					new double[] { 500.0, 500.0, 400.0 },
					new double[] { 500.0, 500.0, 400.0 } },
				new double[][] {
					new double[] { 100.0, 200.0, 200.0 },
					new double[] { 0.0, 100.0, 100.0 },
					new double[] { 0.0, 100.0, 100.0 } } );
	}
}
