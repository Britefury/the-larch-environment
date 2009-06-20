//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;

public class TextElement extends EditableEntryLeafElement
{
	public TextElement(String text)
	{
		this( TextStyleSheet.defaultStyleSheet, text );
	}

	public TextElement(TextStyleSheet styleSheet, String text)
	{
		super( new DPText( styleSheet, text ), text );
	}

	public TextElement(TextStyleSheet styleSheet, String text, String textRepresentation)
	{
		super( new DPText( styleSheet, text, textRepresentation ), textRepresentation );
	}



	public DPText getWidget()
	{
		return (DPText)widget;
	}
	
	
	

	//
	// Meta-element
	//
	
	public DPWidget createMetaHeaderData()
	{
		return new DPText( "'" + getWidget().getText() + "'" );
	}
}
