package Britefury.DocPresent;

import java.awt.Color;

import Britefury.Math.Point2;




public class DPVBox extends DPAbstractBox
{
	public enum Alignment { LEFT, CENTRE, RIGHT, EXPAND };
	public enum Typesetting { NONE, ALIGN_WITH_TOP, ALIGN_WITH_BOTTOM, IN_TO_TOP_OUT_FROM_BOTTOM };
	
	
	protected static class VBoxChildEntry extends DPAbstractBox.BoxChildEntry
	{
		public Alignment alignment;
		
		public VBoxChildEntry(DPWidget child, Alignment alignment, boolean bExpand, boolean bFill, boolean bShrink, double padding)
		{
			super( child, bExpand, bFill, bShrink, padding );
			
			this.alignment = alignment;
		}
	}
	
	
	Typesetting typesetting;
	Alignment alignment;
	
	
	
	
	
	public DPVBox()
	{
		this( Typesetting.NONE, Alignment.CENTRE, 0.0, false, false, false, 0.0, null );
	}
	
	public DPVBox(Typesetting typesetting, Alignment alignment, double spacing, boolean bExpand, boolean bFill, boolean bShrink, double padding)
	{
		this( typesetting, alignment, spacing, bExpand, bFill, bShrink, padding, null );
	}
	
	public DPVBox(Typesetting typesetting, Alignment alignment, double spacing, boolean bExpand, boolean bFill, boolean bShrink, double padding, Color backgroundColour)
	{
		super( spacing, bExpand, bFill, bShrink, padding, backgroundColour );
		
		this.typesetting = typesetting;
		this.alignment = alignment;
	}
	
	
	
	
	public Typesetting getTypesetting()
	{
		return typesetting;
	}

	public void setAlignment(Typesetting typesetting)
	{
		this.typesetting = typesetting;
		queueResize();
	}

	
	public Alignment getAlignment()
	{
		return alignment;
	}

	public void setAlignment(Alignment alignment)
	{
		this.alignment = alignment;
		queueResize();
	}

	

	public void append(DPWidget child, Alignment alignment, boolean bExpand, boolean bFill, boolean bShrink, double padding)
	{
		appendChildEntry( new VBoxChildEntry( child, alignment, bExpand, bFill, bShrink, padding ) );
	}

	
	public void insert(int index, DPWidget child, Alignment alignment, boolean bExpand, boolean bFill, boolean bShrink, double padding)
	{
		insertChildEntry( index, new VBoxChildEntry( child, alignment, bExpand, bFill, bShrink, padding ) );
	}
	
	
	
	protected VBoxChildEntry createChildEntryForChild(DPWidget child)
	{
		return new VBoxChildEntry( child, alignment, bExpand, bFill, bShrink, padding );
	}
	
	
	
