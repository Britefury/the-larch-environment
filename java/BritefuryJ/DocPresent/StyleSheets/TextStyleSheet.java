package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;
import java.awt.Font;

public class TextStyleSheet extends ContentLeafStyleSheet
{
	private static Font defaultFont = new Font( "Sans serif", Font.PLAIN, 12 );

	
	public static TextStyleSheet defaultStyleSheet = new TextStyleSheet();
	
	
	
	protected Font font;
	protected Color colour;
	
	
	public TextStyleSheet()
	{
		this( defaultFont, Color.black );
	}
	
	public TextStyleSheet(Font font, Color colour)
	{
		super();
		
		this.font = font;
		this.colour = colour;
	}
	
	
	
	public Font getFont()
	{
		return font;
	}
	
	
	public Color getColour()
	{
		return colour;
	}
}
