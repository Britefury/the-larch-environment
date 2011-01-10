//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.List;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerNavigationEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Interactor.NavigationElementInteractor;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeViewport;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.DocPresent.Util.Range;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;

public class DPViewport extends DPContainer implements Range.RangeListener
{
	private static class ViewportNavigationInteractor implements NavigationElementInteractor
	{
		@Override
		public boolean navigationGestureBegin(PointerInputElement element, PointerButtonEvent event)
		{
			return true;
		}

		@Override
		public void navigationGestureEnd(PointerInputElement element, PointerButtonEvent event)
		{
		}

		@Override
		public void navigationGesture(PointerInputElement element, PointerNavigationEvent event)
		{
			DPViewport viewport = (DPViewport)element;
			Xform2 xform = event.createXform();
			viewport.applyLocalSpaceXform( xform );
		}
	}
	
	
	private static ViewportNavigationInteractor interactor = new ViewportNavigationInteractor();
	
	
	protected final static int FLAG_IGNORE_RANGE_EVENTS = FLAGS_CONTAINER_END * 0x1;

	protected final static int FLAGS_VIEWPORT_END = FLAGS_CONTAINER_END  <<  1;

	
	
	private Range xRange, yRange;
	private Xform2 allocationSpaceToLocalSpace;
	private PersistentState state;
	
	
	public DPViewport(PersistentState state)
	{
		this( ContainerStyleParams.defaultStyleParams, null, null, state );
	}

	public DPViewport(Range xRange, Range yRange, PersistentState state)
	{
		this( ContainerStyleParams.defaultStyleParams, xRange, yRange, state );
	}

	public DPViewport(ContainerStyleParams styleParams, PersistentState state)
	{
		this( styleParams, null, null, state );
	}

	public DPViewport(ContainerStyleParams styleParams, Range xRange, Range yRange, PersistentState state)
	{
		super(styleParams);
		
		this.xRange = xRange;
		this.yRange = yRange;
		layoutNode = new LayoutNodeViewport( this );
		this.state = state;
		Xform2 x = state.getValueAsType( Xform2.class );
		if ( x == null )
		{
			x = new Xform2();
			state.setValue( x );
		}
		allocationSpaceToLocalSpace = x;
		
		addElementInteractor( interactor );
	}
	
	private DPViewport(DPViewport element)
	{
		super( element );
		
		this.xRange = element.xRange;
		this.yRange = element.yRange;
		layoutNode = new LayoutNodeViewport( this );
		this.state = element.state;
		allocationSpaceToLocalSpace = element.allocationSpaceToLocalSpace;
		
		addElementInteractor( interactor );
	}
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	protected void clonePostConstuct(DPElement src)
	{
		super.clonePostConstuct( src );
		DPElement child = ((DPViewport)src).getChild();
		if ( child != null )
		{
			setChild( child.clonePresentationSubtree() );
		}
	}
	
	public DPElement clonePresentationSubtree()
	{
		DPViewport clone = new DPViewport( this );
		clone.clonePostConstuct( this );
		return clone;
	}
	
	
	
	
	
	
	
	public Range getXRange()
	{
		return xRange;
	}
	
	public Range getYRange()
	{
		return yRange;
	}
	
	
	public Xform2 getViewportXform()
	{
		return allocationSpaceToLocalSpace.clone();
	}
	
	public void setViewportXform(Xform2 x)
	{
		allocationSpaceToLocalSpace = x.clone();
		onXformModified();
	}
	
	
	public void oneToOne()
	{
		allocationSpaceToLocalSpace.scale = 1.0;
		onXformModified();
	}
	
	public void resetXform()
	{
		allocationSpaceToLocalSpace = new Xform2();
		onXformModified();
	}
	
	
	public void focusOn(DPElement element)
	{
		allocationSpaceToLocalSpace.scale = 1.0;
		Point2 topLeft = element.getLocalPointRelativeToAncestor( this, new Point2( 0.0, 0.0 ) );
		Point2 bottomRight = element.getLocalPointRelativeToAncestor( this, new Point2( element.getSize() ) );
		Point2 centre = Point2.average( topLeft, bottomRight );
		Point2 topLeftCorner = centre.sub( getSize().mul( 0.5 ) );
		allocationSpaceToLocalSpace.translation = topLeftCorner.toVector2().negate();
		onXformModified();
	}
	
