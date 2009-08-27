//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class VBoxTestPage extends SystemPage
{
	protected VBoxTestPage()
	{
		register( "tests.vbox" );
	}
	
	
	private static SolidBorder outline = new SolidBorder( 1.0, 0.0, new Color( 0.0f, 0.3f, 0.7f ), null );

	protected DPWidget wrapInOutline(DPWidget w)
	{
		DPBorder border = new DPBorder( outline );
		border.setChild( w );
		return border;
	}
	
	
	protected static DPWidget makeTextOnGrey(String text)
	{
		DPStaticText t = new DPStaticText( text );
		
		EmptyBorder b = new EmptyBorder( new Color( 0.8f, 0.8f, 0.8f ) );
		DPBorder border = new DPBorder( b );
		border.setChild( t );
		return border;
	}

	
	protected String getTitle()
	{
		return "V-Box test";
	}

	protected static DPStaticText[] makeTexts(String header)
	{
		StaticTextStyleSheet t12 = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		StaticTextStyleSheet t18 = new StaticTextStyleSheet( new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPStaticText h = new DPStaticText( t18, header );
		DPStaticText t0 = new DPStaticText( t12, "Hello" );
		DPStaticText t1 = new DPStaticText( t12, "World" );
		DPStaticText t2 = new DPStaticText( t12, "Foo" );
		
		DPStaticText[] texts = { h, t0, t1, t2 };
		return texts;
	}

	protected DPWidget createContents()
	{
		StaticTextStyleSheet t36 = new StaticTextStyleSheet( new Font( "Sans serif", Font.BOLD, 36 ), Color.BLACK );

		DPStaticText[] c0 = makeTexts( "TEST" );
		
		DPVBox b0 = new DPVBox();
		b0.extend( c0 );
		
		VBoxStyleSheet b1s = new VBoxStyleSheet( VTypesetting.NONE, 10.0 );
		DPVBox b1 = new DPVBox( b1s );
		b1.append( new DPStaticText( t36, "VBOX ALIGNMENT" ) );
		b1.append( makeTextOnGrey( "Left" ).alignHLeft() );
		b1.append( makeTextOnGrey( "Centre" ).alignHCentre() );
		b1.append( makeTextOnGrey( "Right" ).alignHRight() );
		b1.append( makeTextOnGrey( "Expand" ).alignHExpand() );
				
		VBoxStyleSheet boxS = new VBoxStyleSheet( VTypesetting.NONE, 20.0 );
		DPVBox box = new DPVBox( boxS );
		box.append( wrapInOutline( b0 ) );
		box.append( wrapInOutline( b1 ) );
		
		return box;
	}
}
