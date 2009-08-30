//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;


public class LAllocV
{
	protected double ascent, descent;
	protected boolean bHasBaseline;

	
	public LAllocV(double height)
	{
		this.ascent = height * 0.5;
		this.descent = height * 0.5;
		bHasBaseline = false;
	}
	
	public LAllocV(double ascent, double descent)
	{
		this.ascent = ascent;
		this.descent = descent;
		bHasBaseline = true;
	}
	
	public LAllocV(double ascent, double descent, boolean bHasBaseline)
	{
		this.ascent = ascent;
		this.descent = descent;
		this.bHasBaseline = bHasBaseline;
	}
	
	
	public double getAscent()
	{
		return ascent;
	}
	
	public double getDescent()
	{
		return descent;
	}
	
	public double getHeight()
	{
		return ascent + descent;
	}
	
	public boolean hasBaseline()
	{
		return bHasBaseline;
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
			
			return ascent == v.ascent  &&  descent == v.descent  &&  bHasBaseline == v.bHasBaseline;
		}
		else
		{
			return false;
		}
	}
	
	
	public String toString()
	{
		return "LAllocV( ascent=" + ascent + ", descent=" + descent + ", bHasBaseline=" + bHasBaseline + ")";
	}
}
