//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;


public class ParagraphStyleSheet extends ContainerStyleSheet
{
	public static final ParagraphStyleSheet defaultStyleSheet = new ParagraphStyleSheet();
	
	
	private final double spacing, lineSpacing, indentation;
	
	
	public ParagraphStyleSheet()
	{
		this( 0.0, 0.0, 0.0 );
	}
	
	public ParagraphStyleSheet(double spacing, double lineSpacing, double indentation)
	{
		super();
		
		this.spacing = spacing;
		this.lineSpacing = lineSpacing;
		this.indentation = indentation;
	}
	
	
	
	public double getSpacing()
	{
		return spacing;
	}

	public double getLineSpacing()
	{
		return lineSpacing;
	}

	public double getIndentation()
	{
		return indentation;
	}
}
