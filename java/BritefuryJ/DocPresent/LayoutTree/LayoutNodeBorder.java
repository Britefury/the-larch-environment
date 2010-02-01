//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Layout.LAllocV;

public class LayoutNodeBorder extends LayoutNodeBin
{
	public LayoutNodeBorder(DPBorder element)
	{
		super( element );
	}

	
	
	protected void updateRequisitionX()
	{
		DPBorder borderElement = (DPBorder)element;
		DPWidget child = borderElement.getChild();
		Border border = borderElement.getBorder();
		if ( child != null )
		{
			layoutReqBox.setRequisitionX( child.getLayoutNode().refreshRequisitionX() );
		}
		else
		{
			layoutReqBox.clearRequisitionX();
		}
		layoutReqBox.borderX( border.getLeftMargin(), border.getRightMargin() );
	}

	protected void updateRequisitionY()
	{
		DPBorder borderElement = (DPBorder)element;
		DPWidget child = borderElement.getChild();
		Border border = borderElement.getBorder();
		if ( child != null )
		{
			layoutReqBox.setRequisitionY( child.getLayoutNode().refreshRequisitionY() );
		}
		else
		{
			layoutReqBox.clearRequisitionY();
		}
		layoutReqBox.borderY( border.getTopMargin(), border.getBottomMargin() );
	}

	
	
	
	protected void updateAllocationX()
	{
		DPBorder borderElement = (DPBorder)element;
		DPWidget child = borderElement.getChild();
		Border border = borderElement.getBorder();
		if ( child != null )
		{
			LayoutNode childLayoutNode = child.getLayoutNode();
			double prevWidth = childLayoutNode.getAllocationBox().getAllocationX();
			double hborder = border.getLeftMargin() + border.getRightMargin();
			layoutAllocBox.allocateChildXAligned( childLayoutNode.getAllocationBox(), childLayoutNode.getRequisitionBox(), child.getAlignmentFlags(), border.getLeftMargin(), layoutAllocBox.getAllocationX() - hborder );
			childLayoutNode.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		DPBorder borderElement = (DPBorder)element;
		DPWidget child = borderElement.getChild();
		Border border = borderElement.getBorder();
		if ( child != null )
		{
			LayoutNode childLayoutNode = child.getLayoutNode();
			LAllocV prevAllocV = childLayoutNode.getAllocationBox().getAllocV();
			layoutAllocBox.allocateChildYAligned( childLayoutNode.getAllocationBox(), childLayoutNode.getRequisitionBox(), child.getAlignmentFlags(),
					border.getTopMargin(), layoutAllocBox.getAllocV().borderY( border.getTopMargin(), border.getBottomMargin() ) );
			childLayoutNode.refreshAllocationY( prevAllocV );
		}
	}
}
