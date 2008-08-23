package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;

import BritefuryJ.DocPresent.DPHBox;

public class HBoxStyleSheet extends AbstractBoxStyleSheet
{
	public static HBoxStyleSheet defaultStyleSheet = new HBoxStyleSheet();
	
	
	protected DPHBox.Alignment alignment;


	public HBoxStyleSheet()
	{
		this( DPHBox.Alignment.CENTRE, 0.0, false, 0.0, null );
	}
	
	public HBoxStyleSheet(Color backgroundColour)
	{
		this( DPHBox.Alignment.CENTRE, 0.0, false, 0.0, backgroundColour );
	}
	
	public HBoxStyleSheet(DPHBox.Alignment alignment, double spacing, boolean bExpand, double padding)
	{
		this( alignment, spacing, bExpand, padding, null );
	}
	
	public HBoxStyleSheet(DPHBox.Alignment alignment, double spacing, boolean bExpand, double padding, Color backgroundColour)
	{
		super( spacing, bExpand, padding, backgroundColour );
		
		this.alignment = alignment;
	}

	
	public DPHBox.Alignment getAlignment()
	{
		return alignment;
	}
}
