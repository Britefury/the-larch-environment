//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.util.ArrayList;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public abstract class SystemPage extends Page
{
	protected String systemLocation;
	
	
	protected void register(String systemLocation) 
	{
		this.systemLocation = systemLocation;
		SystemLocationResolver.getSystemResolver().registerPage( systemLocation, this );
	}
	
	protected Location getLocation()
	{
		return new Location( SystemLocationResolver.systemLocationToLocation( systemLocation ) );
	}
	
	

	private static final PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static final ControlsStyleSheet controlsStyleSheet = ControlsStyleSheet.instance;

	
	
	public DPElement getContentsElement()
	{
		
		DPElement title = styleSheet.withFontFace( "Serif" ).withFontBold( true ).withFontSize( 32 ).withTextSmallCaps( true ).staticText( "System page: " + getTitle() );
		
		ArrayList<DPElement> headChildren = new ArrayList<DPElement>();
		headChildren.add( SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_ROOTPAGE | SystemRootPage.LINKHEADER_SYSTEMPAGE ) );
		headChildren.add( title.alignHCentre() );
		
		ArrayList<DPElement> pageChildren = new ArrayList<DPElement>();
		pageChildren.add( styleSheet.vbox( headChildren.toArray( new DPElement[0] ) ).alignHExpand() );
		String description = getDescription();
		if ( description != null )
		{
			pageChildren.add( createTextParagraph( styleSheet.withFontSize( 16 ), description ) );
		}
		pageChildren.add( createContents().alignHExpand().present() );
		
		return styleSheet.withVBoxSpacing( 40.0 ).vbox( pageChildren.toArray( new DPElement[0] ) ).alignHExpand();
	}


	protected DPElement createLink()
	{
		return controlsStyleSheet.link( getTitle(), getLocation() ).getElement();
	}
	
	
	protected ArrayList<DPElement> createTextNodes(PrimitiveStyleSheet style, String text)
	{
		String[] words = text.split( " " );
		ArrayList<DPElement> nodes = new ArrayList<DPElement>();
		boolean bFirst = true;
		for (String word: words)
		{
			if ( !word.equals( "" ) )
			{
				if ( !bFirst )
				{
					nodes.add( style.staticText( " " ) );
					nodes.add( style.lineBreak() );
				}
				nodes.add( style.staticText( word ) );
				bFirst = false;
			}
		}
		
		return nodes;
	}

	protected ArrayList<DPElement> createTextNodes(String text)
	{
		return createTextNodes( styleSheet, text );
	}
	
	protected DPParagraph createTextParagraph(PrimitiveStyleSheet style, String text)
	{
		return style.paragraph( createTextNodes( style, text ).toArray( new DPElement[0] ) );
	}

	protected DPParagraph createTextParagraph(String text)
	{
		return createTextParagraph( styleSheet, text );
	}


	
	protected String getDescription()
	{
		return null;
	}
	
	protected abstract Pres createContents();
}
