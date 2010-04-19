//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.ContextMenu.ContextMenu;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerNavigationEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.DocPresent.Input.DndHandler;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.Layout.ElementAlignment;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.LayoutTree.ArrangedSequenceLayoutNode;
import BritefuryJ.DocPresent.LayoutTree.LayoutNode;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StructuralRepresentation.StructuralRepresentation;
import BritefuryJ.DocPresent.StructuralRepresentation.StructuralValue;
import BritefuryJ.DocPresent.StructuralRepresentation.StructuralValueObject;
import BritefuryJ.DocPresent.StructuralRepresentation.StructuralValueSequence;
import BritefuryJ.DocPresent.StructuralRepresentation.StructuralValueStream;
import BritefuryJ.DocPresent.StyleParams.ElementStyleParams;
import BritefuryJ.DocPresent.StyleParams.HBoxStyleParams;
import BritefuryJ.DocPresent.StyleParams.TextStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;
import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;
import BritefuryJ.Utils.HashUtils;





abstract public class DPElement extends PointerInputElement
{
	protected static double NON_TYPESET_CHILD_BASELINE_OFFSET = -5.0;
	
	
	public static interface ContextMenuFactory
	{
		public void buildContextMenu(ContextMenu menu);
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
	// Padding
	//
	//
	
	private static class PaddingKey
	{
		private double leftPad, rightPad, topPad, bottomPad;
		private int hash;
		
		
		public PaddingKey(double leftPad, double rightPad, double topPad, double bottomPad)
		{
			this.leftPad = leftPad;
			this.rightPad = rightPad;
			this.topPad = topPad;
			this.bottomPad = bottomPad;
			hash = HashUtils.nHash( new int[] { new Double( leftPad ).hashCode(), new Double( rightPad ).hashCode(), new Double( topPad ).hashCode(), new Double( bottomPad ).hashCode() } );
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
				
				return leftPad == k.leftPad  &&  rightPad == k.rightPad  &&  topPad == k.topPad  &&  bottomPad == k.bottomPad;
			}
			else
			{
				return false;
			}
		}
	}
	

	private static HashMap<PaddingKey, FilledBorder> paddingBorders = new HashMap<PaddingKey, FilledBorder>();
	
	
	
	//
	//
	// INTERACTOR
	//
	//
	
	private static class InteractionFields
	{
		private ObjectDndHandler dndHandler;

		private ElementLinearRepresentationListener linearRepresentationListener;		// Move this and the next one into an 'interactor' element
		
		private ArrayList<ElementInteractor> interactors;
		private ArrayList<ContextMenuFactory> contextFactories;
		
		
		
		public InteractionFields()
		{
		}
		
		
		public void addInteractor(ElementInteractor interactor)
		{
			if ( interactors == null )
			{
				interactors = new ArrayList<ElementInteractor>();
			}
			interactors.add( interactor );
		}
		
		public void removeInteractor(ElementInteractor interactor)
		{
			if ( interactors != null )
			{
				interactors.remove( interactor );
				if ( interactors.isEmpty() )
				{
					interactors = null;
				}
			}
		}
		
		
		public void addContextMenuFactory(ContextMenuFactory contextFactory)
		{
			if ( contextFactories == null )
			{
				contextFactories = new ArrayList<ContextMenuFactory>();
			}
			contextFactories.add( contextFactory );
		}
		
		public void removeContextMenuFactory(ContextMenuFactory contextFactory)
		{
			if ( contextFactories != null )
			{
				contextFactories.remove( contextFactory );
				if ( contextFactories.isEmpty() )
				{
					contextFactories = null;
				}
			}
		}
		
		
		public boolean isIdentity()
		{
			return dndHandler == null  &&  linearRepresentationListener == null  &&  interactors == null  &&  contextFactories == null;
		}
	}

	
	
	
	//
	//
	// FIELDS
	//
	//
	
	protected final static int FLAG_REALISED = 0x1;
	protected final static int FLAG_RESIZE_QUEUED = 0x2;
	protected final static int FLAG_ALLOCATION_UP_TO_DATE = 0x4;
	protected final static int FLAG_CARET_GRABBED = 0x8;
	protected final static int FLAG_HOVER = 0x10;

