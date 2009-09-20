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
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class HBoxTestPage extends SystemPage
{
	protected HBoxTestPage()
	{
		register( "tests.hbox" );
	}
	
	
	public String getTitle()
	{
		return "H-Box test";
	}
	
	protected String getDescription()
	{
		return "The HBox element arranges its child elements in a horizontal box."; 
	}

	
	private static SolidBorder outline = new SolidBorder( 1.0, 0.0, new Color( 0.0f, 0.3f, 0.7f ), null );

	
	protected static DPStaticText[] makeTexts(String header)
	{
		StaticTextStyleSheet t12 = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		StaticTextStyleSheet t18 = new StaticTextStyleSheet( new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPStaticText h = new DPStaticText( t18, header );
		DPStaticText t0 = new DPStaticText( t12, "Hello" );
		DPStaticText t1 = new DPStaticText( t12, "World" );
		DPStaticText t2 = new DPStaticText( t12, "Foo" );
		DPStaticText t3 = new DPStaticText( t12, "j" );
		DPStaticText t4 = new DPStaticText( t12, "q" );
		DPStaticText t5 = new DPStaticText( t12, "'" );
		DPStaticText t6 = new DPStaticText( t12, "." );
		DPStaticText t7 = new DPStaticText( t12, "Bar" );
		
		DPStaticText[] texts = { h, t0, t1, t2, t3, t4, t5, t6, t7 };
		return texts;
	}
	
	protected DPWidget wrapInOutline(DPWidget w)
	{
		DPBorder border = new DPBorder( outline );
		border.setChild( w );
		return border;
	}
	
	protected DPWidget makeText(String text, int size)
	{
		StaticTextStyleSheet ts = new StaticTextStyleSheet( new Font( "Sans serif", Font.BOLD, size ), Color.BLACK );

		DPStaticText t = new DPStaticText( ts, text );
		return wrapInOutline( t );
	}
	
	
	protected DPWidget createContents()
	{
		DPHBox b0 = new DPHBox();
		b0.extend( makeTexts( "HBOX-TEST" ) );
		
		DPHBox b1 = new DPHBox();
		b1.append( makeText( "a", 24 ).alignVBaselines() );
		b1.append( makeText( "g", 24 ).alignVBaselines() );
		b1.append( makeText( "v_baselines", 18 ).alignVBaselines() );
		b1.append( makeText( "v_baselines48", 48 ).alignVBaselines() );
		b1.append( makeText( "v_baselines-expand", 18 ).alignVBaselinesExpand() );
		b1.append( makeText( "v_top", 18 ).alignVTop() );
		b1.append( makeText( "v_centre", 18 ).alignVCentre() );
		b1.append( makeText( "v_bottom", 18 ).alignVBottom() );
		b1.append( makeText( "v_expand", 18 ).alignVExpand() );
		
		VBoxStyleSheet boxS = new VBoxStyleSheet( VTypesetting.NONE, 20.0 );
		DPVBox box = new DPVBox( boxS );
		box.append( b0.alignHExpand() );
		box.append( wrapInOutline( b1 ) );
		
		return box;
	}
}
