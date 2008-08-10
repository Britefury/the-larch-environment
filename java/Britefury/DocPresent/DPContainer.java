package Britefury.DocPresent;

import java.util.Vector;
import java.util.HashMap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

import Britefury.DocPresent.Event.PointerButtonEvent;
import Britefury.DocPresent.Event.PointerMotionEvent;
import Britefury.DocPresent.Event.PointerScrollEvent;
import Britefury.DocPresent.Input.PointerInterface;
import Britefury.Math.AABox2;
import Britefury.Math.Point2;
import Britefury.Math.Vector2;
import Britefury.Math.Xform2;




public abstract class DPContainer extends DPWidget {
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
	protected Color backgroundColour;
	
	
	
	public DPContainer()
	{
		this( null );
	}
	
	public DPContainer(Color backgroundColour)
	{
		childEntries = new Vector<ChildEntry>();
		childToEntry = new HashMap<DPWidget, ChildEntry>();
		
		pointerChildEntryTable = new HashMap<PointerInterface, ChildEntry>();
		pointerDndChildEntryTable = new HashMap<PointerInterface, ChildEntry>();
		
		this.backgroundColour = backgroundColour;
	}
	
	
	
	public Color getBackgroundColour()
	{
		return backgroundColour;
	}
	
	public void setBackgroundColour(Color colour)
	{
		backgroundColour = colour;
		queueFullRedraw();
	}

	
	
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
	
	
	public Xform2 getChildTransformRelativeToAncestor(DPWidget child, DPWidget ancestor, Xform2 x)
	{
		ChildEntry entry = childToEntry.get( child );
		Xform2 localX = x.concat( entry.childToContainerXform );
		return getTransformRelativeToAncestor( ancestor, localX );
	}

	public Point2 getChildLocalPointRelativeToAncestor(DPWidget child, DPWidget ancestor, Point2 p)
	{
		ChildEntry entry = childToEntry.get( child );
		Point2 localP = entry.childToContainerXform.transform( p );
		return getLocalPointRelativeToAncestor( ancestor, localP );
	}
	
	
	
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
	}
	
	
	protected abstract void removeChild(DPWidget child);
	
	
	
	
	
	protected void refreshScale(double scale, double rootScale)
	{
		super.refreshScale( scale, rootScale );
		
		for (ChildEntry childEntry: childEntries)
		{
			childEntry.child.setScale( 1.0, rootScale );
		}
	}
	
	
	

	protected void onLeaveIntoChild(PointerMotionEvent event, DPWidget child)
	{
	}
	
	protected void onEnterFromChild(PointerMotionEvent event, DPWidget child)
	{
	}
	
	
	
	
	
	protected void onChildResizeRequest(DPWidget child)
	{
	}
	
	protected void childResizeRequest(DPWidget child)
	{
		onChildResizeRequest( child );
	}
	
	
	
	
	protected HMetrics allocateChildX(DPWidget child, double localPosX, double localWidth)
	{
		double childWidth = localWidth / child.scale;
		HMetrics hm = child.allocateX( childWidth );
		ChildEntry entry = childToEntry.get( child );
		
		entry.pos.x = localPosX;
		entry.size.x = hm.width;
		
		return hm;
	}
	
	protected VMetrics allocateChildY(DPWidget child, double localPosY, double localHeight)
	{
		double childHeight = localHeight / child.scale;
		VMetrics vm = child.allocateY( childHeight );
		ChildEntry entry = childToEntry.get( child );
		
		entry.pos.y = localPosY;
		entry.size.y = vm.height;
		
		entry.childToContainerXform = new Xform2( child.scale, entry.pos.toVector2() );
		entry.containerToChildXform = entry.childToContainerXform.inverse();
		entry.box = new AABox2( entry.pos, entry.pos.add( entry.size ) );
		
		return vm;
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
				boolean bHandled = entry.child.handleButtonDown( event.transformed( entry.containerToChildXform) );
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
				Vector2 translation = entry.childToContainerXform.translation;
				double scale = entry.childToContainerXform.scale;
				graphics.translate( translation.x, translation.y );
				graphics.scale( scale, scale );
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
}
