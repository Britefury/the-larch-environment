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

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPSegment;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementContext;
import BritefuryJ.DocPresent.ElementKeyboardListener;
import BritefuryJ.DocPresent.ElementTextRepresentationListener;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;

public abstract class Element
{
	protected DPWidget widget;
	protected BranchElement parent;
	protected ElementTree tree;
	protected DPWidget metaElement;
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
		metaElement = null;
		debugName = null;
	}
	
	
	
	//
	// Owner
	//
	
	public ElementContext getContext()
	{
		return getWidget().getContext();
	}
	
	public void setContext(ElementContext context)
	{
		getWidget().setContext( context );
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
	
	public ElementTextRepresentationListener getTextRepresentationListener()
	{
		return getWidget().getTextRepresentationListener();
	}
	
	public void setTextRepresentationListener(ElementTextRepresentationListener listener)
	{
		getWidget().setTextRepresentationListener( listener );
	}
	
	
	public ElementKeyboardListener getKeyboardListener()
	{
		return getWidget().getKeyboardListener();
	}
	
	public void setKeyboardListener(ElementKeyboardListener listener)
	{
		getWidget().setKeyboardListener( listener );
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
	
	public static void getPathsFromCommonSubtreeRoot(Element e0, List<Element> path0, Element e1, List<Element> path1)
	{
		ArrayList<DPWidget> wpath0 = new ArrayList<DPWidget>();
		ArrayList<DPWidget> wpath1 = new ArrayList<DPWidget>();
		DPWidget.getPathsFromCommonSubtreeRoot( e0.getWidget(), wpath0, e1.getWidget(), wpath1 );
		for (DPWidget w: wpath0)
		{
			path0.add( w.getElement() );
		}
		for (DPWidget w: wpath1)
		{
			path1.add( w.getElement() );
		}
	}


	
	public LeafElement getFirstLeafInSubtree()
	{
		return (LeafElement)getWidget().getFirstLeafInSubtree().getElement();
	}

	public LeafElement getFirstEditableEntryLeafInSubtree()
	{
		return (LeafElement)getWidget().getFirstEditableEntryLeafInSubtree().getElement();
	}

	public LeafElement getLastLeafInSubtree()
	{
		return (LeafElement)getWidget().getLastLeafInSubtree().getElement();
	}

	public LeafElement getLastEditableEntryLeafInSubtree()
	{
		return (LeafElement)getWidget().getLastEditableEntryLeafInSubtree().getElement();
	}


	
	
	
	
	
	//
	// Text representation methods
	//
	
	public LeafElement getLeafAtTextRepresentationPosition(int position)
	{
		DPContentLeaf w = getWidget().getLeafAtTextRepresentationPosition( position );
		return w != null  ?  (LeafElement)w.getElement()  :  null;
	}
	
	
	public int getTextRepresentationOffsetInSubtree(BranchElement subtreeRoot)
	{
		return getWidget().getTextRepresentationOffsetInSubtree( subtreeRoot.getWidget() );
	}
	
	
	public String getTextRepresentationFromStartToMarker(ElementMarker marker)
	{
		return getWidget().getTextRepresentationFromStartToMarker( marker.getWidgetMarker() );
	}
	
	public String getTextRepresentationFromMarkerToEnd(ElementMarker marker)
	{
		return getWidget().getTextRepresentationFromMarkerToEnd( marker.getWidgetMarker() );
	}



	
	public String getTextRepresentation()
	{
		return getWidget().getTextRepresentation();
	}

	public int getTextRepresentationLength()
	{
		return getWidget().getTextRepresentationLength();
	}


	
	//
	//
	// Segment methods
	//
	//
	
	public SegmentElement getSegment()
	{
		DPSegment seg = getWidget().getSegment();
		if ( seg != null )
		{
			return (SegmentElement)seg.getElement();
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
	
	
	
	//
	// Meta-element
	//
	
	protected static TextStyleSheet headerDebugTextStyle = new TextStyleSheet( new Font( "Sans serif", Font.BOLD, 14 ), new Color( 0.0f, 0.5f, 0.5f ) );
	protected static TextStyleSheet headerDescriptionTextStyle = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 14 ), new Color( 0.0f, 0.0f, 0.75f ) );
	protected static HBoxStyleSheet metaHeaderHBoxStyle = new HBoxStyleSheet( VAlignment.BASELINES, 10.0, false, 0.0 );
	protected static EmptyBorder metaHeaderEmptyBorder = new EmptyBorder();


	public DPWidget createMetaHeaderData()
	{
		return null;
	}
	
	public DPWidget createMetaHeaderDebug()
	{
		if ( debugName != null )
		{
			return new DPText( headerDebugTextStyle, "<" + debugName + ">" );
		}
		else
		{
			return null;
		}
	}
	
	public DPWidget createMetaDescription()
	{
		String description = toString();
		description = description.replace( "BritefuryJ.DocPresent.ElementTree.", "" );
		return new DPText( headerDescriptionTextStyle, description );
	}
	
	protected Border getMetaHeaderBorder()
	{
		return metaHeaderEmptyBorder;
	}
	
	public DPWidget createMetaHeader()
	{
		DPHBox hbox = new DPHBox( metaHeaderHBoxStyle );
		DPWidget data = createMetaHeaderData();
		DPWidget debug = createMetaHeaderDebug();
		DPWidget descr = createMetaDescription();
		if ( data != null )
		{
			hbox.append( data );
		}
		if ( debug != null )
		{
			hbox.append( debug );
		}
		hbox.append( descr );
		

		DPBorder border = new DPBorder( getMetaHeaderBorder() );
		border.setChild( hbox );
		return border;
	}
	
	public DPBorder getMetaHeaderBorderWidget()
	{
		if ( metaElement != null )
		{
			DPBin bin = (DPBin)metaElement;
			return (DPBorder)bin.getChild();
		}
		else
		{
			return null;
		}
	}
	
	public DPWidget createMetaElement()
	{
		DPBin bin = new DPBin();
		bin.setChild( createMetaHeader() );
		return bin;
	}
	
	public DPWidget initialiseMetaElement()
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
	
	public DPWidget getMetaElement()
	{
		return metaElement;
	}
	
	
	public void setDebugName(String debugName)
	{
		this.debugName = debugName;
	}
}
