//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Metrics;

public class VMetrics extends Metrics
{
	public double height, vspacing;
	
	
	
	public VMetrics()
	{
		height = vspacing = 0.0;
	}
	
	public VMetrics(double height, double vspacing)
	{
		this.height = height;
		this.vspacing = vspacing;
	}

	
	public double getLength()
	{
		return height;
	}
	
	public double getTotalLength()
	{
		return height + vspacing;
	}
	
	public VMetrics scaled(double scale)
	{
		return new VMetrics( height * scale, vspacing * scale );
	}
	
	public VMetrics minSpacing(double spacing)
	{
		if ( spacing > vspacing )
		{
			return new VMetrics( height, spacing );
		}
		else
		{
			return this;
		}
	}
	
	public VMetrics offsetLength(double deltaLength)
	{
		return new VMetrics( height + deltaLength, vspacing );
	}
	
	public VMetrics withHeight(double height)
	{
		return new VMetrics( height, vspacing );
	}
	
	public VMetrics border(double topMargin, double bottomMargin)
	{
		return new VMetrics( height + topMargin + bottomMargin, Math.max( vspacing - bottomMargin, 0.0 ) );
	}
	
	
	
	public boolean isTypeset()
	{
		return false;
	}
	
	
	public String toString()
	{
		return "VMetrics( height=" + String.valueOf( height ) + ", vspacing=" + String.valueOf( vspacing ) + " )";
	}
}
