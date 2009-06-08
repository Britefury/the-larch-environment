//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Typesetting;

import BritefuryJ.DocPresent.Typesetting.BoxPackingAllocation;
import BritefuryJ.DocPresent.Typesetting.BoxPackingRequisition;
import BritefuryJ.DocPresent.Typesetting.HAlignment;
import BritefuryJ.DocPresent.Typesetting.TSBox;
import junit.framework.TestCase;

public class Test_BoxPackingAllocation extends TestCase
{
	private TSBox xbox(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing)
	{
		return new TSBox( minWidth, prefWidth, minHSpacing, prefHSpacing, 0.0, 0.0, 0.0, 0.0 );
	}
	
	private void hpackXSpaceTest(TSBox children[], double spacing, double childPadding[], int packFlags[], TSBox expectedBox, double boxAllocation, double expectedSpaceAllocation[])
	{ 
		TSBox box = new TSBox();
		BoxPackingRequisition.accumulateX( box, children, spacing, childPadding );
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
		BoxPackingAllocation.allocateSpaceHorizontalPackingX( box, children, packFlags );
		for (int i = 0; i < children.length; i++)
		{
			if ( children[i].getAllocationX() != expectedSpaceAllocation[i] )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSpaceAllocation[i] + ", result=" + children[i].getAllocationX() );
			}
			assertEquals( children[i].getAllocationX(), expectedSpaceAllocation[i] );
		}
	}

	
	private void hpackXSpaceTests(TSBox children[], double spacing, double childPadding[], int packFlags[], TSBox expectedBox, double boxAllocations[], double expectedSpaceAllocations[][])
	{
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			hpackXSpaceTest( children, spacing, childPadding, packFlags, expectedBox, boxAllocations[i], expectedSpaceAllocations[i] );
		}
	}


	private void vpackYSpaceTest(TSBox children[], double spacing, double childPadding[], int packFlags[], TSBox expectedBox, double boxAllocation, double expectedSpaceAllocation[])
	{ 
		TSBox box = new TSBox();
		BoxPackingRequisition.accumulateY( box, children, spacing, childPadding );
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
		BoxPackingAllocation.allocateSpaceVerticalPackingY( box, children, packFlags );
		for (int i = 0; i < children.length; i++)
		{
			if ( children[i].getAllocationY() != expectedSpaceAllocation[i] )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSpaceAllocation[i] + ", result=" + children[i].getAllocationX() );
			}
			assertEquals( children[i].getAllocationY(), expectedSpaceAllocation[i] );
		}
	}

	
	private void vpackYSpaceTests(TSBox children[], double spacing, double childPadding[], int packFlags[], TSBox expectedBox, double boxAllocations[], double expectedSpaceAllocations[][])
	{
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			vpackYSpaceTest( children, spacing, childPadding, packFlags, expectedBox, boxAllocations[i], expectedSpaceAllocations[i] );
		}
	}


	
	private void cumulativePackSpaceTests(TSBox children[], double spacing, double childPadding[], int packFlags[], TSBox expectedBox, double boxAllocations[], double expectedSpaceAllocations[][])
	{
		// Combined horizontal packing in X, and vertical packing in Y
		
		TSBox yChildrenA[] = new TSBox[children.length];
		TSBox yChildrenB[] = new TSBox[children.length];
		
		for (int i = 0; i < children.length; i++)
		{
			TSBox c = children[i];
			yChildrenA[i] = new TSBox( c.getMinHeight(), c.getPrefHeight(), c.getMinVSpacing(), c.getPrefVSpacing(),
					c.getMinWidth(), c.getPrefWidth(), c.getMinHSpacing(), c.getPrefHSpacing() );
			yChildrenB[i] = new TSBox( c.getMinHeight(), c.getPrefHeight(), c.getMinVSpacing(), c.getPrefVSpacing(),
					c.getMinWidth() * 0.5, c.getPrefWidth() * 0.5, c.getMinWidth() * 0.5, c.getPrefWidth() * 0.5, c.getMinHSpacing(), c.getPrefHSpacing() );
		}
		
		TSBox expectedY = new TSBox( expectedBox.getMinHeight(), expectedBox.getPrefHeight(), expectedBox.getMinVSpacing(), expectedBox.getPrefVSpacing(),
					expectedBox.getMinWidth(), expectedBox.getPrefWidth(), expectedBox.getMinHSpacing(), expectedBox.getPrefHSpacing() );
	
		hpackXSpaceTests( children, spacing, childPadding, packFlags, expectedBox, boxAllocations, expectedSpaceAllocations );
		vpackYSpaceTests( yChildrenA, spacing, childPadding, packFlags, expectedY, boxAllocations, expectedSpaceAllocations );
		vpackYSpaceTests( yChildrenB, spacing, childPadding, packFlags, expectedY, boxAllocations, expectedSpaceAllocations );
	}


	
	public void test_allocateSpaceCumulativePacking()
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
		
		
		// cumulativePackSpace( [ <100-200,0-0> ], spacing=0, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200 ]		- no expansion
		// 	boxAllocation=200   ->   [ 200 ]		- all allocated to 1 child
		// 	boxAllocation=150   ->   [ 150 ]		- all allocated to 1 child
		// 	boxAllocation=100   ->   [ 100 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 100 ]		- will not go below minimum
		// No padding, no expand
		cumulativePackSpaceTests( new TSBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ) }, 0.0, null, null,
				xbox( 100.0, 200.0, 0.0, 0.0 ),
				new double[] { 300.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 200.0 },
					new double[] { 200.0 },
					new double[] { 150.0 },
					new double[] { 100.0 },
					new double[] { 100.0 } } );
		
		
		// cumulativePackSpace( [ <100-200,0-0> ], spacing=0, padding=10, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200 ]		- no expansion
		// 	boxAllocation=220   ->   [ 200 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=200   ->   [ 180 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=150   ->   [ 130 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=120   ->   [ 100 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=100   ->   [ 100 ]		- will not go below minimum, 20 to padding
		// 	boxAllocation=50   ->   [ 100 ]		- will not go below minimum, 20 to padding
		// 10 padding, no expand
		cumulativePackSpaceTests( new TSBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ) }, 0.0, new double[] { 10.0 }, null,
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
		
		
		// cumulativePackSpace( [ <100-200,0-0> ], spacing=0, padding=0, packFlags=EXPAND )
		// 	boxAllocation=300   ->   [ 300 ]		- expansion; extra space allocated to child
		// 	boxAllocation=200   ->   [ 200 ]		- all allocated to 1 child
		// 	boxAllocation=150   ->   [ 150 ]		- all allocated to 1 child
		// 	boxAllocation=100   ->   [ 100 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 100 ]		- will not go below minimum
		// No padding, expand
		cumulativePackSpaceTests( new TSBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ) }, 0.0, null, new int[] { TSBox.packFlags( true ) },
				xbox( 100.0, 200.0, 0.0, 0.0 ),
				new double[] { 300.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 300.0 },
					new double[] { 200.0 },
					new double[] { 150.0 },
					new double[] { 100.0 },
					new double[] { 100.0 } } );


		// cumulativePackSpace( [ <100-200,0-0> ], spacing=0, padding=10, packFlags=EXPAND )
		// 	boxAllocation=300   ->   [ 280 ]		- expansion; extra space allocated to child, 20 go to padding
		// 	boxAllocation=220   ->   [ 200 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=200   ->   [ 180 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=150   ->   [ 130 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=120   ->   [ 100 ]		- all allocated to 1 child, 20 to padding
		// 	boxAllocation=100   ->   [ 100 ]		- will not go below minimum, 20 to padding
		// 	boxAllocation=50   ->   [ 100 ]		- will not go below minimum, 20 to padding
		// 10 padding, expand
		cumulativePackSpaceTests( new TSBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ) }, 0.0, new double[] { 10.0 }, new int[] { TSBox.packFlags( true ) },
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
		// cumulativePackSpace( [ <100-200,10-10> ], spacing=0, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200 ]		- no expansion
		// 	boxAllocation=200   ->   [ 200 ]		- all allocated to 1 child
		// 	boxAllocation=150   ->   [ 150 ]		- all allocated to 1 child
		// 	boxAllocation=100   ->   [ 100 ]		- all allocated to 1 child
		// 	boxAllocation=50   ->   [ 100 ]		- will not go below minimum
		cumulativePackSpaceTests( new TSBox[] { xbox( 100.0, 200.0, 10.0, 10.0 ) }, 0.0, null, null,
				xbox( 100.0, 200.0, 10.0, 10.0 ),
				new double[] { 300.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 200.0 },
					new double[] { 200.0 },
					new double[] { 150.0 },
					new double[] { 100.0 },
					new double[] { 100.0 } } );
		

		
		
		
		
		
		
		
		// cumulativePackSpace( [ <100-200,0-0>, <50-70,0-0> ], spacing=0, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200, 70 ]		- no expansion
		// 	boxAllocation=270   ->   [ 200, 70 ]		- preferred sizes
		// 	boxAllocation=210   ->   [ 150, 60 ]		- space above minimum distributed evenly
		// 	boxAllocation=150   ->   [ 100, 50 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ]		- will not go below minimum
		cumulativePackSpaceTests( new TSBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 0.0, null, null,
				xbox( 150.0, 270.0, 0.0, 0.0 ),
				new double[] { 300.0, 270.0, 210.0, 150.0, 100.0 },
				new double[][] {
					new double[] { 200.0, 70.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } } );
		
		
		// cumulativePackSpace( [ <100-200,0-0>, <50-70,0-0> ], spacing=0, padding=0, packFlags=[ EXPAND, 0 ] )
		// 	boxAllocation=300   ->   [ 230, 70 ]		- space above preferred goes to first child, none to second
		// 	boxAllocation=270   ->   [ 200, 70 ]		- preferred sizes
		// 	boxAllocation=210   ->   [ 150, 60 ]		- space above minimum distributed evenly
		// 	boxAllocation=150   ->   [ 100, 50 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ]		- will not go below minimum
		cumulativePackSpaceTests( new TSBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 0.0, null, new int[] { TSBox.packFlags( true ), TSBox.packFlags( false ) },
				xbox( 150.0, 270.0, 0.0, 0.0 ),
				new double[] { 300.0, 270.0, 210.0, 150.0, 100.0 },
				new double[][] {
					new double[] { 230.0, 70.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } } );
		

		// cumulativePackSpace( [ <100-200,0-0>, <50-70,0-0> ], spacing=0, padding=0, packFlags=[ 0, EXPAND ] )
		// 	boxAllocation=300   ->   [ 200, 100 ]		- space above preferred goes to secnd child, none to first
		// 	boxAllocation=270   ->   [ 200, 70 ]		- preferred sizes
		// 	boxAllocation=210   ->   [ 150, 60 ]		- space above minimum distributed evenly
		// 	boxAllocation=150   ->   [ 100, 50 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ]		- will not go below minimum
		cumulativePackSpaceTests( new TSBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 0.0, null, new int[] { TSBox.packFlags( false ), TSBox.packFlags( true ) },
				xbox( 150.0, 270.0, 0.0, 0.0 ),
				new double[] { 300.0, 270.0, 210.0, 150.0, 100.0 },
				new double[][] {
					new double[] { 200.0, 100.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } } );
		

		// cumulativePackSpace( [ <100-200,0-0>, <50-70,0-0> ], spacing=0, padding=0, packFlags=[ EXPAND, EXPAND ] )
		// 	boxAllocation=300   ->   [ 215, 85 ]		- space above preferred gets distributed between both children
		// 	boxAllocation=270   ->   [ 200, 70 ]		- preferred sizes
		// 	boxAllocation=210   ->   [ 150, 60 ]		- space above minimum distributed evenly
		// 	boxAllocation=150   ->   [ 100, 50 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ]		- will not go below minimum
		cumulativePackSpaceTests( new TSBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 0.0, null, new int[] { TSBox.packFlags( true ), TSBox.packFlags( true ) },
				xbox( 150.0, 270.0, 0.0, 0.0 ),
				new double[] { 300.0, 270.0, 210.0, 150.0, 100.0 },
				new double[][] {
					new double[] { 215.0, 85.0 },
					new double[] { 200.0, 70.0 },
					new double[] { 150.0, 60.0 },
					new double[] { 100.0, 50.0 },
					new double[] { 100.0, 50.0 } } );
	}








	private void hpackXTest(TSBox children[], double spacing, double childPadding[], int packFlags[], TSBox expectedBox, double boxAllocation, double expectedSize[], double expectedPosition[])
	{ 
		TSBox box = new TSBox();
		BoxPackingRequisition.accumulateX( box, children, spacing, childPadding );
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
		BoxPackingAllocation.allocateHorizontalPackingX( box, children, spacing, childPadding, packFlags );
		for (int i = 0; i < children.length; i++)
		{
			if ( children[i].getAllocationX() != expectedSize[i] )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSize[i] + ", result=" + children[i].getAllocationX() );
			}
			assertEquals( children[i].getAllocationX(), expectedSize[i] );

			if ( children[i].getPositionInParentSpaceX() != expectedPosition[i] )
			{
				System.out.println( "Child position for " + i + " is not as expected; expected=" + expectedPosition[i] + ", result=" + children[i].getPositionInParentSpaceX() );
			}
			assertEquals( children[i].getPositionInParentSpaceX(), expectedPosition[i] );
		}
	}
	
	private void hpackXTests(TSBox children[], double spacing, double childPadding[], int packFlags[], TSBox expectedBox, double boxAllocations[], double expectedSize[][], double expectedPosition[][])
	{
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			hpackXTest( children, spacing, childPadding, packFlags, expectedBox, boxAllocations[i], expectedSize[i], expectedPosition[i] );
		}
	}


	private void vpackYTest(TSBox children[], double spacing, double childPadding[], int packFlags[], TSBox expectedBox, double boxAllocation, double expectedSize[], double expectedPosition[])
	{ 
		TSBox box = new TSBox();
		BoxPackingRequisition.accumulateY( box, children, spacing, childPadding );
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
		BoxPackingAllocation.allocateVerticalPackingY( box, children, spacing, childPadding, packFlags );
		for (int i = 0; i < children.length; i++)
		{
			if ( children[i].getAllocationY() != expectedSize[i] )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSize[i] + ", result=" + children[i].getAllocationY() );
			}
			assertEquals( children[i].getAllocationY(), expectedSize[i] );

			if ( children[i].getPositionInParentSpaceY() != expectedPosition[i] )
			{
				System.out.println( "Child position for " + i + " is not as expected; expected=" + expectedPosition[i] + ", result=" + children[i].getPositionInParentSpaceY() );
			}
			assertEquals( children[i].getPositionInParentSpaceY(), expectedPosition[i] );
		}
	}

	
	private void vpackYTests(TSBox children[], double spacing, double childPadding[], int packFlags[], TSBox expectedBox, double boxAllocations[], double expectedSize[][], double expectedPosition[][])
	{
		for (int i = 0; i  < boxAllocations.length; i++)
		{
			vpackYTest( children, spacing, childPadding, packFlags, expectedBox, boxAllocations[i], expectedSize[i], expectedPosition[i] );
		}
	}


	private void cumulativePackTests(TSBox children[], double spacing, double childPadding[], int packFlags[], TSBox expectedBox, double boxAllocations[], double expectedSize[][], double expectedPosition[][])
	{
		// Combined horizontal packing in X, and vertical packing in Y
		
		TSBox yChildrenA[] = new TSBox[children.length];
		TSBox yChildrenB[] = new TSBox[children.length];
		
		for (int i = 0; i < children.length; i++)
		{
			TSBox c = children[i];
			yChildrenA[i] = new TSBox( c.getMinHeight(), c.getPrefHeight(), c.getMinVSpacing(), c.getPrefVSpacing(),
					c.getMinWidth(), c.getPrefWidth(), c.getMinHSpacing(), c.getPrefHSpacing() );
			yChildrenB[i] = new TSBox( c.getMinHeight(), c.getPrefHeight(), c.getMinVSpacing(), c.getPrefVSpacing(),
					c.getMinWidth() * 0.5, c.getPrefWidth() * 0.5, c.getMinWidth() * 0.5, c.getPrefWidth() * 0.5, c.getMinHSpacing(), c.getPrefHSpacing() );
		}
		
		TSBox expectedY = new TSBox( expectedBox.getMinHeight(), expectedBox.getPrefHeight(), expectedBox.getMinVSpacing(), expectedBox.getPrefVSpacing(),
					expectedBox.getMinWidth(), expectedBox.getPrefWidth(), expectedBox.getMinHSpacing(), expectedBox.getPrefHSpacing() );
	
		hpackXTests( children, spacing, childPadding, packFlags, expectedBox, boxAllocations, expectedSize, expectedPosition );
		vpackYTests( yChildrenA, spacing, childPadding, packFlags, expectedY, boxAllocations, expectedSize, expectedPosition );
		vpackYTests( yChildrenB, spacing, childPadding, packFlags, expectedY, boxAllocations, expectedSize, expectedPosition );
	}


	public void test_allocateCumulativePacking()
	{
		// cumulativePack( [ <100-200,0-0>, <50-70,0-0> ], spacing=0, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200, 70 ] @ [ 0, 200 ]		- no expansion
		// 	boxAllocation=270   ->   [ 200, 70 ] @ [ 0, 200 ]		- preferred sizes
		// 	boxAllocation=210   ->   [ 150, 60 ] @ [ 0, 150 ]		- space above minimum distributed evenly
		// 	boxAllocation=150   ->   [ 100, 50 ] @ [ 0, 100 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 100 ]		- will not go below minimum
		cumulativePackTests( new TSBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 0.0, null, null,
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

	
		// cumulativePack( [ <100-200,0-0>, <50-70,0-0> ], spacing=10, padding=0, packFlags=0 )
		// 	boxAllocation=300   ->   [ 200, 70 ] @ [ 0, 210 ]		- no expansion
		// 	boxAllocation=280   ->   [ 200, 70 ] @ [ 0, 210 ]		- preferred sizes
		// 	boxAllocation=220   ->   [ 150, 60 ] @ [ 0, 160 ]		- space above minimum distributed evenly
		// 	boxAllocation=160   ->   [ 100, 50 ] @ [ 0, 110 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 110 ]		- will not go below minimum
		cumulativePackTests( new TSBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 10.0, null, null,
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

	
		// cumulativePack( [ <100-200,0-0>, <50-70,0-0> ], spacing=0, padding=[ 10, 20 ], packFlags=0 )
		// 	boxAllocation=400   ->   [ 200, 70 ] @ [ 0, 240 ]		- no expansion
		// 	boxAllocation=330   ->   [ 200, 70 ] @ [ 0, 240 ]		- preferred sizes
		// 	boxAllocation=270   ->   [ 150, 60 ] @ [ 0, 190 ]		- space above minimum distributed evenly
		// 	boxAllocation=210   ->   [ 100, 50 ] @ [ 0, 140 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 140 ]		- will not go below minimum
		cumulativePackTests( new TSBox[] { xbox( 100.0, 200.0, 0.0, 0.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 0.0, new double[] { 10.0, 20.0 }, null,
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

		
		// cumulativePack( [ <100-200,15-15>, <50-70,0-0> ], spacing=0, padding=[ 10, 20 ], packFlags=0 )
		// 	boxAllocation=400   ->   [ 200, 70 ] @ [ 0, 245 ]		- no expansion
		// 	boxAllocation=335   ->   [ 200, 70 ] @ [ 0, 245 ]		- preferred sizes
		// 	boxAllocation=275   ->   [ 150, 60 ] @ [ 0, 195 ]		- space above minimum distributed evenly
		// 	boxAllocation=215   ->   [ 100, 50 ] @ [ 0, 145 ]		- minimum sizes
		// 	boxAllocation=100   ->   [ 100, 50 ] @ [ 0, 145 ]		- will not go below minimum
		cumulativePackTests( new TSBox[] { xbox( 100.0, 200.0, 15.0, 15.0 ),  xbox( 50.0, 70.0, 0.0, 0.0 ) }, 0.0, new double[] { 10.0, 20.0 }, null,
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





	private void vpackXTest(TSBox children[], HAlignment alignment, TSBox expectedBox, double boxAllocation, double expectedSize[], double expectedPosition[])
	{ 
		TSBox box = new TSBox();
		BoxPackingRequisition.maximumX( box, children );
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
		BoxPackingAllocation.allocateVerticalPackingX( box, children, alignment );
		for (int i = 0; i < children.length; i++)
		{
			if ( children[i].getAllocationX() != expectedSize[i] )
			{
				System.out.println( "Child allocation for " + i + " is not as expected; expected=" + expectedSize[i] + ", result=" + children[i].getAllocationX() );
			}
			assertEquals( children[i].getAllocationX(), expectedSize[i] );

			if ( children[i].getPositionInParentSpaceX() != expectedPosition[i] )
			{
				System.out.println( "Child position for " + i + " is not as expected; expected=" + expectedPosition[i] + ", result=" + children[i].getPositionInParentSpaceX() );
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
		// 	boxAllocation=150   ->   [ 200, 150 ] @ [ 0, 0 ]		- below min size, between min and pref
		// 	boxAllocation=100   ->   [ 200, 100 ] @ [ 0, 0 ]		- below min size, min size
		// 	boxAllocation=50   ->   [ 200, 100 ] @ [ 0, 0 ]		- below min size, below min size
		vpackXTests( new TSBox[] { xbox( 200.0, 300.0, 0.0, 0.0 ),  xbox( 100.0, 200.0, 0.0, 0.0 ) }, HAlignment.LEFT,
				xbox( 200.0, 300.0, 0.0, 0.0 ),
				new double[] { 400.0, 300.0, 250.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 250.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 150.0 },
					new double[] { 200.0, 100.0 },
					new double[] { 200.0, 100.0 } },
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
		// 	boxAllocation=150   ->   [ 200, 150 ] @ [ 0, 0 ]		- below min size, between min and pref
		// 	boxAllocation=100   ->   [ 200, 100 ] @ [ 0, 0 ]		- below min size, min size
		// 	boxAllocation=50   ->   [ 200, 100 ] @ [ 0, 0 ]		- below min size, below min size
		vpackXTests( new TSBox[] { xbox( 200.0, 300.0, 0.0, 0.0 ),  xbox( 100.0, 200.0, 0.0, 0.0 ) }, HAlignment.CENTRE,
				xbox( 200.0, 300.0, 0.0, 0.0 ),
				new double[] { 400.0, 300.0, 250.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 250.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 150.0 },
					new double[] { 200.0, 100.0 },
					new double[] { 200.0, 100.0 } },
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
		// 	boxAllocation=150   ->   [ 200, 150 ] @ [ 0, 0 ]		- below min size, between min and pref
		// 	boxAllocation=100   ->   [ 200, 100 ] @ [ 0, 0 ]		- below min size, min size
		// 	boxAllocation=50   ->   [ 200, 100 ] @ [ 0, 0 ]		- below min size, below min size
		vpackXTests( new TSBox[] { xbox( 200.0, 300.0, 0.0, 0.0 ),  xbox( 100.0, 200.0, 0.0, 0.0 ) }, HAlignment.RIGHT,
				xbox( 200.0, 300.0, 0.0, 0.0 ),
				new double[] { 400.0, 300.0, 250.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 300.0, 200.0 },
					new double[] { 300.0, 200.0 },
					new double[] { 250.0, 200.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 150.0 },
					new double[] { 200.0, 100.0 },
					new double[] { 200.0, 100.0 } },
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
		// 	boxAllocation=150   ->   [ 200, 150 ] @ [ 0, 0 ]		- below min size, between min and pref
		// 	boxAllocation=100   ->   [ 200, 100 ] @ [ 0, 0 ]		- below min size, min size
		// 	boxAllocation=50   ->   [ 200, 100 ] @ [ 0, 0 ]		- below min size, below min size
		vpackXTests( new TSBox[] { xbox( 200.0, 300.0, 0.0, 0.0 ),  xbox( 100.0, 200.0, 0.0, 0.0 ) }, HAlignment.EXPAND,
				xbox( 200.0, 300.0, 0.0, 0.0 ),
				new double[] { 400.0, 300.0, 250.0, 200.0, 150.0, 100.0, 50.0 },
				new double[][] {
					new double[] { 400.0, 400.0 },
					new double[] { 300.0, 300.0 },
					new double[] { 250.0, 250.0 },
					new double[] { 200.0, 200.0 },
					new double[] { 200.0, 150.0 },
					new double[] { 200.0, 100.0 },
					new double[] { 200.0, 100.0 } },
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
