//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Util;

import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;

public class FiniteViewportBehaviour implements Range.RangeListener
{
	public static interface FiniteViewport
	{
		public Vector2 getFiniteViewportSize();
		public Vector2 getFiniteWorldSize();
		public void onFiniteViewportXformModified();
	}
	
	
	private Range xRange, yRange;
	private Xform2 worldToViewSpace;
	
	private FiniteViewport viewport;
	
	private boolean bIgnoreRangeEvents = false;
	
	
	public FiniteViewportBehaviour(FiniteViewport viewport, Range xRange, Range yRange, Xform2 worldToViewSpace)
	{
		this.viewport = viewport;
		this.xRange = xRange;
		this.yRange = yRange;

		if ( xRange != null )
		{
			xRange.addListener( this );
		}

		if ( yRange != null )
		{
			yRange.addListener( this );
		}

		this.worldToViewSpace = worldToViewSpace;
	}
	
	
	public Xform2 getWorldToViewXform()
	{
		return worldToViewSpace.copy();
	}
	
	public void setWorldToViewXform(Xform2 x)
	{
		worldToViewSpace = x.copy();
		onXformModified();
	}
	
	
	public Range getXRange()
	{
		return xRange;
	}
	
	public Range getYRange()
	{
		return yRange;
	}
	
	
	
	public void oneToOne()
	{
		worldToViewSpace.scale = 1.0;
		onXformModified();
	}
	
	public void resetXform()
	{
		setWorldToViewXform( new Xform2() );
	}
	
	
	public void focusOn(AABox2 box)
	{
		worldToViewSpace.scale = 1.0;
		Point2 topLeft = box.getLower();
		Point2 bottomRight = box.getUpper();
		Point2 centre = Point2.average( topLeft, bottomRight );
		Point2 topLeftCorner = centre.sub( viewport.getFiniteViewportSize().mul( 0.5 ) );
		worldToViewSpace.translation = topLeftCorner.toVector2().negate();
		onXformModified();
	}
	
	public void zoomToFit()
	{
		Vector2 viewportSize = viewport.getFiniteViewportSize();
		Vector2 worldSize = viewport.getFiniteWorldSize();

		double ax = worldSize.x == 0.0  ?  1.0  :  worldSize.x;
		double ay = worldSize.y == 0.0  ?  1.0  :  worldSize.y;
		
		worldToViewSpace.translation = new Vector2();
		worldToViewSpace.scale = Math.min( viewportSize.x / ax, viewportSize.y / ay );
		worldToViewSpace.scale = worldToViewSpace.scale == 0.0  ?  1.0  :  worldToViewSpace.scale;
		onXformModified();
	}
	
	
	public Xform2 ensureRegionVisible(AABox2 box)
	{
		AABox2 viewportBox = new AABox2( new Point2(), viewport.getFiniteViewportSize() );
		
		boolean bScroll = !box.intersects( viewportBox );
		
		if ( !bScroll )
		{
			if ( box.getWidth() < viewportBox.getWidth() )
			{
				if ( box.getLowerX() < viewportBox.getLowerX()  ||  box.getUpperX() > viewportBox.getUpperX() )
				{
					bScroll = true;
				}
			}
			else
			{
				if ( box.getUpperX() < viewportBox.getLowerX()  ||  box.getLowerX() > viewportBox.getUpperX() )
				{
					bScroll = true;
				}
			}
		}
		
		if ( !bScroll )
		{
			if ( box.getHeight() < viewportBox.getHeight() )
			{
				if ( box.getLowerY() < viewportBox.getLowerY()  ||  box.getUpperY() > viewportBox.getUpperY() )
				{
					bScroll = true;
				}
			}
			else
			{
				if ( box.getUpperY() < viewportBox.getLowerY()  ||  box.getLowerY() > viewportBox.getUpperY() )
				{
					bScroll = true;
				}
			}
		}
		
		if ( bScroll )
		{
			double deltaX = 0.0, deltaY = 0.0;
			
			if ( box.getUpperX() < viewportBox.getLowerX() )
			{
				deltaX = viewportBox.getLowerX() - box.getLowerX();
			}
			else if ( box.getLowerX() > viewportBox.getUpperX() )
			{
				deltaX = viewportBox.getUpperX() - box.getUpperX();
			}

			if ( box.getUpperY() < viewportBox.getLowerY() )
			{
				deltaY = viewportBox.getLowerY() - box.getLowerY();
			}
			else if ( box.getLowerY() > viewportBox.getUpperY() )
			{
				deltaY = viewportBox.getUpperY() - box.getUpperY();
			}
			
			Xform2 translation = new Xform2( new Vector2( deltaX, deltaY ) );
			applyViewportSpaceXform( translation );
			
			return translation;
		}
		else
		{
			return null;
		}
	}
	
	
	public void applyViewportSpaceXform(Xform2 x)
	{
		worldToViewSpace = worldToViewSpace.concat( x );
		clampXform( worldToViewSpace );
		onXformModified();
	}
	
	
	public void onWorldSizeChanged()
	{
		refreshRangesFromXform();
	}



