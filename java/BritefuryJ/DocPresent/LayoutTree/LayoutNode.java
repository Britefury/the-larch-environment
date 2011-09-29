//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.List;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFilter;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;

public abstract class LayoutNode
{
	protected DPElement element;
	
	
	protected LayoutNode(DPElement element)
	{
		this.element = element;
	}
	
	
	public boolean hasLayoutForElement(DPElement e)
	{
		return e.getLayoutNode() != null;
	}
	
	public DPElement getElement()
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
	// Resize queueing methods
	//
	//
	
	protected void handleQueueResize()
	{
		if ( !element.isResizeQueued() )
		{
			DPContainer parent = element.getParent();
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

	
	
	public DPContentLeaf getLeftContentLeaf()
	{
		return null;
	}
	
	public DPContentLeaf getRightContentLeaf()
	{
		return null;
	}
	
	public DPContentLeafEditable getLeftEditableContentLeaf()
	{
		return null;
	}
	
	public DPContentLeafEditable getRightEditableContentLeaf()
	{
		return null;
	}
	
	public DPContentLeaf getContentLeafToLeft()
	{
		DPContainer parent = element.getParent();
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
	
	public DPContentLeaf getContentLeafToRight()
	{
		DPContainer parent = element.getParent();
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
	
	public DPContentLeafEditable getTopOrBottomEditableContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace)
	{
		return null;
	}

	public abstract DPElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter);

	
	

	public List<DPElement> horizontalNavigationList()
	{
		return null;
	}

	public List<DPElement> verticalNavigationList()
	{
		return null;
	}
}