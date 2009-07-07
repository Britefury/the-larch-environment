//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;

@SuppressWarnings("unchecked")
public class LinkStyleSheet extends TextStyleSheet
{
	private static Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );
	public static LinkStyleSheet defaultStyleSheet = new LinkStyleSheet();
	
	
	static
	{
		Map<TextAttribute, Object> attribs = (Map<TextAttribute, Object>)defaultFont.getAttributes();
		attribs.put( TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON );
	}


	public LinkStyleSheet()
	{
		super( defaultFont, Color.blue, null, false );
	}
	
	public LinkStyleSheet(Font font, Color colour)
	{
		super( font, colour, null, false );
	}
	
	public LinkStyleSheet(Font font, Color colour, boolean bMixedSizeCaps)
	{
		super( font, colour, null, bMixedSizeCaps );
	}

	public LinkStyleSheet(Font font, Color colour, Color squiggleUnderlineColour)
	{
		super( font, colour, squiggleUnderlineColour, false );
	}
	
	public LinkStyleSheet(Font font, Color colour, Color squiggleUnderlineColour, boolean bMixedSizeCaps)
	{
		super( font, colour, squiggleUnderlineColour, bMixedSizeCaps );
	}
}
