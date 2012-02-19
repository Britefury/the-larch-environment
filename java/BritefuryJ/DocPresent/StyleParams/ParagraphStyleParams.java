//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Cursor;
import java.util.List;

import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.Graphics.Painter;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;


public class ParagraphStyleParams extends ContainerStyleParams
{
	public static final ParagraphStyleParams defaultStyleParams = new ParagraphStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, 0.0, 0.0, 0.0 );
	
	
	private final double spacing, lineSpacing, indentation;


	public ParagraphStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor, double spacing, double lineSpacing, double indentation)
	{
		super( hAlign, vAlign, background, hoverBackground, pointerCursor );
		
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
	
	
	
	protected void buildFieldList(List<Object> fields)
	{
		super.buildFieldList( fields );
		fields.add( new HorizontalField( "Spacing", Pres.coerceNonNull( spacing ) ) );
		fields.add( new HorizontalField( "Line spacing", Pres.coerceNonNull( lineSpacing ) ) );
		fields.add( new HorizontalField( "Indentation", Pres.coerceNonNull( indentation ) ) );
	}
}
