//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import BritefuryJ.DocPresent.Painter.Painter;

public class TextStyleParams extends ContentLeafEditableStyleParams
{
	public static final Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );

	
	public static final TextStyleParams defaultStyleParams = new TextStyleParams( null, true, defaultFont, Color.black, null, false );
	
	
	
	protected final Font font;
	protected final Paint textPaint, squiggleUnderlinePaint;
	protected final boolean bMixedSizeCaps;


	public TextStyleParams(Painter background, boolean bEditable, Font font, Paint textPaint, Paint squiggleUnderlinePaint, boolean bMixedSizeCaps)
	{
		super( background, bEditable );
		
		this.font = font;
		this.textPaint = textPaint;
		this.squiggleUnderlinePaint = squiggleUnderlinePaint;
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
	
	
	public Paint getSquiggleUnderlinePaint()
	{
		return squiggleUnderlinePaint;
	}
	
	
	public boolean getMixedSizeCaps()
	{
		return bMixedSizeCaps;
	}
}
