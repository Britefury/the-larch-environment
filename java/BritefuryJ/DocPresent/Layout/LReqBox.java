//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

import BritefuryJ.Math.Xform2;




public class LReqBox implements LReqBoxInterface
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

	
	public LReqBox(LReqBoxInterface box)
	{
		minWidth = box.getReqMinWidth();
		prefWidth = box.getReqPrefWidth();
		minHAdvance = box.getReqMinHAdvance();
		prefHAdvance = box.getReqPrefHAdvance();
		reqHeight = box.getReqHeight();
		reqVSpacing = box.getReqVSpacing();
		refY = box.getReqRefY();
		
		flags = 0;
		setFlag( FLAG_LINEBREAK, box.isReqLineBreak() );
		setFlag( FLAG_PARAGRAPH_INDENT, box.isReqParagraphIndentMarker() );
		setFlag( FLAG_PARAGRAPH_DEDENT, box.isReqParagraphDedentMarker() );

		lineBreakCost = box.getReqLineBreakCost();
	}
	
	public LReqBox(LReqBoxInterface box, Xform2 xform)
	{
		minWidth = xform.scale( box.getReqMinWidth() );
		prefWidth = xform.scale( box.getReqPrefWidth() );
		minHAdvance = xform.scale( box.getReqMinHAdvance() );
		prefHAdvance = xform.scale( box.getReqPrefHAdvance() );
		reqHeight = xform.scale( box.getReqHeight() );
		reqVSpacing = xform.scale( box.getReqVSpacing() );
		refY = xform.scale( box.getReqRefY() );

		flags = 0;
		setFlag( FLAG_LINEBREAK, box.isReqLineBreak() );
		setFlag( FLAG_PARAGRAPH_INDENT, box.isReqParagraphIndentMarker() );
		setFlag( FLAG_PARAGRAPH_DEDENT, box.isReqParagraphDedentMarker() );

		lineBreakCost = box.getReqLineBreakCost();
	}
	
	
	
	public double getReqMinWidth()
	{
		return minWidth;
	}
	
	public double getReqPrefWidth()
	{
		return prefWidth;
	}
	
	public double getReqMinHAdvance()
	{
		return minHAdvance;
	}
	
	public double getReqPrefHAdvance()
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
	
	public double getReqRefY()
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
		this.minWidth = box.getReqMinWidth(); 
		this.prefWidth = box.getReqPrefWidth();
		this.minHAdvance = box.getReqMinHAdvance(); 
		this.prefHAdvance = box.getReqPrefHAdvance();
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
		refY = reqBox.getReqRefY();
	}
	
	
	public void maxRequisitionX(LReqBoxInterface box)
	{
		setRequisitionX( Math.max( minWidth, box.getReqMinWidth() ), Math.max( prefWidth, box.getReqPrefWidth() ),
				Math.max( minHAdvance, box.getReqMinHAdvance() ), Math.max( prefHAdvance, box.getReqPrefHAdvance() ) );
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
	
	
	public boolean isReqParagraphIndentMarker()
	{
		return getFlag( FLAG_PARAGRAPH_INDENT );
	}
	
	public boolean isReqParagraphDedentMarker()
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
		refY += topMargin;
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




	public LReqBox copy()
	{
		return new LReqBox( this );
	}
	
	public LReqBoxInterface transformedRequisition(Xform2 xform)
	{
		return new LReqBox( this, xform );
	}
	
	
	
	public LReqBox lineBreakBox(int cost)
	{
		LReqBox b = new LReqBox( this );
		b.setFlag( FLAG_LINEBREAK, true );
		b.lineBreakCost = cost;
		return b;
	}
	
	public boolean isReqLineBreak()
	{
		return getFlag( FLAG_LINEBREAK );
	}
	
	public int getReqLineBreakCost()
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
