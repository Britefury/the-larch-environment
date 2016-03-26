//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.StyleParams;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Paint;
import java.util.List;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;

public class LabelStyleParams extends ElementStyleParams
{
	public static final Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );

	
	public static final LabelStyleParams defaultStyleParams = new LabelStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, defaultFont, Color.black, null, null, false, false, false );
	
	
	
	protected final Font font;
	protected final Paint textPaint, hoverTextPaint, squiggleUnderlinePaint;
	protected final boolean bUnderline, bStrikethrough, bMixedSizeCaps;


	public LabelStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor,
			Font font, Paint textPaint, Paint hoverTextPaint,
			Paint squiggleUnderlinePaint, boolean bUnderline, boolean bStrikethrough, boolean bMixedSizeCaps)
	{
		super( hAlign, vAlign, background, hoverBackground, pointerCursor );
		
		this.font = font;
		this.textPaint = textPaint;
		this.hoverTextPaint = hoverTextPaint;
		this.squiggleUnderlinePaint = squiggleUnderlinePaint;
		this.bUnderline = bUnderline;
		this.bStrikethrough = bStrikethrough;
		this.bMixedSizeCaps = bMixedSizeCaps;
	}


	public Font getFont()
	{
		return font;
	}
	
	
	public Paint getTextPaint()
	{
		return textPaint;
	}
	
	public Paint getHoverTextPaint()
	{
		return hoverTextPaint;
	}
	
	
	public Paint getSquiggleUnderlinePaint()
	{
		return squiggleUnderlinePaint;
	}
	
	
	public boolean getUnderline()
	{
		return bUnderline;
	}
	
	public boolean getStrikethrough()
	{
		return bStrikethrough;
	}
	
	public boolean getMixedSizeCaps()
	{
		return bMixedSizeCaps;
	}
	
	
	
	protected void buildFieldList(List<Object> fields)
	{
		super.buildFieldList( fields );
		fields.add( new HorizontalField( "Font", Pres.coercePresentingNull(font) ) );
		fields.add( new HorizontalField( "Text paint", Pres.coercePresentingNull(textPaint) ) );
		fields.add( new HorizontalField( "Hover text paint", Pres.coercePresentingNull(hoverTextPaint) ) );
		fields.add( new HorizontalField( "Squiggle underline paint", Pres.coercePresentingNull(squiggleUnderlinePaint) ) );
		fields.add( new HorizontalField( "Underline", Pres.coercePresentingNull(bUnderline) ) );
		fields.add( new HorizontalField( "Strikethrough", Pres.coercePresentingNull(bStrikethrough) ) );
		fields.add( new HorizontalField( "Mixed size caps", Pres.coercePresentingNull(bMixedSizeCaps) ) );
	}
}
