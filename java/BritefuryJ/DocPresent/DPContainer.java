//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.Vector;
import java.util.HashMap;
import java.util.List;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
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




public abstract class DPContainer extends DPWidget implements ContentInterface
{
	public static class CouldNotFindChildException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	
	protected static class ChildEntry
	{
		public DPWidget child;
		public Xform2 childToContainerXform, containerToChildXform;
		public AABox2 box;
		public Point2 pos;
		public Vector2 size;
		
		
		public ChildEntry(DPWidget child)
		{
			this.child = child;
			childToContainerXform = new Xform2();
			containerToChildXform = new Xform2();
			box = new AABox2();
			pos = new Point2();
			size = new Vector2();
		}
		
		
		public boolean isContainerSpacePointWithinBounds(Point2 p)
		{
			return box.containsPoint( p );
		}
	}
	
	
	
	
	protected Vector<ChildEntry> childEntries;
	protected HashMap<DPWidget, ChildEntry> childToEntry;
	protected ChildEntry pressGrabChildEntry;
	protected int pressGrabButton;
	protected HashMap<PointerInterface, ChildEntry> pointerChildEntryTable, pointerDndChildEntryTable;
	
	
	
	
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
		
		childEntries = new Vector<ChildEntry>();
		childToEntry = new HashMap<DPWidget, ChildEntry>();
		
