//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Text;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class TextTestPage extends SystemPage
{
	protected TextTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Text test";
	}

	protected String getDescription()
	{
		return "The text element supports mixed-caps style, and a squiggle-underline.";
	}
	
	
	
	private static StyleSheet styleSheet = StyleSheet.instance;
	private static StyleSheet smallCapsStyle = styleSheet.withAttr( Primitive.textSmallCaps, true );
	private static StyleSheet redUnderlineStyle = styleSheet.withAttr( Primitive.textSquiggleUnderlinePaint, Color.red );
	private static StyleSheet hoverStyle = styleSheet.withAttr( Primitive.hoverForeground, new Color( 0.0f, 0.5f, 0.5f ) );

	
	
	protected Pres createContents()
	{
		Pres t0 = styleSheet.applyTo( new Text( "Normal text; with characters that go above and below the basline." ) );
		Pres t1 = smallCapsStyle.applyTo( new Text( "Small caps text; with characters that go above and below the basline." ) );
		Pres t2 = redUnderlineStyle.applyTo( new Text( "Normal text with squiggle-underline; with characters that go above and below the basline." ) );
		Pres t3 = hoverStyle.applyTo( new Text( "Text with colour that is affected by pointer hover." ) );
		
		return new Body( new Pres[] { t0, t1, t2, t3 } );
	}
}
