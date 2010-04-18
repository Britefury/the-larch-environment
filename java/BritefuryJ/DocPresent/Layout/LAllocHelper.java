//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class LAllocHelper
{
	public static void allocateX(LAllocBoxInterface alloc, LReqBoxInterface requisition, double x, double width)
	{
		double w = Math.max( width, requisition.getReqMinWidth() );
		alloc.setPositionInParentAllocationSpaceAndAllocationX( x, w );
	}
	
	public static void allocateY(LAllocBoxInterface alloc, LReqBoxInterface requisition, double y, double height)
	{
		double h = Math.max( height, requisition.getReqHeight() );
		double delta = Math.max( ( height - requisition.getReqHeight() )  *  0.5,  0.0 );
		double refY = requisition.getReqRefY() + delta;
		
		alloc.setPositionInParentAllocationSpaceAndAllocationY( y, h, refY );
	}
	
	public static void allocateY(LAllocBoxInterface alloc, LReqBoxInterface requisition, double y, double height, double refY)
	{
		double h = Math.max( height, requisition.getReqHeight() );
		double delta = Math.max( ( height - requisition.getReqHeight() )  *  0.5,  0.0 );
		refY += delta;
		
		alloc.setPositionInParentAllocationSpaceAndAllocationY( y, h, refY );
	}
	
	public static void allocateY(LAllocBoxInterface alloc, LReqBoxInterface requisition, double y, LAllocV allocV)
	{
		double h = Math.max( allocV.height, requisition.getReqHeight() );
		double refY = Math.max( allocV.refY, requisition.getReqRefY() );
		
		alloc.setPositionInParentAllocationSpaceAndAllocationY( y, h, refY );
	}

	
	public static void allocateX(LAllocBoxInterface alloc, LAllocBoxInterface box)
	{
		alloc.setAllocationX( box.getAllocationX() );
	}
	
	public static void allocateY(LAllocBoxInterface alloc, LAllocBoxInterface box)
	{
		alloc.setAllocationY( box.getAllocationY(), box.getAllocRefY() );
	}
	
	public static void allocateSize(LAllocBoxInterface alloc, LAllocBoxInterface box)
	{
		alloc.setAllocation( box.getAllocationX(), box.getAllocationY(), box.getAllocRefY() );
	}
	

	
	
	
	
	protected static void allocateChildPositionX(LAllocBoxInterface childAllocation, double localPosX)
	{
		childAllocation.setAllocPositionInParentAllocationSpaceX( localPosX );
	}
	
	protected static void allocateChildWidth(LAllocBoxInterface childAllocation, double localWidth)
	{
		childAllocation.setAllocationX( localWidth );
	}
	
	protected static void allocateChildX(LAllocBoxInterface childAllocation, double localPosX, double localWidth)
	{
		childAllocation.setPositionInParentAllocationSpaceAndAllocationX( localPosX, localWidth );
	}
	
	public static void allocateChildXAligned(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, int alignmentFlags, double regionX, double regionWidth)
	{
		allocateChildXAligned( childAllocation, childRequisition, ElementAlignment.getHAlignment( alignmentFlags ), regionX, regionWidth );
	}
	
	public static void allocateChildXAligned(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, HAlignment hAlign, double regionX, double regionWidth)
	{
		double childWidth = childRequisition.getReqPrefWidth();
		if ( regionWidth <= childWidth )
		{
			childAllocation.setPositionInParentAllocationSpaceAndAllocationX( regionX, Math.max( regionWidth, childRequisition.getReqMinWidth() ) );
		}
		else
		{
			if ( hAlign == HAlignment.EXPAND )
			{
				childAllocation.setPositionInParentAllocationSpaceAndAllocationX( regionX, regionWidth );
			}
			else
			{
				if ( hAlign == HAlignment.LEFT )
				{
					childAllocation.setPositionInParentAllocationSpaceAndAllocationX( regionX, childWidth );
				}
				else if ( hAlign == HAlignment.CENTRE )
				{
					childAllocation.setPositionInParentAllocationSpaceAndAllocationX( regionX + ( regionWidth - childWidth ) * 0.5, childWidth );
				}
				else if ( hAlign == HAlignment.RIGHT )
				{
					childAllocation.setPositionInParentAllocationSpaceAndAllocationX( regionX + ( regionWidth - childWidth ), childWidth );
				}
				else
				{
					throw new RuntimeException( "Invalid h-alignment" );
				}
			}
		}
	}
	

	
	
	
	
	protected static void allocateChildPositionY(LAllocBoxInterface childAllocation, double localPosY)
	{
		childAllocation.setAllocPositionInParentAllocationSpaceY( localPosY );
	}
	
	protected static void allocateChildHeightAsRequisition(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition)
	{
		childAllocation.setAllocationY( childRequisition.getReqHeight(), childRequisition.getReqRefY() );
	}
	
	protected static void allocateChildHeightPaddedRequisition(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, double totalHeight)
	{
		double totalPadding = Math.max( totalHeight - childRequisition.getReqHeight(),  0.0 );
		double padding = totalPadding * 0.5;
		childAllocation.setAllocationY( childRequisition.getReqHeight() + totalPadding, childRequisition.getReqRefY() + padding );
	}
	
	protected static void allocateChildHeight(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, double height, double refY)
	{
		childAllocation.setAllocationY( height, refY );
	}
	
	
	protected static void allocateChildYAsRequisition(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, double localPosY)
	{
		childAllocation.setPositionInParentAllocationSpaceAndAllocationY( localPosY, childRequisition.getReqHeight(), childRequisition.getReqRefY() );
	}
	
	
	public static void allocateChildYAligned(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, int alignmentFlags, double regionY, LAllocV regionAllocV)
	{
		allocateChildYAligned( childAllocation, childRequisition, ElementAlignment.getVAlignment( alignmentFlags ), regionY, regionAllocV );
	}
	
	public static void allocateChildYAligned(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, VAlignment vAlign, double regionY, LAllocV regionAllocV)
	{
		if ( vAlign == VAlignment.REFY_EXPAND )
		{
			double childHeight = Math.max( childRequisition.getReqHeight(), regionAllocV.getHeight() );
			childAllocation.setPositionInParentAllocationSpaceAndAllocationY( regionY, childHeight, regionAllocV.getRefY() );
		}
		else if ( vAlign == VAlignment.REFY )
		{
			double offset = regionAllocV.getRefY() - childRequisition.getReqRefY();
			childAllocation.setPositionInParentAllocationSpaceAndAllocationY( regionY + offset, childRequisition.getReqHeight(), childRequisition.getReqRefY() );
		}
		else if ( vAlign == VAlignment.EXPAND )
		{
			double childHeight = Math.max( childRequisition.getReqHeight(), regionAllocV.getHeight() );
			double delta = Math.max( regionAllocV.getHeight() - childRequisition.getReqHeight(), 0.0 );
			childAllocation.setPositionInParentAllocationSpaceAndAllocationY( regionY, childHeight, childRequisition.getReqRefY() + delta * 0.5 );
		}
		else if ( vAlign == VAlignment.TOP )
		{
			childAllocation.setPositionInParentAllocationSpaceAndAllocationY( regionY, childRequisition.getReqHeight(), childRequisition.getReqRefY() );
		}
		else if ( vAlign == VAlignment.CENTRE )
		{
			double delta = Math.max( regionAllocV.getHeight() - childRequisition.getReqHeight(), 0.0 );
			childAllocation.setPositionInParentAllocationSpaceAndAllocationY( regionY + delta * 0.5, childRequisition.getReqHeight(), childRequisition.getReqRefY() );
		}
		else if ( vAlign == VAlignment.BOTTOM )
		{
			double delta = Math.max( regionAllocV.getHeight() - childRequisition.getReqHeight(), 0.0 );
			childAllocation.setPositionInParentAllocationSpaceAndAllocationY( regionY + delta, childRequisition.getReqHeight(), childRequisition.getReqRefY() );
		}
	}
}
