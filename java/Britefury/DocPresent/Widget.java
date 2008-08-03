package Britefury.DocPresent;

import java.lang.Boolean;

import java.util.LinkedList;
import java.util.Vector;
import java.util.HashMap;

import java.awt.event.KeyEvent;
import java.awt.Graphics2D;
import java.awt.geom.*;

import Britefury.DocPresent.Event.PointerButtonEvent;
import Britefury.DocPresent.Event.PointerMotionEvent;
import Britefury.DocPresent.Event.PointerScrollEvent;
import Britefury.DocPresent.Input.InputState;
import Britefury.DocPresent.Input.InputTable;
import Britefury.DocPresent.Input.PointerInterface;
import Britefury.Math.AABox2;
import Britefury.Math.Point2;
import Britefury.Math.Vector2;
import Britefury.Math.Xform2;





public class Widget {
	//
	//
	// EXCEPTIONS
	//
	//
	
	public static class CouldNotFindWidgetAncestorException extends RuntimeException {
		static final long serialVersionUID = 0L;
		
		public CouldNotFindWidgetAncestorException()
		{
		}
	}
	
	
	public static class DndDisabledException extends RuntimeException {
		static final long serialVersionUID = 0L;
		
		public DndDisabledException()
		{
		}
	}

	
	public static class DndOperationAlreadyInList extends RuntimeException {
		static final long serialVersionUID = 0L;
		
		public DndOperationAlreadyInList()
		{
		}
	}

	
	public static class DndOperationNotInList extends RuntimeException {
		static final long serialVersionUID = 0L;
		
		public DndOperationNotInList()
		{
		}
	}
	
	
	
	//
	//
	// Immediate Event interface
	//
	//
	
	public static interface IImmediateEvent {
		abstract void onEvent();
	}
	

	
	
	//
	//
	// Drag and drop classes
	//
	//
	
	// Drag and drop state
	protected static class DndState {
		protected Vector<DndOperation> sourceOps, destOps;


		protected IDndListener dndListener;
		
		
		protected DndState()
		{
			sourceOps = new Vector<DndOperation>();
			destOps = new Vector<DndOperation>();
		}
	}

	

	//
	//
	// ENUMERATIONS
	//
	//

	
	// Cursor movement
	public enum CursorMovement {
		MOVE,
		DRAG
	};
	
	
	// Focus policy
	public enum FocusPolicy {
		IGNORE,
		CHILDRENFIRST,
		TAKE
	};
	
	
	
	
	//
	//
	// FIELDS
	//
	//
	
	protected Container parent;
	protected PresentationArea presentationArea;
	protected boolean bHasFocus, bFocusGrabbed, bRealised, bResizeQueued;
	protected double scale, rootScale;
	protected HMetrics hmetrics;
	protected VMetrics vmetrics;
	protected Vector2 allocation;
	
	protected LinkedList<IImmediateEvent> waitingImmediateEvents;
	
	protected LinkedList<PointerInterface> pointersWithinBounds;
	
	protected DndState dndState;
	
	
	
	

	//
	//
	// METHODS
	//
	//
	
	
	//
	//
	// Constructor
	//
	//
	
	public Widget()
	{
		scale = rootScale = 1.0;
		hmetrics = new HMetrics();
		vmetrics = new VMetrics();
		allocation = new Vector2();
		waitingImmediateEvents = new LinkedList<IImmediateEvent>();
		pointersWithinBounds = new LinkedList<PointerInterface>();
	}
	
	

	//
	// Geometry methods
	//
	
	public Vector2 getAllocation()
	{
		return allocation;
	}
	
	
	public AABox2 getLocalAABox()
	{
		return new AABox2( new Point2(), new Point2( allocation ) );
	}
	
	
	public Xform2 getTransformRelativeToRoot(Xform2 x)
	{
		return getTransformRelativeToAncestor( null, x );
	}
	
	public Xform2 getTransformRelativeToRoot()
	{
		return getTransformRelativeToRoot( new Xform2() );
	}
	
	
	
