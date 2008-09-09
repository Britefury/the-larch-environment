//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;

public class TextElement extends LeafElement
{
	public TextElement(String text)
	{
		this( TextStyleSheet.defaultStyleSheet, text );
	}

	public TextElement(TextStyleSheet styleSheet, String text)
	{
		super( new DPText( styleSheet, text ) );
	}



	public DPText getWidget()
	{
		return (DPText)widget;
	}
}
