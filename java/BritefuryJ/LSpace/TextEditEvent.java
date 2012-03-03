//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

public abstract class TextEditEvent extends EditEvent
{
	protected LSContentLeaf leaf, prevNeighbour, nextNeighbour;
	protected LSElement prevNeighbourCommonAncestor, nextNeighbourCommonAncestor;
	protected boolean computedAncestors = false;
	
	
	public TextEditEvent(LSContentLeaf leaf, LSContentLeaf prevNeighbour, LSContentLeaf nextNeighbour)
	{
		this.leaf = leaf;
		this.prevNeighbour = prevNeighbour;
		this.nextNeighbour = nextNeighbour;
	}
	
	public TextEditEvent(LSContentLeaf leaf)
	{
		this( leaf, null, null );
	}
	
	
	public LSContentLeaf getLeaf()
	{
		return leaf;
	}
	
	public LSContentLeaf getPrevNeighbour()
	{
		return prevNeighbour;
	}
	
	public LSContentLeaf getNextNeighbour()
	{
		return nextNeighbour;
	}
	
	
	public LSElement getPrevNeighbourCommonAncestor()
	{
		computeAncestors();
		return prevNeighbourCommonAncestor;
	}
	
	public LSElement getNextNeighbourCommonAncestor()
	{
		computeAncestors();
		return nextNeighbourCommonAncestor;
	}
	
	
	private void computeAncestors()
	{
		if ( !computedAncestors )
		{
			prevNeighbourCommonAncestor = prevNeighbour != null  ?  LSElement.getCommonAncestor( leaf, prevNeighbour )  :  null;
			nextNeighbourCommonAncestor = nextNeighbour != null  ?  LSElement.getCommonAncestor( leaf, nextNeighbour )  :  null;
			computedAncestors = true;
		}
	}
	
	
	public abstract boolean revert();
}
