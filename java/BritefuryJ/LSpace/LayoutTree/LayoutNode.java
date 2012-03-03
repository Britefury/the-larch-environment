//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import java.util.List;

import BritefuryJ.LSpace.LSContainer;
import BritefuryJ.LSpace.LSContentLeaf;
import BritefuryJ.LSpace.LSContentLeafEditable;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Layout.LAllocBoxInterface;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;

public abstract class LayoutNode
{
	protected LSElement element;
	
	
	protected LayoutNode(LSElement element)
	{
		this.element = element;
	}
	
	
	public boolean hasLayoutForElement(LSElement e)
	{
		return e.getLayoutNode() != null;
	}
	
	public LSElement getElement()
	{
		return element;
	}
	
	public abstract LReqBoxInterface getRequisitionBox();
	public abstract LAllocBoxInterface getAllocationBox();
	
	public abstract Point2 getPositionInParentSpace();
	public abstract double getAllocPositionInParentSpaceX();
	public abstract double getAllocPositionInParentSpaceY();
	public abstract Point2 getPositionInParentAllocationSpace();
	public abstract double getActualWidth();
	public abstract double getActualHeight();
	public abstract Vector2 getActualSize();
	public abstract double getActualWidthInParentSpace();
	public abstract double getActualHeightInParentSpace();
	public abstract Vector2 getActualSizeInParentSpace();
	public abstract double getAllocWidth();
	public abstract double getAllocHeight();
	public abstract Vector2 getAllocSize();
	public abstract LAllocV getAllocV();

	
	
	protected Xform2 getLocalToParentAllocationSpaceXform()
	{
		return element.getLocalToParentAllocationSpaceXform();
	}
	
	
	

	//
	//
	// Requisition refresh methods
	//
	//
	
	public LReqBoxInterface refreshRequisitionX()
	{
		if ( !element.isAllocationUpToDate() )
		{
			updateRequisitionX();
		}
		return getRequisitionBox();
	}
	
	public LReqBoxInterface refreshRequisitionY()
	{
		if ( !element.isAllocationUpToDate() )
		{
			updateRequisitionY();
		}
		return getRequisitionBox();
	}

	
	
	protected abstract void updateRequisitionX();
	protected abstract void updateRequisitionY();
	
	
	
	//
	//
	// Allocation refresh methods
	//
	//
	
	public void refreshAllocationX(double prevWidth)
	{
		if ( !element.isAllocationUpToDate()  ||  getAllocWidth() != prevWidth )
		{
			updateAllocationX();
			element.clearFlagAllocationUpToDate();
		}
	}
	
	public void refreshAllocationY(LAllocV prevHeight)
	{
		if ( !element.isAllocationUpToDate()  ||  !getAllocV().equals( prevHeight ) )
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
	
	protected void onAllocationRefreshed()
	{
		element.clearFlagResizeQueued();
		element.setFlagAllocationUpToDate();
		LSContainer parent = element.getParent();
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
	// Resize queueing methods
	//
	//
	
	protected void handleQueueResize()
	{
		if ( !element.isResizeQueued() )
		{
			LSContainer parent = element.getParent();
			if ( parent != null )
			{
				LayoutNode parentLayout = parent.getValidLayoutNode();
				parentLayout.queueResize();
			}
			element.setFlagResizeQueued();
		}
	}

	public void queueResize()
	{
		handleQueueResize();
		element.clearFlagAllocationUpToDate();
	}

	
	
	public LSContentLeaf getLeftContentLeaf()
	{
		return null;
	}
	
	public LSContentLeaf getRightContentLeaf()
	{
		return null;
	}
	
	public LSContentLeafEditable getLeftEditableContentLeaf()
	{
		return null;
	}
	
	public LSContentLeafEditable getRightEditableContentLeaf()
	{
		return null;
	}
	
	public LSContentLeaf getContentLeafToLeft()
	{
		LSContainer parent = element.getParent();
		BranchLayoutNode parentBranchLayout = parent != null  ?  (BranchLayoutNode)parent.getValidLayoutNodeOfClass( BranchLayoutNode.class )  :  null;
		
		if ( parentBranchLayout != null )
		{
			return parentBranchLayout.getContentLeafToLeftFromChild( element );
		}
		else
		{
			return null;
		}
	}
	
	public LSContentLeaf getContentLeafToRight()
	{
		LSContainer parent = element.getParent();
		BranchLayoutNode parentBranchLayout = parent != null  ?  (BranchLayoutNode)parent.getValidLayoutNodeOfClass( BranchLayoutNode.class )  :  null;
		
		if ( parentBranchLayout != null )
		{
			return parentBranchLayout.getContentLeafToRightFromChild( element );
		}
		else
		{
			return null;
		}
	}
	
	public LSContentLeafEditable getTopOrBottomEditableContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace)
	{
		return null;
	}

	public abstract LSElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter);

	
	

	public List<LSElement> horizontalNavigationList()
	{
		return null;
	}

	public List<LSElement> verticalNavigationList()
	{
		return null;
	}
}