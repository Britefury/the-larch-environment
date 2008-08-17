package BritefuryJ.GSymViewSwing.DocLayout;

import javax.swing.text.Element;


public abstract class DocLayoutNode
{
	protected DocLayoutNodeBranch parent;
	protected DocLayout docLayout;
	protected Element element;
	
	
	
	protected void setParent(DocLayoutNodeBranch parent)
	{
		this.parent = parent;
	}
	
	protected void setDocLayout(DocLayout doc)
	{
		this.docLayout = doc;
	}
	
	
	public boolean isInSubtreeRootedAt(DocLayoutNode node)
	{
		if ( this == node )
		{
			return true;
		}
		else if ( parent != null )
		{
			return parent.isInSubtreeRootedAt( node );
		}
		else
		{
			return false;
		}
	}
	
	
	public abstract String getText();
	public abstract Element createElementSubtree(Element parent, int offset);
	
	
	protected void requestRefresh()
	{
		if ( docLayout != null )
		{
			docLayout.nodeRefreshRequest( this );
		}
	}
	
	public void setElement(Element e)
	{
		element = e;
	}
	
	protected Element getElement()
	{
		return element;
	}
}
