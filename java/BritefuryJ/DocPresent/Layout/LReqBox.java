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
	
	
	private static int FLAG_HASBASELINE = 0x1  *  ElementAlignment._ELEMENTALIGN_END;
	private static int FLAG_LINEBREAK = 0x2  *  ElementAlignment._ELEMENTALIGN_END;
	private static int FLAG_PARAGRAPH_INDENT = 0x4  *  ElementAlignment._ELEMENTALIGN_END;
	private static int FLAG_PARAGRAPH_DEDENT = 0x8  *  ElementAlignment._ELEMENTALIGN_END;
	
	
	protected int flags = 0;
	protected int lineBreakCost;
	
	protected double minWidth, prefWidth, minHAdvance, prefHAdvance;
	protected double reqAscent, reqDescent, reqVSpacing;
	
	
	
	public LReqBox()
	{
		setFlag( FLAG_HASBASELINE, false );
		lineBreakCost = -1;
	}
	
	public LReqBox(double width, double hAdvance, double height, double vSpacing)
	{
		minWidth = prefWidth = width;
		minHAdvance = prefHAdvance = hAdvance;
		reqAscent = height * 0.5;
		reqDescent = height * 0.5;
		reqVSpacing = vSpacing;
		setFlag( FLAG_HASBASELINE, false );
		lineBreakCost = -1;
	}
	
	public LReqBox(double width, double hAdvance, double ascent, double descent, double vSpacing)
	{
		minWidth = prefWidth = width;
		minHAdvance = prefHAdvance = hAdvance;
		reqAscent = ascent;
		reqDescent = descent;
		reqVSpacing = vSpacing;
		setFlag( FLAG_HASBASELINE, true );
		lineBreakCost = -1;
	}

	public LReqBox(double minWidth, double prefWidth, double minHAdvance, double prefHAdvance, double height, double vSpacing)
	{
		this.minWidth = minWidth;
		this.prefWidth = prefWidth;
		this.minHAdvance = minHAdvance;
		this.prefHAdvance = prefHAdvance;
		reqAscent = height * 0.5;
		reqDescent = height * 0.5;
		this.reqVSpacing = vSpacing;
		setFlag( FLAG_HASBASELINE, false );
		lineBreakCost = -1;
	}

	public LReqBox(double minWidth, double prefWidth, double minHAdvance, double prefHAdvance, double ascent, double descent, double vSpacing)
	{
		this.minWidth = minWidth;
		this.prefWidth = prefWidth;
		this.minHAdvance = minHAdvance;
		this.prefHAdvance = prefHAdvance;
		this.reqAscent = ascent;
		this.reqDescent = descent;
		this.reqVSpacing = vSpacing;
		setFlag( FLAG_HASBASELINE, true );
		lineBreakCost = -1;
	}

	
	private LReqBox(LReqBox box)
	{
		minWidth = box.minWidth;
		prefWidth = box.prefWidth;
		minHAdvance = box.minHAdvance;
		prefHAdvance = box.prefHAdvance;
		reqAscent = box.reqAscent;
		reqDescent = box.reqDescent;
		reqVSpacing = box.reqVSpacing;
		flags = box.flags;
		lineBreakCost = box.lineBreakCost;
	}
	
	private LReqBox(LReqBox box, double scale)
	{
		minWidth = box.minWidth * scale;
		prefWidth = box.prefWidth * scale;
		minHAdvance = box.minHAdvance * scale;
		prefHAdvance = box.prefHAdvance * scale;
		reqAscent = box.reqAscent * scale;
		reqDescent = box.reqDescent * scale;
		reqVSpacing = box.reqVSpacing * scale;
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
	
	
	
	protected void setHasBaseline(boolean value)
	{
		setFlag( FLAG_HASBASELINE, value );
	}
	
	public boolean hasBaseline()
	{
		return getFlag( FLAG_HASBASELINE );
	}
	
	
	
	public void clear()
	{
		minWidth = prefWidth = minHAdvance = prefHAdvance = 0.0;
		reqAscent = reqDescent = reqVSpacing = 0.0;
		flags = 0;
	}
	
	public void clearRequisitionX()
	{
		minWidth = prefWidth = minHAdvance = prefHAdvance = 0.0;
	}
	
	public void clearRequisitionY()
	{
		reqAscent = reqDescent = reqVSpacing = 0.0;
		setFlag( FLAG_HASBASELINE, false );
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
		reqAscent = height * 0.5;
		reqDescent = height * 0.5;
		reqVSpacing = vSpacing;
		setFlag( FLAG_HASBASELINE, false );
	}
	
	public void setRequisitionY(double ascent, double descent, double vSpacing)
	{
		reqAscent = ascent;
		reqDescent = descent;
		reqVSpacing = vSpacing;
		setFlag( FLAG_HASBASELINE, true );
	}
	
	public void setRequisitionY(LReqBoxInterface reqBox)
	{
		reqAscent = reqBox.getReqAscent();
		reqDescent = reqBox.getReqDescent();
		reqVSpacing = reqBox.getReqVSpacing();
		setFlag( FLAG_HASBASELINE, reqBox.hasBaseline() );
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
		reqAscent += topMargin;
		reqDescent += bottomMargin;
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
					reqAscent == b.reqAscent  &&  reqDescent == b.reqDescent  &&  reqVSpacing == b.reqVSpacing  &&  hasBaseline() == b.hasBaseline();
		}
		else
		{
			return false;
		}
	}
	
	
	public String toString()
	{
		return "LReqBox( minWidth=" + minWidth + ", prefWidth=" + prefWidth +  ", minHAdvance=" + minHAdvance + ", prefHAdvance=" + prefHAdvance +
			", reqAscent=" + reqAscent + ", reqDescent=" + reqDescent + ", reqVSpacing=" + reqVSpacing +  ", bHasBaseline=" + hasBaseline() + ")";
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
