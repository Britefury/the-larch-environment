//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Cursor;

import BritefuryJ.DocPresent.Painter.Painter;


public class ParagraphStyleParams extends ContainerStyleParams
{
	public static final ParagraphStyleParams defaultStyleParams = new ParagraphStyleParams( null, null, null, 0.0, 0.0, 0.0 );
	
	
	private final double spacing, lineSpacing, indentation;


	public ParagraphStyleParams(Painter background, Painter hoverBackground, Cursor pointerCursor, double spacing, double lineSpacing, double indentation)
	{
		super( background, hoverBackground, pointerCursor );
		
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
