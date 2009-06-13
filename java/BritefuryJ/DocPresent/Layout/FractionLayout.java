//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;


public class FractionLayout
{
	private static double BARHEIGHT = 1.5;
	
	
	public static double getBarHeight()
	{
		return BARHEIGHT;
	}
	
	
	public static void computeRequisitionX(LBox box, LBox numerator, LBox bar, LBox denominator, double hPadding, double vSpacing, double baselinePos)
	{
		double minWidth = 0.0, prefWidth = 0.0;
		if ( numerator != null )
		{
			minWidth = Math.max( minWidth, numerator.minWidth );
			prefWidth = Math.max( prefWidth, numerator.prefWidth );
		}
		if ( denominator != null )
		{
			minWidth = Math.max( minWidth, denominator.minWidth );
			prefWidth = Math.max( prefWidth, denominator.prefWidth );
		}

		double padding = hPadding * 2.0;
		box.setRequisitionX( minWidth + padding, prefWidth + padding, 0.0, 0.0 );
	}

	public static void computeRequisitionY(LBox box, LBox numerator, LBox bar, LBox denominator, double hPadding, double vSpacing, double baselineOffset)
	{
		double numHeight = numerator != null  ?  numerator.getReqHeight()  :  0.0;
		double numSpacing = numerator != null  ?  numerator.getReqVSpacing()  :  0.0;
		double denomHeight = denominator != null  ?  denominator.getReqHeight()  :  0.0;
		double denomSpacing = denominator != null  ?  denominator.getReqVSpacing()  :  0.0;
		
		
		double halfBarHeight = bar != null  ?  BARHEIGHT * 0.5  :  0.0;
		double ascent = numHeight + Math.max( numSpacing, vSpacing ) + halfBarHeight  +  baselineOffset;
		double descent = halfBarHeight + vSpacing + denomHeight  -  baselineOffset;
		
		box.setRequisitionY( ascent, descent, denomSpacing );
	}




	public static void allocateX(LBox box, LBox numerator, LBox bar, LBox denominator, double hPadding, double vSpacing, double baselineOffset)
	{
		double width = Math.min( Math.max( box.allocationX, box.minWidth ), box.prefWidth );
		double childrenAlloc = width - hPadding * 2.0;
		
		
		double numDenomAlloc = 0.0;
		if ( numerator != null )
		{
			double childWidth = Math.min( Math.max( childrenAlloc, numerator.minWidth ), numerator.prefWidth );
			numDenomAlloc = Math.max( numDenomAlloc, childWidth );
			double childPos = Math.max( hPadding  +  ( childrenAlloc - childWidth ) * 0.5, 0.0 );
			box.allocateChildX( numerator, childPos, childWidth );
		}
		
		if ( denominator != null )
		{
			double childWidth = Math.min( Math.max( childrenAlloc, denominator.minWidth ), denominator.prefWidth );
			numDenomAlloc = Math.max( numDenomAlloc, childWidth );
			double childPos = Math.max( hPadding  +  ( childrenAlloc - childWidth ) * 0.5, 0.0 );
			box.allocateChildX( denominator, childPos, childWidth );
		}

		if ( bar != null )
		{
			box.allocateChildX( bar, 0.0, width );
		}
		
	}

	public static void allocateY(LBox box, LBox numerator, LBox bar, LBox denominator, double hPadding, double vSpacing, double baselineOffset)
	{
		double y = 0.0;
		
		if ( numerator != null )
		{
			double childHeight = numerator.getReqHeight();
			box.allocateChildY( numerator, y, childHeight );
			
			y += childHeight  +  Math.max( numerator.getReqVSpacing(), vSpacing );
		}
		else
		{
			y += vSpacing;
		}
		
		if ( bar != null )
		{
			double childHeight = BARHEIGHT;
			box.allocateChildY( bar, y, childHeight );
			
			y += childHeight  +  vSpacing;
		}
		else
		{
			y += vSpacing;
		}
		
		if ( denominator != null )
		{
			double childHeight = denominator.getReqHeight();
			box.allocateChildY( denominator, y, childHeight );
		}
	}
}
