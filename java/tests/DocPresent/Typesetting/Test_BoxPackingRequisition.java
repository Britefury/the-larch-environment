//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Typesetting;

import BritefuryJ.DocPresent.Typesetting.BoxPackingRequisition;
import BritefuryJ.DocPresent.Typesetting.TSBox;
import BritefuryJ.DocPresent.Typesetting.VAlignment;
import junit.framework.TestCase;

public class Test_BoxPackingRequisition extends TestCase
{
	private TSBox xbox(double width, double hspacing)
	{
		return new TSBox( width, hspacing, 0.0, 0.0 );
	}
	
	public void test_maximumX()
	{
		TSBox result = new TSBox();
		
		// Empty list should result in empty
		BoxPackingRequisition.maximumX( result, new TSBox[] {} );
		assertEquals( result, new TSBox() );

		// List of one empty box should result in empty
		BoxPackingRequisition.maximumX( result, new TSBox[] { new TSBox() } );
		assertEquals( result, new TSBox() );

		// 1 Box of width 1 should result in same
		BoxPackingRequisition.maximumX( result, new TSBox[] { xbox( 1.0, 0.0 ) } );
		assertEquals( result, xbox( 1.0, 0.0 ) );

		// 1 Box of width 10, hspacing 10 should result in same
		BoxPackingRequisition.maximumX( result, new TSBox[] { xbox( 10.0, 1.0 ) } );
		assertEquals( result, xbox( 10.0, 1.0 ) );

		// max( [ <10,1>, <20,1> ] )  ->  <20,1>
		BoxPackingRequisition.maximumX( result, new TSBox[] { xbox( 10.0, 1.0 ),  xbox( 20.0, 1.0 ) } );
		assertEquals( result, xbox( 20.0, 1.0 ) );

		// max( [ <1,10>, <2,20> ] )  ->  <2,20>
		BoxPackingRequisition.maximumX( result, new TSBox[] { xbox( 1.0, 10.0 ),  xbox( 2.0, 20.0 ) } );
		assertEquals( result, xbox( 2.0, 20.0 ) );

		// max( [ <10,3>, <11,1> ] )  ->  <11,2>
		// The first box advances X the most overall, although the second has the greater width
		BoxPackingRequisition.maximumX( result, new TSBox[] { xbox( 10.0, 3.0 ),  xbox( 11.0, 1.0 ) } );
		assertEquals( result, xbox( 11.0, 2.0 ) );

		// max( [ <10,5>, <5,10> ] )  ->  <10,5>
		// Both advance X by the same amount (15 units), but the first has the greater width
		BoxPackingRequisition.maximumX( result, new TSBox[] { xbox( 10.0, 5.0 ),  xbox( 5.0, 10.0 ) } );
		assertEquals( result, xbox( 10.0, 5.0 ) );
	}


	public void test_accumulateX()
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing

		TSBox result = new TSBox();
		
		// accum()  ->  <0,0>
		BoxPackingRequisition.accumulateX( result, new TSBox[] {},  0.0, null );
		assertEquals( result, new TSBox() );

		// accum( [ <0,0> ] )  ->  <0,0>
		BoxPackingRequisition.accumulateX( result, new TSBox[] { new TSBox() },  0.0, null );
		assertEquals( result, new TSBox() );

		// accum( [ <0,0>:pad=1 ] )  ->  <2,0>
		BoxPackingRequisition.accumulateX( result, new TSBox[] { new TSBox() },  0.0, new double[] { 1.0 } );
		assertEquals( result, xbox( 2.0, 0.0 ) );

		// accum( [ <10,0>:pad=2 ] )  ->  <14,0>
		BoxPackingRequisition.accumulateX( result, new TSBox[] { xbox( 10.0, 0.0 ) },  0.0, new double[] { 2.0 } );
		assertEquals( result, xbox( 14.0, 0.0 ) );

		// Padding 'consumes' h-spacing
		// accum( [ <10,1>:pad=2 ] )  ->  <14,0>
		BoxPackingRequisition.accumulateX( result, new TSBox[] { xbox( 10.0, 1.0 ) },  0.0, new double[] { 2.0 } );
		assertEquals( result, xbox( 14.0, 0.0 ) );

		// Padding 'consumes' all h-spacing
		// accum( [ <10,3>:pad=2 ] )  ->  <14,1>
		BoxPackingRequisition.accumulateX( result, new TSBox[] { xbox( 10.0, 3.0 ) },  0.0, new double[] { 2.0 } );
		assertEquals( result, xbox( 14.0, 1.0 ) );

