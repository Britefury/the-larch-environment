//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class LAllocHelper
{
	public static void allocateX(LAllocBoxInterface alloc, LReqBoxInterface requisition, double x, double allocX)
	{
		double width = Math.max( allocX, requisition.getReqMinWidth() );
		alloc.setPositionInParentSpaceAndAllocationX( x, allocX, width );
	}
	
	public static void allocateY(LAllocBoxInterface alloc, LReqBoxInterface requisition, double y, double height)
	{
		double h = Math.max( height, requisition.getReqHeight() );
		double delta = Math.max( ( height - requisition.getReqHeight() )  *  0.5,  0.0 );
		double refY = requisition.getReqRefY() + delta;
		
		alloc.setPositionInParentSpaceAndAllocationY( y, h, refY );
	}
	
	public static void allocateY(LAllocBoxInterface alloc, LReqBoxInterface requisition, double y, double height, double refY)
	{
		double h = Math.max( height, requisition.getReqHeight() );
		double delta = Math.max( ( height - requisition.getReqHeight() )  *  0.5,  0.0 );
		refY += delta;
		
		alloc.setPositionInParentSpaceAndAllocationY( y, h, refY );
	}
	
	public static void allocateY(LAllocBoxInterface alloc, LReqBoxInterface requisition, double y, LAllocV allocV)
	{
		double h = Math.max( allocV.height, requisition.getReqHeight() );
		double refY = Math.max( allocV.refY, requisition.getReqRefY() );
		
		alloc.setPositionInParentSpaceAndAllocationY( y, h, refY );
	}

	
	public static void allocateX(LAllocBoxInterface alloc, LAllocBoxInterface box)
	{
		alloc.setAllocationX( box.getAllocationX(), box.getWidth() );
	}
	
	public static void allocateY(LAllocBoxInterface alloc, LAllocBoxInterface box)
	{
		alloc.setAllocationY( box.getAllocationY(), box.getAllocRefY() );
	}
	

	
	
	
	
	protected static void allocateChildX(LAllocBoxInterface childAllocation, double localPosX, double localAllocX, double localWidth)
	{
		childAllocation.setPositionInParentSpaceAndAllocationX( localPosX, localAllocX, localWidth );
	}
	
	public static void allocateChildXAligned(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, int alignmentFlags, double regionX, double regionWidth)
	{
		allocateChildXAligned( childAllocation, childRequisition, ElementAlignment.getHAlignment( alignmentFlags ), regionX, regionWidth );
	}
	
	public static void allocateChildXAligned(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, HAlignment hAlign, double regionX, double regionAllocX)
	{
		double childWidth = childRequisition.getReqPrefWidth();
		if ( regionAllocX <= childWidth )
		{
			childAllocation.setPositionInParentSpaceAndAllocationX( regionX, regionAllocX, Math.max( regionAllocX, childRequisition.getReqMinWidth() ) );
		}
		else
		{
			if ( hAlign == HAlignment.EXPAND )
			{
				childAllocation.setPositionInParentSpaceAndAllocationX( regionX, regionAllocX, regionAllocX );
			}
			else
			{
				if ( hAlign == HAlignment.LEFT  ||  hAlign == HAlignment.PACK )
				{
					childAllocation.setPositionInParentSpaceAndAllocationX( regionX, childWidth, childWidth );
				}
				else if ( hAlign == HAlignment.CENTRE )
				{
					childAllocation.setPositionInParentSpaceAndAllocationX( regionX + ( regionAllocX - childWidth ) * 0.5, childWidth, childWidth );
				}
				else if ( hAlign == HAlignment.RIGHT )
				{
					childAllocation.setPositionInParentSpaceAndAllocationX( regionX + ( regionAllocX - childWidth ), childWidth, childWidth );
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
		childAllocation.setAllocPositionInParentSpaceY( localPosY );
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
		childAllocation.setPositionInParentSpaceAndAllocationY( localPosY, childRequisition.getReqHeight(), childRequisition.getReqRefY() );
	}
	
	protected static void allocateChildYAsPaddedRequisition(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, double localPosY, double totalHeight)
	{
		double totalPadding = Math.max( totalHeight - childRequisition.getReqHeight(),  0.0 );
		double padding = totalPadding * 0.5;
		childAllocation.setPositionInParentSpaceAndAllocationY( localPosY, childRequisition.getReqHeight() + totalPadding, childRequisition.getReqRefY() + padding );
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
			childAllocation.setPositionInParentSpaceAndAllocationY( regionY, childHeight, regionAllocV.getRefY() );
		}
		else if ( vAlign == VAlignment.REFY )
		{
			double offset = regionAllocV.getRefY() - childRequisition.getReqRefY();
			childAllocation.setPositionInParentSpaceAndAllocationY( regionY + offset, childRequisition.getReqHeight(), childRequisition.getReqRefY() );
		}
		else if ( vAlign == VAlignment.EXPAND )
		{
			double childHeight = Math.max( childRequisition.getReqHeight(), regionAllocV.getHeight() );
			double delta = Math.max( regionAllocV.getHeight() - childRequisition.getReqHeight(), 0.0 );
			childAllocation.setPositionInParentSpaceAndAllocationY( regionY, childHeight, childRequisition.getReqRefY() + delta * 0.5 );
		}
		else if ( vAlign == VAlignment.TOP )
		{
			childAllocation.setPositionInParentSpaceAndAllocationY( regionY, childRequisition.getReqHeight(), childRequisition.getReqRefY() );
		}
		else if ( vAlign == VAlignment.CENTRE )
		{
			double delta = Math.max( regionAllocV.getHeight() - childRequisition.getReqHeight(), 0.0 );
			childAllocation.setPositionInParentSpaceAndAllocationY( regionY + delta * 0.5, childRequisition.getReqHeight(), childRequisition.getReqRefY() );
		}
		else if ( vAlign == VAlignment.BOTTOM )
		{
			double delta = Math.max( regionAllocV.getHeight() - childRequisition.getReqHeight(), 0.0 );
			childAllocation.setPositionInParentSpaceAndAllocationY( regionY + delta, childRequisition.getReqHeight(), childRequisition.getReqRefY() );
		}
		else
		{
			throw new RuntimeException( "Invalid v-align" );
		}
	}
}
