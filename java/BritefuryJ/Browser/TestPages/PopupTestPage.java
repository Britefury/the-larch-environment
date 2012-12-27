//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;

import BritefuryJ.Controls.AbstractHyperlink;
import BritefuryJ.Controls.HPopupMenu;
import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.Controls.MenuItem;
import BritefuryJ.Controls.PopupMenu;
import BritefuryJ.Controls.VPopupMenu;
import BritefuryJ.LSpace.Anchor;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.Pres.ElementRef;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.Pres.UI.BubblePopup;
import BritefuryJ.StyleSheet.StyleSheet;

public class PopupTestPage extends TestPage
{
	protected PopupTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Popup test";
	}
	
	protected String getDescription()
	{
		return "Popup windows are used to display additional presented content, in a detached window.";
	}
	
	

	private static StyleSheet styleSheet = StyleSheet.instance;
	private static StyleSheet blackText = styleSheet.withValues( Primitive.foreground.as( Color.black ) );
	private static StyleSheet redText = styleSheet.withValues( Primitive.foreground.as( Color.red ) );
	private static StyleSheet greenText = styleSheet.withValues( Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.0f ) ) );
	private static StyleSheet blueText = styleSheet.withValues( Primitive.foreground.as( new Color( 0.0f, 0.0f, 0.5f ) ) );
	private static StyleSheet purpleText = styleSheet.withValues( Primitive.foreground.as( new Color( 0.5f, 0.0f, 0.5f ) ) );
	private static StyleSheet cyanText = styleSheet.withValues( Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.5f ) ) );

	
	
	protected static Pres colouredText(StyleSheet style)
	{
		return style.withValues( Primitive.editable.as( false ) ).applyTo(
				new NormalText( "Change the colour of this text, using the hyperlinks within the popup activated by the hyperlink below. The last links in the embedded popups will close the popup chain." ) );
	}
	
	protected Pres createContents()
	{
		ElementRef colouredTextProxyRef = new Proxy( colouredText( blackText ) ).elementRef();
		AbstractHyperlink blackLink = new Hyperlink( "Black", new HyperlinkTestPage.LinkContentChanger( colouredTextProxyRef, colouredText( blackText ) ) );
		AbstractHyperlink redLink = new Hyperlink( "Red", new HyperlinkTestPage.LinkContentChanger( colouredTextProxyRef, colouredText( redText ) ) );
		AbstractHyperlink greenLink = new Hyperlink( "Green", new HyperlinkTestPage.LinkContentChanger( colouredTextProxyRef, colouredText( greenText ) ) );
		AbstractHyperlink blueLink = new Hyperlink( "Blue", new HyperlinkTestPage.LinkContentChanger( colouredTextProxyRef, colouredText( blueText ) ) );
		AbstractHyperlink purpleLink = new Hyperlink( "Purple", new HyperlinkTestPage.LinkContentChanger( colouredTextProxyRef, colouredText( purpleText ) ) );
		AbstractHyperlink cyanLink = new Hyperlink( "Cyan", new HyperlinkTestPage.LinkContentChanger( colouredTextProxyRef, colouredText( cyanText ) ) );

		
		PopupMenu menuA = new VPopupMenu( new Pres[] { greenLink, blueLink } );
		PopupMenu menuB = new VPopupMenu( new Pres[] { purpleLink, cyanLink } );
		
		MenuItem menuAItem = MenuItem.menuItemWithLabel( "Submenu A", menuA, MenuItem.SubmenuPopupDirection.DOWN );
		MenuItem menuBItem = MenuItem.menuItemWithLabel( "Submenu B", menuB, MenuItem.SubmenuPopupDirection.DOWN );
		
		final PopupMenu mainMenu = new HPopupMenu( new Pres[] { blackLink, redLink, menuAItem, menuBItem } );
		
		
		Hyperlink.LinkListener popupListener = new Hyperlink.LinkListener()
		{
			public void onLinkClicked(Hyperlink.AbstractHyperlinkControl link, PointerButtonClickedEvent event)
			{
				mainMenu.popupMenu( link.getElement(), Anchor.TOP_RIGHT, Anchor.TOP_LEFT );
			}
		};
		
		final AbstractHyperlink popupLink = new Hyperlink( "Popup", popupListener );
		
		Hyperlink.LinkListener showBubblePopupListener = new Hyperlink.LinkListener()
		{
			public void onLinkClicked(Hyperlink.AbstractHyperlinkControl link, PointerButtonClickedEvent event)
			{
				BubblePopup.popupInBubbleAdjacentTo( popupLink, link.getElement(), Anchor.BOTTOM, true, true );
			}
		};
		
		AbstractHyperlink displayBubblePopup = new Hyperlink( "Display in nested bubble popup", showBubblePopupListener );
		
		Pres colourBox = new Column( new Pres[] { colouredTextProxyRef, popupLink } );
		
		return new Body( new Object[] { new Heading2( "Action hyperlinks" ), colourBox, displayBubblePopup } );
	}
}
