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
	public boolean hasLayoutForElement(DPElement element)
	{
		return element.getLayoutNode() != null;
	}
	
	public abstract DPElement getElement();
	
	public abstract LReqBoxInterface getRequisitionBox();
	public abstract LAllocBoxInterface getAllocationBox();
	
	public abstract Point2 getPositionInParentSpace();
	public abstract double getAllocPositionInParentAllocationSpaceX();
	public abstract double getAllocPositionInParentAllocationSpaceY();
	public abstract Point2 getPositionInParentAllocationSpace();
	public abstract double getAllocationX();
	public abstract double getAllocationY();
	public abstract Vector2 getAllocation();
	public abstract LAllocV getAllocV();
	public abstract double getAllocationInParentSpaceX();
	public abstract double getAllocationInParentSpaceY();
	public abstract Vector2 getAllocationInParentSpace();

	
	public abstract LReqBoxInterface refreshRequisitionX();
	public abstract LReqBoxInterface refreshRequisitionY();

	public abstract void refreshAllocationX(double prevWidth);
	public abstract void refreshAllocationY(LAllocV prevHeight);

	protected abstract void onAllocationXRefreshed();
	protected abstract void onAllocationYRefreshed();
	
	protected abstract void onAllocationRefreshed();
	protected abstract void onChildSizeRefreshed();
	

	
	protected Xform2 getParentAllocationToParentSpaceXform()
	{
		return getElement().getParentAllocationToParentSpaceXform();
	}
	

	protected void handleQueueResize()
	{
		DPElement element = getElement();

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
		DPElement element = getElement();
		
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
		DPElement element = getElement();
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
		DPElement element = getElement();
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