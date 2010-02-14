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
import BritefuryJ.DocPresent.DPLink;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class SystemRootPage extends Page
{
	protected SystemRootPage()
	{
		SystemLocationResolver.getSystemResolver().registerPage( "system", this );
	}
	
	
	
	public String getTitle()
	{
		return "System page";
	}
	
	public DPWidget getContentsElement()
	{
		VBoxStyleSheet pageBoxStyle = new VBoxStyleSheet( 40.0 );
		DPVBox pageBox = new DPVBox( pageBoxStyle );
		
		DPVBox headBox = new DPVBox( );
		
		StaticTextStyleSheet titleStyle = new StaticTextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPStaticText title = new DPStaticText( titleStyle, "gSym System Page" );
		
		headBox.append( createLinkHeader( SystemRootPage.LINKHEADER_ROOTPAGE ) );
		headBox.append( title.alignHCentre() );
		
		pageBox.append( headBox.alignHExpand() );
		pageBox.append( createContents().alignHExpand() );
		
		return pageBox.alignHExpand();
	}

	
	protected DPWidget createContents()
	{
		DPVBox contentsBox = new DPVBox( );
		
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
		EmptyBorder inner = new EmptyBorder( 5.0, 5.0, 5.0, 5.0, new Color( 184, 206, 203 ) );
		EmptyBorder outer = new EmptyBorder( 5.0, 5.0, 5.0, 5.0 );
		HBoxStyleSheet linkHBoxStyle = new HBoxStyleSheet( 25.0 );
		
		DPBorder innerBorder = new DPBorder( inner );
		DPBorder outerBorder = new DPBorder( outer );
		DPHBox linkHBox = new DPHBox( linkHBoxStyle );
		
		if ( ( linkHeaderFlags & LINKHEADER_ROOTPAGE )  !=  0 )
		{
			linkHBox.append( new DPLink( "GSYM ROOT PAGE", "" ) );
		}
		
		if ( ( linkHeaderFlags & LINKHEADER_SYSTEMPAGE )  !=  0 )
		{
			linkHBox.append( new DPLink( "SYSTEM PAGE", "system" ) );
		}

		innerBorder.setChild( linkHBox.alignHRight() );
		outerBorder.setChild( innerBorder.alignHExpand() );
		
		return outerBorder.alignHExpand();
	}
}
