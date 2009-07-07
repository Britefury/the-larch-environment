//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;

import BritefuryJ.DocPresent.DPLink;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class SystemRootPage extends Page
{
	protected SystemRootPage()
	{
		SystemLocationResolver.getSystemResolver().registerPage( "system", this );
	}
	
	
	
	public DPWidget getContentsElement()
	{
		VBoxStyleSheet pageBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 40.0, false, 10.0 );
		DPVBox pageBox = new DPVBox( pageBoxStyle );
		
		VBoxStyleSheet titleBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 0.0, false, 0.0 );
		DPVBox titleBox = new DPVBox( titleBoxStyle );
		
		TextStyleSheet titleStyle = new TextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPText title = new DPText( titleStyle, "gSym System Page" );
		titleBox.append( title );
		
		pageBox.append( titleBox );
		pageBox.append( createContents() );
		
		return pageBox;
	}

	
	protected DPWidget createContents()
	{
		VBoxStyleSheet contentsBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 0.0, false, 0.0 );
		DPVBox contentsBox = new DPVBox( contentsBoxStyle );
		
		TextStyleSheet titleStyle = new TextStyleSheet( new Font( "Serif", Font.BOLD, 18 ), Color.BLACK );
		DPText title = new DPText( titleStyle, "Tests:" );
		contentsBox.append( title );
		
		for (SystemPage page: SystemDirectory.getTestPages())
		{
			contentsBox.append( page.createLink() );
		}
		
		return contentsBox;
	}



	protected DPLink createLink(String linkText)
	{
		return new DPLink( linkText, "system" );
	}
}
