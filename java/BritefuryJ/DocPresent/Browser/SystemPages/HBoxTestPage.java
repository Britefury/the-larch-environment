//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;

import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class HBoxTestPage extends SystemPage
{
	protected static void initialise()
	{
		new HBoxTestPage().register( "hbox" );
	}
	
	
	private HBoxTestPage()
	{
	}
	
	
	protected String getTitle()
	{
		return "H-Box test";
	}

	
	protected static DPText[] makeTexts(String header)
	{
		TextStyleSheet t12 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		TextStyleSheet t18 = new TextStyleSheet( new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPText h = new DPText( t18, header );
		DPText t0 = new DPText( t12, "Hello" );
		DPText t1 = new DPText( t12, "World" );
		DPText t2 = new DPText( t12, "Foo" );
		DPText t3 = new DPText( t12, "j" );
		DPText t4 = new DPText( t12, "q" );
		DPText t5 = new DPText( t12, "'" );
		DPText t6 = new DPText( t12, "." );
		DPText t7 = new DPText( t12, "Bar" );
		
		DPText[] texts = { h, t0, t1, t2, t3, t4, t5, t6, t7 };
		return texts;
	}
	
	
	protected DPWidget createContents()
	{
		DPText[] c0 = makeTexts( "TOP" );
		DPText[] c1 = makeTexts( "CENTRE" );
		DPText[] c2 = makeTexts( "BOTTOM" );
		DPText[] c3 = makeTexts( "EXPAND" );
		DPText[] c4 = makeTexts( "BASELINES" );
		
		HBoxStyleSheet b0s = new HBoxStyleSheet( VAlignment.TOP, 0.0, false, 0.0 );
		DPHBox b0 = new DPHBox( b0s );
		b0.extend( c0 );
		
		HBoxStyleSheet b1s = new HBoxStyleSheet( VAlignment.CENTRE, 0.0, false, 0.0 );
		DPHBox b1 = new DPHBox( b1s );
		b1.extend( c1 );
		
		HBoxStyleSheet b2s = new HBoxStyleSheet( VAlignment.BOTTOM, 0.0, false, 0.0 );
		DPHBox b2 = new DPHBox( b2s );
		b2.extend( c2 );
		
		HBoxStyleSheet b3s = new HBoxStyleSheet( VAlignment.EXPAND, 0.0, false, 0.0 );
		DPHBox b3 = new DPHBox( b3s );
		b3.extend( c3 );
		
		HBoxStyleSheet b4s = new HBoxStyleSheet( VAlignment.BASELINES, 0.0, false, 0.0 );
		DPHBox b4 = new DPHBox( b4s );
		b4.extend( c4 );
		
		VBoxStyleSheet boxS = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 20.0, false, 0.0 );
		DPVBox box = new DPVBox( boxS );
		box.append( b0 );
		box.append( b1 );
		box.append( b2 );
		box.append( b3 );
		box.append( b4 );
		
		return box;
	}
}
