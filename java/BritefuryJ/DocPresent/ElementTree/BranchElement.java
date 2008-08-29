package BritefuryJ.DocPresent.ElementTree;

import java.util.List;
import java.util.Vector;

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
		return getWidget().getContentOffsetOfChild( getWidgetAtContentStart() );
	}
	
	public int getContentOffsetOfDescendent(Element descendent)
	{
		Vector<Element> path = new Vector<Element>();
		descendent.getElementPathToSubtreeRoot( this, path );
		int offset = 0;
		for (int i = 0; i < path.size() - 1; i++)
		{
			BranchElement parent = (BranchElement)path.get( i );
			Element child = path.get( i + 1 );
			offset += parent.getContentOffsetOfChild( child );
		}
		return offset;
	}

	
	protected boolean onChildContentModified(Element child)
	{
		return onContentModified();
	}
}
