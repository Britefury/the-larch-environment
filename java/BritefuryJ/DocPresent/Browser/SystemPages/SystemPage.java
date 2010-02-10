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

import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.DPLink;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementContext;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

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
	
	
	protected ElementContext getContext()
	{
		return null;
	}



	public DPWidget getContentsElement()
	{
		VBoxStyleSheet pageBoxStyle = new VBoxStyleSheet( 40.0 );
		DPVBox pageBox = new DPVBox( getContext(), pageBoxStyle );
		
		DPVBox headBox = new DPVBox( getContext() );
		
		StaticTextStyleSheet descriptionStyle = new StaticTextStyleSheet( new Font( "Sans Serif", Font.PLAIN, 16 ), Color.BLACK );
		
		StaticTextStyleSheet titleStyle = new StaticTextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPStaticText title = new DPStaticText( getContext(), titleStyle, "System page: " + getTitle() );
		
		headBox.append( SystemRootPage.createLinkHeader( getContext(), SystemRootPage.LINKHEADER_ROOTPAGE | SystemRootPage.LINKHEADER_SYSTEMPAGE ) );
		headBox.append( title.alignHCentre() );

		pageBox.append( headBox.alignHExpand() );
		String description = getDescription();
		if ( description != null )
		{
			pageBox.append( createTextParagraph( descriptionStyle, description ) );
		}
		pageBox.append( createContents().alignHExpand() );
		
		return pageBox.alignHExpand();
	}


	protected DPLink createLink()
	{
		return new DPLink( getContext(), getTitle(), getLocation() );
	}
	
	
	protected ArrayList<DPWidget> createTextNodes(StaticTextStyleSheet textStyle, String text)
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
					DPStaticText space = new DPStaticText( getContext(), textStyle, " " );
					DPLineBreak b = new DPLineBreak( getContext() );
					b.setChild( space );
					nodes.add( b );
				}
				nodes.add( new DPStaticText( getContext(), textStyle, word ) );
				bFirst = false;
			}
		}
		
		return nodes;
	}

	protected ArrayList<DPWidget> createTextNodes(String text)
	{
		return createTextNodes( StaticTextStyleSheet.defaultStyleSheet, text );
	}
	
	protected DPParagraph createTextParagraph(ParagraphStyleSheet paraStyle, StaticTextStyleSheet textStyle, String text)
	{
		ArrayList<DPWidget> nodes = createTextNodes( textStyle, text );
		DPParagraph para = new DPParagraph( getContext(), paraStyle );
		para.setChildren( nodes );
		return para;
	}

	protected DPParagraph createTextParagraph(StaticTextStyleSheet textStyle, String text)
	{
		return createTextParagraph( ParagraphStyleSheet.defaultStyleSheet, textStyle, text );
	}

	protected DPParagraph createTextParagraph(String text)
	{
		return createTextParagraph( ParagraphStyleSheet.defaultStyleSheet, StaticTextStyleSheet.defaultStyleSheet, text );
	}

	
	protected String getDescription()
	{
		return null;
	}
	
	protected abstract DPWidget createContents();
}
