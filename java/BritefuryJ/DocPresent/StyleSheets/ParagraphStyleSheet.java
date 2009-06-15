//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import BritefuryJ.DocPresent.Layout.VAlignment;

public class ParagraphStyleSheet extends ContainerStyleSheet
{
	public static ParagraphStyleSheet defaultStyleSheet = new ParagraphStyleSheet();
	
	
	private double spacing, vSpacing, padding, indentation;
	private VAlignment alignment;
	
	
	public ParagraphStyleSheet()
	{
		this( VAlignment.BASELINES, 0.0, 0.0, 0.0, 0.0 );
	}
	
	public ParagraphStyleSheet(VAlignment alignment, double spacing, double vSpacing, double padding, double indentation)
	{
		super();
		
		this.alignment = alignment;
		this.spacing = spacing;
		this.vSpacing = vSpacing;
		this.padding = padding;
		this.indentation = indentation;
	}
	
	
	
	public VAlignment getAlignment()
	{
		return alignment;
	}

	public double getSpacing()
	{
		return spacing;
	}

	public double getVSpacing()
	{
		return vSpacing;
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
