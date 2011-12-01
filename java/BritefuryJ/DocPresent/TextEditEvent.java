//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

public abstract class TextEditEvent extends EditEvent
{
	protected DPContentLeaf leaf, prevNeighbour, nextNeighbour;
	protected DPElement prevNeighbourCommonAncestor, nextNeighbourCommonAncestor;
	protected boolean computedAncestors = false;
	
	
	public TextEditEvent(DPContentLeaf leaf, DPContentLeaf prevNeighbour, DPContentLeaf nextNeighbour)
	{
		this.leaf = leaf;
		this.prevNeighbour = prevNeighbour;
		this.nextNeighbour = nextNeighbour;
	}
	
	
	public DPContentLeaf getLeaf()
	{
		return leaf;
	}
	
	public DPContentLeaf getPrevNeighbour()
	{
		return prevNeighbour;
	}
	
	public DPContentLeaf getNextNeighbour()
	{
		return nextNeighbour;
	}
	
	
	public DPElement getPrevNeighbourCommonAncestor()
	{
		computeAncestors();
		return prevNeighbourCommonAncestor;
	}
	
	public DPElement getNextNeighbourCommonAncestor()
	{
		computeAncestors();
		return nextNeighbourCommonAncestor;
	}
	
	
	private void computeAncestors()
	{
		if ( !computedAncestors )
		{
			prevNeighbourCommonAncestor = prevNeighbour != null  ?  DPElement.getCommonAncestor( leaf, prevNeighbour )  :  null;
			nextNeighbourCommonAncestor = nextNeighbour != null  ?  DPElement.getCommonAncestor( leaf, nextNeighbour )  :  null;
			computedAncestors = true;
		}
	}
	
	
	public abstract boolean revert();
}
