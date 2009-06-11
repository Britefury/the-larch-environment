//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Typesetting;

import BritefuryJ.DocPresent.Typesetting.BoxPackingParams;
import BritefuryJ.DocPresent.Typesetting.HAlignment;
import BritefuryJ.DocPresent.Typesetting.TSBox;
import BritefuryJ.DocPresent.Typesetting.VerticalPack;

public class Test_VerticalPack extends Test_BoxPack_base
{
	//
	//
	// REQUISITION TESTS
	//
	//
	
	public void test_requisitionX()
	{
		TSBox result = new TSBox();
		
		// Empty list should result in empty
		VerticalPack.computeRequisitionX( result, new TSBox[] {} );
		assertEquals( result, new TSBox() );

		// List of one empty box should result in empty
		VerticalPack.computeRequisitionX( result, new TSBox[] { new TSBox() } );
		assertEquals( result, new TSBox() );

		// 1 Box of width 1 should result in same
		VerticalPack.computeRequisitionX( result, new TSBox[] { xbox( 1.0, 0.0 ) } );
		assertEquals( result, xbox( 1.0, 0.0 ) );

		// 1 Box of width 10, hspacing 10 should result in same
		VerticalPack.computeRequisitionX( result, new TSBox[] { xbox( 10.0, 1.0 ) } );
		assertEquals( result, xbox( 10.0, 1.0 ) );

		// requisitionX( [ <10,1>, <20,1> ] )  ->  <20,1>
		VerticalPack.computeRequisitionX( result, new TSBox[] { xbox( 10.0, 1.0 ),  xbox( 20.0, 1.0 ) } );
		assertEquals( result, xbox( 20.0, 1.0 ) );

		// requisitionX( [ <1,10>, <2,20> ] )  ->  <2,20>
		VerticalPack.computeRequisitionX( result, new TSBox[] { xbox( 1.0, 10.0 ),  xbox( 2.0, 20.0 ) } );
		assertEquals( result, xbox( 2.0, 20.0 ) );

		// requisitionX( [ <10,3>, <11,1> ] )  ->  <11,2>
		// The first box advances X the most overall, although the second has the greater width
		VerticalPack.computeRequisitionX( result, new TSBox[] { xbox( 10.0, 3.0 ),  xbox( 11.0, 1.0 ) } );
		assertEquals( result, xbox( 11.0, 2.0 ) );

		// requisitionX( [ <10,5>, <5,10> ] )  ->  <10,5>
		// Both advance X by the same amount (15 units), but the first has the greater width
		VerticalPack.computeRequisitionX( result, new TSBox[] { xbox( 10.0, 5.0 ),  xbox( 5.0, 10.0 ) } );
		assertEquals( result, xbox( 10.0, 5.0 ) );
	}

	
	
	public void test_requisitionY()
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing

		TSBox result = new TSBox();
		
		// requisitionY()  ->  <0,0>
		VerticalPack.computeRequisitionY( result, new TSBox[] {},  0.0, null );
		assertEquals( result, new TSBox() );

		// requisitionY( [ <0,0> ] )  ->  <0,0>
		VerticalPack.computeRequisitionY( result, new TSBox[] { new TSBox() },  0.0, null );
		assertEquals( result, new TSBox() );

		// requisitionY( [ <0,0>:pad=1 ] )  ->  <2,0>
		VerticalPack.computeRequisitionY( result, new TSBox[] { new TSBox() },  0.0, new BoxPackingParams[] { new BoxPackingParams( 1.0 ) } );
		assertEquals( result, ybox( 2.0, 0.0 ) );

		// requisitionY( [ <10,0>:pad=2 ] )  ->  <14,0>
		VerticalPack.computeRequisitionY( result, new TSBox[] { ybox( 10.0, 0.0 ) },  0.0, new BoxPackingParams[] { new BoxPackingParams( 2.0 ) } );
		assertEquals( result, ybox( 14.0, 0.0 ) );

		// Padding 'consumes' h-spacing
		// requisitionY( [ <10,1>:pad=2 ] )  ->  <14,0>
		VerticalPack.computeRequisitionY( result, new TSBox[] { ybox( 10.0, 1.0 ) },  0.0, new BoxPackingParams[] { new BoxPackingParams( 2.0 ) } );
		assertEquals( result, ybox( 14.0, 0.0 ) );

