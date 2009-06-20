//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.Math.AABox2;

public abstract class DPContainerSequenceCollated extends DPContainerSequence
{
	protected DPWidget collationLeaves[], collationBranches[];
	
	
	
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
	
	protected void gatherCount(DPContainer branch, int counts[])
	{
		for (DPWidget w: branch.registeredChildren)
		{
			if ( w instanceof Collateable )
			{
				counts[1]++;
				gatherCount( (DPContainer)w, counts );
			}
			else
			{
				counts[0]++;
			}
		}
	}
	
	protected void gatherItems(DPContainer branch, int indices[])
	{
		for (DPWidget w: branch.registeredChildren)
		{
			if ( w instanceof Collateable )
			{
				Collateable c = (Collateable)w;
				int start = indices[0];
				collationBranches[indices[1]++] = w;
				gatherItems( (DPContainer)w, indices );
				int end = indices[0];
				c.setCollationRange( start, end );
			}
			else
			{
				collationLeaves[indices[0]++] = w;
			}
		}
	}
	
	
	protected void refreshCollation()
	{
		if ( collationLeaves == null )
		{
			int counts[] = new int [2];
			gatherCount( this, counts );
			
			collationLeaves = new DPWidget[counts[0]];
			collationBranches = new DPWidget[counts[1]];
			
			counts[0] = 0;
			counts[1] = 0;
			gatherItems( this, counts );
		}
	}
	
	
	
	protected void onChildListModified()
	{
		super.onChildListModified();
		resetCollation();
	}

	protected void onCollatedBranchChildListModified(Collateable branch)
	{
		resetCollation();
	}
	
	
	private void resetCollation()
	{
		collationLeaves = null;
		collationBranches = null;
	}
	
	
	protected void onSizeRefreshed()
	{
		super.onSizeRefreshed();
		for (DPWidget c: collationBranches)
		{
			c.onSizeRefreshed();
		}
	}

	
	
	
	
	protected abstract AABox2[] computeCollatedBranchBoundsBoxes(DPContainer collatedBranch, int rangeStart, int rangeEnd);






	List<DPWidget> getCollatedChildren()
	{
		refreshCollation();
		return Arrays.asList( collationLeaves );
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