	public int getInsertIndex(Point2 localPos)
	{
		//Return the index at which an item could be inserted.
		// localPos is checked against the contents of the box in order to determine the insert index
		
		if ( size() == 0 )
		{
			return 0;
		}
	
		double pos = localPos.y;
		
		double[] midPoints = new double[childEntries.size()];
		
		for (int i = 0; i < midPoints.length; i++)
		{
			ChildEntry entry = childEntries.get( i );
			midPoints[i] = entry.pos.y  +  entry.size.y * 0.5;
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
		if ( childEntries.size() == 0 )
		{
			childrenHMetrics = new HMetrics();
		}
		else
		{
			HMetrics[] childHMetrics = new HMetrics[childEntries.size()];
			for (int i = 0; i < childHMetrics.length; i++)
			{
				childHMetrics[i] = childEntries.get( i ).child.getRequiredHMetrics();
			}
			
			HMetricsTypeset typesetHMetrics = new HMetricsTypeset();
			boolean bTypeset = false;
			for (int i = 0; i < childHMetrics.length; i++)
			{
				HMetrics chm = childHMetrics[i];
				typesetHMetrics.width = typesetHMetrics.width > chm.width  ?  typesetHMetrics.width : chm.width;
				if ( chm instanceof HMetricsTypeset)
				{
					HMetricsTypeset chmt = (HMetricsTypeset)chm;
					typesetHMetrics.advance = typesetHMetrics.advance > chmt.advance  ?  typesetHMetrics.advance : chmt.advance;
					bTypeset = true;
				}
			}
			
			if ( bTypeset )
			{
				typesetHMetrics.advance = typesetHMetrics.advance > typesetHMetrics.width  ?  typesetHMetrics.advance : typesetHMetrics.width;
				childrenHMetrics = typesetHMetrics;
			}
			else
			{
				HMetrics hm = new HMetrics( typesetHMetrics.width );
				childrenHMetrics = hm;
			}
		}
		
		return childrenHMetrics;
	}



	protected VMetrics computeRequiredVMetrics()
	{
		if ( childEntries.isEmpty() )
		{
			childrenVMetrics = new VMetrics();
		}
		else
		{
			// Get the vmetrics for the children
			VMetrics[] childVMetrics = new VMetrics[childEntries.size()];
			for (int i = 0; i < childVMetrics.length; i++)
			{
				childVMetrics[i] = childEntries.get( i ).child.getRequiredVMetrics();
			}
			
			// Accumulate the height required for all the children
			double height = 0.0;
			for (int i = 0; i < childVMetrics.length; i++)
			{
				VMetrics chm = childVMetrics[i];
				VBoxChildEntry entry = (VBoxChildEntry)childEntries.get( i );
				
				// The spacing for the box is @spacing if this is NOT the last child; else 0.0
				double boxSpacing = ( i == childVMetrics.length - 1 )  ?  0.0  :  spacing;
				// The spacing to be used is either the box spacing, or the child's v-spacing, whichever is greater
				double childSpacing = boxSpacing  >  chm.vspacing  ?  boxSpacing  :  chm.vspacing;
				
				double chHeight = chm.height;
				height += ( chHeight + childSpacing + entry.padding * 2.0 );
			}
			
			VMetrics topMetrics = childVMetrics[0], bottomMetrics = childVMetrics[childVMetrics.length-1];

			// The vertical spacing to go below @this is the vspacing of the bottom child
			double vspacing = bottomMetrics.vspacing;
			
			if ( typesetting == Typesetting.NONE )
			{
				childrenVMetrics = new VMetrics( height, vspacing );
			}
			else
			{
				// Need the metrics for the top and bottom entries
				VMetricsTypeset topTSMetrics = null, bottomTSMetrics = null;
				
				if ( topMetrics  instanceof VMetricsTypeset )
				{
					topTSMetrics = (VMetricsTypeset)topMetrics;
				}

				if ( bottomMetrics  instanceof VMetricsTypeset )
				{
					bottomTSMetrics = (VMetricsTypeset)bottomMetrics;
				}

				if ( typesetting == Typesetting.ALIGN_WITH_TOP )
				{
					if ( topTSMetrics != null )
					{
						childrenVMetrics = new VMetricsTypeset( topTSMetrics.ascent, height - topTSMetrics.ascent, vspacing );
					}
					else
					{
						childrenVMetrics = new VMetricsTypeset( topMetrics.height, height - topMetrics.height, vspacing );
					}
				}
				else if ( typesetting == Typesetting.ALIGN_WITH_BOTTOM )
				{
					if ( topTSMetrics != null )
					{
						childrenVMetrics = new VMetricsTypeset( height - bottomTSMetrics.descent, bottomTSMetrics.descent, vspacing );
					}
					else
					{
						childrenVMetrics = new VMetricsTypeset( height, 0.0, vspacing );
					}
				}
				else if ( typesetting == Typesetting.IN_TO_TOP_OUT_FROM_BOTTOM )
				{
					double topAscent, bottomDescent;

					if ( topTSMetrics != null )
					{
						topAscent = topTSMetrics.ascent;
					}
					else
					{
						topAscent = topMetrics.height;
					}

					if ( bottomTSMetrics != null )
					{
						bottomDescent = bottomTSMetrics.descent;
					}
					else
					{
						bottomDescent = 0.0;
					}
					
					childrenVMetrics = new VMetricsTypeset( topAscent, height - topAscent, height - topAscent - bottomDescent, vspacing );
				}
			}
		}
		
		return childrenVMetrics;
	}

	

	protected void onAllocateX(double allocation)
	{
		for (ChildEntry baseEntry: childEntries)
		{
			VBoxChildEntry entry = (VBoxChildEntry)baseEntry;
			double childAlloc = entry.child.hmetrics.width < allocation  ?  entry.child.hmetrics.width : allocation;
			if ( entry.alignment == Alignment.LEFT )
			{
				allocateChildX( entry.child, 0.0, childAlloc );
			}
			else if ( entry.alignment == Alignment.CENTRE )
			{
				allocateChildX( entry.child, ( allocation - childAlloc )  *  0.5, childAlloc );
			}
			else if ( entry.alignment == Alignment.RIGHT )
			{
				allocateChildX( entry.child, allocation - childAlloc, childAlloc );
			}
			else if ( entry.alignment == Alignment.EXPAND )
			{
				allocateChildX( entry.child, 0.0, allocation );
			}
		}
	}

	protected void onAllocateY(double allocation)
	{
		double expandPerChild = 0.0, shrinkPerChild = 0.0;
		if ( allocation > childrenVMetrics.height )
		{
			// More space than is required
			if ( numExpand > 0 )
			{
				double totalExpand = allocation - childrenVMetrics.height;
				expandPerChild = totalExpand / (double)numExpand;
			}
		}
		else if ( allocation < childrenVMetrics.height )
		{
			// Insufficient space; shrink
			if ( numShrink > 0 )
			{
				double totalShrink = childrenVMetrics.height - allocation;
				shrinkPerChild = totalShrink / (double)numShrink;
			}
		}
		
		
		double y = 0.0;
		for (ChildEntry baseEntry: childEntries)
		{
			VBoxChildEntry entry = (VBoxChildEntry)baseEntry;
			
			double childAlloc = entry.child.vmetrics.height;
			double childHeight = childAlloc;
			double childY = y + entry.padding;
			
			if ( entry.bExpand )
			{
				childAlloc += expandPerChild;
				if ( entry.bFill )
				{
					childHeight += expandPerChild;
				}
				else
				{
					childY += expandPerChild * 0.5;
				}
			}
			if ( entry.bShrink )
			{
				childAlloc -= shrinkPerChild;
				childHeight -= shrinkPerChild;
			}
			

			// The spacing to be used is either the box spacing, or the child's v-spacing, whichever is greater
			double childSpacing = spacing  >  entry.child.vmetrics.vspacing  ?  spacing  :  entry.child.vmetrics.vspacing;

			allocateChildY( entry.child, childY, childHeight );
			y += childAlloc + childSpacing;
		}
	}
}
