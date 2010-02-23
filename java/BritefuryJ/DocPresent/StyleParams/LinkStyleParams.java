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

import BritefuryJ.DocPresent.Painter.Painter;

public class LinkStyleParams extends StaticTextStyleParams
{
	private static final Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );
	public static final LinkStyleParams defaultStyleParams = new LinkStyleParams( null, defaultFont, Color.blue, false );


	public LinkStyleParams(Painter background, Font font, Paint textPaint, boolean bMixedSizeCaps)
	{
		super( background, font, textPaint, bMixedSizeCaps );
	}
}
