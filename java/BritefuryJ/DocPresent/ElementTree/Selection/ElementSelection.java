//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree.Selection;

import java.util.ArrayList;

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementTree.BranchElement;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementTree;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;
import BritefuryJ.DocPresent.Selection.Selection;

public class ElementSelection
{
	protected ElementTree tree;
	protected ElementMarker marker0, marker1;
	protected Selection widgetSelection;
	
	
	public ElementSelection(ElementTree tree, Selection widgetSelection)
	{
		this.tree = tree;
		this.widgetSelection = widgetSelection;
		marker0 = new ElementMarker( tree, widgetSelection.getMarker0() );
		marker1 = new ElementMarker( tree, widgetSelection.getMarker1() );
	}



	public boolean isEmpty()
	{
		return widgetSelection.isEmpty();
	}
	
	
	
	public ElementMarker getMarker0()
	{
		return marker0;
	}
	
	public ElementMarker getMarker1()
	{
		return marker1;
	}
	
	
	public void clear()
	{
		widgetSelection.clear();
	}
	
	
	
	public ElementMarker getStartMarker()
	{
		return widgetSelection.getStartMarker() == marker0.getWidgetMarker()  ?  marker0  :  marker1;
	}
	
	public ElementMarker getEndMarker()
	{
		return widgetSelection.getEndMarker() == marker0.getWidgetMarker()  ?  marker0  :  marker1;
	}
	
	public ArrayList<Element> getStartPathFromCommonRoot()
	{
		return widgetPathToElementPath( widgetSelection.getStartPathFromCommonRoot() );
	}
	
	public ArrayList<Element> getEndPathFromCommonRoot()
	{
		return widgetPathToElementPath( widgetSelection.getEndPathFromCommonRoot() );
	}
	
	public BranchElement getCommonRoot()
	{
		return (BranchElement)widgetSelection.getCommonRoot().getElement();
	}
	
	
	private ArrayList<Element> widgetPathToElementPath(ArrayList<DPWidget> widgetPath)
	{
		ArrayList<Element> elementPath = new ArrayList<Element>();
		elementPath.ensureCapacity( widgetPath.size() );
		for (DPWidget w: widgetPath)
		{
			elementPath.add( w.getElement() );
		}
		return elementPath;
	}
}
