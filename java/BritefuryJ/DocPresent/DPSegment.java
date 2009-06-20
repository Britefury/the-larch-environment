//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.List;

import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;

public class DPSegment extends DPContainerCollateable
{
	//
	// Utility classes
	//
	
	public static class SegmentFilter implements WidgetFilter
	{
		private DPSegment segment;
		
		
		public SegmentFilter(DPSegment seg)
		{
			segment = seg;
		}
		
		public boolean testElement(DPWidget element)
		{
			return element.getSegment() == segment;
		}
	}

	
	
	protected TextStyleSheet textStyleSheet;
	protected boolean bGuardBegin, bGuardEnd;
	protected DPWidget beginGuard, endGuard;
	protected DPWidget child;
	protected boolean bGuardsRefreshing;
	
	
	//
	// Constructor
	//
	
	public DPSegment(boolean bGuardBegin, boolean bGuardEnd)
	{
		this( TextStyleSheet.defaultStyleSheet, bGuardBegin, bGuardEnd );
	}

	public DPSegment(TextStyleSheet textStyleSheet, boolean bGuardBegin, boolean bGuardEnd)
	{
		super();
		this.textStyleSheet = textStyleSheet;
		this.bGuardBegin = bGuardBegin;
		this.bGuardEnd = bGuardEnd;
		bGuardsRefreshing = false;
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
	
	public void setChild(DPWidget child)
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
				registerChild( child, null );
			}
			
			queueResize();
			onChildListModified();
		}
	}
	
	public DPWidget getChild()
	{
		return child;
	}

	
	protected void replaceChildWithEmpty(DPWidget child)
	{
		setChild( null );
	}

	
	protected List<DPWidget> getChildren()
	{
		return registeredChildren;
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
			DPWidget firstLeaf = child.getFirstLeafInSubtree();
			DPWidget lastLeaf = child.getLastLeafInSubtree();
			
			if ( firstLeaf != null  &&  lastLeaf != null )
			{
				bBegin = firstLeaf.getSegment() != this;
				bEnd = lastLeaf.getSegment() != this;
			}
		}
		
		if ( bGuardBegin )
		{
			if ( bBegin  &&  !( beginGuard instanceof DPText ) )
			{
				unregisterBeginGuard();
				beginGuard = new DPText( textStyleSheet, "" );
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
				endGuard = new DPText( textStyleSheet, "" );
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
			registerChild( beginGuard, null );
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
			registerChild( endGuard, null );
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
	
	
	
	
	public DPSegment getSegment()
	{
		return this;
	}
}
