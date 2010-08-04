//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class HBoxTestPage extends SystemPage
{
	protected HBoxTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "H-Box test";
	}
	
	protected String getDescription()
	{
		return "The HBox element arranges its child elements in a horizontal box."; 
	}

	
	private static StyleSheet styleSheet = StyleSheet.instance;
	private static StyleSheet outlineStyleSheet = styleSheet.withAttr( Primitive.border, new SolidBorder( 1.0, 0.0, new Color( 0.0f, 0.3f, 0.7f ), null ) );

	
	protected Pres makeText(String text, int size)
	{
		StyleSheet styleSheet = StyleSheet.instance.withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontSize, size );
		return outlineStyleSheet.applyTo( new Border( styleSheet.applyTo( new StaticText( text ) ) ) );
	}
	
	
	protected Pres createHBox1()
	{
		ArrayList<Object> children = new ArrayList<Object>();
		children.add( makeText( "a", 24 ).alignVRefY() );
		children.add( makeText( "g", 24 ).alignVRefY() );
		children.add( makeText( "v_ref_y", 18 ).alignVRefY() );
		children.add( makeText( "v_ref_y48", 48 ).alignVRefY() );
		children.add( makeText( "v_ref_y-expand", 18 ).alignVRefYExpand() );
		children.add( makeText( "v_top", 18 ).alignVTop() );
		children.add( makeText( "v_centre", 18 ).alignVCentre() );
		children.add( makeText( "v_bottom", 18 ).alignVBottom() );
		children.add( makeText( "v_expand", 18 ).alignVExpand() );
		
		return outlineStyleSheet.applyTo( new Border( new HBox( children ).alignHExpand() ) ).alignHExpand().pad( 10.0, 20.0 );
	}

	protected Pres createHBox2()
	{
		ArrayList<Object> children = new ArrayList<Object>();
		children.add( makeText( "h_pack", 18 ).alignVRefY() );
		children.add( makeText( "h_left", 18 ).alignHLeft() );
		children.add( makeText( "h_centre", 18 ).alignHCentre() );
		children.add( makeText( "h_right", 18 ).alignHRight() );
		children.add( makeText( "h_expand", 18 ).alignHExpand() );
		
		return outlineStyleSheet.applyTo( new Border( new HBox( children ).alignHExpand() ) ).alignHExpand().pad( 10.0, 20.0 );
	}

	
	protected Pres createContents()
	{
		return new Body( new Pres[] { createHBox1().alignHExpand(), createHBox2().alignHExpand() } ).alignHExpand();
	}
}
