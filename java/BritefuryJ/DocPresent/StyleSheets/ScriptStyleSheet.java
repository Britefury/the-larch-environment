package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;

public class ScriptStyleSheet extends ContainerStyleSheet
{
	public static ScriptStyleSheet defaultStyleSheet = new ScriptStyleSheet();
	
	
	private double spacing, scriptSpacing;
	
	
	public ScriptStyleSheet()
	{
		this( 1.0, 1.0, null );
	}
	
	public ScriptStyleSheet(double spacing, double scriptSpacing)
	{
		this( spacing, scriptSpacing, null );
	}
	
	public ScriptStyleSheet(double spacing, double scriptSpacing, Color backgroundColour)
	{
		super( backgroundColour );
		
		this.spacing = spacing;
		this.scriptSpacing = scriptSpacing;
	}
	
	
	
	public double getSpacing()
	{
		return this.spacing;
	}

	
	public double getScriptSpacing()
	{
		return this.scriptSpacing;
	}
}
