package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPWhitespace;

public class WhitespaceElement extends LeafElement
{
	public WhitespaceElement(String whitespace)
	{
		this( whitespace, 0.0 );
	}

	public WhitespaceElement(String whitespace, double width)
	{
		super( new DPWhitespace( whitespace, width ) );
	}



	public DPWhitespace getWidget()
	{
		return (DPWhitespace)widget;
	}
}
