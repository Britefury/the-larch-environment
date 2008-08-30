package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.StyleSheets.BorderStyleSheet;

public class BorderElement extends BinElement
{
	public BorderElement()
	{
		this( BorderStyleSheet.defaultStyleSheet );
	}
	
	public BorderElement(BorderStyleSheet styleSheet)
	{
		super( new DPBorder( styleSheet ) );
	}
	
	
	public DPBorder getWidget()
	{
		return (DPBorder)widget;
	}
}
