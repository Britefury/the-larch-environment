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
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class HBoxTypesetTestPage extends SystemPage
{
	protected HBoxTypesetTestPage()
	{
		register( "tests.hboxts" );
	}
	
	
	protected String getTitle()
	{
		return "H-Box typeset test";
	}

	
	protected static DPText[] makeTexts(String header)
	{
		DPText t0 = new DPText( "Hello" );
		DPText t1 = new DPText( "World" );
		DPText t2 = new DPText( "Foo" );
		DPText t3 = new DPText( "Bar" );
		
		DPText[] texts = { t0, t1, t2, t3 };
		return texts;
	}
	
	
	protected static DPHBox makeTypesetHBox(VTypesetting typesetting, String header)
	{
		DPText[] txt = makeTexts( header );
		VBoxStyleSheet vs = new VBoxStyleSheet( typesetting, 0.0 );
		DPVBox v = new DPVBox( vs );
		v.extend( txt );
		TextStyleSheet t18 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 18 ), new Color( 0.0f, 0.3f, 0.6f ) );
		DPText before = new DPText( t18, header );
		DPText after = new DPText( t18, " After" );
		DPHBox t = new DPHBox();
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
		
		VBoxStyleSheet boxs = new VBoxStyleSheet( VTypesetting.NONE, 20.0 );
		DPVBox box = new DPVBox( boxs );
		box.append( t0 );
		box.append( t1 );
		box.append( t2 );
		
		return box;
	}
}
