//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Typesetting;

import java.util.ArrayList;

import BritefuryJ.DocPresent.Typesetting.HorizontalPack;
import BritefuryJ.DocPresent.Typesetting.ParagraphPack;
import BritefuryJ.DocPresent.Typesetting.TSBox;

public class Test_ParagraphPack extends Test_BoxPack_base
{
	protected TSBox lineBreakBox(int cost)
	{
		return new TSBox().lineBreakBox( cost );
	}
	
	protected TSBox lineBreakBox(double width, int cost)
	{
		return new TSBox( width, 0.0, 0.0, 0.0 ).lineBreakBox( cost );
	}
	
	protected TSBox lineBreakBox(double width, double hSpacing, int cost)
	{
		return new TSBox( width, hSpacing, 0.0, 0.0 ).lineBreakBox( cost );
	}
	
	
	//
	//
	// REQUISITION TESTS
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

		
		
		TSBox result = new TSBox();
		
		
		// First, perform the same tests as a normal horizontal layout.
		// requisitionX()  ->  <0,0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] {},  0.0, 0.0, null );
		assertEquals( result, new TSBox() );

		// requisitionX( [ <0,0> ] )  ->  <0,0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { new TSBox() },  0.0, 0.0, null );
		assertEquals( result, new TSBox() );

		// requisitionX( [ <0,0>:pad=1 ] )  ->  <2,0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { new TSBox() },  0.0, 0.0, new double[] { 1.0 } );
		assertEquals( result, xbox( 2.0, 0.0 ) );

		// requisitionX( [ <10,0>:pad=2 ] )  ->  <14,0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 10.0, 0.0 ) },  0.0, 0.0, new double[] { 2.0 } );
		assertEquals( result, xbox( 14.0, 0.0 ) );

		// Padding 'consumes' h-spacing
		// requisitionX( [ <10,1>:pad=2 ] )  ->  <14,0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 10.0, 1.0 ) },  0.0, 0.0, new double[] { 2.0 } );
		assertEquals( result, xbox( 14.0, 0.0 ) );

		// Padding 'consumes' all h-spacing
		// requisitionX( [ <10,3>:pad=2 ] )  ->  <14,1>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 10.0, 3.0 ) },  0.0, 0.0, new double[] { 2.0 } );
		assertEquals( result, xbox( 14.0, 1.0 ) );

		// requisitionX( [ <0,0>, <0,0> ] )  ->  <0,0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { new TSBox(), new TSBox() },  0.0, 0.0, null );
		assertEquals( result, new TSBox() );

		// Width accumulates
		// requisitionX( [ <10,0>, <5,0> ] )  ->  <15,0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 10.0, 0.0 ), xbox( 5.0, 0.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 15.0, 0.0 ) );

		// H-spacing of child puts space before next child
		// requisitionX( [ <10,2>, <5,0> ] )  ->  <17,0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 10.0, 2.0 ), xbox( 5.0, 0.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 17.0, 0.0 ) );

		// H-spacing of last child gets put onto the result
		// requisitionX( [ <10,2>, <5,1> ] )  ->  <17,1>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 10.0, 2.0 ), xbox( 5.0, 1.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 17.0, 1.0 ) );

		// Spacing between children adds extra width
		// requisitionX( [ <0,0>, <0,0> ], spacing=1 )  ->  <1,0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { new TSBox(), new TSBox() },  0.0, 1.0, null );
		assertEquals( result, xbox( 1.0, 0.0 ) );
		// requisitionX( [ <10,0>, <5,0> ], spacing=1 )  ->  <15,0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 10.0, 0.0 ), xbox( 5.0, 0.0 ) },  0.0, 1.0, null );
		assertEquals( result, xbox( 16.0, 0.0 ) );

