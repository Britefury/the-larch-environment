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

public abstract class Test_Layout_base extends TestCase
{
	protected static int HLEFT = ElementAlignment.HALIGN_LEFT;
	protected static int HCENTRE = ElementAlignment.HALIGN_CENTRE;
	protected static int HRIGHT = ElementAlignment.HALIGN_RIGHT;
	protected static int HEXPAND = ElementAlignment.HALIGN_EXPAND;

	protected static int VREFY = ElementAlignment.VALIGN_REFY;
	protected static int VREFY_EXPAND = ElementAlignment.VALIGN_REFY_EXPAND;
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
		return new LReqBox( minWidth, prefWidth, minHAdvance, prefHAdvance, 0.0, 0.0, 0.0 );
	}
	
	
	protected LReqBox ybox(double height, double vspacing)
	{
		return new LReqBox( 0.0, 0.0, height, vspacing );
	}
	
	protected LReqBox yrbox(double height, double vspacing, double refY)
	{
		return new LReqBox( 0.0, 0.0, height, vspacing, refY );
	}
	
	
	protected LReqBox box(double width, double hAdvance, double height, double vspacing)
	{
		return new LReqBox( width, hAdvance, height, vspacing );
	}

	protected LReqBox rbox(double width, double hAdvance, double height, double vspacing, double refY)
	{
		return new LReqBox( width, hAdvance, height, vspacing, refY );
	}

	protected LReqBox box(double minWidth, double prefWidth, double minHAdvance, double prefHAdvance, double height, double vspacing)
	{
		return new LReqBox( minWidth, prefWidth, minHAdvance, prefHAdvance, height, vspacing, height * 0.5 );
	}

	protected LReqBox rbox(double minWidth, double prefWidth, double minHAdvance, double prefHAdvance, double height, double vspacing, double refY)
	{
		return new LReqBox( minWidth, prefWidth, minHAdvance, prefHAdvance, height, vspacing, refY );
	}



	protected LAllocBox alloc(double x, double y, double width, double height)
	{
		return new LAllocBox( x, y, width, height, width, height * 0.5, null );
	}
	
	protected LAllocBox allocr(double x, double y, double width, double height, double refY)
	{
		return new LAllocBox( x, y, width, height, width, refY, null );
	}
	
	
	protected LAllocBox alloc(double x, double y, double width, double height, double actualWidth)
	{
		return new LAllocBox( x, y, width, height, actualWidth, height * 0.5, null );
	}
	
	protected LAllocBox allocr(double x, double y, double width, double height, double refY, double actualWidth)
	{
		return new LAllocBox( x, y, width, height, actualWidth, refY, null );
	}
	
	
	
	protected void assertBoxesEqual(LReqBox result, LReqBox expected)
	{
		assertBoxesEqual( result, expected, "REQUISITION" );
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


	protected void assertAllocsEqual(LAllocBox result, LAllocBox expected)
	{
		assertAllocsEqual( result, expected, "ALLOCATION" );
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
