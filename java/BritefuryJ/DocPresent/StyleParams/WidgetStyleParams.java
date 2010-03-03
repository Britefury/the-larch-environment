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

public class WidgetStyleParams
{
	public static final WidgetStyleParams defaultStyleParams = new WidgetStyleParams( null, null, null );
	
	
	private final Painter background, hoverBackground;
	private final Cursor cursor;
	
	
	public WidgetStyleParams(Painter background, Painter hoverBackground, Cursor pointerCursor)
	{
		this.background = background;
		this.hoverBackground = hoverBackground;
		this.cursor = pointerCursor;
	}
	
	
	public Painter getBackground()
	{
		return background;
	}
	
	public Painter getHoverBackground()
	{
		return hoverBackground;
	}
	
	public Cursor getCursor()
	{
		return cursor;
	}
}
