//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPButton;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Layout.LAllocHelper;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.StyleParams.ButtonStyleParams;

public class LayoutNodeButton extends LayoutNodeBin
{
	public LayoutNodeButton(DPButton element)
	{
		super( element );
	}


	
	
	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPButton button = (DPButton)element;
		ButtonStyleParams buttonStyle = (ButtonStyleParams)button.getStyleSheet();
		SolidBorder border = buttonStyle.getBorder();
		DPWidget child = button.getChild();

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
		DPButton button = (DPButton)element;
		ButtonStyleParams buttonStyle = (ButtonStyleParams)button.getStyleSheet();
		SolidBorder border = buttonStyle.getBorder();
		DPWidget child = button.getChild();

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
		DPButton button = (DPButton)element;
		ButtonStyleParams buttonStyle = (ButtonStyleParams)button.getStyleSheet();
		SolidBorder border = buttonStyle.getBorder();
		DPWidget child = button.getChild();

		if ( child != null )
		{
			LayoutNode childLayoutNode = child.getLayoutNode();
			double prevWidth = childLayoutNode.getAllocationBox().getAllocationX();
			double hborder = border.getLeftMargin() + border.getRightMargin();
			LAllocHelper.allocateChildXAligned( childLayoutNode.getAllocationBox(), childLayoutNode.getRequisitionBox(), child.getAlignmentFlags(), border.getLeftMargin(), getAllocationBox().getAllocationX() - hborder );
			childLayoutNode.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		DPButton button = (DPButton)element;
		ButtonStyleParams buttonStyle = (ButtonStyleParams)button.getStyleSheet();
		SolidBorder border = buttonStyle.getBorder();
		DPWidget child = button.getChild();

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
