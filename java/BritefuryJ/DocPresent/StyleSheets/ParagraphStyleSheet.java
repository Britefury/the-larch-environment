package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;

import BritefuryJ.DocPresent.DPParagraph;

public class ParagraphStyleSheet extends ContainerStyleSheet
{
	public static ParagraphStyleSheet defaultStyleSheet = new ParagraphStyleSheet();
	
	
	private double spacing, padding, indentation;
	private DPParagraph.Alignment alignment;
	
	
	public ParagraphStyleSheet()
	{
		this( DPParagraph.Alignment.BASELINES, 0.0, 0.0, 0.0, null );
	}
	
	public ParagraphStyleSheet(DPParagraph.Alignment alignment, double spacing, double padding, double indentation)
	{
		this( alignment, spacing, padding, indentation, null );
	}
	
	public ParagraphStyleSheet(DPParagraph.Alignment alignment, double spacing, double padding, double indentation, Color backgroundColour)
	{
		super( backgroundColour );
		
		this.alignment = alignment;
		this.spacing = spacing;
		this.padding = padding;
		this.indentation = indentation;
	}
	
	
	
	public DPParagraph.Alignment getAlignment()
	{
		return alignment;
	}

	public double getSpacing()
	{
		return spacing;
	}

	public double getPadding()
	{
		return padding;
	}

	public double getIndentation()
	{
		return indentation;
	}
}
