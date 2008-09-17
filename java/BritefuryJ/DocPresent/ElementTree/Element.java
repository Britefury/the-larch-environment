//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.List;

import BritefuryJ.DocPresent.ContentInterface;
import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.DPWidget.IsNotInSubtreeException;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;
import BritefuryJ.DocPresent.Marker.Marker;

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


	public int getContentOffsetInSubtree(BranchElement subtreeRoot)
	{
		if ( this == subtreeRoot )
		{
			return 0;
		}
		else
		{
			return parent.getChildContentOffsetInSubtree( this, subtreeRoot );
		}
	}

	public LeafElement getLeftContentLeaf()
	{
		DPContentLeaf leaf = getWidget().getLeftContentLeaf();
		
		return (LeafElement)tree.getElementForWidget( leaf );
	}
	
	public LeafElement getRightContentLeaf()
	{
		DPContentLeaf leaf = getWidget().getRightContentLeaf();
		
		return (LeafElement)tree.getElementForWidget( leaf );
	}
	
	public LeafElement getLeafAtContentPosition(int position)
	{
		return null;
	}
	
	public Element getContentLine()
	{
		if ( parent != null )
		{
			return parent.getContentLineFromChild( this );
		}
		else
		{
			return null;
		}
	}



	//
	//
	// MARKER METHODS
	//
	//
	
	public ElementMarker marker(int position, Marker.Bias bias)
	{
		return new ElementMarker( tree, getWidget().marker( position, bias ) );
	}
	
	public ElementMarker markerAtStart()
	{
		return new ElementMarker( tree, getWidget().markerAtStart() );
	}
	
	public ElementMarker markerAtEnd()
	{
		return new ElementMarker( tree, getWidget().markerAtEnd() );
	}
	
	
	public void moveMarker(ElementMarker m, int position, Marker.Bias bias)
	{
		getWidget().moveMarker( m.getWidgetMarker(), position, bias );
	}
	
	public void moveMarkerToStart(ElementMarker m)
	{
		getWidget().moveMarkerToStart( m.getWidgetMarker() );
	}
	
	public void moveMarkerToEnd(ElementMarker m)
	{
		getWidget().moveMarkerToEnd( m.getWidgetMarker() );
	}
	
	
	
	public boolean isMarkerAtStart(ElementMarker m)
	{
		return getWidget().isMarkerAtStart( m.getWidgetMarker() );
	}
	
	public boolean isMarkerAtEnd(ElementMarker m)
	{
		return getWidget().isMarkerAtEnd( m.getWidgetMarker() );
	}
}
