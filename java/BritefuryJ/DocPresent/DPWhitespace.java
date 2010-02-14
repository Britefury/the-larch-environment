//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeWhitespace;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleParams.ContentLeafStyleParams;
import BritefuryJ.Math.Point2;

public class DPWhitespace extends DPContentLeafEditable
{
	protected double width;
	
	
	public DPWhitespace(String whitespace)
	{
		this( ContentLeafStyleParams.defaultStyleParams, whitespace, 0.0 );
	}
	
	public DPWhitespace(ContentLeafStyleParams styleParams, String whitespace)
	{
		this(styleParams, whitespace, 0.0 );
	}
	
	public DPWhitespace(String whitespace, double width)
	{
		this( ContentLeafStyleParams.defaultStyleParams, whitespace, width );
	}

	public DPWhitespace(ContentLeafStyleParams styleParams, String whitespace, double width)
	{
		super(styleParams, whitespace );
		this.width = width;
		
		layoutNode = new LayoutNodeWhitespace( this );
	}
	
	
	
	public double getWhitespaceWidth()
	{
		return width;
	}

	
	
	//
	//
	// CARET METHODS
	//
	//
	
	public void drawCaret(Graphics2D graphics, Caret c)
	{
		if ( c.getMarker().getIndex() == 0 )
		{
			drawCaretAtStart( graphics );
		}
		else
		{
			drawCaretAtEnd( graphics );
		}
	}

	public void drawCaretAtStart(Graphics2D graphics)
	{
		DPContentLeaf leaf = this;
		while ( leaf != null  &&  leaf.isWhitespace() )
		{
			leaf = leaf.getLayoutNode().getContentLeafToLeft();
		}
		
		if ( leaf != null )
		{
			leaf.drawCaretAtEnd( graphics );
		}
	}
	
	public void drawCaretAtEnd(Graphics2D graphics)
	{
		DPContentLeaf leaf = this;
		while ( leaf != null  &&  leaf.isWhitespace() )
		{
			leaf = leaf.getContentLeafToRight();
		}
		
		if ( leaf != null )
		{
			leaf.drawCaretAtStart( graphics );
		}
	}

	
	//
	//
	// SELECTION METHODS
	//
	//
	
	public void drawSelection(Graphics2D graphics, Marker from, Marker to)
	{
		double allocationX = getAllocationX();
		double allocationY = getAllocationY();
		AffineTransform current = pushGraphicsTransform( graphics );
		int startIndex = from != null  ?  from.getIndex()  :  0;
		int endIndex = to != null  ?  to.getIndex()  :  1;
		double startX = startIndex == 0  ?  0.0  :  allocationX;
		double endX = endIndex == 0  ?  0.0  :  allocationX;
		Rectangle2D.Double shape = new Rectangle2D.Double( startX, 0.0, endX - startX, allocationY);
		graphics.fill( shape );
		popGraphicsTransform( graphics, current );
	}
	
	

	//
	// Marker methods
	//
	
	public int getMarkerPositonForPoint(Point2 localPos)
	{
		if ( localPos.x >= width * 0.5 )
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	public int getMarkerRange()
	{
		return 1;
	}

	
	public boolean isWhitespace()
	{
		return true;
	}
}
