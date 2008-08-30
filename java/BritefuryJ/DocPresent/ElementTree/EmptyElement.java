package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPEmpty;
import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;

public class EmptyElement extends Element
{
	public EmptyElement()
	{
		this( WidgetStyleSheet.defaultStyleSheet );
	}
	
	protected EmptyElement(WidgetStyleSheet styleSheet)
	{
		super( new DPEmpty( styleSheet ) );
	}
	
	
	
	public DPEmpty getWidget()
	{
		return (DPEmpty)widget;
	}
}
