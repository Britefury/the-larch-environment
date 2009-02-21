//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import BritefuryJ.DocPresent.DPParagraph;

public class ParagraphStyleSheet extends ContainerStyleSheet
{
	public static ParagraphStyleSheet defaultStyleSheet = new ParagraphStyleSheet();
	
	
	private double spacing, padding, indentation;
	private DPParagraph.Alignment alignment;
	
	
	public ParagraphStyleSheet()
	{
		this( DPParagraph.Alignment.BASELINES, 0.0, 0.0, 0.0 );
	}
	
	public ParagraphStyleSheet(DPParagraph.Alignment alignment, double spacing, double padding, double indentation)
	{
		super();
		
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
