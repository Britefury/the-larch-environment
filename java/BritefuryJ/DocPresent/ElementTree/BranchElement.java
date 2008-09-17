//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.List;

import BritefuryJ.DocPresent.DPContainer;

public abstract class BranchElement extends Element
{
	protected BranchElement(DPContainer widget)
	{
		super( widget );
	}


	public DPContainer getWidget()
	{
		return (DPContainer)widget;
	}
	
	
	
	protected abstract List<Element> getChildren();
	
	
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
	
	
	
	public int getContentOffsetOfChild(Element elem)
	{
		return getWidget().getContentOffsetOfChild( elem.getWidgetAtContentStart() );
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

	public LeafElement getLeftContentLeaf()
	{
		for (Element c: getChildren())
		{
			LeafElement leaf = c.getLeftContentLeaf();
			if ( leaf != null )
			{
				return leaf;
			}
		}
		
		return null;
	}
	
	public LeafElement getRightContentLeaf()
	{
		List<Element> ch = getChildren();
		for (int i = ch.size() - 1; i >= 0; i--)
		{
			LeafElement leaf = ch.get( i ).getRightContentLeaf();
			if ( leaf != null )
			{
				return leaf;
			}
		}
		
		return null;
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
}
