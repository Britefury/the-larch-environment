//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;

import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;

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

	protected static DPText[] makeTexts(String header)
	{
		ElementStyleSheet t12 = DPText.styleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		ElementStyleSheet t18 = DPText.styleSheet( new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
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
		
		ElementStyleSheet b0s = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.LEFT, 0.0, false, 0.0 );
		DPVBox b0 = new DPVBox( b0s );
		b0.extend( c0 );
		
		ElementStyleSheet b1s = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.CENTRE, 0.0, false, 0.0 );
		DPVBox b1 = new DPVBox( b1s );
		b1.extend( c1 );
		
		ElementStyleSheet b2s = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.RIGHT, 0.0, false, 0.0 );
		DPVBox b2 = new DPVBox( b2s );
		b2.extend( c2 );
		
		ElementStyleSheet b3s = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.EXPAND, 0.0, true, 0.0 );
		DPVBox b3 = new DPVBox( b3s );
		b3.extend( c3 );
		
		ElementStyleSheet boxS = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.EXPAND, 20.0, false, 0.0 );
		DPVBox box = new DPVBox( boxS );
		box.append( b0 );
		box.append( b1 );
		box.append( b2 );
		box.append( b3 );
		
		return box;
	}
}
