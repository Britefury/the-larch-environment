//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;

import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.LSpace.PersistentState.PersistentState;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.SpaceBin;
import BritefuryJ.Pres.Primitive.Viewport;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.StyleSheet.StyleSheet;

public class ViewportTestPage extends TestPage
{
	protected ViewportTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Viewport test";
	}
	
	protected String getDescription()
	{
		return "The viewport element allows zooming and panning of its contents. ALT+left drag to pan, ALT+right drag or ALT+scroll wheel to zoom."; 
	}

	
	static String textBlock = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	
	protected Pres createContents()
	{
		StyleSheet blackText = StyleSheet.style( Primitive.fontSize.as( 12 ), Primitive.foreground.as( Color.black ) );

		SolidBorder viewportBorder = new SolidBorder( 2.0, 2.0, 5.0, 5.0, Color.black, null ); 
		
		Pres b2 = blackText.applyTo( new NormalText( textBlock ) );
		
		Pres viewport = new SpaceBin( 0.0, 200.0, new Viewport( b2, new PersistentState() ) );
		Pres border = viewportBorder.surround( viewport );
		Pres column = StyleSheet.style( Primitive.columnSpacing.as( 5.0 ) ).applyTo( new Column( new Pres[] { new Label( "Viewport:" ).alignHPack().alignVRefY(), border } ) );
		return column.pad( 50.0, 50.0 ).alignHExpand().alignVExpand();
	}
}
