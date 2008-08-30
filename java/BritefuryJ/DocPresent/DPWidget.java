package BritefuryJ.DocPresent;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import java.awt.event.KeyEvent;
import java.awt.Graphics2D;
import java.awt.geom.*;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;





abstract public class DPWidget implements ContentInterface
{
	protected static double NON_TYPESET_CHILD_BASELINE_OFFSET = -5.0;
	
	
	
	//
	//
	// EXCEPTIONS
	//
	//
	
	public static class IsNotInSubtreeException extends RuntimeException
	{
		static final long serialVersionUID = 0L;
	}
	
	
	public static class DndDisabledException extends RuntimeException
	{
		static final long serialVersionUID = 0L;
	}

	
	public static class DndOperationAlreadyInList extends RuntimeException
	{
		static final long serialVersionUID = 0L;
	}

	
	public static class DndOperationNotInList extends RuntimeException
	{
		static final long serialVersionUID = 0L;
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


		protected DndListener dndListener;
		
		
		protected DndState()
		{
			sourceOps = new Vector<DndOperation>();
			destOps = new Vector<DndOperation>();
		}
	}

	

	//
	//
	// FIELDS
	//
	//
	
	protected WidgetStyleSheet styleSheet;
	protected DPContainer parent;
	protected DPPresentationArea presentationArea;
	protected boolean bRealised, bResizeQueued;
	protected double scale, rootScale;
	protected HMetrics minH, prefH;
	protected VMetrics minV, prefV;
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
	
	public DPWidget()
	{
		this( WidgetStyleSheet.defaultStyleSheet );
	}
	