	protected final static int _ALIGN_SHIFT = 5;
	protected final static int _ALIGN_MASK = ElementAlignment._ELEMENTALIGN_MASK  <<  _ALIGN_SHIFT;
	protected final static int _HALIGN_MASK = ElementAlignment._HALIGN_MASK  <<  _ALIGN_SHIFT;
	protected final static int _VALIGN_MASK = ElementAlignment._VALIGN_MASK  <<  _ALIGN_SHIFT;
	protected final static int FLAGS_ELEMENT_END = ElementAlignment._ELEMENTALIGN_END  <<  _ALIGN_SHIFT;
	
	
	protected int flags;
	
	protected ElementStyleParams styleParams;
	protected DPContainer parent;
	protected PresentationComponent.RootElement rootElement;
	
	protected LayoutNode layoutNode;
	
	private InteractionFields interactionFields;

	protected DPElement metaElement;
	protected String debugName; 
	
	protected StructuralRepresentation structuralRepresentation;
	
	
	
	
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
			FragmentContext c = w.getFragmentContext_helper();
			if ( c != null )
			{
				return c;
			}
			
			w = w.getParent();
		}
		
		return null;
	}
	
	// Override this in subclasses
	protected FragmentContext getFragmentContext_helper()
	{
		return null;
	}
	
	
	
	

	
	
	
	//
	// Alignment methods
	//
	
	public DPElement align(HAlignment hAlign, VAlignment vAlign)
	{
		setAlignmentFlags( ElementAlignment.flagValue( hAlign, vAlign ) );
		return this;
	}

	public DPElement alignH(HAlignment hAlign)
	{
		setHAlignmentFlags( ElementAlignment.flagValue( hAlign ) );
		return this;
	}
	
	public DPElement alignV(VAlignment vAlign)
	{
		setVAlignmentFlags( ElementAlignment.flagValue( vAlign ) );
		return this;
	}
	

	public DPElement alignHLeft()
	{
		return alignH( HAlignment.LEFT );
	}

	public DPElement alignHCentre()
	{
		return alignH( HAlignment.CENTRE );
	}

	public DPElement alignHRight()
	{
		return alignH( HAlignment.RIGHT );
	}

	public DPElement alignHExpand()
	{
		return alignH( HAlignment.EXPAND );
	}
	
	
	public DPElement alignVRefY()
	{
		return alignV( VAlignment.REFY );
	}

	public DPElement alignVRefYExpand()
	{
		return alignV( VAlignment.REFY_EXPAND );
	}

	public DPElement alignVTop()
	{
		return alignV( VAlignment.TOP );
	}

	public DPElement alignVCentre()
	{
		return alignV( VAlignment.CENTRE );
	}

	public DPElement alignVBottom()
	{
		return alignV( VAlignment.BOTTOM );
	}

	public DPElement alignVExpand()
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
	// Padding methods
	//
	
	public DPElement pad(double leftPad, double rightPad, double topPad, double bottomPad)
	{
		if ( leftPad == 0.0  &&  rightPad == 0.0  &&  topPad == 0.0  &&  bottomPad == 0.0 )
		{
			return this;
		}
		else
		{
			PaddingKey key = new PaddingKey( leftPad, rightPad, topPad, bottomPad );
			FilledBorder border = paddingBorders.get( key );
			
			if ( border == null )
			{
				border = new FilledBorder( leftPad, rightPad, topPad, bottomPad );
				paddingBorders.put( key, border );
			}
			
			DPBorder padElement = new DPBorder( border );
			padElement.setChild( this );
			return padElement;
		}
	}
	
	public DPElement pad(double xPad, double yPad)
	{
		return pad( xPad, xPad, yPad, yPad );
	}
	
	public DPElement padX(double xPad)
	{
		return pad( xPad, xPad, 0.0, 0.0 );
	}
	
	public DPElement padX(double leftPad, double rightPad)
	{
		return pad( leftPad, rightPad, 0.0, 0.0 );
	}
	
	public DPElement padY(double yPad)
	{
		return pad( 0.0, 0.0, yPad, yPad );
	}
	
	public DPElement padY(double topPad, double bottomPad)
	{
		return pad( 0.0, 0.0, topPad, bottomPad );
	}
	

	
	
	
	
	//
	// Geometry methods
	//
	
	public Point2 getPositionInParentSpace()
	{
		return layoutNode != null  ?  layoutNode.getPositionInParentSpace()  :  new Point2();
	}
	
	public Point2 getPositionInParentAllocationSpace()
	{
		return layoutNode != null  ?  layoutNode.getPositionInParentAllocationSpace()  :  new Point2();
	}
	
	public double getPositionInParentAllocationSpaceX()
	{
		return layoutNode != null  ?  layoutNode.getAllocPositionInParentAllocationSpaceX()  :  0.0;
	}
	
	public double getPositionInParentAllocationSpaceY()
	{
		return layoutNode != null  ?  layoutNode.getAllocPositionInParentAllocationSpaceY()  :  0.0;
	}
	
	public double getAllocationX()
	{
		return layoutNode != null  ?  layoutNode.getAllocationX()  :  parent.getAllocationX();
	}
	
	public double getAllocationY()
	{
		return layoutNode != null  ?  layoutNode.getAllocationY()  :  parent.getAllocationY();
	}
	
	public LAllocV getAllocV()
	{
		return layoutNode != null  ?  layoutNode.getAllocV()  :  parent.getAllocV();
	}
	
	public Vector2 getAllocation()
	{
		return layoutNode != null  ?  layoutNode.getAllocation()  :  parent.getAllocation();
	}
	
	public double getAllocationInParentSpaceX()
	{
		return layoutNode != null  ?  layoutNode.getAllocationInParentSpaceX()  :  parent.getAllocationInParentSpaceX();
	}
	
	public double getAllocationInParentSpaceY()
	{
		return layoutNode != null  ?  layoutNode.getAllocationInParentSpaceY()  :  parent.getAllocationInParentSpaceY();
	}
	
	public Vector2 getAllocationInParentSpace()
	{
		return layoutNode != null  ?  layoutNode.getAllocationInParentSpace()  :  parent.getAllocationInParentSpace();
	}
	
	
	public AABox2 getLocalAABox()
	{
		return new AABox2( new Point2(), getAllocation() );
	}
	
	public AABox2 getLocalClipBox()
	{
		return null;
	}
	
	public AABox2 getAABoxInParentAllocationSpace()
	{
		return new AABox2( getPositionInParentAllocationSpace(), getAllocation() );
	}
	
	public AABox2 getAABoxInParentSpace()
	{
		return getParentAllocationToParentSpaceXform().transform( getAABoxInParentAllocationSpace() );
	}
	
	
	protected Shape[] getShapes()
	{
		Vector2 alloc = getAllocation();
		return new Shape[] { new Rectangle2D.Double( 0.0, 0.0, alloc.x, alloc.y ) };
	}

	
	public Xform2 getParentAllocationToParentSpaceXform()
	{
		return parent != null  ?  parent.getAllocationSpaceToLocalSpaceXform( this )  :  Xform2.identity;
	}
	
	public Xform2 getLocalToParentXform()
	{
		return new Xform2( getPositionInParentAllocationSpace().toVector2() ).concat( getParentAllocationToParentSpaceXform() );
	}
	
	public Xform2 getParentToLocalXform()
	{
		return getLocalToParentXform().inverse();
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

		return null;
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

		return null;
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
	// SELECTION METHODS
	//
	//
	
	protected void drawSubtreeSelection(Graphics2D graphics, Marker startMarker, List<DPElement> startPath, Marker endMarker, List<DPElement> endPath)
	{
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
	
	
	protected void clip(Graphics2D graphics)
	{
		graphics.clip( new Rectangle2D.Double( 0.0, 0.0, getAllocationX(), getAllocationY() ) );
	}

	
	protected void queueResize()
	{
		LayoutNode layout = getValidLayoutNode();
		if ( layout != null )
		{
			layout.queueResize();
		}
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
	}
	
	protected void handleUnrealise(DPElement unrealiseRoot)
	{
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
		onUnrealise( unrealiseRoot );
		clearFlagRealised();
	}
	
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
	
	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
		drawBackground( graphics );
		List<ElementInteractor> interactors = getInteractors();
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				interactor.drawBackground( this, graphics );
			}
		}
	}
	
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		draw( graphics );
		List<ElementInteractor> interactors = getInteractors();
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				interactor.draw( this, graphics );
			}
		}
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
	

	protected boolean handlePointerButtonDown(PointerButtonEvent event)
	{
		if ( onButtonDown( event ) )
		{
			return true;
		}
		List<ElementInteractor> interactors = getInteractors();
		boolean bResult = false;
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				bResult = bResult || interactor.onButtonDown( this, event );
			}
		}
		return bResult;
	}
	
	protected boolean handlePointerButtonDown2(PointerButtonEvent event)
	{
		if ( onButtonDown2( event ) )
		{
			return true;
		}
		List<ElementInteractor> interactors = getInteractors();
		boolean bResult = false;
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				bResult = bResult || interactor.onButtonDown2( this, event );
			}
		}
		return bResult;
	}
	
	protected boolean handlePointerButtonDown3(PointerButtonEvent event)
	{
		if ( onButtonDown3( event ) )
		{
			return true;
		}
		List<ElementInteractor> interactors = getInteractors();
		boolean bResult = false;
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				bResult = bResult || interactor.onButtonDown3( this, event );
			}
		}
		return bResult;
	}
	
	protected boolean handlePointerButtonUp(PointerButtonEvent event)
	{
		if ( onButtonUp( event ) )
		{
			return true;
		}
		List<ElementInteractor> interactors = getInteractors();
		boolean bResult = false;
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				bResult = bResult || interactor.onButtonUp( this, event );
			}
		}
		return bResult;
	}
	
	protected boolean handlePointerContextButton(ContextMenu menu)
	{
		List<ContextMenuFactory> contextFactories = getContextMenuFactories();
		boolean bResult = false;
		if ( contextFactories != null )
		{
			for (ContextMenuFactory contextFactory: contextFactories)
			{
				contextFactory.buildContextMenu( menu );
				bResult = true;
			}
		}
		return bResult;
	}
	
	protected void handlePointerMotion(PointerMotionEvent event)
	{
		onMotion( event );
		List<ElementInteractor> interactors = getInteractors();
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				interactor.onMotion( this, event );
			}
		}
	}
	
	protected void handlePointerDrag(PointerMotionEvent event)
	{
		onDrag( event );
		List<ElementInteractor> interactors = getInteractors();
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				interactor.onDrag( this, event );
			}
		}
	}
	
	protected void handlePointerEnter(PointerMotionEvent event)
	{
		handleHover();
			
		Cursor cursor = getCursor();
		if ( cursor != null  &&  rootElement != null )
		{
			rootElement.setPointerCursor( cursor );
		}
		onEnter( event );
		List<ElementInteractor> interactors = getInteractors();
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				interactor.onEnter( this, event );
			}
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
		onLeave( event );
		List<ElementInteractor> interactors = getInteractors();
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				interactor.onLeave( this, event );
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
		if ( onScroll( event ) )
		{
			return true;
		}
		List<ElementInteractor> interactors = getInteractors();
		boolean bResult = false;
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				bResult = bResult || interactor.onScroll( this, event );
			}
		}
		return bResult;
	}
	
	
	protected boolean handlePointerNavigationGestureBegin(PointerButtonEvent event)
	{
		return false;
	}
	
	protected boolean handlePointerNavigationGestureEnd(PointerButtonEvent event)
	{
		return false;
	}

	protected boolean handlePointerNavigationGesture(PointerNavigationEvent event)
	{
		return false;
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
		List<ElementInteractor> interactors = getInteractors();
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				interactor.onCaretEnter( this, c );
			}
		}
	}
	
	protected void handleCaretLeave(Caret c)
	{
		onCaretLeave( c );
		List<ElementInteractor> interactors = getInteractors();
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				interactor.onCaretLeave( this, c );
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
	
	public void addDragSource(Class<?> dataType, ObjectDndHandler.SourceDataFn sourceDataFn, ObjectDndHandler.ExportDoneFn exportDoneFn)
	{
		ObjectDndHandler current = getValidInitialDndHandler();
		ensureValidInteractionFields();
		interactionFields.dndHandler = current.withDragSource( new ObjectDndHandler.DragSource( dataType, sourceDataFn, exportDoneFn ) ); 
		notifyInteractionFieldsModified();
	}
	
	public void addDragSource(Class<?> dataType, ObjectDndHandler.SourceDataFn sourceDataFn)
	{
		addDragSource( dataType, sourceDataFn, null );
	}
	
	
	public void addDropDest(Class<?> dataType, ObjectDndHandler.CanDropFn canDropFn, ObjectDndHandler.DropFn dropFn)
	{
		ObjectDndHandler current = getValidInitialDndHandler();
		ensureValidInteractionFields();
		interactionFields.dndHandler = current.withDropDest( new ObjectDndHandler.DropDest( dataType, canDropFn, dropFn ) );
		notifyInteractionFieldsModified();
	}
	
	public void addDropDest(Class<?> dataType, ObjectDndHandler.DropFn dropFn)
	{
		addDropDest( dataType, null, dropFn );
	}
	
	
	public void addNonLocalDropDest(DataFlavor dataFlavor, ObjectDndHandler.DropFn dropFn)
	{
		ObjectDndHandler current = getValidInitialDndHandler();
		ensureValidInteractionFields();
		interactionFields.dndHandler = current.withNonLocalDropDest( new ObjectDndHandler.NonLocalDropDest( dataFlavor, dropFn ) );
		notifyInteractionFieldsModified();
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
	
	
	
	public PointerInputElement getDndElement(Point2 localPos, Point2 targetPos[])
	{
		if ( getDndHandler() != null )
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
		return interactionFields != null  ?  interactionFields.dndHandler  :  null;
	}
	
	
	
	
	//
	//
	// INTERACTOR METHODS
	//
	//
	
	public void addInteractor(ElementInteractor interactor)
	{
		ensureValidInteractionFields();
		interactionFields.addInteractor( interactor );
		notifyInteractionFieldsModified();
	}
	
	public void removeInteractor(ElementInteractor interactor)
	{
		if ( interactionFields != null )
		{
			interactionFields.removeInteractor( interactor );
			notifyInteractionFieldsModified();
		}
	}
	
	public ArrayList<ElementInteractor> getInteractors()
	{
		return interactionFields != null  ?  interactionFields.interactors  :  null;
	}
	
	
	
	
	//
	//
	// CONTEXT MENU FACTORY METHODS
	//
	//
	
	public void addContextMenuFactory(ContextMenuFactory contextFactory)
	{
		ensureValidInteractionFields();
		interactionFields.addContextMenuFactory( contextFactory );
		notifyInteractionFieldsModified();
	}
	
	public void removeContextMenuFactory(ContextMenuFactory contextFactory)
	{
		if ( interactionFields != null )
		{
			interactionFields.removeContextMenuFactory( contextFactory );
			notifyInteractionFieldsModified();
		}
	}
	
	public ArrayList<ContextMenuFactory> getContextMenuFactories()
	{
		return interactionFields != null  ?  interactionFields.contextFactories  :  null;
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
	
	protected boolean handleMotionKeyPress(KeyEvent keyEvent, int modifiers)
	{
		return false;
	}
	
	protected List<DPElement> horizontalNavigationList()
	{
		if ( layoutNode != null )
		{
			return layoutNode.horizontalNavigationList();
		}
		else
		{
			DPContainer p = parent;
			while ( p != null )
			{
				LayoutNode parentLayoutNode = p.getLayoutNode();
				if ( parentLayoutNode != null )
				{
					return parentLayoutNode.horizontalNavigationList();
				}
				p = p.getParent();
			}
			return null;
		}
	}
	
	protected List<DPElement> verticalNavigationList()
	{
		if ( layoutNode != null )
		{
			return layoutNode.verticalNavigationList();
		}
		else
		{
			DPContainer p = parent;
			while ( p != null )
			{
				LayoutNode parentLayoutNode = p.getLayoutNode();
				if ( parentLayoutNode != null )
				{
					return parentLayoutNode.verticalNavigationList();
				}
				p = p.getParent();
			}
			return null;
		}
	}
	
	public Point2 getMarkerPosition(Marker marker)
	{
		if ( marker.getElement() != this )
		{
			throw new RuntimeException( "Marker is not within the bounds of this element" );
		}
		return new Point2( getAllocationX() * 0.5, getAllocationY() * 0.5 );
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



	protected DPElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
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
	
	
	
	//
	//
	// LISTENER METHODS
	//
	//
	
	
	public ElementLinearRepresentationListener getLinearRepresentationListener()
	{
		return interactionFields != null  ?  interactionFields.linearRepresentationListener  :  null;
	}
	
	public void setLinearRepresentationListener(ElementLinearRepresentationListener listener)
	{
		ensureValidInteractionFields();
		interactionFields.linearRepresentationListener = listener;
		notifyInteractionFieldsModified();
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

	protected abstract void getTextRepresentationFromStartToPath(StringBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex);
	protected abstract void getTextRepresentationFromPathToEnd(StringBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex);



	
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
		ElementLinearRepresentationListener linearRepresentationListener = getLinearRepresentationListener();
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
	
	public DPElement getElementAtTextRepresentationStart()
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

	protected abstract void getLinearRepresentationFromStartToPath(ItemStreamBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex);
	protected abstract void getLinearRepresentationFromPathToEnd(ItemStreamBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex);



	
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
		ElementLinearRepresentationListener linearRepresentationListener = getLinearRepresentationListener();
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
	
	public void clearStructuralRepresentationUpTo(DPElement subtreeRoot)
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

	// Sent directly to caret leaf, from root element, hence pass on to parent
	protected boolean onKeyPress(KeyEvent event)
	{
		boolean bHandled = false;
		List<ElementInteractor> interactors = getInteractors();
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				bHandled = bHandled  ||  interactor.onKeyPress( this, event );
			}
		}
		
		if ( bHandled )
		{
			return true;
		}

		if ( parent != null )
		{
			return parent.onKeyPress( event );
		}
		
		return false;
	}

	// Sent directly to caret leaf, from root element, hence pass on to parent
	protected boolean onKeyRelease(KeyEvent event)
	{
		boolean bHandled = false;
		List<ElementInteractor> interactors = getInteractors();
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				bHandled = bHandled  ||  interactor.onKeyRelease( this, event );
			}
		}
		
		if ( bHandled )
		{
			return true;
		}

		if ( parent != null )
		{
			return parent.onKeyRelease( event );
		}
		
		return false;
	}

	// Sent directly to caret leaf, from root element, hence pass on to parent
	protected boolean onKeyTyped(KeyEvent event)
	{
		boolean bHandled = false;
		List<ElementInteractor> interactors = getInteractors();
		if ( interactors != null )
		{
			for (ElementInteractor interactor: interactors)
			{
				bHandled = bHandled  ||  interactor.onKeyTyped( this, event );
			}
		}
		
		if ( bHandled )
		{
			return true;
		}

		if ( parent != null )
		{
			return parent.onKeyTyped( event );
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
	// REGION METHODS
	//
	//
	
	public DPRegion getRegion()
	{
		if ( parent != null )
		{
			return parent.getRegion();
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	
	
	//
	// Meta-element
	//
	
	protected static TextStyleParams headerDebugTextStyle = new TextStyleParams( null, null, null, true, new Font( "Sans serif", Font.BOLD, 14 ), new Color( 0.0f, 0.5f, 0.5f ), null, null, false );
	protected static TextStyleParams headerDescriptionTextStyle = new TextStyleParams( null, null, null, true, new Font( "Sans serif", Font.PLAIN, 14 ), new Color( 0.0f, 0.0f, 0.75f ), null, null, false );
	protected static HBoxStyleParams metaHeaderHBoxStyle = new HBoxStyleParams( null, null, null, 10.0 );
	protected static FilledBorder metaHeaderEmptyBorder = new FilledBorder();


	public DPElement createMetaHeaderData()
	{
		return null;
	}
	
	public DPElement createMetaHeaderDebug()
	{
		if ( debugName != null )
		{
			return new DPText( headerDebugTextStyle, "<" + debugName + ">" );
		}
		else
		{
			return null;
		}
	}
	
	public DPElement createMetaDescription()
	{
		String description = toString();
		description = description.replace( "BritefuryJ.DocPresent.", "" );
		return new DPText( headerDescriptionTextStyle, description );
	}
	
	protected Border getMetaHeaderBorder()
	{
		return metaHeaderEmptyBorder;
	}
	
	public DPElement createMetaHeader()
	{
		DPHBox hbox = new DPHBox( metaHeaderHBoxStyle );
		DPElement data = createMetaHeaderData();
		DPElement debug = createMetaHeaderDebug();
		DPElement descr = createMetaDescription();
		if ( data != null )
		{
			hbox.append( data );
		}
		if ( debug != null )
		{
			hbox.append( debug );
		}
		hbox.append( descr );
		

		DPBorder border = new DPBorder( getMetaHeaderBorder() );
		border.setChild( hbox );
		return border;
	}
	
	public DPBorder getMetaHeaderBorderElement()
	{
		if ( metaElement != null )
		{
			DPBox bin = (DPBox)metaElement;
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
			DPBorder border = getMetaHeaderBorderElement();
			DPHBox hbox = (DPHBox)border.getChild();
			
			DPElement data = createMetaHeaderData();
			DPElement debug = createMetaHeaderDebug();
			DPElement descr = createMetaDescription();
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

	public DPElement createMetaElement()
	{
		DPBox bin = new DPBox( );
		bin.setChild( createMetaHeader() );
		return bin;
	}
	
	public DPElement initialiseMetaElement()
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
	
	public DPElement getMetaElement()
	{
		return metaElement;
	}
	
	
	
	
	public void setDebugName(String debugName)
	{
		this.debugName = debugName;
	}
	
	public String getDebugName()
	{
		return debugName;
	}

	
	
	
	public ElementStyleParams getStyleParams()
	{
		return styleParams;
	}
}