	public Xform2 getTransformRelativeToAncestor(Widget ancestor, Xform2 x)
	{
		if ( ancestor == this )
		{
			return x;
		}
		else if ( parent != null )
		{
			return parent.getChildTransformRelativeToAncestor( this, ancestor, x );
		}
		else
		{
			if ( ancestor != null )
			{
				throw new CouldNotFindWidgetAncestorException();
			}
			return x;
		}
	}
	
	public Xform2 getTransformRelativeToAncestor(Widget ancestor)
	{
		return getTransformRelativeToAncestor( ancestor, new Xform2() );
	}
	
	
	
	public Xform2 getTransformRelativeTo(Widget toWidget, Xform2 x)
	{
		Xform2 myXform = getTransformRelativeToRoot();
		Xform2 toWidgetXform = toWidget.getTransformRelativeToRoot();
		return myXform.concat( toWidgetXform.inverse() );
	}
	
	
	public Point2 getLocalPointRelativeToRoot(Point2 p)
	{
		return getLocalPointRelativeToAncestor( null, p );
	}
	
	public Point2 getLocalPointRelativeToAncestor(Widget ancestor, Point2 p)
	{
		if ( ancestor == this )
		{
			return p;
		}
		else if ( parent != null )
		{
			return parent.getChildLocalPointRelativeToAncestor( this, ancestor, p );
		}
		else
		{
			if ( ancestor != null )
			{
				throw new CouldNotFindWidgetAncestorException();
			}
			return p;
		}
	}
	
	public Point2 getLocalPointRelativeTo(Widget toWidget, Point2 p)
	{
		Point2 pointInRoot = getLocalPointRelativeToRoot( p );
		Xform2 toWidgetXform = toWidget.getTransformRelativeToRoot();
		return toWidgetXform.inverse().transform( pointInRoot );
	}
	
	
	
	
	//
	// Widget tree methods
	//
	
	public boolean isRealised()
	{
		return bRealised;
	}
	
	public PresentationArea getPresentationArea()
	{
		return presentationArea;
	}
	
	
	public Container getParent()
	{
		return parent;
	}
	
	protected void setParent(Container parent, PresentationArea area)
	{
		this.parent = parent;
		setPresentationArea( area );
	}
	
	
	protected void unparent()
	{
		if ( parent != null )
		{
			parent.removeChild( this );
		}
		presentationArea = null;
	}
	
	
	protected void setPresentationArea(PresentationArea area)
	{
		if ( area != presentationArea )
		{
			presentationArea = area;
			if ( presentationArea != null )
			{
				for (IImmediateEvent event: waitingImmediateEvents)
				{
					presentationArea.queueImmediateEvent( event );
				}
				waitingImmediateEvents.clear();
			}
			onSetPresentationArea( area );
		}
	}
	
	
	protected void onSetPresentationArea(PresentationArea area)
	{
	}

	
	
	

	
	//
	// Focus methods
	//

	public void grabFocus()
	{
		if ( !bFocusGrabbed )
		{
			bFocusGrabbed = true;
			if ( presentationArea != null )
			{
				return presentationArea.takeFocusGrab( this );
			}
		}
	}

	public void ungrabFocus()
	{
		if ( bFocusGrabbed )
		{
			bFocusGrabbed = false;
			if ( presentationArea != null )
			{
				return presentationArea.relinquishFocusGrab( this );
			}
		}
	}
	
	
	
	
	//
	// Drag and drop methods
	//
	
	
	public void enableDnd()
	{
		if ( dndState == null )
		{
			dndState = new DndState();
		}
	}
	
	public void disableDnd()
	{
		if ( dndState != null )
		{
			dndState = null;
		}
	}
	
	public boolean isDndEnabled()
	{
		return dndState != null;
	}
	
	
	
	public void addDndSourceOp(DndOperation op) throws DndOperationAlreadyInList
	{
		if ( !isDndEnabled() )
		{
			throw new DndDisabledException();
		}
		
		if ( dndState.sourceOps.contains( op ) )
		{
			throw new DndOperationAlreadyInList();
		}
		
		dndState.sourceOps.add( op );
	}

