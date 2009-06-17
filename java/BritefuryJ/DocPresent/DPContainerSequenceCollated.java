//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Stack;

import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public abstract class DPContainerSequenceCollated extends DPContainerSequence
{
	protected DPWidget collationLeaves[];
	protected Collateable collationBranches[];
	
	
	
	public DPContainerSequenceCollated()
	{
		this( ContainerStyleSheet.defaultStyleSheet );
	}

	public DPContainerSequenceCollated(ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
	}
	
	
	
	
	//
	//
	// COLLATION METHODS
	//
	//
	
	protected void gather(ArrayList<DPWidget> leaves, ArrayList<Collateable> branches)
	{
		ArrayList<DPWidget> stack = new ArrayList<DPWidget>();
		
		stack.add( this );
		
		while ( !stack.isEmpty() )
		{
			DPWidget item = stack.get( stack.size() - 1 );
			stack.remove( stack.size() - 1 );
			
			if ( item instanceof Collateable  ||  item == this )
			{
				if ( item != this )
				{
					branches.add( (Collateable)item );
				}
				
				
				DPContainer container = (DPContainer)item;
				List<DPWidget> children = container.getChildren();
				
				for (int i = children.size() - 1; i >= 0; i--)
				{
					stack.add( children.get( i ) );
				}
			}
			else
			{
				leaves.add( item );
			}
		}
	}
	
	protected void refreshCollation()
	{
		if ( collationLeaves == null )
		{
			ArrayList<DPWidget> leaves = new ArrayList<DPWidget>();
			ArrayList<Collateable> branches = new ArrayList<Collateable>();
			
			gather( leaves, branches );
			
			collationLeaves = new DPWidget[leaves.size()];
			collationLeaves = leaves.toArray( collationLeaves );
			collationBranches = new Collateable[branches.size()];
			collationBranches = branches.toArray( collationBranches );
			
			for (Collateable branch: collationBranches)
			{
				branch.setCollationRoot( this );
			}
		}
	}
	
	
	
	protected void childListModified()
	{
		resetCollation();
	}

	protected void onCollatedBranchChildListModified(Collateable branch)
	{
		resetCollation();
	}
	
	
	private void resetCollation()
	{
		if ( collationBranches != null )
		{
			for (Collateable branch: collationBranches)
			{
				branch.setCollationRoot( null );
			}
		}
		collationLeaves = null;
		collationBranches = null;
	}









	LReqBox[] getCollatedChildrenRefreshedRequistionXBoxes()
	{
		return getChildrenRefreshedRequistionXBoxes( Arrays.asList( collationLeaves ) );
	}

	LReqBox[] getCollatedChildrenRefreshedRequistionYBoxes()
	{
		return getChildrenRefreshedRequistionYBoxes( Arrays.asList( collationLeaves ) );
	}

	LReqBox[] getCollatedChildrenRequisitionBoxes()
	{
		return getChildrenRequisitionBoxes( Arrays.asList( collationLeaves ) );
	}

	LAllocBox[] getCollatedChildrenAllocationBoxes()
	{
		return getChildrenAllocationBoxes( Arrays.asList( collationLeaves ) );
	}

	<T extends PackingParams> T[] getCollatedChildrenPackingParams(T packingParams[])
	{
		return getChildrenPackingParams( Arrays.asList( collationLeaves ), packingParams );
	}
	
	
	double[] getCollatedChildrenAllocationX()
	{
		return getChildrenAllocationX( Arrays.asList( collationLeaves ) );
	}

	double[] getCollatedChildrenAllocationY()
	{
		return getChildrenAllocationY( Arrays.asList( collationLeaves ) );
	}
}