		// accum( [ <0,0>, <0,0> ] )  ->  <0,0>
		BoxPackingRequisition.accumulateX( result, new TSBox[] { new TSBox(), new TSBox() },  0.0, null );
		assertEquals( result, new TSBox() );

		// Width accumulates
		// accum( [ <10,0>, <5,0> ] )  ->  <15,0>
		BoxPackingRequisition.accumulateX( result, new TSBox[] { xbox( 10.0, 0.0 ), xbox( 5.0, 0.0 ) },  0.0, null );
		assertEquals( result, xbox( 15.0, 0.0 ) );

		// H-spacing of child puts space before next child
		// accum( [ <10,2>, <5,0> ] )  ->  <17,0>
		BoxPackingRequisition.accumulateX( result, new TSBox[] { xbox( 10.0, 2.0 ), xbox( 5.0, 0.0 ) },  0.0, null );
		assertEquals( result, xbox( 17.0, 0.0 ) );

		// H-spacing of last child gets put onto the result
		// accum( [ <10,2>, <5,1> ] )  ->  <17,1>
		BoxPackingRequisition.accumulateX( result, new TSBox[] { xbox( 10.0, 2.0 ), xbox( 5.0, 1.0 ) },  0.0, null );
		assertEquals( result, xbox( 17.0, 1.0 ) );

		// Spacing between children adds extra width
		// accum( [ <0,0>, <0,0> ], spacing=1 )  ->  <1,0>
		BoxPackingRequisition.accumulateX( result, new TSBox[] { new TSBox(), new TSBox() },  1.0, null );
		assertEquals( result, xbox( 1.0, 0.0 ) );
		// accum( [ <10,0>, <5,0> ], spacing=1 )  ->  <15,0>
		BoxPackingRequisition.accumulateX( result, new TSBox[] { xbox( 10.0, 0.0 ), xbox( 5.0, 0.0 ) },  1.0, null );
		assertEquals( result, xbox( 16.0, 0.0 ) );

