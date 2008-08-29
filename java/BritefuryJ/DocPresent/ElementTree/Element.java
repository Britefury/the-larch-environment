package BritefuryJ.DocPresent.ElementTree;

import java.util.List;

import BritefuryJ.DocPresent.ContentInterface;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.DPWidget.IsNotInSubtreeException;

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
	
	
	public void getElementPathToRoot(List<Element> path)
	{
		// Root to top
		if ( parent != null )
		{
			parent.getElementPathToRoot( path );
		}
		
		path.add( this );
	}
	
	public void getElementPathToSubtreeRoot(BranchElement subtreeRoot, List<Element> path)
	{
		// Root to top
		if ( subtreeRoot != this )
		{
			if ( parent != null )
			{
				parent.getElementPathToSubtreeRoot( subtreeRoot, path );
			}
			else
			{
				throw new IsNotInSubtreeException();
			}
		}
		
		path.add( this );
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
