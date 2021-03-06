//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Layout;

import BritefuryJ.LSpace.LayoutTree.LayoutNode;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;

public class LAllocBox implements LAllocBoxInterface
{
	protected double positionInParentAllocationSpaceX, positionInParentAllocationSpaceY;
	protected double actualWidth, allocWidth, allocHeight;
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
		allocWidth = width;
		allocHeight = height;
		this.refY = refY;
		this.layoutNode = layoutNode;
	}
	
	public LAllocBox(double x, double y, double width, double height, double actualWidth, double refY, LayoutNode layoutNode)
	{
		positionInParentAllocationSpaceX = x;
		positionInParentAllocationSpaceY = y;
		allocWidth = width;
		allocHeight = height;
		this.actualWidth = actualWidth;
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
	
	public double getActualWidth()
	{
		return actualWidth;
	}
	
	public Vector2 getActualSize()
	{
		return new Vector2( actualWidth, allocHeight );
	}

	public double getAllocWidth()
	{
		return allocWidth;
	}
	
	public double getAllocHeight()
	{
		return allocHeight;
	}
	
	public double getAllocRefY()
	{
		return refY;
	}
	
	public LAllocV getAllocV()
	{
		return new LAllocV( allocHeight, refY );
	}
	
	public Vector2 getAllocSize()
	{
		return new Vector2( allocWidth, allocHeight );
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
	
	public void setAllocationX(double allocWidth, double actualWidth)
	{
		this.allocWidth = allocWidth;
		this.actualWidth = actualWidth;
	}

	public void setAllocationY(double allocHeight, double refY)
	{
		this.allocHeight = allocHeight;
		this.refY = refY;
	}

	public void setPositionInParentSpaceAndAllocationX(double x, double allocWidth, double actualWidth)
	{
		positionInParentAllocationSpaceX = x;
		this.allocWidth = allocWidth;
		this.actualWidth = actualWidth;
	}
	
	public void setPositionInParentSpaceAndAllocationY(double y, double height)
	{
		positionInParentAllocationSpaceY = y;
		allocHeight = height;
		refY = height * 0.5;
	}
	
	public void setPositionInParentSpaceAndAllocationY(double y, double height, double refY)
	{
		positionInParentAllocationSpaceY = y;
		allocHeight = height;
		this.refY = refY;
	}



	public void transformAllocationX(Xform2 xform)
	{
		allocWidth = xform.scale( allocWidth );
	}

	public void transformAllocationY(Xform2 xform)
	{
		allocHeight = xform.scale( allocHeight );
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
					allocWidth == b.allocWidth  &&  allocHeight == b.allocHeight  &&  actualWidth == b.actualWidth  &&  refY == b.refY;
		}
		else
		{
			return false;
		}
	}
	
	
	public String toString()
	{
		return "LAllocBox( positionInParentAllocationSpaceX=" + positionInParentAllocationSpaceX + ", positionInParentAllocationSpaceY=" + positionInParentAllocationSpaceY +
			", allocWidth=" + allocWidth + ", allocHeight=" + allocHeight + ", actualWidth=" + actualWidth + ", refY=" + refY + " )";
	}
}
