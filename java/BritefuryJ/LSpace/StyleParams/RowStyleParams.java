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


public class RowStyleParams extends AbstractBoxStyleParams
{
	public static final RowStyleParams defaultStyleParams = new RowStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, 0.0 );


	public RowStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor, double spacing)
	{
		super( hAlign, vAlign, background, hoverBackground, pointerCursor, spacing );
	}
}
