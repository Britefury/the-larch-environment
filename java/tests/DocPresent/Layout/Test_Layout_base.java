//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Layout;

import junit.framework.TestCase;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.VAlignment;

public class Test_Layout_base extends TestCase
{
	protected LReqBox xbox(double width, double hspacing)
	{
		return new LReqBox( width, hspacing, 0.0, 0.0 );
	}
	
	protected LReqBox xbox(HAlignment hAlign, double width, double hspacing)
	{
		return new LReqBox( hAlign, VAlignment.BASELINES, width, hspacing, 0.0, 0.0 );
	}
	
	protected LReqBox xbox(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing)
	{
		return new LReqBox( minWidth, prefWidth, minHSpacing, prefHSpacing, 0.0, 0.0 );
	}
	
	protected LReqBox xbox(HAlignment hAlign, double minWidth, double prefWidth, double minHSpacing, double prefHSpacing)
	{
		return new LReqBox( hAlign, VAlignment.BASELINES, minWidth, prefWidth, minHSpacing, prefHSpacing, 0.0, 0.0 );
	}
	
	
	protected LReqBox ybox(double height, double vspacing)
	{
		return new LReqBox( 0.0, 0.0, height, vspacing );
	}
	
	protected LReqBox ybox(VAlignment vAlign, double height, double vspacing)
	{
		return new LReqBox( HAlignment.LEFT, vAlign, 0.0, 0.0, height, vspacing );
	}
	
	protected LReqBox ybbox(double ascent, double descent, double vspacing)
	{
		return new LReqBox( 0.0, 0.0, ascent, descent, vspacing );
	}
	
	protected LReqBox ybbox(VAlignment vAlign, double ascent, double descent, double vspacing)
	{
		return new LReqBox( HAlignment.LEFT, vAlign, 0.0, 0.0, ascent, descent, vspacing );
	}
	
	
	protected LReqBox box(double width, double hspacing, double height, double vspacing)
	{
		return new LReqBox( width, hspacing, height, vspacing );
	}

	protected LReqBox box(double width, double hspacing, double ascent, double descent, double vspacing)
	{
		return new LReqBox( width, hspacing, ascent, descent, vspacing );
	}

	protected LReqBox box(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double height, double vspacing)
	{
		return new LReqBox( minWidth, prefWidth, minHSpacing, prefHSpacing, height, vspacing );
	}

	protected LReqBox box(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double ascent, double descent, double vspacing)
	{
		return new LReqBox( minWidth, prefWidth, minHSpacing, prefHSpacing, ascent, descent, vspacing );
	}


	protected LReqBox box(VAlignment vAlign, double width, double hspacing, double height, double vspacing)
	{
		return new LReqBox( HAlignment.LEFT, vAlign, width, hspacing, height, vspacing );
	}

	protected LReqBox box(VAlignment vAlign, double width, double hspacing, double ascent, double descent, double vspacing)
	{
		return new LReqBox( HAlignment.LEFT, vAlign, width, hspacing, ascent, descent, vspacing );
	}

	protected LReqBox box(VAlignment vAlign, double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double height, double vspacing)
	{
		return new LReqBox( HAlignment.LEFT, vAlign, minWidth, prefWidth, minHSpacing, prefHSpacing, height, vspacing );
	}

	protected LReqBox box(VAlignment vAlign, double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double ascent, double descent, double vspacing)
	{
		return new LReqBox( HAlignment.LEFT, vAlign, minWidth, prefWidth, minHSpacing, prefHSpacing, ascent, descent, vspacing );
	}


	protected LReqBox box(HAlignment hAlign, double width, double hspacing, double height, double vspacing)
	{
		return new LReqBox( hAlign, VAlignment.BASELINES, width, hspacing, height, vspacing );
	}

	protected LReqBox box(HAlignment hAlign, double width, double hspacing, double ascent, double descent, double vspacing)
	{
		return new LReqBox( hAlign, VAlignment.BASELINES, width, hspacing, ascent, descent, vspacing );
	}

	protected LReqBox box(HAlignment hAlign, double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double height, double vspacing)
	{
		return new LReqBox( hAlign, VAlignment.BASELINES, minWidth, prefWidth, minHSpacing, prefHSpacing, height, vspacing );
	}

	protected LReqBox box(HAlignment hAlign, double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double ascent, double descent, double vspacing)
	{
		return new LReqBox( hAlign, VAlignment.BASELINES, minWidth, prefWidth, minHSpacing, prefHSpacing, ascent, descent, vspacing );
	}


	protected LReqBox box(HAlignment hAlign, VAlignment vAlign, double width, double hspacing, double height, double vspacing)
	{
		return new LReqBox( hAlign, vAlign, width, hspacing, height, vspacing );
	}

	protected LReqBox box(HAlignment hAlign, VAlignment vAlign, double width, double hspacing, double ascent, double descent, double vspacing)
	{
		return new LReqBox( hAlign, vAlign, width, hspacing, ascent, descent, vspacing );
	}

	protected LReqBox box(HAlignment hAlign, VAlignment vAlign, double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double height, double vspacing)
	{
		return new LReqBox( hAlign, vAlign, minWidth, prefWidth, minHSpacing, prefHSpacing, height, vspacing );
	}

	protected LReqBox box(HAlignment hAlign, VAlignment vAlign, double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double ascent, double descent, double vspacing)
	{
		return new LReqBox( hAlign, vAlign, minWidth, prefWidth, minHSpacing, prefHSpacing, ascent, descent, vspacing );
	}



	protected LAllocBox alloc(double x, double y, double w, double h)
	{
		LAllocBox box = new LAllocBox( null );
		box.setAllocationX( w );
		box.setAllocationY( h );
		box.setPositionInParentSpaceX( x );
		box.setPositionInParentSpaceY( y );
		return box;
	}
	
	protected LAllocBox alloc(double x, double y, double w, double a, double d)
	{
		LAllocBox box = new LAllocBox( null );
		box.setAllocationX( w );
		box.setAllocationY( a, d );
		box.setPositionInParentSpaceX( x );
		box.setPositionInParentSpaceY( y );
		return box;
	}
	
	
	
	protected void assertBoxesEqual(LReqBox result, LReqBox expected, String description)
	{
		if ( !result.equals( expected ) )
		{
			System.out.println( description + " IS NOT AS EXPECTED" );
			System.out.println( "EXPECTED" );
			System.out.println( expected );
			System.out.println( "RESULT" );
			System.out.println( result );
		}
		assertEquals( result, expected );
	}


	protected void assertAllocsEqual(LAllocBox result, LAllocBox expected, String description)
	{
		if ( !result.equals( expected ) )
		{
			System.out.println( description + " IS NOT AS EXPECTED" );
			System.out.println( "EXPECTED" );
			System.out.println( expected );
			System.out.println( "RESULT" );
			System.out.println( result );
		}
		assertEquals( result, expected );
	}
}
