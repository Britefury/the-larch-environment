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
