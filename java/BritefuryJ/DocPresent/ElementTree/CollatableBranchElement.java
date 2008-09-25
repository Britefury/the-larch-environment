//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.List;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public abstract class CollatableBranchElement extends BranchElement
{
	protected enum CollationMode { UNINITIALISED, ROOT, CONTENTSCOLLATED };
	
	private ContainerStyleSheet styleSheet;
	private DPContainer container;
	protected CollationMode collationMode;
	private ElementCollator collationRootCollator;

	
	//
	// Constructor
	//
	
	protected CollatableBranchElement(ContainerStyleSheet styleSheet)
	{
		super( null );
		
		this.styleSheet = styleSheet;
		this.container = null;
		this.collationMode = CollationMode.UNINITIALISED;
		this.collationRootCollator = null;
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
		if ( collationMode == CollationMode.UNINITIALISED )
		{
			setCollationMode( CollationMode.ROOT );
		}
	}
	
	protected void setCollationMode(CollationMode m)
	{
		collationMode = m;
		
		if ( collationMode == CollationMode.ROOT )
		{
			container = createContainerWidget( styleSheet );
			widget = container;
			collationRootCollator = null;
			if ( tree != null )
			{
				tree.registerElement( this );
			}
		}
		else if ( collationMode == CollationMode.CONTENTSCOLLATED )
		{
			if ( tree != null )
			{
				tree.unregisterElement( this );
			}
			container = null;
			widget = null;
			collationRootCollator = null;
		}
		else
		{
			if ( tree != null )
			{
				tree.unregisterElement( this );
			}
			container = null;
			widget = null;
			collationRootCollator = null;
		}
	}
	
	
	
	protected void collateSubtree(List<Element> childElementsOut, List<CollatableBranchElement> collatedBranchesOut, CollatableBranchFilter collationFilter)
	{
		for (Element child: getChildren())
		{
			if ( child.isCollatableBranch()  &&  collationFilter.test( (CollatableBranchElement)child ) )
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
	
	protected void setCollator(ElementCollator c)
	{
		collationRootCollator = c;
	}
	
	protected void refreshContainerWidgetContents()
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
		if ( collationMode == CollationMode.UNINITIALISED )
		{
			setCollationMode( CollationMode.ROOT );
		}
		
		if ( collationMode == CollationMode.ROOT )
		{
			refreshContainerWidgetContents();
		}
		else if ( collationMode == CollationMode.CONTENTSCOLLATED )
		{
			collationRootCollator.onCollatedSubtreeStructureChanged( this );
		}

		super.onChildListChanged();
	}
	

	
	public DPWidget getWidgetAtContentStart()
	{
		if ( collationMode ==  CollationMode.ROOT )
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



	public String getContent()
	{
		String result = "";
		
		for (Element child: getChildren())
		{
			result += child.getContent();
		}
		
		return result;
	}
	
	public int getContentLength()
	{
		int result = 0;
		
		for (Element child: getChildren())
		{
			result += child.getContentLength();
		}
		
		return result;
	}
	

	
	//
	//
	// MARKER METHODS
	//
	//
	
	public ElementMarker marker(int position, Marker.Bias bias)
	{
		Element child = getChildAtContentPosition( position );
		return child.marker( position - getContentOffsetOfChild( child ), bias );
	}
	
	public ElementMarker markerAtStart()
	{
		List<Element> children = getChildren();
		if ( children.size() > 0 )
		{
			return children.get( 0 ).markerAtStart();
		}
		else
		{
			return null;
		}
	}
	
	public ElementMarker markerAtEnd()
	{
		List<Element> children = getChildren();
		if ( children.size() > 0 )
		{
			return children.get( children.size() - 1 ).markerAtEnd();
		}
		else
		{
			return null;
		}
	}
	
	
	public void moveMarker(ElementMarker m, int position, Marker.Bias bias)
	{
		Element child = getChildAtContentPosition( position );
		child.moveMarker( m, position - getContentOffsetOfChild( child ), bias );
	}
	
	public void moveMarkerToStart(ElementMarker m)
	{
		List<Element> children = getChildren();
		if ( children.size() > 0 )
		{
			children.get( 0 ).moveMarkerToStart( m );
		}
	}
	
	public void moveMarkerToEnd(ElementMarker m)
	{
		List<Element> children = getChildren();
		if ( children.size() > 0 )
		{
			children.get( children.size() - 1 ).moveMarkerToEnd( m );
		}
	}
	
	
	
	public boolean isMarkerAtStart(ElementMarker m)
	{
		List<Element> children = getChildren();
		if ( children.size() > 0 )
		{
			return children.get( 0 ).isMarkerAtStart( m );
		}
		else
		{
			return false;
		}
	}
	
	public boolean isMarkerAtEnd(ElementMarker m)
	{
		List<Element> children = getChildren();
		if ( children.size() > 0 )
		{
			return children.get( children.size() - 1 ).isMarkerAtEnd( m );
		}
		else
		{
			return false;
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
