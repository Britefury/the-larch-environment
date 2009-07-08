//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;

import BritefuryJ.DocPresent.Border.SolidBorder;

public class ButtonStyleSheet extends ContainerStyleSheet
{
	public static ButtonStyleSheet defaultStyleSheet = new ButtonStyleSheet();
	
	
	
	protected Color borderColour, backgroundColour, highlightBackgColour;
	protected SolidBorder border, highlightBorder;
	
	
	public ButtonStyleSheet()
	{
		this( Color.black, new Color( 0.7f, 0.8f, 1.0f ), new Color( 0.55f, 0.6f, 1.0f ) );
	}
	
	public ButtonStyleSheet(Color borderColour, Color backgroundColour, Color highlightBackgColour)
	{
		this.borderColour = borderColour;
		this.backgroundColour = backgroundColour;
		this.highlightBackgColour = highlightBackgColour;
		
		border = new SolidBorder( 1.0, 2.0, 2.0, 2.0, borderColour, backgroundColour );
		highlightBorder = new SolidBorder( 1.0, 2.0, 2.0, 2.0, borderColour, highlightBackgColour );
	}
	
	
	
	public Color getBorderColour()
	{
		return borderColour;
	}
	
	public Color getBackgroundColour()
	{
		return backgroundColour;
	}
	
	public Color getHighlightBackgroundColour()
	{
		return highlightBackgColour;
	}
	
	
	public SolidBorder getBorder()
	{
		return border;
	}

	public SolidBorder getHighlightBorder()
	{
		return highlightBorder;
	}
}
