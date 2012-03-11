//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;

import BritefuryJ.LSpace.Anchor;
import BritefuryJ.LSpace.ElementPainter;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Bin;

public class BubblePopup
{
	private static final double BORDER = 5.0;
	private static final double RADIUS = BORDER * 2.0;
	private static final double ARROW_LENGTH = 15.0;
	private static final double ARROW_WIDTH = 15.0;
	
	private static class BubblePainter implements ElementPainter
	{
		private LSElement targetElement;
		private Anchor targetAnchor;
		
		
		private BubblePainter(LSElement targetElement, Anchor targetAnchor)
		{
			this.targetElement = targetElement;
			this.targetAnchor = targetAnchor;
		}
		
		
		@Override
		public void drawBackground(LSElement element, Graphics2D graphics)
		{
			Paint p = graphics.getPaint();
			Stroke s = graphics.getStroke();
			Shape c = graphics.getClip();
			AffineTransform t = graphics.getTransform();
			
			Vector2 size = element.getAllocSize();

			Vector2 targetAnchorRelativeToPopup;
			
			if ( targetElement != null  &&  targetElement.isRealised() )
			{
				Point2 posOnScreen = element.getLocalPointRelativeToScreen( new Point2() );

				Point2 targetAnchorLocal = targetElement.getLocalAnchor( targetAnchor );
				Point2 targetAnchorScreen = targetElement.getLocalPointRelativeToScreen( targetAnchorLocal );
				
				targetAnchorRelativeToPopup = targetAnchorScreen.sub( posOnScreen );
			}
			else
			{
				targetAnchorRelativeToPopup = new Vector2( size.x * 0.5, 0.0 );
			}
			
			
			Path2D.Double path = new Path2D.Double();
			path.append( new Arc2D.Double( 0.0, ARROW_LENGTH, RADIUS, RADIUS, 90.0, 90.0, Arc2D.OPEN ), false );
			path.append( new Arc2D.Double( 0.0, size.y - RADIUS, RADIUS, RADIUS, 180.0, 90.0, Arc2D.OPEN ), true );
			path.append( new Arc2D.Double( size.x - RADIUS, size.y - RADIUS, RADIUS, RADIUS, 270.0, 90.0, Arc2D.OPEN ), true );
			path.append( new Arc2D.Double( 0.0, ARROW_LENGTH, RADIUS, RADIUS, 0.0, 90.0, Arc2D.OPEN ), true );
			path.lineTo( targetAnchorRelativeToPopup.x + ARROW_WIDTH * 0.5, ARROW_LENGTH );
			path.lineTo( targetAnchorRelativeToPopup.x, 0.0 );
			path.lineTo( targetAnchorRelativeToPopup.x - ARROW_WIDTH * 0.5, ARROW_LENGTH );
			path.closePath();
			
			
			graphics.setPaint( Color.WHITE );
			graphics.fill( path );
			
			graphics.clip( path );
			
			graphics.setPaint( new Color( 0.75f, 0.75f, 0.75f ) );
			graphics.setStroke( new BasicStroke( 2.0f ) );
			
			graphics.translate( 0.0, 1.0 );
			graphics.draw( path );
			
			graphics.setTransform( t );
			graphics.setClip( c );
			
			graphics.setPaint( new Color( 0.5f, 0.5f, 0.5f ) );
			graphics.setStroke( new BasicStroke( 1.0f ) );
			
			graphics.draw( path );
			
			graphics.setPaint( p );
			graphics.setStroke( s );
		}

		@Override
		public void draw(LSElement element, Graphics2D graphics)
		{
		}
	}
	
	
	
	public static void popupInBubbleOver(Object contents, LSElement target)
	{
		BubblePainter painter = new BubblePainter( target, Anchor.BOTTOM );
		
		Pres b = new Bin( Pres.coerce( contents ).pad( BORDER, BORDER, BORDER + ARROW_LENGTH, BORDER ) ).withPainter( painter );
		
		b.popup( target, Anchor.BOTTOM, Anchor.TOP, true, true );
	}
}
