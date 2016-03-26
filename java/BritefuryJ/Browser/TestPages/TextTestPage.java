//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.StyleSheet.StyleSheet;

public class TextTestPage extends TestPage
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
	private static StyleSheet smallCapsStyle = styleSheet.withValues( Primitive.fontSmallCaps.as( true ) );
	private static StyleSheet redUnderlineStyle = styleSheet.withValues( Primitive.textSquiggleUnderlinePaint.as( Color.red ) );
	private static StyleSheet hoverStyle = styleSheet.withValues( Primitive.hoverForeground.as( new Color( 0.0f, 0.5f, 0.5f ) ) );

	
	
	protected Pres createContents()
	{
		Pres t0 = styleSheet.applyTo( new Text( "Hello world" ) );
		Pres t1 = styleSheet.applyTo( new Text( "Normal text; with characters that go above and below the basline." ) );
		Pres t2 = smallCapsStyle.applyTo( new Text( "Small caps text; with characters that go above and below the basline. Would Normally Be Used For a Title." ) );
		Pres t3 = redUnderlineStyle.applyTo( new Text( "Normal text with squiggle-underline; with characters that go above and below the basline." ) );
		Pres t4 = hoverStyle.applyTo( new Text( "Text with colour that is affected by pointer hover." ) );
		
		return new Body( new Pres[] { t0, t1, t2, t3, t4 } );
	}
}
