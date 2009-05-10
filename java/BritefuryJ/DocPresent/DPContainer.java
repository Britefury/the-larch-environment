//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;




public abstract class DPContainer extends DPWidget
{
	public static class CouldNotFindChildException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected static interface ClosestPointChildSearcher
	{
		DPWidget getLeafClosestToLocalPointFromChild(DPWidget child, Point2 localPos, WidgetFilter filter);
	}
	
	protected static class ClosestPointChildContainerSearcher implements ClosestPointChildSearcher
	{
		private DPContainer container;
		
		
		public ClosestPointChildContainerSearcher(DPContainer container)
		{
			this.container = container;
		}

		public DPWidget getLeafClosestToLocalPointFromChild(DPWidget child, Point2 localPos, WidgetFilter filter)
		{
			return container.getChildLeafClosestToLocalPoint( localPos, filter );
		}
	}
	
	
	
	
	protected ArrayList<DPWidget> registeredChildren;
	protected DPWidget pressGrabChild;
	protected int pressGrabButton;
	protected HashMap<PointerInterface, DPWidget> pointerChildTable;
	
	
	
	
	//
	// Constructors
	//
	
	public DPContainer()
	{
		this( ContainerStyleSheet.defaultStyleSheet );
	}

	public DPContainer(ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
		
		registeredChildren = new ArrayList<DPWidget>();
	}
	
	
	
	//
	// Geometry methods
	//
	
	public boolean isLocalSpacePointWithinBoundsOfChild(Point2 p, DPWidget child)
	{
		return child.getAABoxInParentSpace().containsPoint( p );
	}

	
	
	protected Xform2 getChildTransformRelativeToAncestor(DPWidget child, DPWidget ancestor, Xform2 x) throws IsNotInSubtreeException
	{
		Xform2 localX = x.concat( child.getLocalToParentXform() );
		return getTransformRelativeToAncestor( ancestor, localX );
	}

	protected Point2 getChildLocalPointRelativeToAncestor(DPWidget child, DPWidget ancestor, Point2 p) throws IsNotInSubtreeException
	{
		Point2 localP = child.getLocalToParentXform().transform( p );
		return getLocalPointRelativeToAncestor( ancestor, localP );
	}
	

	protected void refreshScale(double scale, double rootScale)
	{
		super.refreshScale( scale, rootScale );
		
		for (DPWidget child: registeredChildren)
		{
			child.setScale( 1.0, rootScale );
		}
	}
	
	
	
	
	//
	// Child registration methods
	//
	
	protected DPWidget registerChild(DPWidget child)
	{
		child.unparent();
		
		child.setParent( this, presentationArea );
		
		child.setParentPacking( createParentPackingForChild( child ) );
		
		if ( isRealised() )
		{
			child.handleRealise();
		}
		
		structureChanged();
		
		return child;
	}
	
	protected void unregisterChild(DPWidget child)
	{
		if ( isRealised() )
		{
			child.handleUnrealise( child );
		}
		
		child.setParentPacking( null );
		
		child.setParent( null, null );
		
		structureChanged();
	}
	
	protected ParentPacking createParentPackingForChild(DPWidget child)
	{
		return null;
	}
	
	
	
	
	
	//
	// Tree structure methods
	//
	
	
	protected abstract void replaceChildWithEmpty(DPWidget child);
	
