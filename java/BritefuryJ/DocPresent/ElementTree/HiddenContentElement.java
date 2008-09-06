package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPHiddenContent;
import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;

public class HiddenContentElement extends EmptyElement
{
	public HiddenContentElement()
	{
		this( WidgetStyleSheet.defaultStyleSheet, "" );
	}
	
	public HiddenContentElement(WidgetStyleSheet styleSheet)
	{
		this( styleSheet, "" );
	}
	
	public HiddenContentElement(String content)
	{
		this( WidgetStyleSheet.defaultStyleSheet, content );
	}
	
	public HiddenContentElement(WidgetStyleSheet styleSheet, String content)
	{
		super( new DPHiddenContent( styleSheet, content ) );
	}
	
	
	
	public DPHiddenContent getWidget()
	{
		return (DPHiddenContent)widget;
	}
}
