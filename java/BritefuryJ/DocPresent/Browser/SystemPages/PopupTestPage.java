//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.Controls.Hyperlink;
import BritefuryJ.DocPresent.Controls.MenuItem;
import BritefuryJ.DocPresent.Controls.PopupMenu;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.RichTextStyleSheet;

public class PopupTestPage extends SystemPage
{
	protected PopupTestPage()
	{
		register( "tests.controls.popup" );
	}
	
	
	public String getTitle()
	{
		return "Popup test";
	}
	
	protected String getDescription()
	{
		return "Popup windows are used to display additional presented content, in a detached window.";
	}
	
	
	private class LinkColourChanger implements Hyperlink.LinkListener
	{
		private DPProxy parentElement;
		private PrimitiveStyleSheet style;
		
		
		public LinkColourChanger(DPProxy parentElement, PrimitiveStyleSheet style)
		{
			this.parentElement = parentElement;
			this.style = style;
		}


		public void onLinkClicked(Hyperlink link, PointerButtonEvent event)
		{
			parentElement.setChild( colouredText( style ) );
		}
	}

	

	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet headingStyleSheet = styleSheet.withFontSize( 18 );
	private static PrimitiveStyleSheet blackText = styleSheet.withForeground( Color.black );
	private static PrimitiveStyleSheet redText = styleSheet.withForeground( Color.red );
	private static PrimitiveStyleSheet greenText = styleSheet.withForeground( new Color( 0.0f, 0.5f, 0.0f ) );
	private static PrimitiveStyleSheet blueText = styleSheet.withForeground( new Color( 0.0f, 0.0f, 0.5f ) );
	private static PrimitiveStyleSheet purpleText = styleSheet.withForeground( new Color( 0.5f, 0.0f, 0.5f ) );
	private static PrimitiveStyleSheet cyanText = styleSheet.withForeground( new Color( 0.0f, 0.5f, 0.5f ) );

	private static ControlsStyleSheet controlsStyleSheet = ControlsStyleSheet.instance;
	private static ControlsStyleSheet popupCloseControlsStyleSheet = ControlsStyleSheet.instance.withClosePopupOnActivate();

	
	
	protected DPElement section(String title, DPElement contents)
	{
		DPElement heading = headingStyleSheet.staticText( title );
		
		return styleSheet.vbox( new DPElement[] { heading.padY( 10.0 ), contents } );
	}
	
	protected static DPElement colouredText(PrimitiveStyleSheet style)
	{
		RichTextStyleSheet textStyle = RichTextStyleSheet.instance.withNonEditable().withPrimitiveStyleSheet( style );
		return textStyle.paragraph( "Change the colour of this text, using the hyperlinks within the popup activated by the hyperlink below. The last links in the embedded popups will close the popup chain." );
	}
	
	protected DPElement createContents()
	{
		DPProxy colouredTextProxy = styleSheet.proxy( colouredText( blackText ) );
		Hyperlink blackLink = controlsStyleSheet.link( "Black", new LinkColourChanger( colouredTextProxy, blackText ) );
		Hyperlink redLink = controlsStyleSheet.link( "Red", new LinkColourChanger( colouredTextProxy, redText ) );
		Hyperlink greenLink = controlsStyleSheet.link( "Green", new LinkColourChanger( colouredTextProxy, greenText ) );
		Hyperlink blueLink = popupCloseControlsStyleSheet.link( "Blue", new LinkColourChanger( colouredTextProxy, blueText ) );
		Hyperlink purpleLink = controlsStyleSheet.link( "Purple", new LinkColourChanger( colouredTextProxy, purpleText ) );
		Hyperlink cyanLink = popupCloseControlsStyleSheet.link( "Cyan", new LinkColourChanger( colouredTextProxy, cyanText ) );

		
		PopupMenu menuA = controlsStyleSheet.vpopupMenu( new DPElement[] { greenLink.getElement(), blueLink.getElement() } );
		PopupMenu menuB = controlsStyleSheet.vpopupMenu( new DPElement[] { purpleLink.getElement(), cyanLink.getElement() } );
		
		MenuItem menuAItem = controlsStyleSheet.subMenuItemDownWithLabel( "Submenu A", menuA );
		MenuItem menuBItem = controlsStyleSheet.subMenuItemDownWithLabel( "Submenu B", menuB );
		
		final PopupMenu mainMenu = controlsStyleSheet.hpopupMenu( new DPElement[] { blackLink.getElement(), redLink.getElement(),
				menuAItem.getElement(), menuBItem.getElement() } );
		
		
		Hyperlink.LinkListener popupListener = new Hyperlink.LinkListener()
		{
			public void onLinkClicked(Hyperlink link, PointerButtonEvent event)
			{
				mainMenu.popupToRightOf( link.getElement() );
			}
		};
		
		Hyperlink popupLink = controlsStyleSheet.link( "Popup", popupListener );
		DPElement colourBox = styleSheet.vbox( new DPElement[] { colouredTextProxy, popupLink.getElement() } );
		DPElement colourSection = section( "Action hyperlinks", colourBox );
		
		Hyperlink locationLink = controlsStyleSheet.link( "To system page", SystemRootPage.getLocation() );
		DPElement locationSection = section( "Location hyperlinks", locationLink.getElement() );
		
		return styleSheet.withVBoxSpacing( 30.0 ).vbox( new DPElement[] { colourSection, locationSection } );
	}
}
