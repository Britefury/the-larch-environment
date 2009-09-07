//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Layout;

import junit.framework.TestCase;
import BritefuryJ.DocPresent.Layout.ElementAlignment;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LReqBox;

public class Test_Layout_base extends TestCase
{
	protected static int HLEFT = ElementAlignment.HALIGN_LEFT;
	protected static int HCENTRE = ElementAlignment.HALIGN_CENTRE;
	protected static int HRIGHT = ElementAlignment.HALIGN_RIGHT;
	protected static int HEXPAND = ElementAlignment.HALIGN_EXPAND;

	protected static int VBASELINES = ElementAlignment.VALIGN_BASELINES;
	protected static int VBASELINES_EXPAND = ElementAlignment.VALIGN_BASELINES_EXPAND;
	protected static int VTOP = ElementAlignment.VALIGN_TOP;
	protected static int VCENTRE = ElementAlignment.VALIGN_CENTRE;
	protected static int VBOTTOM = ElementAlignment.VALIGN_BOTTOM;
	protected static int VEXPAND = ElementAlignment.VALIGN_EXPAND;
	
	
	protected LReqBox xbox(double width, double hAdvance)
	{
		return new LReqBox( width, hAdvance, 0.0, 0.0 );
	}
	
	protected LReqBox xbox(double minWidth, double prefWidth, double minHAdvance, double prefHAdvance)
	{
		return new LReqBox( minWidth, prefWidth, minHAdvance, prefHAdvance, 0.0, 0.0 );
	}
	
	
	protected LReqBox ybox(double height, double vspacing)
	{
		return new LReqBox( 0.0, 0.0, height, vspacing );
	}
	
	protected LReqBox ybbox(double ascent, double descent, double vspacing)
	{
		return new LReqBox( 0.0, 0.0, ascent, descent, vspacing );
	}
	
	
	protected LReqBox box(double width, double hAdvance, double height, double vspacing)
	{
		return new LReqBox( width, hAdvance, height, vspacing );
	}

	protected LReqBox box(double width, double hAdvance, double ascent, double descent, double vspacing)
	{
		return new LReqBox( width, hAdvance, ascent, descent, vspacing );
	}

	protected LReqBox box(double minWidth, double prefWidth, double minHAdvance, double prefHAdvance, double height, double vspacing)
	{
		return new LReqBox( minWidth, prefWidth, minHAdvance, prefHAdvance, height, vspacing );
	}

	protected LReqBox box(double minWidth, double prefWidth, double minHAdvance, double prefHAdvance, double ascent, double descent, double vspacing)
	{
		return new LReqBox( minWidth, prefWidth, minHAdvance, prefHAdvance, ascent, descent, vspacing );
	}



	protected LAllocBox alloc(double x, double y, double w, double h)
	{
		return new LAllocBox( x, y, w, h * 0.5, h * 0.5, null, false );
	}
	
	protected LAllocBox alloc(double x, double y, double w, double a, double d)
	{
		return new LAllocBox( x, y, w, a, d, null, false );
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
