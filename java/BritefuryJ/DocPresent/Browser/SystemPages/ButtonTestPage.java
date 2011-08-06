//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.Controls.Button;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
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

public class ButtonTestPage extends SystemPage
{
	protected ButtonTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Button test";
	}
	
	protected String getDescription()
	{
		return "Button element: performs an action when clicked";
	}
	
	
	protected static class ButtonContentChanger implements Button.ButtonListener
	{
		private ElementRef parentElement;
		private Pres newContents;
		
		
		public ButtonContentChanger(ElementRef parentElement, Pres newContents)
		{
			this.parentElement = parentElement;
			this.newContents = newContents;
		}


		public void onButtonClicked(Button.ButtonControl button, PointerButtonClickedEvent event)
		{
			for (DPElement element: parentElement.getElements())
			{
				DPProxy proxy = (DPProxy)element;
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
				new NormalText( "Change the colour of this text, using the buttons below." ) );
	}
	
	protected Pres createContents()
	{
		ElementRef colouredTextProxyRef = new Proxy( colouredText( blackText ) ).elementRef();
		Button blackButton = Button.buttonWithLabel( "Black", new ButtonContentChanger( colouredTextProxyRef, colouredText( blackText ) ) );
		Button redButton = Button.buttonWithLabel( "Red", new ButtonContentChanger( colouredTextProxyRef, colouredText( redText ) ) );
		Button greenButton = Button.buttonWithLabel( "Green", new ButtonContentChanger( colouredTextProxyRef, colouredText( greenText ) ) );
		Pres colourLinks = styleSheet.withValues( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Pres[] { blackButton, redButton, greenButton } ) ).padX( 5.0 );
		Pres colourBox = new Column( new Pres[] { colouredTextProxyRef, colourLinks } );
		
		return new Body( new Pres[] { new Heading2( "Action button" ), colourBox } );
	}
}
