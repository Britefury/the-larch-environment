//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.DPWidget.IsNotInSubtreeException;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;

public abstract class Element
{
	protected DPWidget widget;
	protected BranchElement parent;
	protected ElementTree tree;
	protected ElementContentListener contentListener;
	protected Element metaElement;
	protected String debugName;
	
	
	
	//
	// Constructor
	//
	
	protected Element(DPWidget widget)
	{
		this.widget = widget;
		
		if ( widget != null )
		{
			widget.setElement( this );
		}
		
		parent = null;
		tree = null;
		contentListener = null;
		metaElement = null;
		debugName = null;
	}
	
	
	
	//
	// Widget
	//
	
	public DPWidget getWidget()
	{
		return widget;
	}
	
	protected void setWidget(DPWidget w)
	{
		if ( w != widget )
		{
			if ( widget != null )
			{
				widget.setElement( null );
			}
			
			widget = w;
	
			if ( widget != null )
			{
				widget.setElement( this );
			}
		}
	}
	
	
	
	//
	// Listeners
	//
	
	public void setContentListener(ElementContentListener listener)
	{
		contentListener = listener;
	}
	
	
	
	//
	// Element tree and parent methods
	//
	
	protected void setParent(BranchElement parent)
	{
		this.parent = parent;
	}
	
	protected void setElementTree(ElementTree tree)
	{
		this.tree = tree;
	}
	
	
	public Element getParent()
	{
		return parent;
	}

	public ElementTree getElementTree()
	{
		return tree;
	}
	
	
	

	//
	// Element tree structure methods
	//
	
	public boolean isInSubtreeRootedAt(BranchElement r)
	{
		Element e = this;
		
		while ( e != null  &&  e != r )
		{
			e = e.getParent();
		}
		
		return e == r;
	}
	
	public void getElementPathFromRoot(List<Element> path)
	{
		// Root to top
		if ( parent != null )
		{
			parent.getElementPathFromRoot( path );
		}
		
		path.add( this );
	}
	
	public void getElementPathFromSubtreeRoot(BranchElement subtreeRoot, List<Element> path) throws IsNotInSubtreeException
	{
		// Root to top
		if ( subtreeRoot != this )
		{
			if ( parent != null )
			{
				parent.getElementPathFromSubtreeRoot( subtreeRoot, path );
			}
			else
			{
				throw new IsNotInSubtreeException();
			}
		}
		
		path.add( this );
	}


	
	public List<LeafElement> getLeavesInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		return new ArrayList<LeafElement>();
	}
	
	public List<LeafElement> getLeavesInSubtree()
	{
		return getLeavesInSubtree( null, null );
	}
	
	public LeafElement getFirstLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		return null;
	}

	public LeafElement getFirstLeafInSubtree()
	{
		return getFirstLeafInSubtree( null, null );
	}

	public LeafElement getLastLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		return null;
	}

	public LeafElement getLastLeafInSubtree()
	{
		return getLastLeafInSubtree( null, null );
	}

	
	public LeafElement getLeafAtContentPosition(int position)
	{
		return null;
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



	
	//
	// Content methods
	//
	
	protected void contentChanged()
	{
		onContentModified();
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
	
	public abstract String getContent();
	public abstract int getContentLength();


	
	//
	//
	// Segment methods
	//
	//
	
	public SegmentElement getSegment()
	{
		if ( parent != null )
		{
			return parent.getLinearTextSectionFromChild( this );
		}
		else
		{
			return null;
		}
	}



	//
	//
	// Marker methods
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
	
	
	
	
	//
	// Meta-element
	//
	
	static TextStyleSheet headerDebugTextStyle = new TextStyleSheet( new Font( "Sans serif", Font.BOLD, 14 ), new Color( 0.0f, 0.5f, 0.5f ) );
	static TextStyleSheet headerDescriptionTextStyle = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 14 ), new Color( 0.0f, 0.0f, 0.75f ) );
	static HBoxStyleSheet metaHeaderHBoxStyle = new HBoxStyleSheet( DPHBox.Alignment.BASELINES, 10.0, false, 0.0 );

	public Element createMetaHeaderData()
	{
		return null;
	}
	
	public Element createMetaHeaderDebug()
	{
		if ( debugName != null )
		{
			return new TextElement( headerDebugTextStyle, "<" + debugName + ">" );
		}
		else
		{
			return null;
		}
	}
	
	public Element createMetaDescription()
	{
		String description = toString();
		int index = description.lastIndexOf( "." );
		return new TextElement( headerDescriptionTextStyle, description.substring( index + 1 ) );
	}
	
	public Element createMetaHeader()
	{
		HBoxElement hbox = new HBoxElement( metaHeaderHBoxStyle );
		ArrayList<Element> children = new ArrayList<Element>();
		Element data = createMetaHeaderData();
		Element debug = createMetaHeaderDebug();
		Element descr = createMetaDescription();
		if ( data != null )
		{
			children.add( data );
		}
		if ( debug != null )
		{
			children.add( debug );
		}
		children.add( descr );
		hbox.setChildren( children );
		return hbox;
	}
	
	public Element createMetaElement()
	{
		BinElement bin = new BinElement();
		bin.setChild( createMetaHeader() );
		return bin;
	}
	
	public Element initialiseMetaElement()
	{
		if ( metaElement == null )
		{
			metaElement = createMetaElement();
		}
		return metaElement;
	}
	
	public void shutdownMetaElement()
	{
		metaElement = null;
	}
	
	
	public void refreshMetaElement()
	{
		if ( metaElement != null )
		{
			BinElement bin = (BinElement)metaElement;
			bin.setChild( createMetaHeader() );
		}
	}

	public void setDebugName(String debugName)
	{
		this.debugName = debugName;
	}
	
	
	
	
	//
	// Element type methods
	//
	
	protected boolean isBranch()
	{
		return false;
	}
	
	protected boolean isCollatableBranch()
	{
		return false;
	}
	
	protected boolean isProxy()
	{
		return false;
	}
	
	protected boolean isParagraph()
	{
		return false;
	}
	
	protected boolean isSegment()
	{
		return false;
	}
}
