//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPLink;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

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
	

	
	private static final PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;

	
	
	public DPWidget getContentsElement()
	{
		DPWidget title = styleSheet.withFont( new Font( "Serif", Font.BOLD, 32 ) ).staticText( "gSym System page" );
		
		ArrayList<DPWidget> headChildren = new ArrayList<DPWidget>();
		headChildren.add( createLinkHeader( SystemRootPage.LINKHEADER_ROOTPAGE ) );
		headChildren.add( title.alignHCentre() );
		DPVBox headBox = styleSheet.vbox( headChildren );
		
		ArrayList<DPWidget> pageChildren = new ArrayList<DPWidget>();
		pageChildren.add( headBox.alignHExpand() );
		pageChildren.add( createContents().alignHExpand() );
		
		return styleSheet.withVBoxSpacing( 40.0 ).vbox( pageChildren ).alignHExpand();
	}

	
	protected DPWidget createContents()
	{
		ArrayList<DPWidget> contentsBoxChildren = new ArrayList<DPWidget>();
		
		DPWidget title = styleSheet.withFont( new Font( "Serif", Font.BOLD, 18 ) ).staticText( "Tests:" );
		contentsBoxChildren.add( title );
		
		for (SystemPage page: SystemDirectory.getTestPages())
		{
			contentsBoxChildren.add( page.createLink() );
		}
		
		return styleSheet.vbox( contentsBoxChildren );
	}



	protected DPLink createLink(String linkText)
	{
		return styleSheet.link( linkText, "system" );
	}
	
	
	
	public static int LINKHEADER_ROOTPAGE = 0x1;
	public static int LINKHEADER_SYSTEMPAGE = 0x2;
	
	public static DPWidget createLinkHeader(int linkHeaderFlags)
	{
		ArrayList<DPWidget> linkElements = new ArrayList<DPWidget>();
		
		if ( ( linkHeaderFlags & LINKHEADER_ROOTPAGE )  !=  0 )
		{
			linkElements.add( styleSheet.link( "GSYM ROOT PAGE", "" ) );
		}
		
		if ( ( linkHeaderFlags & LINKHEADER_SYSTEMPAGE )  !=  0 )
		{
			linkElements.add( styleSheet.link( "SYSTEM PAGE", "system" ) );
		}

		DPWidget links = styleSheet.withHBoxSpacing( 25.0 ).hbox( linkElements );
		
		DPWidget linksBackground = styleSheet.withBorder( new EmptyBorder( 5.0, 5.0, 5.0, 5.0, new Color( 184, 206, 203 ) ) ).border( links.alignHRight() );
		DPWidget linksHeader = styleSheet.withBorder( new EmptyBorder( 5.0, 5.0, 5.0, 5.0 ) ).border( linksBackground.alignHExpand() );
		
		return linksHeader.alignHExpand();
	}
}