	public DPWidget(WidgetStyleSheet styleSheet)
	{
		this.styleSheet = styleSheet;
		scale = rootScale = 1.0;
		minH = new HMetrics();
		prefH = new HMetrics();
		minV = new VMetrics();
		prefV = new VMetrics();
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
	
	
	
	public Xform2 getTransformRelativeToAncestor(DPWidget ancestor, Xform2 x)
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
				throw new IsNotInSubtreeException();
			}
			return x;
		}
	}
	
	public Xform2 getTransformRelativeToAncestor(DPWidget ancestor)
	{
		return getTransformRelativeToAncestor( ancestor, new Xform2() );
	}
	
	
	
	public Xform2 getTransformRelativeTo(DPWidget toWidget, Xform2 x)
	{
		Xform2 myXform = getTransformRelativeToRoot();
		Xform2 toWidgetXform = toWidget.getTransformRelativeToRoot();
		return myXform.concat( toWidgetXform.inverse() );
	}
	
	
	public Point2 getLocalPointRelativeToRoot(Point2 p)
	{
		return getLocalPointRelativeToAncestor( null, p );
	}
	
	public Point2 getLocalPointRelativeToAncestor(DPWidget ancestor, Point2 p)
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
				throw new IsNotInSubtreeException();
			}
			return p;
		}
	}
	
	public Point2 getLocalPointRelativeTo(DPWidget toWidget, Point2 p)
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
	
	public DPPresentationArea getPresentationArea()
	{
		return presentationArea;
	}
	
	
	public DPContainer getParent()
	{
		return parent;
	}
	
	protected void setParent(DPContainer parent, DPPresentationArea area)
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
	
	
	
	public boolean isInSubtreeRootedAt(DPContainer r)
	{
		DPWidget w = this;
		
		while ( w != null  &&  w != r )
		{
			w = w.getParent();
		}
		
		return w == r;
	}
	
	
	public void getWidgetPathToRoot(List<DPWidget> path)
	{
		// Root to top
		if ( parent != null )
		{
			parent.getWidgetPathToRoot( path );
		}
		
		path.add( this );
	}
	
	public void getWidgetPathToSubtreeRoot(DPContainer subtreeRoot, List<DPWidget> path)
	{
		// Root to top
		if ( subtreeRoot != this )
		{
			if ( parent != null )
			{
				parent.getWidgetPathToSubtreeRoot( subtreeRoot, path );
			}
			else
			{
				throw new IsNotInSubtreeException();
			}
		}
		
		path.add( this );
	}
	
	
	public static void getPathsToCommonSubtreeRoot(DPWidget w0, List<DPWidget> path0, DPWidget w1, List<DPWidget> path1)
	{
		w0.getWidgetPathToRoot( path0 );
		w1.getWidgetPathToRoot( path1 );
		
		int minLength = Math.min( path0.size(), path1.size() );
		
		for (int i = 0; i < minLength; i++)
		{
			DPWidget p0 = path0.get( path0.size() - 1 - i );
			DPWidget p1 = path1.get( path1.size() - 1 - i );
			
			if ( p0 == p1 )
			{
				break;
			}
			
			path0.remove( path0.size() - 1 );
			path1.remove( path1.size() - 1 );
		}
	}
	
	
	protected void setPresentationArea(DPPresentationArea area)
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
	
	
	protected void onSetPresentationArea(DPPresentationArea area)
	{
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
		DPWidget dndSrc = drag.srcWidget;
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
			DPWidget dndSrc = drag.srcWidget;

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
	
	
	protected Object dndDragTo(DndDrag drag, DPWidget dragDest)
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
	
	
	protected void clip(Graphics2D graphics)
	{
		graphics.clip( new Rectangle2D.Double( 0.0, 0.0, allocation.x, allocation.y ) );
	}

	protected void clipIfAllocationInsufficient(Graphics2D graphics)
	{
		if ( allocation.x < minH.width  ||  allocation.y < minV.height )
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
	
	
	
	
	//
	//
	// LAYOUT METHODS
	//
	//
	
	
	abstract protected HMetrics computeMinimumHMetrics();
	abstract protected HMetrics computePreferredHMetrics();
	
	abstract protected VMetrics computeMinimumVMetrics();
	abstract protected VMetrics computePreferredVMetrics();

	
	protected void allocateContentsX(double allocation)
	{
	}
	
	protected void allocateContentsY(double allocation)
	{
	}
	
	
	
	protected HMetrics refreshMinimumHMetrics()
	{
		minH = computeMinimumHMetrics().scaled( scale );
		return minH;
	}
	
	protected HMetrics refreshPreferredHMetrics()
	{
		prefH = computePreferredHMetrics().scaled( scale );
		return prefH;
	}
	
	protected VMetrics refreshMinimumVMetrics()
	{
		minV = computeMinimumVMetrics().scaled( scale );
		return minV;
	}
	
	protected VMetrics refreshPreferredVMetrics()
	{
		prefV = computePreferredVMetrics().scaled( scale );
		return prefV;
	}
	
	
	protected void allocateX(double width)
	{
		allocation.x = width;
		allocateContentsX( width );
	}
	
	protected void allocateY(double height)
	{
		allocation.y = height;
		allocateContentsY( height );
		bResizeQueued = false;
	}
	
	
	
	//
	// Focus navigation methods
	//
	
	protected boolean handleMotionKeyPress(KeyEvent keyEvent, int modifiers)
	{
		return false;
	}
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return null;
	}
	
	protected List<DPWidget> verticalNavigationList()
	{
		return null;
	}
	
	protected Point2 getCursorPosition()
	{
		return new Point2( allocation.mul( 0.5 ) );
	}
	
	
	
	
	//
	//
	// PARAGRAPH METHODS
	//
	//
	
	public LineBreakInterface getLineBreakInterface()
	{
		return null;
	}
	
	
	
	//
	//
	// CONTENT LEAF METHODS
	//
	//
	
	public boolean isContentLeaf()
	{
		return false;
	}


	
	protected DPContentLeaf getLeftContentLeaf()
	{
		return null;
	}
	
	protected DPContentLeaf getRightContentLeaf()
	{
		return null;
	}
	
	protected DPContentLeaf getTopOrBottomContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace)
	{
		return null;
	}



	public DPContentLeaf getLeafAtContentPosition(int position)
	{
		return null;
	}
	
	
	
	
	//
	//
	// CONTENT METHODS
	//
	//
	
	public int getContentOffsetInSubtree(DPContainer subtreeRoot)
	{
		if ( this == subtreeRoot )
		{
			return 0;
		}
		else
		{
			return parent.getChildContentOffsetInSubtree( this, subtreeRoot );
		}
	}
	
	
	
	//
	//
	// MARKER METHODS
	//
	//
	
	public Marker marker(int position, Marker.Bias bias)
	{
		return markerAtStart();
	}
	
	public Marker markerAtStart()
	{
		DPContentLeaf leaf = null;
		
		if ( parent != null )
		{
			leaf = parent.getContentLeafToRightFromChild( this );
		}
		
		if ( leaf != null )
		{
			return leaf.markerAtStart();
		}
		else
		{
			leaf = parent.getContentLeafToLeftFromChild( this );
			if ( leaf != null )
			{
				return leaf.markerAtEnd();
			}
			else
			{
				throw new Marker.InvalidMarkerPosition();
			}
		}
	}
	
	public Marker markerAtEnd()
	{
		return markerAtStart();
	}
	
	
	public void moveMarker(Marker m, int position, Marker.Bias bias)
	{
		moveMarkerToStart( m );
	}
	
	public void moveMarkerToStart(Marker m)
	{
		DPContentLeaf leaf = null;
		
		if ( parent != null )
		{
			leaf = parent.getContentLeafToRightFromChild( this );
		}
		
		if ( leaf != null )
		{
			leaf.moveMarkerToStart( m );
		}
		else
		{
			leaf = parent.getContentLeafToLeftFromChild( this );
			if ( leaf != null )
			{
				leaf.moveMarkerToEnd( m );
			}
			else
			{
				throw new Marker.InvalidMarkerPosition();
			}
		}
	}
	
	public void moveMarkerToEnd(Marker m)
	{
		moveMarkerToStart( m );
	}
	
	
	
	public boolean isMarkerAtStart(Marker m)
	{
		return false;
	}
	
	public boolean isMarkerAtEnd(Marker m)
	{
		return false;
	}
}
