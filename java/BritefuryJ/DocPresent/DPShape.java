//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeShape;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleParams.ShapeStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class DPShape extends DPContentLeaf
{
	private Shape shape;
	private AABox2 bounds;
	
	
	public DPShape(String textRepresentation, Shape shape)
	{
		this( ShapeStyleParams.defaultStyleParams, textRepresentation, shape );
	}
	
	public DPShape(ShapeStyleParams styleParams, String textRepresentation, Shape shape)
	{
		super( styleParams, textRepresentation );
		
		this.shape = shape;
		
		Rectangle2D bounds2D = shape.getBounds2D();

		bounds = new AABox2( bounds2D.getMinX(), bounds2D.getMinY(), bounds2D.getMaxX(), bounds2D.getMaxY() );
		
		layoutNode = new LayoutNodeShape( this );
	}
	
	

	public AABox2 getShapeBounds()
	{
		return bounds;
	}
	
	public AABox2 getLocalAABox()
	{
		return bounds;
	}


	public boolean containsParentSpacePoint(Point2 parentPos)
	{
		Point2 localPos = getParentToLocalXform().transform( parentPos );
		return containsLocalSpacePoint( localPos );
	}

	public boolean containsLocalSpacePoint(Point2 localPos)
	{
		return shape.contains( new Point2D.Double( localPos.x, localPos.y ) );
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
		if ( testFlag( FLAG_HOVER ) )
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
			painter.drawShape( graphics, shape );
		}
	}
}
