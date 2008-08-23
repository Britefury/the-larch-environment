package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;

public class ContainerStyleSheet extends WidgetStyleSheet
{
	public static ContainerStyleSheet defaultStyleSheet = new ContainerStyleSheet();
	
	
	private Color backgroundColour;
	
	
	public ContainerStyleSheet()
	{
		this( null );
	}
	
	public ContainerStyleSheet(Color backgroundColour)
	{
		super();
		
		this.backgroundColour = backgroundColour;
	}
	
	
	
	public Color getBackgroundColour()
	{
		return this.backgroundColour;
	}
}
