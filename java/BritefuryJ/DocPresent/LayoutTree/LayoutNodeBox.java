//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.List;

import BritefuryJ.DocPresent.DPBox;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFilter;
import BritefuryJ.DocPresent.Layout.LAllocHelper;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.Math.Point2;

public class LayoutNodeBox extends ArrangedLayoutNode
{
	public LayoutNodeBox(DPBox element)
	{
		super( element );
	}

	
	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPBox bin = (DPBox)element;
		DPElement child = bin.getChild();
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
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPBox bin = (DPBox)element;
		DPElement child = bin.getChild();
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
		DPBox bin = (DPBox)element;
		DPElement child = bin.getChild();
		if ( child != null )
		{
			LayoutNode childLayout = child.getLayoutNode();
			double prevWidth = childLayout.getAllocationBox().getAllocationX();
			LAllocHelper.allocateChildXAligned( childLayout.getAllocationBox(), childLayout.getRequisitionBox(), child.getAlignmentFlags(), 0.0, getAllocationBox().getAllocationX() );
			childLayout.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		DPBox bin = (DPBox)element;
		DPElement child = bin.getChild();
		if ( child != null )
		{
			LayoutNode childLayout = child.getLayoutNode();
			LAllocV prevAllocV = childLayout.getAllocationBox().getAllocV();
			LAllocHelper.allocateChildYAligned( childLayout.getAllocationBox(), childLayout.getRequisitionBox(), child.getAlignmentFlags(), 0.0, getAllocationBox().getAllocV() );
			childLayout.refreshAllocationY( prevAllocV );
		}
	}
	
	
	
	protected DPElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		DPBox bin = (DPBox)element;
		DPElement child = bin.getChild();
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
	
	public List<DPElement> horizontalNavigationList()
	{
		DPBox bin = (DPBox)element;
		List<DPElement> children = bin.getLayoutChildren();
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
