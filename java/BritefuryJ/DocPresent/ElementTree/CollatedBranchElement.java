//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.util.HashSet;
import java.util.Vector;

import BritefuryJ.DocPresent.DPContainerSequence;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public abstract class CollatedBranchElement extends CollatableSequenceBranchElement
{
	private Vector<CollatableBranchElement> collatedChildBranches;
	
	
	//
	// Constructor
	//
	
	public CollatedBranchElement(ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
		
		this.collatedChildBranches = null;
	}

	
	//
	// Widget
	//
	
	public DPContainerSequence getWidget()
	{
		return (DPContainerSequence)getContainer();
	}
	
	
	
	//
	// Collation methods
	//
	
	protected void setCollationMode(CollationMode m)
	{
		super.setCollationMode( m );
		
		if ( collationMode == CollationMode.INDEPENDENT )
		{
			collatedChildBranches = new Vector<CollatableBranchElement>();
		}
		else if ( collationMode == CollationMode.INPARENT )
		{
			collatedChildBranches = null;
		}
		else
		{
			collatedChildBranches = null;
		}
	}
	
	

	protected abstract BranchFilter createCollationFilter();

	protected void refreshCollatedContents()
	{
		if ( collationMode == CollationMode.INDEPENDENT )
		{
			// Gather the collated contents that are current, and the state that they will be in after completion
			Vector<Element> newChildren = new Vector<Element>();
			Vector<CollatableBranchElement> newCollatedChildBranches = new Vector<CollatableBranchElement>();
			
			collateSubtree( newChildren, newCollatedChildBranches, createCollationFilter() );
			
			
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
				x.setCollationRoot( null );
				x.setCollationMode( CollationMode.NONE );
			}
			
			collatedChildBranches = newCollatedChildBranches;
			
			getWidget().setChildren( childWidgets );
	
			for (CollatableBranchElement x: addedCollatedChildBranches)
			{
				x.setCollationMode( CollationMode.INPARENT );
				x.setCollationRoot( this );
			}
		}
	}
	
	
	protected void onCollatedSubtreeStructureChanged(BranchElement child)
	{
		refreshCollatedContents();
	}

	
	
	
	//
	// Element type methods
	//
	
	protected boolean isCollatedBranch()
	{
		return true;
	}
}
