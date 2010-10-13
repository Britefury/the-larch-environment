//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.Column;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.SpaceBin;
import BritefuryJ.DocPresent.Combinators.Primitive.Viewport;
import BritefuryJ.DocPresent.Combinators.RichText.NormalText;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class ViewportTestPage extends SystemPage
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
		return "The viewport element allows zooming and panning of its contents."; 
	}

	
	static String textBlock = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	
	protected Pres createContents()
	{
		StyleSheet blackText = StyleSheet.instance.withAttr( Primitive.fontSize, 12 ).withAttr( Primitive.foreground, Color.black );
		
		StyleSheet borderStyle = StyleSheet.instance.withAttr( Primitive.border,  new SolidBorder( 2.0, 2.0, 5.0, 5.0, Color.black, null ) );
		
		Pres b2 = blackText.applyTo( new NormalText( textBlock ) );
		
		Pres viewport = new SpaceBin( new Viewport( b2, new PersistentState() ).alignHExpand().alignVExpand(), 0.0, 200.0 );
		Pres border = borderStyle.applyTo( new Border( viewport.alignHExpand().alignVExpand() ).alignHExpand().alignVExpand() );
		Pres column = StyleSheet.instance.withAttr( Primitive.columnSpacing, 5.0 ).applyTo( new Column( new Pres[] { new Label( "Viewport:" ), border } ) ).alignHExpand().alignVExpand();
		return column.pad( 50.0, 50.0 );
	}
}
