//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.StyleParams;

import java.awt.Cursor;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;

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
