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

public class LAllocBox implements LAllocBoxInterface
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
	
	
	public LayoutNode getAllocLayoutNode()
	{
		return layoutNode;
	}
	
	
	
	public double getAllocPositionInParentSpaceX()
	{
		return positionInParentSpaceX;
	}
	
	public double getAllocPositionInParentSpaceY()
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
	
	public double getAllocRefY()
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


	
	
	
	//
	// SETTERS
	//
	
	public void setAllocPositionInParentSpaceX(double x)
	{
		positionInParentSpaceX = x;
	}
	
	public void setAllocPositionInParentSpaceY(double y)
	{
		positionInParentSpaceY = y;
	}
	
	public void setAllocationX(double width)
	{
		allocationX = width;
	}

	public void setAllocationY(double height, double refY)
	{
		allocationY = height;
		this.refY = refY;
	}

	public void setAllocation(double width, double height, double refY)
	{
		allocationX = width;
		allocationY = height;
		this.refY = refY;
	}

	public void setPositionInParentSpaceAndAllocationX(double x, double width)
	{
		positionInParentSpaceX = x;
		allocationX = width;
	}
	
	public void setPositionInParentSpaceAndAllocationY(double y, double height)
	{
		positionInParentSpaceY = y;
		allocationY = height;
		refY = height * 0.5;
	}
	
	public void setPositionInParentSpaceAndAllocationY(double y, double height, double refY)
	{
		positionInParentSpaceY = y;
		allocationY = height;
		this.refY = refY;
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
