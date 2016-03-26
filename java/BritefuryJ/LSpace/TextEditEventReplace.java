//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

public class TextEditEventReplace extends TextEditEvent
{
	protected int position;
	protected String originalText, replacement;
	
	public TextEditEventReplace(LSContentLeaf leaf, LSContentLeaf prevNeighbour, LSContentLeaf nextNeighbour, int position, String originalText, String replacement)
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
