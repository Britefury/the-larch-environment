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
import BritefuryJ.DocPresent.DPLink;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class SystemRootPage extends Page
{
	protected SystemRootPage()
	{
		SystemLocationResolver.getSystemResolver().registerPage( "system", this );
	}
	
	
	
	public DPWidget getContentsElement()
	{
		VBoxStyleSheet pageBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 40.0, false, 0.0 );
		DPVBox pageBox = new DPVBox( pageBoxStyle );
		
		VBoxStyleSheet headBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 0.0, false, 0.0 );
		DPVBox headBox = new DPVBox( headBoxStyle );
		
		VBoxStyleSheet titleBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 0.0, false, 0.0 );
		DPVBox titleBox = new DPVBox( titleBoxStyle );
		
		StaticTextStyleSheet titleStyle = new StaticTextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPStaticText title = new DPStaticText( titleStyle, "gSym System Page" );
		titleBox.append( title );
		
		headBox.append( SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_ROOTPAGE ) );
		headBox.append( titleBox );
		
		pageBox.append( headBox );
		pageBox.append( createContents() );
		
		return pageBox;
	}

	
	protected DPWidget createContents()
	{
		VBoxStyleSheet contentsBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 0.0, false, 0.0 );
		DPVBox contentsBox = new DPVBox( contentsBoxStyle );
		
		StaticTextStyleSheet titleStyle = new StaticTextStyleSheet( new Font( "Serif", Font.BOLD, 18 ), Color.BLACK );
		DPStaticText title = new DPStaticText( titleStyle, "Tests:" );
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
	
	
	
	public static int LINKHEADER_ROOTPAGE = 0x1;
	public static int LINKHEADER_SYSTEMPAGE = 0x2;
	
	public static DPWidget createLinkHeader(int linkHeaderFlags)
	{
		VBoxStyleSheet linkVBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.RIGHT, 0.0, false, 10.0 );
		HBoxStyleSheet linkHBoxStyle = new HBoxStyleSheet( VAlignment.BASELINES, 0.0, false, 10.0 );
		DPVBox linkVBox = new DPVBox( linkVBoxStyle );
		DPHBox linkHBox = new DPHBox( linkHBoxStyle );
		
		if ( ( linkHeaderFlags & LINKHEADER_ROOTPAGE )  !=  0 )
		{
			linkHBox.append( new DPLink( "GSYM ROOT PAGE", "" ) );
		}
		
		if ( ( linkHeaderFlags & LINKHEADER_SYSTEMPAGE )  !=  0 )
		{
			linkHBox.append( new DPLink( "SYSTEM PAGE", "system" ) );
		}

		linkVBox.append( linkHBox );
		
		return linkVBox;
	}
}
