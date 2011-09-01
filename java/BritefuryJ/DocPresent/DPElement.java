//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

/*
 * Layout spaces:
 * 
 * Local space - local to element
 * Allocation space - space in which children are positioned
 * 
 * 
 * Transformations:
 * Child local space to allocation space - the position allocated to the child by this element
 * Allocation space to local space - normally identity, but the viewport transformation for viewport elements
 * Child local space to local space - the concatenation of child local space to allocation space, and allocation space to local space
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.DndHandler;
import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
import BritefuryJ.DocPresent.Interactor.CaretCrossingElementInteractor;
import BritefuryJ.DocPresent.Interactor.ContextMenuElementInteractor;
import BritefuryJ.DocPresent.Interactor.MotionElementInteractor;
import BritefuryJ.DocPresent.Interactor.PushElementInteractor;
import BritefuryJ.DocPresent.Interactor.RealiseElementInteractor;
import BritefuryJ.DocPresent.Layout.ElementAlignment;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.LayoutTree.ArrangedSequenceLayoutNode;
import BritefuryJ.DocPresent.LayoutTree.LayoutNode;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StreamValue.SequentialStreamValueVisitor;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.DocPresent.StreamValue.StreamValueBuilder;
import BritefuryJ.DocPresent.StyleParams.ElementStyleParams;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.IncrementalView.ViewFragmentFunction;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;
import BritefuryJ.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;
import BritefuryJ.Pres.ObjectPres.ObjectBoxWithFields;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Spacer;
import BritefuryJ.Projection.Perspective;
import BritefuryJ.StyleSheet.StyleSheet;





abstract public class DPElement extends PointerInputElement implements Presentable
{
	protected static double NON_TYPESET_CHILD_BASELINE_OFFSET = -5.0;
	
	
	
	//
	//
	// FILTERS
	//
	//
	
	public static class SubtreeElementFilter implements ElementFilter
	{
		private DPElement subtreeRoot;
		
		
		public SubtreeElementFilter(DPElement subtreeRoot)
		{
			this.subtreeRoot = subtreeRoot;
		}
		
		
		public boolean testElement(DPElement element)
		{
			return element.isInSubtreeRootedAt( subtreeRoot );
		}
	}
	
	
	
	
	
	//
	//
	// PRESENTABLE
	//
	//
	
	private static class TreeExplorerViewFragmentFn implements ViewFragmentFunction
	{
		public Pres createViewFragment(Object x, FragmentView ctx, SimpleAttributeTable inheritedState)
		{
			DPElement element = (DPElement)x;
			return element.exploreTreePresent( ctx, inheritedState );
		}
	}
	
	protected static Perspective treeExplorerPerspective = new Perspective( new TreeExplorerViewFragmentFn() );
	
	public static class ElementTreeExplorer implements Presentable
	{
		private DPElement element;
		
		
		protected ElementTreeExplorer(DPElement element)
		{
			this.element = element;
		}
		
		
		public DPElement getElement()
		{
			return element;
		}


		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return treeExplorerPerspective.applyTo( new InnerFragment( element ) ); 
		}
	}

	

	
	
	//
	//
	// EXCEPTIONS
	//
	//
	
	public static class IsNotInSubtreeException extends RuntimeException
	{
		static final long serialVersionUID = 0L;
	}
	
	
	public static class ChildHasNoLayoutException extends RuntimeException
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
	// TREE EVENTS
	//
	//
	
	private static class TreeEvent
	{
		private DPElement sourceElement;
		private Object value;
		
		
		public TreeEvent(DPElement sourceElement, Object value)
		{
			this.sourceElement = sourceElement;
			this.value = value;
		}
	}
	
	//
	//
	// INTERACTION FIELDS
	//
	//
	
	private static class InteractionFields
	{
		private ObjectDndHandler dndHandler;
		
		private ArrayList<AbstractElementInteractor> elementInteractors;
		private ArrayList<ElementPainter> painters;
		private ArrayList<TreeEventListener> treeEventListeners;
		
		
		
		public InteractionFields()
		{
		}
		
		
		public InteractionFields copy()
		{
			InteractionFields f = new InteractionFields();
			f.dndHandler = dndHandler;
			if ( elementInteractors != null )
			{
				f.elementInteractors = new ArrayList<AbstractElementInteractor>();
				f.elementInteractors.addAll( elementInteractors );
			}
			if ( painters != null )
			{
				f.painters = new ArrayList<ElementPainter>();
				f.painters.addAll( painters );
			}
			if ( treeEventListeners != null )
			{
				f.treeEventListeners = new ArrayList<TreeEventListener>();
				f.treeEventListeners.addAll( treeEventListeners );
			}
			return f;
		}
		
		
		public void addElementInteractor(AbstractElementInteractor interactor)
		{
			if ( elementInteractors == null )
			{
				elementInteractors = new ArrayList<AbstractElementInteractor>();
			}
			elementInteractors.add( interactor );
		}
		
		public void removeElementInteractor(AbstractElementInteractor interactor)
		{
			if ( elementInteractors != null )
			{
				elementInteractors.remove( interactor );
				if ( elementInteractors.isEmpty() )
				{
					elementInteractors = null;
				}
			}
		}
		
		
		
		public void addPainter(ElementPainter painter)
		{
			if ( painters == null )
			{
				painters = new ArrayList<ElementPainter>();
			}
			painters.add( painter );
		}
		
		public void removePainter(ElementPainter painter)
		{
			if ( painters != null )
			{
				painters.remove( painter );
				if ( painters.isEmpty() )
				{
					painters = null;
				}
			}
		}
		
		
		
		public void addTreeEventListener(TreeEventListener listener)
		{
			if ( treeEventListeners == null )
			{
				treeEventListeners = new ArrayList<TreeEventListener>();
			}
			treeEventListeners.add( listener );
		}
		
		public void removeTreeEventListener(TreeEventListener listener)
		{
			if ( treeEventListeners != null )
			{
				treeEventListeners.remove( listener );
				if ( treeEventListeners.isEmpty() )
				{
					treeEventListeners = null;
				}
			}
		}
		
		
		
		
		
		public boolean isIdentity()
		{
			return dndHandler == null  &&  treeEventListeners == null  &&  elementInteractors == null  &&  painters == null;
		}
	}

	
	
	
	//
	//
	// FLAGS
	//
	//
	
	protected final static int FLAG_REALISED = 0x1;
	protected final static int FLAG_RESIZE_QUEUED = 0x2;
	protected final static int FLAG_ALLOCATION_UP_TO_DATE = 0x4;
	protected final static int FLAG_CARET_GRABBED = 0x8;
	protected final static int FLAG_HOVER = 0x10;
	protected final static int FLAG_ENSURE_VISIBLE_QUEUED = 0x20;
	protected final static int FLAG_HAS_FIXED_VALUE = 0x40;
	protected final static int FLAG_HAS_CACHED_VALUES = 0x80;

	protected final static int _ALIGN_SHIFT = 8;
	protected final static int _ALIGN_MASK = ElementAlignment._ELEMENTALIGN_MASK  <<  _ALIGN_SHIFT;
	protected final static int _HALIGN_MASK = ElementAlignment._HALIGN_MASK  <<  _ALIGN_SHIFT;
	protected final static int _VALIGN_MASK = ElementAlignment._VALIGN_MASK  <<  _ALIGN_SHIFT;
	protected final static int FLAGS_ELEMENT_END = ElementAlignment._ELEMENTALIGN_END  <<  _ALIGN_SHIFT;
	
	
	//
	//
	// FIELDS
	//
	//
	
	protected int flags;
	
	protected ElementStyleParams styleParams;
	protected DPContainer parent;
	protected PresentationComponent.RootElement rootElement;
	
	protected LayoutNode layoutNode;
	
	private InteractionFields interactionFields;

	protected PresentationStateListenerList debugPresStateListeners = null;
	protected String debugName;
	
	protected ElementValueFunction valueFn;
	protected Object fixedValue;
	
	
	
	
	//
	//
	// FIELDS AS DICTIONARY VALUES
	//
	//
	
	// These fields would be null/non-existant for the vast majority of elements, so store them in a global dictionary to
	// save space
	
	private static WeakHashMap<DPElement, ArrayList<Runnable>> waitingImmediateEventsByElement = new WeakHashMap<DPElement, ArrayList<Runnable>>();
	
	
	
	

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
	
	public DPElement()
	{
		this( ElementStyleParams.defaultStyleParams );
	}
	
	public DPElement(ElementStyleParams styleParams)
	{
		flags = 0;
		this.styleParams = styleParams;
		setAlignmentFlags( ElementAlignment.flagValue( styleParams.getHAlignment(), styleParams.getVAlignment() ) );
	}
	
	protected DPElement(DPElement element)
	{
		this( element.styleParams );
		debugName = element.debugName;
		valueFn = element.valueFn;
		if ( element.hasFixedValue() )
		{
			setFixedValue( element.getFixedValue() );
		}
		if ( element.interactionFields != null )
		{
			interactionFields = element.interactionFields.copy();
		}
	}
	
	
	
	//
	//
	// Context
	//
	//
	
	public FragmentContext getFragmentContext()
	{
		DPElement w = this;
		while ( w != null )
		{
			FragmentContext c = w.getContextOfFragment();
			if ( c != null )
			{
				return c;
			}
			
			w = w.getParent();
		}
		
		return null;
	}
	
	// Override this in subclasses
	protected FragmentContext getContextOfFragment()
	{
		return null;
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
	
	public void setAlignment(HAlignment hAlign, VAlignment vAlign)
	{
		setAlignmentFlags( ElementAlignment.flagValue( hAlign, vAlign ) );
	}
	
	
	public int getAlignmentFlags()
	{
		return ( flags & _ALIGN_MASK )  >>  _ALIGN_SHIFT;
	}
	
	public HAlignment getHAlignment()
	{
		return ElementAlignment.getHAlignment( getAlignmentFlags() );
	}
	
	public VAlignment getVAlignment()
	{
		return ElementAlignment.getVAlignment( getAlignmentFlags() );
	}
	

	
	
	//
	// Layout wrap method - papers over the fact that some elements cannot have layout-less elements as children
	//
	
	public DPElement layoutWrap(HAlignment hAlign, VAlignment vAlign)
	{
		if ( getLayoutNode() != null )
		{
			return this;
		}
		else
		{
			DPRow row = new DPRow();
			row.setChildren( new DPElement[] { this } );
			row.setAlignment( hAlign, vAlign );
			return row;
		}
	}
	
	

	
	
	//
	// Geometry methods
	//
	
	public Point2 getPositionInParentSpace()
	{
		return layoutNode != null  ?  layoutNode.getPositionInParentSpace()  :  new Point2();
	}
	
	public double getPositionInParentSpaceX()
	{
		return layoutNode != null  ?  layoutNode.getAllocPositionInParentSpaceX()  :  0.0;
	}
	
	public double getPositionInParentSpaceY()
	{
		return layoutNode != null  ?  layoutNode.getAllocPositionInParentSpaceY()  :  0.0;
	}
	
	public Point2 getPositionInParentAllocationSpace()
	{
		return layoutNode != null  ?  layoutNode.getPositionInParentAllocationSpace()  :  new Point2();
	}
	
	public double getActualWidth()
	{
		return layoutNode != null  ?  layoutNode.getActualWidth()  :  ( parent != null  ?  parent.getActualWidth()  :  0.0 );
	}
	
	public double getActualHeight()
	{
		return layoutNode != null  ?  layoutNode.getActualHeight()  :  ( parent != null  ?  parent.getActualHeight()  :  0.0 );
	}
	
	public Vector2 getActualSize()
	{
		return layoutNode != null  ?  layoutNode.getActualSize()  :  ( parent != null  ?  parent.getActualSize()  :  new Vector2() );
	}
	
	public double getActualWidthInParentSpace()
	{
		return layoutNode != null  ?  layoutNode.getActualWidthInParentSpace()  :  ( parent != null  ?  parent.getActualWidth()  :  0.0 );
	}
	
	public double getActualHeightInParentSpace()
	{
		return layoutNode != null  ?  layoutNode.getActualHeightInParentSpace()  :  ( parent != null  ?  parent.getActualHeight()  :  0.0 );
	}
	
	public Vector2 getActualSizeInParentSpace()
	{
		return layoutNode != null  ?  layoutNode.getActualSizeInParentSpace()  :  ( parent != null  ?  parent.getActualSize()  :  new Vector2());
	}
	
	protected AABox2 getVisibleBoxInLocalSpace()
	{
		return getLocalAABox();
	}
	
	

	public double getAllocWidth()
	{
		return layoutNode != null  ?  layoutNode.getAllocWidth()  :  ( parent != null  ?  parent.getAllocWidth()  :  0.0 );
	}
	
	public double getAllocHeight()
	{
		return layoutNode != null  ?  layoutNode.getAllocHeight()  :  ( parent != null  ?  parent.getAllocHeight()  :  0.0 );
	}
	
	public LAllocV getAllocV()
	{
		return layoutNode != null  ?  layoutNode.getAllocV()  :  ( parent != null  ?  parent.getAllocV()  :  new LAllocV( 0.0 ) );
	}
	
	public Vector2 getAllocSize()
	{
		return layoutNode != null  ?  layoutNode.getAllocSize()  :  ( parent != null  ?  parent.getAllocSize()  :  new Vector2() );
	}

	
	
	public AABox2 getLocalAABox()
	{
		return new AABox2( new Point2(), getActualSize() );
	}
	
	public AABox2 getLocalClipBox()
	{
		return null;
	}
	
	public AABox2 getAABoxInParentSpace()
	{
		return new AABox2( getPositionInParentSpace(), getActualSizeInParentSpace() );
	}
	
	
	protected Shape[] getShapes()
	{
		Vector2 size = getActualSize();
		return new Shape[] { new Rectangle2D.Double( 0.0, 0.0, size.x, size.y ) };
	}
	
	// For most elements, this is the bounding box. For layout-less elements, it is their bounds within the closest
	// non-layout-less parent
	public AABox2 getVisibleSpaceBox()
	{
		Vector2 size = getActualSize();
		return new AABox2( 0.0, 0.0, size.x, size.y );
	}

	
	public Xform2 getLocalToParentAllocationSpaceXform()
	{
		return parent != null  ?  parent.getAllocationSpaceToLocalSpaceXform( this )  :  Xform2.identity;
	}
	
	public Xform2 getLocalToParentXform()
	{
		return getLocalToParentAllocationSpaceXform().concat( new Xform2( getPositionInParentAllocationSpace().toVector2() ) );
	}
	
	public Xform2 getParentToLocalXform()
	{
		return getLocalToParentXform().inverse();
	}
	
	
	
	public AffineTransform getLocalToParentAffineTransform()
	{
		return getLocalToParentXform().toAffineTransform();
	}

	public AffineTransform getParentToLocalAffineTransform()
	{
		return getParentToLocalXform().toAffineTransform();
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
	
	
	
	public Xform2 getLocalToAncestorXform(DPElement ancestor, Xform2 x)
	{
		DPElement node = this;
		
		while ( node != ancestor )
		{
			DPElement parentNode = node.parent;
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
	
	public Xform2 getLocalToAncestorXform(DPElement ancestor)
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
	
	
	
	public Xform2 getAncestorToLocalXform(DPElement ancestor, Xform2 x)
	{
		DPElement node = this;
		
		while ( node != ancestor )
		{
			DPElement parentNode = node.parent;
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
	
	public Xform2 getAncestorToLocalXform(DPElement ancestor)
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
	
	
	
	public Xform2 getTransformRelativeTo(DPElement toElement, Xform2 x)
	{
		Xform2 myXform = getLocalToRootXform();
		Xform2 toElementXform = toElement.getLocalToRootXform();
		return myXform.concat( toElementXform.inverse() );
	}
	
	
	public Point2 getLocalPointRelativeToRoot(Point2 p)
	{
		return getLocalPointRelativeToAncestor( null, p );
	}
	
	public Point2 getLocalPointRelativeToAncestor(DPElement ancestor, Point2 p)
	{
		DPElement node = this;
		
		while ( node != ancestor )
		{
			DPElement parentNode = node.parent;
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
	
	public Point2 getLocalPointRelativeTo(DPElement toElement, Point2 p)
	{
		Point2 pointInRoot = getLocalPointRelativeToRoot( p );
		Xform2 toElementXform = toElement.getLocalToRootXform();
		return toElementXform.inverse().transform( pointInRoot );
	}
	
	
	public AffineTransform pushGraphicsTransform(Graphics2D graphics)
	{
		AffineTransform current = graphics.getTransform();
		getLocalToRootXform().apply( graphics );
		return current;
	}
	
	public void popGraphicsTransform(Graphics2D graphics, AffineTransform x)
	{
		graphics.setTransform( x );
	}
	
	
	
	protected void ensureRegionVisible(AABox2 box)
	{
		if ( parent != null )
		{
			parent.ensureRegionVisible( getLocalToParentXform().transform( box ) );
		}
	}
	
	protected boolean isLocalSpacePointVisible(Point2 point)
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
	
	public void ensureVisible()
	{
		if ( isRealised() )
		{
			ensureRegionVisible( getLocalAABox() );
		}
		else
		{
			setFlag( FLAG_ENSURE_VISIBLE_QUEUED );
		}
	}
	
	
	
	

	//
	//
	// Flag methods
	//
	//
	
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
	
	
	public boolean isResizeQueued()
	{
		return testFlag( FLAG_RESIZE_QUEUED );
	}
	
	public void clearFlagResizeQueued()
	{
		clearFlag( FLAG_RESIZE_QUEUED );
	}
	
	public void setFlagResizeQueued()
	{
		setFlag( FLAG_RESIZE_QUEUED );
	}
	
	
	public boolean isAllocationUpToDate()
	{
		return testFlag( FLAG_ALLOCATION_UP_TO_DATE );
	}
	
	public void clearFlagAllocationUpToDate()
	{
		clearFlag( FLAG_ALLOCATION_UP_TO_DATE );
	}
	
	public void setFlagAllocationUpToDate()
	{
		setFlag( FLAG_ALLOCATION_UP_TO_DATE );
	}
	
	

	
	
	//
	//
	// Pointers within bounds
	//
	//
	
	protected boolean arePointersWithinBounds()
	{
		if ( rootElement != null )
		{
			return rootElement.getInputTable().arePointersWithinBoundsOfElement( this );
		}
		else
		{
			return false;
		}
	}
	
	protected ArrayList<PointerInterface> getPointersWithinBounds()
	{
		if ( rootElement != null )
		{
			return rootElement.getInputTable().getPointersWithinBoundsOfElement( this );
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
	
	public PresentationComponent.RootElement getRootElement()
	{
		return rootElement;
	}
	
	
	public DPContainer getParent()
	{
		return parent;
	}
	
	protected void setParent(DPContainer parent, PresentationComponent.RootElement root)
	{
		this.parent = parent;
		if ( root != rootElement )
		{
			setRootElement( root );
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
	
	
	
	public void replaceWith(DPElement replacement)
	{
		if ( parent != null )
		{
			int alignmentFlags = getAlignmentFlags();
			replacement.setAlignmentFlags( alignmentFlags );
			parent.replaceChild( this, replacement );
		}
		else
		{
			throw new RuntimeException( "Could not replace element - element has no parent" );
		}
	}
	
	
	public int computeSubtreeSize()
	{
		return 1;
	}
	
	
	public boolean isInSubtreeRootedAt(DPElement r)
	{
		DPElement e = this;
		
		while ( e != null )
		{
			if ( e == r )
			{
				return true;
			}
			
			e = e.getParent();
		}
		
		return false;
	}
	
	
	public ArrayList<DPElement> getElementPathFromRoot()
	{
		ArrayList<DPElement> path = new ArrayList<DPElement>();
		
		DPElement element = this;
		while ( element != null )
		{
			path.add( 0, element );
			element = element.getParent();
		}
		
		return path;
	}
	
	public ArrayList<DPElement> getElementPathToRoot()
	{
		ArrayList<DPElement> path = new ArrayList<DPElement>();
		
		DPElement element = this;
		while ( element != null )
		{
			path.add( element );
			element = element.getParent();
		}
		
		return path;
	}
	
	public ArrayList<DPElement> getElementPathFromSubtreeRoot(DPContainer subtreeRoot)
	{
		ArrayList<DPElement> path = new ArrayList<DPElement>();
		
		DPElement element = this;
		while ( element != null )
		{
			path.add( 0, element );
			if ( element == subtreeRoot )
			{
				return path;
			}
			element = element.getParent();
		}
		
		throw new RuntimeException( "Element not within subtree" );
	}
	
	public ArrayList<DPElement> getElementPathToSubtreeRoot(DPContainer subtreeRoot)
	{
		ArrayList<DPElement> path = new ArrayList<DPElement>();
		
		DPElement element = this;
		while ( element != null )
		{
			path.add( element );
			if ( element == subtreeRoot )
			{
				return path;
			}
			element = element.getParent();
		}

		throw new RuntimeException( "Element not within subtree" );
	}
	
	

	public DPContentLeaf getFirstLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		return null;
	}

	public DPContentLeaf getFirstLeafInSubtree()
	{
		return getFirstLeafInSubtree( null, null );
	}

	public DPContentLeaf getFirstEditableLeafInSubtree()
	{
		return getFirstLeafInSubtree( null, new DPContentLeafEditable.EditableLeafElementFilter() );
	}

	public DPContentLeaf getLastLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		return null;
	}

	public DPContentLeaf getLastLeafInSubtree()
	{
		return getLastLeafInSubtree( null, null );
	}

	public DPContentLeaf getLastEditableLeafInSubtree()
	{
		return getLastLeafInSubtree( null, new DPContentLeafEditable.EditableLeafElementFilter() );
	}

	
	
	public static void getPathsFromCommonSubtreeRoot(DPElement w0, List<DPElement> path0, DPElement w1, List<DPElement> path1)
	{
		if ( w0 == w1 )
		{
			path0.add( w0 );
			path1.add( w1 );
		}
		else
		{
			ArrayList<DPElement> p0 = w0.getElementPathFromRoot();
			ArrayList<DPElement> p1 = w1.getElementPathFromRoot();
			
			int minLength = Math.min( p0.size(), p1.size() );
			
			if ( p0.get( 0 ) != p1.get( 0 ) )
			{
				throw new RuntimeException( "Bad path" );
			}
			
			int numCommonElements = 0;
			
			for (int i = 0; i < minLength; i++)
			{
				numCommonElements = i;
				
				if ( p0.get( i ) != p1.get( i ) )
				{
					break;
				}
			}
			
			path0.addAll( p0.subList( numCommonElements - 1, p0.size() ) );
			path1.addAll( p1.subList( numCommonElements - 1, p1.size() ) );
		}
	}
	
	public static boolean areElementsInOrder(DPElement w0, DPElement w1)
	{
		ArrayList<DPElement> path0 = new ArrayList<DPElement>();
		ArrayList<DPElement> path1 = new ArrayList<DPElement>();
		DPElement.getPathsFromCommonSubtreeRoot( w0, path0, w1, path1 );
		
		if ( path0.size() > 1  &&  path1.size() > 1 )
		{
			DPContainer commonRoot = (DPContainer)path0.get( 0 );
			return commonRoot.areChildrenInOrder( path0.get( 1 ), path1.get( 1 ) );
		}
		else if ( path0.size() == 1  &&  path1.size() == 1 )
		{
			if ( w0 != w1 )
			{
				throw new RuntimeException( "Paths have length 1, but elements are different" );
			}
			return true;
		}
		else
		{
			throw new RuntimeException( "Paths should either both have length == 1, or both have length > 1" );
		}
	}
	
	
	protected void setRootElement(PresentationComponent.RootElement root)
	{
		if ( root != rootElement )
		{
			rootElement = root;
			if ( rootElement != null )
			{
				ArrayList<Runnable> waitingImmediateEvents = waitingImmediateEventsByElement.get( this );
				if ( waitingImmediateEvents != null )
				{
					for (Runnable event: waitingImmediateEvents)
					{
						rootElement.queueImmediateEvent( event );
					}
					waitingImmediateEventsByElement.remove( this );
				}
			}
		}
	}
	
	
	
	//
	//
	// ELEMENT TREE SEARCHING
	//
	//
	
	protected List<DPElement> getSearchChildren()
	{
		return null;
	}
	
	

	
	//
	// Immediate event queue methods
	//
	
	public void queueImmediateEvent(Runnable event)
	{
		if ( rootElement != null )
		{
			rootElement.queueImmediateEvent( event );
		}
		else
		{
			ArrayList<Runnable> waitingImmediateEvents = waitingImmediateEventsByElement.get( this );
			if ( waitingImmediateEvents == null )
			{
				waitingImmediateEvents = new ArrayList<Runnable>();
				waitingImmediateEventsByElement.put( this, waitingImmediateEvents );
			}
			if ( !waitingImmediateEvents.contains( event ) )
			{
				waitingImmediateEvents.add( event );
			}
		}
			
	}

	public void dequeueImmediateEvent(Runnable event)
	{
		if ( rootElement != null )
		{
			rootElement.dequeueImmediateEvent( event );
		}
		else
		{
			ArrayList<Runnable> waitingImmediateEvents = waitingImmediateEventsByElement.get( this );
			if ( waitingImmediateEvents != null )
			{
				waitingImmediateEvents.remove( event );
				if ( waitingImmediateEvents.isEmpty() )
				{
					waitingImmediateEvents = null;
					waitingImmediateEventsByElement.remove( this );
				}
			}
		}
			
	}
	
	
	
	
	//
	// General event methods
	//
	
	protected void onRealise()
	{
	}
	
	protected void onUnrealise(DPElement unrealiseRoot)
	{
	}
	
	protected void onParentChanged()
	{
	}
	
	
	protected void drawBackground(Graphics2D graphics)
	{
		ElementStyleParams styleParams = getStyleParams();
		
		Painter backgroundPainter;
		if ( testFlag( FLAG_HOVER ) )
		{
			Painter hoverBackground = styleParams.getHoverBackground();
			backgroundPainter = hoverBackground != null  ?  hoverBackground  :  styleParams.getBackground();
		}
		else
		{
			backgroundPainter = styleParams.getBackground();
		}
		if ( backgroundPainter != null )
		{
			backgroundPainter.drawShapes( graphics, getShapes() );
		}
	}
	
	protected void draw(Graphics2D graphics)
	{
	}
	
	
	protected void onSetScale(double scale)
	{
	}
	
	
	protected void queueResize()
	{
		LayoutNode layout = getValidLayoutNode();
		if ( layout != null )
		{
			layout.queueResize();
		}
	}
	
	public void onSizeChange()
	{
	}
	
	
	protected void queueRedraw(AABox2 box)
	{
		if ( isRealised()  &&  parent != null )
		{
			parent.childRedrawRequest( this, box );
		}
	}
	
	public void queueFullRedraw()
	{
		queueRedraw( getLocalAABox() );
	}
	
	
	public boolean isRedrawRequiredOnHover()
	{
		return styleParams.getHoverBackground() != null;
	}
	
	public boolean isResizeRequiredOnHover()
	{
		return false;
	}
	
	
	
	
	
	




	protected void handleRealise()
	{
		setFlagRealised();
		onRealise();
		
		Iterable<AbstractElementInteractor> interactors = getElementInteractors( RealiseElementInteractor.class );
		if ( interactors != null )
		{
			for (AbstractElementInteractor interactor: interactors )
			{
				RealiseElementInteractor realiseInt = (RealiseElementInteractor)interactor;
				realiseInt.elementRealised( this );
			}
		}
		
		if ( testFlag( FLAG_ENSURE_VISIBLE_QUEUED ) )
		{
			clearFlag( FLAG_ENSURE_VISIBLE_QUEUED );
			rootElement.queueEnsureVisible( this );
		}
	}
	
	protected void handleUnrealise(DPElement unrealiseRoot)
	{
		invalidateCachedValues();

		if ( testFlag( FLAG_CARET_GRABBED ) )
		{
			if ( rootElement != null )
			{
				rootElement.caretUngrab( this );
			}
			clearFlag( FLAG_CARET_GRABBED );
		}
		
		if ( rootElement != null )
		{
			rootElement.elementUnrealised( this );
		}
		
		Iterable<AbstractElementInteractor> interactors = getElementInteractors( RealiseElementInteractor.class );
		if ( interactors != null )
		{
			for (AbstractElementInteractor interactor: interactors )
			{
				RealiseElementInteractor realiseInt = (RealiseElementInteractor)interactor;
				realiseInt.elementUnrealised( this );
			}
		}

		onUnrealise( unrealiseRoot );
		clearFlagRealised();
	}
	
	
	//
	//
	// PAINTING METHODS
	//
	//
	
	protected Shape pushClip(Graphics2D graphics)
	{
		Shape clipShape = null;
		AABox2 localClip = getLocalClipBox();
		if ( localClip != null )
		{
			clipShape = graphics.getClip();
			graphics.clip( new Rectangle2D.Double( localClip.getLowerX(), localClip.getLowerY(), localClip.getWidth(), localClip.getHeight() ) );
		}
		return clipShape;
	}
	
	protected void popClip(Graphics2D graphics, Shape clipShape)
	{
		if ( getLocalClipBox() != null )
		{
			graphics.setClip( clipShape );
		}
	}

	public void clip(Graphics2D graphics)
	{
		graphics.clip( new Rectangle2D.Double( 0.0, 0.0, getActualWidth(), getActualHeight() ) );
	}

	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
		drawBackground( graphics );
		List<ElementPainter> painters = getPainters();
		if ( painters != null )
		{
			for (ElementPainter painter: painters)
			{
				try
				{
					painter.drawBackground( this, graphics );
				}
				catch (Throwable e)
				{
					notifyExceptionDuringEventHandler( painter, "drawBackground", e );
				}
			}
		}
	}
	
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		draw( graphics );
		List<ElementPainter> painters = getPainters();
		if ( painters != null )
		{
			for (ElementPainter painter: painters)
			{
				try
				{
					painter.draw( this, graphics );
				}
				catch (Throwable e)
				{
					notifyExceptionDuringEventHandler( painter, "draw", e );
				}
			}
		}
	}
	
	
	public void drawToGraphics(Graphics2D graphics, AABox2 areaBox)
	{
		handleDrawBackground( graphics, areaBox );
		handleDraw( graphics, areaBox );
	}
	

	
	
	
	//
	//
	// Element tree traversal methods
	//
	//
	
	protected DPElement getFirstChildAtLocalPoint(Point2 localPos)
	{
		return null;
	}
	
	protected DPElement getLastChildAtLocalPoint(Point2 localPos)
	{
		return null;
	}
	

	
	
	//
	//
	// POINTER INPUT ELEMENT METHODS
	//
	//
	
	
	protected Cursor getCursor()
	{
		return styleParams.getCursor();
	}
	
	protected Cursor getAncestorCursor()
	{
		DPElement w = getParent();
		while ( w != null )
		{
			Cursor cursor = w.getCursor();
			if ( cursor != null )
			{
				return cursor;
			}
			
			w = w.getParent();
		}
		
		return null;
	}
	

	protected void handlePointerEnter(PointerMotionEvent event)
	{
		handleHover();
			
		Cursor cursor = getCursor();
		if ( cursor != null  &&  rootElement != null )
		{
			rootElement.setPointerCursor( cursor );
		}
	}
	
	protected void handlePointerLeave(PointerMotionEvent event)
	{
		handleHover();
		
		Cursor cursor = getCursor();
		if ( cursor != null  &&  rootElement != null )
		{
			Cursor ancestorCursor = getAncestorCursor();
			if ( ancestorCursor != null )
			{
				getRootElement().setPointerCursor( ancestorCursor );
			}
			else
			{
				getRootElement().setPointerCursorDefault();
			}
		}
	}
	
	private void handleHover()
	{
		boolean bPrevHover = testFlag( FLAG_HOVER );
		boolean bHover = arePointersWithinBounds();
		if ( bHover != bPrevHover )
		{
			setFlagValue( FLAG_HOVER, bHover );
			if ( isResizeRequiredOnHover() )
			{
				queueResize();
			}
			else if ( isRedrawRequiredOnHover() )
			{
				queueFullRedraw();
			}
		}
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
	
	public boolean isPointerInputElementRealised()
	{
		return isRealised();
	}
	
	public boolean containsParentSpacePoint(Point2 parentPos)
	{
		return getAABoxInParentSpace().containsPoint( parentPos );
	}

	public boolean containsLocalSpacePoint(Point2 localPos)
	{
		return getLocalAABox().containsPoint( localPos );
	}
	

	
	
	//
	//
	// CARET METHODS
	//
	//
	
	protected void onCaretEnter(Caret c)
	{
	}
	
	protected void onCaretLeave(Caret c)
	{
	}
	
	
	protected void handleCaretEnter(Caret c)
	{
		onCaretEnter( c );
		Iterable<AbstractElementInteractor> interactors = getElementInteractors( CaretCrossingElementInteractor.class );
		if ( interactors != null )
		{
			for (AbstractElementInteractor interactor: interactors )
			{
				CaretCrossingElementInteractor caretCrossInt = (CaretCrossingElementInteractor)interactor;
				caretCrossInt.caretEnter( this, c );
			}
		}
	}
	
	protected void handleCaretLeave(Caret c)
	{
		onCaretLeave( c );
		Iterable<AbstractElementInteractor> interactors = getElementInteractors( CaretCrossingElementInteractor.class );
		if ( interactors != null )
		{
			for (AbstractElementInteractor interactor: interactors )
			{
				CaretCrossingElementInteractor caretCrossInt = (CaretCrossingElementInteractor)interactor;
				caretCrossInt.caretLeave( this, c );
			}
		}
	}
	
	
	
	public void grabCaret()
	{
		if ( isRealised() )
		{
			setFlag( FLAG_CARET_GRABBED );
			getRootElement().caretGrab( this );
		}
	}
	
	public void ungrabCaret()
	{
		if ( isRealised()  &&  testFlag( FLAG_CARET_GRABBED ) )
		{
			getRootElement().caretUngrab( this );
			clearFlag( FLAG_CARET_GRABBED );
		}
	}

	
	
	//
	//
	// INTERACTION FIELDS METHODS
	//
	//
	
	private void ensureValidInteractionFields()
	{
		if ( interactionFields == null )
		{
			interactionFields = new InteractionFields();
		}
	}
	
	private void notifyInteractionFieldsModified()
	{
		if ( interactionFields != null  &&  interactionFields.isIdentity() )
		{
			interactionFields = null;
		}
	}
	
	
	
	
	//
	//
	// DRAG AND DROP METHODS
	//
	//
	
	public void addDragSource(ObjectDndHandler.DragSource dragSource)
	{
		ObjectDndHandler current = getValidInitialDndHandler();
		ensureValidInteractionFields();
		interactionFields.dndHandler = current.withDragSource( dragSource ); 
		notifyInteractionFieldsModified();
	}
	
	public void addDragSource(Class<?> dataType, int sourceAspects, ObjectDndHandler.SourceDataFn sourceDataFn, ObjectDndHandler.ExportDoneFn exportDoneFn)
	{
		addDragSource( new ObjectDndHandler.DragSource( dataType, sourceAspects, sourceDataFn, exportDoneFn ) );
	}
	
	public void addDragSource(Class<?> dataType, int sourceAspects, ObjectDndHandler.SourceDataFn sourceDataFn)
	{
		addDragSource( dataType, sourceAspects, sourceDataFn, null );
	}
	
	
	public void addDropDest(ObjectDndHandler.DropDest dropDest)
	{
		ObjectDndHandler current = getValidInitialDndHandler();
		ensureValidInteractionFields();
		interactionFields.dndHandler = current.withDropDest( dropDest );
		notifyInteractionFieldsModified();
	}
	
	public void addDropDest(Class<?> dataType, ObjectDndHandler.CanDropFn canDropFn, ObjectDndHandler.DropFn dropFn)
	{
		addDropDest( new ObjectDndHandler.DropDest( dataType, canDropFn, dropFn ) );
	}
	
	public void addDropDest(Class<?> dataType, ObjectDndHandler.DropFn dropFn)
	{
		addDropDest( dataType, null, dropFn );
	}
	
	
	public void addNonLocalDropDest(ObjectDndHandler.NonLocalDropDest dropDest)
	{
		ObjectDndHandler current = getValidInitialDndHandler();
		ensureValidInteractionFields();
		interactionFields.dndHandler = current.withNonLocalDropDest( dropDest );
		notifyInteractionFieldsModified();
	}
	
	public void addNonLocalDropDest(DataFlavor dataFlavor, ObjectDndHandler.DropFn dropFn)
	{
		addNonLocalDropDest( new ObjectDndHandler.NonLocalDropDest( dataFlavor, dropFn ) );
	}
	

	private ObjectDndHandler getValidInitialDndHandler()
	{
		if ( interactionFields != null  &&  interactionFields.dndHandler != null )
		{
			return interactionFields.dndHandler;
		}
		else
		{
			return ObjectDndHandler.instance;
		}
	}
	
	
	
	public DndHandler getDndHandler()
	{
		return interactionFields != null  ?  interactionFields.dndHandler  :  null;
	}
	
	
	
	
	//
	//
	// ELEMENT INTERACTOR METHODS
	//
	//
	
	public void addElementInteractor(AbstractElementInteractor interactor)
	{
		ensureValidInteractionFields();
		interactionFields.addElementInteractor( interactor );
		notifyInteractionFieldsModified();
	}
	
	public void removeElementInteractor(AbstractElementInteractor interactor)
	{
		if ( interactionFields != null )
		{
			interactionFields.removeElementInteractor( interactor );
			notifyInteractionFieldsModified();
		}
	}
	
	public Iterable<AbstractElementInteractor> getElementInteractors()
	{
		return interactionFields != null  ?  interactionFields.elementInteractors  :  null;
	}
	
	public Iterable<AbstractElementInteractor> getElementInteractors(final Class<?> interactorClass)
	{
		final ArrayList<AbstractElementInteractor> interactors = interactionFields != null  ?  interactionFields.elementInteractors  :  null;
		
		if ( interactors != null )
		{
			Iterable<AbstractElementInteractor> iterable = new Iterable<AbstractElementInteractor>()
			{
				public Iterator<AbstractElementInteractor> iterator()
				{
					Iterator<AbstractElementInteractor> iter = new Iterator<AbstractElementInteractor>()
					{
						private int index = nextIndex( 0 );
						
						
						public boolean hasNext()
						{
							return index != -1;
						}
	
						public AbstractElementInteractor next()
						{
							if ( index == -1 )
							{
								throw new NoSuchElementException();
							}
							else
							{
								AbstractElementInteractor interactor = interactors.get( index );
								index = nextIndex( index + 1 );
								return interactor;
							}
						}
	
						public void remove()
						{
							throw new UnsupportedOperationException();
						}
						
						
						private int nextIndex(int current)
						{
							for (int i = current; i < interactors.size(); i++)
							{
								AbstractElementInteractor interactor = interactors.get( i );
								if ( interactorClass.isInstance( interactor ) )
								{
									return i;
								}
							}
							
							return -1;
						}
					};
					
					return iter;
				}
			};
			
			return iterable;
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	//
	//
	// PAINTER METHODS
	//
	//
	
	public void addPainter(ElementPainter interactor)
	{
		ensureValidInteractionFields();
		interactionFields.addPainter( interactor );
		notifyInteractionFieldsModified();
	}
	
	public void removePainter(ElementPainter interactor)
	{
		if ( interactionFields != null )
		{
			interactionFields.removePainter( interactor );
			notifyInteractionFieldsModified();
		}
	}
	
	public ArrayList<ElementPainter> getPainters()
	{
		return interactionFields != null  ?  interactionFields.painters  :  null;
	}
	
	
	
	
	//
	//
	// CONTEXT MENU INTERACTOR METHODS
	//
	//
	
	public void addContextMenuInteractor(ContextMenuElementInteractor interactor)
	{
		addElementInteractor( interactor );
	}
	
	public void removeContextMenuInteractor(ContextMenuElementInteractor interactor)
	{
		removeElementInteractor( interactor );
	}
	
	
	
	
	//
	//
	// LAYOUT METHODS
	//
	//
	
	
	public LayoutNode getLayoutNode()
	{
		return layoutNode;
	}
	
	public LayoutNode getValidLayoutNode()
	{
		if ( layoutNode != null )
		{
			return layoutNode;
		}
		else
		{
			DPContainer c = parent;
			while ( c != null  )
			{
				if ( c.layoutNode != null )
				{
					return c.layoutNode;
				}
				c = c.getParent();
			}
			
			return null;
		}
	}
	
	public LayoutNode getValidLayoutNodeOfClass(Class<? extends LayoutNode> layoutNodeClass)
	{
		if ( layoutNode != null )
		{
			return layoutNode;
		}
		else
		{
			DPContainer c = parent;
			while ( c != null  )
			{
				if ( c.layoutNode != null )
				{
					if ( layoutNodeClass.isInstance( c.layoutNode ) )
					{
						return c.layoutNode;
					}
				}
				c = c.getParent();
			}
			
			return null;
		}
	}
	
	

	

	
	
	//
	// Focus navigation methods
	//
	
	public Point2 getMarkerPosition(Marker marker)
	{
		if ( marker.getElement() != this )
		{
			throw new RuntimeException( "Marker is not within the bounds of this element" );
		}
		return new Point2( getActualWidth() * 0.5, getActualHeight() * 0.5 );
	}
	
	
	
	
	//
	//
	// CONTENT LEAF METHODS
	//
	//
	
	public DPContentLeaf getLeftContentLeaf()
	{
		if ( layoutNode != null )
		{
			return layoutNode.getLeftContentLeaf();
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getLeftContentLeafWithinElement( this );
		}
	}
	
	public DPContentLeaf getRightContentLeaf()
	{
		if ( layoutNode != null )
		{
			return layoutNode.getRightContentLeaf();
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getRightContentLeafWithinElement( this );
		}
	}
	
	public DPContentLeafEditable getLeftEditableContentLeaf()
	{
		if ( layoutNode != null )
		{
			return layoutNode.getLeftEditableContentLeaf();
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getLeftEditableContentLeafWithinElement( this );
		}
	}
	
	public DPContentLeafEditable getRightEditableContentLeaf()
	{
		if ( layoutNode != null )
		{
			return layoutNode.getRightEditableContentLeaf();
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getRightEditableContentLeafWithinElement( this );
		}
	}
	
	public DPContentLeaf getContentLeafToLeft()
	{
		if ( layoutNode != null )
		{
			return layoutNode.getContentLeafToLeft();
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getContentLeafToLeftOfElement( this );
		}
	}
	
	public DPContentLeaf getContentLeafToRight()
	{
		if ( layoutNode != null )
		{
			return layoutNode.getContentLeafToRight();
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getContentLeafToRightOfElement( this );
		}
	}
	
	public DPContentLeafEditable getTopOrBottomEditableContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace)
	{
		if ( layoutNode != null )
		{
			return layoutNode.getTopOrBottomEditableContentLeaf( bBottom, cursorPosInRootSpace );
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getTopOrBottomEditableContentLeafWithinElement( this, bBottom, cursorPosInRootSpace );
		}
	}



	public DPElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		if ( layoutNode != null )
		{
			return layoutNode.getLeafClosestToLocalPoint( localPos, filter );
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getLeafClosestToLocalPointWithinElement( this, localPos, filter );
		}
	}
	
	public DPElement getEditableLeafClosestToLocalPoint(Point2 localPos)
	{
		return getLeafClosestToLocalPoint( localPos, new DPContentLeafEditable.EditableLeafElementFilter() );
	}

	public DPElement getSelectableLeafClosestToLocalPoint(Point2 localPos)
	{
		return getLeafClosestToLocalPoint( localPos, new DPContentLeafEditable.SelectableLeafElementFilter() );
	}

	
	
	
	
	
	
	
	//
	//
	// MARKER METHODS
	//
	//
	
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
                        if ( parent != null )
                        {
        			leaf = parent.getContentLeafToLeftFromChild( this );
			}
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
			if ( parent != null )
			{
				leaf = parent.getContentLeafToLeftFromChild( this );
			}
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
	
	public Marker getMarkerClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		DPContentLeafEditable leaf = (DPContentLeafEditable)getLeafClosestToLocalPoint( localPos, filter );
		if ( leaf != null )
		{
			Xform2 x = leaf.getAncestorToLocalXform( this );
			
			return leaf.markerAtPoint( x.transform( localPos ) );
		}
		else
		{
			return null;
		}
	}
	
	public Marker getEditableMarkerClosestToLocalPoint(Point2 localPos)
	{
		return getMarkerClosestToLocalPoint( localPos, new DPContentLeafEditable.EditableLeafElementFilter() );
	}
	
	public Marker getSelectableMarkerClosestToLocalPoint(Point2 localPos)
	{
		return getMarkerClosestToLocalPoint( localPos, new DPContentLeafEditable.SelectableLeafElementFilter() );
	}
	
	
	
	
	//
	//
	// TREE EVENT METHODS
	//
	//
	
	public void addTreeEventListener(TreeEventListener listener)
	{
		ensureValidInteractionFields();
		interactionFields.addTreeEventListener( listener );
		notifyInteractionFieldsModified();
	}
	
	public void removeTreeEventListener(TreeEventListener listener)
	{
		if ( interactionFields != null )
		{
			interactionFields.removeTreeEventListener( listener );
			notifyInteractionFieldsModified();
		}
	}
	
	public ArrayList<TreeEventListener> getTreeEventListeners()
	{
		return interactionFields != null  ?  interactionFields.treeEventListeners  :  null;
	}

	
	
	public boolean postTreeEventToParent(Object event)
	{
		if ( parent != null )
		{
			return parent.treeEvent( new TreeEvent( this, event ) );
		}
		else
		{
			return false;
		}
	}
	
	public boolean postTreeEvent(Object event)
	{
		return treeEvent( new TreeEvent( this, event ) );
	}
	
	
	protected boolean treeEvent(TreeEvent event)
	{
		ArrayList<TreeEventListener> listeners = getTreeEventListeners();
		if ( listeners != null )
		{
			for (TreeEventListener listener: listeners)
			{
				if ( listener.onTreeEvent( this, event.sourceElement, event.value ) )
				{
					return true;
				}
			}
		}
		
		if ( parent != null )
		{
			return parent.treeEvent( event );
		}
		
		return false;
	}
	
	
	
	
	//
	//
	// TEXT REPRESENTATION METHODS
	//
	//
	
	
	public DPContentLeaf getLeafAtTextRepresentationPosition(int position)
	{
		return getRootElement().getDefaultTextRepresentationManager().getLeafAtPositionInSubtree( this, position );
	}
	
	
	public int getTextRepresentationOffsetInSubtree(DPContainer subtreeRoot)
	{
		return getRootElement().getDefaultTextRepresentationManager().getPositionOfElementInSubtree( subtreeRoot, this );
	}
	
	
	public String getTextRepresentationFromStartToMarker(Marker marker)
	{
		return getRootElement().getDefaultTextRepresentationManager().getTextRepresentationFromStartToMarker( marker, this );
	}
	
	public String getTextRepresentationFromMarkerToEnd(Marker marker)
	{
		return getRootElement().getDefaultTextRepresentationManager().getTextRepresentationFromMarkerToEnd( marker, this );
	}



	
	protected void textRepresentationChanged(Object event)
	{
		onTextRepresentationModified();
		postTreeEvent( event );
	}
	
	protected void onTextRepresentationModified()
	{
		if ( parent != null )
		{
			parent.onTextRepresentationModified();
		}
		
		invalidateCachedValues();
	}
	
	public String getTextRepresentation()
	{
		String leafTextRep = getLeafTextRepresentation();
		if ( leafTextRep != null )
		{
			return leafTextRep;
		}
		else
		{
			return getRootElement().getDefaultTextRepresentationManager().getTextRepresentationOf( this );
		}
	}
	
	public int getTextRepresentationLength()
	{
		String leafTextRep = getLeafTextRepresentation();
		if ( leafTextRep != null )
		{
			return leafTextRep.length();
		}
		else
		{
			return getTextRepresentation().length();
		}
	}
	
	
	
	protected String getLeafTextRepresentation()
	{
		return null;
	}
	
	
	
	
	
	//
	//
	// VALUE METHODS
	//
	//
	
	public Object getValue()
	{
		return getValue( valueFn );
	}
	
	public Object getValue(ElementValueFunction fn)
	{
		if ( hasFixedValue() )
		{
			return fixedValue;
		}
		else
		{
			if ( fn != null )
			{
				return fn.computeElementValue( this );
			}
			else
			{
				return getDefaultValue();
			}
		}
	}
	
	public abstract Object getDefaultValue();
	
	
	
	
	
	//
	//
	// SEQUENTIAL VISIT METHODS
	//
	//
	
	private static List<DPElement> emptyChildList = Arrays.asList();
	
	public List<DPElement> getChildrenInSequentialOrder()
	{
		return emptyChildList;
	}
	
	

	//
	//
	// STREAM VALUE METHODS
	//
	//
	
	public void addToStreamValue(StreamValueBuilder builder)
	{
	}
	
	public StreamValue getStreamValue()
	{
		SequentialStreamValueVisitor visitor = new SequentialStreamValueVisitor();
		return getStreamValue( visitor );
	}
	
	public StreamValue getStreamValue(SequentialStreamValueVisitor visitor)
	{
		return visitor.getStreamValue( this );
	}
	
	
	
	// Value function
	
	public void setValueFunction(ElementValueFunction fn)
	{
		valueFn = fn;
	}
	
	public ElementValueFunction getValueFunction()
	{
		return valueFn;
	}
	
	
	// Fixed value
	
	public void setFixedValue(Object value)
	{
		fixedValue = value;
		setFlag( FLAG_HAS_FIXED_VALUE );
	}
	
	public void clearFixedValue()
	{
		fixedValue = null;
		clearFlag( FLAG_HAS_FIXED_VALUE );
	}
	
	public void clearFixedValuesOnPathUpTo(DPElement subtreeRoot)
	{
		DPElement e = this;
		
		while ( e != subtreeRoot )
		{
			e.clearFixedValue();
			
			e = e.parent;
			
			if ( e == null )
			{
				throw new IsNotInSubtreeException();
			}
		}
	}

	public boolean hasFixedValue()
	{
		return testFlag( FLAG_HAS_FIXED_VALUE );
	}
	
	public Object getFixedValue()
	{
		if ( hasFixedValue() )
		{
			return fixedValue;
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	
	//
	//
	// POPUP METHODS
	//
	//
	
	public PresentationComponent.PresentationPopup popup(DPElement targetElement, Corner targetAnchor, Corner popupAnchor, boolean bCloseOnLoseFocus, boolean bRequestFocus)
	{
		AABox2 visibleBox = targetElement.getVisibleBoxInLocalSpace();
		Point2 targerCorner = targetAnchor.getBoxCorner( visibleBox );
		return popupOver( targetElement, targerCorner, popupAnchor, bCloseOnLoseFocus, bRequestFocus );
	}
	
	public PresentationComponent.PresentationPopup popupOver(DPElement targetElement, Point2 targetLocalPos, Corner popupAnchor, boolean bCloseOnLoseFocus, boolean bRequestFocus)
	{
		if ( targetElement.isLocalSpacePointVisible( targetLocalPos ) )
		{
			Xform2 x = targetElement.getLocalToRootXform();
			Point2 rootPos = x.transform( targetLocalPos );
			return targetElement.getRootElement().createPopupPresentation( this, rootPos, popupAnchor, bCloseOnLoseFocus, bRequestFocus );
		}
		else
		{
			return targetElement.getRootElement().createPopupAtMousePosition( this, popupAnchor, bCloseOnLoseFocus, bRequestFocus );
		}
	}
	
	public boolean isInsidePopup()
	{
		return getRootElement().getComponent().isPopup();
	}
	
	public void closeContainingPopupChain()
	{
		if ( isInsidePopup() )
		{
			getRootElement().getComponent().closeContainingPopupChain();
		}
	}
	

	
	
	//
	//
	// SEGMENT METHODS
	//
	//
	
	protected boolean isSegment()
	{
		return false;
	}
	
	public DPSegment getSegment()
	{
		return DPSegment.getSegmentOf( this );
	}
	
	
	
	//
	//
	// REGION METHODS
	//
	//
	
	protected boolean isRegion()
	{
		return false;
	}
	
	public DPRegion getRegion()
	{
		return DPRegion.getRegionOf( this );
	}
	
	
	
	//
	//
	// VALUE CACHE METHODS
	//
	//
	
	protected boolean hasCachedValues()
	{
		return testFlag( FLAG_HAS_CACHED_VALUES );
	}
	
	protected void setHasCachedValues()
	{
		setFlag( FLAG_HAS_CACHED_VALUES );
	}
	
	protected void clearHasCachedValues()
	{
		clearFlag( FLAG_HAS_CACHED_VALUES );
	}
	
	protected void invalidateCachedValues()
	{
		if ( hasCachedValues() )
		{
			getRootElement().getElementValueCacheManager().invalidateCachedValuesFor( this );
		}
	}
	
	
	
	
	//
	//
	// DEBUG NAME METHODS
	//
	//	
	
	public void setDebugName(String debugName)
	{
		this.debugName = debugName;
	}
	
	public String getDebugName()
	{
		return debugName;
	}

	
	
	
	//
	//
	// STYLE PARAMS METHODS
	//
	//
	
	public ElementStyleParams getStyleParams()
	{
		return styleParams;
	}
	
	
	
	
	
	//
	// Meta-element
	//

	protected static StyleSheet headerDebugTextStyle = StyleSheet.style( Primitive.fontBold.as( true ), Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.5f ) ) );
	protected static StyleSheet headerDescriptionTextStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.0f, 0.75f ) ) );
	protected static StyleSheet metaHeaderRowStyle = StyleSheet.style( Primitive.rowSpacing.as( 10.0 ) );
	protected static StyleSheet metaHeaderEmptyBorderStyle = StyleSheet.style( Primitive.border.as( new FilledBorder() ) );
	
	private static Color explorerHeadHoverFillPaint = new Color( 0.0f, 0.4f, 0.8f, 0.25f );
	private static Color explorerHeadHoverOutlinePaint = new Color( 0.0f, 0.4f, 0.8f, 0.5f );
	private static Stroke explorerHeadHoverStroke = new BasicStroke( 1.0f );
	
	private static ElementPainter explorerHeadHoverPainter = new ElementPainter()
	{
		public void drawBackground(DPElement element, Graphics2D graphics)
		{
		}

		public void draw(DPElement element, Graphics2D graphics)
		{
			Paint paint = graphics.getPaint();
			Stroke stroke = graphics.getStroke();

			graphics.setStroke( explorerHeadHoverStroke );
			for (Shape shape: element.getShapes())
			{
				graphics.setPaint( explorerHeadHoverFillPaint );
				graphics.fill( shape );
				graphics.setPaint( explorerHeadHoverOutlinePaint );
				graphics.draw( shape );
			}
			
			graphics.setPaint( paint );
			graphics.setStroke( stroke );
		}
	};
	
	
	
	private static void activateExplorerHighlight(PointerInputElement explorerElement)
	{
		DPElement el = (DPElement)explorerElement;
		FragmentView fragment = (FragmentView)el.getFragmentContext();
		DPElement model = (DPElement)fragment.getModel();
		model.addPainter( explorerHeadHoverPainter );
		model.queueFullRedraw();
	}
	
	private static void deactivateExplorerHighlight(PointerInputElement explorerElement)
	{
		DPElement el = (DPElement)explorerElement;
		FragmentView fragment = (FragmentView)el.getFragmentContext();
		DPElement model = (DPElement)fragment.getModel();
		model.removePainter( explorerHeadHoverPainter );
		model.queueFullRedraw();
	}
	
	
	
	private static MotionElementInteractor explorerHeaderHoverInteractor = new MotionElementInteractor()
	{
		public void pointerEnter(PointerInputElement element, PointerMotionEvent event)
		{
			if ( ( event.getModifiers() & Modifier.KEYS_MASK )  == 0 )
			{
				activateExplorerHighlight( element );
			}
		}

		public void pointerLeave(PointerInputElement element, PointerMotionEvent event)
		{
			deactivateExplorerHighlight( element );
		}
		
		@Override
		public void pointerMotion(PointerInputElement element, PointerMotionEvent event)
		{
			// Suppress highlight if any modifier keys are pressed - this is helpful for capturing an element for SVG rendering. 
			if ( ( event.getModifiers() & Modifier.KEYS_MASK )  != 0 )
			{
				deactivateExplorerHighlight( element );
			}
		}

		@Override
		public void pointerLeaveIntoChild(PointerInputElement element, PointerMotionEvent event)
		{
		}

		@Override
		public void pointerEnterFromChild(PointerInputElement element, PointerMotionEvent event)
		{
		}
	};

	private static PushElementInteractor explorerHeaderInspectInteractor = new PushElementInteractor()
	{
		public boolean buttonPress(PointerInputElement element, PointerButtonEvent event)
		{
			return event.getButton() == 1;
		}

		public void buttonRelease(PointerInputElement element, PointerButtonEvent event)
		{
			DPElement el = (DPElement)element;
			FragmentView fragment = (FragmentView)el.getFragmentContext();
			DPElement model = (DPElement)fragment.getModel();

			Pres p = inspect( model );
			p.popupAtMousePosition( el, Corner.TOP_LEFT, true, true );
		}
		
		
		private Pres inspect(DPElement model)
		{
			ElementStyleParams styleParams = model.getStyleParams();
			
			ArrayList<Object> fields = new ArrayList<Object>();
			fields.add( new HorizontalField( "H-Align", new Label( model.getHAlignment().toString() ) ) );
			fields.add( new HorizontalField( "V-Align", new Label( model.getVAlignment().toString() ) ) );
			
			Pres state = new ObjectBoxWithFields( "State", fields );
			
			Pres inspector = new Column( new Object[] { styleParams, new Spacer( 2.0, 2.0 ), state } ).alignVRefY();
			
			return DefaultPerspective.instance.applyTo( inspector );
		}
	};


	protected StyleSheet getDebugPresentationHeaderBorderStyle()
	{
		return metaHeaderEmptyBorderStyle;
	}
	
	protected void createDebugPresentationHeaderContents(ArrayList<Object> elements)
	{
		if ( debugName != null )
		{
			elements.add( headerDebugTextStyle.applyTo( new Label( "<" + debugName + ">" ) ) );
		}

		String description = toString();
		description = description.replace( "BritefuryJ.DocPresent.", "" );
		elements.add( headerDescriptionTextStyle.applyTo( new Label( description ) ) );
	}
	
	public Pres createDebugPresentationHeader()
	{
		ArrayList<Object> elements = new ArrayList<Object>();
		createDebugPresentationHeaderContents( elements );
		Pres box = metaHeaderRowStyle.applyTo( new Row( elements ).alignHPack() );
		Border border = new Border( box );
		return getDebugPresentationHeaderBorderStyle().applyTo( border.withElementInteractor( explorerHeaderHoverInteractor ).withElementInteractor( explorerHeaderInspectInteractor ) );
	}
	
	public Pres createMetaElement(FragmentView ctx, SimpleAttributeTable state)
	{
		return createDebugPresentationHeader();
	}
	
	private Pres exploreTreePresent(FragmentView ctx, SimpleAttributeTable state)
	{
		debugPresStateListeners = PresentationStateListenerList.addListener( debugPresStateListeners, ctx );
		return createMetaElement( ctx, state );
	}
	
	public ElementTreeExplorer treeExplorer()
	{
		return new ElementTreeExplorer( this );
	}
	
	
	protected void onDebugPresentationStateChanged()
	{
		debugPresStateListeners = PresentationStateListenerList.onPresentationStateChanged( debugPresStateListeners, this );
	}



	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		if ( !isRealised() )
		{
			return Pres.elementToPres( this );
		}
		else
		{
			return alreadyInUseStyle.applyTo( new Border( new Label( "Element already in use (element is realised)." ) ) );
		}
	}
	
	
	
	public void notifyExceptionDuringEventHandler(Object eventHandler, String event, Throwable e)
	{
		System.err.println( "Exception during element event handler " + eventHandler + ":" );
		e.printStackTrace();
	}


	private static final StyleSheet alreadyInUseStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.75f, 0.0f, 0.0f ) ), Primitive.border.as( new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.75f, 0.0f, 0.0f ), null ) ) );
}
