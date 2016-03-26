//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.UI;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;

import BritefuryJ.LSpace.Anchor;
import BritefuryJ.LSpace.ElementPainter;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.LSpace.PresentationPopupWindow;
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
	private static final double ARROW_OVERLAP_THRESHOLD = 1.0;
	
	private enum ArrowEdge
	{
		TOP,
		BOTTOM,
		LEFT,
		RIGHT,
	}
	
	private static abstract class AbstractBubblePainter implements ElementPainter
	{
		protected static class ArrowLocation
		{
			public ArrowEdge arrowEdge;
			public Point2 arrowPos;
			
			public ArrowLocation(ArrowEdge arrowEdge, Point2 arrowPos)
			{
				this.arrowPos = arrowPos;
				this.arrowEdge = arrowEdge;
			}
		}
		
		private double radius, arrowLength, arrowWidth;
		
		
		private AbstractBubblePainter(double radius, double arrowLength, double arrowWidth)
		{
			this.radius = radius;
			this.arrowLength = arrowLength;
			this.arrowWidth = arrowWidth;
		}
		
		
		protected abstract ArrowLocation computeArrowLocation(LSElement popupElement);
		
		
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
			
			ArrowLocation arrowLoc = computeArrowLocation( element );

			double bubblePosX = arrowLength;
			double bubblePosY = arrowLength;
					
			double bubbleWidth = size.x - arrowLength * 2.0;
			double bubbleHeight = size.y - arrowLength * 2.0;

			Point2 relativeArrowPos = null;
			if ( arrowLoc.arrowPos != null )
			{
				relativeArrowPos = new Point2( arrowLoc.arrowPos.x - posOnScreen.x - bubblePosX, arrowLoc.arrowPos.y - posOnScreen.y - bubblePosY );
			}
			
			// PATH GOES COUNTER-CLOCKWISE
			Path2D.Double path = new Path2D.Double();
			
			// Compute what is at each corner
			boolean arrowAtTopLeft = arrowLoc.arrowEdge == ArrowEdge.LEFT && relativeArrowPos.y < cornerOverlapThreshhold  ||
					arrowLoc.arrowEdge == ArrowEdge.TOP && relativeArrowPos.x < cornerOverlapThreshhold;
			boolean arrowAtTopRight = arrowLoc.arrowEdge == ArrowEdge.RIGHT && relativeArrowPos.y < cornerOverlapThreshhold  ||
					arrowLoc.arrowEdge == ArrowEdge.TOP && relativeArrowPos.x > ( bubbleWidth - cornerOverlapThreshhold );
			boolean arrowAtBottomLeft = arrowLoc.arrowEdge == ArrowEdge.LEFT && relativeArrowPos.y > ( bubbleHeight - cornerOverlapThreshhold )  ||
					arrowLoc.arrowEdge == ArrowEdge.BOTTOM && relativeArrowPos.x < cornerOverlapThreshhold;
			boolean arrowAtBottomRight = arrowLoc.arrowEdge == ArrowEdge.RIGHT && relativeArrowPos.y > ( bubbleHeight - cornerOverlapThreshhold )  ||
					arrowLoc.arrowEdge == ArrowEdge.BOTTOM && relativeArrowPos.x > ( bubbleWidth - cornerOverlapThreshhold );

			
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
			if ( !arrowAtTopLeft  &&  !arrowAtBottomLeft  &&  arrowLoc.arrowEdge == ArrowEdge.LEFT )
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
			if ( !arrowAtBottomLeft  &&  !arrowAtBottomRight  &&  arrowLoc.arrowEdge == ArrowEdge.BOTTOM )
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
			if ( !arrowAtBottomRight  &&  !arrowAtTopRight  &&  arrowLoc.arrowEdge == ArrowEdge.RIGHT )
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
			if ( !arrowAtTopRight  &&  !arrowAtTopLeft  &&  arrowLoc.arrowEdge == ArrowEdge.TOP )
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
	
	
	
	private static class ElementBubblePainter extends AbstractBubblePainter
	{
		private LSElement targetElement;
		
		
		private ElementBubblePainter(LSElement targetElement, double radius, double arrowLength, double arrowWidth)
		{
			super( radius, arrowLength, arrowWidth );
			this.targetElement = targetElement;
		}
		
		
		@Override
		protected ArrowLocation computeArrowLocation(LSElement popupElement)
		{
			AABox2 popupAABoxScreen = popupElement.getVisibleBoxRelativeToScreen();

			ArrowEdge arrowEdge;
			Point2 arrowPos;
			
			if ( targetElement != null  &&  targetElement.isRealised() )
			{
				AABox2 targetAABoxScreen = targetElement.getVisibleBoxRelativeToScreen();
				AABox2 intersection = popupAABoxScreen.intersection( targetAABoxScreen );
				Vector2 intersectionSz = intersection.getSize();
				double minOverlap = Math.min( intersectionSz.x, intersectionSz.y );
				if ( minOverlap > ARROW_OVERLAP_THRESHOLD )
				{
					// More than one pixel overlap - don't draw an arrow
					arrowEdge = null;
					arrowPos = null;
					System.out.println("BubblePopup.ElementBubblePainter.computeArrowLocation: Hiding arrow; targetBox="+targetAABoxScreen);
					System.out.println("BubblePopup.ElementBubblePainter.computeArrowLocation: Hiding arrow; targetSz="+targetElement.getActualSize());
					System.out.println("BubblePopup.ElementBubblePainter.computeArrowLocation: Hiding arrow; popupBox ="+popupAABoxScreen);
					System.out.println("BubblePopup.ElementBubblePainter.computeArrowLocation: Hiding arrow; intersect="+intersection);
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
			
			return new ArrowLocation( arrowEdge, arrowPos );
		}
	}
	
	
	
	private static class MouseBubblePainter extends AbstractBubblePainter
	{
		private Point2 mousePos;
		
		
		private MouseBubblePainter(Point2  mousePos, double radius, double arrowLength, double arrowWidth)
		{
			super( radius, arrowLength, arrowWidth );
			this.mousePos = mousePos;
		}
		
		
		@Override
		protected ArrowLocation computeArrowLocation(LSElement popupElement)
		{
			AABox2 popupAABoxScreen = popupElement.getAABoxRelativeToScreen();

			ArrowEdge arrowEdge = null;
			Point2 arrowPos = null;
			
			Point2 closestPointOnPopup = popupAABoxScreen.closestPointTo( mousePos );
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
			
			return new ArrowLocation( arrowEdge, arrowPos );
		}
	}
	
	
	
	private static class ElementBubblePres extends CompositePres
	{
		private Pres contents;
		private LSElement targetElement;
		
		private ElementBubblePres(Object contents, LSElement targetElement)
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
			
			ElementBubblePainter painter = new ElementBubblePainter( targetElement, radius, arrowLength, arrowWidth );
			
			double padding = border + arrowLength;
			
			return new Bin( contents.pad( padding, padding ) ).withPainter( painter );
		}
	}
	
	
	private static class MouseBubblePres extends CompositePres
	{
		private Pres contents;
		private Point2 mousePos;
		
		private MouseBubblePres(Object contents, Point2 mousePos)
		{
			this.contents = Pres.coerce( contents );
			this.mousePos = mousePos;
		}

		@Override
		public Pres pres(PresentationContext ctx, StyleValues style)
		{
			double border = style.get( UI.bubblePopupBorderWidth, double.class );
			double radius = style.get( UI.bubblePopupCornerRadius, double.class );
			double arrowLength = style.get( UI.bubblePopupArrowLength, double.class );
			double arrowWidth = style.get( UI.bubblePopupArrowWidth, double.class );
			
			MouseBubblePainter painter = new MouseBubblePainter( mousePos, radius, arrowLength, arrowWidth );
			
			double padding = border + arrowLength;
			
			return new Bin( contents.pad( padding, padding ) ).withPainter( painter );
		}
	}
	
	
	
	public static PresentationPopupWindow popupInBubbleAdjacentTo(Object contents, LSElement target, Anchor targetAnchor, boolean closeAutomatically, boolean requestFocus)
	{
		ElementBubblePres b = new ElementBubblePres( contents, target );
		
		return b.popup( target, targetAnchor, targetAnchor.opposite(), closeAutomatically, requestFocus );
	}

	public static PresentationPopupWindow popupInBubbleAdjacentToMouse(Object contents, LSElement target, Anchor popupAnchor, boolean closeAutomatically, boolean requestFocus)
	{
        Point mouse = MouseInfo.getPointerInfo().getLocation();
        Point2 mousePos = new Point2((double)mouse.x, (double)mouse.y);
		MouseBubblePres b = new MouseBubblePres( contents, mousePos );
		
		return b.popupAtMousePosition( target, popupAnchor, closeAutomatically, requestFocus );
	}
}
