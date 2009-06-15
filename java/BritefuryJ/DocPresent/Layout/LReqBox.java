//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;




public class LReqBox
{
	protected static double EPSILON = 1.0e-9;
	protected static double ONE_MINUS_EPSILON = 1.0 - EPSILON;
	protected static double ONE_PLUS_EPSILON = 1.0 + EPSILON;
	
	
	
	protected double minWidth, prefWidth, minHSpacing, prefHSpacing;
	protected double reqAscent, reqDescent, reqVSpacing;
	
	protected boolean bHasBaseline;		// If false, then ascent = height, descent = 0

	protected boolean bLineBreak;
	protected int lineBreakCost;
	
	
	public LReqBox()
	{
		bHasBaseline = false;
		lineBreakCost = -1;
	}
	
	public LReqBox(double width, double hSpacing, double height, double vSpacing)
	{
		minWidth = prefWidth = width;
		minHSpacing = prefHSpacing = hSpacing;
		reqAscent = height;
		reqVSpacing = vSpacing;
		bHasBaseline = false;
		lineBreakCost = -1;
	}
	
	public LReqBox(double width, double hSpacing, double ascent, double descent, double vSpacing)
	{
		minWidth = prefWidth = width;
		minHSpacing = prefHSpacing = hSpacing;
		reqAscent = ascent;
		reqDescent = descent;
		reqVSpacing = vSpacing;
		bHasBaseline = true;
		lineBreakCost = -1;
	}

	public LReqBox(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double height, double vSpacing)
	{
		this.minWidth = minWidth;
		this.prefWidth = prefWidth;
		this.minHSpacing = minHSpacing;
		this.prefHSpacing = prefHSpacing;
		this.reqAscent = height;
		this.reqVSpacing = vSpacing;
		bHasBaseline = false;
		lineBreakCost = -1;
	}

	public LReqBox(double minWidth, double prefWidth, double minHSpacing, double prefHSpacing, double ascent, double descent, double vSpacing)
	{
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
	
	
	private LReqBox(LReqBox box)
	{
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
	
	private LReqBox(LReqBox box, double scale)
	{
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
	
	
	
	public void clear()
	{
		minWidth = prefWidth = minHSpacing = prefHSpacing = 0.0;
		reqAscent = reqDescent = reqVSpacing = 0.0;
		bHasBaseline = false;
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
	
	public void setRequisitionX(LReqBox box)
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
	
	public void setRequisitionY(LReqBox box)
	{
		reqAscent = box.reqAscent;
		reqDescent = box.reqDescent;
		reqVSpacing = box.reqVSpacing;
		bHasBaseline = box.bHasBaseline;
	}
	
	
	public void maxRequisitionX(LReqBox box)
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
		
		if ( x instanceof LReqBox )
		{
			LReqBox b = (LReqBox)x;
			
			return minWidth == b.minWidth  &&  prefWidth == b.prefWidth  &&  minHSpacing == b.minHSpacing  &&  prefHSpacing == b.prefHSpacing  &&
					reqAscent == b.reqAscent  &&  reqDescent == b.reqDescent  &&  reqVSpacing == b.reqVSpacing  &&  bHasBaseline == b.bHasBaseline;
		}
		else
		{
			return false;
		}
	}
	
	
	public String toString()
	{
		return "LReqBox( minWidth=" + minWidth + ", prefWidth=" + prefWidth +  ", minHSpacing=" + minHSpacing + ", prefHSpacing=" + prefHSpacing +
			", reqAscent=" + reqAscent + ", reqDescent=" + reqDescent + ", reqVSpacing=" + reqVSpacing +  ", bHasBaseline=" + bHasBaseline + ")";
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
	
	
	
	public LReqBox copy()
	{
		return new LReqBox( this );
	}
	
	public LReqBox scaled(double scale)
	{
		return new LReqBox( this, scale );
	}
	
	
	
	public LReqBox lineBreakBox(int cost)
	{
		LReqBox b = new LReqBox( this );
		b.bLineBreak = true;
		b.lineBreakCost = cost;
		return b;
	}
}
