package BritefuryJ.DocPresent;

import java.awt.Color;
import java.lang.Math;
import java.util.List;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.Metrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.Metrics.VMetricsTypeset;
import BritefuryJ.Math.Point2;



public class DPHBox extends DPAbstractBox
{
	public enum Alignment { TOP, CENTRE, BOTTOM, EXPAND, BASELINES };
	
	
	Alignment alignment;
	
	
	
	
	
	public DPHBox()
	{
		this( Alignment.CENTRE, 0.0, false, 0.0, null );
	}
	
	public DPHBox(Alignment alignment, double spacing, boolean bExpand, double padding)
	{
		this( alignment, spacing, bExpand, padding, null );
	}
	
	public DPHBox(Alignment alignment, double spacing, boolean bExpand, double padding, Color backgroundColour)
	{
		super( spacing, bExpand, padding, backgroundColour );
		
		this.alignment = alignment;
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

	

	public void append(DPWidget child,  boolean bExpand, double padding)
	{
		appendChildEntry( new BoxChildEntry( child, bExpand, padding ) );
	}

	
	public void insert(int index, DPWidget child, boolean bExpand, double padding)
	{
		insertChildEntry( index, new BoxChildEntry( child, bExpand, padding ) );
	}
	
	
	
	protected BoxChildEntry createChildEntryForChild(DPWidget child)
	{
		return new BoxChildEntry( child, bExpand, padding );
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

	
	
	private HMetrics combineHMetricsHorizontally(HMetrics[] childHMetrics)
	{
		if ( childHMetrics.length == 0 )
		{
			return new HMetrics();
		}
		else
		{
			// Accumulate the width required for all the children
			double width = 0.0;
			double x = 0.0;
			for (int i = 0; i < childHMetrics.length; i++)
			{
				HMetrics chm = childHMetrics[i];
				
				if ( i != childHMetrics.length - 1 )
				{
					chm = chm.minSpacing( spacing );
				}
				
				width = x + chm.width  +  getChildPadding( i ) * 2.0;
				x = width + chm.hspacing;
			}
			
			return new HMetrics( width, x - width );
		}
	}
	

	private VMetrics combineVMetricsHorizontally(VMetrics[] childVMetrics)
	{
		if ( childVMetrics.length == 0 )
		{
			return new VMetrics();
		}
		else
		{
			if ( alignment == Alignment.BASELINES )
			{
				double ascent = 0.0, descent = 0.0;
				double descentAndSpacing = 0.0;
				for (int i = 0; i < childVMetrics.length; i++)
				{
					VMetrics chm = childVMetrics[i];
					double chAscent, chDescent;
					if ( chm.isTypeset() )
					{
						VMetricsTypeset tchm = (VMetricsTypeset)chm;
						chAscent = tchm.ascent;
						chDescent = tchm.descent;
					}
					else
					{
						chAscent = chm.height * 0.5  -  NON_TYPESET_CHILD_BASELINE_OFFSET;
						chDescent = chm.height * 0.5  +  NON_TYPESET_CHILD_BASELINE_OFFSET;
					}
					ascent = Math.max( ascent, chAscent );
					descent = Math.max( descent, chDescent );
					double chDescentAndSpacing = chDescent + chm.vspacing;
					descentAndSpacing = Math.max( descentAndSpacing, chDescentAndSpacing );
				}
				
				return new VMetricsTypeset( ascent, descent, descentAndSpacing - descent );
			}
			else
			{
				double height = 0.0;
				double advance = 0.0;
				for (int i = 0; i < childVMetrics.length; i++)
				{
					VMetrics chm = childVMetrics[i];
					double chAdvance = chm.height + chm.vspacing;
					height = Math.max( height, chm.height );
					advance = Math.max( advance, chAdvance );
				}
				
				
				return new VMetrics( height, advance - height );
			}
		}
	}

	
	
	protected HMetrics computeMinimumHMetrics()
	{
		return combineHMetricsHorizontally( getChildrenRefreshedMinimumHMetrics() );
	}

	protected HMetrics computePreferredHMetrics()
	{
		return combineHMetricsHorizontally( getChildrenRefreshedPreferredHMetrics() );
	}

	
	protected VMetrics computeMinimumVMetrics()
	{
		return combineVMetricsHorizontally( getChildrenRefreshedMinimumVMetrics() );
	}

	protected VMetrics computePreferredVMetrics()
	{
		return combineVMetricsHorizontally( getChildrenRefreshedPreferredVMetrics() );
	}


	

	protected void allocateContentsX(double allocation)
	{
		super.allocateContentsX( allocation );
		
		Metrics[] allocated = VMetrics.allocateSpacePacked( getChildrenMinimumHMetrics(), getChildrenPreferredHMetrics(), getChildrenPackFlags(), allocation );
		
		double width = 0.0;
		double x = 0.0;
		for (int i = 0; i < allocated.length; i++)
		{
			HMetrics chm = (HMetrics)allocated[i];
			
			if ( i != allocated.length - 1)
			{
				chm = chm.minSpacing( spacing );
			}

			double childPadding = getChildPadding( i );
			double childX = x + childPadding;
			
			allocateChildX( childEntries.get( i ).child, childX, chm.width );

			width = x + chm.width + childPadding * 2.0;
			x = width + chm.hspacing;
		}
	}
	
	
	
	protected void allocateContentsY(double allocation)
	{
		super.allocateContentsY( allocation );
		
		if ( alignment == Alignment.BASELINES )
		{
			VMetricsTypeset vmt = (VMetricsTypeset)prefV;
			
			double delta = allocation - vmt.height;
			double y = vmt.ascent + delta * 0.5;
			
			for (ChildEntry entry: childEntries)
			{
				BoxChildEntry boxEntry = (BoxChildEntry)entry;
				DPWidget child = boxEntry.child;
				double chAscent;
				VMetrics chm = child.prefV;
				if ( chm.isTypeset() )
				{
					VMetricsTypeset tchm = (VMetricsTypeset)chm;
					chAscent = tchm.ascent;
				}
				else
				{
					chAscent = chm.height * 0.5  -  NON_TYPESET_CHILD_BASELINE_OFFSET;
				}

				double childY = Math.max( y - chAscent, 0.0 );
				double childHeight = Math.min( chm.height, allocation );
				allocateChildY( child, childY, childHeight );
			}
		}
		else
		{
			for (ChildEntry entry: childEntries)
			{
				DPWidget child = entry.child;
				double childHeight = Math.min( child.prefV.height, allocation );
				if ( alignment == Alignment.TOP )
				{
					allocateChildY( child, 0.0, childHeight );
				}
				else if ( alignment == Alignment.CENTRE )
				{
					allocateChildY( child, ( allocation - childHeight ) * 0.5, childHeight );
				}
				else if ( alignment == Alignment.BOTTOM )
				{
					allocateChildY( child, allocation - childHeight, childHeight );
				}
				else if ( alignment == Alignment.EXPAND )
				{
					allocateChildY( child, 0.0, allocation );
				}
			}
		}
	}
	
	
	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return getChildren();
	}
}
