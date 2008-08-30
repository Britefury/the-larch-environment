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

	
	
	protected boolean onChildContentModified(Element child)
	{
		return onContentModified();
	}
}
