//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public enum Corner
{
	TOP_LEFT,
	BOTTOM_LEFT,
	TOP_RIGHT,
	BOTTOM_RIGHT;


	public Point2 getBoxCorner(AABox2 box)
	{
		switch (this)
		{
			case TOP_LEFT:
				return box.getLower();
			case BOTTOM_LEFT:
				return new Point2( box.getLowerX(), box.getUpperY() );
			case TOP_RIGHT:
				return new Point2( box.getUpperX(), box.getLowerY() );
			case BOTTOM_RIGHT:
				return box.getUpper();
			default:
				return box.getLower();
		}
	}
}
