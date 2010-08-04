//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Combinators.ElementRef;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Proxy;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Combinators.RichText.Heading2;
import BritefuryJ.DocPresent.Combinators.RichText.NormalText;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class HyperlinkTestPage extends SystemPage
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


		public void onLinkClicked(Hyperlink.HyperlinkControl link, PointerButtonEvent event)
		{
			for (DPElement element: parentElement.getElements())
			{
				DPProxy proxy = (DPProxy)element;
				proxy.setChild( newContents.present( parentElement.getContextForElement( element ), parentElement.getStyleForElement( element ) ) );
			}
		}
	}

	

	private static StyleSheet styleSheet = StyleSheet.instance;
	private static StyleSheet blackText = styleSheet.withAttr( Primitive.foreground, Color.black );
	private static StyleSheet redText = styleSheet.withAttr( Primitive.foreground, Color.red );
	private static StyleSheet greenText = styleSheet.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) );


	
	
	private static Pres colouredText(StyleSheet style)
	{
		return style.withAttr( Primitive.editable, false ).applyTo(
				new NormalText( "Change the colour of this text, using the hyperlinks below." ) );
	}
	
	protected Pres createContents()
	{
		ElementRef colouredTextProxyRef = new Proxy( colouredText( blackText ) ).elementRef();
		Hyperlink blackLink = new Hyperlink( "Black", new LinkContentChanger( colouredTextProxyRef, colouredText( blackText ) ) );
		Hyperlink redLink = new Hyperlink( "Red", new LinkContentChanger( colouredTextProxyRef, colouredText( redText ) ) );
		Hyperlink greenLink = new Hyperlink( "Green", new LinkContentChanger( colouredTextProxyRef, colouredText( greenText ) ) );
		Pres colourLinks = styleSheet.withAttr( Primitive.hboxSpacing, 20.0 ).applyTo( new HBox( new Pres[] { blackLink, redLink, greenLink } ) ).padX( 5.0 );
		Pres colourBox = new VBox( new Pres[] { colouredTextProxyRef, colourLinks } );
		
		Hyperlink locationLink = new Hyperlink( "To home page", new Location( "" ) );
		
		return new Body( new Object[] { new Heading2( "Action hyperlinks" ), colourBox, new Heading2( "Location hyperlinks" ), locationLink } );
	}
}
