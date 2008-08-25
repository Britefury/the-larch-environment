package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;

public class TextElement extends LeafElement
{
	public TextElement(TextStyleSheet styleSheet, String text)
	{
		super( new DPText( styleSheet, text ) );
	}



	public DPText getWidget()
	{
		return (DPText)widget;
	}
}
