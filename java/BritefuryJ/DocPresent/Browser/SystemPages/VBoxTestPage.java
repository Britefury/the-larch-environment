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
import BritefuryJ.DocPresent.StyleParams.StaticTextStyleParams;
import BritefuryJ.DocPresent.StyleParams.VBoxStyleParams;

public class VBoxTestPage extends SystemPage
{
	protected VBoxTestPage()
	{
		register( "tests.vbox" );
	}
	
	public String getTitle()
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
	
	
	protected DPWidget makeTextOnGrey(String text)
	{
		DPStaticText t = new DPStaticText( text );
		
		EmptyBorder b = new EmptyBorder( new Color( 0.8f, 0.8f, 0.8f ) );
		DPBorder border = new DPBorder( b );
		border.setChild( t );
		return border;
	}

	
	
	protected DPStaticText[] makeTSTexts(String header)
	{
		DPStaticText t0 = new DPStaticText( "First item" );
		DPStaticText t1 = new DPStaticText( "Second item" );
		DPStaticText t2 = new DPStaticText( "Third item" );
		DPStaticText t3 = new DPStaticText( "Fourth item" );
		
		DPStaticText[] texts = { t0, t1, t2, t3 };
		return texts;
	}
	
	
	protected DPHBox makeRefAlignedHBox(int refPointIndex, String header)
	{
		DPStaticText[] txt = makeTSTexts( header );
		VBoxStyleParams vs = new VBoxStyleParams( null, 0.0 );
		DPVBox v = new DPVBox( vs );
		v.extend( txt );
		v.setRefPointIndex( refPointIndex );
		StaticTextStyleParams t18 = new StaticTextStyleParams( null, new Font( "Sans serif", Font.PLAIN, 18 ), new Color( 0.0f, 0.3f, 0.6f ), false );
		DPStaticText before = new DPStaticText( t18, header );
		DPStaticText after = new DPStaticText( t18, " After" );
		DPHBox t = new DPHBox( );
		t.append( before );
		t.append( v );
		t.append( after );
		return t;
	}
	
	

	protected DPWidget createContents()
	{
		StaticTextStyleParams t12 = new StaticTextStyleParams( null, new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK, false );
		StaticTextStyleParams t18 = new StaticTextStyleParams( null, new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK, false );
		StaticTextStyleParams t24 = new StaticTextStyleParams( null, new Font( "Sans serif", Font.PLAIN, 24 ), Color.BLACK, false );

		DPStaticText h = new DPStaticText( t18, "VBox" );
		DPStaticText t0 = new DPStaticText( t12, "First item" );
		DPStaticText t1 = new DPStaticText( t12, "Second item" );
		DPStaticText t2 = new DPStaticText( t12, "Third item" );
		
		DPVBox vboxTest = new DPVBox( );
		vboxTest.extend( new DPWidget[] { h, t0, t1, t2 } );
		
		VBoxStyleParams b1s = new VBoxStyleParams( null, 10.0 );
		DPVBox hAlignTest = new DPVBox( b1s );
		hAlignTest.append( new DPStaticText( t24, "Horizontal alignment" ) );
		hAlignTest.append( makeTextOnGrey( "Left" ).alignHLeft() );
		hAlignTest.append( makeTextOnGrey( "Centre" ).alignHCentre() );
		hAlignTest.append( makeTextOnGrey( "Right" ).alignHRight() );
		hAlignTest.append( makeTextOnGrey( "Expand" ).alignHExpand() );
				
		
		DPHBox ra0 = makeRefAlignedHBox( 0, "ALIGN_WITH_0" );
		DPHBox ra1 = makeRefAlignedHBox( 1, "ALIGN_WITH_1" );
		DPHBox ra2 = makeRefAlignedHBox( 2, "ALIGN_WITH_2" );
		DPHBox ra3 = makeRefAlignedHBox( 3, "ALIGN_WITH_3" );
		
		VBoxStyleParams boxs = new VBoxStyleParams( null, 20.0 );
		DPVBox refPointAlignTest = new DPVBox( boxs );
		refPointAlignTest.append( new DPStaticText( t24, "VBox reference point alignment" ) );
		refPointAlignTest.append( ra0 );
		refPointAlignTest.append( ra1 );
		refPointAlignTest.append( ra2 );
		refPointAlignTest.append( ra3 );
		
		
		VBoxStyleParams boxS = new VBoxStyleParams( null, 20.0 );
		DPVBox box = new DPVBox( boxS );
		box.append( wrapInOutline( vboxTest ) );
		box.append( wrapInOutline( hAlignTest ) );
		box.append( wrapInOutline( refPointAlignTest ) );
		
		return box;
	}
}
