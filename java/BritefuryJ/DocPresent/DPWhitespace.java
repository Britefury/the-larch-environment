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

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeWhitespace;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleParams.ContentLeafStyleParams;

public class DPWhitespace extends DPContentLeaf
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
	
	protected DPWhitespace(DPWhitespace element)
	{
		super( element );
		
		this.width = element.width;
		
		layoutNode = new LayoutNodeWhitespace( this );
	}
	
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPWhitespace clone = new DPWhitespace( this );
		clone.clonePostConstuct( this );
		return clone;
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
		double width = getWidth();
		double height = getHeight();
		AffineTransform current = pushGraphicsTransform( graphics );
		int startIndex = from != null  ?  from.getIndex()  :  0;
		int endIndex = to != null  ?  to.getIndex()  :  1;
		double startX = startIndex == 0  ?  0.0  :  width;
		double endX = endIndex == 0  ?  0.0  :  width;
		Rectangle2D.Double shape = new Rectangle2D.Double( startX, 0.0, endX - startX, height );
		graphics.fill( shape );
		popGraphicsTransform( graphics, current );
	}
}
