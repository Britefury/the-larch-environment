//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.awt.Color;
import java.awt.Font;

import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemRootPage;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public abstract class RootPage extends Page
{
	public DPWidget getContentsElement()
	{
		VBoxStyleSheet pageBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 0.0, false, 0.0 );
		DPVBox pageBox = new DPVBox( pageBoxStyle );
		
		VBoxStyleSheet contentBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 40.0, false, 0.0 );
		DPVBox contentBox = new DPVBox( contentBoxStyle );
		
		StaticTextStyleSheet titleStyle = new StaticTextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPStaticText title = new DPStaticText( titleStyle, "Welcome to gSym" );
		
		contentBox.append( title );
		contentBox.append( getPageContents() );

		pageBox.append( SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_SYSTEMPAGE ) );

		pageBox.append( contentBox );
		
		return pageBox;
	}
	
	public abstract DPWidget getPageContents();
}
