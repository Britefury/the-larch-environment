//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.StyleParams;

import java.awt.Cursor;
import java.util.List;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
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
		fields.add( new HorizontalField( "Spacing", Pres.coercePresentingNull(spacing) ) );
		fields.add( new HorizontalField( "Line spacing", Pres.coercePresentingNull(lineSpacing) ) );
		fields.add( new HorizontalField( "Indentation", Pres.coercePresentingNull(indentation) ) );
	}
}
