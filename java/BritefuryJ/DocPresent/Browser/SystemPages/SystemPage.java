//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Font;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Browser.Page;
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
	
	protected String getSystemLocation()
	{
		return systemLocation;
	}
	
	protected String getLocation()
	{
		return SystemLocationResolver.systemLocationToLocation( systemLocation );
	}
	
	

	private static final PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static final ControlsStyleSheet controlsStyleSheet = ControlsStyleSheet.instance;

	
	
	public DPWidget getContentsElement()
	{
		
		DPWidget title = styleSheet.withFont( new Font( "Serif", Font.BOLD, 32 ) ).withTextSmallCaps( true ).staticText( "System page: " + getTitle() );
		
		ArrayList<DPWidget> headChildren = new ArrayList<DPWidget>();
		headChildren.add( SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_ROOTPAGE | SystemRootPage.LINKHEADER_SYSTEMPAGE ) );
		headChildren.add( title.alignHCentre() );
		
		ArrayList<DPWidget> pageChildren = new ArrayList<DPWidget>();
		pageChildren.add( styleSheet.vbox( headChildren ).alignHExpand() );
		String description = getDescription();
		if ( description != null )
		{
			pageChildren.add( createTextParagraph( styleSheet.withFont( new Font( "Sans Serif", Font.PLAIN, 16 ) ), description ) );
		}
		pageChildren.add( createContents().alignHExpand() );
		
		return styleSheet.withVBoxSpacing( 40.0 ).vbox( pageChildren );
	}


	protected DPWidget createLink()
	{
		return controlsStyleSheet.link( getTitle(), getLocation() ).getElement();
	}
	
	
	protected ArrayList<DPWidget> createTextNodes(PrimitiveStyleSheet style, String text)
	{
		String[] words = text.split( " " );
		ArrayList<DPWidget> nodes = new ArrayList<DPWidget>();
		boolean bFirst = true;
		for (String word: words)
		{
			if ( !word.equals( "" ) )
			{
				if ( !bFirst )
				{
					nodes.add( style.lineBreak( style.staticText( " " ) ) );
				}
				nodes.add( style.staticText( word ) );
				bFirst = false;
			}
		}
		
		return nodes;
	}

	protected ArrayList<DPWidget> createTextNodes(String text)
	{
		return createTextNodes( styleSheet, text );
	}
	
	protected DPParagraph createTextParagraph(PrimitiveStyleSheet style, String text)
	{
		return style.paragraph( createTextNodes( style, text ) );
	}

	protected DPParagraph createTextParagraph(String text)
	{
		return createTextParagraph( styleSheet, text );
	}


	
	protected String getDescription()
	{
		return null;
	}
	
	protected abstract DPWidget createContents();
}
