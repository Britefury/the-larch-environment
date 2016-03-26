//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

public class TextEditEventInsert extends TextEditEvent
{
	protected int position;
	protected String textInserted;
	
	public TextEditEventInsert(LSContentLeaf leaf, LSContentLeaf prevNeighbour, LSContentLeaf nextNeighbour, int position, String textInserted)
	{
		super( leaf, prevNeighbour, nextNeighbour );
		
		this.position = position;
		this.textInserted = textInserted;
	}
	
	
	public int getPosition()
	{
		return position;
	}
	
	public String getTextInserted()
	{
		return textInserted;
	}
	
	
	public boolean revert()
	{
		if ( leaf.isRealised() )
		{
			leaf.revert_remove( position, textInserted.length() );
			return true;
		}
		else
		{
			return false;
		}
	}
}
