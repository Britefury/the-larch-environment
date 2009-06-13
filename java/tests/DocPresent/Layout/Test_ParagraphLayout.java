//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Layout;

import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.DocPresent.Layout.BoxPackingParams;
import BritefuryJ.DocPresent.Layout.ParagraphLayout;
import BritefuryJ.DocPresent.Layout.LBox;
import BritefuryJ.DocPresent.Layout.VAlignment;

public class Test_ParagraphLayout extends Test_Layout_base
{
	protected LBox lineBreakBox(int cost)
	{
		return new LBox().lineBreakBox( cost );
	}
	
	protected LBox lineBreakBox(double width, int cost)
	{
		return new LBox( width, 0.0, 0.0, 0.0 ).lineBreakBox( cost );
	}
	
	protected LBox lineBreakBox(double width, double hSpacing, int cost)
	{
		return new LBox( width, hSpacing, 0.0, 0.0 ).lineBreakBox( cost );
	}
	
	
	//
	//
	// REQUISITION X TESTS
	//
	//
	
	public void test_testRequisitionX()
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// Line breaks will break the list of children into lines. The space requirements of these lines will affect only the minimum required space

		
		
		LBox result = new LBox();
		
		
		// First, perform the same tests as a normal horizontal layout.
		// requisitionX()  ->  <0,0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] {},  0.0, 0.0, null );
		assertEquals( result, new LBox() );

		// requisitionX( [ <0,0> ] )  ->  <0,0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { new LBox() },  0.0, 0.0, null );
		assertEquals( result, new LBox() );

		// requisitionX( [ <0,0>:pad=1 ] )  ->  <2,0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { new LBox() },  0.0, 0.0, new BoxPackingParams[] { new BoxPackingParams( 1.0 ) } );
		assertEquals( result, xbox( 2.0, 0.0 ) );

		// requisitionX( [ <10,0>:pad=2 ] )  ->  <14,0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 10.0, 0.0 ) },  0.0, 0.0, new BoxPackingParams[] { new BoxPackingParams( 2.0 ) } );
		assertEquals( result, xbox( 14.0, 0.0 ) );

		// Padding 'consumes' h-spacing
		// requisitionX( [ <10,1>:pad=2 ] )  ->  <14,0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 10.0, 1.0 ) },  0.0, 0.0, new BoxPackingParams[] { new BoxPackingParams( 2.0 ) } );
		assertEquals( result, xbox( 14.0, 0.0 ) );

		// Padding 'consumes' all h-spacing
		// requisitionX( [ <10,3>:pad=2 ] )  ->  <14,1>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 10.0, 3.0 ) },  0.0, 0.0, new BoxPackingParams[] { new BoxPackingParams( 2.0 ) } );
		assertEquals( result, xbox( 14.0, 1.0 ) );

		// requisitionX( [ <0,0>, <0,0> ] )  ->  <0,0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { new LBox(), new LBox() },  0.0, 0.0, null );
		assertEquals( result, new LBox() );

		// Width accumulates
		// requisitionX( [ <10,0>, <5,0> ] )  ->  <15,0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 10.0, 0.0 ), xbox( 5.0, 0.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 15.0, 0.0 ) );

		// H-spacing of child puts space before next child
		// requisitionX( [ <10,2>, <5,0> ] )  ->  <17,0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 10.0, 2.0 ), xbox( 5.0, 0.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 17.0, 0.0 ) );

		// H-spacing of last child gets put onto the result
		// requisitionX( [ <10,2>, <5,1> ] )  ->  <17,1>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 10.0, 2.0 ), xbox( 5.0, 1.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 17.0, 1.0 ) );

		// Spacing between children adds extra width
		// requisitionX( [ <0,0>, <0,0> ], spacing=1 )  ->  <1,0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { new LBox(), new LBox() },  0.0, 1.0, null );
		assertEquals( result, xbox( 1.0, 0.0 ) );
		// requisitionX( [ <10,0>, <5,0> ], spacing=1 )  ->  <15,0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 10.0, 0.0 ), xbox( 5.0, 0.0 ) },  0.0, 1.0, null );
		assertEquals( result, xbox( 16.0, 0.0 ) );

