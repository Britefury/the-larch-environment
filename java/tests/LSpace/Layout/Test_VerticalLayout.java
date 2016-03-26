//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.LSpace.Layout;

import BritefuryJ.LSpace.Layout.LAllocBox;
import BritefuryJ.LSpace.Layout.LAllocHelper;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBox;
import BritefuryJ.LSpace.Layout.VerticalLayout;

public class Test_VerticalLayout extends Test_Layout_base
{
	//
	//
	// REQUISITION TESTS
	//
	//
	
	public void test_requisitionX()
	{
		LReqBox result = new LReqBox();
		
		// Empty list should result in empty
		VerticalLayout.computeRequisitionX( result, new LReqBox[] {} );
		assertBoxesEqual( result, new LReqBox() );

		// List of one empty box should result in empty
		VerticalLayout.computeRequisitionX( result, new LReqBox[] { new LReqBox() } );
		assertBoxesEqual( result, new LReqBox() );

		// 1 Box of width 1 should result in same
		VerticalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 1.0, 1.0 ) } );
		assertBoxesEqual( result, xbox( 1.0, 1.0 ) );

		// 1 Box of width 10, advance 11 should result in same
		VerticalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 11.0 ) } );
		assertBoxesEqual( result, xbox( 10.0, 11.0 ) );

		// requisitionX( [ <10,11>, <20,21> ] )  ->  <20,21>
		VerticalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 11.0 ),  xbox( 20.0, 21.0 ) } );
		assertBoxesEqual( result, xbox( 20.0, 21.0 ) );

		// requisitionX( [ <1,11>, <2,22> ] )  ->  <2,20>
		VerticalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 1.0, 11.0 ),  xbox( 2.0, 22.0 ) } );
		assertBoxesEqual( result, xbox( 2.0, 22.0 ) );

		// requisitionX( [ <10,13>, <11,11> ] )  ->  <11,13>
		// The first box advances X the most overall, although the second has the greater width
		VerticalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 13.0 ),  xbox( 11.0, 12.0 ) } );
		assertBoxesEqual( result, xbox( 11.0, 13.0 ) );

		// requisitionX( [ <10,15>, <5,15> ] )  ->  <10,15>
		// Both advance X by the same amount (15 units), but the first has the greater width
		VerticalLayout.computeRequisitionX( result, new LReqBox[] { xbox( 10.0, 15.0 ),  xbox( 5.0, 15.0 ) } );
		assertBoxesEqual( result, xbox( 10.0, 15.0 ) );
	}

	
	
	public void test_requisitionY()
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing

		LReqBox result = new LReqBox();
		
		// requisitionY()  ->  <0,0>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] {}, 0, 0.0 );
		assertBoxesEqual( result, new LReqBox() );

		// requisitionY( [ <0,0> ] )  ->  <0,0>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { new LReqBox() },  0, 0.0 );
		assertBoxesEqual( result, new LReqBox() );

		// requisitionY( [ <10,0> ] )  ->  <0,0>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 0.0 ) },  0, 0.0 );
		assertBoxesEqual( result, ybox( 10.0, 0.0 ) );

		// Padding 'consumes' h-spacing
		// requisitionY( [ <10,1> ] )  ->  <10,1>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 1.0 ) },  0, 0.0 );
		assertBoxesEqual( result, ybox( 10.0, 1.0 ) );


		// requisitionY( [ <0,0>, <0,0> ] )  ->  <0,0>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { new LReqBox(), new LReqBox() },  0, 0.0 );
		assertBoxesEqual( result, new LReqBox() );

		// Height accumulates
		// requisitionY( [ <10,0>, <5,0> ] )  ->  <5:10,0>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 0.0 ), ybox( 5.0, 0.0 ) },  0, 0.0 );
		assertBoxesEqual( result, yrbox( 15.0, 0.0, 5.0 ) );

		// V-spacing of child puts space before next child
		// requisitionY( [ <10,2>, <5,0> ] )  ->  <5:12,0>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 2.0 ), ybox( 5.0, 0.0 ) },  0, 0.0 );
		assertBoxesEqual( result, yrbox( 17.0, 0.0, 5.0 ) );

		// V-spacing of last child gets put onto the result
		// requisitionY( [ <10,2>, <5,1> ] )  ->  <5:12,1>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 2.0 ), ybox( 5.0, 1.0 ) },  0, 0.0 );
		assertBoxesEqual( result, yrbox( 17.0, 1.0, 5.0 ) );

		// Spacing between children adds extra height
		// requisitionY( [ <0,0>, <0,0> ], spacing=1 )  ->  <1,0>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { new LReqBox(), new LReqBox() },  0, 1.0 );
		assertBoxesEqual( result, yrbox( 1.0, 0.0, 0.0 ) );
		// requisitionY( [ <10,0>, <5,0> ], spacing=1 )  ->  <5:11,0>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 0.0 ), ybox( 5.0, 0.0 ) },  0, 1.0 );
		assertBoxesEqual( result, yrbox( 16.0, 0.0, 5.0 ) );

		// Spacing between children is added to the child's own spacing
		// requisitionY( [ <10,2>, <5,1> ], spacing=1 )  ->  <5:13,1>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 2.0 ), ybox( 5.0, 1.0 ) },  0, 1.0 );
		assertBoxesEqual( result, yrbox( 18.0, 1.0, 5.0 ) );

		
		// Now test typesetting parameter
		
		// Height accumulates
		// requisitionY( [ <10,0>, <5,0>, <10,0> ], ref_index=0 )  ->  <5:20,0>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 0.0 ), ybox( 5.0, 0.0 ), ybox( 10.0, 0.0 ) },  0, 0.0 );
		assertBoxesEqual( result, yrbox( 25.0, 0.0, 5.0 ) );

		// requisitionY( [ <10,0>, <5,0>, <10,0> ], ref_index=1 )  ->  <12.5:12.5,0>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 0.0 ), ybox( 5.0, 0.0 ), ybox( 10.0, 0.0 ) },  1, 0.0 );
		assertBoxesEqual( result, yrbox( 25.0, 0.0, 12.5 ) );

		// requisitionY( [ <10,0>, <5,0>, <10,0> ], ref_index=2 )  ->  <20:5,0>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { ybox( 10.0, 0.0 ), ybox( 5.0, 0.0 ), ybox( 10.0, 0.0 ) },  2, 0.0 );
		assertBoxesEqual( result, yrbox( 25.0, 0.0, 20.0 ) );

	
		// requisitionY( [ <7:3,0>, <3:2,0>, <6:4,0> ], ref_index=0 )  ->  <7:18,0>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { yrbox( 10.0, 0.0, 7.0 ), yrbox( 5.0, 0.0, 3.0 ), yrbox( 10.0, 0.0, 6.0 ) },  0, 0.0 );
		assertBoxesEqual( result, yrbox( 25.0, 0.0, 7.0 ) );

		// requisitionY( [ <7:3,0>, <3:2,0>, <6:4,0> ], ref_index=1 )  ->  <13:12,0>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { yrbox( 10.0, 0.0, 7.0 ), yrbox( 5.0, 0.0, 3.0 ), yrbox( 10.0, 0.0, 6.0 ) },  1, 0.0 );
		assertBoxesEqual( result, yrbox( 25.0, 0.0, 13.0 ) );

		// requisitionY( [ <7:3,0>, <3:2,0>, <6:4,0> ], ref_index=2 )  ->  <21:4,0>
		VerticalLayout.computeRequisitionY( result, new LReqBox[] { yrbox( 10.0, 0.0, 7.0 ), yrbox( 5.0, 0.0, 3.0 ), yrbox( 10.0, 0.0, 6.0 ) },  2, 0.0 );
		assertBoxesEqual( result, yrbox( 25.0, 0.0, 21.0 ) );
	}





	//
	//
	// SPACE ALLOCATION TESTS
	//
	//

	private void allocYSpaceTest(LReqBox children[], int childAllocFlags[], int refPointIndex, double spacing, LReqBox expectedBox, LAllocV boxAllocation, LAllocV expectedSpaceAllocation[])
	{ 
		LReqBox box = new LReqBox();
		LAllocBox boxAlloc = new LAllocBox( null );
		LAllocBox childrenAlloc[] = new LAllocBox[children.length];
		for (int i = 0; i < children.length; i++)
		{
			childrenAlloc[i] = new LAllocBox( null );
		}

		VerticalLayout.computeRequisitionY( box, children, refPointIndex, spacing );

		assertBoxesEqual( box, expectedBox, "PARENT BOX" );

		LAllocHelper.allocateY( boxAlloc, box, 0.0, boxAllocation );
		VerticalLayout.allocateSpaceY( box, children, boxAlloc, childrenAlloc, childAllocFlags, refPointIndex );
		for (int i = 0; i < children.length; i++)
		{
			if ( !childrenAlloc[i].getAllocV().equals( expectedSpaceAllocation[i] ) )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSpaceAllocation[i] + ", result=" + childrenAlloc[i].getAllocV() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( childrenAlloc[i].getAllocV(), expectedSpaceAllocation[i] );
		}
	}

	
	private void allocYSpaceTests(LReqBox children[], int childAllocFlags[], int refPointIndex, double spacing, LReqBox expectedBox, LAllocV boxAllocations[], LAllocV expectedSpaceAllocations[][])
	{
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			allocYSpaceTest( children, childAllocFlags, refPointIndex, spacing, expectedBox, boxAllocations[i], expectedSpaceAllocations[i] );
		}
	}







	public void test_allocateHeight()
	{
		// We need to test for the following conditions:
		//	- allocation < required
		//		- spacing and padding have no effect
		//		- expand has no effect
		//	- allocation == required
		//		- allocation must include spacing and padding
		//		- expand has no effect
		//	- allocation > required
		//		- allocation must include spacing and padding
		//		- expansion distributed among children		
		
		
		// hpackXSpace( [ <150,0> ], spacing=0 )
		// 	boxAllocation=300   ->   [ 150 ]		- no expansion
		// 	boxAllocation=150   ->   [ 150 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 150 ]		- will not go below requirement
		// No padding, no expand
		allocYSpaceTests( new LReqBox[] { ybox( 150.0, 0.0 ) }, new int[] { 0 }, 0, 0.0,
				ybox( 150.0, 0.0 ),
				new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 150.0 ), new LAllocV( 50.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 150.0 ) },
					new LAllocV[] { new LAllocV( 150.0 ) },
					new LAllocV[] { new LAllocV( 150.0 ) } } );
		
		
		// hpackXSpace( [ <75:75,0> ], spacing=0 )
		// 	boxAllocation=300   ->   [ 150 ]		- no expansion
		// 	boxAllocation=150   ->   [ 150 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 150 ]		- will not go below requirement
		// No padding, no expand
		allocYSpaceTests( new LReqBox[] { yrbox( 150.0, 0.0, 75.0 ) }, new int[] { 0 }, 0, 0.0,
				ybox( 150.0, 0.0 ),
				new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 150.0 ), new LAllocV( 50.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 150.0, 75.0 ) },
					new LAllocV[] { new LAllocV( 150.0, 75.0 ) },
					new LAllocV[] { new LAllocV( 150.0, 75.0 ) } } );
		

		// hpackXSpace( [ <150,0,V.EXPAND> ], spacing=0 )
		// 	boxAllocation=300   ->   [ 300 ]		- expansion; extra space allocated to child
		// 	boxAllocation=150   ->   [ 150 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 150 ]		- will not go below requirement
		// No padding, expand
		allocYSpaceTests( new LReqBox[] { ybox( 150.0, 0.0 ) }, new int[] { VEXPAND }, 0, 0.0,
				ybox( 150.0, 0.0 ),
				new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 150.0 ), new LAllocV( 50.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 300.0 ) },
					new LAllocV[] { new LAllocV( 150.0 ) },
					new LAllocV[] { new LAllocV( 150.0 ) } } );


		
		
		
		
		
		

		// hpackXSpace( [ <150,0>, <100,0> ], spacing=0 )
		// 	boxAllocation=300   ->   [ 150, 100 ]		- no expansion
		// 	boxAllocation=250   ->   [ 150, 100 ]		- required sizes
		// 	boxAllocation=100   ->   [ 150, 100 ]		- will not go below requirement
		allocYSpaceTests( new LReqBox[] { ybox( 150.0, 0.0 ), ybox( 100.0, 0.0 ) }, new int[] { 0, 0 }, 0, 0.0,
				yrbox( 250.0, 0.0, 75.0 ),
				new LAllocV[] { new LAllocV( 300.0 ), new LAllocV( 250.0 ), new LAllocV( 100.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 150.0 ), new LAllocV( 100.0 ) },
					new LAllocV[] { new LAllocV( 150.0 ), new LAllocV( 100.0 ) },
					new LAllocV[] { new LAllocV( 150.0 ), new LAllocV( 100.0 ) } } );


		// hpackXSpace( [ <150,0,V.EXPAND>, <100,0> ], spacing=0)
		// 	boxAllocation=300   ->   [ 200, 100 ]		- space above preferred goes to first child, none to second
		// 	boxAllocation=250   ->   [ 150, 100 ]		- required sizes
		// 	boxAllocation=100   ->   [ 150, 100 ]		- will not go below requirement
		allocYSpaceTests( new LReqBox[] { ybox( 150.0, 0.0 ), ybox( 100.0, 0.0 ) }, new int[] { VEXPAND, 0 }, 0, 0.0,
				yrbox( 250.0, 0.0, 75.0 ),
				new LAllocV[] { new LAllocV( 300.0, 100.0 ), new LAllocV( 250.0, 75.0 ), new LAllocV( 100.0, 35.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 100.0 ) },
					new LAllocV[] { new LAllocV( 150.0 ), new LAllocV( 100.0 ) },
					new LAllocV[] { new LAllocV( 150.0 ), new LAllocV( 100.0 ) } } );
		

		// hpackXSpace( [ <150,0>, <100,0,V.EXPAND> ], spacing=0 )
		// 	boxAllocation=300   ->   [ 150, 150 ]		- space above preferred goes to second child, none to first
		// 	boxAllocation=250   ->   [ 150, 100 ]		- required sizes
		// 	boxAllocation=100   ->   [ 150, 100 ]		- will not go below requirement
		allocYSpaceTests( new LReqBox[] { ybox( 150.0, 0.0 ), ybox( 100.0, 0.0 ) }, new int[] { 0, VEXPAND }, 0, 0.0,
				yrbox( 250.0, 0.0, 75.0 ),
				new LAllocV[] { new LAllocV( 300.0, 75.0 ), new LAllocV( 300.0, 80.0 ), new LAllocV( 250.0, 75.0 ), new LAllocV( 100.0, 35.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 150.0 ), new LAllocV( 150.0 ) },
					new LAllocV[] { new LAllocV( 150.0 ), new LAllocV( 145.0, 72.5 ) },
					new LAllocV[] { new LAllocV( 150.0 ), new LAllocV( 100.0 ) },
					new LAllocV[] { new LAllocV( 150.0 ), new LAllocV( 100.0 ) } } );
		

		// hpackXSpace( [ <150,0,V.EXPAND>, <100,0,V.EXPAND> ], spacing=0 )
		// 	boxAllocation=300   ->   [ 175, 125 ]		- space above preferred gets distributed between both children
		// 	boxAllocation=250   ->   [ 150, 100 ]		- required sizes
		// 	boxAllocation=100   ->   [ 150, 100 ]		- will not go below requirement
		allocYSpaceTests( new LReqBox[] { ybox( 150.0, 0.0 ), ybox( 100.0, 0.0 ) }, new int[] { VEXPAND, VEXPAND }, 0, 0.0,
				yrbox( 250.0, 0.0, 75.0 ),
				new LAllocV[] { new LAllocV( 350.0, 100.0 ), new LAllocV( 250.0 ), new LAllocV( 100.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 150.0 ) },
					new LAllocV[] { new LAllocV( 150.0 ), new LAllocV( 100.0 ) },
					new LAllocV[] { new LAllocV( 150.0 ), new LAllocV( 100.0 ) } } );
	}







	//
	//
	// ALLOCATION TESTS
	//
	//

	private void allocYTest(LReqBox children[], int childAllocFlags[], int refPointIndex, double spacing, LReqBox expectedBox, LAllocV boxAllocation, LAllocV expectedSize[], double expectedPosition[])
	{ 
		LReqBox box = new LReqBox();
		LAllocBox boxAlloc = new LAllocBox( null );
		LAllocBox childrenAlloc[] = new LAllocBox[children.length];
		for (int i = 0; i < children.length; i++)
		{
			childrenAlloc[i] = new LAllocBox( null );
		}

		VerticalLayout.computeRequisitionY( box, children, refPointIndex, spacing );

		assertBoxesEqual( box, expectedBox, "PARENT BOX" );

		LAllocHelper.allocateY( boxAlloc, box, 0.0, boxAllocation );
		VerticalLayout.allocateY( box, children, boxAlloc, childrenAlloc, childAllocFlags, refPointIndex, spacing );
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

	
	private void allocYTests(LReqBox children[], int childAllocFlags[], int refPointIndex, double spacing, LReqBox expectedBox, LAllocV boxAllocations[], LAllocV expectedSize[][], double expectedPosition[][])
	{
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			allocYTest( children, childAllocFlags, refPointIndex, spacing, expectedBox, boxAllocations[i], expectedSize[i], expectedPosition[i] );
		}
	}



	public void test_allocateY()
	{
		// vpackY( [ <200,0> ], spacing=0 )
		// 	boxAllocation=100:200   ->   [ 200 ] @ [ 0 ]		- no expansion
		// 	boxAllocation=100:150   ->   [ 200 ] @ [ 0 ]		- required sizes
		// 	boxAllocation=50:50   ->   [ 200 ] @ [ 0 ]		- will not go below requirement
		allocYTests( new LReqBox[] { ybox( 200.0, 0.0 ) }, new int[] { 0 }, 0, 0.0,
				ybox( 200.0, 0.0 ),
				new LAllocV[] { new LAllocV( 300.0, 100.0 ), new LAllocV( 250.0, 100.0 ), new LAllocV( 100.0, 50.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 200.0 ) },
					new LAllocV[] { new LAllocV( 200.0 ) } },
				new double[][] {
					new double[] { 0.0 },
					new double[] { 0.0 },
					new double[] { 0.0 } } );

	
		// vpackY( [ <125:75,0> ], spacing=0 )
		// 	boxAllocation=125:175   ->   [ 100:100 ] @ [ 50 ]		- no expansion
		// 	boxAllocation=125:75   ->   [ 100:100 ] @ [ 0 ]		- required sizes
		// 	boxAllocation=50:50       ->   [ 100:100 ] @ [ 0 ]		- will not go below requirement
		allocYTests( new LReqBox[] { yrbox( 200.0, 0.0, 125.0 ) }, new int[] { 0 }, 0, 0.0,
				yrbox( 200.0, 0.0, 125.0 ),
				new LAllocV[] { new LAllocV( 300.0, 125.0 ), new LAllocV( 200.0, 125.0 ), new LAllocV( 100.0, 50.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 200.0, 125.0 ) },
					new LAllocV[] { new LAllocV( 200.0, 125.0 ) },
					new LAllocV[] { new LAllocV( 200.0, 125.0 ) } },
				new double[][] {
					new double[] { 0.0 },
					new double[] { 0.0 },
					new double[] { 0.0 } } );


		// vpackY( [ <200,0>, <50,0> ], spacing=0 )
		// 	boxAllocation=100:200   ->   [ 200, 50 ] @ [ 0, 200 ]		- no expansion
		// 	boxAllocation=100:150   ->   [ 200, 50 ] @ [ 0, 200 ]		- required sizes
		// 	boxAllocation=62.5:125   ->   [ 200, 50 ] @ [ 0, 200 ]		- will not go below requirement
		allocYTests( new LReqBox[] { ybox( 200.0, 0.0 ),  ybox( 50.0, 0.0 ) }, new int[] { 0, 0 }, 0, 0.0,
				yrbox( 250.0, 0.0, 100.0 ),
				new LAllocV[] { new LAllocV( 350.0, 100.0 ), new LAllocV( 250.0, 100.0 ), new LAllocV( 125.0, 62.5 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ) },
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ) },
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ) } },
				new double[][] {
					new double[] { 0.0, 200.0 },
					new double[] { 0.0, 200.0 },
					new double[] { 0.0, 200.0 } } );


		// vpackY( [ <200,0>, <50,0> ], spacing=0, refPointIndex=1 )
		// 	boxAllocation=225:125   ->   [ 200, 50 ] @ [ 0, 200 ]		- no expansion
		// 	boxAllocation=225:25   ->   [ 200, 50 ] @ [ 0, 200 ]		- required sizes
		// 	boxAllocation=100:25   ->   [ 200, 50 ] @ [ 0, 200 ]		- will not go below requirement
		allocYTests( new LReqBox[] { ybox( 200.0, 0.0 ),  ybox( 50.0, 0.0 ) }, new int[] { 0, 0 }, 1, 0.0,
				yrbox( 250.0, 0.0, 225.0 ),
				new LAllocV[] { new LAllocV( 350.0, 225.0 ), new LAllocV( 250.0, 225.0 ), new LAllocV( 125.0, 100.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ) },
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ) },
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ) } },
				new double[][] {
					new double[] { 0.0, 200.0 },
					new double[] { 0.0, 200.0 },
					new double[] { 0.0, 200.0 } } );


		// vpackY( [ <200,0>, <50,0> ], spacing=10 )
		// 	boxAllocation=100:260   ->   [ 200, 50 ] @ [ 0, 210 ]		- no expansion
		// 	boxAllocation=100:160   ->   [ 200, 50 ] @ [ 0, 210 ]		- required sizes
		// 	boxAllocation=30:70   ->   [ 200, 50 ] @ [ 0, 210 ]		- will not go below requirement
		allocYTests( new LReqBox[] { ybox( 200.0, 0.0 ),  ybox( 50.0, 0.0 ) }, new int[] { 0, 0 }, 0, 10.0,
				yrbox( 260.0, 0.0, 100.0 ),
				new LAllocV[] { new LAllocV( 360.0, 100.0 ), new LAllocV( 260.0, 100.0 ), new LAllocV( 100.0, 30.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ) },
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ) },
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ) } },
				new double[][] {
					new double[] { 0.0, 210.0 },
					new double[] { 0.0, 210.0 },
					new double[] { 0.0, 210.0 } } );

	

		// vpackY( [ <200,15>, <50,0> ], spacing=0 )
		// 	boxAllocation=100:300   ->   [ 200, 50 ] @ [ 0, 215 ]		- no expansion
		// 	boxAllocation=100:215   ->   [ 200, 50 ] @ [ 0, 215 ]		- required sizes
		// 	boxAllocation=30:70   ->   [ 200, 50 ] @ [ 0, 215 ]		- will not go below requirement
		allocYTests( new LReqBox[] { ybox( 200.0, 15.0 ),  ybox( 50.0, 0.0 ) }, new int[] { 0, 0 }, 0, 0.0,
				yrbox( 265.0, 0.0, 100.0 ),
				new LAllocV[] { new LAllocV( 400.0, 100.0 ), new LAllocV( 315.0, 100.0 ), new LAllocV( 100.0, 30.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ) },
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ) },
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ) } },
				new double[][] {
					new double[] { 0.0, 215.0 },
					new double[] { 0.0, 215.0 },
					new double[] { 0.0, 215.0 } } );


		// vpackY( [ <200,0>, <50,0>, <100,0> ], spacing=0 )
		// 	boxAllocation=100:300   ->   [ 200, 50, 100 ] @ [ 0, 200, 250 ]		- no expansion
		// 	boxAllocation=125:275   ->   [ 200, 50, 100 ] @ [ 25, 225, 275 ]		- required sizes
		// 	boxAllocation=100:250   ->   [ 200, 50, 100 ] @ [ 0, 200, 250 ]		- required sizes
		// 	boxAllocation=50:150   ->   [ 200, 50, 100 ] @ [ 0, 200, 250 ]		- will not go below requirement
		allocYTests( new LReqBox[] { ybox( 200.0, 0.0 ),  ybox( 50.0, 0.0 ),  ybox( 100.0, 0.0 ) }, new int[] { 0, 0 }, 0, 0.0,
				yrbox( 350.0, 0.0, 100.0 ),
				new LAllocV[] { new LAllocV( 400.0, 100.0 ), new LAllocV( 400.0, 125.0 ), new LAllocV( 350.0, 100.0 ), new LAllocV( 200.0, 50.0 ) },
				new LAllocV[][] {
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ), new LAllocV( 100.0 ) },
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ), new LAllocV( 100.0 ) },
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ), new LAllocV( 100.0 ) },
					new LAllocV[] { new LAllocV( 200.0 ), new LAllocV( 50.0 ), new LAllocV( 100.0 ) } },
				new double[][] {
					new double[] { 0.0, 200.0, 250.0 },
					new double[] { 25.0, 225.0, 275.0 },
					new double[] { 0.0, 200.0, 250.0 },
					new double[] { 0.0, 200.0, 250.0 } } );

	}






	private void allocXTest(LReqBox children[], int childAllocFlags[], LReqBox expectedBox, double boxAllocation, double expectedSize[], double expectedPosition[])
	{ 
		LReqBox box = new LReqBox();
		LAllocBox boxAlloc = new LAllocBox( null );
		LAllocBox childrenAlloc[] = new LAllocBox[children.length];
		for (int i = 0; i < children.length; i++)
		{
			childrenAlloc[i] = new LAllocBox( null );
		}

		VerticalLayout.computeRequisitionX( box, children );

		assertBoxesEqual( box, expectedBox, "PARENT BOX" );

		LAllocHelper.allocateX( boxAlloc, box, 0.0, boxAllocation );
		VerticalLayout.allocateX( box, children, boxAlloc, childrenAlloc, childAllocFlags );
		for (int i = 0; i < children.length; i++)
		{
			if ( childrenAlloc[i].getAllocWidth() != expectedSize[i] )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSize[i] + ", result=" + childrenAlloc[i].getAllocWidth() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( childrenAlloc[i].getAllocWidth(), expectedSize[i] );

			if ( childrenAlloc[i].getAllocPositionInParentSpaceX() != expectedPosition[i] )
			{
				System.out.println( "Child position for " + i + " is not as expected; expected=" + expectedPosition[i] + ", result=" + childrenAlloc[i].getAllocPositionInParentSpaceX() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( childrenAlloc[i].getAllocPositionInParentSpaceX(), expectedPosition[i] );
		}
	}
	
	private void allocXTests(LReqBox children[], int childAllocFlags[], LReqBox expectedBox, double boxAllocations[], double expectedSize[][], double expectedPosition[][])
	{
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			allocXTest( children, childAllocFlags, expectedBox, boxAllocations[i], expectedSize[i], expectedPosition[i] );
		}
	}



	public void test_allocateX()
	{
		// allocX( [ <200-300,0-0,H.LEFT>, <100-200,0-0,H.LEFT> ] )
		// 	boxAllocation=400   ->   [ 300, 200 ] @ [ 0, 0 ]		- no expansion, no expansion
		// 	boxAllocation=300   ->   [ 300, 200 ] @ [ 0, 0 ]		- pref size, no expansion
		// 	boxAllocation=250   ->   [ 250, 200 ] @ [ 0, 0 ]		- between min and pref, no expansion
		// 	boxAllocation=200   ->   [ 200, 200 ] @ [ 0, 0 ]		- min size, pref size
		// 	boxAllocation=150   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, between min and pref
		// 	boxAllocation=100   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, min size
		// 	boxAllocation=50   ->     [ 200, 200 ] @ [ 0, 0 ]		- below min size, below min size
		allocXTests( new LReqBox[] { xbox( 200.0, 300.0, 0.0, 0.0 ),  xbox( 100.0, 200.0, 0.0, 0.0 ) }, new int[] { HLEFT, HLEFT },
				xbox( 200.0, 300.0, 0.0, 0.0 ),
				new double[] { 400.0, 300.0, 250.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 250.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 150.0, 150.0 },
					new double[] { 100.0, 100.0 },
					new double[] { 50.0, 50.0 } },
				new double[][] {
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );

	
	
		// allocX( [ <200-300,0-0,H.CENTRE>, <100-200,0-0,H.CENTRE> ] )
		// 	boxAllocation=400   ->   [ 300, 200 ] @ [ 50, 100 ]	- no expansion, no expansion
		// 	boxAllocation=300   ->   [ 300, 200 ] @ [ 0, 50 ]		- pref size, no expansion
		// 	boxAllocation=250   ->   [ 250, 200 ] @ [ 0, 25 ]		- between min and pref, no expansion
		// 	boxAllocation=200   ->   [ 200, 200 ] @ [ 0, 0 ]		- min size, pref size
		// 	boxAllocation=150   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, between min and pref
		// 	boxAllocation=100   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, min size
		// 	boxAllocation=50   ->     [ 200, 200 ] @ [ 0, 0 ]		- below min size, below min size
		allocXTests( new LReqBox[] { xbox( 200.0, 300.0, 0.0, 0.0 ),  xbox( 100.0, 200.0, 0.0, 0.0 ) }, new int[] { HCENTRE, HCENTRE },
				xbox( 200.0, 300.0, 0.0, 0.0 ),
				new double[] { 400.0, 300.0, 250.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 250.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 150.0, 150.0 },
					new double[] { 100.0, 100.0 },
					new double[] { 50.0, 50.0 } },
				new double[][] {
					new double[] { 50.0, 100.0 },
					new double[] { 0.0, 50.0 },
					new double[] { 0.0, 25.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );

		
		
		// allocX( [ <200-300,0-0,H.RIGHT>, <100-200,0-0,H.RIGHT> ] )
		// 	boxAllocation=400   ->   [ 300, 200 ] @ [ 100, 200 ]	- no expansion, no expansion
		// 	boxAllocation=300   ->   [ 300, 200 ] @ [ 0, 100 ]		- pref size, no expansion
		// 	boxAllocation=250   ->   [ 250, 200 ] @ [ 0, 50 ]		- between min and pref, no expansion
		// 	boxAllocation=200   ->   [ 200, 200 ] @ [ 0, 0 ]		- min size, pref size
		// 	boxAllocation=150   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, between min and pref
		// 	boxAllocation=100   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, min size
		// 	boxAllocation=50   ->     [ 200, 200 ] @ [ 0, 0 ]		- below min size, below min size
		allocXTests( new LReqBox[] { xbox( 200.0, 300.0, 0.0, 0.0 ),  xbox( 100.0, 200.0, 0.0, 0.0 ) }, new int[] { HRIGHT, HRIGHT },
				xbox( 200.0, 300.0, 0.0, 0.0 ),
				new double[] { 400.0, 300.0, 250.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 250.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 150.0, 150.0 },
					new double[] { 100.0, 100.0 },
					new double[] { 50.0, 50.0 } },
				new double[][] {
					new double[] { 100.0, 200.0 },
					new double[] { 0.0, 100.0 },
					new double[] { 0.0, 50.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );

	
	
		// allocX( [ <200-300,0-0,H.EXPAND>, <100-200,0-0,H.EXPAND> ] )
		// 	boxAllocation=400   ->   [ 400, 400 ] @ [ 0, 0 ]		- expansion, expansion
		// 	boxAllocation=300   ->   [ 300, 300 ] @ [ 0, 0 ]		- pref size, expansion
		// 	boxAllocation=250   ->   [ 250, 250 ] @ [ 0, 0 ]		- between min and pref, expansion
		// 	boxAllocation=200   ->   [ 200, 200 ] @ [ 0, 0 ]		- min size, pref size
		// 	boxAllocation=150   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, between min and pref
		// 	boxAllocation=100   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, min size
		// 	boxAllocation=50   ->     [ 200, 200 ] @ [ 0, 0 ]		- below min size, below min size
		allocXTests( new LReqBox[] { xbox( 200.0, 300.0, 0.0, 0.0 ),  xbox( 100.0, 200.0, 0.0, 0.0 ) }, new int[] { HEXPAND, HEXPAND },
				xbox( 200.0, 300.0, 0.0, 0.0 ),
				new double[] { 400.0, 300.0, 250.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 400.0, 400.0 },
					new double[] { 300.0, 300.0 },
					new double[] { 250.0, 250.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 150.0, 150.0 },
					new double[] { 100.0, 100.0 },
					new double[] { 50.0, 50.0 } },
				new double[][] {
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );
	}

}
