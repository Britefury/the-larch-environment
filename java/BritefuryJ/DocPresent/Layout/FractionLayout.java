//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;


public class FractionLayout
{
	public static void computeRequisitionX(LReqBoxInterface box, LReqBoxInterface numerator, LReqBoxInterface bar, LReqBoxInterface denominator, double hPadding, double vSpacing, double baselinePos)
	{
		double minWidth = 0.0, prefWidth = 0.0;
		if ( numerator != null )
		{
			minWidth = Math.max( minWidth, numerator.getReqMinWidth() );
			prefWidth = Math.max( prefWidth, numerator.getReqPrefWidth() );
		}
		if ( denominator != null )
		{
			minWidth = Math.max( minWidth, denominator.getReqMinWidth() );
			prefWidth = Math.max( prefWidth, denominator.getReqPrefWidth() );
		}

		double padding = hPadding * 2.0;
		minWidth += padding;
		prefWidth += padding;
		
		box.setRequisitionX( minWidth, prefWidth, minWidth, prefWidth );
	}

	public static void computeRequisitionY(LReqBoxInterface box, LReqBoxInterface numerator, LReqBoxInterface bar, LReqBoxInterface denominator, double hPadding, double vSpacing, double baselineOffset)
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




	public static void allocateX(LReqBoxInterface box, LReqBoxInterface numerator, LReqBoxInterface bar, LReqBoxInterface denominator,
			LAllocBoxInterface boxAlloc, LAllocBoxInterface numeratorAlloc, LAllocBoxInterface barAlloc, LAllocBoxInterface denominatorAlloc, double hPadding, double vSpacing, double baselineOffset)
	{
		double boxAllocAllocationX = boxAlloc.getAllocWidth();
		double allocX = Math.min( boxAllocAllocationX, box.getReqPrefWidth() );
		double childrenAlloc = allocX - hPadding * 2.0;
		
		
		if ( numerator != null )
		{
			double childWidth = Math.min( childrenAlloc, numerator.getReqPrefWidth() );
			double childPos = Math.max( hPadding  +  ( childrenAlloc - childWidth ) * 0.5, 0.0 );
			LAllocHelper.allocateChildX( numeratorAlloc, childPos, childWidth, Math.max( childWidth, numerator.getReqMinWidth() ) );
		}
		
		if ( denominator != null )
		{
			double childWidth = Math.min( childrenAlloc, denominator.getReqPrefWidth() );
			double childPos = Math.max( hPadding  +  ( childrenAlloc - childWidth ) * 0.5, 0.0 );
			LAllocHelper.allocateChildX( denominatorAlloc, childPos, childWidth, Math.max( childWidth, denominator.getReqMinWidth() ) );
		}

		if ( bar != null )
		{
			LAllocHelper.allocateChildX( barAlloc, 0.0, allocX, Math.max( allocX, bar.getReqMinWidth() ) );
		}
	}

	public static void allocateY(LReqBoxInterface box, LReqBoxInterface numerator, LReqBoxInterface bar, LReqBoxInterface denominator,
			LAllocBoxInterface boxAlloc, LAllocBoxInterface numeratorAlloc, LAllocBoxInterface barAlloc, LAllocBoxInterface denominatorAlloc, double hPadding, double vSpacing, double baselineOffset)
	{
		double y = 0.0;
		
		if ( numerator != null )
		{
			LAllocHelper.allocateChildYAsRequisition( numeratorAlloc, numerator, y );
			
			y += numerator.getReqHeight()  +  Math.max( numerator.getReqVSpacing(), vSpacing );
		}
		else
		{
			y += vSpacing;
		}
		
		if ( bar != null )
		{
			LAllocHelper.allocateChildYAsRequisition( barAlloc, bar, y );
			
			y += bar.getReqHeight()  +  vSpacing;
		}
		else
		{
			y += vSpacing;
		}
		
		if ( denominator != null )
		{
			LAllocHelper.allocateChildYAsRequisition( denominatorAlloc, denominator, y );
		}
	}
}
