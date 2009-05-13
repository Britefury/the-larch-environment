//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Metrics;

public class HMetrics extends Metrics
{
	public double width, hspacing;
	
	
	
	public HMetrics()
	{
		width = 0.0;
		hspacing = 0.0;
	}
	
	public HMetrics(double width)
	{
		this.width = width;
		this.hspacing = 0.0;
	}
	
	public HMetrics(double width, double hspacing)
	{
		this.width = width;
		this.hspacing = hspacing;
	}
	
	
	public double getLength()
	{
		return width;
	}
	
	public double getTotalLength()
	{
		return width + hspacing;
	}
	
	public HMetrics scaled(double scale)
	{
		if ( scale == 1.0 )
		{
			return this;
		}
		else
		{
			return new HMetrics( width * scale, hspacing * scale );
		}
	}
	
	
	public HMetrics minSpacing(double spacing)
	{
		if ( spacing > hspacing )
		{
			return new HMetrics( width, spacing );
		}
		else
		{
			return this;
		}
	}
	
	public HMetrics pad(double padding)
	{
		if ( padding != 0.0 )
		{
			return new HMetrics( width + padding  * 2.0, hspacing );
		}
		else
		{
			return this;
		}
	}
	
	public HMetrics offsetLength(double deltaLength)
	{
		return new HMetrics( width + deltaLength, hspacing );
	}
	
	public HMetrics withWidth(double width)
	{
		return new HMetrics( width, hspacing );
	}
	
	
	public HMetrics border(double leftMargin, double rightMargin)
	{
		return new HMetrics( width + leftMargin + rightMargin, Math.max( hspacing - rightMargin, 0.0 ) );
	}
	
	
	
	public HMetrics add(HMetrics x)
	{
		return new HMetrics( width + hspacing + x.width, x.hspacing );
	}
	
	public static HMetrics max(HMetrics a, HMetrics b)
	{
		if ( a.width >= b.width  &&  a.hspacing >= b.hspacing )
		{
			return a;
		}
		else if ( b.width >= a.width  &&  b.hspacing >= a.hspacing )
		{
			return b;
		}
		else
		{
			double width = Math.max( a.width, b.width );
			double advance = Math.max( a.width + a.hspacing, b.width + b.hspacing );
			return new HMetrics( width, advance - width );
		}
	}
	
	
	public static HMetrics max(HMetrics[] xs)
	{
		double width = 0.0, advance = 0.0;
		for (int i = 0; i < xs.length; i++)
		{
			HMetrics x = xs[i];
			double xAdvance = x.width + x.hspacing;
			width = Math.max( width, x.width );
			advance = Math.max( advance, xAdvance );
		}
		
		return new HMetrics( width, advance - width );
	}
	
	
	public static HMetrics lerp(HMetrics a, HMetrics b, double t)
	{
		double width = a.width  +  ( b.width - a.width ) * t;
		double advance = a.width + a.hspacing  +  ( b.width + b.hspacing - ( a.width + a.hspacing ) ) * t;
		return new HMetrics( width, advance - width );
	}
	
	
	
	
	
	
	public String toString()
	{
		return "HMetrics( width=" + String.valueOf( width ) + ", hspacing=" + String.valueOf( hspacing ) + " )";
	}
}
