//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeBox;
import BritefuryJ.DocPresent.StyleParams.ShapeStyleParams;
import BritefuryJ.Graphics.Painter;

public class DPBox extends DPContentLeaf
{
	private double minWidth, minHeight;
	
	
	public DPBox(String textRepresentation, double minWidth, double minHeight)
	{
		this( ShapeStyleParams.defaultStyleParams, textRepresentation, minWidth, minHeight );
	}
	
	public DPBox(ShapeStyleParams styleParams, String textRepresentation, double minWidth, double minHeight)
	{
		super( styleParams, textRepresentation );
		
		this.minWidth = minWidth;
		this.minHeight = minHeight;
		
		layoutNode = new LayoutNodeBox( this );
	}
	
	
	//
	//
	// Box size
	//
	//
	
	public double getMinWidth()
	{
		return minWidth;
	}
	
	public double getMinHeight()
	{
		return minHeight;
	}



	public boolean isRedrawRequiredOnHover()
	{
		ShapeStyleParams s = (ShapeStyleParams)styleParams;
		return super.isRedrawRequiredOnHover()  ||  s.getHoverPainter() != null;
	}
	

	protected void draw(Graphics2D graphics)
	{
		ShapeStyleParams p = (ShapeStyleParams)styleParams;
		
		Painter painter;
		if ( isHoverActive() )
		{
			Painter hoverPainter = p.getHoverPainter();
			painter = hoverPainter != null  ?  hoverPainter  :  p.getPainter();
		}
		else
		{
			painter = p.getPainter();
		}
		
		if ( painter != null )
		{
			painter.drawShape( graphics, new Rectangle2D.Double( 0.0, 0.0, getActualWidth(), getActualHeight() ) );
		}
	}
}
