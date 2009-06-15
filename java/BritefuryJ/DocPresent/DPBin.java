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
				registerChild( child, null );				
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
	
	
	

	protected void updateRequisitionX()
	{
		if ( child != null )
		{
			layoutBox.setRequisitionX( child.refreshRequisitionX() );
		}
		else
		{
			layoutBox.clearRequisitionX();
		}
	}

	protected void updateRequisitionY()
	{
		if ( child != null )
		{
			layoutBox.setRequisitionY( child.refreshRequisitionY() );
		}
		else
		{
			layoutBox.clearRequisitionY();
		}
	}
	
	
	
	protected void updateAllocationX()
	{
		if ( child != null )
		{
			double prevWidth = child.layoutBox.getAllocationX();
			layoutBox.allocateChildX( child.layoutBox );
			child.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		if ( child != null )
		{
			double prevHeight = child.layoutBox.getAllocationY();
			layoutBox.allocateChildY( child.layoutBox );
			child.refreshAllocationY( prevHeight );
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
