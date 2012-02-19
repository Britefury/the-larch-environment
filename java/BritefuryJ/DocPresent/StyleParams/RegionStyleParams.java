//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Cursor;

import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.Graphics.Painter;

public class RegionStyleParams extends ContainerStyleParams
{
	public static final RegionStyleParams defaultStyleParams = new RegionStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, true, true );

	private final boolean editable, selectable;

	
	public RegionStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor, boolean editable, boolean selectable)
	{
		super( hAlign, vAlign, background, hoverBackground, pointerCursor );
		
		this.editable = editable;
		this.selectable = selectable;
	}


	public boolean getEditable()
	{
		return editable;
	}
	
	public boolean getSelectable()
	{
		return selectable;
	}
}