		// Spacing between children is added to the child's own spacing
		// requisitionX( [ <10,2>, <5,1> ], spacing=1 )  ->  <18,1>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 10.0, 2.0 ), xbox( 5.0, 1.0 ) },  0.0, 1.0, null );
		assertEquals( result, xbox( 18.0, 1.0 ) );
		
		
		
		
		// Now, introduce some line breaks.
		
		
		// Two lines, the second of greater length than the first
		// requisitionX( [ <15,0>, break(<0,0>), <10,0>, <20,0> ] )  ->  <30-45,0-0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 10.0, 0.0 ), xbox( 20.0, 0.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 30.0, 45.0, 0.0, 0.0 ) );

		// Two lines, the second of greater length than the first, with indentation
		// requisitionX( [ <15,0>, break(<0,0>), <10,0>, <20,0> ], indentation=5.0 )  ->  <35-45,0-0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 10.0, 0.0 ), xbox( 20.0, 0.0 ) },  5.0, 0.0, null );
		assertEquals( result, xbox( 35.0, 45.0, 0.0, 0.0 ) );

		// Two lines, the second of greater length than the first, with padding
		// requisitionX( [ <10,0>, <20,0>, break(<0,0>), <15,0> ], padding=[0,10,100,0] )  ->  <35-45,0-0>
		// Padding around line break should only affect the preferred width, not the minimum width
		// Padding before line break should effect both
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 10.0, 0.0 ), xbox( 20.0, 0.0 ), lineBreakBox( 0 ), xbox( 15.0, 0.0 ) },  0.0, 0.0, new double[] { 0.0, 10.0, 100.0, 0.0 } );
		assertEquals( result, xbox( 50.0, 265.0, 0.0, 0.0 ) );

		// Two lines, the second of greater length than the first, with spacing
		// requisitionX( [ <15,0>, break(<0,0>), <10,0>, <20,0> ], spacing=10 )  ->  <40-75,0-0>
		// Minimum width: spacing should only be placed between the two elements on second line
		// Preferred width: spacing should be placed between all elements
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 10.0, 0.0 ), xbox( 20.0, 0.0 ) },  0.0, 10.0, null );
		assertEquals( result, xbox( 40.0, 75.0, 0.0, 0.0 ) );

	
	
	
		// Now, with three lines
		
		
		// Ensure that all lines can contribute to the overall required width
		// requisitionX( [ <25,0>, break(<0,0>), <15,0>, break(<0,0>), <15,0> ] )  ->  <30-45,0-0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 25.0, 0.0 ), lineBreakBox( 0 ), xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 15.0, 0.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 25.0, 55.0, 0.0, 0.0 ) );

		// requisitionX( [ <15,0>, break(<0,0>), <20,0>, break(<0,0>), <15,0> ] )  ->  <30-45,0-0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 20.0, 0.0 ), lineBreakBox( 0 ), xbox( 15.0, 0.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 20.0, 50.0, 0.0, 0.0 ) );

		// requisitionX( [ <15,0>, break(<0,0>), <15,0>, break(<0,0>), <18,0> ] )  ->  <30-45,0-0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 18.0, 0.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 18.0, 48.0, 0.0, 0.0 ) );

		
		// Now with indentation
		// requisitionX( [ <25,0>, break(<0,0>), <10,0>, break(<0,0>), <10,0> ] )  ->  <30-45,0-0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 25.0, 0.0 ), lineBreakBox( 0 ), xbox( 10.0, 0.0 ), lineBreakBox( 0 ), xbox( 10.0, 0.0 ) },  5.0, 0.0, null );
		assertEquals( result, xbox( 25.0, 45.0, 0.0, 0.0 ) );

		// requisitionX( [ <15,0>, break(<0,0>), <15,0>, break(<0,0>), <10,0> ] )  ->  <30-45,0-0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 10.0, 0.0 ) },  5.0, 0.0, null );
		assertEquals( result, xbox( 20.0, 40.0, 0.0, 0.0 ) );

		// requisitionX( [ <15,0>, break(<0,0>), <10,0>, break(<0,0>), <18,0> ] )  ->  <30-45,0-0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 15.0, 0.0 ), lineBreakBox( 0 ), xbox( 10.0, 0.0 ), lineBreakBox( 0 ), xbox( 18.0, 0.0 ) },  5.0, 0.0, null );
		assertEquals( result, xbox( 23.0, 43.0, 0.0, 0.0 ) );

	
		// Ensure that line breaks with size contribute to the preferred width
		// requisitionX( [ <25,0>, break(<5,0>), <15,0>, break(<5,5>), <15,0> ] )  ->  <30-45,0-0>
		ParagraphPack.computeRequisitionX( result, new TSBox[] { xbox( 25.0, 0.0 ), lineBreakBox( 5.0, 0.0, 0 ), xbox( 15.0, 0.0 ), lineBreakBox( 5.0, 5.0, 0 ), xbox( 15.0, 0.0 ) },  0.0, 0.0, null );
		assertEquals( result, xbox( 25.0, 70.0, 0.0, 0.0 ) );
	}





	//
	//
	// ALLOCATION TESTS
	//
	//

	private void ppackXTest(TSBox children[], double indentation, double spacing, double childPadding[],TSBox expectedBox, double boxAllocation, double expectedSize[], double expectedPosition[])
	{ 
		TSBox box = new TSBox();
		ParagraphPack.computeRequisitionX( box, children, indentation, spacing, childPadding );
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
		ArrayList<ParagraphPack.Line> lines = ParagraphPack.allocateX( box, children, indentation, spacing, childPadding );
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
	
	private void ppackXTests(TSBox children[], double indentation, double spacing, double childPadding[], TSBox expectedBox, double boxAllocations[], double expectedSize[][], double expectedPosition[][])
	{
		TSBox childrenCopy[] = new TSBox[children.length];
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			for (int j = 0; j < children.length; j++)
			{
				childrenCopy[j] = children[j].copy();
			}
			ppackXTest( childrenCopy, indentation, spacing, childPadding, expectedBox, boxAllocations[i], expectedSize[i], expectedPosition[i] );
		}
	}


	public void test_allocateX()
	{
		// Test that the line breaks activate
		// hpackX( [ <25,0>, break(<5,0>), <15,0>, break(<5,0>), <15,0> ], indentation=5, spacing=0, padding=0 )
		// 	boxAllocation=65   ->     [ 25, 5, 15, 5, 15 ] @ [ 0, 25, 30, 45, 50 ]		- sufficient space - 1 line
		// 	boxAllocation=55   ->     [ 25, 5, 15, 0, 15 ] @ [ 0, 25, 30, 0, 5 ]			- break at last line break
		// 	boxAllocation=35   ->     [ 25, 0, 15, 0, 15 ] @ [ 0, 0, 5, 0, 5 ]			- break at both line breaks
		ppackXTests( new TSBox[] { xbox( 25.0, 0.0 ), lineBreakBox( 5.0, 0 ), xbox( 15.0, 0.0 ), lineBreakBox( 5.0, 0 ), xbox( 15.0, 0.0 ) }, 5.0, 0.0, null,
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
		// 	boxAllocation=55   ->     [ 25, 5, 15, 0, 15 ] @ [ 0, 25, 30, 0, 5 ]			- break at first line break, due to lower cost
		ppackXTests( new TSBox[] { xbox( 25.0, 0.0 ), lineBreakBox( 5.0, 1 ), xbox( 15.0, 0.0 ), lineBreakBox( 5.0, 2 ), xbox( 15.0, 0.0 ) }, 5.0, 0.0, null,
				xbox( 25.0, 65.0, 0.0, 0.0 ),
				new double[] { 70.0, 55.0 },
				new double[][] {
					new double[] { 25, 5, 15, 5, 15 },
					new double[] { 25, 0, 15, 5, 15 } },
				new double[][] {
					new double[] { 0, 25, 30, 45, 50 },
					new double[] { 0, 0, 5, 20, 25 } } );
	}
}
