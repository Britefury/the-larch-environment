//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;




public class LReqBox extends LReqBoxInterface
{
	protected static double EPSILON = 1.0e-9;
	protected static double ONE_MINUS_EPSILON = 1.0 - EPSILON;
	protected static double ONE_PLUS_EPSILON = 1.0 + EPSILON;
	
	
	private static int FLAG_LINEBREAK = 0x1  *  ElementAlignment._ELEMENTALIGN_END;
	private static int FLAG_PARAGRAPH_INDENT = 0x2  *  ElementAlignment._ELEMENTALIGN_END;
	private static int FLAG_PARAGRAPH_DEDENT = 0x4  *  ElementAlignment._ELEMENTALIGN_END;
	
	
	protected int flags = 0;
	protected int lineBreakCost;
	
	protected double minWidth, prefWidth, minHAdvance, prefHAdvance;
	protected double reqHeight, reqVSpacing;
	protected double refY;
	
	
	
	public LReqBox()
	{
		lineBreakCost = -1;
	}
	
	public LReqBox(double width, double hAdvance, double height, double vSpacing)
	{
		minWidth = prefWidth = width;
		minHAdvance = prefHAdvance = hAdvance;
		reqHeight = height;
		reqVSpacing = vSpacing;
		refY = height * 0.5;
		lineBreakCost = -1;
	}
	
	public LReqBox(double width, double hAdvance, double height, double vSpacing, double refY)
	{
		this.minWidth = prefWidth = width;
		this.minHAdvance = prefHAdvance = hAdvance;
		this.reqHeight = height;
		this.reqVSpacing = vSpacing;
		this.refY = refY;
		lineBreakCost = -1;
	}

	public LReqBox(double minWidth, double prefWidth, double minHAdvance, double prefHAdvance, double height, double vSpacing, double refY)
	{
		this.minWidth = minWidth;
		this.prefWidth = prefWidth;
		this.minHAdvance = minHAdvance;
		this.prefHAdvance = prefHAdvance;
		this.reqHeight = height;
		this.reqVSpacing = vSpacing;
		this.refY = refY;
		lineBreakCost = -1;
	}

	
	private LReqBox(LReqBox box)
	{
		minWidth = box.minWidth;
		prefWidth = box.prefWidth;
		minHAdvance = box.minHAdvance;
		prefHAdvance = box.prefHAdvance;
		reqHeight = box.reqHeight;
		reqVSpacing = box.reqVSpacing;
		refY = box.refY;
		flags = box.flags;
		lineBreakCost = box.lineBreakCost;
	}
	
