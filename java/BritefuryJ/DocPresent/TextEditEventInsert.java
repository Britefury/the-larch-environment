//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

public class TextEditEventInsert extends TextEditEvent
{
	protected int position;
	protected String textInserted;
	
	public TextEditEventInsert(DPContentLeaf leaf, DPContentLeaf prevNeighbour, DPContentLeaf nextNeighbour, int position, String textInserted)
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