	private void clampXform(Xform2 worldToView)
	{
		Vector2 worldSize = viewport.getFiniteWorldSize();
		Vector2 viewportSize = viewport.getFiniteViewportSize();

		double scale = worldToView.scale;
		Xform2 viewToWorld = worldToView.inverse();
		Point2 topLeftInWorld = viewToWorld.transform( new Point2() );
		Point2 bottomRightInWorld = viewToWorld.transform( new Point2( viewportSize.x, viewportSize.y ) );
		
		Vector2 viewportSizeInWorld = bottomRightInWorld.sub( topLeftInWorld );
		
		if ( viewportSizeInWorld.x > worldSize.x )
		{
			// Viewport wider than contents
			if ( topLeftInWorld.x > 0.0 )
			{
				worldToView.translation.x = 0.0;
			}
			else if ( bottomRightInWorld.x < worldSize.x )
			{
				worldToView.translation.x = ( viewportSizeInWorld.x - worldSize.x ) * scale;
			}
		}
		else
		{
			if ( topLeftInWorld.x < 0.0 )
			{
				worldToView.translation.x = 0.0;
			}
			else if ( bottomRightInWorld.x > worldSize.x )
			{
				worldToView.translation.x = ( viewportSizeInWorld.x - worldSize.x ) * scale;
			}
		}
		
		if ( viewportSizeInWorld.y > worldSize.y )
		{
			// Viewport higher than contents
			if ( topLeftInWorld.y > 0.0 )
			{
				worldToView.translation.y = 0.0;
			}
			else if ( bottomRightInWorld.y < worldSize.y )
			{
				worldToView.translation.y = ( viewportSizeInWorld.y - worldSize.y ) * scale;
			}
		}
		else
		{
			if ( topLeftInWorld.y < 0.0 )
			{
				worldToView.translation.y = 0.0;
			}
			else if ( bottomRightInWorld.y > worldSize.y )
			{
				worldToView.translation.y = ( viewportSizeInWorld.y - worldSize.y ) * scale;
			}
		}
	}
	
	
	private void onXformModified()
	{
		refreshRangesFromXform();
		viewport.onFiniteViewportXformModified();
	}
	
	


	private void refreshRangesFromXform()
	{
		bIgnoreRangeEvents = true;
		
		Vector2 viewportSize = viewport.getFiniteViewportSize();
		Vector2 worldSize = viewport.getFiniteWorldSize();
		
		double scale = worldToViewSpace.scale;
		double invScale = 1.0 / scale;
		Xform2 viewToWorld = worldToViewSpace.inverse();
		Point2 topLeftInWorld = viewToWorld.transform( new Point2() );
		Point2 bottomRightInWorld = viewToWorld.transform( new Point2( viewportSize.x, viewportSize.y ) );
		
		Vector2 viewportSizeInAlloc = bottomRightInWorld.sub( topLeftInWorld );

		if ( xRange != null )
		{
			double min, max;
			
			if ( viewportSizeInAlloc.x > worldSize.x )
			{
				min = worldSize.x - viewportSizeInAlloc.x;
				max = viewportSizeInAlloc.x;
			}
			else
			{
				min = 0.0;
				max = worldSize.x;
			}

			updateRange( xRange, min, max, topLeftInWorld.x, bottomRightInWorld.x, 10.0 * invScale );
		}

		if ( yRange != null )
		{
			double min, max;
			
			if ( viewportSizeInAlloc.y > worldSize.y )
			{
				min = worldSize.y - viewportSizeInAlloc.y;
				max = viewportSizeInAlloc.y;
			}
			else
			{
				min = 0.0;
				max = worldSize.y;
			}

			updateRange( yRange, min, max, topLeftInWorld.y, bottomRightInWorld.y, 10.0 * invScale );
		}

		bIgnoreRangeEvents = false;
	}
	
	
	private static void updateRange(Range range, double min, double max, double begin, double end, double stepSize)
	{
		double size = end - begin;
		
		if ( begin < min )
		{
			begin = min;
			end = Math.min( begin + size, max );
		}
		if ( end > max )
		{
			end = max;
			begin = Math.max( end - size, min );
		}

		range.setBounds( min, max );
		range.setValue( begin, end );
		range.setStepSize( stepSize );
	}
	

	@Override
	public void onRangeModified(Range r)
	{
		if ( !bIgnoreRangeEvents )
		{
			if ( r == xRange )
			{
				worldToViewSpace.translation.x = -xRange.getBegin() * worldToViewSpace.scale;
			}
			else if ( r == yRange )
			{
				worldToViewSpace.translation.y = -yRange.getBegin() * worldToViewSpace.scale;
			}
			
			viewport.onFiniteViewportXformModified();
		}
	}
}
