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
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class BorderTestPage extends SystemPage
{
	protected BorderTestPage()
	{
		register( "tests.border" );
	}
	
	
	protected String getTitle()
	{
		return "Border test";
	}

	protected static DPText[] makeTexts(String header)
	{
		TextStyleSheet t12 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		TextStyleSheet t18 = new TextStyleSheet( new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPText h = new DPText( t18, header );
		DPText t0 = new DPText( t12, "Hello" );
		DPText t1 = new DPText( t12, "World" );
		DPText t2 = new DPText( t12, "Foo" );
		
		DPText[] texts = { h, t0, t1, t2 };
		return texts;
	}
	
	
	protected DPWidget createContents()
	{
		DPText[] c0 = makeTexts( "LEFT" );
		DPText[] c1 = makeTexts( "CENTRE" );
		DPText[] c2 = makeTexts( "RIGHT" );
		DPText[] c3 = makeTexts( "EXPAND" );
		
		VBoxStyleSheet b0s = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 0.0, false, 0.0 );
		DPVBox b0 = new DPVBox( b0s );
		b0.extend( c0 );
		
		VBoxStyleSheet b1s = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 0.0, false, 0.0 );
		DPVBox b1 = new DPVBox( b1s );
		b1.extend( c1 );
		
		VBoxStyleSheet b2s = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.RIGHT, 0.0, false, 0.0 );
		DPVBox b2 = new DPVBox( b2s );
		b2.extend( c2 );
		
		VBoxStyleSheet b3s = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 0.0, true, 0.0 );
		DPVBox b3 = new DPVBox( b3s );
		b3.extend( c3 );
		
		
		Border b = new EmptyBorder( 20.0, 40.0, 60.0, 80.0, new Color( 0.75f, 0.75f, 1.0f ) );
		DPBorder border = new DPBorder( b );
		border.setChild( b0 );
		DPHBox hb = new DPHBox();
		hb.append( border );

		
		VBoxStyleSheet boxS = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 20.0, false, 0.0 );
		DPVBox box = new DPVBox( boxS );
		box.append( hb );
		box.append( b1 );
		box.append( b2 );
		box.append( b3 );
		
		return box;
	}
}
