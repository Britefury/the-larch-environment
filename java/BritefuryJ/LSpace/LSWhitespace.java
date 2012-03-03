//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeWhitespace;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.StyleParams.ContentLeafStyleParams;

public class LSWhitespace extends LSContentLeaf
{
	protected double width;
	
	
	public LSWhitespace(String whitespace)
	{
		this( ContentLeafStyleParams.defaultStyleParams, whitespace, 0.0 );
	}
	
	public LSWhitespace(ContentLeafStyleParams styleParams, String whitespace)
	{
		this(styleParams, whitespace, 0.0 );
	}
	
	public LSWhitespace(String whitespace, double width)
	{
		this( ContentLeafStyleParams.defaultStyleParams, whitespace, width );
	}

	public LSWhitespace(ContentLeafStyleParams styleParams, String whitespace, double width)
	{
		super(styleParams, whitespace );
		this.width = width;
		
		layoutNode = new LayoutNodeWhitespace( this );
	}
	
	
	
	//
	//
	// Whitespace width
	//
	//
	
	public double getWhitespaceWidth()
	{
		return width;
	}

	
	
	//
	//
	// SELECTION METHODS
	//
	//
	
	public void drawSelection(Graphics2D graphics, Marker from, Marker to)
	{
		double width = getActualWidth();
		double height = getActualHeight();
		AffineTransform current = pushGraphicsTransform( graphics );
		int startIndex = from != null  ?  from.getIndex()  :  0;
		int endIndex = to != null  ?  to.getIndex()  :  1;
		double startX = startIndex == 0  ?  0.0  :  width;
		double endX = endIndex == 0  ?  0.0  :  width;
		Rectangle2D.Double shape = new Rectangle2D.Double( startX, 0.0, endX - startX, height );
		graphics.fill( shape );
		popGraphicsTransform( graphics, current );
	}
	
	
	
	//
	//
	// WHITESPACE METHODS
	//
	//
	
	protected boolean isWhitespace()
	{
		return true;
	}
}
