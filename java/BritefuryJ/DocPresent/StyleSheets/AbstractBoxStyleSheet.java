package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;

public class AbstractBoxStyleSheet extends ContainerStyleSheet
{
	public static AbstractBoxStyleSheet defaultStyleSheet = new AbstractBoxStyleSheet();
	
	
	protected double spacing, padding;
	protected boolean bExpand;


	public AbstractBoxStyleSheet()
	{
		this( 0.0, false, 0.0, null );
	}
	
	public AbstractBoxStyleSheet(Color backgroundColour)
	{
		this( 0.0, false, 0.0, backgroundColour );
	}
	
	public AbstractBoxStyleSheet(double spacing, boolean bExpand, double padding)
	{
		this( spacing, bExpand, padding, null );
	}
	
	public AbstractBoxStyleSheet(double spacing, boolean bExpand, double padding, Color backgroundColour)
	{
		super( backgroundColour );
		
		this.spacing = spacing;
		this.bExpand = bExpand;
		this.padding = padding;
	}

	
	public double getSpacing()
	{
		return spacing;
	}

	public boolean getExpand()
	{
		return bExpand;
	}

	public double getPadding()
	{
		return padding;
	}
}
