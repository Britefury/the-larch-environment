//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;


public class LBox
{
	protected static double EPSILON = 1.0e-9;
	protected static double ONE_MINUS_EPSILON = 1.0 - EPSILON;
	protected static double ONE_PLUS_EPSILON = 1.0 + EPSILON;
	
	
	
	
	protected double scale;
	protected double minWidth, prefWidth, minHSpacing, prefHSpacing;
	protected double reqAscent, reqDescent, reqVSpacing;
	
	protected double positionInParentSpaceX, positionInParentSpaceY;
	protected double allocationX, allocationY;
	
	protected boolean bHasBaseline;		// If false, then ascent = height, descent = 0

	protected boolean bLineBreak;
	protected int lineBreakCost;
	
	
	public LBox()
	{
		scale = 1.0;
		bHasBaseline = false;
		lineBreakCost = -1;
	}
	
	public LBox(double width, double hSpacing, double height, double vSpacing)
	{
		scale = 1.0;
		minWidth = prefWidth = width;
		minHSpacing = prefHSpacing = hSpacing;
		reqAscent = height;
		reqVSpacing = vSpacing;
		bHasBaseline = false;
		lineBreakCost = -1;
	}
	
	public LBox(double width, double hSpacing, double ascent, double descent, double vSpacing)
	{
		scale = 1.0;
		minWidth = prefWidth = width;
		minHSpacing = prefHSpacing = hSpacing;
		reqAscent = ascent;
		reqDescent = descent;
		reqVSpacing = vSpacing;
		bHasBaseline = true;
		lineBreakCost = -1;
	}

	public LBox(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double height, double vSpacing)
	{
		scale = 1.0;
		this.minWidth = minWidth;
		this.prefWidth = prefWidth;
		this.minHSpacing = minHSpacing;
		this.prefHSpacing = prefHSpacing;
		this.reqAscent = height;
		this.reqVSpacing = vSpacing;
		bHasBaseline = false;
		lineBreakCost = -1;
	}

	public LBox(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double ascent, double descent, double vSpacing)
	{
		scale = 1.0;
		this.minWidth = minWidth;
		this.prefWidth = prefWidth;
		this.minHSpacing = minHSpacing;
		this.prefHSpacing = prefHSpacing;
		this.reqAscent = ascent;
		this.reqDescent = descent;
		this.reqVSpacing = vSpacing;
		bHasBaseline = true;
		lineBreakCost = -1;
	}
	
	
	private LBox(LBox box)
	{
		scale = 1.0;
		minWidth = box.minWidth;
		prefWidth = box.prefWidth;
		minHSpacing = box.minHSpacing;
		prefHSpacing = box.prefHSpacing;
		reqAscent = box.reqAscent;
		reqDescent = box.reqDescent;
		reqVSpacing = box.reqVSpacing;
		bHasBaseline = box.bHasBaseline;
		bLineBreak = box.bLineBreak;
		lineBreakCost = box.lineBreakCost;
	}
	

	public double getScale()
	{
		return scale;
	}
	
	
	public double getMinWidth()
	{
		return minWidth;
	}
	
	public double getPrefWidth()
	{
		return prefWidth;
	}
	
	public double getMinHSpacing()
	{
		return minHSpacing;
	}
	
	public double getPrefHSpacing()
	{
		return minHSpacing;
	}
	

	public double getReqAscent()
	{
		return reqAscent;
	}
	
	public double getReqDescent()
	{
		return reqDescent;
	}
	
	public double getReqHeight()
	{
		return reqAscent + reqDescent;
	}
	
	public double getReqVSpacing()
	{
		return reqVSpacing;
	}
	
	
	public boolean hasBaseline()
	{
		return bHasBaseline;
	}
	
	
	
	public double getPositionInParentSpaceX()
	{
		return positionInParentSpaceX;
	}
	
	public double getPositionInParentSpaceY()
	{
		return positionInParentSpaceY;
	}
	

	public double getAllocationX()
	{
		return allocationX;
	}
	
	public double getAllocationY()
	{
		return allocationY;
	}
	

	
	
	
	
	public void clear()
	{
		scale = 1.0;
		minWidth = prefWidth = minHSpacing = prefHSpacing = 0.0;
		reqAscent = reqDescent = reqVSpacing = 0.0;
		bHasBaseline = false;
		positionInParentSpaceX = positionInParentSpaceY = allocationX = allocationY = 0.0;
	}
	
	public void clearRequisitionX()
	{
		minWidth = prefWidth = minHSpacing = prefHSpacing = 0.0;
	}
	
	public void clearRequisitionY()
	{
		reqAscent = reqDescent = reqVSpacing = 0.0;
	}
	
	
	
