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
	
	public static class LeafFilterEditable implements ElementFilter
	{
		public boolean test(Element element)
		{
			return ((LeafElement)element).isEditable();
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
	
	public List<LeafElement> getLeavesInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		Vector<LeafElement> leaves = new Vector<LeafElement>();
		if ( leafFilter == null  ||  leafFilter.test( this ) )
		{
			leaves.add( this );
		}
		return leaves;
	}
	
	public LeafElement getFirstLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
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

	public LeafElement getLastLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
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


	public LeafElement getPreviousLeaf()
	{
		return getPreviousLeaf( null, null, null );
	}

	public LeafElement getPreviousLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
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
	
	public LeafElement getNextLeaf()
	{
		return getNextLeaf( null, null, null );
	}

	public LeafElement getNextLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
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
	
	public LeafElement getPreviousEditableLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter)
	{
		return getPreviousLeaf( subtreeRootFilter, branchFilter, new LeafFilterEditable() );
	}

	public LeafElement getPreviousEditableLeaf()
	{
		return getPreviousEditableLeaf( null, null );
	}

	public LeafElement getNextEditableLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter)
	{
		return getNextLeaf( subtreeRootFilter, branchFilter, new LeafFilterEditable() );
	}

	public LeafElement getNextEditableLeaf()
	{
		return getNextEditableLeaf( null, null );
	}

	
	
	
	//
	// Content methods
	//
	
	public String getContent()
	{
		return getWidget().getContent();
	}
	
	public int getContentLength()
	{
		return getWidget().getContentLength();
	}
	
	
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




	public boolean isWhitespace()
	{
		return getWidget().isWhitespace();
	}
	
	public boolean isEditable()
	{
		return getWidget().isEditable();
	}
}
