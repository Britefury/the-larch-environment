package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;

public class BorderStyleSheet extends ContainerStyleSheet
{
	public static BorderStyleSheet defaultStyleSheet = new BorderStyleSheet( 0.0, 0.0, 0.0, 0.0 );
	
	
	private double leftMargin, topMargin, rightMargin, bottomMargin;
	
	
	public BorderStyleSheet(double leftMargin, double topMargin, double rightMargin, double bottomMargin)
	{
		this( leftMargin, topMargin, rightMargin, bottomMargin, null );
	}
	
	public BorderStyleSheet(double leftMargin, double topMargin, double rightMargin, double bottomMargin, Color backgroundColour)
	{
		super( backgroundColour );
		
		this.leftMargin = leftMargin;
		this.topMargin = topMargin;
		this.rightMargin = rightMargin;
		this.bottomMargin = bottomMargin;
	}

	
	
	public double getLeftMargin()
	{
		return leftMargin;
	}

	public double getTopMargin()
	{
		return topMargin;
	}

	public double getRightMargin()
	{
		return rightMargin;
	}

	public double getBottomMargin()
	{
		return bottomMargin;
	}
}