		// Padding 'consumes' all h-spacing
		// requisitionY( [ <10,3>:pad=2 ] )  ->  <14,1>
		VerticalPack.computeRequisitionY( result, new TSBox[] { ybox( 10.0, 3.0 ) },  0.0, new BoxPackingParams[] { new BoxPackingParams( 2.0 ) } );
		assertEquals( result, ybox( 14.0, 1.0 ) );

		// requisitionY( [ <0,0>, <0,0> ] )  ->  <0,0>
		VerticalPack.computeRequisitionY( result, new TSBox[] { new TSBox(), new TSBox() },  0.0, null );
		assertEquals( result, new TSBox() );

		// Width accumulates
		// requisitionY( [ <10,0>, <5,0> ] )  ->  <15,0>
		VerticalPack.computeRequisitionY( result, new TSBox[] { ybox( 10.0, 0.0 ), ybox( 5.0, 0.0 ) },  0.0, null );
		assertEquals( result, ybox( 15.0, 0.0 ) );

		// H-spacing of child puts space before next child
		// requisitionY( [ <10,2>, <5,0> ] )  ->  <17,0>
		VerticalPack.computeRequisitionY( result, new TSBox[] { ybox( 10.0, 2.0 ), ybox( 5.0, 0.0 ) },  0.0, null );
		assertEquals( result, ybox( 17.0, 0.0 ) );

		// H-spacing of last child gets put onto the result
		// requisitionY( [ <10,2>, <5,1> ] )  ->  <17,1>
		VerticalPack.computeRequisitionY( result, new TSBox[] { ybox( 10.0, 2.0 ), ybox( 5.0, 1.0 ) },  0.0, null );
		assertEquals( result, ybox( 17.0, 1.0 ) );

		// Spacing between children adds extra width
		// requisitionY( [ <0,0>, <0,0> ], spacing=1 )  ->  <1,0>
		VerticalPack.computeRequisitionY( result, new TSBox[] { new TSBox(), new TSBox() },  1.0, null );
		assertEquals( result, ybox( 1.0, 0.0 ) );
		// requisitionY( [ <10,0>, <5,0> ], spacing=1 )  ->  <15,0>
		VerticalPack.computeRequisitionY( result, new TSBox[] { ybox( 10.0, 0.0 ), ybox( 5.0, 0.0 ) },  1.0, null );
		assertEquals( result, ybox( 16.0, 0.0 ) );

