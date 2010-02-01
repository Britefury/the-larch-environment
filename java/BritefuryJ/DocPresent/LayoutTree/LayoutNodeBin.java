//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.List;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.Math.Point2;

public class LayoutNodeBin extends ArrangedLayoutNode
{
	public LayoutNodeBin(DPBin element)
	{
		super( element );
	}

	
	protected void updateRequisitionX()
	{
		DPBin bin = (DPBin)element;
		DPWidget child = bin.getChild();
		if ( child != null )
		{
			layoutReqBox.setRequisitionX( child.getLayoutNode().refreshRequisitionX() );
		}
		else
		{
			layoutReqBox.clearRequisitionX();
		}
	}

	protected void updateRequisitionY()
	{
		DPBin bin = (DPBin)element;
		DPWidget child = bin.getChild();
		if ( child != null )
		{
			layoutReqBox.setRequisitionY( child.getLayoutNode().refreshRequisitionY() );
		}
		else
		{
			layoutReqBox.clearRequisitionY();
		}
	}
	
	
	
	protected void updateAllocationX()
	{
		DPBin bin = (DPBin)element;
		DPWidget child = bin.getChild();
		if ( child != null )
		{
			LayoutNode childLayout = child.getLayoutNode();
			double prevWidth = childLayout.getAllocationBox().getAllocationX();
			layoutAllocBox.allocateChildXAligned( childLayout.getAllocationBox(), childLayout.getRequisitionBox(), child.getAlignmentFlags(), 0.0, layoutAllocBox.getAllocationX() );
			childLayout.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		DPBin bin = (DPBin)element;
		DPWidget child = bin.getChild();
		if ( child != null )
		{
			LayoutNode childLayout = child.getLayoutNode();
			LAllocV prevAllocV = childLayout.getAllocationBox().getAllocV();
			layoutAllocBox.allocateChildYAligned( childLayout.getAllocationBox(), childLayout.getRequisitionBox(), child.getAlignmentFlags(), 0.0, layoutAllocBox.getAllocV() );
			childLayout.refreshAllocationY( prevAllocV );
		}
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		DPBin bin = (DPBin)element;
		DPWidget child = bin.getChild();
		if ( child == null )
		{
			return null;
		}
		else
		{
			return getLeafClosestToLocalPointFromChild( child, localPos, filter );
		}
	}

	
	
	
	//
	// Focus navigation methods
	//
	
	public List<DPWidget> horizontalNavigationList()
	{
		DPBin bin = (DPBin)element;
		List<DPWidget> children = bin.getLayoutChildren();
		if ( children.size() > 0 )
		{
			return children;
		}
		else
		{
			return null;
		}
	}
}
