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

import BritefuryJ.DocPresent.DPContainer;

public abstract class BranchElement extends Element
{
	//
	// Constructor
	//
	
	protected BranchElement(DPContainer widget)
	{
		super( widget );
	}


	
	//
	// Widget
	//
	
	public DPContainer getWidget()
	{
		return (DPContainer)widget;
	}
	
	
	
	//
	// Element tree and parent methods
	//
	
	protected void setElementTree(ElementTree tree)
	{
		if ( tree != this.tree )
		{
			super.setElementTree( tree );
			
			for (Element c: getChildren())
			{
				c.setElementTree( tree );
			}
		}
	}
	

	
	//
	// Element tree structure methods
	//
	
	protected void onChildListChanged()
	{
		onSubtreeStructureChanged();
	}
	
	protected void onSubtreeStructureChanged()
	{
		if ( parent != null )
		{
			parent.onSubtreeStructureChanged();
		}
	}

	public abstract List<Element> getChildren();
	

	
	public List<LeafElement> getLeavesInSubtree(BranchFilter branchFilter, LeafFilter leafFilter)
	{
		Vector<LeafElement> leaves = new Vector<LeafElement>();

		if ( branchFilter == null  ||  branchFilter.test( this ) )
		{
			for (Element ch: getChildren())
			{
				leaves.addAll( ch.getLeavesInSubtree( branchFilter, leafFilter ) );
			}
		}

		return leaves;
	}
	
	public LeafElement getFirstLeafInSubtree(BranchFilter branchFilter, LeafFilter leafFilter)
	{
		if ( branchFilter == null  ||  branchFilter.test( this ) )
		{
			for (Element child: getChildren())
			{
				LeafElement leaf = child.getFirstLeafInSubtree( branchFilter, leafFilter );
				if ( leaf != null )
				{
					return leaf;
				}
			}
			return null;
		}
		else
		{
			return null;
		}
	}

	public LeafElement getLastLeafInSubtree(BranchFilter branchFilter, LeafFilter leafFilter)
	{
		if ( branchFilter == null  ||  branchFilter.test( this ) )
		{
			List<Element> children = getChildren();
			for (int i = children.size() - 1; i >= 0; i--)
			{
				LeafElement leaf = children.get( i ).getLastLeafInSubtree( branchFilter, leafFilter );
				if ( leaf != null )
				{
					return leaf;
				}
			}
			return null;
		}
		else
		{
			return null;
		}
	}
	
	
	public LeafElement getLeafAtContentPosition(int position)
	{
		Element c = getChildAtContentPosition( position );
		
		if ( c != null )
		{
			return c.getLeafAtContentPosition( position - getContentOffsetOfChild( c ) );
		}
		else
		{
			return null;
		}
	}

	public Element getChildAtContentPosition(int position)
	{
		int offset = 0;
		for (Element c: getChildren())
		{
			int end = offset + c.getContentLength();
			if ( position >= offset  &&  position < end )
			{
				return c;
			}
			offset = end;
		}
		
		return null;
	}

	
	protected LeafElement getPreviousLeafFromChild(Element child, BranchFilter subtreeRootFilter, BranchFilter branchFilter, LeafFilter leafFilter)
	{
		if ( subtreeRootFilter == null  ||  subtreeRootFilter.test( this ) )
		{
			List<Element> children = getChildren();
			int index = children.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index - 1; i >= 0; i--)
				{
					Element e = children.get( i );
					LeafElement l = e.getLastLeafInSubtree( branchFilter, leafFilter );
					if ( l != null )
					{
						return l;
					}
				}
			}
			
			if ( parent != null )
			{
				return parent.getPreviousLeafFromChild( this, subtreeRootFilter, branchFilter, leafFilter );
			}
		}
		
		return null;
	}
	
	protected LeafElement getNextLeafFromChild(Element child, BranchFilter subtreeRootFilter, BranchFilter branchFilter, LeafFilter leafFilter)
	{
		if ( subtreeRootFilter == null  ||  subtreeRootFilter.test( this ) )
		{
			List<Element> children = getChildren();
			int index = children.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index + 1; i < children.size(); i++)
				{
					Element e = children.get( i );
					LeafElement l = e.getFirstLeafInSubtree( branchFilter, leafFilter );
					if ( l != null )
					{
						return l;
					}
				}
			}
		
			if ( parent != null )
			{
				return parent.getNextLeafFromChild( this, subtreeRootFilter, branchFilter, leafFilter );
			}
		}

		return null;
	}
	

	
	public int getContentOffsetOfChild(Element elem)
	{
		int offset = 0;
		for (Element c: getChildren())
		{
			if ( c == elem )
			{
				return offset;
			}
			offset += c.getContentLength();
		}
		
		throw new DPContainer.CouldNotFindChildException();
	}
	
	protected int getChildContentOffsetInSubtree(Element child, BranchElement subtreeRoot)
	{
		return getContentOffsetOfChild( child )  +  getContentOffsetInSubtree( subtreeRoot );
	}



	public SegmentElement getLinearTextSectionFromChild(Element element)
	{
		return getSegment();
	}
	
	
	protected boolean onChildContentModified(Element child)
	{
		return onContentModified();
	}



	//
	// Element type methods
	//
	
	protected boolean isBranch()
	{
		return true;
	}
}
