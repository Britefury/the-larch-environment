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
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;
import BritefuryJ.Math.Point2;




public class DPVBox extends DPAbstractBox
{
	public enum Alignment { LEFT, CENTRE, RIGHT, EXPAND };
	public enum Typesetting { NONE, ALIGN_WITH_TOP, ALIGN_WITH_BOTTOM };
	
	
	public static class InvalidTypesettingException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	

	
	public DPVBox()
	{
		this( VBoxStyleSheet.defaultStyleSheet );
	}
	
	public DPVBox(VBoxStyleSheet syleSheet)
	{
		super( syleSheet );
	}
	
	
	
	
	public void append(DPWidget child,  boolean bExpand, double padding)
	{
		append( child );
		child.setParentPacking( new BoxParentPacking( bExpand, padding ) );
	}

	
	public void insert(int index, DPWidget child, boolean bExpand, double padding)
	{
		insert( index, child );
		child.setParentPacking( new BoxParentPacking( bExpand, padding ) );
	}
	
	
	
	protected BoxParentPacking createParentPackingForChild(DPWidget child)
	{
		return new BoxParentPacking( getExpand(), getPadding() );
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
		
		double[] midPoints = new double[registeredChildren.size()];
		
		for (int i = 0; i < midPoints.length; i++)
		{
			DPWidget child = registeredChildren.get( i );
			midPoints[i] = child.getPositionInParentSpace().y  +  child.getAllocationInParentSpace().y * 0.5;
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

		Typesetting typesetting = getTypesetting();
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
				if ( bottomTSMetrics != null )
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
			double spacing = getSpacing();
			
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
		
		Alignment alignment = getAlignment();

		for (DPWidget child: registeredChildren)
		{
			double childWidth = Math.min( child.prefH.width, allocation );
			if ( alignment == Alignment.LEFT )
			{
				allocateChildX( child, 0.0, childWidth );
			}
			else if ( alignment == Alignment.CENTRE )
			{
				allocateChildX( child, ( allocation - childWidth )  *  0.5, childWidth );
			}
			else if ( alignment == Alignment.RIGHT )
			{
				allocateChildX( child, allocation - childWidth, childWidth );
			}
			else if ( alignment == Alignment.EXPAND )
			{
				allocateChildX( child, 0.0, allocation );
			}
		}
	}

	protected void allocateContentsY(double allocation)
	{
		super.allocateContentsY( allocation );
		
		double spacing = getSpacing();
		
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
			
			allocateChildY( registeredChildren.get( i ), childY, chm.height );
			
			height = y + chm.height + childPadding * 2.0;
			y = height + chm.vspacing;
		}
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( localPos, filter );
	}


	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return getChildren();
	}
	
	protected List<DPWidget> verticalNavigationList()
	{
		return getChildren();
	}



	
	
	
	
	protected Typesetting getTypesetting()
	{
		return ((VBoxStyleSheet)styleSheet).getTypesetting();
	}

	protected Alignment getAlignment()
	{
		return ((VBoxStyleSheet)styleSheet).getAlignment();
	}
}
