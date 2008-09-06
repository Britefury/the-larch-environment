package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPEmpty;
import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;

public class EmptyElement extends Element
{
	public EmptyElement()
	{
		this( WidgetStyleSheet.defaultStyleSheet );
	}
	
	public EmptyElement(WidgetStyleSheet styleSheet)
	{
		this( new DPEmpty( styleSheet ) );
	}
	
	protected EmptyElement(DPEmpty widget)
	{
		super( widget );
	}
	
	
	
	public DPEmpty getWidget()
	{
		return (DPEmpty)widget;
	}
}
