//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.Controls.ScrolledViewport;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.SpaceBin;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Combinators.RichText.NormalText;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;

public class ScrolledViewportTestPage extends SystemPage
{
	protected ScrolledViewportTestPage()
	{
		register( "tests.controls.scrolledviewport" );
	}
	
	
	public String getTitle()
	{
		return "Scrolled viewport test";
	}
	
	protected String getDescription()
	{
		return "The scrolled viewport control combined a viewport with scrollbars."; 
	}

	
	static String textBlock = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	
	protected Pres createContents()
	{
		StyleSheet2 blackText = StyleSheet2.instance.withAttr( Primitive.fontSize, 12 ).withAttr( Primitive.foreground, Color.black );
		
		StyleSheet2 borderStyle = StyleSheet2.instance.withAttr( Primitive.border,  new SolidBorder( 2.0, 2.0, 5.0, 5.0, Color.black, null ) );
		
		Pres b2 = blackText.applyTo( new NormalText( textBlock ) );
		
		Pres viewport = new SpaceBin( new ScrolledViewport( b2, 0.0, 0.0, new PersistentState() ).alignHExpand().alignVExpand(), 0.0, 200.0 );
		Pres border = borderStyle.applyTo( new Border( viewport.alignHExpand().alignVExpand() ).alignHExpand().alignVExpand() );
		Pres vbox = StyleSheet2.instance.withAttr( Primitive.vboxSpacing, 5.0 ).applyTo( new VBox( new Pres[] { new StaticText( "Viewport:" ), border } ) ).alignHExpand().alignVExpand();
		return vbox.pad( 50.0, 50.0 );
	}
}