		// Spacing between children is added to the child's own spacing
		// requisitionX( [ <10,2>, <5,1> ], spacing=1 )  ->  <18,1>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 10.0, 2.0 ), xbox( 5.0, 1.0 ) },  0.0, 1.0, null );
		assertEquals( result, xbox( 18.0, 1.0 ) );
		
		
		
		
		// Now, introduce some line breaks.
		
		
		// Two lines, the second of greater length than the first
		// requisitionX( [ <15,0>, break(<0,0>), <10,0>, <20,0> ] )  ->  <30-45,0-0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 10.0, 0.0 ), xbox( 20.0, 0.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 30.0, 45.0, 0.0, 0.0 ) );

		// Two lines, the second of greater length than the first, with indentation
		// requisitionX( [ <15,0>, break(<0,0>), <10,0>, <20,0> ], indentation=5.0 )  ->  <35-45,0-0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 10.0, 0.0 ), xbox( 20.0, 0.0 ) },  5.0, 0.0, null );
		assertEquals( result, xbox( 35.0, 45.0, 0.0, 0.0 ) );

		// Two lines, the second of greater length than the first, with padding
		// requisitionX( [ <10,0>, <20,0>, break(<0,0>), <15,0> ], padding=[0,10,100,0] )  ->  <35-45,0-0>
		// Padding around line break should only affect the preferred width, not the minimum width
		// Padding before line break should effect both
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 10.0, 0.0 ), xbox( 20.0, 0.0 ), lineBreakBox( 0 ), xbox( 15.0, 0.0 ) },  0.0, 0.0,
				new BoxPackingParams[] { new BoxPackingParams( 0.0 ), new BoxPackingParams( 10.0 ), new BoxPackingParams( 100.0 ), new BoxPackingParams( 0.0 ) } );
		assertEquals( result, xbox( 50.0, 265.0, 0.0, 0.0 ) );

		// Two lines, the second of greater length than the first, with spacing
		// requisitionX( [ <15,0>, break(<0,0>), <10,0>, <20,0> ], spacing=10 )  ->  <40-75,0-0>
		// Minimum width: spacing should only be placed between the two elements on second line
		// Preferred width: spacing should be placed between all elements
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 10.0, 0.0 ), xbox( 20.0, 0.0 ) },  0.0, 10.0, null );
		assertEquals( result, xbox( 40.0, 75.0, 0.0, 0.0 ) );

	
	
	
		// Now, with three lines
		
		
		// Ensure that all lines can contribute to the overall required width
		// requisitionX( [ <25,0>, break(<0,0>), <15,0>, break(<0,0>), <15,0> ] )  ->  <30-45,0-0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 25.0, 0.0 ), lineBreakBox( 0 ), xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 15.0, 0.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 25.0, 55.0, 0.0, 0.0 ) );

		// requisitionX( [ <15,0>, break(<0,0>), <20,0>, break(<0,0>), <15,0> ] )  ->  <30-45,0-0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 20.0, 0.0 ), lineBreakBox( 0 ), xbox( 15.0, 0.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 20.0, 50.0, 0.0, 0.0 ) );

		// requisitionX( [ <15,0>, break(<0,0>), <15,0>, break(<0,0>), <18,0> ] )  ->  <30-45,0-0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 18.0, 0.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 18.0, 48.0, 0.0, 0.0 ) );

		
		// Now with indentation
		// requisitionX( [ <25,0>, break(<0,0>), <10,0>, break(<0,0>), <10,0> ] )  ->  <30-45,0-0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 25.0, 0.0 ), lineBreakBox( 0 ), xbox( 10.0, 0.0 ), lineBreakBox( 0 ), xbox( 10.0, 0.0 ) },  5.0, 0.0, null );
		assertEquals( result, xbox( 25.0, 45.0, 0.0, 0.0 ) );

		// requisitionX( [ <15,0>, break(<0,0>), <15,0>, break(<0,0>), <10,0> ] )  ->  <30-45,0-0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 10.0, 0.0 ) },  5.0, 0.0, null );
		assertEquals( result, xbox( 20.0, 40.0, 0.0, 0.0 ) );

		// requisitionX( [ <15,0>, break(<0,0>), <10,0>, break(<0,0>), <18,0> ] )  ->  <30-45,0-0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 10.0, 0.0 ), lineBreakBox( 0 ), xbox( 18.0, 0.0 ) },  5.0, 0.0, null );
		assertEquals( result, xbox( 23.0, 43.0, 0.0, 0.0 ) );

	
		// Ensure that line breaks with size contribute to the preferred width
		// requisitionX( [ <25,0>, break(<5,0>), <15,0>, break(<5,5>), <15,0> ] )  ->  <30-45,0-0>
		ParagraphLayout.computeRequisitionX( result, new LBox[] { xbox( 25.0, 0.0 ), lineBreakBox( 5.0, 0.0, 0 ), xbox( 15.0, 0.0 ), lineBreakBox( 5.0, 5.0, 0 ), xbox( 15.0, 0.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 25.0, 70.0, 0.0, 0.0 ) );
	}





	//
	//
	// ALLOCATION X TESTS
	//
	//

	private void allocXTest(LBox children[], double indentation, double hSpacing, BoxPackingParams packingParams[],LBox expectedBox, double boxAllocation, double expectedSize[], double expectedPosition[])
	{ 
		LBox box = new LBox();
		ParagraphLayout.computeRequisitionX( box, children, indentation, hSpacing, packingParams );
		
		assertBoxesEqual( box, expectedBox, "PARENT BOX" );

		box.setAllocationX( boxAllocation );
		ParagraphLayout.allocateX( box, children, indentation, hSpacing, packingParams );
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
	
	private void allocXTests(LBox children[], double indentation, double hSpacing, BoxPackingParams packingParams[], LBox expectedBox, double boxAllocations[], double expectedSize[][], double expectedPosition[][])
	{
		LBox childrenCopy[] = new LBox[children.length];
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			for (int j = 0; j < children.length; j++)
			{
				childrenCopy[j] = children[j].copy();
			}
			allocXTest( childrenCopy, indentation, hSpacing, packingParams, expectedBox, boxAllocations[i], expectedSize[i], expectedPosition[i] );
		}
	}


	public void test_allocateX()
	{
		// Test that the line breaks activate
		// hpackX( [ <25,0>, break(<5,0>), <15,0>, break(<5,0>), <15,0> ], indentation=5, spacing=0, padding=0 )
		// 	boxAllocation=65   ->     [ 25, 5, 15, 5, 15 ] @ [ 0, 25, 30, 45, 50 ]		- sufficient space - 1 line
		// 	boxAllocation=55   ->     [ 25, 5, 15, 0, 15 ] @ [ 0, 25, 30, 0, 5 ]			- break at last line break
		// 	boxAllocation=35   ->     [ 25, 0, 15, 0, 15 ] @ [ 0, 0, 5, 0, 5 ]			- break at both line breaks
		allocXTests( new LBox[] { xbox( 25.0, 0.0 ), lineBreakBox( 5.0, 0 ), xbox( 15.0, 0.0 ), lineBreakBox( 5.0, 0 ), xbox( 15.0, 0.0 ) }, 5.0, 0.0, null,
				xbox( 25.0, 65.0, 0.0, 0.0 ),
				new double[] { 70.0, 55.0, 35.0 },
				new double[][] {
					new double[] { 25, 5, 15, 5, 15 },
					new double[] { 25, 5, 15, 0, 15 },
					new double[] { 25, 0, 15, 0, 15 } },
				new double[][] {
					new double[] { 0, 25, 30, 45, 50 },
					new double[] { 0, 25, 30, 0, 5 },
					new double[] { 0, 0, 5, 0, 5 } } );

	
		// Test line break costs
		// hpackX( [ <25,0>, break(1:<5,0>), <15,0>, break(2:<5,0>), <15,0> ], indentation=5, spacing=0, padding=0 )
		// 	boxAllocation=65   ->     [ 25, 5, 15, 5, 15 ] @ [ 0, 25, 30, 45, 50 ]		- sufficient space - 1 line
		// 	boxAllocation=55   ->     [ 25, 0, 15, 5, 15 ] @ [ 0, 0, 5, 20, 25 ]			- break at first line break, due to lower cost
		allocXTests( new LBox[] { xbox( 25.0, 0.0 ), lineBreakBox( 5.0, 1 ), xbox( 15.0, 0.0 ), lineBreakBox( 5.0, 2 ), xbox( 15.0, 0.0 ) }, 5.0, 0.0, null,
				xbox( 25.0, 65.0, 0.0, 0.0 ),
				new double[] { 70.0, 55.0 },
				new double[][] {
					new double[] { 25, 5, 15, 5, 15 },
					new double[] { 25, 0, 15, 5, 15 } },
				new double[][] {
					new double[] { 0, 25, 30, 45, 50 },
					new double[] { 0, 0, 5, 20, 25 } } );

	
	
	
		// The last item in this case is sufficiently large that breaking at the first line break (which has a lower cost) would not be sufficient; the second line break
		// would need to be used as well. However, this means that the first two lines are shorter than they need to be. In this case, only the second
		// line break should be used, despite having a higher cost.
		// hpackX( [ <25,0>, break(1:<5,0>), <15,0>, break(2:<5,0>), <40,0> ], indentation=0, spacing=0, padding=0 )
		// 	boxAllocation=55   ->     [ 25, 5, 15, 0, 40 ] @ [ 0, 25, 30, 0, 5 ]
		allocXTests( new LBox[] { xbox( 25.0, 0.0 ), lineBreakBox( 5.0, 1 ), xbox( 15.0, 0.0 ), lineBreakBox( 5.0, 2 ), xbox( 40.0, 0.0 ) }, 5.0, 0.0, null,
				xbox( 45.0, 90.0, 0.0, 0.0 ),
				new double[] { 55.0 },
				new double[][] {
					new double[] { 25, 5, 15, 0, 40 } },
				new double[][] {
					new double[] { 0, 25, 30, 0, 5 } } );

	
	
	
		// In this case, the maximum allowed width 'hits' a line break
		// hpackX( [ <25,0>, break(0:<20,0>), <15,0>, break(0:<20,0>), break(0:<20,0>), break(0:<20,0>), <15,0> ], indentation=0, spacing=0, padding=0 )
		// 	boxAllocation=135   ->     [ 25, 20, 15, 20, 20, 20, 15 ] @ [ 0, 25, 45, 60, 80, 100, 120 ]		- sufficient space - 1 line
		// 	boxAllocation=105   ->     [ 25, 20, 15, 20, 20, 0, 15 ] @ [ 0, 25, 45, 60, 80, 0, 0 ]			- end of line in last line break
		// 	boxAllocation=85   ->     [ 25, 20, 15, 20, 0, 20, 15 ] @ [ 0, 25, 45, 60, 0, 0, 20 ]				- end of line in second last line break
		// 	boxAllocation=65   ->     [ 25, 20, 15, 0, 20, 20, 15 ] @ [ 0, 25, 45, 0, 0, 20, 40 ]				- end of line in third last line break
		allocXTests( new LBox[] { xbox( 25.0, 0.0 ), lineBreakBox( 20.0, 0 ), xbox( 15.0, 0.0 ), lineBreakBox( 20.0, 0 ), lineBreakBox( 20.0, 0 ), lineBreakBox( 20.0, 0 ), xbox( 15.0, 0.0 ) }, 0.0, 0.0, null,
				xbox( 25.0, 135.0, 0.0, 0.0 ),
				new double[] { 135.0, 105.0, 85.0, 65.0 },
				new double[][] {
					new double[] { 25, 20, 15, 20, 20, 20, 15 },
					new double[] { 25, 20, 15, 20, 20, 0, 15 },
					new double[] { 25, 20, 15, 20, 0, 20, 15 },
					new double[] { 25, 20, 15, 0, 20, 20, 15 } },
				new double[][] {
					new double[] { 0, 25, 45, 60, 80, 100, 120 },
					new double[] { 0, 25, 45, 60, 80, 0, 0 },
					new double[] { 0, 25, 45, 60, 0, 0, 20 },
					new double[] { 0, 25, 45, 0, 0, 20, 40 } } );
	}




	
	//
	//
	// REQUISITION Y TESTS

	private void allocYTest(LBox children[], double indentation, double hSpacing, double vSpacing, VAlignment vAlignment, BoxPackingParams packingParams[],
			LBox expectedBox, double boxAllocation, double expectedSize[], double expectedPosition[])
	{ 
		LBox box = new LBox();
		
		// X
		ParagraphLayout.computeRequisitionX( box, children, indentation, hSpacing, packingParams );
		box.setAllocationX( boxAllocation );
		ArrayList<ParagraphLayout.Line> lines = ParagraphLayout.allocateX( box, children, indentation, hSpacing, packingParams );
		
		// Y
		ParagraphLayout.computeRequisitionY( box, lines, vSpacing, vAlignment );
		ParagraphLayout.allocateY( box, lines, vSpacing, vAlignment );
		
		LBox b = box.copy();
		b.setAllocationX( 0.0 );
		b.setPositionInParentSpaceX( 0.0 );
		
		assertBoxesEqual( b, expectedBox, "PARENT BOX" );
		
		for (int i = 0; i < children.length; i++)
		{
			double lineY = 0.0;
			for (ParagraphLayout.Line line: lines)
			{
				if ( Arrays.asList( line.getChildBoxes() ).contains( children[i] ) )
				{
					lineY = line.getLineBox().getPositionInParentSpaceY(); 
				}
			}
			
			if ( children[i].getAllocationX() != expectedSize[i*2] )
			{
				System.out.println( "Child allocation width for " + i + " is not as expected; expected=" + expectedSize[i*2] + ", result=" + children[i].getAllocationX() );
			}
			assertEquals( children[i].getAllocationX(), expectedSize[i*2] );

			if ( children[i].getAllocationY() != expectedSize[i*2+1] )
			{
				System.out.println( "Child allocation height for " + i + " is not as expected; expected=" + expectedSize[i*2+1] + ", result=" + children[i].getAllocationY() );
			}
			assertEquals( children[i].getAllocationY(), expectedSize[i*2+1] );

			
			if ( children[i].getPositionInParentSpaceX() != expectedPosition[i*2] )
			{
				System.out.println( "Child position X for " + i + " is not as expected; expected=" + expectedPosition[i*2] + ", result=" + children[i].getPositionInParentSpaceX() );
			}
			assertEquals( children[i].getPositionInParentSpaceX(), expectedPosition[i*2] );
			
			if ( children[i].getPositionInParentSpaceY()+lineY != expectedPosition[i*2+1] )
			{
				System.out.println( "Child position Y for " + i + " is not as expected; expected=" + expectedPosition[i*2+1] + ", result=" + (children[i].getPositionInParentSpaceY()+lineY) );
			}
			assertEquals( children[i].getPositionInParentSpaceY()+lineY, expectedPosition[i*2+1] );
		}
	}
	
	
	
	public void test_requisitionY_allocateY()
	{
		// Sufficient space - 1 line
		// hpackX( [ <25,0,15,0>, break(<5,0>), <15,0,20,0>, break(<5,0>), <15,0,10,0> ], indentation=5, spacing=0, padding=0 )
		//	parentBox = <25, 65, 0, 0, 20, 0>
		// 		boxAllocation=65   ->     [ 25,15,  5,0,  15,20,  5,0,  15,10 ]  @  [ 0,0,  25,0,  30,0,  45,0,  50,0 ]
		allocYTest( new LBox[] { box( 25, 0, 15, 0 ), lineBreakBox( 5, 0 ), box( 15, 0, 20, 0 ), lineBreakBox( 5, 0 ), box( 15, 0, 10, 0 ) }, 5.0, 0.0, 0.0, VAlignment.TOP, null,
				box( 25, 65, 0, 0, 20, 0 ),
				65,
				new double[] { 25,15,  5,0,  15,20,  5,0,  15,10 },
				new double[] { 0,0,  25,0,  30,0,  45,0,  50,0 } );


		// Break at last line break
		// hpackX( [ <25,0,15,0>, break(<5,0>), <15,0,20,0>, break(<5,0>), <15,0,10,0> ], indentation=5, spacing=0, padding=0 )
		//	parentBox = <25, 65, 0, 0, 30, 0>
		// 		boxAllocation=55   ->     [ 25,15,  5,0,  15,20,  0,0,  15,10 ]  @  [ 0,0,  25,0,  30,0,  0,0,  5,20 ]
		allocYTest( new LBox[] { box( 25, 0, 15, 0 ), lineBreakBox( 5, 0 ), box( 15, 0, 20, 0 ), lineBreakBox( 5, 0 ), box( 15, 0, 10, 0 ) }, 5.0, 0.0, 0.0, VAlignment.TOP, null,
				box( 25, 65, 0, 0, 30, 0 ),
				55,
				new double[] { 25,15,  5,0,  15,20,  0,0,  15,10 },
				new double[] { 0,0,  25,0,  30,0,  0,0,  5,20 } );

	
		// Break at both line breaks
		// hpackX( [ <25,0,15,0>, break(<5,0>), <15,0,20,0>, break(<5,0>), <15,0,10,0> ], indentation=5, spacing=0, padding=0 )
		//	parentBox = <25, 65, 0, 0, 45, 0>
		// 		boxAllocation=35   ->     [ 25,15,  0,0,  15,20,  0,0,  15,10 ]  @  [ 0,0,  0,0,  5,15,  0,0,  5,35 ]
		allocYTest( new LBox[] { box( 25, 0, 15, 0 ), lineBreakBox( 5, 0 ), box( 15, 0, 20, 0 ), lineBreakBox( 5, 0 ), box( 15, 0, 10, 0 ) }, 5.0, 0.0, 0.0, VAlignment.TOP, null,
				box( 25, 65, 0, 0, 45, 0 ),
				35,
				new double[] { 25,15,  0,0,  15,20,  0,0,  15,10 },
				new double[] { 0,0,  0,0,  5,15,  0,0,  5,35 } );

	
	

		// Check v-spacing
		// hpackX( [ <25,0,15,0>, break(<5,0>), <15,0,20,0>, break(<5,0>), <15,0,10,0> ], indentation=5, spacing=0, padding=0 )
		//	parentBox = <25, 65, 0, 0, 45, 0>
		// 		boxAllocation=35   ->     [ 25,15,  0,0,  15,20,  0,0,  15,10 ]  @  [ 0,0,  0,0,  5,0,  0,0,  5,0 ]
		allocYTest( new LBox[] { box( 25, 0, 15, 0 ), lineBreakBox( 5, 0 ), box( 15, 0, 20, 0 ), lineBreakBox( 5, 0 ), box( 15, 0, 10, 0 ) }, 5.0, 0.0, 5.0, VAlignment.TOP, null,
				box( 25, 65, 0, 0, 55, 0 ),
				35,
				new double[] { 25,15,  0,0,  15,20,  0,0,  15,10 },
				new double[] { 0,0,  0,0,  5,20,  0,0,  5,45 } );
	}
}
