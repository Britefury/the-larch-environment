//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.List;

import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;
import BritefuryJ.Math.Point2;


public class DPBin extends DPContainer
{
	public DPBin()
	{
		this( null );
	}

	public DPBin(ElementStyleSheet styleSheet)
	{
		super( styleSheet );
	}
	
	
	
	public DPWidget getChild()
	{
		if ( registeredChildren.size() > 0 )
		{
			return registeredChildren.get( 0 );
		}
		else
		{
			return null;
		}
	}
	
	public void setChild(DPWidget child)
	{
		DPWidget prevChild = getChild();
		if ( child != prevChild )
		{
			if ( prevChild != null )
			{
				unregisterChild( prevChild );
				registeredChildren.remove( 0 );
			}
			
			if ( child != null )
			{
				registeredChildren.add( child );
				registerChild( child, null );				
			}
			
			onChildListModified();
			queueResize();
		}
	}
	
	
	protected void replaceChildWithEmpty(DPWidget child)
	{
		assert child == this.getChild();
		setChild( null );
	}
	
	

	public List<DPWidget> getChildren()
	{
		return registeredChildren;
	}

	
	protected void updateRequisitionX()
	{
		DPWidget child = getChild();
		if ( child != null )
		{
			layoutReqBox.setRequisitionX( child.refreshRequisitionX() );
		}
		else
		{
			layoutReqBox.clearRequisitionX();
		}
	}

	protected void updateRequisitionY()
	{
		DPWidget child = getChild();
		if ( child != null )
		{
			layoutReqBox.setRequisitionY( child.refreshRequisitionY() );
		}
		else
		{
			layoutReqBox.clearRequisitionY();
		}
	}
	
	
	
	protected void updateAllocationX()
	{
		DPWidget child = getChild();
		if ( child != null )
		{
			double prevWidth = child.layoutAllocBox.getAllocationX();
			layoutAllocBox.allocateChildX( child.layoutAllocBox );
			child.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		DPWidget child = getChild();
		if ( child != null )
		{
			double prevHeight = child.layoutAllocBox.getAllocationY();
			layoutAllocBox.allocateChildY( child.layoutAllocBox );
			child.refreshAllocationY( prevHeight );
		}
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		DPWidget child = getChild();
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
	// Packing parameters
	//
	
	protected PackingParams getDefaultPackingParams()
	{
		return null;
	}



	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		DPWidget child = getChild();
		if ( child != null )
		{
			return registeredChildren;
		}
		else
		{
			return null;
		}
	}
	
	
	
	//
	// Text representation methods
	//
	
	protected String computeSubtreeTextRepresentation()
	{
		DPWidget child = getChild();
		return child != null  ?  child.getTextRepresentation()  :  "";
	}
}
