//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

import BritefuryJ.DocPresent.LayoutTree.LayoutNode;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class LAllocBox extends LAllocBoxInterface
{
	protected double positionInParentSpaceX, positionInParentSpaceY;
	protected double allocationX, allocationY;
	protected double refY;
	protected LayoutNode layoutNode;

	
	public LAllocBox(LayoutNode layoutNode)
	{
		this.layoutNode = layoutNode;
	}
	
	public LAllocBox(double x, double y, double width, double height, double refY, LayoutNode layoutNode)
	{
		positionInParentSpaceX = x;
		positionInParentSpaceY = y;
		allocationX = width;
		allocationY = height;
		this.refY = refY;
		this.layoutNode = layoutNode;
	}
	
	
	public LayoutNode getLayoutNode()
	{
		return layoutNode;
	}
	
	
	
	public void clear()
	{
		positionInParentSpaceX = positionInParentSpaceY = allocationX = allocationY = refY = 0.0;
	}
	
	public void applyBorder(double leftMargin, double rightMargin, double topMargin, double bottomMargin)
	{
		positionInParentSpaceX += leftMargin;
		allocationX -= leftMargin + rightMargin;
		positionInParentSpaceY += topMargin;
		allocationY -= topMargin + bottomMargin;
		refY += topMargin;
	}
	
	public void applyBorderX(double leftMargin, double rightMargin)
	{
		positionInParentSpaceX += leftMargin;
		allocationX -= leftMargin + rightMargin;
	}
	
	public void applyBorderY(double topMargin, double bottomMargin)
	{
		positionInParentSpaceY += topMargin;
		allocationY -= topMargin + bottomMargin;
		refY += topMargin;
	}
	
	
	
	public double getPositionInParentSpaceX()
	{
		return positionInParentSpaceX;
	}
	
	public double getPositionInParentSpaceY()
	{
		return positionInParentSpaceY;
	}
	
	public Point2 getPositionInParentSpace()
	{
		return new Point2( positionInParentSpaceX, positionInParentSpaceY );
	}
	
	public double getAllocationX()
	{
		return allocationX;
	}
	
	public double getAllocationY()
	{
		return allocationY;
	}
	
	public double getRefY()
	{
		return refY;
	}
	
	public LAllocV getAllocV()
	{
		return new LAllocV( allocationY, refY );
	}
	
	public Vector2 getAllocation()
	{
		return new Vector2( allocationX, allocationY );
	}
	
	public Point2 getRefPoint()
	{
		return new Point2( 0.0, refY );
	}


	
	
	
	protected void clearPositionX()
	{
		positionInParentSpaceX = 0.0;
	}
	
	protected void clearPositionY()
	{
		positionInParentSpaceY = 0.0;
	}
	
	
	
	public void allocateX(LReqBoxInterface requisition, double x, double width)
	{
		positionInParentSpaceX = x;
		allocationX = Math.max( width, requisition.getMinWidth() );
	}
	
	public void allocateY(LReqBoxInterface requisition, double y, double height)
	{
		positionInParentSpaceY = y;
		allocationY = Math.max( height, requisition.getReqHeight() );
		double delta = Math.max( ( height - requisition.getReqHeight() )  *  0.5,  0.0 );
		refY = requisition.getRefY() + delta;
	}
	
	public void allocateY(LReqBoxInterface requisition, double y, double height, double refY)
	{
		positionInParentSpaceY = y;
		allocationY = Math.max( height, requisition.getReqHeight() );
		double delta = Math.max( ( height - requisition.getReqHeight() )  *  0.5,  0.0 );
		this.refY = refY + delta;
	}
	
	public void allocateY(LReqBoxInterface requisition, double y, LAllocV allocV)
	{
		positionInParentSpaceY = y;
		allocationY = Math.max( allocV.height, requisition.getReqHeight() );
		refY = Math.max( allocV.refY, requisition.getRefY() );
	}

	
	public void allocateX(LAllocBox box)
	{
		allocationX = box.allocationX;
	}
	
	public void allocateY(LAllocBox box)
	{
		allocationY = box.allocationY;
		refY = box.refY;
	}
	
	public void allocateSize(LAllocBox box)
	{
		allocationX = box.allocationX;
		allocationY = box.allocationY;
		refY = box.refY;
	}
	

	
	protected void allocateChildPositionX(LAllocBoxInterface childAllocation, double localPosX)
	{
		childAllocation.setPositionInParentSpaceX( localPosX );
	}
	
	protected void allocateChildWidth(LAllocBoxInterface childAllocation, double localWidth)
	{
		childAllocation.setAllocationX( localWidth );
	}
	
	protected void allocateChildX(LAllocBoxInterface childAllocation, double localPosX, double localWidth)
	{
		childAllocation.setPositionInParentSpaceAndAllocationX( localPosX, localWidth );
	}
	
	public void allocateChildXAligned(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, int alignmentFlags, double regionX, double regionWidth)
	{
		allocateChildXAligned( childAllocation, childRequisition, ElementAlignment.getHAlignment( alignmentFlags ), regionX, regionWidth );
	}
	
	public void allocateChildXAligned(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, HAlignment hAlign, double regionX, double regionWidth)
	{
		double childWidth = childRequisition.getPrefWidth();
		if ( regionWidth <= childWidth )
		{
			childAllocation.setPositionInParentSpaceAndAllocationX( regionX, Math.max( regionWidth, childRequisition.getMinWidth() ) );
		}
		else
		{
			if ( hAlign == HAlignment.EXPAND )
			{
				childAllocation.setPositionInParentSpaceAndAllocationX( regionX, regionWidth );
			}
			else
			{
				if ( hAlign == HAlignment.LEFT )
				{
					childAllocation.setPositionInParentSpaceAndAllocationX( regionX, childWidth );
				}
				else if ( hAlign == HAlignment.CENTRE )
				{
					childAllocation.setPositionInParentSpaceAndAllocationX( regionX + ( regionWidth - childWidth ) * 0.5, childWidth );
				}
				else if ( hAlign == HAlignment.RIGHT )
				{
					childAllocation.setPositionInParentSpaceAndAllocationX( regionX + ( regionWidth - childWidth ), childWidth );
				}
				else
				{
					throw new RuntimeException( "Invalid h-alignment" );
				}
			}
		}
	}
	

	
	
	
	
	protected void allocateChildPositionY(LAllocBoxInterface childAllocation, double localPosY)
	{
		childAllocation.setPositionInParentSpaceY( localPosY );
	}
	
	protected void allocateChildHeightAsRequisition(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition)
	{
		childAllocation.setAllocationY( childRequisition.getReqHeight(), childRequisition.getRefY() );
	}
	
	protected void allocateChildHeightPaddedRequisition(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, double totalHeight)
	{
		double totalPadding = Math.max( totalHeight - childRequisition.getReqHeight(),  0.0 );
		double padding = totalPadding * 0.5;
		childAllocation.setAllocationY( childRequisition.getReqHeight() + totalPadding, childRequisition.getRefY() + padding );
	}
	
	protected void allocateChildHeight(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, double height, double refY)
	{
		childAllocation.setAllocationY( height, refY );
	}
	
	
	protected void allocateChildYAsRequisition(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, double localPosY)
	{
		childAllocation.setPositionInParentSpaceAndAllocationY( localPosY, childRequisition.getReqHeight(), childRequisition.getRefY() );
	}
	
	
	public void allocateChildYAligned(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, int alignmentFlags, double regionY, LAllocV regionAllocV)
	{
		allocateChildYAligned( childAllocation, childRequisition, ElementAlignment.getVAlignment( alignmentFlags ), regionY, regionAllocV );
	}
	
	public void allocateChildYAligned(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, VAlignment vAlign, double regionY, LAllocV regionAllocV)
	{
		if ( vAlign == VAlignment.REFY_EXPAND )
		{
			double childHeight = Math.max( childRequisition.getReqHeight(), regionAllocV.getHeight() );
			childAllocation.setPositionInParentSpaceAndAllocationY( regionY, childHeight, regionAllocV.getRefY() );
		}
		else if ( vAlign == VAlignment.REFY )
		{
			double offset = regionAllocV.getRefY() - childRequisition.getRefY();
			childAllocation.setPositionInParentSpaceAndAllocationY( regionY + offset, childRequisition.getReqHeight(), childRequisition.getRefY() );
		}
		else if ( vAlign == VAlignment.EXPAND )
		{
			double childHeight = Math.max( childRequisition.getReqHeight(), regionAllocV.getHeight() );
			double delta = Math.max( regionAllocV.getHeight() - childRequisition.getReqHeight(), 0.0 );
			childAllocation.setPositionInParentSpaceAndAllocationY( regionY, childHeight, childRequisition.getRefY() + delta * 0.5 );
		}
		else if ( vAlign == VAlignment.TOP )
		{
			childAllocation.setPositionInParentSpaceAndAllocationY( regionY, childRequisition.getReqHeight(), childRequisition.getRefY() );
		}
		else if ( vAlign == VAlignment.CENTRE )
		{
			double delta = Math.max( regionAllocV.getHeight() - childRequisition.getReqHeight(), 0.0 );
			childAllocation.setPositionInParentSpaceAndAllocationY( regionY + delta * 0.5, childRequisition.getReqHeight(), childRequisition.getRefY() );
		}
		else if ( vAlign == VAlignment.BOTTOM )
		{
			double delta = Math.max( regionAllocV.getHeight() - childRequisition.getReqHeight(), 0.0 );
			childAllocation.setPositionInParentSpaceAndAllocationY( regionY + delta, childRequisition.getReqHeight(), childRequisition.getRefY() );
		}
	}
	
	
	public void scaleAllocationX(double scale)
	{
		allocationX *= scale;
	}

	public void scaleAllocationY(double scale)
	{
		allocationY *= scale;
		refY *= scale;
	}
	
	
	
	//
	// SETTERS
	//
	
	public void setPositionInParentSpaceX(double x)
	{
		positionInParentSpaceX = x;
	}
	
	public void setPositionInParentSpaceY(double y)
	{
		positionInParentSpaceY = y;
	}
	
	public void setAllocationX(double width)
	{
		allocationX = width;
	}

	public void setAllocationY(double height)
	{
		allocationY = height;
		refY = height * 0.5;
	}

	public void setAllocationY(double height, double refY)
	{
		allocationY = height;
		this.refY = refY;
	}

	protected void setPositionInParentSpaceAndAllocationX(double x, double width)
	{
		positionInParentSpaceX = x;
		allocationX = width;
	}
	
	protected void setPositionInParentSpaceAndAllocationY(double y, double height)
	{
		positionInParentSpaceY = y;
		allocationY = height;
		refY = height * 0.5;
	}
	
	protected void setPositionInParentSpaceAndAllocationY(double y, double height, double refY)
	{
		positionInParentSpaceY = y;
		allocationY = height;
		this.refY = refY;
	}



	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof LAllocBox )
		{
			LAllocBox b = (LAllocBox)x;
			
			return positionInParentSpaceX == b.positionInParentSpaceX  &&  positionInParentSpaceY == b.positionInParentSpaceY  &&  
					allocationX == b.allocationX  &&  allocationY == b.allocationY  &&  refY == b.refY;
		}
		else
		{
			return false;
		}
	}
	
	
	public String toString()
	{
		return "LAllocBox( positionInParentSpaceX=" + positionInParentSpaceX + ", positionInParentSpaceY=" + positionInParentSpaceY +
			", allocationX=" + allocationX + ", allocationY=" + allocationY + ", refY=" + refY + " )";
	}
}
