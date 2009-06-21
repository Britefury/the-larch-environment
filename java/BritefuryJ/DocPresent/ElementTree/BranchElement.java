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

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;

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
}
