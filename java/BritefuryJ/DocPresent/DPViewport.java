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
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeViewport;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;

public class DPViewport extends DPContainer
{
	public static interface ViewportListener
	{
		void onViewportXformModified(DPViewport viewport);
	}
	
	
	private double minWidth, minHeight;
	private Xform2 allocationSpaceToLocalSpace;
	private ViewportListener listener;
	
	
	public DPViewport(double minWidth, double minHeight)
	{
		this( ContainerStyleParams.defaultStyleParams, minWidth, minHeight );
	}

	public DPViewport(ContainerStyleParams styleParams, double minWidth, double minHeight)
	{
		super(styleParams);
		
		this.minWidth = minWidth;
		this.minHeight = minHeight;
		layoutNode = new LayoutNodeViewport( this );
		allocationSpaceToLocalSpace = new Xform2();
	}
	
	
	public double getMinWidth()
	{
		return minWidth;
	}
	
	public double getMinHeight()
	{
		return minHeight;
	}
	
	
	public void setListener(ViewportListener listener)
	{
		this.listener = listener;
	}
	
	
	public Xform2 getViewportXform()
	{
		return allocationSpaceToLocalSpace.clone();
	}
	
	public void setViewportXform(Xform2 x)
	{
		allocationSpaceToLocalSpace = x.clone();
		queueFullRedraw();
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
		Point2 bottomRight = element.getLocalPointRelativeToAncestor( this, new Point2( element.getAllocation() ) );
		Point2 centre = Point2.average( topLeft, bottomRight );
		Point2 topLeftCorner = centre.sub( getAllocation().mul( 0.5 ) );
		allocationSpaceToLocalSpace.translation = topLeftCorner.toVector2().negate();
		onXformModified();
	}
	
	public void zoomToFit()
	{
		DPElement child = getChild();
		double allocationX = child != null  ?  child.getAllocationX()  :  1.0;
		double allocationY = child != null  ?  child.getAllocationY()  :  1.0;

		double ax = allocationX == 0.0  ?  1.0  :  allocationX;
		double ay = allocationY == 0.0  ?  1.0  :  allocationY;
		
		allocationSpaceToLocalSpace.translation = new Vector2();
		allocationSpaceToLocalSpace.scale = Math.min( getAllocationX() / ax, getAllocationY() / ay );
		allocationSpaceToLocalSpace.scale = allocationSpaceToLocalSpace.scale == 0.0  ?  1.0  :  allocationSpaceToLocalSpace.scale;
		onXformModified();
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
	
	

	public List<DPElement> getChildren()
	{
		return registeredChildren;
	}

	
	
	
	protected boolean handlePointerNavigationGestureBegin(PointerButtonEvent event)
	{
		return true;
	}
	
	protected boolean handlePointerNavigationGestureEnd(PointerButtonEvent event)
	{
		return true;
	}

	protected boolean handlePointerNavigationGesture(PointerNavigationEvent event)
	{
		Xform2 xform = event.createXform();
		applyLocalSpaceXform( xform );
		return true;
	}
	
	protected boolean handlePointerScroll(PointerScrollEvent event)
	{
		double delta = (double)event.scrollY;
		Xform2 xform = new Xform2( new Vector2( 0.0, delta * 75.0 ) );
		applyLocalSpaceXform( xform );
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
		onXformModified();
	}
	
	
	public void onXformModified()
	{
		if ( listener != null )
		{
			listener.onViewportXformModified( this );
		}
		queueFullRedraw();
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