	private LReqBox(LReqBox box, double scale)
	{
		minWidth = box.minWidth * scale;
		prefWidth = box.prefWidth * scale;
		minHAdvance = box.minHAdvance * scale;
		prefHAdvance = box.prefHAdvance * scale;
		reqHeight = box.reqHeight * scale;
		reqVSpacing = box.reqVSpacing * scale;
		refY = box.refY * scale;
		flags = box.flags;
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
	
	public double getMinHAdvance()
	{
		return minHAdvance;
	}
	
	public double getPrefHAdvance()
	{
		return prefHAdvance;
	}
	

	public double getReqHeight()
	{
		return reqHeight;
	}
	
	public double getReqVSpacing()
	{
		return reqVSpacing;
	}
	
	public double getRefY()
	{
		return refY;
	}
	
	public double getReqHeightBelowRefPoint()
	{
		return reqHeight - refY;
	}
	
	
	public void clear()
	{
		minWidth = prefWidth = minHAdvance = prefHAdvance = 0.0;
		reqHeight = reqVSpacing = 0.0;
		refY = 0.0;
		flags = 0;
	}
	
	public void clearRequisitionX()
	{
		minWidth = prefWidth = minHAdvance = prefHAdvance = 0.0;
	}
	
	public void clearRequisitionY()
	{
		reqHeight = reqVSpacing = refY = 0.0;
	}
	
	
	
	public void setRequisitionX(double width, double hAdvance)
	{
		minWidth = prefWidth = width;
		minHAdvance = prefHAdvance = hAdvance;
	}
	
	public void setRequisitionX(double minWidth, double prefWidth, double minHAdvance, double prefHAdvance)
	{
		this.minWidth = minWidth; 
		this.prefWidth = prefWidth;
		this.minHAdvance = minHAdvance; 
		this.prefHAdvance = prefHAdvance;
	}
	
	public void setRequisitionX(LReqBox box)
	{
		this.minWidth = box.minWidth; 
		this.prefWidth = box.prefWidth;
		this.minHAdvance = box.minHAdvance; 
		this.prefHAdvance = box.prefHAdvance;
	}
	
	public void setRequisitionX(LReqBoxInterface box)
	{
		this.minWidth = box.getMinWidth(); 
		this.prefWidth = box.getPrefWidth();
		this.minHAdvance = box.getMinHAdvance(); 
		this.prefHAdvance = box.getPrefHAdvance();
	}
	
	

	public void setRequisitionY(double height, double vSpacing)
	{
		reqHeight = height;
		reqVSpacing = vSpacing;
		refY = height * 0.5;
	}
	
	public void setRequisitionY(double height, double vSpacing, double refY)
	{
		reqHeight = height;
		reqVSpacing = vSpacing;
		this.refY = refY;
	}
	
	public void setRequisitionY(LReqBoxInterface reqBox)
	{
		reqHeight = reqBox.getReqHeight();
		reqVSpacing = reqBox.getReqVSpacing();
		refY = reqBox.getRefY();
	}
	
	
	public void maxRequisitionX(LReqBoxInterface box)
	{
		setRequisitionX( Math.max( minWidth, box.getMinWidth() ), Math.max( prefWidth, box.getPrefWidth() ),
				Math.max( minHAdvance, box.getMinHAdvance() ), Math.max( prefHAdvance, box.getPrefHAdvance() ) );
	}
	
	
	
	
	public void setLineBreakCost(int cost)
	{
		lineBreakCost = cost;
		setFlag( FLAG_LINEBREAK, true );
	}
	
	
	
	public void setParagraphIndentMarker()
	{
		setFlag( FLAG_PARAGRAPH_INDENT, true );
	}
	
	public void setParagraphDedentMarker()
	{
		setFlag( FLAG_PARAGRAPH_DEDENT, true );
	}
	
	
	public boolean isParagraphIndentMarker()
	{
		return getFlag( FLAG_PARAGRAPH_INDENT );
	}
	
	public boolean isParagraphDedentMarker()
	{
		return getFlag( FLAG_PARAGRAPH_DEDENT );
	}
	
	
	
	public void borderX(double leftMargin, double rightMargin)
	{
		if ( minHAdvance <= minWidth )
		{
			minWidth += leftMargin + rightMargin;
			minHAdvance = minWidth;
		}
		else
		{
			double hspacing = minHAdvance - minWidth;
			hspacing = Math.max( hspacing - rightMargin, 0.0 );
			minWidth += leftMargin + rightMargin;
			minHAdvance = minWidth + hspacing;
		}
		
		if ( prefHAdvance <= prefWidth )
		{
			prefWidth += leftMargin + rightMargin;
			prefHAdvance = prefWidth;
		}
		else
		{
			double hspacing = prefHAdvance - prefWidth;
			hspacing = Math.max( hspacing - rightMargin, 0.0 );
			prefWidth += leftMargin + rightMargin;
			prefHAdvance = prefWidth + hspacing;
		}
	}
	
	public void borderY(double topMargin, double bottomMargin)
	{
		reqHeight += ( topMargin + bottomMargin );
		reqVSpacing = Math.max( reqVSpacing - bottomMargin, 0.0 );
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
			
			return minWidth == b.minWidth  &&  prefWidth == b.prefWidth  &&  minHAdvance == b.minHAdvance  &&  prefHAdvance == b.prefHAdvance  &&
				reqHeight == b.reqHeight  &&  reqVSpacing == b.reqVSpacing  &&  refY == b.refY;
		}
		else
		{
			return false;
		}
	}
	
	
	public String toString()
	{
		return "LReqBox( minWidth=" + minWidth + ", prefWidth=" + prefWidth +  ", minHAdvance=" + minHAdvance + ", prefHAdvance=" + prefHAdvance +
			", reqHeight=" + reqHeight + ", reqVSpacing=" + reqVSpacing +  ", refY=" + refY + ")";
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
		b.setFlag( FLAG_LINEBREAK, true );
		b.lineBreakCost = cost;
		return b;
	}
	
	public boolean isLineBreak()
	{
		return getFlag( FLAG_LINEBREAK );
	}
	
	public int getLineBreakCost()
	{
		return lineBreakCost;
	}
	
	
	
	private void setFlag(int f, boolean value)
	{
		if ( value )
		{
			flags |= f;
		}
		else
		{
			flags &= ~f;
		}
	}
	
	private boolean getFlag(int f)
	{
		return ( flags & f )  !=  0;
	}
}
