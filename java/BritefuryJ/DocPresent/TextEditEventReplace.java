//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

public class TextEditEventReplace extends TextEditEvent
{
	protected int position;
	protected String originalText, replacement;
	
	public TextEditEventReplace(DPContentLeaf leaf, DPContentLeaf prevNeighbour, DPContentLeaf nextNeighbour, int position, String originalText, String replacement)
	{
		super( leaf, prevNeighbour, nextNeighbour );
		
		this.position = position;
		this.originalText = originalText;
		this.replacement = replacement;
	}
	
	
	public int getPosition()
	{
		return position;
	}
	
	public String getOriginalText()
	{
		return originalText;
	}
	
	public int getLength()
	{
		return originalText.length();
	}
	
	public String getReplacement()
	{
		return replacement;
	}
	
	
	public boolean revert()
	{
		if ( leaf.isRealised() )
		{
			leaf.revert_replace( position, replacement.length(), originalText );
			return true;
		}
		else
		{
			return false;
		}
	}
}