	public boolean hasChild(DPWidget c)
	{
		for (DPWidget child: registeredChildren)
		{
			if ( c == child )
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	protected abstract List<DPWidget> getChildren();
	
	
	public boolean areChildrenInOrder(DPWidget child0, DPWidget child1)
	{
		List<DPWidget> children = getChildren();
		int index0 = children.indexOf( child0 );
		int index1 = children.indexOf( child1 );
		
		if ( index0 != -1  &&  index1 != -1 )
		{
			return index0 < index1;
		}
		else
		{
			throw new CouldNotFindChildException();
		}
	}
	
	
	
	
	
	protected void structureChanged()
	{
		if ( parent != null )
		{
			parent.structureChanged();
		}
	}
	

	
	
	
	//
	//
	// SELECTION METHODS
	//
	//
	
	protected void drawSubtreeSelection(Graphics2D graphics, Marker startMarker, List<DPWidget> startPath, Marker endMarker, List<DPWidget> endPath)
	{
		List<DPWidget> children = getChildren();
		
		int startIndex = startMarker != null  ?  children.indexOf( startPath.get( 1 ) )  :  0;
		int endIndex = endMarker != null  ?  children.indexOf( endPath.get( 1) )  :  children.size() - 1;
		
		for (int i = startIndex; i <= endIndex; i++)
		{
			if ( i == startIndex  &&  startMarker != null )
			{
				children.get( i ).drawSubtreeSelection( graphics, startMarker, startPath.subList( 1, startPath.size() ), null, null );
			}
			else if ( i == endIndex  &&  endMarker != null )
			{
				children.get( i ).drawSubtreeSelection( graphics, null, null, endMarker, endPath.subList( 1, endPath.size() ) );
			}
			else
			{
				children.get( i ).drawSubtreeSelection( graphics, null, null, null, null );
			}
		}
	}
	
	
	
	
	
	//
	// Event handling methods
	//
	
	protected void onLeaveIntoChild(PointerMotionEvent event, DPWidget child)
	{
	}
	
	protected void onEnterFromChild(PointerMotionEvent event, DPWidget child)
	{
	}
	
	
	
	
	
	protected void onChildResizeRequest(DPWidget child)
	{
		queueResize();
	}
	
	protected void childResizeRequest(DPWidget child)
	{
		onChildResizeRequest( child );
	}
	
	
	
	
	protected void allocateChildX(DPWidget child, double localPosX, double localWidth)
	{
		double childWidth = localWidth / child.scale;
		child.allocateX( childWidth );
		
		child.positionInParentSpaceX = localPosX;
	}
	
	protected void allocateChildY(DPWidget child, double localPosY, double localHeight)
	{
		double childHeight = localHeight / child.scale;
		child.allocateY( childHeight );
		
		child.positionInParentSpaceY = localPosY;
	}
	
	
	
	
	
	
	protected void childRedrawRequest(DPWidget child, Point2 childPos, Vector2 childSize)
	{
		Xform2 childToContainer = child.getLocalToParentXform();
		Point2 localPos = childToContainer.transform( childPos );
		Vector2 localSize = childToContainer.transform( childSize );
		queueRedraw( localPos, localSize );
	}
	
	
	
	protected DPWidget getChildAtLocalPoint(Point2 localPos)
	{
		for (DPWidget child: registeredChildren)
		{
			if ( child.getAABoxInParentSpace().containsPoint( localPos ) )
			{
				return child;
			}
		}
		
		return null;
	}
	
	
	
	
	//
	// Drag and drop methods
	//
	
	protected DndDrag handleDndButtonDown(PointerButtonEvent event)
	{
		DPWidget child = getChildAtLocalPoint( event.pointer.getLocalPos() );
		if ( child != null )
		{
			DndDrag drag = child.handleDndButtonDown( event.transformed( child.getParentToLocalXform() ) );
			if ( drag != null )
			{
				return drag;
			}
			else
			{
				return super.handleDndButtonDown( event );
			}
		}
		
		return null;
	}
	
	protected boolean handleDndMotion(PointerMotionEvent event, DndDrag drag)
	{
		DPWidget child = getChildAtLocalPoint( event.pointer.getLocalPos() );
		if ( child != null )
		{
			boolean bDropped = child.handleDndMotion( event.transformed( child.getParentToLocalXform() ), drag );
			if ( bDropped )
			{
				return true;
			}
			else
			{
				return super.handleDndMotion( event, drag );
			}
		}
		
		return false;
	}
	
	protected boolean handleDndButtonUp(PointerButtonEvent event, DndDrag drag)
	{
		DPWidget child = getChildAtLocalPoint( event.pointer.getLocalPos() );
		if ( child != null )
		{
			boolean bDropped = child.handleDndButtonUp( event.transformed( child.getParentToLocalXform() ), drag );
			if ( bDropped )
			{
				return true;
			}
			else
			{
				return super.handleDndButtonUp( event, drag );
			}
		}
		
		return false;
	}
	
	
	
	
	
	//
	// Regular events
	//
	
	protected boolean handleButtonDown(PointerButtonEvent event)
	{
		if ( pressGrabChild == null )
		{
			DPWidget child = getChildAtLocalPoint( event.pointer.getLocalPos() );
			if ( child != null )
			{
				boolean bHandled = child.handleButtonDown( event.transformed( child.getParentToLocalXform() ) );
				if ( bHandled )
				{
					pressGrabChild = child;
					pressGrabButton = event.button;
					return true;
				}
			}
			
			if ( pressGrabChild != null )
			{
				return onButtonDown( event );
			}
			else
			{
				return false;
			}
		}
		else
		{
			return pressGrabChild.handleButtonDown( event.transformed( pressGrabChild.getParentToLocalXform() ) );
		}
	}
	
	protected boolean handleButtonDown2(PointerButtonEvent event)
	{
		if ( pressGrabChild != null )
		{
			return pressGrabChild.handleButtonDown2( event.transformed( pressGrabChild.getParentToLocalXform() ) );
		}
		else
		{
			return onButtonDown2( event );
		}
	}
	
	protected boolean handleButtonDown3(PointerButtonEvent event)
	{
		if ( pressGrabChild != null )
		{
			return pressGrabChild.handleButtonDown3( event.transformed( pressGrabChild.getParentToLocalXform() ) );
		}
		else
		{
			return onButtonDown3( event );
		}
	}
	
	protected boolean handleButtonUp(PointerButtonEvent event)
	{
		if ( pressGrabChild != null )
		{
			PointerButtonEvent childSpaceEvent = event.transformed( pressGrabChild.getParentToLocalXform() );
			if ( event.button == pressGrabButton )
			{
				pressGrabButton = 0;
				Point2 localPos = event.pointer.getLocalPos();
				if ( !isLocalSpacePointWithinBoundsOfChild( localPos, pressGrabChild ) )
				{
					pressGrabChild.handleLeave( new PointerMotionEvent( childSpaceEvent.pointer, PointerMotionEvent.Action.LEAVE ) );
				}
				
				boolean bHandled = pressGrabChild.handleButtonUp( childSpaceEvent );
				DPWidget savedPressGrabChild = pressGrabChild;
				pressGrabChild = null;
				
				if ( localPos.x >= 0.0  &&  localPos.x <= allocationX  &&  localPos.y >= 0.0  &&  localPos.y <= allocationY )
				{
					DPWidget child = getChildAtLocalPoint( localPos );
					if ( child != null )
					{
						if ( child != savedPressGrabChild )
						{
							child.handleEnter( new PointerMotionEvent( childSpaceEvent.pointer, PointerMotionEvent.Action.ENTER ) );
						}
						putChildForPointer( event.pointer.concretePointer(), child );
					}
					else
					{
						removeChildForPointer( event.pointer.concretePointer() );
						onEnter( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.ENTER ) );
					}
				}
				
				return bHandled;
			}
			else
			{
				return pressGrabChild.handleButtonUp( childSpaceEvent );
			}
		}
		else
		{
			return onButtonUp( event );
		}
	}


	protected void handleMotion(PointerMotionEvent event)
	{
		if ( pressGrabChild != null )
		{
			pressGrabChild.handleMotion( event.transformed( pressGrabChild.getParentToLocalXform() ) );
		}
		else
		{
			DPWidget pointerChild = getChildForPointer( event.pointer.concretePointer() );
			DPWidget oldPointerChild = pointerChild;
			
			if ( pointerChild != null )
			{
				if ( !isLocalSpacePointWithinBoundsOfChild( event.pointer.getLocalPos(), pointerChild ) )
				{
					pointerChild.handleLeave( new PointerMotionEvent( event.pointer.transformed( pointerChild.getParentToLocalXform() ), PointerMotionEvent.Action.LEAVE ) );
					removeChildForPointer( event.pointer.concretePointer() );
					pointerChild = null;
				}
				else
				{
					pointerChild.handleMotion( event.transformed( pointerChild.getParentToLocalXform() ) );
				}
			}
			
			if ( pointerChild == null )
			{
				DPWidget child = getChildAtLocalPoint( event.pointer.getLocalPos() );
				if ( child != null )
				{
					child.handleEnter( event.transformed( child.getParentToLocalXform() ) );
					pointerChild = child;
					putChildForPointer( event.pointer.concretePointer(), pointerChild );
				}
			}
			
			if ( oldPointerChild == null  &&  pointerChild != null )
			{
				onLeaveIntoChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.LEAVE ), pointerChild );
			}
			else if ( oldPointerChild != null  &&  pointerChild == null )
			{
				onEnterFromChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.ENTER ), oldPointerChild );
			}
		}
		
		onMotion( event );
	}
	
	protected void handleEnter(PointerMotionEvent event)
	{
		onEnter( event );
		
		Point2 localPos = event.pointer.getLocalPos();
		
		for (int i = registeredChildren.size() - 1; i >= 0; i--)
		{
			DPWidget child = registeredChildren.get( i );
			if ( isLocalSpacePointWithinBoundsOfChild( localPos, child ) )
			{
				child.handleEnter( event.transformed( child.getParentToLocalXform() ) );
				putChildForPointer( event.pointer.concretePointer(), child );
				onLeaveIntoChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.LEAVE ), child );
				break;
			}
		}
	}
	
	protected void handleLeave(PointerMotionEvent event)
	{
		if ( pressGrabChild == null )
		{
			DPWidget pointerChild = getChildForPointer( event.pointer.concretePointer() );
			if ( pointerChild != null )
			{
				pointerChild.handleLeave( event.transformed( pointerChild.getParentToLocalXform() ) );
				onEnterFromChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.ENTER ), pointerChild );
				removeChildForPointer( event.pointer.concretePointer() );
			}
		}

		onLeave( event );
	}
	
	
	
	protected boolean handleScroll(PointerScrollEvent event)
	{
		DPWidget pointerChild = getChildForPointer( event.pointer.concretePointer() );
		if ( pressGrabChild != null )
		{
			pressGrabChild.handleScroll( event.transformed( pressGrabChild.getParentToLocalXform() ) );
		}
		else if ( pointerChild != null )
		{
			pointerChild.handleScroll( event.transformed( pressGrabChild.getParentToLocalXform() ) );
		}
		return onScroll( event );
	}
	
	
	
	protected void handleRealise()
	{
		super.handleRealise();
		for (DPWidget child: registeredChildren)
		{
			child.handleRealise();
		}
	}
	
	protected void handleUnrealise(DPWidget unrealiseRoot)
	{
		for (DPWidget child: registeredChildren)
		{
			child.handleUnrealise( unrealiseRoot );
		}
		super.handleUnrealise( unrealiseRoot );
	}
	
	
	
	protected void drawBackground(Graphics2D graphics)
	{
	}
	
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		drawBackground( graphics );
		super.handleDraw( graphics, areaBox );
		
		AffineTransform currentTransform = graphics.getTransform();
		for (DPWidget child: registeredChildren)
		{
			if ( child.getAABoxInParentSpace().intersects( areaBox ) )
			{
				child.getLocalToParentXform().apply( graphics );
				child.handleDraw( graphics, child.getParentToLocalXform().transform( areaBox ) );
				graphics.setTransform( currentTransform );
			}
		}
	}
	
	
	
	
	protected void setPresentationArea(DPPresentationArea area)
	{
		super.setPresentationArea( area );
		
		for (DPWidget child: registeredChildren)
		{
			child.setPresentationArea( area );
		}
	}


	
	
	//
	//
	// CONTENT LEAF METHODS
	//
	//

	public DPContentLeaf getLeftContentLeaf()
	{
		// Check the child nodes
		List<DPWidget> navList = horizontalNavigationList();
		if ( navList != null )
		{
			for (DPWidget w: navList)
			{
				DPContentLeaf l = w.getLeftContentLeaf();
				if ( l != null )
				{
					return l;
				}
			}
		}
		
		return null;
	}
	
	public DPContentLeaf getRightContentLeaf()
	{
		// Check the child nodes
		List<DPWidget> navList = horizontalNavigationList();
		if ( navList != null )
		{
			for (int i = navList.size() - 1; i >= 0; i--)
			{
				DPWidget w = navList.get( i );
				DPContentLeaf l = w.getRightContentLeaf();
				if ( l != null )
				{
					return l;
				}
			}
		}
		
		return null;
	}

	protected DPContentLeaf getTopOrBottomContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace, boolean bSkipWhitespace)
	{
		List<DPWidget> navList = verticalNavigationList();
		if ( navList != null )
		{
			if ( bBottom )
			{
				for (int i = navList.size() - 1; i >= 0; i--)
				{
					DPWidget w = navList.get( i );
					DPContentLeaf l = w.getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
					if ( l != null )
					{
						return l;
					}
				}
			}
			else
			{
				for (DPWidget w: navList)
				{
					DPContentLeaf l = w.getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
					if ( l != null )
					{
						return l;
					}
				}
			}
			
			return null;
		}
		else
		{
			navList = horizontalNavigationList();
			if ( navList != null )
			{
				double closestDistance = 0.0;
				DPContentLeaf closestNode = null;
				for (DPWidget item: navList)
				{
					AABox2 bounds = getLocalAABox();
					double lower = item.getLocalPointRelativeToRoot( bounds.getLower() ).x;
					double upper = item.getLocalPointRelativeToRoot( bounds.getUpper() ).x;
					if ( cursorPosInRootSpace.x >=  lower  &&  cursorPosInRootSpace.x <= upper )
					{
						DPContentLeaf l = item.getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
						if ( l != null )
						{
							return l;
						}
					}
					else
					{
						double distance;
						if ( cursorPosInRootSpace.x < lower )
						{
							// Cursor to the left of the box
							distance = lower - cursorPosInRootSpace.x;
						}
						else // cursorPosInRootSpace.x > upper
						{
							// Cursor to the right of the box
							distance = cursorPosInRootSpace.x - upper;
						}
						
						if ( closestNode == null  ||  distance < closestDistance )
						{
							DPContentLeaf l = item.getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
							if ( l != null )
							{
								closestDistance = distance;
								closestNode = l;
							}
						}
					}
				}
				
				if ( closestNode != null )
				{
					return closestNode;
				}
			}
			
			return null;
		}
	}
	
	
	protected DPContentLeaf getContentLeafToLeftFromChild(DPWidget child)
	{
		List<DPWidget> navList = horizontalNavigationList();
		if ( navList != null )
		{
			int index = navList.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index - 1; i >= 0; i--)
				{
					DPWidget w = navList.get( i );
					DPContentLeaf l = w.getRightContentLeaf();
					if ( l != null )
					{
						return l;
					}
				}
			}
		}
		
		if ( parent != null )
		{
			return parent.getContentLeafToLeftFromChild( this );
		}
		else
		{
			return null;
		}
	}
	
	protected DPContentLeaf getContentLeafToRightFromChild(DPWidget child)
	{
		List<DPWidget> navList = horizontalNavigationList();
		if ( navList != null )
		{
			int index = navList.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index + 1; i < navList.size(); i++)
				{
					DPWidget w = navList.get( i );
					DPContentLeaf l = w.getLeftContentLeaf();
					if ( l != null )
					{
						return l;
					}
				}
			}
		}
		
		if ( parent != null )
		{
			return parent.getContentLeafToRightFromChild( this );
		}
		else
		{
			return null;
		}
	}
	
	protected DPContentLeaf getContentLeafAboveOrBelowFromChild(DPWidget child, boolean bBelow, Point2 localCursorPos, boolean bSkipWhitespace)
	{
		List<DPWidget> navList = verticalNavigationList();
		if ( navList != null )
		{
			int index = navList.indexOf( child );
			if ( index != -1 )
			{
				Point2 cursorPosInRootSpace = getLocalPointRelativeToRoot( localCursorPos );
				if ( bBelow )
				{
					for (int i = index + 1; i < navList.size(); i++)
					{
						DPWidget w = navList.get( i );
						DPContentLeaf l = w.getTopOrBottomContentLeaf( false, cursorPosInRootSpace, bSkipWhitespace );
						if ( l != null )
						{
							return l;
						}
					}
				}
				else
				{
					for (int i = index - 1; i >= 0; i--)
					{
						DPWidget w = navList.get( i );
						DPContentLeaf l = w.getTopOrBottomContentLeaf( true, cursorPosInRootSpace, bSkipWhitespace );
						if ( l != null )
						{
							return l;
						}
					}
				}
			}
		}
		
		if ( parent != null )
		{
			try
			{
				return parent.getContentLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( parent, localCursorPos ), bSkipWhitespace );
			}
			catch (IsNotInSubtreeException e)
			{
				throw new RuntimeException();
			}
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	protected DPWidget getLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		if ( filter.testContainer( this ) )
		{
			return getChildLeafClosestToLocalPoint( localPos, filter );
		}
		else
		{
			return null;
		}
	}

	protected abstract DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter);
	
	protected DPWidget getLeafClosestToLocalPointFromChild(DPWidget child, Point2 localPos, WidgetFilter filter)
	{
		return child.getLeafClosestToLocalPoint( child.getParentToLocalXform().transform( localPos ), filter );
	}
	

	
	protected DPWidget getChildLeafClosestToLocalPointHorizontal(Point2 localPos, WidgetFilter filter)
	{
		if ( registeredChildren.size() == 0 )
		{
			return null;
		}
		else if ( registeredChildren.size() == 1 )
		{
			return getLeafClosestToLocalPointFromChild( registeredChildren.get( 0 ), localPos, filter );
		}
		else
		{
			DPWidget start = null;
			int startIndex = -1;
			DPWidget childI = registeredChildren.get( 0 );
			for (int i = 0; i < registeredChildren.size() - 1; i++)
			{
				DPWidget childJ = registeredChildren.get( i + 1 );
				double iUpperX = childI.getPositionInParentSpace().x + childI.getAllocationInParentSpace().x;
				double jLowerX = childJ.getPositionInParentSpace().x;
				
				double midx = ( iUpperX + jLowerX ) * 0.5;
				
				if ( localPos.x < midx )
				{
					startIndex = i;
					start = childI;
					break;
				}
				
				childI = childJ;
			}
			
			if ( start == null )
			{
				startIndex = registeredChildren.size() - 1;
				start = registeredChildren.get( startIndex );
			}
			
			DPWidget c = getLeafClosestToLocalPointFromChild( start, localPos, filter );
			if ( c != null )
			{
				return c;
			}
			else
			{
				DPWidget next = null;
				DPWidget nextC = null;
				for (int j = startIndex + 1; j < registeredChildren.size(); j++)
				{
					nextC = getLeafClosestToLocalPointFromChild( registeredChildren.get( j ), localPos, filter );
					if ( nextC != null )
					{
						next = registeredChildren.get( j );
						break;
					}
				}

				DPWidget prev = null;
				DPWidget prevC = null;
				for (int j = startIndex - 1; j >= 0; j--)
				{
					prevC = getLeafClosestToLocalPointFromChild( registeredChildren.get( j ), localPos, filter );
					if ( prevC != null )
					{
						prev = registeredChildren.get( j );
						break;
					}
				}
				

				if ( prev == null  &&  next == null )
				{
					return null;
				}
				else if ( prev == null  &&  next != null )
				{
					return nextC;
				}
				else if ( prev != null  &&  next == null )
				{
					return prevC;
				}
				else
				{
					double distToPrev = localPos.x - ( prev.getPositionInParentSpace().x + prev.getAllocationInParentSpace().x );
					double distToNext = next.getPositionInParentSpace().x - localPos.x;
					
					return distToPrev > distToNext  ?  prevC  :  nextC;
				}
			}
		}
	}
	
	protected DPWidget getChildLeafClosestToLocalPointVertical(Point2 localPos, WidgetFilter filter)
	{
		if ( registeredChildren.size() == 0 )
		{
			return null;
		}
		else if ( registeredChildren.size() == 1 )
		{
			return getLeafClosestToLocalPointFromChild( registeredChildren.get( 0 ), localPos, filter );
		}
		else
		{
			DPWidget start = null;
			int startIndex = -1;
			DPWidget childI = registeredChildren.get( 0 );
			for (int i = 0; i < registeredChildren.size() - 1; i++)
			{
				DPWidget childJ = registeredChildren.get( i + 1 );
				double iUpperY = childI.getPositionInParentSpace().y + childI.getAllocationInParentSpace().y;
				double jLowerY = childJ.getPositionInParentSpace().y;
				
				double midY = ( iUpperY + jLowerY ) * 0.5;
				
				if ( localPos.y < midY )
				{
					startIndex = i;
					start = childI;
					break;
				}
				
				childI = childJ;
			}
			
			if ( start == null )
			{
				startIndex = registeredChildren.size() - 1;
				start = registeredChildren.get( startIndex );
			}
			
			DPWidget c = getLeafClosestToLocalPointFromChild( start, localPos, filter );
			if ( c != null )
			{
				return c;
			}
			else
			{
				DPWidget next = null;
				for (int j = startIndex + 1; j < registeredChildren.size(); j++)
				{
					next = getLeafClosestToLocalPointFromChild( registeredChildren.get( j ), localPos, filter );
					if ( next != null )
					{
						break;
					}
				}

				DPWidget prev = null;
				for (int j = startIndex - 1; j >= 0; j--)
				{
					prev = getLeafClosestToLocalPointFromChild( registeredChildren.get( j ), localPos, filter );
					if ( prev != null )
					{
						break;
					}
				}
				
				
				if ( prev == null  &&  next == null )
				{
					return null;
				}
				else if ( prev == null  &&  next != null )
				{
					return next;
				}
				else if ( prev != null  &&  next == null )
				{
					return prev;
				}
				else
				{
					double distToPrev = localPos.y - ( prev.getPositionInParentSpace().y + prev.getAllocationInParentSpace().y );
					double distToNext = next.getPositionInParentSpace().y - localPos.y;
					
					return distToPrev > distToNext  ?  prev  :  next;
				}
			}
		}
	}
	
	
	
	
	
	//
	//
	// PRIVATE ACCESSOR METHODS FOR @pointerChildTable AND @pointerDndChildTable
	//
	//
	
	private DPWidget getChildForPointer(PointerInterface pointer)
	{
		if ( pointerChildTable != null )
		{
			return pointerChildTable.get( pointer );
		}
		else
		{
			return null;
		}
	}
	
	private void putChildForPointer(PointerInterface pointer, DPWidget child)
	{
		if ( pointerChildTable == null )
		{
			pointerChildTable = new HashMap<PointerInterface, DPWidget>();
		}
		pointerChildTable.put( pointer, child );
	}
	
	private void removeChildForPointer(PointerInterface pointer)
	{
		if ( pointerChildTable != null )
		{
			pointerChildTable.remove( pointer );
			if ( pointerChildTable.size() == 0 )
			{
				pointerChildTable = null;
			}
		}
	}

	
	
	
	
	//
	//
	// STYLESHEET METHODS
	//
	//
	
	protected ContainerStyleSheet getStyleSheet()
	{
		return (ContainerStyleSheet)styleSheet;
	}
}
