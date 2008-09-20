//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.lang.Math;
import java.util.List;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.Metrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.Metrics.VMetricsTypeset;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.Math.Point2;



public class DPHBox extends DPAbstractBox
{
	public enum Alignment { TOP, CENTRE, BOTTOM, EXPAND, BASELINES };
	
	
	
	public DPHBox()
	{
		this( HBoxStyleSheet.defaultStyleSheet );
	}
	
	public DPHBox(HBoxStyleSheet syleSheet)
	{
		super( syleSheet );
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
		return new BoxChildEntry( child, getExpand(), getPadding() );
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
			double spacing = getSpacing();
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
			Alignment alignment = getAlignment();
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
		
		double spacing = getSpacing();
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
		
		Alignment alignment = getAlignment();
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
	
	
	
	protected ChildEntry getChildEntryClosestToLocalPoint(Point2 localPos)
	{
		if ( childEntries.size() == 0 )
		{
			return null;
		}
		else if ( childEntries.size() == 1 )
		{
			return childEntries.firstElement();
		}
		else
		{
			ChildEntry entryI = childEntries.firstElement();
			for (int i = 0; i < childEntries.size() - 1; i++)
			{
				ChildEntry entryJ = childEntries.get( i + 1 );
				double iUpperX = entryI.pos.x + entryI.size.x;
				double jLowerX = entryJ.pos.x;
				
				double midX = ( iUpperX + jLowerX ) * 0.5;
				
				if ( localPos.x < midX )
				{
					return entryI;
				}
				
				entryI = entryJ;
			}
			
			return childEntries.lastElement();
		}
	}


	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return getChildren();
	}
	
	
	protected Alignment getAlignment()
	{
		return ((HBoxStyleSheet)styleSheet).getAlignment();
	}
}
