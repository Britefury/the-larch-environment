//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.List;

import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerNavigationEvent;
import BritefuryJ.LSpace.Interactor.NavigationElementInteractor;
import BritefuryJ.LSpace.LayoutTree.LayoutNodeViewport;
import BritefuryJ.LSpace.PersistentState.PersistentState;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.LSpace.Util.FiniteViewportBehaviour;
import BritefuryJ.LSpace.Util.Range;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;

public class LSViewport extends LSContainerNonOverlayed implements FiniteViewportBehaviour.FiniteViewport
{
	private static class ViewportNavigationInteractor implements NavigationElementInteractor
	{
		@Override
		public boolean navigationGestureBegin(LSElement element, PointerButtonEvent event)
		{
			return true;
		}

		@Override
		public void navigationGestureEnd(LSElement element, PointerButtonEvent event)
		{
		}

		@Override
		public void navigationGesture(LSElement element, PointerNavigationEvent event)
		{
			LSViewport viewport = (LSViewport)element;
			Xform2 xform = event.createXform();
			viewport.viewportBehaviour.applyViewportSpaceXform( xform );
		}
	}
	
	
	private static ViewportNavigationInteractor interactor = new ViewportNavigationInteractor();
	
	
	protected final static int FLAG_IGNORE_RANGE_EVENTS = FLAGS_CONTAINER_END * 0x1;

	protected final static int FLAGS_VIEWPORT_END = FLAGS_CONTAINER_END  <<  1;

	
	
	private PersistentState state;
	private FiniteViewportBehaviour viewportBehaviour;
	
	
	public LSViewport(ContainerStyleParams styleParams, Range xRange, Range yRange, PersistentState state, LSElement child)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeViewport( this );
		this.state = state;
		Xform2 x = state != null  ?  state.getValueAsType( Xform2.class )  :  null;
		if ( x == null )
		{
			x = new Xform2();
			if ( state != null )
			{
				state.setValue( x );
			}
		}

		viewportBehaviour = new FiniteViewportBehaviour( this, xRange, yRange, x );
		
		addElementInteractor( interactor );
		
		if ( child != null )
		{
			if ( child.getLayoutNode() == null )
			{
				throw new ChildHasNoLayoutException();
			}

			registeredChildren.add( child );
			registerChild( child );				
		}
	}
	
	


	public LSElement getChild()
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
	
	public void setChild(LSElement child)
	{
		LSElement prevChild = getChild();
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
	
	
	protected void replaceChildWithEmpty(LSElement child)
	{
		assert child == this.getChild();
		setChild( null );
	}
	
	protected void replaceChild(LSElement child, LSElement replacement)
	{
		assert child == this.getChild();
		setChild( replacement );
	}
	
	

	public List<LSElement> getChildren()
	{
		return registeredChildren;
	}

	
	public boolean isSingleElementContainer()
	{
		return true;
	}

	
	
	

	
	
	public Range getXRange()
	{
		return viewportBehaviour.getXRange();
	}
	
	public Range getYRange()
	{
		return viewportBehaviour.getYRange();
	}
	
	
	public Xform2 getViewportXform()
	{
		return viewportBehaviour.getWorldToViewXform();
	}
	
	public void setViewportXform(Xform2 x)
	{
		viewportBehaviour.setWorldToViewXform( x );
	}
	
	
	public void applyViewportSpaceXform(Xform2 x)
	{
		viewportBehaviour.applyViewportSpaceXform( x );
	}
	
	
	public void oneToOne()
	{
		viewportBehaviour.oneToOne();
	}
	
	public void resetXform()
	{
		viewportBehaviour.oneToOne();
	}
	
	
	public void focusOn(LSElement element)
	{
		Point2 topLeft = element.getLocalPointRelativeToAncestor( this, new Point2( 0.0, 0.0 ) );
		Point2 bottomRight = element.getLocalPointRelativeToAncestor( this, new Point2( element.getActualSize() ) );
		viewportBehaviour.focusOn( new AABox2( topLeft, bottomRight ) );
	}
	
	public void zoomToFit()
	{
		viewportBehaviour.zoomToFit();
	}
	
	
	protected void ensureRegionVisible(AABox2 box)
	{
		Xform2 x = viewportBehaviour.ensureRegionVisible( box );

		if ( x != null )
		{
			if ( parent != null )
			{
				box = x.transform( box );
				parent.ensureRegionVisible( getLocalToParentXform().transform( box ) );
			}
		}
		else
		{
			super.ensureRegionVisible( box );
		}
	}
	
	@Override
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
	
	@Override
	protected Xform2 getAllocationSpaceToLocalSpaceXform(LSElement child)
	{
		return viewportBehaviour.getWorldToViewXform();
	}

	@Override
	public AABox2 getLocalVisibleBoundsClipBox()
	{
		return getLocalAABox();
	}
	

	
	
	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
		Shape clipShape = pushVisibleBoundsClip( graphics );
		super.handleDrawBackground( graphics, areaBox );
		popVisibleBoundsClip( graphics, clipShape );
	}
	
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		Shape clipShape = pushVisibleBoundsClip( graphics );
		super.handleDraw( graphics, areaBox );
		popVisibleBoundsClip( graphics, clipShape );
	}

	
	
	@Override
	public Vector2 getFiniteViewportSize()
	{
		return getActualSize();
	}
	
	@Override
	public Vector2 getFiniteWorldSize()
	{
		LSElement child = getChild();
		
		return child != null  ?  child.getActualSize()  :  new Vector2( 1.0, 1.0 );
	}
	
	@Override
	public void onFiniteViewportXformModified()
	{
		if ( state != null )
		{
			state.setValue( viewportBehaviour.getWorldToViewXform() );
		}
		queueFullRedraw();
	}

	
	
	
	public void onAllocationRefreshed()
	{
		viewportBehaviour.onWorldSizeChanged();
	}
}