	public void zoomToFit()
	{
		DPElement child = getChild();
		double width = child != null  ?  child.getWidth()  :  1.0;
		double height = child != null  ?  child.getHeight()  :  1.0;

		double ax = width == 0.0  ?  1.0  :  width;
		double ay = height == 0.0  ?  1.0  :  height;
		
		allocationSpaceToLocalSpace.translation = new Vector2();
		allocationSpaceToLocalSpace.scale = Math.min( getWidth() / ax, getHeight() / ay );
		allocationSpaceToLocalSpace.scale = allocationSpaceToLocalSpace.scale == 0.0  ?  1.0  :  allocationSpaceToLocalSpace.scale;
		onXformModified();
	}
	
	
	protected void ensureRegionVisible(AABox2 box)
	{
		AABox2 localBox = getLocalAABox();
		
		boolean bScroll = !box.intersects( localBox );
		
		if ( !bScroll )
		{
			if ( box.getWidth() < localBox.getWidth() )
			{
				if ( box.getLowerX() < localBox.getLowerX()  ||  box.getUpperX() > localBox.getUpperX() )
				{
					bScroll = true;
				}
			}
			else
			{
				if ( box.getUpperX() < localBox.getLowerX()  ||  box.getLowerX() > localBox.getUpperX() )
				{
					bScroll = true;
				}
			}
		}
		
		if ( !bScroll )
		{
			if ( box.getHeight() < localBox.getHeight() )
			{
				if ( box.getLowerY() < localBox.getLowerY()  ||  box.getUpperY() > localBox.getUpperY() )
				{
					bScroll = true;
				}
			}
			else
			{
				if ( box.getUpperY() < localBox.getLowerY()  ||  box.getLowerY() > localBox.getUpperY() )
				{
					bScroll = true;
				}
			}
		}
		
		if ( bScroll )
		{
			double deltaX = 0.0, deltaY = 0.0;
			
			if ( box.getUpperX() < localBox.getLowerX() )
			{
				deltaX = localBox.getLowerX() - box.getLowerX();
			}
			else if ( box.getLowerX() > localBox.getUpperX() )
			{
				deltaX = localBox.getUpperX() - box.getUpperX();
			}

			if ( box.getUpperY() < localBox.getLowerY() )
			{
				deltaY = localBox.getLowerY() - box.getLowerY();
			}
			else if ( box.getLowerY() > localBox.getUpperY() )
			{
				deltaY = localBox.getUpperY() - box.getUpperY();
			}
			
			Xform2 translation = new Xform2( new Vector2( deltaX, deltaY ) );
			applyLocalSpaceXform( translation );

			if ( parent != null )
			{
				box = translation.transform( box );
				parent.ensureRegionVisible( getLocalToParentXform().transform( box ) );
			}
		}
		else
		{
			super.ensureRegionVisible( box );
		}
	}
	
	protected boolean isLocalSpacePointVisible(Point2 point)
	{
		if ( getLocalAABox().containsPoint( point ) )
		{
			if ( parent != null )
			{
				return parent.isLocalSpacePointVisible( getLocalToParentXform().transform( point ) );
			}
			else
			{
				return true;
			}
		}
		else
		{
			return false;
		}
	}
	
	
	
	//
	// Geometry methods
	//
	
	protected Xform2 getAllocationSpaceToLocalSpaceXform(DPElement child)
	{
		return allocationSpaceToLocalSpace;
	}
	
	public AABox2 getLocalClipBox()
	{
		return getLocalAABox();
	}
	

	
	
	public DPElement getChild()
	{
		if ( registeredChildren.size() > 0 )
		{
			return registeredChildren.get( 0 );
		}
		else
		{
			return null;
		}
	}
	
	public void setChild(DPElement child)
	{
		DPElement prevChild = getChild();
		if ( child != prevChild )
		{
			if ( child != null  &&  child.getLayoutNode() == null )
			{
				throw new ChildHasNoLayoutException();
			}

			if ( prevChild != null )
			{
				unregisterChild( prevChild );
				registeredChildren.remove( 0 );
			}
			
			if ( child != null )
			{
				registeredChildren.add( child );
				registerChild( child );				
			}
			
			onChildListModified();
			queueResize();
		}
	}
	
	
	protected void replaceChildWithEmpty(DPElement child)
	{
		assert child == this.getChild();
		setChild( null );
	}
	
	protected void replaceChild(DPElement child, DPElement replacement)
	{
		assert child == this.getChild();
		setChild( replacement );
	}
	
	

