//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.LayoutTree.LayoutNodeBox;
import BritefuryJ.LSpace.StyleParams.ShapeStyleParams;

public class LSBox extends LSBlank
{
	private double minWidth, minHeight;
	
	
	public LSBox(double minWidth, double minHeight)
	{
		this( ShapeStyleParams.defaultStyleParams, minWidth, minHeight );
	}
	
	public LSBox(ShapeStyleParams styleParams, double minWidth, double minHeight)
	{
		super( styleParams );
		
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
