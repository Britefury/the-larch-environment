//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.Controls.HPopupMenu;
import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.Controls.MenuItem;
import BritefuryJ.Controls.PopupMenu;
import BritefuryJ.Controls.VPopupMenu;
import BritefuryJ.DocPresent.Combinators.ElementRef;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Proxy;
import BritefuryJ.DocPresent.Combinators.Primitive.Column;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Combinators.RichText.Heading2;
import BritefuryJ.DocPresent.Combinators.RichText.NormalText;
import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class PopupTestPage extends SystemPage
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
	private static StyleSheet blackText = styleSheet.withAttr( Primitive.foreground, Color.black );
	private static StyleSheet redText = styleSheet.withAttr( Primitive.foreground, Color.red );
	private static StyleSheet greenText = styleSheet.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) );
	private static StyleSheet blueText = styleSheet.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.5f ) );
	private static StyleSheet purpleText = styleSheet.withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.5f ) );
	private static StyleSheet cyanText = styleSheet.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.5f ) );

	
	
	protected static Pres colouredText(StyleSheet style)
	{
		return style.withAttr( Primitive.editable, false ).applyTo(
				new NormalText( "Change the colour of this text, using the hyperlinks within the popup activated by the hyperlink below. The last links in the embedded popups will close the popup chain." ) );
	}
	
	protected Pres createContents()
	{
		ElementRef colouredTextProxyRef = new Proxy( colouredText( blackText ) ).elementRef();
		Hyperlink blackLink = new Hyperlink( "Black", new HyperlinkTestPage.LinkContentChanger( colouredTextProxyRef, colouredText( blackText ) ) );
		Hyperlink redLink = new Hyperlink( "Red", new HyperlinkTestPage.LinkContentChanger( colouredTextProxyRef, colouredText( redText ) ) );
		Hyperlink greenLink = new Hyperlink( "Green", new HyperlinkTestPage.LinkContentChanger( colouredTextProxyRef, colouredText( greenText ) ) );
		Hyperlink blueLink = new Hyperlink( "Blue", new HyperlinkTestPage.LinkContentChanger( colouredTextProxyRef, colouredText( blueText ) ) );
		Hyperlink purpleLink = new Hyperlink( "Purple", new HyperlinkTestPage.LinkContentChanger( colouredTextProxyRef, colouredText( purpleText ) ) );
		Hyperlink cyanLink = new Hyperlink( "Cyan", new HyperlinkTestPage.LinkContentChanger( colouredTextProxyRef, colouredText( cyanText ) ) );

		
		PopupMenu menuA = new VPopupMenu( new Pres[] { greenLink, blueLink } );
		PopupMenu menuB = new VPopupMenu( new Pres[] { purpleLink, cyanLink } );
		
		MenuItem menuAItem = MenuItem.menuItemWithLabel( "Submenu A", menuA, MenuItem.SubmenuPopupDirection.DOWN );
		MenuItem menuBItem = MenuItem.menuItemWithLabel( "Submenu B", menuB, MenuItem.SubmenuPopupDirection.DOWN );
		
		final PopupMenu mainMenu = new HPopupMenu( new Pres[] { blackLink, redLink, menuAItem, menuBItem } );
		
		
		Hyperlink.LinkListener popupListener = new Hyperlink.LinkListener()
		{
			public void onLinkClicked(Hyperlink.HyperlinkControl link, PointerButtonClickedEvent event)
			{
				mainMenu.popupToRightOf( link.getElement() );
			}
		};
		
		Hyperlink popupLink = new Hyperlink( "Popup", popupListener );
		Pres colourBox = new Column( new Pres[] { colouredTextProxyRef, popupLink } );
		
		return new Body( new Object[] { new Heading2( "Action hyperlinks" ), colourBox } );
	}
}
