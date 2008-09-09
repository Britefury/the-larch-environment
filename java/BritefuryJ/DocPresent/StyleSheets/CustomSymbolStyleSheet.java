//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;

public class CustomSymbolStyleSheet extends ContentLeafStyleSheet
{
	public static CustomSymbolStyleSheet defaultStyleSheet = new CustomSymbolStyleSheet();
	
	
	private Color colour;
	
	
	public CustomSymbolStyleSheet()
	{
		this( Color.black );
	}
	
	public CustomSymbolStyleSheet(Color colour)
	{
		super();
		
		this.colour = colour;
	}
	
	
	public Color getColour()
	{
		return colour;
	}
}
