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
import java.util.List;

import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class SystemRootPage extends Page
{
	public static String getLocation()
	{
		return "system";
	}
	
	
	protected SystemRootPage()
	{
		SystemLocationResolver.getSystemResolver().registerPage( getLocation(), this );
	}
	
	
	
	public String getTitle()
	{
		return "System page";
	}
	

	
	private static final PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet outlineStyle = styleSheet.withBorder( new SolidBorder( 2.0, 10.0, new Color( 0.6f, 0.7f, 0.8f ), null ) );

	private static final ControlsStyleSheet controlsStyleSheet = ControlsStyleSheet.instance;

	
	
	public DPElement getContentsElement()
	{
		DPElement title = styleSheet.withFont( new Font( "Serif", Font.BOLD, 32 ) ).withTextSmallCaps( true ).staticText( "GSym System Page" );
		
		ArrayList<DPElement> headChildren = new ArrayList<DPElement>();
		headChildren.add( createLinkHeader( SystemRootPage.LINKHEADER_ROOTPAGE ) );
		headChildren.add( title.alignHCentre() );
		DPVBox headBox = styleSheet.vbox( headChildren );
		
		ArrayList<DPElement> pageChildren = new ArrayList<DPElement>();
		pageChildren.add( headBox.alignHExpand() );
		pageChildren.add( createContents().alignHExpand() );
		
		return styleSheet.withVBoxSpacing( 40.0 ).vbox( pageChildren ).alignHExpand();
	}

	
	protected DPElement createTestsBox(String title, List<SystemPage> pages)
	{
		ArrayList<DPElement> testBoxChildren = new ArrayList<DPElement>();
		
		DPElement titleElement = styleSheet.withFont( new Font( "Serif", Font.BOLD, 18 ) ).staticText( title ).pad( 30.0, 30.0, 5.0, 15.0 );
		testBoxChildren.add( titleElement );
		
		for (SystemPage page: pages)
		{
			testBoxChildren.add( page.createLink() );
		}
		
		return outlineStyle.border( styleSheet.vbox( testBoxChildren ) );
	}
	
	protected DPElement createContents()
	{
		DPElement titleElement = styleSheet.withFont( new Font( "Serif", Font.BOLD, 24 ) ).staticText( "Tests:" ).pad( 0.0, 5.0 ).alignHCentre();
		
		ArrayList<DPElement> testBoxes = new ArrayList<DPElement>();
		testBoxes.add( createTestsBox( "Primitive elements:", SystemDirectory.getPrimitiveTestPages() ).pad( 25.0, 5.0 ) );
		testBoxes.add( createTestsBox( "Controls:", SystemDirectory.getControlTestPages() ).pad( 25.0, 5.0 ) );

		ArrayList<DPElement> contents = new ArrayList<DPElement>();
		contents.add( titleElement );
		contents.add( styleSheet.hbox( testBoxes ) );
		
		return styleSheet.vbox( contents ).alignHExpand();
	}



	public static int LINKHEADER_ROOTPAGE = 0x1;
	public static int LINKHEADER_SYSTEMPAGE = 0x2;
	
	public static DPElement createLinkHeader(int linkHeaderFlags)
	{
		ArrayList<DPElement> linkElements = new ArrayList<DPElement>();
		
		if ( ( linkHeaderFlags & LINKHEADER_ROOTPAGE )  !=  0 )
		{
			linkElements.add( controlsStyleSheet.link( "GSYM ROOT PAGE", "" ).getElement() );
		}
		
		if ( ( linkHeaderFlags & LINKHEADER_SYSTEMPAGE )  !=  0 )
		{
			linkElements.add( controlsStyleSheet.link( "SYSTEM PAGE", "system" ).getElement() );
		}

		DPElement links = styleSheet.withHBoxSpacing( 25.0 ).hbox( linkElements );
		
		DPElement linksBackground = styleSheet.withBorder( new FilledBorder( 5.0, 5.0, 5.0, 5.0, new Color( 184, 206, 203 ) ) ).border( links.alignHRight() );
		DPElement linksHeader = styleSheet.withBorder( new FilledBorder( 5.0, 5.0, 5.0, 5.0 ) ).border( linksBackground.alignHExpand() );
		
		return linksHeader.alignHExpand();
	}
}
