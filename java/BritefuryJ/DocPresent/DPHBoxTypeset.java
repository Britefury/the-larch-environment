package BritefuryJ.DocPresent;

import java.awt.Color;
import java.lang.Math;


public class DPHBoxTypeset extends DPAbstractHBox
{
	public DPHBoxTypeset()
	{
		this( 0.0, false, false, true, 0.0, null );
	}
	
	public DPHBoxTypeset(double spacing, boolean bExpand, boolean bFill, boolean bShrink, double padding)
	{
		this( spacing, bExpand, bFill, bShrink, padding, null );
	}
	
	public DPHBoxTypeset(double spacing, boolean bExpand, boolean bFill, boolean bShrink, double padding, Color backgroundColour)
	{
		super( spacing, bExpand, bFill, bShrink, padding, backgroundColour );
	}
	
	
	
	
	public void append(DPWidget child, boolean bExpand, boolean bFill, boolean bShrink, double padding)
	{
		appendChildEntry( new BoxChildEntry( child, bExpand, bFill, bShrink, padding ) );
	}

	
	public void insert(int index, DPWidget child, boolean bExpand, boolean bFill, boolean bShrink, double padding)
	{
		insertChildEntry( index, new BoxChildEntry( child, bExpand, bFill, bShrink, padding ) );
	}
	
	
	
	protected BoxChildEntry createChildEntryForChild(DPWidget child)
	{
		return new BoxChildEntry( child, bExpand, bFill, bShrink, padding );
	}
	
	
	
	protected VMetrics computeRequiredVMetrics()
	{
		if ( childEntries.size() == 0 )
		{
			childrenVMetrics = new VMetrics();
		}
		else
		{
			VMetrics[] childVMetrics = new VMetrics[childEntries.size()];
			for (int i = 0; i < childVMetrics.length; i++)
			{
				childVMetrics[i] = childEntries.get( i ).child.getRequiredVMetrics();
			}
			
			// @baseline is the position of the baseline relative to the baseline at the start. A positive value indices that it has been moved down
			double ascent = 0.0, descent = 0.0, baseline = 0.0, descentAndVSpacing = 0.0;
			boolean bBaselineIsOffset = false;
			
			for (int i = 0; i < childVMetrics.length; i++)
			{
				VMetrics chm = childVMetrics[i];
				
				// Get the ascent and descent for the child
				double childAscent, childDescent;
				if ( chm instanceof VMetricsTypeset )
				{
					VMetricsTypeset chmt = (VMetricsTypeset)chm;
					childAscent = chmt.ascent;
					childDescent = chmt.descent;
				}
				else
				{
					double childHeight = chm.height;
					childAscent = childHeight * 0.5  -  NON_TYPESET_CHILD_BASELINE_OFFSET;
					childDescent = childHeight * 0.5  +  NON_TYPESET_CHILD_BASELINE_OFFSET;
				}
				
				// Apply any active offset
				if ( bBaselineIsOffset )
				{
					// Offsetting the baseline downards (+ve value of @baseline) reduces the ascent above the origin baseline
					childAscent -= baseline;
					childDescent += baseline;
				}
				
				// Accumulate the ascent and descent
				ascent = Math.max( ascent, childAscent );
				descent = Math.max( descent, childDescent );
				
				// Accumulate the baseline offset of the child, if it has one
				if ( chm instanceof VMetricsTypesetWithBaselineOffset )
				{
					VMetricsTypesetWithBaselineOffset chmtb = (VMetricsTypesetWithBaselineOffset)chm;
					bBaselineIsOffset = true;
					baseline += chmtb.baselineOffset;
				}

				// Handle the v-spacing
				double childDescentAndVSpacing = childDescent + chm.vspacing;
				descentAndVSpacing = Math.max( descentAndVSpacing, childDescentAndVSpacing );
			}
			
			if ( bBaselineIsOffset )
			{
				childrenVMetrics = new VMetricsTypesetWithBaselineOffset( ascent, descent, baseline, descentAndVSpacing - descent );
			}
			else
			{
				childrenVMetrics = new VMetricsTypeset( ascent, descent, descentAndVSpacing - descent );
			}
		}
		
		return childrenVMetrics;
	}



	protected VMetrics onAllocateY(double allocation)
	{
		VMetricsTypeset boxMetrics = (VMetricsTypeset)childrenVMetrics;
		// Start at the baseline of the box. This will change if baseline offsets are encountered
		double y = boxMetrics.ascent;

		for (ChildEntry baseEntry: childEntries)
		{
			BoxChildEntry entry = (BoxChildEntry)baseEntry;
			
			VMetrics chm = entry.child.vmetrics;
			
			// Get the ascent and descent for the child
			double childAscent, childDescent;
			if ( chm instanceof VMetricsTypeset )
			{
				VMetricsTypeset chmt = (VMetricsTypeset)chm;
				childAscent = chmt.ascent;
				childDescent = chmt.descent;
			}
			else
			{
				double childHeight = chm.height;
				childAscent = childHeight * 0.5  -  NON_TYPESET_CHILD_BASELINE_OFFSET;
				childDescent = childHeight * 0.5  +  NON_TYPESET_CHILD_BASELINE_OFFSET;
			}

			// Allocate the child
			allocateChildY( entry.child, y - childAscent, childAscent + childDescent );
			
			// Accumulate the baseline offset of the child, if it has one
			if ( chm instanceof VMetricsTypesetWithBaselineOffset )
			{
				VMetricsTypesetWithBaselineOffset chmtb = (VMetricsTypesetWithBaselineOffset)chm;
				y += chmtb.baselineOffset;
			}
		}
		
		return childrenVMetrics;
	}
}
