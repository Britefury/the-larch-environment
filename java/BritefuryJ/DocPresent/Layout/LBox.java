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



public class LBox
{
	protected static double EPSILON = 1.0e-9;
	protected static double ONE_MINUS_EPSILON = 1.0 - EPSILON;
	protected static double ONE_PLUS_EPSILON = 1.0 + EPSILON;
	
	
	
	protected DPWidget element;
	protected double minWidth, prefWidth, minHSpacing, prefHSpacing;
	protected double reqAscent, reqDescent, reqVSpacing;
	
	protected double positionInParentSpaceX, positionInParentSpaceY;
	protected double allocationX, allocationY;
	
	protected boolean bHasBaseline;		// If false, then ascent = height, descent = 0

	protected boolean bLineBreak;
	protected int lineBreakCost;
	
	
	public LBox(DPWidget element)
	{
		this.element = element;
		bHasBaseline = false;
		lineBreakCost = -1;
	}
	
	public LBox(DPWidget element, double width, double hSpacing, double height, double vSpacing)
	{
		this.element = element;
		minWidth = prefWidth = width;
		minHSpacing = prefHSpacing = hSpacing;
		reqAscent = height;
		reqVSpacing = vSpacing;
		bHasBaseline = false;
		lineBreakCost = -1;
	}
	
	public LBox(DPWidget element, double width, double hSpacing, double ascent, double descent, double vSpacing)
	{
		this.element = element;
		minWidth = prefWidth = width;
		minHSpacing = prefHSpacing = hSpacing;
		reqAscent = ascent;
		reqDescent = descent;
		reqVSpacing = vSpacing;
		bHasBaseline = true;
		lineBreakCost = -1;
	}

	public LBox(DPWidget element, double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double height, double vSpacing)
	{
		this.element = element;
		this.minWidth = minWidth;
		this.prefWidth = prefWidth;
		this.minHSpacing = minHSpacing;
		this.prefHSpacing = prefHSpacing;
		this.reqAscent = height;
		this.reqVSpacing = vSpacing;
		bHasBaseline = false;
		lineBreakCost = -1;
	}

	public LBox(DPWidget element, double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double ascent, double descent, double vSpacing)
	{
		this.element = element;
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
	
	
	private LBox(DPWidget element, LBox box)
	{
		this.element = element;
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
	
	private LBox(DPWidget element, LBox box, double scale)
	{
		this.element = element;
		minWidth = box.minWidth * scale;
		prefWidth = box.prefWidth * scale;
		minHSpacing = box.minHSpacing * scale;
		prefHSpacing = box.prefHSpacing * scale;
		reqAscent = box.reqAscent * scale;
		reqDescent = box.reqDescent * scale;
		reqVSpacing = box.reqVSpacing * scale;
		bHasBaseline = box.bHasBaseline;
		bLineBreak = box.bLineBreak;
		lineBreakCost = box.lineBreakCost;
	}
	
	
	
	public DPWidget getElement()
	{
		return element;
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


	
	
	
	
	public void clear()
	{
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
	
	public void setRequisitionX(LBox box)
	{
		this.minWidth = box.minWidth; 
		this.prefWidth = box.prefWidth;
		this.minHSpacing = box.minHSpacing; 
		this.prefHSpacing = box.prefHSpacing;
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
	
	public void setRequisitionY(LBox box)
	{
		reqAscent = box.reqAscent;
		reqDescent = box.reqDescent;
		reqVSpacing = box.reqVSpacing;
		bHasBaseline = box.bHasBaseline;
	}
	
	
	public void maxRequisitionX(LBox box)
	{
		double minW = Math.max( minWidth, box.minWidth );
		double minA = Math.max( minWidth + minHSpacing, box.minWidth + box.minHSpacing );
		double prefW = Math.max( prefWidth, box.prefWidth );
		double prefA = Math.max( prefWidth + prefHSpacing, box.prefWidth + box.prefHSpacing );
		setRequisitionX( minW, prefW, minA - minW, prefA - prefW );
	}
	
	
	
	
	public void setLineBreakCost(int cost)
	{
		lineBreakCost = cost;
		bLineBreak = true;
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
	

	public void setAllocation(LBox box)
	{
		allocationX = box.allocationX;
		allocationY = box.allocationY;
		positionInParentSpaceX = box.positionInParentSpaceX;
		positionInParentSpaceY = box.positionInParentSpaceY;
	}
	
	
	public void allocateChildX(LBox child, double localPosX, double localWidth)
	{
		child.allocationX = localWidth;
		child.positionInParentSpaceX = localPosX;
	}
	
	public void allocateChildY(LBox child, double localPosY, double localHeight)
	{
		child.allocationY = localHeight;
		child.positionInParentSpaceY = localPosY;
	}
	
	
	public void allocateChildX(LBox child)
	{
		child.allocationX = allocationX;
		child.positionInParentSpaceX = 0.0;
	}
	
	public void allocateChildY(LBox child)
	{
		child.allocationY = allocationY;
		child.positionInParentSpaceY = 0.0;
	}
	
	
	protected void allocateChildSpaceX(LBox child, double localWidth)
	{
		child.allocationX = localWidth;
	}
	
	protected void allocateChildSpaceY(LBox child, double localHeight)
	{
		child.allocationY = localHeight;
	}
	
	
	protected void allocateChildPositionX(LBox child, double localPosX)
	{
		child.positionInParentSpaceX = localPosX;
	}
	
	protected void allocateChildPositionY(LBox child, double localPosY)
	{
		child.positionInParentSpaceY = localPosY;
	}
	
	
	public void scaleAllocationX(double scale)
	{
		allocationX *= scale;
	}
	
	
	public void borderX(double leftMargin, double rightMargin)
	{
		minWidth += leftMargin + rightMargin;
		prefWidth += leftMargin + rightMargin;
		minHSpacing = Math.max( minHSpacing - rightMargin, 0.0 );
		prefHSpacing = Math.max( prefHSpacing - rightMargin, 0.0 );
	}
	
	public void borderY(double topMargin, double bottomMargin)
	{
		if ( bHasBaseline )
		{
			reqAscent += topMargin;
			reqDescent += bottomMargin;
			reqVSpacing = Math.max( reqVSpacing - bottomMargin, 0.0 );
		}
		else
		{
			reqAscent += topMargin + bottomMargin;
			reqVSpacing = Math.max( reqVSpacing - bottomMargin, 0.0 );
		}
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
			
			return minWidth == b.minWidth  &&  prefWidth == b.prefWidth  &&  minHSpacing == b.minHSpacing  &&  prefHSpacing == b.prefHSpacing  &&
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
	
	
	
	public LBox copy(DPWidget element)
	{
		return new LBox( element, this );
	}
	
	public LBox scaled(DPWidget element, double scale)
	{
		return new LBox( element, this, scale );
	}
	
	
	
	public LBox lineBreakBox(DPWidget element, int cost)
	{
		LBox b = new LBox( element, this );
		b.bLineBreak = true;
		b.lineBreakCost = cost;
		return b;
	}
}
