//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

public class StaticTextStyleParams extends WidgetStyleParams
{
	private static final Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );

	
	public static final StaticTextStyleParams defaultStyleParams = new StaticTextStyleParams();
	
	
	
	protected final Font font;
	protected final Paint textPaint;
	protected final boolean bMixedSizeCaps;


	public StaticTextStyleParams()
	{
		this( defaultFont, Color.black, false );
	}
	
	public StaticTextStyleParams(Font font, Paint textPaint)
	{
		this( font, textPaint, false );
	}
	
	public StaticTextStyleParams(Font font, Paint textPaint, boolean bMixedSizeCaps)
	{
		super();
		
		this.font = font;
		this.textPaint = textPaint;
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
	
	
	public boolean getMixedSizeCaps()
	{
		return bMixedSizeCaps;
	}
}