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
import BritefuryJ.Math.Xform2;

public class LAllocBox implements LAllocBoxInterface
{
	protected double positionInParentAllocationSpaceX, positionInParentAllocationSpaceY;
	protected double width, allocationX, allocationY;
	protected double refY;
	protected LayoutNode layoutNode;

	
	public LAllocBox(LayoutNode layoutNode)
	{
		this.layoutNode = layoutNode;
	}
	
	public LAllocBox(double x, double y, double width, double height, double refY, LayoutNode layoutNode)
	{
		positionInParentAllocationSpaceX = x;
		positionInParentAllocationSpaceY = y;
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
		return positionInParentAllocationSpaceX;
	}
	
	public double getAllocPositionInParentSpaceY()
	{
		return positionInParentAllocationSpaceY;
	}
	
	public Point2 getPositionInParentSpace()
	{
		return new Point2( positionInParentAllocationSpaceX, positionInParentAllocationSpaceY );
	}
	
	public double getWidth()
	{
		return width;
	}
	
	public double getHeight()
	{
		return allocationY;
	}
	
	public Vector2 getSize()
	{
		return new Vector2( width, allocationY );
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
		positionInParentAllocationSpaceX = x;
	}
	
	public void setAllocPositionInParentSpaceY(double y)
	{
		positionInParentAllocationSpaceY = y;
	}
	
	public void setAllocationX(double allocX, double width)
	{
		allocationX = allocX;
		this.width = width;
	}

	public void setAllocationY(double height, double refY)
	{
		allocationY = height;
		this.refY = refY;
	}

	public void setPositionInParentSpaceAndAllocationX(double x, double allocX, double width)
	{
		positionInParentAllocationSpaceX = x;
		allocationX = allocX;
		this.width = width;
	}
	
	public void setPositionInParentSpaceAndAllocationY(double y, double height)
	{
		positionInParentAllocationSpaceY = y;
		allocationY = height;
		refY = height * 0.5;
	}
	
	public void setPositionInParentSpaceAndAllocationY(double y, double height, double refY)
	{
		positionInParentAllocationSpaceY = y;
		allocationY = height;
		this.refY = refY;
	}



	public void transformAllocationX(Xform2 xform)
	{
		allocationX = xform.scale( allocationX );
	}

	public void transformAllocationY(Xform2 xform)
	{
		allocationY = xform.scale( allocationY );
		refY = xform.scale( refY );
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
			
			return positionInParentAllocationSpaceX == b.positionInParentAllocationSpaceX  &&  positionInParentAllocationSpaceY == b.positionInParentAllocationSpaceY  &&  
					allocationX == b.allocationX  &&  allocationY == b.allocationY  &&  refY == b.refY;
		}
		else
		{
			return false;
		}
	}
	
	
	public String toString()
	{
		return "LAllocBox( positionInParentSpaceX=" + positionInParentAllocationSpaceX + ", positionInParentSpaceY=" + positionInParentAllocationSpaceY +
			", allocationX=" + allocationX + ", allocationY=" + allocationY + ", refY=" + refY + " )";
	}
}
