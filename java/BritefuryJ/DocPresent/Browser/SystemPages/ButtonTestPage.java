//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.Map;

import BritefuryJ.Controls.Button;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.Combinators.ElementRef;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Proxy;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Combinators.RichText.Heading2;
import BritefuryJ.DocPresent.Combinators.RichText.NormalText;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;

public class ButtonTestPage extends SystemPage
{
	protected ButtonTestPage()
	{
		register( "tests.controls.button" );
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


		public void onButtonClicked(Button.ButtonControl button, PointerButtonEvent event)
		{
			for (Map.Entry<DPElement, PresentationContext> entry: parentElement.getElementsAndContexts())
			{
				DPProxy proxy = (DPProxy)entry.getKey();
				proxy.setChild( newContents.present( entry.getValue() ) );
			}
		}
	}

	

	private static StyleSheet2 styleSheet = StyleSheet2.instance;
	private static StyleSheet2 blackText = styleSheet.withAttr( Primitive.foreground, Color.black );
	private static StyleSheet2 redText = styleSheet.withAttr( Primitive.foreground, Color.red );
	private static StyleSheet2 greenText = styleSheet.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) );


	
	private static Pres colouredText(StyleSheet2 style)
	{
		return style.withAttr( Primitive.editable, false ).applyTo(
				new NormalText( "Change the colour of this text, using the buttons below." ) );
	}
	
	protected Pres createContents()
	{
		ElementRef colouredTextProxyRef = new Proxy( colouredText( blackText ) ).elementRef();
		Button blackButton = Button.buttonWithLabel( "Black", new ButtonContentChanger( colouredTextProxyRef, colouredText( blackText ) ) );
		Button redButton = Button.buttonWithLabel( "Red", new ButtonContentChanger( colouredTextProxyRef, colouredText( redText ) ) );
		Button greenButton = Button.buttonWithLabel( "Green", new ButtonContentChanger( colouredTextProxyRef, colouredText( greenText ) ) );
		Pres colourLinks = styleSheet.withAttr( Primitive.hboxSpacing, 20.0 ).applyTo( new HBox( new Pres[] { blackButton, redButton, greenButton } ) ).padX( 5.0 );
		Pres colourBox = new VBox( new Pres[] { colouredTextProxyRef, colourLinks } );
		
		return new Body( new Pres[] { new Heading2( "Action button" ), colourBox } );
	}
}
