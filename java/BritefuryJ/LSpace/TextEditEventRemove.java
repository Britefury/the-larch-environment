//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

public class TextEditEventRemove extends TextEditEvent
{
	protected int position;
	protected String textRemoved;
	
	public TextEditEventRemove(LSContentLeaf leaf, LSContentLeaf prevNeighbour, LSContentLeaf nextNeighbour, int position, String textRemoved)
	{
		super( leaf, prevNeighbour, nextNeighbour );
		
		this.position = position;
		this.textRemoved = textRemoved;
	}
	
	
	public int getPosition()
	{
		return position;
	}
	
	public String getTextRemoved()
	{
		return textRemoved;
	}
	
	public int getLength()
	{
		return textRemoved.length();
	}
	
	
	public boolean revert()
	{
		if ( leaf.isRealised() )
		{
			leaf.revert_insert( position, textRemoved );
			return true;
		}
		else
		{
			return false;
		}
	}
}
