//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.Math.Point2;


public class DPBin extends DPContainer
{
	protected DPWidget child;
	protected double childScale;
	
	
	
	public DPBin()
	{
		this( ContainerStyleSheet.defaultStyleSheet );
	}

	public DPBin(ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
		
		childScale = 1.0;
	}
	
	
	
	public DPWidget getChild()
	{
		return child;
	}
	
	public void setChild(DPWidget child)
	{
		if ( child != this.child )
		{
			if ( child != null )
			{
				child.unparent();
			}
			
			DPWidget prevChild = this.child;
			
			if ( prevChild != null )
			{
				unregisterChild( prevChild );
				registeredChildren.remove( prevChild );
			}
			
			this.child = child;
			
			if ( this.child != null )
			{
				registeredChildren.add( child );
				registerChild( child );				
			}
			
			queueResize();
		}
	}
	
	
	protected void replaceChildWithEmpty(DPWidget child)
	{
		assert child == this.child;
		setChild( null );
	}
	
	

	protected List<DPWidget> getChildren()
	{
		if ( child != null )
		{
			DPWidget[] children = { child };
			return Arrays.asList( children );
		}
		else
		{
			DPWidget[] children = {};
			return Arrays.asList( children );
		}
	}

	
	public double getChildScale()
	{
		return childScale;
	}
	
	public void setChildScale(double scale)
	{
		childScale = scale;
		queueResize();
	}
	
	
	

	protected HMetrics computeMinimumHMetrics()
	{
		if ( child != null )
		{
			return child.refreshMinimumHMetrics();
		}
		else
		{
			return new HMetrics();
		}
	}
	
	protected HMetrics computePreferredHMetrics()
	{
		if ( child != null )
		{
			return child.refreshPreferredHMetrics();
		}
		else
		{
			return new HMetrics();
		}
	}
	
	protected VMetrics computeMinimumVMetrics()
	{
		if ( child != null )
		{
			return child.refreshMinimumVMetrics();
		}
		else
		{
			return new VMetrics();
		}
	}

	protected VMetrics computePreferredVMetrics()
	{
		if ( child != null )
		{
			return child.refreshPreferredVMetrics();
		}
		else
		{
			return new VMetrics();
		}
	}
	
	
	
	
	protected void allocateContentsX(double width)
	{
		if ( child != null )
		{
			allocateChildX( child, 0.0, width );
		}
	}

	protected void allocateContentsY(double height)
	{
		if ( child != null )
		{
			allocateChildY( child, 0.0, height );
		}
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		if ( child == null )
		{
			return null;
		}
		else
		{
			return getLeafClosestToLocalPointFromChild( registeredChildren.get( 0 ), localPos, filter );
		}
	}

	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		if ( child != null )
		{
			DPWidget[] navList = { child };
			return Arrays.asList( navList );
		}
		else
		{
			return null;
		}
	}
}
