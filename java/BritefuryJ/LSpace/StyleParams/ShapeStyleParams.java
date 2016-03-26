//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.StyleParams;

import java.awt.Color;
import java.awt.Cursor;
import java.util.List;

import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;

public class ShapeStyleParams extends ElementStyleParams
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
		fields.add( new HorizontalField( "Painter", Pres.coercePresentingNull(painter) ) );
		fields.add( new HorizontalField( "Hover painter", Pres.coercePresentingNull(hoverPainter) ) );
	}
}
