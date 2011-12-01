//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

public class TextEditEventRemove extends TextEditEvent
{
	protected int position;
	protected String textRemoved;
	
	public TextEditEventRemove(DPContentLeaf leaf, DPContentLeaf prevNeighbour, DPContentLeaf nextNeighbour, int position, String textRemoved)
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