	public void setRequisitionX(double width, double hSpacing)
	{
		minWidth = prefWidth = width;
		minHSpacing = prefHSpacing = hSpacing;
	}
	
	public void setRequisitionX(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing)
	{
		this.minWidth = minWidth; 
		this.prefWidth = prefWidth;
		this.minHSpacing = minHSpacing; 
		this.prefHSpacing = prefHSpacing;
	}
	
	

	public void setRequisitionY(double height, double vSpacing)
	{
		reqAscent = height;
		reqDescent = 0.0;
		reqVSpacing = vSpacing;
		bHasBaseline = false;
	}
	
	public void setRequisitionY(double ascent, double descent, double vSpacing)
	{
		reqAscent = ascent;
		reqDescent = descent;
		reqVSpacing = vSpacing;
		bHasBaseline = true;
	}
	
	
	public void maxRequisitionX(LBox box)
	{
		double minW = Math.max( minWidth, box.minWidth );
		double minA = Math.max( minWidth + minHSpacing, box.minWidth + box.minHSpacing );
		double prefW = Math.max( prefWidth, box.prefWidth );
		double prefA = Math.max( prefWidth + prefHSpacing, box.prefWidth + box.prefHSpacing );
		setRequisitionX( minW, prefW, minA - minW, prefA - prefW );
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
	

	public void setAllocationFrom(LBox box)
	{
		allocationX = box.allocationX;
		allocationY = box.allocationY;
		positionInParentSpaceX = box.positionInParentSpaceX;
		positionInParentSpaceY = box.positionInParentSpaceY;
	}
	
	
	protected void allocateChildX(LBox child, double localPosX, double localWidth)
	{
		child.allocationX = localWidth / child.scale;
		child.positionInParentSpaceX = localPosX;
	}
	
	protected void allocateChildY(LBox child, double localPosY, double localHeight)
	{
		child.allocationY = localHeight / child.scale;
		child.positionInParentSpaceY = localPosY;
	}
	
	
	protected void allocateChildSpaceX(LBox child, double localWidth)
	{
		child.allocationX = localWidth / child.scale;
	}
	
	protected void allocateChildSpaceY(LBox child, double localHeight)
	{
		child.allocationY = localHeight / child.scale;
	}
	
	
	protected void allocateChildPositionX(LBox child, double localPosX)
	{
		child.positionInParentSpaceX = localPosX;
	}
	
	protected void allocateChildPositionY(LBox child, double localPosY)
	{
		child.positionInParentSpaceY = localPosY;
	}
	
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof LBox )
		{
			LBox b = (LBox)x;
			
			return scale == b.scale  &&
					minWidth == b.minWidth  &&  prefWidth == b.prefWidth  &&  minHSpacing == b.minHSpacing  &&  prefHSpacing == b.prefHSpacing  &&
					reqAscent == b.reqAscent  &&  reqDescent == b.reqDescent  &&  reqVSpacing == b.reqVSpacing  &&  bHasBaseline == b.bHasBaseline  &&
					positionInParentSpaceX == b.positionInParentSpaceX  &&  positionInParentSpaceY == b.positionInParentSpaceY  &&  
					allocationX == b.allocationX  &&  allocationY == b.allocationY;
		}
		else
		{
			return false;
		}
	}
	
	
	public String toString()
	{
		return "TSBox( minWidth=" + minWidth + ", prefWidth=" + prefWidth +  ", minHSpacing=" + minHSpacing + ", prefHSpacing=" + prefHSpacing +
			", reqAscent=" + reqAscent + ", reqDescent=" + reqDescent + ", reqVSpacing=" + reqVSpacing +  ", bHasBaseline=" + bHasBaseline +
			", positionInParentSpaceX=" + positionInParentSpaceX + ", positionInParentSpaceY=" + positionInParentSpaceY +
			", allocationX=" + allocationX + ", allocationY=" + allocationY + ")";
	}






	private static int PACKFLAG_EXPAND = 1;
	
	
	public static int packFlags(boolean bExpand)
	{
		return ( bExpand ? PACKFLAG_EXPAND : 0 );
	}
	
	public static int combinePackFlags(int flags0, int flags1)
	{
		return flags0 | flags1;
	}
	
	public static boolean testPackFlagExpand(int packFlags)
	{
		return ( packFlags & PACKFLAG_EXPAND )  !=  0;
	}
	
	
	
	public LBox copy()
	{
		return new LBox( this );
	}
	
	
	
	public LBox lineBreakBox(int cost)
	{
		LBox b = new LBox( this );
		b.bLineBreak = true;
		b.lineBreakCost = cost;
		return b;
	}
}
