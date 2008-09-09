//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
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
