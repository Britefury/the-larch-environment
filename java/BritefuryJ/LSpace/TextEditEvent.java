//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
