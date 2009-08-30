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
import BritefuryJ.DocPresent.DPHBox;
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
	
	protected String getTitle()
	{
		return "V-Box test";
	}
	
	protected String getDescription()
	{
		return "The V-box element arranges its child elements in a vertical box. The v-box typesetting property controls the position of the baseline requested by a v-box."; 
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

	
	
	protected static DPStaticText[] makeTSTexts(String header)
	{
		DPStaticText t0 = new DPStaticText( "First item" );
		DPStaticText t1 = new DPStaticText( "Second item" );
		DPStaticText t2 = new DPStaticText( "Third item" );
		DPStaticText t3 = new DPStaticText( "Fourth item" );
		
		DPStaticText[] texts = { t0, t1, t2, t3 };
		return texts;
	}
	
	
	protected static DPHBox makeTypesetHBox(VTypesetting typesetting, String header)
	{
		DPStaticText[] txt = makeTSTexts( header );
		VBoxStyleSheet vs = new VBoxStyleSheet( typesetting, 0.0 );
		DPVBox v = new DPVBox( vs );
		v.extend( txt );
		StaticTextStyleSheet t18 = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 18 ), new Color( 0.0f, 0.3f, 0.6f ) );
		DPStaticText before = new DPStaticText( t18, header );
		DPStaticText after = new DPStaticText( t18, " After" );
		DPHBox t = new DPHBox();
		t.append( before );
		t.append( v );
		t.append( after );
		return t;
	}
	
	

	protected DPWidget createContents()
	{
		StaticTextStyleSheet t12 = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		StaticTextStyleSheet t18 = new StaticTextStyleSheet( new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		StaticTextStyleSheet t24 = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 24 ), Color.BLACK );

		DPStaticText h = new DPStaticText( t18, "VBox" );
		DPStaticText t0 = new DPStaticText( t12, "First item" );
		DPStaticText t1 = new DPStaticText( t12, "Second item" );
		DPStaticText t2 = new DPStaticText( t12, "Third item" );
		
		DPVBox vboxTest = new DPVBox();
		vboxTest.extend( new DPWidget[] { h, t0, t1, t2 } );
		
		VBoxStyleSheet b1s = new VBoxStyleSheet( VTypesetting.NONE, 10.0 );
		DPVBox hAlignTest = new DPVBox( b1s );
		hAlignTest.append( new DPStaticText( t24, "Horizontal alignment" ) );
		hAlignTest.append( makeTextOnGrey( "Left" ).alignHLeft() );
		hAlignTest.append( makeTextOnGrey( "Centre" ).alignHCentre() );
		hAlignTest.append( makeTextOnGrey( "Right" ).alignHRight() );
		hAlignTest.append( makeTextOnGrey( "Expand" ).alignHExpand() );
				
		
		DPHBox ts0 = makeTypesetHBox( VTypesetting.NONE, "NONE" );
		DPHBox ts1 = makeTypesetHBox( VTypesetting.ALIGN_WITH_TOP, "ALIGN_WITH_TOP" );
		DPHBox ts2 = makeTypesetHBox( VTypesetting.ALIGN_WITH_BOTTOM, "ALIGN_WITH_BOTTOM" );
		
		VBoxStyleSheet boxs = new VBoxStyleSheet( VTypesetting.NONE, 20.0 );
		DPVBox typesettingTest = new DPVBox( boxs );
		typesettingTest.append( new DPStaticText( t24, "VBox typesetting" ) );
		typesettingTest.append( ts0 );
		typesettingTest.append( ts1 );
		typesettingTest.append( ts2 );
		
		
		VBoxStyleSheet boxS = new VBoxStyleSheet( VTypesetting.NONE, 20.0 );
		DPVBox box = new DPVBox( boxS );
		box.append( wrapInOutline( vboxTest ) );
		box.append( wrapInOutline( hAlignTest ) );
		box.append( wrapInOutline( typesettingTest ) );
		
		return box;
	}
}
