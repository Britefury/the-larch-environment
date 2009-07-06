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

public class HBoxTypesetTestPage extends SystemPage
{
	protected static void initialise()
	{
		new HBoxTypesetTestPage().register( "hboxts" );
	}
	
	
	private HBoxTypesetTestPage()
	{
	}
	
	
	protected String getTitle()
	{
		return "H-Box typeset test";
	}

	
	protected static DPText[] makeTexts(String header)
	{
		TextStyleSheet t12 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t0 = new DPText( t12, "Hello" );
		DPText t1 = new DPText( t12, "World" );
		DPText t2 = new DPText( t12, "Foo" );
		DPText t3 = new DPText( t12, "Bar" );
		
		DPText[] texts = { t0, t1, t2, t3 };
		return texts;
	}
	
	
	protected static DPHBox makeTypesetHBox(VTypesetting typesetting, String header)
	{
		DPText[] txt = makeTexts( header );
		VBoxStyleSheet vs = new VBoxStyleSheet( typesetting, HAlignment.LEFT, 0.0, false, 0.0 );
		DPVBox v = new DPVBox( vs );
		v.extend( txt );
		TextStyleSheet t18 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText before = new DPText( t18, header );
		DPText after = new DPText( t18, " After" );
		HBoxStyleSheet ts = new HBoxStyleSheet( VAlignment.BASELINES, 0.0, false, 0.0 );
		DPHBox t = new DPHBox( ts );
		t.append( before );
		t.append( v );
		t.append( after );
		return t;
	}
	
	
	protected DPWidget createContents()
	{
		DPHBox t0 = makeTypesetHBox( VTypesetting.NONE, "NONE" );
		DPHBox t1 = makeTypesetHBox( VTypesetting.ALIGN_WITH_TOP, "ALIGN_WITH_TOP" );
		DPHBox t2 = makeTypesetHBox( VTypesetting.ALIGN_WITH_BOTTOM, "ALIGN_WITH_BOTTOM" );
		
		VBoxStyleSheet boxs = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 20.0, false, 0.0 );
		DPVBox box = new DPVBox( boxs );
		box.append( t0 );
		box.append( t1 );
		box.append( t2 );
		
		return box;
	}
}