		// Spacing between children is added to the child's own spacing
		// accum( [ <10,2>, <5,1> ], spacing=1 )  ->  <18,1>
		BoxPackingRequisition.accumulateX( result, new TSBox[] { xbox( 10.0, 2.0 ), xbox( 5.0, 1.0 ) },  1.0, null );
		assertEquals( result, xbox( 18.0, 1.0 ) );
	}



	private TSBox ybox(double height, double vspacing)
	{
		return new TSBox( 0.0, 0.0, height, vspacing );
	}
	
	private TSBox ybbox(double ascent, double descent, double vspacing)
	{
		return new TSBox( 0.0, 0.0, ascent, descent, vspacing );
	}
	
	public void test_maximumY()
	{
		TSBox result = new TSBox();
		
		
		// First, test the simple cases of no baseline alignment, or with baseline alignment with children that do not have baselines
		
		// Empty list should result in empty
		BoxPackingRequisition.maximumY( result, new TSBox[] {}, VAlignment.CENTRE );
		assertEquals( result, new TSBox() );
		BoxPackingRequisition.maximumY( result, new TSBox[] {}, VAlignment.BASELINES );
		assertEquals( result, new TSBox() );

		// List of one empty box should result in empty
		BoxPackingRequisition.maximumY( result, new TSBox[] { new TSBox() }, VAlignment.CENTRE );
		assertEquals( result, new TSBox() );
		BoxPackingRequisition.maximumY( result, new TSBox[] { new TSBox() }, VAlignment.BASELINES );
		assertEquals( result, new TSBox() );

		// 1 Box of height 1 should result in same
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybox( 1.0, 0.0 ) }, VAlignment.CENTRE );
		assertEquals( result, ybox( 1.0, 0.0 ) );
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybox( 1.0, 0.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybox( 1.0, 0.0 ) );

		// 1 Box of height 10, vspacing 1 should result in same
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybox( 10.0, 1.0 ) }, VAlignment.CENTRE );
		assertEquals( result, ybox( 10.0, 1.0 ) );
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybox( 10.0, 1.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybox( 10.0, 1.0 ) );

		// max( [ <10,1>, <20,1> ] )  ->  <20,1>
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybox( 10.0, 1.0),  ybox( 20.0, 1.0 ) }, VAlignment.CENTRE );
		assertEquals( result, ybox( 20.0, 1.0 ) );
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybox( 10.0, 1.0),  ybox( 20.0, 1.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybox( 20.0, 1.0 ) );

		// max( [ <1,10>, <2,20> ] )  ->  <2,20>
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybox( 1.0, 10.0 ),  ybox( 2.0, 20.0 ) }, VAlignment.CENTRE );
		assertEquals( result, ybox( 2.0, 20.0 ) );
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybox( 1.0, 10.0 ),  ybox( 2.0, 20.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybox( 2.0, 20.0 ) );

		// max( [ <10,3>, <11,1> ] )  ->  <11,2>
		// The first box advances X the most overall, although the second has the greater height
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybox( 10.0, 3.0 ),  ybox( 11.0, 1.0 ) }, VAlignment.CENTRE );
		assertEquals( result, ybox( 11.0, 2.0 ) );
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybox( 10.0, 3.0 ),  ybox( 11.0, 1.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybox( 11.0, 2.0 ) );

		// max( [ <10,5>, <5,10> ] )  ->  <10,5>
		// Both advance X by the same amount (15 units), but the first has the greater height
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybox( 10.0, 5.0 ),  ybox( 5.0, 10.0 ) }, VAlignment.CENTRE );
		assertEquals( result, ybox( 10.0, 5.0 ) );
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybox( 10.0, 5.0 ),  ybox( 5.0, 10.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybox( 10.0, 5.0 ) );

	
	
	
		// Now test the cases where baselines are used
		
		// 1 Box of 3:2 should result in same
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybbox( 3.0, 2.0, 0.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 3.0, 2.0, 0.0 ) );

		// 1 Box of height 3:2, vspacing 1 should result in same
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybbox( 3.0, 2.0, 1.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 3.0, 2.0, 1.0 ) );

		// max( [ <5:3,0>, <2:4,0> ] )  ->  <5:4,0>
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybbox( 5.0, 3.0, 0.0 ),  ybbox( 2.0, 4.0, 0.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 5.0, 4.0, 0.0 ) );

		// max( [ <5:3,1>, <2:4,1> ] )  ->  <5:4,1>
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybbox( 5.0, 3.0, 1.0 ),  ybbox( 2.0, 4.0, 1.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 5.0, 4.0, 1.0 ) );

		// max( [ <5:3,1>, <2:4,2> ] )  ->  <5:4,2>
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybbox( 5.0, 3.0, 1.0 ),  ybbox( 2.0, 4.0, 2.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 5.0, 4.0, 2.0 ) );

		// max( [ <5:3,3>, <2:4,1> ] )  ->  <5:4,2>
		// The first box advances Y (below baseline) the most overall, although the second has the greater descent
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybbox( 5.0, 3.0, 3.0 ),  ybbox( 2.0, 4.0, 1.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 5.0, 4.0, 2.0 ) );

		// max( [ <2:4,2>, <5:2,4> ] )  ->  <5:4,2>
		// Both advance T (below baseline) by the same amount (6 units), but the first has the greater descent
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybbox( 2.0, 4.0, 2.0 ),  ybbox( 5.0, 2.0, 4.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 5.0, 4.0, 2.0 ) );
		
		
		
		// Now test the situation where baseline alignment is used, but the some children do not have baselines

		// max( [ <6:3,0>, <8,0> ] )  ->  max( [ <6:3,0>, <4+3:4-3,0> ] )  ->  max( [ <6:3,0>, <7:1,0> ] )  ->  <7:3,0>
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybbox( 6.0, 3.0, 0.0 ),  ybox( 8.0, 0.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 7.0, 3.0, 0.0 ) );

		// max( [ <6:3,2>, <8,1> ] )  ->  max( [ <6:3,2>, <4+3:4-3,1> ] )  ->  max( [ <6:3,2>, <7:1,1> ] )  ->  <7:3,2>
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybbox( 6.0, 3.0, 2.0 ),  ybox( 8.0, 1.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 7.0, 3.0, 2.0 ) );

		// max( [ <6:3,1>, <8,2> ] )  ->  max( [ <6:3,1>, <4+3:4-3,2> ] )  ->  max( [ <6:3,1>, <7:1,2> ] )  ->  <7:3,1>
		BoxPackingRequisition.maximumY( result, new TSBox[] { ybbox( 6.0, 3.0, 1.0 ),  ybox( 8.0, 2.0 ) }, VAlignment.BASELINES );
		assertEquals( result, ybbox( 7.0, 3.0, 1.0 ) );
	}


	public void test_accumulateY()
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing

		TSBox result = new TSBox();
		
		// accum()  ->  <0,0>
		BoxPackingRequisition.accumulateY( result, new TSBox[] {},  0.0, null );
		assertEquals( result, new TSBox() );

		// accum( [ <0,0> ] )  ->  <0,0>
		BoxPackingRequisition.accumulateY( result, new TSBox[] { new TSBox() },  0.0, null );
		assertEquals( result, new TSBox() );

		// accum( [ <0,0>:pad=1 ] )  ->  <2,0>
		BoxPackingRequisition.accumulateY( result, new TSBox[] { new TSBox() },  0.0, new double[] { 1.0 } );
		assertEquals( result, ybox( 2.0, 0.0 ) );

		// accum( [ <10,0>:pad=2 ] )  ->  <14,0>
		BoxPackingRequisition.accumulateY( result, new TSBox[] { ybox( 10.0, 0.0 ) },  0.0, new double[] { 2.0 } );
		assertEquals( result, ybox( 14.0, 0.0 ) );

		// Padding 'consumes' h-spacing
		// accum( [ <10,1>:pad=2 ] )  ->  <14,0>
		BoxPackingRequisition.accumulateY( result, new TSBox[] { ybox( 10.0, 1.0 ) },  0.0, new double[] { 2.0 } );
		assertEquals( result, ybox( 14.0, 0.0 ) );

		// Padding 'consumes' all h-spacing
		// accum( [ <10,3>:pad=2 ] )  ->  <14,1>
		BoxPackingRequisition.accumulateY( result, new TSBox[] { ybox( 10.0, 3.0 ) },  0.0, new double[] { 2.0 } );
		assertEquals( result, ybox( 14.0, 1.0 ) );

		// accum( [ <0,0>, <0,0> ] )  ->  <0,0>
		BoxPackingRequisition.accumulateY( result, new TSBox[] { new TSBox(), new TSBox() },  0.0, null );
		assertEquals( result, new TSBox() );

		// Width accumulates
		// accum( [ <10,0>, <5,0> ] )  ->  <15,0>
		BoxPackingRequisition.accumulateY( result, new TSBox[] { ybox( 10.0, 0.0 ), ybox( 5.0, 0.0 ) },  0.0, null );
		assertEquals( result, ybox( 15.0, 0.0 ) );

		// H-spacing of child puts space before next child
		// accum( [ <10,2>, <5,0> ] )  ->  <17,0>
		BoxPackingRequisition.accumulateY( result, new TSBox[] { ybox( 10.0, 2.0 ), ybox( 5.0, 0.0 ) },  0.0, null );
		assertEquals( result, ybox( 17.0, 0.0 ) );

		// H-spacing of last child gets put onto the result
		// accum( [ <10,2>, <5,1> ] )  ->  <17,1>
		BoxPackingRequisition.accumulateY( result, new TSBox[] { ybox( 10.0, 2.0 ), ybox( 5.0, 1.0 ) },  0.0, null );
		assertEquals( result, ybox( 17.0, 1.0 ) );

		// Spacing between children adds extra width
		// accum( [ <0,0>, <0,0> ], spacing=1 )  ->  <1,0>
		BoxPackingRequisition.accumulateY( result, new TSBox[] { new TSBox(), new TSBox() },  1.0, null );
		assertEquals( result, ybox( 1.0, 0.0 ) );
		// accum( [ <10,0>, <5,0> ], spacing=1 )  ->  <15,0>
		BoxPackingRequisition.accumulateY( result, new TSBox[] { ybox( 10.0, 0.0 ), ybox( 5.0, 0.0 ) },  1.0, null );
		assertEquals( result, ybox( 16.0, 0.0 ) );

		// Spacing between children is added to the child's own spacing
		// accum( [ <10,2>, <5,1> ], spacing=1 )  ->  <18,1>
		BoxPackingRequisition.accumulateY( result, new TSBox[] { ybox( 10.0, 2.0 ), ybox( 5.0, 1.0 ) },  1.0, null );
		assertEquals( result, ybox( 18.0, 1.0 ) );
	}
}
