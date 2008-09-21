//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.List;
import java.util.Vector;

import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.WidgetContentListener;
import BritefuryJ.DocPresent.Marker.Marker;

public abstract class LeafElement extends Element implements WidgetContentListener
{
	//
	// Utility classes
	//
	
	public static class LeafFilterEditable implements LeafFilter
	{
		public boolean test(LeafElement element)
		{
			return element.isEditable();
		}
	}
	
	
	
	
	
	//
	// Constructor
	//
	
	protected LeafElement(DPContentLeaf widget)
	{
		super( widget );
		widget.setContentListener( this );
	}
	
	
	
	//
	// Widget
	//
	
	public DPContentLeaf getWidget()
	{
		return (DPContentLeaf)widget;
	}

	



	//
	// Element tree structure methods
	//
	
	public List<LeafElement> getLeavesInSubtree(BranchFilter branchFilter, LeafFilter leafFilter)
	{
		Vector<LeafElement> leaves = new Vector<LeafElement>();
		if ( leafFilter == null  ||  leafFilter.test( this ) )
		{
			leaves.add( this );
		}
		return leaves;
	}
	
	public LeafElement getFirstLeafInSubtree(BranchFilter branchFilter, LeafFilter leafFilter)
	{
		if ( leafFilter == null  ||  leafFilter.test( this ) )
		{
			return this;
		}
		else
		{
			return null;
		}
	}

	public LeafElement getLastLeafInSubtree(BranchFilter branchFilter, LeafFilter leafFilter)
	{
		if ( leafFilter == null  ||  leafFilter.test( this ) )
		{
			return this;
		}
		else
		{
			return null;
		}
	}
	
	
	public LeafElement getLeafAtContentPosition(int position)
	{
		return this;
	}


	public LeafElement getPreviousLeaf(BranchFilter subtreeRootFilter, BranchFilter branchFilter, LeafFilter leafFilter)
	{
		if ( parent != null )
		{
			return parent.getPreviousLeafFromChild( this, subtreeRootFilter, branchFilter, leafFilter );
		}
		else
		{
			return null;
		}
	}
	
	public LeafElement getNextLeaf(BranchFilter subtreeRootFilter, BranchFilter branchFilter, LeafFilter leafFilter)
	{
		if ( parent != null )
		{
			return parent.getNextLeafFromChild( this, subtreeRootFilter, branchFilter, leafFilter );
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	//
	// Content methods
	//
	
	public void contentInserted(Marker m, String x)
	{
		onContentModified();
	}

	public void contentRemoved(Marker m, int length)
	{
		onContentModified();
	}

	public void contentReplaced(Marker m, int length, String x)
	{
		onContentModified();
	}
	
	
	
	public LeafElement getEditableContentLeafToLeft()
	{
		DPContentLeaf w = getWidget().getEditableContentLeafToLeft();
		if ( w != null )
		{
			return (LeafElement)tree.getElementForWidget( w );
		}
		else
		{
			return null;
		}
	}

	public LeafElement getEditableContentLeafToRight()
	{
		DPContentLeaf w = getWidget().getEditableContentLeafToRight();
		if ( w != null )
		{
			return (LeafElement)tree.getElementForWidget( w );
		}
		else
		{
			return null;
		}
	}




	public boolean isWhitespace()
	{
		return getWidget().isWhitespace();
	}
	
	public boolean isEditable()
	{
		return getWidget().isEditable();
	}
}
