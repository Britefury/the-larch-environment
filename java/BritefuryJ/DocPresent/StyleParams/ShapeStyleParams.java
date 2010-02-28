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
	public static final ShapeStyleParams defaultStyleParams = new ShapeStyleParams( null, null, new FillPainter( Color.black ) );
	private Painter painter;
	
	
	public ShapeStyleParams(Painter background, Cursor pointerCursor, Painter painter)
	{
		super( background, pointerCursor );
		
		this.painter = painter;
	}
	
	
	public Painter getPainter()
	{
		return painter;
	}
}