	public void removeDndSourceOp(DndOperation op) throws DndOperationNotInList
	{
		if ( !isDndEnabled() )
		{
			throw new DndDisabledException();
		}
		
		if ( !dndState.sourceOps.contains( op ) )
		{
			throw new DndOperationNotInList();
		}
		
		dndState.sourceOps.remove( op );
	}



	public void addDndDestOp(DndOperation op) throws DndOperationAlreadyInList
	{
		if ( !isDndEnabled() )
		{
			throw new DndDisabledException();
		}
		
		if ( dndState.destOps.contains( op ) )
		{
			throw new DndOperationAlreadyInList();
		}
		
		dndState.destOps.add( op );
	}

	public void removeDndDestOp(DndOperation op) throws DndOperationNotInList
	{
		if ( !isDndEnabled() )
		{
			throw new DndDisabledException();
		}
		
		if ( !dndState.destOps.contains( op ) )
		{
			throw new DndOperationNotInList();
		}
		
		dndState.destOps.remove( op );
	}
	
	
	
	//
	//
	// DRAG AND DROP PROTOCOL
	//
	// 1. The user presses a mouse button:
	//    onButtonDown is sent.
	//    onDndButtonDown is also sent. If a widget with DnD enabled can be found, it creates and returns a DndDrag structure that will be used to track information
	//    on the drag.
	// 2. The first motion event after the button press:
	//    onDndBegin is sent. It calls the onDndBegin() method of the DnD listener. The resulting DnD begin data is stored in the beginData field of the DnDDrag structure.
	//	  onDndMotion is also sent
	// 3. Motion events:
	//	  onDndMotion is sent
	//      This results in the dndCanDropFrom() method of the DnD listener being called to determine if a drop to this widget is possible. If so, then
	//      the onDndMotion() method of the DnD listener is sent
	// 
	//
	//
	//
	
	
	
	protected DndDrag onDndButtonDown(PointerButtonEvent event)
	{
		if ( dndState != null  &&  dndState.sourceOps.size() > 0 )
		{
			return new DndDrag( this, event );
		}
		else
		{
			return null;
		}
	}
	
	protected DndDrag handleDndButtonDown(PointerButtonEvent event)
	{
		return onDndButtonDown( event );
	}


	
	
	protected void onDndBegin(PointerMotionEvent event, DndDrag drag)
	{
		if ( dndState != null  &&  dndState.dndListener != null )
		{
			drag.beginData = dndState.dndListener.onDndBegin( drag );
		}
	}
	
	protected void handleDndBegin(PointerMotionEvent event, DndDrag drag)
	{
		if ( dndState != null )
		{
			onDndBegin( event, drag );
		}
	}
	
	

	
	protected boolean onDndMotion(PointerMotionEvent event, DndDrag drag)
	{
		Widget dndSrc = drag.srcWidget;
		if ( dndState != null  &&  dndSrc.dndState != null )
		{
			boolean bCanDrop = false;
			
			for (DndOperation op: dndState.destOps)
			{
				if ( dndSrc.dndState.sourceOps.contains( op ) )
				{
					bCanDrop = dndCanDropFrom( event.pointer, drag );
				}
			}
			
			if ( bCanDrop )
			{
				if ( dndState.dndListener != null )
				{
					dndState.dndListener.onDndMotion( drag, this, event.pointer.getLocalPos() );
				}
				return true;
			}
		}
		
		return false;
	}
	
	protected boolean handleDndMotion(PointerMotionEvent event, DndDrag drag)
	{
		return onDndMotion( event, drag );
	}
	
