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
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;

public abstract class Element
{
	public static interface ElementContext
	{
	};
	
	protected DPWidget widget;
	protected BranchElement parent;
	protected ElementTree tree;
	protected ElementTextRepresentationListener contentListener;
	protected ElementKeyboardListener keyboardListener;
	protected DPWidget metaElement;
	protected ElementContext context;
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
		keyboardListener = null;
		metaElement = null;
		context = null;
		debugName = null;
	}
	
	
	
	//
	// Owner
	//
	
	public ElementContext getContext()
	{
		return context;
	}
	
	public void setContext(ElementContext context)
	{
		this.context = context;
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
		return contentListener;
	}
	
	public void setTextRepresentationListener(ElementTextRepresentationListener listener)
	{
		contentListener = listener;
	}
	
	
	public ElementKeyboardListener getKeyboardListener()
	{
		return keyboardListener;
	}
	
	public void setKeyboardListener(ElementKeyboardListener listener)
	{
		keyboardListener = listener;
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
	
	public ArrayList<Element> getElementPathFromRoot()
	{
		ArrayList<Element> path = new ArrayList<Element>();
		
		Element element = this;
		while ( element != null )
		{
			path.add( 0, element );
			element = element.getParent();
		}
		
		return path;
	}
	
	public ArrayList<Element> getElementPathFromSubtreeRoot(BranchElement subtreeRoot)
	{
		ArrayList<Element> path = new ArrayList<Element>();
		
		Element element = this;
		while ( element != null )
		{
			path.add( 0, element );
			if ( element == subtreeRoot )
			{
				return path;
			}
			element = element.getParent();
		}

		return null;
	}
	
	
	public static void getPathsFromCommonSubtreeRoot(Element e0, List<Element> path0, Element e1, List<Element> path1)
	{
		if ( e0 == e1 )
		{
			path0.add( e0 );
			path1.add( e1 );
		}
		else
		{
			ArrayList<Element> p0 = e0.getElementPathFromRoot();
			ArrayList<Element> p1 = e1.getElementPathFromRoot();
			
			int minLength = Math.min( p0.size(), p1.size() );
			
			int numCommonElements = 0;
			
			for (int i = 0; i < minLength; i++)
			{
				numCommonElements = i;
				
				if ( p0.get( i ) != p1.get( i ) )
				{
					break;
				}
			}
			
			path0.addAll( p0.subList( numCommonElements - 1, p0.size() ) );
			path1.addAll( p1.subList( numCommonElements - 1, p1.size() ) );
		}
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

	public LeafElement getFirstEditableLeafInSubtree()
	{
		return getFirstLeafInSubtree( null, new LeafElement.LeafFilterEditable() );
	}

	public LeafElement getFirstEditableEntryLeafInSubtree()
	{
		return getFirstLeafInSubtree( null, new LeafElement.LeafFilterEditableEntry() );
	}

	public LeafElement getLastLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		return null;
	}

	public LeafElement getLastLeafInSubtree()
	{
		return getLastLeafInSubtree( null, null );
	}

	public LeafElement getLastEditableLeafInSubtree()
	{
		return getLastLeafInSubtree( null, new LeafElement.LeafFilterEditable() );
	}

	public LeafElement getLastEditableEntryLeafInSubtree()
	{
		return getLastLeafInSubtree( null, new LeafElement.LeafFilterEditableEntry() );
	}

	
	public LeafElement getLeafAtTextRepresentationPosition(int position)
	{
		return null;
	}
	
	
	public int getTextRepresentationOffsetInSubtree(BranchElement subtreeRoot)
	{
		if ( this == subtreeRoot )
		{
			return 0;
		}
		else
		{
			return parent.getChildTextRepresentationOffsetInSubtree( this, subtreeRoot );
		}
	}
	
	
	public String getTextRepresentationFromStartToMarker(ElementMarker marker)
	{
		StringBuilder builder = new StringBuilder();
		marker.getElement().getTextRepresentationFromStartOfRootToMarker( builder, marker, this );
		return builder.toString();
	}
	
	public String getTextRepresentationFromMarkerToEnd(ElementMarker marker)
	{
		StringBuilder builder = new StringBuilder();
		marker.getElement().getTextRepresentationFromMarkerToEndOfRoot( builder, marker, this );
		return builder.toString();
	}

	protected abstract void getTextRepresentationFromStartToPath(StringBuilder builder, ElementMarker marker, ArrayList<Element> path, int pathMyIndex);
	protected abstract void getTextRepresentationFromPathToEnd(StringBuilder builder, ElementMarker marker, ArrayList<Element> path, int pathMyIndex);



	
	//
	// Text representation methods
	//
	
	protected void textRepresentationChanged()
	{
		onTextRepresentationModified();
		onTextRepresentationModifiedEvent();
	}
	
	protected void onTextRepresentationModified()
	{
		if ( parent != null )
		{
			parent.onTextRepresentationModified();
		}
	}
	
	protected boolean onTextRepresentationModifiedEvent()
	{
		if ( contentListener != null )
		{
			if ( contentListener.textRepresentationModified( this ) )
			{
				return true;
			}
		}
		
		if ( parent != null )
		{
			return parent.onChildTextRepresentationModifiedEvent( this );
		}
		
		return false;
	}
	
	public DPWidget getWidgetAtTextRepresentationStart()
	{
		return getWidget();
	}
	
		
	public abstract String getTextRepresentation();
	public abstract int getTextRepresentationLength();


	
	//
	//
	// Segment methods
	//
	//
	
	public SegmentElement getSegment()
	{
		if ( parent != null )
		{
			return parent.getSegmentFromChild( this );
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
	
	
	
	


	//
	// Keyboard input event methods
	//

	public boolean onKeyPress(KeyEvent event)
	{
		if ( keyboardListener != null )
		{
			if ( keyboardListener.onKeyPress( this, event ) )
			{
				return true;
			}
		}

		if ( parent != null )
		{
			return parent.onKeyPress( event );
		}
		
		return false;
	}

	public boolean onKeyRelease(KeyEvent event)
	{
		if ( keyboardListener != null )
		{
			if ( keyboardListener.onKeyRelease( this, event ) )
			{
				return true;
			}
		}

		if ( parent != null )
		{
			return parent.onKeyRelease( event );
		}
		
		return false;
	}

	public boolean onKeyTyped(KeyEvent event)
	{
		if ( keyboardListener != null )
		{
			if ( keyboardListener.onKeyTyped( this, event ) )
			{
				return true;
			}
		}

		if ( parent != null )
		{
			return parent.onKeyTyped( event );
		}
		
		return false;
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
