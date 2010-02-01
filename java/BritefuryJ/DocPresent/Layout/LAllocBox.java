//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.LayoutTree.LayoutNode;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class LAllocBox extends LAllocBoxInterface
{
	protected double positionInParentSpaceX, positionInParentSpaceY;
	protected double allocationX, allocationAscent, allocationDescent;
	protected DPWidget element;
	protected boolean bHasBaseline;

	
	public LAllocBox(DPWidget element)
	{
		this.element = element;
	}
	
	public LAllocBox(double x, double y, double width, double ascent, double descent, DPWidget element, boolean bHasBaseline)
	{
		positionInParentSpaceX = x;
		positionInParentSpaceY = y;
		allocationX = width;
		allocationAscent = ascent;
		allocationDescent = descent;
		this.element = element;
		this.bHasBaseline = bHasBaseline;
	}
	
	
	public DPWidget getElement()
	{
		return element;
	}
	
	public LayoutNode getLayoutNode()
	{
		return element.getLayoutNode();
	}
	
	
	
	public void clear()
	{
		positionInParentSpaceX = positionInParentSpaceY = allocationX = allocationAscent = allocationDescent = 0.0;
		bHasBaseline = false;
	}
	
	public void applyBorder(double leftMargin, double rightMargin, double topMargin, double bottomMargin)
	{
		positionInParentSpaceX += leftMargin;
		allocationX -= leftMargin + rightMargin;
		positionInParentSpaceY += topMargin;
		allocationAscent -= topMargin;
		allocationDescent -= bottomMargin;
	}
	
	public void applyBorderX(double leftMargin, double rightMargin)
	{
		positionInParentSpaceX += leftMargin;
		allocationX -= leftMargin + rightMargin;
	}
	
	public void applyBorderY(double topMargin, double bottomMargin)
	{
		positionInParentSpaceY += topMargin;
		allocationAscent -= topMargin;
		allocationDescent -= bottomMargin;
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
	
	public double getAllocationAscent()
	{
		return allocationAscent;
	}
	
	public double getAllocationDescent()
	{
		return allocationDescent;
	}
	
	public double getAllocationY()
	{
		return allocationAscent + allocationDescent;
	}
	
	public LAllocV getAllocV()
	{
		return new LAllocV( allocationAscent, allocationDescent, bHasBaseline );
	}
	
	public Vector2 getAllocation()
	{
		return new Vector2( allocationX, allocationAscent + allocationDescent );
	}

	public boolean hasBaseline()
	{
		return bHasBaseline;
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
		double delta = Math.max( ( height - requisition.getReqHeight() )  *  0.5,  0.0 );
		allocationAscent = requisition.getReqAscent()  +  delta;
		allocationDescent = requisition.getReqDescent()  +  delta;
		bHasBaseline = false;
	}
	
	public void allocateY(LReqBoxInterface requisition, double y, double ascent, double descent)
	{
		positionInParentSpaceY = y;
		allocationAscent = Math.max( ascent, requisition.getReqAscent() );
		allocationDescent = Math.max( descent, requisition.getReqDescent() );
		bHasBaseline = true;
	}
	
	public void allocateY(LReqBoxInterface requisition, double y, LAllocV allocV)
	{
		positionInParentSpaceY = y;
		allocationAscent = Math.max( allocV.ascent, requisition.getReqAscent() );
		allocationDescent = Math.max( allocV.descent, requisition.getReqDescent() );
		bHasBaseline = allocV.bHasBaseline;
	}

	
	public void allocateX(LAllocBox box)
	{
		allocationX = box.allocationX;
	}
	
	public void allocateY(LAllocBox box)
	{
		allocationAscent = box.allocationAscent;
		allocationDescent = box.allocationDescent;
		bHasBaseline = box.bHasBaseline;
	}
	
	public void allocateSize(LAllocBox box)
	{
		allocationX = box.allocationX;
		allocationAscent = box.allocationAscent;
		allocationDescent = box.allocationDescent;
		bHasBaseline = box.bHasBaseline;
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
		childAllocation.setAllocationY( childRequisition.getReqAscent(), childRequisition.getReqDescent(), childRequisition.hasBaseline() );
	}
	
	protected void allocateChildHeightPaddedRequisition(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, double totalHeight)
	{
		if ( childRequisition.hasBaseline() )
		{
			double padding = Math.max( ( totalHeight - childRequisition.getReqHeight() )  *  0.5,  0.0 );
			childAllocation.setAllocationY( childRequisition.getReqAscent() + padding, childRequisition.getReqDescent() + padding );
		}
		else
		{
			childAllocation.setAllocationY( totalHeight );
		}
	}
	
	
	protected void allocateChildYAsRequisition(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, double localPosY)
	{
		childAllocation.setPositionInParentSpaceAndAllocationY( localPosY, childRequisition.getReqAscent(), childRequisition.getReqDescent(), childRequisition.hasBaseline() );
	}
	
	protected void allocateChildYPaddedRequisition(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, double localPosY, double localHeight)
	{
		if ( childRequisition.hasBaseline() )
		{
			double delta = Math.max( ( localHeight - childRequisition.getReqHeight() )  *  0.5,  0.0 );
			childAllocation.setPositionInParentSpaceAndAllocationY( localPosY, childRequisition.getReqAscent() + delta, childRequisition.getReqDescent() + delta );
		}
		else
		{
			childAllocation.setPositionInParentSpaceAndAllocationY( localPosY, localHeight );
		}
	}
	
	protected void allocateChildYPaddedRequisition(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, double localPosY, double localAscent, double localDescent)
	{
		if ( childRequisition.hasBaseline() )
		{
			childAllocation.setPositionInParentSpaceAndAllocationY( localPosY, localAscent, localDescent );
		}
		else
		{
			double localHeight = localAscent + localDescent;
			childAllocation.setPositionInParentSpaceAndAllocationY( localPosY, localHeight );
		}
	}
	
	
	public void allocateChildYAligned(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, int alignmentFlags, double regionY, LAllocV regionAllocV)
	{
		allocateChildYAligned( childAllocation, childRequisition, ElementAlignment.getVAlignment( alignmentFlags ), regionY, regionAllocV );
	}
	
	public void allocateChildYAligned(LAllocBoxInterface childAllocation, LReqBoxInterface childRequisition, VAlignment vAlign, double regionY, LAllocV regionAllocV)
	{
		if ( vAlign == VAlignment.BASELINES_EXPAND )
		{
			if ( regionAllocV.hasBaseline() )
			{
				allocateChildYPaddedRequisition( childAllocation, childRequisition, regionY, regionAllocV.ascent, regionAllocV.descent );
			}
			else
			{
				allocateChildYPaddedRequisition( childAllocation, childRequisition, regionY, regionAllocV.getHeight() );
			}
		}
		else if ( vAlign == VAlignment.BASELINES )
		{
			double offsetY;
			if ( regionAllocV.hasBaseline()  &&  childRequisition.hasBaseline() )
			{
				offsetY = regionAllocV.ascent - childRequisition.getReqAscent();
			}
			else
			{
				offsetY = ( regionAllocV.getHeight() - childRequisition.getReqHeight() )  *  0.5;
			}
			allocateChildYAsRequisition( childAllocation, childRequisition, regionY + offsetY );
		}
		else if ( vAlign == VAlignment.EXPAND )
		{
			allocateChildYPaddedRequisition( childAllocation, childRequisition, regionY, regionAllocV.getHeight());
		}
		else if ( vAlign == VAlignment.TOP )
		{
			allocateChildYAsRequisition( childAllocation, childRequisition, regionY );
		}
		else if ( vAlign == VAlignment.CENTRE )
		{
			allocateChildYAsRequisition( childAllocation, childRequisition, regionY + ( regionAllocV.getHeight() - childRequisition.getReqHeight() ) * 0.5 );
		}
		else if ( vAlign == VAlignment.BOTTOM )
		{
			allocateChildYAsRequisition( childAllocation, childRequisition, regionY + ( regionAllocV.getHeight() - childRequisition.getReqHeight() ) );
		}
	}
	
	
	public void scaleAllocationX(double scale)
	{
		allocationX *= scale;
	}

	public void scaleAllocationY(double scale)
	{
		allocationAscent *= scale;
		allocationDescent *= scale;
	}
	
	
	
	//
	// SETTERS
	//
	
	protected void setPositionInParentSpaceX(double x)
	{
		positionInParentSpaceX = x;
	}
	
	protected void setPositionInParentSpaceY(double y)
	{
		positionInParentSpaceY = y;
	}
	
	protected void setAllocationX(double width)
	{
		allocationX = width;
	}

	protected void setAllocationY(double height)
	{
		allocationAscent = allocationDescent = height * 0.5;
		bHasBaseline = false;
	}

	protected void setAllocationY(double ascent, double descent)
	{
		allocationAscent = ascent;
		allocationDescent = descent;
		bHasBaseline = true;
	}

	protected void setAllocationY(double ascent, double descent, boolean bHasBaseline)
	{
		allocationAscent = ascent;
		allocationDescent = descent;
		this.bHasBaseline = bHasBaseline;
	}

	protected void setPositionInParentSpaceAndAllocationX(double x, double width)
	{
		positionInParentSpaceX = x;
		allocationX = width;
	}
	
	protected void setPositionInParentSpaceAndAllocationY(double y, double height)
	{
		positionInParentSpaceY = y;
		allocationAscent = allocationDescent = height * 0.5;
		bHasBaseline = false;
	}
	
	protected void setPositionInParentSpaceAndAllocationY(double y, double ascent, double descent)
	{
		positionInParentSpaceY = y;
		allocationAscent = ascent;
		allocationDescent = descent;
		bHasBaseline = true;
	}

	protected void setPositionInParentSpaceAndAllocationY(double y, double ascent, double descent, boolean bHasBaseline)
	{
		positionInParentSpaceY = y;
		allocationAscent = ascent;
		allocationDescent = descent;
		this.bHasBaseline = bHasBaseline;
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
					allocationX == b.allocationX  &&  allocationAscent == b.allocationAscent  &&  allocationDescent == b.allocationDescent  &&  bHasBaseline == b.bHasBaseline;
		}
		else
		{
			return false;
		}
	}
	
	
	public String toString()
	{
		return "LAllocBox( positionInParentSpaceX=" + positionInParentSpaceX + ", positionInParentSpaceY=" + positionInParentSpaceY +
			", allocationX=" + allocationX + ", allocationAscent=" + allocationAscent + ", allocationDescent=" + allocationDescent + ", bHasBaseline=" + bHasBaseline + " )";
	}
}
