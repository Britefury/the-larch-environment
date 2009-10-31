//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.DocPresent.Input.DndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.Layout.ElementAlignment;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StructuralRepresentation.StructuralRepresentation;
import BritefuryJ.DocPresent.StructuralRepresentation.StructuralValue;
import BritefuryJ.DocPresent.StructuralRepresentation.StructuralValueObject;
import BritefuryJ.DocPresent.StructuralRepresentation.StructuralValueSequence;
import BritefuryJ.DocPresent.StructuralRepresentation.StructuralValueStream;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;
import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;
import BritefuryJ.Utils.HashUtils;





abstract public class DPWidget extends PointerInputElement
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
	// Parent packing
	//
	//
	
	protected static class ParentPacking
	{
	};

	
	
	//
	//
	// Padding
	//
	//
	
	private static class PaddingKey
	{
		private double xPad, yPad;
		private int hash;
		
		
		public PaddingKey(double xPad, double yPad)
		{
			this.xPad = xPad;
			this.yPad = yPad;
			hash = HashUtils.doubleHash( new Double( xPad ).hashCode(), new Double( yPad ).hashCode() );
		}
		
		
		public int hashCode()
		{
			return hash;
		}
		
		
		public boolean equals(Object x)
		{
			if ( this == x )
			{
				return true;
			}
			
			
			if ( x instanceof PaddingKey )
			{
				PaddingKey k = (PaddingKey)x;
				
				return xPad == k.xPad  &&  yPad == k.yPad;
			}
			else
			{
				return false;
			}
		}
	}
	

	private static HashMap<PaddingKey, EmptyBorder> paddingBorders = new HashMap<PaddingKey, EmptyBorder>();

	
	
	
	//
	//
	// FIELDS
	//
	//
	
	protected static int FLAG_REALISED = 0x1;
	protected static int FLAG_RESIZE_QUEUED = 0x2;
	protected static int FLAG_SIZE_UP_TO_DATE = 0x4;

	protected static int _ALIGN_SHIFT = 3;
	protected static int _ALIGN_MASK = ElementAlignment._ELEMENTALIGN_MASK  <<  _ALIGN_SHIFT;
	protected static int _HALIGN_MASK = ElementAlignment._HALIGN_MASK  <<  _ALIGN_SHIFT;
	protected static int _VALIGN_MASK = ElementAlignment._VALIGN_MASK  <<  _ALIGN_SHIFT;
	protected static int FLAGS_ELEMENT_END = ElementAlignment._ELEMENTALIGN_END  <<  _ALIGN_SHIFT;
	
	
	protected int flags;
	
	protected WidgetStyleSheet styleSheet;
	protected DPContainer parent;
	protected DPPresentationArea presentationArea;
	protected LReqBox layoutReqBox;
	protected LAllocBox layoutAllocBox;
	protected PackingParams parentPacking;
	
	protected DndHandler dndHandler;
	
	protected ElementLinearRepresentationListener linearRepresentationListener;
	protected ElementKeyboardListener keyboardListener;
	
	protected ElementContext context;

	protected DPWidget metaElement;
	protected String debugName;
	
	protected StructuralRepresentation structuralRepresentation;
	
	
	
	
	//
	//
	// FIELDS AS DICTIONARY VALUES
	//
	//
	
	// These fields would be null/non-existant for the vast majority of elements, so store them in a global dictionary to
	// save space
	
	private static WeakHashMap<DPWidget, ArrayList<Runnable>> waitingImmediateEventsByWidget = new WeakHashMap<DPWidget, ArrayList<Runnable>>();
	
	
	
	

	//
	//
	// METHODS
	//
	//
	
	
	//
	//
	// Constructors
	//
	//
	
	public DPWidget(ElementContext context)
	{
		this( context, WidgetStyleSheet.defaultStyleSheet );
	}
	
	public DPWidget(ElementContext context, WidgetStyleSheet styleSheet)
	{
		flags = 0;
		this.styleSheet = styleSheet;
		layoutReqBox = new LReqBox();
		layoutAllocBox = new LAllocBox( this );
		parentPacking = null;
		this.context = context;
	}
	
	
	
	
	//
	// Owner
	//
	
	public ElementContext getContext()
	{
		return context;
	}
	
	public void setContext(ElementContext context)
	{
		this.context = context;
	}

	
	
	
	//
	// Alignment methods
	//
	
	public DPWidget align(HAlignment hAlign, VAlignment vAlign)
	{
		setAlignmentFlags( ElementAlignment.flagValue( hAlign, vAlign ) );
		return this;
	}

	public DPWidget alignH(HAlignment hAlign)
	{
		setHAlignmentFlags( ElementAlignment.flagValue( hAlign ) );
		return this;
	}
	
	public DPWidget alignV(VAlignment vAlign)
	{
		setVAlignmentFlags( ElementAlignment.flagValue( vAlign ) );
		return this;
	}
	

	public DPWidget alignHLeft()
	{
		return alignH( HAlignment.LEFT );
	}

	public DPWidget alignHCentre()
	{
		return alignH( HAlignment.CENTRE );
	}

	public DPWidget alignHRight()
	{
		return alignH( HAlignment.RIGHT );
	}

	public DPWidget alignHExpand()
	{
		return alignH( HAlignment.EXPAND );
	}
	
	
	public DPWidget alignVBaselines()
	{
		return alignV( VAlignment.BASELINES );
	}

	public DPWidget alignVBaselinesExpand()
	{
		return alignV( VAlignment.BASELINES_EXPAND );
	}

	public DPWidget alignVTop()
	{
		return alignV( VAlignment.TOP );
	}

	public DPWidget alignVCentre()
	{
		return alignV( VAlignment.CENTRE );
	}

	public DPWidget alignVBottom()
	{
		return alignV( VAlignment.BOTTOM );
	}

	public DPWidget alignVExpand()
	{
		return alignV( VAlignment.EXPAND );
	}


	
	protected void setAlignmentFlags(int alignmentFlags)
	{
		flags = ( flags & ~_ALIGN_MASK )   |   ( alignmentFlags << _ALIGN_SHIFT );
	}
	
	protected void setHAlignmentFlags(int alignmentFlags)
	{
		flags = ( flags & ~_HALIGN_MASK )   |   ( alignmentFlags << _ALIGN_SHIFT );
	}
	
	protected void setVAlignmentFlags(int alignmentFlags)
	{
		flags = ( flags & ~_VALIGN_MASK )   |   ( alignmentFlags << _ALIGN_SHIFT );
	}
	
	
	protected int getAlignmentFlags()
	{
		return ( flags & _ALIGN_MASK )  >>  _ALIGN_SHIFT;
	}
	
	
	
	
	//
	// Padding methods
	//
	
	public DPWidget pad(double xPad, double yPad)
	{
		PaddingKey key = new PaddingKey( xPad, yPad );
		EmptyBorder border = paddingBorders.get( key );
		
		if ( border == null )
		{
			border = new EmptyBorder( xPad, xPad, yPad, yPad );
			paddingBorders.put( key, border );
		}
		
		DPBorder padElement = new DPBorder( context, border );
		padElement.setChild( this );
		padElement.setContext( context );
		return padElement;
	}
	
	public DPWidget padX(double xPad)
	{
		return pad( xPad, 0.0 );
	}
	
	public DPWidget padY(double yPad)
	{
		return pad( 0.0, yPad );
	}
	

	
	
	
	
	//
	// Parent packing methods
	//
	
	public PackingParams getParentPacking()
	{
		return parentPacking;
	}
	
	public void setParentPacking(PackingParams parentPacking)
	{
		this.parentPacking = parentPacking;
	}
	

	//
	// Geometry methods
	//
	
	public Point2 getPositionInParentSpace()
	{
		return layoutAllocBox.getPositionInParentSpace();
	}
	
	public double getPositionInParentSpaceX()
	{
		return layoutAllocBox.getPositionInParentSpaceX();
	}
	
	public double getPositionInParentSpaceY()
	{
		return layoutAllocBox.getPositionInParentSpaceY();
	}
	
	public double getAllocationX()
	{
		return layoutAllocBox.getAllocationX();
	}
	
	public double getAllocationY()
	{
		return layoutAllocBox.getAllocationY();
	}
	
	public LAllocV getAllocV()
	{
		return layoutAllocBox.getAllocV();
	}
	
	public Vector2 getAllocation()
	{
		return layoutAllocBox.getAllocation();
	}
	
	public double getAllocationInParentSpaceX()
	{
		return layoutAllocBox.getAllocationX()  *  getScale();
	}
	
	public double getAllocationInParentSpaceY()
	{
		return layoutAllocBox.getAllocationY()  *  getScale();
	}
	
	public Vector2 getAllocationInParentSpace()
	{
		return layoutAllocBox.getAllocation().mul( getScale() );
	}
	
	
	public AABox2 getLocalAABox()
	{
		return new AABox2( new Point2(), new Point2( getAllocation() ) );
	}
	
	public AABox2 getAABoxInParentSpace()
	{
		return new AABox2( getPositionInParentSpace(), getAllocationInParentSpace() );
	}

	
	protected double getScale()
	{
		return parent != null  ?  parent.getInternalChildScale( this )  :  1.0;
	}
	
	public Xform2 getLocalToParentXform()
	{
		return new Xform2( getScale(), getPositionInParentSpace().toVector2() );
	}
	
	public Xform2 getParentToLocalXform()
	{
		return Xform2.inverseOf( getScale(), getPositionInParentSpace().toVector2() );
	}
	
	
	
	public Xform2 getLocalToRootXform(Xform2 x)
	{
		return getLocalToAncestorXform( null, x );
	}
	
	public Xform2 getLocalToRootXform()
	{
		return getLocalToRootXform( new Xform2() );
	}
	
	
	
	public Xform2 getRootToLocalXform(Xform2 x)
	{
		return getAncestorToLocalXform( null, x );
	}
	
	public Xform2 getRootToLocalXform()
	{
		return getRootToLocalXform( new Xform2() );
	}
	
	
	
	public Xform2 getLocalToAncestorXform(DPWidget ancestor, Xform2 x)
	{
		DPWidget node = this;
		
		while ( node != ancestor )
		{
			DPWidget parentNode = node.parent;
			if ( parentNode != null )
			{
				x = x.concat( node.getLocalToParentXform() );
				node = parentNode;
			}
			else
			{
				if ( ancestor != null )
				{
					// Did not reach ancestor
					throw new IsNotInSubtreeException();
				}
				else
				{
					return x;
				}
			}
		}
		
		return x;
	}
	
	public Xform2 getLocalToAncestorXform(DPWidget ancestor)
	{
		if ( ancestor == parent )
		{
			// Early out
			return getLocalToParentXform();
		}
		else
		{
			return getLocalToAncestorXform( ancestor, new Xform2() );
		}
	}
	
	
	
	public Xform2 getAncestorToLocalXform(DPWidget ancestor, Xform2 x)
	{
		DPWidget node = this;
		
		while ( node != ancestor )
		{
			DPWidget parentNode = node.parent;
			if ( parentNode != null )
			{
				x = node.getParentToLocalXform().concat( x );
				node = parentNode;
			}
			else
			{
				if ( ancestor != null )
				{
					// Did not reach ancestor
					throw new IsNotInSubtreeException();
				}
				else
				{
					return x;
				}
			}
		}
		
		return x;
	}
	
	public Xform2 getAncestorToLocalXform(DPWidget ancestor)
	{
		if ( ancestor == parent )
		{
			// Early out
			return getParentToLocalXform();
		}
		else
		{
			return getAncestorToLocalXform( ancestor, new Xform2() );
		}
	}
	
	
	
	public Xform2 getTransformRelativeTo(DPWidget toWidget, Xform2 x)
	{
		Xform2 myXform = getLocalToRootXform();
		Xform2 toWidgetXform = toWidget.getLocalToRootXform();
		return myXform.concat( toWidgetXform.inverse() );
	}
	
	
	public Point2 getLocalPointRelativeToRoot(Point2 p)
	{
		return getLocalPointRelativeToAncestor( null, p );
	}
	
	public Point2 getLocalPointRelativeToAncestor(DPWidget ancestor, Point2 p)
	{
		DPWidget node = this;
		
		while ( node != ancestor )
		{
			DPWidget parentNode = node.parent;
			if ( parentNode != null )
			{
				p = node.getLocalToParentXform().transform( p );
				node = parentNode;
			}
			else
			{
				if ( ancestor != null )
				{
					// Did not reach ancestor
					throw new IsNotInSubtreeException();
				}
				else
				{
					return p;
				}
			}
		}
		
		return p;
	}
	
	public Point2 getLocalPointRelativeTo(DPWidget toWidget, Point2 p)
	{
		Point2 pointInRoot = getLocalPointRelativeToRoot( p );
		Xform2 toWidgetXform = toWidget.getLocalToRootXform();
		return toWidgetXform.inverse().transform( pointInRoot );
	}
	
	
	protected AffineTransform pushGraphicsTransform(Graphics2D graphics)
	{
		AffineTransform current = graphics.getTransform();
		getLocalToRootXform().apply( graphics );
		return current;
	}
	
	protected void popGraphicsTransform(Graphics2D graphics, AffineTransform x)
	{
		graphics.setTransform( x );
	}
	
	
	
	

	//
	//
	// Flag methods
	//
	//
	
	protected boolean isResizeQueued()
	{
		return testFlag( FLAG_RESIZE_QUEUED );
	}
	
	protected boolean isSizeUpToDate()
	{
		return testFlag( FLAG_SIZE_UP_TO_DATE );
	}
	
	
	protected void clearFlag(int flag)
	{
		flags &= ~flag;
	}
	
	protected void setFlag(int flag)
	{
		flags |= flag;
	}
	
	protected void setFlagValue(int flag, boolean value)
	{
		if ( value )
		{
			flags |= flag;
		}
		else
		{
			flags &= ~flag;
		}
	}
	
	protected boolean testFlag(int flag)
	{
		return ( flags & flag )  !=  0;
	}
	
	
	protected void clearFlagRealised()
	{
		clearFlag( FLAG_REALISED );
	}
	
	protected void setFlagRealised()
	{
		setFlag( FLAG_REALISED );
	}
	
	
	protected void clearFlagResizeQueued()
	{
		clearFlag( FLAG_RESIZE_QUEUED );
	}
	
	protected void setFlagResizeQueued()
	{
		setFlag( FLAG_RESIZE_QUEUED );
	}
	
	
	protected void clearFlagSizeUpToDate()
	{
		clearFlag( FLAG_SIZE_UP_TO_DATE );
	}
	
	protected void setFlagSizeUpToDate()
	{
		setFlag( FLAG_SIZE_UP_TO_DATE );
	}
	
	
	
	
	//
	//
	// Pointers within bounds
	//
	//
	
	protected ArrayList<PointerInterface> getPointersWithinBounds()
	{
		if ( presentationArea != null )
		{
			return presentationArea.getInputTable().getPointersWithinBoundsOfElement( this );
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	//
	//
	// Tree structure methods
	//
	//
	
	public boolean isRealised()
	{
		return testFlag( FLAG_REALISED );
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
		if ( area != presentationArea )
		{
			setPresentationArea( area );
		}
		onParentChanged();
	}
	
	
	protected void unparent()
	{
		if ( parent != null )
		{
			parent.replaceChildWithEmpty( this );
		}
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
	
	
	public ArrayList<DPWidget> getElementPathFromRoot()
	{
		ArrayList<DPWidget> path = new ArrayList<DPWidget>();
		
		DPWidget widget = this;
		while ( widget != null )
		{
			path.add( 0, widget );
			widget = widget.getParent();
		}
		
		return path;
	}
	
	public ArrayList<DPWidget> getElementPathFromSubtreeRoot(DPContainer subtreeRoot)
	{
		ArrayList<DPWidget> path = new ArrayList<DPWidget>();
		
		DPWidget widget = this;
		while ( widget != null )
		{
			path.add( 0, widget );
			if ( widget == subtreeRoot )
			{
				return path;
			}
			widget = widget.getParent();
		}

		return null;
	}
	
	

	public DPContentLeaf getFirstLeafInSubtree(WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		return null;
	}

	public DPContentLeaf getFirstLeafInSubtree()
	{
		return getFirstLeafInSubtree( null, null );
	}

	public DPContentLeaf getFirstEditableEntryLeafInSubtree()
	{
		return getFirstLeafInSubtree( null, new DPContentLeafEditableEntry.EditableEntryLeafElementFilter() );
	}

	public DPContentLeaf getLastLeafInSubtree(WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		return null;
	}

	public DPContentLeaf getLastLeafInSubtree()
	{
		return getLastLeafInSubtree( null, null );
	}

	public DPContentLeaf getLastEditableEntryLeafInSubtree()
	{
		return getLastLeafInSubtree( null, new DPContentLeafEditableEntry.EditableEntryLeafElementFilter() );
	}

	
	
	public static void getPathsFromCommonSubtreeRoot(DPWidget w0, List<DPWidget> path0, DPWidget w1, List<DPWidget> path1)
	{
		if ( w0 == w1 )
		{
			path0.add( w0 );
			path1.add( w1 );
		}
		else
		{
			ArrayList<DPWidget> p0 = w0.getElementPathFromRoot();
			ArrayList<DPWidget> p1 = w1.getElementPathFromRoot();
			
			int minLength = Math.min( p0.size(), p1.size() );
			
			if ( p0.get( 0 ) != p1.get( 0 ) )
			{
				throw new RuntimeException( "Bad path" );
			}
			
			int numCommonWidgets = 0;
			
			for (int i = 0; i < minLength; i++)
			{
				numCommonWidgets = i;
				
				if ( p0.get( i ) != p1.get( i ) )
				{
					break;
				}
			}
			
			path0.addAll( p0.subList( numCommonWidgets - 1, p0.size() ) );
			path1.addAll( p1.subList( numCommonWidgets - 1, p1.size() ) );
		}
	}
	
	
	protected void setPresentationArea(DPPresentationArea area)
	{
		if ( area != presentationArea )
		{
			presentationArea = area;
			if ( presentationArea != null )
			{
				ArrayList<Runnable> waitingImmediateEvents = waitingImmediateEventsByWidget.get( this );
				if ( waitingImmediateEvents != null )
				{
					for (Runnable event: waitingImmediateEvents)
					{
						presentationArea.queueImmediateEvent( event );
					}
					waitingImmediateEventsByWidget.remove( this );
					waitingImmediateEvents = null;
				}
			}
		}
	}

	
	
	

	
	//
	//
	// SELECTION METHODS
	//
	//
	
	protected void drawSubtreeSelection(Graphics2D graphics, Marker startMarker, List<DPWidget> startPath, Marker endMarker, List<DPWidget> endPath)
	{
	}

	
	
	
	//
	//
	// DRAG AND DROP METHODS
	//
	//
	
	public void enableDnd(DndHandler handler)
	{
		dndHandler = handler;
	}
	
	public void disableDnd()
	{
		dndHandler = null;
	}
	
	public boolean isDndEnabled()
	{
		return dndHandler != null;
	}
	
	
	
	
	
	
	//
	// Immediate event queue methods
	//
	
	public void queueImmediateEvent(Runnable event)
	{
		if ( presentationArea != null )
		{
			presentationArea.queueImmediateEvent( event );
		}
		else
		{
			ArrayList<Runnable> waitingImmediateEvents = waitingImmediateEventsByWidget.get( this );
			if ( waitingImmediateEvents == null )
			{
				waitingImmediateEvents = new ArrayList<Runnable>();
				waitingImmediateEventsByWidget.put( this, waitingImmediateEvents );
			}
			if ( !waitingImmediateEvents.contains( event ) )
			{
				waitingImmediateEvents.add( event );
			}
		}
			
	}

	public void dequeueImmediateEvent(Runnable event)
	{
		if ( presentationArea != null )
		{
			presentationArea.dequeueImmediateEvent( event );
		}
		else
		{
			ArrayList<Runnable> waitingImmediateEvents = waitingImmediateEventsByWidget.get( this );
			if ( waitingImmediateEvents != null )
			{
				waitingImmediateEvents.remove( event );
				if ( waitingImmediateEvents.isEmpty() )
				{
					waitingImmediateEvents = null;
					waitingImmediateEventsByWidget.remove( this );
				}
			}
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

	protected void onDrag(PointerMotionEvent event)
	{
	}

	protected void onEnter(PointerMotionEvent event)
	{
	}

	protected void onLeave(PointerMotionEvent event)
	{
	}
	
	protected void onLeaveIntoChild(PointerMotionEvent event, PointerInputElement child)
	{
	}
	
	protected void onEnterFromChild(PointerMotionEvent event, PointerInputElement child)
	{
	}
	
	
	protected boolean onScroll(PointerScrollEvent event)
	{
		return false;
	}
	
	
	protected void onRealise()
	{
	}
	
	protected void onUnrealise(DPWidget unrealiseRoot)
	{
	}
	
	protected void onParentChanged()
	{
	}
	
	
	protected void drawBackground(Graphics2D graphics)
	{
	}
	
	protected void draw(Graphics2D graphics)
	{
	}
	
	
	protected void onSetScale(double scale)
	{
	}
	
	
	protected void clip(Graphics2D graphics)
	{
		graphics.clip( new Rectangle2D.Double( 0.0, 0.0, getAllocationX(), getAllocationY() ) );
	}

	protected void clipIfAllocationInsufficient(Graphics2D graphics)
	{
		if ( getAllocationX()  <  layoutReqBox.getMinWidth()  ||  getAllocationY()  <  layoutReqBox.getReqHeight() )
		{
			clip( graphics );
		}
	}
	
	
	protected void handleQueueResize()
	{
		if ( !isResizeQueued() )
		{
			if ( parent != null )
			{
				parent.queueResize();
			}
			setFlagResizeQueued();
		}
	}
	
	
	protected void queueResize()
	{
		handleQueueResize();
		clearFlagSizeUpToDate();
	}
	
	
	protected void queueRedraw(Point2 localPos, Vector2 localSize)
	{
		if ( isRealised()  &&  parent != null )
		{
			parent.childRedrawRequest( this, localPos, localSize );
		}
	}
	
	protected void queueFullRedraw()
	{
		queueRedraw( new Point2(), getAllocation() );
	}
	
	
	
	
	
	




	protected void handleRealise()
	{
		setFlagRealised();
		onRealise();
	}
	
	protected void handleUnrealise(DPWidget unrealiseRoot)
	{
		if ( presentationArea != null )
		{
			presentationArea.elementUnrealised( this );
		}
		onUnrealise( unrealiseRoot );
		clearFlagRealised();
	}
	
	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
		/*Stroke s = new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL );
		graphics.setStroke( s );
		graphics.setPaint( new Color( 0.0f, 0.0f, 0.0f, 0.1f ) );
		graphics.draw( new Rectangle2D.Double( 0.0, 0.0, getAllocationX(), getAllocationY() ) );*/
		drawBackground( graphics );
	}
	
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		draw( graphics );
	}
	

	
	
	
	//
	//
	// Element tree traversal methods
	//
	//
	
	protected DPWidget getFirstChildAtLocalPoint(Point2 localPos)
	{
		return null;
	}
	
	protected DPWidget getLastChildAtLocalPoint(Point2 localPos)
	{
		return null;
	}
	

	
	
	//
	//
	// POINTER INPUT ELEMENT METHODS
	//
	//

	protected boolean handlePointerButtonDown(PointerButtonEvent event)
	{
		return onButtonDown( event );
	}
	
	protected boolean handlePointerButtonDown2(PointerButtonEvent event)
	{
		return onButtonDown2( event );
	}
	
	protected boolean handlePointerButtonDown3(PointerButtonEvent event)
	{
		return onButtonDown3( event );
	}
	
	protected boolean handlePointerButtonUp(PointerButtonEvent event)
	{
		return onButtonUp( event );
	}
	
	protected void handlePointerMotion(PointerMotionEvent event)
	{
		onMotion( event );
	}
	
	protected void handlePointerDrag(PointerMotionEvent event)
	{
		onDrag( event );
	}
	
	protected void handlePointerEnter(PointerMotionEvent event)
	{
		onEnter( event );
	}
	
	protected void handlePointerLeave(PointerMotionEvent event)
	{
		onLeave( event );
	}
	

	protected void handlePointerEnterFromChild(PointerMotionEvent event, PointerInputElement childElement)
	{
		onEnterFromChild( event, childElement );
	}
	
	protected void handlePointerLeaveIntoChild(PointerMotionEvent event, PointerInputElement childElement)
	{
		onLeaveIntoChild( event, childElement );
	}
	
	protected boolean handlePointerScroll(PointerScrollEvent event)
	{
		return onScroll( event );
	}
	
	
	protected PointerInputElement getFirstPointerChildAtLocalPoint(Point2 localPos)
	{
		return getFirstChildAtLocalPoint( localPos );
	}
	
	protected PointerInputElement getLastPointerChildAtLocalPoint(Point2 localPos)
	{
		return getLastChildAtLocalPoint( localPos );
	}
	
	protected PointerEvent transformParentToLocalEvent(PointerEvent event)
	{
		return event.transformed( getParentToLocalXform() );
	}
	
	protected PointerInterface transformParentToLocalPointer(PointerInterface pointer)
	{
		return pointer.transformed( getParentToLocalXform() );
	}
	
	public Point2 transformParentToLocalPoint(Point2 parentPos)
	{
		return getParentToLocalXform().transform( parentPos );
	}
	
	protected boolean isPointerInputElementRealised()
	{
		return isRealised();
	}
	
	public boolean containsParentSpacePoint(Point2 parentPos)
	{
		return getAABoxInParentSpace().containsPoint( parentPos );
	}

	public boolean containsLocalSpacePoint(Point2 localPos)
	{
		return localPos.x >= 0.0  &&  localPos.y >= 0.0  &&  localPos.x < getAllocationX()  &&  localPos.y < getAllocationY();
	}
	
	protected PointerInputElement getDndElement(Point2 localPos, Point2 targetPos[])				// targetPos is an output parameter
	{
		if ( dndHandler != null )
		{
			if ( targetPos != null )
			{
				targetPos[0] = localPos;
			}
			return this;
		}
		else
		{
			return null;
		}
	}
	
	public DndHandler getDndHandler()
	{
		return dndHandler;
	}
	
	
	
	
	//
	//
	// LAYOUT METHODS
	//
	//
	
	
	abstract protected void updateRequisitionX();
	abstract protected void updateRequisitionY();

	
	protected void updateAllocationX()
	{
	}
	
	protected void updateAllocationY()
	{
	}
	
	
	
	protected LReqBox refreshRequisitionX()
	{
		if ( !isSizeUpToDate() )
		{
			updateRequisitionX();
		}
		return layoutReqBox;
	}
	
	protected LReqBox refreshRequisitionY()
	{
		if ( !isSizeUpToDate() )
		{
			updateRequisitionY();
		}
		return layoutReqBox;
	}
	
	
	protected void refreshAllocationX(double prevWidth)
	{
		if ( !isSizeUpToDate()  ||  layoutAllocBox.getAllocationX() != prevWidth )
		{
			updateAllocationX();
			clearFlagSizeUpToDate();
		}
	}
	
	protected void refreshAllocationY(LAllocV prevHeight)
	{
		if ( !isSizeUpToDate()  ||  !layoutAllocBox.getAllocV().equals( prevHeight ) )
		{
			updateAllocationY();
		}
		onSizeRefreshed();
	}
	
	
	protected void onAllocationXRefreshed()
	{
		clearFlagSizeUpToDate();
	}
	
	protected void onAllocationYRefreshed()
	{
		onSizeRefreshed();
	}
	

	protected void onSizeRefreshed()
	{
		clearFlagResizeQueued();
		setFlagSizeUpToDate();
		if ( parent != null )
		{
			parent.onChildSizeRefreshed();
		}
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
	
	protected Point2 getMarkerPosition(Marker marker)
	{
		return new Point2( getAllocationX() * 0.5, getAllocationY() * 0.5 );
	}
	
	
	
	
	//
	//
	// CONTENT LEAF METHODS
	//
	//
	
	public DPContentLeaf getLeftContentLeaf()
	{
		return null;
	}
	
	public DPContentLeaf getRightContentLeaf()
	{
		return null;
	}
	
	public DPContentLeaf getContentLeafToLeft()
	{
		if ( parent != null )
		{
			return parent.getContentLeafToLeftFromChild( this );
		}
		else
		{
			return null;
		}
	}
	
	public DPContentLeaf getContentLeafToRight()
	{
		if ( parent != null )
		{
			return parent.getContentLeafToRightFromChild( this );
		}
		else
		{
			return null;
		}
	}
	
	protected DPContentLeaf getTopOrBottomContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace, boolean bSkipWhitespace)
	{
		return null;
	}



	protected abstract DPWidget getLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter);
	
	
	
	
	
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
				throw new Marker.InvalidMarkerPosition( "Cannot find leaf to place marker" );
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
				throw new Marker.InvalidMarkerPosition( "Cannot find leaf to place marker" );
			}
		}
	}
	
	public void moveMarkerToEnd(Marker m)
	{
		moveMarkerToStart( m );
	}
	
	
	
	//
	//
	// LISTENER METHODS
	//
	//
	
	
	public ElementLinearRepresentationListener getLinearRepresentationListener()
	{
		return linearRepresentationListener;
	}
	
	public void setLinearRepresentationListener(ElementLinearRepresentationListener listener)
	{
		linearRepresentationListener = listener;
	}
	

	public ElementKeyboardListener getKeyboardListener()
	{
		return keyboardListener;
	}
	
	public void setKeyboardListener(ElementKeyboardListener listener)
	{
		keyboardListener = listener;
	}
	
	

	
	
	
	
	//
	//
	// TEXT REPRESENTATION METHODS
	//
	//
	
	
	public DPContentLeaf getLeafAtTextRepresentationPosition(int position)
	{
		return null;
	}
	
	
	public int getTextRepresentationOffsetInSubtree(DPContainer subtreeRoot)
	{
		if ( this == subtreeRoot )
		{
			return 0;
		}
		else
		{
			return parent.getChildTextRepresentationOffsetInSubtree( this, subtreeRoot );
		}
	}
	
	
	public String getTextRepresentationFromStartToMarker(Marker marker)
	{
		StringBuilder builder = new StringBuilder();
		marker.getElement().getTextRepresentationFromStartOfRootToMarker( builder, marker, this );
		return builder.toString();
	}
	
	public String getTextRepresentationFromMarkerToEnd(Marker marker)
	{
		StringBuilder builder = new StringBuilder();
		marker.getElement().getTextRepresentationFromMarkerToEndOfRoot( builder, marker, this );
		return builder.toString();
	}

	protected abstract void getTextRepresentationFromStartToPath(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex);
	protected abstract void getTextRepresentationFromPathToEnd(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex);



	
	protected void textRepresentationChanged(LinearRepresentationEvent event)
	{
		onTextRepresentationModified();
		onTextRepresentationModifiedEvent( event );
		onLinearRepresentationModifiedEvent( event );
	}
	
	protected void onTextRepresentationModified()
	{
		if ( parent != null )
		{
			parent.onTextRepresentationModified();
		}
	}
	
	protected boolean onTextRepresentationModifiedEvent(LinearRepresentationEvent event)
	{
		if ( linearRepresentationListener != null )
		{
			if ( linearRepresentationListener.textRepresentationModified( this, event ) )
			{
				return true;
			}
		}
		
		if ( parent != null )
		{
			return parent.onTextRepresentationModifiedEvent( event );
		}
		
		return false;
	}
	
	public DPWidget getElementAtTextRepresentationStart()
	{
		return this;
	}
	
		
	public abstract String getTextRepresentation();
	public abstract int getTextRepresentationLength();
	
	
	
	
	
	//
	//
	// LINEAR REPRESENTATION METHODS
	//
	//
	
	
	public ItemStream getLinearRepresentationFromStartToMarker(Marker marker)
	{
		ItemStreamBuilder builder = new ItemStreamBuilder();
		marker.getElement().getLinearRepresentationFromStartOfRootToMarker( builder, marker, this );
		return builder.stream();
	}
	
	public ItemStream getLinearRepresentationFromMarkerToEnd(Marker marker)
	{
		ItemStreamBuilder builder = new ItemStreamBuilder();
		marker.getElement().getLinearRepresentationFromMarkerToEndOfRoot( builder, marker, this );
		return builder.stream();
	}

	protected abstract void getLinearRepresentationFromStartToPath(ItemStreamBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex);
	protected abstract void getLinearRepresentationFromPathToEnd(ItemStreamBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex);



	
	public boolean sendLinearRepresentationModifiedEventToParent(LinearRepresentationEvent event)
	{
		if ( parent != null )
		{
			return parent.onLinearRepresentationModifiedEvent( event );
		}
		else
		{
			return false;
		}
	}
	
	public boolean sendLinearRepresentationModifiedEvent(LinearRepresentationEvent event)
	{
		return onLinearRepresentationModifiedEvent( event );
	}
	
	
	protected boolean onLinearRepresentationModifiedEvent(LinearRepresentationEvent event)
	{
		if ( linearRepresentationListener != null )
		{
			if ( linearRepresentationListener.linearRepresentationModified( this, event ) )
			{
				return true;
			}
		}
		
		if ( parent != null )
		{
			return parent.onLinearRepresentationModifiedEvent( event );
		}
		
		return false;
	}
	
		
	protected abstract void buildLinearRepresentation(ItemStreamBuilder builder);
	
	protected void appendToLinearRepresentation(ItemStreamBuilder builder)
	{
		if ( structuralRepresentation != null )
		{
			structuralRepresentation.addPrefixToStream( builder );
			if ( structuralRepresentation.getMainValue()  !=  null )
			{
				structuralRepresentation.addMainToStream( builder );
			}
			else
			{
				buildLinearRepresentation( builder );
			}
			structuralRepresentation.addSuffixToStream( builder );
		}
		else
		{
			buildLinearRepresentation( builder );
		}
	}
	
	protected void appendStructuralPrefixToLinearRepresentation(ItemStreamBuilder builder)
	{
		if ( structuralRepresentation != null )
		{
			structuralRepresentation.addPrefixToStream( builder );
		}
	}
	
	protected void appendStructuralSuffixToLinearRepresentation(ItemStreamBuilder builder)
	{
		if ( structuralRepresentation != null )
		{
			structuralRepresentation.addSuffixToStream( builder );
		}
	}
	
	public ItemStream getLinearRepresentation()
	{
		ItemStreamBuilder builder = new ItemStreamBuilder();
		appendToLinearRepresentation( builder );
		return builder.stream();
	}
	
	
	
	public void clearStructuralRepresentation()
	{
		structuralRepresentation = null;
	}
	
	public void clearStructuralRepresentationUpTo(DPWidget subtreeRoot)
	{
		structuralRepresentation = null;
		if ( this != subtreeRoot )
		{
			if ( parent != null )
			{
				parent.clearStructuralRepresentationUpTo( subtreeRoot );
			}
			else
			{
				throw new IsNotInSubtreeException();
			}
		}
	}

	
	
	public void setStructuralPrefix(StructuralValue value)
	{
		if ( structuralRepresentation == null )
		{
			structuralRepresentation = new StructuralRepresentation();
		}
		structuralRepresentation.setPrefixValue( value );
	}
	
	public void setStructuralPrefixObject(Object value)
	{
		setStructuralPrefix( new StructuralValueObject( value ) );
	}
	
	public void setStructuralPrefixSequence(List<Object> value)
	{
		setStructuralPrefix( new StructuralValueSequence( value ) );
	}
	
	public void setStructuralPrefixStream(ItemStream value)
	{
		setStructuralPrefix( new StructuralValueStream( value ) );
	}
	
	public void clearStructuralPrefix()
	{
		if ( structuralRepresentation != null )
		{
			structuralRepresentation.clearPrefixValue();
		}
	}
	
	
	public void setStructuralSuffix(StructuralValue value)
	{
		if ( structuralRepresentation == null )
		{
			structuralRepresentation = new StructuralRepresentation();
		}
		structuralRepresentation.setSuffixValue( value );
	}
	
	public void setStructuralSuffixObject(Object value)
	{
		setStructuralSuffix( new StructuralValueObject( value ) );
	}
	
	public void setStructuralSuffixSequence(List<Object> value)
	{
		setStructuralSuffix( new StructuralValueSequence( value ) );
	}
	
	public void setStructuralSuffixStream(ItemStream value)
	{
		setStructuralSuffix( new StructuralValueStream( value ) );
	}
	
	public void clearStructuralValue()
	{
		if ( structuralRepresentation != null )
		{
			structuralRepresentation.clearMainValue();
		}
	}
	
	
	
	public void setStructuralValue(StructuralValue value)
	{
		if ( structuralRepresentation == null )
		{
			structuralRepresentation = new StructuralRepresentation();
		}
		structuralRepresentation.setMainValue( value );
	}
	
	public void setStructuralValueObject(Object value)
	{
		setStructuralValue( new StructuralValueObject( value ) );
	}
	
	public void setStructuralValueSequence(List<Object> value)
	{
		setStructuralValue( new StructuralValueSequence( value ) );
	}
	
	public void setStructuralValueStream(ItemStream value)
	{
		setStructuralValue( new StructuralValueStream( value ) );
	}
	
	public void clearStructuralSuffix()
	{
		if ( structuralRepresentation != null )
		{
			structuralRepresentation.clearSuffixValue();
		}
	}
	
	
	
	public boolean hasStructuralRepresentation()
	{
		return structuralRepresentation != null;
	}
	
	
	
	
	
	//
	//
	// KEYBOARD EVENTS
	//
	//
	
	protected boolean propagateKeyPress(KeyEvent event)
	{
		if ( keyboardListener != null )
		{
			if ( keyboardListener.onKeyPress( this, event ) )
			{
				return true;
			}
		}

		if ( parent != null )
		{
			return parent.propagateKeyPress( event );
		}
		
		return false;
	}

	protected boolean propagateKeyRelease(KeyEvent event)
	{
		if ( keyboardListener != null )
		{
			if ( keyboardListener.onKeyRelease( this, event ) )
			{
				return true;
			}
		}

		if ( parent != null )
		{
			return parent.propagateKeyRelease( event );
		}
		
		return false;
	}

	protected boolean propagateKeyTyped(KeyEvent event)
	{
		if ( keyboardListener != null )
		{
			if ( keyboardListener.onKeyTyped( this, event ) )
			{
				return true;
			}
		}

		if ( parent != null )
		{
			return parent.propagateKeyTyped( event );
		}
		
		return false;
	}
	

	
	
	//
	//
	// SEGMENT METHODS
	//
	//
	
	public DPSegment getSegment()
	{
		if ( parent != null )
		{
			return parent.getSegment();
		}
		else
		{
			return null;
		}
	}
	
	
	
	//
	//
	// FRAME METHODS
	//
	//
	
	public DPFrame getFrame()
	{
		if ( parent != null )
		{
			return parent.getFrame();
		}
		else
		{
			return null;
		}
	}
	
	
	
	//
	// Meta-element
	//
	
	protected static TextStyleSheet headerDebugTextStyle = new TextStyleSheet( new Font( "Sans serif", Font.BOLD, 14 ), new Color( 0.0f, 0.5f, 0.5f ) );
	protected static TextStyleSheet headerDescriptionTextStyle = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 14 ), new Color( 0.0f, 0.0f, 0.75f ) );
	protected static HBoxStyleSheet metaHeaderHBoxStyle = new HBoxStyleSheet( 10.0 );
	protected static EmptyBorder metaHeaderEmptyBorder = new EmptyBorder();


	public DPWidget createMetaHeaderData()
	{
		return null;
	}
	
	public DPWidget createMetaHeaderDebug()
	{
		if ( debugName != null )
		{
			return new DPText( null, headerDebugTextStyle, "<" + debugName + ">" );
		}
		else
		{
			return null;
		}
	}
	
	public DPWidget createMetaDescription()
	{
		String description = toString();
		description = description.replace( "BritefuryJ.DocPresent.", "" );
		return new DPText( null, headerDescriptionTextStyle, description );
	}
	
	protected Border getMetaHeaderBorder()
	{
		return metaHeaderEmptyBorder;
	}
	
	public DPWidget createMetaHeader()
	{
		DPHBox hbox = new DPHBox( null, metaHeaderHBoxStyle );
		DPWidget data = createMetaHeaderData();
		DPWidget debug = createMetaHeaderDebug();
		DPWidget descr = createMetaDescription();
		if ( data != null )
		{
			hbox.append( data );
		}
		if ( debug != null )
		{
			hbox.append( debug );
		}
		hbox.append( descr );
		

		DPBorder border = new DPBorder( null, getMetaHeaderBorder() );
		border.setChild( hbox );
		return border;
	}
	
	public DPBorder getMetaHeaderBorderWidget()
	{
		if ( metaElement != null )
		{
			DPBin bin = (DPBin)metaElement;
			return (DPBorder)bin.getChild();
		}
		else
		{
			return null;
		}
	}
	
	public void refreshMetaHeader()
	{
		if ( metaElement != null )
		{
			DPBorder border = getMetaHeaderBorderWidget();
			DPHBox hbox = (DPHBox)border.getChild();
			
			DPWidget data = createMetaHeaderData();
			DPWidget debug = createMetaHeaderDebug();
			DPWidget descr = createMetaDescription();
			hbox.clear();
			
			if ( data != null )
			{
				hbox.append( data );
			}
			if ( debug != null )
			{
				hbox.append( debug );
			}
			hbox.append( descr );
		}
	}

	public DPWidget createMetaElement()
	{
		DPBin bin = new DPBin( null );
		bin.setChild( createMetaHeader() );
		return bin;
	}
	
	public DPWidget initialiseMetaElement()
	{
		if ( metaElement == null )
		{
			metaElement = createMetaElement();
		}
		return metaElement;
	}
	
	public void shutdownMetaElement()
	{
		metaElement = null;
	}
	
	public DPWidget getMetaElement()
	{
		return metaElement;
	}
	
	
	
	
	public void setDebugName(String debugName)
	{
		this.debugName = debugName;
	}

	
	
	
	//
	// TYPE METHODS
	//

	public boolean isContentLeaf()
	{
		return false;
	}
}
