//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Math;

import BritefuryJ.Math.Point2;

public class AABox2 implements Cloneable
{
	private double lowerX, lowerY, upperX, upperY;
	
	
	public AABox2()
	{
		lowerX = lowerY = 1.0;
		upperX = upperY = -1.0;
	}
	
	public AABox2(Point2 a)
	{
		lowerX = upperX = a.x;
		lowerY = upperY = a.y;
	}
	
	public AABox2(Point2 a, Point2 b)
	{
		lowerX = a.x < b.x  ?  a.x  :  b.x;
		lowerY = a.y < b.y  ?  a.y  :  b.y;
		upperX = a.x < b.x  ?  b.x  :  a.x;
		upperY = a.y < b.y  ?  b.y  :  a.y;
	}
	
	public AABox2(Point2 a, Vector2 size)
	{
		assert size.x >= 0.0  &&  size.y >= 0.0;
		lowerX = a.x;
		lowerY = a.y;
		upperX = a.x + size.x;
		upperY = a.y + size.y;
	}
	
	
	
	public boolean containsPoint(Point2 p)
	{
		return p.x >= lowerX  &&  p.x <= upperX  &&  p.y >= lowerY  &&  p.y <= upperY;
	}
	
	public boolean intersects(AABox2 b)
	{
		return lowerX <= b.upperX  &&  lowerY <= b.upperY   &&   upperX >= b.lowerX  &&  upperY >= b.lowerY;
	}
	
	
	public Point2 getLower()
	{
		return new Point2( lowerX, lowerY );
	}
	
	public Point2 getUpper()
	{
		return new Point2( upperX, upperY );
	}
	

	public String toString()
	{
		return "AABox2( " + lowerX + "," + lowerY + " -> " + lowerX + "," + lowerY + " )";
	}
}
