//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public abstract class BranchElement extends Element
{
	//
	// Constructor
	//
	
	protected BranchElement(DPContainer widget)
	{
		super( widget );
	}


	
	//
	// Widget
	//
	
	public DPContainer getWidget()
	{
		return (DPContainer)widget;
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
		onSubtreeStructureChanged();
		refreshMetaElement();
	}
	
	protected void onSubtreeStructureChanged()
	{
		if ( parent != null )
		{
			parent.onSubtreeStructureChanged();
		}
	}

	public abstract List<Element> getChildren();
	

	
	
	
	//
	// Text representation methods
	//
	
	protected void getTextRepresentationBetweenPaths(StringBuilder builder, ElementMarker startMarker, ArrayList<Element> startPath, int startPathMyIndex,
			ElementMarker endMarker, ArrayList<Element> endPath, int endPathMyIndex)
	{
		ArrayList<DPWidget> startWidgetPath = new ArrayList<DPWidget>();
		ArrayList<DPWidget> endWidgetPath = new ArrayList<DPWidget>();
		for (Element e: startPath)
		{
			startWidgetPath.add( e.getWidget() );
		}
		for (Element e: endPath)
		{
			endWidgetPath.add( e.getWidget() );
		}
		getWidget().getTextRepresentationBetweenPaths( builder, startMarker.getWidgetMarker(), startWidgetPath, startPathMyIndex, endMarker.getWidgetMarker(), endWidgetPath, endPathMyIndex );
	}
	
	

	//
	// Meta-element
	//
	
	static EmptyBorder metaIndentBorder = new EmptyBorder( 25.0, 0.0, 0.0, 0.0 );
	static VBoxStyleSheet metaVBoxStyle = new VBoxStyleSheet( VTypesetting.ALIGN_WITH_BOTTOM, HAlignment.LEFT, 0.0, false, 0.0 );
	
	public DPBorder getMetaHeaderBorderWidget()
	{
		if ( metaElement != null )
		{
			DPVBox metaVBox = (DPVBox)metaElement;
			return (DPBorder)metaVBox.get( 0 );
		}
		else
		{
			return null;
		}
	}
	
	public DPWidget createMetaElement()
	{
		DPVBox metaChildrenVBox = new DPVBox( metaVBoxStyle );
		for (Element child: getChildren())
		{
			DPWidget metaChild = child.initialiseMetaElement();
			metaChildrenVBox.append( metaChild );
		}
		
		DPBorder indentMetaChildren = new DPBorder( metaIndentBorder );
		indentMetaChildren.setChild( metaChildrenVBox );
		
		DPVBox metaVBox = new DPVBox( metaVBoxStyle );
		metaVBox.append( createMetaHeader() );
		metaVBox.append( indentMetaChildren );
		
		return metaVBox;
	}
	
	public void refreshMetaElement()
	{
		if ( metaElement != null )
		{
			DPVBox metaVBox = (DPVBox)metaElement;
			
			DPBorder indentMetaChildren = (DPBorder)metaVBox.get( 1 );
			DPVBox metaChildrenVBox = (DPVBox)indentMetaChildren.getChild();

			ArrayList<DPWidget> childMetaElements = new ArrayList<DPWidget>();
			for (Element child: getChildren())
			{
				DPWidget metaChild = child.initialiseMetaElement();
				childMetaElements.add( metaChild );
			}
			metaChildrenVBox.setChildren( childMetaElements );
		}
	}

	public void shutdownMetaElement()
	{
		if ( metaElement != null )
		{
			for (Element child: getChildren())
			{
				child.shutdownMetaElement();
			}
		}
		super.shutdownMetaElement();
	}
}
