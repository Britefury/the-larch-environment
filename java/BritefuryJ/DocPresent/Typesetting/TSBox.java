//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Typesetting;

public class TSBox
{
	protected static double EPSILON = 1.0e-9;
	protected static double ONE_MINUS_EPSILON = 1.0 - EPSILON;
	protected static double ONE_PLUS_EPSILON = 1.0 + EPSILON;
	
	
	
	
	protected double scale;
	protected double minWidth, prefWidth, minHSpacing, prefHSpacing;
	protected double minAscent, prefAscent, minDescent, prefDescent, minVSpacing, prefVSpacing;
	protected boolean bHasBaseline;		// If false, then ascent = height, descent = 0
	protected double positionInParentSpaceX, positionInParentSpaceY;
	protected double allocationX, allocationY;
	
	
	
	public TSBox()
	{
		scale = 1.0;
		bHasBaseline = false;
	}
	
	public TSBox(double width, double hSpacing, double height, double vSpacing)
	{
		scale = 1.0;
		minWidth = prefWidth = width;
		minHSpacing = prefHSpacing = hSpacing;
		minAscent = prefAscent = height;
		minVSpacing = prefVSpacing = vSpacing;
		bHasBaseline = false;
	}
	
	public TSBox(double width, double hSpacing, double ascent, double descent, double vSpacing)
	{
		scale = 1.0;
		minWidth = prefWidth = width;
		minHSpacing = prefHSpacing = hSpacing;
		minAscent = prefAscent = ascent;
		minDescent = prefDescent = descent;
		minVSpacing = prefVSpacing = vSpacing;
		bHasBaseline = true;
	}

	public TSBox(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double minHeight, double prefHeight, double minVSpacing, double prefVSpacing)
	{
		scale = 1.0;
		this.minWidth = minWidth;
		this.prefWidth = prefWidth;
		this.minHSpacing = minHSpacing;
		this.prefHSpacing = prefHSpacing;
		this.minAscent = minHeight;
		this.prefAscent = prefHeight;
		this.minVSpacing = minVSpacing;
		this.prefVSpacing = prefVSpacing;
		bHasBaseline = false;
	}

	public TSBox(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing,
			double minAscent, double prefAscent, double minDescent, double prefDescent, double minVSpacing, double prefVSpacing)
	{
		scale = 1.0;
		this.minWidth = minWidth;
		this.prefWidth = prefWidth;
		this.minHSpacing = minHSpacing;
		this.prefHSpacing = prefHSpacing;
		this.minAscent = minAscent;
		this.prefAscent = prefAscent;
		this.minDescent = minDescent;
		this.prefDescent = prefDescent;
		this.minVSpacing = minVSpacing;
		this.prefVSpacing = prefVSpacing;
		bHasBaseline = true;
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
	

	public double getMinAscent()
	{
		return minAscent;
	}
	
	public double getPrefAscent()
	{
		return prefAscent;
	}
	
	public double getMinDescent()
	{
		return minDescent;
	}
	
	public double getPrefDescent()
	{
		return prefDescent;
	}
	
	public double getMinHeight()
	{
		return minAscent + minDescent;
	}
	
	public double getPrefHeight()
	{
		return prefAscent + prefDescent;
	}
	
	public double getMinVSpacing()
	{
		return minVSpacing;
	}
	
	public double getPrefVSpacing()
	{
		return prefVSpacing;
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
		minAscent = prefAscent = minDescent = prefDescent = minVSpacing = prefVSpacing = 0.0;
		bHasBaseline = false;
		positionInParentSpaceX = positionInParentSpaceY = allocationX = allocationY = 0.0;
	}
	
	public void clearRequisitionX()
	{
		minWidth = prefWidth = minHSpacing = prefHSpacing = 0.0;
	}
	
	public void clearRequisitionY()
	{
		minAscent = prefAscent = minDescent = prefDescent = minVSpacing = prefVSpacing = 0.0;
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
		minAscent = prefAscent = height;
		minDescent = prefDescent = 0.0;
		minVSpacing = prefVSpacing = vSpacing;
		bHasBaseline = false;
	}
	
	public void setRequisitionY(double ascent, double descent, double vSpacing)
	{
		minAscent = prefAscent = ascent;
		minDescent = prefDescent = descent;
		minVSpacing = prefVSpacing = vSpacing;
		bHasBaseline = true;
	}
	
	public void setRequisitionY(double minHeight, double prefHeight, double minVSpacing, double prefVSpacing)
	{
		this.minAscent = minHeight; 
		this.prefAscent = prefHeight;
		minDescent = prefDescent = 0.0;
		this.minVSpacing = minVSpacing; 
		this.prefVSpacing = prefVSpacing;
		bHasBaseline = false;
	}
	
	public void setRequisitionY(double minAscent, double prefAscent, double minDescent, double prefDescent, double minVSpacing, double prefVSpacing)
	{
		this.minAscent = minAscent; 
		this.prefAscent = prefAscent;
		this.minDescent = minDescent; 
		this.prefDescent = prefDescent;
		this.minVSpacing = minVSpacing; 
		this.prefVSpacing = prefVSpacing;
		bHasBaseline = true;
	}
	
	
	public void setAllocationX(double allocation)
	{
		allocationX = allocation;
	}
	
	public void setAllocationY(double allocation)
	{
		allocationY = allocation;
	}
	

	
	protected void allocateChildX(TSBox child, double localPosX, double localWidth)
	{
		child.allocationX = localWidth / child.scale;
		child.positionInParentSpaceX = localPosX;
	}
	
	protected void allocateChildY(TSBox child, double localPosY, double localHeight)
	{
		child.allocationY = localHeight / child.scale;
		child.positionInParentSpaceY = localPosY;
	}
	
	
	protected void allocateChildSpaceX(TSBox child, double localWidth)
	{
		child.allocationX = localWidth / child.scale;
	}
	
	protected void allocateChildSpaceY(TSBox child, double localHeight)
	{
		child.allocationY = localHeight / child.scale;
	}
	
	
	protected void allocateChildPositionX(TSBox child, double localPosX)
	{
		child.positionInParentSpaceX = localPosX;
	}
	
	protected void allocateChildPositionY(TSBox child, double localPosY)
	{
		child.positionInParentSpaceY = localPosY;
	}
	
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof TSBox )
		{
			TSBox b = (TSBox)x;
			
			return scale == b.scale  &&
					minWidth == b.minWidth  &&  prefWidth == b.prefWidth  &&  minHSpacing == b.minHSpacing  &&  prefHSpacing == b.prefHSpacing  &&
					minAscent == b.minAscent  &&  prefAscent == b.prefAscent  &&  minDescent == b.minDescent  &&  prefDescent == b.prefDescent  &&
					minVSpacing == b.minVSpacing  &&  prefVSpacing == b.prefVSpacing  &&
					bHasBaseline == b.bHasBaseline  &&
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
			", minAscent=" + minAscent + ", prefAscent=" + prefAscent + ", minDescent=" + minDescent + ", prefDescent=" + prefDescent +
			", minVSpacing=" + minVSpacing + ", prefVSpacing=" + prefVSpacing +
			", bHasBaseline=" + bHasBaseline +
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
}
