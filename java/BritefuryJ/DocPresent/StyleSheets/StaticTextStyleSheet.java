//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;
import java.awt.Font;

public class StaticTextStyleSheet extends WidgetStyleSheet
{
	private static Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );

	
	public static StaticTextStyleSheet defaultStyleSheet = new StaticTextStyleSheet();
	
	
	
	protected Font font;
	protected Color colour;
	protected boolean bMixedSizeCaps;
	
	
	public StaticTextStyleSheet()
	{
		this( defaultFont, Color.black, false );
	}
	
	public StaticTextStyleSheet(Font font, Color colour)
	{
		this( font, colour, false );
	}
	
	public StaticTextStyleSheet(Font font, Color colour, boolean bMixedSizeCaps)
	{
		super();
		
		this.font = font;
		this.colour = colour;
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
	
	
	public boolean getMixedSizeCaps()
	{
		return bMixedSizeCaps;
	}
}
