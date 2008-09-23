//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.List;
import java.util.Vector;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public abstract class CollatableBranchElement extends BranchElement
{
	protected enum CollationMode { NONE, INDEPENDENT, INPARENT };
	
	private ContainerStyleSheet styleSheet;
	private DPContainer container;
	protected CollationMode collationMode;
	private CollatedBranchElement collationRoot;

	
	//
	// Constructor
	//
	
	protected CollatableBranchElement(ContainerStyleSheet styleSheet)
	{
		super( null );
		
		this.styleSheet = styleSheet;
		this.container = null;
		this.collationMode = CollationMode.NONE;
		this.collationRoot = null;
	}


	
	//
	// Widget
	//
	
	public DPContainer getWidget()
	{
		return getContainer();
	}
	
	protected DPContainer getContainer()
	{
		refreshContainer();
		return (DPContainer)widget;
	}
	
	protected abstract DPContainer createContainerWidget(ContainerStyleSheet styleSheet);

	
	
	//
	// Collation methods
	//
	
	protected void refreshContainer()
	{
		if ( collationMode == CollationMode.NONE )
		{
			setCollationMode( CollationMode.INDEPENDENT );
		}
	}
	
	protected void setCollationMode(CollationMode m)
	{
		collationMode = m;
		
		if ( collationMode == CollationMode.INDEPENDENT )
		{
			
			container = createContainerWidget( styleSheet );
			widget = container;
			collationRoot = null;
			if ( tree != null )
			{
				tree.registerElement( this );
			}
		}
		else if ( collationMode == CollationMode.INPARENT )
		{
			if ( tree != null )
			{
				tree.unregisterElement( this );
			}
			container = null;
			widget = null;
			collationRoot = null;
		}
		else
		{
			if ( tree != null )
			{
				tree.unregisterElement( this );
			}
			container = null;
			widget = null;
			collationRoot = null;
		}
	}
	
	
	
	protected void collateSubtree(List<Element> childElementsOut, List<CollatableBranchElement> collatedBranchesOut, BranchFilter collationFilter)
	{
		for (Element child: getChildren())
		{
			if ( child.isCollatableBranch()  &&  collationFilter.test( (BranchElement)child ) )
			{
				CollatableBranchElement b = (CollatableBranchElement)child;
				collatedBranchesOut.add( b );
				b.collateSubtree( childElementsOut, collatedBranchesOut, collationFilter );
			}
			else
			{
				childElementsOut.add( child );
			}
		}
	}
	
	protected void setCollationRoot(CollatedBranchElement b)
	{
		collationRoot = b;
	}
	
	protected void refreshCollatedContents()
	{
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
		super.onChildListChanged();
		
		if ( collationMode == CollationMode.NONE )
		{
			setCollationMode( CollationMode.INDEPENDENT );
		}
		
		if ( collationMode == CollationMode.INDEPENDENT )
		{
			// Refresh the widget contents
			refreshCollatedContents();
		}
		else if ( collationMode == CollationMode.INPARENT )
		{
			collationRoot.onCollatedSubtreeStructureChanged( this );
		}
	}
	

	
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
		return getWidget().getContentOffsetOfChild( elem.getWidgetAtContentStart() );
	}
	
	protected int getChildContentOffsetInSubtree(Element child, BranchElement subtreeRoot)
	{
		return getContentOffsetOfChild( child )  +  getContentOffsetInSubtree( subtreeRoot );
	}



	public Element getContentLineFromChild(Element element)
	{
		return getContentLine();
	}
	
	
	protected boolean onChildContentModified(Element child)
	{
		return onContentModified();
	}
	
	
	
	public DPWidget getWidgetAtContentStart()
	{
		if ( collationMode ==  CollationMode.INDEPENDENT )
		{
			return getWidget();
		}
		else
		{
			List<Element> ch = getChildren();
			if ( ch.size() > 0 )
			{
				return ch.get( 0 ).getWidgetAtContentStart();
			}
			else
			{
				return null;
			}
		}
	}



	//
	// Element type methods
	//
	
	protected boolean isCollatableBranch()
	{
		return true;
	}
}
