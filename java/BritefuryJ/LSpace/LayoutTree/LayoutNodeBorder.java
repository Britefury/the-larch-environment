//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.LSpace.LSBorder;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Layout.LAllocHelper;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;

public class LayoutNodeBorder extends LayoutNodeBin
{
	public LayoutNodeBorder(LSBorder element)
	{
		super( element );
	}

	
	
	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSBorder borderElement = (LSBorder)element;
		LSElement child = borderElement.getChild();
		AbstractBorder border = borderElement.getBorder();
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
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSBorder borderElement = (LSBorder)element;
		LSElement child = borderElement.getChild();
		AbstractBorder border = borderElement.getBorder();
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
		LSBorder borderElement = (LSBorder)element;
		LSElement child = borderElement.getChild();
		AbstractBorder border = borderElement.getBorder();
		if ( child != null )
		{
			LayoutNode childLayoutNode = child.getLayoutNode();
			double prevWidth = childLayoutNode.getAllocationBox().getAllocWidth();
			double hborder = border.getLeftMargin() + border.getRightMargin();
			LAllocHelper.allocateChildXAligned( childLayoutNode.getAllocationBox(), childLayoutNode.getRequisitionBox(), child.getAlignmentFlags(),
					border.getLeftMargin(), getAllocationBox().getAllocWidth() - hborder );
			childLayoutNode.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		LSBorder borderElement = (LSBorder)element;
		LSElement child = borderElement.getChild();
		AbstractBorder border = borderElement.getBorder();
		if ( child != null )
		{
			LayoutNode childLayoutNode = child.getLayoutNode();
			LAllocV prevAllocV = childLayoutNode.getAllocationBox().getAllocV();
			LAllocHelper.allocateChildYAligned( childLayoutNode.getAllocationBox(), childLayoutNode.getRequisitionBox(), child.getAlignmentFlags(),
					border.getTopMargin(), getAllocationBox().getAllocV().borderY( border.getTopMargin(), border.getBottomMargin() ) );
			childLayoutNode.refreshAllocationY( prevAllocV );
		}
	}
}
