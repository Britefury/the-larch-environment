//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.LSpace.LayoutTree.ArrangedSequenceLayoutNode;
import BritefuryJ.LSpace.StyleParams.CaretSlotStyleParams;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class LSSegment extends LSContainerNonOverlayed
{
	protected final static int FLAGS_SEGMENT_BEGIN = FLAGS_CONTAINERNONOVERLAYED_END;
	protected final static int FLAG_GUARD_BEGIN = FLAGS_SEGMENT_BEGIN * 0x1;
	protected final static int FLAG_GUARD_END = FLAGS_SEGMENT_BEGIN * 0x2;
	protected final static int FLAG_CARET_BOUNDARY = FLAGS_SEGMENT_BEGIN * 0x4;

	
	protected CaretSlotStyleParams caretSlotStyleParams;
	protected LSElement beginGuard, endGuard;
	protected LSElement child;
	
	
	//
	// Constructor
	//
	
	public LSSegment(ContainerStyleParams styleParams, CaretSlotStyleParams caretSlotStyleParams, boolean bGuardBegin, boolean bGuardEnd,
			 boolean caretBoundary)
	{
		this( styleParams, caretSlotStyleParams, bGuardBegin, bGuardEnd, caretBoundary, null );
	}
	
	public LSSegment(ContainerStyleParams styleParams, CaretSlotStyleParams caretSlotStyleParams, boolean bGuardBegin, boolean bGuardEnd,
			 boolean caretBoundary, LSElement child)
	{
		super( styleParams );
		this.caretSlotStyleParams = caretSlotStyleParams;
		setFlagValue( FLAG_GUARD_BEGIN, bGuardBegin );
		setFlagValue( FLAG_GUARD_END, bGuardEnd );
		setFlagValue( FLAG_CARET_BOUNDARY, caretBoundary );

		if ( child != null )
		{
			this.child = child;
			registeredChildren.add( child );
			registerChild( child );
		}
		
		refreshGuards();
	}
	
	
	
	public void setGuardPolicy(boolean bGuardBegin, boolean bGuardEnd)
	{
		if ( bGuardBegin != testFlag( FLAG_GUARD_BEGIN )  ||  bGuardEnd != testFlag( FLAG_GUARD_END ) )
		{
			setFlagValue( FLAG_GUARD_BEGIN, bGuardBegin );
			setFlagValue( FLAG_GUARD_END, bGuardEnd );
			refreshGuards();
		}
	}



	//
	// Caret boundary
	//

	public boolean isCaretBoundary() {
		return testFlag(FLAG_CARET_BOUNDARY);
	}
	
	
	//
	// Container
	//
	
	public void setChild(LSElement child)
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
	
	public LSElement getChild()
	{
		return child;
	}

	
	protected void replaceChildWithEmpty(LSElement child)
	{
		assert child == this.child;
		setChild( null );
	}
	
	protected void replaceChild(LSElement child, LSElement replacement)
	{
		assert child == this.child;
		setChild( replacement );
	}

	
	public List<LSElement> getChildren()
	{
		if ( child != null )
		{
			LSElement[] children = { child };
			return Arrays.asList( children );
		}
		else
		{
			LSElement[] children = {};
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
		if ( testFlag( FLAG_GUARD_BEGIN ) )
		{
			unregisterBeginGuard();
			beginGuard = new LSCaretSlot( caretSlotStyleParams, LSCaretSlot.SlotType.SEGMENT_BOUNDARY );
			registerBeginGuard();
		}
		else
		{
			unregisterBeginGuard();
			beginGuard = null;
		}
		
		
		if ( testFlag( FLAG_GUARD_END ) )
		{
			unregisterEndGuard();
			endGuard = new LSCaretSlot( caretSlotStyleParams, LSCaretSlot.SlotType.SEGMENT_BOUNDARY );
			registerEndGuard();
		}
		else
		{
			unregisterEndGuard();
			endGuard = null;
		}
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


	
	// For most elements, this is the bounding box. For layout-less elements, it is their bounds within the closest
	// non-layout-less parent
	@Override
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
	
	protected static LSSegment getSegmentOf(LSElement e)
	{
		while ( e != null )
		{
			if ( e.isSegment() )
			{
				return (LSSegment)e;
			}
			
			e = e.parent;
		}
		return null;
	}
}
