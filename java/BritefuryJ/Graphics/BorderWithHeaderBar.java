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
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Interactor.HoverElementInteractor;
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

	private class HeaderPainterAndInteractor implements ElementPainter, HoverElementInteractor
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

			if (element.isHoverActive()  &&  headerHoverPaint != null)
			{
				graphics.setPaint(headerHoverPaint);
			}
			else
			{
				graphics.setPaint( headerPaint );
			}
			graphics.fill( headerShape );

			graphics.setPaint( prevPaint );
		}

		@Override
		public void draw(LSElement element, Graphics2D graphics)
		{
		}

		@Override
		public void pointerEnter(LSElement element, PointerMotionEvent event)
		{
			if (headerHoverPaint != null)
			{
				element.queueFullRedraw();
			}
		}

		@Override
		public void pointerLeave(LSElement element, PointerMotionEvent event)
		{
			if (headerHoverPaint != null)
			{
				element.queueFullRedraw();
			}
		}
	}

	private HeaderPainterAndInteractor elementInteractor = new HeaderPainterAndInteractor();

	private AbstractBorder border;
	private Paint headerPaint, headerHoverPaint;
	
	
	public BorderWithHeaderBar(AbstractBorder border, Paint headerPaint)
	{
		this.border = border;
		this.headerPaint = headerPaint;
		this.headerHoverPaint = null;
	}
	
	public BorderWithHeaderBar(AbstractBorder border, Paint headerPaint, Paint headerHoverPaint)
	{
		this.border = border;
		this.headerPaint = headerPaint;
		this.headerHoverPaint = headerHoverPaint;
	}


	public Pres surround(Object header, Object body)
	{
		Pres headerBin = new Bin( header ).withPainter( elementInteractor ).withElementInteractor(elementInteractor);
		Column contents = new Column( new Object[] { headerBin.alignHExpand(), body } );
		return border.surroundAndClip( contents ).withProperty( borderKey, null );
	}


	public Pres surroundHeader(Object header)
	{
		Pres headerBin = new Bin( header ).withPainter( elementInteractor ).withElementInteractor(elementInteractor);
		return border.surroundAndClip( headerBin.alignHExpand() ).withProperty( borderKey, null );
	}
}
