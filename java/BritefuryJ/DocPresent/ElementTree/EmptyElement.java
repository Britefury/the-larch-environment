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
	
	
	
	public String getTextRepresentation()
	{
		return "";
	}
	
	public int getTextRepresentationLength()
	{
		return 0;
	}
}
