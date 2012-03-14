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
import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Pres.CompositePres;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.StyleSheet.StyleValues;

public class BubblePopup
{
	private enum ArrowEdge
	{
		TOP,
		BOTTOM,
		LEFT,
		RIGHT,
	}
	
	private static class BubblePainter implements ElementPainter
	{
		private LSElement targetElement;
		private double radius, arrowLength, arrowWidth;
		
		
		private BubblePainter(LSElement targetElement, double radius, double arrowLength, double arrowWidth)
		{
			this.targetElement = targetElement;
			this.radius = radius;
			this.arrowLength = arrowLength;
			this.arrowWidth = arrowWidth;
		}
		
		
		@Override
		public void drawBackground(LSElement element, Graphics2D graphics)
		{
			double cornerOverlapThreshhold = radius + arrowWidth * 0.5;
			
			Paint p = graphics.getPaint();
			Stroke s = graphics.getStroke();
			Shape c = graphics.getClip();
			AffineTransform t = graphics.getTransform();
			
			Vector2 size = element.getAllocSize();

			Point2 posOnScreen = element.getLocalPointRelativeToScreen( new Point2() );
			AABox2 popupAABoxScreen = element.getAABoxRelativeToScreen();

			ArrowEdge arrowEdge;
			Point2 arrowPos;
			
			if ( targetElement != null  &&  targetElement.isRealised() )
			{
				AABox2 targetAABoxScreen = targetElement.getAABoxRelativeToScreen();
				AABox2 intersection = popupAABoxScreen.intersection( targetAABoxScreen );
				Vector2 intersectionSz = intersection.getSize();
				double minOverlap = Math.min( intersectionSz.x, intersectionSz.y );
				if ( minOverlap > 1.0 )
				{
					// More than one pixel overlap - don't draw an arrow
					arrowEdge = null;
					arrowPos = null;
				}
				else
				{
					Point2[] closestPoints = popupAABoxScreen.closestPoints( targetAABoxScreen );
					Point2 closestPointOnPopup = closestPoints[0];
					arrowPos = closestPointOnPopup;
					
					// Determine which edge the arrow should come from
					// First, get the distance to each edge
					double left = Math.abs( closestPointOnPopup.x - popupAABoxScreen.getLowerX() );
					double right = Math.abs( closestPointOnPopup.x - popupAABoxScreen.getUpperX() );
					double top = Math.abs( closestPointOnPopup.y - popupAABoxScreen.getLowerY() );
					double bottom = Math.abs( closestPointOnPopup.y - popupAABoxScreen.getUpperY() );
					
					// Determine which is closest
					double d = left;
					arrowEdge = ArrowEdge.LEFT;
					
					if ( right < d )
					{
						d = right;
						arrowEdge = ArrowEdge.RIGHT;
					}
					
					if ( top < d )
					{
						d = top;
						arrowEdge = ArrowEdge.TOP;
					}
					
					if ( bottom < d )
					{
						d = bottom;
						arrowEdge = ArrowEdge.BOTTOM;
					}
				}
			}
			else
			{
				// Target element unavailable - don't draw 
				arrowEdge = null;
				arrowPos = null;
			}
			

			double bubblePosX = arrowLength;
			double bubblePosY = arrowLength;
					
			double bubbleWidth = size.x - arrowLength * 2.0;
			double bubbleHeight = size.y - arrowLength * 2.0;

			Point2 relativeArrowPos = null;
			if ( arrowPos != null )
			{
				relativeArrowPos = new Point2( arrowPos.x - posOnScreen.x - bubblePosX, arrowPos.y - posOnScreen.y - bubblePosY );
			}
			
			// PATH GOES COUNTER-CLOCKWISE
			Path2D.Double path = new Path2D.Double();
			
			// Compute what is at each corner
			boolean arrowAtTopLeft = arrowEdge == ArrowEdge.LEFT && relativeArrowPos.y < cornerOverlapThreshhold  ||
					arrowEdge == ArrowEdge.TOP && relativeArrowPos.x < cornerOverlapThreshhold;
			boolean arrowAtTopRight = arrowEdge == ArrowEdge.RIGHT && relativeArrowPos.y < cornerOverlapThreshhold  ||
					arrowEdge == ArrowEdge.TOP && relativeArrowPos.x > ( bubbleWidth - cornerOverlapThreshhold );
			boolean arrowAtBottomLeft = arrowEdge == ArrowEdge.LEFT && relativeArrowPos.y > ( bubbleHeight - cornerOverlapThreshhold )  ||
					arrowEdge == ArrowEdge.BOTTOM && relativeArrowPos.x < cornerOverlapThreshhold;
			boolean arrowAtBottomRight = arrowEdge == ArrowEdge.RIGHT && relativeArrowPos.y > ( bubbleHeight - cornerOverlapThreshhold )  ||
					arrowEdge == ArrowEdge.BOTTOM && relativeArrowPos.x > ( bubbleWidth - cornerOverlapThreshhold );

			
			// Top left corner
			if ( arrowAtTopLeft )
			{
				// Arrow at corner
				path.moveTo( bubblePosX + arrowWidth * 0.5, bubblePosY );
				path.lineTo( 0.0, 0.0 );
				path.lineTo( bubblePosX, bubblePosY + arrowWidth * 0.5 );
			}
			else
			{
				// Round corner
				path.append( new Arc2D.Double( bubblePosX, bubblePosY, radius, radius, 90.0, 90.0, Arc2D.OPEN ), false );
			}
			
			
			// Left edge
			if ( !arrowAtTopLeft  &&  !arrowAtBottomLeft  &&  arrowEdge == ArrowEdge.LEFT )
			{
				path.lineTo( bubblePosX, bubblePosY + relativeArrowPos.y - arrowWidth * 0.5 );
				path.lineTo( 0.0, bubblePosY + relativeArrowPos.y );
				path.lineTo( bubblePosX, bubblePosY + relativeArrowPos.y + arrowWidth * 0.5 );
			}
			
			
			// Bottom left corner
			if ( arrowAtBottomLeft )
			{
				// Arrow at corner
				path.lineTo( bubblePosX, bubblePosY + bubbleHeight - arrowWidth * 0.5 );
				path.lineTo( 0.0, size.y );
				path.lineTo( bubblePosX + arrowWidth * 0.5, bubblePosY + bubbleHeight );
			}
			else
			{
				// Round corner
				path.append( new Arc2D.Double( bubblePosX, bubblePosY + bubbleHeight - radius, radius, radius, 180.0, 90.0, Arc2D.OPEN ), true );
			}
			
			
			// Bottom edge
			if ( !arrowAtBottomLeft  &&  !arrowAtBottomRight  &&  arrowEdge == ArrowEdge.BOTTOM )
			{
				path.lineTo( bubblePosX + relativeArrowPos.x - arrowWidth * 0.5, bubblePosY + bubbleHeight );
				path.lineTo( bubblePosX + relativeArrowPos.x, size.y );
				path.lineTo( bubblePosX + relativeArrowPos.x + arrowWidth * 0.5, bubblePosY + bubbleHeight );
			}
			
			
			// Bottom right corner
			if ( arrowAtBottomRight )
			{
				// Arrow at corner
				path.lineTo( bubblePosX + bubbleWidth - arrowWidth * 0.5, bubblePosY + bubbleHeight );
				path.lineTo( size.x, size.y );
				path.lineTo( bubblePosX + bubbleWidth, bubblePosY + bubbleHeight - arrowWidth * 0.5 );
			}
			else
			{
				// Round corner
				path.append( new Arc2D.Double( bubblePosX + bubbleWidth - radius, bubblePosY + bubbleHeight - radius, radius, radius, 270.0, 90.0, Arc2D.OPEN ), true );
			}
			
			
			// Right edge
			if ( !arrowAtBottomRight  &&  !arrowAtTopRight  &&  arrowEdge == ArrowEdge.RIGHT )
			{
				path.lineTo( bubblePosX + bubbleWidth, bubblePosY + relativeArrowPos.y + arrowWidth * 0.5 );
				path.lineTo( size.x, bubblePosY + relativeArrowPos.y );
				path.lineTo( bubblePosX + bubbleWidth, bubblePosY + relativeArrowPos.y - arrowWidth * 0.5 );
			}
			
			
			// Top right corner
			if ( arrowAtTopRight )
			{
				// Arrow at corner
				path.lineTo( bubblePosX + bubbleWidth, bubblePosY + arrowWidth * 0.5 );
				path.lineTo( size.x, 0.0 );
				path.lineTo( bubblePosX + bubbleWidth - arrowWidth * 0.5, bubblePosY );
			}
			else
			{
				// Round corner
				path.append( new Arc2D.Double( bubblePosX + bubbleWidth - radius, bubblePosY, radius, radius, 0.0, 90.0, Arc2D.OPEN ), true );
			}
			
			
			// Top edge
			if ( !arrowAtTopRight  &&  !arrowAtTopLeft  &&  arrowEdge == ArrowEdge.TOP )
			{
				path.lineTo( bubblePosX + relativeArrowPos.x + arrowWidth * 0.5, bubblePosY );
				path.lineTo( bubblePosX + relativeArrowPos.x, 0.0 );
				path.lineTo( bubblePosX + relativeArrowPos.x - arrowWidth * 0.5, bubblePosY );
			}
			
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
	
	
	
	private static class BubblePres extends CompositePres
	{
		private Pres contents;
		private LSElement targetElement;
		
		private BubblePres(Object contents, LSElement targetElement)
		{
			this.contents = Pres.coerce( contents );
			this.targetElement = targetElement;
		}

		@Override
		public Pres pres(PresentationContext ctx, StyleValues style)
		{
			double border = style.get( UI.bubblePopupBorderWidth, double.class );
			double radius = style.get( UI.bubblePopupCornerRadius, double.class );
			double arrowLength = style.get( UI.bubblePopupArrowLength, double.class );
			double arrowWidth = style.get( UI.bubblePopupArrowWidth, double.class );
			
			BubblePainter painter = new BubblePainter( targetElement, radius, arrowLength, arrowWidth );
			
			double padding = border + arrowLength;
			
			return new Bin( contents.pad( padding, padding ) ).withPainter( painter );
		}
	}
	
	
	
	public static PresentationComponent.PresentationPopup popupInBubbleAdjacentTo(Object contents, LSElement target, Anchor targetAnchor, boolean bCloseOnLoseFocus, boolean bRequestFocus)
	{
		BubblePres b = new BubblePres( contents, target );
		
		return b.popup( target, targetAnchor, targetAnchor.opposite(), bCloseOnLoseFocus, bRequestFocus );
	}
}
