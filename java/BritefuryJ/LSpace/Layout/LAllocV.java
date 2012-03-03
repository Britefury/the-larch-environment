//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.Layout;


public class LAllocV
{
	protected double height, refY;

	
	public LAllocV(double height)
	{
		this.height = height;
		refY = height * 0.5;
	}
	
	public LAllocV(double height, double refY)
	{
		this.height = height;
		this.refY = refY;
	}
	
	public LAllocV(LReqBoxInterface req)
	{
		this.height = req.getReqHeight();
		this.refY = req.getReqRefY();
	}
	
	
	public double getHeight()
	{
		return height;
	}
	
	public double getRefY()
	{
		return refY;
	}
	
	
	
	public LAllocV borderY(double topMargin, double bottomMargin)
	{
		return new LAllocV( height - ( topMargin + bottomMargin ), refY - topMargin );
	}
	
	public LAllocV expandToHeight(double h)
	{
		double padding = ( h - height ) * 0.5;
		return new LAllocV( h, refY + padding );
	}



	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof LAllocV )
		{
			LAllocV v = (LAllocV)x;
			
			return height == v.height  &&  refY == v.refY;
		}
		else
		{
			return false;
		}
	}
	
	
	public String toString()
	{
		return "LAllocV( height=" + height + ", refY=" + refY + " )";
	}
}
