//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Typesetting;

import BritefuryJ.DocPresent.Typesetting.TSBox;
import junit.framework.TestCase;

public class Test_BoxPack_base extends TestCase
{
	protected TSBox xbox(double width, double hspacing)
	{
		return new TSBox( width, hspacing, 0.0, 0.0 );
	}
	
	protected TSBox xbox(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing)
	{
		return new TSBox( minWidth, prefWidth, minHSpacing, prefHSpacing, 0.0, 0.0 );
	}
	
	
	protected TSBox ybox(double height, double vspacing)
	{
		return new TSBox( 0.0, 0.0, height, vspacing );
	}
	
	protected TSBox ybbox(double ascent, double descent, double vspacing)
	{
		return new TSBox( 0.0, 0.0, ascent, descent, vspacing );
	}
	
	
	protected TSBox box(double width, double hspacing, double height, double vspacing)
	{
		return new TSBox( width, hspacing, height, vspacing );
	}

	protected TSBox box(double width, double hspacing, double ascent, double descent, double vspacing)
	{
		return new TSBox( width, hspacing, ascent, descent, vspacing );
	}

	protected TSBox box(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double height, double vspacing)
	{
		return new TSBox( minWidth, prefWidth, minHSpacing, prefHSpacing, height, vspacing );
	}

	protected TSBox box(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double ascent, double descent, double vspacing)
	{
		return new TSBox( minWidth, prefWidth, minHSpacing, prefHSpacing, ascent, descent, vspacing );
	}
}
