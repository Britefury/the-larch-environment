//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Color;
import java.awt.Cursor;

import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.Painter;

public class ShapeStyleParams extends ContentLeafStyleParams
{
	public static final ShapeStyleParams defaultStyleParams = new ShapeStyleParams( null, null, null, new FillPainter( Color.black ), null );
	private Painter painter, hoverPainter;
	
	
	public ShapeStyleParams(Painter background, Painter hoverBackground, Cursor pointerCursor, Painter painter, Painter hoverPainter)
	{
		super( background, hoverBackground, pointerCursor );
		
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
}
