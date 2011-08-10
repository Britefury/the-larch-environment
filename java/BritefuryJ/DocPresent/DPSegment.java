//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.LayoutTree.ArrangedSequenceLayoutNode;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.DocPresent.StyleParams.TextStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class DPSegment extends DPContainer
{
	//
	// Utility classes
	//
	
	public static class SegmentFilter implements ElementFilter
	{
		private DPSegment segment;
		
		
		public SegmentFilter(DPSegment seg)
		{
			segment = seg;
		}
		
		public boolean testElement(DPElement element)
		{
			return getSegmentOf( element ) == segment;
		}
	}

	
	
	protected TextStyleParams textStyleParams;
	protected boolean bGuardBegin, bGuardEnd;
	protected DPElement beginGuard, endGuard;
	protected DPElement child;
	protected boolean bGuardsRefreshing;
	
	
	//
	// Constructor
	//
	
	public DPSegment(boolean bGuardBegin, boolean bGuardEnd)
	{
		this( ContainerStyleParams.defaultStyleParams, TextStyleParams.defaultStyleParams, bGuardBegin, bGuardEnd );
	}

	public DPSegment(ContainerStyleParams styleParams, TextStyleParams textStyleParams, boolean bGuardBegin, boolean bGuardEnd)
	{
		super( styleParams );
		this.textStyleParams = textStyleParams;
		this.bGuardBegin = bGuardBegin;
		this.bGuardEnd = bGuardEnd;
		bGuardsRefreshing = false;
	}
	
	protected DPSegment(DPSegment element)
	{
		super( element );
		this.textStyleParams = element.textStyleParams;
		this.bGuardBegin = element.bGuardBegin;
		this.bGuardEnd = element.bGuardEnd;
		bGuardsRefreshing = false;
	}
	
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	protected void clonePostConstuct(DPElement src)
	{
		super.clonePostConstuct( src );

		DPElement child = ((DPSegment)src).getChild();
		if ( child != null )
		{
			setChild( child.clonePresentationSubtree() );
		}
	}
	
	public DPElement clonePresentationSubtree()
	{
		DPSegment clone = new DPSegment( this );
		clone.clonePostConstuct( this );
		return clone;
	}

	
	
	
	
	
	
	public void setGuardPolicy(boolean bGuardBegin, boolean bGuardEnd)
	{
		if ( bGuardBegin != this.bGuardBegin  ||  bGuardEnd != this.bGuardEnd )
		{
			this.bGuardBegin = bGuardBegin;
			this.bGuardEnd = bGuardEnd;
			refreshGuards();
		}
	}
	
	
	
	//
	// Container
	//
	
	public void setChild(DPElement child)
	{
		if ( child != this.child )
		{
			if ( this.child != null )
			{
				unregisterChild( this.child );
				registeredChildren.remove( this.child );
			}
			this.child = child;
			if ( this.child != null )
			{
				int index = beginGuard != null  ?  1  :  0;
				registeredChildren.add( index, child );
				registerChild( child );
			}
			
			queueResize();
			onChildListModified();
		}
	}
	
	public DPElement getChild()
	{
		return child;
	}

	
	protected void replaceChildWithEmpty(DPElement child)
	{
		assert child == this.child;
		setChild( null );
	}
	
	protected void replaceChild(DPElement child, DPElement replacement)
	{
		assert child == this.child;
		setChild( replacement );
	}

	
	public List<DPElement> getChildren()
	{
		if ( child != null )
		{
			DPElement[] children = { child };
			return Arrays.asList( children );
		}
		else
		{
			DPElement[] children = {};
			return Arrays.asList( children );
		}
	}

	
	public boolean isSingleElementContainer()
	{
		return true;
	}

	
	
	//
	// Collation methods
	//
	
	private void refreshGuards()
	{
		// Set the flag to indicate that the guard elements are being refreshed
		bGuardsRefreshing = true;
		boolean bBegin = false, bEnd = false;
		
		if ( child != null )
		{
			DPElement firstLeaf = child.getFirstLeafInSubtree();
			DPElement lastLeaf = child.getLastLeafInSubtree();
			
			if ( firstLeaf != null  &&  lastLeaf != null )
			{
				bBegin = getSegmentOf( firstLeaf ) != this;
				bEnd = getSegmentOf( lastLeaf ) != this;
			}
		}
		
		if ( bGuardBegin )
		{
			if ( bBegin  &&  !( beginGuard instanceof DPText ) )
			{
				unregisterBeginGuard();
				beginGuard = new DPText(textStyleParams, "" );
				registerBeginGuard();
			}
			
			if ( !bBegin  &&  !( beginGuard instanceof DPWhitespace ) )
			{
				unregisterBeginGuard();
				beginGuard = new DPWhitespace( "" );
				registerBeginGuard();
			}
		}
		else
		{
			unregisterBeginGuard();
			beginGuard = null;
		}
		
		
		if ( bGuardEnd )
		{
			if ( bEnd  &&  !( endGuard instanceof DPText ) )
			{
				unregisterEndGuard();
				endGuard = new DPText(textStyleParams, "" );
				registerEndGuard();
			}
			
			if ( !bEnd  &&  !( endGuard instanceof DPWhitespace ) )
			{
				unregisterEndGuard();
				endGuard = new DPWhitespace( "" );
				registerEndGuard();
			}
		}
		else
		{
			unregisterEndGuard();
			endGuard = null;
		}
		// Clear the flag to indicate that the guard elements are no longer being refreshed
		bGuardsRefreshing = false;
	}
	
	
	private void registerBeginGuard()
	{
		if ( beginGuard != null )
		{
			registeredChildren.add( 0, beginGuard );
			registerChild( beginGuard );
		}
	}

	private void unregisterBeginGuard()
	{
		if ( beginGuard != null )
		{
			unregisterChild( beginGuard );
			registeredChildren.remove( 0 );
		}
	}

	
	private void registerEndGuard()
	{
		if ( endGuard != null )
		{
			registeredChildren.add( endGuard );
			registerChild( endGuard );
		}
	}

	private void unregisterEndGuard()
	{
		if ( endGuard != null )
		{
			unregisterChild( endGuard );
			registeredChildren.remove( registeredChildren.size() - 1 );
		}
	}


	
	protected void onSubtreeStructureChanged()
	{
		super.onSubtreeStructureChanged();
		
		if ( !bGuardsRefreshing )
		{
			refreshGuards();
		}
	}
	
	
	
	protected AABox2 getVisibleBoxInLocalSpace()
	{
		AABox2 boxes[] = computeBoundingBoxes();
		if ( boxes.length > 0 )
		{
			AABox2 visibleBox = new AABox2();
			for (AABox2 box: boxes)
			{
				visibleBox.addBox( box );
			}
			return visibleBox;
		}
		else
		{
			return getLocalAABox();
		}
	}
	
	//
	//
	// Bounding box methods, and point query methods
	//
	//
	
	private AABox2[] computeBoundingBoxes()
	{
		ArrangedSequenceLayoutNode arrangedLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
		if ( arrangedLayout != null )
		{
			return arrangedLayout.computeBranchBoundsBoxes( this );
		}
		else
		{
			return null;
		}
	}
	
	public boolean containsParentSpacePoint(Point2 p)
	{
		AABox2 boundsBoxes[] = computeBoundingBoxes();
		if ( boundsBoxes != null )
		{
			for (AABox2 box: boundsBoxes)
			{
				if ( box.containsPoint( p ) )
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean containsLocalSpacePoint(Point2 p)
	{
		AABox2 boundsBoxes[] = computeBoundingBoxes();
		if ( boundsBoxes != null )
		{
			for (AABox2 box: boundsBoxes)
			{
				if ( box.containsPoint( p ) )
				{
					return true;
				}
			}
		}
		
		return false;
	}

	
	
	
	//
	// SEGMENT
	//
	
	protected boolean isSegment()
	{
		return true;
	}
	
	protected static DPSegment getSegmentOf(DPElement e)
	{
		while ( e != null )
		{
			if ( e.isSegment() )
			{
				return (DPSegment)e;
			}
			
			e = e.parent;
		}
		return null;
	}
}
