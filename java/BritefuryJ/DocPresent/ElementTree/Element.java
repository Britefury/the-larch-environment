package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.ContentInterface;
import BritefuryJ.DocPresent.DPWidget;

public abstract class Element
{
	protected DPWidget widget;
	protected BranchElement parent;
	protected ElementTree tree;
	protected ElementContentListener contentListener;
	
	
	protected Element(DPWidget widget)
	{
		this.widget = widget;
		parent = null;
		tree = null;
		contentListener = null;
	}
	
	
	public DPWidget getWidget()
	{
		return widget;
	}
	
	
	
	public void setContentListener(ElementContentListener listener)
	{
		contentListener = listener;
	}
	
	
	
	public ContentInterface getContentInterface()
	{
		if ( widget != null )
		{
			return widget.getContentInterface();
		}
		return null;
	}
	
	
	protected void setParent(BranchElement parent)
	{
		this.parent = parent;
	}
	
	protected void setElementTree(ElementTree tree)
	{
		if ( tree != this.tree )
		{
			if ( this.tree != null )
			{
				this.tree.unregisterElement( this );
			}
			
			this.tree = tree;

			if ( this.tree != null )
			{
				this.tree.registerElement( this );
			}
		}
	}
	
	
	public Element getParent()
	{
		return parent;
	}

	public ElementTree getElementTree()
	{
		return tree;
	}
	
	
	
	protected boolean isParagraph()
	{
		return false;
	}
	
	
	
	
	
	protected boolean onContentModified()
	{
		if ( contentListener != null )
		{
			if ( contentListener.contentModified( this ) )
			{
				return true;
			}
		}
		
		if ( parent != null )
		{
			return parent.onChildContentModified( this );
		}
		
		return false;
	}
	
	
	
	public String getContent()
	{
		return "";
	}
	
	public int getContentLength()
	{
		return 0;
	}
}
