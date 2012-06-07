//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;

import BritefuryJ.Browser.Location;
import BritefuryJ.Controls.AbstractHyperlink;
import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSProxy;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.Pres.ElementRef;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.StyleSheet.StyleSheet;

public class HyperlinkTestPage extends TestPage
{
	protected HyperlinkTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Hyperlink test";
	}
	
	protected String getDescription()
	{
		return "Hyperlink control: used to take the browser to a different location, or perform an action";
	}
	
	
	protected static class LinkContentChanger implements Hyperlink.LinkListener
	{
		private ElementRef parentElement;
		private Pres newContents;
		
		
		public LinkContentChanger(ElementRef parentElement, Pres newContents)
		{
			this.parentElement = parentElement;
			this.newContents = newContents;
		}


		public void onLinkClicked(Hyperlink.AbstractHyperlinkControl link, PointerButtonClickedEvent event)
		{
			for (LSElement element: parentElement.getElements())
			{
				LSProxy proxy = (LSProxy)element;
				proxy.setChild( newContents.present( parentElement.getContextForElement( element ), parentElement.getStyleForElement( element ) ) );
			}
		}
	}

	

	private static StyleSheet styleSheet = StyleSheet.instance;
	private static StyleSheet blackText = styleSheet.withValues( Primitive.foreground.as( Color.black ) );
	private static StyleSheet redText = styleSheet.withValues( Primitive.foreground.as( Color.red ) );
	private static StyleSheet greenText = styleSheet.withValues( Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.0f ) ) );


	
	
	private static Pres colouredText(StyleSheet style)
	{
		return style.withValues( Primitive.editable.as( false ) ).applyTo(
				new NormalText( "Change the colour of this text, using the hyperlinks below." ) );
	}
	
	protected Pres createContents()
	{
		ElementRef colouredTextProxyRef = new Proxy( colouredText( blackText ) ).elementRef();
		AbstractHyperlink blackLink = new Hyperlink( "Black", new LinkContentChanger( colouredTextProxyRef, colouredText( blackText ) ) );
		AbstractHyperlink redLink = new Hyperlink( "Red", new LinkContentChanger( colouredTextProxyRef, colouredText( redText ) ) );
		AbstractHyperlink greenLink = new Hyperlink( "Green", new LinkContentChanger( colouredTextProxyRef, colouredText( greenText ) ) );
		Pres colourLinks = styleSheet.withValues( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Pres[] { blackLink, redLink, greenLink } ) ).padX( 5.0 );
		Pres colourBox = new Column( new Pres[] { colouredTextProxyRef, colourLinks } );
		
		AbstractHyperlink locationLink = new Hyperlink( "To home page", new Location( "" ) );
		
		return new Body( new Object[] { new Heading2( "Action hyperlinks" ), colourBox, new Heading2( "Location hyperlinks" ), locationLink } );
	}
}
