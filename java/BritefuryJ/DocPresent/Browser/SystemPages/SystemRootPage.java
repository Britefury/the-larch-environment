//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Combinators.RichText.Head;
import BritefuryJ.DocPresent.Combinators.RichText.Heading2;
import BritefuryJ.DocPresent.Combinators.RichText.Heading4;
import BritefuryJ.DocPresent.Combinators.RichText.LinkHeaderBar;
import BritefuryJ.DocPresent.Combinators.RichText.TitleBar;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;

public class SystemRootPage extends Page
{
	public static Location getLocation()
	{
		return new Location( "system" );
	}
	
	
	protected SystemRootPage()
	{
		SystemLocationResolver.getSystemResolver().registerPage( getLocation().getLocationString(), this );
	}
	
	
	
	public String getTitle()
	{
		return "System page";
	}
	

	
	private static StyleSheet2 outlineStyle = StyleSheet2.instance.withAttr( Primitive.border, new SolidBorder( 2.0, 10.0, new Color( 0.6f, 0.7f, 0.8f ), null ) );

	
	
	public DPElement getContentsElement()
	{
		Pres title = new TitleBar( "GSym System Page" );
		
		Pres head = new Head( new Pres[] { createLinkHeader( SystemRootPage.LINKHEADER_ROOTPAGE ), title } );
		
		return new BritefuryJ.DocPresent.Combinators.RichText.Page( new Pres[] { head, createContents() } ).present();
	}

	
	protected Pres createTestsBox(String title, List<SystemPage> pages)
	{
		ArrayList<Object> testBoxChildren = new ArrayList<Object>();
		
		Pres heading = new Heading4( title ).pad( 30.0, 30.0, 5.0, 15.0 );
		testBoxChildren.add( heading );
		
		for (SystemPage page: pages)
		{
			testBoxChildren.add( page.createLink() );
		}
		
		return outlineStyle.applyTo( new Border( new VBox( testBoxChildren ) ) );
	}
	
	protected Pres createContents()
	{
		Pres heading = new Heading2( "Tests" ).alignHCentre();
		
		ArrayList<Object> testBoxes = new ArrayList<Object>();
		testBoxes.add( createTestsBox( "Primitive elements:", SystemDirectory.getPrimitiveTestPages() ).pad( 25.0, 5.0 ) );
		testBoxes.add( createTestsBox( "Controls:", SystemDirectory.getControlTestPages() ).pad( 25.0, 5.0 ) );

		return new Body( new Pres[] { heading, new HBox( testBoxes ) } );
	}



	public static int LINKHEADER_ROOTPAGE = 0x1;
	public static int LINKHEADER_SYSTEMPAGE = 0x2;
	
	public static Pres createLinkHeader(int linkHeaderFlags)
	{
		ArrayList<Object> links = new ArrayList<Object>();
		
		if ( ( linkHeaderFlags & LINKHEADER_ROOTPAGE )  !=  0 )
		{
			links.add( new Hyperlink( "GSYM ROOT PAGE", new Location( "" ) ) );
		}
		
		if ( ( linkHeaderFlags & LINKHEADER_SYSTEMPAGE )  !=  0 )
		{
			links.add( new Hyperlink( "SYSTEM PAGE", new Location( "system" ) ) );
		}
		
		return new LinkHeaderBar( links );
	}
}