		// Spacing between children is added to the child's own spacing
		// requisitionY( [ <10,2>, <5,1> ], spacing=1 )  ->  <18,1>
		VerticalPack.computeRequisitionY( result, new TSBox[] { ybox( 10.0, 2.0 ), ybox( 5.0, 1.0 ) },  1.0, null );
		assertEquals( result, ybox( 18.0, 1.0 ) );
	}





	//
	//
	// SPACE ALLOCATION TESTS
	//
	//

	private void vpackYSpaceTest(TSBox children[], double spacing, BoxPackingParams packingParams[], TSBox expectedBox, double boxAllocation, double expectedSpaceAllocation[])
	{ 
		TSBox box = new TSBox();
		VerticalPack.computeRequisitionY( box, children, spacing, packingParams );
		if ( !box.equals( expectedBox ) )
		{
			System.out.println( "PARENT BOX IS NOT AS EXPECTED" );
			System.out.println( "EXPECTED" );
			System.out.println( expectedBox );
			System.out.println( "RESULT" );
			System.out.println( box );
		}
		assertEquals( box, expectedBox );
		box.setAllocationY( boxAllocation );
		VerticalPack.allocateSpaceY( box, children, packingParams );
		for (int i = 0; i < children.length; i++)
		{
			if ( children[i].getAllocationY() != expectedSpaceAllocation[i] )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSpaceAllocation[i] + ", result=" + children[i].getAllocationY() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( children[i].getAllocationY(), expectedSpaceAllocation[i] );
		}
	}

	
	private void vpackYSpaceTests(TSBox children[], double spacing, BoxPackingParams packingParams[], TSBox expectedBox, double boxAllocations[], double expectedSpaceAllocations[][])
	{
		TSBox baselineChildren[] = new TSBox[children.length];
		
		for (int i = 0; i < children.length; i++)
		{
			TSBox c = children[i];
			baselineChildren[i] = new TSBox( c.getMinWidth(), c.getPrefWidth(), c.getMinHSpacing(), c.getPrefHSpacing(), 
					c.getReqHeight() * 0.5, c.getReqHeight() * 0.5, c.getReqVSpacing());
		}

		for (int i = 0; i  < boxAllocations.length; i++)
		{
			vpackYSpaceTest( children, spacing, packingParams, expectedBox, boxAllocations[i], expectedSpaceAllocations[i] );
		}

		for (int i = 0; i  < boxAllocations.length; i++)
		{
			vpackYSpaceTest( baselineChildren, spacing, packingParams, expectedBox, boxAllocations[i], expectedSpaceAllocations[i] );
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
		
		
		// hpackXSpace( [ <150,0> ], spacing=0, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 150 ]		- no expansion
		// 	boxAllocation=150   ->   [ 150 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 150 ]		- will not go below requirement
		// No padding, no expand
		vpackYSpaceTests( new TSBox[] { ybox( 150.0, 0.0 ) }, 0.0, null,
				ybox( 150.0, 0.0 ),
				new double[] { 300.0, 150.0, 50.0 },
				new double[][] {
					new double[] { 150.0 },
					new double[] { 150.0 },
					new double[] { 150.0 } } );
		
		
		// hpackXSpace( [ <150,0> ], spacing=0, padding=10, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200 ]		- no expansion
		// 	boxAllocation=170   ->   [ 150 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=150   ->   [ 150 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=50   ->   [ 150 ]		- will not go below requirement, 20 to padding
		// 10 padding, no expand
		vpackYSpaceTests( new TSBox[] { ybox( 150.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( 10.0 ) },
				ybox( 170.0, 0.0 ),
				new double[] { 300.0, 170.0, 150.0, 50.0 },
				new double[][] {
					new double[] { 150.0 },
					new double[] { 150.0 },
					new double[] { 150.0 },
					new double[] { 150.0 } } );
		
		
		// hpackXSpace( [ <150,0> ], spacing=0, padding=0, packFlags=EXPAND )
		// 	boxAllocation=300   ->   [ 300 ]		- expansion; extra space allocated to child
		// 	boxAllocation=150   ->   [ 150 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 150 ]		- will not go below requirement
		// No padding, expand
		vpackYSpaceTests( new TSBox[] { ybox( 150.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( true ) },
				ybox( 150.0, 0.0 ),
				new double[] { 300.0, 150.0, 50.0 },
				new double[][] {
					new double[] { 300.0 },
					new double[] { 150.0 },
					new double[] { 150.0 } } );


		// hpackXSpace( [ <150,0> ], spacing=0, padding=10, packFlags=EXPAND )
		// 	boxAllocation=300   ->   [ 280 ]		- no expansion
		// 	boxAllocation=170   ->   [ 150 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=150   ->   [ 150 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=50   ->   [ 150 ]		- will not go below requirement, 20 to padding
		// 10 padding, expand
		vpackYSpaceTests( new TSBox[] { ybox( 150.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( 10.0, true ) },
				ybox( 170.0, 0.0 ),
				new double[] { 300.0, 170.0, 150.0, 50.0 },
				new double[][] {
					new double[] { 280.0 },
					new double[] { 150.0 },
					new double[] { 150.0 },
					new double[] { 150.0 } } );
		
		

		// h-spacing applied to 1 child should not make a difference, since it is the last child
		// hpackXSpace( [ <150,10> ], spacing=0, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 150 ]		- no expansion
		// 	boxAllocation=150   ->   [ 150 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 150 ]		- will not go below requirement
		// No padding, no expand
		vpackYSpaceTests( new TSBox[] { ybox( 150.0, 10.0 ) }, 0.0, null,
				ybox( 150.0, 10.0 ),
				new double[] { 300.0, 150.0, 50.0 },
				new double[][] {
					new double[] { 150.0 },
					new double[] { 150.0 },
					new double[] { 150.0 } } );
		

		
		
		
		
		
		
		
		// hpackXSpace( [ <150,0>, <100,0> ], spacing=0, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 150, 100 ]		- no expansion
		// 	boxAllocation=250   ->   [ 150, 100 ]		- required sizes
		// 	boxAllocation=100   ->   [ 150, 100 ]		- will not go below requirement
		vpackYSpaceTests( new TSBox[] { ybox( 150.0, 0.0 ), ybox( 100.0, 0.0 ) }, 0.0, null,
				ybox( 250.0, 0.0 ),
				new double[] { 300.0, 250.0, 100.0 },
				new double[][] {
					new double[] { 150.0, 100.0 },
					new double[] { 150.0, 100.0 },
					new double[] { 150.0, 100.0 } } );
		
		
		// hpackXSpace( [ <150,0>, <100,0> ], spacing=0, padding=0, packFlags=[ EXPAND, 0 ] )
		// 	boxAllocation=300   ->   [ 200, 100 ]		- space above preferred goes to first child, none to second
		// 	boxAllocation=250   ->   [ 150, 100 ]		- required sizes
		// 	boxAllocation=100   ->   [ 150, 100 ]		- will not go below requirement
		vpackYSpaceTests( new TSBox[] { ybox( 150.0, 0.0 ), ybox( 100.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( true ), new BoxPackingParams( false ) },
				ybox( 250.0, 0.0 ),
				new double[] { 300.0, 250.0, 100.0 },
				new double[][] {
					new double[] { 200.0, 100.0 },
					new double[] { 150.0, 100.0 },
					new double[] { 150.0, 100.0 } } );
		

		// hpackXSpace( [ <150,0>, <100,0> ], spacing=0, padding=0, packFlags=[ 0, EXPAND ] )
		// 	boxAllocation=300   ->   [ 150, 150 ]		- space above preferred goes to second child, none to first
		// 	boxAllocation=250   ->   [ 150, 100 ]		- required sizes
		// 	boxAllocation=100   ->   [ 150, 100 ]		- will not go below requirement
		vpackYSpaceTests( new TSBox[] { ybox( 150.0, 0.0 ), ybox( 100.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( false ), new BoxPackingParams( true ) },
				ybox( 250.0, 0.0 ),
				new double[] { 300.0, 250.0, 100.0 },
				new double[][] {
					new double[] { 150.0, 150.0 },
					new double[] { 150.0, 100.0 },
					new double[] { 150.0, 100.0 } } );
		

		// hpackXSpace( [ <150,0>, <100,0> ], spacing=0, padding=0, packFlags=[ EXPAND, EXPAND ] )
		// 	boxAllocation=300   ->   [ 175, 125 ]		- space above preferred gets distributed between both children
		// 	boxAllocation=250   ->   [ 150, 100 ]		- required sizes
		// 	boxAllocation=100   ->   [ 150, 100 ]		- will not go below requirement
		vpackYSpaceTests( new TSBox[] { ybox( 150.0, 0.0 ), ybox( 100.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( true ), new BoxPackingParams( true ) },
				ybox( 250.0, 0.0 ),
				new double[] { 300.0, 250.0, 100.0 },
				new double[][] {
					new double[] { 175.0, 125.0 },
					new double[] { 150.0, 100.0 },
					new double[] { 150.0, 100.0 } } );
	}







	//
	//
	// ALLOCATION TESTS
	//
	//

	private void vpackYTest(TSBox children[], double spacing, BoxPackingParams packingParams[], TSBox expectedBox, double boxAllocation, double expectedSize[], double expectedPosition[])
	{ 
		TSBox box = new TSBox();
		VerticalPack.computeRequisitionY( box, children, spacing, packingParams );
		if ( !box.equals( expectedBox ) )
		{
			System.out.println( "PARENT BOX IS NOT AS EXPECTED" );
			System.out.println( "EXPECTED" );
			System.out.println( expectedBox );
			System.out.println( "RESULT" );
			System.out.println( box );
		}
		assertEquals( box, expectedBox );
		box.setAllocationY( boxAllocation );
		VerticalPack.allocateY( box, children, spacing, packingParams );
		for (int i = 0; i < children.length; i++)
		{
			if ( children[i].getAllocationY() != expectedSize[i] )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSize[i] + ", result=" + children[i].getAllocationY() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( children[i].getAllocationY(), expectedSize[i] );

			if ( children[i].getPositionInParentSpaceY() != expectedPosition[i] )
			{
				System.out.println( "Child position for " + i + " is not as expected; expected=" + expectedPosition[i] + ", result=" + children[i].getPositionInParentSpaceY() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( children[i].getPositionInParentSpaceY(), expectedPosition[i] );
		}
	}

	
	private void vpackYTests(TSBox children[], double spacing, BoxPackingParams packingParams[], TSBox expectedBox, double boxAllocations[], double expectedSize[][], double expectedPosition[][])
	{
		TSBox baselineChildren[] = new TSBox[children.length];
		
		for (int i = 0; i < children.length; i++)
		{
			TSBox c = children[i];
			baselineChildren[i] = new TSBox( c.getMinWidth(), c.getPrefWidth(), c.getMinHSpacing(), c.getPrefHSpacing(), 
					c.getReqHeight() * 0.5, c.getReqHeight() * 0.5, c.getReqVSpacing() );
		}

		for (int i = 0; i  < boxAllocations.length; i++)
		{
			vpackYTest( children, spacing, packingParams, expectedBox, boxAllocations[i], expectedSize[i], expectedPosition[i] );
		}

		for (int i = 0; i  < boxAllocations.length; i++)
		{
			vpackYTest( baselineChildren, spacing, packingParams, expectedBox, boxAllocations[i], expectedSize[i], expectedPosition[i] );
		}
	}



	public void test_allocateY()
	{
		// vpackY( [ <200,0>, <50,0> ], spacing=0, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200, 50 ] @ [ 0, 200 ]		- no expansion
		// 	boxAllocation=250   ->   [ 200, 50 ] @ [ 0, 200 ]		- required sizes
		// 	boxAllocation=100   ->   [ 200, 50 ] @ [ 0, 200 ]		- will not go below requirement
		vpackYTests( new TSBox[] { ybox( 200.0, 0.0 ),  ybox( 50.0, 0.0 ) }, 0.0, null,
				ybox( 250.0, 0.0 ),
				new double[] { 300.0, 250.0, 100.0 },
				new double[][] {
					new double[] { 200.0, 50.0 },
					new double[] { 200.0, 50.0 },
					new double[] { 200.0, 50.0 } },
				new double[][] {
					new double[] { 0.0, 200.0 },
					new double[] { 0.0, 200.0 },
					new double[] { 0.0, 200.0 } } );

	
		// vpackY( [ <200,0>, <50,0> ], spacing=10, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200, 50 ] @ [ 0, 210 ]		- no expansion
		// 	boxAllocation=260   ->   [ 200, 50 ] @ [ 0, 210 ]		- required sizes
		// 	boxAllocation=100   ->   [ 200, 50 ] @ [ 0, 210 ]		- will not go below requirement
		vpackYTests( new TSBox[] { ybox( 200.0, 0.0 ),  ybox( 50.0, 0.0 ) }, 10.0, null,
				ybox( 260.0, 0.0 ),
				new double[] { 300.0, 260.0, 100.0 },
				new double[][] {
						new double[] { 200.0, 50.0 },
						new double[] { 200.0, 50.0 },
						new double[] { 200.0, 50.0 } },
					new double[][] {
						new double[] { 0.0, 210.0 },
						new double[] { 0.0, 210.0 },
						new double[] { 0.0, 210.0 } } );

	
		// vpackY( [ <200,0>, <50,0> ], spacing=0, padding=[ 10, 20 ], packFlags=0 )
		// 	boxAllocation=400   ->   [ 200, 50 ] @ [ 10, 240 ]		- no expansion
		// 	boxAllocation=310   ->   [ 200, 50 ] @ [ 10, 240 ]		- required sizes
		// 	boxAllocation=100   ->   [ 200, 50 ] @ [ 10, 240 ]		- will not go below requirement
		vpackYTests( new TSBox[] { ybox( 200.0, 0.0 ),  ybox( 50.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( 10.0 ), new BoxPackingParams( 20.0 ) },
				ybox( 310.0, 0.0 ),
				new double[] { 400.0, 310.0, 100.0 },
				new double[][] {
						new double[] { 200.0, 50.0 },
						new double[] { 200.0, 50.0 },
						new double[] { 200.0, 50.0 } },
					new double[][] {
						new double[] { 10.0, 240.0 },
						new double[] { 10.0, 240.0 },
						new double[] { 10.0, 240.0 } } );

		
		// vpackY( [ <200,15>, <50,0> ], spacing=0, padding=[ 10, 20 ], packFlags=0 )
		// 	boxAllocation=400   ->   [ 200, 50 ] @ [ 10, 245 ]		- no expansion
		// 	boxAllocation=315   ->   [ 200, 50 ] @ [ 10, 245 ]		- required sizes
		// 	boxAllocation=100   ->   [ 200, 50 ] @ [ 10, 245 ]		- will not go below requirement
		vpackYTests( new TSBox[] { ybox( 200.0, 15.0 ),  ybox( 50.0, 0.0 ) }, 0.0, new BoxPackingParams[] { new BoxPackingParams( 10.0 ), new BoxPackingParams( 20.0 ) },
				ybox( 315.0, 0.0 ),
				new double[] { 400.0, 315.0, 100.0 },
				new double[][] {
						new double[] { 200.0, 50.0 },
						new double[] { 200.0, 50.0 },
						new double[] { 200.0, 50.0 } },
					new double[][] {
						new double[] { 10.0, 245.0 },
						new double[] { 10.0, 245.0 },
						new double[] { 10.0, 245.0 } } );
	}






	private void vpackXTest(TSBox children[], HAlignment alignment, TSBox expectedBox, double boxAllocation, double expectedSize[], double expectedPosition[])
	{ 
		TSBox box = new TSBox();
		VerticalPack.computeRequisitionX( box, children );
		if ( !box.equals( expectedBox ) )
		{
			System.out.println( "PARENT BOX IS NOT AS EXPECTED" );
			System.out.println( "EXPECTED" );
			System.out.println( expectedBox );
			System.out.println( "RESULT" );
			System.out.println( box );
		}
		assertEquals( box, expectedBox );
		box.setAllocationX( boxAllocation );
		VerticalPack.allocateX( box, children, alignment );
		for (int i = 0; i < children.length; i++)
		{
			if ( children[i].getAllocationX() != expectedSize[i] )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSize[i] + ", result=" + children[i].getAllocationX() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( children[i].getAllocationX(), expectedSize[i] );

			if ( children[i].getPositionInParentSpaceX() != expectedPosition[i] )
			{
				System.out.println( "Child position for " + i + " is not as expected; expected=" + expectedPosition[i] + ", result=" + children[i].getPositionInParentSpaceX() + ", boxAllocation=" + boxAllocation );
			}
			assertEquals( children[i].getPositionInParentSpaceX(), expectedPosition[i] );
		}
	}
	
	private void vpackXTests(TSBox children[], HAlignment alignment, TSBox expectedBox, double boxAllocations[], double expectedSize[][], double expectedPosition[][])
	{
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			vpackXTest( children, alignment, expectedBox, boxAllocations[i], expectedSize[i], expectedPosition[i] );
		}
	}



	public void test_vpackX()
	{
		// vpackX( [ <200-300,0-0>, <100-200,0-0> ], alignment=LEFT )
		// 	boxAllocation=400   ->   [ 300, 200 ] @ [ 0, 0 ]		- no expansion, no expansion
		// 	boxAllocation=300   ->   [ 300, 200 ] @ [ 0, 0 ]		- pref size, no expansion
		// 	boxAllocation=250   ->   [ 250, 200 ] @ [ 0, 0 ]		- between min and pref, no expansion
		// 	boxAllocation=200   ->   [ 200, 200 ] @ [ 0, 0 ]		- min size, pref size
		// 	boxAllocation=150   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, parent box min size prevents child size from going below 200
		// 	boxAllocation=100   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, parent box min size prevents child size from going below 200
		// 	boxAllocation=50   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, parent box min size prevents child size from going below 200
		vpackXTests( new TSBox[] { xbox( 200.0, 300.0, 0.0, 0.0 ),  xbox( 100.0, 200.0, 0.0, 0.0 ) }, HAlignment.LEFT,
				xbox( 200.0, 300.0, 0.0, 0.0 ),
				new double[] { 400.0, 300.0, 250.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 250.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 200.0 } },
				new double[][] {
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );

	
	
		// vpackX( [ <200-300,0-0>, <100-200,0-0> ], alignment=CENTRE )
		// 	boxAllocation=400   ->   [ 300, 200 ] @ [ 50, 100 ]	- no expansion, no expansion
		// 	boxAllocation=300   ->   [ 300, 200 ] @ [ 0, 50 ]		- pref size, no expansion
		// 	boxAllocation=250   ->   [ 250, 200 ] @ [ 0, 25 ]		- between min and pref, no expansion
		// 	boxAllocation=200   ->   [ 200, 200 ] @ [ 0, 0 ]		- min size, pref size
		// 	boxAllocation=150   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, parent box min size prevents child size from going below 200
		// 	boxAllocation=100   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, parent box min size prevents child size from going below 200
		// 	boxAllocation=50   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, parent box min size prevents child size from going below 200
		vpackXTests( new TSBox[] { xbox( 200.0, 300.0, 0.0, 0.0 ),  xbox( 100.0, 200.0, 0.0, 0.0 ) }, HAlignment.CENTRE,
				xbox( 200.0, 300.0, 0.0, 0.0 ),
				new double[] { 400.0, 300.0, 250.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 250.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 200.0 } },
				new double[][] {
					new double[] { 50.0, 100.0 },
					new double[] { 0.0, 50.0 },
					new double[] { 0.0, 25.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );

		
		
		// vpackX( [ <200-300,0-0>, <100-200,0-0> ], alignment=RIGHT )
		// 	boxAllocation=400   ->   [ 300, 200 ] @ [ 100, 200 ]	- no expansion, no expansion
		// 	boxAllocation=300   ->   [ 300, 200 ] @ [ 0, 100 ]		- pref size, no expansion
		// 	boxAllocation=250   ->   [ 250, 200 ] @ [ 0, 50 ]		- between min and pref, no expansion
		// 	boxAllocation=200   ->   [ 200, 200 ] @ [ 0, 0 ]		- min size, pref size
		// 	boxAllocation=150   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, parent box min size prevents child size from going below 200
		// 	boxAllocation=100   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, parent box min size prevents child size from going below 200
		// 	boxAllocation=50   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, parent box min size prevents child size from going below 200
		vpackXTests( new TSBox[] { xbox( 200.0, 300.0, 0.0, 0.0 ),  xbox( 100.0, 200.0, 0.0, 0.0 ) }, HAlignment.RIGHT,
				xbox( 200.0, 300.0, 0.0, 0.0 ),
				new double[] { 400.0, 300.0, 250.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 250.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 200.0 } },
				new double[][] {
					new double[] { 100.0, 200.0 },
					new double[] { 0.0, 100.0 },
					new double[] { 0.0, 50.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 },
					new double[] { 0.0, 0.0 } } );

	
	
		// vpackX( [ <200-300,0-0>, <100-200,0-0> ], alignment=EXPAND )
		// 	boxAllocation=400   ->   [ 400, 400 ] @ [ 0, 0 ]		- expansion, expansion
		// 	boxAllocation=300   ->   [ 300, 300 ] @ [ 0, 0 ]		- pref size, expansion
		// 	boxAllocation=250   ->   [ 250, 250 ] @ [ 0, 0 ]		- between min and pref, expansion
		// 	boxAllocation=200   ->   [ 200, 200 ] @ [ 0, 0 ]		- min size, pref size
		// 	boxAllocation=150   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, parent box min size prevents child size from going below 200
		// 	boxAllocation=100   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, parent box min size prevents child size from going below 200
		// 	boxAllocation=50   ->   [ 200, 200 ] @ [ 0, 0 ]		- below min size, parent box min size prevents child size from going below 200
		vpackXTests( new TSBox[] { xbox( 200.0, 300.0, 0.0, 0.0 ),  xbox( 100.0, 200.0, 0.0, 0.0 ) }, HAlignment.EXPAND,
				xbox( 200.0, 300.0, 0.0, 0.0 ),
				new double[] { 400.0, 300.0, 250.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 400.0, 400.0 },
					new double[] { 300.0, 300.0 },
					new double[] { 250.0, 250.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 200.0 } },
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
