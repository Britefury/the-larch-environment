//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;

public abstract class LeafElement extends Element
{
	//
	// Constructor
	//
	
	protected LeafElement(DPContentLeaf widget)
	{
		super( widget );
	}
	
	
	
	//
	// Widget
	//
	
	public DPContentLeaf getWidget()
	{
		return (DPContentLeaf)widget;
	}

	



	//
	// Element tree structure methods
	//
	
	public LeafElement getPreviousLeaf()
	{
		return (LeafElement)getWidget().getPreviousLeaf().getElement();
	}

	public LeafElement getNextLeaf()
	{
		return (LeafElement)getWidget().getNextLeaf().getElement();
	}

	
	public LeafElement getPreviousLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		return (LeafElement)getWidget().getPreviousLeaf( subtreeRootFilter, branchFilter, leafFilter ).getElement();
	}

	public LeafElement getNextLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		return (LeafElement)getWidget().getNextLeaf( subtreeRootFilter, branchFilter, leafFilter ).getElement();
	}

	

	public LeafElement getPreviousEditableLeaf()
	{
		return (LeafElement)getWidget().getPreviousEditableLeaf().getElement();
	}

	public LeafElement getNextEditableLeaf()
	{
		return (LeafElement)getWidget().getNextEditableLeaf().getElement();
	}

	
	public LeafElement getPreviousEditableLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter)
	{
		return (LeafElement)getWidget().getPreviousEditableLeaf( subtreeRootFilter, branchFilter ).getElement();
	}

	public LeafElement getNextEditableLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter)
	{
		return (LeafElement)getWidget().getNextEditableLeaf( subtreeRootFilter, branchFilter ).getElement();
	}

	
	
	public LeafElement getPreviousEditableEntryLeaf()
	{
		return (LeafElement)getWidget().getPreviousEditableEntryLeaf().getElement();
	}

	public LeafElement getNextEditableEntryLeaf()
	{
		return (LeafElement)getWidget().getNextEditableEntryLeaf().getElement();
	}

	
	public LeafElement getPreviousEditableEntryLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter)
	{
		return (LeafElement)getWidget().getPreviousEditableEntryLeaf( subtreeRootFilter, branchFilter ).getElement();
	}

	public LeafElement getNextEditableEntryLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter)
	{
		return (LeafElement)getWidget().getNextEditableEntryLeaf( subtreeRootFilter, branchFilter ).getElement();
	}


	
	
	//
	// Text representation methods
	//

	protected String getTextRepresentationBetweenMarkers(ElementMarker startMarker, ElementMarker endMarker)
	{
		return getWidget().getTextRepresentationBetweenMarkers( startMarker.getWidgetMarker(), endMarker.getWidgetMarker() );
	}



	

	
	
	//
	// Type methods
	//


	public boolean isWhitespace()
	{
		return getWidget().isWhitespace();
	}
	
	public boolean isEditable()
	{
		return getWidget().isEditable();
	}
	
	public boolean isEditableEntry()
	{
		return getWidget().isEditableEntry();
	}
}
