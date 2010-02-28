//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.StyleSheet.ControlsStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class HyperlinkTestPage extends SystemPage
{
	protected HyperlinkTestPage()
	{
		register( "tests.controls.hyperlink" );
	}
	
	
	public String getTitle()
	{
		return "Hyperlink test";
	}
	
	protected String getDescription()
	{
		return "Hyperlink element: used to take the browser to a different location, or perform an action";
	}
	
	
	private class LinkColourChanger extends ControlsStyleSheet.LinkListener
	{
		private DPProxy parentElement;
		private PrimitiveStyleSheet style;
		
		
		public LinkColourChanger(DPProxy parentElement, PrimitiveStyleSheet style)
		{
			this.parentElement = parentElement;
			this.style = style;
		}


		protected boolean onLinkClicked(DPWidget element, PointerButtonEvent event)
		{
			parentElement.setChild( colouredText( style ) );
			return true;
		}
		
	}

	

	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet headingStyleSheet = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 18) );
	private static PrimitiveStyleSheet blackText = styleSheet.withForeground( Color.black );
	private static PrimitiveStyleSheet redText = styleSheet.withForeground( Color.red );
	private static PrimitiveStyleSheet greenText = styleSheet.withForeground( new Color( 0.0f, 0.5f, 0.0f ) );

	private static ControlsStyleSheet controlsStyleSheet = ControlsStyleSheet.instance;

	
	
	protected DPWidget section(String title, DPWidget contents)
	{
		DPWidget heading = headingStyleSheet.staticText( title );
		
		return styleSheet.vbox( Arrays.asList( new DPWidget[] { heading.padY( 10.0 ), contents } ) );
	}
	
	protected DPWidget colouredText(PrimitiveStyleSheet style)
	{
		return style.staticText( "Change the colour of this text using the hyperlinks below." );
	}
	
	protected DPWidget createContents()
	{
		DPProxy colouredTextProxy = styleSheet.proxy( colouredText( blackText ) );
		DPWidget blackLink = controlsStyleSheet.link( "Black", new LinkColourChanger( colouredTextProxy, blackText ) );
		DPWidget redLink = controlsStyleSheet.link( "Red", new LinkColourChanger( colouredTextProxy, redText ) );
		DPWidget greenLink = controlsStyleSheet.link( "Green", new LinkColourChanger( colouredTextProxy, greenText ) );
		DPWidget colourLinks = styleSheet.withHBoxSpacing( 20.0 ).hbox( Arrays.asList( new DPWidget[] { blackLink, redLink, greenLink } ) ).padX( 5.0 );
		DPWidget colourBox = styleSheet.vbox( Arrays.asList( new DPWidget[] { colouredTextProxy, colourLinks } ) );
		DPWidget colourSection = section( "Action hyperlinks", colourBox );
		
		DPWidget locationLink = controlsStyleSheet.link( "To system page", SystemRootPage.getLocation() );
		DPWidget locationSection = section( "Location hyperlinks", locationLink );
		
		return styleSheet.withVBoxSpacing( 30.0 ).vbox( Arrays.asList( new DPWidget[] { colourSection, locationSection } ) );
	}
}
