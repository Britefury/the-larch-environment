package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;

public class DPHiddenContent extends DPEmpty
{
	protected String content;
	
	
	
	public DPHiddenContent()
	{
		this( WidgetStyleSheet.defaultStyleSheet, "" );
	}
	
	public DPHiddenContent(WidgetStyleSheet styleSheet)
	{
		this( styleSheet, "" );
	}
	
	public DPHiddenContent(WidgetStyleSheet styleSheet, String content)
	{
		super( styleSheet );
		
		this.content = content;
	}
	
	
	
	//
	//
	// CONTENT METHODS
	//
	//
	
	public String getContent()
	{
		return content;
	}
	
	public int getContentLength()
	{
		return content.length();
	}
	
	public void setContent(String x)
	{
		content = x;
	}
}
