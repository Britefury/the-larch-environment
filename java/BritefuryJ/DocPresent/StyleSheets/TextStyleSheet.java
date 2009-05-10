//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;
import java.awt.Font;

public class TextStyleSheet extends ContentLeafStyleSheet
{
	private static Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );

	
	public static TextStyleSheet defaultStyleSheet = new TextStyleSheet();
	
	
	
	protected Font font;
	protected Color colour, squiggleUnderlineColour;
	protected boolean bMixedSizeCaps;
	
	
	public TextStyleSheet()
	{
		this( defaultFont, Color.black, null, false );
	}
	
	public TextStyleSheet(Font font, Color colour)
	{
		this( font, colour, null, false );
	}
	
	public TextStyleSheet(Font font, Color colour, boolean bMixedSizeCaps)
	{
		this( font, colour, null, bMixedSizeCaps );
	}

	public TextStyleSheet(Font font, Color colour, Color squiggleUnderlineColour)
	{
		this( font, colour, squiggleUnderlineColour, false );
	}
	
	public TextStyleSheet(Font font, Color colour, Color squiggleUnderlineColour, boolean bMixedSizeCaps)
	{
		super();
		
		this.font = font;
		this.colour = colour;
		this.squiggleUnderlineColour = squiggleUnderlineColour;
		this.bMixedSizeCaps = bMixedSizeCaps;
	}
	
	
	
	public Font getFont()
	{
		return font;
	}
	
	
	public Color getColour()
	{
		return colour;
	}
	
	
	public Color getSquiggleUnderlineColour()
	{
		return squiggleUnderlineColour;
	}
	
	
	public boolean getMixedSizeCaps()
	{
		return bMixedSizeCaps;
	}
}
