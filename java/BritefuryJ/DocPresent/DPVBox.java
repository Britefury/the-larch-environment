package BritefuryJ.DocPresent;

import java.awt.Color;
import java.lang.Math;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.Metrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.Metrics.VMetricsTypeset;
import BritefuryJ.Math.Point2;




public class DPVBox extends DPAbstractBox
{
	public enum Alignment { LEFT, CENTRE, RIGHT, EXPAND };
	public enum Typesetting { NONE, ALIGN_WITH_TOP, ALIGN_WITH_BOTTOM };
	
	
	public static class InvalidTypesettingException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	Typesetting typesetting;
	Alignment alignment;
	
	
	
	
	
	public DPVBox()
	{
		this( Typesetting.NONE, Alignment.CENTRE, 0.0, false, 0.0, null );
	}
	
	public DPVBox(Typesetting typesetting, Alignment alignment, double spacing, boolean bExpand, double padding)
	{
		this( typesetting, alignment, spacing, bExpand, padding, null );
	}
	
	public DPVBox(Typesetting typesetting, Alignment alignment, double spacing, boolean bExpand, double padding, Color backgroundColour)
	{
		super( spacing, bExpand, padding, backgroundColour );
		
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

	

	public void append(DPWidget child, boolean bExpand, double padding)
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
	

	
	private VMetrics computeVMetricsTypesetting(VMetrics[] childVMetrics, double height, double vspacing)
	{
		VMetrics topMetrics = childVMetrics[0], bottomMetrics = childVMetrics[childVMetrics.length-1];

		if ( typesetting == Typesetting.NONE )
		{
			return new VMetrics( height, vspacing );
		}
		else
		{
			// Need the metrics for the top and bottom entries
			VMetricsTypeset topTSMetrics = null, bottomTSMetrics = null;
			
			if ( topMetrics.isTypeset() )
			{
				topTSMetrics = (VMetricsTypeset)topMetrics;
			}

			if ( bottomMetrics.isTypeset() )
			{
				bottomTSMetrics = (VMetricsTypeset)bottomMetrics;
			}

			if ( typesetting == Typesetting.ALIGN_WITH_TOP )
			{
				if ( topTSMetrics != null )
				{
					return new VMetricsTypeset( topTSMetrics.ascent, height - topTSMetrics.ascent, vspacing );
				}
				else
				{
					return new VMetricsTypeset( topMetrics.height, height - topMetrics.height, vspacing );
				}
			}
			else if ( typesetting == Typesetting.ALIGN_WITH_BOTTOM )
			{
				if ( topTSMetrics != null )
				{
					return new VMetricsTypeset( height - bottomTSMetrics.descent, bottomTSMetrics.descent, vspacing );
				}
				else
				{
					return new VMetricsTypeset( height, 0.0, vspacing );
				}
			}
		}
		
		throw new InvalidTypesettingException();
	}


	
	private VMetrics combineVMetrics(VMetrics[] childVMetrics)
	{
		if ( childVMetrics.length == 0 )
		{
			return new VMetrics();
		}
		else
		{
			// Accumulate the height required for all the children
			double height = 0.0;
			double y = 0.0;
			for (int i = 0; i < childVMetrics.length; i++)
			{
				VMetrics chm = childVMetrics[i];
				
				if ( i != childVMetrics.length - 1)
				{
					chm = chm.minSpacing( spacing );
				}
				
				height = y + chm.height  +  getChildPadding( i ) * 2.0;
				y = height + chm.vspacing;
			}
			
			//return computeVMetricsTypesetting( childVMetrics, height, y - height );
			VMetrics vm = computeVMetricsTypesetting( childVMetrics, height, y - height );
			return vm;
		}
	}

	

	
	
	protected HMetrics computeMinimumHMetrics()
	{
		return HMetrics.max( getChildrenRefreshedMinimumHMetrics() );
	}

	protected HMetrics computePreferredHMetrics()
	{
		return HMetrics.max( getChildrenRefreshedPreferredHMetrics() );
	}

	
	protected VMetrics computeMinimumVMetrics()
	{
		return combineVMetrics( getChildrenRefreshedMinimumVMetrics() );
	}

	protected VMetrics computePreferredVMetrics()
	{
		return combineVMetrics( getChildrenRefreshedPreferredVMetrics() );
	}


	

	
	
	protected void allocateContentsX(double allocation)
	{
		super.allocateContentsX( allocation );

		for (ChildEntry baseEntry: childEntries)
		{
			BoxChildEntry entry = (BoxChildEntry)baseEntry;
			double childWidth = Math.min( entry.child.prefH.width, allocation );
			if ( alignment == Alignment.LEFT )
			{
				allocateChildX( entry.child, 0.0, childWidth );
			}
			else if ( alignment == Alignment.CENTRE )
			{
				allocateChildX( entry.child, ( allocation - childWidth )  *  0.5, childWidth );
			}
			else if ( alignment == Alignment.RIGHT )
			{
				allocateChildX( entry.child, allocation - childWidth, childWidth );
			}
			else if ( alignment == Alignment.EXPAND )
			{
				allocateChildX( entry.child, 0.0, allocation );
			}
		}
	}

	protected void allocateContentsY(double allocation)
	{
		super.allocateContentsY( allocation );
		
		Metrics[] allocated = VMetrics.allocateSpacePacked( getChildrenMinimumVMetrics(), getChildrenPreferredVMetrics(), getChildrenPackFlags(), allocation );
		
		double height = 0.0;
		double y = 0.0;
		for (int i = 0; i < allocated.length; i++)
		{
			VMetrics chm = (VMetrics)allocated[i];
			
			if ( i != allocated.length - 1)
			{
				chm = chm.minSpacing( spacing );
			}
			
			double childPadding = getChildPadding( i );
			double childY = y + childPadding;
			
			allocateChildY( childEntries.get( i ).child, childY, chm.height );
			
			height = y + chm.height + childPadding * 2.0;
			y = height + chm.vspacing;
		}
	}
}
