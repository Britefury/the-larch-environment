//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.HashSet;
import java.util.Vector;

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementTree.CollatableBranchElement.CollationMode;

public class ElementCollator
{
	private Vector<CollatableBranchElement> collatedChildBranches;
	private CollatedElementInterface element;
	
	
	//
	// Constructor
	//
	
	public ElementCollator(CollatedElementInterface element)
	{
		collatedChildBranches = new Vector<CollatableBranchElement>();
		this.element = element;
	}

	
	//
	// Collation methods
	//
	
	protected void refreshContainerWidgetContents()
	{
		// Gather the collated contents that are current, and the state that they will be in after completion
		Vector<Element> newChildren = new Vector<Element>();
		Vector<CollatableBranchElement> newCollatedChildBranches = new Vector<CollatableBranchElement>();
		
		element.collateSubtree( newChildren, newCollatedChildBranches );
		
		
		// Generate the list of child widgets
		Vector<DPWidget> childWidgets = new Vector<DPWidget>();
		
		childWidgets.setSize( newChildren.size() );
		for (int i = 0; i < newChildren.size(); i++)
		{
			childWidgets.set( i, newChildren.get( i ).getWidget() );
		}
		

		
		// Work out what has been added, and what has been removed
		HashSet<CollatableBranchElement> addedCollatedChildBranches, removedCollatedChildBranches;
		
		addedCollatedChildBranches = new HashSet<CollatableBranchElement>( newCollatedChildBranches );
		removedCollatedChildBranches = new HashSet<CollatableBranchElement>( collatedChildBranches );
		addedCollatedChildBranches.removeAll( collatedChildBranches );
		removedCollatedChildBranches.removeAll( newCollatedChildBranches );
		
		
		for (CollatableBranchElement x: removedCollatedChildBranches)
		{
			x.setCollator( null );
			x.setCollationMode( CollationMode.UNINITIALISED );
		}
		
		collatedChildBranches = newCollatedChildBranches;
		
		element.setCollatedContainerChildWidgets( childWidgets );

		for (CollatableBranchElement x: addedCollatedChildBranches)
		{
			x.setCollationMode( CollationMode.CONTENTSCOLLATED );
			x.setCollator( this );
		}
	}
	
	protected void onCollatedSubtreeStructureChanged(BranchElement child)
	{
		refreshContainerWidgetContents();
	}
}
