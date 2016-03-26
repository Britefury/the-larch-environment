//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.StyleParams;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.util.List;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.LSpace.Util.TextVisual;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;

public class CaretSlotStyleParams extends ContentLeafEditableStyleParams
{
	public static final Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );

	
	public static final CaretSlotStyleParams defaultStyleParams = new CaretSlotStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, new Color( 0.0f, 0.0f, 1.0f ), true, true, defaultFont );
	
	
	
	protected final Font font;
	protected final TextVisual visual;


	public CaretSlotStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor,
			Color caretColour, boolean bEditable, boolean bSelectable, Font font)
	{
		super( hAlign, vAlign, background, hoverBackground, pointerCursor, caretColour, bEditable, bSelectable );
		
		this.font = font;
		this.visual = TextVisual.getTextVisual( "", font, false, false, false );
	}


	public Font getFont()
	{
		return font;
	}
	
	public TextVisual getVisual()
	{
		return visual;
	}
	
	
	
	protected void buildFieldList(List<Object> fields)
	{
		super.buildFieldList( fields );
		fields.add( new HorizontalField( "Font", Pres.coercePresentingNull(font) ) );
	}
}
