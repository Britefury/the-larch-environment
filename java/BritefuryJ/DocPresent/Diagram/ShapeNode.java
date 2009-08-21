//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Diagram;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class ShapeNode extends DiagramNode
{
	protected Shape shape;
	protected AABox2 parentSpaceBox;
	
	
	public ShapeNode(Shape shape)
	{
		super();
		this.shape = shape;
		Rectangle2D.Double rect = (Rectangle2D.Double)shape.getBounds2D();
		parentSpaceBox = new AABox2( rect.x, rect.y, rect.x + rect.width, rect.y + rect.height );
	}


	public void draw(Graphics2D graphics, DrawContext context)
	{
		Paint fillPaint = context.getFillPaint();
		Paint strokePaint = context.getStrokePaint();
		if ( fillPaint != null )
		{
			graphics.setPaint( fillPaint );
			graphics.fill( shape );
		}
		if ( strokePaint != null )
		{
			graphics.setPaint( strokePaint );
			graphics.draw( shape );
		}
	}


	public AABox2 getParentSpaceBoundingBox()
	{
		return parentSpaceBox;
	}

	
	public boolean containsLocalSpacePoint(Point2 localPos)
	{
		if ( parentSpaceBox.containsPoint( localPos ) )
		{
			return shape.contains( localPos.x, localPos.y );
		}
		else
		{
			return false;
		}
	}


	public boolean containsParentSpacePoint(Point2 parentPos)
	{
		if ( parentSpaceBox.containsPoint( parentPos ) )
		{
			return shape.contains( parentPos.x, parentPos.y );
		}
		else
		{
			return false;
		}
	}
}
