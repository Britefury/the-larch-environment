//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public abstract class LeafLayoutNodeSharedReq extends LayoutNode implements LAllocBoxInterface
{
	protected DPElement element;
	protected LReqBox layoutReqBox;
	
	
	public LeafLayoutNodeSharedReq(DPElement element, LReqBox reqBox)
	{
		this.element = element;
		layoutReqBox = reqBox;
	}


	public DPElement getElement()
	{
		return element;
	}
	

	
	
	public LReqBoxInterface getRequisitionBox()
	{
		return layoutReqBox;
	}
	
	public LAllocBoxInterface getAllocationBox()
	{
		return this;
	}
	
	
	
	
	
	public double getAllocationInParentSpaceX()
	{
		return getAllocationX()  *  getParentAllocationToParentSpaceXform().scale;
	}
	
	public double getAllocationInParentSpaceY()
	{
		return getAllocationY()  *  getParentAllocationToParentSpaceXform().scale;
	}
	
	public Vector2 getAllocationInParentSpace()
	{
		return getAllocation().mul( getParentAllocationToParentSpaceXform().scale );
	}
	
	
	
	
	
	public LReqBoxInterface refreshRequisitionX()
	{
		if ( !element.isAllocationUpToDate() )
		{
			updateRequisitionX();
		}
		return layoutReqBox;
	}
	
	public LReqBoxInterface refreshRequisitionY()
	{
		if ( !element.isAllocationUpToDate() )
		{
			updateRequisitionY();
		}
		return layoutReqBox;
	}
	

	
	protected abstract void updateRequisitionX();
	protected abstract void updateRequisitionY();


	
	
	
	public void refreshAllocationX(double prevWidth)
	{
		if ( !element.isAllocationUpToDate()  ||  getAllocationBox().getAllocationX() != prevWidth )
		{
			updateAllocationX();
			element.clearFlagAllocationUpToDate();
		}
	}
	
	public void refreshAllocationY(LAllocV prevHeight)
	{
		if ( !element.isAllocationUpToDate()  ||  !getAllocationBox().getAllocV().equals( prevHeight ) )
		{
			updateAllocationY();
		}
		onAllocationRefreshed();
	}
	

	
	
	protected void updateAllocationX()
	{
	}

	protected void updateAllocationY()
	{
	}

	
	

	
	
	
	protected void onAllocationXRefreshed()
	{
		element.clearFlagAllocationUpToDate();
	}
	
	protected void onAllocationYRefreshed()
	{
		onAllocationRefreshed();
	}
	
	protected void onAllocationRefreshed()
	{
		element.clearFlagResizeQueued();
		element.setFlagAllocationUpToDate();
		DPContainer parent = element.getParent();
		while ( parent != null )
		{
			LayoutNode parentLayout = parent.getLayoutNode();
			if ( parentLayout != null )
			{
				parentLayout.onChildSizeRefreshed();
				break;
			}
			parent = parent.getParent();
		}
	}
	
	protected void onChildSizeRefreshed()
	{
	}
	
	
	
	
	
	
	
	
	
	
	
	//
	//
	//
	//
	//
	// ALLOCATION BOX IMPLEMENTATION
	//
	//
	//
	//
	//

	protected double alloc_positionInParentAllocationSpaceX, alloc_positionInParentAllocationSpaceY;
	protected double alloc_allocationX, alloc_allocationY;
	protected double alloc_refY;

	
	public LayoutNode getAllocLayoutNode()
	{
		return this;
	}
	
	
	
	public Point2 getPositionInParentSpace()
	{
		return getParentAllocationToParentSpaceXform().transform( new Point2( alloc_positionInParentAllocationSpaceX, alloc_positionInParentAllocationSpaceY ) );
	}
	
	public double getAllocPositionInParentAllocationSpaceX()
	{
		return alloc_positionInParentAllocationSpaceX;
	}
	
	public double getAllocPositionInParentAllocationSpaceY()
	{
		return alloc_positionInParentAllocationSpaceY;
	}
	
	public Point2 getPositionInParentAllocationSpace()
	{
		return new Point2( alloc_positionInParentAllocationSpaceX, alloc_positionInParentAllocationSpaceY );
	}
	
	public double getAllocationX()
	{
		return alloc_allocationX;
	}
	
	public double getAllocationY()
	{
		return alloc_allocationY;
	}
	
	public double getAllocRefY()
	{
		return alloc_refY;
	}
	
	public LAllocV getAllocV()
	{
		return new LAllocV( alloc_allocationY, alloc_refY );
	}
	
	public Vector2 getAllocation()
	{
		return new Vector2( alloc_allocationX, alloc_allocationY );
	}


	
	
	
	//
	// SETTERS
	//
	
	public void setAllocPositionInParentAllocationSpaceX(double x)
	{
		alloc_positionInParentAllocationSpaceX = x;
	}
	
	public void setAllocPositionInParentAllocationSpaceY(double y)
	{
		alloc_positionInParentAllocationSpaceY = y;
	}
	
	public void setAllocationX(double width)
	{
		alloc_allocationX = width;
	}

	public void setAllocationY(double height, double refY)
	{
		alloc_allocationY = height;
		this.alloc_refY = refY;
	}

	public void setAllocation(double width, double height, double refY)
	{
		alloc_allocationX = width;
		alloc_allocationY = height;
		this.alloc_refY = refY;
	}

	public void setPositionInParentAllocationSpaceAndAllocationX(double x, double width)
	{
		alloc_positionInParentAllocationSpaceX = x;
		alloc_allocationX = width;
	}
	
	public void setPositionInParentAllocationSpaceAndAllocationY(double y, double height)
	{
		alloc_positionInParentAllocationSpaceY = y;
		alloc_allocationY = height;
		alloc_refY = height * 0.5;
	}
	
	public void setPositionInParentAllocationSpaceAndAllocationY(double y, double height, double refY)
	{
		alloc_positionInParentAllocationSpaceY = y;
		alloc_allocationY = height;
		this.alloc_refY = refY;
	}



	public void scaleAllocationX(double scale)
	{
		alloc_allocationX *= scale;
	}

	public void scaleAllocationY(double scale)
	{
		alloc_allocationY *= scale;
		alloc_refY *= scale;
	}
}
