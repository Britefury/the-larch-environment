//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPEmpty;
import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;

public class HiddenContentElement extends EmptyElement
{
	private String textRepresentation;
	
	
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
	
	public HiddenContentElement(WidgetStyleSheet styleSheet, String textRepresentation)
	{
		super( new DPEmpty( styleSheet ) );
		
		this.textRepresentation = textRepresentation;
	}
	
	
	
	public DPEmpty getWidget()
	{
		return (DPEmpty)widget;
	}





	//
	//
	// TEXT REPRESENTATION METHODS
	//
	//
	
	protected void computeSubtreeTextRepresentation(StringBuilder builder)
	{
		builder.append( textRepresentation );
	}
	
	public int getTextRepresentationLength()
	{
		return textRepresentation.length();
	}
	
	public void setTextRepresentation(String x)
	{
		textRepresentation = x;
	}
}
