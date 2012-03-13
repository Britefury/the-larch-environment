//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class Anchor
{
	public static final Anchor LEFT = new Anchor( 0.0, 0.5 );
	public static final Anchor RIGHT = new Anchor( 1.0, 0.5 );
	public static final Anchor TOP = new Anchor( 0.5, 0.0 );
	public static final Anchor BOTTOM = new Anchor( 0.5, 1.0 );

	public static final Anchor TOP_LEFT = new Anchor( 0.0, 0.0 );
	public static final Anchor TOP_RIGHT = new Anchor( 1.0, 0.0 );
	public static final Anchor BOTTOM_LEFT = new Anchor( 0.0, 1.0 );
	public static final Anchor BOTTOM_RIGHT = new Anchor( 1.0, 1.0 );
	
	public static final Anchor CENTRE = new Anchor( 0.5, 0.5 );
	
	private double propX, propY;
	
	
	
	public Anchor()
	{
		propX = 0.0;
		propY = 0.0;
	}
	
	public Anchor(double propX, double propY)
	{
		this.propX = propX;
		this.propY = propY;
	}
	
	
	
	public double getPropX()
	{
		return propX;
	}
	
	public double getPropY()
	{
		return propY;
	}
	
	
	
	
	public Point2 getBoxCorner(AABox2 box)
	{
		return box.getLower().add( new Vector2( box.getWidth() * propX, box.getHeight() * propY ) );
	}
}
