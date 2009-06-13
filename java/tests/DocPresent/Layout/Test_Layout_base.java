//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Layout;

import BritefuryJ.DocPresent.Layout.LBox;
import junit.framework.TestCase;

public class Test_Layout_base extends TestCase
{
	protected LBox xbox(double width, double hspacing)
	{
		return new LBox( width, hspacing, 0.0, 0.0 );
	}
	
	protected LBox xbox(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing)
	{
		return new LBox( minWidth, prefWidth, minHSpacing, prefHSpacing, 0.0, 0.0 );
	}
	
	
	protected LBox ybox(double height, double vspacing)
	{
		return new LBox( 0.0, 0.0, height, vspacing );
	}
	
	protected LBox ybbox(double ascent, double descent, double vspacing)
	{
		return new LBox( 0.0, 0.0, ascent, descent, vspacing );
	}
	
	
	protected LBox box(double width, double hspacing, double height, double vspacing)
	{
		return new LBox( width, hspacing, height, vspacing );
	}

	protected LBox box(double width, double hspacing, double ascent, double descent, double vspacing)
	{
		return new LBox( width, hspacing, ascent, descent, vspacing );
	}

	protected LBox box(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double height, double vspacing)
	{
		return new LBox( minWidth, prefWidth, minHSpacing, prefHSpacing, height, vspacing );
	}

	protected LBox box(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double ascent, double descent, double vspacing)
	{
		return new LBox( minWidth, prefWidth, minHSpacing, prefHSpacing, ascent, descent, vspacing );
	}



	protected LBox alloc(double x, double y, double w, double h)
	{
		LBox box = new LBox();
		box.setAllocationX( w );
		box.setAllocationY( h );
		box.setPositionInParentSpaceX( x );
		box.setPositionInParentSpaceY( y );
		return box;
	}
	
	
	
	protected void assertBoxesEqual(LBox result, LBox expected, String description)
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
