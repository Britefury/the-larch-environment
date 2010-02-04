//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;


public class FractionLayout
{
	public static void computeRequisitionX(LReqBox box, LReqBoxInterface numerator, LReqBoxInterface bar, LReqBoxInterface denominator, double hPadding, double vSpacing, double baselinePos)
	{
		double minWidth = 0.0, prefWidth = 0.0;
		if ( numerator != null )
		{
			minWidth = Math.max( minWidth, numerator.getMinWidth() );
			prefWidth = Math.max( prefWidth, numerator.getPrefWidth() );
		}
		if ( denominator != null )
		{
			minWidth = Math.max( minWidth, denominator.getMinWidth() );
			prefWidth = Math.max( prefWidth, denominator.getPrefWidth() );
		}

		double padding = hPadding * 2.0;
		minWidth += padding;
		prefWidth += padding;
		
		box.setRequisitionX( minWidth, prefWidth, minWidth, prefWidth );
	}

	public static void computeRequisitionY(LReqBox box, LReqBoxInterface numerator, LReqBoxInterface bar, LReqBoxInterface denominator, double hPadding, double vSpacing, double baselineOffset)
	{
		double y = 0.0;
		double height = 0.0, ascent = 0.0;
		double heightAndSpacing = 0.0;
		
		if ( numerator != null )
		{
			height = y + numerator.getReqHeight();
			heightAndSpacing = height + numerator.getReqVSpacing();
			y = height + Math.max( numerator.getReqVSpacing(), vSpacing );
		}
		
		if ( bar != null )
		{
			double barHeight = bar.getReqHeight();
			ascent = y + barHeight * 0.5 + baselineOffset; 
			
			height = y + barHeight;
			heightAndSpacing = height;
			y = height + vSpacing;
		}
		else
		{
			if ( numerator != null  &&  denominator != null )
			{
				ascent = y + baselineOffset - vSpacing * 0.5;
			}
			else
			{
				ascent = y + baselineOffset;
			}
		}
		
		if ( denominator != null )
		{
			height = y + denominator.getReqHeight();
			heightAndSpacing = height + denominator.getReqVSpacing();
			y = heightAndSpacing;
		}
		
		
		box.setRequisitionY( height, heightAndSpacing - height, ascent );
	}




	public static void allocateX(LReqBox box, LReqBoxInterface numerator, LReqBoxInterface bar, LReqBoxInterface denominator,
			LAllocBox boxAlloc, LAllocBoxInterface numeratorAlloc, LAllocBoxInterface barAlloc, LAllocBoxInterface denominatorAlloc, double hPadding, double vSpacing, double baselineOffset)
	{
		double width = Math.min( Math.max( boxAlloc.allocationX, box.minWidth ), box.prefWidth );
		double childrenAlloc = width - hPadding * 2.0;
		
		
		double numDenomAlloc = 0.0;
		if ( numerator != null )
		{
			double childWidth = Math.min( Math.max( childrenAlloc, numerator.getMinWidth() ), numerator.getPrefWidth() );
			numDenomAlloc = Math.max( numDenomAlloc, childWidth );
			double childPos = Math.max( hPadding  +  ( childrenAlloc - childWidth ) * 0.5, 0.0 );
			boxAlloc.allocateChildX( numeratorAlloc, childPos, childWidth );
		}
		
		if ( denominator != null )
		{
			double childWidth = Math.min( Math.max( childrenAlloc, denominator.getMinWidth() ), denominator.getPrefWidth() );
			numDenomAlloc = Math.max( numDenomAlloc, childWidth );
			double childPos = Math.max( hPadding  +  ( childrenAlloc - childWidth ) * 0.5, 0.0 );
			boxAlloc.allocateChildX( denominatorAlloc, childPos, childWidth );
		}

		if ( bar != null )
		{
			boxAlloc.allocateChildX( barAlloc, 0.0, width );
		}
	}

	public static void allocateY(LReqBox box, LReqBoxInterface numerator, LReqBoxInterface bar, LReqBoxInterface denominator,
			LAllocBox boxAlloc, LAllocBoxInterface numeratorAlloc, LAllocBoxInterface barAlloc, LAllocBoxInterface denominatorAlloc, double hPadding, double vSpacing, double baselineOffset)
	{
		double y = 0.0;
		
		if ( numerator != null )
		{
			boxAlloc.allocateChildYAsRequisition( numeratorAlloc, numerator, y );
			
			y += numerator.getReqHeight()  +  Math.max( numerator.getReqVSpacing(), vSpacing );
		}
		else
		{
			y += vSpacing;
		}
		
		if ( bar != null )
		{
			boxAlloc.allocateChildYAsRequisition( barAlloc, bar, y );
			
			y += bar.getReqHeight()  +  vSpacing;
		}
		else
		{
			y += vSpacing;
		}
		
		if ( denominator != null )
		{
			boxAlloc.allocateChildYAsRequisition( denominatorAlloc, denominator, y );
		}
	}
}
