//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

public class TextStyleSheet extends ContentLeafStyleSheet
{
	private static final Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );

	
	public static final TextStyleSheet defaultStyleSheet = new TextStyleSheet();
	
	
	
	protected final Font font;
	protected final Paint textPaint, squiggleUnderlinePaint;
	protected final boolean bMixedSizeCaps;
	
	
	public TextStyleSheet()
	{
		this( defaultFont, Color.black, null, false );
	}
	
	public TextStyleSheet(Font font, Paint textPaint)
	{
		this( font, textPaint, null, false );
	}
	
	public TextStyleSheet(Font font, Paint textPaint, boolean bMixedSizeCaps)
	{
		this( font, textPaint, null, bMixedSizeCaps );
	}

	public TextStyleSheet(Font font, Paint textPaint, Paint squiggleUnderlinePaint)
	{
		this( font, textPaint, squiggleUnderlinePaint, false );
	}
	
	public TextStyleSheet(Font font, Paint textPaint, Paint squiggleUnderlinePaint, boolean bMixedSizeCaps)
	{
		super();
		
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
