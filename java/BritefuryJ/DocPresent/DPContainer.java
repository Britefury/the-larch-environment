//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;




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
	public String cachedTextRep;
	
	
	
	
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
		cachedTextRep = null;
	}
	
	
	
	//
	// Geometry methods
	//
	
	protected double getInternalChildScale(DPWidget child)
	{
		return 1.0;
	}
	

	
	
	
	//
	// Child registration methods
	//
	
	protected DPWidget registerChild(DPWidget child, PackingParams packing)
	{
		child.unparent();
		
		child.setParent( this, presentationArea );
		
		packing = packing != null  ?  packing  :  getDefaultPackingParams();
		child.setParentPacking( packing );
		
		if ( isRealised() )
		{
			child.handleRealise();
		}
		
		return child;
	}
	
	protected void unregisterChild(DPWidget child)
	{
		if ( pressGrabChild == child )
		{
			pressGrabChild = null;
			pressGrabButton = -1;
		}
		
		if ( pointerChildTable != null )
		{
			
			for (Map.Entry<PointerInterface, DPWidget> e: pointerChildTable.entrySet())
			{
				if ( e.getValue() == child )
				{
					pointerChildTable.remove( e.getKey() );
				}
			}
		}

		if ( isRealised() )
		{
			child.handleUnrealise( child );
		}
		
		child.setParentPacking( null );
		
		child.setParent( null, null );
	}
	
	
	protected void onChildListModified()
	{
		onSubtreeStructureChanged();
		refreshMetaElement();
	}
	
	
	protected abstract PackingParams getDefaultPackingParams();
	
	
	
	
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
	
	
	protected List<DPWidget> getInternalChildren()
	{
		return registeredChildren;
	}
	
	public abstract List<DPWidget> getChildren();

	
	public boolean areChildrenInOrder(DPWidget child0, DPWidget child1)
	{
		List<DPWidget> children = getInternalChildren();
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
	
	
	
	
	
	protected void onSubtreeStructureChanged()
	{
		cachedTextRep = null;
		
		if ( parent != null )
		{
			parent.onSubtreeStructureChanged();
		}
	}
	
	
	
	
	//
	//
	// SELECTION METHODS
	//
	//
	
	protected void drawSubtreeSelection(Graphics2D graphics, Marker startMarker, List<DPWidget> startPath, Marker endMarker, List<DPWidget> endPath)
	{
		List<DPWidget> children = getInternalChildren();
		
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
			if ( child.containsParentSpacePoint( localPos ) )
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
				if ( bHandled  &&  isRealised() )
				{
					pressGrabChild = child;
					pressGrabButton = event.button;
					return true;
				}
			}
			
			if ( pressGrabChild == null )
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
				if ( !pressGrabChild.containsParentSpacePoint( localPos ) )
				{
					pressGrabChild.handleLeave( new PointerMotionEvent( childSpaceEvent.pointer, PointerMotionEvent.Action.LEAVE ) );
				}
				
				boolean bHandled = pressGrabChild.handleButtonUp( childSpaceEvent );
				DPWidget savedPressGrabChild = pressGrabChild;
				pressGrabChild = null;
				
				if ( isRealised()  &&  localPos.x >= 0.0  &&  localPos.x <= getAllocationX()  &&  localPos.y >= 0.0  &&  localPos.y <= getAllocationY() )
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
				if ( !pointerChild.containsParentSpacePoint( event.pointer.getLocalPos() ) )
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
		
		super.handleMotion( event );
	}
	
	protected void handleEnter(PointerMotionEvent event)
	{
		super.handleEnter( event );
		
		Point2 localPos = event.pointer.getLocalPos();
		
		for (int i = registeredChildren.size() - 1; i >= 0; i--)
		{
			DPWidget child = registeredChildren.get( i );
			if ( child.containsParentSpacePoint( localPos ) )
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

		super.handleLeave( event );
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
			pointerChild.handleScroll( event.transformed( pointerChild.getParentToLocalXform() ) );
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
		if ( pressGrabChild != null )
		{
			pressGrabChild = null;
			pressGrabButton = -1;
		}
		for (DPWidget child: registeredChildren)
		{
			child.handleUnrealise( unrealiseRoot );
		}
		super.handleUnrealise( unrealiseRoot );
	}
	
	
	
	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
		super.handleDrawBackground( graphics, areaBox );
		
		AffineTransform currentTransform = graphics.getTransform();
		for (DPWidget child: registeredChildren)
		{
			if ( child.getAABoxInParentSpace().intersects( areaBox ) )
			{
				child.getLocalToParentXform().apply( graphics );
				child.handleDrawBackground( graphics, child.getParentToLocalXform().transform( areaBox ) );
				graphics.setTransform( currentTransform );
			}
		}
	}
	
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
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

	public DPContentLeaf getFirstLeafInSubtree(WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( branchFilter == null  ||  branchFilter.testElement( this ) )
		{
			for (DPWidget child: getInternalChildren())
			{
				DPContentLeaf leaf = child.getFirstLeafInSubtree( branchFilter, leafFilter );
				if ( leaf != null )
				{
					return leaf;
				}
			}
			return null;
		}
		else
		{
			return null;
		}
	}

	public DPContentLeaf getLastLeafInSubtree(WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( branchFilter == null  ||  branchFilter.testElement( this ) )
		{
			List<DPWidget> children = getInternalChildren();
			for (int i = children.size() - 1; i >= 0; i--)
			{
				DPContentLeaf leaf = children.get( i ).getLastLeafInSubtree( branchFilter, leafFilter );
				if ( leaf != null )
				{
					return leaf;
				}
			}
			return null;
		}
		else
		{
			return null;
		}
	}
	
	

	
	protected DPContentLeaf getPreviousLeafFromChild(DPWidget child, WidgetFilter subtreeRootFilter, WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( subtreeRootFilter == null  ||  subtreeRootFilter.testElement( this ) )
		{
			List<DPWidget> children = getInternalChildren();
			int index = children.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index - 1; i >= 0; i--)
				{
					DPWidget e = children.get( i );
					DPContentLeaf l = e.getLastLeafInSubtree( branchFilter, leafFilter );
					if ( l != null )
					{
						return l;
					}
				}
			}
			
			if ( parent != null )
			{
				return parent.getPreviousLeafFromChild( this, subtreeRootFilter, branchFilter, leafFilter );
			}
		}
		
		return null;
	}
	
	protected DPContentLeaf getNextLeafFromChild(DPWidget child, WidgetFilter subtreeRootFilter, WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( subtreeRootFilter == null  ||  subtreeRootFilter.testElement( this ) )
		{
			List<DPWidget> children = getInternalChildren();
			int index = children.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index + 1; i < children.size(); i++)
				{
					DPWidget e = children.get( i );
					DPContentLeaf l = e.getFirstLeafInSubtree( branchFilter, leafFilter );
					if ( l != null )
					{
						return l;
					}
				}
			}
		
			if ( parent != null )
			{
				return parent.getNextLeafFromChild( this, subtreeRootFilter, branchFilter, leafFilter );
			}
		}

		return null;
	}
	

	

	
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
					AABox2 bounds = item.getLocalAABox();
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
			return parent.getContentLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( parent, localCursorPos ), bSkipWhitespace );
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	protected DPWidget getLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPoint( localPos, filter );
	}

	protected abstract DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter);
	
	protected DPWidget getLeafClosestToLocalPointFromChild(DPWidget child, Point2 localPos, WidgetFilter filter)
	{
		return child.getLeafClosestToLocalPoint( child.getParentToLocalXform().transform( localPos ), filter );
	}
	

	
	protected DPWidget getChildLeafClosestToLocalPointHorizontal(List<DPWidget> searchList, Point2 localPos, WidgetFilter filter)
	{
		if ( searchList.size() == 0 )
		{
			return null;
		}
		else if ( searchList.size() == 1 )
		{
			return getLeafClosestToLocalPointFromChild( searchList.get( 0 ), localPos, filter );
		}
		else
		{
			DPWidget start = null;
			int startIndex = -1;
			DPWidget childI = searchList.get( 0 );
			for (int i = 0; i < searchList.size() - 1; i++)
			{
				DPWidget childJ = searchList.get( i + 1 );
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
				startIndex = searchList.size() - 1;
				start = searchList.get( startIndex );
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
				for (int j = startIndex + 1; j < searchList.size(); j++)
				{
					nextC = getLeafClosestToLocalPointFromChild( searchList.get( j ), localPos, filter );
					if ( nextC != null )
					{
						next = searchList.get( j );
						break;
					}
				}

				DPWidget prev = null;
				DPWidget prevC = null;
				for (int j = startIndex - 1; j >= 0; j--)
				{
					prevC = getLeafClosestToLocalPointFromChild( searchList.get( j ), localPos, filter );
					if ( prevC != null )
					{
						prev = searchList.get( j );
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
	
	protected DPWidget getChildLeafClosestToLocalPointVertical(List<DPWidget> searchList, Point2 localPos, WidgetFilter filter)
	{
		if ( searchList.size() == 0 )
		{
			return null;
		}
		else if ( searchList.size() == 1 )
		{
			return getLeafClosestToLocalPointFromChild( searchList.get( 0 ), localPos, filter );
		}
		else
		{
			DPWidget start = null;
			int startIndex = -1;
			DPWidget childI = searchList.get( 0 );
			for (int i = 0; i < searchList.size() - 1; i++)
			{
				DPWidget childJ = searchList.get( i + 1 );
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
				startIndex = searchList.size() - 1;
				start = searchList.get( startIndex );
			}
			
			DPWidget c = getLeafClosestToLocalPointFromChild( start, localPos, filter );
			if ( c != null )
			{
				return c;
			}
			else
			{
				DPWidget next = null;
				for (int j = startIndex + 1; j < searchList.size(); j++)
				{
					next = getLeafClosestToLocalPointFromChild( searchList.get( j ), localPos, filter );
					if ( next != null )
					{
						break;
					}
				}

				DPWidget prev = null;
				for (int j = startIndex - 1; j >= 0; j--)
				{
					prev = getLeafClosestToLocalPointFromChild( searchList.get( j ), localPos, filter );
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
	// TEXT REPRESENTATION METHODS
	//
	//
	
	public DPContentLeaf getLeafAtTextRepresentationPosition(int position)
	{
		DPWidget c = getChildAtTextRepresentationPosition( position );
		
		if ( c != null )
		{
			return c.getLeafAtTextRepresentationPosition( position - getTextRepresentationOffsetOfChild( c ) );
		}
		else
		{
			return null;
		}
	}

	public DPWidget getChildAtTextRepresentationPosition(int position)
	{
		int offset = 0;
		for (DPWidget c: getInternalChildren())
		{
			int end = offset + c.getTextRepresentationLength();
			if ( position >= offset  &&  position < end )
			{
				return c;
			}
			offset = end;
		}
		
		return null;
	}

	
	public int getTextRepresentationOffsetOfChild(DPWidget elem)
	{
		int offset = 0;
		for (DPWidget c: getInternalChildren())
		{
			if ( c == elem )
			{
				return offset;
			}
			offset += c.getTextRepresentationLength();
		}
		
		throw new DPContainer.CouldNotFindChildException();
	}
	
	protected int getChildTextRepresentationOffsetInSubtree(DPWidget child, DPContainer subtreeRoot)
	{
		return getTextRepresentationOffsetOfChild( child )  +  getTextRepresentationOffsetInSubtree( subtreeRoot );
	}



	public void onTextRepresentationModified()
	{
		cachedTextRep = null;
		super.onTextRepresentationModified();
	}
	
	
	public String getTextRepresentation()
	{
		if ( cachedTextRep == null )
		{
			cachedTextRep = computeSubtreeTextRepresentation();
		}
		return cachedTextRep;
	}
	
	public int getTextRepresentationLength()
	{
		return getTextRepresentation().length();
	}
	
	
	

	protected String computeSubtreeTextRepresentation()
	{
		StringBuilder builder = new StringBuilder();
		for (DPWidget child: getInternalChildren())
		{
			builder.append( child.getTextRepresentation() );
		}
		return builder.toString();
	}
	
	
	
	public void getTextRepresentationFromStartToPath(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
		DPWidget pathChild = path.get( pathMyIndex + 1 );
		for (DPWidget child: getInternalChildren())
		{
			if ( child != pathChild )
			{
				builder.append( child.getTextRepresentation() );
			}
			else
			{
				child.getTextRepresentationFromStartToPath( builder, marker, path, pathMyIndex + 1 );
				break;
			}
		}
	}
	
	public void getTextRepresentationFromPathToEnd(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
		List<DPWidget> children = getInternalChildren();
		int pathChildIndex = pathMyIndex + 1;
		DPWidget pathChild = path.get( pathChildIndex );
		int childIndex = children.indexOf( pathChild );
		
		pathChild.getTextRepresentationFromPathToEnd( builder, marker, path, pathChildIndex );

		if ( (childIndex + 1) < children.size() )
		{
			for (DPWidget child: children.subList( childIndex + 1, children.size() ))
			{
				builder.append( child.getTextRepresentation() );
			}
		}
	}

	public void getTextRepresentationBetweenPaths(StringBuilder builder, Marker startMarker, ArrayList<DPWidget> startPath, int startPathMyIndex,
			Marker endMarker, ArrayList<DPWidget> endPath, int endPathMyIndex)
	{
		List<DPWidget> children = getInternalChildren();
		
	
		int startPathChildIndex = startPathMyIndex + 1;
		int endPathChildIndex = endPathMyIndex + 1;
		
		DPWidget startChild = startPath.get( startPathChildIndex );
		DPWidget endChild = endPath.get( endPathChildIndex );
		
		int startIndex = children.indexOf( startChild );
		int endIndex = children.indexOf( endChild );
	
		
		startChild.getTextRepresentationFromPathToEnd( builder, startMarker, startPath, startPathChildIndex );
		
		for (int i = startIndex + 1; i < endIndex; i++)
		{
			builder.append( children.get( i ).getTextRepresentation() );
		}

		endChild.getTextRepresentationFromStartToPath( builder, endMarker, endPath, endPathChildIndex );
	}


	protected void getTextRepresentationFromStartOfRootToMarkerFromChild(StringBuilder builder, Marker marker, DPWidget root, DPWidget fromChild)
	{
		if ( root != this  &&  parent != null )
		{
			parent.getTextRepresentationFromStartOfRootToMarkerFromChild( builder, marker, root, this );
		}
		
		for (DPWidget child: getInternalChildren())
		{
			if ( child != fromChild )
			{
				builder.append( child.getTextRepresentation() );
			}
			else
			{
				break;
			}
		}
	}
	
	protected void getTextRepresentationFromMarkerToEndOfRootFromChild(StringBuilder builder, Marker marker, DPWidget root, DPWidget fromChild)
	{
		List<DPWidget> children = getInternalChildren();
		int childIndex = children.indexOf( fromChild );
		
		if ( (childIndex + 1) < children.size() )
		{
			for (DPWidget child: children.subList( childIndex + 1, children.size() ))
			{
				builder.append( child.getTextRepresentation() );
			}
		}

		if ( root != this  &&  parent != null )
		{
			parent.getTextRepresentationFromMarkerToEndOfRootFromChild( builder, marker, root, this );
		}
	}

	
	
	
	
	//
	//
	// LINEAR REPRESENTATION METHODS
	//
	//
	
	public void buildLinearRepresentation(ItemStreamBuilder builder)
	{
		for (DPWidget child: getInternalChildren())
		{
			child.appendToLinearRepresentation( builder );
		}
	}
	
	
	

	public void getLinearRepresentationFromStartToPath(ItemStreamBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
		DPWidget pathChild = path.get( pathMyIndex + 1 );
		for (DPWidget child: getInternalChildren())
		{
			if ( child != pathChild )
			{
				child.appendToLinearRepresentation( builder );
			}
			else
			{
				child.getLinearRepresentationFromStartToPath( builder, marker, path, pathMyIndex + 1 );
				break;
			}
		}
	}
	
	public void getLinearRepresentationFromPathToEnd(ItemStreamBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
		List<DPWidget> children = getInternalChildren();
		int pathChildIndex = pathMyIndex + 1;
		DPWidget pathChild = path.get( pathChildIndex );
		int childIndex = children.indexOf( pathChild );
		
		pathChild.getLinearRepresentationFromPathToEnd( builder, marker, path, pathChildIndex );

		if ( (childIndex + 1) < children.size() )
		{
			for (DPWidget child: children.subList( childIndex + 1, children.size() ))
			{
				child.appendToLinearRepresentation( builder );
			}
		}
	}

	public void getLinearRepresentationBetweenPaths(ItemStreamBuilder builder, Marker startMarker, ArrayList<DPWidget> startPath, int startPathMyIndex,
			Marker endMarker, ArrayList<DPWidget> endPath, int endPathMyIndex)
	{
		List<DPWidget> children = getInternalChildren();
		
	
		int startPathChildIndex = startPathMyIndex + 1;
		int endPathChildIndex = endPathMyIndex + 1;
		
		DPWidget startChild = startPath.get( startPathChildIndex );
		DPWidget endChild = endPath.get( endPathChildIndex );
		
		int startIndex = children.indexOf( startChild );
		int endIndex = children.indexOf( endChild );
	
		
		startChild.getLinearRepresentationFromPathToEnd( builder, startMarker, startPath, startPathChildIndex );
		
		for (int i = startIndex + 1; i < endIndex; i++)
		{
			children.get( i ).appendToLinearRepresentation( builder );
		}

		endChild.getLinearRepresentationFromStartToPath( builder, endMarker, endPath, endPathChildIndex );
	}


	protected void getLinearRepresentationFromStartOfRootToMarkerFromChild(ItemStreamBuilder builder, Marker marker, DPWidget root, DPWidget fromChild)
	{
		if ( root != this  &&  parent != null )
		{
			parent.getLinearRepresentationFromStartOfRootToMarkerFromChild( builder, marker, root, this );
		}
		
		appendStructuralPrefixToLinearRepresentation( builder );

		for (DPWidget child: getInternalChildren())
		{
			if ( child != fromChild )
			{
				child.appendToLinearRepresentation( builder );
			}
			else
			{
				break;
			}
		}
	}
	
	protected void getLinearRepresentationFromMarkerToEndOfRootFromChild(ItemStreamBuilder builder, Marker marker, DPWidget root, DPWidget fromChild)
	{
		List<DPWidget> children = getInternalChildren();
		int childIndex = children.indexOf( fromChild );
		
		if ( (childIndex + 1) < children.size() )
		{
			for (DPWidget child: children.subList( childIndex + 1, children.size() ))
			{
				child.appendToLinearRepresentation( builder );
			}
		}

		appendStructuralSuffixToLinearRepresentation( builder );

		if ( root != this  &&  parent != null )
		{
			parent.getLinearRepresentationFromMarkerToEndOfRootFromChild( builder, marker, root, this );
		}
	}

	
	
	
	
	//
	// Meta-element
	//
	
	static EmptyBorder metaIndentBorder = new EmptyBorder( 25.0, 0.0, 0.0, 0.0 );
	static VBoxStyleSheet metaVBoxStyle = new VBoxStyleSheet( VTypesetting.ALIGN_WITH_BOTTOM, HAlignment.LEFT, 0.0, false, 0.0 );
	
	public DPBorder getMetaHeaderBorderWidget()
	{
		if ( metaElement != null )
		{
			DPVBox metaVBox = (DPVBox)metaElement;
			return (DPBorder)metaVBox.get( 0 );
		}
		else
		{
			return null;
		}
	}
	
	public DPWidget createMetaElement()
	{
		DPVBox metaChildrenVBox = new DPVBox( metaVBoxStyle );
		for (DPWidget child: getChildren())
		{
			if ( child != null )
			{
				DPWidget metaChild = child.initialiseMetaElement();
				metaChildrenVBox.append( metaChild );
			}
			else
			{
				System.out.println( "DPContainer.createMetaElement(): null child in " + getClass().getName() );
			}
		}
		
		DPBorder indentMetaChildren = new DPBorder( metaIndentBorder );
		indentMetaChildren.setChild( metaChildrenVBox );
		
		DPVBox metaVBox = new DPVBox( metaVBoxStyle );
		metaVBox.append( createMetaHeader() );
		metaVBox.append( indentMetaChildren );
		
		return metaVBox;
	}
	
	public void refreshMetaElement()
	{
		if ( metaElement != null )
		{
			DPVBox metaVBox = (DPVBox)metaElement;
			
			DPBorder indentMetaChildren = (DPBorder)metaVBox.get( 1 );
			DPVBox metaChildrenVBox = (DPVBox)indentMetaChildren.getChild();

			ArrayList<DPWidget> childMetaElements = new ArrayList<DPWidget>();
			for (DPWidget child: getChildren())
			{
				DPWidget metaChild = child.initialiseMetaElement();
				childMetaElements.add( metaChild );
			}
			metaChildrenVBox.setChildren( childMetaElements );
		}
	}

	public void shutdownMetaElement()
	{
		if ( metaElement != null )
		{
			for (DPWidget child: getChildren())
			{
				child.shutdownMetaElement();
			}
		}
		super.shutdownMetaElement();
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
