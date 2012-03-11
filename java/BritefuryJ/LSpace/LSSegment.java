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
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.LSpace.StyleParams.TextStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class LSSegment extends LSContainerNonOverlayed
{
	//
	// Utility classes
	//
	
	public static class SegmentFilter implements ElementFilter
	{
		private LSSegment segment;
		
		
		public SegmentFilter(LSSegment seg)
		{
			segment = seg;
		}
		
		public boolean testElement(LSElement element)
		{
			return getSegmentOf( element ) == segment;
		}
	}

	
	
	protected final static int FLAGS_SEGMENT_BEGIN = FLAGS_CONTAINERNONOVERLAYED_END;
	protected final static int FLAG_GUARD_BEGIN = FLAGS_SEGMENT_BEGIN * 0x1;
	protected final static int FLAG_GUARD_END = FLAGS_SEGMENT_BEGIN * 0x2;
	protected final static int FLAG_GUARDS_REFRESHING = FLAGS_SEGMENT_BEGIN * 0x4;

	
	protected TextStyleParams textStyleParams;
	protected LSElement beginGuard, endGuard;
	protected LSElement child;
	
	
	//
	// Constructor
	//
	
	public LSSegment(boolean bGuardBegin, boolean bGuardEnd)
	{
		this( ContainerStyleParams.defaultStyleParams, TextStyleParams.defaultStyleParams, bGuardBegin, bGuardEnd );
	}

	public LSSegment(ContainerStyleParams styleParams, TextStyleParams textStyleParams, boolean bGuardBegin, boolean bGuardEnd)
	{
		super( styleParams );
		this.textStyleParams = textStyleParams;
		setFlagValue( FLAG_GUARD_BEGIN, bGuardBegin );
		setFlagValue( FLAG_GUARD_END, bGuardEnd );
		clearFlag( FLAG_GUARDS_REFRESHING );
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
		// Set the flag to indicate that the guard elements are being refreshed
		setFlag( FLAG_GUARDS_REFRESHING );
		boolean bBegin = false, bEnd = false;
		
		if ( child != null )
		{
			LSElement firstLeaf = child.getFirstLeafInSubtree();
			LSElement lastLeaf = child.getLastLeafInSubtree();
			
			if ( firstLeaf != null  &&  lastLeaf != null )
			{
				bBegin = getSegmentOf( firstLeaf ) != this;
				bEnd = getSegmentOf( lastLeaf ) != this;
			}
		}
		
		if ( testFlag( FLAG_GUARD_BEGIN ) )
		{
			if ( bBegin  &&  !( beginGuard instanceof LSText ) )
			{
				unregisterBeginGuard();
				beginGuard = new LSText(textStyleParams, "" );
				registerBeginGuard();
			}
			
			if ( !bBegin  &&  !( beginGuard instanceof LSWhitespace ) )
			{
				unregisterBeginGuard();
				beginGuard = new LSWhitespace( "" );
				registerBeginGuard();
			}
		}
		else
		{
			unregisterBeginGuard();
			beginGuard = null;
		}
		
		
		if ( testFlag( FLAG_GUARD_END ) )
		{
			if ( bEnd  &&  !( endGuard instanceof LSText ) )
			{
				unregisterEndGuard();
				endGuard = new LSText(textStyleParams, "" );
				registerEndGuard();
			}
			
			if ( !bEnd  &&  !( endGuard instanceof LSWhitespace ) )
			{
				unregisterEndGuard();
				endGuard = new LSWhitespace( "" );
				registerEndGuard();
			}
		}
		else
		{
			unregisterEndGuard();
			endGuard = null;
		}
		// Clear the flag to indicate that the guard elements are no longer being refreshed
		clearFlag( FLAG_GUARDS_REFRESHING );
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
		
		if ( !testFlag( FLAG_GUARDS_REFRESHING ) )
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