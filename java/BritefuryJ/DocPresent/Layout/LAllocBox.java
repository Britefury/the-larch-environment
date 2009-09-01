//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class LAllocBox
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
	
	
	
	public void allocateX(LReqBox requisition, double x, double width)
	{
		positionInParentSpaceX = x;
		allocationX = Math.max( width, requisition.getMinWidth() );
	}
	
	public void allocateY(LReqBox requisition, double y, double height)
	{
		positionInParentSpaceY = y;
		double delta = Math.max( ( height - requisition.getReqHeight() )  *  0.5,  0.0 );
		allocationAscent = requisition.reqAscent  +  delta;
		allocationDescent = requisition.reqDescent  +  delta;
		bHasBaseline = false;
	}
	
	public void allocateY(LReqBox requisition, double y, double ascent, double descent)
	{
		positionInParentSpaceY = y;
		allocationAscent = Math.max( ascent, requisition.getReqAscent() );
		allocationDescent = Math.max( descent, requisition.getReqDescent() );
		bHasBaseline = true;
	}
	
	public void allocateY(LReqBox requisition, double y, LAllocV allocV)
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
	

	
	protected void allocateChildPositionX(LAllocBox childAllocation, double localPosX)
	{
		childAllocation.positionInParentSpaceX = localPosX;
	}
	
	protected void allocateChildWidth(LAllocBox childAllocation, double localWidth)
	{
		childAllocation.allocationX = localWidth;
	}
	
	protected void allocateChildX(LAllocBox childAllocation, double localPosX, double localWidth)
	{
		childAllocation.allocationX = localWidth;
		childAllocation.positionInParentSpaceX = localPosX;
	}
	
	public void allocateChildXAligned(LAllocBox childAllocation, LReqBox childRequisition, int alignmentFlags, double regionX, double regionWidth)
	{
		allocateChildXAligned( childAllocation, childRequisition, ElementAlignment.getHAlignment( alignmentFlags ), regionX, regionWidth );
	}
	
	public void allocateChildXAligned(LAllocBox childAllocation, LReqBox childRequisition, HAlignment hAlign, double regionX, double regionWidth)
	{
		double childWidth = childRequisition.getPrefWidth();
		if ( regionWidth <= childWidth )
		{
			childAllocation.allocationX = Math.max( regionWidth, childRequisition.getMinWidth() );
			childAllocation.positionInParentSpaceX = regionX;
		}
		else
		{
			if ( hAlign == HAlignment.EXPAND )
			{
				childAllocation.allocationX = regionWidth;
				childAllocation.positionInParentSpaceX = regionX;
			}
			else
			{
				childAllocation.allocationX = childWidth;
				if ( hAlign == HAlignment.LEFT )
				{
					childAllocation.positionInParentSpaceX = regionX;
				}
				else if ( hAlign == HAlignment.CENTRE )
				{
					childAllocation.positionInParentSpaceX = regionX + ( regionWidth - childWidth ) * 0.5;
				}
				else if ( hAlign == HAlignment.RIGHT )
				{
					childAllocation.positionInParentSpaceX = regionX + ( regionWidth - childWidth );
				}
				else
				{
					throw new RuntimeException( "Invalid h-alignment" );
				}
			}
		}
	}
	

	
	
	
	
	protected void allocateChildPositionY(LAllocBox childAllocation, double localPosY)
	{
		childAllocation.positionInParentSpaceY = localPosY;
	}
	
	protected void allocateChildHeightAsRequisition(LAllocBox childAllocation, LReqBox childRequisition)
	{
		childAllocation.allocationAscent = childRequisition.reqAscent;
		childAllocation.allocationDescent = childRequisition.reqDescent;
		childAllocation.bHasBaseline = childRequisition.hasBaseline();
	}
	
	protected void allocateChildHeightPaddedRequisition(LAllocBox childAllocation, LReqBox childRequisition, double totalHeight)
	{
		if ( childRequisition.hasBaseline() )
		{
			double padding = Math.max( ( totalHeight - childRequisition.getReqHeight() )  *  0.5,  0.0 );
			childAllocation.allocationAscent = childRequisition.reqAscent + padding;
			childAllocation.allocationDescent = childRequisition.reqDescent + padding;
			childAllocation.bHasBaseline = true;
		}
		else
		{
			childAllocation.allocationAscent = totalHeight * 0.5;
			childAllocation.allocationDescent = totalHeight * 0.5;
			childAllocation.bHasBaseline = false;
		}
	}
	
	
	protected void allocateChildYAsRequisition(LAllocBox childAllocation, LReqBox childRequisition, double localPosY)
	{
		childAllocation.allocationAscent = childRequisition.reqAscent;
		childAllocation.allocationDescent = childRequisition.reqDescent;
		childAllocation.bHasBaseline = childRequisition.hasBaseline();
		childAllocation.positionInParentSpaceY = localPosY;
	}
	
	protected void allocateChildYPaddedRequisition(LAllocBox childAllocation, LReqBox childRequisition, double localPosY, double localHeight)
	{
		if ( childRequisition.hasBaseline() )
		{
			double delta = Math.max( ( localHeight - childRequisition.getReqHeight() )  *  0.5,  0.0 );
			childAllocation.allocationAscent = childRequisition.reqAscent + delta;
			childAllocation.allocationDescent = childRequisition.reqDescent + delta;
			childAllocation.bHasBaseline = true;
			childAllocation.positionInParentSpaceY = localPosY;
		}
		else
		{
			childAllocation.allocationAscent = localHeight * 0.5;
			childAllocation.allocationDescent = localHeight * 0.5;
			childAllocation.bHasBaseline = false;
			childAllocation.positionInParentSpaceY = localPosY;
		}
	}
	
	protected void allocateChildYPaddedRequisition(LAllocBox childAllocation, LReqBox childRequisition, double localPosY, double localAscent, double localDescent)
	{
		if ( childRequisition.hasBaseline() )
		{
			childAllocation.allocationAscent = localAscent;
			childAllocation.allocationDescent = localDescent;
			childAllocation.bHasBaseline = true;
			childAllocation.positionInParentSpaceY = localPosY;
		}
		else
		{
			double localHeight = localAscent + localDescent;
			childAllocation.allocationAscent = localHeight * 0.5;
			childAllocation.allocationDescent = localHeight * 0.5;
			childAllocation.bHasBaseline = false;
			childAllocation.positionInParentSpaceY = localPosY;
		}
	}
	
	
	public void allocateChildYAligned(LAllocBox childAllocation, LReqBox childRequisition, int alignmentFlags, double regionY, LAllocV regionAllocV)
	{
		allocateChildYAligned( childAllocation, childRequisition, ElementAlignment.getVAlignment( alignmentFlags ), regionY, regionAllocV );
	}
	
	public void allocateChildYAligned(LAllocBox childAllocation, LReqBox childRequisition, VAlignment vAlign, double regionY, LAllocV regionAllocV)
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
