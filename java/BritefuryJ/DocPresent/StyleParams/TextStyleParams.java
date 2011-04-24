//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Paint;

import BritefuryJ.DocPresent.Painter.Painter;

public class TextStyleParams extends ContentLeafEditableStyleParams
{
	public static final Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );

	
	public static final TextStyleParams defaultStyleParams = new TextStyleParams( null, null, null, true, true, defaultFont, Color.black, null, null, false, false, false );
	
	
	
	protected final Font font;
	protected final Paint textPaint, hoverTextPaint, squiggleUnderlinePaint;
	protected final boolean bUnderline, bStrikethrough, bMixedSizeCaps;


	public TextStyleParams(Painter background, Painter hoverBackground, Cursor pointerCursor, boolean bEditable, boolean bSelectable, Font font, Paint textPaint, Paint hoverTextPaint,
			Paint squiggleUnderlinePaint, boolean bUnderline, boolean bStrikethrough, boolean bMixedSizeCaps)
	{
		super( background, hoverBackground, pointerCursor, bEditable, bSelectable );
		
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
}