		pointerChildEntryTable = new HashMap<PointerInterface, ChildEntry>();
		pointerDndChildEntryTable = new HashMap<PointerInterface, ChildEntry>();
	}
	
	
	
	//
	// Geometry methods
	//
	
	protected Xform2 getChildTransformRelativeToAncestor(DPWidget child, DPWidget ancestor, Xform2 x)
	{
		ChildEntry entry = childToEntry.get( child );
		Xform2 localX = x.concat( entry.childToContainerXform );
		return getTransformRelativeToAncestor( ancestor, localX );
	}

	protected Point2 getChildLocalPointRelativeToAncestor(DPWidget child, DPWidget ancestor, Point2 p)
	{
		ChildEntry entry = childToEntry.get( child );
		Point2 localP = entry.childToContainerXform.transform( p );
		return getLocalPointRelativeToAncestor( ancestor, localP );
	}
	

	protected void refreshScale(double scale, double rootScale)
	{
		super.refreshScale( scale, rootScale );
		
		for (ChildEntry childEntry: childEntries)
		{
			childEntry.child.setScale( 1.0, rootScale );
		}
	}
	
	
	
	
	//
	// Child registration methods
	//
	
	protected ChildEntry registerChildEntry(ChildEntry childEntry)
	{
		DPWidget child = childEntry.child;
		
		childToEntry.put( child, childEntry );
		
		child.unparent();
		
		child.setParent( this, presentationArea );
		
		if ( isRealised() )
		{
			child.handleRealise();
		}
		
		structureChanged();
		
		return childEntry;
	}
	
	protected void unregisterChildEntry(ChildEntry childEntry)
	{
		DPWidget child = childEntry.child;
		
		if ( isRealised() )
		{
			child.handleUnrealise();
		}
		
		child.setParent( null, null );
		
		childToEntry.remove( child );

		structureChanged();
	}
	
	
	
	
	
	
	//
	// Tree structure methods
	//
	
	
	protected abstract void removeChild(DPWidget child);
	
	public boolean hasChild(DPWidget child)
	{
		for (ChildEntry entry: childEntries)
		{
			if ( child == entry.child )
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	protected abstract List<DPWidget> getChildren();
	
	
	
	
	
	protected void structureChanged()
	{
		if ( parent != null )
		{
			parent.structureChanged();
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
		ChildEntry entry = childToEntry.get( child );
		
		entry.pos.x = localPosX;
		entry.size.x = localWidth;
	}
	
	protected void allocateChildY(DPWidget child, double localPosY, double localHeight)
	{
		double childHeight = localHeight / child.scale;
		child.allocateY( childHeight );
		ChildEntry entry = childToEntry.get( child );
		
		entry.pos.y = localPosY;
		entry.size.y = localHeight;
		
		entry.childToContainerXform = new Xform2( child.scale, entry.pos.toVector2() );
		entry.containerToChildXform = entry.childToContainerXform.inverse();
		entry.box = new AABox2( entry.pos, entry.pos.add( entry.size ) );
	}
	
	
	
	
	
	
	protected void childRedrawRequest(DPWidget child, Point2 childPos, Vector2 childSize)
	{
		ChildEntry entry = childToEntry.get( child );
		Point2 localPos = entry.childToContainerXform.transform( childPos );
		Vector2 localSize = entry.childToContainerXform.transform( childSize );
		queueRedraw( localPos, localSize );
	}
	
	
	
	protected ChildEntry getChildEntryAtLocalPoint(Point2 localPos)
	{
		for (ChildEntry entry: childEntries)
		{
			if ( entry.box.containsPoint( localPos ) )
			{
				return entry;
			}
		}
		
		return null;
	}
	
	protected abstract ChildEntry getChildEntryClosestToLocalPoint(Point2 localPos);
	
	
	
	//
	// Drag and drop methods
	//
	
	protected DndDrag handleDndButtonDown(PointerButtonEvent event)
	{
		ChildEntry entry = getChildEntryAtLocalPoint( event.pointer.getLocalPos() );
		if ( entry != null )
		{
			DndDrag drag = entry.child.handleDndButtonDown( event.transformed( entry.containerToChildXform ) );
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
		ChildEntry entry = getChildEntryAtLocalPoint( event.pointer.getLocalPos() );
		if ( entry != null )
		{
			boolean bDropped = entry.child.handleDndMotion( event.transformed( entry.containerToChildXform ), drag );
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
		ChildEntry entry = getChildEntryAtLocalPoint( event.pointer.getLocalPos() );
		if ( entry != null )
		{
			boolean bDropped = entry.child.handleDndButtonUp( event.transformed( entry.containerToChildXform ), drag );
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
		if ( pressGrabChildEntry == null )
		{
			ChildEntry entry = getChildEntryAtLocalPoint( event.pointer.getLocalPos() );
			if ( entry != null )
			{
				boolean bHandled = entry.child.handleButtonDown( event.transformed( entry.containerToChildXform ) );
				if ( bHandled )
				{
					pressGrabChildEntry = entry;
					pressGrabButton = event.button;
					return true;
				}
			}
			
			if ( pressGrabChildEntry != null )
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
			return pressGrabChildEntry.child.handleButtonDown( event.transformed( pressGrabChildEntry.containerToChildXform ) );
		}
	}
	
	protected boolean handleButtonDown2(PointerButtonEvent event)
	{
		if ( pressGrabChildEntry != null )
		{
			return pressGrabChildEntry.child.handleButtonDown2( event.transformed( pressGrabChildEntry.containerToChildXform ) );
		}
		else
		{
			return onButtonDown2( event );
		}
	}
	
	protected boolean handleButtonDown3(PointerButtonEvent event)
	{
		if ( pressGrabChildEntry != null )
		{
			return pressGrabChildEntry.child.handleButtonDown3( event.transformed( pressGrabChildEntry.containerToChildXform ) );
		}
		else
		{
			return onButtonDown3( event );
		}
	}
	
	protected boolean handleButtonUp(PointerButtonEvent event)
	{
		if ( pressGrabChildEntry != null )
		{
			PointerButtonEvent childSpaceEvent = event.transformed( pressGrabChildEntry.containerToChildXform );
			if ( event.button == pressGrabButton )
			{
				pressGrabButton = 0;
				Point2 localPos = event.pointer.getLocalPos();
				if ( !pressGrabChildEntry.isContainerSpacePointWithinBounds( localPos ) )
				{
					pressGrabChildEntry.child.handleLeave( new PointerMotionEvent( childSpaceEvent.pointer, PointerMotionEvent.Action.LEAVE ) );
				}
				
				boolean bHandled = pressGrabChildEntry.child.handleButtonUp( childSpaceEvent );
				ChildEntry savedPressGrabChildEntry = pressGrabChildEntry;
				pressGrabChildEntry = null;
				
				if ( localPos.x >= 0.0  &&  localPos.x <= allocation.x  &&  localPos.y >= 0.0  &&  localPos.y <= allocation.y )
				{
					ChildEntry entry = getChildEntryAtLocalPoint( localPos );
					if ( entry != null )
					{
						if ( entry != savedPressGrabChildEntry )
						{
							entry.child.handleEnter( new PointerMotionEvent( childSpaceEvent.pointer, PointerMotionEvent.Action.ENTER ) );
						}
						pointerChildEntryTable.put( event.pointer.concretePointer(), entry );
					}
					else
					{
						pointerChildEntryTable.remove( event.pointer.concretePointer() );
						onEnter( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.ENTER ) );
					}
				}
				
				return bHandled;
			}
			else
			{
				return pressGrabChildEntry.child.handleButtonUp( childSpaceEvent );
			}
		}
		else
		{
			return onButtonUp( event );
		}
	}


	protected void handleMotion(PointerMotionEvent event)
	{
		if ( pressGrabChildEntry != null )
		{
			pressGrabChildEntry.child.handleMotion( event.transformed( pressGrabChildEntry.containerToChildXform ) );
		}
		else
		{
			ChildEntry pointerChildEntry = pointerChildEntryTable.get( event.pointer.concretePointer() );
			ChildEntry oldPointerChildEntry = pointerChildEntry;
			
			if ( pointerChildEntry != null )
			{
				if ( !pointerChildEntry.isContainerSpacePointWithinBounds( event.pointer.getLocalPos() ) )
				{
					pointerChildEntry.child.handleLeave( new PointerMotionEvent( event.pointer.transformed( pointerChildEntry.containerToChildXform ), PointerMotionEvent.Action.LEAVE ) );
					pointerChildEntryTable.remove( event.pointer.concretePointer() );
					pointerChildEntry = null;
				}
				else
				{
					pointerChildEntry.child.handleMotion( event.transformed( pointerChildEntry.containerToChildXform ) );
				}
			}
			
			if ( pointerChildEntry == null )
			{
				ChildEntry entry = getChildEntryAtLocalPoint( event.pointer.getLocalPos() );
				if ( entry != null )
				{
					entry.child.handleEnter( event.transformed( entry.containerToChildXform ) );
					pointerChildEntry = entry;
					pointerChildEntryTable.put( event.pointer.concretePointer(), pointerChildEntry );
				}
			}
			
			if ( oldPointerChildEntry == null  &&  pointerChildEntry != null )
			{
				onLeaveIntoChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.LEAVE ), pointerChildEntry.child );
			}
			else if ( oldPointerChildEntry != null  &&  pointerChildEntry == null )
			{
				onEnterFromChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.ENTER ), oldPointerChildEntry.child );
			}
		}
		
		onMotion( event );
	}
	
	protected void handleEnter(PointerMotionEvent event)
	{
		onEnter( event );
		
		Point2 localPos = event.pointer.getLocalPos();
		
		for (int i = childEntries.size() - 1; i >= 0; i--)
		{
			ChildEntry entry = childEntries.get( i );
			if ( entry.isContainerSpacePointWithinBounds( localPos ) )
			{
				entry.child.handleEnter( event.transformed( entry.containerToChildXform ) );
				pointerChildEntryTable.put( event.pointer.concretePointer(), entry );
				onLeaveIntoChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.LEAVE ), entry.child );
				break;
			}
		}
	}
	
	protected void handleLeave(PointerMotionEvent event)
	{
		if ( pressGrabChildEntry == null )
		{
			ChildEntry pointerChildEntry = pointerChildEntryTable.get( event.pointer.concretePointer() );
			if ( pointerChildEntry != null )
			{
				pointerChildEntry.child.handleLeave( event.transformed( pointerChildEntry.containerToChildXform ) );
				onEnterFromChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.ENTER ), pointerChildEntry.child );
				pointerChildEntryTable.remove( event.pointer.concretePointer() );
			}
		}

		onLeave( event );
	}
	
	
	
	protected boolean handleScroll(PointerScrollEvent event)
	{
		ChildEntry pointerChildEntry = pointerChildEntryTable.get( event.pointer.concretePointer() );
		if ( pressGrabChildEntry != null )
		{
			pressGrabChildEntry.child.handleScroll( event.transformed( pressGrabChildEntry.containerToChildXform ) );
		}
		else if ( pointerChildEntry != null )
		{
			pointerChildEntry.child.handleScroll( event.transformed( pressGrabChildEntry.containerToChildXform ) );
		}
		return onScroll( event );
	}
	
	
	
	protected void handleRealise()
	{
		super.handleRealise();
		for (ChildEntry entry: childEntries)
		{
			entry.child.handleRealise();
		}
	}
	
	protected void handleUnrealise()
	{
		for (ChildEntry entry: childEntries)
		{
			entry.child.handleUnrealise();
		}
		super.handleUnrealise();
	}
	
	
	
	protected void drawBackground(Graphics2D graphics)
	{
		Color backgroundColour = getStyleSheet().getBackgroundColour();
		if ( backgroundColour != null )
		{
			graphics.setColor( backgroundColour );
			graphics.fill( new Rectangle2D.Double( 0.0, 0.0, allocation.x, allocation.y ) );
		}
	}
	
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		drawBackground( graphics );
		super.handleDraw( graphics, areaBox );
		
		AffineTransform currentTransform = graphics.getTransform();
		for (ChildEntry entry: childEntries)
		{
			if ( entry.box.intersects( areaBox ) )
			{
				entry.childToContainerXform.apply( graphics );
				entry.child.handleDraw( graphics, entry.containerToChildXform.transform( areaBox ) );
				graphics.setTransform( currentTransform );
			}
		}
	}
	
	
	
	
	protected ChildEntry createChildEntryForChild(DPWidget child)
	{
		return new ChildEntry( child );
	}
	
	
	
	protected void setPresentationArea(DPPresentationArea area)
	{
		super.setPresentationArea( area );
		
		for (ChildEntry entry: childEntries)
		{
			entry.child.setPresentationArea( area );
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
			return parent.getContentLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( parent, localCursorPos ), bSkipWhitespace );
		}
		else
		{
			return null;
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
	
	
	
	
	
	//
	//
	// CONTENT METHODS
	//
	//
	
	public int getContentOffsetOfChild(DPWidget child)
	{
		int offset = 0;
		for (DPWidget c: getChildren())
		{
			if ( c == child )
			{
				return offset;
			}
			offset += c.getContentLength();
		}
		
		throw new CouldNotFindChildException();
	}

	public DPWidget getChildAtContentPosition(int position)
	{
		int offset = 0;
		for (DPWidget c: getChildren())
		{
			int end = offset + c.getContentLength();
			if ( position >= offset  &&  position < end )
			{
				return c;
			}
			offset = end;
		}
		
		return null;
	}

	public DPContentLeaf getLeafAtContentPosition(int position)
	{
		DPWidget c = getChildAtContentPosition( position );
		
		if ( c != null )
		{
			return c.getLeafAtContentPosition( position - getContentOffsetOfChild( c ) );
		}
		else
		{
			return null;
		}
	}
	
	
	
	protected int getChildContentOffsetInSubtree(DPWidget child, DPContainer subtreeRoot)
	{
		return getContentOffsetOfChild( child )  +  getContentOffsetInSubtree( subtreeRoot );
	}





	//
	//
	// MARKER METHODS
	//
	//

	public Marker marker(int position, Marker.Bias bias)
	{
		DPContentLeaf leaf = getLeafAtContentPosition( position );
		
		if ( leaf != null )
		{
			return leaf.marker( position - leaf.getContentOffsetInSubtree( this ), bias );
		}
		else
		{
			throw new Marker.InvalidMarkerPosition();
		}
	}
	
	public Marker markerAtStart()
	{
		DPContentLeaf leaf = getLeftContentLeaf();
		
		if ( leaf != null )
		{
			return leaf.markerAtStart();
		}
		else
		{
			throw new Marker.InvalidMarkerPosition();
		}
	}
	
	public Marker markerAtEnd()
	{
		DPContentLeaf leaf = getRightContentLeaf();
		
		if ( leaf != null )
		{
			return leaf.markerAtEnd();
		}
		else
		{
			throw new Marker.InvalidMarkerPosition();
		}
	}
	
	
	public void moveMarker(Marker m, int position, Marker.Bias bias)
	{
		DPContentLeaf leaf = getLeafAtContentPosition( position );
		
		if ( leaf != null )
		{
			leaf.moveMarker( m, position - leaf.getContentOffsetInSubtree( this ), bias );
		}
		else
		{
			throw new Marker.InvalidMarkerPosition();
		}
	}
	
	public void moveMarkerToStart(Marker m)
	{
		DPContentLeaf leaf = getLeftContentLeaf();
		
		if ( leaf != null )
		{
			leaf.moveMarkerToStart( m );
		}
		else
		{
			throw new Marker.InvalidMarkerPosition();
		}
	}
	
	public void moveMarkerToEnd(Marker m)
	{
		DPContentLeaf leaf = getRightContentLeaf();
		
		if ( leaf != null )
		{
			leaf.moveMarkerToEnd( m );
		}
		else
		{
			throw new Marker.InvalidMarkerPosition();
		}
	}
	
	
	
	public boolean isMarkerAtStart(Marker m)
	{
		DPContentLeaf leaf = getLeftContentLeaf();
		
		if ( leaf != null )
		{
			return leaf.isMarkerAtStart( m );
		}
		else
		{
			throw new Marker.InvalidMarkerPosition();
		}
	}
	
	public boolean isMarkerAtEnd(Marker m)
	{
		DPContentLeaf leaf = getRightContentLeaf();
		
		if ( leaf != null )
		{
			return leaf.isMarkerAtEnd( m );
		}
		else
		{
			throw new Marker.InvalidMarkerPosition();
		}
	}
}
