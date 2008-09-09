//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Metrics;

public class VMetricsTypeset extends VMetrics
{
	public double ascent, descent;
	
	
	
	public VMetricsTypeset()
	{
		super();
		ascent = descent = 0.0;
	}
	
	public VMetricsTypeset(double ascent, double descent, double vspacing)
	{
		super( ascent + descent, vspacing );
		this.ascent = ascent;
		this.descent = descent;
	}


	public VMetricsTypeset scaled(double scale)
	{
		return new VMetricsTypeset( ascent * scale, descent * scale, vspacing * scale );
	}
	
	public VMetricsTypeset minSpacing(double spacing)
	{
		if ( spacing > vspacing )
		{
			return new VMetricsTypeset( ascent, descent, spacing );
		}
		else
		{
			return this;
		}
	}
	
	public VMetrics offsetLength(double deltaLength)
	{
		return new VMetricsTypeset( ascent + deltaLength *0.5, descent + deltaLength * 0.5, vspacing );
	}
	
	public VMetricsTypeset withHeight(double height)
	{
		double deltaHeight = height - this.height;
		return new VMetricsTypeset( ascent + deltaHeight * 0.5, descent + deltaHeight * 0.5, vspacing );
	}

	public VMetrics border(double topMargin, double bottomMargin)
	{
		return new VMetricsTypeset( ascent + topMargin, descent + bottomMargin, Math.max( vspacing - bottomMargin, 0.0 ) );
	}
	

	
	public boolean isTypeset()
	{
		return true;
	}
	

	public String toString()
	{
		return "VMetricsTypeset( ascent=" + String.valueOf( ascent ) + ", descent=" + String.valueOf( descent ) + ", height=" + String.valueOf( height ) + ", vspacing=" + String.valueOf( vspacing ) + " )";
	}
}
