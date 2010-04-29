//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class TextTestPage extends SystemPage
{
	protected TextTestPage()
	{
		register( "tests.text" );
	}
	
	
	public String getTitle()
	{
		return "Text test";
	}

	protected String getDescription()
	{
		return "The text element supports mixed-caps style, and a squiggle-underline.";
	}
	
	
	
	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet smallCapsStyle = styleSheet.withTextSmallCaps( true );
	private static PrimitiveStyleSheet redUnderlineStyle = styleSheet.withTextSquiggleUnderlinePaint( Color.red );
	private static PrimitiveStyleSheet hoverStyle = styleSheet.withHoverForeground( new Color( 0.0f, 0.5f, 0.5f ) );

	
	
	protected DPElement createContents()
	{
		DPText t0 = styleSheet.text( "Normal text; with characters that go above and below the basline." );
		DPText t1 = smallCapsStyle.text( "Small caps text; with characters that go above and below the basline." );
		DPText t2 = redUnderlineStyle.text( "Normal text with squiggle-underline; with characters that go above and below the basline." );
		DPText t3 = hoverStyle.text( "Text with colour that is affected by pointer hover." );
		
		return styleSheet.vbox( new DPElement[] { t0, t1, t2, t3 } );
	}
}
