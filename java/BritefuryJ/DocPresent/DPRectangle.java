//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeRectangle;
import BritefuryJ.DocPresent.StyleParams.ShapeStyleParams;

public class DPRectangle extends DPContentLeaf
{
	private double minWidth, minHeight;
	
	
	public DPRectangle(String textRepresentation, double minWidth, double minHeight)
	{
		this( ShapeStyleParams.defaultStyleParams, textRepresentation, minWidth, minHeight );
	}
	
	public DPRectangle(ShapeStyleParams styleParams, String textRepresentation, double minWidth, double minHeight)
	{
		super( styleParams, textRepresentation );
		
		this.minWidth = minWidth;
		this.minHeight = minHeight;
		
		layoutNode = new LayoutNodeRectangle( this );
	}
	
	
	
	public double getMinWidth()
	{
		return minWidth;
	}
	
	public double getMinHeight()
	{
		return minHeight;
	}



	protected void draw(Graphics2D graphics)
	{
		ShapeStyleParams p = (ShapeStyleParams)styleParams;
		
		p.getPainter().drawShape( graphics, new Rectangle2D.Double( 0.0, 0.0, getAllocationX(), getAllocationY() ) );
	}
}
