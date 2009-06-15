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
	protected DPWidget element;
	protected double positionInParentSpaceX, positionInParentSpaceY;
	protected double allocationX, allocationY;

	
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
		positionInParentSpaceX = positionInParentSpaceY = allocationX = allocationY = 0.0;
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
	
	public Vector2 getAllocation()
	{
		return new Vector2( allocationX, allocationY );
	}


	
	public void setAllocationX(double allocation)
	{
		allocationX = allocation;
	}
	
	public void setAllocationY(double allocation)
	{
		allocationY = allocation;
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
		allocationY = box.allocationY;
		positionInParentSpaceX = box.positionInParentSpaceX;
		positionInParentSpaceY = box.positionInParentSpaceY;
	}
	
	
	public void allocateChildX(LAllocBox child, double localPosX, double localWidth)
	{
		child.allocationX = localWidth;
		child.positionInParentSpaceX = localPosX;
	}
	
	public void allocateChildY(LAllocBox child, double localPosY, double localHeight)
	{
		child.allocationY = localHeight;
		child.positionInParentSpaceY = localPosY;
	}
	
	
	public void allocateChildX(LAllocBox child)
	{
		child.allocationX = allocationX;
		child.positionInParentSpaceX = 0.0;
	}
	
	public void allocateChildY(LAllocBox child)
	{
		child.allocationY = allocationY;
		child.positionInParentSpaceY = 0.0;
	}
	
	
	protected void allocateChildSpaceX(LAllocBox child, double localWidth)
	{
		child.allocationX = localWidth;
	}
	
	protected void allocateChildSpaceY(LAllocBox child, double localHeight)
	{
		child.allocationY = localHeight;
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
					allocationX == b.allocationX  &&  allocationY == b.allocationY;
		}
		else
		{
			return false;
		}
	}
	
	
	public String toString()
	{
		return "LAllocBox( positionInParentSpaceX=" + positionInParentSpaceX + ", positionInParentSpaceY=" + positionInParentSpaceY +
			", allocationX=" + allocationX + ", allocationY=" + allocationY + ")";
	}
}
