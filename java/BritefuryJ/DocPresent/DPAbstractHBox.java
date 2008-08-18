package Britefury.DocPresent;

import java.awt.Color;

import Britefury.Math.Point2;



abstract public class DPAbstractHBox extends DPAbstractBox
{
	public DPAbstractHBox()
	{
		this( 0.0, false, false, false, 0.0, null );
	}
	
	public DPAbstractHBox(double spacing, boolean bExpand, boolean bFill, boolean bShrink, double padding)
	{
		this( spacing, bExpand, bFill, bShrink, padding, null );
	}
	
	public DPAbstractHBox(double spacing, boolean bExpand, boolean bFill, boolean bShrink, double padding, Color backgroundColour)
	{
		super( spacing, bExpand, bFill, bShrink, padding, backgroundColour );
	}
	
	
	
	public int getInsertIndex(Point2 localPos)
	{
		//Return the index at which an item could be inserted.
		// localPos is checked against the contents of the box in order to determine the insert index
		
		if ( size() == 0 )
		{
			return 0;
		}
	
		double pos = localPos.x;
		
		double[] midPoints = new double[childEntries.size()];
		
		for (int i = 0; i < midPoints.length; i++)
		{
			ChildEntry entry = childEntries.get( i );
			midPoints[i] = entry.pos.x  +  entry.size.x * 0.5;
		}
		
		if ( pos < midPoints[0] )
		{
			return size();
		}
		else if ( pos > midPoints[midPoints.length-1] )
		{
			return 0;
		}
		else
		{
			for (int i = 0; i < midPoints.length-1; i++)
			{
				double lower = midPoints[i];
				double upper = midPoints[i+1];
				if ( pos >= lower  &&  pos <= upper )
				{
					return i + 1;
				}
			}
			
			throw new CouldNotFindInsertionPointException();
		}
	}
	
	
	
	
	protected HMetrics computeRequiredHMetrics()
	{
		if ( childEntries.isEmpty() )
		{
			childrenHMetrics = new HMetrics();
		}
		else
		{
			// Get the hmetrics for the children
			HMetrics[] childHMetrics = new HMetrics[childEntries.size()];
			for (int i = 0; i < childHMetrics.length; i++)
			{
				childHMetrics[i] = childEntries.get( i ).child.getRequiredHMetrics();
			}
			
			// Accumulate the width required for all the children
			double width = 0.0, advance = 0.0;
			for (int i = 0; i < childHMetrics.length; i++)
			{
				HMetrics chm = childHMetrics[i];
				BoxChildEntry entry = (BoxChildEntry)childEntries.get( i );
				
				// The spacing for the box is @spacing if this is NOT the last child; else 0.0
				double boxSpacing = ( i == childHMetrics.length - 1 )  ?  0.0  :  spacing;
				// Compute the spacing inherent in the advance field of the child h-metrics
				double advanceSpacing = chm.advance - chm.width;
				// Compute the spacing for this child; the greater of the advance spacing and the box spacing
				double childSpacing = advanceSpacing > boxSpacing  ?  advanceSpacing : boxSpacing;
				
				width = advance + chm.width + entry.padding * 2.0;
				advance = width + childSpacing;
			}
			
			childrenHMetrics = new HMetrics( width, advance );
		}
		
		return childrenHMetrics;
	}

	

	protected HMetrics onAllocateX(double allocation)
	{
		double expandPerChild = 0.0, shrinkPerChild = 0.0;
		if ( allocation > childrenHMetrics.width )
		{
			// More space than is required
			if ( numExpand > 0 )
			{
				double totalExpand = allocation - childrenHMetrics.width;
				expandPerChild = totalExpand / (double)numExpand;
			}
		}
		else if ( allocation < childrenHMetrics.width )
		{
			// Insufficient space; shrink
			if ( numShrink > 0 )
			{
				double totalShrink = childrenHMetrics.width - allocation;
				shrinkPerChild = totalShrink / (double)numShrink;
			}
		}
		
		
		double x = 0.0, width = 0.0;
		for (ChildEntry baseEntry: childEntries)
		{
			BoxChildEntry entry = (BoxChildEntry)baseEntry;
			
			double childBox = entry.child.hmetrics.width;
			double childAlloc = childBox;
			double childX = x + entry.padding;
			
			if ( entry.bExpand )
			{
				childBox += expandPerChild;
				if ( entry.bFill )
				{
					childAlloc += expandPerChild;
				}
				else
				{
					childX += expandPerChild * 0.5;
				}
			}
			if ( entry.bShrink )
			{
				childBox -= shrinkPerChild;
				childAlloc -= shrinkPerChild;
			}
			

			HMetrics childAllocMetrics = allocateChildX( entry.child, childX, childAlloc );

			// Compute the spacing inherent in the advance field of the child h-metrics
			double advanceSpacing = childAllocMetrics.advance - childAllocMetrics.width;
			// Compute the spacing for this child; the greater of the advance spacing and the box spacing
			double childSpacing = advanceSpacing > spacing  ?  advanceSpacing : spacing;

			width = x + childAllocMetrics.width + entry.padding * 2.0;
			x = width + childSpacing;
		}
		
		return new HMetrics( width, x );
	}
}
