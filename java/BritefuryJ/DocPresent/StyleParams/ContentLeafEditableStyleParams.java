//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Cursor;

import BritefuryJ.DocPresent.Painter.Painter;

public class ContentLeafEditableStyleParams extends ContentLeafStyleParams
{
	public static final ContentLeafEditableStyleParams defaultStyleParams = new ContentLeafEditableStyleParams( null, null, null, true, true );
	
	private final boolean bEditable, bSelectable;
	
	
	
	public ContentLeafEditableStyleParams(Painter background, Painter hoverBackground, Cursor pointerCursor, boolean bEditable, boolean bSelectable)
	{
		super( background, hoverBackground, pointerCursor );
		
		this.bEditable = bEditable;
		this.bSelectable = bSelectable;
	}
	
	
	public boolean getEditable()
	{
		return bEditable;
	}
	
	public boolean getSelectable()
	{
		return bSelectable;
	}
}
