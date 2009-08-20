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
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class TextTestPage extends SystemPage
{
	protected TextTestPage()
	{
		register( "tests.text" );
	}
	
	
	protected String getTitle()
	{
		return "Text test";
	}

	protected DPWidget createContents()
	{
		TextStyleSheet ts0 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		TextStyleSheet ts1 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK, true );
		TextStyleSheet ts2 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK, Color.RED );
		DPText t0 = new DPText( ts0, "Hello World Abcdefghijklmnopqrstuvwxyz" );
		DPText t1 = new DPText( ts1, "Hello World Abcdefghijklmnopqrstuvwxyz" );
		DPText t2 = new DPText( ts2, "Hello World Abcdefghijklmnopqrstuvwxyz" );

		VBoxStyleSheet b0s = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 0.0, false, 0.0 );
		DPVBox b0 = new DPVBox( b0s );
		b0.append( t0 );
		b0.append( t1 );
		b0.append( t2 );
		
		return b0;
	}
}
