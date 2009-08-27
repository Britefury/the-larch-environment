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
	
	
	public DPWidget getElement()
	{
		return element;
	}
	
	
	
	public void clear()
	{
		positionInParentSpaceX = positionInParentSpaceY = allocationX = allocationAscent = allocationDescent = 0.0;
		bHasBaseline = false;
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
	
	public boolean hasBaseline()
	{
		return bHasBaseline;
	}
	
	public LAllocV getAllocV()
	{
		return new LAllocV( allocationAscent, allocationDescent, bHasBaseline );
	}
	
	public Vector2 getAllocation()
	{
		return new Vector2( allocationX, allocationAscent + allocationDescent );
	}


	
	public void setAllocationX(double allocation)
	{
		allocationX = allocation;
	}
	
	public void setAllocationY(double allocation)
	{
		this.allocationAscent = allocation;
		this.allocationDescent = 0.0;
		bHasBaseline = false;
	}
	
	public void setAllocationY(double allocationAscent, double allocationDescent)
	{
		this.allocationAscent = allocationAscent;
		this.allocationDescent = allocationDescent;
		bHasBaseline = true;
	}
	
	public void setAllocationY(LAllocV alloc)
	{
		allocationAscent = alloc.ascent;
		allocationDescent = alloc.descent;
		bHasBaseline = alloc.bHasBaseline;
	}
	
	public void setAllocationY(LReqBox box)
	{
		allocationAscent = box.getReqAscent();
		allocationDescent = box.getReqDescent();
		bHasBaseline = box.hasBaseline();
		positionInParentSpaceY = 0.0;
	}

	
	public void setPositionInParentSpaceX(double x)
	{
		positionInParentSpaceX = x;
	}
	
	public void setPositionInParentSpaceY(double y)
	{
		positionInParentSpaceY = y;
	}
	

	public void setAllocation(LAllocBox box)
	{
		allocationX = box.allocationX;
		allocationAscent = box.allocationAscent;
		allocationDescent = box.allocationDescent;
		bHasBaseline = box.bHasBaseline;
		positionInParentSpaceX = box.positionInParentSpaceX;
		positionInParentSpaceY = box.positionInParentSpaceY;
	}
	
	
	public void allocateChildXAligned(LAllocBox child, LReqBox req, double localPosX, double localWidth)
	{
		double childWidth = req.getPrefWidth();
		if ( localWidth <= childWidth )
		{
			child.allocationX = localWidth;
			child.positionInParentSpaceX = localPosX;
		}
		else
		{
			HAlignment alignment = req.getHAlignment();
			if ( alignment == HAlignment.EXPAND )
			{
				child.allocationX = localWidth;
				child.positionInParentSpaceX = localPosX;
			}
			else
			{
				child.allocationX = childWidth;
				if ( alignment == HAlignment.LEFT )
				{
					child.positionInParentSpaceX = localPosX;
				}
				else if ( alignment == HAlignment.CENTRE )
				{
					child.positionInParentSpaceX = localPosX + ( localWidth - childWidth ) * 0.5;
				}
				else if ( alignment == HAlignment.RIGHT )
				{
					child.positionInParentSpaceX = localPosX + ( localWidth - childWidth );
				}
				else
				{
					throw new RuntimeException( "Invalid h-alignment" );
				}
			}
		}
	}
	
	public void allocateChildYAligned(LAllocBox child, LReqBox childReq, double localPosY, double localHeight)
	{
		double childHeight = childReq.getReqHeight();
		VAlignment alignment = childReq.getVAlignment();
		if ( alignment == VAlignment.BASELINES_EXPAND )
		{
			double topMargin = localPosY;
			double bottomMargin = Math.max( getAllocationY() - ( localPosY + localHeight ),  0.0 );
			if ( hasBaseline() )
			{
				if ( childReq.hasBaseline() )
				{
					double ascent = Math.max( allocationAscent - topMargin, childReq.getReqAscent() );
					double descent = Math.max( allocationDescent - bottomMargin, childReq.getReqDescent() );
					allocateChildY( child, localPosY, ascent, descent );
				}
				else
				{
					allocateChildY( child, 0.0, getAllocationY() );
				}
			}
			else
			{
				if ( childReq.hasBaseline() )
				{
					double delta = localHeight - childHeight;
					allocateChildY( child, localPosY, childReq.getReqAscent() + delta * 0.5, childReq.getReqDescent() + delta * 0.5 );
				}
				else
				{
					double ascent = localHeight  *  0.5;
					allocateChildY( child, localPosY, ascent, ascent );
				}
			}
		}
		else if ( alignment == VAlignment.BASELINES )
		{
			if ( hasBaseline() )
			{
				if ( childReq.hasBaseline() )
				{
					allocateChildY( child, allocationAscent - childReq.getReqAscent(), childReq.getReqAscent(), childReq.getReqDescent() );
				}
				else
				{
					allocateChildY( child, 0.0, childReq.getReqHeight() );
				}
			}
			else
			{
				double delta = localHeight - childHeight;
				double y = localPosY + delta * 0.5;
				if ( childReq.hasBaseline() )
				{
					allocateChildY( child, y, childReq.getReqAscent(), childReq.getReqDescent() );
				}
				else
				{
					double ascent = childHeight * 0.5;
					allocateChildY( child, y, ascent, ascent );
				}
			}
		}
		else if ( alignment == VAlignment.EXPAND )
		{
			allocateChildYByReq( child, localPosY, childReq, localHeight );
		}
		else if ( alignment == VAlignment.TOP )
		{
			allocateChildYByReq( child, localPosY, childReq );
		}
		else if ( alignment == VAlignment.CENTRE )
		{
			allocateChildYByReq( child, localPosY + ( localHeight - childHeight ) * 0.5, childReq );
		}
		else if ( alignment == VAlignment.BOTTOM )
		{
			allocateChildYByReq( child, localPosY + ( localHeight - childHeight ), childReq );
		}
	}
	
	
	public void allocateChildX(LAllocBox child, double localPosX, double localWidth)
	{
		child.allocationX = localWidth;
		child.positionInParentSpaceX = localPosX;
	}
	
	public void allocateChildY(LAllocBox child, double localPosY, double localHeight)
	{
		child.allocationAscent = localHeight;
		child.allocationDescent = 0.0;
		child.bHasBaseline = false;
		child.positionInParentSpaceY = localPosY;
	}
	
	public void allocateChildY(LAllocBox child, double localPosY, double localAscent, double localDescent)
	{
		child.allocationAscent = localAscent;
		child.allocationDescent = localDescent;
		child.bHasBaseline = true;
		child.positionInParentSpaceY = localPosY;
	}
	
	public void allocateChildYByReq(LAllocBox child, double localPosY, LReqBox childReq)
	{
		child.allocationAscent = childReq.reqAscent;
		child.allocationDescent = childReq.reqDescent;
		child.bHasBaseline = childReq.hasBaseline();
		child.positionInParentSpaceY = localPosY;
	}
	
	public void allocateChildYByReq(LAllocBox child, double localPosY, LReqBox childReq, double totalHeight)
	{
		if ( childReq.hasBaseline() )
		{
			double delta = totalHeight - childReq.getReqHeight();
			allocateChildY( child, localPosY, childReq.reqAscent, childReq.reqDescent + delta );
		}
		else
		{
			allocateChildY( child, localPosY, totalHeight );
		}
	}
	
	
	protected void allocateChildSpaceX(LAllocBox child, double localWidth)
	{
		child.allocationX = localWidth;
	}
	
	protected void allocateChildSpaceY(LAllocBox child, double localHeight)
	{
		child.allocationAscent = localHeight;
		child.allocationDescent = 0.0;
		child.bHasBaseline = false;
	}
	
	protected void allocateChildSpaceY(LAllocBox child, double localAscent, double localDescent)
	{
		child.allocationAscent = localAscent;
		child.allocationDescent = localDescent;
		child.bHasBaseline = true;
	}
	
	public void allocateChildSpaceYByReq(LAllocBox child, LReqBox childReq)
	{
		child.allocationAscent = childReq.reqAscent;
		child.allocationDescent = childReq.reqDescent;
		child.bHasBaseline = childReq.hasBaseline();
	}
	
	public void allocateChildSpaceYByReq(LAllocBox child, LReqBox childReq, double totalHeight)
	{
		if ( childReq.hasBaseline() )
		{
			double delta = ( totalHeight - childReq.getReqHeight() )  *  0.5;
			allocateChildSpaceY( child, childReq.reqAscent + delta, childReq.reqDescent + delta );
		}
		else
		{
			allocateChildSpaceY( child, totalHeight );
		}
	}
	
	
	protected void allocateChildPositionX(LAllocBox child, double localPosX)
	{
		child.positionInParentSpaceX = localPosX;
	}
	
	protected void allocateChildPositionY(LAllocBox child, double localPosY)
	{
		child.positionInParentSpaceY = localPosY;
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
