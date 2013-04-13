//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Graphics;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import BritefuryJ.LSpace.ElementPainter;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSElement.PropertyValue;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Column;

public class BorderWithHeaderBar
{
	private static Object borderKey = new Object();
	
	private ElementPainter headerPainter = new ElementPainter()
	{
		@Override
		public void drawBackground(LSElement element, Graphics2D graphics)
		{
			PropertyValue propVal = element.findPropertyInAncestors( borderKey );
			LSElement borderElement = propVal.getElement();
			Xform2 x = element.getLocalToAncestorXform( borderElement );
			Vector2 offset = x.translation;
			
			double width = borderElement.getActualWidth();
			double height = element.getActualHeight() + offset.y;
			
			Paint prevPaint = graphics.getPaint();
			
			Shape headerShape = new Rectangle2D.Double( -offset.x, -offset.y, width, height );
			
			graphics.setPaint( headerPaint );
			graphics.fill( headerShape );
			
			graphics.setPaint( prevPaint );
		}

		@Override
		public void draw(LSElement element, Graphics2D graphics)
		{
		}
	};
	
	
	private AbstractBorder border;
	private Paint headerPaint;
	
	
	public BorderWithHeaderBar(AbstractBorder border, Paint headerPaint)
	{
		this.border = border;
		this.headerPaint = headerPaint;
	}
	
	
	public Pres surround(Object header, Object body)
	{
		Pres headerBin = new Bin( header ).withPainter( headerPainter );
		Column contents = new Column( new Object[] { headerBin, body } );		
		return border.surroundAndClip( contents ).withProperty( borderKey, null );
	}
}
