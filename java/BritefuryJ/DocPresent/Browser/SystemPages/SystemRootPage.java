//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.util.ArrayList;

import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Browser.BrowserPage;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.RichText.Head;
import BritefuryJ.Pres.RichText.LinkHeaderBar;
import BritefuryJ.Pres.RichText.Page;
import BritefuryJ.Pres.RichText.TitleBar;

public class SystemRootPage extends BrowserPage
{
	public static final TestsDirectory tests = new TestsDirectory();
	
	
	public SystemRootPage()
	{
	}
	
	
	
	public String getTitle()
	{
		return "System page";
	}
	

	
	public Pres getContentsPres()
	{
		Pres title = new TitleBar( "System Page" );
		
		Pres head = new Head( new Pres[] { createLinkHeader( SystemRootPage.LINKHEADER_ROOTPAGE ), title } );
		
		return new Page( new Pres[] { head, tests.createContents() } );
	}

	
	public static int LINKHEADER_ROOTPAGE = 0x1;
	public static int LINKHEADER_SYSTEMPAGE = 0x2;
	
	public static Pres createLinkHeader(int linkHeaderFlags)
	{
		ArrayList<Object> links = new ArrayList<Object>();
		
		if ( ( linkHeaderFlags & LINKHEADER_ROOTPAGE )  !=  0 )
		{
			links.add( new Hyperlink( "HOME PAGE", new Location( "" ) ) );
		}
		
		if ( ( linkHeaderFlags & LINKHEADER_SYSTEMPAGE )  !=  0 )
		{
			links.add( new Hyperlink( "SYSTEM PAGE", new Location( "system" ) ) );
		}
		
		return new LinkHeaderBar( links );
	}
}
