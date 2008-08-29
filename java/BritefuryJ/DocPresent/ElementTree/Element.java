package BritefuryJ.DocPresent.ElementTree;

import java.util.List;

import BritefuryJ.DocPresent.ContentInterface;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.DPWidget.CouldNotFindAncestorException;

public abstract class Element implements ContentInterface
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
	
	
	

	public boolean isInSubtreeRootedAt(BranchElement r)
	{
		Element e = this;
		
		while ( e != null  &&  e != r )
		{
			e = e.getParent();
		}
		
		return e == r;
	}
	
	
	public void getAncestry(List<Element> ancestry)
	{
		// Root to top
		if ( parent != null )
		{
			parent.getAncestry( ancestry );
		}
		
		ancestry.add( this );
	}
	
	public void getAncestryTo(BranchElement r, List<Element> ancestry)
	{
		// Root to top
		if ( r != this )
		{
			if ( parent != null )
			{
				parent.getAncestryTo( r, ancestry );
			}
			else
			{
				throw new CouldNotFindAncestorException();
			}
		}
		
		ancestry.add( this );
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
	
	
	
	public DPWidget getWidgetAtContentStart()
	{
		return getWidget();
	}
	
	public String getContent()
	{
		return getWidget().getContent();
	}
	
	public int getContentLength()
	{
		return getWidget().getContentLength();
	}
}