	public List<DPElement> getChildren()
	{
		return registeredChildren;
	}

	
	public boolean isSingleElementContainer()
	{
		return true;
	}

	
	
	
	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
		Shape clipShape = pushClip( graphics );
		super.handleDrawBackground( graphics, areaBox );
		popClip( graphics, clipShape );
	}
	
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		Shape clipShape = pushClip( graphics );
		super.handleDraw( graphics, areaBox );
		popClip( graphics, clipShape );
	}

	
	
	private void applyLocalSpaceXform(Xform2 x)
	{
		allocationSpaceToLocalSpace = allocationSpaceToLocalSpace.concat( x );
		clampXform( allocationSpaceToLocalSpace );
		onXformModified();
	}
	
	
	private void clampXform(Xform2 allocToLocal)
	{
		DPElement child = getChild();

		double scale = allocToLocal.scale;
		Xform2 localToAlloc = allocToLocal.inverse();
		Point2 topLeftInAlloc = localToAlloc.transform( new Point2() );
		Point2 bottomRightInAlloc = localToAlloc.transform( new Point2( getWidth(), getHeight() ) );
		
		Vector2 allocSize = new Vector2( child != null  ?  child.getWidth()  :  1.0,  child != null  ?  child.getHeight() : 1.0 );
		Vector2 viewportSizeInAlloc = bottomRightInAlloc.sub( topLeftInAlloc );
		
		if ( viewportSizeInAlloc.x > allocSize.x )
		{
			// Viewport wider than contents
			if ( topLeftInAlloc.x > 0.0 )
			{
				allocToLocal.translation.x = 0.0;
			}
			else if ( bottomRightInAlloc.x < allocSize.x )
			{
				allocToLocal.translation.x = ( viewportSizeInAlloc.x - allocSize.x ) * scale;
			}
		}
		else
		{
			if ( topLeftInAlloc.x < 0.0 )
			{
				allocToLocal.translation.x = 0.0;
			}
			else if ( bottomRightInAlloc.x > allocSize.x )
			{
				allocToLocal.translation.x = ( viewportSizeInAlloc.x - allocSize.x ) * scale;
			}
		}
		
		if ( viewportSizeInAlloc.y > allocSize.y )
		{
			// Viewport higher than contents
			if ( topLeftInAlloc.y > 0.0 )
			{
				allocToLocal.translation.y = 0.0;
			}
			else if ( bottomRightInAlloc.y < allocSize.y )
			{
				allocToLocal.translation.y = ( viewportSizeInAlloc.y - allocSize.y ) * scale;
			}
		}
		else
		{
			if ( topLeftInAlloc.y < 0.0 )
			{
				allocToLocal.translation.y = 0.0;
			}
			else if ( bottomRightInAlloc.y > allocSize.y )
			{
				allocToLocal.translation.y = ( viewportSizeInAlloc.y - allocSize.y ) * scale;
			}
		}
	}
	
	
	public void onXformModified()
	{
		state.setValue( allocationSpaceToLocalSpace );
		refreshRangesFromXform();
		queueFullRedraw();
	}
	
	
	
	@Override
	protected void onRealise()
	{
		if ( xRange != null )
		{
			xRange.addListener( this );
		}
		if ( yRange != null )
		{
			yRange.addListener( this );
		}
	}
	
	@Override
	protected void onUnrealise(DPElement unrealiseRoot)
	{
		if ( xRange != null )
		{
			xRange.removeListener( this );
		}
		if ( yRange != null )
		{
			yRange.removeListener( this );
		}
	}
	
	
	private void refreshRangesFromXform()
	{
		DPElement child = getChild();
		
		setFlag( FLAG_IGNORE_RANGE_EVENTS );
		
		double scale = allocationSpaceToLocalSpace.scale;
		double invScale = 1.0 / scale;
		Xform2 localToAlloc = allocationSpaceToLocalSpace.inverse();
		Point2 topLeftInAlloc = localToAlloc.transform( new Point2() );
		Point2 bottomRightInAlloc = localToAlloc.transform( new Point2( getWidth(), getHeight() ) );
		
		Vector2 allocSize = new Vector2( child != null  ?  child.getWidth()  :  1.0,  child != null  ?  child.getHeight() : 1.0 );
		Vector2 viewportSizeInAlloc = bottomRightInAlloc.sub( topLeftInAlloc );

		if ( xRange != null )
		{
			double min, max;
			
			if ( viewportSizeInAlloc.x > allocSize.x )
			{
				min = allocSize.x - viewportSizeInAlloc.x;
				max = viewportSizeInAlloc.x;
			}
			else
			{
				min = 0.0;
				max = allocSize.x;
			}

			updateRange( xRange, min, max, topLeftInAlloc.x, bottomRightInAlloc.x, 10.0 * invScale );
		}

		if ( yRange != null )
		{
			double min, max;
			
			if ( viewportSizeInAlloc.y > allocSize.y )
			{
				min = allocSize.y - viewportSizeInAlloc.y;
				max = viewportSizeInAlloc.y;
			}
			else
			{
				min = 0.0;
				max = allocSize.y;
			}

			updateRange( yRange, min, max, topLeftInAlloc.y, bottomRightInAlloc.y, 10.0 * invScale );
		}

		clearFlag( FLAG_IGNORE_RANGE_EVENTS );
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
	
	
	
	public void onAllocationRefreshed()
	{
		refreshRangesFromXform();
	}
	
	
	

	@Override
	public void onRangeModified(Range r)
	{
		if ( !testFlag( FLAG_IGNORE_RANGE_EVENTS ) )
		{
			Xform2 xform = allocationSpaceToLocalSpace.clone();
			
			if ( r == xRange )
			{
				xform.translation.x = -xRange.getBegin() * xform.scale;
			}
			else if ( r == yRange )
			{
				xform.translation.y = -yRange.getBegin() * xform.scale;
			}
			
			allocationSpaceToLocalSpace = xform;
			state.setValue( allocationSpaceToLocalSpace );
			queueFullRedraw();
		}
	}

	
	//
	// Text representation methods
	//
	
	protected String computeSubtreeTextRepresentation()
	{
		DPElement child = getChild();
		return child != null  ?  child.getTextRepresentation()  :  "";
	}
}