	protected boolean dndCanDropFrom(PointerInterface pointer, DndDrag drag)
	{
		if ( dndState != null )
		{
			if ( dndState.dndListener != null )
			{
				return dndState.dndListener.dndCanDropFrom( drag, this, pointer.getLocalPos() );
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
	

	
	
	
	protected boolean onDndButtonUp(PointerButtonEvent event, DndDrag drag)
	{
		if ( dndState != null )
		{
			Widget dndSrc = drag.srcWidget;

			if ( dndSrc.dndState != null )
			{
				for (DndOperation op: dndState.destOps)
				{
					if ( dndSrc.dndState.sourceOps.contains( op ) )
					{
						boolean bCanDrop = dndCanDropFrom( event.pointer, drag );
						if ( bCanDrop )
						{
							Object dndData = dndSrc.dndDragTo( drag, this );
							drag.dragData = dndData;
							dndDropFrom( drag, event.pointer );
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	protected boolean handleDndButtonUp(PointerButtonEvent event, DndDrag drag)
	{
		return onDndButtonUp( event, drag );
	}
	
	
	protected Object dndDragTo(DndDrag drag, Widget dragDest)
	{
		if ( dndState != null  &&  dndState.dndListener != null )
		{
			return dndState.dndListener.dndDragTo( drag, dragDest );
		}
		else
		{
			return null;
		}
	}
	
	protected void dndDropFrom(DndDrag drag, PointerInterface pointer)
	{
		if ( dndState != null  &&  dndState.dndListener != null )
		{
			dndState.dndListener.dndDropFrom( drag, this, pointer.getLocalPos() );
		}
	}

	
	
	
	//
	// Immediate event queue methods
	//
	
	public void queueImmediateEvent(IImmediateEvent event)
	{
		if ( presentationArea != null )
		{
			presentationArea.queueImmediateEvent( event );
		}
		else
		{
			waitingImmediateEvents.push( event );
		}
			
	}

	public void dequeueImmediateEvent(IImmediateEvent event)
	{
		if ( presentationArea != null )
		{
			presentationArea.dequeueImmediateEvent( event );
		}
		else
		{
			waitingImmediateEvents.remove( event );
		}
			
	}
	
	
	
	
	//
	// General event methods
	//
	
	protected boolean onButtonDown(PointerButtonEvent event)
	{
		return false;
	}

	protected boolean onButtonDown2(PointerButtonEvent event)
	{
		return false;
	}
	
	protected boolean onButtonDown3(PointerButtonEvent event)
	{
		return false;
	}

	protected boolean onButtonUp(PointerButtonEvent event)
	{
		return false;
	}


	protected void onMotion(PointerMotionEvent event)
	{
	}

	protected void onEnter(PointerMotionEvent events)
	{
	}

	protected void onLeave(PointerMotionEvent event)
	{
	}
	
	
	protected boolean onScroll(PointerScrollEvent event)
	{
		return false;
	}
	
	
	protected boolean onKeyPress(KeyEvent event)
	{
		return false;
	}
	
	protected boolean onKeyRelease(KeyEvent event)
	{
		return false;
	}
	
	
	protected void onGainFocus()
	{
		bHasFocus = true;
	}
	
	protected void onLoseFocus()
	{
		bHasFocus = false;
	}
	
	
	protected void onRealise()
	{
	}
	
	protected void onUnrealise()
	{
	}
	
	
	protected void draw(Graphics2D graphics)
	{
	}
	
	
	protected void onSetScale(double scale, double rootScale)
	{
	}
	
	protected HMetrics computeRequiredHMetrics()
	{
		return new HMetrics();
	}
	
	protected VMetrics computeRequiredVMetrics()
	{
		return new VMetrics();
	}
	
	protected void onAllocateX(double width)
	{
	}
	
	protected void onAllocateY(double height)
	{
	}
	
	
	
	protected void clip(Graphics2D graphics)
	{
		graphics.clip( new Rectangle2D.Double( 0.0, 0.0, allocation.x, allocation.y ) );
	}

	protected void clipIfAllocationInsufficient(Graphics2D graphics)
	{
		if ( allocation.x < hmetrics.width  ||  allocation.y < vmetrics.height )
		{
			clip( graphics );
		}
	}
	
	
	protected void queueResize()
	{
		if ( !bResizeQueued  &&  bRealised )
		{
			if ( parent != null )
			{
				parent.childResizeRequest( this );
			}
			bResizeQueued = true;
		}
	}
	
	
	protected void queueRedraw(Point2 localPos, Vector2 localSize)
	{
		if ( bRealised  &&  parent != null )
		{
			parent.childRedrawRequest( this, localPos, localSize );
		}
	}
	
	protected void queueFullRedraw()
	{
		queueRedraw( new Point2(), allocation );
	}
	
	
	
	
	
	




	protected boolean handleButtonDown(PointerButtonEvent event)
	{
		return onButtonDown( event );
	}
	
	protected boolean handleButtonDown2(PointerButtonEvent event)
	{
		return onButtonDown2( event );
	}
	
	protected boolean handleButtonDown3(PointerButtonEvent event)
	{
		return onButtonDown3( event );
	}
	
	protected boolean handleButtonUp(PointerButtonEvent event)
	{
		return onButtonUp( event );
	}
	
	
	protected void handleMotion(PointerMotionEvent event)
	{
		pointersWithinBounds.add( event.pointer );
		onMotion( event );
	}
	
	protected void handleEnter(PointerMotionEvent event)
	{
		onEnter( event );
	}
	
	protected void handleLeave(PointerMotionEvent event)
	{
		pointersWithinBounds.remove( event.pointer );
		onLeave( event );
	}
	
	protected boolean handleScroll(PointerScrollEvent event)
	{
		return onScroll( event );
	}
	
	protected void handleRealise()
	{
		bRealised = true;
		onRealise();
		queueResize();
	}
	
	@SuppressWarnings("unchecked")
	protected void handleUnrealise()
	{
		LinkedList<PointerInterface> pointers = (LinkedList<PointerInterface>)pointersWithinBounds.clone();
		for (PointerInterface pointer: pointers)
		{
			handleLeave( new PointerMotionEvent( pointer, PointerMotionEvent.Action.LEAVE ) );
		}
		onUnrealise();
		bRealised = false;		
	}
	
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		draw( graphics );
	}
	
	
	
	protected void refreshScale(double scale, double rootScale)
	{
		onSetScale( scale, rootScale );
	}
	
	protected void setScale(double scale, double rootScale)
	{
		if ( scale != this.scale  ||  rootScale != this.rootScale )
		{
			this.scale = scale;
			this.rootScale = rootScale;
			refreshScale( scale, rootScale );
		}
	}
	
	
	protected HMetrics getRequiredHMetrics()
	{
		hmetrics = computeRequiredHMetrics().scaled( scale );
		return hmetrics;
	}
	
	protected VMetrics getRequiredVMetrics()
	{
		vmetrics = computeRequiredVMetrics().scaled( scale );
		return vmetrics;
	}
	
	
	protected void allocateX(double allocation)
	{
		this.allocation.x = allocation;
		onAllocateX( allocation );
	}
	
	protected void allocateY(double allocation)
	{
		this.allocation.y = allocation;
		onAllocateY( allocation );
	}
	
	
	
	//
	// Focus navigation methods
	//
	
	protected FocusPolicy focusPolicy()
	{
		return FocusPolicy.IGNORE;
	}
	
	
	protected boolean handleMotionKeyPress(KeyEvent keyEvent)
	{
		return false;
	}
	
	protected Widget[] horizontalNavigationList()
	{
		return null;
	}
	
	protected Widget[] verticalNavigationList()
	{
		return null;
	}
	
	protected static int navListIndexOf(Widget[] navList, Widget child)
	{
		for (int i = 0; i < navList.length; i++)
		{
			if ( child == navList[i] )
			{
				return i;
			}
		}
		
		return -1;
	}
	
	protected Point2 getCursorPosition()
	{
		return new Point2( allocation.mul( 0.5 ) );
	}
	
	
	
	public void startEditing()
	{
		makeCurrent();
	}
	
	public void startEditingAtStart()
	{
		makeCurrent();
	}
	
	public void startEditingAtEnd()
	{
		makeCurrent();
	}
	
	public void startEditingAtPosition(Point2 pos)
	{
		makeCurrent();
	}
	
	public void finishEditing()
	{	
	}
	
	
	public void makeCurrent()
	{
		grabFocus();
	}
	
	
	
	protected void cursorLeft(boolean bItemStep)
	{
		Widget left = getFocusLeafToLeft();
		if ( left != null )
		{
			finishEditing();
			if ( bItemStep )
			{
				left.makeCurrent();
			}
			else
			{
				left.startEditingAtEnd();
			}
		}
	}
	
	protected void cursorRight(boolean bItemStep)
	{
		Widget right = getFocusLeafToRight();
		if ( right != null )
		{
			finishEditing();
			if ( bItemStep )
			{
				right.makeCurrent();
			}
			else
			{
				right.startEditingAtStart();
			}
		}
	}
	
	
	protected void cursorToLeftChild()
	{
		Widget[] navList = horizontalNavigationList();
		if ( navList != null )
		{
			finishEditing();
			navList[0].makeCurrent();
		}
	}
	
	protected void cursorToRightChild()
	{
		Widget[] navList = horizontalNavigationList();
		if ( navList != null )
		{
			finishEditing();
			navList[navList.length-1].makeCurrent();
		}
	}
	
	
	protected void cursorToParent()
	{
		if ( parent != null )
		{
			finishEditing();
			parent.makeCurrent();
		}
	}
	
	
	
	protected void cursorUp()
	{
		Widget above = getFocusLeafAbove();
		if ( above != null )
		{
			Point2 cursorPosInAbove = getLocalPointRelativeTo( above, getCursorPosition() );
			finishEditing();
			above.startEditingAtPosition( cursorPosInAbove );
		}
	}
	
	protected void cursorDown()
	{
		Widget below = getFocusLeafBelow();
		if ( below != null )
		{
			Point2 cursorPosInAbove = getLocalPointRelativeTo( below, getCursorPosition() );
			finishEditing();
			below.startEditingAtPosition( cursorPosInAbove );
		}
	}
	
	
	
	protected Widget getLeftFocusLeaf()
	{
		if ( focusPolicy() == FocusPolicy.TAKE )
		{
			// Take the focus
			return this;
		}
		else
		{
			// Check the child nodes
			Widget[] navList = horizontalNavigationList();
			if ( navList != null )
			{
				for (int i = 0; i < navList.length; i++)
				{
					Widget l = navList[i].getLeftFocusLeaf();
					if ( l != null )
					{
						return l;
					}
				}
			}
			
			if ( focusPolicy() == FocusPolicy.CHILDRENFIRST )
			{
				return this;
			}
			else
			{
				return null;
			}
		}
	}
	
	protected Widget getRightFocusLeaf()
	{
		if ( focusPolicy() == FocusPolicy.TAKE )
		{
			// Take the focus
			return this;
		}
		else
		{
			// Check the child nodes
			Widget[] navList = horizontalNavigationList();
			if ( navList != null )
			{
				for (int i = navList.length - 1; i >= 0; i--)
				{
					Widget l = navList[i].getLeftFocusLeaf();
					if ( l != null )
					{
						return l;
					}
				}
			}
			
			if ( focusPolicy() == FocusPolicy.CHILDRENFIRST )
			{
				return this;
			}
			else
			{
				return null;
			}
		}
	}
	
	
	protected Widget getFocusLeafToLeft()
	{
		if ( parent != null )
		{
			return parent.getFocusLeafToLeftFromChild( this );
		}
		else
		{
			return null;
		}
	}
	
	protected Widget getFocusLeafToRight()
	{
		if ( parent != null )
		{
			return parent.getFocusLeafToRightFromChild( this );
		}
		else
		{
			return null;
		}
	}
	
	
	protected Widget getFocusLeafToLeftFromChild(Widget child)
	{
		Widget[] navList = horizontalNavigationList();
		if ( navList != null )
		{
			int index = navListIndexOf( navList, child );
			if ( index != -1 )
			{
				for (int i = index - 1; i >= 0; i--)
				{
					Widget l = navList[i].getRightFocusLeaf();
					if ( l != null )
					{
						return l;
					}
				}
			}
		}
		
		if ( parent != null )
		{
			return parent.getFocusLeafToLeftFromChild( this );
		}
		else
		{
			return null;
		}
	}
	
	protected Widget getFocusLeafToRightFromChild(Widget child)
	{
		Widget[] navList = horizontalNavigationList();
		if ( navList != null )
		{
			int index = navListIndexOf( navList, child );
			if ( index != -1 )
			{
				for (int i = index + 1; i < navList.length; i++)
				{
					Widget l = navList[i].getLeftFocusLeaf();
					if ( l != null )
					{
						return l;
					}
				}
			}
		}
		
		if ( parent != null )
		{
			return parent.getFocusLeafToRightFromChild( this );
		}
		else
		{
			return null;
		}
	}
	
	
	
	protected Widget getFocusLeafAbove()
	{
		return getFocusLeafAboveOrBelow( false );
	}
	
	protected Widget getFocusLeafBelow()
	{
		return getFocusLeafAboveOrBelow( true );
	}
	
	protected Widget getFocusLeafAboveOrBelow(boolean bBelow)
	{
		if ( parent != null )
		{
			Point2 localCursorPos = getCursorPosition();
			return parent.getFocusLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( parent, localCursorPos ) );
		}
		else
		{
			return null;
		}
	}
	
	protected Widget getFocusLeafAboveOrBelowFromChild(Widget child, boolean bBelow, Point2 localCursorPos)
	{
		Widget[] navList = verticalNavigationList();
		if ( navList != null )
		{
			int index = navListIndexOf( navList, child );
			if ( index != -1 )
			{
				Point2 cursorPosInRootSpace = getLocalPointRelativeToRoot( localCursorPos );
				if ( bBelow )
				{
					for (int i = index + 1; i < navList.length; i++)
					{
						Widget l = navList[i].getTopOrBottomFocusLeaf( false, cursorPosInRootSpace );
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
						Widget l = navList[i].getTopOrBottomFocusLeaf( true, cursorPosInRootSpace );
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
			return parent.getFocusLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( parent, localCursorPos ) );
		}
		else
		{
			return null;
		}
	}
	
	protected Widget getTopOrBottomFocusLeaf(boolean bBottom, Point2 cursorPosInRootSpace)
	{
		if ( focusPolicy() == FocusPolicy.TAKE )
		{
			return this;
		}
		else
		{
			Widget[] navList = verticalNavigationList();
			if ( navList != null )
			{
				if ( bBottom )
				{
					for (int i = navList.length - 1; i >= 0; i--)
					{
						Widget l = navList[i].getTopOrBottomFocusLeaf( bBottom, cursorPosInRootSpace );
						if ( l != null )
						{
							return l;
						}
					}
				}
				else
				{
					for (int i = 0; i < navList.length; i++)
					{
						Widget l = navList[i].getTopOrBottomFocusLeaf( bBottom, cursorPosInRootSpace );
						if ( l != null )
						{
							return l;
						}
					}
				}
				
				if ( focusPolicy() == FocusPolicy.CHILDRENFIRST )
				{
					return this;
				}
				else
				{
					return null;
				}
			}
			else
			{
				navList = horizontalNavigationList();
				if ( navList != null )
				{
					double closestDistance = 0.0;
					Widget closestNode = null;
					for (Widget item: navList)
					{
						AABox2 bounds = getLocalAABox();
						double lower = item.getLocalPointRelativeToRoot( bounds.getLower() ).x;
						double upper = item.getLocalPointRelativeToRoot( bounds.getUpper() ).x;
						if ( cursorPosInRootSpace.x >=  lower  &&  cursorPosInRootSpace.x <= upper )
						{
							Widget l = item.getTopOrBottomFocusLeaf( bBottom, cursorPosInRootSpace );
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
								distance = lower - cursorPosInRootSpace.x;
							}
							else // cursorPosInRootSpace.x > upper
							{
								distance = cursorPosInRootSpace.x - upper;
							}
							
							if ( closestNode == null  ||  distance < closestDistance )
							{
								closestDistance = distance;
								closestNode = item;
							}
						}
					}
					
					if ( closestNode != null )
					{
						Widget l = closestNode.getTopOrBottomFocusLeaf( bBottom, cursorPosInRootSpace );
						if ( l != null )
						{
							return l;
						}
					}
				}
				
				if ( focusPolicy() == FocusPolicy.CHILDRENFIRST )
				{
					return this;
				}
				else
				{
					return null;
				}
			}
		}
	}
		
	

}
