//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Color;
import java.awt.Cursor;
import java.util.List;

import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;

public class ShapeStyleParams extends ContentLeafStyleParams
{
	public static final ShapeStyleParams defaultStyleParams = new ShapeStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, new FillPainter( Color.black ), null );
	private Painter painter, hoverPainter;
	
	
	public ShapeStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor, Painter painter, Painter hoverPainter)
	{
		super( hAlign, vAlign, background, hoverBackground, pointerCursor );
		
		this.painter = painter;
		this.hoverPainter = hoverPainter;
	}
	
	
	public Painter getPainter()
	{
		return painter;
	}

	public Painter getHoverPainter()
	{
		return hoverPainter;
	}



	protected void buildFieldList(List<Object> fields)
	{
		super.buildFieldList( fields );
		fields.add( new HorizontalField( "Painter", Pres.coerceNonNull( painter ) ) );
		fields.add( new HorizontalField( "Hover painter", Pres.coerceNonNull( hoverPainter ) ) );
	}
}
